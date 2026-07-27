/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.feature.staffcontent.learning.dto.CreateKanjiRequest;
import com.jlpt.feature.staffcontent.learning.dto.CreateVocabularyRequest;
import com.jlpt.feature.staffcontent.learning.dto.KanjiDetailResponse;
import com.jlpt.feature.staffcontent.learning.dto.LessonDetailResponse;
import com.jlpt.feature.staffcontent.learning.dto.UpdateKanjiRequest;
import com.jlpt.feature.staffcontent.learning.dto.UpdateLessonRequest;
import com.jlpt.feature.staffcontent.learning.dto.UpdateVocabularyRequest;
import com.jlpt.feature.staffcontent.learning.dto.VocabularyDetailResponse;
import com.jlpt.feature.staffcontent.learning.service.LearningContentService;
import com.jlpt.shared.common.ApiResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

/**
 * UC-27 — controller chỉ điều phối: truyền đúng email staff xuống service và gói Page thành
 * {content, totalElements, totalPages}; tạo mới trả 201.
 */
@ExtendWith(MockitoExtension.class)
class StaffLearningContentControllerTest {

    private static final String STAFF_EMAIL = "staff@sakuji.com";

    @Mock
    private LearningContentService learningContentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StaffLearningContentController controller;

    @BeforeEach
    void setUp() {
        lenient().when(authentication.getName()).thenReturn(STAFF_EMAIL);
    }

    // ── lesson ───────────────────────────────────────────────────────────────

    @Test
    void updateLesson_delegatesAndWrapsResponse() {
        UpdateLessonRequest request = new UpdateLessonRequest();
        LessonDetailResponse data =
                LessonDetailResponse.builder().lessonId(10L).title("Bài 1").build();
        when(learningContentService.updateLesson(10L, request, STAFF_EMAIL)).thenReturn(data);

        ResponseEntity<ApiResponse<LessonDetailResponse>> response =
                controller.updateLesson(10L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(data, response.getBody().getData());
        assertEquals("Cập nhật học liệu thành công", response.getBody().getMessage());
    }

    @Test
    void listLessons_wrapsPageIntoContentAndTotals() {
        LessonDetailResponse row = LessonDetailResponse.builder().lessonId(10L).build();
        when(learningContentService.listLessons("nghe", "N5", "listening", "draft", 0, 20, STAFF_EMAIL))
                .thenReturn(new PageImpl<>(List.of(row)));

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                controller.listLessons("nghe", "N5", "listening", "draft", 0, 20, authentication);

        Map<String, Object> data = response.getBody().getData();
        assertEquals(List.of(row), data.get("content"));
        assertEquals(1L, data.get("totalElements"));
        assertEquals(1, data.get("totalPages"));
    }

    @Test
    void listLessons_emptyResult_reportsZeroTotals() {
        when(learningContentService.listLessons(null, null, null, null, 0, 20, STAFF_EMAIL))
                .thenReturn(new PageImpl<>(List.of()));

        Map<String, Object> data = controller
                .listLessons(null, null, null, null, 0, 20, authentication)
                .getBody()
                .getData();

        assertEquals(0L, data.get("totalElements"));
    }

    // GET /lessons/{id} đã bị gỡ khỏi controller (nhánh branch_for_hung) nên test getLesson_delegates
    // tương ứng đã được xoá.

    // ── vocabulary ───────────────────────────────────────────────────────────

    @Test
    void createVocabulary_returns201Created() {
        CreateVocabularyRequest request = new CreateVocabularyRequest();
        VocabularyDetailResponse data =
                VocabularyDetailResponse.builder().vocabularyId(100L).build();
        when(learningContentService.createVocabulary(request, STAFF_EMAIL)).thenReturn(data);

        ResponseEntity<ApiResponse<VocabularyDetailResponse>> response =
                controller.createVocabulary(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Tạo từ vựng thành công", response.getBody().getMessage());
        assertSame(data, response.getBody().getData());
    }

    @Test
    void updateVocabulary_delegates() {
        UpdateVocabularyRequest request = new UpdateVocabularyRequest();
        VocabularyDetailResponse data =
                VocabularyDetailResponse.builder().vocabularyId(100L).build();
        when(learningContentService.updateVocabulary(100L, request, STAFF_EMAIL))
                .thenReturn(data);

        ResponseEntity<ApiResponse<VocabularyDetailResponse>> response =
                controller.updateVocabulary(100L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cập nhật từ vựng thành công", response.getBody().getMessage());
    }

    @Test
    void listVocabulary_wrapsPageIntoContentAndTotals() {
        VocabularyDetailResponse row =
                VocabularyDetailResponse.builder().vocabularyId(100L).build();
        when(learningContentService.listVocabulary("tu", "N5", 5L, "draft", 0, 20, STAFF_EMAIL))
                .thenReturn(new PageImpl<>(List.of(row)));

        Map<String, Object> data = controller
                .listVocabulary("tu", "N5", 5L, "draft", 0, 20, authentication)
                .getBody()
                .getData();

        assertEquals(List.of(row), data.get("content"));
        assertEquals(1L, data.get("totalElements"));
    }

    @Test
    void getVocabulary_delegates() {
        VocabularyDetailResponse data =
                VocabularyDetailResponse.builder().vocabularyId(100L).build();
        when(learningContentService.getVocabulary(100L, STAFF_EMAIL)).thenReturn(data);

        assertSame(
                data, controller.getVocabulary(100L, authentication).getBody().getData());
    }

    // ── kanji ────────────────────────────────────────────────────────────────

    @Test
    void createKanji_returns201Created() {
        CreateKanjiRequest request = new CreateKanjiRequest();
        KanjiDetailResponse data = KanjiDetailResponse.builder().kanjiId(200L).build();
        when(learningContentService.createKanji(request, STAFF_EMAIL)).thenReturn(data);

        ResponseEntity<ApiResponse<KanjiDetailResponse>> response = controller.createKanji(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Tạo Kanji thành công", response.getBody().getMessage());
    }

    @Test
    void updateKanji_delegates() {
        UpdateKanjiRequest request = new UpdateKanjiRequest();
        KanjiDetailResponse data = KanjiDetailResponse.builder().kanjiId(200L).build();
        when(learningContentService.updateKanji(200L, request, STAFF_EMAIL)).thenReturn(data);

        ResponseEntity<ApiResponse<KanjiDetailResponse>> response =
                controller.updateKanji(200L, request, authentication);

        assertEquals("Cập nhật Kanji thành công", response.getBody().getMessage());
        assertSame(data, response.getBody().getData());
    }

    @Test
    void listKanji_wrapsPageIntoContentAndTotals() {
        KanjiDetailResponse row = KanjiDetailResponse.builder().kanjiId(200L).build();
        when(learningContentService.listKanji("nhat", "N5", "draft", 0, 20, STAFF_EMAIL))
                .thenReturn(new PageImpl<>(List.of(row)));

        Map<String, Object> data = controller
                .listKanji("nhat", "N5", "draft", 0, 20, authentication)
                .getBody()
                .getData();

        assertEquals(List.of(row), data.get("content"));
        assertEquals(1, data.get("totalPages"));
    }

    @Test
    void getKanji_delegates() {
        KanjiDetailResponse data = KanjiDetailResponse.builder().kanjiId(200L).build();
        when(learningContentService.getKanji(200L, STAFF_EMAIL)).thenReturn(data);

        assertSame(data, controller.getKanji(200L, authentication).getBody().getData());
    }
}
