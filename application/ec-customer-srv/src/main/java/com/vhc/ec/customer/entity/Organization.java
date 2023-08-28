package com.vhc.ec.customer.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class Organization extends Base implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    @NotBlank(message = "Name is mandatory")
    @Length(max = 600, message = "Name '${validatedValue}' must be less than {max} characters long")
    private String name;

    @Column(name = "short_name")
    @Length(max = 63, message = "Short name '${validatedValue}' must be less than {max} characters long")
    private String shortName;

    @Column
    @NotBlank(message = "Code is mandatory")
    @Length(max = 63, message = "Code '${validatedValue}' must be less than {max} characters long")
    private String code;

    @Column
    //@NotBlank(message = "Email is mandatory")
    @Length(max = 255, message = "Email '${validatedValue}' must be less than {max} characters long")
    private String email;

    @Column
    //@NotBlank(message = "Phone is mandatory")
    @Length(max = 15, message = "Phone '${validatedValue}' must be less than {max} characters long")
    private String phone;

    @Column
    @Length(max = 15, message = "Fax '${validatedValue}' must be less than {max} characters long")
    private String fax;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(columnDefinition = "ltree")
    @org.hibernate.annotations.Type(type = "com.vhc.ec.customer.dbtype.LTreeType")
    private String path;
    
    @Column(name = "tax_code")
    private String taxCode;
     
    @Column(name = "ceca_push_mode")
    private String cecaPushMode;
    
    // thoi gian user con login duoc tinh tu ngay to chuc tam dung, huy dich vu, don vi thang
    // ten truong nay do mbf yeu cau dat ten nhu vay
    private Integer endTime;

    // ngay tam dung, huy dich vu
    private LocalDate stopServiceDay;

    // so luong hop dong co the tao
    private Integer numberOfContractsCanCreate;

    private Integer numberOfEkyc;

    private Integer numberOfSms;

    private LocalDate startLicense;

    private LocalDate endLicense;

    // tong so luong hop dong trong cac goi so luong hop dong da dang ky
    private Integer totalContractsPurchased;

    private Integer totalSmsPurchased;

    private Integer totalEkycPurchased;

    private Integer totalPackagePurchased;
    
    private String brandName;
    
    private String smsUser;
    
    private String smsPass;
    
    private String smsSendMethor;

    @Column(name = "usb_token_version")
    private String usbTokenVersion;
}
