package com.vhc.ec.contract.controller;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.service.TemplateContractService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import java.util.Collection;
import java.util.Optional;

/**
 * Định nghĩa end-point liên quan tới hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/contracts/template")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class TemplateContractController {
    private final TemplateContractService contractService;

    /**
     * Thêm mới mẫu hợp đồng của khách hàng
     *
     * @param customerUser {@link CustomerUser} Thông tin của khách hàng tạo hợp đồng
     * @param contractDto  {@link TemplateContractDto} Đối tượng hợp đồng cần lưu trữ
     * @return {@link ContractDto} Đối tượng hợp đồng đã được lưu trữ thành công
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateContractDto create(
            @CurrentCustomer CustomerUser customerUser,
            @Valid @RequestBody TemplateContractDto contractDto) {
        return contractService.create(contractDto, customerUser.getId());
    }

    /**
     * Cập nhật mẫu hợp đồng của khách hàng
     *
     * @param id          Mã mẫu hợp đồng
     * @param contractDto {@link TemplateContractDto} Đối tượng hợp đồng cần lưu trữ
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateContractDto> update(
            @CurrentCustomer CustomerUser customerUser,
            @PathVariable("id") int id,
            @Valid @RequestBody TemplateContractDto contractDto) {
        final var contractOptional = contractService.update(customerUser.getId(), id, contractDto);

        return contractOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.badRequest().build()
        );
    }

    /**
     * Cập nhật trạng thái của hợp đồng
     *
     * @param id     Mã hợp đồng
     * @param status Trạng thái mới của hợp đồng
     * @return Thông tin chi tiết của hợp đồng
     */
    @PostMapping(value = {"/{id}/change-status/{newStatus}", "/internal/{id}/change-status/{newStatus}"})
    public ResponseEntity<TemplateContractDto> changeStatus(@PathVariable("id") int id, @PathVariable("newStatus") int status,
                                                            @RequestBody @Valid Optional<ContractChangeStatusRequest> request) {
        final var contractOptional = contractService.changeStatus(
                id, status, request.isPresent() ? request.get() : null
        );

        return contractOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Tìm kiếm mẫu hợp đồng của tôi (hợp đồng do người dùng tạo)
     *
     * @param customerUser {@link CustomerUser} Người dùng được xác thực qua api
     * @param contractType Loại hợp đồng
     * @param contractName Tên mẫu hợp đồng
     * @param pageable     Phân trang tìm kiếm
     * @return {@link PageDto <ContractDto>} Kết quả tìm kiếm
     */
    @GetMapping("/my-contract/list")
    public ResponseEntity<PageDto<TemplateContractDto>> searchMyContract(@CurrentCustomer CustomerUser customerUser,
                                                                         @RequestParam(name = "type", required = false) Integer contractType,
                                                                         @RequestParam(name = "name", required = false, defaultValue = "") String contractName,
                                                                         Pageable pageable) {
        try {
            PageDto<TemplateContractDto> pageDto = contractService.searchMyContract(
                    customerUser.getId(),
                    contractType,
                    contractName,
                    pageable
            );

            return ResponseEntity.ok(pageDto);
        } catch (Exception e) {
            log.error("unexpected error", e);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy thông tin chi tiết của mẫu hợp đồng
     *
     * @param id Mã mẫu hợp đồng
     * @return {@link TemplateContractDto} Thông tin chi tiết của mẫu hợp đồng
     */
    @GetMapping(value = {"/{id}", "/internal/{id}"})
    public ResponseEntity<TemplateContractDto> findById(@PathVariable int id) {
        final var contractDtoOptional = contractService.findById(id);

        if (contractDtoOptional.isPresent()) {
            final var contractDto = contractDtoOptional.get();

            return ResponseEntity.ok(contractDto);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Xóa mẫu hợp đồng
     *
     * @param id Mã mẫu hợp đồng
     * @return {@link MessageDto} Thông tin lỗi
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDto> delete(@PathVariable int id) {
        final var delete = contractService.delete(id);

        return ResponseEntity.ok(delete);
    }

    @PostMapping("/check-code-unique")
    public ResponseEntity<MessageDto> checkUniqueCode(
            @RequestBody TemplateContractCodeUniqueRequest request) {

        final var messageOptional = contractService.findAllByCodeStartTimeEndTimeOrgId(request.getCode(), request.getStartTime(), request.getEndTime(), request.getOrganizationId(), request.getContractId());

        return messageOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
    
    /**
     * clone file hợp đồng, file đính kèm
     *
     * @param customerUser authorized user id
     * @param templateContractId   template contract id need to copy
     * @param contractId   contract save file
     * @return {@link ContractDto}
     */
    
    @GetMapping("/clone/{template_contract_id}/{contract_id}")
    public ResponseEntity<Collection<DocumentDto>> clone(@CurrentCustomer CustomerUser customerUser, 
    		@PathVariable("template_contract_id") Integer templateContractId, 
    		@PathVariable("contract_id") Integer contractId
    	) {
        var contractOptional = contractService.clone(customerUser.getId(), templateContractId, contractId);

        return contractOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.internalServerError().build()
        );
    }
    
    /**
     * 
     * @param customerUser
     * @return
     */
    @GetMapping("/release/list")
    public ResponseEntity<Collection<TemplateContractModifileDto>> getTemplateList(@CurrentCustomer CustomerUser customerUser) {
    	final var templateOptional = contractService.getTemplateList(customerUser.getId());

        if (templateOptional.isPresent()) {
            final var templateContractDto = templateOptional.get();

            return ResponseEntity.ok(templateContractDto);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/is-use/{id}")
    public ResponseEntity<MessageDto> isUse(@PathVariable int id) {
        final var contractByTemplateId = contractService.findByTemplateContractId(id);

        if(contractByTemplateId.size() > 0) {
            return ResponseEntity.ok(
                    MessageDto.builder()
                    .success(true)
                    .message("E00")
                    .build()
            );
        }

        return ResponseEntity.ok(
                MessageDto.builder()
                .success(false)
                .message("E02")
                .build()
        );
    }

    @PostMapping("/check-view-contract/{id}")
    public ResponseEntity<MessageDto> checkViewContract(
            @PathVariable("id") Integer contractId,
            @RequestBody CustomerDto customerUser ) {
        final var checkViewContract = contractService.checkViewContract(contractId, customerUser.getEmail());

        return ResponseEntity.ok(checkViewContract);
    }
}
