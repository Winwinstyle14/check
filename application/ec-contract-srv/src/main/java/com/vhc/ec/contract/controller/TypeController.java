package com.vhc.ec.contract.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.MessageDto;
import com.vhc.ec.contract.dto.TypeCodeUniqueRequest;
import com.vhc.ec.contract.dto.TypeDto;
import com.vhc.ec.contract.dto.TypeNameUniqueRequest;
import com.vhc.ec.contract.service.TypeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Định nghĩa end-point liên quan tới loại hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/contract-types")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class TypeController {

    private final TypeService typeService;

    /**
     * Tạo mới loại hợp đồng
     *
     * @param typeDto {@link TypeDto} Thông tin loại hợp đồng cần tạo
     * @return {@link ResponseEntity} Thông tin loại hợp đồng đã được lưu vào hệ thống
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TypeDto> create(@Valid @RequestBody TypeDto typeDto) {
        Optional<TypeDto> typeDtoOptional = typeService.create(typeDto);

        return typeDtoOptional.map(
                dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto)
        ).orElseGet(() -> ResponseEntity.internalServerError().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeDto> update(@PathVariable int id, @Valid @RequestBody TypeDto typeDto) {
        final var updated = typeService.update(id, typeDto);

        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Lấy thông tin loại hợp đồng theo mã số của loại hợp đồng
     *
     * @param id Mã số của loại hợp đồng
     * @return {@link TypeDto} Thông tin của loại hợp đồng
     */
    @GetMapping("/{id}")
    public ResponseEntity<TypeDto> findById(@PathVariable int id) {
        Optional<TypeDto> typeDtoOptional = typeService.findById(id);

        return typeDtoOptional.map(
                ResponseEntity::ok
        ).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDto> delete(@PathVariable int id) {
        final var deletedOptional = typeService.delete(id);

        MessageDto messageDto = null;

        if (deletedOptional.isPresent()) {
            messageDto = MessageDto.builder()
                    .success(true)
                    .message("type deleted")
                    .build();
        } else {
            messageDto = MessageDto.builder()
                    .success(false)
                    .message("can't delete type")
                    .build();
        }

        return ResponseEntity.ok(messageDto);
    }

    /**
     * Lấy toàn bộ loại hợp đồng của tổ chức
     *
     * @param organizationId Mã số của tổ chức
     * @return Danh sách loại hợp đồng
     */
    @GetMapping("/organizations/{organizationId}")
    public Collection<TypeDto> findByOrgId(
            @PathVariable int organizationId,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "code", required = false, defaultValue = "") String code) {
        return typeService.findByOrgId(organizationId, name, code);
    }

    @PostMapping("/check-name-unique")
    public ResponseEntity<MessageDto> checkUniqueName(
            @Valid @RequestBody TypeNameUniqueRequest request) {
        final var typeOptional = typeService.findByOrgIdAndName(
                request.getOrganizationId(),
                request.getName()
        );

        var message = MessageDto.builder()
                .success(true)
                .message("contract type name not exists")
                .build();

        if (typeOptional.isPresent()) {
            message = MessageDto.builder()
                    .success(false)
                    .message("contract type name existed")
                    .build();
        }

        return ResponseEntity.ok(message);
    }

    @PostMapping("/check-code-unique")
    public ResponseEntity<MessageDto> checkUniqueCodde(
            @Valid @RequestBody TypeCodeUniqueRequest request) {
        final var typeOptional = typeService.findByOrgIdAndCode(
                request.getOrganizationId(),
                request.getCode()
        );

        var message = MessageDto.builder()
                .success(true)
                .message("contract type code not exists")
                .build();
        if (typeOptional.isPresent()) {
            message = MessageDto.builder()
                    .success(false)
                    .message("contract type code existed")
                    .build();
        }

        return ResponseEntity.ok(message);
    }
}
