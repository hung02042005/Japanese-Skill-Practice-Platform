/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

/**
 * UC-26 — Self-contained JPA mapping for the `question_assignments` table.
 *
 * <p>ADD-ONLY MODE: distinct class name vs the pre-existing
 * {@code com.jlpt.feature.assessment.QuestionAssignment}. Uses a plain {@code Long questionId}
 * column (no @ManyToOne) to stay independent of other feature packages.
 */
@Entity
@Table(name = "question_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long id;

    @Column(name = "parent_type", nullable = false, length = 30)
    @Builder.Default
    private String parentType = "assessment";

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "section_name", length = 100)
    private String sectionName;

    @Column(nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal score = BigDecimal.ONE;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
