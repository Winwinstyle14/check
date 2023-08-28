package com.vhc.ec.contract.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.vhc.ec.contract.definition.DocumentType;
import com.vhc.ec.contract.entity.Document;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface DocumentRepository extends CrudRepository<Document, Integer> {

    @Query(
            value = "select d.* from documents d" +
                    " where (d.contract_id = :contractId)" +
                    "   and (d.status = :status) " +
                    "order by d.type desc",
            nativeQuery = true
    )
    Collection<Document> findAllByContractIdAndStatusOrderByTypeDesc(
            @Param("contractId") int contractId, @Param("status") int status
    );

    Collection<Document> findAllByContractId(int contractId);

    @Query(
            value = "select d.* from documents d" +
                    " where (d.contract_id = :contractId)" +
                    "   and (d.type = :type) " +
                    "order by d.created_at desc",
            nativeQuery = true
    )
    Optional<Document> findFirstByContractIdAndTypeOrderByCreatedAtDesc(
            @Param("contractId") int contractId,
            @Param("type") int type
    );
    
    int deleteByContractIdAndType(int contractId, DocumentType type);
}
