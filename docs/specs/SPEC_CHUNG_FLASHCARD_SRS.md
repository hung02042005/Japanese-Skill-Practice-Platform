# SPEC CHUNG — Flashcard SRS + Dictionary + Bookmark
## feat-flashcard-srs · feat-dictionary-bookmark
### Người thực hiện: Người 4 | Ngày: 2026-06-09

---

## 1. MỤC TIÊU MODULE

| Module | Mục tiêu |
|--------|----------|
| **Flashcard SRS** | Học sinh ôn tập từ vựng/kanji/ngữ pháp theo lịch SM-2 tự động, chia theo **topic** để học có chủ đề |
| **Dictionary** | Tra cứu nhanh toàn bộ kho Vocab + Kanji + Grammar + Lesson trong một endpoint duy nhất |
| **Bookmark** | Đánh dấu nội dung cần ghi nhớ; tự động gợi ý bookmark khi trả lời sai flashcard |

---

## 2. PHÂN TÍCH DB — KHÔNG XUNG ĐỘT

> Kết quả kiểm tra toàn bộ entity liên quan. **Tất cả ý tưởng đề xuất đều tương thích với schema hiện tại — không cần migration mới.**

### 2.1 Mapping ý tưởng → DB field

| Ý tưởng đề xuất | Field DB hỗ trợ | Ghi chú |
|-----------------|-----------------|---------|
| Flashcard chia theo topic | `vocabulary.topic VARCHAR(100)` | Dùng làm `deckName` khi tạo card từ vocab |
| Flashcard lấy từ kho vocab | `flashcard.content_type + content_id` | Polymorphic ref đã có sẵn |
| SM-2 scheduling | `flashcard.interval_days`, `repetition_count`, `next_review_date`, `last_rating` | Đủ 3 field SM-2 cần |
| Gợi ý bookmark khi sai | `student_content_progress.is_bookmarked`, `bookmark_note`, `bookmarked_at` | Không cần bảng mới |
| Tìm kiếm theo topic | `vocabulary.topic`, `kanji.jlpt_level`, `grammar_points.jlpt_level` | Đủ để filter |

### 2.2 Lưu ý tương thích ContentType

```
Flashcard.ContentType enum:   KANJI | VOCABULARY | GRAMMAR | CUSTOM
StudentContentProgress.ContentType: LESSON | VOCABULARY | KANJI | KANA | GRAMMAR
```

Overlap: VOCABULARY, KANJI, GRAMMAR → dùng được cho cả Bookmark lẫn Flashcard.
KANA flashcard → dùng `ContentType.CUSTOM` trên Flashcard (KanaCharacter không có topic/jlpt).

---

## 3. THIẾT KẾ DECK THEO TOPIC

### 3.1 Cơ chế tổ chức Deck

```
deckName convention:  "{JLPT_LEVEL}_{TOPIC}"
Ví dụ:
  - "N5_食べ物"       ← từ vựng chủ đề ăn uống N5
  - "N5_動詞"         ← động từ N5
  - "N4_KANJI"        ← kanji N4 (topic mặc định cho kanji)
  - "N3_GRAMMAR"      ← ngữ pháp N3
  - "Mặc định"        ← deck thủ công (giữ default hiện tại)
```

### 3.2 Nguồn dữ liệu cho Deck

| Source | Cách lấy topic | deckName pattern |
|--------|---------------|-----------------|
| Vocabulary | `vocabulary.topic` (đã có) | `{jlptLevel}_{topic}` |
| Kanji | Không có topic → dùng level | `{jlptLevel}_KANJI` |
| GrammarPoint | Không có topic → dùng level | `{jlptLevel}_GRAMMAR` |
| Custom (tự tạo) | User tự đặt | bất kỳ string |

---

## 4. THUẬT TOÁN SM-2

```
Input: rating ∈ {EASY=5, HARD=3, WRONG=1}  (mapped internally)
Fields used: intervalDays, repetitionCount, lastRating, nextReviewDate

Logic:
  if rating == WRONG:
    repetitionCount = 0
    intervalDays = 1
  else if rating == HARD:
    intervalDays = max(1, intervalDays * 1.2)   // tăng chậm
  else (EASY):
    if repetitionCount == 0: intervalDays = 1
    if repetitionCount == 1: intervalDays = 6
    else: intervalDays = round(intervalDays * 2.5)
    repetitionCount += 1

nextReviewDate = today + intervalDays days
lastReviewedAt = now()
lastRating = rating
```

---

## 5. FLOW GỢI Ý BOOKMARK KHI SAI

```
Student submit review (rating=WRONG)
  → FlashcardSrsService.submitReview()
      ├── Cập nhật SM-2 (reset interval)
      ├── Nếu topic session kết thúc (dueOnly deck hết card)
      │     → Gom danh sách cardId có lastRating=WRONG trong session
      │     → Trả suggestedBookmarks: [{ contentType, contentId, front }]
      └── ReviewResultResponse.suggestBookmark = true

Frontend nhận suggestedBookmarks
  → Hiển thị popup "Bạn muốn thêm vào sổ tay?"
  → Student confirm → POST /api/bookmarks (BookmarkRequest)
```

---

## 6. DEPENDENCY MAP

```
Person 4 (Người 4) PHỤ THUỘC:
  ← Người 1: VocabularyRepository, KanjiRepository, GrammarPointRepository, LessonRepository
  ← Người 3: StudentContentProgressRepository

Person 4 CUNG CẤP:
  → FlashcardRepository (các module khác có thể tham chiếu nếu cần)
  → BookmarkService (có thể tái sử dụng trong module khác)
```

---

## 7. SECURITY & AUTHORIZATION

| Rule | Chi tiết |
|------|---------|
| Tất cả API | Yêu cầu `@AuthenticationPrincipal` (JWT valid) |
| Role | Chỉ STUDENT được gọi các endpoint này |
| Data isolation | Mọi query phải filter `student_id = currentStudentId` |
| Content filter | Chỉ trả content có `status = PUBLISHED` |
| Level gate | Dictionary/Flashcard chỉ trả nội dung ≤ student.currentJlptLevel (nếu FREE); VIP xem tất cả |

---

## 8. ERROR HANDLING

| Trường hợp | HTTP | message |
|-----------|------|---------|
| Flashcard không thuộc student | 403 | "Flashcard không thuộc về bạn" |
| contentId không tồn tại | 404 | "Nội dung không tồn tại" |
| contentType không hợp lệ | 400 | "Loại nội dung không hợp lệ" |
| Tìm kiếm chuỗi rỗng | 400 | "Từ khóa tìm kiếm không được để trống" |
| Bookmark trùng | 200 | Idempotent — không báo lỗi, trả lại bookmark hiện có |
| Xóa bookmark không tồn tại | 404 | "Bookmark không tồn tại" |

---

## 9. PAGINATION

Tất cả API trả danh sách đều hỗ trợ:
```
?page=0&size=20&sort=nextReviewDate,asc
```
Trả về `Page<T>` wrapped trong `ApiResponse<Page<DTO>>`.

---
