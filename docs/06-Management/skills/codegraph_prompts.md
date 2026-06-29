# Bộ Prompt Sử dụng CodeGraph

Dưới đây là bộ 20 prompt chuyên sâu được thiết kế để sử dụng với CodeGraph, giúp bạn phân tích, truy vết, đánh giá và tối ưu hóa dự án một cách toàn diện.

---

## ⚠️ Đọc trước khi dùng

### 1. `projectPath` — bắt buộc, vì index không nằm ở root

Index `.codegraph/` của repo này nằm tại `docs/06-Management/skills/.codegraph/`, **không** nằm ở root repo. CodeGraph chỉ tìm `.codegraph/` bằng cách đi **lên** (lên thư mục cha) từ path được truyền — không đi xuống. Vì vậy nếu gọi tool/CLI với path là root repo, CodeGraph sẽ báo *"not indexed"* dù index thực tế đã phủ đủ cả `apps/backend` và `apps/frontend`.

→ Khi gọi MCP tool, luôn truyền:

```
projectPath = "<đường dẫn repo>/docs/06-Management/skills"
```

(hoặc bất kỳ thư mục con của nó). Khi dùng CLI, `cd` vào thư mục đó trước khi chạy `codegraph explore`.

→ Nếu muốn dùng path root cho gọn, cần `codegraph init` lại tại root repo (việc này dời/tạo lại index — **hỏi ý kiến trước khi chạy**, không tự thực hiện).

### 2. Đừng hỏi "toàn bộ repo" trong 1 câu — chia theo module

`codegraph_explore` trả verbatim source + call path trong 1 lần gọi, nhưng bị giới hạn `maxFiles` (mặc định 12) và có "explore budget" hợp lý theo quy mô project (repo này ~619 files → khoảng 2 lần gọi rộng là hết budget). Hỏi "phân tích toàn bộ kiến trúc" sẽ chỉ trả về danh sách rút gọn cho phần lớn file, không ra source thật.

→ Chia nhỏ theo **feature module thật** của repo: `auth`, `student.kana`, `student.reading`, `flashcard`, `dictionary`, `notification`, `support`, quiz/exam, AI (OCR Kanji, Speech shadowing) — xem danh sách ở mục 6.
→ Ưu tiên hỏi bằng **tên symbol/class cụ thể** (vd. `"QuizService submitQuiz QuizAttempt"`) thay vì câu hỏi mở (vd. `"phân tích kiến trúc"`) — tool trả thẳng source liên quan, không tốn budget dò tìm.
→ Khi 1 câu hỏi báo còn file chưa hiển thị ("Not shown above — explore these names"), gọi tiếp 1 lần `codegraph_explore` nhắm đúng các tên đó trước khi tổng hợp.

### 3. Gắn review vào rule riêng của dự án (CLAUDE.md / CONSTITUTION.md / AGENTS.md)

Repo có ADR và anti-pattern cụ thể: bcrypt cost ≥ 10, soft delete bắt buộc (không `DELETE FROM`), DTO không lộ Entity ra API, AI module phải async + retry 3 lần + fallback + log (không silent fail), quiz/question phải lock sau khi có attempt, authorization phải check **cả** role **và** subscription/level. Các prompt review dưới đây đã trỏ thẳng vào các rule này thay vì hỏi chung.

---

## 1. Hiểu toàn bộ dự án

```text
Using CodeGraph (projectPath: docs/06-Management/skills), explore module-by-module:
auth, student.kana, student.reading, flashcard, dictionary, notification, support,
quiz/exam, AI (OCR, speech).

For each module provide:
1. Entry point (Controller)
2. Service + business logic
3. Repository + entities touched
4. Frontend page/component calling it

Then summarize:
- High-level architecture (frontend / backend / DB)
- Request flow pattern (Controller → Service → Repository → Entity)
- Cross-module dependencies

Show file paths for all findings. If a module's files weren't returned in the
source, issue a follow-up codegraph_explore for that module's symbol names
before answering.
```

## 2. Vẽ kiến trúc hệ thống

```text
Using CodeGraph (projectPath: docs/06-Management/skills), generate a detailed
architecture description for this JLPT e-learning platform.

Include:
- React frontend (apps/frontend/src: pages by role — student/staff/admin/manager)
- Spring Boot backend (apps/backend: feature-based packages com.jlpt.feature.*)
- Database (entities + Flyway migrations)
- JWT-based authentication
- AI services (OCR for Kanji, Speech for shadowing) and how they're called
  (sync vs async/job-polling)

Describe component relationships and data flow. Flag any place where this
diverges from the documented layer order Controller → Service → Repository → Entity.
```

## 3. Tìm toàn bộ API

