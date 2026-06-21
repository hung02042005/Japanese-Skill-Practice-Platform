# SPEC — Backend: Flashcard · Sổ tay (Notebook) · Từ điển (Dictionary)

> **Feature ID:** `feat-flashcard` + `feat-dictionary`
> **UC Coverage:** UC-16 (Tra từ điển), Flashcard SRS, Sổ tay "Từ cần ôn lại"
> **Version:** 1.0 | **Status:** Implemented (tài liệu hoá code hiện hành)
> **Author:** Người 4 (MinhPham) | **Last Updated:** 2026-06-21
> **Đọc kèm:** `CLAUDE.md`, `CONSTITUTION.md`, `AGENTS.md`

---

## 0. PHẠM VI & QUAN HỆ 3 PHẦN

Tài liệu này đặc tả **code backend đã hiện thực** của 3 module gắn kết chặt với nhau cho Student:

| Phần | Vai trò | Package |
|:---|:---|:---|
| **Từ điển (Dictionary)** | Tra cứu toàn hệ thống (vocab/kanji/grammar/lesson), chỉ đọc | `com.jlpt.feature.dictionary` |
| **Flashcard** | Học + ôn theo SRS (SM-2), phiên học trộn NEW/REVIEW theo chủ đề/sổ tay | `com.jlpt.feature.flashcard` |
| **Sổ tay (Notebook)** | Tập các "sổ" (deck) cá nhân; sổ hệ thống đặc biệt **"Từ cần ôn lại"** gom từ sai + từ lưu thủ công | `com.jlpt.feature.flashcard` (deck = first-class) |

**Ranh giới (theo memory dự án):**
- **Từ điển = tra cứu.** Từ Từ điển, học viên bấm "Lưu" → đẩy vào sổ "Từ cần ôn lại" (`reason = manual`).
- **Sổ tay = các deck.** Mỗi deck có id riêng (first-class từ V9). Sổ "Từ cần ôn lại" là deck hệ thống tự sinh per-student (`is_review_deck = 1`), nuôi bởi: (a) từ vựng bị chấm SAI trong phiên ôn (`reason = wrong`); (b) lưu thủ công từ Từ điển (`reason = manual`).
- **Flashcard = Quizlet theo chủ đề.** Phiên học sinh từ `topicId` (giáo trình) hoặc `deckId` (sổ tay), trộn "học rồi kiểm tra".

> **Lưu ý:** Module bookmark cũ qua `student_content_progress` (mô tả ở `feat-dictionary-bookmark/SPEC.md` v1.0) **đã được thay thế** bằng cơ chế sổ "Từ cần ôn lại". Spec đó coi như deprecated cho phần lưu/ôn.

---

## 1. KIẾN TRÚC & TUÂN THỦ CHUẨN

Tuân thủ layer architecture (CLAUDE.md): **Controller → Service → Repository → Entity**.

```
StudentDictionaryController ─► DictionaryService ─► {Vocabulary,Kanji,GrammarPoint,Lesson}Repository
StudentFlashcardController  ─► FlashcardSrsService ─► {Flashcard,FlashcardDeck}Repository
                                                  └─► {Vocabulary,Kanji,GrammarPoint,VocabularyTopic,StudentUser}Repository
```

| Chuẩn (ADR / Anti-pattern) | Cách áp dụng trong code |
|:---|:---|
| **ADR-005 DTO Pattern** | Controller chỉ nhận/trả `record` DTO (`*Request` / `*Response`); mapping Entity↔DTO tại Service (`toFlashcardResponse`, `toVocabItem`…). Không trả Entity ra API. |
| **ADR-004 Soft Delete** | `Flashcard` & `FlashcardDeck` có `is_deleted`; lọc tự động bằng `@SQLRestriction("is_deleted = 0")`. Xóa = set cờ (`deleteCard`, `deleteDeck`, `softDeleteByDeckId`). Không `DELETE FROM`. |
| **ADR-008 Global Exception** | Ném `BadRequestException` / `ResourceNotFoundException` / `ForbiddenException` / `DuplicateResourceException` (shared) → `@ControllerAdvice` chuẩn hoá `{status,message,data}`. |
| **@Transactional tại Service** | `FlashcardSrsService` `@Transactional` mặc định; read-only được đánh dấu riêng (`getDecks`, `getCards`, `revealCard`). `DictionaryService` `@Transactional(readOnly=true)`. |
| **Authorization = Role + Ownership** | Controller `@PreAuthorize("hasRole('STUDENT')")`; Service kiểm tra quyền sở hữu (`ownCardOrThrow`, `ownDeckOrThrow`) → 403 nếu không phải chủ. |
| **Chống N+1** | Live-resolve theo lô qua `loadContentMaps` (1 query/loại); phiên học dựng thẻ mới bằng `saveAll` 1 lần; `findByStudentAndContentIds` thay vì lặp `findById`. |
| **Magic numbers → constant** | `NEW_CARDS_PER_DAY`, `LEARN_BATCH_MIN/MAX`, `MAX_NEW`, `EASE_DEFAULT/MIN/MAX` là `private static final`. |
| **Client-trusted data** | Trắc nghiệm: server tự xác định đúng/sai (`correct = selectedOptionId == contentId`), KHÔNG tin client gửi điểm. |

