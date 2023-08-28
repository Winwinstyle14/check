package com.vhc.ec.contract.thridparty;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.dto.tp.SignContractDto;
import com.vhc.ec.contract.repository.TemplateContractRepository;
import com.vhc.ec.contract.repository.TemplateShareRepository;
import com.vhc.ec.contract.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/tp/contracts")
@ApiVersion("1")
@Slf4j
@RequiredArgsConstructor
public class TpContractController {
    private final BatchService batchService;

    private final CustomerService customerService;

    private final ContractService contractService;

    private final ParticipantService participantService;

    private final TpContractService tpContractService;

    private final ProcessService processService;

    private final SignService signService;

    private final TemplateShareRepository templateShareRepository;

    private final TemplateContractRepository templateContractRepository;

    private final ModelMapper modelMapper;


    @GetMapping
    public PageDto<ContractDto> getContracts(@RequestParam(required = false) String email,
                                             @RequestParam(required = false) String phone,
                                             @RequestParam(required = false, defaultValue = "-1") Integer contractStatus,
                                             Pageable page) {

        return contractService.getContracts(email, phone, contractStatus, page);
    }

    @GetMapping("/get-templates")
    public Collection<TemplateContractDto> getTemplateContractByEmail(@RequestParam String email) {
        var customer = customerService.getCustomerByEmail(email);
        if (customer != null) {
            var templates = templateContractRepository.getTemplateList(customer.getId());
            return modelMapper.map(
                    templates, new TypeToken<Collection<TemplateContractDto>>() {
                    }.getType()
            );
        }

        return Collections.emptyList();
    }

    @GetMapping("/{id}/documents")
    public Collection<DocumentDto> getDocuments(@PathVariable int id) {
        return tpContractService.getDocuments(id);
    }

    @PutMapping("/sign/{id}")
    public ResponseEntity<?> sign(@PathVariable int id,
                                  @CurrentCustomer CustomerUser currentCustomer,
                                  @RequestBody SignContractDto signContractDto) {
        return tpContractService.sign(id, currentCustomer, signContractDto);
    }

    @PostMapping("/sign/{recipientId}/gen-otp")
    public OtpMessageDto genOtp(@PathVariable int recipientId,
                                @RequestBody GenOtpRequest otpRequest) {

        return tpContractService.genOtp(recipientId, otpRequest);
    }

    @PutMapping("/approval/{recipientId}")
    public ResponseEntity<RecipientDto> approval(
            @PathVariable("recipientId") int recipientId,
            @RequestBody @Valid Collection<FieldUpdateRequest> fieldUpdateRequestCollection) {
        return processService.approval(recipientId, fieldUpdateRequestCollection, new ProcessApprovalDto());
    }

    @GetMapping("/can-sign-multi")
    List<MyProcessResponse> listContractCanSignMulti(@RequestParam String platform,
                                                     @CurrentCustomer CustomerUser customerUser) {
        return contractService.listContractCanSignMulti(platform, customerUser);
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

    @PostMapping("/batch")
    public ResponseEntity<?> createBatchContract(
            @CurrentCustomer CustomerUser customerUser,
            @RequestParam int templateId,
            @RequestParam("file") MultipartFile multipartFile) {


        var failMessage = MessageDto.builder()
                .success(false)
                .build();

        var templateShare = templateShareRepository
                .findFirstByContractIdAndEmail(templateId, customerUser.getEmail())
                .orElse(null);

        var template = templateContractRepository
                .findById(templateId)
                .orElse(null);

        if (templateShare == null && (template != null && template.getCustomerId() != customerUser.getId())) {
            failMessage.setMessage("You are not allowed to perform this action!");
            return ResponseEntity.badRequest().body(failMessage);
        }

        var orgDto = customerService.getOrganizationByCustomer(customerUser.getId()).orElse(null);
        if (orgDto == null) {
            return ResponseEntity.badRequest().body(failMessage);
        }

        var validateDto = batchService.validate(templateId, multipartFile, orgDto.getId(), false).getBody();

        if (!validateDto.getSuccess()) {
            failMessage.setDetails(validateDto.getDetail());
            return ResponseEntity.badRequest().body(failMessage);
        }

        var contracts = batchService.process(customerUser, templateId, 2, validateDto.getTempFile());
        // lay thong tin participant
        for (var contract : contracts.getBody()) {
            contract.setParticipants(participantService.findByContract(contract.getId()));
        }
        return contracts;
    }
}
