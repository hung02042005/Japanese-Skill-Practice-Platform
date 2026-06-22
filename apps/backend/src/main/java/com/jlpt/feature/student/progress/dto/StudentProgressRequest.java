/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.progress.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressRequest {
    private String contentType;
    private Long contentId;
    private String status;
    private BigDecimal progressPercent;
}
