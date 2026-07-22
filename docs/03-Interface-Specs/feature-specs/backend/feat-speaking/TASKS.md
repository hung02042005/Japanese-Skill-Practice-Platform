# TASKS — Speaking End-to-End (`feat-speaking`)

> **UC:** UC-SPK-01, UC-SPK-02, UC-13, UC-31 | **Nguồn:** `SPEC.md`, `PLAN.md`
> **Cập nhật:** 2026-07-21
> **Chú thích:** 🆕 = viết mới · ♻️ = tái dùng/wiring code sẵn có · ✅ = đã tồn tại, chỉ verify

---

## Phase 1: Database & Domain

- [ ] 1.1 🆕 Migration `Vx__create_speaking_questions.sql`: bảng `speaking_questions(speaking_question_id PK, lesson_id FK→lessons, prompt_text TEXT NOT NULL, instruction TEXT NULL, sample_audio_url VARCHAR(500) NULL, display_order INT DEFAULT 0, created_at, updated_at)`, charset utf8mb4_unicode_ci; index `ix_spkq_lesson_order (lesson_id, display_order)` (DD-02, NFR-SPK-07).
- [ ] 1.2 🆕 Verify/thêm index trên `lessons (lesson_type, jlpt_level, status, display_order)` phục vụ list publish + review queue (NFR-SPK-06). KHÔNG sửa cột có sẵn.
- [ ] 1.3 🆕 Entity `SpeakingQuestion` (`@Entity @Table(name="speaking_questions")`, `@ManyToOne(LAZY) Lesson`, `@PreUpdate` cập nhật `updated_at`).
- [ ] 1.4 ✅ Xác nhận `Lesson.LessonType.SPEAKING`, `LessonStatus`, `StudentSubmission.SubmissionType.SPEAKING/SubmissionStatus` đã đủ — KHÔNG sửa.

## Phase 2: Repository Layer

- [ ] 2.1 🆕 `SpeakingQuestionRepository`: `List<SpeakingQuestion> findByLesson_IdOrderByDisplayOrderAsc(Long)`, `void deleteByLesson_Id(Long)` (replace khi update draft).
- [ ] 2.2 ✅ Verify `LessonRepository.findByJlptLevelAndLessonTypeAndStatusOrderByDisplayOrderAscIdAsc(...)` và `findByIdAndStatus(...)` dùng được cho list/submit.
- [ ] 2.3 ✅ Verify `StudentSubmissionRepository.findSpeakingStats(...)`, `findByIdAndStudent_Id(...)` — không đổi.

## Phase 3: DTO & Validation

- [ ] 3.1 🆕 `SpeakingQuestionDto` (`speakingQuestionId?`, `promptText @NotBlank`, `instruction`, `sampleAudioUrl`, `displayOrder`).
- [ ] 3.2 🆕 `SpeakingLessonCreateRequest` (`jlptLevel` hợp lệ, `title @NotBlank ≤255`, `questions @NotEmpty @Valid`).
- [ ] 3.3 🆕 `SpeakingLessonDetailResponse` (lessonId, title, jlptLevel, status, createdAt, questions[]).
- [ ] 3.4 ♻️ Tái dùng `ReviewActionRequest`/`RequestChangesRequest`, `SpeakingExerciseResponse`, `SpeakingSubmitResponse`, `SpeakingResultResponse`, `ManualGradeRequest`, `GradeResponse` — không tạo trùng.
- [ ] 3.5 🆕 Thêm nhãn provisional cho `SpeakingResultResponse` (vd cờ `provisional`/status `AWAITING_REVIEW`) — giữ tương thích frontend cũ (DD-06).

## Phase 4: Service Layer

### 4a. Authoring (UC-SPK-01) — 🆕
- [ ] 4.1 🆕 `SpeakingAuthoringService.create(req, staffEmail)`: resolve StaffUser, ép `status=draft`, set `createdBy`, `lessonType=SPEAKING`, lưu Lesson + `speaking_questions` theo `displayOrder` (FR-SPK-01/02/03, DD-07).
- [ ] 4.2 🆕 `update(id, req, staffEmail)`: owner-check; chỉ khi `draft/rejected` (ngược lại `INVALID_STATE_TRANSITION` 409); replace toàn bộ câu hỏi atomic trong 1 `@Transactional` (FR-SPK-04).
- [ ] 4.3 🆕 `getOwnDetail(id, staffEmail)`: trả full câu hỏi bất kể status; 404 nếu không phải của mình/không tồn tại (FR-SPK-05).
- [ ] 4.4 🆕 Backward-compat: nếu bài không có bản ghi `speaking_questions`, suy ra 1 câu hỏi từ `lessons.content_text` khi đọc (DD-02).

### 4b. Submit-review (UC-SPK-01) — ♻️
- [ ] 4.5 ♻️ Mở rộng dispatcher `POST /api/staff/contents/submit-review` xử lý `contentType=speaking`: `draft/rejected → pending_review` (409 nếu khác); validate FR-SPK-02 trước khi gửi (FR-SPK-06).
- [ ] 4.6 ♻️ Bắn notify tới StaffManager khi vào `pending_review` (tái dùng staff notification channel — FR-SPK-07).

