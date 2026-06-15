# SPEC CHI TIẾT — Người 4: Flashcard SRS + Dictionary + Bookmark
## JLPT E-Learning System v2.0
### Ngày: 2026-06-09 | Branch: feat-flashcard-srs, feat-dictionary-bookmark

> **Đọc trước:** `SPEC_CHUNG_FLASHCARD_SRS.md` — phân tích DB, SM-2 algorithm, security rules
> **Đọc kèm:** `CLAUDE.md` (ADR-004 Soft Delete, ADR-005 DTO Pattern, ADR-008 Global Exception)

---

## PHẦN 0 — PHÂN TÍCH DB & XÁC NHẬN KHÔNG XUNG ĐỘT

### Kết luận kiểm tra

**✅ Ý tưởng "flashcard chia theo topic"** — KHÔNG xung đột
- `Vocabulary.topic` (VARCHAR 100) đã tồn tại, đủ dữ liệu để nhóm deck
- `Flashcard.deckName` (VARCHAR 255) lưu pattern `"{level}_{topic}"` — không cần field mới

**✅ Ý tưởng "lấy flashcard từ kho vocab"** — KHÔNG xung đột
- `Flashcard.contentType` (KANJI/VOCABULARY/GRAMMAR/CUSTOM) + `contentId` là polymorphic ref đủ mạnh
- Không cần join table hay foreign key mới

**✅ Ý tưởng "gợi ý bookmark khi trả lời sai"** — KHÔNG xung đột
- `StudentContentProgress.isBookmarked` + `bookmarkNote` + `bookmarkedAt` đã đủ để lưu bookmark
- Không cần bảng `bookmarks` riêng — BookmarkService sẽ thao tác trên `StudentContentProgress`

**⚠️ Lưu ý nhỏ — ContentType mismatch**
- `Flashcard.ContentType` có `CUSTOM` (không có trong `StudentContentProgress.ContentType`)
- `StudentContentProgress.ContentType` có `LESSON`, `KANA` (không có trong `Flashcard.ContentType`)
- **Giải pháp:** BookmarkService chỉ xử lý VOCABULARY, KANJI, GRAMMAR — không bookmark CUSTOM/KANA qua endpoint này

---

## PHẦN 1 — DANH SÁCH FILE

| # | File | Package | Loại | Ghi chú |
|---|------|---------|------|---------|
| 1 | `FlashcardRepository.java` | `repository` | Repository | Query SM-2, deck listing |
| 2 | `FlashcardSrsService.java` | `service` | Service | SM-2 logic hoàn chỉnh |
| 3 | `DictionaryService.java` | `service` | Service | Multi-source search |
| 4 | `BookmarkService.java` | `service` | Service | Thao tác isBookmarked |
| 5 | `StudentFlashcardController.java` | `controller/student` | Controller | Flashcard endpoints |
| 6 | `StudentDictionaryController.java` | `controller/student` | Controller | Dictionary search |
| 7 | `StudentBookmarkController.java` | `controller/student` | Controller | Bookmark CRUD |
| 8 | `FlashcardResponse.java` | `dto/response` | DTO | Card list item |
| 9 | `FlashcardRevealResponse.java` | `dto/response` | DTO | Card back side |
| 10 | `ReviewRequest.java` | `dto/request` | DTO | Submit rating |
| 11 | `ReviewResultResponse.java` | `dto/response` | DTO | SM-2 result + bookmark suggestion |
| 12 | `SearchResponse.java` | `dto/response` | DTO | Multi-source search result |
| 13 | `BookmarkRequest.java` | `dto/request` | DTO | Add/update bookmark |
| 14 | `BookmarkResponse.java` | `dto/response` | DTO | Bookmark item |
| 15 | `DeckSummaryResponse.java` | `dto/response` | DTO | Deck metadata |

---

## PHẦN 2 — DTO SPECIFICATION

### 2.1 FlashcardResponse.java
```java
// GET /api/flashcards — card list item (front chỉ)
public record FlashcardResponse(
    Long       flashcardId,
    String     deckName,          // "N5_食べ物"
    String     contentType,       // "VOCABULARY" | "KANJI" | "GRAMMAR" | "CUSTOM"
    Long       contentId,         // null nếu CUSTOM
    String     frontText,         // mặt trước (từ/kanji/cấu trúc ngữ pháp)
    boolean    isSystem,          // true = card do hệ thống tạo
    LocalDate  nextReviewDate,
    Integer    intervalDays,
    Integer    repetitionCount,
    String     lastRating         // "EASY" | "HARD" | "WRONG" | null
) {}
```

