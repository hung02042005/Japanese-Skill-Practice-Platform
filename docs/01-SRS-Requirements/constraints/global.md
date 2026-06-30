# Constraints — Global (Technical)

> **Phạm vi**: Ràng buộc kỹ thuật toàn hệ thống cho dự án JLPT E-Learning.
> Đây là các ràng buộc cứng về stack, môi trường, convention và hiệu năng —
> mọi spec, code, test phải tuân thủ.
> Liên quan: [`business.md`](./business.md) (ràng buộc nghiệp vụ), [`safety.md`](./safety.md) (ràng buộc an toàn vận hành), [`shared_context.md`](../shared_context.md).

---

## 1. Tech Stack (Bắt buộc)

| Layer | Công nghệ | Phiên bản | Ghi chú |
|-------|-----------|-----------|---------|
| **Backend** | Java + Spring Boot | Java 21 / Spring Boot 3.x | ADR-001 |
| **Frontend** | React | 18.x | SPA, Vite build |
| **Database** | Microsoft SQL Server | 2022 | Container: `mcr.microsoft.com/mssql/server:2022-latest` |
| **Cache** | Redis | Alpine (latest stable) | Subscription cache, session |
| **Auth** | JWT + bcrypt | bcrypt cost **≥ 10** (chuẩn: 12) | ADR-003 |
| **Migration** | Flyway | Theo Spring Boot 3.x | Mọi schema change phải có migration |
| **Build Backend** | Maven | 3.x | `mvn clean install` |
| **Build Frontend** | npm / Vite | npm ≥ 9 | `npm run build` |
| **Container** | Docker / Docker Compose | 3.8 | `docker-compose.yml` tại root |
| **Logging** | SLF4J (Logback) | Theo Spring Boot 3.x | **KHÔNG** `System.out.println()` |

> ⚠️ **Cấm thay thế tech stack trên** mà không có ADR mới được duyệt.

---

## 2. Ràng buộc Kiến trúc

| ID | Rule | Rationale |
|----|------|-----------|
| GLOB-ARCH-01 | Kiến trúc: `Controller → Service → Repository → Entity` — không bỏ qua layer | Separation of concerns |
| GLOB-ARCH-02 | Controller chỉ nhận/trả **DTO** — Entity không được trả trực tiếp ra API (ADR-005) | Tránh information leakage |
| GLOB-ARCH-03 | Toàn bộ business logic tập trung ở **Service layer** — Frontend là untrusted client | Bảo mật + Consistency |
| GLOB-ARCH-04 | Mọi exception đi qua `@ControllerAdvice` Global Exception Handler (ADR-008) | Consistent error response |
| GLOB-ARCH-05 | API response format chuẩn: `{ "status": <int>, "message": "...", "data": ... }` | Dễ tích hợp Frontend |
| GLOB-ARCH-06 | Monolithic (ADR-002) — không tự ý tách microservice | Team nhỏ, dev/debug dễ |

---

## 3. Ràng buộc API

| ID | Rule | Ví dụ |
|----|------|-------|
| GLOB-API-01 | Prefix tất cả REST endpoint bằng `/api/` | `/api/courses`, `/api/quizzes` |
| GLOB-API-02 | Resource name dạng **kebab-case, số nhiều** | `/api/quiz-attempts`, `/api/mock-exams` |
| GLOB-API-03 | Sub-resource dạng nested | `/api/courses/{id}/lessons` |
| GLOB-API-04 | HTTP Status codes đúng chuẩn (xem bảng bên dưới) | `201` cho POST tạo mới |
| GLOB-API-05 | Backend chạy tại port **8080**; Frontend chạy tại port **80** (Docker) hoặc **5173** (dev) | `docker-compose.yml` |

### HTTP Status Code chuẩn

| Code | Usage |
|------|-------|
| 200 | Success (GET, PUT) |
| 201 | Created (POST) |
| 400 | Bad Request (validation failed) |
| 401 | Unauthorized (missing/invalid JWT) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 409 | Conflict (duplicate resource) |
| 422 | Unprocessable Entity (business rule violation) |
| 500 | Internal Server Error |

