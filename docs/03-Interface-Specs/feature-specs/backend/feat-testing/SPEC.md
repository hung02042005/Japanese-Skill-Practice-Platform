# SPEC — Test Strategy & Specification (Toàn Dự Án)
>
> **Feature ID:** `feat-testing`
> **UC Coverage:** Tất cả UC (UC-01 → UC-40) — phủ 14 feature modules
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-05-30

---

## 1. CONTEXT & GOAL

### 1.1 Bối cảnh

Nền tảng JLPT E-Learning hiện tại có **0% test coverage** (chỉ tồn tại file `apps/frontend/src/test/setup.js` không có test case nào). Với 14 feature modules đã được đặc tả, hệ thống vận hành hoàn toàn dựa trên kiểm tra thủ công. Điều này tạo ra rủi ro nghiêm trọng:

- **Business rule regression:** Thay đổi nhỏ ở `AuthService` có thể phá vỡ lock-out logic mà không ai biết
- **Security regression:** `correct_option` bị vô tình trả về client sau refactor
- **SM-2 algorithm drift:** `ease_factor` tính sai sau khi sửa công thức không được phát hiện
- **AI async contract break:** Submission endpoint trả về 200 thay vì 202 sau merge conflict

Spec này thiết lập **chiến lược test toàn diện** cho backend (Spring Boot 3.x / Java 21) và frontend (React 18), bao phủ mọi business rule quan trọng đã định nghĩa trong các feature specs.

### 1.2 Mục tiêu

- **TDD-ready:** Mỗi FR quan trọng có ≥1 test case tương ứng viết trước khi implement
- **Coverage gate:** Service layer ≥ 80% line coverage; Controller layer ≥ 70%
- **Security invariants locked:** 5 invariant bảo mật cốt lõi được test độc lập, không thể bypass
- **CI gate:** Pull request không được merge nếu test fails hoặc coverage giảm xuống dưới ngưỡng
- **Fast feedback:** Toàn bộ unit + integration suite chạy xong trong < 5 phút trên CI

### 1.3 Tại sao cần?

Không có test → không có refactoring an toàn → technical debt tích lũy → hệ thống phải rewrire toàn bộ sau 12 tháng. Test là điều kiện tiên quyết để scale team và ship feature tự tin.

---

## 2. ACTOR

| Actor | Role | Điều kiện tiền quyết |
|:---|:---|:---|
| **Backend Developer** | Viết unit test (JUnit 5 + Mockito) và integration test (Spring Test + Testcontainers) | Có quyền đọc/ghi source, Docker running |
| **Frontend Developer** | Viết component test (Jest + RTL) và E2E test (Playwright) | Node.js 18+, dev server accessible |
| **CI/CD Pipeline** | Chạy toàn bộ test suite, enforce coverage gate, block merge khi fail | GitHub Actions runner, Docker daemon |
| **QA Engineer** | Review acceptance criteria, validate test scenarios match business rules | Đọc spec tất cả features |
| **StaffManager / Admin** | Không viết test — nhưng là owner của acceptance criteria | Phê duyệt AC trước khi sprint kết thúc |

---

## 3. FUNCTIONAL REQUIREMENTS (EARS)

> **EARS Syntax:**
>
> - `WHEN [trigger] THE TEST SHALL [verify]`
> - `IF [condition] THEN THE TEST SHALL [assert]`
> - `THE TEST SUITE SHALL [invariant requirement]`
> - `WHILE [state] THE TEST SHALL [guard]`

---

### 3.1 Unit Tests — Service Layer (Backend)

> **Stack:** JUnit 5, Mockito 5, AssertJ | **Naming:** `{ServiceName}Test.java`

#### 3.1.1 feat-auth: AuthService

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-U-01 | WHEN `loginWithPassword()` is called with a correct credential, THE TEST SHALL assert that a JWT access token and a refresh token are returned, and that `login_attempts` is reset to 0. |
| FR-TEST-U-02 | WHEN `loginWithPassword()` is called and `login_attempts` reaches 5, THE TEST SHALL assert that `locked_until` is set to `now + 15 minutes` and HTTP 429 equivalent exception is thrown. |
| FR-TEST-U-03 | WHILE `student_users.status = 'suspended'`, THE TEST SHALL assert that `loginWithPassword()` throws `AccountSuspendedException` with the suspension reason. |
| FR-TEST-U-04 | THE TEST SHALL verify that `registerStudent()` stores `password_hash` using bcrypt, and that the stored value is NEVER equal to the plaintext password. |
| FR-TEST-U-05 | WHEN `resetPassword()` is called with an expired token (`expires_at < NOW`), THE TEST SHALL assert that `InvalidTokenException` is thrown and the password is NOT changed. |
| FR-TEST-U-06 | WHEN `changePassword()` is called with `newPassword == currentPassword`, THE TEST SHALL assert that `SamePasswordException` (HTTP 422) is thrown. |
| FR-TEST-U-07 | WHEN `logout()` is called, THE TEST SHALL assert that only the current session token is revoked (not all tokens) and `revoked_at` is set. |
| FR-TEST-U-08 | THE TEST SHALL verify that the email verification OTP token is deleted (not merely revoked) immediately after `verifyEmail()` succeeds (one-time use enforcement). |

