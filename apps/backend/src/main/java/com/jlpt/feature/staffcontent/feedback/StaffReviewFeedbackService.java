/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.feedback;

import com.jlpt.feature.admin.AdminAuditLog;
import com.jlpt.feature.admin.AdminAuditLogRepository;
import com.jlpt.feature.contentreview.ContentType;
import com.jlpt.feature.contentreview.ReviewableContentResolver;
import com.jlpt.feature.contentreview.ReviewAuditService;
import com.jlpt.feature.contentreview.handler.ReviewableContentHandler;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cho phép staff xem phản hồi (từ chối / yêu cầu chỉnh sửa) mà manager đã gửi
 * cho nội dung của họ. Chỉ được xem nội dung do chính mình tạo ra.
 */
@Service
@RequiredArgsConstructor
public class StaffReviewFeedbackService {

    private static final List<String> FEEDBACK_ACTIONS = List.of(
            ReviewAuditService.ACTION_REJECT,
            ReviewAuditService.ACTION_REQUEST_CHANGES);

    private final AdminAuditLogRepository auditLogRepository;
    private final ReviewableContentResolver resolver;
    private final StaffUserRepository staffUserRepository;

    @Transactional(readOnly = true)
    public ReviewFeedbackResponse getContentFeedback(Long contentId, String contentTypeStr, String staffEmail) {
        ContentType type = ContentType.fromValue(contentTypeStr);
        ReviewableContentHandler handler = resolver.resolve(type);

        // Lấy thông tin nội dung để kiểm tra quyền sở hữu
        var snapshot = handler.findActiveById(contentId)
                .orElseThrow(() -> new BusinessException(404, "CONTENT_NOT_FOUND",
                        "Không tìm thấy nội dung: " + contentId));

        StaffUser staff = staffUserRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ForbiddenException("Không xác định được tài khoản"));

        if (!staff.getId().equals(snapshot.getCreatedById())) {
            throw new ForbiddenException("Bạn không có quyền xem phản hồi của nội dung này");
        }

        AdminAuditLog log = auditLogRepository
                .findFirstByTargetIdAndTargetTableAndActionInOrderByCreatedAtDesc(
                        contentId, handler.tableName(), FEEDBACK_ACTIONS)
                .orElseThrow(() -> new BusinessException(404, "FEEDBACK_NOT_FOUND",
                        "Chưa có phản hồi nào cho nội dung này"));

        return ReviewFeedbackResponse.builder()
                .feedback(log.getDescription())
                .actionType(log.getAction())
                .reviewedAt(log.getCreatedAt())
                .build();
    }
}
