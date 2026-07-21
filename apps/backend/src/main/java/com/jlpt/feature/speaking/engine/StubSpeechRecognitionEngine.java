/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Engine chấm phát âm placeholder (ADR-007 / UC-13 "engine cụ thể chưa chốt").
 *
 * <p>Chưa tích hợp dịch vụ ASR thật, engine này sinh kết quả MÔ PHỎNG có tính ổn định: điểm và các
 * từ "cần cải thiện" được suy ra một cách tất định từ độ dài file audio + nội dung câu mẫu, nên mỗi
 * lần ghi âm khác nhau sẽ ra điểm khác nhau nhưng không nhảy loạn. Khi có engine thật, thêm một
 * {@link SpeechRecognitionEngine} khác đánh dấu {@code @Primary} để nó được ưu tiên inject.
 */
@Component
@Slf4j
public class StubSpeechRecognitionEngine implements SpeechRecognitionEngine {

    // Cụm được coi là "chưa đạt" khi băm rơi vào lát cắt này (~1/5 số từ).
    private static final int MISS_MODULO = 5;
    private static final int MIN_SCORE = 62;
    private static final int SCORE_SPAN = 34; // trần lý thuyết 96

    @Override
    public SpeechAnalysis analyze(Path audioPath, String targetText) throws IOException {
        long audioSize = Files.size(audioPath);
        String text = targetText == null ? "" : targetText.trim();

        // Seed tất định theo lần ghi (kích thước audio) + nội dung câu.
        long seed = audioSize * 31L + text.hashCode();

        List<String> tokens = tokenize(text);
        List<SpeechAnalysis.WordScore> words = new ArrayList<>(tokens.size());
        int missed = 0;
        for (String token : tokens) {
            boolean correct = Math.floorMod(token.hashCode() * 17L + seed, MISS_MODULO) != 0;
            if (!correct) {
                missed++;
            }
            words.add(new SpeechAnalysis.WordScore(token, correct, correct ? null : "Phát âm chưa rõ"));
        }

        int overall = clamp(MIN_SCORE + (int) Math.floorMod(seed, SCORE_SPAN) - missed * 3);
        int pronunciation = clamp(overall - Math.floorMod((int) seed, 4));
        int fluency = clamp(overall + 2 - Math.floorMod((int) (seed >> 3), 6));

        String suggestions = missed == 0
                ? "Phát âm rất tốt! Tiếp tục duy trì nhịp độ và ngữ điệu."
                : "Chú ý phát âm rõ các cụm được đánh dấu, đọc chậm và tròn vành âm cuối.";

        log.debug(
                "[SpeakingStub] audioSize={}B tokens={} missed={} overall={}",
                audioSize,
                tokens.size(),
                missed,
                overall);

        // Mô phỏng transcript: coi như học viên đọc đúng nội dung câu mẫu.
        return new SpeechAnalysis(overall, pronunciation, fluency, text, words, suggestions);
    }

    @Override
    public String engineName() {
        return "stub-speech-v1";
    }

    /** Tách câu tiếng Nhật thành cụm theo dấu câu / khoảng trắng; fallback nguyên câu nếu rỗng. */
    private List<String> tokenize(String text) {
        List<String> result = new ArrayList<>();
        for (String piece : text.split("[\\s。、，,\\.！？!?「」『』（）()・〜~…]+")) {
            String trimmed = piece.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        if (result.isEmpty() && !text.isEmpty()) {
            result.add(text);
        }
        return result;
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}
