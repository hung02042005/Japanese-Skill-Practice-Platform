# Test Specification — Authentication API
## JLPT E-Learning Platform

**Phạm vi**: `/api/auth/**` và `/api/staff/auth/**`
**Loại test**: Unit (MockMvc + Mockito) & Integration (SpringBootTest + H2)
**Cập nhật**: 2026-06-08

---

## Mục lục

1. [Quy ước chung](#1-quy-ước-chung)
2. [POST /api/auth/check-account-type](#2-post-apiauthcheck-account-type)
3. [POST /api/auth/login](#3-post-apiauthlogin)
4. [POST /api/auth/register](#4-post-apiauthregister)
5. [POST /api/auth/verify-email](#5-post-apiauthverify-email)
6. [POST /api/auth/resend-verification](#6-post-apiauthresend-verification)
7. [POST /api/auth/forgot-password](#7-post-apiauthforgot-password)
8. [POST /api/auth/reset-password](#8-post-apiauthreset-password)
9. [POST /api/auth/refresh](#9-post-apiauthrefresh)
10. [POST /api/auth/logout](#10-post-apiauthlogout)
11. [POST /api/auth/google](#11-post-apiauthgoogle)
12. [POST /api/staff/auth/login](#12-post-apistaffauthlogin)
13. [POST /api/staff/auth/setup-password](#13-post-apistaffauthsetup-password)
14. [POST /api/staff/auth/forgot-password](#14-post-apistaffauthforgot-password)
15. [POST /api/staff/auth/change-temp-password](#15-post-apistaffauthchange-temp-password)
16. [Security & JWT Tests](#16-security--jwt-tests)
17. [Rate Limiting Tests](#17-rate-limiting-tests)
18. [Account Locking Tests](#18-account-locking-tests)

---

## 1. Quy ước chung

### Response format chuẩn
```json
{ "status": 200, "message": "...", "data": { ... } }
```

### Error format chuẩn
```json
{ "status": 4xx, "message": "...", "errorCode": "ERROR_CODE", "data": null }
```

### Cấu hình test data mặc định

| Role    | Email                       | Password       | Status   |
|---------|-----------------------------|----------------|----------|
| Student | student@test.com            | Student@123    | ACTIVE   |
| Staff   | staff@test.com              | Staff@123      | ACTIVE   |
| Admin   | admin@test.com              | Admin@123456   | ACTIVE   |
| Student | pending@test.com            | Pending@123    | PENDING  |
| Student | suspended@test.com          | Suspend@123    | SUSPENDED|
| Staff   | newstaff@test.com           | (setup pending)| PENDING  |
| Staff   | mustchange@test.com         | Temp@123456    | ACTIVE, mustChangePassword=true |

### Password policy (cho tất cả reset/setup endpoints)
- Tối thiểu 8 ký tự
- Ít nhất 1 chữ HOA (A-Z)
- Ít nhất 1 chữ số (0-9)
- Regex: `^(?=.*[A-Z])(?=.*\d).{8,}$`

---

## 2. POST /api/auth/check-account-type

**Request body**: `{ "email": "..." }`
**Auth required**: Không
**Rate limit**: 10 req/phút/IP

| ID | Tên test case | Input | Expected HTTP | Expected response |
|----|---------------|-------|---------------|-------------------|
| CAT-01 | Email thuộc student | `student@test.com` | 200 | `data.accountType = "student"` |
| CAT-02 | Email thuộc staff | `staff@test.com` | 200 | `data.accountType = "staff"` |
| CAT-03 | Email không tồn tại | `unknown@test.com` | 200 | `data.accountType = "unknown"` |
| CAT-04 | Email bị thiếu | `{}` | 400 | errorCode = `VALIDATION_ERROR` |
| CAT-05 | Email không đúng format | `{ "email": "not-an-email" }` | 400 | errorCode = `VALIDATION_ERROR` |
| CAT-06 | Email rỗng | `{ "email": "" }` | 400 | errorCode = `VALIDATION_ERROR` |
| CAT-07 | Vượt rate limit (11 req từ cùng IP) | 11 requests liên tiếp | 429 | errorCode = `RATE_LIMIT_EXCEEDED` |

---

## 3. POST /api/auth/login

**Request body**: `{ "email": "...", "password": "..." }`
**Auth required**: Không
**Ghi chú**: Endpoint thử login theo thứ tự Admin → Staff → Student

### 3.1 Happy path

| ID | Tên test case | Input | Expected HTTP | Expected response |
|----|---------------|-------|---------------|-------------------|
| LGN-01 | Student ACTIVE đăng nhập đúng | `student@test.com / Student@123` | 200 | `data.role="STUDENT"`, `data.accessToken` tồn tại, `data.refreshToken` tồn tại, `data.user.email` đúng |
| LGN-02 | Staff ACTIVE đăng nhập đúng | `staff@test.com / Staff@123` | 200 | `data.role="STAFF"`, `data.staffRole` tồn tại, `data.requirePasswordChange=false` |
| LGN-03 | Admin ACTIVE đăng nhập đúng | `admin@test.com / Admin@123456` | 200 | `data.role="ADMIN"`, `data.accessToken` tồn tại |
| LGN-04 | Staff mustChangePassword=true | `mustchange@test.com / Temp@123456` | 200 | `data.requirePasswordChange=true`, `data.accessToken` là LIMITED_SESSION (30 phút) |

### 3.2 Validation errors (400)

| ID | Tên test case | Input | Expected HTTP | Expected errorCode |
|----|---------------|-------|---------------|--------------------|
| LGN-05 | Thiếu email | `{ "password": "..." }` | 400 | `VALIDATION_ERROR` |
| LGN-06 | Thiếu password | `{ "email": "..." }` | 400 | `VALIDATION_ERROR` |
| LGN-07 | Email không đúng format | `{ "email": "abc", "password": "..." }` | 400 | `VALIDATION_ERROR` |
| LGN-08 | Body rỗng | `{}` | 400 | `VALIDATION_ERROR` |

### 3.3 Business logic errors

| ID | Tên test case | Input | Expected HTTP | Expected errorCode |
|----|---------------|-------|---------------|--------------------|
| LGN-09 | Sai mật khẩu | `student@test.com / WrongPass@1` | 401 | `INVALID_CREDENTIALS` |
| LGN-10 | Email không tồn tại | `nouser@test.com / Any@123` | 401 | `INVALID_CREDENTIALS` |
| LGN-11 | Student chưa verify email (PENDING) | `pending@test.com / Pending@123` | 403 | `EMAIL_NOT_VERIFIED` |
| LGN-12 | Tài khoản bị SUSPENDED | `suspended@test.com / Suspend@123` | 403 | `ACCOUNT_SUSPENDED` |
| LGN-13 | Tài khoản bị khóa (lockedUntil trong tương lai) | Tài khoản có `lockedUntil = now+10min` | 429 | `TOO_MANY_REQUESTS` |

### 3.4 Security assertions

| ID | Điều cần kiểm tra |
|----|-------------------|
| LGN-S1 | Response KHÔNG chứa `passwordHash` hoặc `password` |
| LGN-S2 | Sai mật khẩu lần 1 → `loginAttempts` tăng lên 1 (kiểm tra qua DB hoặc lần đăng nhập tiếp theo) |
| LGN-S3 | Sai mật khẩu lần thứ 5 → tài khoản bị lock, `lockedUntil` được set |
| LGN-S4 | Đăng nhập thành công sau lần sai → `loginAttempts` reset về 0 |
| LGN-S5 | accessToken phải là JWT hợp lệ với claim `sub = email` |

---

## 4. POST /api/auth/register

**Request body**: `{ "fullName", "email", "password", "confirmPassword" }`
**Auth required**: Không
**Side effect**: Gửi email xác thực (mock EmailService trong unit test)

### 4.1 Happy path

| ID | Tên test case | Expected HTTP | Expected response |
|----|---------------|---------------|-------------------|
| REG-01 | Đăng ký hợp lệ | 201 | `data.studentId` tồn tại, `data.email` khớp, `data.fullName` khớp, KHÔNG có `passwordHash` |
| REG-02 | Email service được gọi sau đăng ký | 201 | `emailService.sendVerificationEmail(...)` được gọi 1 lần |
| REG-03 | Student được tạo với status=PENDING | 201 | (kiểm tra DB) `status = PENDING` |

### 4.2 Validation errors (400)

| ID | Tên test case | Input sai | Expected errorCode |
|----|---------------|-----------|--------------------|
| REG-04 | Thiếu fullName | (bỏ fullName) | `VALIDATION_ERROR` |
| REG-05 | fullName quá ngắn (1 ký tự) | `"fullName": "A"` | `VALIDATION_ERROR` |
| REG-06 | Thiếu email | (bỏ email) | `VALIDATION_ERROR` |
| REG-07 | Email không đúng format | `"email": "abc"` | `VALIDATION_ERROR` |
| REG-08 | Thiếu password | (bỏ password) | `VALIDATION_ERROR` |
| REG-09 | Password < 8 ký tự | `"password": "Ab1"` | `VALIDATION_ERROR` |
| REG-10 | Password không có chữ HOA | `"password": "abc12345"` | `VALIDATION_ERROR` |
| REG-11 | Password không có chữ số | `"password": "AbcdEFgh"` | `VALIDATION_ERROR` |
| REG-12 | confirmPassword không khớp | `"confirmPassword": "Different@1"` | `VALIDATION_ERROR` |

### 4.3 Business logic errors

| ID | Tên test case | Expected HTTP | Expected errorCode |
|----|---------------|---------------|--------------------|
| REG-13 | Email đã tồn tại (student) | 409 | `EMAIL_ALREADY_EXISTS` |
| REG-14 | Email đã tồn tại (staff dùng email đó) | 409 | `EMAIL_ALREADY_EXISTS` |

---

## 5. POST /api/auth/verify-email

**Request body**: `{ "token": "..." }`
**Auth required**: Không

| ID | Tên test case | Input | Expected HTTP | Expected errorCode / ghi chú |
|----|---------------|-------|---------------|-------------------------------|
| VFY-01 | Token hợp lệ, chưa hết hạn | Token EMAIL_VERIFICATION còn hạn | 200 | Student status → ACTIVE, `emailVerifiedAt` được set |
| VFY-02 | Token không tồn tại | `"token": "invalid-uuid"` | 400/404 | `INVALID_TOKEN` |
| VFY-03 | Token đã hết hạn (> 24h) | Token `expiresAt` trong quá khứ | 400 | `TOKEN_EXPIRED` |
| VFY-04 | Token sai type (PASSWORD_RESET dùng cho verify-email) | PASSWORD_RESET token | 400 | `INVALID_TOKEN` |
| VFY-05 | Token đã dùng rồi (dùng lần 2) | Token đã revoke | 400 | `INVALID_TOKEN` |
| VFY-06 | Thiếu token field | `{}` | 400 | `VALIDATION_ERROR` |

---

## 6. POST /api/auth/resend-verification

**Request body**: `{ "email": "..." }`
**Auth required**: Không

| ID | Tên test case | Input | Expected HTTP | Ghi chú |
|----|---------------|-------|---------------|---------|
| RSV-01 | Email PENDING hợp lệ | `pending@test.com` | 200 | Token mới được tạo, email được gửi |
| RSV-02 | Email đã ACTIVE | `student@test.com` | 400 | `ALREADY_VERIFIED` |
| RSV-03 | Email không tồn tại | `nobody@test.com` | 200 | Response generic (không tiết lộ email không tồn tại) |
| RSV-04 | Email của SUSPENDED student | `suspended@test.com` | 403 | `ACCOUNT_SUSPENDED` |
| RSV-05 | Email không đúng format | `"email": "bad"` | 400 | `VALIDATION_ERROR` |

---

## 7. POST /api/auth/forgot-password

**Request body**: `{ "email": "..." }`
**Auth required**: Không

| ID | Tên test case | Input | Expected HTTP | Ghi chú |
|----|---------------|-------|---------------|---------|
| FPW-01 | Email student tồn tại | `student@test.com` | 200 | Token PASSWORD_RESET tạo (1h), email gửi |
| FPW-02 | Email không tồn tại | `nobody@test.com` | 200 | Response giống FPW-01 (security: không tiết lộ) |
| FPW-03 | Email không đúng format | `"email": "bad"` | 400 | `VALIDATION_ERROR` |
| FPW-04 | Gọi 2 lần với cùng email | 2 request liên tiếp | 200 | Token mới ghi đè / token cũ bị revoke |

---

## 8. POST /api/auth/reset-password

**Request body**: `{ "token", "newPassword", "confirmPassword" }`
**Auth required**: Không

### 8.1 Happy path

| ID | Tên test case | Expected HTTP | Ghi chú |
|----|---------------|---------------|---------|
| RPW-01 | Token hợp lệ, password đúng chuẩn | 200 | Password được đổi, token bị revoke |
| RPW-02 | Sau khi đổi password thành công, login với password mới | 200 | Login thành công |
| RPW-03 | Sau khi đổi password thành công, login với password cũ | 401 | `INVALID_CREDENTIALS` |

### 8.2 Error cases

| ID | Tên test case | Input | Expected HTTP | Expected errorCode |
|----|---------------|-------|---------------|--------------------|
| RPW-04 | Token không tồn tại | `"token": "fake"` | 400/404 | `INVALID_TOKEN` |
| RPW-05 | Token đã hết hạn (> 1h) | Token `expiresAt` quá khứ | 400 | `TOKEN_EXPIRED` |
| RPW-06 | Token sai type (EMAIL_VERIFICATION) | EMAIL_VERIFICATION token | 400 | `INVALID_TOKEN` |
| RPW-07 | Dùng lại token đã dùng | Token đã revoke | 400 | `INVALID_TOKEN` |
| RPW-08 | confirmPassword không khớp | `"confirmPassword": "Other@1"` | 400 | `VALIDATION_ERROR` |
| RPW-09 | Password < 8 ký tự | `"newPassword": "Ab1"` | 400 | `VALIDATION_ERROR` |
| RPW-10 | Password không có chữ HOA | `"newPassword": "abc12345"` | 400 | `VALIDATION_ERROR` |
| RPW-11 | Password không có số | `"newPassword": "AbcdEFgh"` | 400 | `VALIDATION_ERROR` |

---

## 9. POST /api/auth/refresh

**Request body**: `{ "refreshToken": "..." }`
**Auth required**: Có (valid accessToken trong header) — *hoặc kiểm tra implementation cụ thể*

| ID | Tên test case | Input | Expected HTTP | Ghi chú |
|----|---------------|-------|---------------|---------|
| RFR-01 | refreshToken hợp lệ | Token REFRESH còn hạn trong DB | 200 | Trả về `accessToken` mới + `refreshToken` mới |
| RFR-02 | refreshToken mới khác token cũ | So sánh giá trị | 200 | Token rotation: refreshToken mới ≠ cũ |
| RFR-03 | refreshToken cũ bị revoke sau refresh | Dùng lại token cũ | 401 | `INVALID_TOKEN` |
| RFR-04 | refreshToken không tồn tại | `"refreshToken": "fake"` | 401 | `INVALID_TOKEN` |
| RFR-05 | refreshToken đã bị revoke (sau logout) | Token đã revoke | 401 | `INVALID_TOKEN` |
| RFR-06 | refreshToken hết hạn (> 7 ngày) | Token `expiresAt` quá khứ | 401 | `TOKEN_EXPIRED` |
| RFR-07 | Body thiếu refreshToken | `{}` | 400 | `VALIDATION_ERROR` |

---

## 10. POST /api/auth/logout

**Request body**: `{ "refreshToken": "..." }`
**Auth required**: Có

| ID | Tên test case | Input | Expected HTTP | Ghi chú |
|----|---------------|-------|---------------|---------|
| LGT-01 | Logout với token hợp lệ | Token REFRESH tồn tại | 200 | Token bị xóa khỏi DB |
| LGT-02 | Idempotency: logout token đã xóa | Token không còn trong DB | 200 | Không throw exception |
| LGT-03 | Sau logout, dùng token để refresh | Token đã xóa | 401 | `INVALID_TOKEN` |

---

## 11. POST /api/auth/google

**Request body**: `{ "idToken": "..." }`
**Auth required**: Không

| ID | Tên test case | Expected HTTP | Ghi chú |
|----|---------------|---------------|---------|
| GGL-01 | idToken hợp lệ, user chưa tồn tại | 200/201 | Tạo student mới, status=ACTIVE, `emailVerifiedAt` được set |
| GGL-02 | idToken hợp lệ, email đã tồn tại | 200 | Link oauth provider, trả về tokens |
| GGL-03 | idToken không hợp lệ / giả mạo | 401 | `INVALID_GOOGLE_TOKEN` |
| GGL-04 | idToken hợp lệ nhưng tài khoản bị SUSPENDED | 403 | `ACCOUNT_SUSPENDED` |
| GGL-05 | Response KHÔNG chứa Google ID token raw | 200 | Security: idToken không được echo lại |

---

## 12. POST /api/staff/auth/login

**Request body**: `{ "email", "password" }`
**Auth required**: Không

### 12.1 Happy path

| ID | Tên test case | Expected HTTP | Expected response |
|----|---------------|---------------|-------------------|
| SLG-01 | Staff ACTIVE đăng nhập đúng | 200 | `data.role="STAFF"`, `data.staffRole` (STAFF / STAFF_MANAGER), `data.accessToken`, `data.refreshToken` |
| SLG-02 | Staff mustChangePassword=true | 200 | `data.requirePasswordChange=true`, accessToken là LIMITED_SESSION (30 phút), `data.role="STAFF"` |

### 12.2 Error cases

| ID | Tên test case | Expected HTTP | Expected errorCode |
|----|---------------|---------------|--------------------|
| SLG-03 | Sai mật khẩu | 401 | `INVALID_CREDENTIALS` |
| SLG-04 | Email không tồn tại | 401 | `INVALID_CREDENTIALS` |
| SLG-05 | Tài khoản SUSPENDED | 403 | `ACCOUNT_SUSPENDED` |
| SLG-06 | Tài khoản bị khóa | 429 | `TOO_MANY_REQUESTS` |
| SLG-07 | Sai mật khẩu 5 lần → locked | 5 lần sai | 429 (lần 6) | `TOO_MANY_REQUESTS` + `lockedUntil` được set |

### 12.3 Security

| ID | Điều cần kiểm tra |
|----|-------------------|
| SLG-S1 | Response KHÔNG chứa `passwordHash` |
| SLG-S2 | Student email không login được qua endpoint này → 401 `INVALID_CREDENTIALS` |
| SLG-S3 | LIMITED_SESSION token chứa claim `tokenType = "limited_session"` |

---

## 13. POST /api/staff/auth/setup-password

**Request body**: `{ "token", "newPassword", "confirmPassword" }`
**Auth required**: Không
**Mục đích**: Staff kích hoạt tài khoản lần đầu sau khi được admin mời

| ID | Tên test case | Input | Expected HTTP | Ghi chú |
|----|---------------|-------|---------------|---------|
| SSP-01 | Token hợp lệ, password đúng chuẩn | Setup token còn hạn | 200 | Staff status → ACTIVE, password được hash lưu, token bị revoke |
| SSP-02 | Token không tồn tại | `"token": "fake"` | 400/404 | `INVALID_TOKEN` |
| SSP-03 | Token đã hết hạn | Token `expiresAt` quá khứ | 400 | `TOKEN_EXPIRED` |
| SSP-04 | Token đã dùng rồi (lần 2) | Token đã revoke | 400 | `INVALID_TOKEN` |
| SSP-05 | Password < 8 ký tự | `"newPassword": "Ab1"` | 400 | `VALIDATION_ERROR` |
| SSP-06 | Password không HOA | `"newPassword": "abc12345"` | 400 | `VALIDATION_ERROR` |
| SSP-07 | confirmPassword không khớp | Khác nhau | 400 | `VALIDATION_ERROR` |
| SSP-08 | Sau setup, login với password mới | — | 200 | Đăng nhập thành công |

---

## 14. POST /api/staff/auth/forgot-password

**Request body**: `{ "email": "..." }`
**Auth required**: Không
**Rate limit**: 3 req/giờ/staff member

| ID | Tên test case | Expected HTTP | Ghi chú |
|----|---------------|---------------|---------|
| SFP-01 | Staff email tồn tại | 200 | StaffPasswordResetRequest với status=PENDING được tạo, admin được notify |
| SFP-02 | Email không tồn tại | 200 | Response generic (security) |
| SFP-03 | Vượt rate limit (4 lần trong 1h) | 429 | `RATE_LIMIT_EXCEEDED` |
| SFP-04 | Email không đúng format | 400 | `VALIDATION_ERROR` |

---

## 15. POST /api/staff/auth/change-temp-password

**Request body**: `{ "newPassword", "confirmPassword" }`
**Auth required**: Có — phải là **LIMITED_SESSION** token trong `Authorization: Bearer ...`

| ID | Tên test case | Input / Setup | Expected HTTP | Ghi chú |
|----|---------------|---------------|---------------|---------|
| CTP-01 | Token LIMITED_SESSION hợp lệ, mustChangePassword=true | Token đúng loại | 200 | Password thay đổi, token bị revoke, `mustChangePassword=false` |
| CTP-02 | Không có Authorization header | (bỏ header) | 401 | `UNAUTHORIZED` |
| CTP-03 | Dùng access token thông thường (không phải limited) | Dùng token STAFF bình thường | 403 | `FORBIDDEN` — chỉ cho phép limited_session |
| CTP-04 | mustChangePassword=false (staff bình thường) | Staff không có flag này | 403 | `FORBIDDEN` |
| CTP-05 | newPassword giống với temp password cũ | Nhập lại password cũ | 400 | `SAME_AS_CURRENT_PASSWORD` |
| CTP-06 | Password < 8 ký tự | `"newPassword": "Ab1"` | 400 | `VALIDATION_ERROR` |
| CTP-07 | confirmPassword không khớp | Khác nhau | 400 | `VALIDATION_ERROR` |
| CTP-08 | Token đã revoke (dùng lại sau lần đổi thành công) | Token cũ | 401 | `INVALID_TOKEN` |
| CTP-09 | Sau đổi thành công, login với password mới | — | 200 | Login thành công, `requirePasswordChange=false` |

---

## 16. Security & JWT Tests

### 16.1 Unauthenticated access

| ID | Test case | Expected |
|----|-----------|----------|
| JWT-01 | GET /api/student/profile không có token | 401 |
| JWT-02 | GET /api/staff/... không có token | 401 |
| JWT-03 | GET /api/admin/... không có token | 401 |
| JWT-04 | Authorization header có format sai (`"Token xxx"` thay vì `"Bearer xxx"`) | 401 |

### 16.2 Role authorization

| ID | Test case | Token | Endpoint | Expected |
|----|-----------|-------|----------|----------|
| JWT-05 | Student token truy cập /api/admin/** | STUDENT | /api/admin/users | 403 |
| JWT-06 | Student token truy cập /api/staff/** | STUDENT | /api/staff/content | 403 |
| JWT-07 | Staff token truy cập /api/admin/** | STAFF | /api/admin/users | 403 |
| JWT-08 | Admin token truy cập /api/student/** | ADMIN | /api/student/courses | 403 |
| JWT-09 | Staff token truy cập /api/student/** | STAFF | /api/student/courses | 403 |

### 16.3 Token tampering & expiry

| ID | Test case | Expected |
|----|-----------|----------|
| JWT-10 | JWT với signature giả mạo (sửa 1 ký tự ở signature part) | 401 |
| JWT-11 | JWT hết hạn (accessToken > 15 phút) | 401 |
| JWT-12 | JWT decode được nhưng claim `sub` là email không tồn tại | 401 |
| JWT-13 | Dùng refreshToken như accessToken trong Authorization header | 401 |

### 16.4 LIMITED_SESSION token restrictions

| ID | Test case | Token | Endpoint | Expected |
|----|-----------|-------|----------|----------|
| JWT-14 | LIMITED_SESSION token dùng cho endpoint khác (không phải change-temp-password) | LIMITED_SESSION | /api/staff/content | 403 |
| JWT-15 | LIMITED_SESSION token dùng đúng endpoint | LIMITED_SESSION | /api/staff/auth/change-temp-password | 200 |
| JWT-16 | Regular STAFF token dùng cho change-temp-password | STAFF | /api/staff/auth/change-temp-password | 403 |

---

## 17. Rate Limiting Tests

| ID | Endpoint | Ngưỡng | Test case | Expected |
|----|----------|--------|-----------|----------|
| RL-01 | POST /api/auth/check-account-type | 10/phút/IP | 11 requests từ cùng IP trong 1 phút | Request thứ 11 → 429 |
| RL-02 | POST /api/auth/check-account-type | 10/phút/IP | 10 requests từ IP-A, 1 request từ IP-B | IP-B → 200 (không bị ảnh hưởng) |
| RL-03 | POST /api/staff/auth/forgot-password | 3/giờ/staff | Staff gửi 4 lần trong 1 giờ | Request thứ 4 → 429 |
| RL-04 | POST /api/staff/auth/forgot-password | 3/giờ/staff | 3 requests rồi chờ >1 giờ | Request sau 1 giờ → 200 |

---

## 18. Account Locking Tests

**Áp dụng cho**: Student, Staff, Admin

| ID | Test case | Precondition | Action | Expected |
|----|-----------|-------------|--------|----------|
| ALK-01 | Sai mật khẩu 1 lần | `loginAttempts=0` | 1 lần sai | `loginAttempts=1`, `lockedUntil=null` |
| ALK-02 | Sai mật khẩu 4 lần | `loginAttempts=3` | 1 lần sai | `loginAttempts=4`, `lockedUntil=null` |
| ALK-03 | Sai mật khẩu lần thứ 5 → lock | `loginAttempts=4` | 1 lần sai | `loginAttempts=5`, `lockedUntil ≈ now+15min`, response 401 `INVALID_CREDENTIALS` |
| ALK-04 | Đăng nhập khi đang bị lock | `lockedUntil = now+10min` | Login (đúng hoặc sai pass) | 429 `TOO_MANY_REQUESTS` |
| ALK-05 | Đăng nhập khi lock đã hết hạn | `lockedUntil = now-1min` | Login đúng pass | 200, `loginAttempts` reset về 0 |
| ALK-06 | Đăng nhập thành công reset attempts | `loginAttempts=3` | Login đúng pass | 200, `loginAttempts=0`, `lockedUntil=null` |

---

## Checklist tổng thể

### Thứ tự ưu tiên implement

| Priority | Nhóm test | Lý do |
|----------|-----------|-------|
| P0 — Critical | LGN (login), REG (register), JWT security | Core auth flow, security |
| P1 — High | VFY (verify email), RFR (refresh), LGT (logout) | Token lifecycle |
| P2 — Medium | FPW + RPW (forgot/reset password), account locking | Password recovery |
| P3 — Normal | Staff auth flows, rate limiting | Staff-specific features |

### Công cụ đề xuất

| Loại | Tool |
|------|------|
| Unit test (Controller) | `@WebMvcTest` + `@MockBean` (AuthService) |
| Unit test (Service) | `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` |
| Integration test | `@SpringBootTest` + `@AutoConfigureMockMvc` + H2 in-memory |
| JWT test | `JwtProvider` với `ReflectionTestUtils` để inject secret |
| Email mock | `@MockBean EmailService` |
| Google OAuth mock | `@MockBean` GoogleIdTokenVerifier |
