/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * UC-28 — Read-only projection of the questions table used to validate that a referenced
 * question exists, is published, and has matching {@code jlpt_level} (FR-28-25).
 *
 * <p>ADD-ONLY MODE: distinct class name; the exam feature never writes to questions.
 */
@Entity
@Table(name = "questions")
@Getter
public class ExamQuestionRefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "question_text", columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    @Column(length = 20)
    private String status;

    @Column(name = "jlpt_level", length = 5)
    private String jlptLevel;
}