### 4c. Review (UC-SPK-02) — ♻️/verify
- [ ] 4.7 ♻️ Xác nhận `ContentReviewService` resolver map `type=speaking` → lesson handler (review queue + approve/reject/request-changes chạy đúng cho speaking).
- [ ] 4.8 🆕 Bổ sung render nội dung speaking (danh sách câu hỏi) trong `ReviewableContentDetailResponse` để Manager xem đủ nội dung đã gửi (FR-SPK-11).
- [ ] 4.9 ✅ Verify self-review 403 + concurrent 409 + audit `admin_audit_logs` áp dụng cho speaking (FR-SPK-12..15) — không viết lại.

### 4d. Practice + Grading (UC-13 / UC-31) — ✅/♻️
- [ ] 4.10 ✅ Verify `SpeakingService.getExercises/submit/getResult` + `SpeakingAsyncProcessor` (retry 3x, fallback, clamp) khớp FR-SPK-20..23, LESSON-006.
- [ ] 4.11 ♻️ Verify `SupportTicketService.manualGrade` set `graded`, `manual_score/feedback`, `graded_by/at`, notify Student (FR-SPK-30..33); điểm Staff override AI khi poll (đã đúng trong `toGradedResponse`).

## Phase 5: Controller & Security

- [ ] 5.1 🆕 `StaffSpeakingContentController` (`/api/staff/speaking-lessons`): `POST`, `PUT /{id}`, `GET /{id}`; `@PreAuthorize("hasRole('STAFF')")`; owner-check ở service. Tránh va chạm route với các controller staff khác.
- [ ] 5.2 ✅ Verify `ManagerReviewController` (`/api/manager/**`), `SpeakingController` (`/api/speaking/**`), `StaffGradingController` (`/api/staff/submissions/**`) không cần đổi (chỉ dùng).
- [ ] 5.3 🆕 Custom exceptions kế thừa `BusinessException` (`INVALID_STATE_TRANSITION` 409, `CONTENT_NOT_FOUND` 404, `VALIDATION_FAILED`/`INVALID_LEVEL` 400) — map qua `GlobalExceptionHandler` sẵn có, ADD-ONLY (ADR-008).
- [ ] 5.4 🆕 Đảm bảo Student bị 403 khi gọi `/api/staff/**` & `/api/manager/**`; staff thường bị chặn hành vi manager ở service (NFR-SPK-02).

## Phase 6: Testing & QA (Backend)

- [ ] 6.1 🆕 Unit `SpeakingAuthoringService` ≥80%: create ép draft, validate thiếu câu hỏi (400), update chặn pending/published (409), replace câu hỏi đúng thứ tự, owner-check.
- [ ] 6.2 ♻️ Regression: review speaking (approve→published+audit, reject→rejected+feedback bắt buộc, self-review 403, concurrent 409).
- [ ] 6.3 ✅ Test AI fallback: engine lỗi 3 lần → không 5xx, poll trả trạng thái thân thiện, bài vẫn chấm được.
- [ ] 6.4 ♻️ Test manual grade: `graded`, điểm override AI, chỉ chủ sở hữu poll thấy (404 cho người khác).
- [ ] 6.5 🆕 Backward-compat: bài legacy 1 câu (`content_text`, không có `speaking_questions`) list + submit + poll chạy đúng.
- [ ] 6.6 Bất biến: response không lộ Entity; utf8mb4 giữ kanji; map về AC-SPK-01..14 (xem `feat-testing/TC-UC-SPK`).

## Phase 7: Frontend

- [ ] 7.1 🆕 `services/staffSpeakingService` (create/update/getDetail) + wiring submit-review, review, grade.
- [ ] 7.2 🆕 `SpeakingAuthorPage` (Staff): form level+title, danh sách câu hỏi (thêm/xóa/kéo thứ tự), Lưu nháp / Gửi duyệt, xem lại nội dung.
- [ ] 7.3 ♻️ `ReviewDetailDrawer` render `type=speaking` (hiển thị các câu hỏi + chi tiết) — Approve/Reject/Request Changes.
- [ ] 7.4 ♻️ Màn Staff chấm bài nói (UC-31): nghe audio + nhập điểm/feedback.
- [ ] 7.5 ♻️ `SpeakingPage.jsx` (Student): hỗ trợ nhiều câu hỏi + nhãn "đang chờ giáo viên chấm" khi provisional (DD-06). Tách UI theo role (LESSON-001).

## Phase 8: Final Review

- [ ] 8.1 Cross-check SPEC §3 (FR-SPK-*) ↔ code; xác nhận AC-SPK-01..14 đều có test phủ.
- [ ] 8.2 `mvn clean compile` BUILD SUCCESS; migration test trên DB trống (MySQL không rollback DDL — ADR-009).
- [ ] 8.3 Lint sạch (`mvn spotless:apply`, `npm run lint`); không TODO; PR ≤ 400 dòng (chia nhỏ nếu vượt).
