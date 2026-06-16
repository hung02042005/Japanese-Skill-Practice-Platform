/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.flashcard.dto.request;

import lombok.Data;

/** UC-07 — POST /api/flashcards body. Validate thủ công ở Service (LearningException). */
@Data
public class AddFlashcardRequest {

    private String contentType;
    private Long contentId;

    /** Tùy chọn, mặc định "Mặc định" (UC-07 §5). */
    private String deckName;
}
