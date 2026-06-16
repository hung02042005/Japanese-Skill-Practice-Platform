/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * UC-24 — Maps to the {@code questions} table for Staff question-bank operations.
 * Separate from assessment.Question to avoid modifying existing code (ADD-ONLY MODE).
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffContentQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "question_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 30)
    private String questionType;

    @Column(nullable = false, length = 30)
    private String skill;

    @Column(name = "jlpt_level", nullable = false, length = 5)
    private String jlptLevel;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String explanation;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "option_a", columnDefinition = "NVARCHAR(MAX)")
    private String optionA;

    @Column(name = "option_b", columnDefinition = "NVARCHAR(MAX)")
    private String optionB;

    @Column(name = "option_c", columnDefinition = "NVARCHAR(MAX)")
    private String optionC;

    @Column(name = "option_d", columnDefinition = "NVARCHAR(MAX)")
    private String optionD;

    @Column(name = "correct_option", columnDefinition = "CHAR(1)")
    private String correctOption;

    @Column(name = "correct_answer_text", columnDefinition = "NVARCHAR(MAX)")
    private String correctAnswerText;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "draft";

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
