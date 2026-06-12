# TASKS — JLPT Mock Exam (`feat-mock-test`)

> **Feature ID:** `feat-mock-test` | **UC Coverage:** UC-10 (JLPT Mock Test)
> **Phiên bản:** 1.0 | **Cập nhật:** 2026-06-10
> **Phụ thuộc:** `feat-assessment` (dùng chung DB schema và một số Entities/Repos)

---

## Phase 1: Database & Entities (phối hợp với `feat-assessment`)

- [ ] **1.1** Xác nhận migration từ `feat-assessment` đã tạo đầy đủ bảng `assessments`, `test_attempts`, `attempt_answers`, `question_assignments` — không duplicate migration
- [ ] **1.2** Thêm sample data (seed script) cho 1–2 exam mẫu mỗi level (N5, N4, N3) với `assessment_type = 'exam'`, `duration_min`, `pass_score`, `total_score`, `audio_url` (dev placeholder)
- [ ] **1.3** Thêm sample `question_assignments` với `section_name` đúng convention: `language_knowledge`, `reading`, `listening`

## Phase 2: DTOs & Validation

- [ ] **2.1** Request DTO: `SubmitExamRequest` — `attemptId`, `isAutoSubmit: boolean`, `answers: List<ExamAnswerRequest>` với `@Valid`, `@NotNull`, `@NotEmpty`
- [ ] **2.2** Request DTO: `ExamAnswerRequest` — `questionId: Long`, `selectedOption: @Pattern("[ABCD]")|\|null`, `answerText: String|\|null`
- [ ] **2.3** Response DTO: `ExamListResponse` — kèm `passScore`, `durationMin`, `isVipOnly`, `questionCount`
- [ ] **2.4** Response DTO: `ExamStartResponse` — `attemptId`, `startedAt`, `expiresAt`, `sections[]` (KHÔNG có `correctOption`), `listeningAudioUrl`
- [ ] **2.5** Response DTO: `SectionResponse` — `sectionName`, `questions: List<ExamQuestionResponse>`
- [ ] **2.6** Response DTO: `ExamQuestionResponse` — `questionId`, `questionText`, `questionType`, `skill`, `optionA–D`, `audioUrl`, `imageUrl`, `displayOrder` — **KHÔNG có `correctOption`**
- [ ] **2.7** Response DTO: `ExamStatusResponse` — `attemptId`, `status`, `remainingSeconds`, `isExpired`
- [ ] **2.8** Response DTO: `ExamSubmitResponse` — `totalScore`, `maxScore`, `isPassed`, `sectionScores{languageKnowledge, reading, listening}`, `durationSeconds`, `submittedAt`, `results: List<ExamResultItem>`
- [ ] **2.9** Response DTO: `ExamResultItem` — `questionId`, `sectionName`, `isCorrect`, `selectedOption`, `correctOption`, `score`, `explanation`
- [ ] **2.10** Response DTO: `ExamHistoryResponse` — kèm `sectionScores`, `isPassed`, `status`
- [ ] **2.11** Response DTO: `ExamReviewResponse` — giống ExamSubmitResponse nhưng thêm `questionText`, `optionA–D` cho mỗi câu

## Phase 3: Business Logic (MockExamService)

- [ ] **3.1** `MockExamService.startExam(assessmentId, studentId)`:
  - Validate `assessment_type = 'exam'` và `status = 'published'`
  - VIP check: nếu `is_vip_only = true` → verify student subscription (real-time, không cache > 5 phút)
  - Tạo `TestAttempt` (`status = 'in_progress'`, `started_at = LocalDateTime.now()` — server time)
  - Load `QuestionAssignment` ordered by `display_order`, group by `section_name`
  - Map sang `ExamQuestionResponse` — **mapper KHÔNG map `correctOption`**
  - Tính `expiresAt = started_at + duration_min`
  - Trả `ExamStartResponse`

- [ ] **3.2** `MockExamService.getExamStatus(attemptId, studentId)`:
  - Validate attempt thuộc student (throw 403 nếu không)
  - Tính `remainingSeconds = max(0, expiresAt - now)`
  - Trả `ExamStatusResponse`

- [ ] **3.3** `MockExamService.submitExam(attemptId, studentId, isAutoSubmit, answers)`:
  - Validate attempt tồn tại và thuộc student (throw 403 nếu không)
  - Validate `attempt.status = 'in_progress'` (throw `AttemptAlreadySubmittedException` nếu đã submitted)
  - **Time validation:** nếu `isAutoSubmit = false` → validate `NOW() <= expiresAt` (throw `TimeExceededException`)
  - Load `QuestionAssignment` map: `{questionId → (section_name, score, correctOption)}`
  - Tính điểm từng câu:
    - `multiple_choice`: `answer.selectedOption == question.correctOption`
    - `fill_blank`: `answer.answerText.trim().equalsIgnoreCase(question.correctAnswerText.trim())`
    - `true_false`: `answer.selectedOption == question.correctOption`
  - Tính `sectionScores` theo `section_name`
  - Tính `totalScore = languageScore + readingScore + listeningScore`
  - **Invariant check:** `totalScore >= 0 && totalScore <= maxScore` → throw `BusinessRuleViolationException` nếu vi phạm; ghi [ERROR] log
  - Tính `isPassed = (totalScore >= assessment.passScore)`
  - Batch insert `AttemptAnswer` records (`attemptAnswerRepository.saveAll(...)`)
  - `@Transactional` update `TestAttempt`:
    - `status = isAutoSubmit ? 'auto_submitted' : 'submitted'`
    - `submitted_at = NOW()`, `duration_seconds = submittedAt - startedAt`
    - `total_score`, `max_score`, `language_knowledge_score`, `reading_score`, `listening_score`, `is_passed`
  - Audit log: `auditLogService.log("EXAM_SUBMITTED", studentId, {attemptId, assessmentId, score, isPassed, durationSeconds})`
  - Log: `[INFO] [MockExamService] EXAM_SUBMITTED {studentId, assessmentId, score, isPassed}`
  - Trả `ExamSubmitResponse`

