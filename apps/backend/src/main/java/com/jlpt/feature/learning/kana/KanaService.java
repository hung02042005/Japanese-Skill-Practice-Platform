/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.learning.kana;

import com.jlpt.feature.learning.KanaCharacter;
import com.jlpt.feature.learning.exception.LearningException;
import com.jlpt.feature.learning.kana.dto.response.KanaChartResponse;
import com.jlpt.feature.learning.kana.dto.response.KanaResponse;
import com.jlpt.feature.student.StudentContentProgress;
import com.jlpt.feature.student.StudentContentProgressRepository;
import com.jlpt.feature.student.StudentUserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-08 — Học Kana: bảng chữ đầy đủ theo loại (hiragana/katakana) + cờ tiến độ.
 * Không có pagination (BR-08-01). Không check status/VIP (BR-08-07).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KanaService {

    private final KanaRepository kanaRepository;
    private final StudentContentProgressRepository progressRepository;
    private final StudentUserRepository studentUserRepository;

    @Transactional
    public KanaChartResponse getKanaChart(String type, Long studentId) {
        KanaCharacter.KanaType kanaType = parseKanaType(type);

        List<KanaCharacter> characters = kanaRepository.findByKanaTypeOrderByDisplayOrder(kanaType);

        List<Long> kanaIds = characters.stream()
                .map(c -> c.getId().longValue())
                .toList();

        Map<Long, StudentContentProgress> progressMap = kanaIds.isEmpty()
                ? Map.of()
                : progressRepository.findByStudent_IdAndContentTypeAndContentIdIn(
                                studentId, StudentContentProgress.ContentType.KANA, kanaIds)
                        .stream()
                        .collect(Collectors.toMap(StudentContentProgress::getContentId, Function.identity()));

        long completedCount = progressMap.values().stream()
                .filter(p -> p.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED)
                .count();

        log.info("[KanaService] Truy cập bảng kana {studentId={}, type={}}", studentId, type);
        touchLastActivity(studentId);

        List<KanaResponse> responseList = characters.stream()
                .map(c -> toResponse(c, progressMap.get(c.getId().longValue())))
                .toList();

        return KanaChartResponse.builder()
                .characters(responseList)
                .completedCount(completedCount)
                .totalCount(characters.size())
                .build();
    }

    private KanaCharacter.KanaType parseKanaType(String type) {
        if (type == null) {
            throw LearningException.validationFailed("type");
        }
        return switch (type.toLowerCase()) {
            case "hiragana" -> KanaCharacter.KanaType.HIRAGANA;
            case "katakana" -> KanaCharacter.KanaType.KATAKANA;
            default -> throw LearningException.validationFailed("type");
        };
    }

    private void touchLastActivity(Long studentId) {
        studentUserRepository.findById(studentId).ifPresent(student -> {
            student.setLastActivityDate(LocalDate.now());
            studentUserRepository.save(student);
        });
    }

    /**
     * Tính nhóm hàng từ display_order: mỗi hàng Kana có tối đa 5 ký tự.
     * あ行=1(1-5), か行=2(6-10), さ行=3(11-15), ... Frontend dùng để group ký tự.
     */
    private String computeRowGroup(int displayOrder) {
        return String.valueOf((displayOrder - 1) / 5 + 1);
    }

    private KanaResponse toResponse(KanaCharacter character, StudentContentProgress progress) {
        return KanaResponse.builder()
                .kanaId(character.getId())
                .character(character.getCharacterValue())
                .romaji(character.getRomaji())
                .rowGroup(computeRowGroup(character.getDisplayOrder()))
                .audioUrl(character.getAudioUrl())
                .strokeGifUrl(character.getStrokeOrderUrl())
                .kanaType(character.getKanaType().getValue())
                .isCompleted(progress != null
                        && progress.getStatus() == StudentContentProgress.ProgressStatus.COMPLETED)
                .build();
    }
}
