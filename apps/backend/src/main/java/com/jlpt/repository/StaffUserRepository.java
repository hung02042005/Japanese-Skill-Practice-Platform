/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.StaffUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {
    Optional<StaffUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
