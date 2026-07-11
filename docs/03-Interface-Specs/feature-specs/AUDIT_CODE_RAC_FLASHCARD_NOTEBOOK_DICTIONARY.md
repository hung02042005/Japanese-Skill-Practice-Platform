# AUDIT — Code Rác: Flashcard · Sổ tay (Notebook) · Từ điển (Dictionary)

> **Phạm vi quét:** `feature/flashcard`, `feature/dictionary` (backend) + trang Notebook/Dictionary/VocabFlashcardSession (frontend).
> **Lần quét gần nhất:** 2026-07-11 | **Người quét:** Claude (đối chiếu code thực tế)
> **Mục đích:** Theo dõi dead code / field thừa / trùng lặp — kèm `file:line`, trạng thái xử lý.
> **Spec liên quan:** [`SPEC_BACKEND_FLASHCARD_NOTEBOOK_DICTIONARY.md`](SPEC_BACKEND_FLASHCARD_NOTEBOOK_DICTIONARY.md)

---

## 0. TRẠNG THÁI HIỆN TẠI (2026-07-11)

Sau đợt refactor lớn (tách `FlashcardResolver` / `FlashcardDeckSupport` / `NotebookService`, hợp nhất SM-2) + đợt dọn 2026-07-11, **toàn bộ 12 phát hiện của lần audit 2026-06-21 đã được xử lý**. Không còn dead code đáng kể trong 3 phần này.

| # | Loại | Vị trí (audit cũ) | Trạng thái |
|:--:|:---|:---|:---|
| 1 | Dead method `findVocabContentIdsByStudent` | FlashcardRepository | ✅ Đã xóa (không còn trong repo) |
| 2 | Field thừa `DeckSummaryResponse.displayName` | DeckSummaryResponse | ✅ Đã xóa (2026-07-11 cắt tiếp còn 4 field) |
| 3 | Bug CUSTOM bỏ qua tên đã normalize | NotebookService.addCard | ✅ Dùng `requestedDeckName != null ? … : DEFAULT_DECK_NAME` |
| 4 | `newCount == reviewCount` | SessionResponse | ✅ Gộp còn `wordCount` |
| 5 | Search `q` bị nuốt khi `deckId == null` | NotebookService.getCards | ✅ Tìm trên `findByStudent` khi không có deck |
| 6 | Constraint tên sổ lệch (255 vs 100) | *Request DTOs | ✅ Thống nhất `FlashcardConstants.DECK_NAME_MAX`; `DeckCreate/UpdateRequest` đã gỡ |
| 7 | Magic string `"Mặc định"` | FlashcardSrsService | ✅ `FlashcardConstants.DEFAULT_DECK_NAME` / `REVIEW_DECK_NAME` |
| 8 | Trùng resolve `buildRevealResponse` | FlashcardSrsService | ✅ Hợp nhất vào `FlashcardResolver` |
| 9 | `new Random()` mỗi lần gọi | FlashcardSrsService | ✅ `ThreadLocalRandom.current()` |
| 10 | Naming `WrongWord.frontText` | ReviewResultResponse | ✅ Đổi thành `word` |
| 11 | Spec `.sdd/.../feat-flashcard`, `feat-dictionary-bookmark` | docs | ✅ Không còn (thư mục `.sdd` đã gỡ) |
| 12 | Cây trùng `project-git/` | repo root | ✅ Không còn |

---

## 1. ĐỢT DỌN 2026-07-11 (đã áp dụng)

Rà lại cả BE + FE, phát hiện & xử lý các mục còn sót:

### 1.1 Dead branch `sourceTopic` (FE)
[`NotebookWordCard.jsx`](../../apps/frontend/src/components/student/NotebookWordCard.jsx) đọc `word.sourceTopic`, nhưng [`FlashcardResponse`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/dto/FlashcardResponse.java) **không có** field này → nhánh `(Topic: …)` không bao giờ chạy.
→ **Đã bỏ** `sourceTopic` khỏi destructure + template.

### 1.2 `DeckSummaryResponse` trả field không client nào đọc (BE)
`getFlashcardDecks()` chỉ được dùng ở [`Notebook.jsx`](../../apps/frontend/src/pages/notebook/Notebook.jsx), và chỉ đọc `deckId, deckName, totalCards, isReviewDeck`. Các field `dueToday, jlptLevel, topic, isSystem` bị bỏ phí; query còn `SELECT` cả `description, color` rồi không map.
→ **Đã cắt** [`DeckSummaryResponse`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/dto/DeckSummaryResponse.java) còn 4 field; [`findDeckSummaries`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/repository/FlashcardDeckRepository.java) gọn `SELECT`/`GROUP BY`, bỏ hẳn `SUM(CASE…)` + tham số `today`; [`NotebookService.getDecks`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/service/NotebookService.java) map theo shape mới.

### 1.3 `ownCardOrThrow` 2 query → 1 (BE)
[`FlashcardDeckSupport.ownCardOrThrow`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/service/FlashcardDeckSupport.java) trước gọi `existsByIdAndStudentId` **rồi** `findById`.
→ **Đã gộp** `findById` một lần rồi so chủ sở hữu trên FK id (`student` LAZY, `getId()` không hit DB). Giữ nguyên ngữ nghĩa 404 (không tồn tại) vs 403 (không phải của bạn).

---

## 2. XEM XÉT — KHÔNG xử lý (có chủ đích / không phải rác)

- **`addVocabToFlashcard` vs `addToFlashcard`** ([studentService.js](../../apps/frontend/src/api/studentService.js)): trông giống nhưng **khác hành vi** — bản không gửi `deckName` dồn thẻ vào deck `{level}_{topic}`, bản gửi `'Mặc định'` dồn vào deck "Mặc định". Là quyết định sản phẩm, giữ nguyên.
- **Nhánh `dueOnly` trong `getCards`** + `findDueByDeck`/`findAllDue`: hiện FE luôn truyền `dueOnly=false`, nhưng là bề mặt API hợp lệ → giữ.
- **Field SRS trong `ReviewResultResponse`** (`newEaseFactor`, `newIntervalDays`, `repetitionCount`, `nextReviewDate`, `rating`): FE chưa đọc hết, nhưng là payload kết quả ôn hợp lý → giữ.

---

## 3. XÁC MINH (2026-07-11)

- `mvn -o compile` — ✅ pass
- `mvn -o test -Dtest=FlashcardSrsServiceSm2Test` — ✅ pass
- `mvn -o spotless:check` — ✅ pass
- `eslint` (NotebookWordCard / Notebook / Dictionary) — ✅ pass
