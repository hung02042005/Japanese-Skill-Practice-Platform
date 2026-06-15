/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.dto;

import java.util.List;

/**
 * Phiên học trộn (§3.6/§3.7). Hàng đợi gồm tối đa {@code newLimit} thẻ NEW + tất cả thẻ REVIEW
 * đến hạn, trộn ngẫu nhiên. Với {@code stage = REVIEW}, {@code learn} = null và payload KHÔNG
 * chứa nghĩa đúng/contentId (chống lộ đáp án — FR-FC-55).
 */
public record SessionResponse(Long deckId, int newCount, int reviewCount, List<QueueItem> queue) {

    public record QueueItem(Long flashcardId, String stage, Front front, Learn learn, Quiz quiz) {}

    public record Front(String word, String furigana) {}

    public record Learn(String meaning, String exampleJp, String exampleVi, String audioUrl) {}

    public record Quiz(List<Option> options) {}

    public record Option(Long optionId, String meaning) {}
}
