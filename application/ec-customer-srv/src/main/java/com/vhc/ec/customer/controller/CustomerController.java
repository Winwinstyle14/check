package com.vhc.ec.customer.controller;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.customer.dto.*;
import com.vhc.ec.customer.service.CustomerService;
import com.vhc.ec.customer.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ResponseStatus(HttpStatus.OK)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private final FileService fileService;

    /**
     * Tìm kiếm thông tin khách hàng, theo nhiều tiêu chí
     *
     * @param name     Họ và tên của khách hàng
     * @param email    Địa chỉ email
     * @param phone    Số điện thoại
     * @param orgId    Mã số tham chiếu tới tổ chức
     * @param pageable Thông tin phân trang
     * @return Danh sách khách hàng, đã phân trang
     */
    @GetMapping("/customers/search")
    public ResponseEntity<PageDto<CustomerDto>> search(
            @CurrentCustomer CustomerUser customerUser,
            @RequestParam(value = "name", defaultValue = "", required = false) String name,
            @RequestParam(value = "email", defaultValue = "", required = false) String email,
            @RequestParam(value = "phone", defaultValue = "", required = false) String phone,
            @RequestParam(value = "organization_id", required = false) Integer orgId,
            Pageable pageable) {

        final var page = customerService.search(
                customerUser.getId(),
                name, email, phone, orgId, pageable
        );

        return ResponseEntity.ok(page);
    }

    /**
     * Tạo mới khách hàng trên hệ thống
     *
     * @param customerDto {@link CustomerDto}
     * @return Đối tượng người dùng đã được lưu vào hệ thống
     */
    @PostMapping(value = {"/customers", "/internal/customers"})
    public ResponseEntity<CustomerDto> create(@Valid @RequestBody final CustomerDto customerDto) {
        final var customer = customerService.create(customerDto);
        return ResponseEntity.ok(customer);
    }

    @PostMapping(value = "/customers/import")
    public ResponseEntity<?> importUsers(@RequestParam("file") MultipartFile file,
                                         @CurrentCustomer CustomerUser user) {
        var customer = customerService.getById(user.getId()).orElse(null);
        if (customer == null) {
            return ResponseEntity.badRequest().build();
        }
        return customerService.importUsers(customer.getOrganizationId(), file);
    }

    /**
     * Cập nhật thông tin của khách hàng
     *
     * @param id          Mã số tham chiếu tới khách hàng
     * @param customerDto Thông tin chi tiết của khách hàng
     * @return Thông tin chi tiết của khách hàng
     */
    @PutMapping({"/customers/{id}", "/internal/customers/{id}"})
    public ResponseEntity<CustomerDto> update(
            @PathVariable("id") int id,
            @Valid @RequestBody final CustomerDto customerDto) {
        final var customerOptional = customerService.update(id, customerDto);

        return customerOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.badRequest().build()
        );
    }

    /**
     * Lấy thông tin chi tiết của người dùng
     *
     * @param id Mã người dùng
     * @return {@link CustomerDto} Thông tin chi tiết của người dùng
     */
    @GetMapping(value = {"/customers/{id}", "/internal/customers/{id}"})
    public ResponseEntity<?> getById(@PathVariable int id) {
        final var customerOptional = customerService.getById(id);
        if (customerOptional.isPresent()) {
            final var customer = customerOptional.get();

            if (customer.getSignImage() != null) {

                customer.getSignImage().forEach(signImage -> {
                    try {
                        final var presignedObjectUrl = fileService.getPresignedObjectUrl(
                                signImage.getBucket(),
                                signImage.getPath()
                        );

                        signImage.setPresignedUrl(presignedObjectUrl);
                    } catch (Exception e) {
                        log.error("error", e);
                    }
                });
            }
        }

        return customerOptional.map(
                ResponseEntity::ok
        ).orElseGet(() -> ResponseEntity.ok(null));
    }

    /**
     * Đổi mật khẩu của khách hàng
     *
     * @param customerUser Thông tin của khách hàng đang đăng nhập
     * @param request      Thông tin mật khẩu cần đổi
     * @return Thông tin trạng thái của dữ liệu đã được thay đổi
     */
    @PostMapping("/customers/changePassword")
    public ResponseEntity<?> changePassword(
            @CurrentCustomer CustomerUser customerUser,
            @Valid @RequestBody final ChangePasswordRequest request) {
        Map<String, Object> data = new HashMap<>();

        try {
            // cập nhật mật khẩu của người dùng
            boolean res = customerService.updatePassword(
                    customerUser.getId(),
                    request.getPassword(),
                    request.getNewPassword()
            );

            // dữ liệu trả về cho người dùng cuối
            data.put("status", res ? 0 : 1);
        } catch (Exception e) {
            data.put("status", 1);
        }

        return ResponseEntity.ok(data);
    }

    /**
     * Lấy thông tin của người dùng theo địa chỉ email
     *
     * @param request {@link FindCustomerByEmailRequest}
     * @return {@link CustomerDto} Thông tin chi tiết của người dùng
     */
    @PostMapping(value = {"/customers/get-by-email", "/internal/customers/getByEmail"})
    public ResponseEntity<CustomerDto> getByEmail(@Valid @RequestBody FindCustomerByEmailRequest request) {
        final var customerDtoOptional = customerService.getByEmail(request.getEmail());

        return customerDtoOptional.map(ResponseEntity::ok).orElseGet(() -> {
            final var customer = new CustomerDto();
            customer.setId(0);

            return ResponseEntity.ok(customer);
        });
    }

    /**
     * Lấy danh sách khách hàng của tổ chức
     *
     * @param id Mã tham chiếu của tổ chức
     * @return Danh sách khách hàng của tổ chức
     */
    @GetMapping("/by-organization/{id}")
    public ResponseEntity<Collection<CustomerDto>> getByOrganization(@PathVariable("id") int id) {
        return ResponseEntity.ok(
                customerService.getByOrganizationId(id)
        );
    }

    @PostMapping("/customers/check-phone-unique")
    public ResponseEntity<MessageDto> checkPhoneUnique(
            @Valid @RequestBody CustomerPhoneTelUniqueRequest request) {
        final var custOptional = customerService.findByPhone(request.getPhoneTel());

        var message = MessageDto.builder()
                .code("00")
                .message("customer phone not exists")
                .build();
        if (custOptional.isPresent()) {
            message = MessageDto.builder()
                    .code("01")
                    .message("customer phone existed")
                    .build();
        }

        return ResponseEntity.ok(message);
    }

    @GetMapping("/customers/check-service-status")
    public ServiceStatusRes checkServiceStatus(@CurrentCustomer CustomerUser user) {
        return customerService.checkServiceStatus(user);
    }

    @GetMapping("/customers/suggested-list")
    public List<SuggestedCustomerDto> suggestCustomer(@RequestParam String name, @CurrentCustomer CustomerUser user) {
        return customerService.suggestCustomer(name, user);
    }
}
