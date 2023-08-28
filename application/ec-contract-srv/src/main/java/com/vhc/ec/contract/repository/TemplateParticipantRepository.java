package com.vhc.ec.contract.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhc.ec.contract.entity.TemplateParticipant;

public interface TemplateParticipantRepository extends JpaRepository<TemplateParticipant, Integer> {
    Collection<TemplateParticipant> findByContractIdOrderByOrderingAsc(int contractId);
    
    /**
     * Lấy thông tin tổ chức của tôi
     * @param contractId Mã hợp đồng mẫu
     * @return
     */
    @Query(
            value = "select p.* from template_participants p" +
                    " where (p.contract_id = :contractId)" +
                    " and type = 1 " +
                    " limit 1",
            nativeQuery = true
    )
    Optional<TemplateParticipant> findMyParticipantByContractId(
            @Param("contractId") int contractId
    );
    
    /**
     * Lấy thông tin đối tác
     * @param contractId
     * @return
     */
    @Query(
            value = "select p.* from template_participants p" +
                    " where (p.contract_id = :contractId)" +
                    " and type <> 1 " +
                    " order by id asc",
            nativeQuery = true
    )
    Collection<TemplateParticipant> findPartnerByContractIdOrderById(
            @Param("contractId") int contractId
    );
}