### 2.2 FlashcardRevealResponse.java
```java
// GET /api/flashcards/{id}/reveal — mặt sau card
public record FlashcardRevealResponse(
    Long   flashcardId,
    String frontText,
    String backText,           // nghĩa/reading/ví dụ
    // enriched từ content source (Vocabulary/Kanji/Grammar)
    String furigana,           // null nếu không phải vocab/kanji
    String exampleSentenceJp,
    String exampleSentenceVi,
    String audioUrl,           // null nếu không có
    String strokeOrderUrl      // null nếu không phải kanji
) {}
```

### 2.3 ReviewRequest.java
```java
// POST /api/flashcards/{id}/review
public record ReviewRequest(
    @NotNull
    @Pattern(regexp = "EASY|HARD|WRONG")
    String rating,

    boolean isLastCardInSession  // frontend báo đây là card cuối của topic session
) {}
```

### 2.4 ReviewResultResponse.java
```java
// Response sau khi submit review
public record ReviewResultResponse(
    Long      flashcardId,
    String    rating,
    Integer   newIntervalDays,
    LocalDate nextReviewDate,
    Integer   repetitionCount,
    // Bookmark suggestion — chỉ có giá trị khi isLastCardInSession=true
    boolean   suggestBookmark,
    List<BookmarkSuggestionItem> suggestedBookmarks
) {
    public record BookmarkSuggestionItem(
        String contentType,
        Long   contentId,
        String frontText    // hiển thị cho student biết đây là từ gì
    ) {}
}
```

### 2.5 DeckSummaryResponse.java
```java
// GET /api/flashcard-decks — deck listing
public record DeckSummaryResponse(
    String deckName,       // "N5_食べ物"
    String displayName,    // "N5 · Ăn uống" (localized label)
    int    totalCards,
    int    dueToday,       // số card cần ôn hôm nay
    String jlptLevel,      // "N5"
    String topic           // "食べ物" — null nếu deckName là "Mặc định"
) {}
```

### 2.6 SearchResponse.java
```java
// GET /api/dictionary/search?q=...
public record SearchResponse(
    String            keyword,
    List<VocabItem>   vocabulary,
    List<KanjiItem>   kanji,
    List<GrammarItem> grammar,
    List<LessonItem>  lessons
) {
    public record VocabItem(
        Long   id, String word, String furigana,
        String meaning, String wordType, String jlptLevel, String topic
    ) {}
    public record KanjiItem(
        Long   id, String character, String meaning,
        String onyomi, String kunyomi, String jlptLevel
    ) {}
    public record GrammarItem(
        Long   id, String structure, String meaning,
        String formula, String jlptLevel
    ) {}
    public record LessonItem(
        Long   id, String title, String jlptLevel, String lessonType
    ) {}
}
```

### 2.7 BookmarkRequest.java
```java
// POST /api/bookmarks
public record BookmarkRequest(
    @NotNull
    @Pattern(regexp = "VOCABULARY|KANJI|GRAMMAR")
    String contentType,

    @NotNull
    Long   contentId,

    @Size(max = 500)
    String note           // ghi chú cá nhân (có thể null)
) {}
```

### 2.8 BookmarkResponse.java
```java
// GET /api/bookmarks — list item
public record BookmarkResponse(
    Long          progressId,
    String        contentType,
    Long          contentId,
    String        displayText,   // tên/từ/kanji để hiển thị
    String        note,          // bookmarkNote
    LocalDateTime bookmarkedAt,
    String        jlptLevel
) {}
```

---

## PHẦN 3 — REPOSITORY

### FlashcardRepository.java

