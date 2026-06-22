/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * UC-26 — Read-only projection of the `questions` table used to validate that a referenced question
 * exists and is published before it can be assigned to a quiz (FR-26-21), and to expose the question
 * text in the quiz detail view (FR-26-13).
 *
 * <p>ADD-ONLY MODE: distinct class name; the quiz feature never writes to `questions`.
 */
@Entity
@Table(name = "questions")
@Getter
public class QuizQuestionRefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "question_text", columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    @Column(length = 20)
    private String status;
}
