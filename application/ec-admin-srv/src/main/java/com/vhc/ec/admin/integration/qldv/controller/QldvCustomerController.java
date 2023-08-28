package com.vhc.ec.admin.integration.qldv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhc.ec.admin.integration.qldv.dto.QldvCommonRes;
import com.vhc.ec.admin.integration.qldv.dto.QldvCustomerDto;
import com.vhc.ec.admin.service.OrganizationService;
import com.vhc.ec.api.versioning.ApiVersion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApiVersion("1")
@RestController
@RequestMapping("/admin/integration/qldv-mbf/sync")
@RequiredArgsConstructor
@Slf4j
public class QldvCustomerController {

    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QldvCommonRes save(@RequestBody QldvCustomerDto qldvCustomerDto) {
        var res = new QldvCommonRes();
        try {
        	log.info("request: {}", qldvCustomerDto);
            organizationService.syncOrganizationAndServiceFromQldv(qldvCustomerDto);
            res.setStatus(1);
            res.setMessage("success");

        } catch (Exception ex) {
            res.setStatus(2);
            res.setMessage("fail");
            log.error("err", ex);
        }
        return res;
    }



}
