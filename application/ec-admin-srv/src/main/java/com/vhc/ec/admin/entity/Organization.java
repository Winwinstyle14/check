package com.vhc.ec.admin.entity;

import com.vhc.ec.admin.constant.CeCAPushMode;
import com.vhc.ec.admin.constant.OrgStatus;
import com.vhc.ec.admin.entity.association.ServicePackageOrganization;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Entity(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Organization implements Serializable {
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

    @Column
    private String address;

    private String size; // quy mo

    private String representative;

    private String position;

    private String taxCode;

    private Boolean createdByAdmin;

    @Column(columnDefinition = "int4")
    @Enumerated(EnumType.ORDINAL)
    private OrgStatus status;

    @Column(columnDefinition = "ltree")
    @org.hibernate.annotations.Type(type = "com.vhc.ec.admin.entity.dbtype.LTreeType")
    private String path;

    @Column(name="CeCA_push_mode")
    @Enumerated(EnumType.STRING)
    private CeCAPushMode ceCAPushMode;

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

    @OneToMany(mappedBy = "organization")
    private Set<ServicePackageOrganization> services = new HashSet<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Date updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    private String smsSendMethor = "API";
}
