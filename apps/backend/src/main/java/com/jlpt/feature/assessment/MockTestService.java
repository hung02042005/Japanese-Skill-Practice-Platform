/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.assessment;

import com.jlpt.feature.assessment.dto.request.MockTestRequest;
import com.jlpt.feature.assessment.dto.response.MockTestResponse;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.student.StudentUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Tạo nội dung đề thi thử (Staff) — chấm điểm/nộp bài đã chuyển sang {@link MockExamService} (xem
 * `.sdd/specs/backend/feat-mock-test`).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockTestService {

    private final AssessmentRepository assessmentRepository;

    @Transactional
    public MockTestResponse generateMockTest(MockTestRequest request, com.jlpt.feature.staff.StaffUser staffUser) {
        Assessment assessment = Assessment.builder()
                .assessmentType(Assessment.AssessmentType.EXAM)
                .title(request.getTitle())
                .jlptLevel(
                        request.getJlptLevel() != null ? StudentUser.JlptLevel.valueOf(request.getJlptLevel()) : null)
                .durationMin(request.getDurationMin())
                .passScore(request.getPassScore())
                .audioUrl(request.getAudioUrl())
                .status(Kanji.ContentStatus.DRAFT)
                .createdBy(staffUser)
                .build();

        assessment = assessmentRepository.save(assessment);
        return mapToResponse(assessment);
    }

    private MockTestResponse mapToResponse(Assessment a) {
        return MockTestResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .jlptLevel(a.getJlptLevel() != null ? a.getJlptLevel().name() : null)
                .durationMin(a.getDurationMin())
                .passScore(a.getPassScore())
                .totalScore(a.getTotalScore())
                .audioUrl(a.getAudioUrl())
                .status(a.getStatus().name())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
