# TC-UC-02 — Test Cases: Đăng Ký Tài Khoản (User Register)

> **Feature:** `feat-auth` | **UC:** UC-02 | **Version:** 1.0
> **Nguồn AC:** UC-02 § 8 | **Nguồn FR-TEST:** SPEC.md § 3.1.1, § 3.3.2
> **Cập nhật:** 2026-05-30

---

## 1. UNIT TESTS — AuthService (JUnit 5 + Mockito)

> **File:** `AuthServiceTest.java` | **Tag:** `@Tag("unit")`

---

### TC-U-02-01 — Đăng ký thành công: tạo user với status = pending

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-01 |
| **Tham chiếu** | AC-02-01, BR-02-01 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**

```java
when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
when(passwordEncoder.encode("Abcdef12")).thenReturn("$2a$10$hashed");
```

**Steps:**

1. Gọi `authService.registerStudent(RegisterRequest("Nguyễn Văn A", "new@test.com", "Abcdef12", "Abcdef12"))`

**Expected:**

- `userRepository.save()` được gọi với entity có `status = "pending"`
- `entity.emailVerifiedAt` = NULL
- `entity.passwordHash` = `"$2a$10$hashed"` (KHÔNG phải plaintext)
- `entity.passwordHash` ≠ `"Abcdef12"`
- `tokenRepository.save()` được gọi với `tokenType = "email_verification"`, `tokenValue` là chuỗi 6 chữ số (sinh bằng `SecureRandom`), `expiresAt ≈ NOW() + 10 phút`
- `emailService.sendVerificationEmail()` (mã OTP, không kèm liên kết) được gọi một lần
- Trả về `RegisterResponse(studentId, email = "new@test.com")`
- HTTP equivalent: 201

---

### TC-U-02-02 — Mật khẩu được hash bằng bcrypt, không lưu plaintext

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-02 |
| **Tham chiếu** | BR-02-02, FR-TEST-U-04 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 — CRITICAL |

**Setup:**

```java
when(userRepository.existsByEmail(any())).thenReturn(false);
// Dùng BCryptPasswordEncoder thật (không mock) để kiểm tra
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
authService = new AuthService(userRepository, encoder, ...);
```

**Steps:**

1. Gọi `authService.registerStudent(...)` với password = `"TestPass12"`

**Expected:**

- Giá trị được lưu vào `passwordHash` bắt đầu bằng `"$2a$10$"` (bcrypt format)
- `passwordHash` ≠ `"TestPass12"`
- `BCrypt.checkpw("TestPass12", storedHash)` = true
- Cost factor ≥ 10 (verify từ hash format)

---

### TC-U-02-03 — Email đã tồn tại → ném EmailExistsException

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-03 |
| **Tham chiếu** | AC-02-02, BR-02-01 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**

```java
when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);
```

**Steps:**

1. Gọi `authService.registerStudent(...)` với email `"existing@test.com"`

**Expected:**

- Ném `EmailExistsException` (HTTP 409)
- `userRepository.save()` KHÔNG được gọi
- `emailService` KHÔNG được gọi

---

### TC-U-02-04 — Mã OTP đã dùng (đã bị xoá) không thể xác minh lại

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-04 |
| **Tham chiếu** | AC-02-07, BR-02-04, FR-TEST-U-08 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 — CRITICAL |

**Setup:**

```java
StudentUser pendingUser = aStudent().withStatus("pending").build();
when(studentUserRepository.findByEmail("pending@test.com")).thenReturn(Optional.of(pendingUser));

// Mã OTP trước đó đã xác minh thành công → toàn bộ token EMAIL_VERIFICATION của user đã bị XOÁ
when(tokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
        pendingUser.getId(), AuthToken.TokenType.EMAIL_VERIFICATION))
    .thenReturn(Optional.empty());
```

**Steps:**

1. Gọi `authService.verifyEmail(new VerifyEmailRequest("pending@test.com", "123456"))`

**Expected:**

- Ném `BusinessException(400, "OTP_EXPIRED", ...)`
- `studentUserRepository.save()` KHÔNG được gọi với `status = "active"`

---

### TC-U-02-05 — Mã OTP hết hạn sau 10 phút

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-05 |
| **Tham chiếu** | AC-02-06, BR-02-03 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**

