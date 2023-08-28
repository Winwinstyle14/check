package com.vhc.ec.admin.entity.id;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ServicePackageOrganizationPK implements Serializable {
    @Column(name = "services_id")
    private long serviceId;

    @Column(name = "organizations_id")
    private int organizationId;
}
