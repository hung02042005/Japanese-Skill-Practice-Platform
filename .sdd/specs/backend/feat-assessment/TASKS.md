# TASKS — Assessment & Quiz Practice (`feat-assessment`)

> **Feature ID:** `feat-assessment` | **UC Coverage:** UC-11 (Quiz & Practice)
> **Phiên bản:** 1.0 | **Cập nhật:** 2026-06-10

---

## Phase 1: Database & Entities

- [ ] **1.1** Viết Flyway migration tạo bảng `questions`, `assessments`, `question_assignments`, `test_attempts`, `attempt_answers` (theo schema tại `SPEC.md §5`)
- [ ] **1.2** Tạo JPA Entity `Question` — map đúng `question_type`, `skill`, `jlpt_level` CHECK constraints; `correct_option` và `correct_answer_text` là `@JsonIgnore` để không bao giờ lộ ra API
- [ ] **1.3** Tạo JPA Entity `Assessment` — map `assessment_type`, `status`, FK → `staff_users`
- [ ] **1.4** Tạo JPA Entity `QuestionAssignment` — composite key `(parent_type, parent_id, question_id)`; `@ManyToOne` → `Question`
- [ ] **1.5** Tạo JPA Entity `TestAttempt` — `@ManyToOne` → `StudentUser`; map `status` enum
- [ ] **1.6** Tạo JPA Entity `AttemptAnswer` — `@ManyToOne` → `TestAttempt` và `Question`

## Phase 2: Repositories

- [ ] **2.1** `AssessmentRepository`: `findAllByAssessmentTypeAndJlptLevelAndStatus(...)` với `Pageable`
- [ ] **2.2** `QuestionAssignmentRepository`: `findByParentTypeAndParentIdOrderByDisplayOrder(...)`
- [ ] **2.3** `TestAttemptRepository`: `findByAttemptIdAndStudentId(...)`, `findByStudentId(...)` với `Pageable`
- [ ] **2.4** `AttemptAnswerRepository`: không có custom method, dùng `saveAll()`

## Phase 3: DTOs & Validation

- [ ] **3.1** Request DTOs: `SubmitQuizRequest` (`@NotNull attemptId`, `@NotEmpty answers`), `AnswerRequest` (`questionId`, `selectedOption` validated A–D, `answerText`)
- [ ] **3.2** Response DTOs: `AssessmentSummaryResponse`, `QuizStartResponse`, `SectionResponse`, `QuestionResponse` (KHÔNG có `correctOption` field)
- [ ] **3.3** Response DTOs: `QuizResultResponse`, `QuestionResultResponse` (có `correctOption` sau khi nộp), `AttemptHistoryResponse`
- [ ] **3.4** Xác minh: `QuestionResponse` mapper **không** map field `correct_option`/`correct_answer_text` — viết unit test xác nhận

## Phase 4: Business Logic (Services)

- [ ] **4.1** `AssessmentService.listAssessments()`:
  - Filter `status = 'published'` và đúng `assessment_type`, `jlpt_level`
  - Trả `Page<AssessmentSummaryResponse>`
  - Đếm số câu từ `question_assignments`

- [ ] **4.2** `AssessmentService.startAssessment(assessmentId, studentId)`:
  - Validate assessment tồn tại và `status = 'published'`
  - Check VIP nếu `assessments.is_vip_only = true`
  - Tạo `TestAttempt` (`status = 'in_progress'`, `started_at = NOW()`)
  - Load questions qua `QuestionAssignmentRepository`, group theo `section_name`
  - Trả `QuizStartResponse` — questions không có `correctOption`

- [ ] **4.3** `AssessmentService.submitAssessment(attemptId, studentId, answers)`:
  - Validate `attempt.student_id = studentId` (chặn submit của người khác)
  - Validate `attempt.status = 'in_progress'` → throw `AttemptAlreadySubmittedException` nếu đã submitted
  - Tính `duration_seconds = NOW() - started_at`
  - Tính điểm từng câu: `multiple_choice` → so sánh `selected_option` vs `correct_option`; `fill_blank` → so sánh case-insensitive `answer_text` vs `correct_answer_text`
  - **Invariant check:** `score >= 0` && `score <= maxScore` → throw `BusinessRuleViolationException` nếu vi phạm
  - Batch insert `AttemptAnswer` records (dùng `saveAll()`)
  - Update `TestAttempt`: `status = 'submitted'`, `submitted_at`, `total_score`, `max_score`, `duration_seconds`
  - Gọi `AuditLogService.log("QUIZ_SUBMITTED", studentId, {attemptId, assessmentId, score})`
  - Trả `QuizResultResponse` với `explanation` từng câu

- [ ] **4.4** `AssessmentService.getAttemptHistory(studentId, pageable)`:
  - Filter theo `student_id`
  - Trả `Page<AttemptHistoryResponse>`

## Phase 5: Controllers & Security

- [ ] **5.1** `AssessmentController`:
  - `GET /api/assessments` — query params: `type`, `level`, `page`, `size`
  - `POST /api/assessments/{assessmentId}/start` — `@PreAuthorize("hasRole('STUDENT')")`
  - `POST /api/assessments/{assessmentId}/submit` — `@PreAuthorize("hasRole('STUDENT')")`, `@Valid @RequestBody`

- [ ] **5.2** `TestAttemptController`:
  - `GET /api/test-attempts` — query params: `assessmentId`, `page`, `size`
  - `@PreAuthorize("hasRole('STUDENT')")`

- [ ] **5.3** Cập nhật `GlobalExceptionHandler` với các exception mới: `AssessmentNotFoundException`, `AttemptAlreadySubmittedException`, `TimeExceededException`, `BusinessRuleViolationException`, `VipRequiredException`

## Phase 6: Testing

- [ ] **6.1** Unit Tests `AssessmentService`:
  - `testScoreCalculation_allCorrect()` — score = maxScore
  - `testScoreCalculation_allWrong()` — score = 0 (không âm)
  - `testSubmit_alreadySubmitted_throwsException()`
  - `testStartAssessment_notPublished_throwsNotFound()`
  - `testCorrectOptionNotInStartResponse()` — xác minh `QuestionResponse` không có `correctOption`

- [ ] **6.2** Integration Tests (Spring Boot Test + H2/Testcontainers):
  - `GET /api/assessments` — happy path, filter theo type/level
  - `POST /api/assessments/{id}/start` — happy path; 404 nếu không tìm thấy
  - `POST /api/assessments/{id}/submit` — happy path; 422 nếu nộp lại; 422 nếu score invariant vi phạm
  - `GET /api/test-attempts` — trả đúng lịch sử của student hiện tại

- [ ] **6.3** Security Test: gọi endpoint không có JWT → HTTP 401

## Phase 7: Final Review

- [ ] **7.1** Cross-check với `SPEC.md §8 Acceptance Criteria` — tất cả AC-ASSESS-01 đến AC-ASSESS-08 pass
- [ ] **7.2** Cross-check với `AGENTS.md §7.1` — Luật điểm số (score >= 0, score <= max_score)
- [ ] **7.3** Xác minh `correct_option` không có trong bất kỳ response nào của `/start` endpoint
- [ ] **7.4** `mvn spotless:apply` — 0 warnings
- [ ] **7.5** Code Review và Merge theo quy định PR (`CONSTITUTION.md §4.3`)
