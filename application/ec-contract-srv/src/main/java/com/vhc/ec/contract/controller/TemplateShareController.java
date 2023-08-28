package com.vhc.ec.contract.controller;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.service.TemplateContractService;
import com.vhc.ec.contract.service.TemplateShareService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Collection;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/shares/template")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
public class TemplateShareController {

    private final TemplateShareService shareService;
    private final TemplateContractService contractService;

    /**
     * Chia sẻ mẫu hợp đồng
     *
     * @param id Mã bản ghi
     * @return {@link ShareListDto}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ShareListDto> create(@CurrentCustomer CustomerUser customerUser, @Valid @RequestBody ShareListDto shareListDto) {
        final var created = shareService.create(customerUser, shareListDto);

        return created.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Danh sách mẫu hợp đồng được chia sẻ
     *
     * @param type Mã loại mẫu hợp đồng
     * @param name Tên mẫu hợp đồng
     * @return {@link TemplateContractDto}
     */
    @GetMapping
    public ResponseEntity<PageDto<TemplateContractDto>> getAll(
            @CurrentCustomer CustomerUser customerUser,
            @RequestParam(name = "type", required = false) Integer contractType,
            @RequestParam(name = "name", required = false, defaultValue = "") String contractName,
            Pageable pageable) {
        final var page = contractService.getShares(
                customerUser.getEmail(),
                contractType,
                contractName,
                pageable
        );

        return ResponseEntity.ok(page);
    }

    /**
     * Xóa chia sẻ mẫu hợp đồng
     *
     * @param id Mã bản ghi
     * @return {@link MessageDto}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDto> delete(@PathVariable int id) {
        final var delete = shareService.deleteById(id);

        return ResponseEntity.ok(delete);
    }

    /**
     * Danh sách chia sẻ mẫu hợp đồng theo mã mẫu hợp đồng
     *
     * @param contractId Mã mẫu hợp đồng
     * @return {@link MessageDto}
     */
    @GetMapping("/by-contract/{contractId}")
    public ResponseEntity<Collection<TemplateShareDto>> getByContract(
            @PathVariable("contractId") int id,
            @RequestParam(name = "organization_id", required = false) Integer organizationId
    ) {
        final var shareOptional = shareService.findByContract(id, organizationId);

        return ResponseEntity.ok(shareOptional);
    }
}