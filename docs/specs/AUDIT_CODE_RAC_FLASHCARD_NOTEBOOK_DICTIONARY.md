# AUDIT — Code Rác: Flashcard · Sổ tay (Notebook) · Từ điển (Dictionary)

> **Phạm vi quét:** `feature/flashcard`, `feature/dictionary` (backend).
> **Ngày:** 2026-06-21 | **Người quét:** Claude (đối chiếu code thực tế)
> **Mục đích:** Liệt kê dead code, method không dùng, field thừa, trùng lặp, bug ngầm và smell — kèm `file:line` và đề xuất xử lý.
> **Spec liên quan:** [`SPEC_BACKEND_FLASHCARD_NOTEBOOK_DICTIONARY.md`](SPEC_BACKEND_FLASHCARD_NOTEBOOK_DICTIONARY.md)

---

## 0. BẢNG TỔNG HỢP (xử lý theo thứ tự ưu tiên)

| # | Mức | Loại | Vị trí | Tóm tắt |
|:--:|:--:|:---|:---|:---|
| 1 | 🔴 Cao | Dead code | `FlashcardRepository.java:118-126` | `findVocabContentIdsByStudent` không có caller nào |
| 2 | 🟠 TB | Field thừa | `DeckSummaryResponse.java:7` | `displayName` luôn = `deckName` (nhân đôi vô nghĩa) |
| 3 | 🟠 TB | Bug ngầm | `FlashcardSrsService.java:281` | Nhánh CUSTOM bỏ qua `requestedDeckName` đã normalize → không trim |
| 4 | 🟠 TB | Field gây hiểu nhầm | `SessionResponse.java:12` | `newCount` == `reviewCount` luôn bằng nhau |
| 5 | 🟠 TB | Logic gap | `FlashcardSrsService.java:176` | Tìm kiếm `q` bị bỏ qua âm thầm khi `deckId == null` |
| 6 | 🟡 Thấp | Constraint lệch | `AddFlashcardRequest.java:13` | `deckName` max 255 vs deck khác max 100 |
| 7 | 🟡 Thấp | Magic string | `FlashcardSrsService.java:281` | `"Mặc định"` hard-code, không hằng số |
| 8 | 🟡 Thấp | Trùng lặp logic | `FlashcardSrsService.java:656-779` | `buildRevealResponse` lặp lại resolve của `resolveFront/resolveBackMeta` |
| 9 | 🟡 Thấp | Micro | `FlashcardSrsService.java:508,640` | `new Random()` mỗi lần gọi (nên `ThreadLocalRandom`) |
| 10 | 🟡 Thấp | Naming lệch | `ReviewResultResponse.java:25` | `WrongWord.frontText` thực ra chứa `word` |
| 11 | ⚪ Doc | Spec rác | `.sdd/.../feat-flashcard/SPEC.md` (rỗng) + `feat-dictionary-bookmark/SPEC.md` (lỗi thời) | Tài liệu chết / sai lệch |
| 12 | ⚪ Repo | Cây trùng | `project-git/` | Bản sao gần như toàn bộ repo (rác lớn nếu không chủ đích) |

---

## 1. 🔴 Dead code — `findVocabContentIdsByStudent`

**File:** [`FlashcardRepository.java:118-126`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/repository/FlashcardRepository.java)

```java
@Query("SELECT f.contentId FROM Flashcard f WHERE f.student.id = :studentId AND ...")
java.util.Set<Long> findVocabContentIdsByStudent(Long studentId, Flashcard.ContentType contentType);
```

**Bằng chứng:** grep toàn `apps/` chỉ có 2 hit: (a) chính dòng định nghĩa; (b) comment trong migration `V14__flashcard_content_index.sql:7`. **Không có service/test nào gọi.**

**Đề xuất:** Xóa method (ADR: Dead Code → xóa ngay, Git recover). Cập nhật/bỏ dòng comment tham chiếu trong V14 (không sửa nội dung migration đã chạy, chỉ comment).

---

## 2. 🟠 Field thừa — `DeckSummaryResponse.displayName`

**File:** [`DeckSummaryResponse.java:5-7`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/dto/DeckSummaryResponse.java)

