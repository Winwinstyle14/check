package com.vhc.ec.filemgmt.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.filemgmt.dto.CreateBucketRequest;
import com.vhc.ec.filemgmt.dto.CreateBucketResponse;
import com.vhc.ec.filemgmt.service.BucketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/buckets")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@RequiredArgsConstructor
public class BucketController {

    final BucketService bucketService;

    /**
     * Tạo mới bucket, nếu chưa tồn tại trên MinIO
     *
     * @param request {@link CreateBucketRequest} Thông tin của bucket cần tạo
     * @return {@link CreateBucketResponse} Thông tin của bucket đã được tạo
     */
    @PostMapping
    public CreateBucketResponse create(@Valid @RequestBody CreateBucketRequest request) {
        boolean success = bucketService.createBucketIfNotExists(request.getName());

        if (success) {
            return CreateBucketResponse.builder()
                    .success(true)
                    .message(String.format("create bucket \"%s\" success", request))
                    .build();
        }

        return CreateBucketResponse.builder()
                .success(false)
                .message(String.format("create bucket \"%s\" failure", request))
                .build();
    }

}
