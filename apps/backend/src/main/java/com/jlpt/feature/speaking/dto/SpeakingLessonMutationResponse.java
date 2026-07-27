/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpeakingLessonMutationResponse {
    private final Long lessonId;
    private final String status;
}
