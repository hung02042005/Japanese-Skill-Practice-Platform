# AGENTS.md — Dự án: Hệ Thống Học Tiếng Nhật JLPT
# Phiên bản: 2.0.0 | Cập nhật: 2026-05-27 | Tác giả: [Team]

## 1. PROJECT OVERVIEW

**Tên dự án:** Hệ Thống Học Tiếng Nhật JLPT
**Loại:** Full-stack Web Application + REST API
**Domain:** E-Learning / Ngôn ngữ học / Luyện thi JLPT
**Giai đoạn:** Development

> **Tech Stack**: Java 21 + Spring Boot 3.x (Backend) | React 18 (Frontend) | SQL Server
> Xem chi tiết: `CONSTITUTION.md § ĐIỀU 1`

**Mục tiêu chính**: Xây dựng hệ thống học tiếng Nhật hỗ trợ lộ trình từ N5 đến N1 với các tính năng chuyên sâu: Kanji, Kana, Ngữ pháp, Từ vựng, và luyện tập AI (OCR & Speech Recognition); đảm bảo lộ trình học, tính điểm, phân quyền, và trải nghiệm người dùng được thực thi chính xác và có audit trail đầy đủ.

**Đọc theo thứ tự**:
1. `CONSTITUTION.md` — Tech stack, Security, Code Standards, Git Workflow
2. `CLAUDE.md` — Kiến trúc hệ thống, ADR, Lessons Learned, Anti-patterns
3. File này — Domain Rules, Forbidden Patterns, Golden Patterns
4. `/docs/use-cases/` — Use Cases theo vai trò

---

## 2. ARCHITECTURE PRINCIPLES

> **Lớp kiến trúc**: `Controller → Service → Repository → Entity`
> Xem chi tiết sơ đồ: `CLAUDE.md § KIẾN TRÚC HỆ THỐNG`

### 2.1. API Design
- REST style với prefix `/api/[resource]`
- Luôn trả JSON chuẩn: `{ "status": 200, "message": "...", "data": ... }`
- Xử lý lỗi tập trung qua `@ControllerAdvice`

### 2.2. DTO Pattern (BẮT BUỘC)
```
Entity (JPA)  ──mapping──►  DTO (Request/Response)  ──►  API
       │                          ▲
       └──  Service Layer ◄──────┘
```

### 2.3. Logging
- **MUST** dùng SLF4J Logger — **KHÔNG BAO GIỜ** `System.out.println()` / `console.log()`
- Log format: `[LEVEL] timestamp [class] message {context}`

### 2.4. Frontend / Backend Separation (BẮT BUỘC)

> ⚠️ Vi phạm nguyên tắc này là lỗi nghiêm trọng — mọi logic quan trọng phải nằm ở backend.

#### Backend chịu trách nhiệm TOÀN BỘ:
- **Business logic**: tính điểm, kiểm tra điều kiện mở khóa bài, xử lý tiến trình học
- **Authorization**: kiểm tra Role + Subscription trước khi trả data
- **Validation**: validate input, business rule (score range, level access, v.v.)
- **State quan trọng**: thời gian làm bài, kết quả thi, trạng thái subscription

#### Frontend CHỈ được phép:
- Render/hiển thị data nhận từ API
- Gọi API và xử lý response (loading, error state)
- Validation UX cục bộ (format email, required field) — **KHÔNG thay thế** backend validation
- Quản lý UI state (modal open/close, tab active, v.v.)

#### Cấm tuyệt đối ở Frontend:
| ❌ Không được | ✅ Thay bằng |
|--------------|-------------|
| Tính điểm quiz ở client | Gửi answers lên API, nhận score về |
| Kiểm tra quyền truy cập bằng JS | Backend trả 403, frontend chỉ handle lỗi |
| Lưu kết quả thi vào localStorage | Backend lưu DB, frontend chỉ hiển thị |
| Validate business rule (score range, level) | Backend validate, trả error message |
| Trust dữ liệu từ client không qua backend | Luôn verify lại ở Service layer |

---

## 3. NAMING CONVENTIONS

### 3.1. Java Backend

