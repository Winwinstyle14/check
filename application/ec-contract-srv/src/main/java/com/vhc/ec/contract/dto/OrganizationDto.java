package com.vhc.ec.contract.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationDto implements Serializable {

    private int id;

    private String name;

    private String code;
    
    private String brandName;
    
    private String smsUser;
    
    private String smsPass;
    
    private String smsSendMethor;

    private String path;

    private Integer numberOfContractsCanCreate;

    private Integer numberOfEkyc;

    private Integer numberOfSms;

}
