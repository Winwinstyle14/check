package com.vhc.ec.contract.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.ContractDto;
import com.vhc.ec.contract.dto.ContractStatusDto;
import com.vhc.ec.contract.dto.OrganizationDto;
import com.vhc.ec.contract.dto.PageDto;
import com.vhc.ec.contract.dto.StatisticDto;
import com.vhc.ec.contract.service.ContractService;
import com.vhc.ec.contract.service.CustomerService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dịch vụ quản lý thông tin dùng chung trên hệ thống
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/dashboard")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class DashboardController {

    private final ContractService contractService;
    private final CustomerService customerService;

    @GetMapping("/my-contract")
    public ResponseEntity<StatisticDto> getMyContract(@CurrentCustomer CustomerUser customerUser,
                                                      @RequestParam(name = "from_date", required = false)
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                              Date fromDate,
                                                      @RequestParam(name = "to_date", required = false)
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                              Date toDate) {
    	//Lấy tổ chức hiện tại
    	Optional<OrganizationDto> organizationOptional = customerService.getOrganizationByCustomer(customerUser.getId());
    	
    	if(organizationOptional.isPresent()) {
    		OrganizationDto organization = organizationOptional.get();
    		
	        fromDate = getStartOfDay(fromDate);
	        toDate = getFinishOfDay(toDate);
	
	        final var statistic = contractService.myContractStatistic(
	        		organization.getId(),
	                customerUser.getId(),
	                fromDate,
	                toDate
	        );
	        
	        return ResponseEntity.ok(statistic);
    	}
    	
    	
    	return ResponseEntity.noContent().build(); 
    }

    @GetMapping("/my-process")
    public ResponseEntity<ContractStatusDto> getMyProcess(@CurrentCustomer CustomerUser customerUser,
                                                          @RequestParam(name = "from_date", required = false)
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                  Date fromDate,
                                                          @RequestParam(name = "to_date", required = false)
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                  Date toDate) {
        fromDate = getStartOfDay(fromDate);
        toDate = getFinishOfDay(toDate);

        final var statistic = contractService.myProcessStatistic(
                customerUser.getEmail(),
                fromDate,
                toDate
        );

        return ResponseEntity.ok(statistic);
    }

    @GetMapping("/organization-contract")
    public ResponseEntity<StatisticDto> getMyContract(@RequestParam(name = "organization_id") int orgId,
                                                      @RequestParam(name = "from_date", required = false)
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                              Date fromDate,
                                                      @RequestParam(name = "to_date", required = false)
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                              Date toDate) {
        fromDate = getStartOfDay(fromDate);
        toDate = getFinishOfDay(toDate);

        final var statistic = contractService.orgContractStatistic(
                orgId,
                fromDate,
                toDate
        );

        return ResponseEntity.ok(statistic);
    }

    @GetMapping("/my-process-by-status/{status}")
    public ResponseEntity<?> getMyProcessByStatus(
            @CurrentCustomer CustomerUser customerUser,
            @PathVariable("status") int status,
            Pageable pageable) {
        // 1: cho xy ly, 2: sap het han, 3: cho phan hoi, 4: hoan thanh
        PageDto<ContractDto> page = null;
        switch (status) {
            case 1:
                page = contractService.getMyProcessContractProcessing(
                        customerUser.getEmail(),
                        pageable
                );
                break;
            case 2:
                page = contractService.getMyProcessContractExpires(
                        customerUser.getEmail(),
                        pageable
                );
                break;
            case 3:
                page = contractService.getMyProcessContractWating(
                        customerUser.getEmail(),
                        pageable
                );
                break;
            case 4:
                page = contractService.getMyProcessContractProcessed(
                        customerUser.getEmail(),
                        pageable
                );
                break;
        }

        return ResponseEntity.ok(page);
    }

    private Date getStartOfDay(Date date) {
        if (date != null) {
            var c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.HOUR, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);

            date = c.getTime();
        }

        return date;
    }

    private Date getFinishOfDay(Date date) {
        if (date != null) {
            var c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.HOUR, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);

            date = c.getTime();
        }

        return date;
    }
}
