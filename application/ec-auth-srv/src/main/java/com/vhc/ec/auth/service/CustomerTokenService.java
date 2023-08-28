package com.vhc.ec.auth.service;

import com.vhc.ec.auth.component.GlobalValue;
import com.vhc.ec.auth.dto.CustomerDto;
import com.vhc.ec.auth.dto.UserViewDto;
import com.vhc.ec.auth.dto.ValidateTokenResponseDto;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.pem.PemReader;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerTokenService {

    /**
     * Khởi tạo đối tượng lưu trữ thông tin khoá mã hoá token
     */
    private static final RSAPrivateKey privateKey;
    private static final RSAPublicKey publicKey;

    static {
        RSAPublicKey rsaPublicKey = null;
        RSAPrivateKey rsaPrivateKey = null;

        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            try (var fileReader = new FileReader(GlobalValue.SECRECT_KEY_DIRECTORY + "/publickey.crt");
                 var pemReader = new PemReader(fileReader)) {

                var pemObject = pemReader.readPemObject();
                byte[] content = pemObject.getContent();
                var pubKeySpec = new X509EncodedKeySpec(content);

                rsaPublicKey = (RSAPublicKey) factory.generatePublic(pubKeySpec);
            }

            try (var fileReader = new FileReader(GlobalValue.SECRECT_KEY_DIRECTORY + "/privatekey.key");
                 var pemReader = new PemReader(fileReader)) {

                var pemObject = pemReader.readPemObject();
                byte[] content = pemObject.getContent();
                var privKeySpec = new PKCS8EncodedKeySpec(content);

                rsaPrivateKey = (RSAPrivateKey) factory.generatePrivate(privKeySpec);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("can't get private and public key.", e);
        } finally {
            privateKey = rsaPrivateKey;
            publicKey = rsaPublicKey;
        }
    }

    private final CustomerService customerService;
    private final ContractService contractService;
    private final AdminUserService userService;
    private final ModelMapper mapper;

    @Value("${vhc.ec.jwt.expires}")
    private int expires; // minutes

    /**
     * Tạo mới token đăng nhập cho khách hàng sử dụng hệ thống,
     * cả khách hàng định danh và không định danh
     *
     * @param claims Thông tin đăng nhập sử dụng để xây dựng token
     * @return Token được sinh ra từ thông tin của khách hàng
     */
    public String generateToken(ClaimSubject subject, Map<String, Object> claims) {
        // Tính toán thời gian hết hạn của token
        var currentTime = LocalDateTime.now();
        currentTime = currentTime.plus(expires, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject.name())
                .setExpiration(
                        Date.from(
                                currentTime.atZone(ZoneId.systemDefault())
                                        .toInstant()
                        )
                )
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Kiểm tra lại thông tin của token khi khách hàng sử dụng token để đăng nhập
     *
     * @param jws Token của khách hàng gửi lên
     * @return Thông tin chi tiết của khách hàng
     */
    public ValidateTokenResponseDto validate(String jws) {
        ValidateTokenResponseDto validateTokenResponseDto = ValidateTokenResponseDto.builder()
                .success(false).customer(null).build();
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(jws);

            // parser claims
            Claims claims = claimsJws.getBody();

            // parse subject
            ClaimSubject claimSubject = ClaimSubject.valueOf(claims.getSubject());

            Optional<CustomerDto> customerDtoOptional = Optional.empty();
            var adminUserOptional = Optional.<UserViewDto>empty();

            switch (claimSubject) {
                case CUSTOMER:
                    int customerId = (int) claims.get("id");
                    customerDtoOptional = customerService.getCustomerById(customerId);
                    break;
                case GUEST:
                    int recipientId = (int) claims.get("id");
                    final var recipientOptional = contractService.getRecipientById(recipientId);
                    if (recipientOptional.isPresent()) {
                        final var recipient = recipientOptional.get();
                        final var customer = CustomerDto.builder()
                                .id(recipient.getId())
                                .email(recipient.getEmail())
                                .name(recipient.getName())
                                .phone("")
                                .status((short) 1)
                                .organizationId(0)
                                .typeId(0)
                                .build();

                        customerDtoOptional = Optional.of(customer);
                    }
                    break;

                case ADMIN:
                    long adminId = Long.valueOf(claims.get("id").toString());
                    adminUserOptional = userService.getUserById(adminId);
                    return ValidateTokenResponseDto.builder()
                            .success(true)
                            .type(claimSubject.ordinal())
                            .customer(mapper.map(adminUserOptional.get(), CustomerDto.class))
                            .build();
            }

            if (customerDtoOptional.isPresent()) {
                validateTokenResponseDto = ValidateTokenResponseDto.builder()
                        .success(true)
                        .type(claimSubject.ordinal())
                        .customer(customerDtoOptional.get())
                        .build();
            }
        } catch (JwtException e) {
            log.error(
                    String.format(
                            "can't parse jwt token: \"%s\"",
                            jws
                    ),
                    e
            );
        }

        return validateTokenResponseDto;
    }

    /**
     * Định nghĩa các đối tượng người dùng sẽ được xác thực trên hệ thống
     */
    public enum ClaimSubject {
        CUSTOMER, // Khách hàng định danh
        GUEST, // Khách hàng không định danh
        ADMIN // Quản trị viên của hệ thống
    }
}
