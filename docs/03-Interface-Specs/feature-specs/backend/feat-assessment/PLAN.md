# PLAN — Assessment & Quiz Practice (`feat-assessment`)

> **Feature ID:** `feat-assessment` | **UC Coverage:** UC-11 (Quiz & Practice)
> **Phiên bản:** 1.0 | **Cập nhật:** 2026-06-10

---

## 1. Mục tiêu

Triển khai module **Quiz & Practice** cho phép học viên làm bài kiểm tra ngắn (10–20 câu) theo bài học hoặc kỹ năng, nhận kết quả và giải thích tức thì. Điểm số phải được tính **100% server-side**, bản ghi bài làm là **bất biến** sau khi nộp.

> **Lưu ý phạm vi:** UC-10 (JLPT Mock Exam) được triển khai riêng tại `feat-mock-test`. Hai feature dùng chung data model (bảng `assessments`, `test_attempts`).

---

## 2. Kiến trúc & Công nghệ

| Thành phần | Công nghệ |
|-----------|-----------|
| Backend | Java 21 + Spring Boot 3.x + Spring Data JPA |
| Database | SQL Server, Migration bằng Flyway |
| Auth | Bearer JWT (từ `feat-auth`) |
| Tuân thủ | Controller → Service → Repository → Entity + DTO Pattern bắt buộc |

---

## 3. Thành phần Backend

### 3.1. Database Migration & Entities

- Migration tạo bảng: `questions`, `assessments`, `question_assignments`, `test_attempts`, `attempt_answers`
- JPA Entities: `Question`, `Assessment`, `QuestionAssignment`, `TestAttempt`, `AttemptAnswer`
- Soft delete: `assessments` và `questions` dùng `status = 'archived'/'deleted'` (không dùng `is_deleted`)
- Audit columns bắt buộc: `created_at`, `updated_at`, `created_by`

### 3.2. Repositories (Spring Data JPA)

| Repository | Các phương thức chính |
|-----------|----------------------|
| `AssessmentRepository` | `findPublishedByTypeAndLevel(type, level, pageable)` |
| `QuestionAssignmentRepository` | `findByParentTypeAndParentIdOrderByDisplayOrder(type, id)` |
| `TestAttemptRepository` | `findByStudentIdAndParentId(...)`, `findByAttemptIdAndStudentId(...)` |
| `AttemptAnswerRepository` | `saveAll(List<AttemptAnswer>)` — batch insert |

### 3.3. DTOs

**Request DTOs** (với `@Valid` + Jakarta annotations):

| DTO | Fields |
|-----|--------|
| `SubmitQuizRequest` | `attemptId: Long`, `answers: List<AnswerRequest>` |
| `AnswerRequest` | `questionId: Long`, `selectedOption: String\|null`, `answerText: String\|null` |

**Response DTOs** (không bao giờ chứa `correct_option` trước khi nộp bài):

| DTO | Fields |
|-----|--------|
| `AssessmentSummaryResponse` | `assessmentId`, `title`, `assessmentType`, `jlptLevel`, `durationMin`, `totalScore`, `questionCount` |
| `QuizStartResponse` | `attemptId`, `startedAt`, `durationMin`, `sections[]` → `QuestionResponse[]` |
| `QuestionResponse` | `questionId`, `questionText`, `questionType`, `optionA–D`, `displayOrder` — **KHÔNG có** `correctOption` |
| `QuizResultResponse` | `attemptId`, `score`, `maxScore`, `isPassed`, `results[]` → `QuestionResultResponse` |
| `QuestionResultResponse` | `questionId`, `isCorrect`, `selectedOption`, `correctOption`, `explanation` |
| `AttemptHistoryResponse` | `attemptId`, `assessmentTitle`, `score`, `maxScore`, `submittedAt`, `durationSeconds` |

### 3.4. Services

**`AssessmentService`** — chứa toàn bộ business logic:

```
listAssessments(type, level, pageable)
  → filter: status='published', assessment_type, jlpt_level
  → trả Page<AssessmentSummaryResponse>

startAssessment(assessmentId, studentId)
  → validate: assessment tồn tại, status='published'
  → tạo TestAttempt (status='in_progress', started_at=NOW())
  → load questions qua question_assignments (ordered by display_order)
  → trả QuizStartResponse — KHÔNG kèm correct_option

submitAssessment(attemptId, studentId, answers)
  → validate: attempt thuộc studentId
  → validate: status = 'in_progress' (chặn nộp lại)
  → [exam only] validate: NOW() <= started_at + duration_min
  → tính điểm từng câu: so sánh selected_option/answer_text với correct_option/correct_answer_text
  → validate: 0 <= score <= maxScore (throw BusinessRuleViolationException nếu vi phạm)
  → batch insert AttemptAnswer records
  → update TestAttempt: status='submitted', submitted_at, total_score, max_score
  → audit log: QUIZ_SUBMITTED {studentId, assessmentId, attemptId, score}
  → trả QuizResultResponse với explanation từng câu

getAttemptHistory(studentId, pageable)
  → filter: student_id = studentId
  → trả Page<AttemptHistoryResponse>
```

### 3.5. Controllers & Security

| Controller | Endpoints |
|-----------|-----------|
| `AssessmentController` | `GET /api/assessments`, `POST /api/assessments/{id}/start`, `POST /api/assessments/{id}/submit` |
| `TestAttemptController` | `GET /api/test-attempts` |

- `@PreAuthorize("hasRole('STUDENT')")` trên tất cả endpoints
- `@Valid` trên tất cả `@RequestBody`

### 3.6. Exception Handling (GlobalExceptionHandler)

| Exception | HTTP Code | Error Code |
|-----------|-----------|------------|
| `AssessmentNotFoundException` | 404 | `ASSESSMENT_NOT_FOUND` |
| `AttemptNotFoundException` | 404 | `ATTEMPT_NOT_FOUND` |
| `AttemptAlreadySubmittedException` | 422 | `ATTEMPT_ALREADY_SUBMITTED` |
| `TimeExceededException` | 400 | `TIME_EXCEEDED` |
| `BusinessRuleViolationException` | 422 | `SCORE_INVARIANT_VIOLATION` |
| `VipRequiredException` | 403 | `VIP_REQUIRED` |

---

## 4. Security & Invariants

| Rule | Cách đảm bảo |
|------|-------------|
| Score tính server-side | Client không gửi score; Service tính lại từ DB |
| `correct_option` không lộ | `QuestionResponse` DTO không có field `correctOption`; mapper không map field này |
| Attempt bất biến | `status = 'submitted'` → reject mọi update/submit lại |
| Score `>= 0` và `<= maxScore` | Validate trong `AssessmentService.submitAssessment()` trước khi persist |

---

## 5. Definition of Done

- [ ] Unit Tests `>= 80%` coverage cho `AssessmentService` (score calc, time validation, invariant check)
- [ ] Integration Tests cho tất cả endpoints (happy + error paths)
- [ ] `correct_option` không xuất hiện trong bất kỳ response nào trước khi nộp bài
- [ ] Audit log ghi đúng mỗi lần nộp bài
- [ ] Không có TODO comments, pass `mvn spotless:apply`
