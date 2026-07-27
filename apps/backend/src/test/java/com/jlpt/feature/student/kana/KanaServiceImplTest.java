/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kana;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jlpt.feature.learning.KanaCharacter;
import com.jlpt.feature.learning.KanaCharacter.KanaType;
import com.jlpt.feature.learning.repository.KanaCharacterRepository;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.kana.dto.response.KanaListResponse;
import com.jlpt.feature.student.kana.service.impl.KanaServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Bảng chữ Kana: phủ toàn bộ nhánh phân hàng (determineRow) và việc ghép tiến độ học của học viên
 * vào từng ký tự.
 */
@ExtendWith(MockitoExtension.class)
class KanaServiceImplTest {

    private static final Long STUDENT_ID = 1L;

    @Mock
    private KanaCharacterRepository kanaRepository;

    @Mock
    private StudentContentProgressRepository progressRepository;

    @InjectMocks
    private KanaServiceImpl service;

    private KanaCharacter kana(int id, String character, String romaji) {
        return KanaCharacter.builder()
                .id(id)
                .characterValue(character)
                .romaji(romaji)
                .kanaType(KanaType.HIRAGANA)
                .audioUrl("/uploads/audio/" + id + ".mp3")
                .strokeOrderUrl("/uploads/stroke/" + id + ".svg")
                .displayOrder(id)
                .build();
    }

    private StudentContentProgress progress(long contentId, StudentContentProgress.ProgressStatus status) {
        return StudentContentProgress.builder()
                .id(contentId)
                .contentType(ContentType.KANA)
                .contentId(contentId)
                .status(status)
                .build();
    }

