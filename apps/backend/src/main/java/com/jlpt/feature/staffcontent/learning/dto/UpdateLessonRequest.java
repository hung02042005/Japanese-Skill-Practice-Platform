/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UC-27 — Request DTO for updating a lesson (PUT /api/staff/lessons/{lessonId}).
 * FR-27-15: update re-validates FR-27-09..FR-27-13, hence the same constraints as create.
 */
@Data
public class UpdateLessonRequest {

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

    @Min(value = 0, message = "displayOrder phải >= 0")
    private Integer displayOrder;
}
