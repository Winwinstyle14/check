package com.vhc.ec.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.notification.dto.SmsResponse;
import com.vhc.ec.notification.entity.SmsLog;
import com.vhc.ec.notification.repository.SmsLogRepository;
import com.vhc.ec.notification.util.VNCharacterUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsLogService {
	private final SmsLogRepository smsLogRepository;

	@Value("${vhc.ec.mobifone-services.sms.login-url}")
	private String loginUrl;

	@Value("${vhc.ec.mobifone-services.sms.send-url}")
	private String sendUrl;
	
	@Value("${vhc.ec.mobifone-services.sms.brandname}")
	private String brandname;
    
    @Value("${vhc.ec.mobifone-services.sms.username}")
    private String user;
    
    @Value("${vhc.ec.mobifone-services.sms.password}")
    private String pass;

	public void sendSMPP(String isdn, String message, int contractId, int organizationId) {
		message = VNCharacterUtils.removeAccent(message);

		if(isdn != null){
			isdn = isdn.replace("+","");
		}

		smsLogRepository.save(new SmsLog(isdn, message, "2", contractId, organizationId));
	}

	public SmsResponse sendAPI(String brandName, String username, String password, String isdn,
							   String message, int contractId, int organizationId) {
    	try {
    		String status = "-1";
    		message = VNCharacterUtils.removeAccent(message);
    		
    		if(brandName == null) {
    			brandName = brandname;
    			username = user;
    			password = pass;
        	}

			if(isdn != null){
				isdn = isdn.replace("+","");
			}
    		
    		final var urlLogin = String.format(loginUrl+"?userName=%s&password=%s&bindMode=T", username, password);

			log.info("urlLogin: {}", urlLogin);
        	 
        	var restTemplate = new RestTemplate();
        	final var responseLogin = restTemplate.getForEntity(urlLogin, String.class).getBody();

        	log.info("responseLogin: {}", responseLogin);

        	ObjectMapper objectMapper = new ObjectMapper();
        	
        	SmsResponse smsResponse = objectMapper.readValue(responseLogin, SmsResponse.class);
        	
        	if(smsResponse.getStatus().equals("200")) {
        		status = "3";
        		
        		final var urlSend =  String.format(sendUrl+"?sid=%s&sender=%s&recipient=%s&content=%s", smsResponse.getSid(), brandName, isdn, message);

				log.info("urlSend: {}", urlSend);

        		final var responseSend = restTemplate.getForEntity(urlSend,  String.class).getBody(); 
        		
            	smsResponse = objectMapper.readValue(responseSend, SmsResponse.class);

				log.info("responseSend: {}", responseSend);
        	}
			  
        	log.info("Send to {} ==> {} message: {}", isdn, smsResponse.toString(), message);
        	
        	//Lưu log
        	smsLogRepository.save(new SmsLog(isdn, message, status, contractId, organizationId));
        	
			return smsResponse;
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
    	
    	return SmsResponse.builder()
    			.status("500")
    			.message("URI error.")
    			.build();
    }
}
