/* (c) JLPT E-Learning Platform */
package com.jlpt.flashcard.service;

import com.jlpt.entity.Flashcard;
import com.jlpt.entity.FlashcardDeck;
import com.jlpt.entity.GrammarPoint;
import com.jlpt.entity.Kanji;
import com.jlpt.entity.StudentUser;
import com.jlpt.entity.Vocabulary;
import com.jlpt.exception.BadRequestException;
import com.jlpt.exception.DuplicateResourceException;
import com.jlpt.exception.ForbiddenException;
import com.jlpt.exception.ResourceNotFoundException;
import com.jlpt.flashcard.dto.AddFlashcardRequest;
import com.jlpt.flashcard.dto.DeckSummaryResponse;
import com.jlpt.flashcard.dto.DeckUpdateRequest;
import com.jlpt.flashcard.dto.FlashcardResponse;
import com.jlpt.flashcard.dto.FlashcardRevealResponse;
import com.jlpt.flashcard.dto.ReviewDeckAddRequest;
import com.jlpt.flashcard.dto.ReviewDeckAddResponse;
import com.jlpt.flashcard.dto.ReviewRequest;
import com.jlpt.flashcard.dto.ReviewResultResponse;
import com.jlpt.flashcard.dto.SessionResponse;
import com.jlpt.flashcard.repository.FlashcardDeckRepository;
import com.jlpt.flashcard.repository.FlashcardRepository;
import com.jlpt.repository.GrammarPointRepository;
import com.jlpt.repository.KanjiRepository;
import com.jlpt.repository.StudentUserRepository;
import com.jlpt.repository.VocabularyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FlashcardSrsService {

    /** Số thẻ MỚI tối đa mỗi phiên (FR-FC-51). */
    private static final int NEW_CARDS_PER_DAY = 10;

    private static final Kanji.ContentStatus PUBLISHED = Kanji.ContentStatus.PUBLISHED;

    private final FlashcardRepository flashcardRepository;
    private final FlashcardDeckRepository flashcardDeckRepository;
    private final VocabularyRepository vocabularyRepository;
    private final KanjiRepository kanjiRepository;
    private final GrammarPointRepository grammarPointRepository;
    private final StudentUserRepository studentUserRepository;

    // ── Deck CRUD ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DeckSummaryResponse> getDecks(Long studentId) {
        LocalDate today = LocalDate.now();
        // Object[]: {deckId, name, description, jlptLevel, topic, color, isSystem, total, due}
        return flashcardDeckRepository.findDeckSummaries(studentId, today).stream()
                .map(r -> new DeckSummaryResponse(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        (String) r[1],
                        r[7] != null ? ((Number) r[7]).intValue() : 0,
                        r[8] != null ? ((Number) r[8]).intValue() : 0,
                        (String) r[3],
                        (String) r[4],
                        Boolean.TRUE.equals(r[6])))
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
                deck.getId(), deckName, deckName, 0, 0, deck.getJlptLevel(), deck.getTopic(), false);
    }

    public DeckSummaryResponse updateDeck(Long studentId, Long deckId, DeckUpdateRequest request) {
        FlashcardDeck deck = ownDeckOrThrow(studentId, deckId);
        if (Boolean.TRUE.equals(deck.getIsSystem())) {
            throw new ForbiddenException("Không thể sửa sổ tay hệ thống");
        }
        if (request.name() != null
                && !request.name().isBlank()
                && !request.name().equals(deck.getName())) {
            if (flashcardDeckRepository.existsByStudentIdAndName(studentId, request.name())) {
                throw new DuplicateResourceException("Sổ tay '" + request.name() + "' đã tồn tại");
            }
            deck.setName(request.name());
        }
        if (request.description() != null) deck.setDescription(request.description());
        if (request.jlptLevel() != null) deck.setJlptLevel(request.jlptLevel());
        if (request.topic() != null) deck.setTopic(request.topic());
        if (request.color() != null) deck.setColor(request.color());
        deck.setUpdatedAt(LocalDateTime.now());
        flashcardDeckRepository.save(deck);
        return new DeckSummaryResponse(
                deck.getId(), deck.getName(), deck.getName(), 0, 0, deck.getJlptLevel(), deck.getTopic(), false);
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
    public Page<FlashcardResponse> getCards(Long studentId, Long deckId, boolean dueOnly, Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<Flashcard> cards;
        if (dueOnly) {
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

    @Transactional(readOnly = true)
    public FlashcardRevealResponse revealCard(Long flashcardId, Long studentId) {
        Flashcard card = ownCardOrThrow(flashcardId, studentId);
        return buildRevealResponse(card);
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
                deckName = requestedDeckName != null ? requestedDeckName : kanji.getJlptLevel().name() + "_KANJI";
            }
            case GRAMMAR -> {
                GrammarPoint grammar = grammarPointRepository
                        .findById(request.contentId())
                        .orElseThrow(() -> new ResourceNotFoundException("GrammarPoint", request.contentId()));
                deckName = requestedDeckName != null ? requestedDeckName : grammar.getJlptLevel().name() + "_GRAMMAR";
            }
            case CUSTOM -> {
                if (request.frontText() == null || request.backText() == null) {
                    throw new BadRequestException("frontText và backText là bắt buộc cho thẻ tùy chỉnh");
                }
                deckName = request.deckName() != null ? request.deckName() : "Mặc định";
                builder.frontText(request.frontText()).backText(request.backText());
            }
            default -> throw new BadRequestException("contentType không hợp lệ");
        }

        FlashcardDeck deck = request.deckId() != null
                ? ownDeckOrThrow(studentId, request.deckId())
                : getOrCreateDeck(student, deckName);
        builder.deck(deck).deckName(deck.getName());

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
            rating = Flashcard.LastRating.valueOf(request.rating());
        }

        applySm2(card, rating);
        flashcardRepository.save(card);

        boolean suggest = false;
        List<ReviewResultResponse.WrongWord> wrongWords = List.of();
        if (request.isLastCardInSession() && card.getDeck() != null) {
            LocalDateTime sessionStart = LocalDateTime.now().minusHours(2);
            List<Flashcard> wrong = flashcardRepository.findWrongVocabCardsInSession(
                    studentId, card.getDeck().getId(), sessionStart);
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
                card.getNextReviewDate(),
                card.getRepetitionCount(),
                suggest,
                wrongWords);
    }

    /** Xác nhận thêm các từ sai vào sổ "Từ cần ôn lại" (§3.5, FR-FC-43/44). */
    public ReviewDeckAddResponse addWrongWordsToReviewDeck(Long studentId, ReviewDeckAddRequest request) {
        StudentUser student = studentUserRepository.getReferenceById(studentId);
        FlashcardDeck deck = getOrCreateReviewDeck(student);
        int added = 0;
        int skipped = 0;
        for (ReviewDeckAddRequest.Item item : request.items()) {
            Long contentId = item.contentId();
            // Trùng (student, vocabulary, contentId) -> bỏ qua (mỗi nội dung 1 thẻ, FR-FC-31/44).
            if (flashcardRepository
                    .findByStudentAndContent(studentId, Flashcard.ContentType.VOCABULARY, contentId)
                    .isPresent()) {
                skipped++;
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
                    .deckName(deck.getName())
                    .contentType(Flashcard.ContentType.VOCABULARY)
                    .contentId(contentId)
                    .isSystem(false)
                    .nextReviewDate(LocalDate.now())
                    .build());
            added++;
        }
        return new ReviewDeckAddResponse(deck.getId(), deck.getName(), added, skipped);
    }

    // ── Phiên học trộn NEW + REVIEW (§3.6/§3.7) ───────────────────────────────

    @Transactional
    public SessionResponse getSession(Long studentId, Long deckId, String level, String topic, Integer newLimit) {
        int limit = (newLimit != null && newLimit > 0) ? newLimit : NEW_CARDS_PER_DAY;
        LocalDate today = LocalDate.now();
        StudentUser student = studentUserRepository.getReferenceById(studentId);

        List<SessionEntry> newEntries = new ArrayList<>();
        List<SessionEntry> reviewEntries = new ArrayList<>();
        FlashcardDeck sessionDeck;
        List<Vocabulary> distractorPool;

        if (deckId != null) {
            sessionDeck = ownDeckOrThrow(studentId, deckId);
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
                if (isNew(c)) newEntries.add(new SessionEntry(c, v, true));
                else if (isDue(c, today)) reviewEntries.add(new SessionEntry(c, v, false));
            }
            distractorPool = new ArrayList<>(vocabMap.values());
        } else {
            StudentUser.JlptLevel jl = parseLevel(level);
            if (jl == null || topic == null || topic.isBlank()) {
                throw new BadRequestException("Cần deckId, hoặc level + topic hợp lệ");
            }
            sessionDeck = getOrCreateDeck(student, jl.name() + "_" + topic);
            List<Vocabulary> vocabList = vocabularyRepository.findPublishedByLevelAndTopic(PUBLISHED, jl, topic);
            Map<Long, Flashcard> byContent = flashcardRepository
                    .findByStudentAndContentIds(
                            studentId,
                            Flashcard.ContentType.VOCABULARY,
                            vocabList.stream().map(Vocabulary::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(Flashcard::getContentId, Function.identity(), (a, b) -> a));
            for (Vocabulary v : vocabList) {
                Flashcard c = byContent.get(v.getId());
                if (c == null || isNew(c)) newEntries.add(new SessionEntry(c, v, true));
                else if (isDue(c, today)) reviewEntries.add(new SessionEntry(c, v, false));
            }
            distractorPool = vocabList.size() >= 2
                    ? new ArrayList<>(vocabList)
                    : vocabularyRepository.findPublishedByLevel(PUBLISHED, jl, PageRequest.of(0, 30));
        }

        // Giới hạn từ MỚI (FR-FC-51), tạo card row cho thẻ mới được chọn (level+topic, FR-FC-64).
        Collections.shuffle(newEntries);
        List<SessionEntry> chosenNew =
                new ArrayList<>(newEntries.stream().limit(limit).toList());
        for (SessionEntry e : chosenNew) {
            if (e.card == null) {
                e.card = getOrCreateVocabCard(student, sessionDeck, e.vocab);
            }
        }

        // Trộn từng nhóm cho đa dạng, rồi xen kẽ 1 thẻ MỚI → 1 thẻ ÔN TẬP (§3.6).
        Collections.shuffle(chosenNew);
        Collections.shuffle(reviewEntries);
        List<SessionEntry> queue = interleave(chosenNew, reviewEntries);

        Random rnd = new Random();
        List<SessionResponse.QueueItem> items =
                queue.stream().map(e -> toQueueItem(e, distractorPool, rnd)).toList();
        return new SessionResponse(sessionDeck.getId(), chosenNew.size(), reviewEntries.size(), items);
    }

    /** Xen kẽ 1 thẻ MỚI → 1 thẻ ÔN TẬP; phần dư của nhóm dài hơn nối tiếp ở cuối (§3.6). */
    private static List<SessionEntry> interleave(List<SessionEntry> news, List<SessionEntry> reviews) {
        List<SessionEntry> out = new ArrayList<>(news.size() + reviews.size());
        for (int ni = 0, ri = 0; ni < news.size() || ri < reviews.size(); ) {
            if (ni < news.size()) out.add(news.get(ni++));
            if (ri < reviews.size()) out.add(reviews.get(ri++));
        }
        return out;
    }

    // ── SM-2 ──────────────────────────────────────────────────────────────────

    void applySm2(Flashcard card, Flashcard.LastRating rating) {
        if (rating == Flashcard.LastRating.WRONG) {
            card.setRepetitionCount(0);
            card.setIntervalDays(1);
        } else if (rating == Flashcard.LastRating.HARD) {
            card.setIntervalDays(Math.max(1, (int) (card.getIntervalDays() * 1.2)));
        } else {
            if (card.getRepetitionCount() == 0) card.setIntervalDays(1);
            else if (card.getRepetitionCount() == 1) card.setIntervalDays(6);
            else card.setIntervalDays((int) Math.round(card.getIntervalDays() * 2.5));
            card.setRepetitionCount(card.getRepetitionCount() + 1);
        }
        card.setNextReviewDate(LocalDate.now().plusDays(card.getIntervalDays()));
        card.setLastReviewedAt(LocalDateTime.now());
        card.setLastRating(rating);
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
                        .name("Từ cần ôn lại")
                        .isReviewDeck(true)
                        .build()));
    }

    private String normalizeDeckName(String deckName) {
        if (deckName == null || deckName.isBlank()) {
            return null;
        }
        return deckName.trim();
    }

    private Flashcard getOrCreateVocabCard(StudentUser student, FlashcardDeck deck, Vocabulary vocab) {
        return flashcardRepository
                .findByStudentAndContent(student.getId(), Flashcard.ContentType.VOCABULARY, vocab.getId())
                .orElseGet(() -> flashcardRepository.save(Flashcard.builder()
                        .student(student)
                        .deck(deck)
                        .deckName(deck.getName())
                        .contentType(Flashcard.ContentType.VOCABULARY)
                        .contentId(vocab.getId())
                        .isSystem(false)
                        .nextReviewDate(LocalDate.now())
                        .build()));
    }

    private SessionResponse.QueueItem toQueueItem(SessionEntry e, List<Vocabulary> pool, Random rnd) {
        SessionResponse.Front front = new SessionResponse.Front(e.vocab.getWord(), e.vocab.getFurigana());
        SessionResponse.Learn learn = e.isNew
                ? new SessionResponse.Learn(
                        e.vocab.getMeaning(),
                        e.vocab.getExampleSentenceJp(),
                        e.vocab.getExampleSentenceVi(),
                        e.vocab.getAudioUrl())
                : null;
        return new SessionResponse.QueueItem(
                e.card.getId(), e.isNew ? "NEW" : "REVIEW", front, learn, buildQuiz(e.vocab, pool, rnd));
    }

    /** 2 đáp án: nghĩa đúng + 1 distractor từ vocab khác (FR-FC-54), trộn ngẫu nhiên. */
    private SessionResponse.Quiz buildQuiz(Vocabulary target, List<Vocabulary> pool, Random rnd) {
        List<SessionResponse.Option> options = new ArrayList<>();
        options.add(new SessionResponse.Option(target.getId(), target.getMeaning()));
        List<Vocabulary> candidates = pool.stream()
                .filter(v -> !v.getId().equals(target.getId())
                        && v.getMeaning() != null
                        && !v.getMeaning().equals(target.getMeaning()))
                .toList();
        if (!candidates.isEmpty()) {
            Vocabulary d = candidates.get(rnd.nextInt(candidates.size()));
            options.add(new SessionResponse.Option(d.getId(), d.getMeaning()));
        }
        Collections.shuffle(options);
        return new SessionResponse.Quiz(options);
    }

    private FlashcardRevealResponse buildRevealResponse(Flashcard card) {
        ContentMaps maps = loadContentMaps(List.of(card));
        String front = null;
        String back = null;
        String furigana = null;
        String exampleJp = null;
        String exampleVi = null;
        String audioUrl = null;
        String strokeUrl = null;
        switch (card.getContentType()) {
            case VOCABULARY -> {
                Vocabulary v = maps.vocab().get(card.getContentId());
                if (v != null && v.getStatus() == PUBLISHED) {
                    front = v.getWord();
                    back = v.getMeaning();
                    furigana = v.getFurigana();
                    exampleJp = v.getExampleSentenceJp();
                    exampleVi = v.getExampleSentenceVi();
                    audioUrl = v.getAudioUrl();
                }
            }
            case KANJI -> {
                Kanji k = maps.kanji().get(card.getContentId());
                if (k != null && k.getStatus() == PUBLISHED) {
                    front = k.getCharacterValue();
                    back = k.getMeaning();
                    strokeUrl = k.getStrokeOrderUrl();
                }
            }
            case GRAMMAR -> {
                GrammarPoint g = maps.grammar().get(card.getContentId());
                if (g != null && g.getStatus() == PUBLISHED) {
                    front = g.getStructure();
                    back = g.getMeaning();
                    exampleJp = g.getExampleSentenceJp();
                    exampleVi = g.getExampleSentenceVi();
                }
            }
            case CUSTOM -> {
                front = card.getFrontText();
                back = card.getBackText();
            }
        }
        return new FlashcardRevealResponse(
                card.getId(), front, back, furigana, exampleJp, exampleVi, audioUrl, strokeUrl);
    }

    private FlashcardResponse toFlashcardResponse(Flashcard card, ContentMaps maps) {
        boolean isDue = isDue(card, LocalDate.now());
        return new FlashcardResponse(
                card.getId(),
                card.getDeck() != null ? card.getDeck().getId() : null,
                card.getContentType().name(),
                card.getContentId(),
                resolveFront(card, maps),
                Boolean.TRUE.equals(card.getIsSystem()),
                card.getNextReviewDate(),
                card.getIntervalDays(),
                card.getRepetitionCount(),
                card.getLastRating() != null ? card.getLastRating().getValue() : null,
                isDue);
    }

    /** Mặt trước resolve live; null nếu nguồn tích hợp đã xóa/không published (FR-FC-34). */
    private String resolveFront(Flashcard card, ContentMaps maps) {
        Long cid = card.getContentId();
        return switch (card.getContentType()) {
            case VOCABULARY -> {
                Vocabulary v = maps.vocab().get(cid);
                yield (v != null && v.getStatus() == PUBLISHED) ? v.getWord() : null;
            }
            case KANJI -> {
                Kanji k = maps.kanji().get(cid);
                yield (k != null && k.getStatus() == PUBLISHED) ? k.getCharacterValue() : null;
            }
            case GRAMMAR -> {
                GrammarPoint g = maps.grammar().get(cid);
                yield (g != null && g.getStatus() == PUBLISHED) ? g.getStructure() : null;
            }
            case CUSTOM -> card.getFrontText();
        };
    }

    private String buildVocabDeckName(Vocabulary vocab) {
        String level = vocab.getJlptLevel() != null ? vocab.getJlptLevel().name() : "UNKNOWN";
        String topic = (vocab.getTopic() != null && !vocab.getTopic().isBlank()) ? vocab.getTopic() : "その他";
        return level + "_" + topic;
    }

    private StudentUser.JlptLevel parseLevel(String levelStr) {
        if (levelStr == null || levelStr.isBlank()) return null;
        try {
            return StudentUser.JlptLevel.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
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

    /** Mục trong hàng đợi phiên — card có thể chưa tồn tại (level+topic) tới khi được chọn. */
    private static final class SessionEntry {
        private Flashcard card;
        private final Vocabulary vocab;
        private final boolean isNew;

        SessionEntry(Flashcard card, Vocabulary vocab, boolean isNew) {
            this.card = card;
            this.vocab = vocab;
            this.isNew = isNew;
        }
    }
}
