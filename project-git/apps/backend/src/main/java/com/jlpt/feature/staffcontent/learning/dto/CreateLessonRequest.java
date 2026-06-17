/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-27 — Request DTO for creating a lesson (POST /api/staff/lessons).
 * FR-27-09: title, lessonType, jlptLevel are mandatory.
 * FR-27-10: lessonType ∈ {lesson, reading, listening, speaking}.
 * FR-27-02: jlptLevel ∈ {N5..N1}.
 * FR-27-11/12: at least one content field (or audioUrl for listening) — validated in service.
 * FR-27-03: media fields carry URLs only.
 */
@Data
public class CreateLessonRequest {

    @NotBlank(message = "Thiếu trường bắt buộc: title")
    private String title;

    @NotBlank(message = "Thiếu trường bắt buộc: lessonType")
    @Pattern(regexp = "^(lesson|reading|listening|speaking)$", message = "lesson_type không hợp lệ")
    private String lessonType;

    @NotBlank(message = "Cấp độ JLPT phải là N5 đến N1")
    @Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "Cấp độ JLPT phải là N5 đến N1")
    private String jlptLevel;

    private String contentText;
    private String videoUrl;
    private String audioUrl;
    private String attachmentUrl;
    private String explanation;

    /** FR-27-13: integer >= 0; defaults to 0 when omitted (handled in service). */
    @Min(value = 0, message = "displayOrder phải >= 0")
    private Integer displayOrder;
}
