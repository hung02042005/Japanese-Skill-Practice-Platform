# TASKS - Authentication & Account Management (`feat-auth`)

## Phase 1: Database & Entities

- [ ] 1.1 Khởi tạo script Migration (Flyway/Liquibase) cho schema Auth (`admin_users`, `staff_users`, `student_users`, `auth_tokens`).
- [ ] 1.2 Tạo JPA Entities: `AdminUser`, `StaffUser`, `StudentUser`, `AuthToken`.
- [ ] 1.3 Thiết lập cơ chế Soft Delete (`is_deleted` / `status` = 'deleted') cho các Entity.
- [ ] 1.4 Tạo các Repositories: `AdminUserRepository`, `StaffUserRepository`, `StudentUserRepository`, `TokenRepository`.

## Phase 2: Core Auth & DTOs Framework

- [ ] 2.1 Cấu hình Spring Security: WebSecurityConfig, SecurityFilterChain (stateless), AuthenticationManager.
- [ ] 2.2 Tạo `JwtService`: logic generate token (Access 15m, Refresh 7d) và validate token.
- [ ] 2.3 Cài đặt `JwtAuthenticationFilter` để đọc token từ Header, xác thực và set vào SecurityContext.
- [ ] 2.4 Khởi tạo các DTO Requests: `LoginRequest`, `RegisterRequest`, `VerifyEmailRequest`, `ResetPasswordRequest`, `ChangePasswordRequest`.
- [ ] 2.5 Khởi tạo các DTO Responses: `AuthResponse`, `StudentResponse`, format chuẩn theo cấu trúc API chung.
- [ ] 2.6 Cập nhật `GlobalExceptionHandler` để handle các custom exception (HTTP 400, 401, 403, 409, 422, 429).

## Phase 3: Business Logic (Services)

- [ ] 3.1 `EmailService`: Logic tạo mẫu và gửi email (Verify Email, Password Reset) dạng Async.
- [ ] 3.2 `AuthService.register()`: Hash mật khẩu (bcrypt cost=10+), tạo user (`pending`), sinh `email_verification` token, gọi gửi mail.
- [ ] 3.3 `AuthService.verifyEmail()` và `resendVerification()`: Validate token, chuyển status về `active`, thu hồi token.
- [ ] 3.4 `AuthService.login()`: Kiểm tra đình chỉ/khóa, đối chiếu bcrypt, quản lý login fail count (khóa 15m sau 5 lần sai), sinh cặp JWT/Refresh.
- [ ] 3.5 `AuthService.forgotPassword()` và `resetPassword()`: Sinh token đặt lại (15m), kiểm tra tính hợp lệ và đổi mật khẩu, vô hiệu hóa các token khác.
- [ ] 3.6 `AuthService.refreshToken()`: Validate refresh token, sinh cặp token mới (rotate refresh token).
- [ ] 3.7 `AuthService.logout()`: Tìm và thu hồi refresh token của user hiện tại.
- [ ] 3.8 `OAuthService`: Xử lý luồng Google OAuth (sinh state, redirect, xử lý callback, đổi mã lấy access token từ Google, tạo/liên kết tài khoản).

## Phase 4: Controllers & Routing

- [ ] 4.1 Cài đặt `AuthController`: Liên kết tới các tính năng public (login, register, forgot-password, oauth, v.v.).
- [ ] 4.2 Cài đặt `StudentController` (`/api/students/me`):
  - GET profile
  - PUT update profile
  - PUT update password (`changePassword()`).
- [ ] 4.3 Setup authorization annotations (ví dụ `@PreAuthorize("hasRole('STUDENT')")`) trên các API yêu cầu đăng nhập.

## Phase 5: Testing & QA (Backend)

- [ ] 5.1 Viết Unit Tests cho `AuthService` đảm bảo coverage >= 80% (đặc biệt rule count failed logins, check suspended).
- [ ] 5.2 Viết Unit Tests cho `JwtService` và `OAuthService`.
- [ ] 5.3 Viết Integration Tests kiểm thử REST API cho `/api/auth/*` (happy/error paths).

## Phase 6: Frontend Development

- [ ] 6.1 Setup Auth State Management (Zustand/Context API) cho `user`, `accessToken`, `isAuthenticated`.
- [ ] 6.2 Cấu hình Axios Interceptors:
  - Đính kèm token vào request header.
  - Xử lý 401 để tự động gọi API `/api/auth/refresh`.
- [ ] 6.3 Xây dựng UI `LoginPage` và tích hợp API `login()`, `loginWithGoogle()`.
- [ ] 6.4 Xây dựng UI `RegisterPage` với form validation (email, password strength, match password) và gọi API.
- [ ] 6.5 Xây dựng UI xác thực email, yêu cầu gửi lại email.
- [ ] 6.6 Xây dựng UI `ForgotPassword` & `ResetPassword`.
- [ ] 6.7 Xây dựng `ProfilePage` (cập nhật thông tin cơ bản) và chức năng thay đổi mật khẩu (Change Password modal/tab).
- [ ] 6.8 Xây dựng component `ProtectedRoute` để kiểm soát các tuyến đường cần đăng nhập.

## Phase 7: Final Review & Refine

- [ ] 7.1 Cross-check với `SPEC.md` và `CONSTITUTION.md`: Đảm bảo quy tắc score, soft delete, error mapping (4xx, 500) được tuân thủ.
- [ ] 7.2 Đảm bảo không có warning lint nào ở Frontend (`npm run lint`) & Backend (`mvn spotless:apply`).
- [ ] 7.3 Code Review và Merge theo quy định PR.
