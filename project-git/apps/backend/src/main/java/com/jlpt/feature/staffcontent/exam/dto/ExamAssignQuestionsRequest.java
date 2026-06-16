/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.exam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * UC-28 — Assign questions to an exam (FR-28-20..22). Replace semantics: the supplied list
 * is the full desired set of assignments. sectionName is REQUIRED for exam items.
 */
@Data
public class ExamAssignQuestionsRequest {

    @NotEmpty(message = "Danh sách câu hỏi gán không được rỗng")
    @Valid
    private List<ExamAssignmentItem> assignments;

    @Data
    public static class ExamAssignmentItem {

        @NotNull(message = "Thiếu questionId") private Long questionId;

        @NotNull(message = "Thiếu sectionName — section bắt buộc cho đề thi") @NotBlank(message = "sectionName không được để trống")
        @Size(max = 100, message = "sectionName không vượt quá 100 ký tự")
        private String sectionName;

        @NotNull(message = "Thiếu displayOrder") @Min(value = 0, message = "displayOrder phải >= 0")
        private Integer displayOrder;

        @NotNull(message = "Thiếu score") @Positive(message = "score phải > 0") private BigDecimal score;
    }
}
