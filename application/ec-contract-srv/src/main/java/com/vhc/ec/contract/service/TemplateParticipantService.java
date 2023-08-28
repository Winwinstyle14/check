package com.vhc.ec.contract.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.contract.definition.BaseStatus;
import com.vhc.ec.contract.definition.ParticipantType;
import com.vhc.ec.contract.definition.RecipientRole;
import com.vhc.ec.contract.definition.RecipientStatus;
import com.vhc.ec.contract.dto.MessageDto;
import com.vhc.ec.contract.dto.ParticipantDto;
import com.vhc.ec.contract.dto.TemplateParticipantDto;
import com.vhc.ec.contract.entity.TemplateField;
import com.vhc.ec.contract.entity.TemplateParticipant;
import com.vhc.ec.contract.entity.TemplateRecipient;
import com.vhc.ec.contract.repository.TemplateFieldRepository;
import com.vhc.ec.contract.repository.TemplateParticipantRepository;
import com.vhc.ec.contract.repository.TemplateRecipientRepository;

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
public class TemplateParticipantService {

    private final TemplateParticipantRepository participantRepository;
    private final TemplateFieldRepository fieldRepository;
    private final TemplateRecipientRepository recipientRepository;
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
    public Collection<TemplateParticipantDto> create(Collection<TemplateParticipantDto> participantDtoCollection, int contractId, int customerId) {
        try {
            final Collection<TemplateParticipant> participantCollection = new ArrayList<>();
            for (var participantDto : participantDtoCollection) {
                var participant = new TemplateParticipant();
                BeanUtils.copyProperties(participantDto, participant,
                        "type", "status", "recipients"
                );

                for (var recipientDto : participantDto.getRecipients()) {
                    var recipient = new TemplateRecipient();
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

            final var participantList = participantRepository.saveAll(participantCollection);

            for(TemplateParticipant participant: participantList) {
                Set<TemplateRecipient> recipientSet = participant.getRecipients();

                for(TemplateRecipient recipient : recipientSet) {
                    Collection<TemplateField> fieldCollection = fieldRepository.findAllByRecipientId(recipient.getId());
                    for(TemplateField field : fieldCollection) {
                        recipient.addField(field);
                    }
                }
            }

            return modelMapper.map(
                    participantList,
                    new TypeToken<Collection<TemplateParticipantDto>>() {}.getType()
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
    public Optional<TemplateParticipantDto> update(int participantId, TemplateParticipantDto participantDto) {
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
            final var typeOptional = Arrays
                    .stream(ParticipantType.values())
                    .filter(participantType -> participantType.getDbVal() == participantDto.getType())
                    .findFirst();
            typeOptional.ifPresent(participant::setType);

            final var statusOptional = Arrays
                    .stream(BaseStatus.values())
                    .filter(status -> status.ordinal() == participantDto.getStatus())
                    .findFirst();
            statusOptional.ifPresent(participant::setStatus);

            // thêm mới, cập nhật thành phần tham gia xử lý hồ sơ
            for (var recipientDto : participantDto.getRecipients()) {

                var recipient = modelMapper.map(recipientDto, TemplateRecipient.class);

                // convert sign type <!--> string
                var objectMapper = new ObjectMapper();
                var signType = objectMapper.writeValueAsString(recipientDto.getSignType());
                recipient.setSignType(signType);

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
                        recipient.setCardId(recipientDto.getCardId());
                        recipient.setIsOtp(recipientDto.getIsOtp());
                        recipient.setLoginBy(recipientDto.getLoginBy());

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
                    modelMapper.map(updated, TemplateParticipantDto.class)
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
    public Collection<TemplateParticipantDto> findByContract(int contractId) {
        final var participantCollection = participantRepository.findByContractIdOrderByOrderingAsc(contractId);

        Type typeToken = new TypeToken<Collection<TemplateParticipantDto>>() {
        }.getType();

        return modelMapper.map(participantCollection, typeToken);
    }

    /**
     * Lấy thông tin thành phần tham gia ký hợp đồng
     *
     * @param id Mã thành phần tham gia
     * @return Thông tin chi tiết thành phần tham gia
     */
    public Optional<TemplateParticipantDto> getById(int id) {
        Optional<TemplateParticipant> participantOptional = participantRepository
                .findById(id);

        return participantOptional.map(
                participant -> modelMapper.map(participant, TemplateParticipantDto.class)
        );
    }

    /**
     * Mapping dữ liệu từ DTO sang ENTITY
     *
     * @param participantDtoCollection {@link TemplateParticipantDto}
     * @return {@link TemplateParticipant}
     */
    @SuppressWarnings("unused")
    private Collection<TemplateParticipant> fromDTO(Collection<TemplateParticipantDto> participantDtoCollection) {
        final var typeToken = new TypeToken<Collection<TemplateParticipant>>() {
        }.getType();

        return modelMapper.map(
                participantDtoCollection,
                typeToken
        );
    }

    /**
     * Mapping dữ liệu từ ENTITY sang DTO
     *
     * @param participantCollection {@link TemplateParticipant}
     * @return {@link TemplateParticipantDto}
     */
    @SuppressWarnings("unused")
    private Collection<TemplateParticipantDto> toDTO(Collection<TemplateParticipant> participantCollection) {
        Type typeToken = new TypeToken<Collection<TemplateParticipantDto>>() {
        }.getType();

        return modelMapper.map(participantCollection, typeToken);
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
            log.error("can't delete participant template id = " + id, e);

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
