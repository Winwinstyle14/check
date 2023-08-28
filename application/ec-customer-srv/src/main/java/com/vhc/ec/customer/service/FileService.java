package com.vhc.ec.customer.service;

import com.vhc.ec.customer.dto.PresignedObjectUrlDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

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

}