---

## 4. Ràng buộc Bảo mật

| ID | Rule | Rationale |
|----|------|-----------|
| GLOB-SEC-01 | Mọi API (trừ `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/forgot-password`) phải đi qua JWT filter | Ngăn truy cập trái phép |
| GLOB-SEC-02 | JWT secret phải đặt trong environment variable — **cấm hardcode** trong source | Tránh lộ key |
| GLOB-SEC-03 | bcrypt cost **≥ 10** (chuẩn dự án: **12**) | Chống brute-force |
| GLOB-SEC-04 | Input validation bằng `@Valid` + Jakarta Bean Validation annotations ở Controller | Không trust client input |
| GLOB-SEC-05 | **Không lưu secrets/passwords/API keys** trong source control | Bảo mật |
| GLOB-SEC-06 | File `.env`, `application-prod.yml` không được đọc/commit vào Git | `.gitignore` bắt buộc |
| GLOB-SEC-07 | HTTPS bắt buộc ở môi trường Production (TLS termination tại reverse proxy) | Mã hóa transit |

---

## 5. Ràng buộc Database

| ID | Rule | Rationale |
|----|------|-----------|
| GLOB-DB-01 | Database: **Microsoft SQL Server 2022** — không dùng DB khác | ADR-001 |
| GLOB-DB-02 | Mọi thay đổi schema phải có **Flyway migration** — cấm sửa schema thủ công trên DB | Reproducible deployments |
| GLOB-DB-03 | Table name và column name dạng **snake_case** | `user_courses`, `created_at` |
| GLOB-DB-04 | Mọi bảng nghiệp vụ quan trọng phải có `created_at`, `updated_at`, `created_by` | Audit trail |
| GLOB-DB-05 | **Soft Delete** toàn hệ thống — cấm `DELETE FROM` trực tiếp (ADR-004) | Bảo toàn dữ liệu |
| GLOB-DB-06 | Không lưu file media (ảnh/audio) dưới dạng BLOB trong DB (ADR-006) | Tránh phình DB |
| GLOB-DB-07 | File media lưu tại `/app/uploads` (dev) hoặc S3 (prod) — lưu path/URL vào DB | ADR-006 |
| GLOB-DB-08 | Cấm `SELECT *` trong query phức tạp | Performance + Security |

---

## 6. Ràng buộc Naming Convention

### 6.1. Java Backend

| Loại | Convention | Ví dụ |
|------|------------|-------|
| Class / Interface | PascalCase | `UserService.java`, `CourseRepository.java` |
| Package | lowercase | `com.jlpt.service`, `com.jlpt.dto` |
| Method / Variable | camelCase | `findByUserId()`, `userId` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `JWT_EXPIRY_HOURS` |
| Enum value | UPPER_SNAKE_CASE | `Role.STUDENT`, `Status.ACTIVE` |
| DB Table | snake_case | `user_courses`, `quiz_attempts` |
| DB Column | snake_case | `created_at`, `is_deleted`, `jlpt_level` |
| DTO Request | PascalCase + `Request` suffix | `LoginRequest`, `CreateCourseRequest` |
| DTO Response | PascalCase + `Response` suffix | `UserResponse`, `CourseDetailResponse` |

### 6.2. React Frontend

| Loại | Convention | Ví dụ |
|------|------------|-------|
| Component | PascalCase + `.jsx` | `KanjiFlashcard.jsx`, `ExamTimer.jsx` |
| Custom Hook | camelCase + `use` prefix + `.js` | `useAuth.js`, `useFlashcard.js` |
| Utility | camelCase + `.js` | `formatDate.js`, `calculateScore.js` |
| Redux Slice | camelCase + `Slice` | `authSlice.js`, `staffGrammarSlice.js` |

### 6.3. API Routes

