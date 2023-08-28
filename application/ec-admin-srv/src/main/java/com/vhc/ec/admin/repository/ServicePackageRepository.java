package com.vhc.ec.admin.repository;

import com.vhc.ec.admin.entity.ServicePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {
    Optional<ServicePackage> findByCode(String code);

    Optional<ServicePackage> findByCodeAndIdNot(String code, long id);

    @Query("select sv  from ServicePackage sv where " +
            "(cast(:code as string) is null or lower(sv.code) like '%' || lower(cast(:code as string)) || '%') and " +
            "(cast(:name as string) is null or lower(sv.name) like '%' || lower(cast(:name as string)) || '%') and " +
            "(:totalBeforeVAT = -1L or sv.totalBeforeVAT = :totalBeforeVAT) and " +
            "(:totalAfterVAT = -1L or sv.totalAfterVAT = :totalAfterVAT) and " +
            "(:duration = -1 or sv.duration = :duration) and " +
            "(:numberOfContracts = -1 or sv.numberOfContracts = :numberOfContracts) and " +
            "(:status = -1 or sv.status = :status)")
    Page<ServicePackage> search(String code, String name, Long totalBeforeVAT, Long totalAfterVAT,
                                Integer duration, Integer numberOfContracts, Integer status, Pageable pageable);
}
