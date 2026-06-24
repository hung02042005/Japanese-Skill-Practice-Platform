/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "attempt_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_option", columnDefinition = "CHAR(1)")
    private String selectedOption;

    @Column(name = "answer_text", columnDefinition = "NVARCHAR(MAX)")
    private String answerText;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(precision = 6, scale = 2)
    private BigDecimal score;

    @Column(name = "answered_at", nullable = false)
    @Builder.Default
    private LocalDateTime answeredAt = LocalDateTime.now();
}
