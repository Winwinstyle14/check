package com.vhc.ec.contract.repository;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhc.ec.contract.entity.TemplateContract;

public interface TemplateContractRepository extends JpaRepository<TemplateContract, Integer> {

    /**
     * Truy vấn dữ liệu mẫu hợp đồng của tôi.
     *
     * @param customerId Mã khách hàng
     * @param typeId     Mã loại hợp đồng
     * @param name       Tên hợp đồng
     * @param pageable   Phân trang dữ liệu
     * @return Phân trang dữ liệu hợp đồng của tôi
     */
    @Query(
            value = "SELECT c.* " +
                    " FROM template_contracts c" +
                    " WHERE ( c.customer_id = :customerId )" +
                    " AND ( c.name ilike %:name% )" +
                    " AND ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    " AND c.status != 0 " +
                    " order by c.created_at desc",
            nativeQuery = true
    )
    Page<TemplateContract> searchMyContract(
            @Param("customerId") int customerId,
            @Param("typeId") Integer typeId,
            @Param("name") String name,
            Pageable pageable
    );

    /**
     * Truy vấn dữ liệu mẫu hợp đồng được chia sẻ
     *
     * @param email      email đăng nhập
     * @param typeId     Mã loại hợp đồng
     * @param name       Tên hợp đồng
     * @param pageable   Phân trang dữ liệu
     * @return Phân trang dữ liệu hợp đồng của tôi
     */
    @Query(
            value = "select c.* from template_contracts c" +
                    "  inner join template_contract_shares s on c.id = s.contract_id" +
                    " where s.email = :email" +
                    "   and ( c.name ilike %:name% )" +
                    "   and ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    "	and c.status = 10" +
                    " order by c.created_at desc",
            nativeQuery = true
    )
    Page<TemplateContract> getShares(
            @Param("email") String email,
            @Param("typeId") Integer typeId,
            @Param("name") String name,
            Pageable pageable
    );

    /**
     *
     */
    @Query(
            value = "select c.* from template_contracts c" +
                    " where (upper(c.code) = upper(:code))" +
                    " 	and (c.status <> :status)" +
                    "   and ( ( :contractId is null ) or ( c.id <> cast ( ( cast(:contractId as varchar) ) as int4) ) )" +
                    " 	and (c.organization_id = cast ( ( cast(:organizationId as varchar) ) as int4))" +
                    "   and (( date_trunc('day', c.start_time) <= cast( ( cast(:startTime as varchar) ) as date ) and date_trunc('day', c.end_time) >= cast( ( cast(:startTime as varchar) ) as date ))" +
                    "   or ( date_trunc('day', c.start_time) <= cast( ( cast(:endTime as varchar) ) as date ) and date_trunc('day', c.end_time) >= cast( ( cast(:endTime as varchar) ) as date )))" +
                    "   limit 1",
            nativeQuery = true
    )
    Optional<TemplateContract> findAllByCodeStartTimeEndTimeOrgId(
            @Param("code") String code,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("organizationId") int organizationId,
            @Param("status") int status,
            @Param("contractId") Integer contractId
    );
    
    /**
    *	Danh sách mẫu hợp đồng tạo, được chia sẻ có hiệu lực và đang phát hành
    */
    @Query(
            value = "select distinct c.* from template_contracts c " + 
            		" left join template_contract_shares s " + 
            		" on c.id = s.contract_id " + 
            		" where (c.created_by = :customerId or s.customer_id = :customerId) " + 
            		" and c.status = 10 " + 
            		" and (CURRENT_DATE BETWEEN cast(c.start_time as date) AND cast(c.end_time as date)) " + 
            		" order by c.created_at desc",
            nativeQuery = true
    )
    Collection<TemplateContract> getTemplateList(
            @Param("customerId") int customerId
    );

    /**
     * Số lượng hợp đồng mà người dùng được phép view
     * @param customerId
     * @param email
     * @param contractId
     * @return
     */
    @Query(
            value = " select count(1) from " +
                    " (select c.id from template_contracts c " +
                    " where created_by = :customerId " +
                    " and id = :contractId " +
                    " union " +
                    " select c.id from template_contracts c " +
                    " join template_contract_shares s on c.id = s.contract_id " +
                    " where s.email = :email " +
                    " and c.id = :contractId) a ",
            nativeQuery = true
    )
    Integer countContractViewByUser(
            @Param("customerId") int customerId,
            @Param("email") String email,
            @Param("contractId") int contractId
    );
}
