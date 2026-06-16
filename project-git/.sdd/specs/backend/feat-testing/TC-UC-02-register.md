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
- `tokenRepository.save()` được gọi với `tokenType = "email_verification"`, `expiresAt ≈ NOW() + 24h`
- `emailService.sendVerificationEmail()` được gọi một lần
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

### TC-U-02-04 — Token xác minh chỉ sử dụng được một lần

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-04 |
| **Tham chiếu** | AC-02-07, BR-02-04, FR-TEST-U-08 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 — CRITICAL |

**Setup:**
```java
AuthToken token = new AuthToken();
token.setTokenValue("valid-token-abc");
token.setTokenType("email_verification");
token.setRevokedAt(Instant.now()); // ĐÃ bị thu hồi
token.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS)); // chưa hết hạn

when(tokenRepository.findByTokenValueAndTokenType("valid-token-abc", "email_verification"))
    .thenReturn(Optional.of(token));
```

**Steps:**
1. Gọi `authService.verifyEmail("valid-token-abc")`

**Expected:**
- Ném `InvalidTokenException`
- `userRepository.updateStatus()` KHÔNG được gọi

---

### TC-U-02-05 — Token xác minh hết hạn sau 24 giờ

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-05 |
| **Tham chiếu** | AC-02-06, BR-02-03 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
// Fixed clock tại 2026-05-30T08:00:00Z
AuthToken token = new AuthToken();
token.setRevokedAt(null);
token.setExpiresAt(Instant.parse("2026-05-29T08:00:00Z")); // đã hết hạn hơn 24h trước
```

**Steps:**
1. Gọi `authService.verifyEmail("expired-token")`

**Expected:**
- Ném `InvalidTokenException` với message chứa "hết hạn"
- `userRepository.updateStatus()` KHÔNG được gọi

---

### TC-U-02-06 — Xác minh email thành công: status → active, token bị thu hồi

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-06 |
| **Tham chiếu** | AC-02-05 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
AuthToken token = validEmailVerificationToken();
when(tokenRepository.findByTokenValueAndTokenType("valid-tok", "email_verification"))
    .thenReturn(Optional.of(token));
StudentUser pendingUser = aStudent().withStatus("pending").build();
when(userRepository.findById(token.getStudentId())).thenReturn(Optional.of(pendingUser));
```

**Steps:**
1. Gọi `authService.verifyEmail("valid-tok")`

**Expected:**
- `userRepository.save()` được gọi với `status = "active"`, `emailVerifiedAt ≈ NOW()`
- `tokenRepository.save()` được gọi với `revokedAt ≈ NOW()`
- Không ném exception

---

### TC-U-02-07 — Gửi lại email xác minh rate limit: lần thứ 4 trong 1 giờ bị chặn

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-07 |
| **Tham chiếu** | AC-02-08, BR-02-05 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
// Giả sử đã có 3 token 'email_verification' được tạo trong 1 giờ qua
when(tokenRepository.countByStudentIdAndTokenTypeAndCreatedAtAfter(
    studentId, "email_verification", NOW_MINUS_1_HOUR)).thenReturn(3L);
```

**Steps:**
1. Gọi `authService.resendVerificationEmail("pending@test.com")`

**Expected:**
- Ném `RateLimitExceededException` (HTTP 429)
- `emailService` KHÔNG được gọi

---

### TC-U-02-08 — Gửi lại email xác minh thu hồi token cũ trước khi tạo mới

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-02-08 |
| **Tham chiếu** | BR-02-09 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
when(tokenRepository.countByStudentIdAndTokenTypeAndCreatedAtAfter(...)).thenReturn(0L);
List<AuthToken> oldTokens = List.of(buildOldValidToken(), buildOldValidToken());
when(tokenRepository.findValidByStudentAndType(studentId, "email_verification"))
    .thenReturn(oldTokens);
```

**Steps:**
1. Gọi `authService.resendVerificationEmail("pending@test.com")`

**Expected:**
- `tokenRepository.revokeAll(oldTokens)` được gọi TRƯỚC khi tạo token mới
- `tokenRepository.save(newToken)` được gọi một lần
- `emailService.sendVerificationEmail()` được gọi

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
1. Seed user `status = "pending"` và token `email_verification` hợp lệ
2. Gọi `authService.verifyEmail(tokenValue)`
3. Query lại DB

**Expected:**
- `student_users.status = "active"`
- `student_users.email_verified_at` IS NOT NULL
- `auth_tokens.revoked_at` IS NOT NULL (token đã bị thu hồi)

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

### TC-A-02-07 — POST /api/auth/verify-email — token hợp lệ → HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-07 |
| **Tham chiếu** | AC-02-05 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `authService.verifyEmail()` thành công

**Expected:**
```
HTTP 200
```

---

### TC-A-02-08 — POST /api/auth/verify-email — token hết hạn → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-08 |
| **Tham chiếu** | AC-02-06, FR-TEST-A-13 (tương tự) |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `authService.verifyEmail()` ném `InvalidTokenException("EXPIRED")`

**Expected:**
```
HTTP 400
{ "errorCode": "INVALID_TOKEN" }
```

---

### TC-A-02-09 — POST /api/auth/resend-verification — email không tồn tại → vẫn HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-09 |
| **Tham chiếu** | BR-02-08 — chống email enumeration |
| **Loại** | API — Security |
| **Ưu tiên** | P0 (Security) |

**Mock:** `authService.resendVerificationEmail()` xử lý không ném exception

**Expected:**
```
HTTP 200
```
- Response body KHÔNG phân biệt giữa "email tồn tại" và "email không tồn tại"

---

### TC-A-02-10 — POST /api/auth/resend-verification — rate limit vượt → HTTP 429

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-02-10 |
| **Tham chiếu** | AC-02-08 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Mock:** `authService.resendVerificationEmail()` ném `RateLimitExceededException`

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

### TC-F-02-02 — Sau đăng ký thành công, hiển thị thông báo kiểm tra email

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
- Hiển thị message "Kiểm tra email để xác minh" (hoặc tương tự)
- Form đăng ký KHÔNG còn hiển thị

---

## 6. COVERAGE CHECKLIST

| Business Rule | Test Case | Covered? |
|:---|:---|:---|
| BR-02-01: status = pending khi tạo mới | TC-U-02-01, TC-I-02-01 | ✅ |
| BR-02-02: bcrypt cost ≥ 10 | TC-U-02-02 | ✅ |
| BR-02-03: Token hết hạn sau 24h | TC-U-02-05 | ✅ |
| BR-02-04: Token dùng một lần | TC-U-02-04 | ✅ |
| BR-02-05: Rate limit 3 lần/giờ | TC-U-02-07 | ✅ |
| BR-02-08: Resend không tiết lộ email | TC-U-02-09, TC-A-02-09 | ✅ |
| BR-02-09: Thu hồi token cũ khi resend | TC-U-02-08 | ✅ |
