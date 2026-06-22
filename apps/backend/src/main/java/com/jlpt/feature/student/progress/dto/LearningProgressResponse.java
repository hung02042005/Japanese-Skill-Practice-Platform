package com.jlpt.feature.student.progress.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LearningProgressResponse {
    private Long id;
    private String contentType;
    private Long contentId;
    private String status;
    private BigDecimal progressPercent;
}
