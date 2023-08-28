package com.vhc.ec.admin.entity.association;

import com.vhc.ec.admin.constant.PaymentStatus;
import com.vhc.ec.admin.constant.PaymentType;
import com.vhc.ec.admin.constant.UsageStatus;
import com.vhc.ec.admin.entity.Organization;
import com.vhc.ec.admin.entity.ServicePackage;
import com.vhc.ec.admin.entity.id.ServicePackageOrganizationPK;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "service_package_organizations")
@Getter
@Setter
public class ServicePackageOrganization {
    @EmbeddedId
    private ServicePackageOrganizationPK id;

    private PaymentStatus paymentStatus;

    private PaymentType paymentType;

    private LocalDate purchaseDate;

    private LocalDate paymentDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private UsageStatus usageStatus;

    private boolean sentExpiredNotification;

    @ManyToOne
    @MapsId("organizationId")
    @JoinColumn(name = "organizations_id")
    private Organization organization;

    @ManyToOne
    @MapsId("serviceId")
    @JoinColumn(name = "services_id")
    private ServicePackage servicePackage;
}
