package com.vhc.ec.contract.repository;

import com.vhc.ec.contract.definition.RecipientStatus;
import com.vhc.ec.contract.entity.Recipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface RecipientRepository extends CrudRepository<Recipient, Integer> {

    Collection<Recipient> findAllByParticipantId(int participantId);

    /**
     * Tìm kiếm người dùng tham gia xử lý hợp đồng theo tên đăng nhập và mật khẩu
     *
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return {@link Recipient}
     */
    Optional<Recipient> findFirstByUsernameAndPassword(String username, String password);

    /**
     * Tìm kiếm người xử lý hợp đồng theo địa chỉ email và mã hợp đồng
     *
     * @param email       Địa chỉ email của người xử lý
     * @param recipientId Mã số tham chiếu tới người xử lý hồ sơ
     * @return Thông tin chi tiết về người xử lý hợp đồng
     */
    @Query("select r from recipients r where r.email = :email and r.id = :recipientId")
    Optional<Recipient> findFirstByEmailAndRecipientIdOrderByOrderingAsc(
            @Param("email") String email,
            @Param("recipientId") int recipientId
    );

    /**
     * Truy vấn dữ liệu hợp đồng người dùng tham gia xử lý dữ liệu.
     *
     * @param email    Địa chỉ e-mail của khách hàng
     * @param typeId   Mã loại hợp đồng
     * @param fromDate Ngày tạo (từ ngày)
     * @param toDate   Ngày tạo (tới ngày)
     * @param status   Trạng thái hợp đồng
     * @param pageable Phân trang dữ liệu
     * spring them order by vao count query nen phai them vao
     * @return Phân trang dữ liệu hợp đồng của tôi
     */
    @Query(
            value = "select distinct r.* from contracts c " +
                    "join participants p on c.id = p.contract_id " +
                    "join recipients r on p.id = r.participant_id " +
                    "left join (" +
                    "    select p.contract_id, p.id, p.name from contracts c, participants p " +
                    "        where c.id = p.contract_id " +
                    ") partner " +
                    "on c.id = partner.contract_id and r.participant_id <>  partner.id" +
                    " where  ((cast(:keyword as varchar) is null or (c.name ilike concat('%', cast(:keyword as varchar), '%'))) " +
                    "   or (( cast(:keyword as varchar) is null  is null or (partner.name ilike concat('%', cast(:keyword as varchar), '%')) ))) " +
                    " and ( r.email = :email )" +
                    " and ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    " and ( ( cast(:fromDate as varchar) is null ) or ( date_trunc('day', c.created_at) >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    " and ( ( cast(:toDate as varchar) is null ) or ( date_trunc('day', c.created_at) <= cast( ( cast(:toDate as varchar) ) as date ) ) )" +
                    " and ( ( :status = -1 ) or ( (:status != -1 and :status != 4) and r.status = :status ) or (:status = 4 and (r.status = 2 or r.status = 3) ) ) " +
                    " and (:contractStatus = -1 or (c.status = :contractStatus))" +
                    " and ( cast(:contractNo as varchar) is null or (c.contract_no ilike concat('%', cast(:contractNo as varchar), '%')) )" +
                    "order by r.created_at desc",
            countQuery = "select count(distinct r.*) from contracts c " +
                    "join participants p on c.id = p.contract_id " +
                    "join recipients r on p.id = r.participant_id " +
                    "left join (" +
                    "    select p.contract_id, p.id, p.name from contracts c, participants p " +
                    "        where c.id = p.contract_id " +
                    ") partner " +
                    "on c.id = partner.contract_id and r.participant_id <>  partner.id" +
                    " where  ((cast(:keyword as varchar) is null or (c.name ilike concat('%', cast(:keyword as varchar), '%'))) " +
                    "   or (( cast(:keyword as varchar) is null  is null or (partner.name ilike concat('%', cast(:keyword as varchar), '%')) ))) " +
                    " and ( r.email = :email )" +
                    " and ( ( :typeId is null ) or ( c.type_id = cast ( ( cast(:typeId as varchar) ) as int4) ) )" +
                    " and ( ( cast(:fromDate as varchar) is null ) or ( date_trunc('day', c.created_at) >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    " and ( ( cast(:toDate as varchar) is null ) or ( date_trunc('day', c.created_at) <= cast( ( cast(:toDate as varchar) ) as date ) ) )" +
                    " and ( ( :status = -1 ) or ( (:status != -1 and :status != 4) and r.status = :status ) or (:status = 4 and (r.status = 2 or r.status = 3) ) ) " +
                    " and (:contractStatus = -1 or (c.status = :contractStatus))" +
                     " and ( cast(:contractNo as varchar) is null or (c.contract_no ilike concat('%', cast(:contractNo as varchar), '%')) )",
            nativeQuery = true
    )
    Page<Recipient> searchByEmailAddress(
            @Param("email") String email,
            @Param("typeId") Integer typeId,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("status") Integer status,
            Integer contractStatus,
            String contractNo,
            String keyword,
            Pageable pageable
    );

    /**
     * Tìm kiếm người xử lý nằm trong hợp đồng sắp hết hạn
     */
    @Query(
            value = "select r.*"
                    + " from contracts c,"
                    + " participants p,"
                    + " recipients r"
                    + " where (c.id = p.contract_id)"
                    + "  and (p.id = r.participant_id)"
                    + "  and CURRENT_DATE = cast(c.sign_time as date) - 5"
                    + "  and c.status = 20 and r.status = 1",
            nativeQuery = true
    )
    List<Recipient> findAllByContractAboutExpire();

    /**
     * Tìm kiếm người xử lý nằm trong hợp đồng quá hạn
     */
    @Query(
            value = "select r.*"
                    + " from contracts c,"
                    + " participants p,"
                    + " recipients r"
                    + " where (c.id = p.contract_id)"
                    + "  and (p.id = r.participant_id)"
                    + "  and cast(c.sign_time as date) >= CURRENT_DATE - 1"
                    + "  and cast(c.sign_time as date) < CURRENT_DATE"
                    + "  and c.status = 20",
            nativeQuery = true
    )
    List<Recipient> findAllByContractExpire();

    @Query(value = "select c.organization_id from recipients r" +
            "    join participants p on r.participant_id = p.id " +
            "    join contracts c on p.contract_id = c.id " +
            "where r.id = :recipientId", nativeQuery = true)
    Optional<Integer> findOrgId(int recipientId);

    List<Recipient> findAllByEmailAndStatus(String email, RecipientStatus status);

    /**
     * Lay ra danh sach hop dong co the ky nhieu (chua loc duoc loai ky)
     *
     */
    @Query(
            value = "select r.* from contracts c, participants p, recipients r" +
                    " where ( p.id = r.participant_id )" +
                    " and c.is_template " +
                    " and ( c.id = p.contract_id ) " +
                    " and ( r.email = :email )" +
                    " and ( r.status = 1 ) " +
                    " and (c.status <> 32)" +
                    " and CURRENT_DATE <= cast(c.sign_time as date)" +
                    " order by r.created_at desc",
            nativeQuery = true
    )
    List<Recipient> findAllContractCanMultiSign(String email);

    Optional<Recipient> findFirstByEmailOrderByIdDesc(String email);
}
