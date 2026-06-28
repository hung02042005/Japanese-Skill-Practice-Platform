/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Gỡ hàng loạt thẻ khỏi sổ tay (3B) — danh sách flashcardId cần soft-delete. */
public record BulkDeleteRequest(@NotEmpty List<Long> ids) {}
