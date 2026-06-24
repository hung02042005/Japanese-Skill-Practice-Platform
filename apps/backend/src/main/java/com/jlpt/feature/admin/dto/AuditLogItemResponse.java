/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditLogItemResponse {
    private Long logId;
    private String actionType;
    private String targetEmail;
    private String adminEmail;
    private String description;
    private LocalDateTime createdAt;
}
