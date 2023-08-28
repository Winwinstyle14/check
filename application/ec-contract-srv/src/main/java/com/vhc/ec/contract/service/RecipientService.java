package com.vhc.ec.contract.service;

import java.time.LocalDateTime;
import java.util.*;

import com.vhc.ec.contract.definition.SignType;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.entity.Contract;
import com.vhc.ec.contract.entity.Participant;
import com.vhc.ec.contract.util.StringUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vhc.ec.contract.definition.ContractApproveType;
import com.vhc.ec.contract.definition.RecipientStatus;
import com.vhc.ec.contract.entity.Recipient;
import com.vhc.ec.contract.repository.FieldRepository;
import com.vhc.ec.contract.repository.RecipientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Dịch vụ xử lý nghiệp vụ của khách hàng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final FieldRepository fieldRepository;
    private final RestTemplate restTemplate;
    private final BpmService bpmService;
    private final CustomerService customerService;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    /**
     * Lấy thông tin người xử lý hồ sơ theo mã số tham chiếu tới thành phần tham gia.
     *
     * @param id Mã số tham chiếu tới thành phần tham gia
     * @return {@link RecipientDto} Thông tin chi tiết về thành phần tham gia xử lý hợp đồng
     */
    public Optional<RecipientDto> findById(int id) {
        final var recipientOptional = recipientRepository
                .findById(id);

        return recipientOptional.map(recipient -> modelMapper.map(recipient, RecipientDto.class));

    }

    /**
     * Tìm kiếm hợp đồng được xử lý bởi người dùng
     *
     * @param email    Địa chỉ email của người xử lý
     * @param typeId   Loại hợp đồng
     * @param fromDate Ngày tạo (từ ngày)
     * @param toDate   Ngày tạo (tới ngày)
     * @param status   Trạng thái
     * @param pageable Thông tin phân trang
     * @return {@link PageDto <MyProcessResponse>} Dữ liệu trả về
     */
    public PageDto<MyProcessResponse> searchByEmail(
            String email,
            Integer typeId,
            Date fromDate,
            Date toDate,
            Integer status,
            Integer contractStatus,
            String contractNo,
            String keyword,
            Pageable pageable
    ) {
        try {
            contractNo = "".equals(contractNo) ? null : contractNo;
            keyword = "".equals(keyword) ? null : keyword;

            final var recipientPage = recipientRepository.searchByEmailAddress(
                    email,
                    typeId,
                    fromDate,
                    toDate,
                    status,
                    contractStatus,
                    contractNo,
                    keyword,
                    pageable
            );

            recipientPage.forEach(r -> {
                if (!StringUtils.hasText(r.getSignType())) {
                    r.setSignType("[]");
                }
            });
            return modelMapper.map(
                    recipientPage, new TypeToken<PageDto<MyProcessResponse>>() {
                    }.getType()
            );
        } catch (Exception e) {
            log.error(
                    String.format(
                            "can't search my contract {\"email\": %s, \"typeId\": %d, \"contractNo\": \"%s\", \"fromDate\": \"%s\", \"toDate\": \"%s\", \"status\": %d}",
                            email, typeId, null, fromDate, toDate, status
                    ),
                    e
            );
        }

        return PageDto.<MyProcessResponse>builder()
                .totalPages(0)
                .totalElements(0)
                .content(Collections.emptyList())
                .build();
    }

    /**
     * Khách hàng xác nhận đồng ý với những điều khoản trong hợp đồng
     *
     * @param recipientId                  Mã tham chiếu người xử lý hồ sơ
     * @param fieldUpdateRequestCollection Thông tin trường dữ liệu khách hàng cần cập nhật
     * @return Thông tin khách hàng đã được cập nhật thành công
     */
    @Transactional
    public Optional<RecipientDto> approval(
            int recipientId,
            Collection<FieldUpdateRequest> fieldUpdateRequestCollection,
            Date processAt
    ) {
        var recipientOptional = recipientRepository.findById(recipientId);

        if (recipientOptional.isPresent()) {
            var recipient = recipientOptional.get();

            fieldUpdateRequestCollection.forEach(fieldUpdateRequest -> {
                var fieldOptional = fieldRepository
                        .findById(fieldUpdateRequest.getId());

                if (fieldOptional.isPresent()) {
                    var field = fieldOptional.get();
                    field.setName(fieldUpdateRequest.getName());
                    field.setValue(fieldUpdateRequest.getValue());
                    field.setFont(fieldUpdateRequest.getFont());
                    field.setFontSize(fieldUpdateRequest.getFontSize());

                    fieldRepository.save(field);
                }
            });

            recipient.setProcessAt(processAt == null ? new Date() : processAt);
            recipient.setStatus(RecipientStatus.APPROVAL);
            Recipient updated = recipientRepository.save(recipient);

            log.info("start bpmn recipient: {}", recipientId);
            var workflowDto = WorkflowDto.builder()
                    .contractId(updated.getParticipant().getContractId())
                    .approveType(ContractApproveType.APPROVAL.getDbVal())
                    .actionType(recipient.getRole().getDbVal())
                    .participantId(updated.getParticipant().getId())
                    .recipientId(recipient.getId())
                    .build();

            bpmService.startWorkflow(workflowDto);

            var recipientDto = modelMapper.map(updated, RecipientDto.class);
            
            return Optional.of(
                    recipientDto
            );
        }

        return Optional.empty();
    }

    /**
     * Cập nhật trạng thái của khách hàng xử lý hồ sơ
     *
     * @param id     Mã tham chiếu khách hàng xử lý hồ sơ
     * @param reason Lý do từ chối duyệt hồ sơ
     * @return {@link RecipientDto} Thông tin của người xử lý hồ sơ đã được cập nhật
     */
    public Optional<RecipientDto> reject(int id, String reason) {
        final var recipientOptional = recipientRepository.findById(id);
        if (recipientOptional.isPresent()) {
            final var recipient = recipientOptional.get();
            recipient.setStatus(RecipientStatus.REJECT);
            recipient.setReasonReject(reason);
            recipient.setProcessAt(new Date());

            final var updated = recipientRepository.save(recipient);

            final var workflowDto = WorkflowDto.builder()
                    .contractId(updated.getParticipant().getContractId())
                    .approveType(ContractApproveType.REJECT.getDbVal())
                    .actionType(recipient.getRole().getDbVal())
                    .participantId(updated.getParticipant().getId())
                    .recipientId(recipient.getId())
                    .build();

            bpmService.startWorkflow(workflowDto);

            return Optional.of(modelMapper.map(updated, RecipientDto.class));
        }

        return Optional.empty();
    }

    /**
     * Cập nhật trạng thái của Recipient sang processing
     *
     * @param id Mã tham chiếu tới khách hàng xử lý hợp đồng
     * @return Thông tin khách hàng xử lý hợp đồng
     */
    public Optional<RecipientDto> processing(int id) {
        final var recipientOptional = recipientRepository.findById(id);
        if (recipientOptional.isPresent()) {
            final var recipient = recipientOptional.get();
            recipient.setStatus(RecipientStatus.PROCESSING);

            final var updated = recipientRepository.save(recipient);
            return Optional.of(modelMapper.map(updated, RecipientDto.class));
        }

        // cap nhat ngay bat dau su dung dich vu
        try {
            int orgId = recipientRepository.findOrgId(id).get();
            String url = String.format("lb://ec-admin-srv/api/v1/admin/organization/internal/%d/service/update-start-date", orgId);
            restTemplate.patchForObject(url, null, Void.class);

        } catch (Exception ex) {
            log.error("error", ex);
        }
        return Optional.empty();
    }
    
    /**
     * Cập nhật trạng thái người xử lý
     * 
     * @param id Mã tham chiếu tới khách hàng xử lý hợp đồng
     * @param newStatus Trạng thái cần cập nhật
     * @return
     */
    public Optional<RecipientDto> changeStatus(int id, int newStatus) {
    	final var recipientStatusOptional =
                Arrays.stream(RecipientStatus.values()).filter(cs -> cs.getDbVal() == newStatus).findFirst();
    	if(recipientStatusOptional.isPresent()) {
    		final var recipientOptional = recipientRepository.findById(id);
            if (recipientOptional.isPresent()) {
                final var recipient = recipientOptional.get();
                recipient.setStatus(recipientStatusOptional.get());

                final var updated = recipientRepository.save(recipient);
                return Optional.of(modelMapper.map(updated, RecipientDto.class));
            }
    	} 

        return Optional.empty();
    }

    /**
     * Cập nhật thông tin người xử lý
     * @param id recipient id
     * @param recipientDto
     * @return
     */
    public Optional<RecipientDto> update(int id, RecipientDto recipientDto) {
        final var recipientOptional = recipientRepository.findById(id);

        if (recipientOptional.isEmpty()) {
            return Optional.empty();
        }

        try {
            final var recipient = recipientOptional.get();

            boolean exists = customerService.findCustomerByEmail(recipientDto.getEmail());

            String pwd = null;
            if (!exists) {
                pwd = StringUtil.generatePwd();
                recipient.setUsername(recipientDto.getEmail());
                recipient.setPassword(pwd);
            }else {
                recipient.setUsername(null);
                recipient.setPassword(null);
            }

            recipient.setName(recipientDto.getName());
            recipient.setEmail(recipientDto.getEmail());
            recipient.setPhone(recipientDto.getPhone());
            recipient.setCardId(recipientDto.getCardId());
            recipient.setLoginBy(recipientDto.getLoginBy());

            if(recipientDto.getChangeNum() != null){
                recipient.setChangeNum(recipient.getChangeNum() + 1);
            } else{
                recipient.setChangeNum(1);
            }

            final var updated = recipientRepository.save(recipient);

            //Gửi thông báo
            try {
                if(recipient.getStatus().getDbVal() == RecipientStatus.PROCESSING.getDbVal()){
                    Participant participant = recipient.getParticipant();
                    Contract contract = participant.getContract();

                    var customerDto = customerService.getCustomerById(contract.getCreatedBy());

                    OrganizationDto organizationDto = customerService.getOrganizationByCustomer(customerDto.getId()).get();

                    notificationService.notificationAuthorize(recipient, customerDto, organizationDto, contract, participant);
                }
            } catch (Exception e) {
                log.error("error send notify, recipientId={}", recipient.getId(), e);
            }

            return Optional.of(modelMapper.map(updated, RecipientDto.class));
        }catch (Exception e){
            log.error("Can't save recipient: {}", e);
        }

        return Optional.empty();
    }

    public boolean authorised(RecipientDto recipient, RecipientDto other){
        if (other.getAuthorisedBy() == null) {
            return false;
        }

        Integer authorisedBy = recipient.getAuthorisedBy();
        while (authorisedBy != null) {
            if (authorisedBy == other.getId()) {
                return true;
            }

            authorisedBy = recipientRepository.findById(authorisedBy).get().getAuthorisedBy();
        }

        return false;
    }

    public Optional<RecipientDto> getByIdRejectField(int id) {
        final var recipientOptional = recipientRepository.findById(id);

        if(recipientOptional.isPresent()){
            var recipient = recipientOptional.get();

            recipient.getFields().clear();

            return Optional.of(modelMapper.map(recipient, RecipientDto.class));
        }

        return Optional.empty();

    }
}
