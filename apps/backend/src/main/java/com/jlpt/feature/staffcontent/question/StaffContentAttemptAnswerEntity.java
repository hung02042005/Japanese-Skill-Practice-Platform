/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question;

import jakarta.persistence.*;
import lombok.*;

/**
 * UC-24 — Minimal mapping to {@code attempt_answers} table.
 * Used only to detect whether a question is locked (has been answered by a student).
 */
@Entity
@Table(name = "attempt_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffContentAttemptAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;
}