#### 3.1.2 feat-assessment: QuizService & ExamService

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-U-10 | THE TEST SHALL assert that `getQuizQuestions()` response DTO does NOT contain `correctOption` or `correctAnswerText` fields for any question object. |
| FR-TEST-U-11 | WHEN `submitQuiz()` is called, THE TEST SHALL assert that the score is calculated by comparing submitted answers against `questions.correct_option` in the database — not from any client-supplied value. |
| FR-TEST-U-12 | THE TEST SHALL assert that `submitQuiz()` always creates a NEW `test_attempts` record and NEVER updates an existing one (immutability invariant). |
| FR-TEST-U-13 | WHEN `submitExam()` is called after `started_at + duration_min`, THE TEST SHALL assert that only answers within the allowed window are accepted, and `TIME_EXCEEDED` is thrown for late submissions. |
| FR-TEST-U-14 | THE TEST SHALL assert that `total_score` is always in range `[0, max_score]`; if out of range, `BusinessRuleViolationException` is thrown. |
| FR-TEST-U-15 | THE TEST SHALL assert that section scores (language, reading, listening) are populated separately when `assessment_type = 'exam'`. |

#### 3.1.3 feat-flashcard-srs: FlashcardSrsService (SM-2 Algorithm)

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-U-20 | WHEN `applyRating('wrong')` is called, THE TEST SHALL assert that `interval_days = 1`, `repetition_count = 0`, and `next_review_date = TODAY + 1`. |
| FR-TEST-U-21 | WHEN `applyRating('easy')` is called with `repetition_count = 2` and `interval_days = 6`, THE TEST SHALL assert that `interval_days = round(6 * ease_factor)`. |
| FR-TEST-U-22 | THE TEST SHALL assert that `ease_factor` NEVER drops below 1.3 regardless of how many consecutive `'wrong'` ratings are applied (minimum floor invariant). |
| FR-TEST-U-23 | THE TEST SHALL assert that `ease_factor` NEVER exceeds 2.5 after consecutive `'easy'` ratings. |
| FR-TEST-U-24 | THE SM-2 test suite SHALL contain at least 10 independent test cases covering: first review (count=0), second review (count=1), third+ review (count≥2), consecutive wrong, consecutive easy, mixed sequence. |

#### 3.1.4 feat-ai-skills: AiSubmissionService

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-U-30 | WHEN `submitSpeaking()` is called, THE TEST SHALL assert that the method returns `{ submissionId, status: PENDING }` immediately WITHOUT awaiting the AI call. |
| FR-TEST-U-31 | WHEN the AI engine returns an error, THE TEST SHALL assert that the service retries exactly 3 times with exponential backoff (1s, 2s, 4s) before setting `status = 'ai_graded'` with fallback. |
| FR-TEST-U-32 | THE TEST SHALL assert that after 3 failed AI retries, the student-facing response contains a user-friendly message and does NOT contain the raw exception message or stack trace. |
| FR-TEST-U-33 | WHEN `processOcr()` returns `similarity_percent >= 70`, THE TEST SHALL assert that `is_correct = true`. WHEN `similarity_percent < 70`, THE TEST SHALL assert `is_correct = false`. |
| FR-TEST-U-34 | THE TEST SHALL assert that `ai_overall_score` is clamped to `[0, 100]` — values outside this range trigger a warning log and are corrected. |

#### 3.1.5 feat-student-management: StudentManagementService

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-U-40 | WHEN `suspendStudent()` is called, THE TEST SHALL assert that `status = 'suspended'`, all `auth_tokens` for the student are revoked (`revoked_at` set), and an `admin_audit_logs` entry is created — all within the same transaction. |
| FR-TEST-U-41 | THE TEST SHALL assert that `suspendStudent()` with a `suspend_reason` shorter than 10 characters throws `ValidationException`. |
| FR-TEST-U-42 | THE TEST SHALL assert that Staff cannot access or modify `password_hash` of any student via any service method. |

