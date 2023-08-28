package com.vhc.ec.contract.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.FieldDto;
import com.vhc.ec.contract.dto.TemplateFieldDto;
import com.vhc.ec.contract.service.TemplateFieldService;
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
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/fields/template")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
public class TemplateFieldController {

    private final TemplateFieldService fieldService;

    /**
     * Thêm mới danh sách trường dữ liệu cho hợp đồng
     *
     * @param fieldDtoCollection Danh sách trường dữ liệu cần thêm mới cho hợp đồng
     * @return Danh sách trường dữ liệu đã được thêm vào hợp đồng
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Collection<TemplateFieldDto>> create(@Valid @RequestBody Collection<TemplateFieldDto> fieldDtoCollection) {
        return ResponseEntity.ok(
                fieldService.create(fieldDtoCollection)
        );
    }

    /**
     * Cập nhật thông tin trường dữ liệu
     *
     * @param fieldDto Thông tin trường dữ liệu cần cập nhật
     * @param id       Mã trường dữ liệu
     * @return Thông tin chi tiết trường dữ liệu
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateFieldDto> update(@RequestBody @Valid TemplateFieldDto fieldDto,
                                                   @PathVariable("id") int id) {
        final var fieldOptional = fieldService.update(id, fieldDto);

        return fieldOptional.map(
                ResponseEntity::ok
        ).orElseGet(() ->
                ResponseEntity.badRequest().build()
        );
    }

    /**
     * Lấy danh sách trường dữ liệu của hợp đồng
     *
     * @param id Mã hợp đồng
     * @return Danh sách trường dữ liệu của hợp đồng
     */
    @GetMapping("/by-contract/{contractId}")
    public ResponseEntity<Collection<TemplateFieldDto>> getByContract(
            @PathVariable("contractId") int id
    ) {
        return ResponseEntity.ok(
                fieldService.findByContract(id)
        );
    }

    /**
     * delete an existed field by id
     *
     * @param id id of field need to delete
     * @return {@link FieldDto}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<TemplateFieldDto> delete(@PathVariable("id") int id) {
        var optional = fieldService.deleteById(id);

        return optional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.internalServerError().build()
        );
    }
}