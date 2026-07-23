# RDS — User Login Screen

> Ví dụ đầy đủ theo đúng format Template2 (`Temp_Document/Template2_RDS Document.pdf`, mẫu GAMS) cho **1 màn hình cụ thể** — theo quy ước tại [`SPEC-rds-template-generation-guide.md § 4`](SPEC-rds-template-generation-guide.md). Dùng làm mẫu tham chiếu khi cần viết RDS chi tiết cho màn hình khác.
> Toàn bộ số liệu (ngưỡng khóa, thời hạn token, exception thật) lấy trực tiếp từ `AuthenticationService.java` — không suy đoán.

---

## II. Requirement Specifications

### 1. Authentication

#### 1.1 UC-01_User Login (Đăng nhập)

##### a. Functionalities

| | |
|---|---|
| **UC ID and Name:** | UC-01_User Login (Đăng nhập) |
| **Created By:** | Minh Pham *(suy từ `git log --follow --diff-filter=A` trên `use-cases/Bao_cao_dac_ta_Use_Case.md`, không phải trường ghi sẵn — cần xác nhận lại với người viết gốc)* | **Date Created:** | 2026-05-28 |
| **Primary Actor:** | Student | **Secondary Actors:** | Google (OAuth provider) |
| **Trigger:** | User bấm nút "Đăng nhập" từ trang chủ, hoặc truy cập thẳng 1 route yêu cầu đăng nhập (`PrivateRoute` redirect về `/login`) |
| **Description:** | Student đăng nhập bằng Email/Mật khẩu hoặc tài khoản Google để truy cập các tính năng học tập cá nhân hoá. |
| **Preconditions:** | PRE-1: Tài khoản đã tồn tại và ở trạng thái `ACTIVE` (không `PENDING`/`SUSPENDED`). PRE-2: Hệ thống không ở chế độ bảo trì (`maintenanceModeService.isEnabled() = false`). |
| **Postconditions:** | POST-1: Nhận `accessToken` + `refreshToken` (JWT), điều hướng tới `/dashboard`. POST-2: `refreshToken` được lưu vào `auth_tokens`, hết hạn sau **7 ngày**. POST-3: `loginAttempts` reset về 0, `lockedUntil` xoá. |
| **Normal Flow:** | **1.0 Login System**<br>1. Student truy cập màn hình `/login`.<br>2. Student nhập Email + Mật khẩu.<br>3. Student bấm nút **"Đăng nhập"**.<br>4. Hệ thống xác thực qua `AuthenticationManager` (xem 1.0.E1–E5 nếu lỗi).<br>5. Hệ thống sinh `accessToken`/`refreshToken`, lưu refresh token vào `auth_tokens`.<br>6. Hệ thống điều hướng Student tới `/dashboard`. |
| **Alternative Flows:** | **1.1 Google Login**<br>1. Student bấm nút Google Login (`<GoogleLogin>`).<br>2. Google xác thực, trả `credentialResponse.credential` (ID token).<br>3. Hệ thống gọi `loginWithGoogle()` — verify ID token qua `GoogleIdTokenVerifier`, tìm hoặc tạo `student_users` theo `oauth_provider_id`.<br>4. Quay lại bước 5 của luồng cơ bản. |
| **Exceptions:** | **1.0.E1** Sai mật khẩu (`BadCredentialsException`) → tăng `loginAttempts`; nếu `>= 5` lần liên tiếp → khoá tài khoản **15 phút** (`lockedUntil`); trả `401 INVALID_CREDENTIALS`.<br>**1.0.E2** Tài khoản đang bị khoá (`lockedUntil` còn hiệu lực) → `429 TOO_MANY_REQUESTS`.<br>**1.0.E3** Tài khoản `SUSPENDED` → `403 ACCOUNT_SUSPENDED` kèm lý do (`suspendReason`).<br>**1.0.E4** Tài khoản `PENDING` (chưa xác minh email) → `403 EMAIL_NOT_VERIFIED`.<br>**1.0.E5** Hệ thống đang bảo trì → `503 MAINTENANCE_MODE`. |
| **Priority:** | Must Have |
| **Frequency of Use:** | Cao nhất trong hệ thống — mọi Student đăng nhập tối thiểu 1 lần/phiên học |
| **Business Rules:** | `BIZ-AUTH-01`, `BIZ-AUTH-02`, `BIZ-AUTH-04`, `BIZ-AUTH-06` *(xem bảng b bên dưới)* |
| **Other Information:** | Refresh token hết hạn sau 7 ngày (`.plusDays(7)`, hard-code trong `AuthenticationService`, chưa đọc từ `application.yml` — cần xác nhận nếu muốn cấu hình được). |
| **Assumptions:** | Giả định IP/thiết bị Student ổn định trong phiên đăng nhập; chưa có "remember device"/MFA cho Student (khác Admin — xem `MfaAttempt` trong `auth_tokens`). |

