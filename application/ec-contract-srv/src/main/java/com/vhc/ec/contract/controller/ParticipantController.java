package com.vhc.ec.contract.controller;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.MessageDto;
import com.vhc.ec.contract.dto.ParticipantDto;
import com.vhc.ec.contract.repository.RecipientRepository;
import com.vhc.ec.contract.service.ParticipantService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Collection;

/**
 * Quản lý thông tin thành phần tham gia ký hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/participants")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class ParticipantController {

    private final ParticipantService participantService;
    private final RecipientRepository recipientRepository;

    /**
     * Thêm mới thông tin thành phần tham gia vào quá trình ký hợp đồng
     *
     * @param contractId               Mã hợp đồng
     * @param participantDtoCollection Danh sách thành phần tham gia vào quá trình ký hợp đồng
     * @return Thông tin về thành phần tham gia vào quá trình ký hợp đồng được tạo thành công
     */
    @PostMapping("/contract/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Collection<ParticipantDto>> create(
            @CurrentCustomer CustomerUser customerUser,
            @PathVariable("id") @NotNull int contractId,
            @Valid @RequestBody Collection<ParticipantDto> participantDtoCollection
    ) {
        try {
            participantService.create(
                    participantDtoCollection,
                    contractId,
                    customerUser.getId()
            );

            final var participantCollection = participantService
                    .findByContract(contractId);
            return ResponseEntity.ok(participantCollection);
        } catch (Exception e) {
            log.error("can't save participant.", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cập nhật thông tin của thành phần tham gia xử lý hồ sơ
     *
     * @param id             Mã số tham chiếu tới thành phần tham gia xử lý hồ sơ
     * @param participantDto Thông tin chi tiết của thành phần tham gia xử lý hồ sơ
     * @return {@link ParticipantDto}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ParticipantDto> update(
            @PathVariable("id") @NotNull Integer id,
            @Valid @RequestBody ParticipantDto participantDto) {
        participantService.update(
                id, participantDto
        );

        final var participantOptional = participantService.getById(id);
        return participantOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.badRequest().build()
        );
    }

    /**
     * Lấy danh sách thành phần tham gia ký hợp đồng
     *
     * @param id Mã hơp đồng
     * @return Danh sách thành phần tham gia ký hợp đồng
     */
    @GetMapping("/by-contract/{id}")
    public ResponseEntity<Collection<ParticipantDto>> getByContract(@PathVariable("id") int id) {
        return ResponseEntity.ok(
                participantService.findByContract(id)
        );
    }

    /**
     * Lấy thông tin thành phần tham dự ký hợp đồng
     *
     * @param id Mã thành phần tham gia
     * @return Thông tin chi tiết thành phần tham gia
     */
    @GetMapping("/{id}")
    public ResponseEntity<ParticipantDto> getById(@PathVariable("id") int id) {
        final var participantOptional = participantService.getById(id);
        return participantOptional.map(
                ResponseEntity::ok
        ).orElseGet(
                () -> ResponseEntity.badRequest().build()
        );
    }

    @GetMapping("/byRecipient/{recipientId}")
    public ResponseEntity<ParticipantDto> getByRecipientId(@PathVariable("recipientId") int recipientId) {
        final var recipientOptional = recipientRepository.findById(recipientId);
        if (recipientOptional.isPresent()) {
            var recipient = recipientOptional.get();

            final var participantOptional = participantService
                    .getById(
                            recipient.getParticipant().getId()
                    );
            return participantOptional.map(
                    ResponseEntity::ok
            ).orElseGet(
                    () -> ResponseEntity.badRequest().build()
            );
        }

        return ResponseEntity.internalServerError().build();
    }
    
    /**
     * Xóa đối tác
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDto> delete(@PathVariable int id) {
        final var delete = participantService.delete(id);

        return ResponseEntity.ok(delete);
    }
}
