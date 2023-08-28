package com.vhc.ec.contract.controller;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.ContractDto;
import com.vhc.ec.contract.dto.PageDto;
import com.vhc.ec.contract.dto.ShareListDto;
import com.vhc.ec.contract.service.ContractService;
import com.vhc.ec.contract.service.ShareService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Date;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/shares")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class ShareController {

    private final ShareService shareService;
    private final ContractService contractService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ShareListDto> create(@CurrentCustomer CustomerUser customerUser, @Valid @RequestBody ShareListDto shareListDto) {
        final var created = shareService.create(customerUser, shareListDto);

        return created.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping
    public ResponseEntity<PageDto<ContractDto>> getAll(
            @CurrentCustomer CustomerUser customerUser,
            @RequestParam(name = "type", required = false) Integer contractType,
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "code", required = false, defaultValue = "") String code,
            @RequestParam(name = "from_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date fromDate,
            @RequestParam(name = "to_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date toDate,
            Pageable pageable) {
        final var page = contractService.getShares(
                customerUser.getEmail(),
                contractType,
                name,
                code,
                fromDate,
                toDate,
                pageable
        );

        return ResponseEntity.ok(page);
    }
}
