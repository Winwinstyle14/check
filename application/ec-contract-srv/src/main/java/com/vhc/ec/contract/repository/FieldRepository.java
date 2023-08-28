package com.vhc.ec.contract.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.vhc.ec.contract.definition.FieldType;
import com.vhc.ec.contract.entity.Field;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface FieldRepository extends CrudRepository<Field, Integer> {

    Collection<Field> findByContractIdOrderByTypeAsc(int contractId);

    Collection<Field> findAllByRecipientId(int recipientId);

    long deleteByContractId(int contractId);
    
    Optional<Field> findByRecipientId(int recipientId);
    
    Optional<Field> findFirstByRecipientIdAndType(int recipientId, FieldType type);

}
