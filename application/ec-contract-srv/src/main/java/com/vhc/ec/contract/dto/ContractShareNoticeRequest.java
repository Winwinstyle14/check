package com.vhc.ec.contract.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

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
