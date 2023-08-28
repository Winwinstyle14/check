package com.vhc.ec.customer.repository;

import com.vhc.ec.customer.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface OrganizationRepository extends CrudRepository<Organization, Integer> {

    /**
     * Lấy thông tin về tổ chức của khách hàng
     *
     * @param id Mã khách hàng
     * @return Thông tin chi tiết về tổ chức
     */
    @Query(
            value = "select o.* from organizations o" +
                    " inner join customers c on o.id = c.organization_id" +
                    " where c.id = ?",
            nativeQuery = true
    )
    Optional<Organization> findByCustomerId(int id);

    @Query(
            value = "select o.* from organizations o " +
                    " where ( ( :name is null ) or ( o.name ilike concat('%', :name, '%') ) )" +
                    "   and ( ( :code is null ) or ( o.code ilike concat('%', :code, '%') ) )" +
                    "   and ( o.path <@ (select o2.path from organizations o2 where o2.id = :currOrgId) )",
            nativeQuery = true
    )
    Page<Organization> search(
            @Param("currOrgId") int currOrgId,
            @Param("name") String name,
            @Param("code") String code,
            Pageable pageable
    );

    //Kiểm tra mã tổ chức
    @Query(
            value = "select o.* from organizations o " +
                    " where ( upper(code) = upper(:code) )" + 
                    " and (id = :orgId or parent_id = :orgId ) limit 1",
            nativeQuery = true
    )
    Optional<Organization> findByCodeOrgId(
    		@Param("code") String code,
            @Param("orgId") int orgId
    );
    
    //Kiểm tra tên tổ chức
    @Query(
    		value = "select o.* from organizations o " +
                    " where ( upper(name) = upper(:name) )" + 
                    " and (id = :orgId or parent_id = :orgId ) limit 1",
            nativeQuery = true
    )
    Optional<Organization> findByNameOrgId( 
            @Param("name") String name,
            @Param("orgId") int orgId
    );


    @Query(value = "WITH RECURSIVE child_org AS (" +
            "    select id from organizations where parent_id = :id " +
            "    UNION " +
            "        select org.id FROM organizations org " +
            "        INNER JOIN child_org child ON child.id = org.parent_id " +
            ") SELECT * from child_org", nativeQuery = true)
    List<Integer> findChildIdRecursiveById(int id);

    Optional<Organization> findByEmail(String email);

    Optional<Organization> findByPhone(String phone);

    @Query(
            value = "select  sv.number_of_contracts " +
                    " from service_package_organizations spo " +
                    " join organizations o on o.id = spo.organizations_id" +
                    " join service_package sv on sv.id = spo.services_id" +
                    " where o.id = :id and sv.calculator_method = 1 and spo.usage_status = 1",
            nativeQuery = true
    )
    Integer getNumberContractInPurchasedService(int id);
    
    Optional<Organization> findFirstByTaxCode(String taxCode);
    
    Optional<Organization> findFirstByTaxCodeAndIdNot(String taxCode, int id);

    @Query(value = "select o.id from organizations o where" +
            " o.path <@ (select path from organizations where id = :id)" +
            " or o.path @> (select path from organizations where id = :id)",
            nativeQuery = true)
    List<Integer> findAllOrgInTree(int id);

    List<Organization> findByParentId(int parentId);

}
