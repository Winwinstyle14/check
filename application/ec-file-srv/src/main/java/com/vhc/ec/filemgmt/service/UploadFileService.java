package com.vhc.ec.filemgmt.service;

import com.vhc.ec.filemgmt.dto.Base64UploadRequest;
import com.vhc.ec.filemgmt.dto.UploadFileResponse;
import com.vhc.ec.filemgmt.service.internal.CustomerService;
import com.vhc.ec.filemgmt.util.EncryptorAes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadFileService {
    private static final String BUCKET_SUFFIX = "ec-bucket";

    private final FileService fileService;
    private final CustomerService customerService;

    public ResponseEntity<UploadFileResponse> uploadBase64(int orgId, Base64UploadRequest base64UploadRequest) {
    // Chuyển đổi chuỗi base64 thành byte[]
        var base64Data = base64UploadRequest.getContent().split(",");
        var decodedBytes = Base64Utils.decodeFromString(base64Data[1]);
        var mimeType = extractMimeType(base64Data[0]);

        //Mã hóa file trước khi đẩy lên MinIO
        var encryptedText =  EncryptorAes.encryptFile(decodedBytes);

        //Chuyển đổi dữ liệu mã hóa sang inputstream
        var length = encryptedText.length;
        final var inputStream = new ByteArrayInputStream(encryptedText);

        // Lấy mã cơ quan ban hành
        final var orgCode = customerService.getOrganizationCodeById(orgId);
        if (StringUtils.hasText(orgCode)) {
            final var bucket = String.format("%s-%s", orgCode, BUCKET_SUFFIX).toLowerCase();

            final var filePath = fileService.upload(
                    bucket, inputStream,
                    base64UploadRequest.getName(),
                    length,
                    mimeType
            );

            if (StringUtils.hasText(filePath)) {
                return ResponseEntity.ok(
                        UploadFileResponse.builder()
                                .success(true)
                                .message(String.format("upload \"%s\" success", filePath))
                                .fileObject(
                                        UploadFileResponse.Uploaded.builder()
                                                .filePath(filePath)
                                                .filename(base64UploadRequest.getName())
                                                .bucket(bucket)
                                                .build()
                                )
                                .build()
                );
            }
        }

        return ResponseEntity.badRequest().body(
                UploadFileResponse.builder()
                        .success(false)
                        .message("can't upload file to server")
                        .build()
        );
    }

    /**
     * Lấy thông tin mô tả về loại nội dung của tệp tin được tải lên
     *
     * @param data Chuỗi base64
     * @return Thông tin chi tiết về mimetype
     */
    private String extractMimeType(String data) {
        final Pattern mime = Pattern.compile("^data:([a-zA-Z0-9]+/[a-zA-Z0-9]+).*");
        final Matcher matcher = mime.matcher(data);
        if (!matcher.find())
            return "";
        return matcher.group(1).toLowerCase();
    }
}
