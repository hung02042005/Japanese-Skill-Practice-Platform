/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji;

import com.jlpt.feature.student.kanji.dto.KanjiWritingAttemptRequest;
import com.jlpt.feature.student.kanji.dto.KanjiWritingAttemptResponse;
import com.jlpt.feature.student.kanji.dto.KanjiWritingEvaluateRequest;
import com.jlpt.feature.student.kanji.dto.KanjiWritingEvaluateResponse;

public interface KanjiWritingService {

    /** Chạy DTW so sánh nét người dùng vs nét chuẩn, trả về chất lượng + hướng */
    KanjiWritingEvaluateResponse evaluateStroke(KanjiWritingEvaluateRequest request);

    /** Lưu toàn bộ kết quả một lần luyện viết vào DB */
    KanjiWritingAttemptResponse saveAttempt(KanjiWritingAttemptRequest request, Long studentId);
}
