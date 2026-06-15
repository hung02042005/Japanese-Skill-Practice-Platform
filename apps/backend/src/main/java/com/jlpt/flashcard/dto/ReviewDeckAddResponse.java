/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.dto;

/** Kết quả thêm từ vào sổ "Từ cần ôn lại" (§3.5). */
public record ReviewDeckAddResponse(Long deckId, String name, int addedCount, int skippedCount) {}
