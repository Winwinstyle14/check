package com.vhc.ec.customer.repository;

import com.vhc.ec.customer.entity.PasswordReset;
import org.springframework.data.repository.CrudRepository;


public interface PasswordResetRepository extends CrudRepository<PasswordReset, String> {

    PasswordReset findByEmail(String email);

    PasswordReset findByToken(String token);
}