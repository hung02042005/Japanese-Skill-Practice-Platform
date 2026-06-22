package com.jlpt.feature.student.kana.service;

import com.jlpt.feature.student.kana.dto.response.KanaListResponse;

public interface KanaService {
    KanaListResponse getKanaChart(String script, Long studentId);
    void markKanaComplete(Integer kanaId, Long studentId);
}