#### 3.1.6 feat-content-management: ContentManagementService

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-U-50 | WHEN `updateQuestion()` is called for a question that has existing `attempt_answers`, THE TEST SHALL assert that `QuestionLockedException` (HTTP 409) is thrown. |
| FR-TEST-U-51 | THE TEST SHALL assert that a new content item is always created with `status = 'draft'` regardless of what `status` value the Staff submits in the request. |
| FR-TEST-U-52 | WHEN `assignQuestionsToAssessment()` is called and the sum of question scores ≠ `assessments.total_score`, THE TEST SHALL assert that `ScoreMismatchException` (HTTP 400) is thrown. |

#### 3.1.7 feat-core-learning: LearningProgressService

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-U-60 | THE TEST SHALL assert that `upsertProgress()` performs an INSERT on first call and an UPDATE on second call for the same `(student_id, content_type, content_id)` — no duplicate rows. |
| FR-TEST-U-61 | THE TEST SHALL assert that `progress_percent` is NEVER decreased by an `upsertProgress()` call (monotonic increase invariant). |
| FR-TEST-U-62 | WHEN a VIP-only content (`is_vip_only = 1`) is requested by a FREE tier student, THE TEST SHALL assert that `VipRequiredException` (HTTP 403) is thrown. |

#### 3.1.8 feat-system-admin: AdminAuthService

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-U-70 | WHEN `loginAdmin()` is called with valid credentials, THE TEST SHALL assert that a full JWT access token and refresh token are returned. |
| FR-TEST-U-71 | WHEN `loginAdmin()` is called with invalid password, THE TEST SHALL assert that INVALID_CREDENTIALS error is returned and login_attempts is incremented. |
| FR-TEST-U-72 | THE TEST SHALL assert that Admin cannot modify their own account via `updateAdminUser()` when the target ID matches the caller's ID. |

---

### 3.2 Integration Tests — Repository & DB Layer

> **Stack:** Spring Boot Test, Testcontainers (SQL Server 2019), Flyway migrations | **Naming:** `{RepositoryName}IT.java`

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-I-01 | THE TEST SHALL use a real SQL Server container (via Testcontainers) loaded with Flyway migrations to verify schema constraints. |
| FR-TEST-I-02 | WHEN a `test_attempts` record is inserted and then an UPDATE attempt is made, THE TEST SHALL assert that the application-level immutability guard throws an exception — no UPDATE reaches the DB. |
| FR-TEST-I-03 | THE TEST SHALL verify that `CONSTRAINT CK_auth_token_actor` in `auth_tokens` prevents a record from having both `admin_id` and `student_id` set simultaneously. |
| FR-TEST-I-04 | THE TEST SHALL verify that `CONSTRAINT UQ_progress UNIQUE (student_id, content_type, content_id)` in `student_content_progress` is enforced at the DB level. |
| FR-TEST-I-05 | THE TEST SHALL verify that `CONSTRAINT UQ_assign UNIQUE (parent_type, parent_id, question_id)` in `question_assignments` blocks duplicate assignments. |
| FR-TEST-I-06 | WHEN a `student_users` record is hard-deleted (which should never happen), THE TEST SHALL assert the `ON DELETE CASCADE` on `auth_tokens` and `student_submissions` is exercised correctly (integration guard for soft-delete path). |
| FR-TEST-I-07 | THE TEST SHALL verify that the `flashcards` repository correctly orders results by `next_review_date ASC` when `dueOnly = true`. |
| FR-TEST-I-08 | THE TEST SHALL verify that `ease_factor` persisted via `FlashcardRepository.save()` respects the DB column precision `DECIMAL(5,2)` and does not suffer rounding drift. |

---

### 3.3 API / Controller Tests — REST Contract

> **Stack:** Spring Boot Test with `MockMvc`, `@WebMvcTest` | **Naming:** `{ControllerName}Test.java`

#### 3.3.1 Security Contract Tests

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-A-01 | THE TEST SHALL assert that every protected endpoint returns HTTP 401 when the `Authorization` header is absent. |
| FR-TEST-A-02 | THE TEST SHALL assert that every Staff endpoint returns HTTP 403 when called with a valid Student JWT. |
| FR-TEST-A-03 | THE TEST SHALL assert that every Admin endpoint returns HTTP 403 when called with a valid Staff JWT. |
| FR-TEST-A-04 | THE TEST SHALL assert that `POST /api/assessments/{id}/start` response body, when parsed for all JSON keys recursively, does NOT contain the strings `"correctOption"` or `"correctAnswerText"`. |
| FR-TEST-A-05 | THE TEST SHALL assert that `POST /api/auth/login` enforces rate limiting: after 5 identical requests per minute from the same IP, the 6th returns HTTP 429. |

