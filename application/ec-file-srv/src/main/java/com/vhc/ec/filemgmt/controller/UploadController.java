package com.vhc.ec.filemgmt.controller;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.filemgmt.dto.Base64UploadRequest;
import com.vhc.ec.filemgmt.dto.UploadFileResponse;
import com.vhc.ec.filemgmt.service.FileService;
import com.vhc.ec.filemgmt.service.UploadFileService;
import com.vhc.ec.filemgmt.service.internal.CustomerService;
import com.vhc.ec.filemgmt.util.BASE64DecodedMultipartFile;
import com.vhc.ec.filemgmt.util.EncryptorAes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * Quản lý tệp tin trên hệ thống MinIO
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/upload")
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class UploadController {
    private static final String BUCKET_SUFFIX = "ec-bucket";

    private final FileService fileService;
    private final CustomerService customerService;
    private final UploadFileService uploadFileService;

    /**
     * Lưu tệp tin khách hàng tải lên theo tổ chức.
     *
     * @param multipartFile Nội dung tệp tin cần tải lên
     * @param id            Mã số tham chiếu tới tổ chức tạo hợp đồng
     * @return {@link UploadFileResponse} Thông tin trả về sau khi xử lý tải lên
     */
    @PostMapping(value = {"/organizations/{id}/single"})
    public UploadFileResponse create(@RequestParam("file") MultipartFile multipartFile,
                                     @PathVariable int id) {
        // Lấy mã cơ quan ban hành
        final String orgCode = customerService.getOrganizationCodeById(id);

        if (StringUtils.hasText(orgCode)) { 
        	try {
        		
        		//Mã hóa file trước khi đẩy lên MinIO
				var encryptedText =  EncryptorAes.encryptFile(multipartFile.getBytes());
				MultipartFile multipartFileAfterEncryp = new BASE64DecodedMultipartFile(encryptedText, multipartFile.getName(), multipartFile.getOriginalFilename(), multipartFile.getContentType());
				multipartFile = multipartFileAfterEncryp;
				
				//Tạo tên bucket từ orgCode
				final var bucket = String.format("%s-%s", orgCode, BUCKET_SUFFIX).toLowerCase();

	            // Lưu thông tin của đối tượng vào MinIO
	            String fileObject = fileService.upload(multipartFile, bucket);

	            // Lưu dữ liệu thành công
	            if (StringUtils.hasText(fileObject)) {
	                return UploadFileResponse.builder()
	                        .success(true)
	                        .message(String.format("file \"%s\" upload successful", multipartFile.getOriginalFilename()))
	                        .fileObject(
	                                UploadFileResponse.Uploaded.builder()
	                                        .filePath(fileObject)
	                                        .filename(multipartFile.getOriginalFilename())
	                                        .bucket(bucket)
	                                        .build()
	                        )
	                        .build();
	            }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        }

        // Lưu dữ liệu thất bại
        return UploadFileResponse.builder().success(false)
                .message(String.format("file \"%s\" upload failure", multipartFile.getOriginalFilename()))
                .fileObject(null)
                .build();

    }

    /**
     * Tải lên tệp tin của khách hàng, dưới dạng chuỗi ký tự Base64
     *
     * @param id                  Mã tham chiếu tới tổ chức của hợp đồng
     * @param base64UploadRequest {@link Base64UploadRequest} Nội dung của tệp tin được tải lên,
     *                            dưới dạng chuỗi Base64
     * @return {@link UploadFileResponse} Thông tin tệp tin đã được tải lên hệ thống MinIO
     */
    @PostMapping("/organizations/{id}/base64")
    public ResponseEntity<UploadFileResponse> uploadBase64(
            @PathVariable("id") int id,
            @RequestBody @Valid Base64UploadRequest base64UploadRequest) {

        return uploadFileService.uploadBase64(id, base64UploadRequest);
    }
}
