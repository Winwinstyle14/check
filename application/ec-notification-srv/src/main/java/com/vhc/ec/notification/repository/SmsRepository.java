package com.vhc.ec.notification.repository;

import com.vhc.ec.notification.entity.Sms;
import org.springframework.data.repository.CrudRepository;

public interface SmsRepository extends CrudRepository<Sms, Long> {
}
