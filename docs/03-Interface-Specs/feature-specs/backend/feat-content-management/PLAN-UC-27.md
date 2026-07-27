# PLAN — Manage Learning Content (`feat-content-management`) — UC-27

> **UC Coverage:** UC-27 (Manage Learning Content)
> **Trọng tâm:** Tạo / cập nhật / gửi duyệt vocabulary (`vocabulary`) và kanji (`kanji`) — vai trò Staff
> **Nguồn:** `UC-27-manage-learning-content.md` | **Cập nhật:** 2026-06-12
> **Lưu ý:** Media chỉ lưu URL (ADR-006).

---

## 1. Mục tiêu (Goals)

Cho phép Staff **tạo và cập nhật** vocabulary/kanji ở `draft`, lưu media dạng URL, và **gửi duyệt** qua endpoint thống nhất; cấm tự publish/archive, chỉ sửa khi `status∈{draft,rejected}`.

## 2. Kiến trúc & Công nghệ

Như `PLAN.md` §2. Bảng: `vocabulary`, `kanji`; đọc thêm `lessons` (chỉ kiểm tra `lessonId` tùy chọn) và `staff_users`. UC-27 chỉ ĐỌC schema, không đổi cấu trúc.

## 3. Quyết định thiết kế chính (Design Decisions)

- **Quy tắc chung 2 loại (FR-27-01..08):** ép `draft` + `created_by`; JLPT ∈ {N5..N1}; media chỉ URL (ADR-006); sửa chỉ khi `draft`/`rejected` (409 `INVALID_STATUS_TRANSITION`); cấm `published`/`archived` (403 `PUBLISH_NOT_ALLOWED`); ownership; refresh `updated_at`; SLF4J log.
- **Vocabulary (FR-27-16..19):** cần `word`/`meaning`/`furigana`/`jlptLevel`; `lessonId` (nếu có) phải tồn tại & chưa `deleted` (404 `LESSON_NOT_FOUND`).
- **Kanji (FR-27-20..24):** cần `characterValue`/`meaning`/`jlptLevel` + ≥1 `onyomi`/`kunyomi`; `characterValue` **UNIQUE** (409 `KANJI_DUPLICATE`); `strokeCount>=1` nếu có.
- **Submit-review đa loại (FR-27-25/26):** `contentType∈{vocabulary,kanji}`; re-validate theo loại + guard status → `pending_review`.

## 4. Các thành phần Backend

### 4.1. Database Migration

- Tận dụng index sẵn (`IX_vocab_*`, `IX_kanji_public_level`). KHÔNG sửa cột; ràng buộc NV enforce ở Service.

### 4.2. Entities

- `Vocabulary`, `Kanji` (map đủ cột + workflow) và `Lesson` read-only. Enum `JlptLevel`.

### 4.3. Repositories

- `VocabularyRepository`, `KanjiRepository` (+ `existsByCharacterValue`), `findByCreatedByWithFilters(...)`; `LessonRepository.findActiveById(...)` chỉ phục vụ liên kết từ vựng.

### 4.4. DTOs

- Request: `CreateVocabularyRequest`, `CreateKanjiRequest`, `SubmitReviewRequest`.
- Response: `VocabularyDetailResponse`, `KanjiDetailResponse`.

### 4.5. Services

- `LearningContentService` (`@Transactional`): create/update vocabulary/kanji + ràng buộc theo type; submit-review qua `ContentSubmissionService`.
- Exceptions: `KanjiDuplicateException`(409), `LessonNotFoundException`(404, liên kết từ vựng), `ContentNotFoundException`(404), `InvalidStatusTransitionException`(409), `PublishNotAllowedException`(403), `OwnershipDeniedException`(403).

### 4.6. Controllers & Security

- `StaffLearningContentController` (`/api/staff/vocabulary`, `/api/staff/kanji`): create/update.
- `@PreAuthorize("hasAnyAuthority('STAFF','STAFF_MANAGER')")`; map lỗi theo UC-27 §7.

## 5. Các thành phần Frontend

- `LearningContentPage` (tab vocabulary/kanji); upload trả URL (ADR-006); form theo loại; loading/error.

## 6. Definition of Done

- Kanji trùng bị chặn (409); vocabulary thiếu furigana bị chặn.
- Media chỉ URL (không BLOB); sửa theo state-machine; cấm publish; ownership.
- DTO; SLF4J; soft delete; coverage ≥ 80% (`feat-testing/TC-UC-27`).
