# PLAN — Học Kanji (`feat-kanji`)

## 1. Mục tiêu (Goals)

Triển khai luồng học Kanji phía học viên theo `SPEC.md` (UC-07), tách chuyên sâu từ `feat-core-learning`. Hệ thống cho phép liệt kê/lọc Kanji theo **level N5→N1 + số nét + bộ thủ + search**, xem chi tiết (ảnh thứ tự nét tĩnh, onyomi/kunyomi, từ ví dụ), đánh dấu tiến độ và thêm Flashcard — tuân thủ `CONSTITUTION.md`, `AGENTS.md`, `CLAUDE.md`, ADR-007.

## 2. Kiến trúc & Công nghệ

- **Backend:** Java 21, Spring Boot 3.x, Spring Data JPA, SQL Server, Migration Flyway/Liquibase.
- **Frontend:** React 18, Tailwind CSS (ref `frontend/feat-student/SPEC-kanji-list.md`, `SPEC-kanji-practice.md`).
- **Tuân thủ:** Controller → Service → Repository → Entity; DTO Pattern (ADR-005); business logic ở backend; response chuẩn `{status, message, data}`; stroke order chỉ ảnh tĩnh (ADR-007).

## 3. Các thành phần Backend

### 3.1. Database Migration & Entities

- Migration bảng `kanji` (kèm `radical`, `is_vip_only`, `is_deleted`, index `IX_kanji_level_status_stroke`); đảm bảo `student_content_progress` dùng chung.
- Entity `Kanji` (`@Entity`); tái dùng `StudentContentProgress`, `Flashcard`.
- Soft Delete qua `is_deleted` / `status='deleted'`.

### 3.2. Repositories (Spring Data JPA)

- `KanjiRepository`: query lọc `jlpt_level` + khoảng `stroke_count` + `radical` + keyword (chỉ published, not deleted), phân trang.
- Query distinct `radical` + count theo level (endpoint `/radicals`).
- Query `completedCount` join `student_content_progress`; `@EntityGraph`/`JOIN FETCH` tránh N+1 (NFR-KAN-02).

### 3.3. DTOs

- Request dùng chung: `LearningProgressRequest`, `AddFlashcardRequest` (validation contentType/progressPercent).
- Response: `KanjiListItemResponse`, `KanjiDetailResponse`, `RadicalResponse`, `PageResponse<T>`. Mapping tại Service, không lộ Entity.

### 3.4. Services (Business Logic)

- **`KanjiService`**:
  - `list(level, strokeMin, strokeMax, radical, search, pageable)`: validate level (FR-KAN-20), `strokeMin ≤ strokeMax` (FR-KAN-26 → 400), clamp page/size (FR-KAN-25), annotate isCompleted/isInFlashcard, VIP check (FR-KAN-22).
  - `getRadicals(level)`: distinct radical + count.
  - `getDetail(kanjiId, student)`: 404 nếu chưa published/đã xóa (FR-KAN-21); trả `stroke_order_url` ảnh tĩnh (FR-KAN-06, ADR-007); cập nhật `last_activity_date`.
- **`LearningProgressService`** (dùng chung): upsert `content_type='kanji'`, chặn regress (FR-KAN-11 → 422).
- **`FlashcardService`** (dùng chung): tạo flashcard kanji, chặn trùng (FR-KAN-23 → 409).

### 3.5. Controllers & Security

- `KanjiController`: `GET /api/kanji`, `GET /api/kanji/radicals`, `GET /api/kanji/{id}` — `@PreAuthorize("hasRole('STUDENT')")`.
- Tái dùng `POST /api/learning-progress`, `POST /api/flashcards`.
- `GlobalExceptionHandler`: map 400/401/403/404/409/422/500 theo §7 SPEC.

## 4. Các thành phần Frontend

- Trang `KanjiList` + `KanjiCard` (theo `SPEC-kanji-list.md`): level tabs, lọc số nét/bộ thủ, search, phân trang.
- Trang chi tiết: hiển thị ảnh thứ tự nét tĩnh, onyomi/kunyomi, từ ví dụ; nút Flashcard / Đã học.
- API service: `getKanjiList`, `getKanjiRadicals`, `getKanjiDetail`, `markKanjiComplete`, `addKanjiToFlashcard`.

## 5. Definition of Done

- Unit test Service ≥ 80% (validate level, stroke range, VIP check, chặn regress).
- Integration test `/api/kanji/*` (happy + error path, 100% endpoint).
- Không trộn level (NFR-KAN-05); upsert không duplicate (NFR-KAN-06); stroke order chỉ ảnh tĩnh (ADR-007).
- Response đúng `{status, message, data}`; không Entity ra API; không TODO; pass lint.
