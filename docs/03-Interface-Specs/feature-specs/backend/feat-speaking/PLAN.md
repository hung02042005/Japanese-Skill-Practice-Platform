# PLAN — Speaking End-to-End (`feat-speaking`)

> **UC Coverage:** UC-SPK-01 (Staff soạn bài Speaking), UC-SPK-02 (StaffManager duyệt), UC-13 (Student luyện nói), UC-31 (Staff chấm bài)
> **Nguồn:** `SPEC.md` | **Cập nhật:** 2026-07-21
> **Nguyên tắc chủ đạo:** **Tái sử dụng tối đa** hạ tầng đã có; chỉ bổ sung phần còn thiếu (nhiều câu hỏi/bài + wiring speaking vào review queue).

---

## 1. Mục tiêu (Goals)

Hoàn thiện **vòng đời đầy đủ** của một bài Speaking: Staff soạn (level + tiêu đề + nhiều câu hỏi & chi tiết) → gửi duyệt → StaffManager duyệt/từ chối/yêu cầu sửa (bốn mắt) → publish → Student ghi âm nộp (async) → AI pre-grade → Staff chấm tay (điểm cuối). Tuân thủ `CONSTITUTION.md`, `AGENTS.md`, `CLAUDE.md` (ADR-004/005/006/008, LESSON-001/006).

## 2. Kiến trúc & Công nghệ

- **Backend:** Java 21, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA, `@Async` cho chấm AI.
- **Database:** MySQL 8 (ADR-009); Migration bằng Flyway — **thêm bảng `speaking_questions` + index**, KHÔNG đổi cột `lessons`/`student_submissions` có sẵn.
- **Storage:** Audio mẫu + bài nộp lưu `/uploads` hoặc S3, DB chỉ giữ URL (ADR-006, LESSON-002).
- **Frontend:** React 18 — Student (`SPEC-speaking.md` có sẵn), thêm màn Staff soạn + Manager duyệt (tái dùng ReviewQueue).
- **Kiến trúc:** Controller → Service → Repository → Entity; DTO Pattern (ADR-005); `@Transactional` ở Service; điểm 100% ở backend (Constitution §2.5).

## 3. Quyết định thiết kế chính (Design Decisions)

- **DD-01 — Speaking là `Lesson` (`lesson_type=speaking`), KHÔNG tạo entity mới.** Tận dụng nguyên cột workflow `status/created_by/approved_by/published_at` → tự động tương thích review queue UC-33 (`contentType=speaking` alias sang handler LESSON). Đồng bộ ràng buộc [[content-review-constraints]] "course=lessons".
- **DD-02 — Bổ sung bảng con `speaking_questions`** cho yêu cầu "nhiều câu hỏi + chi tiết câu hỏi". Bài legacy 1 câu hỏi vẫn đọc được qua `lessons.content_text` (backward-compat): nếu không có bản ghi `speaking_questions`, coi `content_text` là câu hỏi duy nhất.
- **DD-03 — Tái dùng dispatcher `submit-review` chung** (`POST /api/staff/contents/submit-review`, `contentType=speaking`) thay vì tạo endpoint riêng → tránh va chạm route (bài học UC-27 đã đi đường này).
- **DD-04 — Review dùng lại nguyên `ManagerReviewController` + `ContentReviewService`.** Chỉ cần đảm bảo resolver hiểu `type=speaking`. Self-review guard + guarded update (409) đã có sẵn — không viết lại.
- **DD-05 — Chấm điểm 2 tầng:** AI pre-grade (async, gợi ý) → Staff manual grade (authoritative). Tái dùng `SpeakingAsyncProcessor` (retry 3x + fallback) và `StaffGradingController`/`SupportTicketService.manualGrade`. `manual_score` override `ai_overall_score` khi hiển thị (đã đúng trong `SpeakingService.toGradedResponse`).
- **DD-06 — Trạng thái poll cho Student:** khi bài đã `ai_graded` nhưng chưa Staff chấm → có thể trả `AWAITING_REVIEW` (điểm AI provisional) hoặc `COMPLETED` với cờ provisional. Chọn giữ hành vi hiện tại (`COMPLETED` theo điểm AI) + thêm nhãn provisional để không phá frontend cũ.
- **DD-07 — Ép `status='draft'` khi tạo, chặn sửa khi `pending_review/published`** (FR-SPK-01/04) ở Service Layer; Staff không tự publish.

## 4. Các thành phần Backend

### 4.1. Database Migration (MỚI)

- `Vx__create_speaking_questions.sql`: bảng `speaking_questions(lesson_id FK, prompt_text, instruction, sample_audio_url, display_order, created_at, updated_at)`, charset utf8mb4, index `ix_spkq_lesson_order (lesson_id, display_order)`.
- (Tùy chọn) thêm cột nullable `speaking_question_id` vào `student_submissions` nếu cần chấm theo từng câu — mặc định **hoãn**, bản này chấm ở mức bài nộp.
- Đảm bảo có index `(lesson_type, jlpt_level, status, display_order)` trên `lessons` phục vụ list publish + review queue (NFR-SPK-06).

### 4.2. Entities

- **Tái dùng:** `Lesson`, `StudentSubmission`, `StaffUser`, `AdminAuditLog` — KHÔNG sửa.
- **MỚI:** `SpeakingQuestion` (`@Entity`, `@ManyToOne LAZY` về `Lesson`).

### 4.3. Repositories

