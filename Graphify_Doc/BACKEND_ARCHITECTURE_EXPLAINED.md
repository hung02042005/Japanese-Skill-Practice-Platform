# Giải thích kiến trúc Backend bằng Graphify

## 1. Kết luận tổng quan

Backend của Japanese Skill Practice Platform là một **modular monolith** được xây dựng bằng **Spring Boot 3.x và Java 21**.

- **Monolith**: toàn bộ backend được build và triển khai như một ứng dụng Spring Boot.
- **Modular**: mã nguồn được chia theo từng feature/nghiệp vụ như xác thực, học tập, bài thi, flashcard và quản lý nội dung.
- **Layered architecture**: bên trong mỗi feature, luồng xử lý vẫn tuân theo các lớp `Controller -> Service -> Repository -> Entity`.

Do đó, kiến trúc thực tế là sự kết hợp giữa **package-by-feature** và **layered architecture**.

```text
React Frontend
      |
      | REST API + JSON + JWT
      v
+------------------------------------------------------+
| Spring Boot Modular Monolith                         |
|                                                      |
| feature/                                             |
| |-- auth             Xác thực và tài khoản           |
| |-- student          Học tập và tiến trình           |
| |-- assessment       Quiz, bài thi và chấm điểm      |
| |-- staffcontent     Biên soạn nội dung              |
| |-- contentreview    Kiểm duyệt nội dung             |
| |-- publishedcontent Nội dung đã xuất bản            |
| |-- flashcard        Flashcard và SRS                 |
| |-- dictionary       Tra cứu từ điển                 |
| |-- notification     Thông báo                       |
| `-- support          Ticket hỗ trợ                   |
|                                                      |
| shared/                                              |
| |-- security         JWT và Spring Security          |
| |-- exception        Xử lý lỗi tập trung             |
| |-- common           Response dùng chung             |
| |-- config           Cấu hình hệ thống               |
| `-- email            SMTP và email outbox            |
+-------------------------+----------------------------+
                          |
                          v
              SQL Server / uploads / SMTP
```

## 2. Luồng xử lý một request

```text
HTTP Request
    |
    v
Controller
    |  Nhận request, kiểm tra @Valid, chuyển đổi DTO
    v
Service
    |  Xử lý nghiệp vụ, authorization, transaction, audit
    v
Repository
    |  Truy vấn dữ liệu bằng Spring Data JPA/JPQL
    v
Entity
    |  Ánh xạ bảng và quan hệ trong SQL Server
    v
HTTP Response dạng ApiResponse<DTO>
```

### Controller layer

Controller chịu trách nhiệm:

- Nhận HTTP request.
- Kiểm tra dữ liệu đầu vào bằng Jakarta Validation.
- Gọi service tương ứng.
- Trả về DTO và HTTP status phù hợp.

Controller không nên chứa logic tính điểm, kiểm tra subscription hoặc thao tác dữ liệu trực tiếp.

Ví dụ: `apps/backend/src/main/java/com/jlpt/feature/auth/AuthController.java`.

### Service layer

Service là nơi đặt:

- Business logic.
- Transaction với `@Transactional`.
- Kiểm tra role, subscription và quyền truy cập.
- Tính điểm, cập nhật tiến trình và audit log.
- Điều phối email, lưu file hoặc các tác vụ bất đồng bộ.

Các service tiêu biểu:

- `AuthenticationService`: đăng nhập, token và phiên xác thực.
- `QuizService`: xử lý quiz và kết quả.
- `MockExamService`: xử lý bài thi thử.
- `StudentLearningProgressService`: quản lý tiến trình học.
- `ContentReviewService`: điều phối kiểm duyệt nội dung.

### Repository layer

Repository sử dụng Spring Data JPA để:

- Đọc và ghi entity.
- Thực thi derived query, JPQL hoặc `@Query`.
- Giữ chi tiết truy cập dữ liệu khỏi service.

Repository không nên chứa business rule hoặc trả entity trực tiếp ra API.

### Entity layer

Entity ánh xạ dữ liệu trong SQL Server. Những quy tắc quan trọng gồm:

- Không hard delete các bản ghi nghiệp vụ quan trọng.
- Dùng soft delete hoặc trạng thái như `INACTIVE`/`CANCELLED`.
- Có thông tin audit như thời gian tạo, cập nhật và người thực hiện khi cần.
- Kết quả bài thi đã nộp phải bất biến.

## 3. Các module nghiệp vụ chính

### Auth và User

Module `feature/auth` xử lý:

- Đăng ký và đăng nhập.
- JWT access/refresh token.
- Reset mật khẩu.
- Xác minh tài khoản.
- Hồ sơ học viên.

Security được dùng chung qua `shared/security` và `shared/config/SecurityConfig.java`.

### Student và Learning

Các module `feature/student` và `feature/learning` phụ trách:

- Bài học theo cấp độ JLPT.
- Kana, Kanji, ngữ pháp, đọc hiểu và từ vựng.
- Theo dõi tiến trình học.
- Mở khóa nội dung theo thứ tự.
- Ghi nhận hoạt động học tập.

Business rule quan trọng là tiến trình chỉ được tăng hợp lệ và phải được xác minh ở backend.

### Assessment

Module `feature/assessment` phụ trách:

- Quiz.
- Mock exam.
- Câu hỏi và assignment.
- Attempt và answer.
- Tính điểm server-side.

Luồng nộp bài chuẩn:

