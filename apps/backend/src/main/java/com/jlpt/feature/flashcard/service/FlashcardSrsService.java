/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.service;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.FlashcardConstants;
import com.jlpt.feature.flashcard.FlashcardDeck;
import com.jlpt.feature.flashcard.dto.AddFlashcardRequest;
import com.jlpt.feature.flashcard.dto.DeckSummaryResponse;
import com.jlpt.feature.flashcard.dto.FlashcardResponse;
import com.jlpt.feature.flashcard.dto.FlashcardRevealResponse;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddRequest;
import com.jlpt.feature.flashcard.dto.ReviewDeckAddResponse;
import com.jlpt.feature.flashcard.dto.ReviewRequest;
import com.jlpt.feature.flashcard.dto.ReviewResultResponse;
import com.jlpt.feature.flashcard.dto.SessionResponse;
import com.jlpt.feature.flashcard.repository.FlashcardDeckRepository;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.learning.GrammarPoint;
import com.jlpt.feature.learning.GrammarPointRepository;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.KanjiRepository;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.DuplicateResourceException;
import com.jlpt.shared.exception.ForbiddenException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FlashcardSrsService {

    /** Số thẻ MỚI mặc định mỗi phiên khi client không truyền newLimit (FR-FC-51). */
    private static final int NEW_CARDS_PER_DAY = 10;

    /** Trộn "học rồi kiểm tra": số thẻ MỚI tối thiểu mỗi lô trước khi quiz lại đúng các từ đó (FR-FC-70). */
    private static final int LEARN_BATCH_MIN = 2;

    /** Số thẻ MỚI tối đa mỗi lô (lô dao động 2–3 thẻ). */
    private static final int LEARN_BATCH_MAX = 3;

    /** Trần số TỪ nạp vào phiên — mỗi từ xuất hiện 2 lần (học + kiểm tra), chống quá tải (FR-FC-78). */
    private static final int MAX_NEW = 20;

    /** Ease factor mặc định khi thẻ chưa có giá trị (FR-FC-30). */
    private static final double EASE_DEFAULT = 2.50;

    /** Sàn ease factor — chống interval sụp đổ (FR-FC-24). */
    private static final double EASE_MIN = 1.30;

    /** Trần ease factor (FR-FC-23). */
    private static final double EASE_MAX = 2.50;

    private static final Kanji.ContentStatus PUBLISHED = Kanji.ContentStatus.PUBLISHED;

    private static final Map<String, Object> SESSION_LOCKS = new ConcurrentHashMap<>();

    private final FlashcardRepository flashcardRepository;
    private final FlashcardDeckRepository flashcardDeckRepository;
    private final VocabularyRepository vocabularyRepository;
    private final VocabularyTopicRepository vocabularyTopicRepository;
    private final KanjiRepository kanjiRepository;
    private final GrammarPointRepository grammarPointRepository;
    private final StudentUserRepository studentUserRepository;

    // ── Deck CRUD ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DeckSummaryResponse> getDecks(Long studentId) {
        LocalDate today = LocalDate.now();
        // Object[]: {deckId, name, description, jlptLevel, topic, color, isSystem, total, due, isReviewDeck}
        return flashcardDeckRepository.findDeckSummaries(studentId, today).stream()
                .map(r -> new DeckSummaryResponse(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        r[7] != null ? ((Number) r[7]).intValue() : 0,
                        r[8] != null ? ((Number) r[8]).intValue() : 0,
                        (String) r[3],
                        (String) r[4],
                        Boolean.TRUE.equals(r[6]),
                        Boolean.TRUE.equals(r[9])))
                .toList();
    }

    public DeckSummaryResponse createDeck(Long studentId, String deckName) {
        if (flashcardDeckRepository.existsByStudentIdAndName(studentId, deckName)) {
            // 409 Conflict cho tài nguyên trùng (AGENTS.md §6.3), không phải 400.
            throw new DuplicateResourceException("Sổ tay '" + deckName + "' đã tồn tại");
        }
        StudentUser student = studentUserRepository.getReferenceById(studentId);
        // Deck first-class (V9): deck rỗng là bản ghi thật, KHÔNG cần thẻ giữ chỗ (FR-FC-07).
        FlashcardDeck deck = getOrCreateDeck(student, deckName);
        return new DeckSummaryResponse(
                deck.getId(), deckName, 0, 0, deck.getJlptLevel(), deck.getTopic(), false, false);
    }

    public void deleteDeck(Long studentId, Long deckId) {
        FlashcardDeck deck = ownDeckOrThrow(studentId, deckId);
        if (Boolean.TRUE.equals(deck.getIsSystem())) {
            throw new ForbiddenException("Không thể xóa sổ tay hệ thống");
        }
        // Soft delete (ADR-004): deck + toàn bộ thẻ của deck.
        deck.setIsDeleted(true);
        flashcardDeckRepository.save(deck);
        flashcardRepository.softDeleteByDeckId(deckId);
    }

    // ── Card list / reveal (live-resolve §3.4) ───────────────────────────────

    @Transactional(readOnly = true)
    public Page<FlashcardResponse> getCards(
            Long studentId, Long deckId, boolean dueOnly, String q, String sort, Pageable pageable) {
        LocalDate today = LocalDate.now();
        String needle = q == null ? null : q.trim().toLowerCase();
        String sortKey = normalizeSort(sort);

        // Tìm kiếm server-side (SPEC-notebook): resolve live rồi lọc theo mặt trước — không bị giới
        // hạn bởi paging DB nên không bỏ sót thẻ ngoài trang đầu. Khi không có deckId thì tìm trên
        // toàn bộ thẻ của student (trước đây bỏ qua `q` âm thầm — anti-pattern silent ignore).
        if (needle != null && !needle.isEmpty()) {
            List<Flashcard> all = deckId != null
                    ? flashcardRepository.findByStudentAndDeck(studentId, deckId)
                    : flashcardRepository.findByStudent(studentId);
            ContentMaps maps = loadContentMaps(all);
            List<FlashcardResponse> matched = all.stream()
                    .filter(c -> !dueOnly || isDue(c, today))
                    .map(c -> toFlashcardResponse(c, maps))
                    .filter(r ->
                            r.frontText() != null && r.frontText().toLowerCase().contains(needle))
                    .sorted(responseComparator(sortKey))
                    .toList();
            int from = (int) Math.min(pageable.getOffset(), matched.size());
            int to = Math.min(from + pageable.getPageSize(), matched.size());
            return new PageImpl<>(matched.subList(from, to), pageable, matched.size());
        }

        Page<Flashcard> cards;
        if (deckId != null && !"due".equals(sortKey)) {
            // Sort sổ tay (3B) — deck-scoped; alpha/level join Vocabulary (review deck = vocab-only).
            cards = switch (sortKey) {
                case "recent" -> flashcardRepository.findByDeckOrderByRecent(studentId, deckId, dueOnly, today, pageable);
                case "alpha" -> flashcardRepository.findByDeckOrderByWord(
                        studentId, deckId, PUBLISHED, dueOnly, today, pageable);
                default -> flashcardRepository.findByDeckOrderByLevel(
                        studentId, deckId, PUBLISHED, dueOnly, today, pageable);
            };
        } else if (dueOnly) {
            cards = deckId != null
                    ? flashcardRepository.findDueByDeck(studentId, deckId, today, pageable)
                    : flashcardRepository.findAllDue(studentId, today, pageable);
        } else {
            cards = deckId != null
                    ? flashcardRepository.findAllByDeck(studentId, deckId, pageable)
                    : flashcardRepository.findAllByStudent(studentId, pageable);
        }
        ContentMaps maps = loadContentMaps(cards.getContent());
        List<FlashcardResponse> items = cards.getContent().stream()
                .map(c -> toFlashcardResponse(c, maps))
                // Ẩn thẻ tích hợp có nguồn đã xóa/không published (FR-FC-34); custom luôn có text.
                .filter(r -> r.frontText() != null)
                .toList();
        return new PageImpl<>(items, pageable, cards.getTotalElements());
    }

    /** Soft-delete (ADR-004) nhiều thẻ — quyền sở hữu ép trong query; trả số thẻ đã gỡ (3B). */
    public int bulkDelete(Long studentId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return flashcardRepository.softDeleteByIds(ids, studentId);
    }

    /** Khoá sort hợp lệ; mặc định "due" (lịch ôn) cho client cũ/giá trị lạ. */
    private static String normalizeSort(String sort) {
        if (sort == null) return "due";
        return switch (sort.trim().toLowerCase()) {
            case "recent", "alpha", "level" -> sort.trim().toLowerCase();
            default -> "due";
        };
    }

    /** Comparator FlashcardResponse khớp thứ tự DB cho nhánh tìm kiếm in-memory (recency ≈ id DESC). */
    private static Comparator<FlashcardResponse> responseComparator(String sortKey) {
        return switch (sortKey) {
            case "recent" -> Comparator.comparing(FlashcardResponse::flashcardId).reversed();
            case "alpha" -> Comparator.comparing(
                    FlashcardResponse::frontText, Comparator.nullsLast(Comparator.naturalOrder()));
            case "level" -> Comparator.comparing(
                            FlashcardResponse::jlptLevel, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(FlashcardResponse::frontText, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(
                    FlashcardResponse::nextReviewDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    @Transactional(readOnly = true)
    public FlashcardRevealResponse revealCard(Long flashcardId, Long studentId) {
        Flashcard card = ownCardOrThrow(flashcardId, studentId);
        return buildRevealResponse(card);
    }

    /** Gỡ một thẻ khỏi sổ tay (soft-delete, ADR-004 — SPEC-notebook §5). */
    public void deleteCard(Long studentId, Long flashcardId) {
        Flashcard card = ownCardOrThrow(flashcardId, studentId);
        card.setIsDeleted(true);
        flashcardRepository.save(card);
    }

    // ── Add card (live-resolve: KHÔNG copy text cho thẻ tích hợp, §3.4) ───────

    public FlashcardResponse addCard(Long studentId, AddFlashcardRequest request) {
        Flashcard.ContentType contentType = Flashcard.ContentType.valueOf(request.contentType());

        if (contentType != Flashcard.ContentType.CUSTOM && request.contentId() == null) {
            throw new BadRequestException("contentId is required for integrated flashcards");
        }

        if (contentType != Flashcard.ContentType.CUSTOM) {
            // FR-FC-31: trùng (student, content_type, content_id) -> 409, không tạo trùng.
            if (flashcardRepository
                    .findByStudentAndContent(studentId, contentType, request.contentId())
                    .isPresent()) {
                throw new DuplicateResourceException("Nội dung này đã có trong Flashcard");
            }
        }

        StudentUser student = studentUserRepository.getReferenceById(studentId);
        Flashcard.FlashcardBuilder builder = Flashcard.builder()
                .student(student)
                .contentType(contentType)
                .contentId(request.contentId())
                .isSystem(false)
                .addedReason("manual")
                .nextReviewDate(LocalDate.now());

        String requestedDeckName = normalizeDeckName(request.deckName());
        String deckName;
        switch (contentType) {
            case VOCABULARY -> {
                Vocabulary vocab = vocabularyRepository
                        .findById(request.contentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Vocabulary", request.contentId()));
                deckName = requestedDeckName != null ? requestedDeckName : buildVocabDeckName(vocab);
                // Thẻ tích hợp: front/back NULL, resolve live khi đọc (FR-FC-30).
            }
            case KANJI -> {
                Kanji kanji = kanjiRepository
                        .findById(request.contentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Kanji", request.contentId()));
                deckName = requestedDeckName != null
                        ? requestedDeckName
                        : kanji.getJlptLevel().name() + "_KANJI";
            }
            case GRAMMAR -> {
                GrammarPoint grammar = grammarPointRepository
                        .findById(request.contentId())
                        .orElseThrow(() -> new ResourceNotFoundException("GrammarPoint", request.contentId()));
                deckName = requestedDeckName != null
                        ? requestedDeckName
                        : grammar.getJlptLevel().name() + "_GRAMMAR";
            }
            case CUSTOM -> {
                if (request.frontText() == null || request.backText() == null) {
                    throw new BadRequestException("frontText và backText là bắt buộc cho thẻ tùy chỉnh");
                }
                deckName = requestedDeckName != null ? requestedDeckName : FlashcardConstants.DEFAULT_DECK_NAME;
                builder.frontText(request.frontText()).backText(request.backText());
            }
            default -> throw new BadRequestException("contentType không hợp lệ");
        }

        FlashcardDeck deck = request.deckId() != null
                ? ownDeckOrThrow(studentId, request.deckId())
                : getOrCreateDeck(student, deckName);
        builder.deck(deck);

        Flashcard saved = flashcardRepository.save(builder.build());
        return toFlashcardResponse(saved, loadContentMaps(List.of(saved)));
    }

    // ── Review (vocab quiz §3.6 / flip §3.2) + gợi ý từ sai (§3.5) ────────────

    public ReviewResultResponse submitReview(Long flashcardId, Long studentId, ReviewRequest request) {
        Flashcard card = ownCardOrThrow(flashcardId, studentId);
        boolean isVocab = card.getContentType() == Flashcard.ContentType.VOCABULARY;

        Boolean correct = null;
        Long correctOptionId = null;
        String correctMeaning = null;
        Flashcard.LastRating rating;

        if (isVocab && request.selectedOptionId() != null) {
            // Trắc nghiệm: server tự xác định đúng/sai (FR-FC-55/56). optionId = vocabulary_id.
            correctOptionId = card.getContentId();
            correct = request.selectedOptionId().equals(card.getContentId());
            Vocabulary v = vocabularyRepository.findById(card.getContentId()).orElse(null);
            correctMeaning = v != null ? v.getMeaning() : null;
            rating = correct ? Flashcard.LastRating.EASY : Flashcard.LastRating.WRONG;
        } else {
            // Lật thẻ (kanji/grammar/custom): rating bắt buộc.
            if (request.rating() == null) {
                throw new BadRequestException("Rating phải là easy, hard hoặc wrong");
            }
            // Chấp nhận không phân biệt hoa/thường: spec gửi 'easy|hard|wrong', enum là EASY/HARD/WRONG.
            rating = Flashcard.LastRating.valueOf(request.rating().toUpperCase());
        }

        // Đóng dấu phiên (V17) để cuối phiên gom đúng từ sai của CHÍNH phiên này (thay cửa sổ 2h).
        if (request.sessionId() != null && !request.sessionId().isBlank()) {
            card.setLastSessionId(request.sessionId());
        }
        applySm2(card, rating);
        flashcardRepository.save(card);
        // NFR-FC-05: log mọi lượt đánh giá phiên ôn.
        log.info(
                "Flashcard review: studentId={}, flashcardId={}, rating={}, newInterval={}",
                studentId,
                card.getId(),
                rating.getValue(),
                card.getIntervalDays());

        boolean suggest = false;
        List<ReviewResultResponse.WrongWord> wrongWords = List.of();
        if (request.isLastCardInSession() && card.getLastSessionId() != null) {
            // Gom theo session_id (V17): chính xác các từ sai của phiên này, không phụ thuộc thời gian.
            List<Flashcard> wrong =
                    flashcardRepository.findWrongVocabCardsInSession(studentId, card.getLastSessionId());
            if (!wrong.isEmpty()) {
                suggest = true;
                ContentMaps maps = loadContentMaps(wrong);
                wrongWords = wrong.stream()
                        .filter(c -> c.getContentId() != null)
                        .map(c -> {
                            Vocabulary v = maps.vocab().get(c.getContentId());
                            return new ReviewResultResponse.WrongWord(
                                    "vocabulary", c.getContentId(), v != null ? v.getWord() : null);
                        })
                        .toList();
            }
        }

        return new ReviewResultResponse(
                card.getId(),
                correct,
                correctOptionId,
                correctMeaning,
                rating.getValue(),
                card.getIntervalDays(),
                card.getEaseFactor() != null ? card.getEaseFactor().doubleValue() : null,
                card.getNextReviewDate(),
                card.getRepetitionCount(),
                suggest,
                wrongWords);
    }

    /** Xác nhận thêm các từ sai vào sổ "Từ cần ôn lại" (§3.5, FR-FC-43/44). */
    public ReviewDeckAddResponse addWrongWordsToReviewDeck(Long studentId, ReviewDeckAddRequest request) {
        StudentUser student = studentUserRepository.getReferenceById(studentId);
        FlashcardDeck deck = getOrCreateReviewDeck(student);
        // Nguồn thẻ (SPEC-notebook §7): 'wrong' từ phiên ôn, 'manual' từ Từ điển. Mặc định 'manual'.
        String reason = (request.reason() != null && !request.reason().isBlank()) ? request.reason() : "manual";
        int added = 0;
        int skipped = 0;
        for (ReviewDeckAddRequest.Item item : request.items()) {
            Long contentId = item.contentId();
            // Mỗi nội dung chỉ 1 thẻ (FR-FC-31/44). Nếu thẻ đã tồn tại ở sổ khác (vd đã học
            // trong phiên level_topic) thì CHUYỂN sang sổ "Từ cần ôn lại"; chỉ bỏ qua khi đã ở
            // sẵn trong sổ này — nếu không, lưu thủ công từ Từ điển sẽ "im lặng" không vào sổ.
            Optional<Flashcard> existing =
                    flashcardRepository.findByStudentAndContent(studentId, Flashcard.ContentType.VOCABULARY, contentId);
            if (existing.isPresent()) {
                Flashcard card = existing.get();
                if (card.getDeck() != null && deck.getId().equals(card.getDeck().getId())) {
                    skipped++;
                } else {
                    card.setDeck(deck);
                    card.setAddedReason(reason);
                    flashcardRepository.save(card);
                    added++;
                }
                continue;
            }
            Vocabulary vocab = vocabularyRepository.findById(contentId).orElse(null);
            if (vocab == null) {
                skipped++;
                continue;
            }
            flashcardRepository.save(Flashcard.builder()
                    .student(student)
                    .deck(deck)
                    .contentType(Flashcard.ContentType.VOCABULARY)
                    .contentId(contentId)
                    .isSystem(false)
                    .addedReason(reason)
                    .nextReviewDate(LocalDate.now())
                    .build());
            added++;
        }
        return new ReviewDeckAddResponse(deck.getId(), deck.getName(), added, skipped);
    }

    // ── Phiên học trộn NEW + REVIEW (§3.6/§3.7) ───────────────────────────────

    @Transactional
    public SessionResponse getSession(Long studentId, Long deckId, Long topicId, Integer newLimit) {
        String lockKey = studentId + ":" + (deckId != null ? "deck:" + deckId : "topic:" + topicId);
        Object lock = SESSION_LOCKS.computeIfAbsent(lockKey, ignored -> new Object());
        synchronized (lock) {
            return getSessionLocked(studentId, deckId, topicId, newLimit);
        }
    }

    private SessionResponse getSessionLocked(Long studentId, Long deckId, Long topicId, Integer newLimit) {
        int limit = (newLimit != null && newLimit > 0) ? newLimit : NEW_CARDS_PER_DAY;
        LocalDate today = LocalDate.now();
        StudentUser student = studentUserRepository.getReferenceById(studentId);

        FlashcardDeck sessionDeck;
        List<Vocabulary> distractorPool;
        String sessionLevel;
        String sessionTopicTitle;
        // Tập từ ứng viên cho phiên + thẻ tương ứng (thẻ có thể chưa tồn tại với từ chưa học).
        List<WordCard> words = new ArrayList<>();

        if (deckId != null) {
            sessionDeck = ownDeckOrThrow(studentId, deckId);
            sessionLevel = sessionDeck.getJlptLevel();
            sessionTopicTitle = sessionDeck.getName();
            List<Flashcard> cards = flashcardRepository.findByStudentAndDeck(studentId, deckId).stream()
                    .filter(c -> c.getContentType() == Flashcard.ContentType.VOCABULARY && c.getContentId() != null)
                    .toList();
            Map<Long, Vocabulary> vocabMap = toMap(
                    vocabularyRepository.findAllById(
                            cards.stream().map(Flashcard::getContentId).collect(Collectors.toSet())),
                    Vocabulary::getId);
            for (Flashcard c : cards) {
                Vocabulary v = vocabMap.get(c.getContentId());
                if (v == null || v.getStatus() != PUBLISHED) continue; // FR-FC-34/65
                words.add(new WordCard(v, c));
            }
            distractorPool = new ArrayList<>(vocabMap.values());
        } else {
            // FR-redo-topic: phiên theo giáo trình dùng topicId (khoá chủ đề duy nhất).
            if (topicId == null) {
                throw new BadRequestException("Cần deckId hoặc topicId hợp lệ");
            }
            VocabularyTopic topic = vocabularyTopicRepository
                    .findById(topicId)
                    .filter(t -> t.getStatus() == PUBLISHED)
                    .orElseThrow(() -> new ResourceNotFoundException("Chủ đề", topicId));
            StudentUser.JlptLevel jl = topic.getJlptLevel();
            sessionLevel = jl != null ? jl.name() : null;
            sessionTopicTitle = topic.getTitleVi();
            sessionDeck = getOrCreateDeck(student, jl.name() + "_" + topic.getSlug());
            List<Vocabulary> vocabList = vocabularyRepository.findPublishedByTopicId(PUBLISHED, topicId);
            Map<Long, Flashcard> byContent = flashcardRepository
                    .findByStudentAndContentIds(
                            studentId,
                            Flashcard.ContentType.VOCABULARY,
                            vocabList.stream().map(Vocabulary::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(Flashcard::getContentId, Function.identity(), (a, b) -> a));
            for (Vocabulary v : vocabList) {
                words.add(new WordCard(v, byContent.get(v.getId())));
            }
            distractorPool = vocabList.size() >= 2
                    ? new ArrayList<>(vocabList)
                    : vocabularyRepository.findPublishedByLevel(PUBLISHED, jl, PageRequest.of(0, 30));
        }

        // Ưu tiên từ CHƯA HỌC → ĐẾN HẠN ÔN → còn lại; trộn trong từng nhóm cho đa dạng.
        Collections.shuffle(words);
        words.sort(Comparator.comparingInt(w -> rank(w.card, today)));
        // Mỗi từ xuất hiện 2 lần (học rồi kiểm tra) nên giới hạn số TỪ theo trần.
        List<WordCard> chosen =
                new ArrayList<>(words.stream().limit(Math.min(limit, MAX_NEW)).toList());
        // Tạo card row cho mọi từ được chọn → có flashcardId để chấm thẻ kiểm tra (FR-FC-64).
        // Các từ chưa có thẻ đã được xác định null từ byContent/vocabMap ở trên (cùng transaction)
        // nên KHÔNG tìm lại từng từ; dựng thẳng rồi saveAll một lần (gộp INSERT, bỏ N+1 SELECT).
        List<Flashcard> toCreate = new ArrayList<>();
        List<WordCard> needCard = new ArrayList<>();
        for (WordCard w : chosen) {
            if (w.card == null) {
                toCreate.add(Flashcard.builder()
                        .student(student)
                        .deck(sessionDeck)
                        .contentType(Flashcard.ContentType.VOCABULARY)
                        .contentId(w.vocab.getId())
                        .isSystem(false)
                        .addedReason("learn")
                        .nextReviewDate(today)
                        .build());
                needCard.add(w);
            }
        }
        if (!toCreate.isEmpty()) {
            List<Flashcard> saved = flashcardRepository.saveAll(toCreate);
            for (int i = 0; i < saved.size(); i++) {
                needCard.get(i).card = saved.get(i);
            }
        }

        // Trộn "học rồi kiểm tra" (FR-FC-70..72): mỗi lô học LEARN_BATCH (2–3) thẻ MỚI (lật), rồi cho
        // ngay thẻ ÔN TẬP (trắc nghiệm) của đúng các từ vừa học để củng cố, trước khi sang lô kế.
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<SessionResponse.QueueItem> items = new ArrayList<>(chosen.size() * 2);
        for (int i = 0; i < chosen.size(); ) {
            int batch =
                    Math.min(LEARN_BATCH_MIN + rnd.nextInt(LEARN_BATCH_MAX - LEARN_BATCH_MIN + 1), chosen.size() - i);
            for (int j = i; j < i + batch; j++) { // phần học: lật thẻ
                WordCard w = chosen.get(j);
                items.add(toQueueItem(new SessionEntry(w.card, w.vocab, true), distractorPool));
            }
            for (int j = i; j < i + batch; j++) { // phần kiểm tra: trắc nghiệm đúng các từ đó
                WordCard w = chosen.get(j);
                items.add(toQueueItem(new SessionEntry(w.card, w.vocab, false), distractorPool));
            }
            i += batch;
        }
        // session_id (V17): mỗi lượt review trong phiên đóng dấu UUID này lên thẻ → cuối phiên gom
        // đúng các từ sai của CHÍNH phiên này, thay cửa sổ thời gian 2h dễ gom nhầm.
        String sessionId = UUID.randomUUID().toString();
        return new SessionResponse(sessionId, sessionDeck.getId(), sessionLevel, sessionTopicTitle, chosen.size(), items);
    }

    /** Thứ tự ưu tiên chọn từ vào phiên: chưa học (0) → đến hạn ôn (1) → đã học chưa đến hạn (2). */
    private static int rank(Flashcard c, LocalDate today) {
        if (c == null || isNew(c)) return 0;
        return isDue(c, today) ? 1 : 2;
    }

    // ── SM-2 ──────────────────────────────────────────────────────────────────

    void applySm2(Flashcard card, Flashcard.LastRating rating) {
        double ease = card.getEaseFactor() != null ? card.getEaseFactor().doubleValue() : EASE_DEFAULT;
        int rep = card.getRepetitionCount() != null ? card.getRepetitionCount() : 0;

        switch (rating) {
            case WRONG -> {
                // quality 0: ease giảm theo công thức SM-2, sàn 1.3; reset chuỗi (FR-FC-21/24).
                ease = clampEase(applyEaseDelta(ease, 0));
                card.setRepetitionCount(0);
                card.setIntervalDays(1);
            }
            case HARD -> {
                // quality 2: ease GIỮ NGUYÊN, interval = MAX(1, previous) (FR-FC-22).
                card.setIntervalDays(Math.max(1, card.getIntervalDays()));
            }
            case EASY -> {
                // quality 5: ease + 0.1 (clamp ≤ 2.5) rồi mới tính interval (FR-FC-23).
                ease = clampEase(applyEaseDelta(ease, 5));
                if (rep == 0) card.setIntervalDays(1);
                else if (rep == 1) card.setIntervalDays(6);
                else card.setIntervalDays((int) Math.round(card.getIntervalDays() * ease));
                card.setRepetitionCount(rep + 1);
            }
        }

        card.setEaseFactor(BigDecimal.valueOf(ease).setScale(2, RoundingMode.HALF_UP));
        card.setNextReviewDate(LocalDate.now().plusDays(card.getIntervalDays()));
        card.setLastReviewedAt(LocalDateTime.now());
        card.setLastRating(rating);
    }

    /** Delta ease theo công thức SM-2: {@code ease + (0.1 - (5-q)*(0.08 + (5-q)*0.02))}. */
    private static double applyEaseDelta(double ease, int quality) {
        int q = 5 - quality;
        return ease + (0.1 - q * (0.08 + q * 0.02));
    }

    /** Giữ ease trong [EASE_MIN, EASE_MAX] (FR-FC-23/24). */
    private static double clampEase(double ease) {
        return Math.max(EASE_MIN, Math.min(EASE_MAX, ease));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private FlashcardDeck ownDeckOrThrow(Long studentId, Long deckId) {
        return flashcardDeckRepository
                .findByIdAndStudentId(deckId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Sổ tay", deckId));
    }

    private Flashcard ownCardOrThrow(Long flashcardId, Long studentId) {
        if (!flashcardRepository.existsByIdAndStudentId(flashcardId, studentId)) {
            throw new ForbiddenException("Flashcard không thuộc về bạn");
        }
        return flashcardRepository
                .findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", flashcardId));
    }

    private FlashcardDeck getOrCreateDeck(StudentUser student, String name) {
        return flashcardDeckRepository
                .findByStudentIdAndName(student.getId(), name)
                .orElseGet(() -> {
                    FlashcardDeck.FlashcardDeckBuilder b =
                            FlashcardDeck.builder().student(student).name(name);
                    // Tách jlpt_level/topic từ pattern "{level}_{topic}" để deck có metadata.
                    if (name.matches("N[1-5]_.+")) {
                        b.jlptLevel(name.substring(0, 2)).topic(name.substring(3));
                    }
                    return flashcardDeckRepository.save(b.build());
                });
    }

    private FlashcardDeck getOrCreateReviewDeck(StudentUser student) {
        return flashcardDeckRepository
                .findByStudentIdAndIsReviewDeckTrue(student.getId())
                .orElseGet(() -> flashcardDeckRepository.save(FlashcardDeck.builder()
                        .student(student)
                        .name(FlashcardConstants.REVIEW_DECK_NAME)
                        .isReviewDeck(true)
                        .build()));
    }

    private String normalizeDeckName(String deckName) {
        if (deckName == null || deckName.isBlank()) {
            return null;
        }
        return deckName.trim();
    }

    private SessionResponse.QueueItem toQueueItem(SessionEntry e, List<Vocabulary> pool) {
        SessionResponse.Front front = new SessionResponse.Front(e.vocab.getWord(), e.vocab.getFurigana());
        SessionResponse.Learn learn = e.isNew
                ? new SessionResponse.Learn(
                        e.vocab.getMeaning(),
                        e.vocab.getExampleSentenceJp(),
                        e.vocab.getExampleSentenceVi(),
                        e.vocab.getAudioUrl())
                : null;
        // Thẻ MỚI chỉ lật học nghĩa (không quiz); thẻ ÔN TẬP mới có trắc nghiệm.
        SessionResponse.Quiz quiz = e.isNew ? null : buildQuiz(e.vocab, pool);
        return new SessionResponse.QueueItem(e.card.getId(), e.isNew ? "NEW" : "REVIEW", front, learn, quiz);
    }

    /** 2 đáp án: nghĩa đúng + 1 distractor từ vocab khác (FR-FC-54), trộn ngẫu nhiên. */
    private SessionResponse.Quiz buildQuiz(Vocabulary target, List<Vocabulary> pool) {
        List<SessionResponse.Option> options = new ArrayList<>();
        options.add(new SessionResponse.Option(target.getId(), target.getMeaning()));
        List<Vocabulary> candidates = pool.stream()
                .filter(v -> !v.getId().equals(target.getId())
                        && v.getMeaning() != null
                        && !v.getMeaning().equals(target.getMeaning()))
                .toList();
        if (!candidates.isEmpty()) {
            Vocabulary d = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            options.add(new SessionResponse.Option(d.getId(), d.getMeaning()));
        }
        Collections.shuffle(options);
        return new SessionResponse.Quiz(options);
    }

    private FlashcardRevealResponse buildRevealResponse(Flashcard card) {
        ResolvedCard r = resolve(card, loadContentMaps(List.of(card)));
        return new FlashcardRevealResponse(
                card.getId(),
                r.front(),
                r.back(),
                r.furigana(),
                r.exampleJp(),
                r.exampleVi(),
                r.audioUrl(),
                r.strokeUrl());
    }

    /**
     * Resolve live toàn bộ mặt thẻ (front/back/furigana/ví dụ/audio/stroke/level) dùng chung cho cả
     * reveal lẫn danh sách (Notebook §7) — một chỗ switch theo contentType, tránh lệch logic khi sửa.
     * Nguồn tích hợp đã xóa/không PUBLISHED → {@link ResolvedCard#EMPTY} (FR-FC-34). CUSTOM dùng text thẻ.
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

    private FlashcardResponse toFlashcardResponse(Flashcard card, ContentMaps maps) {
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

    private static String levelName(StudentUser.JlptLevel level) {
        return level != null ? level.name() : null;
    }

    private String buildVocabDeckName(Vocabulary vocab) {
        String level = vocab.getJlptLevel() != null ? vocab.getJlptLevel().name() : "UNKNOWN";
        String topic = vocab.getTopicRef() != null ? vocab.getTopicRef().getSlug() : "other";
        return level + "_" + topic;
    }

    private static boolean isNew(Flashcard c) {
        return c.getRepetitionCount() != null && c.getRepetitionCount() == 0 && c.getLastReviewedAt() == null;
    }

    private static boolean isDue(Flashcard c, LocalDate today) {
        return c.getNextReviewDate() != null && !c.getNextReviewDate().isAfter(today);
    }

    // ── Batch live-resolve (tránh N+1, tái dùng pattern loadContentMaps) ──────

    private ContentMaps loadContentMaps(Collection<Flashcard> cards) {
        List<Vocabulary> vocab = vocabularyRepository.findAllById(idsOfType(cards, Flashcard.ContentType.VOCABULARY));
        List<Kanji> kanji = kanjiRepository.findAllById(idsOfType(cards, Flashcard.ContentType.KANJI));
        List<GrammarPoint> grammar =
                grammarPointRepository.findAllById(idsOfType(cards, Flashcard.ContentType.GRAMMAR));
        return new ContentMaps(
                toMap(vocab, Vocabulary::getId), toMap(kanji, Kanji::getId), toMap(grammar, GrammarPoint::getId));
    }

    private static Set<Long> idsOfType(Collection<Flashcard> cards, Flashcard.ContentType type) {
        return cards.stream()
                .filter(c -> c.getContentType() == type && c.getContentId() != null)
                .map(Flashcard::getContentId)
                .collect(Collectors.toSet());
    }

    private static <T> Map<Long, T> toMap(List<T> entities, Function<T, Long> idFn) {
        return entities.stream().collect(Collectors.toMap(idFn, Function.identity(), (a, b) -> a));
    }

    private record ContentMaps(Map<Long, Vocabulary> vocab, Map<Long, Kanji> kanji, Map<Long, GrammarPoint> grammar) {}

    /** Mục trong hàng đợi phiên — mỗi từ sinh 1 mục NEW (lật) và 1 mục REVIEW (quiz). */
    private static final class SessionEntry {
        private final Flashcard card;
        private final Vocabulary vocab;
        private final boolean isNew;

        SessionEntry(Flashcard card, Vocabulary vocab, boolean isNew) {
            this.card = card;
            this.vocab = vocab;
            this.isNew = isNew;
        }
    }

    /** Từ ứng viên + thẻ tương ứng; card được tạo lười (level+topic) khi từ được chọn vào phiên. */
    private static final class WordCard {
        private final Vocabulary vocab;
        private Flashcard card;

        WordCard(Vocabulary vocab, Flashcard card) {
            this.vocab = vocab;
            this.card = card;
        }
    }
}