#### 3.3.2 Auth Endpoints

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-A-10 | WHEN `POST /api/auth/login` is called with valid credentials, THE TEST SHALL assert HTTP 200, body contains `accessToken` and `refreshToken`, and `student` object without `passwordHash`. |
| FR-TEST-A-11 | WHEN `POST /api/auth/register` is called with a duplicate email, THE TEST SHALL assert HTTP 409 with error code `EMAIL_EXISTS`. |
| FR-TEST-A-12 | WHEN `POST /api/auth/forgot-password` is called with a non-existent email, THE TEST SHALL assert HTTP 200 (NOT 404) to prevent email enumeration. |
| FR-TEST-A-13 | WHEN `POST /api/auth/reset-password` is called with an expired token, THE TEST SHALL assert HTTP 400 with error code `INVALID_TOKEN`. |

#### 3.3.3 Assessment Endpoints

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-A-20 | WHEN `POST /api/assessments/{id}/submit` is called with `attemptId` that is already `status = 'submitted'`, THE TEST SHALL assert HTTP 422 with error code `ATTEMPT_ALREADY_SUBMITTED`. |
| FR-TEST-A-21 | THE TEST SHALL assert that `POST /api/assessments/{id}/submit` request body is rejected with HTTP 400 if a client-supplied `score` field is present and differs from server calculation. |

#### 3.3.4 AI Submission Endpoints

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-A-30 | WHEN `POST /api/submissions/speaking` is called with a valid audio file, THE TEST SHALL assert HTTP 202 (NOT 200) and the response body contains `status: "PENDING"`. |
| FR-TEST-A-31 | WHEN `POST /api/submissions/speaking` is called with a file exceeding 10MB, THE TEST SHALL assert HTTP 400 with error code `FILE_TOO_LARGE`. |
| FR-TEST-A-32 | WHEN `POST /api/submissions/speaking` is called with a non-audio file type (e.g. `.exe`), THE TEST SHALL assert HTTP 400 with error code `INVALID_FILE_TYPE`. |
| FR-TEST-A-33 | WHEN `GET /api/submissions/{id}/result` is called for a submission with `status = 'pending'`, THE TEST SHALL assert HTTP 200 with `status: "PENDING"` and no AI score fields. |

#### 3.3.5 Flashcard Endpoints

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-A-40 | WHEN `POST /api/flashcards/{id}/review` is called with `rating: "invalid_value"`, THE TEST SHALL assert HTTP 400 with error code `INVALID_RATING`. |
| FR-TEST-A-41 | WHEN `DELETE /api/flashcard-decks/{deckName}` is called for a system deck (`is_system = 1`), THE TEST SHALL assert HTTP 403 with error code `SYSTEM_DECK_IMMUTABLE`. |

#### 3.3.6 Student Management Endpoints (Staff)

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-A-50 | WHEN `POST /api/staff/students/{id}/suspend` is called with `reason` of fewer than 10 characters, THE TEST SHALL assert HTTP 400 with error code `VALIDATION_FAILED`. |
| FR-TEST-A-51 | WHEN `POST /api/staff/students/{id}/suspend` is called for an already-suspended account, THE TEST SHALL assert HTTP 409 with error code `ALREADY_IN_STATE`. |

---

### 3.4 Frontend Component Tests

> **Stack:** Jest 29, React Testing Library (RTL), `@testing-library/user-event` | **Naming:** `{ComponentName}.test.tsx`

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-F-01 | THE TEST SHALL assert that the Quiz page component does NOT render any `correctOption` or `correctAnswerText` data even if accidentally present in the API response (defense-in-depth). |
| FR-TEST-F-02 | WHEN a user submits the Login form with an empty email field, THE TEST SHALL assert that the HTML5 validation error is displayed and `POST /api/auth/login` is NOT called. |
| FR-TEST-F-03 | THE TEST SHALL assert that the Speaking submission component shows a loading/pending state immediately after "Nộp bài" is clicked, without waiting for polling to complete. |
| FR-TEST-F-04 | THE TEST SHALL assert that the Flashcard review component does NOT display the answer side (`backContent`) until the "Lật thẻ" button is clicked. |
| FR-TEST-F-05 | WHEN the API returns HTTP 401, THE TEST SHALL assert that the global Axios interceptor redirects the user to the Login page and clears the local token state. |
| FR-TEST-F-06 | THE TEST SHALL assert that the Assessment timer component reads remaining time from server-supplied `startedAt + durationMin` and does NOT use `Date.now()` as the reference point. |
| FR-TEST-F-07 | THE TEST SHALL assert that Staff-only navigation items (content management, student management) are NOT rendered in the DOM when the logged-in role is `STUDENT`. |
| FR-TEST-F-08 | WHEN a VIP-gated content card is rendered for a FREE student, THE TEST SHALL assert that the "Xem bài học" button is replaced by an upgrade CTA and the API call is NOT made. |

