/* (c) JLPT E-Learning Platform */
package com.jlpt.bookmark.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookmarkRequest(
        @NotNull @Pattern(regexp = "VOCABULARY|KANJI|GRAMMAR") String contentType,
        @NotNull Long contentId,
        @Size(max = 500) String note) {}
