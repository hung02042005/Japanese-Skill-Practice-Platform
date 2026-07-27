/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.contentreview.handler;

import com.jlpt.feature.assessment.Assessment;
import com.jlpt.feature.assessment.QuestionAssignment;
import com.jlpt.feature.assessment.QuestionAssignmentRepository;
import com.jlpt.feature.contentreview.model.ContentSnapshot;
import com.jlpt.feature.contentreview.model.ContentType;
import com.jlpt.feature.contentreview.repository.ReviewAssessmentRepository;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** UC-33 — Handler kiểm duyệt cho {@code assessments} (quiz/exam). */
@Component
@RequiredArgsConstructor
public class AssessmentContentHandler implements ReviewableContentHandler {

    private final ReviewAssessmentRepository repository;
    private final QuestionAssignmentRepository assignmentRepository;

    @Override
    public ContentType type() {
        return ContentType.ASSESSMENT;
    }

    @Override
    public String tableName() {
        return "assessments";
    }

    @Override
    public List<ContentSnapshot> findPending(JlptLevel level) {
        List<Assessment> assessments = (level == null)
                ? repository.findPending(ContentStatus.PENDING_REVIEW)
                : repository.findPending(ContentStatus.PENDING_REVIEW, level);
        return assessments.stream()
                .map(assessment -> toSnapshot(assessment, false))
                .toList();
    }

    @Override
    public Optional<ContentSnapshot> findActiveById(Long contentId) {
        return repository
                .findActiveById(contentId, ContentStatus.DELETED)
                .map(assessment -> toSnapshot(assessment, true));
    }

    @Override
    public int approve(Long contentId, StaffUser manager, LocalDateTime now) {
        // Chốt xuất bản (defense-in-depth, PA-3): bài quiz/exam lên published bắt buộc
        // có câu hỏi (FR-26-28 / FR-28-31) và tổng điểm khớp totalScore (FR-26-26 / FR-28-30).
        Assessment assessment = repository
                .findActiveById(contentId, ContentStatus.DELETED)
                .orElseThrow(
                        () -> new BusinessException(404, "CONTENT_NOT_FOUND", "Không tìm thấy nội dung cần duyệt"));

        long questionCount =
                assignmentRepository.countByParentTypeAndParentId(QuestionAssignment.ParentType.ASSESSMENT, contentId);
        if (questionCount == 0) {
            throw new BusinessException(422, "ASSESSMENT_EMPTY", "Không thể duyệt: bài chưa có câu hỏi nào");
        }

        BigDecimal assignedSum =
                assignmentRepository.sumScoreByParent(QuestionAssignment.ParentType.ASSESSMENT, contentId);
        BigDecimal total = assessment.getTotalScore() == null ? null : BigDecimal.valueOf(assessment.getTotalScore());
        if (total == null || assignedSum.compareTo(total) != 0) {
            throw new BusinessException(
                    422,
                    "ASSESSMENT_SCORE_MISMATCH",
                    "Không thể duyệt: tổng điểm câu hỏi (" + assignedSum + ") phải bằng tổng điểm bài (" + total + ")");
        }

        return repository.approve(contentId, manager, now, ContentStatus.PENDING_REVIEW, ContentStatus.PUBLISHED);
    }

    @Override
    public int transitionFromPending(Long contentId, String targetStatus, LocalDateTime now) {
        ContentStatus targetContentStatus = HandlerSupport.toEnum(ContentStatus.class, targetStatus);
        return repository.transition(contentId, now, ContentStatus.PENDING_REVIEW, targetContentStatus);
    }

    private ContentSnapshot toSnapshot(Assessment assessment, boolean withDetail) {
        Map<String, Object> detail = null;
        if (withDetail) {
            detail = HandlerSupport.newDetail();
            HandlerSupport.put(
                    detail,
                    "assessmentType",
                    assessment.getAssessmentType() != null
                            ? assessment.getAssessmentType().getValue()
                            : null);
            HandlerSupport.put(detail, "topic", assessment.getTopic());
            HandlerSupport.put(detail, "durationMin", assessment.getDurationMin());
            HandlerSupport.put(detail, "passScore", assessment.getPassScore());
            HandlerSupport.put(detail, "totalScore", assessment.getTotalScore());
        }
        return ContentSnapshot.builder()
                .contentId(assessment.getId())
                .contentType(ContentType.ASSESSMENT)
                .titleOrText(assessment.getTitle())
                .jlptLevel(
                        assessment.getJlptLevel() != null
                                ? assessment.getJlptLevel().name()
                                : null)
                .status(assessment.getStatus() != null ? assessment.getStatus().getValue() : null)
                .createdById(HandlerSupport.creatorId(assessment.getCreatedBy()))
                .createdByName(HandlerSupport.creatorName(assessment.getCreatedBy()))
                .submittedAt(assessment.getUpdatedAt())
                .detail(detail)
                .build();
    }
}