---

## 2. DATA MODEL

### 2.1 `flashcard_decks` (sổ tay — first-class, V9; bỏ `deck_name`, V15)

| Cột | Kiểu | Ghi chú |
|:---|:---|:---|
| `deck_id` | BIGINT PK IDENTITY | |
| `student_id` | BIGINT FK NULL | NULL = deck hệ thống |
| `name` | NVARCHAR(255) NOT NULL | Unique theo (student, name) ở tầng nghiệp vụ |
| `description` | NVARCHAR(500) | |
| `jlpt_level` | NVARCHAR(5) | Tách từ pattern `N#_topic` |
| `topic` | NVARCHAR(100) | |
| `color` | NVARCHAR(20) | |
| `display_order` | INT NOT NULL DEFAULT 0 | Sắp xếp danh sách deck |
| `is_system` | BIT NOT NULL DEFAULT 0 | Không cho sửa/xóa |
| `is_review_deck` | BIT NOT NULL DEFAULT 0 | Sổ "Từ cần ôn lại" (1 per student) |
| `is_deleted` | BIT NOT NULL DEFAULT 0 | Soft delete |
| `created_at` / `updated_at` | DATETIME2 | Audit |

### 2.2 `flashcards` (thẻ)

| Cột | Kiểu | Ghi chú |
|:---|:---|:---|
| `flashcard_id` | BIGINT PK IDENTITY | |
| `student_id` | BIGINT FK | Chủ thẻ |
| `deck_id` | BIGINT FK | Nguồn sự thật của sổ (thay `deck_name`) |
| `content_type` | NVARCHAR(20) | `kanji` / `vocabulary` / `grammar` / `custom` (lowercase qua converter) |
| `content_id` | BIGINT NULL | Trỏ tới nội dung tích hợp; NULL với CUSTOM |
| `added_reason` | NVARCHAR(20) | `wrong` / `manual` / `learn` (NULL với thẻ cũ) |
| `front_text` / `back_text` | NVARCHAR(MAX) | **Chỉ** dùng cho CUSTOM; thẻ tích hợp để NULL, resolve live |
| `last_rating` | NVARCHAR(10) | `easy` / `hard` / `wrong` (converter) |
| `interval_days` | INT NOT NULL DEFAULT 1 | SM-2 |
| `ease_factor` | DECIMAL(5,2) NOT NULL DEFAULT 2.50 | SM-2 (V11) |
| `repetition_count` | INT NOT NULL DEFAULT 0 | SM-2 |
| `next_review_date` | DATE | Hạn ôn |
| `last_reviewed_at` | DATETIME2 | |
| `created_at` | DATETIME2 | |
| `is_deleted` | BIT NOT NULL DEFAULT 0 | Soft delete (V8) |

**Index:** `(student_id, content_type, content_id)` (V14) cho lookup trùng + điều hướng level+topic.

### 2.3 Nguyên tắc "live-resolve" (FR-FC-30/34)

Thẻ tích hợp **không copy** mặt trước/sau vào DB. Khi đọc (list/reveal/session), service resolve từ bảng nguồn (`vocabulary`/`kanji`/`grammar_points`) **và chỉ khi `status = PUBLISHED`**. Nguồn đã xóa/chưa duyệt → `frontText = null` → bị lọc khỏi danh sách. Lợi ích: nội dung luôn đồng bộ với bản gốc do Staff cập nhật, không dữ liệu chết.