```java
@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    // Lấy tất cả deck của student (dùng cho GET /api/flashcard-decks)
    @Query("""
        SELECT f.deckName, COUNT(f), 
               SUM(CASE WHEN f.nextReviewDate <= :today THEN 1 ELSE 0 END)
        FROM Flashcard f
        WHERE f.student.id = :studentId
        GROUP BY f.deckName
    """)
    List<Object[]> findDeckSummaries(@Param("studentId") Long studentId,
                                     @Param("today") LocalDate today);

    // Card trong deck — có filter dueOnly
    @Query("""
        SELECT f FROM Flashcard f
        WHERE f.student.id = :studentId
          AND f.deckName = :deckName
          AND (:dueOnly = false OR f.nextReviewDate <= :today)
        ORDER BY f.nextReviewDate ASC
    """)
    Page<Flashcard> findByDeck(@Param("studentId") Long studentId,
                               @Param("deckName") String deckName,
                               @Param("dueOnly") boolean dueOnly,
                               @Param("today") LocalDate today,
                               Pageable pageable);

    // Kiểm tra card thuộc student (security check)
    boolean existsByIdAndStudentId(Long flashcardId, Long studentId);

    // Tìm card WRONG trong session (cho bookmark suggestion)
    @Query("""
        SELECT f FROM Flashcard f
        WHERE f.student.id = :studentId
          AND f.deckName = :deckName
          AND f.lastRating = 'WRONG'
          AND f.lastReviewedAt >= :sessionStart
    """)
    List<Flashcard> findWrongCardsInSession(@Param("studentId") Long studentId,
                                            @Param("deckName") String deckName,
                                            @Param("sessionStart") LocalDateTime sessionStart);

    // Kiểm tra đã có card cho content này chưa (tránh duplicate)
    boolean existsByStudentIdAndContentTypeAndContentId(
        Long studentId, Flashcard.ContentType contentType, Long contentId);
}
```

---

## PHẦN 4 — SERVICE SPECIFICATION

### 4.1 FlashcardSrsService.java

```java
@Service
@Transactional
public class FlashcardSrsService {

    // Injected repositories (đọc từ Người 1, 3)
    private final FlashcardRepository flashcardRepository;
    private final VocabularyRepository vocabularyRepository;     // từ Người 1
    private final KanjiRepository kanjiRepository;               // từ Người 1
    private final GrammarPointRepository grammarPointRepository; // từ Người 1

    /**
     * GET /api/flashcard-decks
     * Trả danh sách deck với metadata (total, dueToday)
     * Tự động tạo system deck từ vocab topics nếu student chưa có card
     */
    public List<DeckSummaryResponse> getDecks(Long studentId) { ... }

    /**
     * GET /api/flashcards?deckName=&dueOnly=true
     * Trả danh sách card FRONT ONLY (chưa reveal)
     * Nếu dueOnly=true chỉ trả card có nextReviewDate <= hôm nay
     */
    public Page<FlashcardResponse> getCards(Long studentId, String deckName,
                                            boolean dueOnly, Pageable pageable) { ... }

    /**
     * GET /api/flashcards/{id}/reveal
     * Reveal mặt sau card — enrich từ content source
     * Kiểm tra flashcard thuộc student trước khi reveal
     */
    public FlashcardRevealResponse revealCard(Long flashcardId, Long studentId) { ... }

    /**
     * POST /api/flashcards/{id}/review
     * Submit đánh giá SM-2, cập nhật interval/nextReviewDate
     * Nếu isLastCardInSession=true → tìm wrong cards → trả suggestedBookmarks
     */
    public ReviewResultResponse submitReview(Long flashcardId, Long studentId,
                                             ReviewRequest request) { ... }

    /**
     * POST /api/flashcards
     * Thêm card mới từ content source (Vocab/Kanji/Grammar)
     * Idempotent: nếu đã có card cho contentId này → trả lại card hiện có
     * Tự động set deckName = "{jlptLevel}_{topic}" cho Vocabulary
     */
    public FlashcardResponse addCard(Long studentId, AddFlashcardRequest request) { ... }

    // === PRIVATE HELPERS ===

    // SM-2 calculation
    private void applySm2(Flashcard card, LastRating rating) {
        LocalDate today = LocalDate.now();
        if (rating == LastRating.WRONG) {
            card.setRepetitionCount(0);
            card.setIntervalDays(1);
        } else if (rating == LastRating.HARD) {
            card.setIntervalDays(Math.max(1, (int)(card.getIntervalDays() * 1.2)));
        } else { // EASY
            if (card.getRepetitionCount() == 0)      card.setIntervalDays(1);
            else if (card.getRepetitionCount() == 1) card.setIntervalDays(6);
            else card.setIntervalDays((int)Math.round(card.getIntervalDays() * 2.5));
            card.setRepetitionCount(card.getRepetitionCount() + 1);
        }
        card.setNextReviewDate(today.plusDays(card.getIntervalDays()));
        card.setLastReviewedAt(LocalDateTime.now());
        card.setLastRating(rating);
    }

    // Enrich FlashcardRevealResponse từ content source
    private FlashcardRevealResponse buildRevealResponse(Flashcard card) { ... }

    // Build suggested bookmark list (wrong cards in deck session)
    private List<ReviewResultResponse.BookmarkSuggestionItem> buildBookmarkSuggestions(
        Long studentId, String deckName, LocalDateTime sessionStart) { ... }
}
```

