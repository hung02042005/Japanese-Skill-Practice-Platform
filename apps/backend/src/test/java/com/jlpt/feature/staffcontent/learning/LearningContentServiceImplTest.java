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
import com.jlpt.feature.staffcontent.learning.dto.*;
import com.jlpt.feature.staffcontent.learning.exception.LearningContentException;
import com.jlpt.feature.staffcontent.learning.repository.StaffKanjiRepository;
import com.jlpt.feature.staffcontent.learning.repository.StaffVocabularyRepository;
import com.jlpt.feature.staffcontent.learning.service.LearningContentServiceImpl;
import com.jlpt.feature.student.StudentUser.JlptLevel;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class LearningContentServiceImplTest {

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

    @BeforeEach
    void setUp() {
        staff = StaffUser.builder()
                .id(1L)
                .email("staff@example.com")
                .staffRole(StaffUser.StaffRole.STAFF)
                .build();
    }

    @Test
    void updateLesson_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        Lesson lesson = Lesson.builder()
                .id(10L)
                .title("Old Title")
                .status(LessonStatus.DRAFT)
                .createdBy(staff)
                .build();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));

        UpdateLessonRequest req = new UpdateLessonRequest();
        req.setTitle("New Title");
        req.setJlptLevel("N5");
        req.setLessonType("LESSON");
        req.setContentText("Content");

        LessonDetailResponse res = service.updateLesson(10L, req, "staff@example.com");
        assertEquals("New Title", res.getTitle());
        assertEquals("N5", res.getJlptLevel());
    }

    @Test
    void updateLesson_forbiddenStaff() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        StaffUser otherStaff = StaffUser.builder().id(99L).build();
        Lesson lesson = Lesson.builder()
                .id(10L)
                .status(LessonStatus.DRAFT)
                .createdBy(otherStaff)
                .build();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED)).thenReturn(Optional.of(lesson));

        UpdateLessonRequest req = new UpdateLessonRequest();
        assertThrows(LearningContentException.class, () -> service.updateLesson(10L, req, "staff@example.com"));
    }

    @Test
    void createVocabulary_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        VocabularyTopic topic =
                VocabularyTopic.builder().id(5L).jlptLevel(JlptLevel.N5).build();
        when(vocabularyTopicRepository.findById(5L)).thenReturn(Optional.of(topic));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(i -> {
            Vocabulary v = i.getArgument(0);
            v.setId(100L);
            return v;
        });

        CreateVocabularyRequest req = new CreateVocabularyRequest();
        req.setWord("単語");
        req.setFurigana("たんご");
        req.setMeaning("Tu vung");
        req.setJlptLevel("N5");
        req.setTopicId(5L);

        VocabularyDetailResponse res = service.createVocabulary(req, "staff@example.com");
        assertEquals(100L, res.getVocabularyId());
        assertEquals("単語", res.getWord());
    }

    @Test
    void createKanji_duplicateCharacter() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(kanjiRepository.existsByCharacterValue("日")).thenReturn(true);

        CreateKanjiRequest req = new CreateKanjiRequest();
        req.setCharacterValue("日");
        req.setMeaning("Sun");
        req.setOnyomi("NICH");
        req.setJlptLevel("N5");

        assertThrows(LearningContentException.class, () -> service.createKanji(req, "staff@example.com"));
    }

    @Test
    void createKanji_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        when(kanjiRepository.existsByCharacterValue("日")).thenReturn(false);
        when(kanjiRepository.save(any(Kanji.class))).thenAnswer(i -> {
            Kanji k = i.getArgument(0);
            k.setId(200L);
            return k;
        });

        CreateKanjiRequest req = new CreateKanjiRequest();
        req.setCharacterValue("日");
        req.setMeaning("Sun");
        req.setOnyomi("NICHI");
        req.setJlptLevel("N5");

        KanjiDetailResponse res = service.createKanji(req, "staff@example.com");
        assertEquals(200L, res.getKanjiId());
        assertEquals("日", res.getCharacterValue());
    }

    @Test
    void submitForReview_lessonSuccess() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        Lesson lesson = Lesson.builder()
                .id(10L)
                .title("Title")
                .lessonType(LessonType.LESSON)
                .jlptLevel(JlptLevel.N5)
                .contentText("Content")
                .status(LessonStatus.DRAFT)
                .createdBy(staff)
                .build();
        when(lessonRepository.findByIdAndStatusNot(10L, LessonStatus.DELETED)).thenReturn(Optional.of(lesson));

        SubmitReviewRequest req = new SubmitReviewRequest();
        req.setContentType("lesson");
        req.setContentId(10L);

        SubmitReviewResponse res = service.submitForReview(req, "staff@example.com");
        assertEquals("pending_review", res.getStatus());
    }

    @Test
    void listLessons_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        Lesson lesson =
                Lesson.builder().id(1L).title("Test").status(LessonStatus.DRAFT).build();
        Page<Lesson> page = new PageImpl<>(List.of(lesson));
        when(lessonRepository.findByCreatedByWithFilters(
                        anyLong(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<LessonDetailResponse> res = service.listLessons("q", "N5", "LESSON", "DRAFT", 0, 10, "staff@example.com");
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void listVocabulary_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        Vocabulary vocab = Vocabulary.builder()
                .id(1L)
                .word("Word")
                .status(ContentStatus.DRAFT)
                .build();
        Page<Vocabulary> page = new PageImpl<>(List.of(vocab));
        when(vocabularyRepository.findByCreatedByWithFilters(
                        anyLong(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<VocabularyDetailResponse> res = service.listVocabulary("q", "N5", 1L, "DRAFT", 0, 10, "staff@example.com");
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void listKanji_success() {
        when(staffUserRepository.findByEmail("staff@example.com")).thenReturn(Optional.of(staff));
        Kanji kanji = Kanji.builder()
                .id(1L)
                .characterValue("水")
                .status(ContentStatus.DRAFT)
                .build();
        Page<Kanji> page = new PageImpl<>(List.of(kanji));
        when(kanjiRepository.findByCreatedByWithFilters(anyLong(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<KanjiDetailResponse> res = service.listKanji("q", "N5", "DRAFT", 0, 10, "staff@example.com");
        assertEquals(1, res.getTotalElements());
    }
}
