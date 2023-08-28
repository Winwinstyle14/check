package com.vhc.ec.contract.repository;

import com.vhc.ec.contract.entity.Share;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface ShareRepository extends CrudRepository<Share, Integer> {

    Optional<Share> findFirstByContractIdAndEmail(int contractId, String email);

    Optional<Share> findFirstByEmailAndToken(String email, String token);
}
