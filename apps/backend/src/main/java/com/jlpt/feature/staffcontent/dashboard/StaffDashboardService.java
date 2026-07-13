/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.dashboard;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.assessment.AssessmentRepository;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.dashboard.dto.StaffDashboardResponse;
import com.jlpt.shared.exception.ForbiddenException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tổng hợp số liệu bảng điều hành Staff. */
@Service
@RequiredArgsConstructor
public class StaffDashboardService {

        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private final StaffUserRepository staffUserRepository;
        private final AssessmentRepository assessmentRepository;

        @Transactional(readOnly = true)
        public StaffDashboardResponse getDashboard(String staffEmail) {
                StaffUser staff = staffUserRepository
                                .findByEmail(staffEmail)
                                .orElseThrow(() -> new ForbiddenException("Tài khoản nhân viên không tồn tại"));
                Long staffId = staff.getId();

                long draft = assessmentRepository.countByCreatedBy_IdAndStatus(staffId, ContentStatus.DRAFT);
                long pending = assessmentRepository.countByCreatedBy_IdAndStatus(staffId, ContentStatus.PENDING_REVIEW);

                List<StaffDashboardResponse.ActivityItem> activity = assessmentRepository
                                .findTop8ByCreatedBy_IdAndIsDeletedFalseOrderByUpdatedAtDesc(staffId).stream()
                                .map(a -> StaffDashboardResponse.ActivityItem.builder()
                                                .id(a.getId())
                                                .date(
                                                                a.getUpdatedAt() != null
                                                                                ? a.getUpdatedAt().format(DATE_FMT)
                                                                                : null)
                                                .type(typeLabel(a.getAssessmentType()))
                                                .title(a.getTitle())
                                                .status(a.getStatus() != null ? a.getStatus().getValue() : null)
                                                .build())
                                .toList();

                return StaffDashboardResponse.builder()
                                .draftCount(draft)
                                .pendingReviewCount(pending)
                                .openTicketCount(0)
                                .pendingGradingCount(0)
                                .recentActivity(activity)
                                .build();
        }

        private String typeLabel(Assessment.AssessmentType type) {
                if (type == Assessment.AssessmentType.EXAM) {
                        return "Đề thi";
                }
                return "Quiz";
        }
}
