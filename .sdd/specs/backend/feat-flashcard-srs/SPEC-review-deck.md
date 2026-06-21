# SPEC (Backend) — Sổ Tay "Từ cần ôn lại" (review deck)
> **Thuộc:** `feat-flashcard-srs` | **Bổ sung:** `SPEC.md` (định nghĩa `FR-FC-43..46` mà `FR-FC-81` tham chiếu)
> **Frontend ref:** `frontend/feat-student/SPEC-notebook.md`, `SPEC-dictionary.md`
> **Trạng thái:** Draft — chốt contract đầu vào/ra cho Sổ tay.

---

## 1. CONTEXT

"Từ cần ôn lại" là **một deck hệ thống** của riêng mỗi học viên (`flashcard_decks.is_system = 1`, `deck_name = 'Từ cần ôn lại'`), đóng vai trò **Sổ Tay** ở frontend. Thẻ trong deck đến từ 2 nguồn:
1. **Tự động** — cuối phiên học, các từ VOCABULARY trả lời `wrong`/`again` được thêm vào deck (`FR-FC-81`).
2. **Thủ công** — học viên bấm "Lưu vào sổ tay" ở Từ điển / danh sách từ vựng.

Học từ Sổ tay = mở phiên Flashcard theo `deckId` của deck này (đã có `POST /api/flashcards/session?deckId=`).

> **Lý do đặt tại `feat-flashcard-srs`** (không phải `feat-dictionary-bookmark`): bản chất là **deck flashcard** dùng chung cơ chế SM-2, card resolve live (§3.4), session — không phải bookmark `is_bookmarked`. Tham chiếu cũ "feat-dictionary-bookmark, FR-FC-43/44" trong `FR-FC-81` được sửa trỏ về spec này.

---

## 2. FUNCTIONAL REQUIREMENTS (EARS)

| ID | EARS Requirement |
|:---|:---|
| FR-FC-43 | THE SYSTEM SHALL provide a per-student **system deck** named `Từ cần ôn lại` (`is_system = 1`). IF it does not exist when a word is first added, THE SYSTEM SHALL lazily create it for that student. |
| FR-FC-44 | WHEN a Student adds words to the review deck (`POST /api/flashcards/review-deck/add`), THE SYSTEM SHALL, for each item `{contentType, contentId}`, upsert a `flashcards` row into the review deck with `interval_days = 1`, `ease_factor = 2.50`, `next_review_date = CURRENT_DATE`, `repetition_count = 0`. IF a card for the same `(student_id, content_type, content_id)` already exists in the review deck, THE SYSTEM SHALL skip it (no duplicate) and count it as `skipped`. |
| FR-FC-45 | WHEN listing the review deck (`GET /api/flashcards?deckId={reviewDeckId}`), THE SYSTEM SHALL return cards resolved live from their source (`FR-FC-34`), excluding any whose source is soft-deleted or `status != 'published'`. |
| FR-FC-46 | WHEN a Student removes a card (`DELETE /api/flashcards/{flashcardId}`), THE SYSTEM SHALL soft-delete it (`is_deleted = 1`) only IF the card belongs to that student; otherwise HTTP 403. |
| FR-FC-47 | THE SYSTEM SHALL NOT allow deleting the review deck itself via `DELETE /api/flashcard-decks/{deckId}` (system deck → HTTP 403 `SYSTEM_DECK_IMMUTABLE`, đã có FR-FC-05). |
| FR-FC-48 | WHEN building the `review-deck/add` payload automatically at session end, THE SYSTEM SHALL only include VOCABULARY items answered `wrong`/`again` (FR-FC-81); manual adds MAY include any supported `contentType` admitted by FR-FC-44. (Bản này giới hạn `contentType = VOCABULARY`; mở rộng KANJI/GRAMMAR là tương lai.) |

---

## 3. API SPEC

### 3.1 `POST /api/flashcards/review-deck/add`
**Actor:** Student | **Auth:** Bearer JWT
> Thêm 1..N từ vào deck "Từ cần ôn lại" (idempotent theo `(contentType, contentId)`). Lazily tạo deck nếu chưa có (FR-FC-43).

**Request:**
```json
{ "items": [ { "contentType": "VOCABULARY", "contentId": 12 } ] }
```

**Response (200):**
```json
{
  "status": 200,
  "message": "Đã thêm vào Sổ tay",
  "data": { "deckId": 7, "added": 1, "skipped": 0, "flashcardIds": [501] }
}
```

| Trường | Ý nghĩa |
|:--|:--|
| `added` | số thẻ tạo mới |
| `skipped` | số thẻ đã tồn tại trong deck (bỏ qua, không lỗi) |
| `deckId` | id deck "Từ cần ôn lại" (FE dùng để mở session) |

