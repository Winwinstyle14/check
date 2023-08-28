package com.vhc.ec.contract.repository;

import com.vhc.ec.contract.entity.TemplateShare;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface TemplateShareRepository extends CrudRepository<TemplateShare, Integer> {

    Optional<TemplateShare> findFirstByContractIdAndEmail(int contractId, String email);

   // Collection<TemplateShare> findAllByContractIdAndOrganizationId(Integer organizationId, int contractId);

    @Query(
            value = "SELECT c.* FROM template_contract_shares c" +
                    " WHERE c.contract_id = :contractId" +
                    " AND ( ( :organizationId is null ) or ( c.organization_id = cast ( ( cast(:organizationId as varchar) ) as int4) ) )" +
                    " order by c.id desc",
            nativeQuery = true
    )
    Collection<TemplateShare> findByContract(
            @Param("organizationId") Integer organizationId,
            @Param("contractId") int contractId
    );
}
