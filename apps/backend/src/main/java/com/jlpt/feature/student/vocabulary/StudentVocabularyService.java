package com.jlpt.feature.student.vocabulary;

import com.jlpt.feature.student.vocabulary.dto.VocabularyDetailResponse;
import com.jlpt.feature.student.vocabulary.dto.VocabularyListResponse;

public interface StudentVocabularyService {
    VocabularyListResponse getVocabularyList(String level, String topic, Long studentId, int page, int size);
    VocabularyDetailResponse getVocabularyDetail(Long id, Long studentId);
}
