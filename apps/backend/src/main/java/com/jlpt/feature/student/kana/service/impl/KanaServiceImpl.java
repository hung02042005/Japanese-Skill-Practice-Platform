/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student.kana.service.impl;

import com.jlpt.feature.learning.KanaCharacter;
import com.jlpt.feature.learning.KanaCharacter.KanaType;
import com.jlpt.feature.learning.repository.KanaCharacterRepository;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgress.ContentType;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.kana.dto.response.KanaListResponse;
import com.jlpt.feature.student.kana.dto.response.KanaResponse;
import com.jlpt.feature.student.kana.service.KanaService;
import com.jlpt.feature.student.progress.StudentLearningProgressService;
import com.jlpt.feature.student.progress.dto.LearningProgressRequest;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KanaServiceImpl implements KanaService {

    private final KanaCharacterRepository kanaRepository;
    private final StudentContentProgressRepository progressRepository;
    private final StudentLearningProgressService learningProgressService;

    @Override
    public KanaListResponse getKanaChart(String script, Long studentId) {
        KanaType type;
        try {
            type = KanaType.valueOf(script.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid script: " + script);
        }

        List<KanaCharacter> characters = kanaRepository.findByKanaTypeOrderByDisplayOrderAsc(type);

        List<Long> contentIds =
                characters.stream().map(c -> c.getId().longValue()).collect(Collectors.toList());

        List<StudentContentProgress> progresses =
                progressRepository.findByStudentIdAndContentTypeAndContentIdIn(studentId, ContentType.KANA, contentIds);

        Map<Long, Boolean> completedMap = progresses.stream()
                .collect(Collectors.toMap(
                        StudentContentProgress::getContentId,
                        p -> p.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED,
                        (existing, replacement) -> existing));

        List<KanaResponse> responses = characters.stream()
                .map(c -> KanaResponse.builder()
                        .kanaId(c.getId())
                        .character(c.getCharacterValue())
                        .romaji(c.getRomaji())
                        .kanaType(c.getKanaType().getValue())
                        .audioUrl(c.getAudioUrl())
                        .strokeOrderUrl(c.getStrokeOrderUrl())
                        .displayOrder(c.getDisplayOrder())
                        .row(determineRow(c.getRomaji()))
                        .isCompleted(completedMap.getOrDefault(c.getId().longValue(), false))
                        .build())
                .collect(Collectors.toList());

        long completedCount =
                responses.stream().filter(KanaResponse::isCompleted).count();
        long totalCount = responses.size();

        return KanaListResponse.builder()
                .characters(responses)
                .completedCount(completedCount)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public void markKanaComplete(Integer kanaId, Long studentId) {
        if (!kanaRepository.existsById(kanaId)) {
            throw new ResourceNotFoundException("KanaCharacter", kanaId.longValue());
        }

        LearningProgressRequest request = new LearningProgressRequest();
        request.setContentId(kanaId.longValue());
        request.setContentType(ContentType.KANA.getValue());
        request.setStatus(StudentContentProgress.ProgressStatus.COMPLETED.getValue());
        request.setProgressPercent(BigDecimal.valueOf(100));

        learningProgressService.markProgress(request, studentId);
    }

    private String determineRow(String romaji) {
        if (romaji == null || romaji.isEmpty()) return "Other";
        String r = romaji.toLowerCase();
        if ("a,i,u,e,o".contains(r)) return "a-row";
        if (r.startsWith("k")) return "ka-row";
        if (r.startsWith("s") || r.startsWith("sh")) return "sa-row";
        if (r.startsWith("t") || r.startsWith("ch") || r.startsWith("ts")) return "ta-row";
        if (r.startsWith("n") && !r.equals("n")) return "na-row";
        if (r.startsWith("h") || r.startsWith("f")) return "ha-row";
        if (r.startsWith("m")) return "ma-row";
        if (r.startsWith("y")) return "ya-row";
        if (r.startsWith("r")) return "ra-row";
        if (r.startsWith("w")) return "wa-row";
        if (r.equals("n")) return "n";
        if (r.startsWith("g")) return "ga-row";
        if (r.startsWith("z") || r.startsWith("j")) return "za-row";
        if (r.startsWith("d")) return "da-row";
        if (r.startsWith("b")) return "ba-row";
        if (r.startsWith("p")) return "pa-row";
        return "other";
    }
}
