# TASKS — Assessment & Quiz Practice (`feat-assessment`)

> **Feature ID:** `feat-assessment` | **UC Coverage:** UC-11 (Quiz & Practice)
> **Phiên bản:** 1.1 | **Cập nhật:** 2026-06-17
>
> **Lưu ý:** Checklist dưới đây được cập nhật retroactive sau khi code đã được implement — checkbox trước đó
> chưa được tick dù logic đã hoàn thiện. Đã verify lại từng mục bằng cách đọc trực tiếp source code
> (không tin tên file/comment) trước khi tick.

---

## Phase 1: Database & Entities

- [x] **1.1** Migration `V1__init_schema.sql` đã tạo đầy đủ bảng `questions`, `assessments`, `question_assignments`, `test_attempts`, `attempt_answers` (dùng chung với `feat-mock-test`, xem header file đó) — không cần migration riêng.
- [x] **1.2** JPA Entity `Question` (`Question.java`) — map đúng `question_type`, `skill`, `jlpt_level`; `correct_option`/`correct_answer_text` đã có `@JsonIgnore` (xác nhận tại dòng khai báo field).
- [x] **1.3** JPA Entity `Assessment` — map `assessment_type`, `status`, FK → `staff_users`.
- [x] **1.4** JPA Entity `QuestionAssignment` — composite key, `@ManyToOne` → `Question` (`QuestionAssignmentRepository`/`QuestionAssignmentSupport` dùng `JOIN FETCH`, không bị N+1).
- [x] **1.5** JPA Entity `TestAttempt` — `@ManyToOne` → `StudentUser`; `status` enum gồm `IN_PROGRESS/SUBMITTED/AUTO_SUBMITTED/ABANDONED`.
- [x] **1.6** JPA Entity `AttemptAnswer` — `@ManyToOne` → `TestAttempt` và `Question`.

## Phase 2: Repositories

- [x] **2.1** `AssessmentRepository.findAllByAssessmentTypeAndJlptLevelAndStatus(...)` với `Pageable` — confirmed trong `AssessmentService.listAssessments()`.
- [x] **2.2** `QuestionAssignmentRepository` — load theo parent type/id, dùng `JOIN FETCH` trong `loadAssignments()`.
- [x] **2.3** `TestAttemptRepository` — có `findByIdForUpdate` (pessimistic lock, dùng cho `MockExamService.submitExam`) + các query lọc theo `studentId`.
- [x] **2.4** `AttemptAnswerRepository` — dùng `saveAll()` trong `grade()`.

## Phase 3: DTOs & Validation

- [x] **3.1** `SubmitQuizRequest`/`AnswerRequest` — `@NotNull attemptId`, `@NotEmpty answers`, `selectedOption` validate `[ABCD]`.
- [x] **3.2** `AssessmentSummaryResponse`, `QuizStartResponse`, `SectionResponse`, `QuestionResponse` — KHÔNG có field `correctOption`/`correctAnswerText` (xác nhận bằng cách đọc field list của class, không chỉ tên).
- [x] **3.3** `QuizResultResponse`, `QuestionResultResponse` (có `correctOption`+`explanation` sau khi nộp), `AttemptHistoryResponse`.
- [x] **3.4** Đảm bảo cấu trúc: `QuestionResponse` không có field `correctOption`/`correctAnswerText` ở mức compile-time (không chỉ runtime test) — mạnh hơn yêu cầu gốc.

## Phase 4: Business Logic (Services)