### 4.2 DictionaryService.java

```java
@Service
@Transactional(readOnly = true)
public class DictionaryService {

    // Injected repositories từ Người 1
    private final VocabularyRepository vocabularyRepository;
    private final KanjiRepository kanjiRepository;
    private final GrammarPointRepository grammarPointRepository;
    private final LessonRepository lessonRepository;

    /**
     * GET /api/dictionary/search?q=...&jlptLevel=N5&type=VOCABULARY
     * Search đồng thời qua 4 nguồn (PUBLISHED only)
     * q: tìm trong word/meaning/character/structure/title
     * jlptLevel: optional filter (N5..N1)
     * type: optional filter (VOCABULARY|KANJI|GRAMMAR|LESSON)
     */
    public SearchResponse search(String keyword, String jlptLevel, String type) {
        // Validate keyword không rỗng
        // Gọi parallel hoặc sequential tùy impl
        // Chỉ trả content status = PUBLISHED
        // Giới hạn 10 kết quả mỗi loại để tránh response quá lớn
    }
}
```

**Query rules cho DictionaryService:**
- Vocabulary: tìm trong `word`, `furigana`, `meaning` — LIKE `%q%`
- Kanji: tìm trong `characterValue`, `meaning`, `onyomi`, `kunyomi`
- GrammarPoint: tìm trong `structure`, `meaning`
- Lesson: tìm trong `title` — chỉ `status = PUBLISHED`
- Tất cả: `status = PUBLISHED` (không trả DRAFT/ARCHIVED/DELETED)

### 4.3 BookmarkService.java

```java
@Service
@Transactional
public class BookmarkService {

    // Inject từ Người 3
    private final StudentContentProgressRepository progressRepository;

    /**
     * POST /api/bookmarks
     * Idempotent — nếu đã có progress record thì cập nhật isBookmarked=true
     * Nếu chưa có progress record thì tạo mới với status=LEARNING, isBookmarked=true
     */
    public BookmarkResponse addBookmark(Long studentId, BookmarkRequest request) { ... }

    /**
     * DELETE /api/bookmarks?contentType=VOCABULARY&contentId=42
     * Set isBookmarked=false, xóa note
     * Không xóa progress record (soft approach)
     */
    public void removeBookmark(Long studentId, String contentType, Long contentId) { ... }

    /**
     * GET /api/bookmarks?type=VOCABULARY&page=0&size=20
     * Trả danh sách content đang bookmark, enrich display text từ source
     * type: optional filter
     */
    public Page<BookmarkResponse> listBookmarks(Long studentId, String type,
                                                Pageable pageable) { ... }
}
```

---

## PHẦN 5 — CONTROLLER SPECIFICATION

### 5.1 StudentFlashcardController.java

```java
@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('STUDENT')")
public class StudentFlashcardController {

    // GET /api/flashcard-decks
    // Response: ApiResponse<List<DeckSummaryResponse>>
    @GetMapping("/flashcard-decks")
    public ResponseEntity<ApiResponse<List<DeckSummaryResponse>>> getDecks(
        @AuthenticationPrincipal StudentDetails studentDetails) { ... }

    // GET /api/flashcards?deckName=N5_食べ物&dueOnly=true&page=0&size=20
    // Response: ApiResponse<Page<FlashcardResponse>>
    @GetMapping("/flashcards")
    public ResponseEntity<ApiResponse<Page<FlashcardResponse>>> getCards(
        @AuthenticationPrincipal StudentDetails studentDetails,
        @RequestParam(required = false) String deckName,
        @RequestParam(defaultValue = "false") boolean dueOnly,
        Pageable pageable) { ... }

    // GET /api/flashcards/{id}/reveal
    // Response: ApiResponse<FlashcardRevealResponse>
    @GetMapping("/flashcards/{id}/reveal")
    public ResponseEntity<ApiResponse<FlashcardRevealResponse>> revealCard(
        @PathVariable Long id,
        @AuthenticationPrincipal StudentDetails studentDetails) { ... }

    // POST /api/flashcards/{id}/review
    // Body: ReviewRequest | Response: ApiResponse<ReviewResultResponse>
    @PostMapping("/flashcards/{id}/review")
    public ResponseEntity<ApiResponse<ReviewResultResponse>> submitReview(
        @PathVariable Long id,
        @Valid @RequestBody ReviewRequest request,
        @AuthenticationPrincipal StudentDetails studentDetails) { ... }

    // POST /api/flashcards
    // Body: AddFlashcardRequest | Response: ApiResponse<FlashcardResponse>
    @PostMapping("/flashcards")
    public ResponseEntity<ApiResponse<FlashcardResponse>> addCard(
        @Valid @RequestBody AddFlashcardRequest request,
        @AuthenticationPrincipal StudentDetails studentDetails) { ... }
}
```