- **MỚI:** `SpeakingQuestionRepository` — `findByLesson_IdOrderByDisplayOrderAsc`, `deleteByLesson_Id` (dùng khi replace toàn bộ câu hỏi lúc update draft).
- **Tái dùng:** `LessonRepository` (đã có `findByJlptLevelAndLessonTypeAndStatus...`, `findByIdAndStatus`), `StudentSubmissionRepository` (đã có `findSpeakingStats`, `findByIdAndStudent_Id`).

### 4.4. DTOs

- **Authoring (MỚI):** `SpeakingLessonCreateRequest` (`jlptLevel`, `title`, `questions[]`), `SpeakingQuestionDto` (`promptText`, `instruction`, `sampleAudioUrl`, `displayOrder`), `SpeakingLessonDetailResponse` (full câu hỏi + status).
- **Review (tái dùng):** `ReviewActionRequest`, `RequestChangesRequest`, `ReviewQueueResponse`, `ReviewableContentDetailResponse` — chỉ cần resolver render nội dung speaking (câu hỏi) trong detail.
- **Practice/Grading (tái dùng):** `SpeakingExerciseResponse`, `SpeakingSubmitResponse`, `SpeakingResultResponse`, `ManualGradeRequest`, `GradeResponse`, `SubmissionResponse`.
- Validation: `@NotBlank title`, `jlptLevel` hợp lệ, `questions` `@NotEmpty` + mỗi câu `promptText` `@NotBlank`, `manualScore` ∈ [0,100].

### 4.5. Services (Business Logic)

- **MỚI — `SpeakingAuthoringService`** (`@Transactional`):
  - `create(request, staffEmail)` — ép `draft`, set `createdBy`, lưu lesson + speaking_questions (FR-SPK-01/02/03).
  - `update(id, request, staffEmail)` — chỉ khi `draft/rejected`, replace câu hỏi atomic; 409 nếu pending/published (FR-SPK-04).
  - `getOwnDetail(id, staffEmail)` — Staff xem lại nội dung đã tạo (FR-SPK-05).
- **Tái dùng — dispatcher submit-review** (`StaffQuizSubmitReviewController` / service tương ứng): thêm nhánh `contentType=speaking` → `draft/rejected → pending_review` + notify Manager (FR-SPK-06/07).
- **Tái dùng — `ContentReviewService`:** đảm bảo resolver map `speaking`→lesson handler; detail render câu hỏi (FR-SPK-10..15). Self-review + concurrent guard + audit đã có.
- **Tái dùng — `SpeakingService` + `SpeakingAsyncProcessor`:** list publish theo level, submit async, poll, AI retry/fallback (FR-SPK-20..23).
- **Tái dùng — `SupportTicketService.manualGrade`:** Staff chấm tay, set `graded`, notify Student (FR-SPK-30..33).

### 4.6. Controllers & Security

- **MỚI — `StaffSpeakingContentController`** (`/api/staff/speaking-lessons`): `POST`, `PUT /{id}`, `GET /{id}`. `@PreAuthorize("hasRole('STAFF')")`; owner-check ở service.
- **Tái dùng:** `ManagerReviewController` (`/api/manager/**`), `SpeakingController` (`/api/speaking/**`), `StaffGradingController` (`/api/staff/submissions/**`).
- **Security:** phân biệt `staff_manager` enforce ở Service Layer (`StaffManagerGuard`) — JWT chỉ có `ROLE_STAFF` [[content-review-constraints]]. Student chặn khỏi `/api/staff/**` + `/api/manager/**`.
- **Exception:** custom exception kế thừa `BusinessException(status, errorCode, msg)` để map qua `GlobalExceptionHandler` (ADD-ONLY, ADR-008): `INVALID_STATE_TRANSITION`(409), `CONTENT_NOT_FOUND`(404), `VALIDATION_FAILED`(400).

## 5. Các thành phần Frontend

- **Student (tái dùng):** `SpeakingPage.jsx` + `AudioRecorder.jsx` (xem `feat-student/SPEC-speaking.md`) — cần mở rộng hiển thị nhiều câu hỏi/prompt + nhãn "đang chờ giáo viên chấm" khi provisional.
- **Staff (MỚI):** `SpeakingAuthorPage` — form level + title + danh sách câu hỏi (thêm/xóa/sắp xếp), nút Lưu nháp / Gửi duyệt; xem lại nội dung đã tạo.
- **Staff Grading (tái dùng):** màn hàng đợi chấm bài nói (UC-31) — nghe audio + nhập điểm/feedback.
- **StaffManager (tái dùng):** `ReviewQueuePage` + `ReviewDetailDrawer` với `type=speaking` (render câu hỏi).
- Tách UI theo role (LESSON-001); API gom trong `services/`.

## 6. Tiêu chuẩn đánh giá (Definition of Done)

- Tạo bài ép `draft`; sửa khi pending/published → 409; submit-review chỉ từ draft/rejected.
- Review queue hiển thị speaking `pending_review`; approve → `published` + audit; self-review → 403; concurrent → 409 (tái dùng, có test hồi quy).
- Student chỉ thấy bài `published`; nộp trả 202 `jobId`; AI lỗi 3 lần → fallback thân thiện (không 5xx).
- Staff chấm tay → `graded`, điểm override AI hiển thị cho Student; notify hoạt động.
- Nhiều câu hỏi lưu đúng thứ tự; bài legacy 1 câu (`content_text`) vẫn chạy.
- Controller chỉ trả DTO; error đúng `{ status, message, data }`; utf8mb4 không lỗi kanji.
- Unit test service mới ≥ 80%; integration 3 nhóm endpoint mới/wiring; không TODO; pass `mvn spotless:apply` & lint.
