package com.vhc.ec.contract.controller;

import java.util.Collection;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import com.vhc.ec.contract.service.BatchNBService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vhc.ec.api.auth.CurrentCustomer;
import com.vhc.ec.api.auth.CustomerUser;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.ContractDto;
import com.vhc.ec.contract.dto.DocumentDto;
import com.vhc.ec.contract.dto.TemplateDocumentDto;
import com.vhc.ec.contract.dto.ValidateDto;
import com.vhc.ec.contract.service.BatchService;

import lombok.AllArgsConstructor;

/**
 * Xử lý hợp đồng theo lô
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/batch")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@RequiredArgsConstructor
public class BatchController {
    private final BatchService batchService;

    private final BatchNBService batchNBService;

    @Value("${vhc.ec.api.version.site}")
    private String BE_SITE;

    /**
     * Tạo vào lưu thông tin tệp tin mẫu, cho phép người dùng điền thông tin
     * hợp đồng theo lô vào mẫu
     *
     * @param id Mã số tham chiếu tới hợp đồng mẫu
     * @return {@link DocumentDto} Thông tin hợp đồng mẫu đã được lưu trữ trên hệ thống
     */
    
    @PostMapping("/{id}")
    public ResponseEntity<TemplateDocumentDto> process(
            @PathVariable("id") int id) {
        Optional<TemplateDocumentDto> documentOptional = null;

        if(BE_SITE.equals("nb")){
            documentOptional  = batchNBService.generateExcel(id);
        }

        if(BE_SITE.equals("kd")){
            documentOptional  = batchService.generateExcel(id);
        }

        return documentOptional.map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.internalServerError().build()
        );
    }
    
    /**
     * validate template uploaded file
     *
     * @param id            uuid of template contract
     * @param multipartFile file content
     * @return
     */
    @PostMapping("/validate/{id}")
    public ResponseEntity<ValidateDto> validate( 
            @PathVariable("id") int id,
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("organization_id") int organizationId) {

        if(BE_SITE.equals("nb")){
            return batchNBService.validate(id, multipartFile, organizationId, true);
        }

        if(BE_SITE.equals("kd")){
            return batchService.validate(id, multipartFile, organizationId, true);
        }

        return ResponseEntity.internalServerError().build();
    }

    /**
     * processing customer uploaded resource
     *
     * @param customerUser  current customer
     * @param id            template contract id
     * @param multipartFile resource about file uploaded
     * @return {@link ContractDto}
     */
    @PostMapping("/process/{id}/{status}")
    public ResponseEntity<Collection<ContractDto>> process(
            @CurrentCustomer CustomerUser customerUser,
            @PathVariable("id") int id,
            @PathVariable("status") int ceCAPush,
            @RequestParam("file") MultipartFile multipartFile) {

        if(BE_SITE.equals("nb")){
            return batchNBService.process(customerUser, id, ceCAPush, multipartFile, true);
        }

        if(BE_SITE.equals("kd")){
            return batchService.process(customerUser, id, ceCAPush, multipartFile, true);
        }

        return ResponseEntity.internalServerError().build();
    }

    /**
     * parse temporary file
     *
     * @param id            template contract id
     * @param multipartFile resource about file upload
     * @return {@link ContractDto}
     */
    @PostMapping("/parse/{id}")
    public ResponseEntity<Collection<ContractDto>> parse(
            @PathVariable("id") int id,
            @RequestParam("file") MultipartFile multipartFile) {

        if(BE_SITE.equals("nb")){
            return batchNBService.parse(id, multipartFile, true);
        }

        if(BE_SITE.equals("kd")){
            return batchService.parse(id, multipartFile, true);
        }

        return ResponseEntity.internalServerError().build();
    }

}