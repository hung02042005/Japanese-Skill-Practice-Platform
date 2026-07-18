/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.dto;

/**
 * DTO phản hồi chứa thông tin rút gọn của nội dung bị soft-deleted phục vụ giao diện thùng rác của Manager.
 */
public record DeletedContentResponse(
        Long id,
        String contentType, // "lesson", "question", "vocabulary", "grammar", "kanji", "assessment"
        String titleOrText, // Tiêu đề hoặc nội dung hiển thị chính
        String jlptLevel, // Cấp độ JLPT (N5 - N1)
        String updatedAt // Thời gian cập nhật/xóa cuối cùng
        ) {}
