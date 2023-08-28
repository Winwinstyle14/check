package com.vhc.ec.customer.repository;

import com.vhc.ec.customer.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Collection<Permission> findByRoleId(int roleId);

    void deleteAllByRoleId(int roleId);

}
