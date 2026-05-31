/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.AdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
