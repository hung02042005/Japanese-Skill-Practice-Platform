/* (c) JLPT E-Learning Platform */
package com.jlpt.repository;

import com.jlpt.entity.StudentUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentUserRepository extends JpaRepository<StudentUser, Long> {
    Optional<StudentUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
