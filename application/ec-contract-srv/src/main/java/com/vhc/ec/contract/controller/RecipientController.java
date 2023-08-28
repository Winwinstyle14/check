package com.vhc.ec.contract.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.RecipientDto;
import com.vhc.ec.contract.service.RecipientService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * API người tham gia xử lý
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/recipients")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class RecipientController {

    private final RecipientService recipientService;

    /**
     * Cập nhật trạng thái của người xử lý hồ sơ
     *
     * @param id
     * @return
     */
    @PutMapping("/internal/processing/{id}")
    public ResponseEntity<RecipientDto> processing(@PathVariable("id") int id) {
        final var recipientOptional = recipientService.processing(id);

        return recipientOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Lấy thông tin của người xử lý hồ sơ
     *
     * @param id Mã số tham chiếu tới người xử lý hồ sơ
     * @return Thông tin chi tiết của người xử lý hồ sơ
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<RecipientDto> getById(@PathVariable("id") int id) {
        final var recipientOptional = recipientService.findById(id);

        return recipientOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
    
    /**
     * Cập nhật trạng thái của người xử lý hồ sơ
     *
     * @param id
     * @return
     */
    @PutMapping("/{id}/change-status/{newStatus}")
    public ResponseEntity<RecipientDto> changeStatus(@PathVariable("id") int id,
    		@PathVariable("newStatus") int newStatus) {
        final var recipientOptional = recipientService.changeStatus(id, newStatus);

        return recipientOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Cập nhật thông tin của người xử lý hồ sơ
     *
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecipientDto> update(@PathVariable("id") int id, @Valid @RequestBody RecipientDto recipientDto) {
        final var recipientOptional = recipientService.update(id, recipientDto);

        return recipientOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Lấy thông tin của người xử lý hồ sơ lược bỏ thông tin field
     *
     * @param id Mã số tham chiếu tới người xử lý hồ sơ
     * @return Thông tin chi tiết của người xử lý hồ sơ
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecipientDto> getByIdRejectField(@PathVariable("id") int id) {
        final var recipientOptional = recipientService.getByIdRejectField(id);

        return recipientOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
