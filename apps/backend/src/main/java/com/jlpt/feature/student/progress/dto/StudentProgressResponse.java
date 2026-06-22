/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.progress.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressResponse {
    private Long progressId;
    private Long studentId;
    private String contentType;
    private Long contentId;
    private String status;
    private BigDecimal progressPercent;
    private LocalDateTime completedAt;
    private LocalDateTime lastStudiedAt;
}
