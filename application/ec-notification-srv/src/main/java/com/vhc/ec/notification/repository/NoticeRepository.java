package com.vhc.ec.notification.repository;

import com.vhc.ec.notification.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface NoticeRepository extends CrudRepository<Notice, Integer> {

    @Query(
            value = "SELECT c.* FROM notice c" +
                    " WHERE ( c.email = :email )" +
                    " AND ( ( cast(:fromDate as varchar) is null ) or ( c.created_at >= cast( ( cast(:fromDate as varchar) ) as date ) ) )" +
                    " AND ( ( cast(:toDate as varchar) is null ) or (c.created_at <= cast( ( cast(:toDate as varchar) ) as date ) ) )" +
                    " AND ( " +
                    "   ( :status is null ) or " +
                    "   ( ( 0 = cast ( cast(:status as varchar ) as int4) ) and c.status = 0 ) or " +
                    "   ( ( 1 = cast ( cast(:status as varchar ) as int4) ) and c.status = 1 ) " +
                    ") " +
                    "order by c.created_at desc",
            nativeQuery = true
    )
    Page<Notice> findMyNotice(
            @Param("email") String email,
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("status") Integer status,
            Pageable pageable
    );
}