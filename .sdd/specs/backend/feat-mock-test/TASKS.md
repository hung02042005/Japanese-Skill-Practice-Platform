# TASKS — JLPT Mock Exam (`feat-mock-test`)

> **Feature ID:** `feat-mock-test` | **UC Coverage:** UC-10 (JLPT Mock Test)
> **Phiên bản:** 1.0 | **Cập nhật:** 2026-06-16
> **Phụ thuộc:** `feat-assessment` (dùng chung DB schema và một số Entities/Repos)
>
> **⚠️ Quyết định phạm vi (2026-06-16):** VIP/subscription gate (FR-MOCK-05, NFR-MOCK-03, BR-10-11, AC-10-11) đã
> bị loại khỏi iteration này theo quyết định sản phẩm — backend chưa có hệ thống subscription nào (không entity,
> không cột, không service) để check real-time. Mọi assessment loại `exam` hiện được coi là truy cập tự do.
> Các task liên quan VIP dưới đây được đánh dấu ⏭️ SKIPPED (theo quyết định) thay vì ✅.

---

## Phase 1: Database & Entities (phối hợp với `feat-assessment`)

- [x] **1.1** Xác nhận migration từ `feat-assessment` đã tạo đầy đủ bảng `assessments`, `test_attempts`, `attempt_answers`, `question_assignments` — không duplicate migration. Đã xác nhận V1__init_schema.sql có đủ cột (kể cả `language_knowledge_score`, `reading_score`, `listening_score`, `status` CHECK có `auto_submitted`) — **không cần migration mới**.
- [x] **1.2** Thêm sample data cho 1 exam mẫu (N3) với `assessment_type = 'exam'`, `duration_min`, `pass_score`, `total_score`, `audio_url` — qua `DevDataSeeder.seedMockExam()` (dev/test profile, không phải Flyway migration vì đây là dữ liệu demo, nhất quán với cách `seedQuizzes()` hiện có đã làm).
- [x] **1.3** Thêm sample `question_assignments` với `section_name` đúng convention: `language_knowledge`, `reading`, `listening` — trong `seedExamSection()`.

## Phase 2: DTOs & Validation

- [x] **2.1** Request DTO: `SubmitExamRequest` — `attemptId`, `isAutoSubmit`, `answers: List<AnswerRequest>` (tái dùng `AnswerRequest` đã có từ feat-assessment thay vì tạo `ExamAnswerRequest` trùng lặp — cùng field/validation).
- [x] **2.2** `AnswerRequest` (tái dùng) — `questionId: Long`, `selectedOption: @Pattern("[ABCD]")|null`, `answerText: String|null` (đã có sẵn từ feat-assessment).
- [x] **2.3** `passScore`/`durationMin`/`questionCount` — thêm `passScore` vào `AssessmentSummaryResponse` đã có (tái dùng `GET /api/assessments?type=exam` hiện có thay vì tạo `ExamListResponse` riêng). `isVipOnly` ⏭️ SKIPPED theo quyết định phạm vi.
- [x] **2.4** Response DTO: `ExamStartResponse` — `attemptId`, `startedAt`, `expiresAt`, `sections[]`, `listeningAudioUrl`.
- [x] **2.5** `SectionResponse` (tái dùng từ feat-assessment) — `sectionName`, `questions: List<QuestionResponse>`.
- [x] **2.6** `QuestionResponse` (tái dùng, mở rộng) — bổ sung `skill`, `audioUrl`, `imageUrl` vào DTO đã có của feat-assessment thay vì tạo `ExamQuestionResponse` trùng lặp. Vẫn **KHÔNG có `correctOption`**.
- [x] **2.7** Response DTO: `ExamStatusResponse` — `attemptId`, `status`, `remainingSeconds`, `isExpired`.
- [x] **2.8** Response DTO: `ExamSubmitResponse` — `totalScore`, `maxScore`, `isPassed`, `sectionScores`, `durationSeconds`, `submittedAt`, `results: List<ExamResultItem>`.
- [x] **2.9** Response DTO: `ExamResultItem` — `questionId`, `sectionName`, `isCorrect`, `selectedOption`, `correctOption`, `score`, `explanation`.
- [x] **2.10** Response DTO: `ExamHistoryResponse` — kèm `sectionScores`, `isPassed`, `status`.
- [x] **2.11** Response DTO: `ExamReviewResponse` + `ExamReviewItem` — kèm `questionText`, `optionA–D` cho mỗi câu.

## Phase 3: Business Logic (MockExamService)

