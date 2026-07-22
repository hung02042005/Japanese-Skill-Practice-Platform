/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpeakingLessonDetailResponse {
    private final Long lessonId;
    private final String title;
    private final String jlptLevel;
    private final String status;
    private final LocalDateTime createdAt;
    private final List<SpeakingQuestionDto> questions;
}
