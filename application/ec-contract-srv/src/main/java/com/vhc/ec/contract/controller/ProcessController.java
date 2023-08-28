package com.vhc.ec.contract.controller;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.net.MalformedURLException;
import java.util.Collection;

/**
 * Khách hàng xử lý hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/processes")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class ProcessController {

    private final ProcessService processService;
    private final ParticipantService participantService;
    private final RecipientService recipientService;
    private final SignService signService;

    /**
     * Thực hiện uỷ quyền cho người dùng khác
     *
     * @param customerUser Thông tin khách hàng đã đăng nhập
     * @param authorize    Thông tin uỷ quyền
     * @return Thông tin khách hàng đã được uỷ quyền
     */
    @PostMapping("/authorize")
    public MessageDto authorized(
            @CurrentCustomer CustomerUser customerUser,
            @RequestBody @Valid AuthorizeDto authorize) {
        return processService.authorize(
                customerUser.getEmail(),
                authorize.getEmail(),
                authorize.getFullname(),
                authorize.getPhone(),
                authorize.getRole(),
                authorize.getRecipientId(),
                authorize.isReplace(),
                authorize.getCardId()
        );

    }

    /**
     * Khách hàng xác nhận đồng ý xử lý hồ sơ
     *
     * @param recipientId                  Mã số tham chiếu khách hàng xử lý hồ sơ
     * @param fieldUpdateRequestCollection Thông tin chi tiết dữ liệu cập nhật tới hồ sơ
     * @return Thông báo cho người dùng cuối
     */
    @PutMapping("/approval/{recipientId}")
    public ResponseEntity<RecipientDto> approval(
            @PathVariable("recipientId") int recipientId,
            @RequestBody @Valid Collection<FieldUpdateRequest> fieldUpdateRequestCollection) {

        return processService.approval(recipientId, fieldUpdateRequestCollection, new ProcessApprovalDto());
    }

    @PutMapping("/approval-sign-image/{recipientId}")
    public ResponseEntity<?> approvalSignImage(
            @CurrentCustomer CustomerUser currentCustomer,
            @PathVariable("recipientId") int recipientId,
            @RequestBody ProcessApprovalDto processApprovalDto) {
    	log.info("sign image: recipient: {}", recipientId);
        //log.info("sign image: recipient: {}, dto: {}", recipientId, processApprovalDto);
        return processService.approvalSignImage(currentCustomer, recipientId, processApprovalDto);
    }

    @GetMapping("/approval-sign-image/{recipientId}/status")
    public OtpMessageDto approvalSignImage(@PathVariable("recipientId") int recipientId) {
        return processService.checkRecipientStatus(recipientId);
    }

    @PostMapping("/approval/{recipientId}/gen-otp")
    public OtpMessageDto genOtp(@PathVariable int recipientId,
                             @RequestBody GenOtpRequest otpRequest) {

        return processService.genOtp(recipientId, otpRequest);
    }

    /**
     * Khách hàng xác nhận từ chối xử lý hồ sơ
     *
     * @param id Mã tham chiếu tới khách hàng xử lý hồ sơ
     * @return {@link RecipientDto} Thông tin khách hàng xử lý hồ sơ sau khi đã cập nhật
     */
    @PutMapping("/reject/{id}")
    public ResponseEntity<RecipientDto> reject(
            @PathVariable("id") int id, @RequestBody @Valid RejectRequest request) {
        final var recipientOptional = recipientService.reject(
                id, request.getReason()
        );

        return recipientOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Người điều phối cập nhật danh sách người xử lý hồ sơ
     *
     * @param participantId          Mã tham chiếu tới thành phần tham gia xử lý hồ sơ
     * @param recipientDtoCollection Danh sách người tham gia xử lý hồ sơ
     * @return {@link ParticipantDto}
     */
    @PutMapping("/coordinator/{participant_id}/{recipient_id}")
    public ResponseEntity<ParticipantDto> coordinator(
    		@CurrentCustomer CustomerUser customerUser,
            @PathVariable("participant_id") int participantId,
            @PathVariable("recipient_id") int recipinentId,
            @RequestBody @Valid Collection<RecipientDto> recipientDtoCollection) {
        final var participantDtoOptional = participantService.updateRecipientForCoordinator(
        		customerUser.getId(),
                participantId,
                recipinentId,
                recipientDtoCollection
        );


        return participantDtoOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Khách hàng cập nhật nội dung hợp đồng đã được ký số.
     *
     * @param fieldId     Mã số tham chiếu tới trường xử lý hồ sơ
     * @param digitalSign {@link DigitalSignDto} Thông tin hồ sơ đã được ký số
     * @return {@link FieldDto} Thông tin trường dữ liệu sau khi dã cập nhật
     */
    @PutMapping("/digital-sign/{field_id}")
    public ResponseEntity<FieldDto> digitalSign(
            @PathVariable("field_id") int fieldId,
            @RequestBody @Valid DigitalSignDto digitalSign) {

        log.info("ky usb token, fieldId: {}", fieldId);

        final var fieldOptional = processService.digitalSign(
                fieldId, digitalSign
        );

        return fieldOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.badRequest().build()
        );
    }

    @PostMapping("/digital-sign/{recipientId}/create-empty-token")
    public PkcsCreateTokenResponse createEmptyToken(@PathVariable int recipientId,
                                                    @RequestBody PkcsCreateTokenRequest pkcsCreateTokenRequest) {
        try {
            return signService.createPcksEmptyToken(recipientId, pkcsCreateTokenRequest);
        } catch (MalformedURLException e) {
            log.error("error: {}", e);
            return PkcsCreateTokenResponse.builder().message(e.getMessage()).build();
        }
    }

    @PostMapping("/digital-sign/{recipientId}/merge-time-stamp")
    public PcksMergeResponse mergeTimeStamp(@PathVariable int recipientId,
                                            @RequestBody PkcsMergeRequest pkcsMergeRequest) {

        return processService.mergeTimestamp(pkcsMergeRequest);
    }

    /**
     *
     * Gui lai sms, email cho nguoi xu ly
     */
    @PostMapping("/resend-sms-email/{recipientId}")
    public MessageDto resendSmsEmail(@PathVariable int recipientId) {

        return processService.resendSmsEmail(recipientId);
    }
}