- [ ] **3.4** `MockExamService.getExamHistory(studentId, pageable)`:
  - Filter: `student_id = studentId`, `attempt_type IN ('exam', 'auto_submitted')`
  - Trả `Page<ExamHistoryResponse>` ordered by `submitted_at DESC`

- [ ] **3.5** `MockExamService.getExamReview(attemptId, studentId)`:
  - Validate attempt thuộc student, `status ≠ 'in_progress'`
  - Load `attempt_answers` JOIN `questions` JOIN `question_assignments`
  - Trả `ExamReviewResponse` với `correctOption` và `explanation`

## Phase 4: Controllers

- [ ] **4.1** `MockExamController` (hoặc extend `AssessmentController` với type routing):
  - `GET /api/assessments?type=exam&level={level}` — reuse hoặc delegate tới `MockExamService`
  - `POST /api/assessments/{id}/start` — dispatch tới `MockExamService.startExam()` khi `type=exam`
  - `GET /api/test-attempts/{attemptId}/status` — gọi `MockExamService.getExamStatus()`
  - `POST /api/assessments/{id}/submit` — dispatch tới `MockExamService.submitExam()` khi `type=exam`
  - `GET /api/test-attempts?type=exam` — gọi `MockExamService.getExamHistory()`
  - `GET /api/test-attempts/{attemptId}/review` — gọi `MockExamService.getExamReview()`

- [ ] **4.2** Tất cả endpoints: `@PreAuthorize("hasRole('STUDENT')")`, `@Valid @RequestBody`

- [ ] **4.3** Cập nhật `GlobalExceptionHandler` với exception mới nếu chưa có từ `feat-assessment`:
  - `TimeExceededException` → HTTP 400 `TIME_EXCEEDED`
  - `VipRequiredException` → HTTP 403 `VIP_REQUIRED`
  - `BusinessRuleViolationException` → HTTP 422 `SCORE_INVARIANT_VIOLATION`

## Phase 5: Testing

- [ ] **5.1** Unit Tests `MockExamService` — tối thiểu 80% coverage:
  - `testStartExam_happyPath()` — tạo attempt đúng, `started_at` là server time
  - `testStartExam_vipOnly_studentFree_throwsForbidden()`
  - `testStartExam_notPublished_throwsNotFound()`
  - `testStartExam_correctOptionNotInResponse()` — xác minh `ExamQuestionResponse` không có `correctOption`
  - `testSubmitExam_calculateSectionScores()` — tính đúng 3 section
  - `testSubmitExam_isPassed_true()` — `score >= passScore`
  - `testSubmitExam_isPassed_false()` — `score < passScore`
  - `testSubmitExam_allWrong_scoreZero()` — score không âm
  - `testSubmitExam_timeExceeded_manualSubmit_throwsException()`
  - `testSubmitExam_autoSubmit_accepted_afterTimeExpiry()`
  - `testSubmitExam_alreadySubmitted_throwsException()`
  - `testSubmitExam_wrongStudent_throwsForbidden()`
  - `testSubmitExam_scoreInvariantViolation_throwsException()` — mock edge case

- [ ] **5.2** Integration Tests (Spring Boot Test + Testcontainers/H2):
  - `POST /api/assessments/{id}/start` — happy path, VIP check, not found
  - `GET /api/test-attempts/{id}/status` — in_progress, expired
  - `POST /api/assessments/{id}/submit` — manual submit, auto-submit, time exceeded, already submitted
  - `GET /api/test-attempts?type=exam` — filter đúng
  - `GET /api/test-attempts/{id}/review` — trả đúng kết quả

- [ ] **5.3** Security Tests:
  - Endpoint không có JWT → HTTP 401
  - Xem/nộp attempt của người khác → HTTP 403
  - `correctOption` không trong start response

## Phase 6: Final Review

- [ ] **6.1** Cross-check tất cả AC-MOCK-01 đến AC-MOCK-13 (`SPEC.md §8`)
- [ ] **6.2** Cross-check `AGENTS.md §7.1` — Luật điểm số: `score >= 0`, `score <= max_score`
- [ ] **6.3** Cross-check `AGENTS.md §7.3` — Luật subscription: VIP check real-time
- [ ] **6.4** Xác minh `started_at` không thể bị overwrite bởi client
- [ ] **6.5** Xác minh `@Transactional` bao trọn scoring + persist trong `submitExam()`
- [ ] **6.6** `mvn spotless:apply` — 0 warnings
- [ ] **6.7** Code Review và Merge theo quy định PR (`CONSTITUTION.md §4.3`)
