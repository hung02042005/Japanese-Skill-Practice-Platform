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
    /** Tên người thực hiện (Admin/Staff/Manager/Student). */
    private String actorName;
    /** Vai trò người thực hiện: ADMIN | MANAGER | STAFF | STUDENT | SYSTEM. */
    private String actorRole;

    private String description;
    private LocalDateTime createdAt;
}
