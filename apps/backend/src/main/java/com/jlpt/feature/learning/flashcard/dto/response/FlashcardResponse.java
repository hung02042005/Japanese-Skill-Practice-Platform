/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.flashcard.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** UC-07 — Response DTO trả về sau khi tạo flashcards. */
@Data
@Builder
public class FlashcardResponse {

    private Long flashcardId;
    private String contentType;
    private Long contentId;
    private String deckName;
    private LocalDateTime createdAt;
}