---

## 3. FUNCTIONAL REQUIREMENTS

### 3.1 Từ điển (Dictionary)

| ID | Requirement |
|:---|:---|
| FR-DICT-01 | WHEN Student nhập từ khoá, THE SYSTEM SHALL tìm trên `vocabulary` (word/furigana/meaning), `kanji` (character/meaning/on/kun), `grammar_points` (structure/meaning), `lessons` (title). |
| FR-DICT-02 | THE SYSTEM SHALL nhóm kết quả theo loại; mỗi loại tối đa `MAX_RESULTS_PER_TYPE = 10`. |
| FR-DICT-03 | IF `type` được truyền (`VOCABULARY`/`KANJI`/`GRAMMAR`/`LESSON`), THEN chỉ tìm loại đó; ngược lại tìm tất cả. |
| FR-DICT-04 | IF `jlptLevel` được truyền, THEN lọc theo cấp độ; sai cấp độ → 400. |
| FR-DICT-05 | THE SYSTEM SHALL loại bỏ mọi mục `status != published` khỏi kết quả. |
| FR-DICT-06 | IF `q` rỗng/blank, THEN 400 "Từ khóa tìm kiếm không được để trống". |

### 3.2 Flashcard — Deck CRUD (Sổ tay)

| ID | Requirement |
|:---|:---|
| FR-FC-07 | THE SYSTEM SHALL cho phép deck rỗng tồn tại (first-class, không cần thẻ giữ chỗ); danh sách deck dùng LEFT JOIN nên deck rỗng vẫn hiện. |
| FR-FC-08 | WHEN tạo deck trùng tên (theo student), THEN 409 Conflict. |
| FR-FC-09 | THE SYSTEM SHALL cấm sửa/xóa deck `is_system = 1` → 403. |
| FR-FC-10 | WHEN xóa deck, THE SYSTEM SHALL soft-delete deck + toàn bộ thẻ thuộc deck. |
| FR-FC-11 | THE SYSTEM SHALL trả tóm tắt deck kèm `totalCards` và `dueToday` (thẻ `next_review_date <= today`). |

### 3.3 Flashcard — Thẻ (list / reveal / add / delete)

| ID | Requirement |
|:---|:---|
| FR-FC-30 | Thẻ tích hợp resolve live mặt trước/sau từ nguồn PUBLISHED; CUSTOM dùng `front/back_text`. |
| FR-FC-31 | Mỗi (student, content_type, content_id) chỉ 1 thẻ; thêm trùng → 409. |
| FR-FC-34 | Thẻ có nguồn đã xóa/không published bị ẩn khỏi danh sách (front = null). |
| FR-FC-35 | Tìm kiếm trong sổ (`q`) resolve live cả deck rồi lọc theo mặt trước (không bị giới hạn paging DB). |
| FR-FC-36 | Thêm thẻ CUSTOM yêu cầu `frontText` + `backText`, nếu thiếu → 400. |
| FR-FC-37 | Gỡ thẻ = soft-delete (`is_deleted = 1`), chỉ chủ thẻ. |

### 3.4 Sổ "Từ cần ôn lại" (Notebook review-deck)

| ID | Requirement |
|:---|:---|
| FR-FC-40 | THE SYSTEM SHALL duy trì đúng 1 sổ `is_review_deck` per student (get-or-create). |
| FR-FC-43 | WHEN kết thúc phiên có từ vựng chấm SAI, THE SYSTEM SHALL trả `suggest = true` + danh sách `wrongWords` để gợi ý thêm vào sổ ôn. |
| FR-FC-44 | WHEN Student xác nhận thêm, THE SYSTEM SHALL: nếu thẻ chưa có → tạo; nếu đã ở sổ khác → **chuyển** sang sổ ôn; nếu đã ở sổ ôn → bỏ qua (đếm `skipped`). Mỗi nội dung vẫn chỉ 1 thẻ (FR-FC-31). |
| FR-FC-45 | `reason` mặc định `manual` (lưu từ Từ điển), `wrong` khi đẩy từ phiên ôn. |

### 3.5 Phiên học trộn NEW + REVIEW (Quizlet)

