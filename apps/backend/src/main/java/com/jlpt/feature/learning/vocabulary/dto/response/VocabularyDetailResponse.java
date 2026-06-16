/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.vocabulary.dto.response;

import lombok.Builder;
import lombok.Data;

/** UC-09 — Detail DTO for GET /api/vocabulary/{vocabularyId}. Không expose Entity (ADR-005). */
@Data
@Builder
public class VocabularyDetailResponse {

    private Long vocabularyId;
    private String word;
    private String furigana;
    private String meaning;
    private String wordType;
    private String jlptLevel;
    private String topic;
    private String audioUrl;

    /** BR-09-08 — luôn trả cùng nhau (FR-LEARN-31). */
    private String exampleSentenceJp;

    private String exampleSentenceVi;

    /** null khi Student chưa có tiến độ cho từ vựng này. */
    private String progressStatus;
}
