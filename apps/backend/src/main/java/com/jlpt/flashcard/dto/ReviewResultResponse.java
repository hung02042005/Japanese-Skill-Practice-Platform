/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Kết quả đánh giá thẻ. Với thẻ từ vựng (trắc nghiệm) các trường {@code correct},
 * {@code correctOptionId}, {@code correctMeaning} được điền; với thẻ lật (kanji/grammar/custom)
 * chúng là {@code null}. Cuối phiên ({@code isLastCardInSession}) trả gợi ý "Từ cần ôn lại" (§3.5).
 */
public record ReviewResultResponse(
        Long flashcardId,
        Boolean correct,
        Long correctOptionId,
        String correctMeaning,
        String rating,
        Integer newIntervalDays,
        LocalDate nextReviewDate,
        Integer repetitionCount,
        boolean suggestAddToReviewDeck,
        List<WrongWord> wrongWords) {

    public record WrongWord(String contentType, Long contentId, String frontText) {}
}
