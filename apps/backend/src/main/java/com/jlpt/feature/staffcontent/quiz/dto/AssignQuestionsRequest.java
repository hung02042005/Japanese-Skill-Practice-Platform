/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * UC-26 — Assign questions to a quiz (FR-26-19..23). Replace semantics: the supplied list is the full
 * desired set of assignments for the quiz.
 */
@Data
public class AssignQuestionsRequest {

    @NotEmpty(message = "Danh sách câu hỏi gán không được rỗng")
    @Valid
    private List<AssignmentItem> assignments;

    @Data
    public static class AssignmentItem {

        @NotNull(message = "Thiếu questionId") private Long questionId;

        @NotNull(message = "Thiếu displayOrder") @Min(value = 0, message = "displayOrder phải >= 0")
        private Integer displayOrder;

        @NotNull(message = "Thiếu score") @Positive(message = "score phải > 0") private BigDecimal score;

        @Size(max = 100, message = "sectionName không vượt quá 100 ký tự")
        private String sectionName;
    }
}