### 5.2 StudentDictionaryController.java

```java
@RestController
@RequestMapping("/api/dictionary")
@PreAuthorize("hasRole('STUDENT')")
public class StudentDictionaryController {

    // GET /api/dictionary/search?q=食べる&jlptLevel=N5&type=VOCABULARY
    // q required, jlptLevel/type optional
    // Response: ApiResponse<SearchResponse>
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponse>> search(
        @RequestParam @NotBlank String q,
        @RequestParam(required = false) String jlptLevel,
        @RequestParam(required = false) String type,
        @AuthenticationPrincipal StudentDetails studentDetails) { ... }
}
```

### 5.3 StudentBookmarkController.java

```java
@RestController
@RequestMapping("/api/bookmarks")
@PreAuthorize("hasRole('STUDENT')")
public class StudentBookmarkController {

    // GET /api/bookmarks?type=VOCABULARY&page=0&size=20
    // Response: ApiResponse<Page<BookmarkResponse>>
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookmarkResponse>>> listBookmarks(
        @AuthenticationPrincipal StudentDetails studentDetails,
        @RequestParam(required = false) String type,
        Pageable pageable) { ... }

    // POST /api/bookmarks
    // Body: BookmarkRequest | Response: ApiResponse<BookmarkResponse>
    @PostMapping
    public ResponseEntity<ApiResponse<BookmarkResponse>> addBookmark(
        @Valid @RequestBody BookmarkRequest request,
        @AuthenticationPrincipal StudentDetails studentDetails) { ... }

    // DELETE /api/bookmarks?contentType=VOCABULARY&contentId=42
    // Response: ApiResponse<Void>
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
        @RequestParam String contentType,
        @RequestParam Long contentId,
        @AuthenticationPrincipal StudentDetails studentDetails) { ... }
}
```

---

## PHẦN 6 — API ENDPOINT SUMMARY

| Method | Path | Service Method | Auth |
|--------|------|---------------|------|
| GET | `/api/flashcard-decks` | `getDecks()` | STUDENT |
| GET | `/api/flashcards` | `getCards()` | STUDENT |
| GET | `/api/flashcards/{id}/reveal` | `revealCard()` | STUDENT |
| POST | `/api/flashcards/{id}/review` | `submitReview()` | STUDENT |
| POST | `/api/flashcards` | `addCard()` | STUDENT |
| GET | `/api/dictionary/search` | `search()` | STUDENT |
| GET | `/api/bookmarks` | `listBookmarks()` | STUDENT |
| POST | `/api/bookmarks` | `addBookmark()` | STUDENT |
| DELETE | `/api/bookmarks` | `removeBookmark()` | STUDENT |

---

## PHẦN 7 — LUỒNG HOÀN CHỈNH

### Luồng 1: Ôn tập flashcard theo topic

```
1. GET /api/flashcard-decks
   → Hiển thị: "N5_食べ物 · 8 card · 3 due hôm nay"

2. GET /api/flashcards?deckName=N5_食べ物&dueOnly=true
   → Trả danh sách front-only cards

3. Student chọn card → GET /api/flashcards/{id}/reveal
   → Hiển thị mặt sau: nghĩa, ví dụ, audio

4. Student đánh giá → POST /api/flashcards/{id}/review
   Body: { rating: "WRONG", isLastCardInSession: false }
   → SM-2 reset interval, nextReviewDate = hôm nay + 1

5. Card cuối của deck:
   Body: { rating: "EASY", isLastCardInSession: true }
   → Response bao gồm suggestedBookmarks: [từ bị sai trong session]

6. Student confirm → POST /api/bookmarks
   Body: { contentType: "VOCABULARY", contentId: 42, note: "nhớ ôn lại" }
```

### Luồng 2: Tìm kiếm từ điển