| Type | Convention | Ví dụ |
|------|------------|-------|
| Base path | `/api/` prefix | `/api/courses` |
| Resource | kebab-case, plural | `/api/quiz-attempts` |
| Sub-resource | nested | `/api/courses/{id}/lessons` |

---

## 7. Ràng buộc Logging

| ID | Rule |
|----|------|
| GLOB-LOG-01 | **BẮT BUỘC** dùng SLF4J Logger (`@Slf4j` annotation) — **KHÔNG BAO GIỜ** `System.out.println()` hoặc `console.log()` ở backend |
| GLOB-LOG-02 | Log format: `[LEVEL] timestamp [class] message {context}` |
| GLOB-LOG-03 | Lỗi AI (timeout, retry exhaust) phải log đầy đủ với context (`jobId`, `userId`, `error`) |
| GLOB-LOG-04 | Không log sensitive data (password, JWT token, PII) |

---

## 8. Ràng buộc Hiệu năng

| ID | Rule | Giá trị |
|----|------|---------|
| GLOB-PERF-01 | API response time mục tiêu (95th percentile) | **≤ 500ms** cho CRUD thông thường |
| GLOB-PERF-02 | API response time mục tiêu cho AI endpoints | **≤ 2s** cho submit (trả `job_id` ngay) |
| GLOB-PERF-03 | Subscription VIP check cache TTL | Tối đa **5 phút** (Redis) |
| GLOB-PERF-04 | AI call timeout | **30 giây** per attempt |
| GLOB-PERF-05 | AI retry tối đa | **3 lần** với exponential backoff |
| GLOB-PERF-06 | Tránh N+1 query — dùng `JOIN FETCH` hoặc `@EntityGraph` | Áp dụng toàn bộ JPA queries |

---

## 9. Ràng buộc Môi trường & Triển khai

| ID | Rule |
|----|------|
| GLOB-ENV-01 | Môi trường triển khai qua Docker Compose (`docker-compose.yml` tại root) |
| GLOB-ENV-02 | Backend port: **8080**; Frontend port: **80** (production); DB port: **1433**; Redis port: **6379** |
| GLOB-ENV-03 | Volume `jlpt-uploads` dành riêng cho file upload; `sqlserver_data` cho DB data |
| GLOB-ENV-04 | **Cấm commit trực tiếp** vào branch `main` hoặc `production` |
| GLOB-ENV-05 | Branch naming: `feat/*`, `fix/*`, `spec/*`, `chore/*` |
| GLOB-ENV-06 | Schema change phải có Flyway migration — cấm `spring.jpa.hibernate.ddl-auto=create/update` ở production |

---

## 10. Ràng buộc Code Quality

| ID | Rule | Ngưỡng |
|----|------|--------|
| GLOB-QUAL-01 | Unit test coverage | **≥ 80%** cho Service layer |
| GLOB-QUAL-02 | Integration test | Happy path + error path cho mọi API endpoint |
| GLOB-QUAL-03 | Không có linting/type errors trước khi merge | `mvn spotless:apply` / `npm run lint` |
| GLOB-QUAL-04 | API documented trong OpenAPI/Swagger | Bắt buộc cho mọi endpoint |
| GLOB-QUAL-05 | **Cấm TODO comments** trong code đã merge vào `main` | Zero tolerance |
| GLOB-QUAL-06 | Method tối đa **40 dòng**; class tối đa **300 dòng** | Tránh God Class/Method |

---

## Tham chiếu

| Nguồn | Nội dung liên quan |
|-------|---------------------|
| `CLAUDE.md` | ADR-001 → ADR-008, kiến trúc hệ thống, file structure |
| `AGENTS.md § 3` | Naming conventions chi tiết |
| `AGENTS.md § 5` | Forbidden patterns |
| `AGENTS.md § 10` | Definition of Done checklist |
| `docker-compose.yml` | Cấu hình container, port, volume |
| `constraints/business.md` | Ràng buộc nghiệp vụ (JLPT domain rules) |
| `constraints/safety.md` | Ràng buộc an toàn vận hành |
