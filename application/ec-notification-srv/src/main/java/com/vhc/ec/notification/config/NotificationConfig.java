package com.vhc.ec.notification.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Configuration
@Data
public class NotificationConfig {

    @Value("${vhc.ec.notification.code.reset-password:reset_password}")
    private String codeResetPassword;

    @Value("${vhc.ec.notification.code.account-notice:account_notice}")
    private String codeAccountNotice;

    @Value("${vhc.ec.notification.code.admin-account-notice:admin_account_notice}")
    private String codeAdminAccountNotice;

    @Value("${vhc.ec.notification.code.sign-flow-coordinator:sign_flow_coordinator}")
    private String codeSignFlowCoordinator;

    @Value("${vhc.ec.notification.code.sign-flow-review:sign_flow_review}")
    private String codeSignFlowReview;

    @Value("${vhc.ec.notification.code.sign-flow-sign:sign_flow_sign}")
    private String codeSignFlowSign;

    @Value("${vhc.ec.notification.code.sign-flow-publish:sign_flow_publish}")
    private String codeSignFlowPublish;

    @Value("${vhc.ec.notification.code.sign-flow-reject:sign_flow_reject}")
    private String codeSignFlowReject;

    @Value("${vhc.ec.notification.code.sign-flow-finish:sign_flow_finish}")
    private String codeSignFlowFinish;

    @Value("${vhc.ec.notification.code.contract-share-notice:contract_share_notice}")
    private String codeContractShareNotice;
    
    @Value("${vhc.ec.notification.code.contract-about-exprire:contract_about_exprire}")
    private String codeContractAboutExprire; 
    
    @Value("${vhc.ec.notification.code.contract-exprire:contract_exprire}")
    private String codeContractExprire;
    
    @Value("${vhc.ec.notification.code.contract-share-template:contract_share_template}")
    private String codeContractShareTemplate;
    
    @Value("${vhc.ec.notification.code.registration-account:registration_account}")
    private String codeRegistrationAccount;
    
    @Value("${vhc.ec.notification.code.registration-manager:registration_manager}")
    private String codeRegistrationManager;
    
    @Value("${vhc.ec.notification.code.registration-account-manager:registration_account_manager}")
    private String codeRegistrationAccountManager;
    
    @Value("${vhc.ec.notification.code.contract-exprire-time:contract_exprire_time}")
    private String codeContractExprireTime;
}
