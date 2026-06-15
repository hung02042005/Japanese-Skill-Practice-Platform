/* (c) JLPT E-Learning Platform */
package com.jlpt.dto.response;

/** Một bậc cấp độ JLPT trong điều hướng học từ vựng (SPEC feat-flashcard-srs §3.7). */
public record VocabularyLevelResponse(String level, int topicCount, long wordCount) {}
