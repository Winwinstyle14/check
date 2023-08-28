package com.vhc.ec.filemgmt.thirdparty;


import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.filemgmt.dto.Base64UploadRequest;
import com.vhc.ec.filemgmt.dto.UploadFileResponse;
import com.vhc.ec.filemgmt.service.UploadFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/tp/files")
@ApiVersion("1")
@Slf4j
@RequiredArgsConstructor
public class TpFileController {

    private final UploadFileService uploadFileService;

    @PostMapping("/organization/{id}")
    public ResponseEntity<UploadFileResponse> uploadBase64(
            @PathVariable("id") int id,
            @RequestBody @Valid Base64UploadRequest base64UploadRequest) {

        return uploadFileService.uploadBase64(id, base64UploadRequest);
    }
}
