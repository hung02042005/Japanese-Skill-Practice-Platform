/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/** Xác nhận thêm các từ vào sổ "Từ cần ôn lại" (§3.5, FR-FC-43). */
public record ReviewDeckAddRequest(
        @NotEmpty @Valid List<Item> items,
        // Nguồn (SPEC-notebook §7): 'wrong' khi từ sai cuối phiên, 'manual' khi lưu từ Từ điển.
        // Tùy chọn — mặc định 'manual' (xử lý ở service) để tương thích client cũ.
        @Pattern(regexp = "wrong|manual") String reason) {

    public record Item(
            @NotNull @Pattern(regexp = "VOCABULARY|vocabulary") String contentType, @NotNull Long contentId) {}
}
