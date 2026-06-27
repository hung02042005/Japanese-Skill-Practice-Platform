/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.AuditLogItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin — xem nhật ký kiểm toán (UC-38). */
@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    @Transactional(readOnly = true)
    public Page<AuditLogItemResponse> getAuditLogs(
            String action, String targetTable, int page, int size) {
        String a = action == null || action.isBlank() ? null : action;
        String t = targetTable == null || targetTable.isBlank() ? null : targetTable;
        return adminAuditLogRepository
                .findByFilters(a, t, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    private AuditLogItemResponse toResponse(AdminAuditLog l) {
        return AuditLogItemResponse.builder()
                .logId(l.getId())
                .actionType(l.getAction())
                .adminEmail(actorEmail(l))
                .description(l.getDescription())
                .createdAt(l.getCreatedAt())
                .build();
    }

    private String actorEmail(AdminAuditLog l) {
        if (l.getAdminActor() != null) return l.getAdminActor().getEmail();
        if (l.getStaffActor() != null) return l.getStaffActor().getEmail();
        if (l.getStudentActor() != null) return l.getStudentActor().getEmail();
        return null;
    }
}
