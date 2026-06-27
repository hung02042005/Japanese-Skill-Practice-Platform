/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    Page<TestAttempt> findByStudent_IdAndStatusOrderBySubmittedAtDesc(
            Long studentId, TestAttempt.AttemptStatus status, Pageable pageable);

    Page<TestAttempt> findByStudent_IdAndParentIdAndStatusOrderBySubmittedAtDesc(
            Long studentId, Long parentId, TestAttempt.AttemptStatus status, Pageable pageable);

    List<TestAttempt> findByStudent_IdAndParentIdAndStatus(
            Long studentId, Long parentId, TestAttempt.AttemptStatus status);

    Page<TestAttempt> findByStudent_IdAndAttemptTypeAndStatusInOrderBySubmittedAtDesc(
            Long studentId,
            TestAttempt.AttemptType attemptType,
            List<TestAttempt.AttemptStatus> statuses,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TestAttempt t WHERE t.id = :id")
    Optional<TestAttempt> findByIdForUpdate(@Param("id") Long id);

    // Staff view: toàn bộ lần thi đã nộp của 1 học viên (tính điểm TB + lấy gần đây).
    List<TestAttempt> findByStudent_IdAndStatusIn(Long studentId, List<TestAttempt.AttemptStatus> statuses);

    // Dashboard (admin): số lần làm bài bắt đầu từ mốc thời gian (hôm nay).
    @Query("SELECT COUNT(t) FROM TestAttempt t WHERE t.startedAt >= :after")
    long countByStartedAtAfter(@Param("after") java.time.LocalDateTime after);
}