```text
Using CodeGraph (projectPath: docs/06-Management/skills), list all REST APIs,
grouped by feature package under com.jlpt.feature.*
(auth, student.kana, student.reading, flashcard, dictionary, notification,
support, plus any quiz/exam and admin/staff controllers).

For each endpoint provide:
- HTTP method + URL
- Controller class + method
- Service called
- Purpose
- Request DTO / Response DTO (confirm it's a DTO, not the raw Entity)

Show file paths. Call out any endpoint returning an @Entity directly instead of a DTO.
```

## 4. Phân tích database

```text
Using CodeGraph (projectPath: docs/06-Management/skills), identify all @Entity
classes, their repositories, and relationships.

Provide:
- Entity name -> table name
- Repository interface
- Relationships (@OneToMany/@ManyToOne/@ManyToMany) and foreign keys
- Soft-delete fields present (is_deleted / status) — flag any entity missing one
- Audit columns present (created_at/updated_at/created_by) — flag any missing

Generate an ERD description from this.
```

## 5. Truy vết luồng đăng nhập

```text
Using CodeGraph (projectPath: docs/06-Management/skills), trace the complete
login/auth flow.

Start from the React login page/component and follow through:
- API call (services/ layer)
- AuthController
- AuthService (JWT generation, bcrypt check)
- UserRepository
- Database

Explain each step, show related files, and confirm bcrypt cost factor is >= 10
(ADR-003) and that JWT secret/expiry isn't hardcoded.
```

## 6. Truy vết một chức năng bất kỳ

```text
Using CodeGraph (projectPath: docs/06-Management/skills), trace the complete
end-to-end flow of <PICK ONE>:
- Kana learning + progress tracking (student.kana)
- Reading lesson + submission (student.reading)
- Flashcard spaced-repetition review (flashcard)
- Dictionary search (dictionary)
- Support ticket creation + staff reply (support)
- Notification delivery (notification)
- Quiz/exam attempt + auto-grading
- OCR Kanji similarity check (async job)
- Speech shadowing transcription (async job)

Include: frontend component, API call, Controller, Service, Repository,
DB tables/entities touched. Show execution flow and file paths.
```

## 7. Tìm JWT

```text
Using CodeGraph (projectPath: docs/06-Management/skills), analyze authentication
and authorization end to end.

Identify:
- JWT generation + validation (filter/provider classes)
- SecurityConfig (filter chain, permitted vs protected paths)
- Role-based checks (STUDENT/STAFF/ADMIN)
- Subscription/level checks (LESSON-003: must check role AND subscription —
  flag any endpoint that only checks role for level-gated content)

Explain implementation details with file paths.
```

## 8. Tìm Security Issues

```text
Using CodeGraph (projectPath: docs/06-Management/skills), perform a security
review specific to this project's rules.

Check for:
- bcrypt cost factor < 10 (violates ADR-003)
- Hard deletes (DELETE FROM / repository.delete()) instead of soft delete (ADR-004)
- Entities returned directly from controllers instead of DTOs (ADR-005)
- Endpoints checking role only, not role + subscription/level (LESSON-003)
- AI calls (OCR/Speech) without timeout/retry/fallback — silent fail (LESSON-006)
- Hardcoded secrets/credentials
- Missing @Valid / input validation on request DTOs
- CORS misconfiguration

Rank findings by severity, with file paths.
```

## 9. Tìm Dead Code

```text
Using CodeGraph (projectPath: docs/06-Management/skills), identify dead code.

Find:
- Unused Service/Repository methods (no callers in the call graph)
- Unused React components (not imported/rendered anywhere)
- Unused Controller endpoints (no frontend service call references them)
- Unused entities/DTOs

Show file paths. Note: confirm "no callers" via the call graph, not just text
search, since dynamic-dispatch usage (e.g. JSX rendering) won't show in grep.
```

## 10. Tìm Code Trùng Lặp

```text
Using CodeGraph (projectPath: docs/06-Management/skills), detect duplicated
logic across the repository.

Find:
- Duplicate validation logic across Services (e.g. repeated subscription/level
  checks that could be a shared @PreAuthorize or utility)
- Duplicate repository queries doing the same filter/join
- Duplicate React components across role folders (student/staff/admin) that
  could share a common base component

Suggest refactoring opportunities with file paths.
```

## 11. Tìm God Class

```text
Using CodeGraph (projectPath: docs/06-Management/skills), identify classes that
violate the Single Responsibility Principle (CLAUDE.md anti-pattern: max ~300
lines/file, ~40 lines/method).

Check:
- Controllers/Services exceeding these limits
- Classes with excessive dependencies (constructor injecting many repositories/services)
- High coupling (one class touching many unrelated feature packages)

Recommend how to split, with file paths.
```

## 12. Kiểm tra kiến trúc

