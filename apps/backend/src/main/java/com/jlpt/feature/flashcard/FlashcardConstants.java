/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard;

/** Hằng số dùng chung cho sổ tay/flashcard (tránh magic string & ràng buộc lệch). */
public final class FlashcardConstants {

    private FlashcardConstants() {}

    /** Giới hạn độ dài tên sổ tay — thống nhất cho mọi DTO (khớp cột DB {@code name}). */
    public static final int DECK_NAME_MAX = 100;

    /** Tên sổ tay mặc định cho thẻ tùy chỉnh khi client không truyền tên. */
    public static final String DEFAULT_DECK_NAME = "Mặc định";

    /** Tên sổ "Từ cần ôn lại" (sổ hệ thống chứa từ sai/lưu thủ công). */
    public static final String REVIEW_DECK_NAME = "Từ cần ôn lại";
}