```java
// Fixed clock tại 2026-05-30T08:10:01
AuthToken token = new AuthToken();
token.setTokenValue("654321");
token.setTokenType(AuthToken.TokenType.EMAIL_VERIFICATION);
token.setExpiresAt(LocalDateTime.parse("2026-05-30T08:00:00")); // sinh lúc 07:50:00, hết hạn sau 10 phút

when(tokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(any(), any()))
    .thenReturn(Optional.of(token));
```

**Steps:**

1. Gọi `authService.verifyEmail(new VerifyEmailRequest("pending@test.com", "654321"))`

**Expected:**

- Ném `BusinessException(400, "OTP_EXPIRED", ...)`
- `studentUserRepository.save()` KHÔNG được gọi với `status = "active"`

---

### TC-U-02-06 — Xác minh mã OTP thành công: status → active, token OTP bị xoá (không phải thu hồi)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-06 |
| **Tham chiếu** | AC-02-05 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**

```java
StudentUser pendingUser = aStudent().withStatus("pending").build();
when(studentUserRepository.findByEmail("pending@test.com")).thenReturn(Optional.of(pendingUser));

AuthToken token = validEmailVerificationOtpToken("123456"); // chưa hết hạn
when(tokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
        pendingUser.getId(), AuthToken.TokenType.EMAIL_VERIFICATION))
    .thenReturn(Optional.of(token));
```

**Steps:**

1. Gọi `authService.verifyEmail(new VerifyEmailRequest("pending@test.com", "123456"))`

**Expected:**

- `studentUserRepository.save()` được gọi với `status = "active"`, `emailVerifiedAt ≈ NOW()`
- `tokenRepository.deleteByStudentIdAndTokenType(studentId, EMAIL_VERIFICATION)` được gọi (XOÁ, không set `revokedAt`)
- Không ném exception

---

### TC-U-02-07 — Gửi lại mã OTP bị chặn khi chưa đủ 60 giây kể từ lần gửi trước

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-07 |
| **Tham chiếu** | AC-02-08, BR-02-05 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**

```java
// Token EMAIL_VERIFICATION gần nhất được tạo cách đây 30 giây (< 60s cooldown)
AuthToken lastToken = new AuthToken();
lastToken.setCreatedAt(LocalDateTime.now().minusSeconds(30));
when(tokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
        studentId, AuthToken.TokenType.EMAIL_VERIFICATION))
    .thenReturn(Optional.of(lastToken));
```

**Steps:**

1. Gọi `authService.resendVerification(new ResendVerificationRequest("pending@test.com"))`

**Expected:**

- Ném `BusinessException(429, "TOO_MANY_REQUESTS", ...)`
- `emailService`/`eventPublisher` KHÔNG được gọi

---

### TC-U-02-08 — Gửi lại mã OTP xoá token cũ trước khi tạo mã mới

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-08 |
| **Tham chiếu** | BR-02-09 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**

```java
// Token cũ được tạo hơn 60 giây trước → không bị rate limit
AuthToken lastToken = new AuthToken();
lastToken.setCreatedAt(LocalDateTime.now().minusMinutes(5));
when(tokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
        studentId, AuthToken.TokenType.EMAIL_VERIFICATION))
    .thenReturn(Optional.of(lastToken));
```

**Steps:**

1. Gọi `authService.resendVerification(new ResendVerificationRequest("pending@test.com"))`

**Expected:**

- `tokenRepository.deleteByStudentIdAndTokenType(studentId, EMAIL_VERIFICATION)` được gọi TRƯỚC khi tạo token mới
- `tokenRepository.save(newToken)` được gọi một lần với `tokenValue` là chuỗi 6 chữ số mới, `expiresAt ≈ NOW() + 10 phút`
- `eventPublisher.publishEvent(new SendVerificationEmailEvent(...))` được gọi

---

### TC-U-02-09 — Gửi lại email xác minh cho email không tồn tại → vẫn trả thành công (không tiết lộ)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-09 |
| **Tham chiếu** | BR-02-08 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 (Security) |

**Setup:**

```java
when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
```

**Steps:**

1. Gọi `authService.resendVerificationEmail("ghost@test.com")`

