/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.progress;

import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgress.ProgressStatus;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.progress.dto.LearningProgressRequest;
import com.jlpt.feature.student.progress.dto.LearningProgressResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentLearningProgressServiceImpl implements StudentLearningProgressService {

    private final StudentContentProgressRepository progressRepository;
    private final StudentUserRepository studentUserRepository;

    @Override
    @Transactional
    public LearningProgressResponse markProgress(LearningProgressRequest request, Long studentId) {
        StudentUser student = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentUser", studentId));

        ContentType contentType;
        try {
            contentType = ContentType.valueOf(request.getContentType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid content type: " + request.getContentType());
        }

        ProgressStatus newStatus;
        try {
            newStatus = ProgressStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }

        StudentContentProgress progress = progressRepository
                .findByStudentIdAndContentTypeAndContentId(studentId, contentType, request.getContentId())
                .orElse(null);

        if (progress == null) {
            progress = StudentContentProgress.builder()
                    .student(student)
                    .contentType(contentType)
                    .contentId(request.getContentId())
                    .status(newStatus)
                    .progressPercent(
                            request.getProgressPercent() != null ? request.getProgressPercent() : BigDecimal.ZERO)
                    .build();
        } else {
            // BR-07-04: progress_percent/status only increase, not decrease manually
            if (request.getProgressPercent() != null
                    && progress.getProgressPercent().compareTo(request.getProgressPercent()) < 0) {
                progress.setProgressPercent(request.getProgressPercent());
            }
            if (newStatus == ProgressStatus.COMPLETED) {
                progress.setStatus(newStatus);
            }
            progress.setLastStudiedAt(LocalDateTime.now());
        }

        if (progress.getStatus() == ProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        StudentContentProgress saved = progressRepository.save(progress);

        return LearningProgressResponse.builder()
                .id(saved.getId())
                .contentType(saved.getContentType().getValue())
                .contentId(saved.getContentId())
                .status(saved.getStatus().getValue())
                .progressPercent(saved.getProgressPercent())
                .build();
    }

    @Override
    @Transactional
    public void resetProgress(String contentTypeStr, Long studentId) {
        ContentType contentType;
        try {
            contentType = ContentType.valueOf(contentTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid content type: " + contentTypeStr);
        }
        progressRepository.deleteByStudentIdAndContentType(studentId, contentType);
    }
}
