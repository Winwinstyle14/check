package com.vhc.ec.contract.thridparty;

import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.contract.definition.SignType;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.dto.tp.SignContractDto;
import com.vhc.ec.contract.service.DocumentService;
import com.vhc.ec.contract.service.ProcessService;
import com.vhc.ec.contract.service.RecipientService;
import com.vhc.ec.contract.service.SignService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class TpContractService {
    private final RecipientService recipientService;

    private final SignService signService;

    private final ProcessService processService;

    private final DocumentService documentService;

    private final ModelMapper mapper;

    public ResponseEntity<?> sign(int id,
                                  CustomerUser currentCustomer,
                                  SignContractDto signContractDto) {
        var recipient = recipientService.findById(id).orElse(null);
        if (recipient == null) {
            return ResponseEntity.badRequest().build();
        }

        int signType = new ArrayList<>(recipient.getSignType()).get(0).getId();
        if (signType == SignType.SIM_PKI.getDbVal()) {
            var validDto = validRequest(SignType.SIM_PKI, signContractDto);
            if (!validDto.isSuccess()) {
                return ResponseEntity.badRequest().body(validDto);
            }

            var signPkiRes = signService.simPkiSignV3(
                    id,
                    signContractDto.getMobile(),
                    signContractDto.getNetworkCode(),
                    signContractDto.getPrompt(),
                    signContractDto.getReason(),
                    signContractDto.getImageBase64()
            );

            if (signPkiRes.isSuccess()) {
                processService.approval(id, Collections.emptyList(), new ProcessApprovalDto());
            }

            return ResponseEntity.ok()
                    .body(signPkiRes);
        } else if (signType == SignType.IMAGE_AND_OTP.getDbVal()) {
             return processService.approvalSignImage(
                    currentCustomer,
                    id,
                    mapper.map(signContractDto, ProcessApprovalDto.class)
            );
        } else if (signType == SignType.HSM.getDbVal()) {
            var res = signService.hsmSign(id, mapper.map(signContractDto, HsmSignRequest.class));
            if (res.isSuccess()) {
                processService.approval(id, Collections.emptyList(), new ProcessApprovalDto());
            }
            return ResponseEntity.ok().body(res);
        } else if (signType == SignType.USB_TOKEN.getDbVal()) {
            var res= processService.digitalSign(signContractDto.getFieldId(), mapper.map(signContractDto, DigitalSignDto.class));
            if (res.isPresent()) {
                processService.approval(id, Collections.emptyList(), new ProcessApprovalDto());
                return ResponseEntity.ok(res);
            }
        }

        return ResponseEntity.badRequest().build();
    }

    public OtpMessageDto genOtp(int recipientId, GenOtpRequest otpRequest) {
        return processService.genOtp(recipientId, otpRequest);
    }

    private MessageDto validRequest(SignType signType, SignContractDto signContractDto) {
        if (signType == SignType.USB_TOKEN) {
            if (StringUtils.isEmpty(signContractDto.getMobile())) {
                return MessageDto
                        .builder()
                        .success(false)
                        .message("mobile is required")
                        .build();
            }

            if (StringUtils.isEmpty(signContractDto.getNetworkCode())) {
                return MessageDto
                        .builder()
                        .success(false)
                        .message("networkCode is required")
                        .build();
            }
        }

        return MessageDto
                .builder()
                .success(true)
                .build();
    }

    public Collection<DocumentDto> getDocuments(int id) {
        return documentService.findByContract(id);
    }
}
