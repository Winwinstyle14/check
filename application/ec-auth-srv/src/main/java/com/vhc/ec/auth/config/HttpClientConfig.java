package com.vhc.ec.auth.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

}
