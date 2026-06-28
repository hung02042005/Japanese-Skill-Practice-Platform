# PLAN - Authentication & Account Management (`feat-auth`)

## 1. Mục tiêu (Goals)

Triển khai module xác thực (Authentication) cho Hệ thống Học Tiếng Nhật JLPT theo các đặc tả UC-01 đến UC-06 và UC-18. Hệ thống cần đảm bảo an toàn, phân quyền chặt chẽ, quản lý session tốt và tách biệt hoàn toàn giữa backend logic và frontend display theo như đã định nghĩa trong `CONSTITUTION.md` và `AGENTS.md`.

## 2. Kiến trúc & Công nghệ

- **Backend:** Java 21, Spring Boot 3.x, Spring Security, JWT (RS256/HS256).
- **Frontend:** React 18, TypeScript, Tailwind CSS, Zustand/Context API (cho Auth state).
- **Database:** SQL Server (Primary DB), Migration bằng Flyway/Liquibase.
- **Tuân thủ Kiến trúc:** Controller → Service → Repository → Entity. Bắt buộc dùng DTO Pattern. Business logic hoàn toàn đặt ở Backend.

## 3. Các thành phần Backend

### 3.1. Database Migration & Entities

- Viết file migration tạo các bảng: `admin_users`, `staff_users`, `student_users`, `auth_tokens` theo cấu trúc tại `SPEC.md`.
- Tạo JPA Entities với Annotation `@Entity`, map đúng tên bảng và cấu hình quan hệ (ví dụ: `StudentUser` 1-N `AuthToken`).
- Áp dụng cơ chế Soft Delete (`is_deleted` hoặc `status` filter).

### 3.2. Repositories (Spring Data JPA)

- `AdminUserRepository`, `StaffUserRepository`, `StudentUserRepository` (có `findByEmail` và `existsByEmail`).
- `TokenRepository` (tìm kiếm và xoá token bằng `token_value`, `token_type`).

### 3.3. DTOs

- Thực hiện Mapping Pattern qua `MapStruct` hoặc thủ công. Không bao giờ lộ Entity ra API.
- Các requests: `LoginRequest`, `RegisterRequest`, `VerifyEmailRequest`, `ResetPasswordRequest`, `ChangePasswordRequest`, `ProfileUpdateRequest`...
- Cài đặt đầy đủ Validation Annotation (`@Valid`, `@Email`, `@NotBlank`, `@Size`, v.v.).

### 3.4. Services (Nơi chứa Business Logic)

- **`AuthService`**:
  - Đăng ký: Tạo `StudentUser` (`status='pending'`), hash mật khẩu (bcrypt cost >= 10), tạo email_verification token.
  - Đăng nhập: Kiểm tra khóa tài khoản (`locked_until`), trạng thái `suspended`/`pending`, xác thực bcrypt. Xử lý reset số lần đăng nhập sai hoặc tăng bộ đếm/khóa tạm thời.
  - Đổi/Quên mật khẩu: Kiểm tra mật khẩu cũ, tạo password_reset token.
- **`JwtService`**: Tạo/Parse Access Token (15 phút) và Refresh Token (7 ngày).
- **`OAuthService`**: Tạo url authorization, xử lý callback, giao tiếp với Google API để lấy thông tin, tạo hoặc cập nhật tài khoản tương ứng.
- **`EmailService`**: Gửi email kích hoạt, khôi phục mật khẩu (async/không block luồng chính).

### 3.5. Controllers & Security

- Cấu hình **Spring Security**: Không lưu session, cấu hình `JwtAuthenticationFilter`. Cấu hình route công khai (`/api/auth/**`) và route bảo vệ (`/api/students/me`).
- **`AuthController`**: Định tuyến login, register, verify-email, quên/reset password, refresh token, oauth.
- **`StudentController`**: GET/PUT `/api/students/me` và PUT `/api/students/me/password`.
- **`GlobalExceptionHandler`**: Đón và mapping các lỗi validation, `InvalidCredentialsException`, `AccountLockedException`, `EmailExistsException`, v.v. về các mã lỗi HTTP 400, 401, 403, 409, 422, 429 theo quy chuẩn JSON.

## 4. Các thành phần Frontend

### 4.1. Global State & API

- **Auth Store**: Sử dụng Zustand hoặc Context để lưu `accessToken`, `user` profile, `isAuthenticated`.
- **Axios Configuration**: Tạo Axios instance với request interceptor để tự động chèn `Bearer [token]`. Tạo response interceptor để gọi API `/api/auth/refresh` tự động khi nhận lỗi 401.

### 4.2. UI Components & Pages

- Cần xây dựng các màn hình:
  - `Login` & `Register` (Có kiểm tra lỗi form và hiển thị message generic).
  - Khôi phục tài khoản (Gửi email và Form đặt lại).
  - Quản lý tài khoản (Hiển thị Profile, thay đổi thông tin và Đổi mật khẩu).
- Component `ProtectedRoute`: Bảo vệ các route chỉ dành cho Student đã đăng nhập.

## 5. Tiêu chuẩn đánh giá (Definition of Done)

- Code backend không lưu trữ mật khẩu plaintext; bcrypt cost > 10.
- Unit Tests (> 80% coverage) cho Services (đặc biệt là Auth/JWT).
- Integration Tests cho Controller (happy & error paths).
- Error HTTP Response đúng quy chuẩn `status`, `message`, `data`.
- Không cóTODO comments, code pass các bộ linter.
