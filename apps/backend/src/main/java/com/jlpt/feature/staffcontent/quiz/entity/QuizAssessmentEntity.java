/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * UC-26 — Self-contained JPA mapping for the `assessments` table (quiz scope).
 *
 * <p>ADD-ONLY MODE: this is a NEW entity with a distinct class name so it does not collide with the
 * pre-existing {@code com.jlpt.feature.assessment.Assessment}. Both may map the same table — Hibernate
 * only requires distinct entity names. Status / type / level are stored as lowercase strings to match
 * the DB CHECK constraints, mirroring {@code StaffContentQuestionEntity}.
 */
@Entity
@Table(name = "assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id")
    private Long id;

    @Column(name = "assessment_type", nullable = false, length = 20)
    @Builder.Default
    private String assessmentType = "quiz";

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(length = 100)
    private String topic;

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

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
