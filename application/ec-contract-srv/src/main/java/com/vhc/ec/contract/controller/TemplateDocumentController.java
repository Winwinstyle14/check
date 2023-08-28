package com.vhc.ec.contract.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.DocumentDto;
import com.vhc.ec.contract.dto.TemplateDocumentDto;
import com.vhc.ec.contract.service.TemplateDocumentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Collection;

/**
 * Xử lý tài liệu của hồ sơ
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/documents/template")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
public class TemplateDocumentController {
    private final TemplateDocumentService documentService;

    /**
     * Thêm mới tài liệu cho hồ sơ
     *
     * @param documentDto {@link TemplateDocumentDto} Nội dung chi tiết tài liệu
     * @return {@link TemplateDocumentDto} Nội dung chi tiết của tài liệu đã được lưu trữ trên hệ thống
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateDocumentDto create(@Valid @RequestBody TemplateDocumentDto documentDto) {
        return documentService.create(documentDto);
    }

    /**
     * update existed document
     *
     * @param id          uuid of document
     * @param documentDto data about document need to update
     * @return {@link DocumentDto}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateDocumentDto> update(@PathVariable("id") int id, @Valid @RequestBody TemplateDocumentDto documentDto) {
        var optional = documentService.update(id, documentDto);

        return optional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.internalServerError().build()
        );
    }

    /**
     * Lấy danh sách tài liệu liên quan tới hợp đồng
     *
     * @param contractId Mã hợp đồng
     * @return {@link Collection<TemplateDocumentDto>} Danh sách tài liệu liên quan tới hợp đồng
     */
    @GetMapping("/by-contract/{id}")
    public ResponseEntity<Collection<TemplateDocumentDto>> getByContract(
            @PathVariable("id") int contractId) {

        return ResponseEntity.ok(
                documentService.findByContract(contractId)
        );
    }
}