| Loại | Convention | Ví dụ |
|------|------------|-------|
| Class / Interface | PascalCase | `UserService.java`, `CourseRepository.java` |
| Package | lowercase | `com.jlpt.service`, `com.jlpt.dto` |
| Method / Variable | camelCase | `findByUserId()`, `userId` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `JWT_EXPIRY_HOURS` |
| Enum value | UPPER_SNAKE_CASE | `Role.STUDENT`, `Status.ACTIVE` |
| DB Table | snake_case | `user_courses`, `quiz_attempts` |
| DB Column | snake_case | `created_at`, `is_deleted`, `jlpt_level` |
| DTO Request | PascalCase + Request | `LoginRequest`, `CreateCourseRequest` |
| DTO Response | PascalCase + Response | `UserResponse`, `CourseDetailResponse` |

### 3.2. React Frontend

| Loại | Convention | Ví dụ |
|------|------------|-------|
| Component | PascalCase + `.jsx` | `KanjiFlashcard.jsx`, `ExamTimer.jsx` |
| Hook | camelCase + `use` prefix | `useAuth.js`, `useFlashcard.js` |
| Utility | camelCase | `formatDate.js`, `calculateScore.js` |

### 3.3. API Routes

| Type | Convention | Ví dụ |
|------|------------|-------|
| Base path | `/api/` prefix | `/api/courses` |
| Resource | kebab-case, plural | `/api/quiz-attempts` |
| Sub-resource | nested | `/api/courses/{id}/lessons` |

---

## 4. PHẠM VI HOẠT ĐỘNG

### ✅ Được phép

- Đọc, hiểu và chỉnh sửa source code trong các thư mục dự án
- Đọc và tham khảo kỹ các tài liệu Use Case (`/docs/use-cases/`)
- Chạy các lệnh build, test: `mvn test`, `npm run test`, `npm run build`
- Tạo branch theo pattern: `feat/*`, `fix/*`, `spec/*`, `chore/*`

### ❌ Cấm tuyệt đối

- **KHÔNG** viết chung logic Admin / Staff / Student — phân quyền nghiêm ngặt
- **KHÔNG** Hard Delete — luôn Soft Delete (`is_deleted = true`)
- **KHÔNG** bypass JWT + Spring Security
- **KHÔNG** đọc/thay đổi `.env`, `application-prod.yml`, secrets
- **KHÔNG** thay đổi schema DB mà không có Flyway/Liquibase migration
- **KHÔNG** commit trực tiếp vào `main` hoặc `production`

---

## 5. FORBIDDEN PATTERNS

| # | Rule | Lý do |
|---|------|-------|
| 1 | **NEVER** lưu secrets/passwords/API keys trong source control | Bảo mật |
| 2 | **NEVER** dùng `SELECT *` trong query phức tạp | Performance + Security |
| 3 | **NEVER** cập nhật điểm số/tiến trình trực tiếp vào DB | Invariant violation |
| 4 | **NEVER** cho phép `score < 0` hoặc `score > max_score` | Domain integrity |
| 5 | **NEVER** lẫn lộn dữ liệu JLPT levels (N1-N5) | Wrong level access |
| 6 | **NEVER** cấp quyền VIP mà không kiểm tra subscription | Revenue leak |
| 7 | **NEVER** gọi AI model mà không có timeout + retry + fallback | System stability |
| 8 | **NEVER** để TODO comments trong code đã merge | Technical debt |
| 9 | **NEVER** trả Entity/JPA trực tiếp ra API | Information leakage |
| 10 | **NEVER** bypass validation annotations | Security vulnerability |
| 11 | **NEVER** đặt business logic (tính điểm, kiểm tra quyền, validate nghiệp vụ) ở Frontend | Frontend là untrusted client |
| 12 | **NEVER** trust dữ liệu từ client mà không validate lại ở backend | Security vulnerability |

---

## 6. API RESPONSE FORMAT

### 6.1. Success Response

```json
{
  "status": 200,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "name": "Course A"
  }
}
```

### 6.2. Error Response

```json
{
  "status": 400,
  "message": "Validation failed",
  "data": {
    "field": "email",
    "error": "Must be a valid email address"
  }
}
```

### 6.3. Standard HTTP Status Codes

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

## 7. JLPT DOMAIN RULES

> ⚠️ **ĐÂY LÀ LUẬT NGHIỆP VỤ SỐNG CÒN** — AI agent phải đảm bảo các invariants này không bao giờ bị vi phạm.

### 7.1. Luật Điểm số & Bài thi

