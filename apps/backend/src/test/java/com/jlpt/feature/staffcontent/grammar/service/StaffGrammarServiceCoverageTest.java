/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.grammar.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.Kanji.ContentStatus;
import com.jlpt.feature.learning.Lesson;
import com.jlpt.feature.learning.LessonRepository;
import com.jlpt.feature.staff.StaffUser;
import com.jlpt.feature.staff.StaffUserRepository;
import com.jlpt.feature.staffcontent.grammar.dto.CreateGrammarRequest;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarDetailResponse;
import com.jlpt.feature.staffcontent.grammar.dto.GrammarSummaryResponse;
import com.jlpt.feature.staffcontent.grammar.dto.UpdateGrammarRequest;
import com.jlpt.feature.staffcontent.grammar.exception.GrammarBusinessException;
import com.jlpt.feature.staffcontent.grammar.repository.StaffGrammarRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Bổ sung độ phủ UC-25 cho StaffGrammarServiceImpl: listGrammars/getGrammar/updateGrammar, gắn bài
 * học (level phải khớp), và quy tắc quyền sở hữu — Staff chỉ sửa được nội dung mình tạo, Staff
 * Manager thì không giới hạn.
 */
@ExtendWith(MockitoExtension.class)
class StaffGrammarServiceCoverageTest {

    private static final String STAFF_EMAIL = "staff@test.com";
    private static final String MANAGER_EMAIL = "manager@test.com";

    @Mock
    private StaffGrammarRepository grammarRepository;

    @Mock
    private StaffUserRepository staffUserRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private StaffGrammarServiceImpl service;

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

    private GrammarPoint grammar(ContentStatus status, StaffUser owner) {
        return GrammarPoint.builder()
                .id(10L)
                .title("V-てから")
                .structure("V-てから")
                .meaning("Sau khi làm V")
                .usageExplanation("Diễn tả trình tự hành động")
                .exampleSentenceJp("ご飯を食べてから、歯を磨きます。")
                .jlptLevel(JlptLevel.N5)
                .status(status)
                .createdBy(owner)
                .build();
    }

    private Lesson lesson(JlptLevel level) {
        return Lesson.builder()
                .id(50L)
                .title("Bài 1")
                .jlptLevel(level)
                .status(Lesson.LessonStatus.DRAFT)
                .build();
    }

    private CreateGrammarRequest createRequest() {
        CreateGrammarRequest req = new CreateGrammarRequest();
        req.setTitle("V-てから");
        req.setStructure("  V-てから  ");
        req.setMeaning("  Sau khi làm V  ");
        req.setUsageExplanation("  Trình tự hành động  ");
        req.setJlptLevel("N5");
        req.setExampleSentenceJp("  ご飯を食べてから、歯を磨きます。  ");
        return req;
    }

    // ── resolveStaff ─────────────────────────────────────────────────────────

    @Test
    void anyOperation_unknownStaffEmail_throwsForbidden() {
        when(staffUserRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
        CreateGrammarRequest req = createRequest();

        assertThrows(ForbiddenException.class, () -> service.createGrammar(req, "ghost@test.com"));
    }

    // ── createGrammar ────────────────────────────────────────────────────────

    @Test
    void createGrammar_trimsTextAndNormalizesBlankOptionalFieldsToNull() {
        stubStaff();
        when(grammarRepository.save(any())).thenAnswer(i -> {
            GrammarPoint g = i.getArgument(0);
            g.setId(10L);
            return g;
        });
        CreateGrammarRequest req = createRequest();
        req.setFormula("   ");
        req.setExampleSentenceVi("   ");

        GrammarDetailResponse response = service.createGrammar(req, STAFF_EMAIL);

        assertEquals("V-てから", response.getStructure());
        assertEquals("Sau khi làm V", response.getMeaning());
        assertNull(response.getFormula());
        assertNull(response.getExampleSentenceVi());
        assertEquals("draft", response.getStatus());
        assertEquals(1L, response.getCreatedBy());
        assertNull(response.getLesson());
    }

    @Test
    void createGrammar_withMatchingLesson_attachesLessonRef() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(50L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(JlptLevel.N5)));
        when(grammarRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        CreateGrammarRequest req = createRequest();
        req.setLessonId(50L);
        req.setFormula("V + てから");
        req.setExampleSentenceVi("Sau khi ăn cơm, tôi đánh răng.");

        GrammarDetailResponse response = service.createGrammar(req, STAFF_EMAIL);

        assertNotNull(response.getLesson());
        assertEquals(50L, response.getLesson().getLessonId());
        assertEquals("N5", response.getLesson().getJlptLevel());
        assertEquals("V + てから", response.getFormula());
    }

