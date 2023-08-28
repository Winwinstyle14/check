package com.vhc.ec.contract.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.vhc.ec.contract.definition.ContractStatus;
import com.vhc.ec.contract.entity.Contract;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface ContractRepository extends CrudRepository<Contract, Integer> {

    Optional<Contract> findFirstById(int id);

    /**
     * Truy váº¥n dá»¯ liá»‡u há»£p Ä‘á»“ng cá»§a tÃ´i.
     *
     * @param customerId MÃ£ khÃ¡ch hÃ ng
     * @param typeId     MÃ£ loáº¡i há»£p Ä‘á»“ng
     * @param fromDate   NgÃ y táº¡o (tá»« ngÃ y)
     * @param toDate     NgÃ y táº¡o (tá»›i ngÃ y)
     * @param status     Tráº¡ng thÃ¡i há»£p Ä‘á»“ng
     * @param pageable   PhÃ¢n trang dá»¯ liá»‡u
     * @return PhÃ¢n trang dá»¯ liá»‡u há»£p Ä‘á»“ng cá»§a tÃ´i
     */
    @Query(
            value = "SELECT distinct c.* FROM contracts c  " +
                    "join participants p on c.id = p.contract_id " +
                    "join recipients r on p.id = r.participant_id " +
                    "left join (" +
                    "    select p.contract_id, p.id, p.name from contracts c, participants p " +
                    "        where c.id = p.contract_id " +
                    ") partner " +
                    "on c.id = partner.contract_id and r.participant_id <>  partner.id" +
                    " WHERE ( c.customer_id = :customerId )" +
                    " AND ( c.organization_id  = :organizationId )" +
                    " and ((cast(:keyword as varchar) is null or (c.name ilike concat('%', cast(:keyword as varchar), '%'))) " +
                    "   or (( cast(:keyword as varchar) is null  is null or (partner.name ilike concat('%', cast(:keyword as varchar), '%')) ))) " +
                    " and ( cast(:contractNo as varchar) is null or (c.contract_no ilike concat('%', cast(:contractNo as varchar), '%')) )" +
                    " AND ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    " AND ( ( cast(:fromDate as varchar) is null ) or ( date_trunc('day', c.created_at) >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    " AND ( ( cast(:toDate as varchar) is null ) or ( date_trunc('day', c.created_at) <= cast( ( cast(:toDate as varchar) ) as date ) ) )" +
                    " AND ( " +
                    "   ( :status = -1 ) or " +
                    //"   ( c.status = cast ( ( cast(:status as varchar) ) as int4) ) or " +
                    "   ( 0 = :status and c.status = 0 ) or " +                             // ban nhap
                    "   ( 30 = :status and c.status = 30 ) or " +                           // hoan thanh
                    "   ( 31 = :status and c.status = 31 ) or " +                           // tu choi
                    "   ( 32 = :status and c.status = 32 ) or " +                           // huy bo
                    "   ( 20 = :status and c.status = 20 and CURRENT_DATE <= cast(c.sign_time as date) ) or " +   // dang xu ly
                    "   ( 33 = :status and c.status = 20 and CURRENT_DATE <= cast(c.sign_time as date) and cast(c.sign_time as date) < cast( ( cast(:remainTime as varchar) ) as date )) or " +   // sap het han
                    "   ( 34 = :status and c.status = 20 and CURRENT_DATE > cast(c.sign_time as date) ) " +      // qua han
                    ") " +
                    "order by c.created_at desc",
            nativeQuery = true
    )
    Page<Contract> searchMyContract(
    		@Param("organizationId") int organizationId,
            @Param("customerId") int customerId,
            @Param("typeId") Integer typeId,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("status") Integer status,
            @Param("remainTime") Date remainTime,
            String keyword,
            String contractNo,
            Pageable pageable
    );
    
    @Query(
            value = "SELECT c.* FROM contracts c" +
                    " WHERE ( c.customer_id = :customerId )" +
                    " AND ( c.organization_id  != :organizationId )" +
                    " AND ( c.name ilike %:name% )" +
                    " AND ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    " AND ( ( cast(:fromDate as varchar) is null ) or ( date_trunc('day', c.created_at) >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    " AND ( ( cast(:toDate as varchar) is null ) or ( date_trunc('day', c.created_at) <= cast( ( cast(:toDate as varchar) ) as date ) ) )" +
                    " AND ( 30 = :status and c.status = 30 ) " +                           // hoan thanh 
                    " order by c.created_at desc",
            nativeQuery = true
    )
    Page<Contract> searchMyContractOld(
    		@Param("organizationId") int organizationId,
            @Param("customerId") int customerId,
            @Param("typeId") Integer typeId,
            @Param("name") String name,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("status") Integer status,
            Pageable pageable
    );

    /**
     * Truy váº¥n dá»¯ liá»‡u há»£p Ä‘á»“ng cá»§a tá»• chá»©c.
     *
     * @param typeId     MÃ£ loáº¡i há»£p Ä‘á»“ng
     * @param name       TÃªn há»£p Ä‘á»“ng
     * @param fromDate   NgÃ y táº¡o (tá»« ngÃ y)
     * @param toDate     NgÃ y táº¡o (tá»›i ngÃ y)
     * @param status     Tráº¡ng thÃ¡i há»£p Ä‘á»“ng
     * @param pageable   PhÃ¢n trang dá»¯ liá»‡u
     * @return PhÃ¢n trang dá»¯ liá»‡u há»£p Ä‘á»“ng cá»§a tÃ´i
     */
    @Query(
            value = "SELECT c.* FROM contracts c" +
                    " WHERE ( c.name ilike %:name% )" +
                    " AND ( ( :organizationId is null ) or ( c.organization_id = cast ( ( cast(:organizationId  as varchar) ) as int4) ) )" +
                    " AND ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    " AND ( ( cast(:fromDate as varchar) is null ) or ( date_trunc('day', c.created_at) >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    " AND ( ( cast(:toDate as varchar) is null ) or ( date_trunc('day', c.created_at) <= cast( ( cast(:toDate as varchar) ) as date ) ) )" +
                    " AND ( " +
                    "   ( :status = -1 ) or " +
                    "   ( 0 = :status and c.status = 0 ) or " +                             // ban nhap
                    "   ( 30 = :status and c.status = 30 ) or " +                           // hoan thanh
                    "   ( 31 = :status and c.status = 31 ) or " +                           // tu choi
                    "   ( 32 = :status and c.status = 32 ) or " +                           // huy bo
                    "   ( 20 = :status and c.status = 20 and CURRENT_DATE <= cast(c.sign_time as date) ) or " +   // dang xu ly
                    "   ( 33 = :status and c.status = 20 and CURRENT_DATE <= cast(c.sign_time as date) and cast(c.sign_time as date) < cast( ( cast(:remainTime as varchar) ) as date )) or " +   // sap het han
                    "   ( 34 = :status and c.status = 20 and CURRENT_DATE > cast(c.sign_time as date) ) " +      // qua han
                    ") " +
                    "order by c.created_at desc",
            nativeQuery = true
    )
    Page<Contract> searchMyOrgContract(
            @Param("organizationId") Integer organizationId,
            @Param("typeId") Integer typeId,
            @Param("name") String name,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("status") Integer status,
            @Param("remainTime") Date remainTime,
            Pageable pageable
    );


    @Query(
            value = "SELECT distinct c.* FROM contracts c  " +
                    "join participants p on c.id = p.contract_id " +
                    "join recipients r on p.id = r.participant_id " +
                    "left join (" +
                    "    select p.contract_id, p.id, p.name from contracts c, participants p " +
                    "        where c.id = p.contract_id " +
                    ") partner " +
                    "on c.id = partner.contract_id and r.participant_id <>  partner.id" +
                    " where ((cast(:keyword as varchar) is null or (c.name ilike concat('%', cast(:keyword as varchar), '%'))) " +
                    "   or (( cast(:keyword as varchar) is null  is null or (partner.name ilike concat('%', cast(:keyword as varchar), '%')) ))) " +
                    " AND ( c.organization_id in :orgIds)" +
                    " AND ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    " AND ( ( cast(:fromDate as varchar) is null ) or ( date_trunc('day', c.created_at) >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    " AND ( ( cast(:toDate as varchar) is null ) or ( date_trunc('day', c.created_at) <= cast( ( cast(:toDate as varchar) ) as date ) ) )" +
                    " AND ( " +
                    "   ( :status = -1 ) or " +
                    "   ( 0 = :status and c.status = 0 ) or " +                             // ban nhap
                    "   ( 30 = :status and c.status = 30 ) or " +                           // hoan thanh
                    "   ( 31 = :status and c.status = 31 ) or " +                           // tu choi
                    "   ( 32 = :status and c.status = 32 ) or " +                           // huy bo
                    "   ( 20 = :status and c.status = 20 and CURRENT_DATE <= cast(c.sign_time as date) ) or " +   // dang xu ly
                    "   ( 33 = :status and c.status = 20 and CURRENT_DATE <= cast(c.sign_time as date) and cast(c.sign_time as date) < cast( ( cast(:remainTime as varchar) ) as date )) or " +   // sap het han
                    "   ( 34 = :status and c.status = 20 and CURRENT_DATE > cast(c.sign_time as date) ) " +      // qua han
                    ") " +
                    "order by c.created_at desc",
            nativeQuery = true
    )
    Page<Contract> findMyOrgAndDescendantContract(List<Integer> orgIds, Integer typeId, String keyword,
                                                  Date fromDate, Date toDate, Integer status, Date remainTime,
                                                  Pageable pageable);

    @Query(
            value = "SELECT c.* FROM contracts c" +
                    " WHERE ( c.name ilike %:name% )" +
                    " AND ( c.organization_id in :orgIds)" +
                    " AND ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    " AND ( ( cast(:fromDate as varchar) is null ) or ( date_trunc('day', c.created_at) >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    " AND ( ( cast(:toDate as varchar) is null ) or ( date_trunc('day', c.created_at) <= cast( ( cast(:toDate as varchar) ) as date ) ) )" +
                    " AND ( " +
                    "   ( :status = -1 ) or " +
                    "   ( 0 = :status and c.status = 0 ) or " +                             // ban nhap
                    "   ( 30 = :status and c.status = 30 ) or " +                           // hoan thanh
                    "   ( 31 = :status and c.status = 31 ) or " +                           // tu choi
                    "   ( 32 = :status and c.status = 32 ) or " +                           // huy bo
                    "   ( 20 = :status and c.status = 20 and CURRENT_DATE <= cast(c.sign_time as date) ) or " +   // dang xu ly
                    "   ( 33 = :status and c.status = 20 and CURRENT_DATE <= cast(c.sign_time as date) and cast(c.sign_time as date) < cast( ( cast(:remainTime as varchar) ) as date )) or " +   // sap het han
                    "   ( 34 = :status and c.status = 20 and CURRENT_DATE > cast(c.sign_time as date)) " +      // qua han
                    ") " +
                    "order by c.created_at desc",
            nativeQuery = true
    )
    List<Contract> findAllMyOrgAndDescendantContract(List<Integer> orgIds, Integer typeId, String name,
                                                  Date fromDate, Date toDate, Integer status, Date remainTime);

    @Query(
            value = "select c.* from contracts c" +
                    "  inner join shares s on c.id = s.contract_id" +
                    " where s.email = :email" +
                    " and (cast(:name as varchar) is null or (c.name ilike concat('%', cast(:name as varchar), '%'))) " +
                    " and (cast(:code as varchar) is null or (c.code ilike concat('%', cast(:code as varchar), '%'))) " +
                    "   and ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    "   and ( ( cast(:fromDate as varchar) is null ) or ( date_trunc('day', c.created_at) >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    "   and ( ( cast(:toDate as varchar) is null ) or ( date_trunc('day', c.created_at) <= cast( ( cast(:toDate as varchar) ) as date ) ) )" +
                    " order by c.created_at desc",
            nativeQuery = true
    )
    Page<Contract> getShares(
            @Param("email") String email,
            @Param("typeId") Integer typeId,
            @Param("name") String name,
            @Param("code") String code,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            Pageable pageable
    );

    /**
     * Láº¥y toÃ n bá»™ thÃ´ng tin há»£p Ä‘á»“ng cá»§a tÃ´i
     *
     * @param customerId MÃ£ sá»‘ tham chiáº¿u tá»›i khÃ¡ch hÃ ng
     * @param fromDate   Thá»�i gian táº¡o há»£p Ä‘á»“ng (tá»« ngÃ y)
     * @param toDate     Thá»�i gian táº¡o há»£p Ä‘á»“ng (tá»›i ngÃ y)
     * @return Danh sÃ¡ch há»£p Ä‘á»“ng Ä‘Æ°á»£c táº¡o bá»Ÿi khÃ¡ch hÃ ng
     */
    @Query(
            value = "select c.* from contracts c" +
                    " where (c.customer_id = :customerId)" +
            		" and (c.organization_id = :organizationId)" +
                    "   and ( ( cast(:fromDate as varchar) is null ) or (c.created_at >= cast( ( cast(:fromDate as varchar) ) as timestamp ) ) )" +
                    "   and ( ( cast(:toDate as varchar) is null ) or (c.created_at <= cast( ( cast(:toDate as varchar) ) as timestamp ) ) )",
            nativeQuery = true
    )
    Collection<Contract> searchMyContract(
    		@Param("organizationId") int organizationId,
            @Param("customerId") int customerId,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate
    );

    @Query(
            value = "select distinct c.* from contracts c" +
                    " inner join participants p on p.contract_id = c.id" +
                    " inner join recipients r on r.participant_id = p.id" +
                    " where ( r.email = :email )" +
                    "  and ( ( cast(:fromDate as varchar) is null ) or ( c.created_at >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    "  and ( ( cast(:toDate as varchar) is null ) or (c.created_at <= cast( ( cast(:toDate as varchar) ) as date ) ) )",
            nativeQuery = true
    )
    Collection<Contract> searchByEmailAddress(
            @Param("email") String email,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate
    );

    @Query(
            value = "select c.* from contracts c" +
                    " where (c.organization_id = :orgId)" +
                    "   and ( ( cast(:fromDate as varchar) is null ) or (c.created_at >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    "   and ( ( cast(:toDate as varchar) is null ) or (c.created_at <= cast( ( cast(:toDate as varchar) ) as date ) ) )",
            nativeQuery = true
    )
    Collection<Contract> searchOrgContract(
            @Param("orgId") int orgId,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate
    );

    @Query(
            value = "select distinct c.* from contracts c" +
                    " inner join participants p on c.id = p.contract_id" +
                    " inner join recipients r on p.id = r.participant_id" +
                    " where c.status = 30" +
                    "   and r.email = :email" +
                    " order by c.created_at desc",
            nativeQuery = true
    )
    Page<Contract> getMyProcessContractProcessed(
            @Param("email") String email,
            Pageable pageable
    );

    @Query(
            value = "select distinct c.* from contracts c" +
                    " inner join participants p on c.id = p.contract_id" +
                    " inner join recipients r on p.id = r.participant_id" +
                    " where c.status = 20" +
                    "   and r.email = :email" +
                    "   and r.status = 1" +
                    " order by c.created_at desc",
            nativeQuery = true
    )
    Page<Contract> getMyProcessContractProcessing(
            @Param("email") String email,
            Pageable pageable
    );

    @Query(
            value = "select distinct c.* from contracts c" +
                    " inner join participants p on c.id = p.contract_id" +
                    " inner join recipients r on p.id = r.participant_id" +
                    " where c.status = 20" +
                    "   and r.email = :email" +
                    "   and r.status in (2, 3)" +
                    " order by c.created_at desc",
            nativeQuery = true
    )
    Page<Contract> getMyProcessContractWating(
            @Param("email") String email,
            Pageable pageable
    );

    @Query(
            value = "select distinct c.* from contracts c" +
                    " inner join participants p on c.id = p.contract_id" +
                    " inner join recipients r on p.id = r.participant_id" +
                    " where (c.status = 20)" +
                    "   and (c.sign_time is not null and c.sign_time >= :currentDate and c.sign_time <= :expiresDate)" +
                    "   and (r.email = :email)" +
                    "   and r.status = 1" +
                    " order by c.created_at desc",
            nativeQuery = true
    )
    Page<Contract> getMyProcessContractExpires(
            @Param("email") String email,
            @Param("currentDate") Date currentDate,
            @Param("expiresDate") Date expiresDate,
            Pageable pageable
    );


    Collection<Contract> findContractsByTypeId(int typeId);

    Optional<Contract> findByName(String name);
    
    Collection<Contract> findByOrganizationIdAndStatusNot(int orgId, ContractStatus status);

    Optional<Contract> findFirstByContractNoAndOrganizationIdAndStatusIn(String code, int orgId, List<ContractStatus> statusList);

    List<Contract> findByContractNoAndOrganizationIdAndStatusIn(String code, int orgId, List<ContractStatus> statusList);
    
    Collection<Contract> findByTemplateContractId(int templateContractId);
    
    /**
     * Số lượng hợp đồng email đang xử lý + email tạo ở trạng thái xử lý
     * @param email
     * @param customerId
     * @return
     */
    @Query(
            value = " select count(1) from " +
            		" (select distinct c.* from contracts c " + 
            		" inner join participants p on c.id = p.contract_id " + 
            		" inner join recipients r on p.id = r.participant_id " + 
            		" where c.status = 20 " + 
            		" and cast(c.sign_time as date) >= CURRENT_DATE "+
            		" and r.email = :email " + 
            		" and r.status in (0,1) " + 
            		" union " + 
            		" select c.* from contracts c " + 
            		" where c.status = 20 " + 
            		" and cast(c.sign_time as date) >= CURRENT_DATE "+
            		" and c.customer_id = :customerId) a ",
            nativeQuery = true
    )
    Integer getContractProcessByUser(
    		@Param("customerId") int customerId,
            @Param("email") String email
    );

    @Query(
            value = "SELECT count(*) FROM contracts c" +
                    " WHERE  c.organization_id in :orgIds" +
                    " and ((c.status = 30) or " +
                    " (c.status = 20 and CURRENT_DATE <= cast(c.sign_time as date)))",
            nativeQuery = true
    )
    int countAllContracts(List<Integer> orgIds);
    
    /**
     * Tìm kiếm hợp đồng còn hiệu lực 5, 10, 15 ngày
     */
    @Query(
            value = "select c.*"
                    + " from contracts c"
                    + " where status <> 0 and (cast(c.contract_expire_time as date) - 5 = CURRENT_DATE"
                    + "  or cast(c.contract_expire_time as date) - 10 = CURRENT_DATE"
                    + "  or cast(c.contract_expire_time as date) - 15 = CURRENT_DATE)",
            nativeQuery = true
    )
    Collection<Contract> findAllByContractExpireTime();

    @Query(value = "select distinct c.* from contracts c" +
            "    join participants p on c.id = p.contract_id" +
            "    join recipients r on p.id = r.participant_id" +
            " where (cast(:email as varchar) is null or r.email = cast(:email as varchar))" +
            " and (cast(:phone as varchar) is null or r.phone = cast(:phone as varchar)" +
            " and (:status = -1 or c.status = :status))",
            nativeQuery = true)
    Page<Contract> getContracts(String email, String phone, int status, Pageable page);

    /**
     * Số lượng hợp đồng mà người dùng được phép view
     * @param customerId
     * @param email
     * @param contractId
     * @return
     */
    @Query(
            value = " select count(1) from " +
                    " (select c.id from contracts c " +
                    " where created_by = :customerId " +
                    " and id = :contractId " +
                    " union " +
                    " select c.id from contracts c " +
                    " join participants p on c.id = p.contract_id " +
                    " join recipients r on p.id = r.participant_id " +
                    " where r.email = :email " +
                    " and c.id = :contractId " +
                    " union " +
                    " select c.id from contracts c " +
                    " join shares s on c.id = s.contract_id " +
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