- [x] **4.1** `AssessmentService.listAssessments()` — filter `status=PUBLISHED` + `assessmentType` + `jlptLevel`, trả `Page<AssessmentSummaryResponse>` với `questionCount` đếm từ `question_assignments`.
- [x] **4.2** `AssessmentService.startAssessment()` — validate published, tạo `TestAttempt(IN_PROGRESS, started_at=now())`, group theo section, trả `QuizStartResponse` không có `correctOption`. **VIP check ⏭️ SKIPPED** theo quyết định phạm vi (đồng bộ với `feat-mock-test` — chưa có hệ thống subscription).
- [x] **4.3** `AssessmentService.submitAssessment()`/`grade()` — ownership check (`findOwnedAttempt`, throw `ForbiddenException` nếu sai student), status check (throw `AttemptAlreadySubmittedException` nếu không phải `IN_PROGRESS`), tính điểm, **invariant `0 <= score <= maxScore`** (throw `BusinessRuleException` nếu vi phạm), `saveAll()` cho `AttemptAnswer`, update `TestAttempt`, audit log `QUIZ_SUBMITTED` (không log nội dung câu trả lời, chỉ log score).
- [x] **4.4** `AssessmentService.getAttemptHistory(studentId, ...)` — filter theo `studentId`, trả `Page<AttemptHistoryResponse>`.

## Phase 5: Controllers & Security

- [x] **5.1** `AssessmentController` — `GET /api/assessments`, `POST /api/assessments/{id}/start`, `POST /api/assessments/{id}/submit`, `@PreAuthorize("hasRole('STUDENT')")` ở class level. **2026-06-17:** thêm cap `size <= 100` (`Math.min(size, MAX_PAGE_SIZE)`) để chặn DoS qua page size lớn.
- [x] **5.2** `TestAttemptController` — `GET /api/test-attempts` (+ `/status`, `/review` mở rộng cho exam, xem `feat-mock-test`). Cùng cap `size <= 100` đã thêm 2026-06-17.
- [x] **5.3** Exception mới (`AttemptAlreadySubmittedException`, `TimeExceededException`) **kế thừa `BusinessException` có sẵn** → tự động được `GlobalExceptionHandler.handleBusinessException` xử lý qua `ex.getStatus()`, không cần sửa handler (xác nhận bằng cách đọc `GlobalExceptionHandler.java` — không có handler riêng cho 2 exception này, nhưng vẫn map đúng 422/400 nhờ kế thừa). `AssessmentNotFoundException` ⏭️ tái dùng `ResourceNotFoundException` có sẵn. `BusinessRuleViolationException` ⏭️ tái dùng `BusinessRuleException`. `VipRequiredException` ⏭️ SKIPPED theo quyết định phạm vi.

## Phase 6: Testing

- [x] **6.1** Unit Tests `AssessmentServiceTest` — bao gồm happy path (`submitAssessment_correctAnswer_fullScore`), all-wrong (`submitAssessment_wrongAnswer_zeroScore`), already-submitted, not-published→404, ownership (`submitAssessment_attemptBelongsToAnotherStudent_throwsForbidden`), invalid type/level.
- [x] **6.2** Integration Tests `AssessmentControllerIntegrationTest` + `LegacyQuizControllerIntegrationTest` — happy path list/start/submit, 422 khi nộp lại, 422 khi vi phạm score invariant.
- [x] **6.3** Security Test — 401 thiếu JWT (test tại `AssessmentControllerIntegrationTest`), 403 sai owner có test riêng.

## Phase 7: Final Review

- [x] **7.1** Cross-check `SPEC.md §8` AC-ASSESS-01→08: tất cả PASS cho `assessment_type=quiz` (AssessmentService). AC-ASSESS-05 (time exceeded) và AC-ASSESS-07 (section scores) là tiêu chí dành cho `assessment_type=exam`, được implement và verify riêng trong `feat-mock-test` (`MockExamService`) — quiz thường (practice) chủ động không có giới hạn thời gian cứng theo frontend spec ("no strict timer").
- [x] **7.2** Cross-check `AGENTS.md §7.1` — Luật điểm số `0 <= score <= max_score`: enforced ở cả `AssessmentService.grade()` và `MockExamService.submitExam()`, có unit test giả lập vi phạm.
- [x] **7.3** Xác minh `correct_option` không có trong response của `/start`: đúng (xem 3.2/3.4).
- [ ] **7.4** `mvn spotless:apply` — cần chạy lại sau khi sửa page-size cap (2026-06-17) trước khi merge.
- [ ] **7.5** Code Review và Merge theo quy định PR (`CONSTITUTION.md §4.3`) — chờ review từ team, chưa merge.
