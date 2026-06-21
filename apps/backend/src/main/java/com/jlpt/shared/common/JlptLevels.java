/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.common;

import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.BadRequestException;

/**
 * Parse cấp độ JLPT từ chuỗi client gửi lên — một chỗ duy nhất để thống nhất hành vi lỗi (400)
 * thay vì lặp khối {@code valueOf + try/catch} ở từng service student-facing.
 */
public final class JlptLevels {

    private JlptLevels() {}

    /** Bắt buộc: blank → 400 "không được để trống"; sai định dạng → 400 "không hợp lệ". */
    public static JlptLevel parseRequired(String level) {
        if (level == null || level.isBlank()) {
            throw new BadRequestException("Cấp độ JLPT không được để trống");
        }
        return parse(level);
    }

    /** Tuỳ chọn: null/blank → null (dùng mặc định của lời gọi); sai định dạng → 400 "không hợp lệ". */
    public static JlptLevel parseOptional(String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        return parse(level);
    }

    private static JlptLevel parse(String level) {
        try {
            return JlptLevel.valueOf(level.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Cấp độ JLPT không hợp lệ: " + level);
        }
    }
}