- [x] **3.1** `MockExamService.startExam(assessmentId, studentId)` — validate type=exam + published, tạo `TestAttempt` server-time, group theo section, `expiresAt`, trả `ExamStartResponse`. VIP check ⏭️ SKIPPED theo quyết định phạm vi (xem header).
- [x] **3.2** `MockExamService.getExamStatus(attemptId, studentId)` — 403 nếu sai owner, `remainingSeconds = max(0, expiresAt-now)`.
- [x] **3.3** `MockExamService.submitExam(...)` — pessimistic lock (`findByIdForUpdate`), check already-submitted, time validation (skip nếu auto-submit), tính 3 section score + total, invariant check `[0,maxScore]`, `isPassed`, batch insert `AttemptAnswer`, update `TestAttempt` trong `@Transactional`, audit log `EXAM_SUBMITTED`, log `[INFO]`/`[ERROR]`.
- [x] **3.4** `MockExamService.getExamHistory(studentId, pageable)` — filter `attemptType=EXAM`, `status IN (submitted, auto_submitted)`, order `submittedAt DESC`.
- [x] **3.5** `MockExamService.getExamReview(attemptId, studentId)` — 400 nếu còn `in_progress`, trả `correctOption` + `explanation`.

## Phase 4: Controllers

- [x] **4.1** Không tạo `MockExamController` riêng cho `/start`/`/submit` (route đã bị `AssessmentController` của feat-assessment chiếm — tạo controller mới trùng route sẽ vỡ Spring `Ambiguous mapping`). Theo đúng gợi ý dự phòng của TASKS.md (§4.1 "hoặc extend AssessmentController với type routing"): mở rộng `AssessmentController` hiện có để dispatch theo `assessment.assessmentType` (`AssessmentService.resolveType()`). `/status` và `/review` (path mới, không trùng) thêm vào `TestAttemptController` hiện có; `GET /api/test-attempts?type=exam` thêm nhánh dispatch trong cùng endpoint.
- [x] **4.2** Tất cả endpoint mock-exam: `@PreAuthorize("hasRole('STUDENT')")` (kế thừa từ class-level annotation có sẵn), `@Valid @RequestBody` cho submit.
- [x] **4.3** Exception mới: `TimeExceededException`, `AttemptAlreadySubmittedException` (extends `BusinessException` có sẵn → tự động được `GlobalExceptionHandler.handleBusinessException` xử lý, không cần sửa handler). `BusinessRuleViolationException` ⏭️ tái dùng `BusinessRuleException` đã có (cùng semantics, nhất quán với feat-assessment). `VipRequiredException` ⏭️ SKIPPED theo quyết định phạm vi.

## Phase 5: Testing

- [x] **5.1** Unit Tests `MockExamServiceTest` (18 tests, 100% pass): happy path start, not-published→404, wrong-type→404, section score calculation, isPassed true/false, all-wrong→score 0, time-exceeded manual submit, auto-submit accepted after expiry, already-submitted, wrong-student→403, mismatched-assessment→400, score-invariant-violation (dữ liệu hỏng giả lập), status remaining-seconds + expired, history, review (có correctOption/explanation) + review-while-in-progress→400. (VIP test ⏭️ SKIPPED — không còn áp dụng.)
- [x] **5.2** Integration Tests `MockExamControllerIntegrationTest` (4 tests, 100% pass): start (không lộ `correctOption`), start→submit→status→review→history full flow, submit-lại→422, review khi còn in_progress→400.
- [x] **5.3** Security: toàn bộ endpoint kế thừa `@PreAuthorize` của class hiện có (401 nếu thiếu JWT đã được test ở `AssessmentControllerIntegrationTest`); 403 cho attempt của người khác có test riêng (`submitExam_wrongStudent_throwsForbidden`, `getExamStatus_wrongStudent_throwsForbidden`); `correctOption` không lộ ở `/start` được test bằng reflection-safeguard (`startExam_happyPath...`) + jsonPath integration test.

## Phase 6: Final Review

- [x] **6.1** Cross-check AC-10-01 đến AC-10-13: tất cả PASS trừ **AC-10-11 (VIP check) ⏭️ SKIPPED theo quyết định phạm vi** — xem header.
- [x] **6.2** Cross-check `AGENTS.md §7.1` — Luật điểm số `score >= 0 && score <= max_score`: enforced trong `submitExam()`, có unit test giả lập vi phạm.
- [x] **6.3** Cross-check `AGENTS.md §7.3` — Luật subscription (VIP check real-time): ⏭️ SKIPPED — không có hệ thống subscription nào ở backend để check (xem header quyết định phạm vi).
- [x] **6.4** Xác minh `started_at` không thể bị overwrite bởi client: `StartExam` không nhận body, server luôn set `LocalDateTime.now()`.
- [x] **6.5** Xác minh `@Transactional` bao trọn scoring + persist trong `submitExam()`: có `@Transactional` ở method, batch insert `attempt_answers` + update `test_attempts` cùng transaction.
- [x] **6.6** `mvn spotless:apply` — 0 warnings (`spotless:check` exit code 0).
- [ ] **6.7** Code Review và Merge theo quy định PR (`CONSTITUTION.md §4.3`) — chờ review từ team, chưa merge.
