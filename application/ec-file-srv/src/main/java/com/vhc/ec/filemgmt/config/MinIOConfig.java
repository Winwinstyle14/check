package com.vhc.ec.filemgmt.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class MinIOConfig {

    @Value("${vhc.ec.minio.url}")
    private String url;

    @Value("${vhc.ec.minio.credentials.access-key}")
    private String accessKey;

    @Value("${vhc.ec.minio.credentials.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient getMinIOClient() throws MalformedURLException {
        return MinioClient.builder()
                .endpoint(new URL(url))
                .credentials(accessKey, secretKey)
                .build();
    }

}