| # | Rule | Implementation |
|---|------|----------------|
| 1 | `quiz_attempt.score` luôn `>= 0` và `<= quiz.max_score` | Service layer validation |
| 2 | Mỗi lần nộp bài tạo **bản ghi MỚI** | `QuizAttemptRepository.save(new Attempt())` |
| 3 | Điểm số chỉ được tính bởi **Service layer** | Client không gửi score |
| 4 | Kết quả đã nộp là **bất biến** | No UPDATE on submitted records |
| 5 | Thời gian làm bài **server-side validation** | Client không khai báo thời gian |
| 6 | Quiz đã có attempt → **lock câu hỏi** | `is_locked = true`, tạo version mới nếu sửa |

### 7.2. Luật Lộ trình học

| # | Rule |
|---|------|
| 1 | Chỉ mở khóa bài tiếp theo khi hoàn thành bài trước (`lesson_order`) |
| 2 | `user_progress` chỉ **tăng**, không giảm thủ công |
| 3 | Mọi hoạt động học tập phải ghi vào `learning_activity_log` |

### 7.3. Luật Subscription & Phân quyền

| # | Rule |
|---|------|
| 1 | `is_vip_only = true` chỉ hiển thị khi `user.subscription = VIP` |
| 2 | Authorization check **CẢ Role VÀ subscription/level** |
| 3 | Subscription hết hạn check **real-time** — cache tối đa **5 phút** |
| 4 | Thay đổi subscription phải có audit log |
| 5 | Admin bắt buộc **2FA (TOTP)** — không bypass |

### 7.4. Luật Soft Delete

| Entity | Soft Delete Method |
|--------|-------------------|
| User, Course, Lesson, Quiz | `is_deleted = true` |
| QuizAttempt, LearningActivityLog | `status = INACTIVE/CANCELLED` |
| Mọi bảng quan trọng | Phải có `created_at`, `updated_at`, `created_by` |

### 7.5. Luật AI Features (OCR & Speech)

| # | Rule |
|---|------|
| 1 | Kết quả AI phải validate trước khi lưu DB |
| 2 | AI score chỉ là `ai_score_suggestion` — Staff có thể override với `final_score` |
| 3 | Lỗi AI phải log đầy đủ + fallback response |
| 4 | AI call: timeout + retry max **3 lần** |
| 5 | AI call phải **async** (trả `job_id` ngay, poll kết quả) |
| 6 | OCR chỉ so sánh **similarity %** — không phân tích stroke order (ADR-007) |
| 7 | File ảnh/audio lưu tại `/uploads` hoặc S3 — **KHÔNG** lưu BLOB |

---

## 8. GOLDEN PATTERNS

### 8.1. DTO Mapping Pattern

```java
// ❌ BAD - Entity exposed directly
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
}

// ✅ GOOD - DTO mapping
@GetMapping("/{id}")
public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ApiResponse.success(UserMapper.toResponse(user));
}

// Mapper (use MapStruct or manual)
@Component
public class UserMapper {
    public static UserResponse toResponse(User entity) {
        return UserResponse.builder()
            .id(entity.getId())
            .email(entity.getEmail())
            .role(entity.getRole().name())
            .build();
    }
}
```

### 8.2. Service Layer with Transaction

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {
    
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final AuditLogService auditLog;
    
    @Transactional
    public QuizAttempt submitQuiz(Long quizId, Long userId, List<AnswerRequest> answers) {
        Quiz quiz = quizRepository.findByIdOrThrow(quizId);
        
        // Validate server-side time
        validateTimeRemaining(quiz);
        
        // Calculate score
        int score = calculateScore(quiz, answers);
        
        // Create NEW record (never update)
        QuizAttempt attempt = QuizAttempt.builder()
            .quiz(quiz)
            .user(userService.findById(userId))
            .score(score)
            .status(AttemptStatus.SUBMITTED)
            .submittedAt(LocalDateTime.now())
            .build();
        
        attempt = attemptRepository.save(attempt);
        
        // Audit log
        auditLog.log("QUIZ_SUBMITTED", userId, 
            Map.of("attemptId", attempt.getId(), "score", score));
        
        return attempt;
    }
}
```

### 8.3. Authorization Check Pattern

```java
@Service
public class CourseService {
    
    public CourseDetailResponse getCourse(Long courseId, User user) {
        Course course = courseRepository.findByIdOrThrow(courseId);
        
        // Check BOTH role AND subscription
        if (course.isVipOnly()) {
            if (!user.hasVipAccess()) {
                throw new ForbiddenException("VIP content requires active subscription");
            }
        }
        
        return CourseMapper.toDetailResponse(course);
    }
}

