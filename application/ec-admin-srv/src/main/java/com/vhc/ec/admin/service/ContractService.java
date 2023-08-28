package com.vhc.ec.admin.service;

import com.vhc.ec.admin.dto.StatisticDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ContractService {
    @Value("${vhc.micro-services.contract.api-url}")
    private String contractUrl;

    final RestTemplate restTemplate;

    public long countMyOrgAndDescendantContract(int orgId) {
        var statisticDto = restTemplate
                .getForEntity(contractUrl + "/contracts/internal/count-my-org-and-descendant-contract?organizationId={orgId}",
                        StatisticDto.class, orgId).getBody();

        long total = statisticDto.getTotalDraff() + statisticDto.getTotalCreated() +
                statisticDto.getTotalCancel() + statisticDto.getTotalReject() +
                statisticDto.getTotalSigned() + statisticDto.getTotalProcess() +
                statisticDto.getTotalExpires();

        return total;
    }
}