| ID | Requirement |
|:---|:---|
| FR-FC-51 | `newLimit` mặc định `NEW_CARDS_PER_DAY = 10`, trần `MAX_NEW = 20` (mỗi từ xuất hiện 2 lần). |
| FR-FC-54 | Thẻ REVIEW là trắc nghiệm 2 đáp án: nghĩa đúng + 1 distractor (nghĩa khác) từ pool, trộn ngẫu nhiên. |
| FR-FC-55/56 | Server tự chấm đúng/sai (`optionId == content_id`); KHÔNG tin client. |
| FR-FC-64 | Mọi từ được chọn vào phiên đều có `flashcardId` (tạo lười qua `saveAll`) để chấm được. |
| FR-FC-65 | Chỉ nạp từ vựng `status = PUBLISHED`. |
| FR-FC-70..72 | Trộn "học rồi kiểm tra": mỗi lô `LEARN_BATCH = 2–3` thẻ NEW (lật học), rồi REVIEW (trắc nghiệm) đúng các từ vừa học, trước khi sang lô kế. |
| FR-FC-80 | Thứ tự ưu tiên chọn từ: **chưa học (0) → đến hạn ôn (1) → đã học chưa đến hạn (2)**; trộn ngẫu nhiên trong từng nhóm. |
| FR-redo-topic | Phiên theo giáo trình dùng `topicId` (khoá chủ đề duy nhất), không còn free-text topic. |

### 3.6 SM-2 (thuật toán giãn cách)

| ID | Requirement |
|:---|:---|
| FR-FC-21 | WRONG (q=0): ease giảm theo công thức SM-2 (sàn 1.30), `repetition = 0`, `interval = 1`. |
| FR-FC-22 | HARD (q=2): ease **giữ nguyên**, `interval = MAX(1, previous)`. |
| FR-FC-23 | EASY (q=5): ease `+0.1` (trần `EASE_MAX = 2.50`); interval: rep0→1, rep1→6, else `round(interval × ease)`; `repetition++`. |
| FR-FC-24 | Ease luôn kẹp trong `[EASE_MIN 1.30, EASE_MAX 2.50]`. |
| FR-FC-30b | Ease mặc định `2.50` khi thẻ chưa có giá trị. |

> Công thức ease delta: `ease + (0.1 − q'·(0.08 + q'·0.02))` với `q' = 5 − quality`.
> Sau cập nhật: `next_review_date = today + interval_days`, ghi `last_reviewed_at`, `last_rating`.

### 3.7 Non-Functional

| ID | Yêu cầu |
|:---|:---|
| NFR-FC-05 | Log mọi lượt review: `studentId, flashcardId, rating, newInterval` (SLF4J INFO). |
| NFR-FC-06 | Chống N+1: live-resolve theo lô; `saveAll` cho thẻ phiên; query `IN` thay vòng lặp. |
| NFR-DICT-01 | Mỗi loại giới hạn 10 kết quả; query có index trên cột tìm kiếm/`status`. |

---

## 4. API SPEC

> Base: `/api` · Auth: Bearer JWT · `@PreAuthorize("hasRole('STUDENT')")` · Bọc `ApiResponse<T> { status, message, data }`.

### 4.1 Từ điển
| Method | Path | Mô tả |
|:---|:---|:---|
| GET | `/api/dictionary/search?q=&jlptLevel=&type=` | Tìm kiếm đa nhóm (vocab/kanji/grammar/lesson). |

### 4.2 Sổ tay (deck)
| Method | Path | Mô tả |
|:---|:---|:---|
| GET | `/api/flashcard-decks` | Danh sách deck + `totalCards` + `dueToday`. |
| POST | `/api/flashcard-decks` | Tạo deck (`deckName`) → 201. |
| PATCH | `/api/flashcard-decks/{deckId}` | Sửa name/description/jlptLevel/topic/color. |
| DELETE | `/api/flashcard-decks/{deckId}` | Soft-delete deck + thẻ. |