```
1. GET /api/dictionary/search?q=食べ&jlptLevel=N5
   → Trả: vocab ["食べる", "食べ物"], kanji ["食"], lesson ["Bài 3 - Ăn uống"]

2. Student click vocab → thấy chi tiết (furigana, ví dụ, audio)

3. Muốn ôn tập → POST /api/flashcards
   Body: { contentType: "VOCABULARY", contentId: 15, deckName: "N5_食べ物" }
   → Hệ thống tạo card với frontText=word, backText=meaning+example
```

---

## PHẦN 8 — CHECKLIST IMPLEMENTATION

### Repository
- [ ] `FlashcardRepository` — 5 methods: `findDeckSummaries`, `findByDeck`, `existsByIdAndStudentId`, `findWrongCardsInSession`, `existsByStudentIdAndContentTypeAndContentId`

### Service
- [ ] `FlashcardSrsService.getDecks()` — list decks với due count
- [ ] `FlashcardSrsService.getCards()` — paginated card list
- [ ] `FlashcardSrsService.revealCard()` — security check + enrich từ source
- [ ] `FlashcardSrsService.submitReview()` — SM-2 + bookmark suggestion
- [ ] `FlashcardSrsService.addCard()` — idempotent create, auto deckName
- [ ] `DictionaryService.search()` — multi-source, PUBLISHED only
- [ ] `BookmarkService.addBookmark()` — upsert on StudentContentProgress
- [ ] `BookmarkService.removeBookmark()` — set isBookmarked=false
- [ ] `BookmarkService.listBookmarks()` — paginated + enrich displayText

### Controller
- [ ] `StudentFlashcardController` — 5 endpoints
- [ ] `StudentDictionaryController` — 1 endpoint
- [ ] `StudentBookmarkController` — 3 endpoints

### DTO
- [ ] `FlashcardResponse`
- [ ] `FlashcardRevealResponse`
- [ ] `ReviewRequest` + `@Valid` annotations
- [ ] `ReviewResultResponse` + inner `BookmarkSuggestionItem`
- [ ] `DeckSummaryResponse`
- [ ] `SearchResponse` + 4 inner records
- [ ] `BookmarkRequest` + `@Valid` annotations
- [ ] `BookmarkResponse`
- [ ] `AddFlashcardRequest` (cần thêm vào danh sách — chưa có trong spec gốc)

### Security / Validation
- [ ] Tất cả endpoint có `@PreAuthorize("hasRole('STUDENT')")`
- [ ] Mọi query filter `student_id = currentStudentId`
- [ ] `revealCard` + `submitReview` check `existsByIdAndStudentId` trước
- [ ] `DictionaryService` chỉ trả `status = PUBLISHED`

---

## PHẦN 9 — ĐIỀU CẦN CONFIRM VỚI TEAM

| # | Câu hỏi | Ảnh hưởng |
|---|---------|----------|
| 1 | `VocabularyRepository`, `KanjiRepository`, `GrammarPointRepository`, `LessonRepository` — Người 1 tạo interface gì? Custom query nào sẽ expose? | DictionaryService + FlashcardSrsService phụ thuộc |
| 2 | `StudentContentProgressRepository` — Người 3 expose method `findByStudentIdAndContentTypeAndContentId()` chưa? | BookmarkService cần method này |
| 3 | `StudentDetails` (từ JWT) có field `studentId: Long` không? Hay dùng `email` để lookup? | Tất cả Controller phụ thuộc |
| 4 | Cần Flyway migration thêm index không? (`flashcards.next_review_date + student_id`) | Performance |
| 5 | Session start time cho bookmark suggestion — frontend gửi lên hay backend tự tính? | ReviewRequest có thể cần thêm `sessionStartedAt` |

---

## PHẦN 10 — MIGRATION (NẾU CẦN)

Hiện tại **không cần migration mới** — tất cả field đã có trong `V1__init_schema.sql`.

Tuy nhiên, **recommend thêm index** (tạo `V2__add_flashcard_indexes.sql`):

```sql
-- Tăng performance cho SM-2 query (filter due cards)
CREATE INDEX idx_flashcard_student_deck_due
    ON flashcards(student_id, deck_name, next_review_date);

-- Tăng performance cho bookmark query
CREATE INDEX idx_student_content_progress_bookmark
    ON student_content_progress(student_id, is_bookmarked, content_type);
```

> **Hỏi team trước khi tạo migration** — Người 3 có thể đã tạo index này trong migration của họ.

---
