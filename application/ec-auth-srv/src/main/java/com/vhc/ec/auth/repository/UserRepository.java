package com.vhc.ec.auth.repository;

import com.vhc.ec.auth.entity.User;
import org.springframework.data.repository.CrudRepository;

/**
 * @author: VHC JSC
 * @version: 1.0
 * @since: 1.0
 */
public interface UserRepository extends CrudRepository<User, Integer> {
}