```text
Student gửi answers
    -> Controller nhận request
    -> Service kiểm tra thời gian và quyền truy cập
    -> Service tự tính điểm
    -> Tạo attempt mới
    -> Lưu answer và audit
    -> Trả kết quả dạng DTO
```

Frontend không được gửi một giá trị `score` để backend tin tưởng và lưu trực tiếp.

### Vòng đời nội dung

Hệ thống tách luồng quản lý nội dung thành ba khu vực:

```text
staffcontent -> contentreview -> publishedcontent
     |                |                 |
  biên soạn        kiểm duyệt        xuất bản
```

Cách tách này giúp phân biệt rõ nội dung đang chỉnh sửa, nội dung chờ duyệt và nội dung người học được phép sử dụng.

### Flashcard, Dictionary, Notification và Support

- `flashcard`: quản lý deck, flashcard và spaced repetition.
- `dictionary`: cung cấp chức năng tra cứu.
- `notification`: thông báo trong hệ thống.
- `support`: ticket và phản hồi hỗ trợ.

## 4. Security architecture

Backend dùng Spring Security theo mô hình stateless:

```text
Request
  -> CORS/Security filter chain
  -> JwtAuthenticationFilter
  -> Xác minh token
  -> Nạp UserDetails
  -> Kiểm tra role/quyền
  -> Controller
```

Các quyết định chính:

- JWT được kiểm tra trước `UsernamePasswordAuthenticationFilter`.
- Session policy là `STATELESS`.
- Các route `/api/admin/**`, `/api/staff/**` và `/api/manager/**` được phân quyền riêng.
- Mật khẩu dùng BCrypt với cost 12.
- Authorization nghiệp vụ phải kiểm tra cả role và subscription/level khi áp dụng.
- Frontend chỉ điều chỉnh giao diện; backend mới là nơi quyết định quyền thật sự.

## 5. Hạ tầng và tích hợp

Backend sử dụng:

- SQL Server làm cơ sở dữ liệu chính.
- Spring Data JPA/Hibernate cho persistence.
- Flyway cho database migration.
- SMTP/Spring Mail cho email.
- `/uploads` hoặc S3 cho file media.
- Spring Boot Actuator cho health/monitoring.
- Global exception handler để chuẩn hóa lỗi API.

Response API được chuẩn hóa theo dạng:

```json
{
  "status": 200,
  "message": "Operation successful",
  "data": {}
}
```

## 6. Điểm mạnh của kiến trúc

1. **Tổ chức theo feature** giúp tìm và thay đổi một nghiệp vụ dễ hơn so với gom toàn bộ controller/service/repository vào các thư mục toàn cục.
2. **Service giữ transaction và business logic** tạo ranh giới rõ giữa HTTP, nghiệp vụ và persistence.
3. **Security dùng chung nhưng authorization vẫn có thể đặt tại service**, phù hợp với các rule phức tạp như role kết hợp subscription.
4. **Vòng đời nội dung được tách riêng**, hỗ trợ workflow Staff -> Review -> Publish.
5. **DTO bắt buộc** hạn chế lộ entity và cấu trúc database qua API.

## 7. Rủi ro cần kiểm soát

- Không để `shared` trở thành một god module chứa business logic của nhiều feature.
- Không để controller gọi repository trực tiếp.
- Không tạo dependency vòng giữa các feature.
- Không để frontend tính điểm hoặc tự quyết định authorization.
- Không dùng entity làm request/response API.
- Không cập nhật attempt đã submit; mỗi lần nộp phải tạo bản ghi mới.
- Không thay đổi schema nếu thiếu Flyway migration.
- Các lời gọi AI hoặc dịch vụ ngoài phải có timeout, retry và fallback.

## 8. Kết quả Graphify và giới hạn hiện tại

Graphify snapshot được dùng cho tài liệu này cho thấy:

- 168 nodes.
- 159 edges.
- 18 communities.
- Không phát hiện import cycle trong corpus đã index.
- Các hub chính gồm kiến trúc hệ thống, ADR, backend, domain rules và anti-patterns.

Tuy nhiên snapshot chỉ index **4 file tài liệu**, chưa bao phủ đầy đủ source code backend. Nó được tạo từ commit `2e201bc5`, trong khi tại thời điểm phân tích HEAD là `d78286bf`.

Vì vậy:

- Phần kiến trúc cấp cao có thể dùng làm tài liệu định hướng.
- Kết luận về dependency giữa class, call graph hoặc import cycle chưa nên xem là đầy đủ.
- Nên rebuild Graphify với toàn bộ `apps/backend/src` để có graph chính xác hơn.

Lệnh code-only, không cần API key:

```powershell
graphify extract . --code-only
```

Sau khi graph tồn tại, có thể truy vấn:

```powershell
graphify query "backend architecture controller service repository entity security"
graphify explain "AuthenticationService"
graphify path "AuthController" "AuthTokenRepository"
```

## 9. Tài liệu và source liên quan

- `CLAUDE.md`: kiến trúc tổng quan, module, ADR và flow.
- `AGENTS.md`: domain rules, golden patterns và definition of done.
- `apps/backend/pom.xml`: Java version và dependency backend.
- `apps/backend/src/main/java/com/jlpt/shared/config/SecurityConfig.java`: security filter chain.
- `apps/backend/src/main/java/com/jlpt/shared/security/JwtAuthenticationFilter.java`: xác thực JWT.
- `apps/backend/src/main/java/com/jlpt/shared/exception/GlobalExceptionHandler.java`: chuẩn hóa lỗi.
- `apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java`: response envelope.

