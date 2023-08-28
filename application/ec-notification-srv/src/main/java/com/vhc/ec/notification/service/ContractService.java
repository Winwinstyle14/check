package com.vhc.ec.notification.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vhc.ec.notification.dto.ContractDto;
import com.vhc.ec.notification.dto.ContractOriginalLinkDto;

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
public class ContractService {

    final RestTemplate restTemplate;

    private static final String EC_CONTRACT_URL = "http://ec-contract-srv/api/v1";

    /**
     * Get contract by ID
     *
     * @param contractId
     * @return
     */
    public ContractDto getContract(int contractId) {

        return restTemplate.getForEntity(EC_CONTRACT_URL + "/contracts/internal/"+ contractId, ContractDto.class).getBody();
    }
    
    public Optional<ContractOriginalLinkDto> save(ContractOriginalLinkDto contractLink) {

        return Optional.of(restTemplate.postForEntity(EC_CONTRACT_URL + "/handle/internal/", contractLink , ContractOriginalLinkDto.class).getBody());
    }
}
