/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionRequest {
    @NotBlank(message = "Nội dung câu hỏi là bắt buộc")
    private String questionText;

    /** "multiple_choice" | "fill_blank" | "true_false" */
    @NotBlank(message = "Loại câu hỏi là bắt buộc")
    private String questionType;

    /** "vocabulary" | "grammar" | "kanji" | "reading" | "listening" | "mixed" */
    @NotBlank(message = "Kỹ năng là bắt buộc")
    private String skill;

    /** "N5" | "N4" | "N3" | "N2" | "N1" */
    @NotBlank(message = "Cấp độ JLPT là bắt buộc")
    private String jlptLevel;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    /** Dùng cho MULTIPLE_CHOICE và TRUE_FALSE */
    private String correctOption;

    /** Dùng cho FILL_BLANK */
    private String correctAnswerText;

    private String explanation;
    private String audioUrl;
    private String imageUrl;

    @NotNull(message = "Điểm là bắt buộc") private java.math.BigDecimal score;

    private String section;
    private Integer displayOrder;
}
