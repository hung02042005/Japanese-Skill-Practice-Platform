/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.jlpt.feature.staffcontent.learning.service.LearningContentServiceImpl;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import com.jlpt.shared.exception.ForbiddenException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Bổ sung độ phủ UC-27 cho LearningContentServiceImpl: các nhánh update/get/list và toàn bộ
 * submit-for-review (lesson/vocabulary/kanji), gồm quy tắc quyền sở hữu FR-27-06, chỉ sửa khi
 * draft/rejected (FR-27-04) và ràng buộc nội dung bài học FR-27-11/12.
 */
@ExtendWith(MockitoExtension.class)
class LearningContentServiceCoverageTest {

    private static final String STAFF_EMAIL = "staff@example.com";
    private static final String MANAGER_EMAIL = "manager@example.com";

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private StaffVocabularyRepository vocabularyRepository;

    @Mock
    private VocabularyTopicRepository vocabularyTopicRepository;

    @Mock
    private StaffKanjiRepository kanjiRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @InjectMocks
    private LearningContentServiceImpl service;

    private StaffUser staff;
    private StaffUser manager;

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder()
                .id(1L)
                .email(STAFF_EMAIL)
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
        manager = StaffUser.builder()
                .id(2L)
                .email(MANAGER_EMAIL)
                .staffRole(StaffUser.StaffRole.STAFF_MANAGER)
                .build();
    }

    private void stubStaff() {
        when(staffUserRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
    }

    private void stubManager() {
        when(staffUserRepository.findByEmail(MANAGER_EMAIL)).thenReturn(Optional.of(manager));
    }

    private Lesson lesson(LessonStatus status, StaffUser owner) {
        return Lesson.builder()
                .id(10L)
                .title("Bài 1")
                .lessonType(LessonType.LESSON)
                .jlptLevel(JlptLevel.N5)
                .contentText("Nội dung bài học")
                .status(status)
                .displayOrder(1)
                .createdBy(owner)
                .build();
    }

    private Vocabulary vocabulary(ContentStatus status, StaffUser owner) {
        return Vocabulary.builder()
                .id(100L)
                .word("単語")
                .furigana("たんご")
                .meaning("Từ vựng")
                .jlptLevel(JlptLevel.N5)
                .status(status)
                .createdBy(owner)
                .build();
    }

    private Kanji kanji(ContentStatus status, StaffUser owner) {
        return Kanji.builder()
                .id(200L)
                .characterValue("日")
                .meaning("Mặt trời")
                .onyomi("ニチ")
                .kunyomi("ひ")
                .strokeCount(4)
                .jlptLevel(JlptLevel.N5)
                .status(status)
                .createdBy(owner)
                .build();
    }

    private VocabularyTopic topic(JlptLevel level) {
        return VocabularyTopic.builder()
                .id(5L)
                .jlptLevel(level)
                .slug("gia-dinh")
                .titleVi("Gia đình")
                .build();
    }

    private UpdateLessonRequest lessonRequest() {
        UpdateLessonRequest req = new UpdateLessonRequest();
        req.setTitle("  Bài học mới  ");
        req.setJlptLevel("N5");
        req.setLessonType("lesson");
        req.setContentText("  Nội dung  ");
        return req;
    }

    // ── resolveStaff ─────────────────────────────────────────────────────────

    @Test
    void unknownStaffEmail_throwsForbidden() {
        when(staffUserRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.getKanji(200L, "ghost@example.com"));
    }

    // ── updateLesson ─────────────────────────────────────────────────────────

    @Test
    void updateLesson_trimsFieldsAndDefaultsDisplayOrderToZero() {
        stubStaff();
        Lesson existing = lesson(LessonStatus.DRAFT, staff);
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED)).thenReturn(Optional.of(existing));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));
        UpdateLessonRequest req = lessonRequest();
        req.setExplanation("   ");
        req.setVideoUrl("  https://cdn/v.mp4  ");

        LessonDetailResponse response = service.updateLesson(10L, req, STAFF_EMAIL);

        assertEquals("Bài học mới", response.getTitle());
        assertEquals("Nội dung", response.getContentText());
        assertEquals("https://cdn/v.mp4", response.getVideoUrl());
        assertNull(response.getExplanation());
        assertEquals(0, response.getDisplayOrder());
        assertEquals("lesson", response.getLessonType());
    }

    @Test
    void updateLesson_keepsProvidedDisplayOrder() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.REJECTED, staff)));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));
        UpdateLessonRequest req = lessonRequest();
        req.setDisplayOrder(7);

        assertEquals(7, service.updateLesson(10L, req, STAFF_EMAIL).getDisplayOrder());
    }

    @Test
    void updateLesson_publishedLesson_isRejected() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.PUBLISHED, staff)));
        UpdateLessonRequest req = lessonRequest();

        assertThrows(LearningContentException.class, () -> service.updateLesson(10L, req, STAFF_EMAIL));
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void updateLesson_missing_throwsLessonNotFound() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(404L, LessonStatus.DELETED)).thenReturn(Optional.empty());
        UpdateLessonRequest req = lessonRequest();

        assertThrows(LearningContentException.class, () -> service.updateLesson(404L, req, STAFF_EMAIL));
    }

    @Test
    void updateLesson_invalidJlptLevel_throwsInvalidJlptLevel() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        UpdateLessonRequest req = lessonRequest();
        req.setJlptLevel("N9");

        assertThrows(LearningContentException.class, () -> service.updateLesson(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateLesson_nullJlptLevel_throwsInvalidJlptLevel() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        UpdateLessonRequest req = lessonRequest();
        req.setJlptLevel(null);

        assertThrows(LearningContentException.class, () -> service.updateLesson(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateLesson_invalidLessonType_throwsInvalidLessonType() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        UpdateLessonRequest req = lessonRequest();
        req.setLessonType("podcast");

        assertThrows(LearningContentException.class, () -> service.updateLesson(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateLesson_noContentAtAll_throwsLessonContentRequired() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        UpdateLessonRequest req = lessonRequest();
        req.setContentText(null);

        assertThrows(LearningContentException.class, () -> service.updateLesson(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateLesson_listeningWithoutAudio_throwsLessonContentRequired() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        UpdateLessonRequest req = lessonRequest();
        req.setLessonType("listening");

        assertThrows(LearningContentException.class, () -> service.updateLesson(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateLesson_listeningWithAudio_isAccepted() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));
        UpdateLessonRequest req = lessonRequest();
        req.setLessonType("listening");
        req.setContentText(null);
        req.setAudioUrl("https://cdn/a.mp3");

        LessonDetailResponse response = service.updateLesson(10L, req, STAFF_EMAIL);

        assertEquals("listening", response.getLessonType());
        assertEquals("https://cdn/a.mp3", response.getAudioUrl());
    }

    @Test
    void updateLesson_attachmentOnly_satisfiesContentRequirement() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));
        UpdateLessonRequest req = lessonRequest();
        req.setContentText(null);
        req.setAttachmentUrl("https://cdn/f.pdf");

        assertEquals(
                "https://cdn/f.pdf", service.updateLesson(10L, req, STAFF_EMAIL).getAttachmentUrl());
    }

    @Test
    void updateLesson_asManagerOnOtherStaffContent_isAllowed() {
        stubManager();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(
                "Bài học mới",
                service.updateLesson(10L, lessonRequest(), MANAGER_EMAIL).getTitle());
    }

    @Test
    void updateLesson_contentWithoutOwner_throwsOwnershipDenied() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, null)));
        UpdateLessonRequest req = lessonRequest();

        assertThrows(LearningContentException.class, () -> service.updateLesson(10L, req, STAFF_EMAIL));
    }

    // ── createVocabulary ─────────────────────────────────────────────────────

    private CreateVocabularyRequest createVocabRequest() {
        CreateVocabularyRequest req = new CreateVocabularyRequest();
        req.setWord("  単語  ");
        req.setFurigana("  たんご  ");
        req.setMeaning("  Từ vựng  ");
        req.setJlptLevel("N5");
        req.setTopicId(5L);
        return req;
    }

    @Test
    void createVocabulary_withLesson_attachesLessonAndTopicMetadata() {
        stubStaff();
        when(vocabularyTopicRepository.findById(5L)).thenReturn(Optional.of(topic(JlptLevel.N5)));
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(i -> i.getArgument(0));
        CreateVocabularyRequest req = createVocabRequest();
        req.setLessonId(10L);
        req.setWordType("  danh từ  ");

        VocabularyDetailResponse response = service.createVocabulary(req, STAFF_EMAIL);

        assertEquals("単語", response.getWord());
        assertEquals("danh từ", response.getWordType());
        assertEquals(5L, response.getTopicId());
        assertEquals("Gia đình", response.getTopicTitle());
        assertEquals("gia-dinh", response.getTopicSlug());
        assertEquals(10L, response.getLessonId());
        assertEquals("draft", response.getStatus());
    }

    @Test
    void createVocabulary_missingTopicId_throwsMissingField() {
        stubStaff();
        CreateVocabularyRequest req = createVocabRequest();
        req.setTopicId(null);

        assertThrows(LearningContentException.class, () -> service.createVocabulary(req, STAFF_EMAIL));
    }

    @Test
    void createVocabulary_unknownTopic_throwsContentNotFound() {
        stubStaff();
        when(vocabularyTopicRepository.findById(5L)).thenReturn(Optional.empty());
        CreateVocabularyRequest req = createVocabRequest();

        assertThrows(LearningContentException.class, () -> service.createVocabulary(req, STAFF_EMAIL));
    }

    @Test
    void createVocabulary_topicOfDifferentLevel_throwsValidationFailed() {
        stubStaff();
        when(vocabularyTopicRepository.findById(5L)).thenReturn(Optional.of(topic(JlptLevel.N3)));
        CreateVocabularyRequest req = createVocabRequest();

        assertThrows(LearningContentException.class, () -> service.createVocabulary(req, STAFF_EMAIL));
    }

    @Test
    void createVocabulary_unknownLesson_throwsLessonNotFound() {
        stubStaff();
        when(vocabularyTopicRepository.findById(5L)).thenReturn(Optional.of(topic(JlptLevel.N5)));
        when(lessonRepository.findByIdAndStatusNot(404L, LessonStatus.DELETED)).thenReturn(Optional.empty());
        CreateVocabularyRequest req = createVocabRequest();
        req.setLessonId(404L);

        assertThrows(LearningContentException.class, () -> service.createVocabulary(req, STAFF_EMAIL));
    }

    // ── updateVocabulary ─────────────────────────────────────────────────────

    @Test
    void updateVocabulary_allFieldsProvided_trimsAndSaves() {
        stubStaff();
        Vocabulary existing = vocabulary(ContentStatus.DRAFT, staff);
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        when(vocabularyTopicRepository.findById(5L)).thenReturn(Optional.of(topic(JlptLevel.N4)));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(i -> i.getArgument(0));
        UpdateVocabularyRequest req = new UpdateVocabularyRequest();
        req.setWord("  新語  ");
        req.setFurigana("  しんご  ");
        req.setMeaning("  Từ mới  ");
        req.setWordType("  động từ  ");
        req.setJlptLevel("N4");
        req.setTopicId(5L);
        req.setAudioUrl("  https://cdn/w.mp3  ");
        req.setExampleSentenceJp("  例文  ");
        req.setExampleSentenceVi("  Câu ví dụ  ");

        VocabularyDetailResponse response = service.updateVocabulary(100L, req, STAFF_EMAIL);

        assertEquals("新語", response.getWord());
        assertEquals("しんご", response.getFurigana());
        assertEquals("Từ mới", response.getMeaning());
        assertEquals("động từ", response.getWordType());
        assertEquals("N4", response.getJlptLevel());
        assertEquals("https://cdn/w.mp3", response.getAudioUrl());
        assertEquals("例文", response.getExampleSentenceJp());
        assertEquals("Câu ví dụ", response.getExampleSentenceVi());
    }

    @Test
    void updateVocabulary_emptyRequest_keepsExistingValues() {
        stubStaff();
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(vocabulary(ContentStatus.DRAFT, staff)));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(i -> i.getArgument(0));

        VocabularyDetailResponse response = service.updateVocabulary(100L, new UpdateVocabularyRequest(), STAFF_EMAIL);

        assertEquals("単語", response.getWord());
        assertEquals("N5", response.getJlptLevel());
        assertNull(response.getTopicId());
    }

    @Test
    void updateVocabulary_clearLesson_detachesLesson() {
        stubStaff();
        Vocabulary existing = vocabulary(ContentStatus.DRAFT, staff);
        existing.setLesson(lesson(LessonStatus.DRAFT, staff));
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(i -> i.getArgument(0));
        UpdateVocabularyRequest req = new UpdateVocabularyRequest();
        req.setClearLesson(true);

        assertNull(service.updateVocabulary(100L, req, STAFF_EMAIL).getLessonId());
    }

    @Test
    void updateVocabulary_newLesson_isAttached() {
        stubStaff();
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(vocabulary(ContentStatus.DRAFT, staff)));
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.DRAFT, staff)));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(i -> i.getArgument(0));
        UpdateVocabularyRequest req = new UpdateVocabularyRequest();
        req.setLessonId(10L);

        assertEquals(10L, service.updateVocabulary(100L, req, STAFF_EMAIL).getLessonId());
    }

    @Test
    void updateVocabulary_publishedContent_isRejected() {
        stubStaff();
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(vocabulary(ContentStatus.PUBLISHED, staff)));
        UpdateVocabularyRequest req = new UpdateVocabularyRequest();

        assertThrows(LearningContentException.class, () -> service.updateVocabulary(100L, req, STAFF_EMAIL));
    }

    @Test
    void updateVocabulary_missing_throwsContentNotFound() {
        stubStaff();
        when(vocabularyRepository.findByIdAndStatusNot(404L, ContentStatus.DELETED))
                .thenReturn(Optional.empty());
        UpdateVocabularyRequest req = new UpdateVocabularyRequest();

        assertThrows(LearningContentException.class, () -> service.updateVocabulary(404L, req, STAFF_EMAIL));
    }

    // ── createKanji / updateKanji ────────────────────────────────────────────

    private CreateKanjiRequest createKanjiRequest() {
        CreateKanjiRequest req = new CreateKanjiRequest();
        req.setCharacterValue("  月  ");
        req.setMeaning("  Mặt trăng  ");
        req.setOnyomi("  ゲツ  ");
        req.setJlptLevel("N5");
        req.setStrokeCount(4);
        return req;
    }

    @Test
    void createKanji_onlyKunyomi_isAccepted() {
        stubStaff();
        when(kanjiRepository.existsByCharacterValue("月")).thenReturn(false);
        when(kanjiRepository.save(any(Kanji.class))).thenAnswer(i -> i.getArgument(0));
        CreateKanjiRequest req = createKanjiRequest();
        req.setOnyomi(null);
        req.setKunyomi("つき");
        req.setStrokeOrderUrl("  https://cdn/s.svg  ");
        req.setExampleWord("  月曜日  ");
        req.setExampleReading("  げつようび  ");
        req.setExampleMeaning("  Thứ hai  ");

        KanjiDetailResponse response = service.createKanji(req, STAFF_EMAIL);

        assertEquals("月", response.getCharacterValue());
        assertNull(response.getOnyomi());
        assertEquals("つき", response.getKunyomi());
        assertEquals("https://cdn/s.svg", response.getStrokeOrderUrl());
        assertEquals("月曜日", response.getExampleWord());
        assertEquals("Thứ hai", response.getExampleMeaning());
        assertEquals("draft", response.getStatus());
    }

    @Test
    void createKanji_neitherOnyomiNorKunyomi_throwsMissingField() {
        stubStaff();
        CreateKanjiRequest req = createKanjiRequest();
        req.setOnyomi("  ");
        req.setKunyomi(null);

        assertThrows(LearningContentException.class, () -> service.createKanji(req, STAFF_EMAIL));
        verify(kanjiRepository, never()).save(any());
    }

    @Test
    void createKanji_invalidJlptLevel_throwsInvalidJlptLevel() {
        stubStaff();
        CreateKanjiRequest req = createKanjiRequest();
        req.setJlptLevel("N9");

        assertThrows(LearningContentException.class, () -> service.createKanji(req, STAFF_EMAIL));
    }

    @Test
    void updateKanji_allFieldsProvided_trimsAndSaves() {
        stubStaff();
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED))
                .thenReturn(Optional.of(kanji(ContentStatus.DRAFT, staff)));
        when(kanjiRepository.existsByCharacterValue("月")).thenReturn(false);
        when(kanjiRepository.save(any(Kanji.class))).thenAnswer(i -> i.getArgument(0));
        UpdateKanjiRequest req = new UpdateKanjiRequest();
        req.setCharacterValue("  月  ");
        req.setMeaning("  Mặt trăng  ");
        req.setOnyomi("  ゲツ  ");
        req.setKunyomi("  つき  ");
        req.setStrokeCount(4);
        req.setJlptLevel("N4");
        req.setStrokeOrderUrl("  https://cdn/s.svg  ");
        req.setExampleWord("  月曜日  ");
        req.setExampleReading("  げつようび  ");
        req.setExampleMeaning("  Thứ hai  ");

        KanjiDetailResponse response = service.updateKanji(200L, req, STAFF_EMAIL);

        assertEquals("月", response.getCharacterValue());
        assertEquals("Mặt trăng", response.getMeaning());
        assertEquals("N4", response.getJlptLevel());
        assertEquals(4, response.getStrokeCount());
        assertEquals("げつようび", response.getExampleReading());
    }

    @Test
    void updateKanji_emptyRequest_keepsExistingValues() {
        stubStaff();
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED))
                .thenReturn(Optional.of(kanji(ContentStatus.DRAFT, staff)));
        when(kanjiRepository.save(any(Kanji.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(
                "日",
                service.updateKanji(200L, new UpdateKanjiRequest(), STAFF_EMAIL).getCharacterValue());
    }

    @Test
    void updateKanji_sameCharacterValue_skipsDuplicateCheck() {
        stubStaff();
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED))
                .thenReturn(Optional.of(kanji(ContentStatus.DRAFT, staff)));
        when(kanjiRepository.save(any(Kanji.class))).thenAnswer(i -> i.getArgument(0));
        UpdateKanjiRequest req = new UpdateKanjiRequest();
        req.setCharacterValue("日");

        service.updateKanji(200L, req, STAFF_EMAIL);

        verify(kanjiRepository, never()).existsByCharacterValue(any());
    }

    @Test
    void updateKanji_changingToExistingCharacter_throwsDuplicate() {
        stubStaff();
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED))
                .thenReturn(Optional.of(kanji(ContentStatus.DRAFT, staff)));
        when(kanjiRepository.existsByCharacterValue("月")).thenReturn(true);
        UpdateKanjiRequest req = new UpdateKanjiRequest();
        req.setCharacterValue("月");

        assertThrows(LearningContentException.class, () -> service.updateKanji(200L, req, STAFF_EMAIL));
    }

    @Test
    void updateKanji_clearingBothReadings_throwsMissingField() {
        stubStaff();
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED))
                .thenReturn(Optional.of(kanji(ContentStatus.DRAFT, staff)));
        UpdateKanjiRequest req = new UpdateKanjiRequest();
        req.setOnyomi("  ");
        req.setKunyomi("  ");

        assertThrows(LearningContentException.class, () -> service.updateKanji(200L, req, STAFF_EMAIL));
        verify(kanjiRepository, never()).save(any());
    }

    @Test
    void updateKanji_publishedContent_isRejected() {
        stubStaff();
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED))
                .thenReturn(Optional.of(kanji(ContentStatus.PUBLISHED, staff)));
        UpdateKanjiRequest req = new UpdateKanjiRequest();

        assertThrows(LearningContentException.class, () -> service.updateKanji(200L, req, STAFF_EMAIL));
    }

    @Test
    void updateKanji_missing_throwsContentNotFound() {
        stubStaff();
        when(kanjiRepository.findByIdAndStatusNot(404L, ContentStatus.DELETED)).thenReturn(Optional.empty());
        UpdateKanjiRequest req = new UpdateKanjiRequest();

        assertThrows(LearningContentException.class, () -> service.updateKanji(404L, req, STAFF_EMAIL));
    }

    // ── submitForReview ──────────────────────────────────────────────────────

    private SubmitReviewRequest submitRequest(String contentType, Long contentId) {
        SubmitReviewRequest req = new SubmitReviewRequest();
        req.setContentType(contentType);
        req.setContentId(contentId);
        return req;
    }

    @Test
    void submitForReview_unsupportedType_throwsInvalidContentType() {
        stubStaff();
        SubmitReviewRequest req = submitRequest("podcast", 1L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_lesson_setsPendingReview() {
        stubStaff();
        Lesson existing = lesson(LessonStatus.DRAFT, staff);
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED)).thenReturn(Optional.of(existing));

        SubmitReviewResponse response = service.submitForReview(submitRequest("LESSON", 10L), STAFF_EMAIL);

        assertEquals("lesson", response.getContentType());
        assertEquals("pending_review", response.getStatus());
        assertEquals(LessonStatus.PENDING_REVIEW, existing.getStatus());
        verify(lessonRepository).save(existing);
    }

    @Test
    void submitForReview_lessonAlreadyPending_isRejected() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(LessonStatus.PENDING_REVIEW, staff)));
        SubmitReviewRequest req = submitRequest("lesson", 10L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_lessonWithoutTitle_throwsMissingField() {
        stubStaff();
        Lesson existing = lesson(LessonStatus.DRAFT, staff);
        existing.setTitle("  ");
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED)).thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("lesson", 10L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_lessonWithoutType_throwsMissingField() {
        stubStaff();
        Lesson existing = lesson(LessonStatus.DRAFT, staff);
        existing.setLessonType(null);
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED)).thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("lesson", 10L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_lessonWithoutLevel_throwsMissingField() {
        stubStaff();
        Lesson existing = lesson(LessonStatus.DRAFT, staff);
        existing.setJlptLevel(null);
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED)).thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("lesson", 10L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_lessonWithoutContent_throwsLessonContentRequired() {
        stubStaff();
        Lesson existing = lesson(LessonStatus.DRAFT, staff);
        existing.setContentText(null);
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED)).thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("lesson", 10L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_lessonMissing_throwsContentNotFound() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(404L, LessonStatus.DELETED)).thenReturn(Optional.empty());
        SubmitReviewRequest req = submitRequest("lesson", 404L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_vocabulary_setsPendingReview() {
        stubStaff();
        Vocabulary existing = vocabulary(ContentStatus.REJECTED, staff);
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));

        SubmitReviewResponse response = service.submitForReview(submitRequest("vocabulary", 100L), STAFF_EMAIL);

        assertEquals("vocabulary", response.getContentType());
        assertEquals(ContentStatus.PENDING_REVIEW, existing.getStatus());
    }

    @Test
    void submitForReview_vocabularyWithoutWord_throwsMissingField() {
        stubStaff();
        Vocabulary existing = vocabulary(ContentStatus.DRAFT, staff);
        existing.setWord(" ");
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("vocabulary", 100L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_vocabularyWithoutFurigana_throwsMissingField() {
        stubStaff();
        Vocabulary existing = vocabulary(ContentStatus.DRAFT, staff);
        existing.setFurigana(null);
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("vocabulary", 100L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_vocabularyWithoutMeaning_throwsMissingField() {
        stubStaff();
        Vocabulary existing = vocabulary(ContentStatus.DRAFT, staff);
        existing.setMeaning(null);
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("vocabulary", 100L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_vocabularyWithoutLevel_throwsMissingField() {
        stubStaff();
        Vocabulary existing = vocabulary(ContentStatus.DRAFT, staff);
        existing.setJlptLevel(null);
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("vocabulary", 100L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_vocabularyPublished_isRejected() {
        stubStaff();
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(vocabulary(ContentStatus.PUBLISHED, staff)));
        SubmitReviewRequest req = submitRequest("vocabulary", 100L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_kanji_setsPendingReview() {
        stubStaff();
        Kanji existing = kanji(ContentStatus.DRAFT, staff);
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED)).thenReturn(Optional.of(existing));

        SubmitReviewResponse response = service.submitForReview(submitRequest("kanji", 200L), STAFF_EMAIL);

        assertEquals("kanji", response.getContentType());
        assertEquals(ContentStatus.PENDING_REVIEW, existing.getStatus());
    }

    @Test
    void submitForReview_kanjiWithoutCharacter_throwsMissingField() {
        stubStaff();
        Kanji existing = kanji(ContentStatus.DRAFT, staff);
        existing.setCharacterValue(" ");
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED)).thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("kanji", 200L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_kanjiWithoutMeaning_throwsMissingField() {
        stubStaff();
        Kanji existing = kanji(ContentStatus.DRAFT, staff);
        existing.setMeaning(null);
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED)).thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("kanji", 200L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_kanjiWithoutLevel_throwsMissingField() {
        stubStaff();
        Kanji existing = kanji(ContentStatus.DRAFT, staff);
        existing.setJlptLevel(null);
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED)).thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("kanji", 200L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_kanjiWithoutAnyReading_throwsMissingField() {
        stubStaff();
        Kanji existing = kanji(ContentStatus.DRAFT, staff);
        existing.setOnyomi(null);
        existing.setKunyomi("  ");
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED)).thenReturn(Optional.of(existing));
        SubmitReviewRequest req = submitRequest("kanji", 200L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    @Test
    void submitForReview_kanjiOfOtherStaff_throwsOwnershipDenied() {
        stubStaff();
        StaffUser other =
                StaffUser.builder().id(77L).staffRole(StaffUser.StaffRole.STAFF).build();
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED))
                .thenReturn(Optional.of(kanji(ContentStatus.DRAFT, other)));
        SubmitReviewRequest req = submitRequest("kanji", 200L);

        assertThrows(LearningContentException.class, () -> service.submitForReview(req, STAFF_EMAIL));
    }

    // ── GET endpoints ────────────────────────────────────────────────────────

    @Test
    void listLessons_invalidFilters_areIgnoredAndPageSizeCappedAt100() {
        stubStaff();
        when(lessonRepository.findByCreatedByWithFilters(
                        eq(1L), isNull(), isNull(), isNull(), eq(LessonStatus.DELETED), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lesson(LessonStatus.DRAFT, staff))));

        assertEquals(
                1,
                service.listLessons("  ", "N9", "podcast", "khong-ton-tai", 0, 500, STAFF_EMAIL)
                        .getTotalElements());
        verify(lessonRepository)
                .findByCreatedByWithFilters(
                        eq(1L),
                        isNull(),
                        isNull(),
                        isNull(),
                        eq(LessonStatus.DELETED),
                        isNull(),
                        argThat(p -> p.getPageSize() == 100));
    }

    @Test
    void listLessons_validFilters_arePassedThrough() {
        stubStaff();
        when(lessonRepository.findByCreatedByWithFilters(
                        eq(1L),
                        eq(JlptLevel.N5),
                        eq(LessonType.LISTENING),
                        eq(LessonStatus.DRAFT),
                        eq(LessonStatus.DELETED),
                        eq("nghe"),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lesson(LessonStatus.DRAFT, staff))));

        assertEquals(
                1,
                service.listLessons("nghe", "N5", "listening", "draft", 0, 20, STAFF_EMAIL)
                        .getTotalElements());
    }

    // Endpoint getLesson đã bị gỡ khỏi LearningContentService (nhánh branch_for_hung, các commit
    // "Delete màn tạo Lession" / "Update code delete lession") nên 2 test getLesson_* tương ứng
    // đã được xoá. Quyền sở hữu khi đọc chi tiết vẫn được phủ qua getVocabulary/getKanji bên dưới.

    @Test
    void listVocabulary_validFilters_arePassedThrough() {
        stubStaff();
        when(vocabularyRepository.findByCreatedByWithFilters(
                        eq(1L),
                        eq(JlptLevel.N5),
                        eq(5L),
                        eq(ContentStatus.DRAFT),
                        eq(ContentStatus.DELETED),
                        eq("tu"),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(vocabulary(ContentStatus.DRAFT, staff))));

        assertEquals(
                1,
                service.listVocabulary("tu", "N5", 5L, "draft", 0, 20, STAFF_EMAIL)
                        .getTotalElements());
    }

    @Test
    void listVocabulary_invalidFilters_areIgnored() {
        stubStaff();
        when(vocabularyRepository.findByCreatedByWithFilters(
                        eq(1L), isNull(), isNull(), isNull(), eq(ContentStatus.DELETED), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertTrue(service.listVocabulary(null, "N9", null, "khong-ton-tai", 0, 20, STAFF_EMAIL)
                .isEmpty());
    }

    @Test
    void getVocabulary_ownedByRequester_returnsDetail() {
        stubStaff();
        when(vocabularyRepository.findByIdAndStatusNot(100L, ContentStatus.DELETED))
                .thenReturn(Optional.of(vocabulary(ContentStatus.DRAFT, staff)));

        assertEquals(100L, service.getVocabulary(100L, STAFF_EMAIL).getVocabularyId());
    }

    @Test
    void getVocabulary_missing_throwsContentNotFound() {
        stubStaff();
        when(vocabularyRepository.findByIdAndStatusNot(404L, ContentStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThrows(LearningContentException.class, () -> service.getVocabulary(404L, STAFF_EMAIL));
    }

    @Test
    void listKanji_validFilters_arePassedThrough() {
        stubStaff();
        when(kanjiRepository.findByCreatedByWithFilters(
                        eq(1L),
                        eq(JlptLevel.N5),
                        eq(ContentStatus.DRAFT),
                        eq(ContentStatus.DELETED),
                        eq("nhat"),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(kanji(ContentStatus.DRAFT, staff))));

        assertEquals(
                1, service.listKanji("nhat", "N5", "draft", 0, 20, STAFF_EMAIL).getTotalElements());
    }

    @Test
    void listKanji_invalidFilters_areIgnored() {
        stubStaff();
        when(kanjiRepository.findByCreatedByWithFilters(
                        eq(1L), isNull(), isNull(), eq(ContentStatus.DELETED), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertTrue(service.listKanji(null, "N9", "khong-ton-tai", 0, 20, STAFF_EMAIL)
                .isEmpty());
    }

    @Test
    void getKanji_asManager_bypassesOwnership() {
        stubManager();
        when(kanjiRepository.findByIdAndStatusNot(200L, ContentStatus.DELETED))
                .thenReturn(Optional.of(kanji(ContentStatus.DRAFT, staff)));

        assertEquals(200L, service.getKanji(200L, MANAGER_EMAIL).getKanjiId());
    }

    @Test
    void getKanji_missing_throwsContentNotFound() {
        stubStaff();
        when(kanjiRepository.findByIdAndStatusNot(404L, ContentStatus.DELETED)).thenReturn(Optional.empty());

        assertThrows(LearningContentException.class, () -> service.getKanji(404L, STAFF_EMAIL));
    }
}
