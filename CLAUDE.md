# CLAUDE.md — JLPT E-Learning System v2.0
## Hệ Thống Học Tập & Luyện Thi Tiếng Nhật Cấp Độ N5→N1

> **Mục đích**: Bản đồ địa hình — Kiến trúc, ADR, Lessons Learned, Anti-patterns
> **Đọc trước**: `CONSTITUTION.md` (stack, security, standards) | `AGENTS.md` (domain rules, golden patterns)

---

## TL;DR (60 giây)

### Tech Stack (Xem `CONSTITUTION.md § ĐIỀU 1`)

### Domain
- **E-learning luyện thi JLPT** tích hợp AI (N5 → N1)
- **3 Roles**: Student, Staff, Admin
- **AI Modules**: OCR (Kanji) + Speech Recognition (Shadowing)
- **Payment**: VIP subscription qua gateway

### Key Rules
- ✅ DTO Pattern bắt buộc
- ✅ Soft Delete toàn hệ thống
- ✅ AI async + fallback
- ❌ KHÔNG Entity ra API
- ❌ KHÔNG Hard Delete
- ❌ KHÔNG bypass JWT/Security

---

## KIẾN TRÚC HỆ THỐNG

### Sơ đồ tổng quan

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         FRONTEND (ReactJS 18 + TypeScript)                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │Dashboard │  │Learning  │  │Testing   │  │AI Module │  │Payment   │  │
│  │(Student) │  │(Kanji,   │  │(JLPT     │  │(OCR,     │  │(VIP      │  │
│  │          │  │Vocab)    │  │Mock Exam)│  │Speech)   │  │Upgrade)  │  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  │
└───────┼─────────────┼─────────────┼─────────────┼─────────────┼────────┘
        │             │             │             │             │
        ▼             ▼             ▼             ▼             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                       BACKEND (Spring Boot 3.x + Java 21)                     │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │  REST API Layer → Service Layer → Repository Layer → Entity          │  │
