package com.vhc.ec.contract.service;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.vhc.ec.contract.dto.CeCARequest;
import com.vhc.ec.contract.dto.ComposeDto;
import com.vhc.ec.contract.dto.CopyFileRequest;
import com.vhc.ec.contract.dto.CopyFileResponse;
import com.vhc.ec.contract.dto.PresignedObjectUrlDto;
import com.vhc.ec.contract.dto.UploadCeCAResponse;
import com.vhc.ec.contract.dto.UploadFileDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Xử lý tệp tin trên hệ thống lưu trữ
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final RestTemplate restTemplate;
    @Value("${vhc.ec.micro-services.file.api-url}")
    private String fileApiUrl;

    /**
     * Lấy đường dẫn tạm của tệp tin trên hệ thống lưu trữ
     *
     * @param bucket   Thông tin bucket lưu trữ nội dung tệp tin
     * @param filePath Đường dẫn tới tệp tin
     * @return Đường dẫn tạm tới tệp tin
     */
    public String getPresignedObjectUrl(String bucket, String filePath) {
        // build request body
        var paramsMap = new HashMap<String, String>();
        paramsMap.put("bucket", bucket);
        paramsMap.put("file_path", filePath);

        var request = new HttpEntity<>(paramsMap);
        var response = restTemplate.postForEntity(
                String.format("%s/internal/file/presigned-url", fileApiUrl),
                request,
                PresignedObjectUrlDto.class
        );

        return response.getStatusCode() == HttpStatus.OK &&
                response.getBody() != null &&
                response.getBody().isSuccess() ?
                response.getBody().getPresignedUrl() : null;
    }

    /**
     * Cập nhật hợp đồng chính lên hệ thống lưu trữ
     *
     * @param newFilePath Đường dẫn tới tệp tin hợp đồng mới
     * @param bucket      Bucket được hệ thống tạo cho tổ chức
     * @param filePath    Đường dẫn tới tệp tin trên hệ thống lưu trữ
     * @return Thông tin của hợp đồng chính đã được cập nhật lên hệ thống lưu trữ
     */
    public Optional<UploadFileDto> replace(String newFilePath, String bucket, String filePath) {
        log.info(String.format("start replace file %s <- %s", filePath, newFilePath));

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        final var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new FileSystemResource(newFilePath));
        body.add("bucket", bucket);
        body.add("path", filePath);

        final var requestEntity = new HttpEntity<>(body, headers);

        final var response = restTemplate.postForEntity(
                fileApiUrl + "/internal/file/replace",
                requestEntity,
                UploadFileDto.class
        );

        return Optional.ofNullable(
                response.getStatusCode() == HttpStatus.OK
                        && response.hasBody() ? response.getBody() : null
        );
    }

    /**
     * compose multiple to single file
     *
     * @param composeDto componse object
     * @return {@link UploadFileDto}
     */
    public Optional<UploadFileDto> compose(ComposeDto composeDto) {
        final var response = restTemplate.postForEntity(
                String.format("%s/internal/file/compose", fileApiUrl),
                composeDto,
                UploadFileDto.class
        );

        return Optional.ofNullable(
                response.getStatusCode() == HttpStatus.OK
                        && response.hasBody() ? response.getBody() : null
        );
    }

    public Optional<CopyFileResponse> copy(CopyFileRequest copyFileRequest) {
        try {
            var response = restTemplate.postForEntity(
                    fileApiUrl + "/internal/file/copy",
                    copyFileRequest,
                    CopyFileResponse.class
            ).getBody();

            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("can't copy file {}", copyFileRequest.toString(), e);
        }

        return Optional.empty();
    }
    
    /**
     * Lấy file
     *
     * @param bucket   Thông tin bucket lưu trữ nội dung tệp tin
     * @param filePath Đường dẫn tới tệp tin
     * @return Đường dẫn tạm tới tệp tin
     */
    public String getHexToFileMinIO(String bucket, String filePath) {
        // build request body
        var paramsMap = new HashMap<String, String>();
        paramsMap.put("bucket", bucket);
        paramsMap.put("file_path", filePath);

        var request = new HttpEntity<>(paramsMap);
        var response = restTemplate.postForEntity(
                String.format("%s/internal/file/presigned/hex", fileApiUrl),
                request,
                PresignedObjectUrlDto.class
        );

        return response.getStatusCode() == HttpStatus.OK &&
                response.getBody() != null &&
                response.getBody().isSuccess() ?
                response.getBody().getPresignedUrl() : null;
    }
    
    /**
     * Đẩy file nhận được lên MinIO
     * @param ceCARequest
     * @return
     */
    public UploadCeCAResponse receiveCeCA(String hexEncodeFile, int orgId, String fileName) {
    	var ceCARequest = CeCARequest.builder()
				.hexEncodeFile(hexEncodeFile)
				.fileName(fileName)
				.orgId(orgId)
				.build();
        
    	var request = new HttpEntity<>(ceCARequest);
         
        var response = restTemplate.postForEntity(
        		String.format("%s/internal/file/upload/hex", fileApiUrl), 
        		request, 
        		UploadCeCAResponse.class);
        
        return response.getBody();
    }
}
