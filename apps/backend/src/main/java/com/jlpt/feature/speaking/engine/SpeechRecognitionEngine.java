/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.engine;

import java.nio.file.Path;

/**
 * Trừu tượng hoá engine chấm phát âm (BR-13-09 / NFR-AI-08): tầng nghiệp vụ KHÔNG phụ thuộc vào
 * một nhà cung cấp cụ thể. Muốn cắm engine thật (Azure Speech, Google STT, whisper self-host...)
 * chỉ cần thêm một {@link org.springframework.stereotype.Component} implement interface này.
 *
 * <p>Được phép ném exception khi lỗi/timeout — tầng gọi ({@code SpeakingAsyncProcessor}) chịu trách
 * nhiệm retry + fallback + logging (BR-13-03, BR-13-04, BR-13-07).
 */
public interface SpeechRecognitionEngine {

    /**
     * @param audioPath  đường dẫn file audio đã lưu (không đọc BLOB từ DB — ADR-006)
     * @param targetText câu mẫu học viên cần đọc
     * @return kết quả phân tích; điểm trong [0, 100]
     * @throws Exception khi engine lỗi hoặc timeout
     */
    SpeechAnalysis analyze(Path audioPath, String targetText) throws Exception;

    /** Tên engine dùng cho log (BR-13-07). */
    String engineName();
}
