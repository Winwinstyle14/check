package com.vhc.ec.contract.repository;

import com.vhc.ec.contract.entity.Participant;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface ParticipantRepository extends CrudRepository<Participant, Integer> {

    Collection<Participant> findByContractIdOrderByOrderingAsc(int contractId);
    
}
