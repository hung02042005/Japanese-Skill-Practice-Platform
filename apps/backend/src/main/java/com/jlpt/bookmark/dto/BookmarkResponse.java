/* (c) JLPT E-Learning Platform */
package com.jlpt.bookmark.dto;

import java.time.LocalDateTime;

public record BookmarkResponse(
        Long progressId,
        String contentType,
        Long contentId,
        String displayText,
        String note,
        LocalDateTime bookmarkedAt,
        String jlptLevel) {}