### 4.3 Thẻ & phiên học
| Method | Path | Mô tả |
|:---|:---|:---|
| GET | `/api/flashcards?deckId=&dueOnly=&q=&page=&size=` | Danh sách thẻ (resolve live, paging). |
| POST | `/api/flashcards` | Thêm thẻ (VOCABULARY/KANJI/GRAMMAR/CUSTOM) → 201. |
| GET | `/api/flashcards/{id}/reveal` | Mặt sau đầy đủ (nghĩa, furigana, ví dụ, audio, stroke). |
| POST | `/api/flashcards/{id}/review` | Nộp đánh giá; trả kết quả SM-2 + gợi ý từ sai. |
| DELETE | `/api/flashcards/{id}` | Gỡ thẻ (soft-delete). |
| POST | `/api/flashcards/session?deckId=&topicId=&newLimit=` | Dựng phiên học trộn (POST vì có side-effect tạo deck/thẻ). |
| POST | `/api/flashcards/review-deck/add` | Thêm từ vào sổ "Từ cần ôn lại". |

### 4.4 Ví dụ — `POST /api/flashcards/{id}/review`
**Request (trắc nghiệm vocab):**
```json
{ "selectedOptionId": 12, "isLastCardInSession": true }
```
**Request (lật thẻ kanji/grammar/custom):**
```json
{ "rating": "easy" }
```
**Response (200):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "flashcardId": 99,
    "correct": false,
    "correctOptionId": 12,
    "correctMeaning": "Ăn",
    "rating": "wrong",
    "intervalDays": 1,
    "easeFactor": 2.30,
    "nextReviewDate": "2026-06-22",
    "repetitionCount": 0,
    "suggestAddToReviewDeck": true,
    "wrongWords": [ { "contentType": "vocabulary", "contentId": 12, "word": "食べる" } ]
  }
}
```

---

## 5. ERROR HANDLING

| HTTP | Exception | Trigger điển hình |
|:---:|:---|:---|
| 400 | `BadRequestException` | `q` rỗng; `contentType` sai; thiếu `contentId` cho thẻ tích hợp; CUSTOM thiếu front/back; thiếu cả `deckId` lẫn `topicId`; `rating` null khi lật thẻ. |
| 403 | `ForbiddenException` | Thẻ/deck không thuộc Student; sửa/xóa deck hệ thống. |
| 404 | `ResourceNotFoundException` | Deck/Flashcard/Vocabulary/Kanji/Grammar/Topic không tồn tại. |
| 409 | `DuplicateResourceException` | Trùng tên deck; thẻ trùng (student, type, contentId). |
| 401 | (Security) | Thiếu/hết hạn JWT. |

---

## 6. ACCEPTANCE CRITERIA

| ID | Given | When | Then |
|:---|:---|:---|:---|
| AC-DICT-01 | Có vocab/kanji/grammar published | GET `/dictionary/search?q=taberu` | Trả kết quả tách nhóm, ≤10 mỗi loại |
| AC-DICT-02 | Vocab `status=draft` | GET `/dictionary/search?q=…` | Không xuất hiện trong kết quả |
| AC-FC-01 | Student có sổ rỗng | GET `/flashcard-decks` | Sổ rỗng vẫn hiện, `totalCards=0` |
| AC-FC-02 | Thẻ vocab nguồn bị `archived` | GET `/flashcards` | Thẻ bị ẩn (front null) |
| AC-FC-03 | Phiên có từ chấm SAI, `isLastCardInSession=true` | POST `/review` | `suggestAddToReviewDeck=true`, `wrongWords` không rỗng |
| AC-FC-04 | Xác nhận thêm từ sai | POST `/review-deck/add` | Thẻ vào sổ `is_review_deck`, `reason=wrong`; trùng → `skipped++` |
| AC-FC-05 | Thẻ EASY rep≥2 | POST `/review` | `intervalDays = round(prev × ease)`, `nextReviewDate = today+interval` |
| AC-FC-06 | Thẻ WRONG | POST `/review` | `repetitionCount=0`, `intervalDays=1`, ease giảm (≥1.30) |
| AC-FC-07 | Phiên theo `topicId` | POST `/session` | Hàng đợi: lô 2–3 NEW (lật) → REVIEW (trắc nghiệm) đúng các từ đó |

---

## 7. OUT OF SCOPE

- ❌ Tra từ điển bằng OCR (xem `feat-ai-skills`).
- ❌ Chia sẻ sổ tay giữa học viên.
- ❌ Bookmark qua `student_content_progress` (đã thay bằng sổ "Từ cần ôn lại").
- ❌ Phân tích thứ tự nét (stroke order) — ADR-007.
```
