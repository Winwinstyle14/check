package com.vhc.ec.admin.integration.qldv.repository;

import com.vhc.ec.admin.integration.qldv.entity.QldvCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QldvCustomerRepository extends JpaRepository<QldvCustomer, Long> {
}
