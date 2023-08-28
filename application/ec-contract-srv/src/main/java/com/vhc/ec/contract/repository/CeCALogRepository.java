package com.vhc.ec.contract.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.vhc.ec.contract.entity.CeCALog;

public interface CeCALogRepository extends CrudRepository<CeCALog, Integer>{
	Optional<CeCALog> findByMessageId(String messageId);

}
