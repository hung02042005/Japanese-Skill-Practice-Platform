# PLAN — JLPT Mock Exam (`feat-mock-test`)

> **Feature ID:** `feat-mock-test` | **UC Coverage:** UC-10 (JLPT Mock Test)
> **Phiên bản:** 1.0 | **Cập nhật:** 2026-06-10

---

## 1. Mục tiêu

Triển khai module **JLPT Mock Exam** cho phép học viên thi thử đề thi đầy đủ (N5–N1) với 3 phần thi (Ngôn ngữ, Đọc hiểu, Nghe hiểu), timer server-side, chấm điểm từng phần, và xác định đạt/không đạt theo chuẩn JLPT.

> **Lưu ý phạm vi:** Feature này dùng chung data model với `feat-assessment` (bảng `assessments`, `test_attempts`, `attempt_answers`). Cần điều phối để tránh migration conflict. Business logic mock exam tách biệt hoàn toàn vào `MockExamService`.

---

## 2. Kiến trúc & Công nghệ

| Thành phần | Công nghệ |
|-----------|-----------|
| Backend | Java 21 + Spring Boot 3.x + Spring Data JPA |
| Database | SQL Server, Migration bằng Flyway (phối hợp với `feat-assessment`) |
| Auth | Bearer JWT (từ `feat-auth`), check VIP subscription |
| Concurrency | `@Transactional` cho toàn bộ quá trình scoring + persist |
| Timer | Server-side: `started_at` stored at start; `submitted_at - started_at` tính `duration_seconds` |

---

## 3. Thành phần Backend

### 3.1. Entities & Repositories (dùng chung với `feat-assessment`)

Dùng lại entities và repositories từ `feat-assessment`:

- `Assessment`, `Question`, `QuestionAssignment`, `TestAttempt`, `AttemptAnswer`
- Cần bổ sung query:
  - `AssessmentRepository.findExamByIdAndStatus(id, 'published')` — assert `assessment_type = 'exam'`
  - `TestAttemptRepository.findByAttemptIdAndStudentIdForUpdate(...)` — pessimistic lock khi submit

### 3.2. DTOs

**Request DTOs:**

| DTO | Fields | Validation |
|-----|--------|------------|
| `StartExamRequest` | *(Không cần body — assessmentId từ path)* | — |
| `SubmitExamRequest` | `attemptId: Long`, `isAutoSubmit: boolean`, `answers: List<ExamAnswerRequest>` | `@NotNull`, `@NotEmpty` |
| `ExamAnswerRequest` | `questionId: Long`, `selectedOption: String\|null`, `answerText: String\|null` | selectedOption in [A,B,C,D,null] |

**Response DTOs:**

| DTO | Ghi chú |
|-----|---------|
| `ExamListResponse` | Kèm `passScore`, `isVipOnly` — phân biệt với quiz list |
| `ExamStartResponse` | `attemptId`, `startedAt`, `expiresAt`, `sections[]`, `listeningAudioUrl` — KHÔNG có `correctOption` |
| `ExamStatusResponse` | `remainingSeconds`, `isExpired` — dùng cho polling |
| `ExamSubmitResponse` | `totalScore`, `maxScore`, `isPassed`, `sectionScores{language, reading, listening}`, `results[]` |
| `ExamReviewResponse` | Giống submit response nhưng kèm đầy đủ `questionText`, `optionA–D` |
| `ExamHistoryResponse` | Kèm `sectionScores`, `isPassed`, `status (submitted/auto_submitted)` |

### 3.3. Service: `MockExamService`

**Tách biệt khỏi `AssessmentService`** để giữ SRP. Các method chính:

```
startExam(assessmentId, studentId)
  → validate: assessment exists, type='exam', status='published'
  → validate: VIP nếu is_vip_only=true
  → tạo TestAttempt (status='in_progress', started_at=NOW() — server time)
  → load questions grouped by section_name ordered by display_order
  → compute expiresAt = started_at + duration_min
  → trả ExamStartResponse — KHÔNG có correctOption

getExamStatus(attemptId, studentId)
  → validate: attempt thuộc student này
  → tính remainingSeconds = expiresAt - NOW()
  → trả ExamStatusResponse

submitExam(attemptId, studentId, isAutoSubmit, answers)
  → validate: attempt thuộc student này (throw 403 nếu không)
  → validate: attempt.status = 'in_progress' (throw 422 nếu đã submitted)
  → validate time: nếu manual submit → NOW() <= expiresAt (throw 400 nếu quá giờ)
  → tính điểm từng section:
      languageScore = Σ score cho câu đúng trong section='language_knowledge'
      readingScore  = Σ score cho câu đúng trong section='reading'
      listeningScore = Σ score cho câu đúng trong section='listening'
      totalScore = languageScore + readingScore + listeningScore
  → validate: totalScore >= 0 && totalScore <= maxScore (throw 422 nếu vi phạm)
  → set is_passed = (totalScore >= assessment.passScore)
  → batch insert AttemptAnswer records
  → @Transactional: update TestAttempt (status='submitted'/'auto_submitted', all scores, submitted_at, duration_seconds)
  → audit log: EXAM_SUBMITTED {studentId, assessmentId, attemptId, score, isPassed, durationSeconds}
  → trả ExamSubmitResponse

getExamHistory(studentId, pageable)
  → filter: parent_type='assessment', attempt_type IN ('exam','auto_submitted')
  → trả Page<ExamHistoryResponse>

getExamReview(attemptId, studentId)
  → validate: attempt thuộc student, status ≠ 'in_progress'
  → load attempt_answers JOIN questions
  → trả ExamReviewResponse (có correctOption và explanation)
```

