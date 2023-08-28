package com.vhc.ec.notification.controller;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhc.ec.api.versioning.ApiVersion;
import com.vhc.ec.notification.config.NotificationConfig;
import com.vhc.ec.notification.definition.ContractStatus;
import com.vhc.ec.notification.definition.RoleType;
import com.vhc.ec.notification.dto.AccountNoticeRequest;
import com.vhc.ec.notification.dto.BaseResponse;
import com.vhc.ec.notification.dto.ContractDto;
import com.vhc.ec.notification.dto.ContractExprireTimeRequest;
import com.vhc.ec.notification.dto.ContractOriginalLinkDto;
import com.vhc.ec.notification.dto.ContractShareNoticeRequest;
import com.vhc.ec.notification.dto.CreatePasswordResetRequest;
import com.vhc.ec.notification.dto.ParticipantDto;
import com.vhc.ec.notification.dto.RegistrationNoticeRequest;
import com.vhc.ec.notification.dto.ServiceExpiredRequest;
import com.vhc.ec.notification.dto.SmsLogRequest;
import com.vhc.ec.notification.dto.SmsResponse;
import com.vhc.ec.notification.dto.StartSignFlowRequest;
import com.vhc.ec.notification.dto.StartSignFlowResponse;
import com.vhc.ec.notification.dto.UserViewDto;
import com.vhc.ec.notification.entity.Email;
import com.vhc.ec.notification.entity.Message;
import com.vhc.ec.notification.entity.Notice;
import com.vhc.ec.notification.service.ContractService;
import com.vhc.ec.notification.service.CustomerService;
import com.vhc.ec.notification.service.EmailService;
import com.vhc.ec.notification.service.MessageService;
import com.vhc.ec.notification.service.NoticeService;
import com.vhc.ec.notification.service.SmsLogService;
import com.vhc.ec.notification.service.SmsService;
import com.vhc.ec.notification.util.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@RestController
@ResponseStatus(HttpStatus.OK)
@Consumes(MediaType.APPLICATION_JSON_VALUE)
@Produces(MediaType.APPLICATION_JSON_VALUE)
@ApiVersion("1")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    final MessageService messageService;
    final EmailService emailService;
    final SmsService smsService;
    final NotificationConfig notificationConfig;
    final NoticeService noticeService;
    final ContractService contractService;
    final SmsLogService smsLogService;
    private final CustomerService customerService;
    final RabbitTemplate rabbitTemplate;
    final Binding bindingEmail;
    final Binding bindingSms;

    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Tao email thong bao reset password
     *
     * @param request
     * @return
     */
    @PostMapping("/internal/notification/createResetPassword")
    public BaseResponse create(@Valid @RequestBody CreatePasswordResetRequest request) {

        log.info("request: {}", request);

        Message message = messageService.findByCode(notificationConfig.getCodeResetPassword()).orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create reset password notification \"%s\" failure", request))
                    .build();
        }

        if (message.getMailTemplate() != null) {
            String content = message.getMailTemplate().replace("#TOKEN#", request.getToken());
            content = content.replace("#USERNAME#", request.getUsername());

            Email email = new Email();
            email.setMessageId(message.getId());
            email.setSubject(message.getName());
            email.setRecipient(request.getEmail());
            email.setContent(content);
            email.setCreatedAt(new Date());

            Email emailOpt = emailService.save(email);

            // send to rabbitmq email topic
            try {
                rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
            } catch (Exception e) {
                log.error("error", e);
                return BaseResponse.builder()
                        .success(false)
                        .message(String.format("send email queue \"%s\" fail", request))
                        .build();
            }
        }

        return BaseResponse.builder()
                .success(true)
                .message(String.format("create password reset \"%s\" success", request))
                .build();
    }

    /**
     * Tao thong bao luong ky
     *
     * @param request
     * @return
     */
    @PostMapping("/internal/notification/startSignFlow")
    public StartSignFlowResponse startSignFlow(@Valid @RequestBody StartSignFlowRequest request) {
        log.info("request: {}", request);

        // TRUNGNQ kiem tra neu user nam trong he thong thi xoa password di
        String recipientEmail = request.getRecipientEmail();
        var customer = customerService.getCustomerByEmail(recipientEmail);
        if (customer != null && customer.getId() > 0) {
            request.setAccessCode(null);
        }


        boolean success = true;
        String response = "";

        Message message;
        String messageCode = null;
        
        //add link code
        request.setLinkCode(StringUtil.generateLinkCode());
         
    	if (request.getNotificationCode() != null && !request.getNotificationCode().equals("")) {
            messageCode = request.getNotificationCode(); // huy hop dong
        } else {
            // Reject HD
            if (request.getApproveType() == 2) {
            	if(request.getLoginBy().equals("phone")) {
            		messageCode = "sms_sign_flow_reject";
            	}else {
            		messageCode = notificationConfig.getCodeSignFlowReject();
            	} 
            }
            // Hoan thanh HD
            else if (request.getApproveType() == 3) {
            	if(request.getLoginBy().equals("phone")) {
            		messageCode = "sms_sign_flow_finish";
            	}else {
            		messageCode = notificationConfig.getCodeSignFlowFinish();
            	}
            }
            // Dong y
            else {
                if (request.getActionType() == 1) {             // Dieu phoi
                	if(request.getLoginBy().equals("phone")) {
                		messageCode = "sms_sign_flow_coordinator";
                	}else {
                		messageCode = notificationConfig.getCodeSignFlowCoordinator();
                	}
                } else if (request.getActionType() == 2) {      // Xem xet
                	if(request.getLoginBy().equals("phone")) {
                		messageCode = "sms_sign_flow_review";
                	}else {
                		messageCode = notificationConfig.getCodeSignFlowReview();
                	}
                } else if (request.getActionType() == 3) {      // Ky
                	if(request.getLoginBy().equals("phone")) {
                		messageCode = "sms_sign_flow_sign";
                	}else {
                		messageCode = notificationConfig.getCodeSignFlowSign();
                	}
                } else if (request.getActionType() == 4) {      // Ban hanh
                	if(request.getLoginBy().equals("phone")) {
                		messageCode = "sms_sign_flow_publish";
                	}else {
                		messageCode = notificationConfig.getCodeSignFlowPublish();
                	}
                }
            }
        }

        log.info("messageCode: {}", messageCode);

        message = messageService.findByCode(messageCode).orElse(null);


        if (message == null) {
            return StartSignFlowResponse.builder()
                    .success(false)
                    .message(String.format("create notification \"%s\" failure", request))
                    .build();
        }

        // gui email thong bao
        if (message.getMailTemplate() != null) {
            String content = formatContent(message.getMailTemplate(), request);
            String subject = formatContent(message.getName(), request);

            Email email = new Email();
            email.setMessageId(message.getId());
            email.setSubject(subject);
            email.setRecipient(request.getRecipientEmail());
            email.setContent(content);
            email.setCreatedAt(new Date());

            Email emailOpt = emailService.save(email);

            // send to rabbitmq email topic
            try {
                rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
            } catch (Exception e) {
                log.error("error rabbit ", e);

                success = false;
                response += "email";
            }
        }

        // gui sms thong bao
        if (message.getSmsTemplate() != null) {
            try {
                if(request.getLoginBy().equals("phone")) {
                    String content = formatContent(message.getSmsTemplate(), request);
                    String url =  formatContent(message.getUrl(), request);

                    ContractOriginalLinkDto contractLink = ContractOriginalLinkDto.builder()
                            .code(request.getLinkCode())
                            .originalLink(url)
                            .build();

                    var contractOriginalLinkDto = contractService.save(contractLink);

                    if(contractOriginalLinkDto.isPresent()) {
                        if(request.getRecipientPhone() != null) {
                            if(request.getSmsSendMethor() == null || request.getSmsSendMethor().equalsIgnoreCase("API")) {
                                final var responseSend = smsLogService.sendAPI(request.getBrandName(), request.getSmsUser(),
                                        request.getSmsPass(), request.getRecipientPhone(), content,
                                        request.getContractId(), request.getOrgId());
                                //Gửi thành công thì trừ số lượng SMS
                                if(responseSend.getStatus().equalsIgnoreCase("200")) {
                                    customerService.decreaseNumberOfSms(request.getOrgId());
                                }
                            }else {
                                smsLogService.sendSMPP(request.getRecipientPhone(), content, request.getContractId(), request.getOrgId());
                                customerService.decreaseNumberOfSms(request.getOrgId());
                            }
                        }
                    }
                }else{
                    if(request.getContractName().length() > 120){
                        String contractName = request.getContractName().substring(0, 120).toString() + "...";
                        request.setContractName(contractName);
                    }

                    String content = formatContent(message.getSmsTemplate(), request);

                    if(request.getRecipientPhone() != null) {
                        if(request.getSmsSendMethor() == null || request.getSmsSendMethor().equalsIgnoreCase("API")) {
                            final var responseSend = smsLogService.sendAPI(request.getBrandName(), request.getSmsUser(),
                                    request.getSmsPass(), request.getRecipientPhone(), content,
                                    request.getContractId(), request.getOrgId());
                            //Gửi thành công thì trừ số lượng SMS
                            if(responseSend.getStatus().equalsIgnoreCase("200")) {
                                customerService.decreaseNumberOfSms(request.getOrgId());
                            }
                        }else {
                            smsLogService.sendSMPP(request.getRecipientPhone(), content, request.getContractId(), request.getOrgId());
                            customerService.decreaseNumberOfSms(request.getOrgId());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("error rabbit: ", e);

                success = false;

                if (!response.equals("")) {
                    response += ", ";
                }
                response += "sms";
            }
        }

        // gui thong bao tren web
        if (message.getNoticeTemplate() != null) {
            try {
                ContractDto contractDto = contractService.getContract(request.getContractId());

                String subject = formatContent(message.getName(), request);
                String url = formatContent(message.getUrl(), request);

                String content = formatContent(message.getNoticeTemplate(), request);

                // participant name
                int i = 1;
                for (ParticipantDto participantDto: contractDto.getParticipants()) {
                    content = content.replace("#PARTICIPANT_" + i + "#", participantDto.getName());
                    i++;
                }

                Notice notice = new Notice();
                notice.setContractId(request.getContractId());
                notice.setMessageId(message.getId());
                notice.setMessageCode(message.getCode());
                notice.setNoticeName(subject);
                notice.setNoticeContent(content);
                notice.setNoticeUrl(url);
                notice.setEmail(request.getRecipientEmail());
                notice.setNoticeDate(contractDto.getCreatedAt());

                noticeService.save(notice);
            } catch (Exception e) {
                log.error("error: ", e);

                success = false;

                if (!response.equals("")) {
                    response += ", ";
                }
                response += "notice";
            }
        }

        return StartSignFlowResponse.builder()
                .success(success)
                .message(String.format("create notification: %s, %s", success, response))
                .build();
    }

    /**
     * Format content email html
     *
     * @param content
     * @param request
     * @return
     */
    private <T> String formatContent(String content, T request) {

        for (Field field : request.getClass().getDeclaredFields()) {
            field.setAccessible(true); // You might want to set modifier to public first.
            try {
                String name = field.getName().toUpperCase();
                var value =  field.get(request);
                content = content.replace("#" + name + "#", value != null ? value.toString() : "");
                if (name.equals("ACCESSCODE") && value == null && content.contains("<ACCESSCODE>")) {
                    content = content.substring(0, content.indexOf("<ACCESSCODE>")) + content.substring(content.indexOf("</ACCESSCODE>") + 13);
                }
            } catch (Exception e) {
                log.error("error: ", e);
            }
        }

        return content;
    }

    /**
     * Tao email thong bao reset password
     *
     * @param request
     * @return
     */
    @PostMapping("/internal/notification/customerAccountNotice")
    public BaseResponse customerAccountNotice(@Valid @RequestBody AccountNoticeRequest request) {
    	boolean success = true;
    	String response = "";
    	
        log.info("request: {}", request);
        
        String code = notificationConfig.getCodeAccountNotice();
        if ("admin".equals(request.getUserType())) {
            code = notificationConfig.getCodeAdminAccountNotice();
        }

        Message message = messageService.findByCode(code).orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create customer account notice \"%s\" failure", request))
                    .build();
        }

        /**
         * email template
         */
        if (message.getMailTemplate() != null) {
            String content = formatContent(message.getMailTemplate(), request);

            Email email = new Email();
            email.setMessageId(message.getId());
            email.setSubject(message.getName());
            email.setRecipient(request.getEmail());
            email.setContent(content);
            email.setCreatedAt(new Date());

            Email emailOpt = emailService.save(email);

            // send to rabbitmq email topic
            try {
                rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
            } catch (Exception e) { 
                log.error("error: ", e);

                success = false;

                if (!response.equals("")) {
                    response += ", ";
                }
                response += "mail";
            }
        } 
        
        return BaseResponse.builder()
                .success(success)
                .message(String.format("create customer account notice: %s, %s", success, response))
                .build(); 
    }

    @PostMapping("/internal/notification/contractShareNotice")
    public BaseResponse contractShareNotice(@Valid @RequestBody ContractShareNoticeRequest request) {

        log.info("request: {}", request);

        Message message = messageService.findByCode(notificationConfig.getCodeContractShareNotice()).orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create customer account notice \"%s\" failure", request))
                    .build();
        }

        /**
         * email template
         */
        if (message.getMailTemplate() != null) {
            String subject = formatContent(message.getName(), request);
            String content = formatContent(message.getMailTemplate(), request);

            Email email = Email.builder()
                    .messageId(message.getId())
                    .subject(subject)
                    .recipient(request.getEmail())
                    .content(content)
                    .createdAt(new Date()).build(); 

            Email emailOpt = emailService.save(email);

            // send to rabbitmq email topic
            try {
                rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
            } catch (Exception e) {
                log.error("error: ", e);
                return BaseResponse.builder()
                        .success(false)
                        .message(String.format("send email queue \"%s\" fail", request))
                        .build();
            }
        }

        return BaseResponse.builder()
                .success(true)
                .message(String.format("create customer account notice \"%s\" success", request))
                .build();
    }
    
    /**
     * Tạo thông báo sắp hết hạn / quá hạn
     *
     * @param request
     * @return
     */
    @PostMapping("/internal/notification/exprireContract")
    public BaseResponse exprireContract(@Valid @RequestBody StartSignFlowRequest request) {

        log.info("request: {}", request);

        boolean success = true;
        String response = "";

        Message message;
        String messageCode = null;
        
        String roleType = null; 
        
        //add link code
        request.setLinkCode(StringUtil.generateLinkCode());
        
        if(request.getContractStatus() == ContractStatus.ABOUT_EXPRIRE.getDbVal()) {
        	if(request.getLoginBy().equals("phone")) {
        		messageCode = "sms_contract_about_exprire";
        	}else {
        		messageCode = notificationConfig.getCodeContractAboutExprire();
        	} 
        }else if(request.getContractStatus() == ContractStatus.EXPRIRE.getDbVal()) {
        	if(request.getLoginBy().equals("phone")) {
        		messageCode = "sms_contract_exprire";
        	}else {
        		messageCode = notificationConfig.getCodeContractExprire();
        	} 
        }
         
        if (request.getActionType() == 1) {             // Dieu phoi
        	roleType = RoleType.c8.toString();
        } else if (request.getActionType() == 2) {      // Xem xet
        	roleType = RoleType.c9.toString();
        } else if (request.getActionType() == 3) {      // Ky
        	roleType = RoleType.s9.toString();
        } else if (request.getActionType() == 4) {      // Van thu
        	roleType = RoleType.s8.toString();
        }

        message = messageService.findByCode(messageCode).orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create notification \"%s\" failure", request))
                    .build();
        }

        // gui email thong bao
        if (message.getMailTemplate() != null) {
            String content = formatContent(message.getMailTemplate(), request);
            String subject = formatContent(message.getName(), request);
            
            //Thay đổi link theo role của người xử lý
            content = content.replace("#ROLETYPE#", roleType);
            
            Email email = new Email();
            email.setMessageId(message.getId());
            email.setSubject(subject);
            email.setRecipient(request.getRecipientEmail());
            email.setContent(content);
            email.setCreatedAt(new Date());

            Email emailOpt = emailService.save(email);

            // send to rabbitmq email topic
            try {
                rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
            } catch (Exception e) {
                log.error("error rabbit: ",  e);
                success = false;
                response += "email";
            }
        }
        
        // gui sms thong bao
        if (message.getSmsTemplate() != null) {
            try {
                if(request.getLoginBy().equals("phone")) {
                    String content = formatContent(message.getSmsTemplate(), request);
                    String url =  formatContent(message.getUrl(), request);

                    ContractOriginalLinkDto contractLink = ContractOriginalLinkDto.builder()
                            .code(request.getLinkCode())
                            .originalLink(url)
                            .build();

                    var contractOriginalLinkDto = contractService.save(contractLink);

                    if(contractOriginalLinkDto.isPresent()) {
                        if(request.getRecipientPhone() != null) {
                            if(request.getSmsSendMethor() == null || request.getSmsSendMethor().equalsIgnoreCase("API")) {
                                final var responseSend = smsLogService.sendAPI(request.getBrandName(), request.getSmsUser(),
                                        request.getSmsPass(), request.getRecipientPhone(), content,
                                        request.getContractId(), request.getOrgId());
                                //Gửi thành công thì trừ số lượng SMS
                                if(responseSend.getStatus().equalsIgnoreCase("200")) {
                                    customerService.decreaseNumberOfSms(request.getOrgId());
                                }
                            }else {
                                smsLogService.sendSMPP(request.getRecipientPhone(), content, request.getContractId(), request.getOrgId());
                                customerService.decreaseNumberOfSms(request.getOrgId());
                            }
                        }
                    }
                }else{
                    if(request.getContractName().length() > 120){
                        String contractName = request.getContractName().substring(0, 120).toString() + "...";
                        request.setContractName(contractName);
                    }

                    String content = formatContent(message.getSmsTemplate(), request);

                    if(request.getRecipientPhone() != null) {
                        if(request.getSmsSendMethor() == null || request.getSmsSendMethor().equalsIgnoreCase("API")) {
                            final var responseSend = smsLogService.sendAPI(request.getBrandName(), request.getSmsUser(),
                                    request.getSmsPass(), request.getRecipientPhone(), content,
                                    request.getContractId(), request.getOrgId());
                            //Gửi thành công thì trừ số lượng SMS
                            if(responseSend.getStatus().equalsIgnoreCase("200")) {
                                customerService.decreaseNumberOfSms(request.getOrgId());
                            }
                        }else {
                            smsLogService.sendSMPP(request.getRecipientPhone(), content, request.getContractId(), request.getOrgId());
                            customerService.decreaseNumberOfSms(request.getOrgId());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("error: ", e);

                success = false;

                if (!response.equals("")) {
                    response += ", ";
                }
                response += "sms";
            }
        }

        // gui thong bao tren web
        if (message.getNoticeTemplate() != null) {
            try {
                ContractDto contractDto = contractService.getContract(request.getContractId());

                String subject = formatContent(message.getName(), request);
                String url = formatContent(message.getUrl(), request); 
                String content = formatContent(message.getNoticeTemplate(), request);
                 
                //Thay đổi link theo role của người xử lý
                url = url.replace("#ROLETYPE#", roleType);
                
                // participant name
                int i = 1;
                for (ParticipantDto participantDto: contractDto.getParticipants()) {
                    content = content.replace("#PARTICIPANT_" + i + "#", participantDto.getName());
                    i++;
                }

                Notice notice = new Notice();
                notice.setContractId(request.getContractId());
                notice.setMessageId(message.getId());
                notice.setMessageCode(message.getCode());
                notice.setNoticeName(subject);
                notice.setNoticeContent(content);
                notice.setNoticeUrl(url);
                notice.setEmail(request.getRecipientEmail());
                notice.setNoticeDate(contractDto.getCreatedAt());

                noticeService.save(notice);
            } catch (Exception e) {
                log.error("error: ",  e);

                success = false;

                if (!response.equals("")) {
                    response += ", ";
                }
                response += "notice";
            }
        }

        return BaseResponse.builder()
                .success(success)
                .message(String.format("create notification: %s, %s", success, response))
                .build();
    }
    
    @PostMapping("/internal/notification/contractShareTemplate")
    public BaseResponse contractShareTemplate(@Valid @RequestBody ContractShareNoticeRequest request) {

        log.info("request: {}", request);

        Message message = messageService.findByCode(notificationConfig.getCodeContractShareTemplate()).orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create customer account notice \"%s\" failure", request))
                    .build();
        }

        /**
         * email template
         */
        if (message.getMailTemplate() != null) {
            String subject = formatContent(message.getName(), request);
            String content = formatContent(message.getMailTemplate(), request);

            Email email = Email.builder()
                    .messageId(message.getId())
                    .subject(subject)
                    .recipient(request.getEmail())
                    .content(content)
                    .createdAt(new Date()).build(); 

            Email emailOpt = emailService.save(email);

            // send to rabbitmq email topic
            try {
                rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
            } catch (Exception e) {
                log.error("error: ", e);
                return BaseResponse.builder()
                        .success(false)
                        .message(String.format("send email queue \"%s\" fail", request))
                        .build();
            }
        }

        return BaseResponse.builder()
                .success(true)
                .message(String.format("create customer account notice \"%s\" success", request))
                .build();
    }
    
    /**
     * Tạo thông báo cho quản trị khi khách hàng đăng ký tài khoản
     *
     * @param request
     * @return
     */
    @PostMapping("/internal/notification/registrationAccount")
    public BaseResponse registrationAccount(@Valid @RequestBody RegistrationNoticeRequest request) {

        log.info("request: {}", request);

        boolean success = true;
        String response = "";

        Message message;
        String messageCode = null;
        
        messageCode = notificationConfig.getCodeRegistrationAccount();

        message = messageService.findByCode(messageCode).orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create notification \"%s\" failure", request))
                    .build();
        }
        
        //Gửi mail cho người đăng ký 
        if (message.getMailTemplate() != null) {
            String content = formatContent(message.getMailTemplate(), request);
            String subject = formatContent(message.getName(), request);
            
            Email email = new Email();
            email.setMessageId(message.getId());
            email.setSubject(subject);
            email.setRecipient(request.getEmail());
            email.setContent(content);
            email.setCreatedAt(new Date());

            Email emailOpt = emailService.save(email);

            // send to rabbitmq email topic
            try {
                rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
            } catch (Exception e) {
                log.error("error: ", e);
                success = false;
                response += "email";
            }
        }
        
        //Gửi mail cho người quản trị
        messageCode = notificationConfig.getCodeRegistrationAccountManager(); 
        message = messageService.findByCode(messageCode).orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create notification \"%s\" failure", request))
                    .build();
        }
        
        if (message.getMailTemplate() != null) {
            String content = formatContent(message.getMailTemplate(), request);
            String subject = formatContent(message.getName(), request); 
            
            List<UserViewDto> userAdmin = request.getUserAdmin();
            
            for(UserViewDto user: userAdmin) {
            	content = content.replace("#MANAGER#", user.getName());
            			
            	Email email = new Email();
                email.setMessageId(message.getId());
                email.setSubject(subject);
                email.setRecipient(user.getEmail());
                email.setContent(content);
                email.setCreatedAt(new Date());

                Email emailOpt = emailService.save(email);

                // send to rabbitmq email topic
                try {
                    rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
                } catch (Exception e) {
                    log.error("error: ", e);
                    success = false;
                    response += "email";
                }
            } 
        }
        
        return BaseResponse.builder()
                .success(success)
                .message(String.format("create notification: %s, %s", success, response))
                .build();
    }
    
    @PostMapping("/internal/notification/sms")
    public BaseResponse sendSMS(@Valid @RequestBody SmsLogRequest request) {
    	try {
    		if(request.getSmsSendMethor() == null || request.getSmsSendMethor().equalsIgnoreCase("API")) {
    			final var responseSend = smsLogService.sendAPI(request.getBrandName(), request.getSmsUser(), 
    					request.getSmsPass(), request.getIsdn(), request.getMtcontent(),
                        request.getContractId(), request.getOrgId());
    			//Gửi thành công thì trừ số lượng SMS
    			if(responseSend.getStatus().equalsIgnoreCase("200")) {
    				try {
                        log.info("start decreaseNumberOfSms of org : {}", request.getOrgId());
    					customerService.decreaseNumberOfSms(request.getOrgId());
					} catch (Exception e) {
						log.error("error when decreaseNumberOfSms: ", e);
					} 
    			}
        	}else {
        		smsLogService.sendSMPP(request.getIsdn(), request.getMtcontent(), request.getContractId(), request.getOrgId());

        		try {
                    log.info("start decreaseNumberOfSms of org : {}", request.getOrgId());
					customerService.decreaseNumberOfSms(request.getOrgId());
				} catch (Exception e) {
                    log.error("error when decreaseNumberOfSms: ", e);
				} 
        	}
    		
            return BaseResponse.builder()
                    .success(true)
                    .message(String.format("create SMS \"%s\" success", request))
                    .build();
		} catch (Exception e) {
            log.error("error send sms: ", e);
        }
    	
    	return BaseResponse.builder()
                .success(false)
                .message(String.format("create SMS \"%s\" failed", request))
                .build();
    }

    @PostMapping("/internal/notification/serviceExpired")
    public BaseResponse serviceExpired(@Valid @RequestBody ServiceExpiredRequest request) {

        log.info("request: {}", request);

        boolean success = true;
        String response = "";
        Message message;


        message = messageService.findByCode("service_expired").orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create notification \"%s\" failure", request))
                    .build();
        }


        message = messageService.findByCode("service_expired").orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create notification \"%s\" failure", request))
                    .build();
        }

        if (message.getMailTemplate() != null) {
            String content = formatContent(message.getMailTemplate(), request);
            String subject = formatContent(message.getName(), request);

            var emails = request.getEmails();

            for(String recipient: emails) {
                Email email = new Email();
                email.setMessageId(message.getId());
                email.setSubject(subject);
                email.setRecipient(recipient);
                email.setContent(content);
                email.setCreatedAt(new Date());

                Email emailOpt = emailService.save(email);

                // send to rabbitmq email topic
                try {
                    rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
                } catch (Exception e) {
                    log.error("error rabbit: ", e);
                    success = false;
                    response += "email";
                }
            }
        }

        return BaseResponse.builder()
                .success(success)
                .message(String.format("create notification: %s, %s", success, response))
                .build();
    }
    
    /**
     * Tạo thông báo hết hiệu lực hợp đồng
     *
     * @param request
     * @return
     */
    @PostMapping("/internal/notification/exprireContractTime")
    public BaseResponse exprireContractTime(@Valid @RequestBody ContractExprireTimeRequest request) {

        log.info("request: {}", request);

        boolean success = true;
        String response = "";

        Message message;
        String messageCode = null; 
        
        messageCode = notificationConfig.getCodeContractExprireTime();
        message = messageService.findByCode(messageCode).orElse(null);

        if (message == null) {
            return BaseResponse.builder()
                    .success(false)
                    .message(String.format("create notification \"%s\" failure", request))
                    .build();
        }

        // gui email thong bao
        if (message.getMailTemplate() != null) {
            String content = formatContent(message.getMailTemplate(), request);
            String subject = formatContent(message.getName(), request);
            
            Email email = new Email();
            email.setMessageId(message.getId());
            email.setSubject(subject);
            email.setRecipient(request.getRecipientEmail());
            email.setContent(content);
            email.setCreatedAt(new Date());

            Email emailOpt = emailService.save(email);

            // send to rabbitmq email topic
            try {
                rabbitTemplate.convertAndSend(bindingEmail.getExchange(), bindingEmail.getRoutingKey(), objectMapper.writeValueAsString(emailOpt));
            } catch (Exception e) {
                log.error("error: ", e);
                success = false;
                response += "email";
            }
        }

        return BaseResponse.builder()
                .success(success)
                .message(String.format("create notification: %s, %s", success, response))
                .build();
    }
}
