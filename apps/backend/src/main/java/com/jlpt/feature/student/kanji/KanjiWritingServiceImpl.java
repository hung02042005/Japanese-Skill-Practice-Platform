/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kanji;

import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.feature.student.kanji.dto.KanjiWritingAttemptRequest;
import com.jlpt.feature.student.kanji.dto.KanjiWritingAttemptResponse;
import com.jlpt.feature.student.kanji.dto.KanjiWritingEvaluateRequest;
import com.jlpt.feature.student.kanji.dto.KanjiWritingEvaluateResponse;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KanjiWritingServiceImpl implements KanjiWritingService {

    /* ── Ngưỡng chất lượng (DTW đã normalize về [0,100]) ── */
    private static final double THRESHOLD_PERFECT = 300.0;
    private static final double THRESHOLD_GOOD = 650.0;
    private static final double THRESHOLD_OK = 1200.0;
    private static final int MAX_DOWNSAMPLE = 20;

    private final KanjiWritingAttemptRepository attemptRepository;
    private final StudentUserRepository studentUserRepository;

    /* ═══════════════════════════════════════════════════════
    evaluateStroke — stateless, chỉ tính DTW + trả kết quả
    ═══════════════════════════════════════════════════════ */
    @Override
    public KanjiWritingEvaluateResponse evaluateStroke(KanjiWritingEvaluateRequest req) {
        List<double[]> userPath = toDoubleArray(req.getUserPath());
        List<double[]> refPath = toDoubleArray(req.getReferencePath());

        String direction = computeDirection(refPath);

        if (userPath.size() < 2 || refPath.size() < 2) {
            log.debug(
                    "DTW stroke={} skipped — path too short user={} ref={}",
                    req.getStrokeIndex(),
                    userPath.size(),
                    refPath.size());
            return KanjiWritingEvaluateResponse.builder()
                    .dtwScore(0.0)
                    .quality("ok")
                    .direction(direction)
                    .feedbackMsg("Đúng rồi")
                    .build();
        }

        List<double[]> normUser = normalize(downsample(userPath, MAX_DOWNSAMPLE));
        List<double[]> normRef = normalize(downsample(refPath, MAX_DOWNSAMPLE));

        double score = computeDtw(normUser, normRef);
        String quality = qualityFromDtw(score);

        log.debug("DTW stroke={} score={:.1f} quality={} dir={}", req.getStrokeIndex(), score, quality, direction);

        return KanjiWritingEvaluateResponse.builder()
                .dtwScore(score)
                .quality(quality)
                .direction(direction)
                .feedbackMsg(feedbackMessage(quality))
                .build();
    }

    /* ═══════════════════════════════════════════════════════
    saveAttempt — lưu DB, tính điểm tổng từ per-stroke DTW
    ═══════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public KanjiWritingAttemptResponse saveAttempt(KanjiWritingAttemptRequest req, Long studentId) {
        StudentUser student = studentUserRepository
                .findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        List<KanjiWritingAttemptRequest.StrokeResult> strokes = req.getStrokes() != null ? req.getStrokes() : List.of();

        double avgDtw = strokes.isEmpty()
                ? 0.0
                : strokes.stream()
                        .mapToDouble(KanjiWritingAttemptRequest.StrokeResult::getDtwScore)
                        .average()
                        .orElse(0.0);

        String finalQuality = qualityFromDtw(avgDtw);

        KanjiWritingAttempt attempt = KanjiWritingAttempt.builder()
                .student(student)
                .kanjiId(req.getKanjiId())
                .characterValue(req.getCharacterValue())
                .totalStrokes(req.getTotalStrokes())
                .avgDtwScore(avgDtw)
                .finalQuality(finalQuality)
                .strokeDetails(buildStrokeJson(strokes))
                .createdBy(studentId)
                .build();

        attempt = attemptRepository.save(attempt);

        log.info(
                "Saved kanji writing attempt id={} student={} kanji='{}' quality={}",
                attempt.getId(),
                studentId,
                req.getCharacterValue(),
                finalQuality);

        return KanjiWritingAttemptResponse.builder()
                .attemptId(attempt.getId())
                .finalQuality(finalQuality)
                .avgDtwScore(avgDtw)
                .totalStrokes(req.getTotalStrokes())
                .build();
    }

    /* ══════════════════════════════════════════════
    DTW core
    ══════════════════════════════════════════════ */

    private double computeDtw(List<double[]> s1, List<double[]> s2) {
        int n = s1.size(), m = s2.size();
        double[][] dp = new double[n][m];
        for (double[] row : dp) Arrays.fill(row, Double.MAX_VALUE / 2);

        dp[0][0] = euclidean(s1.get(0), s2.get(0));
        for (int i = 1; i < n; i++) dp[i][0] = dp[i - 1][0] + euclidean(s1.get(i), s2.get(0));
        for (int j = 1; j < m; j++) dp[0][j] = dp[0][j - 1] + euclidean(s1.get(0), s2.get(j));

        for (int i = 1; i < n; i++) {
            for (int j = 1; j < m; j++) {
                double cost = euclidean(s1.get(i), s2.get(j));
                dp[i][j] = cost + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
            }
        }
        return dp[n - 1][m - 1];
    }

    private double euclidean(double[] a, double[] b) {
        double dx = a[0] - b[0], dy = a[1] - b[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    /* ── Normalize cả 2 path độc lập về [0,100] → scale + translation invariant ── */
    private List<double[]> normalize(List<double[]> path) {
        double minX = path.stream().mapToDouble(p -> p[0]).min().orElse(0);
        double maxX = path.stream().mapToDouble(p -> p[0]).max().orElse(0);
        double minY = path.stream().mapToDouble(p -> p[1]).min().orElse(0);
        double maxY = path.stream().mapToDouble(p -> p[1]).max().orElse(0);
        double scale = Math.max(Math.max(maxX - minX, maxY - minY), 1.0);

        return path.stream()
                .map(p -> new double[] {(p[0] - minX) / scale * 100.0, (p[1] - minY) / scale * 100.0})
                .collect(Collectors.toList());
    }

    /* ── Downsample đều — giữ MAX_DOWNSAMPLE điểm để cân bằng với reference median ── */
    private List<double[]> downsample(List<double[]> path, int maxPts) {
        if (path.size() <= maxPts) return path;
        List<double[]> out = new ArrayList<>(maxPts);
        double step = (double) (path.size() - 1) / (maxPts - 1);
        for (int i = 0; i < maxPts; i++) {
            out.add(path.get((int) Math.round(i * step)));
        }
        return out;
    }

    /* ── Hướng nét từ điểm đầu→cuối của reference median (Y-up = HanziWriter coords) ── */
    private String computeDirection(List<double[]> refPath) {
        if (refPath.size() < 2) return "";
        double[] s = refPath.get(0);
        double[] e = refPath.get(refPath.size() - 1);
        double dx = e[0] - s[0], dy = e[1] - s[1];
        double adx = Math.abs(dx), ady = Math.abs(dy);

        if (ady > adx * 2.5) return dy > 0 ? "Lên trên" : "Xuống";
        if (adx > ady * 2.5) return dx > 0 ? "Sang phải" : "Sang trái";
        if (dx > 0 && dy < 0) return "Chéo phải xuống";
        if (dx < 0 && dy < 0) return "Chéo trái xuống";
        if (dx > 0) return "Chéo phải lên";
        return "Chéo trái lên";
    }

    private String qualityFromDtw(double score) {
        if (score < THRESHOLD_PERFECT) return "perfect";
        if (score < THRESHOLD_GOOD) return "good";
        if (score < THRESHOLD_OK) return "ok";
        return "bad";
    }

    private String feedbackMessage(String quality) {
        return switch (quality) {
            case "perfect" -> "Hoàn hảo!";
            case "good" -> "Tốt lắm!";
            case "ok" -> "Đúng rồi!";
            default -> "Cần luyện thêm!";
        };
    }

    private List<double[]> toDoubleArray(List<List<Double>> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .filter(p -> p != null && p.size() >= 2)
                .map(p -> new double[] {p.get(0), p.get(1)})
                .collect(Collectors.toList());
    }

    private String buildStrokeJson(List<KanjiWritingAttemptRequest.StrokeResult> strokes) {
        if (strokes.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < strokes.size(); i++) {
            KanjiWritingAttemptRequest.StrokeResult s = strokes.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format(
                    Locale.US,
                    "{\"idx\":%d,\"dtw\":%.2f,\"quality\":\"%s\",\"dir\":\"%s\"}",
                    s.getStrokeIndex(),
                    s.getDtwScore(),
                    s.getQuality() != null ? s.getQuality() : "ok",
                    s.getDirection() != null ? s.getDirection() : ""));
        }
        sb.append("]");
        return sb.toString();
    }
}
