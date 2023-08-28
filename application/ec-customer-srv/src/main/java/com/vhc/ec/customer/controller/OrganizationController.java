package com.vhc.ec.customer.controller;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.customer.dto.*;
import com.vhc.ec.customer.service.CustomerService;
import com.vhc.ec.customer.service.OrganizationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Optional;

/**
 * Quản lý thông tin tổ chức của khách hàng
 *
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
public class OrganizationController {

    private final OrganizationService organizationService;
    private final CustomerService customerService;

    /**
     * Tạo mới tổ chức trên hệ thống
     *
     * @param organizationDto {@link OrganizationDto} Thông tin của tổ chức cần được tạo mới
     * @return {@link OrganizationDto} Thông tin của tổ chức đã được tạo mới thành công trên hệ thống
     */
    @PostMapping("/organizations")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@Valid @RequestBody final OrganizationDto organizationDto) {
    	 return ResponseEntity.ok(
                 organizationService.create(organizationDto)
         );
    }

    @PostMapping("/organizations/import-child-org")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file,
                                         @CurrentCustomer CustomerUser user) {
        var customer = customerService.getById(user.getId()).orElse(null);
        if (customer == null) {
            return ResponseEntity.badRequest().build();
        }
        return organizationService.importChildOrg(customer.getOrganizationId(), file);
    }

    /**
     * Lấy thông tin chi tiết của cơ quan, tổ chức theo mã cơ quan
     *
     * @param id Mã cơ quan
     * @return {@link OrganizationDto} Thông tin chi tiết của cơ quan, tổ chức
     */
    @GetMapping(value = {"/organizations/{id}", "/internal/organizations/{id}"})
    public OrganizationDto getById(@PathVariable @Min(1) int id) {
        Optional<OrganizationDto> orgDtoOptional = organizationService.getById(id);

        return orgDtoOptional.orElse(null);
    }

    /**
     * Cập nhật thông tin của tổ chức
     *
     * @param id              Mã số tham chiếu tới tổ chức
     * @param organizationDto Thông tin cần cập nhật
     * @return Thông tin của tổ chức đã được cập nhật
     */
    @PutMapping(value = {"/organizations/{id}"})
    public ResponseEntity<OrganizationDto> update(
            @PathVariable("id") int id,
            @RequestBody @Valid final OrganizationDto organizationDto) {
        final var orgOptional = organizationService.update(id, organizationDto);

        return orgOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping(value = {"/internal/organizations/{id}/decrease-number-of-contracts"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decreaseNumberOfContracts(
            @PathVariable("id") int id) {
        organizationService.decreaseNumberOfContracts(id);
    }

    @PutMapping(value = {"/internal/organizations/{id}/decrease-number-of-sms"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decreaseNumberOfSms(
            @PathVariable("id") int id) {
        organizationService.decreaseNumberOfSms(id);
    }

    @PutMapping(value = {"/organizations/{id}/decrease-number-of-ekyc"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decreaseNumberOfEkyc(
            @PathVariable("id") int id) {
        organizationService.decreaseNumberOfEkyc(id);
    }
    /**
     * Lấy thông tin chi tiết của tổ chức theo mã tham chiếu của khách hàng định danh trong tổ chức đó
     *
     * @param id Mã tham chiếu của khách hàng định danh
     * @return {@link OrganizationDto} Thông tin chi tiết của tổ chức
     */
    @GetMapping(value = {"/internal/organizations/by-customer/{customerId}"})
    public ResponseEntity<OrganizationDto> getByCustomerId(@PathVariable("customerId") int id) {
        final var orgDtoOptional = organizationService.findByCustomerId(id);

        return orgDtoOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Tìm kiếm cơ quan
     *
     * @param name     Tên cơ quan
     * @param code     Mã cơ quan
     * @param pageable Phân trang
     * @return Danh sách cơ quan thoả mãn điều kiện tìm kiếm
     */
    @GetMapping("/organizations/search")
    public ResponseEntity<PageDto<OrganizationDto>> search(
            @CurrentCustomer CustomerUser customerUser,
            @RequestParam(name = "name", defaultValue = "", required = false) String name,
            @RequestParam(name = "code", defaultValue = "", required = false) String code,
            Pageable pageable) {
        final var customerOptional = customerService.getById(customerUser.getId());
        if (customerOptional.isPresent()) {
            final var page = organizationService.search(
                    customerOptional.get().getOrganizationId(),
                    name, code, pageable
            );

            return ResponseEntity.ok(page);
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/internal/organizations/{id}/get-descendant-id")
    public List<Integer> getDescendantId(@PathVariable int id) {
        return organizationService.findChildIdRecursiveById(id);
    }

    @GetMapping("/internal/organizations/{id}/get-all-org-in-tree")
    public List<Integer> getAllOrgInTree(@PathVariable int id) {
        return organizationService.getAllOrgInTree(id);
    }

    @PostMapping("/organizations/check-code-unique")
    public ResponseEntity<MessageDto> checkCodeUnique(
            @Valid @RequestBody OrganizationCodeUniqueRequest request
    ) {
    	//Kiểm tra trùng code
        final var orgOptional = organizationService.findByCode(request.getCode(), request.getOrgIdCha()); 
        
        var message = MessageDto.builder()
                .code("00")
                .message("organization code not exists")
                .build();
        
        if (orgOptional.isPresent()) {
            message = MessageDto.builder()
                    .code("01")
                    .message("organization code existed")
                    .build();
        }else {
        	if(request.getOrgIdCon() != null) {
        		final var orgConOptional = organizationService.findByCode(request.getCode(), request.getOrgIdCon());
        		
        		if (orgConOptional.isPresent()) {
                    message = MessageDto.builder()
                            .code("01")
                            .message("organization code existed")
                            .build();
                }
        	}  
        }

        return ResponseEntity.ok(message);
    }

    @PostMapping("/organizations/check-name-unique")
    public ResponseEntity<MessageDto> checkNameUnique(
            @Valid @RequestBody OrganizationNameUniqueRequest request
    ) {
        final var orgOptional = organizationService.findByName(request.getName(), request.getOrgIdCha());

        var message = MessageDto.builder()
                .code("00")
                .message("organization name not exists")
                .build();
        
        if (orgOptional.isPresent()) {
            message = MessageDto.builder()
                    .code("01")
                    .message("organization name existed")
                    .build();
        }else {
        	if(request.getOrgIdCon() != null) {
        		final var orgConOptional = organizationService.findByName(request.getName(), request.getOrgIdCon());
        		
        		if (orgConOptional.isPresent()) {
                    message = MessageDto.builder()
                            .code("01")
                            .message("organization name existed")
                            .build();
                }
        	}  
        }

        return ResponseEntity.ok(message);
    }

    @PostMapping("/organizations/check-email-unique")
    public ResponseEntity<MessageDto> checkEmailUnique(
            @CurrentCustomer CustomerUser customerUser,
            @Valid @RequestBody OrganizationEmailUniqueRequest request) {
        final var orgOptional = organizationService.findByEmail(request.getEmail());

        var message = MessageDto.builder()
                .code("00")
                .message("organization email not exists")
                .build();
        if (orgOptional.isPresent()) {
            message = MessageDto.builder()
                    .code("01")
                    .message("organization email existed")
                    .build();
        }

        return ResponseEntity.ok(message);
    }

    @PostMapping("/organizations/check-phone-unique")
    public ResponseEntity<MessageDto> checkPhoneUnique(
            @Valid @RequestBody OrganizationPhoneUniqueRequest request) {
        final var orgOptional = organizationService.findByPhone(request.getPhone());

        var message = MessageDto.builder()
                .code("00")
                .message("organization phone not exists")
                .build();
        if (orgOptional.isPresent()) {
            message = MessageDto.builder()
                    .code("01")
                    .message("organization phone existed")
                    .build();
        }

        return ResponseEntity.ok(message);
    }

    @GetMapping("/organizations/{id}/service/number-of-contracts")
    public AbstractTotalDto getNumberContractInPurchasedService(@PathVariable int id) {
        return organizationService.getNumberContractInPurchasedService(id);
    }

    @GetMapping("/organizations/{id}/service/total-purchased-items")
    public TotalItemDto getTotalItemPurchased(@PathVariable int id) {
        return organizationService.getTotalPurchasedItem(id);
    }

    @GetMapping("/organizations/{id}/service/total-used-items")
    public TotalItemDto getUsedItem(@PathVariable int id) {
        return organizationService.getUsedItem(id);
    }

    @GetMapping("/organizations/{id}/service/license")
    public LicenseInfoDto getLicense(@PathVariable int id) {
        return organizationService.getLicense(id);
    }

}
