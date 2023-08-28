package com.vhc.ec.contract.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.service.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.service.SignService;

import lombok.AllArgsConstructor;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/sign")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class SignController {

    private final SignService signService;
    private final ProcessService processService;

    @PostMapping("/sim-pki-v3/{id}")
    public ResponseEntity<MessageDto> signSimPkiV3(
            @PathVariable("id") int id,
            @Valid @RequestBody SimPkiV3Dto simPkiDto) {
        MessageDto response = null;

        //network code = viettel/Ban co yeu --> ky qua api v2
        if(simPkiDto.getNetworkCode().equals("2")
            || simPkiDto.getNetworkCode().equals("bcy")){
            response = signService
                    .simPkiSignV2(
                            id, simPkiDto.getMobile(),
                            simPkiDto.getNetworkCode(),
                            simPkiDto.getPrompt(),
                            simPkiDto.getReason()
                    );
        }else{
            response = signService
                    .simPkiSignV3(
                            id, simPkiDto.getMobile(),
                            simPkiDto.getNetworkCode(),
                            simPkiDto.getPrompt(),
                            simPkiDto.getReason(),
                            simPkiDto.getImageBase64()
                    );
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sim-pki/{id}")
    public ResponseEntity<MessageDto> signSimPkiV2(
            @PathVariable("id") int id,
            @Valid @RequestBody SimPkiV2Dto simPkiDto) {
        MessageDto response = signService
                .simPkiSignV2(
                        id, simPkiDto.getMobile(),
                        simPkiDto.getNetworkCode(),
                        simPkiDto.getPrompt(),
                        simPkiDto.getReason()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signatureInfo")
    public List<SignatureInfoDto> signatureInfo(@RequestParam("file") MultipartFile file) {
        return signService.signatureInfo(file);
    }
    
    @PostMapping("/hsm/{id}")
    public ResponseEntity<MessageDto> hsm(
            @PathVariable("id") int id,
            @Valid @RequestBody HsmSignRequest hsmSignRequest) {

        final var response = signService.hsmSign(id, hsmSignRequest);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/multi/hsm")
    public List<MultiHsmSignResponse> signMultiHsm(@RequestBody @Valid MultiHsmSignReq req) {
        if (!signService.isValidMutliHsmSignReq(req)) {
            return List.of(new MultiHsmSignResponse(0, MessageDto
                    .builder()
                    .message("Tax code do not match!")
                    .success(false)
                    .build()));
        }

        List<MultiHsmSignResponse> res = new ArrayList<>();

        var hsmReq = (MultiHsmSignReq) req;
        for (int recipient : hsmReq.getRecipients()) {
            var hsmRes = signService.hsmSign(recipient, hsmReq.getHsmSignRequest());
            log.info("hsmRes: {}", hsmRes);
            res.add(new MultiHsmSignResponse(recipient, hsmRes));
            if (hsmRes.isSuccess()) {
                processService.approval(recipient, Collections.emptyList(), new ProcessApprovalDto());
            }
        }

        return res;
    }

    @GetMapping("/multi/usb-token/page-info")
    public List<MyProcessResPageInfo> getPageInfo(@RequestParam int documentId) {
        return signService.getPageInfo(documentId);
    }

    @PostMapping("/multi/usb-token")
    public List<MultiUsbTokenSignResponse> signMultiUsbToken(@RequestBody @Valid List<MultiUsbTokenSignReq> reqList) {
        List<MultiUsbTokenSignResponse> res = new ArrayList<>();
        for (var req : reqList) {
            var signRes = processService.digitalSign(req.getFieldId(), req.getDigitalSign());
            res.add(new MultiUsbTokenSignResponse(
                        req.getFieldId(),
                        MessageDto.builder().success(signRes.isPresent()).build()
                    )
            );
        }

        return res;
    }
}
