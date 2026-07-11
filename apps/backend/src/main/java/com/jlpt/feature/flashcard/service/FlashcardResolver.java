/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.service;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.dto.FlashcardResponse;
import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.GrammarPointRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.KanjiRepository;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.student.StudentUser;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Read-model dùng chung cho Flashcard: resolve live mặt thẻ (front/back/furigana/ví dụ/audio/level)
 * theo contentType và map sang {@link FlashcardResponse}. Tách khỏi {@link FlashcardSrsService} để cả
 * phiên ôn (SRS) lẫn Sổ tay ({@link NotebookService}) cùng dùng, tránh lệch logic khi sửa (§3.4).
 */
@Component
@RequiredArgsConstructor
public class FlashcardResolver {

    private static final Kanji.ContentStatus PUBLISHED = Kanji.ContentStatus.PUBLISHED;

    private final VocabularyRepository vocabularyRepository;
    private final KanjiRepository kanjiRepository;
    private final GrammarPointRepository grammarPointRepository;

    /** Nạp một lần các nội dung tích hợp theo loại (tránh N+1) để resolve nhiều thẻ. */
    public ContentMaps loadContentMaps(Collection<Flashcard> cards) {
        List<Vocabulary> vocab = vocabularyRepository.findAllById(idsOfType(cards, Flashcard.ContentType.VOCABULARY));
        List<Kanji> kanji = kanjiRepository.findAllById(idsOfType(cards, Flashcard.ContentType.KANJI));
        List<GrammarPoint> grammar =
                grammarPointRepository.findAllById(idsOfType(cards, Flashcard.ContentType.GRAMMAR));
        return new ContentMaps(
                toMap(vocab, Vocabulary::getId), toMap(kanji, Kanji::getId), toMap(grammar, GrammarPoint::getId));
    }

    public FlashcardResponse toFlashcardResponse(Flashcard card, ContentMaps maps) {
        boolean isDue = isDue(card, LocalDate.now());
        ResolvedCard r = resolve(card, maps);
        return new FlashcardResponse(
                card.getId(),
                card.getDeck() != null ? card.getDeck().getId() : null,
                card.getContentType().name(),
                card.getContentId(),
                r.front(),
                r.back(),
                r.furigana(),
                r.audioUrl(),
                r.jlptLevel(),
                Boolean.TRUE.equals(card.getIsSystem()),
                card.getNextReviewDate(),
                card.getIntervalDays(),
                card.getRepetitionCount(),
                card.getLastRating() != null ? card.getLastRating().getValue() : null,
                card.getAddedReason(),
                isDue);
    }

    /**
     * Resolve live toàn bộ mặt thẻ dùng chung cho cả reveal lẫn danh sách (Notebook §7) — một chỗ
     * switch theo contentType. Nguồn tích hợp đã xóa/không PUBLISHED → {@link ResolvedCard#EMPTY}
     * (FR-FC-34). CUSTOM dùng text thẻ.
     */
    private ResolvedCard resolve(Flashcard card, ContentMaps maps) {
        Long cid = card.getContentId();
        return switch (card.getContentType()) {
            case VOCABULARY -> {
                Vocabulary v = maps.vocab().get(cid);
                yield (v != null && v.getStatus() == PUBLISHED)
                        ? new ResolvedCard(
                                v.getWord(),
                                v.getMeaning(),
                                v.getFurigana(),
                                v.getExampleSentenceJp(),
                                v.getExampleSentenceVi(),
                                v.getAudioUrl(),
                                null,
                                levelName(v.getJlptLevel()))
                        : ResolvedCard.EMPTY;
            }
            case KANJI -> {
                Kanji k = maps.kanji().get(cid);
                yield (k != null && k.getStatus() == PUBLISHED)
                        ? new ResolvedCard(
                                k.getCharacterValue(),
                                k.getMeaning(),
                                null,
                                null,
                                null,
                                null,
                                k.getStrokeOrderUrl(),
                                levelName(k.getJlptLevel()))
                        : ResolvedCard.EMPTY;
            }
            case GRAMMAR -> {
                GrammarPoint g = maps.grammar().get(cid);
                yield (g != null && g.getStatus() == PUBLISHED)
                        ? new ResolvedCard(
                                g.getStructure(),
                                g.getMeaning(),
                                null,
                                g.getExampleSentenceJp(),
                                g.getExampleSentenceVi(),
                                null,
                                null,
                                levelName(g.getJlptLevel()))
                        : ResolvedCard.EMPTY;
            }
            case CUSTOM -> new ResolvedCard(
                    card.getFrontText(), card.getBackText(), null, null, null, null, null, null);
        };
    }

    public static boolean isNew(Flashcard c) {
        return c.getRepetitionCount() != null && c.getRepetitionCount() == 0 && c.getLastReviewedAt() == null;
    }

    public static boolean isDue(Flashcard c, LocalDate today) {
        return c.getNextReviewDate() != null && !c.getNextReviewDate().isAfter(today);
    }

    public static <T> Map<Long, T> toMap(List<T> entities, Function<T, Long> idFn) {
        return entities.stream().collect(Collectors.toMap(idFn, Function.identity(), (a, b) -> a));
    }

    private static String levelName(StudentUser.JlptLevel level) {
        return level != null ? level.name() : null;
    }

    private static Set<Long> idsOfType(Collection<Flashcard> cards, Flashcard.ContentType type) {
        return cards.stream()
                .filter(c -> c.getContentType() == type && c.getContentId() != null)
                .map(Flashcard::getContentId)
                .collect(Collectors.toSet());
    }

    /** Nội dung tích hợp đã nạp sẵn theo loại, để resolve nhiều thẻ mà không N+1. */
    public record ContentMaps(Map<Long, Vocabulary> vocab, Map<Long, Kanji> kanji, Map<Long, GrammarPoint> grammar) {}

    private record ResolvedCard(
            String front,
            String back,
            String furigana,
            String exampleJp,
            String exampleVi,
            String audioUrl,
            String strokeUrl,
            String jlptLevel) {
        static final ResolvedCard EMPTY = new ResolvedCard(null, null, null, null, null, null, null, null);
    }
}
