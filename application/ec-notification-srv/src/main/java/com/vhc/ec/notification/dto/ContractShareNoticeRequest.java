package com.vhc.ec.notification.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ContractShareNoticeRequest implements Serializable {
    
    @NotBlank(message = "CustomerName is mandatory")
    private String customerName;

    private String senderName;

    private String senderParticipant;

    @NotBlank(message = "contractName is mandatory")
    private String contractName;

    private String email;

    private String phone;

    private String accessCode;

    private String loginType;

    @NotBlank(message = "contractUrl is mandatory")
    private String contractUrl;

    private String contractUid;
}
