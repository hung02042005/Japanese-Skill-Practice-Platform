/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingQuestionDto {

    private Long speakingQuestionId;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String promptText;

    private String instruction;
    private String sampleAudioUrl;
    private Integer displayOrder;
}
