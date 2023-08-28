package com.vhc.ec.contract.service;

import java.lang.reflect.Type;
import java.util.*;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.ContractApproveType;
import com.vhc.ec.contract.definition.ParticipantType;
import com.vhc.ec.contract.definition.RecipientRole;
import com.vhc.ec.contract.definition.RecipientStatus;
import com.vhc.ec.contract.dto.MessageDto;
import com.vhc.ec.contract.dto.ParticipantDto;
import com.vhc.ec.contract.dto.RecipientDto;
import com.vhc.ec.contract.dto.WorkflowDto;
import com.vhc.ec.contract.entity.Field;
import com.vhc.ec.contract.entity.Participant;
import com.vhc.ec.contract.entity.Recipient;
import com.vhc.ec.contract.repository.FieldRepository;
import com.vhc.ec.contract.repository.ParticipantRepository;
import com.vhc.ec.contract.repository.RecipientRepository;
import com.vhc.ec.contract.util.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final RecipientRepository recipientRepository;
    private final FieldRepository fieldRepository;
    private final CustomerService customerService;
    private final BpmService bpmService;

    private final ModelMapper modelMapper;

    /**
     * Thêm mới thông tin thành phần tham gia vào ký hợp đồng
     *
     * @param participantDtoCollection {@link ParticipantDto} Thông tin chi tiết của thành phần tham gia ký hợp đồng
     * @param contractId               Mã số tham chiếu tới hợp đồng
     * @param customerId               Mã số tham chiếu tới khách hàng xử lý
     * @return Danh sách thành phần tham gia
     */
    @Transactional
    public Collection<ParticipantDto> create(Collection<ParticipantDto> participantDtoCollection, int contractId, int customerId) {
        try {
            // tự động sinh tên đăng nhập và mật khẩu cho người dùng không định danh
            participantDtoCollection.forEach(
                    participantDto -> participantDto.getRecipients().forEach(recipientDto -> {
                        boolean exists = checkCustomerByEmail(recipientDto.getEmail());

                        if (!exists) {
                            String pwd = StringUtil.generatePwd();
                            recipientDto.setUsername(recipientDto.getEmail());
                            recipientDto.setPassword(pwd);
                        }
                    }));

            //final var participantCollection = fromDTO(participantDtoCollection);
            final Collection<Participant> participantCollection = new ArrayList<>();
            for (var participantDto : participantDtoCollection) {
                var participant = new Participant();
                BeanUtils.copyProperties(participantDto, participant,
                        "type", "status", "recipients"
                );

                for (var recipientDto : participantDto.getRecipients()) {
                    var recipient = new Recipient();
                    BeanUtils.copyProperties(
                            recipientDto, recipient,
                            "fields", "signType", "role", "status"
                    );

                    // convert sign type <!--> string
                    var objectMapper = new ObjectMapper();
                    var signType = objectMapper.writeValueAsString(recipientDto.getSignType());
                    var role = modelMapper.map(recipientDto.getRole(), RecipientRole.class);
                    var status = modelMapper.map(recipientDto.getStatus(), RecipientStatus.class);

                    recipient.setSignType(signType);
                    recipient.setRole(role);
                    recipient.setStatus(status);
                    recipient.setCreatedBy(customerId);
                    recipient.setUpdatedBy(customerId);
                    recipient.setCreatedAt(new Date());
                    recipient.setUpdatedAt(new Date());

                    participant.addRecipient(recipient);
                }

                var type = modelMapper.map(participantDto.getType(), ParticipantType.class);
                var status = modelMapper.map(participantDto.getStatus(), BaseStatus.class);

                participant.setContractId(contractId);
                participant.setType(type);
                participant.setStatus(status);
                participant.setCreatedAt(new Date());
                participant.setCreatedBy(customerId);

                participantCollection.add(participant);
            }

        /*
        // lưu thông tin của người xử lý hồ sơ
        participantCollection.forEach(participant -> {
            final Set<Recipient> recipients = Set.copyOf(
                    participant.getRecipients()
            );
            final var now = new Date();

            recipients.forEach(recipient -> {
                recipient.setCreatedAt(now);
                recipient.setCreatedBy(customerId);
                recipient.setUpdatedAt(now);
                recipient.setUpdatedBy(customerId);
            });

            participant.setContractId(contractId);
            participant.setCreatedAt(now);
            participant.setCreatedBy(customerId);
            participant.getRecipients().clear();

            recipients.forEach(participant::addRecipient);
        });
        */

            final var participantList = participantRepository.saveAll(participantCollection);
            
            for(Participant participant: participantList) {
            	Set<Recipient> recipientSet = participant.getRecipients();
            	
            	for(Recipient recipient : recipientSet) {
            		Collection<Field> fieldCollection = fieldRepository.findAllByRecipientId(recipient.getId());
            		for(Field field : fieldCollection) {
            			recipient.addField(field);
            		}
            	} 
             }

            Type typeToken = new TypeToken<Collection<Recipient>>() {
            }.getType();

            return modelMapper.map(
                    participantList,
                    typeToken
            );
        } catch (Exception e) {
            log.error("can't add new participants", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return Collections.emptyList();
    }

    /**
     * Cập nhật thông tin của thành phần tham gia xử lý hồ sơ
     *
     * @param participantId  Mã số tham chiếu tới thành phần tham gia xử lý hồ sơ
     * @param participantDto Thông tin chi tiết của thành phần tham gia xử lý hồ sơ
     * @return Thông tin chi tiết của thành phần tham gia xử lý hồ sơ
     */
    @Transactional
    public Optional<ParticipantDto> update(int participantId, ParticipantDto participantDto) {
        try {
            final var participantOptional = participantRepository
                    .findById(participantId);

            if (participantOptional.isEmpty()) {
                return Optional.empty();
            }

            final var participant = participantOptional.get();

            // xoá thông tin của khách hàng xử lý hồ sơ
            final var recipients = participant.getRecipients();

            final var deletedIdList = new ArrayList<Integer>();
            recipients.forEach(recipient -> {
                boolean exists = participantDto.getRecipients().stream().noneMatch(recipientDto ->
                        (recipientDto.getId() != null) && (recipientDto.getId() == recipient.getId().intValue())
                );

                if (exists) {
                    deletedIdList.add(recipient.getId());
                }
            });

            // xoá toàn bộ thành phần tham dự
            //recipientRepository.deleteAllById(deletedIdList);
            deletedIdList.forEach(recipientId -> {
                // set null for fields if present
                var fieldCollection = fieldRepository.findAllByRecipientId(recipientId);
                if (fieldCollection != null && fieldCollection.size() > 0) {
                    for (var field : fieldCollection) {
                        field.setRecipient(null);
                        fieldRepository.save(field);
                    }
                }

                // delete recipient
                final var recipientToDelete = recipientRepository.findById(recipientId);
                recipientToDelete.ifPresent(value -> participant.getRecipients().removeIf(recipient -> recipient.getId().equals(value.getId())));
            });

            // cập nhật thông tin của thành phần tham gia xử lý hồ sơ
            participant.setName(participantDto.getName());
            participant.setOrdering(participantDto.getOrdering());
            participant.setContractId(participantDto.getContractId());

            //
            final var typeOptional = Arrays.stream(ParticipantType.values()).filter(participantType -> participantType.getDbVal() == participantDto.getType())
                    .findFirst();
            typeOptional.ifPresent(participant::setType);

            final var statusOptional = Arrays.stream(BaseStatus.values()).filter(status -> status.ordinal() == participantDto.getStatus())
                    .findFirst();
            statusOptional.ifPresent(participant::setStatus);

            // thêm mới, cập nhật thành phần tham gia xử lý hồ sơ
            for (var recipientDto : participantDto.getRecipients()) {
                // tự sinh thông tin đăng nhập cho khách hàng không định danh
                boolean exists = checkCustomerByEmail(recipientDto.getEmail());
                if (!exists) {
                    String pwd = StringUtil.generatePwd();
                    recipientDto.setUsername(recipientDto.getEmail());
                    recipientDto.setPassword(pwd);
                } else {
                    recipientDto.setPassword(null);
                }

                var recipient = modelMapper.map(recipientDto, Recipient.class);
                var objectMapper = new ObjectMapper();
                var signType = objectMapper.writeValueAsString(recipientDto.getSignType());
                recipient.setSignType(signType);

                // recipient.setParticipant(participant);

                if (recipientDto.getId() != null && recipientDto.getId() != 0) {
                    final var recipientOptional = recipientRepository
                            .findById(recipientDto.getId());

                    if (recipientOptional.isPresent()) {
                        // cập nhật thông tin thành phần tham gia xử lý hồ sơ
                        recipient = recipientOptional.get();

                        recipient.setName(recipientDto.getName());
                        recipient.setEmail(recipientDto.getEmail());
                        recipient.setPhone(recipientDto.getPhone());
                        recipient.setUsername(recipientDto.getUsername());
                        recipient.setPassword(recipientDto.getPassword());
                        recipient.setOrdering(recipientDto.getOrdering());
                        recipient.setFromAt(recipientDto.getFromAt());
                        recipient.setDueAt(recipientDto.getDueAt());
                        recipient.setSignAt(recipientDto.getSignAt());
                        recipient.setProcessAt(recipientDto.getProcessAt());
                        recipient.setNotifyType(recipientDto.getNotifyType());
                        recipient.setRemind(recipientDto.getRemind());
                        recipient.setRemindDate(recipientDto.getRemindDate());
                        recipient.setRemindMessage(recipientDto.getRemindMessage());
                        recipient.setReasonReject(recipientDto.getReasonReject());
                        recipient.setTemplateRecipientId(recipientDto.getTemplateRecipientId());
                        // recipient.setParticipant(participant);

                        //
                        final var roleOptional = Arrays.stream(RecipientRole.values()).filter(recipientRole -> recipientRole.getDbVal() == recipientDto.getRole())
                                .findFirst();
                        if (roleOptional.isPresent()) {
                            recipient.setRole(roleOptional.get());
                        }

                        //
                        final var recipientStatusOptional = Arrays.stream(RecipientStatus.values()).filter(status -> status.getDbVal() == recipientDto.getStatus())
                                .findFirst();
                        if (recipientStatusOptional.isPresent()) {
                            recipient.setStatus(recipientStatusOptional.get());
                        }

                        //
                        if (recipientDto.getSignType() != null && recipientDto.getSignType().size() > 0) {
                            objectMapper = new ObjectMapper();
                            try {
                                String signTypeStr = objectMapper.writeValueAsString(recipientDto.getSignType());
                                recipient.setSignType(signTypeStr);
                            } catch (JsonProcessingException e) {
                                log.error("can't parse sign type to string", e);
                            }
                        }

                        participant.addRecipient(recipient);
                    }
                } else {
                    participant.addRecipient(recipient);
                }
            }

            final var updated = participantRepository.save(participant);
            return Optional.of(
                    modelMapper.map(updated, ParticipantDto.class)
            );
        } catch (Exception e) {
            log.error("can't update participant.", e);
        }

        return Optional.empty();
    }

    /**
     * Lấy danh sách thành phần tham gia ký hợp đồng
     *
     * @param contractId Mã số tham chiếu tới hợp đồng
     * @return Danh sách thành phần tham gia ký hợp đồng
     */
    public Collection<ParticipantDto> findByContract(int contractId) {
        var participants = participantRepository.findByContractIdOrderByOrderingAsc(contractId);
        for (var p : participants) {
            if (p.getRecipients() == null || p.getRecipients().size() == 0) {
                p.setRecipients(new HashSet<>(recipientRepository.findAllByParticipantId(p.getId())));
            }
        }
        return toDTO(
                participants
        );
    }


    /**
     * Lấy thông tin thành phần tham gia ký hợp đồng
     *
     * @param id Mã thành phần tham gia
     * @return Thông tin chi tiết thành phần tham gia
     */
    public Optional<ParticipantDto> getById(int id) {
        Optional<Participant> participantOptional = participantRepository
                .findById(id);

        return participantOptional.map(
                participant -> modelMapper.map(participant, ParticipantDto.class)
        );
    }

    /**
     * Cập nhật thông tin của người xử lý hồ sơ, do người điều phối thực hiện
     *
     * @param participantId          Mã tham chiếu tới thành phần tham gia xử lý hồ sơ
     * @param recipientDtoCollection Danh sách người xử lý hồ sơ
     * @return Thông tin chi tiết về thành phần tham gia xử lý hồ sơ
     */
    @Transactional
    public Optional<ParticipantDto> updateRecipientForCoordinator(
    		int customerId,
            int participantId,
            int recipientId,
            Collection<RecipientDto> recipientDtoCollection) {
        try {
        	final var participantOptional = participantRepository.findById(participantId);

            if (participantOptional.isPresent()) {
                final var participant = participantOptional.get();
                
                //xóa toàn bộ khách hàng xử lý hồ sơ
                participant.getRecipients()
                       .removeIf(recipient -> true);
                
                //Thêm mới khách hàng xử lý
                for(var recipientDto : recipientDtoCollection) {
                	var recipient = new Recipient();
                    BeanUtils.copyProperties(
                            recipientDto, recipient,
                            "fields", "signType", "role", "status"
                    );
                    
                    // convert sign type <!--> string
                    var objectMapper = new ObjectMapper();
                    var signType = objectMapper.writeValueAsString(recipientDto.getSignType());
                    var role = modelMapper.map(recipientDto.getRole(), RecipientRole.class);
                    var status = modelMapper.map(recipientDto.getStatus(), RecipientStatus.class);

                    recipient.setSignType(signType);
                    recipient.setRole(role);
                    recipient.setStatus(status);
                    recipient.setCreatedBy(customerId);
                    recipient.setUpdatedBy(customerId);
                    recipient.setCreatedAt(new Date());
                    recipient.setUpdatedAt(new Date());

                    participant.addRecipient(recipient);
                }
                
                
                final var updated = participantRepository.save(participant);

                final var recipientOptional = recipientRepository.findById(recipientId);
                if (recipientOptional.isPresent()) {
                    final var recipient = recipientOptional.get();

                    // update recipient status
                    recipient.setStatus(RecipientStatus.APPROVAL);
                    recipient.setProcessAt(new Date());
                    recipientRepository.save(recipient);

                    // call bpmn
                    var workflowDto = WorkflowDto.builder()
                            .contractId(participant.getContractId())
                            .approveType(ContractApproveType.APPROVAL.getDbVal())
                            .actionType(recipient.getRole().getDbVal())
                            .participantId(updated.getId())
                            .recipientId(recipient.getId())
                            .build();

                    bpmService.startWorkflow(workflowDto);
                }

                return Optional.ofNullable(
                        modelMapper.map(updated, ParticipantDto.class)
                );
            }
        }catch (Exception e) {
			// TODO: handle exception
		}

        return Optional.empty();
    }

    /**
     * Mapping dữ liệu từ DTO sang ENTITY
     *
     * @param participantDtoCollection {@link ParticipantDto}
     * @return {@link Participant}
     */
    private Collection<Participant> fromDTO(Collection<ParticipantDto> participantDtoCollection) {
        final var typeToken = new TypeToken<Collection<Participant>>() {
        }.getType();

        return modelMapper.map(
                participantDtoCollection,
                typeToken
        );
    }

    /**
     * Mapping dữ liệu từ ENTITY sang DTO
     *
     * @param participantCollection {@link Participant}
     * @return {@link ParticipantDto}
     */
    private Collection<ParticipantDto> toDTO(Collection<Participant> participantCollection) {
        Type typeToken = new TypeToken<Collection<ParticipantDto>>() {
        }.getType();

        return modelMapper.map(participantCollection, typeToken);
    }

    /**
     * Kiểm tra người xử lý hồ sơ có phải khách hàng hay không
     *
     * @param email Địa chỉ email của người xử lý hồ sơ
     * @return Tồn tại / Không tồn tại
     */
    private boolean checkCustomerByEmail(String email) {
        return customerService.findCustomerByEmail(email);
    }
    
    /**
     * 
     * @param id
     * @return
     */
    @Transactional
    public MessageDto delete(int id) {
        try {
            // Xóa thành phần tham gia
            //fieldRepository.deleteByContractId(id);

            // Xóa mẫu hợp đồng trạng thái bản nháp
            participantRepository.deleteById(id);
        } catch (Exception e) {
            log.error("can't delete participant id = " + id, e);

            return MessageDto.builder()
                    .success(false)
                    .message("E01")
                    .build();
        }

        return MessageDto.builder()
                .success(true)
                .message("E00")
                .build();
    }
}
