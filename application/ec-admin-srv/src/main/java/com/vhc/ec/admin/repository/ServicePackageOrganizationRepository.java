package com.vhc.ec.admin.repository;

import com.vhc.ec.admin.entity.association.ServicePackageOrganization;
import com.vhc.ec.admin.entity.id.ServicePackageOrganizationPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServicePackageOrganizationRepository extends JpaRepository<ServicePackageOrganization, ServicePackageOrganizationPK> {

    @Query("select srv from ServicePackageOrganization srv where srv.sentExpiredNotification is false" +
            " and srv.endDate < current_date()")
    List<ServicePackageOrganization> findServiceExpiredHasNotNotice();

    @Query("select srv from ServicePackageOrganization srv where srv.endDate < current_date() " +
            "and srv.usageStatus = com.vhc.ec.admin.constant.UsageStatus.USING")
    List<ServicePackageOrganization> findServiceNeedFinish();
}
