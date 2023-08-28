package com.vhc.ec.contract.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import com.vhc.ec.contract.dto.*;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.definition.ContractStatus;
import com.vhc.ec.contract.service.BpmService;
import com.vhc.ec.contract.service.ContractService;
import com.vhc.ec.contract.service.CustomerService;
import com.vhc.ec.contract.service.ProcessService;
import com.vhc.ec.contract.service.RecipientService;
import com.vhc.ec.contract.service.ReviewContractService;
import com.vhc.ec.contract.util.StringUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Định nghĩa end-point liên quan tới hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/contracts")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class ContractController {

    private final ContractService contractService;
    private final RecipientService recipientService;
    private final ProcessService processService;
    private final BpmService bpmService;
    private final CustomerService customerService;
    private final ReviewContractService reviewContractService;

    /**
     * Thêm mới hợp đồng của khách hàng
     *
     * @param customerUser {@link CustomerUser} Thông tin của khách hàng tạo hợp đồng
     * @param contractDto  {@link ContractDto} Đối tượng hợp đồng cần lưu trữ
     * @return {@link ContractDto} Đối tượng hợp đồng đã được lưu trữ thành công
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractDto create(
            @CurrentCustomer CustomerUser customerUser,
            @Valid @RequestBody ContractDto contractDto) {
        return contractService.create(contractDto, customerUser.getId());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractDto> update(@PathVariable("id") int id, @Valid @RequestBody ContractDto contractDto) {
        final var contractOptional = contractService.update(id, contractDto);

        return contractOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.badRequest().build()
        );
    }

    /**
     * Cập nhật trạng thái của hợp đồng
     *
     * @param id     Mã hợp đồng
     * @param status Trạng thái mới của hợp đồng
     * @return Thông tin chi tiết của hợp đồng
     */
    @PostMapping(value = {"/{id}/change-status/{newStatus}", "/internal/{id}/change-status/{newStatus}"})
    public ResponseEntity<ContractDto> changeStatus(@PathVariable("id") int id, @PathVariable("newStatus") int status,
                                                    @RequestBody @Valid Optional<ContractChangeStatusRequest> request) {
        final var contractOptional = contractService.changeStatus(
                id, status, request.orElse(null)
        );

        //
        if (contractOptional.isPresent()) {
            final var contractStatusOptional = Arrays.stream(ContractStatus.values())
                    .filter(contractStatus -> contractStatus.getDbVal().equals(status))
                    .findFirst();

            if (contractStatusOptional.isPresent()) {
                WorkflowDto workflowDto;
                Optional<MessageDto> messageDtoOptional;

                switch (contractStatusOptional.get()) {
                    case CREATED: // trường hợp chuyển từ trạng thái hợp đồng nháp sang tạo hợp đồng
                        // cập nhật trường mã hợp đồng tới hệ thống lưu trữ nội dung
                        processService.byPassContractNo(id);

                        // cập nhật trường contract UID tới hệ thống lưu trữ nội dung
                        processService.byPassContractUid(id);

                        workflowDto = WorkflowDto
                                .builder()
                                .contractId(id)
                                .approveType(0)
                                .recipientId(0)
                                .participantId(0)
                                .build();
                        // Khởi tạo luồng xử lý HĐ
                        //log.info("start call bpm service for contract: " + id);
                        messageDtoOptional = bpmService.startWorkflow(workflowDto);

                        // Cập nhật trạng thái HĐ thành PROCESSING
                        if (messageDtoOptional.isPresent() && messageDtoOptional.get().isSuccess()) {
                            //log.info("update status");
                            contractService.changeStatus(id, ContractStatus.PROCESSING.getDbVal(), null);
                            var orgId = contractOptional.get().getOrganizationId();
                            log.info("decreaseNumberOfContracts {}", orgId);
                            try {
                                customerService.decreaseNumberOfContracts(orgId);
                            } catch (Exception ex) {
                                log.error("error: {}", ex);
                            }

                        }
                        break;
                    case CANCEL:
                        workflowDto = WorkflowDto
                                .builder()
                                .contractId(id)
                                .approveType(3)
                                .recipientId(0)
                                .participantId(0)
                                .build();
                        // Khởi tạo luồng huỷ HĐ
                        bpmService.startWorkflow(workflowDto);
                        break;
                }
            }
        }

        return contractOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Khởi chạy quy trình xử lý nghiệp vụ BPM
     *
     * @param id Mã hợp đồng
     * @return Thông tin về hợp đồng đã đưa vào quy trình xử lý nghiêp vụ
     */
    @PutMapping("/{id}/start-bpm")
    public ResponseEntity<ContractDto> startBPMWorkflow(@PathVariable("id") int id) {
        final var contractOptional = contractService.startBPMWorklfow(id);

        return contractOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Lấy thông tin chi tiết của hợp đồng
     *
     * @param id Mã hợp đồng
     * @return {@link ContractDto} Thông tin chi tiết của hợp đồng
     */
    @GetMapping(value = {"/{id}", "/internal/{id}"})
    public ResponseEntity<ContractDto> findById(@PathVariable int id) {
        final var contractDtoOptional = contractService.findById(id);

        //
        if (contractDtoOptional.isPresent()) {
            final var contractDto = contractDtoOptional.get();

            if (contractDto.getRefs() != null) {
                contractDto.getRefs().forEach(referenceDto -> {
                    final var tempOptional = contractService.findById(
                            referenceDto.getRefId()
                    );

                    if (tempOptional.isPresent()) {
                        final var temp = tempOptional.get();

                        final var contractId = temp.getId();
                        final var refName = temp.getName();

                        referenceDto.setContractId(contractId);
                        referenceDto.setRefName(refName);
                    }
                });
            }

            return ResponseEntity.ok(contractDto);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bpmn-flow/{id}")
    public BpmnFlowRes getBpmnFlow(@PathVariable int id) {
        return contractService.getBpmnFlow(id);
    }
    /**
     * Tìm kiếm hợp đồng của tôi (hợp đồng do người dùng tạo)
     *
     * @param customerUser {@link CustomerUser} Người dùng được xác thực qua api
     * @param contractType Loại hợp đồng
     * @param fromDate     Ngày tạo hợp đồng (từ ngày)
     * @param toDate       Ngày tạo hợp đồng (tới ngày)
     * @param status       Trạng thái của hợp đồng
     * @param pageable     Phân trang tìm kiếm
     * @return {@link PageDto <ContractDto>} Kết quả tìm kiếm
     */
    @GetMapping("/my-contract")
    public ResponseEntity<PageDto<ContractDto>> searchMyContract(@CurrentCustomer CustomerUser customerUser,
                                                                 @RequestParam(name = "type", required = false) Integer contractType,
                                                                 @RequestParam(name = "from_date", required = false)
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                         Date fromDate,
                                                                 @RequestParam(name = "to_date", required = false)
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                         Date toDate,
                                                                 @RequestParam(name = "status", required = false, defaultValue = "-1") Integer status,
                                                                 @RequestParam(required = false, defaultValue = "") String keyword,
                                                                 @RequestParam(required = false, name="contract_no") String contractNo,
                                                                 @RequestParam(name = "remain_day", required = false) Integer remainDay,
                                                                 Pageable pageable) {
        try { 
            //Lấy tổ chức hiện tại
        	Optional<OrganizationDto> organizationOptional = customerService.getOrganizationByCustomer(customerUser.getId());
        	
        	if(organizationOptional.isPresent()) {
        		OrganizationDto organization = organizationOptional.get();
        		
        		Date remainTime = null;
                if (remainDay != null) {
                    remainTime = DateUtils.addDays(new Date(), remainDay);
                }
                
        		PageDto<ContractDto> pageDto = contractService.searchMyContract(
        				organization.getId(),
                        customerUser.getId(),
                        contractType,
                        fromDate,
                        toDate,
                        status,
                        remainTime,
                        keyword,
                        contractNo,
                        pageable
                );

                return ResponseEntity.ok(pageDto);
        	} 
        } catch (Exception e) {
            log.error("unexpected error", e);
        }

        return ResponseEntity.noContent().build();
    }
    
    /**
     * Tìm kiếm hợp đồng của tôi (hợp đồng do người dùng tạo) thuộc tổ chức cũ
     *
     * @param customerUser {@link CustomerUser} Người dùng được xác thực qua api
     * @param contractType Loại hợp đồng
     * @param fromDate     Ngày tạo hợp đồng (từ ngày)
     * @param toDate       Ngày tạo hợp đồng (tới ngày)
     * @param status       Trạng thái của hợp đồng
     * @param pageable     Phân trang tìm kiếm
     * @return {@link PageDto <ContractDto>} Kết quả tìm kiếm
     */
    
    @GetMapping("/my-contract/organization-old")
    public ResponseEntity<PageDto<ContractDto>> searchMyContractOld(@CurrentCustomer CustomerUser customerUser,
                                                                 @RequestParam(name = "type", required = false) Integer contractType,
                                                                 @RequestParam(name = "name", required = false, defaultValue = "") String name,
                                                                 @RequestParam(name = "from_date", required = false)
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                         Date fromDate,
                                                                 @RequestParam(name = "to_date", required = false)
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                         Date toDate,
                                                                 @RequestParam(name = "status", required = false, defaultValue = "-1") Integer status,
                                                                 @RequestParam(name = "remain_day", required = false) Integer remainDay,
                                                                 Pageable pageable) {
        try {
        	//Lấy tổ chức hiện tại
        	Optional<OrganizationDto> organizationOptional = customerService.getOrganizationByCustomer(customerUser.getId());
        	
        	if(organizationOptional.isPresent()) {
        		OrganizationDto organization = organizationOptional.get();
        		
        		Date remainTime = null;
                if (remainDay != null) {
                    remainTime = DateUtils.addDays(new Date(), remainDay);
                }

                PageDto<ContractDto> pageDto = contractService.searchMyContractOld(
                		organization.getId(),
                        customerUser.getId(),
                        contractType,
                        name,
                        fromDate,
                        toDate,
                        status,
                        remainTime,
                        pageable
                );

                return ResponseEntity.ok(pageDto);
        	} 
        } catch (Exception e) {
            log.error("unexpected error", e);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Tìm kiếm hợp đồng do người dùng xử lý
     *
     * @param customerUser {@link CustomerUser} Người dùng được xác thực qua api
     * @param contractType Loại hợp đồng
     * @param fromDate     Ngày tạo hợp đồng (từ ngày)
     * @param toDate       Ngày tạo hợp đồng (tới ngày)
     * @param status       Trạng thái của hợp đồng
     * @param contractStatus Trạng thái của hợp đồng
     * @param keyword
     * @param pageable     Phân trang tìm kiếm
     * @return {@link PageDto <ContractDto>} Kết quả tìm kiếm
     */
    @GetMapping("/my-process")
    public ResponseEntity<PageDto<MyProcessResponse>> searchByEmail(@CurrentCustomer CustomerUser customerUser,
                                                                    @RequestParam(name = "type", required = false) Integer contractType,
                                                                    @RequestParam(name = "from_date", required = false)
                                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                            Date fromDate,
                                                                    @RequestParam(name = "to_date", required = false)
                                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                            Date toDate,
                                                                    @RequestParam(name = "status", required = false, defaultValue = "-1") Integer status,
                                                                    @RequestParam(required = false, defaultValue = "-1") Integer contractStatus,
                                                                    @RequestParam(required = false, name="contract_no") String contractNo,
                                                                    @RequestParam(required = false, defaultValue = "") String keyword,
                                                                    Pageable pageable) {
        try {
            final var pageDto = recipientService.searchByEmail(
                    customerUser.getEmail(),
                    contractType,
                    fromDate,
                    toDate,
                    status,
                    contractStatus,
                    contractNo,
                    keyword,
                    pageable
            );

            return ResponseEntity.ok(pageDto);
        } catch (Exception e) {
            log.error("unexpected error", e);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * lay ra danh sach cac hop dong co the ky nhieu
     * HĐ được tạo ra từ mẫu, tạo từ tổ chức của tôi hoặc tổ chức con
     * + Hình thức ký Usb token + Ký HSM
     *
     */
    @GetMapping("/my-contract/can-sign-multi")
    List<MyProcessResponse> listContractCanSignMulti(
                                               @RequestParam String platform,
                                               @CurrentCustomer CustomerUser customerUser) {
        return contractService.listContractCanSignMulti(platform, customerUser);
    }
    /**
     * Tìm kiếm hợp đồng của tổ chức (hợp đồng thuộc tổ chức của người dùng)
     *
     * @param organizationId Mã tổ chức
     * @param contractType   Loại hợp đồng
     * @param fromDate       Ngày tạo hợp đồng (từ ngày)
     * @param toDate         Ngày tạo hợp đồng (tới ngày)
     * @param status         Trạng thái của hợp đồng
     * @param pageable       Phân trang tìm kiếm
     * @return {@link PageDto <ContractDto>} Kết quả tìm kiếm
     */
    @GetMapping("/my-organization-contract")
    public ResponseEntity<PageDto<ContractDto>> searchMyOrgContract(@RequestParam(name = "organization_id", required = false) Integer organizationId,
                                                                    @RequestParam(name = "type", required = false) Integer contractType,
                                                                    @RequestParam(name = "name", required = false, defaultValue = "") String name,
                                                                    @RequestParam(name = "from_date", required = false)
                                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                            Date fromDate,
                                                                    @RequestParam(name = "to_date", required = false)
                                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                            Date toDate,
                                                                    @RequestParam(name = "status", required = false, defaultValue = "-1") Integer status,
                                                                    @RequestParam(name = "remain_day", required = false) Integer remainDay,
                                                                    Pageable pageable) {
        try {
            Date remainTime = null;
            if (remainDay != null) {
                remainTime = DateUtils.addDays(new Date(), remainDay);
            }

            PageDto<ContractDto> pageDto = contractService.searchMyOrgContract(
                    organizationId,
                    contractType,
                    name,
                    fromDate,
                    toDate,
                    status,
                    remainTime,
                    pageable
            );

            return ResponseEntity.ok(pageDto);
        } catch (Exception e) {
            log.error("unexpected error", e);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-org-and-descendant-contract")
    PageDto<ContractDto> findMyOrgAndDescendantContract(@RequestParam int organizationId,
                                                        @RequestParam(name = "type", required = false) Integer contractType,
                                                        @RequestParam(name = "from_date", required = false)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                Date fromDate,
                                                        @RequestParam(name = "to_date", required = false)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                Date toDate,
                                                        @RequestParam(name = "status", required = false, defaultValue = "-1") Integer status,
                                                        @RequestParam(name = "remain_day", required = false) Integer remainDay,
                                                        @RequestParam(required = false, defaultValue = "") String keyword,
                                                        Pageable pageable) {
        Date remainTime = null;
        if (remainDay != null) {
            remainTime = DateUtils.addDays(new Date(), remainDay);
        }

        return contractService.findMyOrgAndDescendantContract(organizationId, contractType, keyword, fromDate, toDate,
                status, remainTime, pageable
        );
    }

    @GetMapping({"/count-my-org-and-descendant-contract", "/internal/count-my-org-and-descendant-contract"})
    StatisticDto countMyOrgAndDescendantContract(@RequestParam int organizationId,
                                                 @RequestParam(name = "from_date", required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                         Date fromDate,
                                                 @RequestParam(name = "to_date", required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                         Date toDate
                                                 ) {

        return contractService.myOrgAnDescendantStatistic(organizationId, fromDate, toDate);
    }

    @PostMapping("/check-name-unique")
    public ResponseEntity<MessageDto> checkUniqueName(
            @Valid @RequestBody ContractNameUniqueRequest request) {
        final var contractOptional = contractService.findByName(request.getName());

        var message = MessageDto.builder()
                .success(true)
                .message("contract name not exists")
                .build();
        if (contractOptional.isPresent()) {
            message = MessageDto.builder()
                    .success(false)
                    .message("contract name existed")
                    .build();
        }

        return ResponseEntity.ok(message);
    }

    @PostMapping("/check-code-unique")
    public MessageDto checkUniqueCode(
            @Valid @RequestBody ContractCodeUniqueRequest request) {

        return contractService.checkUniqueCode(request.getCode(), request.getOrganizationId());
    }

    /**
     * clone contract
     *
     * @param customerUser authorized user id
     * @param contractId   contract id need to copy
     * @return {@link ContractDto}
     */
    @GetMapping("/clone/{id}")
    public ResponseEntity<ContractDto> clone(@CurrentCustomer CustomerUser customerUser, @PathVariable("id") Integer contractId) {
        var contractOptional = contractService.clone(customerUser.getId(), contractId);

        return contractOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.internalServerError().build()
        );
    }

    /**
     * Xóa hợp đồng trạng thái 0: bản nháp
     *
     * @param id Mã hợp đồng
     * @return {@link MessageDto}
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDto> delete(@PathVariable int id) {
        final var delete = contractService.delete(id);

        return ResponseEntity.ok(delete);
    }
    
    @GetMapping("/check-contract-exist")
    public ResponseEntity<MessageDto> checkContractExist(@RequestParam(name = "id", required = false) Integer id) {
        final var contractOptional = contractService.getContractProcessByUser(id);

        var message = MessageDto.builder()
                .success(true)
                .message("users with no pending contracts")
                .build();
        
        if (contractOptional.isPresent()) {
        	int count = contractOptional.get();
        	if(count > 0) {
        		message = MessageDto.builder()
                        .success(false)
                        .message("users with pending contracts")
                        .build();
        	} 
        }

        return ResponseEntity.ok(message);
    }
    
    @PutMapping("/ceca-push/{id}/{status}")
    public ResponseEntity<MessageDto> updateCeCAPush(@PathVariable("id") int contractId, @PathVariable("status") int ceCAPush) {
        final var contractOptional = contractService.updateCeCAPush(contractId, ceCAPush);

        return ResponseEntity.ok(contractOptional);
    }

    @GetMapping({"/total-contracts", "/internal/total-contracts"})
    public AbstractTotalDto getTotalContract(@RequestParam int orgId) {
        return  contractService.countTotalContract(orgId);
    }
     
    @PostMapping("/check-mst-exist")
    public ResponseEntity<MessageDto> checkMSTExist(@RequestBody @Valid CheckMSTRequest checkMSTRequest) {
    	var UIDToCert = StringUtil.getMSTFromCert(checkMSTRequest.getCertB64()); 
    	
    	var message = MessageDto.builder()
                .success(true)
                .message("MST/CCCD valid")
                .build();
    	
    	if(UIDToCert.isPresent()) {
    		String uid = UIDToCert.get();
    		String mstToCert = uid.split(":")[1].toString();
			
	    	if(!mstToCert.equals(checkMSTRequest.getMst())) {
	    		message = MessageDto.builder()
	                    .success(false)
	                    .message("MST/CCCD not valid")
	                    .build();
	    	}
    	} 

        return ResponseEntity.ok(message);
    }
    
    @PostMapping("/review/{id}")
    public ResponseEntity<UploadCeCAResponse> reviewSignBox(
    		@PathVariable("id") Integer recipientId, 
    		@RequestBody @Valid ReviewContractRequest reviewContractRequest,
    		HttpServletRequest request) {
    	final var filePathOptional = reviewContractService.reviewSignBox(recipientId, reviewContractRequest.getImageBase64());
    	
    	var message = UploadCeCAResponse.builder()
    			.success(false) 
    			.message("Add image to file error.")
    			.build();
    			
    	if(filePathOptional.isPresent()) {
    		message = UploadCeCAResponse.builder()
    				.success(true)
    				.message("Add image to file success.")
    				.filePath(filePathOptional.get())
    				.build();
    	} 
    	
    	return ResponseEntity.ok(message);
    }

    //Hủy nhiều hợp đồng
    @PostMapping("/multi-cancel")
    public ResponseEntity<MessageDto> multiCancel(@RequestBody @Valid MultiCancelContractDto request) {
        var contractIds = request.getContractIds();
        var reason = new ContractChangeStatusRequest();
        reason.setReason(request.getReason());

        for(var contractId : contractIds){
            try {
                final var contractOptional = contractService.changeStatus(
                        contractId, ContractStatus.CANCEL.getDbVal(), reason
                );

                if (contractOptional.isPresent()) {
                    final var contractStatusOptional = Arrays.stream(ContractStatus.values())
                            .filter(contractStatus -> contractStatus.getDbVal().equals(ContractStatus.CANCEL.getDbVal()))
                            .findFirst();

                    if (contractStatusOptional.isPresent()) {
                        WorkflowDto workflowDto;
                        Optional<MessageDto> messageDtoOptional;

                        workflowDto = WorkflowDto
                                .builder()
                                .contractId(contractId)
                                .approveType(3)
                                .recipientId(0)
                                .participantId(0)
                                .build();

                        // Khởi tạo luồng huỷ HĐ
                        bpmService.startWorkflow(workflowDto);
                    }
                }
            }catch (Exception e){
                log.error("Can't cancel contract id = {} : {}", contractId, e.toString());
            }
        }

        return ResponseEntity.ok(
                MessageDto.builder()
                .success(true)
                .message("Cancel contract success.")
                .build());
    }

    @PostMapping("/check-view-contract/{id}")
        public ResponseEntity<MessageDto> checkViewContract(
                @PathVariable("id") Integer contractId,
                @RequestBody CustomerDto customerUser ) {
        final var checkViewContract = contractService.checkViewContract(contractId, customerUser.getEmail());

        return ResponseEntity.ok(checkViewContract);
    }
}