// Or using @PreAuthorize
@PreAuthorize("hasRole('ADMIN') or (#user.hasVipAccess() and #course.isAccessibleBy(#user))")
public CourseDetailResponse getCourse(Long courseId, User user) { ... }
```

### 8.4. AI Async Pattern with Fallback

```java
@Service
@Slf4j
public class OcrService {
    
    private static final int MAX_RETRIES = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    
    public OcrJobResult submitOcrJob(String imagePath) {
        String jobId = UUID.randomUUID().toString();
        
        // Save file
        String storedPath = fileStorage.store(imagePath);
        
        // Create job record
        OcrJob job = OcrJob.builder()
            .jobId(jobId)
            .imagePath(storedPath)
            .status(JobStatus.PENDING)
            .build();
        ocrJobRepository.save(job);
        
        // Trigger async processing (do not block)
        ocrProcessingQueue.enqueue(jobId);
        
        return OcrJobResult.builder()
            .jobId(jobId)
            .status("PENDING")
            .message("Processing started")
            .build();
    }
    
    public OcrJobResult getJobResult(String jobId) {
        OcrJob job = ocrJobRepository.findByJobId(jobId);
        
        if (job.getStatus() == JobStatus.PENDING) {
            return OcrJobResult.builder()
                .jobId(jobId)
                .status("PROCESSING")
                .build();
        }
        
        return OcrResultMapper.toResult(job);
    }
}
```

### 8.5. Exception Handling Pattern

```java
// Custom Exception
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with id: %d", resource, id));
    }
}

// Global Exception Handler
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ApiResponse.error(404, ex.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return ApiResponse.error(400, "Validation failed", message);
    }
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ApiResponse.error(500, "Internal server error");
    }
}
```

---

## 9. XỬ LÝ LỖI & AN TOÀN THAO TÁC

### 9.1. When to Ask

| Situation | Action |
|-----------|--------|
| UC spec không rõ ràng | Hỏi người dùng trước khi đoán |
| Architecture change needed | Dừng + Ask |
| Unclear business rule | Hỏi người dùng |

### 9.2. High-Risk Operations

Trước khi sửa flow điểm số, tiến trình học, xác thực:
- Đọc code liên quan trong `CLAUDE.md`
- Đọc spec hiện hành
- Kiểm tra module lân cận

### 9.3. Refactor Safeguards

- Thay đổi > 3 files hoặc > 200 dòng → **Backup hoặc xác nhận trước**
- Nếu không thể chạy công cụ phân tích → **Nêu rõ giới hạn trong báo cáo**

---

## 10. DEFINITION OF DONE

Trước khi báo cáo hoàn thành task, tự kiểm tra:

### Code Quality
- [ ] Unit tests viết xong và passing (min 80% coverage)
- [ ] Integration tests cho API endpoints (happy + error path)
- [ ] Không có linting/type errors
- [ ] API document trong OpenAPI/Swagger
- [ ] Error cases xử lý đúng HTTP status codes
- [ ] Không có TODO comments

### Domain Rules
- [ ] Luật điểm số (score >= 0, score <= max_score)
- [ ] Luật question lock (quiz đã thi không sửa được)
- [ ] Luật subscription (VIP check)

### Security & Authorization
- [ ] Phân quyền theo Role VÀ subscription/level
- [ ] Input validation với @Valid / Jakarta annotations
- [ ] Không có secret/key hardcode

### Database
- [ ] Có Flyway/Liquibase migration cho schema changes
- [ ] Soft delete được sử dụng đúng
- [ ] Audit columns có mặt

---

## 11. PROJECT CONTEXT REFERENCES

| File | Nội dung |
|------|----------|
| `CONSTITUTION.md` | Tech stack, Security, Code Standards, Git Workflow, Testing |
| `CLAUDE.md` | Architecture, ADR, Lessons Learned, Anti-patterns, File Structure |
| `/docs/use-cases/uc_detail_student.md` | Student Use Cases |
| `/docs/use-cases/uc_detail_staff.md` | Staff Use Cases |
| `/docs/use-cases/uc_detail_admin.md` | Admin Use Cases |
| `/docs/ADR/` | Architecture Decision Records (ADR-001 đến ADR-008) |

---

<!-- AGENTS.md v2.0 — Refactored to eliminate overlap -->
