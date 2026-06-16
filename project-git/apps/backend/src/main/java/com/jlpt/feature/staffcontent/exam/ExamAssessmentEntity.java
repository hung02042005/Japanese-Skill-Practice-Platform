/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * UC-28 — Self-contained JPA mapping for the ssessments table (exam scope).
 *
 * <p>ADD-ONLY MODE: distinct class name so it does not collide with {@code
 * com.jlpt.feature.assessment.Assessment} or {@code QuizAssessmentEntity}. Hibernate
 * only requires distinct entity names. Status/type/level are stored as lowercase strings
 * to match the DB CHECK constraints.
 */
@Entity
@Table(name = "assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id")
    private Long id;

    @Column(name = "assessment_type", nullable = false, length = 20)
    @Builder.Default
    private String assessmentType = "exam";

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "jlpt_level", length = 5)
    private String jlptLevel;

    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(name = "pass_score")
    private Integer passScore;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "draft";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** Free-text topic/description for the exam. */
    @Column(length = 500)
    private String description;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
