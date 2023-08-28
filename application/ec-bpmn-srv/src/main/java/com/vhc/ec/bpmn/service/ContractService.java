package com.vhc.ec.bpmn.service;

import com.vhc.ec.bpmn.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    @Value("${vhc.micro-services.contract.api-url}")
    private String contractUrl;

    final RestTemplate restTemplate;

    /**
     * Get contract by ID
     *
     * @param contractId
     * @return
     */
    public ContractDto getContract(int contractId) {
        var contractDto = restTemplate
                .getForEntity(contractUrl + "/contracts/internal/" + contractId, ContractDto.class).getBody();

        if (contractDto == null) {
            return null;
        }

        var participants = contractDto.getParticipants();
        Collections.sort(participants, Comparator.comparingInt(ParticipantDto::getOrdering));

        for (var participant : participants) {
            Collections.sort(participant.getRecipients(),
                    Comparator.comparingInt(RecipientDto::getRole)
                            .thenComparingInt(RecipientDto::getOrdering));
        }

        // chuan hoa lai thu tu dam bao thu tu xu ly bat dau tu 1
        for (var participant : participants) {
            int minOrder = -1;
            int prevRole = -1;

            for (var recipient : participant.getRecipients()) {
                int order = recipient.getOrdering();

                if (prevRole != -1  && prevRole != recipient.getRole()) {
                    minOrder = -1;
                }

                prevRole = recipient.getRole();
                if (minOrder == -1) {
                    minOrder = order;
                }

                if (minOrder > 1) {
                    recipient.setOrdering(order - (minOrder - 1));
                }
            }

        }


        return contractDto;

    }


    /**
     * Change contract status
     *
     * @param contractId
     * @param status
     * @return
     */
    public ContractDto changeStatus(int contractId, Integer status) {

        return restTemplate.postForEntity(
                String.format(contractUrl + "/contracts/internal/%s/change-status/%s", contractId, status),
                new ContractChangeStatusRequest(), ContractDto.class).getBody();
    }

    public MessageDto sendContractToCeCA(int contractId) {
        return restTemplate.postForEntity(
                String.format(contractUrl + "/ceca/request-to-ceca/%d", contractId),
                null,
                MessageDto.class
        ).getBody();
    }
    /**
     * Cap nhat trang thai cua Recipient.status = processing
     *
     * @param recipientId
     */
    public void changeRecipientProcessing(int recipientId) {

        log.info("update recipient={}, status={}", recipientId, 1);

        restTemplate.put(contractUrl + "/recipients/internal/processing/" + recipientId, null);
    }

    public void sortParallel(ContractDto contractDto, List<RecipientDto> recipients) {
        Collections.sort(recipients, Comparator
                .<RecipientDto>comparingInt(recipient -> recipient.getParticipant().getOrdering())
                .thenComparingInt(RecipientDto::getRole)
                .thenComparingInt(RecipientDto::getOrdering)
        );
    }
}