**Expected:**

- KHÔNG ném exception
- `emailService` KHÔNG được gọi
- Trả về "thành công" chung chung (HTTP 200 equivalent)

---

### TC-U-02-10 — Nhập sai mã OTP quá 5 lần liên tiếp → khoá, xoá token

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-10 |
| **Tham chiếu** | BR-02-06 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 — Security |

**Setup:**

```java
StudentUser pendingUser = aStudent().withStatus("pending").build();
when(studentUserRepository.findByEmail("pending@test.com")).thenReturn(Optional.of(pendingUser));

AuthToken token = validEmailVerificationOtpToken("123456"); // chưa hết hạn
when(tokenRepository.findFirstByStudentIdAndTokenTypeOrderByCreatedAtDesc(
        pendingUser.getId(), AuthToken.TokenType.EMAIL_VERIFICATION))
    .thenReturn(Optional.of(token));
```

**Steps:**

1. Gọi `authService.verifyEmail(new VerifyEmailRequest("pending@test.com", "000000"))` (sai) 5 lần liên tiếp
2. Gọi lần thứ 6 với `otpCode = "000000"` (vẫn sai) hoặc thậm chí `"123456"` (đúng)

**Expected:**

- 5 lần đầu: mỗi lần ném `BusinessException(400, "INVALID_OTP", ...)`
- Lần thứ 6: ném `BusinessException(429, "TOO_MANY_ATTEMPTS", ...)` — bất kể mã đúng hay sai
- `tokenRepository.deleteByStudentIdAndTokenType(studentId, EMAIL_VERIFICATION)` được gọi ở lần thứ 6 (buộc phải gửi lại mã mới)

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `AuthRepositoryIT.java` | **Tag:** `@Tag("integration")`

---

### TC-I-02-01 — Đăng ký thành công: user tồn tại trong DB với status = pending

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-02-01 |
| **Tham chiếu** | AC-02-01 |
| **Loại** | Integration — Full Service |
| **Ưu tiên** | P1 |

**Steps:**

1. Gọi `authService.registerStudent(...)` với email chưa tồn tại
2. Query `userRepository.findByEmail("new@test.com")`

**Expected:**

- Record tìm thấy với `status = "pending"`, `emailVerifiedAt = NULL`
- `passwordHash` có format bcrypt (`$2a$10$...`)
- `createdAt` được đặt

---

### TC-I-02-02 — Xác minh email: trạng thái DB thay đổi đúng

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-02-02 |
| **Tham chiếu** | AC-02-05 |
| **Loại** | Integration — Full Flow |
| **Ưu tiên** | P1 |

**Steps:**

1. Seed user `status = "pending"` và token `email_verification` (mã OTP 6 chữ số) còn hạn 10 phút
2. Gọi `authService.verifyEmail(new VerifyEmailRequest(email, otpCode))`
3. Query lại DB

**Expected:**

- `student_users.status = "active"`
- `student_users.email_verified_at` IS NOT NULL
- Bản ghi `auth_tokens` (token_type='email_verification') của user đã bị **xoá hẳn** (không còn row, không phải set `revoked_at`)

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `AuthControllerTest.java` | **Tag:** `@Tag("api")`

---

