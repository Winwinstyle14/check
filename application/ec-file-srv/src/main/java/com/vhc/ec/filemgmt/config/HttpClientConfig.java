package com.vhc.ec.filemgmt.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class HttpClientConfig {

    @Value("${vhc.ec.http.timeout:1000}")
    private int timeout;

    /**
     * Khởi tạo đối tượng http client
     *
     * @return {@link CloseableHttpClient} Đối tượng http client
     */
    @Bean
    public CloseableHttpClient getHttpClient() {
        // cấu hình request mặc định
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

        // khởi tạo đối tượng với request mặc định
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        return httpClient;
    }

}
