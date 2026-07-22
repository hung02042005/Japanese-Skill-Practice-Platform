/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.service;

import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.Lesson.LessonStatus;
import com.jlpt.feature.learning.Lesson.LessonType;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.learning.dto.CreateKanjiRequest;
import com.jlpt.feature.staffcontent.learning.dto.CreateVocabularyRequest;
import com.jlpt.feature.staffcontent.learning.dto.KanjiDetailResponse;
import com.jlpt.feature.staffcontent.learning.dto.LessonDetailResponse;
import com.jlpt.feature.staffcontent.learning.dto.SubmitReviewRequest;
import com.jlpt.feature.staffcontent.learning.dto.SubmitReviewResponse;
import com.jlpt.feature.staffcontent.learning.dto.UpdateKanjiRequest;
import com.jlpt.feature.staffcontent.learning.dto.UpdateLessonRequest;
import com.jlpt.feature.staffcontent.learning.dto.UpdateVocabularyRequest;
import com.jlpt.feature.staffcontent.learning.dto.VocabularyDetailResponse;
import com.jlpt.feature.staffcontent.learning.exception.LearningContentException;
import com.jlpt.feature.staffcontent.learning.repository.StaffKanjiRepository;
import com.jlpt.feature.staffcontent.learning.repository.StaffVocabularyRepository;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UC-27 — Service implementation for Staff learning-content operations.
 * Business rules: create => draft (FR-27-01); edit only while {draft, rejected} (FR-27-04);
 * Staff cannot publish (FR-27-05 — no status field is accepted from the client);
 * ownership enforced unless STAFF_MANAGER (FR-27-06); media stored as URL only (FR-27-03).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LearningContentServiceImpl implements LearningContentService {

    private final LessonRepository lessonRepository;
    private final StaffVocabularyRepository vocabularyRepository;
    private final VocabularyTopicRepository vocabularyTopicRepository;
    private final StaffKanjiRepository kanjiRepository;
    private final StaffUserRepository staffUserRepository;

    /* ── Lesson ──────────────────────────────────────────────────── */

    @Override
    @Transactional
    public LessonDetailResponse updateLesson(Long lessonId, UpdateLessonRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        Lesson lesson = lessonRepository
                .findByIdAndStatusNot(lessonId, LessonStatus.DELETED)
                .orElseThrow(LearningContentException::lessonNotFound);

        guardOwnership(lesson.getCreatedBy(), staff);
        guardEditableLesson(lesson.getStatus());

        JlptLevel level = parseLevel(request.getJlptLevel());
        LessonType type = parseLessonType(request.getLessonType());
        validateLessonContent(
                type,
                request.getContentText(),
                request.getVideoUrl(),
                request.getAudioUrl(),
                request.getAttachmentUrl());

        lesson.setTitle(request.getTitle().trim());
        lesson.setLessonType(type);
        lesson.setJlptLevel(level);
        lesson.setContentText(trimToNull(request.getContentText()));
        lesson.setVideoUrl(trimToNull(request.getVideoUrl()));
        lesson.setAudioUrl(trimToNull(request.getAudioUrl()));
        lesson.setAttachmentUrl(trimToNull(request.getAttachmentUrl()));
        lesson.setExplanation(trimToNull(request.getExplanation()));
        lesson.setDisplayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder());

        Lesson saved = lessonRepository.save(lesson);
        log.info("[INFO] Staff {} UPDATED lesson {}", staff.getId(), saved.getId());
        return toLessonDetail(saved);
    }

    /* ── Vocabulary ──────────────────────────────────────────────── */

    @Override
    @Transactional
    public VocabularyDetailResponse createVocabulary(CreateVocabularyRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        JlptLevel level = parseLevel(request.getJlptLevel());

        VocabularyTopic topic = resolveTopic(request.getTopicId(), level);

        Vocabulary vocabulary = Vocabulary.builder()
                .word(request.getWord().trim())
                .furigana(request.getFurigana().trim())
                .meaning(request.getMeaning().trim())
                .wordType(trimToNull(request.getWordType()))
                .jlptLevel(level)
                .topicRef(topic) // FR-redo-topic: khoá chủ đề duy nhất
                .audioUrl(trimToNull(request.getAudioUrl()))
                .exampleSentenceJp(trimToNull(request.getExampleSentenceJp()))
                .exampleSentenceVi(trimToNull(request.getExampleSentenceVi()))
                .status(ContentStatus.DRAFT) // FR-27-01
                .createdBy(staff) // FR-27-01
                .build();

        if (request.getLessonId() != null) {
            Lesson lesson = lessonRepository
                    .findByIdAndStatusNot(request.getLessonId(), LessonStatus.DELETED)
                    .orElseThrow(LearningContentException::lessonNotFound);
            vocabulary.setLesson(lesson);
        }

        Vocabulary saved = vocabularyRepository.save(vocabulary);
        log.info("[INFO] Staff {} CREATED vocabulary {}", staff.getId(), saved.getId());
        return toVocabularyDetail(saved);
    }

    @Override
    @Transactional
    public VocabularyDetailResponse updateVocabulary(
            Long vocabularyId, UpdateVocabularyRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        Vocabulary vocabulary = vocabularyRepository
                .findByIdAndStatusNot(vocabularyId, ContentStatus.DELETED)
                .orElseThrow(LearningContentException::contentNotFound);

        guardOwnership(vocabulary.getCreatedBy(), staff);
        guardEditable(vocabulary.getStatus());

        if (request.getWord() != null) vocabulary.setWord(request.getWord().trim());
        if (request.getFurigana() != null)
            vocabulary.setFurigana(request.getFurigana().trim());
        if (request.getMeaning() != null)
            vocabulary.setMeaning(request.getMeaning().trim());
        if (request.getWordType() != null) vocabulary.setWordType(trimToNull(request.getWordType()));
        if (request.getJlptLevel() != null) vocabulary.setJlptLevel(parseLevel(request.getJlptLevel()));
        if (request.getTopicId() != null) {
            vocabulary.setTopicRef(resolveTopic(request.getTopicId(), vocabulary.getJlptLevel()));
        }
        if (request.getAudioUrl() != null) vocabulary.setAudioUrl(trimToNull(request.getAudioUrl()));
        if (request.getExampleSentenceJp() != null)
            vocabulary.setExampleSentenceJp(trimToNull(request.getExampleSentenceJp()));
        if (request.getExampleSentenceVi() != null)
            vocabulary.setExampleSentenceVi(trimToNull(request.getExampleSentenceVi()));

        if (request.isClearLesson()) {
            vocabulary.setLesson(null);
        } else if (request.getLessonId() != null) {
            Lesson lesson = lessonRepository
                    .findByIdAndStatusNot(request.getLessonId(), LessonStatus.DELETED)
                    .orElseThrow(LearningContentException::lessonNotFound);
            vocabulary.setLesson(lesson);
        }

        Vocabulary saved = vocabularyRepository.save(vocabulary);
        log.info("[INFO] Staff {} UPDATED vocabulary {}", staff.getId(), saved.getId());
        return toVocabularyDetail(saved);
    }

    /* ── Kanji ───────────────────────────────────────────────────── */

    @Override
    @Transactional
    public KanjiDetailResponse createKanji(CreateKanjiRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        JlptLevel level = parseLevel(request.getJlptLevel());

        if (!StringUtils.hasText(request.getOnyomi()) && !StringUtils.hasText(request.getKunyomi())) {
            throw LearningContentException.missingField("onyomi hoặc kunyomi");
        }

        String characterValue = request.getCharacterValue().trim();
        if (kanjiRepository.existsByCharacterValue(characterValue)) {
            throw LearningContentException.kanjiDuplicate();
        }

        Kanji kanji = Kanji.builder()
                .characterValue(characterValue)
                .meaning(request.getMeaning().trim())
                .onyomi(trimToNull(request.getOnyomi()))
                .kunyomi(trimToNull(request.getKunyomi()))
                .strokeCount(request.getStrokeCount())
                .jlptLevel(level)
                .strokeOrderUrl(trimToNull(request.getStrokeOrderUrl()))
                .exampleWord(trimToNull(request.getExampleWord()))
                .exampleReading(trimToNull(request.getExampleReading()))
                .exampleMeaning(trimToNull(request.getExampleMeaning()))
                .status(ContentStatus.DRAFT) // FR-27-01
                .createdBy(staff) // FR-27-01
                .build();

        Kanji saved = kanjiRepository.save(kanji);
        log.info("[INFO] Staff {} CREATED kanji {}", staff.getId(), saved.getId());
        return toKanjiDetail(saved);
    }

    @Override
    @Transactional
    public KanjiDetailResponse updateKanji(Long kanjiId, UpdateKanjiRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        Kanji kanji = kanjiRepository
                .findByIdAndStatusNot(kanjiId, ContentStatus.DELETED)
                .orElseThrow(LearningContentException::contentNotFound);

        guardOwnership(kanji.getCreatedBy(), staff);
        guardEditable(kanji.getStatus());

        if (request.getCharacterValue() != null) {
            String newChar = request.getCharacterValue().trim();
            if (!newChar.equals(kanji.getCharacterValue()) && kanjiRepository.existsByCharacterValue(newChar)) {
                throw LearningContentException.kanjiDuplicate();
            }
            kanji.setCharacterValue(newChar);
        }
        if (request.getMeaning() != null) kanji.setMeaning(request.getMeaning().trim());
        if (request.getOnyomi() != null) kanji.setOnyomi(trimToNull(request.getOnyomi()));
        if (request.getKunyomi() != null) kanji.setKunyomi(trimToNull(request.getKunyomi()));
        if (request.getStrokeCount() != null) kanji.setStrokeCount(request.getStrokeCount());
        if (request.getJlptLevel() != null) kanji.setJlptLevel(parseLevel(request.getJlptLevel()));
        if (request.getStrokeOrderUrl() != null) kanji.setStrokeOrderUrl(trimToNull(request.getStrokeOrderUrl()));
        if (request.getExampleWord() != null) kanji.setExampleWord(trimToNull(request.getExampleWord()));
        if (request.getExampleReading() != null) kanji.setExampleReading(trimToNull(request.getExampleReading()));
        if (request.getExampleMeaning() != null) kanji.setExampleMeaning(trimToNull(request.getExampleMeaning()));

        if (!StringUtils.hasText(kanji.getOnyomi()) && !StringUtils.hasText(kanji.getKunyomi())) {
            throw LearningContentException.missingField("onyomi hoặc kunyomi");
        }

        Kanji saved = kanjiRepository.save(kanji);
        log.info("[INFO] Staff {} UPDATED kanji {}", staff.getId(), saved.getId());
        return toKanjiDetail(saved);
    }

    /* ── Submit for review ───────────────────────────────────────── */

    @Override
    @Transactional
    public SubmitReviewResponse submitForReview(SubmitReviewRequest request, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        String contentType = request.getContentType().toLowerCase();
        Long contentId = request.getContentId();

        return switch (contentType) {
            case "lesson" -> submitLesson(contentId, staff);
            case "vocabulary" -> submitVocabulary(contentId, staff);
            case "kanji" -> submitKanji(contentId, staff);
            default -> throw LearningContentException.invalidContentType();
        };
    }

    /* ── UC-27 GET endpoints ─────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public Page<LessonDetailResponse> listLessons(
            String q,
            String jlptLevelStr,
            String lessonTypeStr,
            String statusStr,
            int page,
            int size,
            String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        int effectiveSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, effectiveSize, Sort.by(Sort.Direction.DESC, "updatedAt"));

        JlptLevel jlptLevel = null;
        if (StringUtils.hasText(jlptLevelStr)) {
            try {
                jlptLevel = JlptLevel.valueOf(jlptLevelStr);
            } catch (Exception ignored) {
            }
        }
        LessonType lessonType = null;
        if (StringUtils.hasText(lessonTypeStr)) {
            try {
                lessonType = LessonType.valueOf(lessonTypeStr.toUpperCase());
            } catch (Exception ignored) {
            }
        }
        LessonStatus status = null;
        if (StringUtils.hasText(statusStr)) {
            try {
                status = LessonStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {
            }
        }

        Page<Lesson> resultPage = lessonRepository.findByCreatedByWithFilters(
                staff.getId(),
                jlptLevel,
                lessonType,
                status,
                LessonStatus.DELETED,
                StringUtils.hasText(q) ? q : null,
                pageable);
        return resultPage.map(this::toLessonDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDetailResponse getLesson(Long lessonId, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        Lesson lesson = lessonRepository
                .findByIdAndStatusNot(lessonId, LessonStatus.DELETED)
                .orElseThrow(LearningContentException::lessonNotFound);
        guardOwnership(lesson.getCreatedBy(), staff);
        return toLessonDetail(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VocabularyDetailResponse> listVocabulary(
            String q, String jlptLevelStr, Long topicId, String statusStr, int page, int size, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        int effectiveSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, effectiveSize, Sort.by(Sort.Direction.DESC, "updatedAt"));

        JlptLevel jlptLevel = null;
        if (StringUtils.hasText(jlptLevelStr)) {
            try {
                jlptLevel = JlptLevel.valueOf(jlptLevelStr);
            } catch (Exception ignored) {
            }
        }
        ContentStatus status = null;
        if (StringUtils.hasText(statusStr)) {
            try {
                status = ContentStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {
            }
        }

        Page<Vocabulary> resultPage = vocabularyRepository.findByCreatedByWithFilters(
                staff.getId(),
                jlptLevel,
                topicId,
                status,
                ContentStatus.DELETED,
                StringUtils.hasText(q) ? q : null,
                pageable);
        return resultPage.map(this::toVocabularyDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyDetailResponse getVocabulary(Long vocabularyId, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        Vocabulary vocabulary = vocabularyRepository
                .findByIdAndStatusNot(vocabularyId, ContentStatus.DELETED)
                .orElseThrow(LearningContentException::contentNotFound);
        guardOwnership(vocabulary.getCreatedBy(), staff);
        return toVocabularyDetail(vocabulary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KanjiDetailResponse> listKanji(
            String q, String jlptLevelStr, String statusStr, int page, int size, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        int effectiveSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, effectiveSize, Sort.by(Sort.Direction.DESC, "updatedAt"));

        JlptLevel jlptLevel = null;
        if (StringUtils.hasText(jlptLevelStr)) {
            try {
                jlptLevel = JlptLevel.valueOf(jlptLevelStr);
            } catch (Exception ignored) {
            }
        }
        ContentStatus status = null;
        if (StringUtils.hasText(statusStr)) {
            try {
                status = ContentStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {
            }
        }

        Page<Kanji> resultPage = kanjiRepository.findByCreatedByWithFilters(
                staff.getId(), jlptLevel, status, ContentStatus.DELETED, StringUtils.hasText(q) ? q : null, pageable);
        return resultPage.map(this::toKanjiDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public KanjiDetailResponse getKanji(Long kanjiId, String staffEmail) {
        StaffUser staff = resolveStaff(staffEmail);
        Kanji kanji = kanjiRepository
                .findByIdAndStatusNot(kanjiId, ContentStatus.DELETED)
                .orElseThrow(LearningContentException::contentNotFound);
        guardOwnership(kanji.getCreatedBy(), staff);
        return toKanjiDetail(kanji);
    }

    /* ── Helpers ─────────────────────────────────────────────────── */

    private StaffUser resolveStaff(String email) {
        return staffUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Tài khoản nhân viên không tồn tại"));
    }

    /** FR-27-06: only the owner or a STAFF_MANAGER may operate on the item. */
    private void guardOwnership(StaffUser owner, StaffUser staff) {
        if (staff.getStaffRole() == StaffUser.StaffRole.STAFF_MANAGER) {
            return;
        }
        if (owner == null || !owner.getId().equals(staff.getId())) {
            throw LearningContentException.ownershipDenied();
        }
    }

    /** FR-27-04: edit allowed only while status ∈ {draft, rejected}. */
    private void guardEditableLesson(LessonStatus status) {
        if (status != LessonStatus.DRAFT && status != LessonStatus.REJECTED) {
            throw LearningContentException.invalidStatusTransition();
        }
    }

    /** FR-27-26: submit allowed only while status ∈ {draft, rejected}. */
    private void guardEditable(ContentStatus status) {
        if (status != ContentStatus.DRAFT && status != ContentStatus.REJECTED) {
            throw LearningContentException.invalidStatusTransition();
        }
    }

    private void guardSubmittable(boolean submittable) {
        if (!submittable) {
            throw LearningContentException.invalidStatusTransition();
        }
    }

    private JlptLevel parseLevel(String value) {
        try {
            return JlptLevel.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw LearningContentException.invalidJlptLevel();
        }
    }

    /**
     * Resolve chủ đề từ catalog, đảm bảo cùng cấp độ với từ vựng (FR-redo-topic).
     * Topic không tồn tại → 404; lệch cấp độ → 400.
     */
    private VocabularyTopic resolveTopic(Long topicId, JlptLevel vocabLevel) {
        if (topicId == null) {
            throw LearningContentException.missingField("topicId");
        }
        VocabularyTopic topic =
                vocabularyTopicRepository.findById(topicId).orElseThrow(LearningContentException::contentNotFound);
        if (vocabLevel != null && topic.getJlptLevel() != vocabLevel) {
            throw LearningContentException.validationFailed("Chủ đề không thuộc cấp độ " + vocabLevel);
        }
        return topic;
    }

    private LessonType parseLessonType(String value) {
        try {
            return LessonType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw LearningContentException.invalidLessonType();
        }
    }

    /** FR-27-11/12: at least one content field; listening additionally requires audioUrl. */
    private void validateLessonContent(
            LessonType type, String contentText, String videoUrl, String audioUrl, String attachmentUrl) {
        boolean hasAnyContent = StringUtils.hasText(contentText)
                || StringUtils.hasText(videoUrl)
                || StringUtils.hasText(audioUrl)
                || StringUtils.hasText(attachmentUrl);
        if (!hasAnyContent) {
            throw LearningContentException.lessonContentRequired();
        }
        if (type == LessonType.LISTENING && !StringUtils.hasText(audioUrl)) {
            throw LearningContentException.lessonContentRequired();
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LessonDetailResponse toLessonDetail(Lesson entity) {
        return LessonDetailResponse.builder()
                .lessonId(entity.getId())
                .title(entity.getTitle())
                .lessonType(
                        entity.getLessonType() != null ? entity.getLessonType().getValue() : null)
                .jlptLevel(entity.getJlptLevel() != null ? entity.getJlptLevel().name() : null)
                .contentText(entity.getContentText())
                .videoUrl(entity.getVideoUrl())
                .audioUrl(entity.getAudioUrl())
                .attachmentUrl(entity.getAttachmentUrl())
                .explanation(entity.getExplanation())
                .displayOrder(entity.getDisplayOrder())
                .status(entity.getStatus().getValue())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private VocabularyDetailResponse toVocabularyDetail(Vocabulary entity) {
        return VocabularyDetailResponse.builder()
                .vocabularyId(entity.getId())
                .word(entity.getWord())
                .furigana(entity.getFurigana())
                .meaning(entity.getMeaning())
                .wordType(entity.getWordType())
                .jlptLevel(entity.getJlptLevel() != null ? entity.getJlptLevel().name() : null)
                .topicId(entity.getTopicRef() != null ? entity.getTopicRef().getId() : null)
                .topicTitle(entity.getTopicRef() != null ? entity.getTopicRef().getTitleVi() : null)
                .topicSlug(entity.getTopicRef() != null ? entity.getTopicRef().getSlug() : null)
                .audioUrl(entity.getAudioUrl())
                .exampleSentenceJp(entity.getExampleSentenceJp())
                .exampleSentenceVi(entity.getExampleSentenceVi())
                .lessonId(entity.getLesson() != null ? entity.getLesson().getId() : null)
                .status(entity.getStatus().getValue())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private KanjiDetailResponse toKanjiDetail(Kanji entity) {
        return KanjiDetailResponse.builder()
                .kanjiId(entity.getId())
                .characterValue(entity.getCharacterValue())
                .meaning(entity.getMeaning())
                .onyomi(entity.getOnyomi())
                .kunyomi(entity.getKunyomi())
                .strokeCount(entity.getStrokeCount())
                .jlptLevel(entity.getJlptLevel() != null ? entity.getJlptLevel().name() : null)
                .strokeOrderUrl(entity.getStrokeOrderUrl())
                .exampleWord(entity.getExampleWord())
                .exampleReading(entity.getExampleReading())
                .exampleMeaning(entity.getExampleMeaning())
                .status(entity.getStatus().getValue())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /* ── Submit helpers ──────────────────────────────────────────── */

    private SubmitReviewResponse submitLesson(Long contentId, StaffUser staff) {
        Lesson lesson = lessonRepository
                .findByIdAndStatusNot(contentId, LessonStatus.DELETED)
                .orElseThrow(LearningContentException::contentNotFound);
        guardOwnership(lesson.getCreatedBy(), staff);
        guardSubmittable(lesson.getStatus() == LessonStatus.DRAFT || lesson.getStatus() == LessonStatus.REJECTED);

        if (!StringUtils.hasText(lesson.getTitle())) throw LearningContentException.missingField("title");
        if (lesson.getLessonType() == null) throw LearningContentException.missingField("lessonType");
        if (lesson.getJlptLevel() == null) throw LearningContentException.missingField("jlptLevel");
        validateLessonContent(
                lesson.getLessonType(),
                lesson.getContentText(),
                lesson.getVideoUrl(),
                lesson.getAudioUrl(),
                lesson.getAttachmentUrl());

        lesson.setStatus(LessonStatus.PENDING_REVIEW);
        lessonRepository.save(lesson);
        log.info("[INFO] Staff {} SUBMITTED lesson {}", staff.getId(), lesson.getId());
        return SubmitReviewResponse.builder()
                .contentId(lesson.getId())
                .contentType("lesson")
                .status(LessonStatus.PENDING_REVIEW.getValue())
                .build();
    }

    private SubmitReviewResponse submitVocabulary(Long contentId, StaffUser staff) {
        Vocabulary vocabulary = vocabularyRepository
                .findByIdAndStatusNot(contentId, ContentStatus.DELETED)
                .orElseThrow(LearningContentException::contentNotFound);
        guardOwnership(vocabulary.getCreatedBy(), staff);
        guardSubmittable(
                vocabulary.getStatus() == ContentStatus.DRAFT || vocabulary.getStatus() == ContentStatus.REJECTED);

        if (!StringUtils.hasText(vocabulary.getWord())) throw LearningContentException.missingField("word");
        if (!StringUtils.hasText(vocabulary.getFurigana())) throw LearningContentException.missingField("furigana");
        if (!StringUtils.hasText(vocabulary.getMeaning())) throw LearningContentException.missingField("meaning");
        if (vocabulary.getJlptLevel() == null) throw LearningContentException.missingField("jlptLevel");

        vocabulary.setStatus(ContentStatus.PENDING_REVIEW);
        vocabularyRepository.save(vocabulary);
        log.info("[INFO] Staff {} SUBMITTED vocabulary {}", staff.getId(), vocabulary.getId());
        return SubmitReviewResponse.builder()
                .contentId(vocabulary.getId())
                .contentType("vocabulary")
                .status(ContentStatus.PENDING_REVIEW.getValue())
                .build();
    }

    private SubmitReviewResponse submitKanji(Long contentId, StaffUser staff) {
        Kanji kanji = kanjiRepository
                .findByIdAndStatusNot(contentId, ContentStatus.DELETED)
                .orElseThrow(LearningContentException::contentNotFound);
        guardOwnership(kanji.getCreatedBy(), staff);
        guardSubmittable(kanji.getStatus() == ContentStatus.DRAFT || kanji.getStatus() == ContentStatus.REJECTED);

        if (!StringUtils.hasText(kanji.getCharacterValue()))
            throw LearningContentException.missingField("characterValue");
        if (!StringUtils.hasText(kanji.getMeaning())) throw LearningContentException.missingField("meaning");
        if (kanji.getJlptLevel() == null) throw LearningContentException.missingField("jlptLevel");
        if (!StringUtils.hasText(kanji.getOnyomi()) && !StringUtils.hasText(kanji.getKunyomi())) {
            throw LearningContentException.missingField("onyomi hoặc kunyomi");
        }

        kanji.setStatus(ContentStatus.PENDING_REVIEW);
        kanjiRepository.save(kanji);
        log.info("[INFO] Staff {} SUBMITTED kanji {}", staff.getId(), kanji.getId());
        return SubmitReviewResponse.builder()
                .contentId(kanji.getId())
                .contentType("kanji")
                .status(ContentStatus.PENDING_REVIEW.getValue())
                .build();
    }
}
