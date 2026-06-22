package com.jlpt.feature.student.progress.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class LearningProgressRequest {
    private String contentType;
    private Long contentId;
    private String status;
    private BigDecimal progressPercent;
}
