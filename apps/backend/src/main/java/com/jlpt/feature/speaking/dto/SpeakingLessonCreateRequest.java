/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeakingLessonCreateRequest {

    @NotBlank(message = "Cấp độ JLPT không được để trống")
    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT không hợp lệ")
    private String jlptLevel;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotEmpty(message = "Bài nói phải có ít nhất một câu hỏi")
    @Valid
    private List<SpeakingQuestionDto> questions;
}