`displayName` **luôn** được gán cùng giá trị với `deckName` tại mọi nơi tạo DTO:
- `getDecks` → `FlashcardSrsService.java:102-103`: truyền `(String) r[1]` hai lần.
- `createDeck` → `:122`: `deckName, deckName`.
- `updateDeck` → `:146-147`: `deck.getName(), deck.getName()`.

Grep frontend không thấy dùng `displayName` riêng biệt.

**Đề xuất:** Bỏ field `displayName` khỏi record + 3 call-site. Nếu FE thực sự đọc `displayName`, đổi FE sang `deckName` trước khi xóa.

---

## 3. 🟠 Bug ngầm — nhánh CUSTOM không dùng tên đã normalize

**File:** [`FlashcardSrsService.java:251` & `:281`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/service/FlashcardSrsService.java)

```java
String requestedDeckName = normalizeDeckName(request.deckName()); // :251 — trim + null nếu blank
...
case CUSTOM -> {
    deckName = request.deckName() != null ? request.deckName() : "Mặc định"; // :281 — dùng RAW, bỏ qua requestedDeckName
}
```

3 nhánh VOCABULARY/KANJI/GRAMMAR dùng `requestedDeckName` (đã trim, blank→null), riêng CUSTOM dùng `request.deckName()` thô → deck tên `"   "` (toàn space) hoặc không trim sẽ lọt. Không nhất quán.

**Đề xuất:** CUSTOM dùng `deckName = requestedDeckName != null ? requestedDeckName : DEFAULT_DECK_NAME;`.

---

## 4. 🟠 Field gây hiểu nhầm — `newCount` luôn == `reviewCount`

**File:** [`SessionResponse.java:11-12`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/dto/SessionResponse.java) — gán tại [`FlashcardSrsService.java:523-524`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/service/FlashcardSrsService.java)

```java
return new SessionResponse(..., chosen.size(), chosen.size(), items); // newCount == reviewCount
```

Vì mỗi từ sinh đúng 1 NEW + 1 REVIEW nên 2 con số **luôn bằng nhau** — một trong hai là thừa, hoặc tên sai nghĩa (thực chất cả hai = "số từ").

**Đề xuất:** Gộp còn 1 field `wordCount` (hoặc `totalWords`), hoặc tính `reviewCount` đúng nghĩa (số mục REVIEW thực = số quiz). Cập nhật FE tương ứng.

---

## 5. 🟠 Logic gap — tìm kiếm `q` bị nuốt âm thầm khi không có deck

**File:** [`FlashcardSrsService.java:176`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/service/FlashcardSrsService.java)

```java
if (needle != null && !needle.isEmpty() && deckId != null) { ... search ... }
// else: rơi xuống nhánh thường — bỏ qua q hoàn toàn
```

Khi client gửi `?q=...` **không kèm `deckId`**, server trả danh sách bình thường, **lờ đi từ khoá** (không lỗi, không lọc) → người dùng tưởng đã tìm. Anti-pattern "silent ignore".

**Đề xuất:** Hoặc hỗ trợ search toàn bộ thẻ của student khi `deckId == null`, hoặc trả 400 yêu cầu `deckId`. Không nên nuốt im lặng.

---

## 6. 🟡 Constraint lệch — giới hạn độ dài tên sổ

| File | Field | Max |
|:---|:---|:--:|
| [`AddFlashcardRequest.java:13`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/dto/AddFlashcardRequest.java) | `deckName` | 255 |
| [`DeckCreateRequest.java:7`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/dto/DeckCreateRequest.java) | `deckName` | 100 |
| [`DeckUpdateRequest.java:9`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/dto/DeckUpdateRequest.java) | `name` | 100 |

Cùng một khái niệm "tên sổ" nhưng 3 ràng buộc khác nhau (DB cột `name` là 255). Tạo qua `addCard` cho 255, tạo trực tiếp chỉ 100 → không nhất quán.

**Đề xuất:** Thống nhất 1 hằng số (`DECK_NAME_MAX`) cho cả 3.

---

## 7. 🟡 Magic string — `"Mặc định"`

**File:** [`FlashcardSrsService.java:281`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/service/FlashcardSrsService.java) + `"Từ cần ôn lại"` tại `:613`.

