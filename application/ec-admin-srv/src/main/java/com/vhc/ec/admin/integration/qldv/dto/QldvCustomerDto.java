package com.vhc.ec.admin.integration.qldv.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@ToString
public class QldvCustomerDto {

    @JsonProperty("customerId")
    private long id;

    @JsonProperty("customerName")
    private String name;

    @JsonProperty("customerEmail")
    private String email;

    @JsonProperty("customerTel")
    private String tel;

    @JsonProperty("licenseNo")
    private String licenseNo;

    @JsonProperty("taxCode")
    private String taxCode;

    @JsonProperty("customerAddress")
    private String address;

    @JsonProperty("adminName")
    private String adminName;

    @JsonProperty("adminPosition")
    private String adminPosition;

    @JsonProperty("adminTel")
    private String adminTel;

    @JsonProperty("adminEmail")
    private String adminEmail;

    @JsonProperty("contractID")
    private String contractId;

    @JsonProperty("contractNo")
    private String contractNo;

    @JsonProperty("contractDate")
    @JsonFormat(pattern="yyyy/MM/dd")
    private LocalDate contractDate;

    @JsonProperty("aMID")
    private String aMid;

    @JsonProperty("aMName")
    private String aMname;

    @JsonProperty("regionID")
    private Integer regionId;

    @JsonProperty("regionName")
    private String regionName;

    @JsonProperty("agencyID")
    private String agencyId;

    @JsonProperty("agencyName")
    private String agencyName;

    @JsonProperty("tenantCode")
    private String tenantCode;

    @JsonProperty("ownerUserName")
    private String ownerUserName;

    @JsonProperty("ownerEmail")
    private String ownerEmail;

    @JsonProperty("ownerFullname")
    private String ownerFullName;

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("token")
    private String token;

    @JsonProperty("startDate")
    @JsonFormat(pattern="yyyy/MM/dd")
    private LocalDate startDate;

    @JsonProperty("endDate")
    @JsonFormat(pattern="yyyy/MM/dd")
    private LocalDate endDate;

    @JsonProperty("paymentMonth")
    @JsonFormat(pattern="yyyy/MM/dd")
    private LocalDate paymentMonth;

    @JsonProperty("paymentType")
    private String paymentType;

    @JsonProperty("prepaidInfo")
    private String prepaidInfo;

    @JsonProperty("prepaidMoney")
    private Long prepaidMoney;

    @JsonProperty("productCode")
    private String productCode;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("timeStamp")
    @JsonFormat(pattern="yyyy/MM/dd HH:mm:ss")
    private LocalDateTime timeStamp;

    @JsonProperty("listPackage")
    private Set<QldvPackageDto> packages;
}
