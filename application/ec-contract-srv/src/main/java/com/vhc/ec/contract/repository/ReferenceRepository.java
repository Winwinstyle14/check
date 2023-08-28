package com.vhc.ec.contract.repository;

import com.vhc.ec.contract.entity.Reference;
import com.vhc.ec.contract.entity.ReferenceId;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface ReferenceRepository extends CrudRepository<Reference, ReferenceId> {

    void deleteAllByContractId(int contractId);

    Collection<Reference> findAllByContractId(int contractId);

}
