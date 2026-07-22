/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/** Một bài luyện nói trong danh sách (UC-13 §3.1 / SPEC-speaking §5). */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeakingExerciseResponse {

    private Long exerciseId;
    private String title;
    private String level;
    private String category;
    private String targetText;
    private String sampleAudioUrl;
    private List<SpeakingQuestionDto> questions;
    private Integer bestScore; // điểm cao nhất của student (null nếu chưa luyện)
    private int attemptCount; // số lần đã luyện
}
