package com.vhc.ec.admin.repository;

import com.vhc.ec.admin.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    Optional<Organization> findFirstByName(String name);

    Optional<Organization> findFirstByNameAndIdNot(String name, int id);

    @Query(value = "select * from organizations where email ilike concat('%', :email, '%')", nativeQuery = true)
    Optional<Organization> findFirstByEmail(@Param("email") String email);

    Optional<Organization> findFirstByEmailAndIdNot(String email, int id);

    Optional<Organization> findFirstByPhone(String phone);

    Optional<Organization> findFirstByPhoneAndIdNot(String phone, int id);

    Optional<Organization> findFirstByTaxCode(String taxCode);

    Optional<Organization> findFirstByTaxCodeAndIdNot(String taxCode, int id);

    @Query("select o from organizations o where " +
            "((cast(:keyword as string) is null or lower(o.name) like '%' || lower(cast(:keyword as string)) || '%') " +
            "or (cast(:keyword as string) is null or lower(o.code) like '%' || lower(cast(:keyword as string)) || '%') ) and" +
            "(cast(:address as string) is null or lower(o.address) like '%' || lower(cast(:address as string)) || '%') and" +
            "(cast(:representative as string) is null or lower(o.representative) like '%' || lower(cast(:representative as string)) || '%') and" +
            "(cast(:email as string) is null or lower(o.email) like '%' || lower(cast(:email as string)) || '%') and" +
            "(cast(:phone as string) is null or lower(o.phone) like '%' || lower(cast(:phone as string)) || '%') and" +
            "(:status = -1 or :status = o.status)" +
            "and parent_id is null")
    Page<Organization> search(String keyword, String address, String representative, String email,
                                     String phone, Integer status, Pageable page);

    @Modifying
    @Transactional
    @Query(value = "delete from customers where organization_id = :id", nativeQuery = true)
    void deleteCustomerByOrgId(int id);

    @Modifying
    @Transactional
    @Query(value = "update customers set email = :email, password = :password where id = :id", nativeQuery = true)
    void updateAdminEmailAndPasswordById(String email, String password, int id);
}
