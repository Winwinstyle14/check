package com.vhc.ec.admin.integration.qldv.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class QldvCustomer {
    @Id
    private long id;

    private String name;

    private String email;

    private String tel;

    private String licenseNo;

    private String taxCode;

    private String address;

    private String adminName;

    private String adminPosition;

    private String adminTel;

    private String adminEmail;

    private String contractId;

    private String contractNo;

    private LocalDate contractDate;

    @Column(name = "a_mid")
    private String aMid;

    @Column(name = "a_mname")
    private String aMname;

    private Integer regionId;

    private String regionName;

    private String agencyId;

    private String agencyName;

    private String tenantCode;

    private String ownerUserName;

    private String ownerEmail;

    private String ownerFullName;

    private Integer type;

    private String token;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate paymentMonth;

    private String paymentType;

    private String prepaidInfo;

    private Long prepaidMoney;

    private String productCode;

    private String productName;

    private LocalDateTime timeStamp;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "qldv_customer_package",
            joinColumns = { @JoinColumn(name = "customer_id") },
            inverseJoinColumns = { @JoinColumn(name = "package_id") }
    )
    private Set<QldvPackage> packages = new HashSet<>();
}
