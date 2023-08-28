package com.vhc.ec.notification.repository;

import com.vhc.ec.notification.entity.Email;
import org.springframework.data.repository.CrudRepository;

public interface EmailRepository extends CrudRepository<Email, Long> {
}