Chuỗi nghiệp vụ hard-code rải rác. **Đề xuất:** đưa lên `private static final String DEFAULT_DECK_NAME` / `REVIEW_DECK_NAME`.

---

## 8. 🟡 Trùng lặp logic resolve

**File:** [`FlashcardSrsService.java:656-779`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/service/FlashcardSrsService.java)

`buildRevealResponse` (switch theo `contentType` lấy word/meaning/furigana/example) **lặp lại** phần lớn logic của `resolveFront` + `resolveBackMeta`. 3 hàm cùng switch `VOCABULARY/KANJI/GRAMMAR/CUSTOM` + cùng check `status == PUBLISHED`.

**Đề xuất:** Trích một `ResolvedCard` (front, back, furigana, exampleJp/Vi, audio, stroke) dùng chung cho cả reveal lẫn list, tránh lệch logic khi sửa.

---

## 9. 🟡 Micro — khởi tạo `Random` lặp lại

**File:** [`FlashcardSrsService.java:508`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/service/FlashcardSrsService.java) (`new Random()` mỗi `getSession`) và truyền vào `buildQuiz` `:640`.

**Đề xuất:** Dùng `ThreadLocalRandom.current()` (không cần giữ instance), giảm cấp phát.

---

## 10. 🟡 Naming lệch — `WrongWord.frontText`

**File:** [`ReviewResultResponse.java:25`](../../apps/backend/src/main/java/com/jlpt/feature/flashcard/dto/ReviewResultResponse.java)

```java
public record WrongWord(String contentType, Long contentId, String frontText) {}
```
Service gán `new WrongWord("vocabulary", id, v.getWord())` → field tên `frontText` nhưng chứa `word`. Gây nhầm khi đọc.

**Đề xuất:** Đổi tên field thành `word` cho khớp ngữ nghĩa (cập nhật FE).

---

## 11. ⚪ Doc rác / spec lỗi thời

- [`.sdd/specs/backend/feat-flashcard/SPEC.md`](../../.sdd/specs/backend/feat-flashcard/SPEC.md): **rỗng** (1 dòng) → spec chết. Đã có spec mới đầy đủ; nên trỏ tới hoặc xoá file rỗng.
- [`.sdd/specs/backend/feat-dictionary-bookmark/SPEC.md`](../../.sdd/specs/backend/feat-dictionary-bookmark/SPEC.md): mô tả bookmark qua `student_content_progress` — **code đã thay** bằng sổ "Từ cần ôn lại". Đánh dấu DEPRECATED để khỏi gây hiểu nhầm.

---

## 12. ⚪ Cây thư mục trùng — `project-git/`

`project-git/` chứa bản sao gần như toàn bộ `docs/` + `.sdd/specs/` của repo (xem kết quả glob: `project-git\.sdd\specs\backend\feat-flashcard\SPEC.md` …). Nếu không phải submodule/backup có chủ đích thì đây là **rác lớn nhất** (file lỗi thời lẫn lộn với bản chính khi tìm kiếm).

**Đề xuất:** Xác nhận mục đích; nếu là backup cũ → xoá khỏi repo hoặc thêm `.gitignore`. (Cần hỏi trước khi xoá vì có thể chủ đích.)

---

## 13. KHÔNG phải rác (đã kiểm, vẫn dùng)

Để tránh xoá nhầm — các method dưới đây **có caller**, giữ nguyên:
`findAllByStudent` (getCards, deckId null), `findAllDue`, `softDeleteByDeckId`, `findWrongVocabCardsInSession`, `findByStudentAndContentIds`, `existsByStudentIdAndName`, `findByStudentIdAndIsReviewDeckTrue`, cả 2 converter (`FlashcardContentTypeConverter`, `FlashcardLastRatingConverter`).

---

## 14. THỨ TỰ DỌN ĐỀ XUẤT

1. **Xoá ngay (an toàn):** #1 dead method, #7 magic string, #9 Random.
2. **Refactor nhỏ + sửa FE:** #2 displayName, #4 newCount/reviewCount, #10 naming, #6 constraint.
3. **Sửa hành vi (cần test):** #3 CUSTOM normalize, #5 search gap.
4. **Refactor lớn:** #8 gộp resolve.
5. **Dọn doc/repo (hỏi trước):** #11, #12.
