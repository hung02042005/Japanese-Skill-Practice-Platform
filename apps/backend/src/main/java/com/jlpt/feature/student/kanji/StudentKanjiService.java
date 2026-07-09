/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji;

import com.jlpt.feature.student.kanji.dto.KanjiDetailResponse;
import com.jlpt.feature.student.kanji.dto.KanjiListResponse;

public interface StudentKanjiService {
    KanjiListResponse getKanjiList(String level, Long studentId, int page, int size);

    KanjiDetailResponse getKanjiDetail(Long kanjiId, Long studentId);
}
