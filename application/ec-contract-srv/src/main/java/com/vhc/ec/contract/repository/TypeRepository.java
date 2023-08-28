package com.vhc.ec.contract.repository;

import com.vhc.ec.contract.entity.Type;
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
public interface TypeRepository extends CrudRepository<Type, Integer> {

    @Query(
            value = "SELECT t FROM types t" +
                    " WHERE t.organizationId = :organizationId" +
                    "   AND (t.name LIKE %:name%)" +
                    "   AND (t.code LIKE %:code%)" +
                    " ORDER BY t.ordering ASC"
    )
    Collection<Type> findByOrganizationIdOrderByOrdering(
            @Param("organizationId") int organizationId,
            @Param("name") String name,
            @Param("code") String code);

    /**
     * find first organization by id an name
     *
     * @param organizationId id of organization need to get
     * @param name           name of organization need to get
     * @return {@link Type}
     */
    Optional<Type> findFirstByOrganizationIdAndNameIgnoreCase(int organizationId, String name);

    /**
     * find first organization by id and code
     *
     * @param organizationId organization id
     * @param code           organization code
     * @return {@link Type}
     */
    Optional<Type> findFirstByOrganizationIdAndCodeIgnoreCase(int organizationId, String code);

}
