package com.vhc.ec.customer.repository;

import com.vhc.ec.customer.dto.SuggestedCustomerDto;
import com.vhc.ec.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface CustomerRepository extends CrudRepository<Customer, Integer> {

    Optional<Customer> findTopByEmail(String email);

    Collection<Customer> findByOrganizationIdOrderByNameAsc(int organizationId);

    @Query(
            value = "select o.* from customers o, organizations g" +
                    " where ( (:name is null) or (o.name ilike concat('%', :name, '%')) )" +
                    "   and ( (:email is null) or (o.email ilike concat('%', :email, '%')) )" +
                    "   and ( (:phone is null) or (o.phone ilike concat('%', :phone, '%')) )" +
                    "   and ( (:orgId is null) or (o.organization_id = cast ( ( cast(:orgId as varchar) ) as int4) ))" +
                    "   and ( o.organization_id = g.id )" +
                    "   and ( g.path <@ ( select r.path from organizations r where r.id = :currOrgId) " +
                    "          or g.path @> ( select r.path from organizations r where r.id = :currOrgId))" +
                    "order by g.name , o.name",
            nativeQuery = true
    )
    Page<Customer> search(
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("orgId") Integer orgId,
            @Param("currOrgId") int currOrgId,
            Pageable pageable
    );

    Optional<Customer> findFirstByRoleId(int roleId);

    Optional<Customer> findFirstByPhone(String phone);

    @Query(value = "select name, email, phone from customers where organization_id in :orgList and " +
            "name ilike concat('%', :name, '%')", nativeQuery = true)
    <T> List<T> findByOrgListAndNameLike(List<Integer> orgList, String name, Class<T> type);
}