##### b. Business Rules

| ID | Business Rule | Business Rule Description |
|---|---|---|
| `BIZ-AUTH-01` | JWT bắt buộc cho API đã xác thực | Mọi API trừ `/api/auth/*` (login/register/forgot-password) phải qua JWT filter |
| `BIZ-AUTH-02` | Mã hoá mật khẩu | Password hash bằng bcrypt, cost ≥ 10 (chuẩn dự án: 12) — **không phải MD5** như ví dụ mẫu gốc của Template2 (GAMS dùng MD5, dự án này dùng bcrypt an toàn hơn) |
| `BIZ-AUTH-04` | Không dùng "ẩn UI" làm bảo mật | Backend luôn trả `401/403` khi không đủ quyền, kể cả khi FE đã ẩn nút/route |
| `BIZ-AUTH-06` | Kiểm tra subscription real-time | Áp dụng sau khi đăng nhập (không thuộc riêng màn Login) — cache tối đa 5 phút |
| ⚠️ *(chưa có ID)* | Khoá tài khoản sau 5 lần sai liên tiếp, khoá 15 phút | Có trong code (`AuthenticationService.handleStudentLogin`) nhưng **chưa được ghi thành rule có ID** trong `constraints/business.md` — đề xuất bổ sung khi cập nhật file đó, không tự gán ID mới ở đây |

---

## III. Design Specifications

### 1. Authentication

#### 1.1 User Login Screen

**Liên quan**: UC-01_User Login · Component thật: `apps/frontend/src/pages/login/Login.jsx` · Service thật: `AuthenticationService.login()`

##### UI Design

*(Không có ảnh mockup — mô tả layout theo component thật, đã verify bằng code, chưa chụp screenshot)*

Layout: tiêu đề "Đăng nhập để tiếp tục hành trình học tiếng Nhật" → form Email/Mật khẩu → liên kết "Quên mật khẩu?" → nút "Đăng nhập" → nút đăng nhập Google → liên kết "Đăng ký ngay".

| Field Name | Field Type | Description |
|---|---|---|
| **Thông tin đăng nhập** | | |
| Email | Text Box (`placeholder="email@example.com"`) | Email đăng nhập, validate định dạng phía client trước khi submit |
| Password | Password Box (`placeholder="••••••••"`) | Mật khẩu, có nút show/hide (button `type="button"` cạnh field) |
| Quên mật khẩu? | Hyperlink (`<Link to="/forgot-password">`) | Điều hướng sang `/forgot-password` |
| Đăng nhập | Button (`aria-label="Đăng nhập vào tài khoản"`) | Submit form, gọi `loginThunk` |
| Google Login | `<GoogleLogin>` component (thư viện `@react-oauth/google`) | Đăng nhập bằng Google, `onSuccess=handleGoogleSuccess` |
| Đăng ký ngay | Hyperlink (`<Link to="/register">`) | Điều hướng sang `/register` |

> Không có nút "Login with Facebook" — khác ví dụ mẫu gốc Template2 (GAMS có cả Google lẫn Facebook), dự án này **chỉ hỗ trợ Google OAuth**.

##### Database Access

| Table | CRUD | Description |
|---|---|---|
| `student_users` | R, U | Xác thực Email/Password (R); cập nhật `login_attempts`, `locked_until`, `last_login_at` sau mỗi lần thử (U) |
| `auth_tokens` | C | Lưu `refresh_token` mới sau khi đăng nhập thành công |

###### SQL Commands

```sql
-- 1/ Xác thực Email + Password (AuthenticationManager -> UserDetailsService)
SELECT id, email, password_hash, status, login_attempts, locked_until,
       oauth_provider, oauth_provider_id
FROM student_users
WHERE email = ? AND is_deleted = 0;

-- 2/ Đăng nhập thành công: reset login_attempts + cập nhật last_login_at
UPDATE student_users
SET login_attempts = 0, locked_until = NULL, last_login_at = GETDATE()
WHERE id = ?;

-- 3/ Đăng nhập thất bại: tăng login_attempts, khoá nếu >= 5 lần
UPDATE student_users
SET login_attempts = login_attempts + 1,
    locked_until = CASE WHEN login_attempts + 1 >= 5 THEN DATEADD(MINUTE, 15, GETDATE()) ELSE locked_until END
WHERE id = ?;

-- 4/ Lưu refresh token mới (hết hạn sau 7 ngày)
INSERT INTO auth_tokens (actor_type, student_id, token_type, token_value, expires_at, created_at)
VALUES ('STUDENT', ?, 'REFRESH', ?, DATEADD(DAY, 7, GETDATE()), GETDATE());

-- 5/ Google Login: tìm tài khoản theo oauth_provider_id, tạo mới nếu chưa có
SELECT id, email, status FROM student_users
WHERE oauth_provider = 'google' AND oauth_provider_id = ?;
```
