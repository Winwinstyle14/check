package com.vhc.ec.notification.controller;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.notification.definition.NoticeStatus;
import com.vhc.ec.notification.dto.*;
import com.vhc.ec.notification.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import java.util.Date;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@RequestMapping("/notification")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    final NoticeService noticeService;

    @GetMapping("/my-notice")
    public ResponseEntity<PageDto<NoticeDto>> findMyNotice(@CurrentCustomer CustomerUser customerUser,
                                                                 @RequestParam(name = "from_date", required = false)
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                         Date fromDate,
                                                                 @RequestParam(name = "to_date", required = false)
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                         Date toDate,
                                                                 @RequestParam(name = "status", required = false) Integer status,
                                                                 Pageable pageable) {
        try {
            PageDto<NoticeDto> pageDto = noticeService.findMyNotice(
                    customerUser.getEmail(),
                    fromDate,
                    toDate,
                    status,
                    pageable
            );

            return ResponseEntity.ok(pageDto);
        } catch (Exception e) {
            log.error("unexpected error", e);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = {"/viewed/{id}"})
    public ResponseEntity<NoticeDto> changeViewed(@CurrentCustomer CustomerUser customerUser, @PathVariable("id") int id) {

        var noticeOptional = noticeService.changeStatus(customerUser.getEmail(), id, NoticeStatus.VIEWED.getDbVal().intValue());

        return noticeOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
