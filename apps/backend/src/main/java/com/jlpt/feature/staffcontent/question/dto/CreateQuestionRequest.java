/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-24 — Create question request DTO.
 */
@Data
public class CreateQuestionRequest {

    @NotBlank(message = "Thiếu trường bắt buộc: questionText")
    private String questionText;

    @NotBlank(message = "questionType không hợp lệ")
    @Pattern(regexp = "^(multiple_choice|fill_blank|true_false)$", message = "questionType không hợp lệ")
    private String questionType;

    @NotBlank(message = "skill không hợp lệ")
    @Pattern(regexp = "^(vocabulary|grammar|kanji|reading|listening|mixed)$", message = "skill không hợp lệ")
    private String skill;

    @NotBlank(message = "Cấp độ JLPT phải là N5 đến N1")
    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT phải là N5 đến N1")
    private String jlptLevel;

    private String explanation;
    private String audioUrl;
    private String imageUrl;

    // multiple_choice fields
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @Pattern(regexp = "^(A|B|C|D)$", message = "correctOption chỉ được A, B, C hoặc D")
    private String correctOption;

    // fill_blank / true_false fields
    private String correctAnswerText;
}