---

### 3.5 End-to-End Tests (E2E)

> **Stack:** Playwright 1.x, Chromium | **Naming:** `{flow-name}.spec.ts` | **Environment:** staging or local docker-compose

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-E-01 | THE E2E TEST SHALL cover the full registration → email verification → login → dashboard flow for a new Student account without any manual intervention. |
| FR-TEST-E-02 | THE E2E TEST SHALL cover the full quiz flow: Student selects quiz → starts → answers all questions → submits → receives score and explanations. |
| FR-TEST-E-03 | THE E2E TEST SHALL assert that a suspended student who attempts to navigate to `/dashboard` is redirected to the login page with a 403 error message. |
| FR-TEST-E-04 | THE E2E TEST SHALL cover the Flashcard review loop: open deck → view card front → flip → rate "easy" → next card appears with updated due date. |
| FR-TEST-E-05 | THE E2E TEST SHALL assert that the Speaking submission page shows `status: PENDING` immediately after upload, then transitions to showing the AI score after polling completes (mock AI engine). |
| FR-TEST-E-06 | THE E2E TEST SHALL cover the Staff content creation flow: login as Staff → create question (draft) → submit for review → verify status = `pending_review` in the response. |

---

### 3.6 Security Invariant Tests (Cross-cutting — KHÔNG được disable)

> Các test này kiểm tra 5 invariant bảo mật cốt lõi. Phải pass 100% — không có exception.

| ID | EARS Requirement |
|:---|:---|
| FR-TEST-S-01 | **[INVARIANT: No Answer Leak]** THE TEST SHALL assert across ALL question-retrieval API responses that `correctOption` and `correctAnswerText` are absent from the response JSON. |
| FR-TEST-S-02 | **[INVARIANT: Server-Side Scoring]** THE TEST SHALL assert that modifying the `score` field in a quiz submission request body has NO effect on the stored score in `test_attempts`. |
| FR-TEST-S-03 | **[INVARIANT: Password Never Exposed]** THE TEST SHALL assert that no API response (including admin endpoints) ever contains a `password`, `passwordHash`, or `two_factor_secret` field. |
| FR-TEST-S-04 | **[INVARIANT: No Hard Delete]** THE TEST SHALL assert that no `DELETE FROM` statement is executed against any primary data table — only soft-delete updates (`is_deleted = 1` or `status = 'deleted'`). |
| FR-TEST-S-05 | **[INVARIANT: Admin Login]** THE TEST SHALL assert that a valid Admin password returns a JWT access token directly. |

---

## 4. NON-FUNCTIONAL REQUIREMENTS

