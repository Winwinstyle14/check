package com.vhc.ec.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.vhc.ec.admin.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailAndIdNot(String email, long id);

    Optional<User> findByPhoneAndIdNot(String phone, long id);

    @Query("select u from users u where " +
            "(cast(:name as string) is null or lower(u.name) like '%' || lower(cast(:name as string)) || '%') and" +
            "(cast(:email as string) is null or lower(u.email) like '%' || lower(cast(:email as string)) || '%') and" +
            "(:phone is null or u.phone like %:phone%)")
    Page<User> search(String name, String email, String phone, Pageable page);
    
    List<User> findAll();
}
