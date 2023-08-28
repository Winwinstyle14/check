package com.vhc.ec.filemgmt.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BucketService {

    private final MinioClient client;

    /**
     * Tạo mới bucket nếu chưa tồn tại trên hệ thống
     *
     * @param bucket Tên bucket cần tạo
     * @return Trả về true nếu thành công, false nếu thất bại
     */
    public boolean createBucketIfNotExists(String bucket) {
        boolean success = false;

        try {
            // check bucket exists
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );

            if (!exists) {
                client.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
            }

            success = true;
        } catch (Exception e) {
            log.error(
                    String.format("can't create bucket \"%s\"", bucket),
                    e
            );
        }

        return success;
    }
}
