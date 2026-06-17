/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UC-24 — Question response DTO. Never exposes the JPA entity (ADR-005).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {

    private Long questionId;
    private String questionText;
    private String questionType;
    private String skill;
    private String jlptLevel;
    private String explanation;
    private String audioUrl;
    private String imageUrl;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private String correctAnswerText;
    private String status;
    private Boolean isLocked;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