    @Test
    void createGrammar_lessonOfDifferentLevel_throwsLevelMismatch() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(50L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(JlptLevel.N3)));
        CreateGrammarRequest req = createRequest();
        req.setLessonId(50L);

        assertThrows(GrammarBusinessException.class, () -> service.createGrammar(req, STAFF_EMAIL));
        verify(grammarRepository, never()).save(any());
    }

    @Test
    void createGrammar_unknownLesson_throwsLessonNotFound() {
        stubStaff();
        when(lessonRepository.findByIdAndStatusNot(99L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.empty());
        CreateGrammarRequest req = createRequest();
        req.setLessonId(99L);

        assertThrows(GrammarBusinessException.class, () -> service.createGrammar(req, STAFF_EMAIL));
    }

    // ── listGrammars ─────────────────────────────────────────────────────────

    @Test
    void listGrammars_withValidFilters_passesParsedEnumsToRepository() {
        stubStaff();
        when(grammarRepository.findByCreatedByWithFilters(
                        eq(1L),
                        eq(JlptLevel.N5),
                        eq(ContentStatus.DRAFT),
                        eq(ContentStatus.DELETED),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(grammar(ContentStatus.DRAFT, staff))));

        Page<GrammarSummaryResponse> page = service.listGrammars("N5", "draft", PageRequest.of(0, 10), STAFF_EMAIL);

        GrammarSummaryResponse row = page.getContent().get(0);
        assertEquals(10L, row.getGrammarId());
        assertEquals("N5", row.getJlptLevel());
        assertEquals("draft", row.getStatus());
        assertEquals(1L, row.getCreatedBy());
    }

    @Test
    void listGrammars_invalidFilters_areIgnoredAsNull() {
        stubStaff();
        when(grammarRepository.findByCreatedByWithFilters(
                        eq(1L), isNull(), isNull(), eq(ContentStatus.DELETED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertTrue(service.listGrammars("N9", "khong-ton-tai", PageRequest.of(0, 10), STAFF_EMAIL)
                .isEmpty());
    }

    @Test
    void listGrammars_blankFilters_areIgnoredAsNull() {
        stubStaff();
        when(grammarRepository.findByCreatedByWithFilters(
                        eq(1L), isNull(), isNull(), eq(ContentStatus.DELETED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertTrue(service.listGrammars("  ", null, PageRequest.of(0, 10), STAFF_EMAIL)
                .isEmpty());
    }

    @Test
    void listGrammars_summaryOfOrphanGrammar_reportsNullCreator() {
        stubStaff();
        GrammarPoint orphan = grammar(ContentStatus.DRAFT, null);
        when(grammarRepository.findByCreatedByWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(orphan)));

        assertNull(service.listGrammars(null, null, PageRequest.of(0, 10), STAFF_EMAIL)
                .getContent()
                .get(0)
                .getCreatedBy());
    }

    // ── getGrammar ───────────────────────────────────────────────────────────

    @Test
    void getGrammar_ownedByRequester_returnsDetail() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.DRAFT, staff)));

        assertEquals(10L, service.getGrammar(10L, STAFF_EMAIL).getGrammarId());
    }

    @Test
    void getGrammar_otherStaffContent_throwsOwnershipDenied() {
        stubStaff();
        StaffUser other =
                StaffUser.builder().id(77L).staffRole(StaffUser.StaffRole.STAFF).build();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.DRAFT, other)));

        assertThrows(GrammarBusinessException.class, () -> service.getGrammar(10L, STAFF_EMAIL));
    }

    @Test
    void getGrammar_contentWithoutCreator_throwsOwnershipDenied() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.DRAFT, null)));

        assertThrows(GrammarBusinessException.class, () -> service.getGrammar(10L, STAFF_EMAIL));
    }

    @Test
    void getGrammar_asStaffManager_bypassesOwnershipCheck() {
        stubManager();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.DRAFT, staff)));

        assertEquals(10L, service.getGrammar(10L, MANAGER_EMAIL).getGrammarId());
    }

    @Test
    void getGrammar_missing_throwsGrammarNotFound() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(404L, ContentStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThrows(GrammarBusinessException.class, () -> service.getGrammar(404L, STAFF_EMAIL));
    }

    // ── updateGrammar ────────────────────────────────────────────────────────

    @Test
    void updateGrammar_allFieldsProvided_trimsAndSaves() {
        stubStaff();
        GrammarPoint existing = grammar(ContentStatus.DRAFT, staff);
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        when(grammarRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateGrammarRequest req = new UpdateGrammarRequest();
        req.setTitle("Tiêu đề mới");
        req.setStructure("  V-たあとで  ");
        req.setFormula("  V-た + あとで  ");
        req.setMeaning("  Sau khi  ");
        req.setUsageExplanation("  Giải thích mới  ");
        req.setExampleSentenceJp("  新しい例文  ");
        req.setExampleSentenceVi("  Câu ví dụ mới  ");
        req.setJlptLevel("N4");

        GrammarDetailResponse response = service.updateGrammar(10L, req, STAFF_EMAIL);

        assertEquals("Tiêu đề mới", response.getTitle());
        assertEquals("V-たあとで", response.getStructure());
        assertEquals("V-た + あとで", response.getFormula());
        assertEquals("Sau khi", response.getMeaning());
        assertEquals("Giải thích mới", response.getUsageExplanation());
        assertEquals("新しい例文", response.getExampleSentenceJp());
        assertEquals("Câu ví dụ mới", response.getExampleSentenceVi());
        assertEquals("N4", response.getJlptLevel());
    }

    @Test
    void updateGrammar_emptyRequest_keepsAllExistingValues() {
        stubStaff();
        GrammarPoint existing = grammar(ContentStatus.DRAFT, staff);
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        when(grammarRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GrammarDetailResponse response = service.updateGrammar(10L, new UpdateGrammarRequest(), STAFF_EMAIL);

        assertEquals("V-てから", response.getStructure());
        assertEquals("N5", response.getJlptLevel());
    }

    @Test
    void updateGrammar_publishedContent_isRejected() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.PUBLISHED, staff)));
        UpdateGrammarRequest req = new UpdateGrammarRequest();

        assertThrows(GrammarBusinessException.class, () -> service.updateGrammar(10L, req, STAFF_EMAIL));
        verify(grammarRepository, never()).save(any());
    }

    @Test
    void updateGrammar_pendingReviewContent_isRejected() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.PENDING_REVIEW, staff)));
        UpdateGrammarRequest req = new UpdateGrammarRequest();

        assertThrows(GrammarBusinessException.class, () -> service.updateGrammar(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateGrammar_rejectedContent_isEditable() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.REJECTED, staff)));
        when(grammarRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateGrammarRequest req = new UpdateGrammarRequest();
        req.setTitle("Sửa sau khi bị từ chối");

        assertEquals("rejected", service.updateGrammar(10L, req, STAFF_EMAIL).getStatus());
    }

    @Test
    void updateGrammar_invalidJlptLevel_throwsInvalidJlptLevel() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.DRAFT, staff)));
        UpdateGrammarRequest req = new UpdateGrammarRequest();
        req.setJlptLevel("N9");

        assertThrows(GrammarBusinessException.class, () -> service.updateGrammar(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateGrammar_clearLesson_detachesLesson() {
        stubStaff();
        GrammarPoint existing = grammar(ContentStatus.DRAFT, staff);
        existing.setLesson(lesson(JlptLevel.N5));
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        when(grammarRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateGrammarRequest req = new UpdateGrammarRequest();
        req.setClearLesson(true);

        assertNull(service.updateGrammar(10L, req, STAFF_EMAIL).getLesson());
    }

    @Test
    void updateGrammar_newLessonWithMatchingLevel_isAttached() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.DRAFT, staff)));
        when(lessonRepository.findByIdAndStatusNot(50L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(JlptLevel.N5)));
        when(grammarRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateGrammarRequest req = new UpdateGrammarRequest();
        req.setLessonId(50L);

        assertEquals(
                50L, service.updateGrammar(10L, req, STAFF_EMAIL).getLesson().getLessonId());
    }

    @Test
    void updateGrammar_newLessonWithDifferentLevel_throwsLevelMismatch() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.DRAFT, staff)));
        when(lessonRepository.findByIdAndStatusNot(50L, Lesson.LessonStatus.DELETED))
                .thenReturn(Optional.of(lesson(JlptLevel.N3)));
        UpdateGrammarRequest req = new UpdateGrammarRequest();
        req.setLessonId(50L);

        assertThrows(GrammarBusinessException.class, () -> service.updateGrammar(10L, req, STAFF_EMAIL));
    }

    /** Đổi level nhưng giữ bài học cũ → bài học cũ không còn khớp level, phải chặn. */
    @Test
    void updateGrammar_levelChangeStrandsExistingLesson_throwsLevelMismatch() {
        stubStaff();
        GrammarPoint existing = grammar(ContentStatus.DRAFT, staff);
        existing.setLesson(lesson(JlptLevel.N5));
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        UpdateGrammarRequest req = new UpdateGrammarRequest();
        req.setJlptLevel("N2");

        assertThrows(GrammarBusinessException.class, () -> service.updateGrammar(10L, req, STAFF_EMAIL));
    }

    @Test
    void updateGrammar_existingLessonStillMatchesLevel_isKept() {
        stubStaff();
        GrammarPoint existing = grammar(ContentStatus.DRAFT, staff);
        existing.setLesson(lesson(JlptLevel.N5));
        when(grammarRepository.findActiveByIdWithLesson(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(existing));
        when(grammarRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        UpdateGrammarRequest req = new UpdateGrammarRequest();
        req.setJlptLevel("N5");

        assertEquals(
                50L, service.updateGrammar(10L, req, STAFF_EMAIL).getLesson().getLessonId());
    }

    @Test
    void updateGrammar_missing_throwsGrammarNotFound() {
        stubStaff();
        when(grammarRepository.findActiveByIdWithLesson(404L, ContentStatus.DELETED))
                .thenReturn(Optional.empty());
        UpdateGrammarRequest req = new UpdateGrammarRequest();

        assertThrows(GrammarBusinessException.class, () -> service.updateGrammar(404L, req, STAFF_EMAIL));
    }

    // ── submitForReview: các nhánh còn thiếu ─────────────────────────────────

    @Test
    void submitForReview_alreadyPendingReview_throwsSubmitInvalidStatus() {
        stubStaff();
        when(grammarRepository.findByIdAndStatusNot(10L, ContentStatus.DELETED))
                .thenReturn(Optional.of(grammar(ContentStatus.PENDING_REVIEW, staff)));

        assertThrows(GrammarBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @Test
    void submitForReview_missingMeaning_throwsSubmitIncomplete() {
        stubStaff();
        GrammarPoint g = grammar(ContentStatus.DRAFT, staff);
        g.setMeaning("  ");
        when(grammarRepository.findByIdAndStatusNot(10L, ContentStatus.DELETED)).thenReturn(Optional.of(g));

        assertThrows(GrammarBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @Test
    void submitForReview_missingUsageExplanation_throwsSubmitIncomplete() {
        stubStaff();
        GrammarPoint g = grammar(ContentStatus.DRAFT, staff);
        g.setUsageExplanation(null);
        when(grammarRepository.findByIdAndStatusNot(10L, ContentStatus.DELETED)).thenReturn(Optional.of(g));

        assertThrows(GrammarBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @Test
    void submitForReview_missingExampleSentence_throwsSubmitIncomplete() {
        stubStaff();
        GrammarPoint g = grammar(ContentStatus.DRAFT, staff);
        g.setExampleSentenceJp(null);
        when(grammarRepository.findByIdAndStatusNot(10L, ContentStatus.DELETED)).thenReturn(Optional.of(g));

        assertThrows(GrammarBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @Test
    void submitForReview_missingJlptLevel_throwsSubmitIncomplete() {
        stubStaff();
        GrammarPoint g = grammar(ContentStatus.DRAFT, staff);
        g.setJlptLevel(null);
        when(grammarRepository.findByIdAndStatusNot(10L, ContentStatus.DELETED)).thenReturn(Optional.of(g));

        assertThrows(GrammarBusinessException.class, () -> service.submitForReview(10L, STAFF_EMAIL));
    }

    @Test
    void submitForReview_rejectedContentByManager_isAllowed() {
        stubManager();
        GrammarPoint g = grammar(ContentStatus.REJECTED, staff);
        when(grammarRepository.findByIdAndStatusNot(10L, ContentStatus.DELETED)).thenReturn(Optional.of(g));

        assertEquals(
                "pending_review", service.submitForReview(10L, MANAGER_EMAIL).getStatus());
        assertEquals(ContentStatus.PENDING_REVIEW, g.getStatus());
    }

    @Test
    void submitForReview_missing_throwsGrammarNotFound() {
        stubStaff();
        when(grammarRepository.findByIdAndStatusNot(404L, ContentStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThrows(GrammarBusinessException.class, () -> service.submitForReview(404L, STAFF_EMAIL));
    }
}
