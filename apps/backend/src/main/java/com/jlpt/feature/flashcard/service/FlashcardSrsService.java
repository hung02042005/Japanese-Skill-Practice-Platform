/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.flashcard.service;

import com.jlpt.feature.flashcard.Flashcard;
import com.jlpt.feature.flashcard.FlashcardDeck;
import com.jlpt.feature.flashcard.dto.ReviewRequest;
import com.jlpt.feature.flashcard.dto.ReviewResultResponse;
import com.jlpt.feature.flashcard.dto.SessionResponse;
import com.jlpt.feature.flashcard.repository.FlashcardRepository;
import com.jlpt.feature.flashcard.service.FlashcardResolver.ContentMaps;
import com.jlpt.feature.learning.Kanji;
import com.jlpt.feature.learning.Vocabulary;
import com.jlpt.feature.learning.VocabularyRepository;
import com.jlpt.feature.learning.VocabularyTopic;
import com.jlpt.feature.learning.VocabularyTopicRepository;
import com.jlpt.feature.student.StudentUser;
import com.jlpt.feature.student.StudentUserRepository;
import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Thuật toán ôn tập Flashcard (SRS): dựng phiên học trộn NEW+REVIEW (§3.6/§3.7), chấm lượt ôn và
 * lịch lại theo SM-2. CRUD sổ/thẻ nằm ở {@link NotebookService}; hai service chia sẻ
 * {@link FlashcardResolver} (resolve mặt thẻ) và {@link FlashcardDeckSupport} (sở hữu + get-or-create).
 */
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

    // Khoá theo (studentId, deck/topic) để tránh race condition khi tạo phiên flashcard đồng thời.
    // static nên dùng chung cho mọi instance của bean trong 1 JVM, nhưng KHÔNG đồng bộ được giữa
    // nhiều instance nếu scale ngang (mỗi instance có bộ khoá riêng) — tương tự trade-off đã ghi
    // trong AuthenticationService.checkAccountTypeAttempts. Entry không bao giờ bị xoá (tăng dần theo số
    // lượng cặp studentId/deck-topic khác nhau từng được truy cập) — chấp nhận được ở quy mô hiện tại.
    private static final Map<String, Object> SESSION_LOCKS = new ConcurrentHashMap<>();

    private final FlashcardRepository flashcardRepository;
    private final VocabularyRepository vocabularyRepository;
    private final VocabularyTopicRepository vocabularyTopicRepository;
    private final StudentUserRepository studentUserRepository;
    private final FlashcardResolver resolver;
    private final FlashcardDeckSupport deckSupport;

    // ── Review (vocab quiz §3.6 / flip §3.2) + gợi ý từ sai (§3.5) ────────────

    public ReviewResultResponse submitReview(Long flashcardId, Long studentId, ReviewRequest request) {
        Flashcard card = deckSupport.ownCardOrThrow(flashcardId, studentId);
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
                ContentMaps maps = resolver.loadContentMaps(wrong);
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
            sessionDeck = deckSupport.ownDeckOrThrow(studentId, deckId);
            sessionLevel = sessionDeck.getJlptLevel();
            sessionTopicTitle = sessionDeck.getName();
            List<Flashcard> cards = flashcardRepository.findByStudentAndDeck(studentId, deckId).stream()
                    .filter(c -> c.getContentType() == Flashcard.ContentType.VOCABULARY && c.getContentId() != null)
                    .toList();
            Map<Long, Vocabulary> vocabMap = FlashcardResolver.toMap(
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
            sessionDeck = deckSupport.getOrCreateDeck(student, jl.name() + "_" + topic.getSlug());
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
        return new SessionResponse(
                sessionId, sessionDeck.getId(), sessionLevel, sessionTopicTitle, chosen.size(), items);
    }

    /** Thứ tự ưu tiên chọn từ vào phiên: chưa học (0) → đến hạn ôn (1) → đã học chưa đến hạn (2). */
    private static int rank(Flashcard c, LocalDate today) {
        if (c == null || FlashcardResolver.isNew(c)) return 0;
        return FlashcardResolver.isDue(c, today) ? 1 : 2;
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

    // ── Helpers phiên ─────────────────────────────────────────────────────────

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
