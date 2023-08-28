package com.vhc.ec.admin.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.vhc.ec.admin.entity.Customer;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface CustomerRepository extends CrudRepository<Customer, Integer> {

    Optional<Customer> findFirstByEmail(String email);

    Optional<Customer> findFirstByPhone(String phone);

    Optional<Customer> findFirstByOrganizationId(int organizationId);
}