│  │  /api/auth  /api/courses  /api/lessons  /api/quizzes  /api/ai/*      │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼──────────────────────┐
        ▼                     ▼                      ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   SQL Server    │  │  File Storage   │  │  Email SMTP     │
│  (Primary DB)   │  │  (/uploads, S3) │  │  (Notification) │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### Layer Architecture (Backend)

```
┌─────────────────────────────────────────┐
│          Controller Layer               │  @RestController
│   - Input validation (@Valid)          │  - Handle HTTP requests
│   - Response formatting (DTO)           │  - Return ApiResponse<DTO>
│   - HTTP status codes                   │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│           Service Layer                  │  @Service
│   - Business logic                      │  - @Transactional HERE
│   - Authorization (Role + Level)         │  - Audit logging
│   - AI orchestration                    │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│          Repository Layer                │  @Repository
│   - JPA/Hibernate queries               │  - Spring Data JPA
│   - Custom JPQL / @Query               │
└─────────────────┬───────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│           Entity Layer                   │  @Entity
│   - DB table mapping                    │  - JPA annotations
│   - Soft delete (is_deleted)            │
└─────────────────────────────────────────┘
```

---

## CORE MODULES

### Auth & User Module
```
├── Login/Register (Student, Staff, Admin)
├── JWT + Refresh Token
├── 2FA cho Admin (TOTP)
├── Role-based access (STUDENT / STAFF / ADMIN)
└── Session management
```

### Learning & Content Module
```
├── Course theo JLPT level (N5-N1)
├── Lesson types: KANJI, KANA, VOCAB, GRAMMAR
├── Flashcard (Spaced Repetition)
└── CRUD by Staff/Admin only
```

### Testing & AI Module
```
├── Mock Exam (auto-grading)
├── Quiz theo chủ đề
├── OCR - Kanji similarity %
└── Speech - Shadowing transcription
```

### Payment Module
```
├── VIP upgrade (packages)
├── Payment gateway (VNPay/Stripe)
└── Transaction history
```

---

## FLOWS

### Quiz Submission Flow

```
Student → Frontend (answer questions)
       → POST /api/quiz-attempts
       → QuizService.submitQuiz()
           ├── Validate server-side time
           ├── Calculate score (NOT from client)
           ├── Create NEW attempt record
           ├── Audit log
           └── Return result
```

### OCR Flow (Async)

```
Student → Upload image
       → POST /api/ai/ocr/submit
       → Returns { jobId, status: PENDING }
       → Poll GET /api/ai/ocr/{jobId}
       → When ready: { similarity, result }
```

### VIP Payment Flow

```
Student → Select package
       → POST /api/payment/initiate
       → Gateway redirect
       → Webhook /api/payment/webhook
           ├── Check idempotency_key
           ├── Process (or skip if duplicate)
           └── Update subscription
```

---

## ADR (Architecture Decision Records)

### ADR-001: Spring Boot 3.x + Java 21
**Decision**: Java/Spring Boot cho backend
**Pros**: Type safety, enterprise ecosystem, mature Security/Data JPA
**Cons**: Verbose, compile time, harder to microservice later
**Status**: ✅ Active

---

### ADR-002: Monolithic Architecture
**Decision**: Monolith đầu tiên, tách module rõ ràng
**Pros**: Dễ develop/debug/deploy cho team nhỏ
**Cons**: Khó scale từng module độc lập
**Future**: Có thể tách AI Module khi cần
**Status**: ✅ Active

---

### ADR-003: JWT + bcrypt (cost 12) + 2FA Admin
**Decision**: Stateless JWT + bcrypt + TOTP
**Bắt buộc**: 
- bcrypt cost **không dưới 10**
- Admin bắt buộc 2FA
**Status**: ✅ Active

---

### ADR-004: Soft Delete toàn hệ thống
**Decision**: `is_deleted = true` hoặc `status = INACTIVE`
**Tuyệt đối**: Không dùng `DELETE FROM`
**Status**: ✅ Active

---

### ADR-005: DTO Pattern bắt buộc
**Decision**: Controller chỉ nhận/trả DTO
**Mapping**: Entity ↔ DTO tại Service Layer
**Tool suggestion**: MapStruct
**Status**: ✅ Active

---

### ADR-006: File Media tại /uploads hoặc S3
**Decision**: Không BLOB trong DB
**Storage**: Path/URL trong DB, file tại storage
**Cleanup**: Job xóa orphan files định kỳ
**Status**: ✅ Active

---

### ADR-007: OCR chỉ so sánh Similarity %
**Decision**: OCR trả về similarity % vs ký tự chuẩn
**Không**: Phân tích stroke order
**Lý do**: Giảm complexity, tập trung core value
**Status**: ✅ Active

---

### ADR-008: Global Exception Handler
**Decision**: `@ControllerAdvice` bắt tất cả exceptions
**Format**: `{ status, message, data }`
**Status**: ✅ Active

---

## LESSONS LEARNED

### LESSON-001: Tách UI Staff và Admin
**Sai lầm**: Gộp chung màn hình → Staff làm thao tác Admin
**Giải pháp**: Component/page riêng cho Staff vs Admin
**Áp dụng**: Mọi màn hình có phân quyền

---

### LESSON-002: Không lưu BLOB trong DB
**Sai lầm**: Lưu audio/image BLOB → DB phình, backup chậm
**Giải pháp**: File tại `/uploads` (dev) hoặc S3 (prod), chỉ lưu path
**Áp dụng**: OcrService, SpeechService, UserService (avatar)

---

### LESSON-003: Authorization = Role + Subscription
**Sai lầm**: Chỉ check role → học viên N5 truy cập N2
**Giải pháp**: Check **both** role AND subscription/level
**Áp dụng**: Mọi API trả nội dung theo cấp độ

---

### LESSON-004: Payment phải Idempotent
**Sai lầm**: Webhook retry → duplicate VIP upgrade
**Giải pháp**: Idempotency key + full logging
**Áp dụng**: PaymentService, webhook handler

---

### LESSON-005: Quiz câu hỏi phải Lock
**Sai lầm**: Sửa câu hỏi đang thi → điểm không nhất quán
**Giải pháp**: Khi có attempt → lock, tạo version mới nếu sửa
**Áp dụng**: QuizService, ExamService

---

### LESSON-006: AI không được Silent Fail
**Sai lầm**: AI timeout → empty response → học viên không biết
**Giải pháp**: Timeout + retry (3x) + fallback + full logging
**Áp dụng**: OcrService, SpeechService

---

## ANTI-PATTERNS

### Code Anti-Patterns

| Anti-Pattern | Mô tả | Tránh bằng cách |
|--------------|-------|-----------------|
| **God Class** | 1 class quá nhiều trách nhiệm | Max 300 lines/file, 40 lines/method |
| **Anemic Domain** | Entity chỉ có getter/setter | Logic vào domain methods khi phù hợp |
| **Magic Numbers** | Hard-code không giải thích | Named constants: `MAX_RETRY = 3` |
| **Dead Code** | Code không bao giờ gọi | Xóa ngay, dùng Git recover |
| **Circular Dependencies** | A → B → C → A | Interface + DI |
| **Premature Optimization** | Tối ưu sớm | YAGNI, đo trước khi tối ưu |

---

### Database Anti-Patterns

| Anti-Pattern | Tránh bằng cách |
|--------------|-----------------|
| **N+1 Query** | `JOIN FETCH`, `@EntityGraph` |
| **Missing Indexes** | Thêm index cho FK và filter columns |
| **Hard Delete** | Soft Delete (`is_deleted`, `status = INACTIVE`) |
| **BLOB in DB** | File tại `/uploads` hoặc S3 |
| **No Migration** | Flyway/Liquibase cho mọi schema change |
| **No Audit Columns** | Mọi table quan trọng có `created_at`, `updated_at`, `created_by` |

---

### Spring Boot Anti-Patterns

| Anti-Pattern | Tránh bằng cách |
|--------------|-----------------|
| **God Controller** | Controller chỉ nhận request, gọi Service, trả response |
| **Anemic Service** | Logic ở Service, không chỉ delegate |
| **Entity in API** | Map sang DTO trước khi trả |
| **@Transactional abuse** | Đặt tại Service, hiểu propagation/isolation |
| **Swallowed Exceptions** | Log + wrap thành custom exception |
| **No Input Validation** | `@Valid` + Jakarta annotations |
| **Hardcoded Config** | `application.yml` + environment variables |

---

### React Anti-Patterns

| Anti-Pattern | Tránh bằng cách |
|--------------|-----------------|
| **Prop Drilling** | Context hoặc Zustand store |
| **Memory Leaks** | Cleanup trong `useEffect` return |
| **God Component** | Tách sub-components nhỏ |
| **Inline Functions in JSX** | Định nghĩa ngoài JSX, `useCallback` nếu cần |
| **Mixed Role UI** | Tách page/component theo role |
| **Direct API in Component** | Tách vào `services/` folder |
| **No Loading/Error State** | Luôn có `isLoading`, `error` |

---

### Integration & AI Anti-Patterns

| Anti-Pattern | Tránh bằng cách |
|--------------|-----------------|
| **Silent AI Fail** | Timeout + retry (3x) + fallback + log |
| **No Idempotency** | Idempotency key + check trước process |
| **Sync AI Calls** | Async, trả job ID, poll kết quả |
| **No Timeout** | Luôn set timeout cho HTTP/AI calls |
| **Chatty APIs** | Batch operations, composite endpoints |

---

### Testing Anti-Patterns

| Anti-Pattern | Sửa bằng cách |
|--------------|---------------|
| **Test coverage only** | Test business rules thực sự quan trọng |
| **No assertion** | Luôn `assertEquals`, `assertTrue` |
| **Shared mutable state** | Mỗi test độc lập, setup/teardown riêng |
| **Slow tests** | Mock dependencies, H2/Testcontainers |
| **Brittle selectors** | `data-testid` attributes |

---

## FILE STRUCTURE

### Backend (Spring Boot)

```
backend/
├── src/main/java/com/jlpt/
│   ├── controller/          # @RestController (nhóm theo feature/role)
│   │   ├── auth/AuthController.java
│   │   ├── student/CourseController.java
│   │   ├── staff/ContentController.java
│   │   └── admin/AdminController.java
│   ├── service/             # Business Logic
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── QuizService.java
│   │   ├── PaymentService.java
│   │   └── OcrService.java
│   ├── repository/          # Spring Data JPA
│   ├── entity/              # JPA Entities
│   ├── dto/
│   │   ├── request/        # *Request.java
│   │   └── response/       # *Response.java
│   ├── security/           # JWT, Security Config
│   ├── exception/          # Global Exception Handler
│   └── config/             # App Config
├── src/main/resources/
│   ├── db/migration/        # Flyway migrations
│   └── application.yml
└── src/test/java/          # Tests
```

### Frontend (React)

```
frontend/
├── src/
│   ├── components/          # Reusable (PascalCase)
│   ├── pages/               # Pages (PascalCase, nhóm theo role)
│   ├── hooks/               # Custom Hooks (camelCase)
│   ├── services/            # API calls
│   ├── stores/              # State (Zustand/Redux)
│   ├── types/               # TypeScript types
│   └── utils/               # Utilities
└── public/
```

---

## DEVELOPMENT WORKFLOW

```
┌─────────────────────────────────────────────────────────┐
│  specify → plan → implement → test → review → deploy     │
│  Define     Plan     Build       Verify   Review         │
└─────────────────────────────────────────────────────────┘
```

### Phase Commands

| Phase | Command |
|-------|---------|
| **Build** | `mvn clean install` / `npm run build` |
| **Test** | `mvn test` / `npm run test` |
| **Lint** | `mvn spotless:apply` / `npm run lint` |

---

## DOMAIN MODEL

### Core Entities

```
User (is_deleted, role: STUDENT/STAFF/ADMIN)
├── Student ──── UserProgress, QuizAttempt
│               ├── jlpt_level (N5-N1)
│               └── subscription (FREE/VIP)
├── Staff  ──── Assigned Content, Grading
└── Admin  ──── System config, User mgmt

Course (jlpt_level, is_deleted, is_vip_only)
└── Lesson (type, order_index)
    └── Content (text, audio_path, image_path)

Quiz (is_deleted, type)
└── Question (is_locked_after_attempt)
    └── Answer (is_correct)

FlashCard (jlpt_level)
└── UserFlashcardProgress (Spaced Repetition)

ExamAttempt (student_id, exam_id, score, timestamps)

Payment (user_id, amount, status, idempotency_key)
```

---

## NAMING QUICK REFERENCE

> Xem đầy đủ: `AGENTS.md § 3`

| Type | Convention |
|------|-----------|
| Java Class | PascalCase |
| Java Method | camelCase |
| Java Constant | UPPER_SNAKE_CASE |
| DB Table/Column | snake_case |
| API Path | kebab-case, plural |
| React Component | PascalCase.tsx |
| React Hook | camelCase.ts |

---

## ADR STATUS TABLE

| ADR | Title | Status | Last Reviewed |
|-----|-------|--------|----------------|
| ADR-001 | Spring Boot + Java 21 | ✅ Active | 2026-05-27 |
| ADR-002 | Monolithic Architecture | ✅ Active | 2026-05-27 |
| ADR-003 | JWT + bcrypt + 2FA | ✅ Active | 2026-05-27 |
| ADR-004 | Soft Delete | ✅ Active | 2026-05-27 |
| ADR-005 | DTO Pattern | ✅ Active | 2026-05-27 |
| ADR-006 | File Media Storage | ✅ Active | 2026-05-27 |
| ADR-007 | OCR Similarity Only | ✅ Active | 2026-05-27 |
| ADR-008 | Global Exception Handler | ✅ Active | 2026-05-27 |

---

<!-- CLAUDE.md v2.0 — Refactored to eliminate overlap with CONSTITUTION.md and AGENTS.md -->
