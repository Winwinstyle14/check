package com.vhc.ec.contract.thridparty;

import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.contract.dto.tp.EkycRecognitionRequest;
import com.vhc.ec.contract.dto.tp.EkycRecognitionResponse;
import com.vhc.ec.contract.dto.tp.EkycVerifyRequest;
import com.vhc.ec.contract.dto.tp.EkycVerifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/tp/contracts/ekyc")
@ApiVersion("1")
@Slf4j
@RequiredArgsConstructor
public class TpEkycController {
    @Value("${vhc.ec.mobifone.sign-service.ekyc-recognition-url}")
    private String ekycRecognitionUrl;

    @Value("${vhc.ec.mobifone.sign-service.ekyc-verification-url}")
    private String ekycVerificationUrl;

    @Value("${vhc.ec.mobifone.sign-service.ekyc-api-key}")
    private String ekycApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final HttpHeaders headers = new HttpHeaders();

    @PostMapping("/recognition")
    public EkycRecognitionResponse recognition(@RequestBody EkycRecognitionRequest req) {
        headers.set("api-key", ekycApiKey);
        var entity = new HttpEntity(req, headers);
        return restTemplate.exchange(ekycRecognitionUrl, HttpMethod.POST,
                entity, EkycRecognitionResponse.class).getBody();
    }

    @PostMapping("/verification")
    public EkycVerifyResponse verify(@RequestBody EkycVerifyRequest req) {
        headers.set("api-key", ekycApiKey);
        var entity = new HttpEntity(req, headers);
        return restTemplate.exchange(ekycVerificationUrl, HttpMethod.POST,
                entity, EkycVerifyResponse.class).getBody();
    }
}
