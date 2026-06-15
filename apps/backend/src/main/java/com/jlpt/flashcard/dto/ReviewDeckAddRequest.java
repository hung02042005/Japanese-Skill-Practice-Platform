/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/** Xác nhận thêm các từ sai vào sổ "Từ cần ôn lại" (§3.5, FR-FC-43). */
public record ReviewDeckAddRequest(@NotEmpty @Valid List<Item> items) {

    public record Item(
            @NotNull @Pattern(regexp = "VOCABULARY|vocabulary") String contentType, @NotNull Long contentId) {}
}
