/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentSubmissionRepository extends JpaRepository<StudentSubmission, Long> {

    /** Poll kết quả — chỉ trả bài của chính student đang đăng nhập (UC-13, BR ownership). */
    Optional<StudentSubmission> findByIdAndStudent_Id(Long id, Long studentId);

    /**
     * Thống kê speaking theo từng bài (exercise) cho một student: [exerciseId, attemptCount, bestScore].
     * bestScore = điểm cao nhất do GIÁO VIÊN chấm (manual_score); null nếu chưa bài nào được chấm.
     * Dùng để đổ cột "Đã luyện / Điểm tốt nhất" ở danh sách mà không gây N+1.
     */
    @Query(
            """
            SELECT s.exercise.id, COUNT(s), MAX(s.manualScore)
            FROM StudentSubmission s
            WHERE s.student.id = :studentId
              AND s.submissionType = :type
              AND s.exercise.id IN :exerciseIds
            GROUP BY s.exercise.id
            """)
    List<Object[]> findSpeakingStats(
            @Param("studentId") Long studentId,
            @Param("type") StudentSubmission.SubmissionType type,
            @Param("exerciseIds") Collection<Long> exerciseIds);

    @Query(
            """
            SELECT s FROM StudentSubmission s
            WHERE (:type IS NULL OR s.submissionType = :type)
              AND (:status IS NULL OR s.status = :status)
            ORDER BY s.submittedAt DESC
            """)
    Page<StudentSubmission> findAllByTypeAndFilters(
            @Param("type") StudentSubmission.SubmissionType type,
            @Param("status") StudentSubmission.SubmissionStatus status,
            Pageable pageable);

    // ── Dashboard (admin) — đếm theo trạng thái / mốc chấm ────────────────────
    @Query("SELECT COUNT(s) FROM StudentSubmission s WHERE s.status IN :statuses")
    long countByStatusIn(@Param("statuses") java.util.Collection<StudentSubmission.SubmissionStatus> statuses);
}
