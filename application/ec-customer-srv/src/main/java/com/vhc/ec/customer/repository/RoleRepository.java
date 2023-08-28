package com.vhc.ec.customer.repository;

import com.vhc.ec.customer.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query(
            value = "select r.* from roles r" +
                    " where ( (:name is null) or (r.name ilike concat('%', :name, '%')) )" +
                    "   and ( (:code is null) or (r.code ilike concat('%', :code, '%')) )" +
                    "   and ( r.organization_id = :org_id )" +
                    " order by name",
            nativeQuery = true
    )
    Page<Role> search(@Param("name") String name, @Param("code") String code, @Param("org_id") int orgId, Pageable pageable);

    List<Role> findByOrganizationId(int orgId);

    Optional<Role> findFirstByNameIgnoreCaseAndOrganizationId(String name, int organizationId);

    Optional<Role> findFirstByCodeIgnoreCaseAndOrganizationId(String code, int organizationId);

    Page<Role> findByOrganizationIdOrderByNameAsc(int organizationId, Pageable pageable);
}
