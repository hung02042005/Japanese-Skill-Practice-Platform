/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.service;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.learning.dto.VocabTopicResponse;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.learning.dto.CreateVocabTopicRequest;
import com.jlpt.feature.staffcontent.learning.exception.LearningContentException;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.DuplicateResourceException;
import com.jlpt.shared.exception.ForbiddenException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quản lý catalog chủ đề từ vựng ({@code vocabulary_topics}) cho Staff.
 * Chủ đề là taxonomy (không phải nội dung học) nên Staff tạo được và publish ngay (FR-redo-topic).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VocabularyTopicService {

    private final VocabularyTopicRepository topicRepository;
    private final StaffUserRepository staffUserRepository;
    private final com.jlpt.feature.learning.VocabularyRepository vocabularyRepository;

    @Transactional(readOnly = true)
    public List<VocabTopicResponse> listByLevel(String levelStr) {
        JlptLevel level = parseLevel(levelStr);
        return topicRepository
                .findByJlptLevelAndStatusNotOrderByDisplayOrderAscIdAsc(level, Kanji.ContentStatus.DELETED)
                .stream()
                .map(VocabTopicResponse::from)
                .toList();
    }

    @Transactional
    public void deleteTopic(Long topicId, String managerEmail) {
        StaffUser manager = requireManager(managerEmail);
        VocabularyTopic topic = topicRepository
                .findById(topicId)
                .orElseThrow(() -> new com.jlpt.shared.exception.ResourceNotFoundException("Không tìm thấy chủ đề"));
        if (topic.getStatus() == Kanji.ContentStatus.DELETED) {
            throw new com.jlpt.shared.exception.BusinessException(400, "ALREADY_DELETED", "Chủ đề đã bị xóa trước đó");
        }
        long activeVocabs = vocabularyRepository.countByTopicRefIdAndStatusNot(topicId, Kanji.ContentStatus.DELETED);
        if (activeVocabs > 0) {
            throw new com.jlpt.shared.exception.BusinessException(
                    400, "RESOURCE_IN_USE", "Không thể xóa chủ đề đang chứa từ vựng");
        }
        topic.setStatus(Kanji.ContentStatus.DELETED);
        topicRepository.save(topic);
        log.info("[INFO] Manager {} DELETED vocabulary topic {}", manager.getId(), topicId);
    }

    @Transactional
    public VocabTopicResponse restoreTopic(Long topicId, String managerEmail) {
        StaffUser manager = requireManager(managerEmail);
        VocabularyTopic topic = topicRepository
                .findById(topicId)
                .orElseThrow(() -> new com.jlpt.shared.exception.ResourceNotFoundException("Không tìm thấy chủ đề"));
        if (topic.getStatus() != Kanji.ContentStatus.DELETED) {
            throw new com.jlpt.shared.exception.BusinessException(
                    400, "NOT_DELETED", "Chủ đề không ở trạng thái bị xóa");
        }
        topic.setStatus(Kanji.ContentStatus.PUBLISHED);
        VocabularyTopic saved = topicRepository.save(topic);
        log.info("[INFO] Manager {} RESTORED vocabulary topic {}", manager.getId(), topicId);
        return VocabTopicResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<VocabTopicResponse> listDeletedTopics(String managerEmail) {
        requireManager(managerEmail);
        return topicRepository.findByStatusOrderByUpdatedAtDesc(Kanji.ContentStatus.DELETED).stream()
                .map(VocabTopicResponse::from)
                .toList();
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

    @Transactional
    public VocabTopicResponse create(CreateVocabTopicRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        JlptLevel level = parseLevel(request.getJlptLevel());
        String titleVi = request.getTitleVi().trim();
        String titleJa = request.getTitleJa().trim();
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug().trim().toLowerCase(Locale.ROOT)
                : slugify(titleVi);

        if (topicRepository.existsByJlptLevelAndSlug(level, slug)) {
            throw new DuplicateResourceException("Chủ đề (slug '" + slug + "') đã tồn tại ở cấp độ này");
        }
        if (topicRepository.existsByJlptLevelAndTitleVi(level, titleVi)) {
            throw new DuplicateResourceException("Chủ đề '" + titleVi + "' đã tồn tại ở cấp độ này");
        }

        int nextOrder = topicRepository.findMaxDisplayOrder(level) + 1;
        VocabularyTopic topic = VocabularyTopic.builder()
                .jlptLevel(level)
                .slug(slug)
                .titleJa(titleJa)
                .titleVi(titleVi)
                .displayOrder(nextOrder)
                .status(Kanji.ContentStatus.PUBLISHED) // FR-redo-topic: publish ngay
                .createdBy(staff)
                .build();

        VocabularyTopic saved = topicRepository.save(topic);
        log.info("[INFO] Staff {} CREATED vocabulary topic {} ({} {})", staff.getId(), saved.getId(), level, slug);
        return VocabTopicResponse.from(saved);
    }

    private StaffUser resolveStaff(String email) {
        return staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Tài khoản nhân viên không tồn tại"));
    }

    private JlptLevel parseLevel(String value) {
        try {
            return JlptLevel.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw LearningContentException.invalidJlptLevel();
        }
    }

    /** Bỏ dấu tiếng Việt + chuẩn hoá thành slug a–z0–9 ngăn cách bằng '-'. */
    static String slugify(String input) {
        String noAccent = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        String slug =
                noAccent.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+)|(-+$)", "");
        return slug.isEmpty() ? "topic" : slug;
    }
}
