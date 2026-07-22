/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.publishedcontent.service;

import com.jlpt.feature.assessment.Question;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.publishedcontent.dto.DeletedContentResponse;
import com.jlpt.feature.publishedcontent.repository.ManagedAssessmentRepository;
import com.jlpt.feature.publishedcontent.repository.ManagedGrammarRepository;
import com.jlpt.feature.publishedcontent.repository.ManagedKanjiRepository;
import com.jlpt.feature.publishedcontent.repository.ManagedLessonRepository;
import com.jlpt.feature.publishedcontent.repository.ManagedQuestionRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.shared.exception.BusinessException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý các yêu cầu liên quan đến khôi phục các thực thể học liệu đã bị soft-deleted cho Manager.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerDeletedContentService {

    private final ManagedLessonRepository lessonRepository;
    private final ManagedQuestionRepository questionRepository;
    private final VocabularyTopicRepository topicRepository;
    private final ManagedGrammarRepository grammarRepository;
    private final ManagedKanjiRepository kanjiRepository;
    private final ManagedAssessmentRepository assessmentRepository;
    private final StaffUserRepository staffUserRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Lấy danh sách toàn bộ hoặc lọc theo loại học liệu đã bị soft-deleted. */
    @Transactional(readOnly = true)
    public List<DeletedContentResponse> listDeleted(String managerEmail, String type) {
        requireManager(managerEmail);
        List<DeletedContentResponse> list = new ArrayList<>();

        boolean all = "all".equalsIgnoreCase(type) || type == null || type.isBlank();

        // 1. Bài học (Lesson)
        if (all || "lesson".equalsIgnoreCase(type)) {
            lessonRepository
                    .findByStatusOrderByUpdatedAtDesc(Lesson.LessonStatus.DELETED)
                    .forEach(item -> list.add(new DeletedContentResponse(
                            item.getId(),
                            "lesson",
                            item.getTitle(),
                            item.getJlptLevel() != null ? item.getJlptLevel().name() : null,
                            item.getUpdatedAt() != null ? item.getUpdatedAt().format(FORMATTER) : null)));
        }

        // 2. Câu hỏi (Question)
        if (all || "question".equalsIgnoreCase(type)) {
            questionRepository
                    .findByStatusOrderByUpdatedAtDesc(Question.ContentStatus.DELETED)
                    .forEach(item -> list.add(new DeletedContentResponse(
                            item.getId(),
                            "question",
                            item.getQuestionText(),
                            item.getJlptLevel() != null ? item.getJlptLevel().name() : null,
                            item.getUpdatedAt() != null ? item.getUpdatedAt().format(FORMATTER) : null)));
        }

        // 3. Từ vựng / Chủ đề (VocabularyTopic)
        if (all || "vocabulary".equalsIgnoreCase(type)) {
            topicRepository
                    .findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED)
                    .forEach(item -> list.add(new DeletedContentResponse(
                            item.getId(),
                            "vocabulary",
                            item.getTitleVi() + " (" + item.getTitleJa() + ")",
                            item.getJlptLevel() != null ? item.getJlptLevel().name() : null,
                            item.getUpdatedAt() != null ? item.getUpdatedAt().format(FORMATTER) : null)));
        }

        // 4. Ngữ pháp (GrammarPoint)
        if (all || "grammar".equalsIgnoreCase(type)) {
            grammarRepository
                    .findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED)
                    .forEach(item -> list.add(new DeletedContentResponse(
                            item.getId(),
                            "grammar",
                            item.getTitle(),
                            item.getJlptLevel() != null ? item.getJlptLevel().name() : null,
                            item.getUpdatedAt() != null ? item.getUpdatedAt().format(FORMATTER) : null)));
        }

        // 5. Kanji
        if (all || "kanji".equalsIgnoreCase(type)) {
            kanjiRepository
                    .findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED)
                    .forEach(item -> list.add(new DeletedContentResponse(
                            item.getId(),
                            "kanji",
                            item.getCharacterValue() + " - " + item.getMeaning(),
                            item.getJlptLevel() != null ? item.getJlptLevel().name() : null,
                            item.getUpdatedAt() != null ? item.getUpdatedAt().format(FORMATTER) : null)));
        }

        // 6. Bài kiểm tra (Assessment)
        if (all || "assessment".equalsIgnoreCase(type)) {
            assessmentRepository
                    .findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED)
                    .forEach(item -> list.add(new DeletedContentResponse(
                            item.getId(),
                            "assessment",
                            item.getTitle(),
                            item.getJlptLevel() != null ? item.getJlptLevel().name() : null,
                            item.getUpdatedAt() != null ? item.getUpdatedAt().format(FORMATTER) : null)));
        }

        return list;
    }

    /** Khôi phục trạng thái một mục đã bị soft-deleted trở lại published. */
    @Transactional
    public void restore(String managerEmail, String type, Long id) {
        StaffUser manager = requireManager(managerEmail);
        LocalDateTime now = LocalDateTime.now();
        int rows = 0;

        switch (type.toLowerCase()) {
            case "lesson":
                rows = lessonRepository.transition(id, Lesson.LessonStatus.DELETED, Lesson.LessonStatus.PUBLISHED, now);
                break;
            case "question":
                rows = questionRepository.transition(
                        id, Question.ContentStatus.DELETED, Question.ContentStatus.PUBLISHED, now);
                break;
            case "vocabulary":
                VocabularyTopic topic = topicRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề"));
                if (topic.getStatus() == Kanji.ContentStatus.DELETED) {
                    topic.setStatus(Kanji.ContentStatus.PUBLISHED);
                    topicRepository.save(topic);
                    rows = 1;
                }
                break;
            case "grammar":
                rows = grammarRepository.transition(
                        id, Kanji.ContentStatus.DELETED, Kanji.ContentStatus.PUBLISHED, now);
                break;
            case "kanji":
                rows = kanjiRepository.transition(id, Kanji.ContentStatus.DELETED, Kanji.ContentStatus.PUBLISHED, now);
                break;
            case "assessment":
                rows = assessmentRepository.transition(
                        id, Kanji.ContentStatus.DELETED, Kanji.ContentStatus.PUBLISHED, now);
                break;
            default:
                throw new BusinessException(400, "INVALID_TYPE", "Loại nội dung không hợp lệ: " + type);
        }

        if (rows == 0) {
            throw new BusinessException(
                    400, "RESTORE_FAILED", "Khôi phục nội dung thất bại hoặc nội dung không ở trạng thái bị xóa");
        }
        log.info("[INFO] Manager {} RESTORED deleted content type={} id={}", manager.getId(), type, id);
    }

    private StaffUser requireManager(String email) {
        StaffUser staff = staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Tài khoản không có thẩm quyền quản lý nội dung xuất bản"));
        if (staff.getStaffRole() != StaffUser.StaffRole.STAFF_MANAGER
                || staff.getStatus() != StaffUser.StaffStatus.ACTIVE) {
            throw new ForbiddenException("Tài khoản không có thẩm quyền quản lý nội dung xuất bản");
        }
        return staff;
    }
}
