/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.staffcontent.learning;

import com.jlpt.feature.staffcontent.learning.dto.CreateKanjiRequest;
import com.jlpt.feature.staffcontent.learning.dto.CreateLessonRequest;
import com.jlpt.feature.staffcontent.learning.dto.CreateVocabularyRequest;
import com.jlpt.feature.staffcontent.learning.dto.KanjiDetailResponse;
import com.jlpt.feature.staffcontent.learning.dto.LessonDetailResponse;
import com.jlpt.feature.staffcontent.learning.dto.SubmitReviewRequest;
import com.jlpt.feature.staffcontent.learning.dto.SubmitReviewResponse;
import com.jlpt.feature.staffcontent.learning.dto.UpdateKanjiRequest;
import com.jlpt.feature.staffcontent.learning.dto.UpdateLessonRequest;
import com.jlpt.feature.staffcontent.learning.dto.UpdateVocabularyRequest;
import com.jlpt.feature.staffcontent.learning.dto.VocabularyDetailResponse;
import org.springframework.data.domain.Page;

/**
 * UC-27 — Staff learning-content management (lesson / vocabulary / kanji).
 * {@code staffEmail} is the authenticated principal name (JWT subject = email).
 */
public interface LearningContentService {

    LessonDetailResponse createLesson(CreateLessonRequest request, String staffEmail);

    LessonDetailResponse updateLesson(Long lessonId, UpdateLessonRequest request, String staffEmail);

    VocabularyDetailResponse createVocabulary(CreateVocabularyRequest request, String staffEmail);

    VocabularyDetailResponse updateVocabulary(Long vocabularyId, UpdateVocabularyRequest request, String staffEmail);

    KanjiDetailResponse createKanji(CreateKanjiRequest request, String staffEmail);

    KanjiDetailResponse updateKanji(Long kanjiId, UpdateKanjiRequest request, String staffEmail);

    SubmitReviewResponse submitForReview(SubmitReviewRequest request, String staffEmail);

    // ── UC-27 GET endpoints ──────────────────────────────────────────

    /** FR-27-10: List lessons with filters; page returns { content, totalElements, totalPages }. */
    Page<LessonDetailResponse> listLessons(
            String q, String jlptLevel, String lessonType, String status, int page, int size, String staffEmail);

    /** FR-27-11: Get lesson detail by id. */
    LessonDetailResponse getLesson(Long lessonId, String staffEmail);

    /** FR-27-16: List vocabulary with filters. */
    Page<VocabularyDetailResponse> listVocabulary(
            String q, String jlptLevel, String topic, String status, int page, int size, String staffEmail);

    /** FR-27-17: Get vocabulary detail by id. */
    VocabularyDetailResponse getVocabulary(Long vocabularyId, String staffEmail);

    /** FR-27-20: List kanji with filters. */
    Page<KanjiDetailResponse> listKanji(
            String q, String jlptLevel, String status, int page, int size, String staffEmail);

    /** FR-27-21: Get kanji detail by id. */
    KanjiDetailResponse getKanji(Long kanjiId, String staffEmail);
}
