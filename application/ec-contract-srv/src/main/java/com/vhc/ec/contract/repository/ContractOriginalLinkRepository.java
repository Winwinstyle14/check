package com.vhc.ec.contract.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
 
import com.vhc.ec.contract.entity.ContractOriginalLink;

public interface ContractOriginalLinkRepository  extends CrudRepository<ContractOriginalLink, Integer>{
	Optional<ContractOriginalLink> findAllByCode(String code);
}