### 3.4. Controllers

| Controller | Endpoints |
|-----------|-----------|
| `MockExamController` | `POST /api/assessments/{id}/start` *(dùng chung với AssessmentController hoặc tag riêng)* |
| | `GET /api/test-attempts/{attemptId}/status` |
| | `POST /api/assessments/{id}/submit` |
| | `GET /api/test-attempts` (filter type=exam) |
| | `GET /api/test-attempts/{attemptId}/review` |

> **Lưu ý:** `/api/assessments/{id}/start` và `/api/assessments/{id}/submit` dùng chung endpoint với `feat-assessment`. `AssessmentService` và `MockExamService` phân nhánh theo `assessment_type` trong controller hoặc service layer.

### 3.5. Security & Subscription Check

```java
// Trong MockExamService.startExam():
if (assessment.isVipOnly()) {
    StudentUser student = studentRepository.findByIdOrThrow(studentId);
    if (!student.hasActiveVipSubscription()) {
        throw new VipRequiredException("Đề thi này yêu cầu tài khoản VIP");
    }
}
```

VIP check phải **real-time** — không cache quá 5 phút (theo `AGENTS.md §7.3`).

---

## 4. Timer Logic Chi Tiết

```
start():    started_at  = server NOW()
            expiresAt   = started_at + duration_min (trả về client để hiển thị)

status():   remainingSeconds = max(0, expiresAt - NOW())
            isExpired = remainingSeconds == 0

submit():   submittedAt = server NOW()
            IF isAutoSubmit=false AND submittedAt > expiresAt → throw TIME_EXCEEDED
            IF isAutoSubmit=true  → accept (client timer đã xử lý UI)
            duration_seconds = submittedAt - started_at (tính bằng giây)
```

---

## 5. Section Scoring Logic

```java
// Tính điểm theo section
Map<String, BigDecimal> sectionScores = new HashMap<>();
for (AttemptAnswer answer : answers) {
    String section = questionAssignmentMap.get(answer.getQuestionId()).getSectionName();
    BigDecimal questionScore = questionAssignmentMap.get(answer.getQuestionId()).getScore();
    if (answer.isCorrect()) {
        sectionScores.merge(section, questionScore, BigDecimal::add);
    }
}

BigDecimal languageScore  = sectionScores.getOrDefault("language_knowledge", BigDecimal.ZERO);
BigDecimal readingScore   = sectionScores.getOrDefault("reading", BigDecimal.ZERO);
BigDecimal listeningScore = sectionScores.getOrDefault("listening", BigDecimal.ZERO);
BigDecimal totalScore = languageScore.add(readingScore).add(listeningScore);

// Invariant check (AGENTS.md §7.1)
if (totalScore.compareTo(BigDecimal.ZERO) < 0 || totalScore.compareTo(maxScore) > 0) {
    log.error("[MockExamService] Score invariant violated: {} / {}", totalScore, maxScore);
    throw new BusinessRuleViolationException("Score invariant violated");
}

boolean isPassed = totalScore.compareTo(new BigDecimal(assessment.getPassScore())) >= 0;
```

---

## 6. Definition of Done

- [ ] Unit Tests `>= 80%` coverage cho `MockExamService` (scoring logic, timer validation, VIP check)
- [ ] Integration Tests cho tất cả endpoints (happy + error paths)
- [ ] `correctOption` không xuất hiện trong `/start` response
- [ ] `started_at` luôn do server set, không thể override bởi client
- [ ] Section scores cộng đúng = total score
- [ ] `@Transactional` bao trọn scoring + persist
- [ ] Audit log ghi đúng sau mỗi lần nộp bài
- [ ] Không có TODO comments, pass `mvn spotless:apply`