### TC-A-02-01 — POST /api/auth/register — thành công → HTTP 201

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-01 |
| **Tham chiếu** | AC-02-01 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "new@test.com",
  "password": "Abcdef12",
  "confirmPassword": "Abcdef12"
}
```

**Expected:**

```
HTTP 201
{ "studentId": <number>, "email": "new@test.com" }
```

- Response KHÔNG chứa `passwordHash`

---

### TC-A-02-02 — POST /api/auth/register — email trùng → HTTP 409

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-02 |
| **Tham chiếu** | AC-02-02, FR-TEST-A-11 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `authService.registerStudent()` ném `EmailExistsException`

**Expected:**

```
HTTP 409
{ "errorCode": "EMAIL_EXISTS" }
```

---

### TC-A-02-03 — POST /api/auth/register — mật khẩu quá yếu → HTTP 422

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-03 |
| **Tham chiếu** | AC-02-03 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:**

```json
{ "fullName": "Test", "email": "t@t.com", "password": "abc", "confirmPassword": "abc" }
```

**Expected:**

```
HTTP 422
{ "errorCode": "WEAK_PASSWORD" }
```

---

### TC-A-02-04 — POST /api/auth/register — mật khẩu xác nhận không khớp → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-04 |
| **Tham chiếu** | AC-02-04 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:**

```json
{ "fullName": "Test", "email": "t@t.com", "password": "Abcdef12", "confirmPassword": "Abcdef99" }
```

**Expected:**

```
HTTP 400
{ "errorCode": "PASSWORD_MISMATCH" }
```

---

### TC-A-02-05 — POST /api/auth/register — fullName rỗng → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-05 |
| **Tham chiếu** | UC-02 § 5 Validation |
| **Loại** | API — Validation |
| **Ưu tiên** | P1 |

**Request:**

```json
{ "fullName": "", "email": "t@t.com", "password": "Abcdef12", "confirmPassword": "Abcdef12" }
```

**Expected:**

```
HTTP 400
{ "errorCode": "VALIDATION_FAILED", "errors": [{ "field": "fullName", "message": "Họ tên là bắt buộc" }] }
```

---

### TC-A-02-06 — POST /api/auth/register — fullName quá ngắn (1 ký tự) → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-06 |
| **Tham chiếu** | UC-02 § 5 Validation |
| **Loại** | API — Validation |
| **Ưu tiên** | P2 |

**Request:**

```json
{ "fullName": "A", "email": "t@t.com", "password": "Abcdef12", "confirmPassword": "Abcdef12" }
```

**Expected:**

```
HTTP 400
{ "errors": [{ "field": "fullName", "message": "Họ tên phải có ít nhất 2 ký tự" }] }
```

---

### TC-A-02-07 — POST /api/auth/verify-email — mã OTP hợp lệ → HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-07 |
| **Tham chiếu** | AC-02-05 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**

```json
{ "email": "pending@test.com", "otpCode": "123456" }
```

**Mock:** `authService.verifyEmail()` thành công (không ném exception)

**Expected:**

```
HTTP 200
```

---

### TC-A-02-08 — POST /api/auth/verify-email — mã OTP hết hạn → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-08 |
| **Tham chiếu** | AC-02-06, FR-TEST-A-13 (tương tự) |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**

```json
{ "email": "pending@test.com", "otpCode": "123456" }
```

**Mock:** `authService.verifyEmail()` ném `BusinessException(400, "OTP_EXPIRED", ...)`

**Expected:**

```
HTTP 400
{ "errorCode": "OTP_EXPIRED" }
```

---

### TC-A-02-08b — POST /api/auth/verify-email — mã OTP sai → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-08b |
| **Tham chiếu** | AC-02-09 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**

```json
{ "email": "pending@test.com", "otpCode": "000000" }
```

**Mock:** `authService.verifyEmail()` ném `BusinessException(400, "INVALID_OTP", ...)`

**Expected:**

```
HTTP 400
{ "errorCode": "INVALID_OTP" }
```

---

### TC-A-02-08c — POST /api/auth/verify-email — sai OTP quá 5 lần → HTTP 429

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-08c |
| **Tham chiếu** | BR-02-06 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 — Security |

**Request:**

```json
{ "email": "pending@test.com", "otpCode": "000000" }
```

**Mock:** `authService.verifyEmail()` ném `BusinessException(429, "TOO_MANY_ATTEMPTS", ...)`

**Expected:**

```
HTTP 429
{ "errorCode": "TOO_MANY_ATTEMPTS" }
```

- Client phải điều hướng người dùng sang luồng "Gửi lại mã xác minh" (không thể thử lại mã cũ)

---

### TC-A-02-09 — POST /api/auth/resend-verification — email không tồn tại → vẫn HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-09 |
| **Tham chiếu** | BR-02-08 — chống email enumeration |
| **Loại** | API — Security |
| **Ưu tiên** | P0 (Security) |

**Request:**

```json
{ "email": "ghost@test.com" }
```

**Mock:** `authService.resendVerification()` xử lý không ném exception

**Expected:**

```
HTTP 200
```

- Response body KHÔNG phân biệt giữa "email tồn tại" và "email không tồn tại"

---

### TC-A-02-10 — POST /api/auth/resend-verification — chưa đủ 60 giây → HTTP 429

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-10 |
| **Tham chiếu** | AC-02-08 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:**

```json
{ "email": "pending@test.com" }
```

**Mock:** `authService.resendVerification()` ném `BusinessException(429, "TOO_MANY_REQUESTS", ...)` (chưa đủ 60 giây kể từ lần gửi trước)

**Expected:**

```
HTTP 429
{ "errorCode": "TOO_MANY_REQUESTS" }
```

---

## 4. VALIDATION MATRIX — POST /api/auth/register

| TC ID | Field | Input | Expected HTTP | Error Code |
|:---|:---|:---|:---|:---|
| TC-A-02-V01 | fullName | null | 400 | VALIDATION_FAILED |
| TC-A-02-V02 | fullName | `""` | 400 | VALIDATION_FAILED |
| TC-A-02-V03 | fullName | `"A"` (1 char) | 400 | VALIDATION_FAILED |
| TC-A-02-V04 | fullName | 151 ký tự | 400 | VALIDATION_FAILED |
| TC-A-02-V05 | email | null | 400 | VALIDATION_FAILED |
| TC-A-02-V06 | email | `"notanemail"` | 400 | VALIDATION_FAILED |
| TC-A-02-V07 | email | 256 ký tự | 400 | VALIDATION_FAILED |
| TC-A-02-V08 | password | `"abc"` (< 8 ký tự) | 422 | WEAK_PASSWORD |
| TC-A-02-V09 | password | `"abcdefgh"` (không có hoa, không có số) | 422 | WEAK_PASSWORD |
| TC-A-02-V10 | password | `"ABCDEFGH"` (không có số) | 422 | WEAK_PASSWORD |
| TC-A-02-V11 | password | `"12345678"` (không có hoa) | 422 | WEAK_PASSWORD |
| TC-A-02-V12 | password | `"Abcdef12"` (hợp lệ) | 201 | — |
| TC-A-02-V13 | confirmPassword | không khớp | 400 | PASSWORD_MISMATCH |

---

## 5. FRONTEND COMPONENT TESTS

> **File:** `RegisterForm.test.tsx`

---

### TC-F-02-01 — Hiển thị lỗi validation trên form

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-02-01 |
| **Tham chiếu** | FR-TEST-F-02 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Steps:**

1. Render `<RegisterForm />`
2. Click submit không điền gì

**Expected:**

- Hiển thị lỗi cho các trường: fullName, email, password, confirmPassword
- `POST /api/auth/register` KHÔNG được gọi

---

### TC-F-02-02 — Sau đăng ký thành công, điều hướng thẳng sang trang nhập mã OTP

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-02-02 |
| **Tham chiếu** | AC-02-01 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Mock:** `POST /api/auth/register` → 201

**Steps:**

1. Điền form hợp lệ → submit

**Expected:**

- `navigate('/verify-email?email=<email>')` được gọi ngay (KHÔNG hiển thị card "kiểm tra email" inline như trước)
- Form đăng ký KHÔNG còn hiển thị (đã điều hướng sang trang khác)

---

## 6. COVERAGE CHECKLIST

| Business Rule | Test Case | Covered? |
|:---|:---|:---|
| BR-02-01: status = pending khi tạo mới | TC-U-02-01, TC-I-02-01 | ✅ |
| BR-02-02: bcrypt cost ≥ 10 | TC-U-02-02 | ✅ |
| BR-02-03: Mã OTP hết hạn sau 10 phút | TC-U-02-05 | ✅ |
| BR-02-04: Mã OTP dùng một lần (xoá sau khi xác minh) | TC-U-02-04, TC-U-02-06 | ✅ |
| BR-02-05: Rate limit gửi lại — 1 lần / 60 giây | TC-U-02-07 | ✅ |
| BR-02-06: Khoá sau 5 lần nhập sai OTP liên tiếp | TC-U-02-10, TC-A-02-08c | ✅ |
| BR-02-08: Resend không tiết lộ email | TC-U-02-09, TC-A-02-09 | ✅ |
| BR-02-09: Xoá token cũ khi resend (không phải thu hồi) | TC-U-02-08 | ✅ |
