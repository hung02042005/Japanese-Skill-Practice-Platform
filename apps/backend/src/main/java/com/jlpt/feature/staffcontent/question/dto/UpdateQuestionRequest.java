/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-24 — Update question request DTO.
 * Uses String fields so partial updates can be distinguished.
 */
@Data
public class UpdateQuestionRequest {

    private String questionText;

    @Pattern(regexp = "^(multiple_choice|fill_blank|true_false)$", message = "questionType không hợp lệ")
    private String questionType;

    @Pattern(regexp = "^(vocabulary|grammar|kanji|reading|listening|mixed)$", message = "skill không hợp lệ")
    private String skill;

    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT phải là N5 đến N1")
    private String jlptLevel;

    private String explanation;
    private String audioUrl;
    private String imageUrl;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @Pattern(regexp = "^(A|B|C|D)$", message = "correctOption chỉ được A, B, C hoặc D")
    private String correctOption;

    private String correctAnswerText;
}
