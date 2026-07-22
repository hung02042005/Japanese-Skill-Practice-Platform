/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.service;

import com.jlpt.feature.contentreview.model.ContentType;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.staff.StaffUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-33 — Ghi nhật ký kiểm duyệt vào {@code admin_audit_logs} (FR-33-20..22).
 *
 * <p>{@link Propagation#MANDATORY} bắt buộc phương thức chạy trong transaction của
 * {@link ContentReviewService} để đổi trạng thái và ghi audit là nguyên tử (FR-33-22).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewAuditService {

    public static final String ACTION_APPROVE = "approve_content";
    public static final String ACTION_REJECT = "reject_content";
    public static final String ACTION_REQUEST_CHANGES = "request_changes_content";

    private final AdminAuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void log(
            StaffUser actor,
            String action,
            ContentType contentType,
            String targetTable,
            Long contentId,
            String feedback) {
        AdminAuditLog entry = AdminAuditLog.builder()
                .staffActor(actor)
                .action(action)
                .targetTable(targetTable)
                .targetId(contentId)
                .description(feedback)
                .build();
        auditLogRepository.save(entry);

        log.info("[INFO] StaffManager {} {} {} {}", actor.getId(), action, contentType.getValue(), contentId);
    }
}