    // ── determineRow: phủ từng nhánh ─────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "a, a-row",
        "i, a-row",
        "u, a-row",
        "e, a-row",
        "o, a-row",
        "ka, ka-row",
        "ki, ka-row",
        "sa, sa-row",
        "shi, sa-row",
        "ta, ta-row",
        "chi, ta-row",
        "tsu, ta-row",
        "na, na-row",
        "ni, na-row",
        "ha, ha-row",
        "fu, ha-row",
        "ma, ma-row",
        "ya, ya-row",
        "ra, ra-row",
        "wa, wa-row",
        "n, n",
        "ga, ga-row",
        "za, za-row",
        "ji, za-row",
        "da, da-row",
        "ba, ba-row",
        "pa, pa-row",
        "xyz, other"
    })
    void getKanaChart_assignsCorrectRowForRomaji(String romaji, String expectedRow) {
        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaType.HIRAGANA))
                .thenReturn(List.of(kana(1, "あ", romaji)));
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(STUDENT_ID), eq(ContentType.KANA), any()))
                .thenReturn(List.of());

        KanaListResponse response = service.getKanaChart("hiragana", STUDENT_ID);

        assertEquals(expectedRow, response.getCharacters().get(0).getRow());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getKanaChart_nullOrEmptyRomaji_fallsBackToOther(String romaji) {
        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaType.HIRAGANA))
                .thenReturn(List.of(kana(1, "あ", romaji)));
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(STUDENT_ID), eq(ContentType.KANA), any()))
                .thenReturn(List.of());

        assertEquals(
                "Other",
                service.getKanaChart("hiragana", STUDENT_ID)
                        .getCharacters()
                        .get(0)
                        .getRow());
    }

    /** "n" đứng riêng là ký tự ん, còn "na"/"ni" thuộc hàng na — phủ cả 2 vế của `startsWith("n") && !equals("n")`. */
    @Test
    void getKanaChart_standaloneNIsNotPartOfNaRow() {
        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaType.HIRAGANA))
                .thenReturn(List.of(kana(1, "ん", "n"), kana(2, "な", "na")));
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(STUDENT_ID), eq(ContentType.KANA), any()))
                .thenReturn(List.of());

        KanaListResponse response = service.getKanaChart("hiragana", STUDENT_ID);

        assertEquals("n", response.getCharacters().get(0).getRow());
        assertEquals("na-row", response.getCharacters().get(1).getRow());
    }

    // ── script parsing ───────────────────────────────────────────────────────

    @Test
    void getKanaChart_katakanaScript_queriesKatakana() {
        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaType.KATAKANA))
                .thenReturn(List.of());
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(STUDENT_ID), eq(ContentType.KANA), any()))
                .thenReturn(List.of());

        assertEquals(0, service.getKanaChart("KATAKANA", STUDENT_ID).getTotalCount());
    }

    @Test
    void getKanaChart_lowercaseScript_isAccepted() {
        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaType.KATAKANA))
                .thenReturn(List.of());
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(STUDENT_ID), eq(ContentType.KANA), any()))
                .thenReturn(List.of());

        assertNotNull(service.getKanaChart("katakana", STUDENT_ID));
    }

    @Test
    void getKanaChart_invalidScript_throwsIllegalArgument() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> service.getKanaChart("romaji", STUDENT_ID));

        assertTrue(ex.getMessage().contains("romaji"));
        verifyNoInteractions(kanaRepository, progressRepository);
    }

    // ── ghép tiến độ ─────────────────────────────────────────────────────────

    @Test
    void getKanaChart_marksCompletedCharactersAndCountsThem() {
        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaType.HIRAGANA))
                .thenReturn(List.of(kana(1, "あ", "a"), kana(2, "い", "i"), kana(3, "う", "u")));
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(STUDENT_ID), eq(ContentType.KANA), any()))
                .thenReturn(List.of(
                        progress(1L, StudentContentProgress.ProgressStatus.COMPLETED),
                        progress(2L, StudentContentProgress.ProgressStatus.LEARNING)));

        KanaListResponse response = service.getKanaChart("hiragana", STUDENT_ID);

        assertTrue(response.getCharacters().get(0).isCompleted());
        assertFalse(response.getCharacters().get(1).isCompleted());
        assertFalse(response.getCharacters().get(2).isCompleted(), "không có bản ghi tiến độ → chưa hoàn thành");
        assertEquals(1, response.getCompletedCount());
        assertEquals(3, response.getTotalCount());
    }

    /** Dữ liệu lỗi có 2 bản ghi tiến độ cho cùng 1 ký tự — merge function giữ bản ghi đầu, không ném lỗi. */
    @Test
    void getKanaChart_duplicateProgressRows_keepsFirstWithoutThrowing() {
        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaType.HIRAGANA))
                .thenReturn(List.of(kana(1, "あ", "a")));
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(STUDENT_ID), eq(ContentType.KANA), any()))
                .thenReturn(List.of(
                        progress(1L, StudentContentProgress.ProgressStatus.COMPLETED),
                        progress(1L, StudentContentProgress.ProgressStatus.LEARNING)));

        KanaListResponse response = service.getKanaChart("hiragana", STUDENT_ID);

        assertTrue(response.getCharacters().get(0).isCompleted());
        assertEquals(1, response.getCompletedCount());
    }

    @Test
    void getKanaChart_emptyChart_returnsZeroCounts() {
        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaType.HIRAGANA))
                .thenReturn(List.of());
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(STUDENT_ID), eq(ContentType.KANA), any()))
                .thenReturn(List.of());

        KanaListResponse response = service.getKanaChart("hiragana", STUDENT_ID);

        assertEquals(0, response.getCompletedCount());
        assertEquals(0, response.getTotalCount());
        assertTrue(response.getCharacters().isEmpty());
    }

    @Test
    void getKanaChart_mapsAllCharacterFields() {
        when(kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(KanaType.HIRAGANA))
                .thenReturn(List.of(kana(7, "か", "ka")));
        when(progressRepository.findByStudentIdAndContentTypeAndContentIdIn(
                        eq(STUDENT_ID), eq(ContentType.KANA), any()))
                .thenReturn(List.of());

        var item = service.getKanaChart("hiragana", STUDENT_ID).getCharacters().get(0);

        assertEquals(7, item.getKanaId());
        assertEquals("か", item.getCharacter());
        assertEquals("ka", item.getRomaji());
        assertEquals("hiragana", item.getKanaType());
        assertEquals("/uploads/audio/7.mp3", item.getAudioUrl());
        assertEquals("/uploads/stroke/7.svg", item.getStrokeOrderUrl());
        assertEquals(7, item.getDisplayOrder());
    }
}
