package com.vhc.ec.filemgmt.service.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CloseableHttpClient httpClient;
    @Value("${vhc.ec.internal.customer-srv.url}")
    private String apiUrl;

    /**
     * Lấy mã cơ quan cần upload dữ liệu
     *
     * @param orgId Mã cơ quan (tự tăng)
     * @return Mã cơ quan (người dùng nhập vào)
     */
    public String getOrganizationCodeById(int orgId) {
        final String url = String.format("%s/internal/organizations/%d", apiUrl, orgId);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);

            String orgJsonString = EntityUtils.toString(httpResponse.getEntity());
            if (StringUtils.hasText(orgJsonString)) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode orgJsonNode = objectMapper.readTree(orgJsonString);
                return orgJsonNode.get("code").asText();
            }
        } catch (IOException e) {
            log.error(String.format("can't make request to \"%s\"", url), e);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error(String.format("can't close reponse from \"%s\"", url), e);
                }
            }
        }

        return null;
    }
}
