# PLAN — Học Từ Vựng (`feat-vocabulary`)

## 1. Mục tiêu (Goals)
Triển khai luồng học Từ vựng phía học viên theo `SPEC.md` (UC-09), tách chuyên sâu từ `feat-core-learning`. Hệ thống cho phép liệt kê/lọc từ vựng theo **level N5→N1 + topic + search**, xem chi tiết, phát âm, đánh dấu tiến độ và thêm Flashcard — tuân thủ `CONSTITUTION.md`, `AGENTS.md`, `CLAUDE.md`.

## 2. Kiến trúc & Công nghệ
- **Backend:** Java 21, Spring Boot 3.x, Spring Data JPA, SQL Server, Migration Flyway/Liquibase.
- **Frontend:** React 18, Tailwind CSS (ref `frontend/feat-student/SPEC-vocabulary.md`).
- **Tuân thủ:** Controller → Service → Repository → Entity; DTO Pattern bắt buộc (ADR-005); business logic ở backend; response chuẩn `{status, message, data}`.

## 3. Các thành phần Backend
### 3.1. Database Migration & Entities
- Migration cho bảng `vocabulary` (kèm `is_vip_only`, `is_deleted`, index `IX_vocabulary_level_topic_status`) và đảm bảo `student_content_progress` tồn tại (dùng chung `feat-core-learning`).
- Entity `Vocabulary` (`@Entity`, map `vocabulary`); tái dùng `StudentContentProgress`, `Flashcard`.
- Soft Delete qua `is_deleted` / `status = 'deleted'`.

### 3.2. Repositories (Spring Data JPA)
- `VocabularyRepository`: query lọc theo `jlpt_level` + `topic` + keyword (JPQL/Specification), chỉ `status='published'` và `is_deleted=0`, phân trang.
- Query distinct topic theo level (FR-VOC-05); query `completedCount` join `student_content_progress`.
- Dùng `@EntityGraph`/`JOIN FETCH` tránh N+1 (NFR-VOC-02).

### 3.3. DTOs
- Request: tái dùng `LearningProgressRequest`, `AddFlashcardRequest` (validation `@NotNull`, `@Pattern` cho contentType, `@Min/@Max` cho progressPercent).
- Response: `VocabularyListItemResponse`, `VocabularyDetailResponse`, `VocabTopicResponse`, `PageResponse<T>`. Mapping tại Service (MapStruct/thủ công), không lộ Entity.

### 3.4. Services (Business Logic)
- **`VocabularyService`**:
  - `list(level, topic, search, pageable)`: validate level ∈ {N5..N1} (FR-VOC-20), clamp page/size (FR-VOC-25), annotate `isCompleted`/`isInFlashcard`, check `is_vip_only` theo subscription (FR-VOC-22).
  - `getTopics(level)`: distinct topic + count.
  - `getDetail(vocabId, student)`: 404 nếu chưa published/đã xóa (FR-VOC-21); cập nhật `last_activity_date` (FR-VOC-14).
- **`LearningProgressService`** (dùng chung): upsert progress, chặn regress (FR-VOC-11 → 422 `PROGRESS_REGRESSION`).
- **`FlashcardService`** (dùng chung): tạo flashcard, chặn trùng (FR-VOC-23 → 409).

### 3.5. Controllers & Security
- `VocabularyController`: `GET /api/vocabulary`, `GET /api/vocabulary/topics`, `GET /api/vocabulary/{id}` — `@PreAuthorize("hasRole('STUDENT')")`.
- Tái dùng `POST /api/learning-progress`, `POST /api/flashcards`.
- `GlobalExceptionHandler`: map 400/401/403/404/409/422/500 theo §7 SPEC.

## 4. Các thành phần Frontend
- Trang `VocabularyList` + `VocabCard` (theo `SPEC-vocabulary.md`): level tabs, topic select, search debounce 400ms, phân trang, audio play, nút Flashcard/Đã học.
- API service: `getVocabularyList`, `getVocabTopics`, `markVocabComplete`, `addVocabToFlashcard`.

## 5. Definition of Done
- Unit test Service ≥ 80% (đặc biệt validate level, chặn regress, VIP check).
- Integration test cho `/api/vocabulary/*` (happy + error path, 100% endpoint).
- Không trộn level (NFR-VOC-05); upsert không tạo duplicate (NFR-VOC-06).
- Response đúng `{status, message, data}`; không Entity ra API; không TODO; pass lint.
