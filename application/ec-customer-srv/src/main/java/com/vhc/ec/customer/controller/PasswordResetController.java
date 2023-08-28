package com.vhc.ec.customer.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.customer.defination.BaseStatus;
import com.vhc.ec.customer.dto.CreatePasswordResetRequest;
import com.vhc.ec.customer.dto.PasswordRecoverDto;
import com.vhc.ec.customer.dto.PasswordResetDto;
import com.vhc.ec.customer.entity.PasswordReset;
import com.vhc.ec.customer.repository.CustomerRepository;
import com.vhc.ec.customer.repository.PasswordResetRepository;
import com.vhc.ec.customer.service.CustomerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.OK)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetRepository passwordResetRepository;
    private final CustomerService customerService;
    private final RestTemplate restTemplate;
    private final CustomerRepository customerRepository;

    /**
     * Yêu cầu reset password
     *
     * @param request Thông tin mật khẩu của người sử dụng
     * @return
     * @throws Exception
     */
    @PostMapping("/password/request")
    @ResponseBody
    public ResponseEntity<?> passwordReset(@RequestBody PasswordResetDto request) throws Exception {

        Map<String, Object> data = new HashMap<String, Object>();

        try {
            var customerOptional = customerRepository.findTopByEmail(request.getEmail());

            // email khong ton tai
            if (!customerOptional.isPresent()) {
                data.put("status", 1);
                data.put("code", "01");

                return ResponseEntity.ok(data);
            }

            // tai khoan khong hoat dong
            var customer = customerOptional.get();
            if (customer.getStatus() != BaseStatus.ACTIVE) {
                data.put("status", 1);
                data.put("code", "02");

                return ResponseEntity.ok(data);
            }

            // to chuc khong hoat dong
            if (customer.getOrganization() == null ||
                    customer.getOrganization().getStatus() != BaseStatus.ACTIVE) {
                data.put("status", 1);
                data.put("code", "03");

                return ResponseEntity.ok(data);
            }

            String token = UUID.randomUUID().toString();

            PasswordReset passwordReset = passwordResetRepository.findByEmail(request.getEmail());

            if (passwordReset == null) {
                passwordReset = new PasswordReset();
                passwordReset.setEmail(request.getEmail());
                passwordReset.setToken(token);
            }

            passwordReset.setToken(token);
            passwordReset.setCreatedAt(new Date());

            passwordResetRepository.save(passwordReset);

            // Send reset password email to customer
            try {
                CreatePasswordResetRequest createPasswordResetRequest = new CreatePasswordResetRequest();
                createPasswordResetRequest.setEmail(request.getEmail());
                createPasswordResetRequest.setToken(token);
                createPasswordResetRequest.setUsername(customer.getName());

                Object object = restTemplate.postForObject("http://ec-notification-srv/api/v1/internal/notification/createResetPassword", createPasswordResetRequest, Object.class);

                data.put("status", 0);
            } catch (Exception e) {
                data.put("status", 1);
                data.put("code", "04");

                log.error("error", e);
            }
        } catch (Exception e) {
            data.put("status", 1);
            data.put("code", "-1");

            log.error("error", e);
        }

        return ResponseEntity.ok(data);
    }

    /**
     * Yêu cầu recover password by token
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/password/recover")
    @ResponseBody
    public ResponseEntity<?> recover(@RequestBody PasswordRecoverDto request) throws Exception {

        Map<String, Object> data = new HashMap<String, Object>();

        // Kiểm tra token
        PasswordReset passwordReset = passwordResetRepository.findByToken(request.getToken());

        if (passwordReset == null || !passwordReset.getToken().equals(request.getToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is invalid");
        }

        request.setEmail(passwordReset.getEmail());

        //Object object = restTemplate.postForObject("http://ec-customer/api/v1//customer/update-password", request, Object.class);

        boolean status = customerService.updatePassword(passwordReset.getEmail(), request.getPassword());

        data.put("status", status ? 0 : 1);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/password/reset")
    @Deprecated
    public ResponseEntity<Boolean> resetPassword(@RequestParam("email") String email) {
        boolean success = customerService.updatePassword(email, "welcome1");
        return ResponseEntity.ok(success);
    }
}