| ID | Category | Requirement |
|:---|:---|:---|
| NFR-TEST-01 | Coverage — Service | JaCoCo: Service layer ≥ **80%** line coverage; CI gate blocks merge nếu dưới ngưỡng |
| NFR-TEST-02 | Coverage — Controller | JaCoCo: Controller layer ≥ **70%** line coverage |
| NFR-TEST-03 | Coverage — Frontend | Istanbul/c8: Component/hook coverage ≥ **70%** branch + line |
| NFR-TEST-04 | Execution Time | Unit + Integration suite (backend) phải hoàn thành trong < **5 phút** trên CI |
| NFR-TEST-05 | Execution Time | E2E suite phải hoàn thành trong < **10 phút** trên CI (chỉ golden paths) |
| NFR-TEST-06 | Isolation | Mỗi test phải **độc lập** — không phụ thuộc thứ tự chạy. `@BeforeEach` setup và `@AfterEach` teardown bắt buộc cho integration tests |
| NFR-TEST-07 | Isolation | Integration tests PHẢI dùng Testcontainers — KHÔNG dùng H2 in-memory (vì SQL Server có dialect riêng) |
| NFR-TEST-08 | Determinism | Tests KHÔNG được dùng `LocalDate.now()` hay `Instant.now()` trực tiếp — phải inject `Clock` để mock thời gian |
| NFR-TEST-09 | Naming | Backend: `{ClassName}Test.java` (unit), `{ClassName}IT.java` (integration). Frontend: `{Component}.test.tsx` |
| NFR-TEST-10 | SM-2 Coverage | `FlashcardSrsServiceTest.java` phải có ≥ **10** test cases covering mọi nhánh của SM-2 algorithm (xem FR-TEST-U-24) |
| NFR-TEST-11 | No Flaky Tests | Flaky tests (fail intermittently) phải được tag `@Disabled("FLAKY: reason")` và tạo ticket fix trong vòng 1 sprint |
| NFR-TEST-12 | CI Gate | GitHub Actions pipeline PHẢI fail PR nếu: (a) bất kỳ test nào FAIL, (b) coverage giảm so với main branch |
| NFR-TEST-13 | Logging | Test failures PHẢI in ra đủ context: `{testName, inputData, expectedValue, actualValue}` — không chỉ "AssertionError" |
| NFR-TEST-14 | Security Tests | 5 Security Invariant Tests (FR-TEST-S-01 → S-05) phải luôn ở trạng thái ENABLED — không được `@Disabled` |

---

## 5. DATA MODEL

### 5.1 Test Environment Configuration

```yaml
# application-test.yml (Spring Boot test profile)
spring:
  datasource:
    url: jdbc:sqlserver://localhost:${test.db.port};databaseName=jlpt_test;encrypt=false
    username: sa
    password: Test@Password123
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    hibernate:
      ddl-auto: none  # Flyway manages schema
  flyway:
    enabled: true
    locations: classpath:db/migration

test:
  jwt:
    secret: test-secret-key-32-characters-long
    expiry-minutes: 15
  ai-engine:
    mock-url: http://localhost:${wiremock.port}/ai
  clock:
    fixed: true  # Inject fixed Clock for deterministic date tests
```

### 5.2 Test Data Seeding (Fixtures)

```sql
-- V999__test_seed.sql (chỉ apply ở test profile)

-- Seed: 1 Student active
INSERT INTO student_users (student_id, email, password_hash, full_name, status, current_jlpt_level)
VALUES (1, 'student@test.com', '$2a$10$...bcrypt_hash...', N'Test Student', 'active', 'N3');

-- Seed: 1 Staff active
INSERT INTO staff_users (staff_id, email, password_hash, full_name, staff_role, status)
VALUES (1, 'staff@test.com', '$2a$10$...bcrypt_hash...', N'Test Staff', 'staff', 'active');

-- Seed: 1 Admin active
INSERT INTO admin_users (admin_id, email, password_hash, full_name, status)
VALUES (1, 'admin@test.com', '$2a$12$...bcrypt_hash...', N'Test Admin', 'active');

-- Seed: 1 Published quiz N3 với 3 câu hỏi
INSERT INTO assessments (assessment_id, assessment_type, title, jlpt_level, duration_min, pass_score, total_score, status)
VALUES (1, 'quiz', N'Test Quiz N3', 'N3', 30, 2, 3, 'published');

-- Seed: 3 questions đã published
INSERT INTO questions (question_id, question_text, question_type, skill, jlpt_level, option_a, option_b, option_c, option_d, correct_option, status)
VALUES 
  (1, N'水 (nước) đọc là?', 'multiple_choice', 'kanji', 'N3', 'mizu', 'yama', 'hana', 'kawa', 'A', 'published'),
  (2, N'山 (núi) đọc là?', 'multiple_choice', 'kanji', 'N3', 'umi', 'yama', 'sora', 'kaze', 'B', 'published'),
  (3, N'花 (hoa) đọc là?', 'multiple_choice', 'kanji', 'N3', 'mori', 'tsuki', 'hana', 'ki', 'C', 'published');

-- Seed: flashcard due today (để test SRS queue)
INSERT INTO flashcards (flashcard_id, student_id, deck_name, content_type, content_id, interval_days, ease_factor, repetition_count, next_review_date)
VALUES (1, 1, N'Default', 'kanji', 1, 1, 2.50, 0, CAST(SYSUTCDATETIME() AS DATE));
```

### 5.3 TestDataBuilder Pattern (Java)

