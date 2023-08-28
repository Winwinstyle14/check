package com.vhc.ec.contract.repository;

import com.vhc.ec.contract.entity.TemplateDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface TemplateDocumentRepository extends JpaRepository<TemplateDocument, Integer> {
    @Query(
            value = "select d.* from template_documents d" +
                    " where (d.contract_id = :contractId)" +
                    "   and (d.status = :status) " +
                    "order by d.type desc",
            nativeQuery = true
    )
    Collection<TemplateDocument> findAllByContractIdAndStatusOrderByTypeDesc(
            @Param("contractId") int contractId, @Param("status") int status
    );

}
