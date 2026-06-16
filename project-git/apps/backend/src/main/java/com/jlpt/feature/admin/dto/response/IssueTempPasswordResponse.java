/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IssueTempPasswordResponse {
    private Long staffId;
    private String staffEmail;
    private LocalDateTime completedAt;
}