```java
// TestDataBuilder.java — fluent builder cho test data
public class StudentBuilder {
    private String email = "student@test.com";
    private String status = "active";
    private String jlptLevel = "N3";

    public static StudentBuilder aStudent() { return new StudentBuilder(); }
    public StudentBuilder withEmail(String email) { this.email = email; return this; }
    public StudentBuilder suspended() { this.status = "suspended"; return this; }
    public StudentBuilder withLevel(String level) { this.jlptLevel = level; return this; }
    public StudentUser build() { /* ... */ }
}

// Usage in tests:
StudentUser suspendedStudent = aStudent().suspended().withEmail("suspended@test.com").build();
```

### 5.4 Mock Clock (Deterministic Time Testing)

```java
// ClockConfig.java — override trong test profile
@TestConfiguration
public class TestClockConfig {
    @Bean
    @Primary
    public Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-05-30T08:00:00Z"), ZoneOffset.UTC);
    }
}
```

---

## 6. API SPEC (Mock Server Contracts)

> Các external services được mock bằng WireMock trong integration và E2E tests.

### 6.1 Mock AI Engine (Speaking & OCR)

**Speaking Analysis Mock — Success:**

```
POST /ai/speech/analyze
Response (200):
{
  "overall_score": 85,
  "pronunciation_score": 80,
  "fluency_score": 90,
  "highlighted_errors": [{"position": 3, "word": "です", "suggestion": "Nhấn âm cuối"}],
  "suggestions": "Phát âm tốt, cần cải thiện ngữ điệu"
}
```

**Speaking Analysis Mock — Timeout (for retry tests):**

```
POST /ai/speech/analyze
→ WireMock: delay 35000ms (simulates 35s timeout)
→ Expected behavior: retry 3x, fallback after exhaustion
```

**OCR Mock — Correct Character:**

```
POST /ai/ocr/recognize
Body: { "image_base64": "..." }
Response (200):
{
  "recognized_character": "水",
  "similarity_percent": 88
}
```

**OCR Mock — Wrong Character:**

```
POST /ai/ocr/recognize
Response (200):
{
  "recognized_character": "永",
  "similarity_percent": 52
}
```

### 6.2 Mock SMTP Server (Email Testing)

> Dùng GreenMail hoặc MailHog trong test profile.

```
SMTP mock captures all outgoing emails.
Tests verify:
  - Email subject contains "Xác minh tài khoản" for verification emails
  - Email body contains verification token (single-use)
  - Reset password email contains token with expiry hint
  - Email content does NOT contain plaintext password
```

### 6.3 Mock OAuth Server (Google)

```
GET /oauth2/authorize
→ WireMock redirect to callback with code=test_auth_code

POST /oauth2/token
Body: { "code": "test_auth_code" }
Response (200):
{
  "access_token": "mock_google_token",
  "id_token": "...",
  "email": "oauthuser@gmail.com",
  "name": "OAuth Test User"
}
```

### 6.4 CI Coverage Report Format (JaCoCo)

```xml
<!-- jacoco.xml — CI artifact consumed by coverage gate -->
<report name="JLPT Backend">
  <package name="com/jlpt/service">
    <counter type="LINE" missed="20" covered="100"/>  <!-- 83% coverage -->
  </package>
  <package name="com/jlpt/controller">
    <counter type="LINE" missed="15" covered="50"/>   <!-- 77% coverage -->
  </package>
</report>
```

**Coverage Gate Script (GitHub Actions):**

```yaml
- name: Check Coverage Gate
  run: |
    SERVICE_COV=$(python scripts/parse_jacoco.py --package service)
    CONTROLLER_COV=$(python scripts/parse_jacoco.py --package controller)
    [ "$SERVICE_COV" -ge 80 ] || (echo "Service coverage $SERVICE_COV% < 80%" && exit 1)
    [ "$CONTROLLER_COV" -ge 70 ] || (echo "Controller coverage $CONTROLLER_COV% < 70%" && exit 1)
```

---

## 7. ERROR HANDLING

### 7.1 Test Failure Response Policy

| Failure Type | Policy | Action |
|:---|:---|:---|
| **Unit test fails** | CI blocks PR immediately | Developer fixes before merge |
| **Integration test fails** | CI blocks PR immediately | Check Testcontainers startup log |
| **Security Invariant fails** | CI blocks PR + Slack alert to team | Treat as P0 bug, fix same day |
| **E2E flaky fail** | Allow 1 retry; if fail again → block | Tag `@flaky`, create fix ticket |
| **Coverage drops below gate** | CI blocks PR | Add missing tests for changed code |

### 7.2 Common Test Failure Causes & Fixes

