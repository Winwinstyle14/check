package com.vhc.ec.contract.service;

import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.contract.dto.*;
import com.vhc.ec.contract.entity.TemplateShare;
import com.vhc.ec.contract.repository.TemplateContractRepository;
import com.vhc.ec.contract.repository.TemplateShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateShareService {

    private final TemplateShareRepository shareRepository;
    private final TemplateContractRepository contractRepository;
    private final CustomerService customerService;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Transactional
    public Optional<ShareListDto> create(CustomerUser customerUser, ShareListDto shareListDto) {
        final var contractOptional = contractRepository.findById(shareListDto.getContractId());
        //if (contractOptional.isEmpty() || contractOptional.get().getStatus() != ContractStatus.SIGNED) {
        if (contractOptional.isEmpty()) {
            return Optional.empty();
        }

        var contract = contractOptional.get();

        TemplateShareDto shareDto;
        for (String email : shareListDto.getEmail()) {
            var customerDto = customerService.getCustomerByEmail(email);
            OrganizationDto organizationDto = customerService.getOrganizationByCustomer(customerDto.getId()).get();

            shareDto = TemplateShareDto.builder()
                    .email(email)
                    .contractId(contract.getId())
                    .organizationId(organizationDto.getId())
                    .customerId(customerDto.getId())
                    .status(1)
                    .build();

            TemplateShare share;

            // check exists shared
            var shareOptional = shareRepository.findFirstByContractIdAndEmail(shareDto.getContractId(), shareDto.getEmail());
            if (shareOptional.isPresent()) {
                share = shareOptional.get();
            } else {
                share = modelMapper.map(shareDto, TemplateShare.class);
            }

            final var created = shareRepository.save(share);

            System.out.println(created);

            //Share thành công gửi thông báo cho email share

            if (created != null) {
                ContractShareNoticeRequest request = ContractShareNoticeRequest.builder()
                        .customerName(customerDto.getName())
                        .senderName(customerUser.getName())
                        .senderParticipant(organizationDto.getName())
                        .contractName(contract.getName())
                        .email(shareDto.getEmail())
                        .phone(customerDto != null ? customerDto.getPhone() : null)
                        .accessCode(null)
                        .contractUrl("" + contract.getId()).build();

                notificationService.notificationShareContractTemplate(request);
            }

        }

        return Optional.of(shareListDto);
    }

    @Transactional
    public MessageDto deleteById(int id) {
        final var shareOptional = shareRepository.findById(id);

        if (shareOptional.isPresent()) {
            try {
                shareRepository.deleteById(id);
            } catch (Exception e) {
                log.error("can't delete share template id = " + id, e);

                return MessageDto.builder()
                        .success(false)
                        .message("E01")
                        .build();
            }
        }

        return MessageDto.builder()
                .success(true)
                .message("E00")
                .build();
    }

    /**
     * Lấy danh sách share mẫu hợp đồng theo mã mẫu hợp đồng
     *
     * @param contractId Mã hợp đồng
     * @return {@link ShareListDto} Danh sách trường dữ liệu
     */
    public Collection<TemplateShareDto> findByContract(int contractId, Integer organizationId) {
        var shareCollection = shareRepository.findByContract(organizationId, contractId);

        if (shareCollection != null && shareCollection.size() > 0) {
            //Chuyển Collection Entity sang Collection Dto
            Collection<TemplateShareDto> shareDtoCollection = modelMapper.map(
                    shareCollection,
                    new TypeToken<Collection<TemplateShareDto>>() {
                    }.getType()
            );

            //Bổ sung thông tin tổ chức của username được chia sẻ
            for (TemplateShareDto item : shareDtoCollection) {
                Optional<OrganizationDto> organizationDto = customerService.getOrganizationById(item.getOrganizationId());

                if (organizationDto.isPresent()) {
                    item.setOrgName(organizationDto.get().getName());
                }
            }

            return shareDtoCollection;
        }

        return Collections.emptyList();
    }
}