```text
Using CodeGraph (projectPath: docs/06-Management/skills), review the repository
architecture against the documented layering:
Controller -> Service -> Repository -> Entity.

Check:
- Controllers calling Repository directly (skipping Service)
- Services calling other Services across unrelated feature packages directly
  (vs. via a clear interface)
- Circular dependencies between feature packages
- @Transactional placed outside the Service layer

Provide improvement recommendations with file paths.
```

## 13. Tìm N+1 Query

```text
Using CodeGraph (projectPath: docs/06-Management/skills), analyze JPA/Hibernate
usage across all repositories.

Find:
- N+1 query risks (lazy-loaded collections accessed in a loop, e.g. in
  flashcard review lists, quiz question lists, kana progress lists)
- Missing JOIN FETCH / @EntityGraph where a Service loops over a relation
- Missing indexes on FK/filter columns used in @Query methods

Explain performance impact with file paths.
```

## 14. Tối ưu React

```text
Using CodeGraph (projectPath: docs/06-Management/skills), analyze React
performance across apps/frontend/src.

Find:
- Unnecessary re-renders (missing memo/useCallback on components passed as
  props, e.g. list item cards like LessonCard, QuizCard, VocabCard)
- Large/God components (pages/ files doing data-fetch + business logic + render)
- Prop drilling that should be Context (especially auth/role state)
- Business logic computed in the frontend that should be server-side (anti-pattern:
  "Business Logic in Frontend" — flag any score/grade calculation done client-side)

Provide optimizations with file paths.
```

## 15. Impact Analysis trước khi sửa code

```text
Using CodeGraph (projectPath: docs/06-Management/skills), perform impact
analysis for <SERVICE OR CLASS NAME, e.g. AuthService, QuizService, OcrService>.

Identify:
- Callers (which Controllers/Services use it)
- Callees (what it depends on — other Services, Repositories)
- Affected frontend pages/components (via the API endpoints it backs)
- Potential regression risks if its method signatures change

Show all related files.
```

## 16. Tìm toàn bộ luồng nghiệp vụ

```text
Using CodeGraph (projectPath: docs/06-Management/skills), identify all major
business features by walking each com.jlpt.feature.* package.

For each feature provide:
- Frontend entry point (page/component)
- API endpoints
- Services
- Database tables/entities

Describe the business flow in 2-3 sentences each.
```

## 17. Chuẩn bị Deploy

```text
Using CodeGraph (projectPath: docs/06-Management/skills), analyze deployment
requirements.

Identify:
- Backend build/run requirements (pom.xml, application.yml profiles)
- Frontend build requirements (package.json scripts, env vars consumed via import.meta.env)
- Database requirements (Flyway migrations under src/main/resources/db/migration)
- Required environment variables (DB creds, JWT secret, SMTP, AI service keys)
- Docker/CI config if present

Generate a deployment checklist with file paths.
```

## 18. Sinh tài liệu SRS

```text
Using CodeGraph (projectPath: docs/06-Management/skills), identify all business
features (see prompt #16's module list).

Generate a Software Requirements Specification (SRS) including:
- Functional requirements per feature
- Non-functional requirements (security: JWT/bcrypt; performance; AI fallback
  behavior per LESSON-006)
- Actors (Student, Staff, Admin)
- Use cases per actor
- Business rules (subscription gating, quiz locking, soft delete)
```

## 19. Sinh tài liệu SDD

```text
Using CodeGraph (projectPath: docs/06-Management/skills), generate a Software
Design Description (SDD).

Include:
- Architecture (layered Controller/Service/Repository/Entity, feature-package
  organization under com.jlpt.feature.*)
- Modules (per prompt #16 list)
- Database design (entities, relationships, soft-delete/audit columns)
- API design (per prompt #3)
- Security design (JWT, bcrypt, role+subscription authorization)
- Deployment design (per prompt #17)
```

## 20. Prompt mạnh nhất (Dùng trước khi bảo vệ đồ án)

```text
Using CodeGraph (projectPath: docs/06-Management/skills), perform a complete
senior-level repository review against this project's own ADRs and anti-patterns
(see CLAUDE.md): DTO pattern, soft delete, bcrypt cost >= 10, AI async+fallback,
role+subscription authorization, quiz locking.

Analyze:
1. Architecture (layering, feature-package boundaries)
2. Database Design (soft delete, audit columns, relationships)
3. REST APIs (DTO usage, consistency)
4. Authentication & Authorization (JWT, role+subscription)
5. Security (per prompt #8's checklist)
6. Performance (N+1 queries, React re-renders)
7. Code Quality (God classes, dead code, duplication)
8. React Frontend
9. Spring Boot Backend
10. Deployment Readiness

For each issue provide:
- Severity
- File path
- Explanation
- Suggested fix

Rank the Top 20 improvements that would have the biggest impact on the project.
If any module wasn't covered by the explore calls used, say so explicitly rather
than guessing.
```