---

### 3.2 `GET /api/flashcards?deckId={reviewDeckId}&page=0&size=200`
**Actor:** Student | **Auth:** Bearer JWT
> Liệt kê thẻ trong Sổ tay (FR-FC-45). Mở rộng item so với listing chuẩn §6 với metadata nguồn cho FE hiển thị cột "Nguồn".

**Response (200):** như `GET /api/flashcards` chuẩn, mỗi item bổ sung:
```json
{
  "flashcardId": 501, "contentType": "VOCABULARY", "contentId": 12,
  "frontText": "食べる", "meaning": "Ăn", "jlptLevel": "N5",
  "nextReviewDate": "2026-06-18", "isDue": true,
  "addedReason": "wrong",          // 'wrong' | 'manual' | 'learn' | null
  "sourceTopic": "Động từ N5",     // nullable
  "sourceLevel": "N5"              // nullable
}
```
> `addedReason`/`sourceTopic`/`sourceLevel` là **tùy chọn** — nếu chưa lưu được, trả `null`; FE ẩn dòng meta nguồn.

---

### 3.3 `DELETE /api/flashcards/{flashcardId}`
**Actor:** Student | **Auth:** Bearer JWT
> Gỡ 1 từ khỏi Sổ tay (soft-delete, FR-FC-46).

**Response (200):**
```json
{ "status": 200, "message": "Đã gỡ khỏi Sổ tay", "data": null }
```

---

### 3.4 Định danh deck "Từ cần ôn lại"
`GET /api/flashcard-decks` (đã có) trả deck này như một system deck. **Khuyến nghị bổ sung cờ** để FE không phải so khớp tên:
```json
{ "deckId": 7, "deckName": "Từ cần ôn lại", "isSystem": true, "deckType": "review",
  "totalCards": 12, "dueToday": 3, "nextReviewDate": "2026-06-18" }
```
> Nếu chưa thêm `deckType`, FE tạm so khớp `deckName === 'Từ cần ôn lại'` (đã ghi `SPEC-notebook.md §12`).

---

## 4. DATA MODEL (không cột bắt buộc mới)

Dùng bảng `flashcards` + `flashcard_decks` sẵn có. `addedReason`/`sourceTopic`/`sourceLevel` là **tùy chọn**:
- Nếu muốn lưu bền → thêm cột `added_reason NVARCHAR(20) NULL` vào `flashcards` (migration mới, nhỏ). `sourceTopic/sourceLevel` resolve live từ `vocabulary.topic/jlpt_level` khi list (không cần cột).
- Nếu **không** thêm cột → `addedReason = null`, FE ẩn meta. **Mặc định khuyến nghị:** thêm `added_reason` (rẻ, hữu ích cho UX "Nguồn").

---

## 5. ERROR HANDLING

| HTTP | Error Code | Trigger |
|:--:|:--|:--|
| 400 | `VALIDATION_FAILED` | `items` rỗng hoặc `contentType` ngoài danh mục cho phép |
| 401 | `UNAUTHORIZED` | thiếu/hết hạn JWT |
| 403 | `FORBIDDEN` | xoá thẻ không thuộc student (FR-FC-46) |
| 403 | `SYSTEM_DECK_IMMUTABLE` | xoá deck "Từ cần ôn lại" (FR-FC-47) |
| 404 | `CONTENT_NOT_FOUND` | `contentId` không tồn tại / không published |
| 404 | `FLASHCARD_NOT_FOUND` | `flashcardId` sai khi DELETE |

---

## 6. ACCEPTANCE CRITERIA

| ID | Given | When | Then |
|:--|:--|:--|:--|
| AC-RD-01 | Student chưa có deck review | POST review-deck/add 1 vocab | Lazily tạo deck + 1 thẻ, `added=1` |
| AC-RD-02 | Vocab đã có trong deck | POST cùng vocab lần 2 | `added=0, skipped=1`, không trùng |
| AC-RD-03 | Deck có 1 vocab nguồn bị `deleted` | GET deckId | Thẻ đó bị ẩn (FR-FC-45) |
| AC-RD-04 | Thẻ của student khác | DELETE flashcardId | 403 FORBIDDEN |
| AC-RD-05 | Cuối phiên có 2 từ sai | session kết thúc `isLastCardInSession` | Gợi ý/auto add 2 từ vào "Từ cần ôn lại" (FR-FC-81) |

---

## OUT OF SCOPE
- ❌ Thêm KANJI/GRAMMAR vào Sổ tay (chỉ VOCABULARY — FR-FC-48).
- ❌ Ghi chú cá nhân trên từng từ trong Sổ tay (khác bookmark `bookmark_note` ở `feat-dictionary-bookmark`).
- ❌ Export/chia sẻ Sổ tay.
