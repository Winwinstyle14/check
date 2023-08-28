package com.vhc.ec.notification.repository;

import com.vhc.ec.notification.entity.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MessageRepository extends CrudRepository<Message, Integer> {

    Optional<Message> findTopByCode(String code);

}