package com.vhc.ec.admin.entity;

import com.vhc.ec.admin.constant.BaseStatus;
import com.vhc.ec.admin.constant.CalculatorMethod;
import com.vhc.ec.admin.constant.ServiceType;
import com.vhc.ec.admin.entity.association.ServicePackageOrganization;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "service_package")
@Getter
@Setter
public class ServicePackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_before_vat")
    private long totalBeforeVAT;

    @Column(name = "total_after_vat")
    private long totalAfterVAT;

    @Column(nullable = false)
    private CalculatorMethod calculatorMethod;

    private ServiceType type;

    private Integer duration; // by month

    private Integer numberOfContracts;

    private Integer numberOfEkyc;

    private Integer numberOfSms;

    private String description;

    private BaseStatus status;

    @OneToMany(mappedBy = "servicePackage")
    private Set<ServicePackageOrganization> organizations = new HashSet<>();
}
