package com.vhc.ec.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhc.ec.notification.entity.SmsLog;

public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
}
