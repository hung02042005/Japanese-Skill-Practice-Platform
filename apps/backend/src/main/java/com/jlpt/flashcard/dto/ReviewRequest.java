/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.dto;

import jakarta.validation.constraints.Pattern;

/**
 * Đánh giá một thẻ. Hai chế độ (§3.6 SUPERSEDES §3.2 cho từ vựng):
 *
 * <ul>
 *   <li><b>Từ vựng (trắc nghiệm):</b> client gửi {@code selectedOptionId}; server tự suy ra
 *       đúng/sai và rating — KHÔNG nhận {@code rating} từ client (chống client-trusted data).
 *   <li><b>Kanji/Grammar/Custom (lật thẻ):</b> client gửi {@code rating} (EASY/HARD/WRONG).
 * </ul>
 */
public record ReviewRequest(
        @Pattern(regexp = "EASY|HARD|WRONG") String rating, Long selectedOptionId, boolean isLastCardInSession) {}
