package com.vhc.ec.filemgmt.controller;

import java.util.Base64;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.filemgmt.dto.ComposeRequest;
import com.vhc.ec.filemgmt.dto.CopyFileRequest;
import com.vhc.ec.filemgmt.dto.CopyFileResponse;
import com.vhc.ec.filemgmt.dto.PresignedObjectUrlRequest;
import com.vhc.ec.filemgmt.dto.PresignedObjectUrlResponse;
import com.vhc.ec.filemgmt.dto.UploadCeCARequest;
import com.vhc.ec.filemgmt.dto.UploadCeCAResponse;
import com.vhc.ec.filemgmt.dto.UploadFileResponse;
import com.vhc.ec.filemgmt.service.FileService;
import com.vhc.ec.filemgmt.service.internal.CustomerService;
import com.vhc.ec.filemgmt.util.BASE64DecodedMultipartFile;
import com.vhc.ec.filemgmt.util.EncryptorAes;
import com.vhc.ec.filemgmt.util.StringUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * API lấy thông tin của tệp tin trên hệ thống
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@AllArgsConstructor
@Slf4j
public class FileController {
	private static final String BUCKET_SUFFIX = "ec-bucket";

    private final FileService fileService;
    private final CustomerService customerService;

    /**
     * Lấy đường dẫn tạm thời của tệp tin trên hệ thống MinIO
     *
     * @param presignedObjectUrlRequest {@link PresignedObjectUrlRequest} Thông tin tệp tin cần lấy
     * @return {@link PresignedObjectUrlResponse} Thông tin đường dẫn tạm trên hệ thống
     */
    @PostMapping(value = {"/file/presigned-url", "/internal/file/presigned-url"})
    public ResponseEntity<PresignedObjectUrlResponse> getPresignedObjectUrl(
            @RequestBody @Valid PresignedObjectUrlRequest presignedObjectUrlRequest
    ) {
        final String presignedUrl = fileService.getPresignedObjectUrl(
                presignedObjectUrlRequest.getBucket(),
                presignedObjectUrlRequest.getFilePath()
        );

        if (StringUtils.hasText(presignedUrl)) {
            return ResponseEntity.ok(
                    PresignedObjectUrlResponse.builder()
                            .success(true)
                            .presignedUrl(presignedUrl)
                            .build()
            );
        }

        return ResponseEntity.ok(
                PresignedObjectUrlResponse.builder()
                        .success(false)
                        .build()
        );
    }

