package com.vhc.ec.contract.service;

import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.contract.definition.ContractStatus;
import com.vhc.ec.contract.definition.ShareType;
import com.vhc.ec.contract.dto.ContractShareNoticeRequest;
import com.vhc.ec.contract.dto.OrganizationDto;
import com.vhc.ec.contract.dto.ShareDto;
import com.vhc.ec.contract.dto.ShareListDto;
import com.vhc.ec.contract.entity.Share;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.ShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShareService {

    private final ShareRepository shareRepository;
    private final ContractRepository contractRepository;
    private final CustomerService customerService;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Transactional
    public Optional<ShareListDto> create(CustomerUser customerUser, ShareListDto shareListDto) {
        final var contractOptional = contractRepository.findById(shareListDto.getContractId());
        if (contractOptional.isEmpty() || contractOptional.get().getStatus() != ContractStatus.SIGNED) {
            return Optional.empty();
        }

        var contract = contractOptional.get();

        ShareDto shareDto;
        for (String email : shareListDto.getEmail()) {

            shareDto = ShareDto.builder()
                    .email(email)
                    .contractId(contract.getId())
                    .build();

            Share share;

            // check exists shared
            var shareOptional = shareRepository.findFirstByContractIdAndEmail(shareDto.getContractId(), shareDto.getEmail());
            if (shareOptional.isPresent()) {
                share = shareOptional.get();
            } else {

                share = modelMapper.map(shareDto, Share.class);
            }

            // check customer
            final var customer = customerService.getCustomerByEmail(shareDto.getEmail());
            var exists = customer != null && customer.getId() > 0;

            String token = null;
            var shareType = ShareType.CUSTOMER;

            // customer khong ton tai
            if (!exists) {
                // generate token
                token = RandomStringUtils.random(6, true, true);
                shareType = ShareType.GUEST;
            }

            share.setShareType(shareType);
            share.setToken(token);

            final var created = shareRepository.save(share);

            if (created != null) {
                OrganizationDto organizationDto = customerService.getOrganizationByCustomer(customerUser.getId()).get();

                ContractShareNoticeRequest request = ContractShareNoticeRequest.builder()
                        .customerName(exists ? customer.getName() : shareDto.getEmail())
                        .senderName(customerUser.getName())
                        .senderParticipant(organizationDto.getName())
                        .contractName(contract.getName())
                        .email(shareDto.getEmail())
                        .phone(customer != null ? customer.getPhone() : null)
                        .accessCode(token)
                        .loginType("1")
                        .contractUid(contract.getContractUid())
                        .contractUrl("" + contract.getId()).build();

                notificationService.notification(request);
            }
        }

        return Optional.of(shareListDto);
    }

}
