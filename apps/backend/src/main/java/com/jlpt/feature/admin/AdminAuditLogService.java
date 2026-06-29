/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.feature.admin.dto.AuditLogItemResponse;
import com.jlpt.feature.staff.StaffUser;
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
                .actorName(actorName(l))
                .actorRole(actorRole(l))
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

    private String actorName(AdminAuditLog l) {
        if (l.getAdminActor() != null) return l.getAdminActor().getFullName();
        if (l.getStaffActor() != null) return l.getStaffActor().getFullName();
        if (l.getStudentActor() != null) return l.getStudentActor().getFullName();
        return "Hệ thống";
    }

    /** Phân biệt MANAGER vs STAFF qua staffRole (JWT chỉ có ROLE_STAFF). */
    private String actorRole(AdminAuditLog l) {
        if (l.getAdminActor() != null) return "ADMIN";
        if (l.getStaffActor() != null) {
            return l.getStaffActor().getStaffRole() == StaffUser.StaffRole.STAFF_MANAGER ? "MANAGER" : "STAFF";
        }
        if (l.getStudentActor() != null) return "STUDENT";
        return "SYSTEM";
    }
}