    /**
     * Cập nhật tệp tin lên hệ thống MinIO, với đối tượng đã tồn tại trên hệ thống
     *
     * @param multipartFile Thông tin tệp tin lưu trữ trong bucket
     * @param bucket        Bucket được hệ thống tạo cho tổ chức
     * @param path          Đường dẫn tới tệp tin trong bucket
     * @return Thông chi tin tiết của tệp tin sau khi tải lên
     */
    @PostMapping(value = "/internal/file/replace", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<UploadFileResponse> replace(
            @RequestPart("file") MultipartFile multipartFile,
            @RequestPart("bucket") String bucket,
            @RequestPart(value = "path", required = false) String path) {
    	try {
    		//Mã hóa file trước khi đẩy lên MinIO
    		var encryptedText =  EncryptorAes.encryptFile(multipartFile.getBytes());
    		MultipartFile multipartFileAfterEncryp = new BASE64DecodedMultipartFile(encryptedText, multipartFile.getName(), multipartFile.getOriginalFilename(), multipartFile.getContentType());
    		multipartFile = multipartFileAfterEncryp;
    		
    		//Cập nhật file đã mã hóa
    		String filePath = fileService.update(
                    multipartFile, bucket, path
            );

            if (StringUtils.hasText(filePath)) {
                return ResponseEntity.ok(
                        UploadFileResponse.builder()
                                .success(true)
                                .message("file upload success")
                                .fileObject(
                                        UploadFileResponse.Uploaded.builder()
                                                .filePath(filePath)
                                                .filename(multipartFile.getOriginalFilename())
                                                .bucket(bucket)
                                                .build()
                                )
                                .build()
                );
            } 
    	}catch (Exception e) {
            log.error("error {}", e);
        }

        return ResponseEntity.ok(
                UploadFileResponse.builder()
                        .success(false)
                        .message("file can't upload")
                        .build()
        );
    }

    @PostMapping(value = "/internal/file/compose")
    public ResponseEntity<UploadFileResponse> compose(
            @RequestBody @Valid ComposeRequest composeRequest) {
        return ResponseEntity.ok(
                fileService.compose(composeRequest)
        );
    }

    /**
     * copy file object
     * @param request data about file need to copy
     * @return {@link CopyFileResponse}
     */
    @PostMapping("/internal/file/copy")
    public ResponseEntity<CopyFileResponse> copy(
            @Valid @RequestBody CopyFileRequest request) {

        log.info("start copy file {}/{}", request.getBucket(), request.getFilePath());
        var newFilePath = fileService.copyObject(request.getBucket(), request.getFilePath());

        if (StringUtils.hasText(newFilePath)) {
            log.info("copy success");
            var response = CopyFileResponse.builder()
                    .bucket(request.getBucket())
                    .filePath(newFilePath)
                    .build();

            return ResponseEntity.ok(response);
        }

        log.warn("copy fail");
        return ResponseEntity.internalServerError().build();
    }
    
    /**
     * Lấy file từ MinIO định dạng Hex
     *
     * @param presignedObjectUrlRequest {@link PresignedObjectUrlRequest} Thông tin tệp tin cần lấy
     * @return {@link PresignedObjectUrlResponse} Thông tin đường dẫn tạm trên hệ thống
     */
    @PostMapping(value = {"/internal/file/presigned/hex"})
    public ResponseEntity<PresignedObjectUrlResponse> getHexToFileMinIO(
            @RequestBody @Valid PresignedObjectUrlRequest presignedObjectUrlRequest
    ) {
        final String presignedUrl = fileService.getHexToFileMinIO(
                presignedObjectUrlRequest.getBucket(),
                presignedObjectUrlRequest.getFilePath()
        );

        if (StringUtils.hasText(presignedUrl)) {
            return ResponseEntity.ok(
                    PresignedObjectUrlResponse.builder()
                            .success(true)
                            .presignedUrl(presignedUrl)
                            .build()
            );
        }

        return ResponseEntity.ok(
                PresignedObjectUrlResponse.builder()
                        .success(false)
                        .build()
        );
    }
    
    
    @PostMapping(value = {"/internal/file/upload/hex"})
    public ResponseEntity<UploadCeCAResponse> upFileHex(@RequestBody @Valid UploadCeCARequest ceCARequest) {
        // Lấy mã cơ quan ban hành
        final String orgCode = customerService.getOrganizationCodeById(ceCARequest.getOrgId());

        if (StringUtils.hasText(orgCode)) { 
        	try {
        		//Convert HEX --> byte[]
        		byte[] encodeBytesTemp = StringUtil.hexStringToByteArray(ceCARequest.getHexEncodeFile());

        		String encodedString =  new String(encodeBytesTemp);

        		byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes());
        		
        		//Mã hóa file trước khi đẩy lên MinIO
				var encryptedText =  EncryptorAes.encryptFile(decodedBytes);
				MultipartFile multipartFile = new BASE64DecodedMultipartFile(encryptedText, "file", ceCARequest.getFileName(), "application/pdf");
				
				//Tạo tên bucket từ orgCode
				final var bucket = String.format("%s-%s", orgCode, BUCKET_SUFFIX).toLowerCase();

	            //Lưu thông tin của đối tượng vào MinIO
	            String fileObject = fileService.upload(multipartFile, bucket);

	            //Lưu dữ liệu thành công
	            if (StringUtils.hasText(fileObject)) {
	                return ResponseEntity.ok(UploadCeCAResponse.builder()
	                        .success(true)
	                        .message(String.format("file \"%s\" upload successful", multipartFile.getOriginalFilename()))
	                        .filePath(fileObject)
	                        .filename(multipartFile.getOriginalFilename())
	                        .bucket(bucket)
	                        .build());
	            }
			} catch (Exception e) {
				e.printStackTrace();
			}   
        }

        // Lưu dữ liệu thất bại
        return ResponseEntity.ok(UploadCeCAResponse.builder().success(false)
                .message(String.format("file \"%s\" upload failure"))
                .build());

    }
}
