package com.vhc.ec.contract.service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import com.vhc.ec.contract.dto.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vhc.ec.contract.definition.ContractStatus;
import com.vhc.ec.contract.entity.Contract;
import com.vhc.ec.contract.entity.Participant;
import com.vhc.ec.contract.entity.Recipient;
import com.vhc.ec.contract.repository.ContractRepository;
import com.vhc.ec.contract.repository.RecipientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String EC_NOTIFICATION_SERVICE = "http://ec-notification-srv";
    final RecipientRepository recipientRepository;
    final CustomerService customerService;
    final RestTemplate restTemplate;
    final ContractRepository contractRepository;
    
    final SimpleDateFormat dff = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Gửi thông báo chia sẻ HĐ
     *
     * @param contractShareNoticeRequest
     * @return
     */
    public Optional<MessageDto> notification(ContractShareNoticeRequest contractShareNoticeRequest) {

        var request = new HttpEntity<>(contractShareNoticeRequest);

        var response = restTemplate.postForEntity(EC_NOTIFICATION_SERVICE + "/api/v1/internal/notification/contractShareNotice", request, MessageDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            final var messageDto = response.getBody();

            if (messageDto != null && !messageDto.isSuccess()) {
                log.error(String.format("can't call notification api. %s", messageDto.getMessage()));
            }

            return Optional.ofNullable(messageDto);
        }

        return Optional.empty();
    }

    /**
     * Thông báo cho người nhận ủy quyền/chuyển tiếp
     *
     * @param recipient
     * @param customerDto
     * @param organizationDto
     * @return
     */
    public Optional<MessageDto> notificationAuthorize(Recipient recipient, CustomerDto customerDto,
                                                      OrganizationDto organizationDto, Contract contract, Participant participant) {

        ContractAuthorizeNoticeRequest request = new ContractAuthorizeNoticeRequest();

        request.setActionType(recipient.getRole().getDbVal().intValue());
        request.setApproveType(1);

        request.setContractId(recipient.getParticipant().getContractId());
        request.setContractName(contract.getName());
        request.setContractUrl("" + contract.getId());
        request.setContractNotes(contract.getNotes());

        request.setAccessCode(recipient.getPassword());

        if (recipient.getPassword() != null && !recipient.getPassword().equals("")) {
            request.setLoginType("1");
        }

        request.setParticipantName(participant.getName());

        request.setRecipientId("" + recipient.getId());
        request.setRecipientName(recipient.getName());
        request.setRecipientEmail(recipient.getEmail());
        request.setRecipientPhone(recipient.getPhone());

        request.setSenderName(customerDto.getName());
        request.setSenderParticipant(organizationDto.getName());
        
        // bổ sung cách thức đăng nhập email/phone
        request.setLoginBy(recipient.getLoginBy());
        
        // bổ sung kênh gửi SMS API/SMPP
        request.setBrandName(organizationDto.getBrandName());
        request.setSmsUser(organizationDto.getSmsUser());
        request.setSmsPass(organizationDto.getSmsPass());
        request.setSmsSendMethor(organizationDto.getSmsSendMethor());

        //bổ sung thông tin contract uid
        request.setContractUid(contract.getContractUid());

        //bổ sung thông tin organizationId tổ chức tạo hợp đồng
        request.setOrgId(contract.getOrganizationId());

        var httpRequest = new HttpEntity<>(request);

        var response = restTemplate.postForEntity(EC_NOTIFICATION_SERVICE + "/api/v1/internal/notification/startSignFlow",
                httpRequest, MessageDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            final var messageDto = response.getBody();

            if (messageDto != null && !messageDto.isSuccess()) {
                log.error(String.format("can't call notification api. %s", messageDto.getMessage()));
            }

            return Optional.ofNullable(messageDto);
        }

        return Optional.empty();
    }

    /**
     * Thông báo hợp đồng sắp quá hạn/ quá hạn
     *
     * @param contractStatus
     */
    public void notificationExpire(int contractStatus) {

        List<Recipient> recipientList = null;

        if (contractStatus == ContractStatus.ABOUT_EXPRIRE.getDbVal()) {
            recipientList = recipientRepository.findAllByContractAboutExpire();
        } else if (contractStatus == ContractStatus.EXPRIRE.getDbVal()) {
            recipientList = recipientRepository.findAllByContractExpire();
        }

        for (Recipient recipient : recipientList) {
            // gửi thông báo đến người đã và đang xử lý hợp đồng
            try {
                Participant participant = recipient.getParticipant();
                Contract contract = participant.getContract();

                var customerDto = customerService.getCustomerById(contract.getCreatedBy());

                OrganizationDto organizationDto = customerService.getOrganizationByCustomer(customerDto.getId()).get();

                ContractAuthorizeNoticeRequest request = new ContractAuthorizeNoticeRequest();

                request.setActionType(recipient.getRole().getDbVal().intValue());
                request.setApproveType(1);

                request.setContractId(recipient.getParticipant().getContractId());
                request.setContractName(contract.getName());
                request.setContractUrl("" + contract.getId());
                request.setContractNotes(contract.getNotes());

                request.setAccessCode(recipient.getPassword());

                if (recipient.getPassword() != null && !recipient.getPassword().equals("")) {
                    request.setLoginType("1");
                }

                request.setParticipantName(participant.getName());

                request.setRecipientId("" + recipient.getId());
                request.setRecipientName(recipient.getName());
                request.setRecipientEmail(recipient.getEmail());
                request.setRecipientPhone(recipient.getPhone());

                request.setSenderName(customerDto.getName());
                request.setSenderParticipant(organizationDto.getName());

                request.setContractStatus(contractStatus);
                request.setLoginBy(recipient.getLoginBy());
                
                // bổ sung kênh gửi SMS API/SMPP
                request.setBrandName(organizationDto.getBrandName());
                request.setSmsUser(organizationDto.getSmsUser());
                request.setSmsPass(organizationDto.getSmsPass());
                request.setSmsSendMethor(organizationDto.getSmsSendMethor());

                request.setContractUid(contract.getContractUid());

                request.setOrgId(contract.getOrganizationId());

                var httpRequest = new HttpEntity<>(request);

                var response = restTemplate.postForEntity(EC_NOTIFICATION_SERVICE + "/api/v1/internal/notification/exprireContract", httpRequest, MessageDto.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    final var messageDto = response.getBody();

                    if (messageDto != null && !messageDto.isSuccess()) {
                        log.error(String.format("can't call notification api. %s", messageDto.getMessage()));
                    } 
                } 
            } catch (Exception e) {
                log.error("error, recipientId={}", recipient.getId(), e);
            }
        } 
    }

    /**
     * Gửi thông báo chia sẻ mẫu HĐ
     *
     * @param contractShareNoticeRequest
     * @return
     */
    public Optional<MessageDto> notificationShareContractTemplate(ContractShareNoticeRequest contractShareNoticeRequest) {

        var request = new HttpEntity<>(contractShareNoticeRequest);

        var response = restTemplate.postForEntity(EC_NOTIFICATION_SERVICE + "/api/v1/internal/notification/contractShareTemplate", request, MessageDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            final var messageDto = response.getBody();

            if (messageDto != null && !messageDto.isSuccess()) {
                log.error(String.format("can't call notification api. %s", messageDto.getMessage()));
            }

            return Optional.ofNullable(messageDto);
        }

        return Optional.empty();
    }
    
    /**
     * Gửi mã OTP
     *
     * @param smsLogRequest
     * @return
     */
    public Optional<MessageDto> notificationOTP(SmsLogRequest smsLogRequest) {  	
    	var organizationDto = customerService.getOrganizationById(smsLogRequest.getOrgId()); 
    	
    	//Mặc định gửi SMS qua API
    	smsLogRequest.setSmsSendMethor("API");
    	
    	if(organizationDto.isPresent()) {
    		var organization = organizationDto.get();
    		
    		smsLogRequest.setBrandName(organization.getBrandName());
    		smsLogRequest.setSmsUser(organization.getSmsUser());
    		smsLogRequest.setSmsPass(organization.getSmsPass());
    		smsLogRequest.setSmsSendMethor(organization.getSmsSendMethor()); 
    	}

        log.info("==> SmsLogRequest: {}", smsLogRequest);
    	
    	var request = new HttpEntity<>(smsLogRequest);

        var response = restTemplate.postForEntity(EC_NOTIFICATION_SERVICE + "/api/v1/internal/notification/sms", request, MessageDto.class);

        log.info("Call /api/v1/internal/notification/sms response: {}", response);

        if (response.getStatusCode() == HttpStatus.OK) {
            final var messageDto = response.getBody();

            if (messageDto != null && !messageDto.isSuccess()) {
                log.error(String.format("can't call notification api. %s", messageDto.getMessage()));
            }

            return Optional.ofNullable(messageDto);
        }

        return Optional.empty();
    }
    
    /**
     * Thông báo hợp đồng hết hiệu lực
     *
     */
    public void notificationExpireTime() {

        var contractList =  contractRepository.findAllByContractExpireTime();

        for (Contract contract : contractList) {
            // gửi thông báo đến người tạo hợp đồng
            try {
                var customerDto = customerService.getCustomerById(contract.getCreatedBy()); 

                ContractExprireTimeRequest request = new ContractExprireTimeRequest();
 
                request.setContractName(contract.getName());
                request.setContractUrl("" + contract.getId());  
                request.setLoginType("1");  
                request.setRecipientName(customerDto.getName());
                request.setRecipientEmail(customerDto.getEmail());
                request.setContractExpireTime(dff.format(contract.getContractExpireTime()));

                var httpRequest = new HttpEntity<>(request);

                var response = restTemplate.postForEntity(EC_NOTIFICATION_SERVICE + "/api/v1/internal/notification/exprireContractTime", httpRequest, MessageDto.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    final var messageDto = response.getBody();

                    if (messageDto != null && !messageDto.isSuccess()) {
                        log.error(String.format("can't call notification api. %s", messageDto.getMessage()));
                    } 
                } 
            } catch (Exception e) {
                log.error("error, customerId={}", contract.getCustomerId(), e);
            }
        }
    }

    public void sendSignFlowNotify(SignFlowNotifyRequest signFlowNotifyRequest) {
       restTemplate.postForEntity(EC_NOTIFICATION_SERVICE + "/api/v1/internal/notification/startSignFlow",
               signFlowNotifyRequest, Object.class).getBody();
    }
}
