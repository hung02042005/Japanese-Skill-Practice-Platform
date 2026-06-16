/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * UC-27 — Response DTO for a lesson (NFR-27-04: never expose the JPA entity).
 */
@Data
@Builder
public class LessonDetailResponse {

    private Long lessonId;
    private String title;
    private String lessonType;
    private String jlptLevel;
    private String contentText;
    private String videoUrl;
    private String audioUrl;
    private String attachmentUrl;
    private String explanation;
    private Integer displayOrder;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