| Error | Root Cause | Fix |
|:---|:---|:---|
| `Testcontainers: Could not create SQL Server container` | Docker not running on CI | Ensure GitHub Actions runner has Docker daemon |
| `Clock-dependent test fails on different timezone` | Using `LocalDate.now()` directly | Inject `Clock` bean, use `LocalDate.now(clock)` |
| `JWT validation fails in MockMvc test` | Test JWT uses wrong secret | Use `TestClockConfig` + consistent test JWT secret |
| `AI retry test is timing-sensitive` | Real sleep in retry logic | Mock `Clock` + use `@SpyBean` to verify call count |
| `WireMock port conflict` | Multiple tests using same port | Use random port: `@WireMockTest(httpPort = 0)` |
| `Flashcard order test non-deterministic` | Missing `ORDER BY` in query | Fix query to always `ORDER BY next_review_date ASC` |

### 7.3 Flaky Test Handling

```java
// Acceptable temporary tagging (must have resolution ticket)
@Test
@Disabled("FLAKY: timing-sensitive AI retry test — fix in JLPT-456")
void aiRetry_shouldRetry3Times_whenEngineTimesOut() { ... }
```

**Rule:** A test tagged `@Disabled("FLAKY: ...")` must have a linked ticket and be fixed within 1 sprint. Security Invariant tests (FR-TEST-S-01 → S-05) are EXEMPT from this rule — they can NEVER be `@Disabled`.

---

## 8. ACCEPTANCE CRITERIA

| ID | Scenario | Given | When | Then |
|:---|:---|:---|:---|:---|
| AC-TEST-01 | Unit test suite passes | Tất cả service implementations đã viết | `mvn test -Dgroups=unit` | 0 failures, 0 errors |
| AC-TEST-02 | Integration test suite passes | Docker daemon running, SQL Server container available | `mvn verify -Dgroups=integration` | 0 failures, schema constraints verified |
| AC-TEST-03 | Security invariants pass | Tất cả FR-TEST-S-01 → S-05 implemented | `mvn test -Dgroups=security` | 5/5 pass, tất cả ENABLED |
| AC-TEST-04 | Service coverage ≥ 80% | Test suite chạy xong | JaCoCo report generated | `com/jlpt/service` ≥ 80% line coverage |
| AC-TEST-05 | Controller coverage ≥ 70% | Test suite chạy xong | JaCoCo report generated | `com/jlpt/controller` ≥ 70% line coverage |
| AC-TEST-06 | SM-2 algorithm fully tested | `FlashcardSrsServiceTest.java` viết đủ | Run SM-2 test class | ≥ 10 test cases, tất cả pass |
| AC-TEST-07 | Frontend tests pass | Component tests viết cho critical components | `npm run test` | 0 failures, ≥ 70% branch coverage |
| AC-TEST-08 | E2E golden paths pass | Dev environment running, WireMock configured | `npx playwright test` | FR-TEST-E-01 → E-06 all pass |
| AC-TEST-09 | CI gate enforced | GitHub Actions configured | Open PR to main | PR blocked if any test fails or coverage below threshold |
| AC-TEST-10 | No correct_option leak | FR-TEST-A-04 + FR-TEST-F-01 implemented | Run security group | Both tests pass, keys not found in response JSON |
| AC-TEST-11 | Test execution time | Full unit + integration suite | Run on CI | Completes in < 5 minutes |
| AC-TEST-12 | AI async contract | FR-TEST-A-30 + FR-TEST-U-30 implemented | Run AI tests | HTTP 202 confirmed, sync wait NOT detected |

---

## OUT OF SCOPE

- ❌ **Load / Performance testing** (JMeter, k6) — Phase 2, sepcific NFR-TEST spike
- ❌ **Security penetration testing** (OWASP ZAP, Burp Suite) — handled by dedicated security audit
- ❌ **Accessibility testing** (axe-core, screen reader) — Phase 2 UX sprint
- ❌ **Cross-browser testing** (Firefox, Safari, Edge) — Playwright chỉ dùng Chromium trong CI
- ❌ **Mobile / Responsive UI testing** — Phase 2
- ❌ **Database migration rollback testing** (Flyway undo) — Phase 2
- ❌ **Chaos engineering** (kill random containers mid-test) — Phase 3
- ❌ **Contract testing giữa BE và FE** (Pact/consumer-driven) — Phase 2
- ❌ **AI Engine accuracy testing** (quality of OCR/speech recognition results) — AI provider's responsibility
- ❌ **Manual exploratory testing checklists** — thuộc QA runbook, không phải spec này
- ❌ **Test data generation cho volume testing** (>50k records) — Phase 2
