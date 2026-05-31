# TC-UC-03 — Test Cases: Khôi Phục Mật Khẩu (Reset Password)

> **Feature:** `feat-auth` | **UC:** UC-03 | **Version:** 1.0
> **Nguồn AC:** UC-03 § 8 | **Nguồn FR-TEST:** SPEC.md § 3.1.1, § 3.3.2
> **Cập nhật:** 2026-05-30

---

## 1. UNIT TESTS — AuthService (JUnit 5 + Mockito)

> **File:** `AuthServiceTest.java` | **Tag:** `@Tag("unit")`

---

### TC-U-03-01 — forgotPassword với email hợp lệ: tạo token và gửi email

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-01 |
| **Tham chiếu** | AC-03-01, BR-03-02 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
// Clock fixed tại 2026-05-30T08:00:00Z
StudentUser user = aStudent().withEmail("active@test.com").withStatus("active").build();
when(userRepository.findByEmail("active@test.com")).thenReturn(Optional.of(user));
when(tokenRepository.countByStudentIdAndTokenTypeAndCreatedAtAfter(...)).thenReturn(0L);
```

**Steps:**
1. Gọi `authService.forgotPassword("active@test.com", "127.0.0.1")`

**Expected:**
- `tokenRepository.save()` được gọi với:
  - `tokenType = "password_reset"`
  - `expiresAt = 2026-05-30T08:15:00Z` (NOW + 15 phút)
  - `tokenValue.length() >= 32` (≥ 32 bytes URL-safe)
- `emailService.sendResetPasswordEmail()` được gọi một lần
- KHÔNG ném exception
- Trả về generic success response (HTTP 200 equivalent)

---

### TC-U-03-02 — forgotPassword với email không tồn tại: KHÔNG gửi email, vẫn trả thành công

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-02 |
| **Tham chiếu** | AC-03-02, BR-03-01 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 (Security — chống email enumeration) |

**Setup:**
```java
when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
```

**Steps:**
1. Gọi `authService.forgotPassword("ghost@test.com", "127.0.0.1")`

**Expected:**
- KHÔNG ném exception
- `tokenRepository.save()` KHÔNG được gọi
- `emailService` KHÔNG được gọi
- Trả về cùng response type như TC-U-03-01

---

### TC-U-03-03 — forgotPassword thu hồi token cũ trước khi tạo token mới

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-03 |
| **Tham chiếu** | BR-03-09 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
AuthToken oldToken = buildValidPasswordResetToken(studentId);
when(tokenRepository.findValidByStudentAndType(studentId, "password_reset"))
    .thenReturn(List.of(oldToken));
```

**Steps:**
1. Gọi `authService.forgotPassword("active@test.com", "127.0.0.1")`

**Expected:**
- Verify call order: `tokenRepository.revokeAll(oldTokens)` được gọi TRƯỚC `tokenRepository.save(newToken)`
- Chỉ có một token mới hợp lệ sau đó

---

### TC-U-03-04 — resetPassword thành công: cập nhật hash, thu hồi sessions

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-04 |
| **Tham chiếu** | AC-03-03, BR-03-05 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
AuthToken token = buildValidToken("password_reset", studentId, /* expires = NOW+5min */);
when(tokenRepository.findByTokenValueAndTokenType("valid-tok", "password_reset"))
    .thenReturn(Optional.of(token));
StudentUser user = aStudent().withPasswordHash(bcrypt("OldPass12")).build();
when(userRepository.findById(studentId)).thenReturn(Optional.of(user));
when(passwordEncoder.matches("NewPass12", user.getPasswordHash())).thenReturn(false);
```

**Steps:**
1. Gọi `authService.resetPassword("valid-tok", "NewPass12", "NewPass12")`

**Expected:**
- `userRepository.save()` được gọi với:
  - `passwordHash` = hash của `"NewPass12"` (KHÔNG phải cũ)
  - `passwordChangedAt ≈ NOW()`
- `tokenRepository.revokeAllByStudentAndTypes(studentId, ["session", "password_reset"])` được gọi
- KHÔNG ném exception

---

### TC-U-03-05 — resetPassword với token hết hạn (sau 15 phút)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-05 |
| **Tham chiếu** | AC-03-04, FR-TEST-U-05 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
// Clock fixed tại 2026-05-30T08:00:00Z
AuthToken token = new AuthToken();
token.setExpiresAt(Instant.parse("2026-05-30T07:44:00Z")); // hết hạn 16 phút trước
token.setRevokedAt(null);
```

**Steps:**
1. Gọi `authService.resetPassword("expired-tok", "NewPass12", "NewPass12")`

**Expected:**
- Ném `InvalidTokenException` (HTTP 400 / `INVALID_TOKEN`)
- `userRepository.save()` KHÔNG được gọi
- `tokenRepository.revokeAll()` KHÔNG được gọi
- Mật khẩu KHÔNG thay đổi

---

### TC-U-03-06 — resetPassword với token đã sử dụng (revoked_at đã đặt)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-06 |
| **Tham chiếu** | AC-03-05, BR-03-04 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
AuthToken usedToken = new AuthToken();
usedToken.setRevokedAt(Instant.now().minus(5, ChronoUnit.MINUTES)); // đã dùng 5 phút trước
usedToken.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES)); // chưa hết hạn
```

**Steps:**
1. Gọi `authService.resetPassword("used-tok", "NewPass12", "NewPass12")`

**Expected:**
- Ném `InvalidTokenException`
- Mật khẩu KHÔNG thay đổi

---

### TC-U-03-07 — resetPassword với mật khẩu mới giống mật khẩu cũ

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-07 |
| **Tham chiếu** | AC-03-07 — BR-03-08 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
StudentUser user = aStudent().withPasswordHash(bcrypt("OldPass12")).build();
when(passwordEncoder.matches("OldPass12", user.getPasswordHash())).thenReturn(true); // mật khẩu mới = cũ
```

**Steps:**
1. Gọi `authService.resetPassword("valid-tok", "OldPass12", "OldPass12")`

**Expected:**
- Ném exception với `errorCode = "SAME_PASSWORD"` (HTTP 422)
- Mật khẩu KHÔNG thay đổi
- Token KHÔNG bị thu hồi (validation thất bại trước khi commit)

---

### TC-U-03-08 — resetPassword với mật khẩu quá yếu

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-08 |
| **Tham chiếu** | AC-03-06, BR-03-07 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Steps:**
1. Gọi `authService.resetPassword("valid-tok", "abc", "abc")`

**Expected:**
- Ném `ValidationException` (HTTP 422 / `WEAK_PASSWORD`)
- Token KHÔNG bị thu hồi (validation xảy ra trước khi query token)

---

### TC-U-03-09 — resetPassword: liên kết KHÔNG được log

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-09 |
| **Tham chiếu** | BR-03-10 |
| **Loại** | Unit — Service (Log inspection) |
| **Ưu tiên** | P0 (Security) |

**Steps:**
1. Gọi `authService.forgotPassword("user@test.com", "127.0.0.1")` (với email tồn tại)
2. Capture tất cả log output

**Expected:**
- Không có log nào chứa giá trị `tokenValue` (sensitive token)
- Không có log nào chứa URL dạng `https://...?token=...`

---

### TC-U-03-10 — forgotPassword rate limit: lần thứ 4 trong 1 giờ bị chặn

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-03-10 |
| **Tham chiếu** | BR-03-06 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
when(tokenRepository.countByStudentIdAndTokenTypeAndCreatedAtAfter(
    studentId, "password_reset", NOW_MINUS_1_HOUR)).thenReturn(3L); // đã đủ 3 lần
```

**Steps:**
1. Gọi `authService.forgotPassword("active@test.com", "127.0.0.1")`

**Expected:**
- Ném `RateLimitExceededException` (HTTP 429)
- `emailService` KHÔNG được gọi

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `AuthRepositoryIT.java` | **Tag:** `@Tag("integration")`

---

### TC-I-03-01 — resetPassword thành công: password_hash thay đổi trong DB

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-03-01 |
| **Tham chiếu** | AC-03-03 |
| **Loại** | Integration — Full Flow |
| **Ưu tiên** | P1 |

**Steps:**
1. Seed user với `password_hash = bcrypt("OldPass12")`
2. Seed token `password_reset` hợp lệ
3. Gọi `authService.resetPassword(tokenValue, "NewPass12", "NewPass12")`
4. Reload user từ DB

**Expected:**
- `BCrypt.checkpw("NewPass12", newHash)` = true
- `BCrypt.checkpw("OldPass12", newHash)` = false
- `password_changed_at` được đặt

---

### TC-I-03-02 — resetPassword thu hồi tất cả session tokens trong DB

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-03-02 |
| **Tham chiếu** | AC-03-03, BR-03-05 |
| **Loại** | Integration — Full Flow |
| **Ưu tiên** | P1 |

**Steps:**
1. Seed user + 2 session tokens (revoked_at = NULL) + 1 password_reset token hợp lệ
2. Gọi `authService.resetPassword(tokenValue, "NewPass12", "NewPass12")`
3. Query tất cả `auth_tokens` của user

**Expected:**
- Tất cả token với `token_type = "session"` có `revoked_at` IS NOT NULL
- Token `password_reset` có `revoked_at` IS NOT NULL
- Đăng nhập bằng `"NewPass12"` thành công
- Đăng nhập bằng `"OldPass12"` thất bại

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `AuthControllerTest.java` | **Tag:** `@Tag("api")`

---

### TC-A-03-01 — POST /api/auth/forgot-password — email tồn tại → HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-03-01 |
| **Tham chiếu** | AC-03-01 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**
```json
{ "email": "active@test.com" }
```

**Expected:**
```
HTTP 200
{ "message": "Nếu email tồn tại, bạn sẽ nhận được liên kết..." }
```

---

### TC-A-03-02 — POST /api/auth/forgot-password — email không tồn tại → HTTP 200 (giống hệt)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-03-02 |
| **Tham chiếu** | AC-03-02, FR-TEST-A-12, BR-03-01 |
| **Loại** | API — Security (Anti-enumeration) |
| **Ưu tiên** | P0 |

**Request:**
```json
{ "email": "ghost@test.com" }
```

**Expected:**
```
HTTP 200
```
- Response body **giống hệt** với TC-A-03-01 (cùng status code, cùng message structure)
- Response KHÔNG chứa thông tin về việc email có tồn tại hay không

---

### TC-A-03-03 — POST /api/auth/forgot-password — email sai định dạng → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-03-03 |
| **Tham chiếu** | UC-03 § 5 Validation |
| **Loại** | API — Validation |
| **Ưu tiên** | P1 |

**Request:**
```json
{ "email": "not-email" }
```

**Expected:**
```
HTTP 400
{ "errorCode": "VALIDATION_FAILED" }
```

---

### TC-A-03-04 — POST /api/auth/reset-password — thành công → HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-03-04 |
| **Tham chiếu** | AC-03-03 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**
```json
{ "token": "valid-token", "newPassword": "NewPass12", "confirmPassword": "NewPass12" }
```

**Mock:** `authService.resetPassword()` thành công

**Expected:**
```
HTTP 200
```

---

### TC-A-03-05 — POST /api/auth/reset-password — token hết hạn → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-03-05 |
| **Tham chiếu** | AC-03-04, FR-TEST-A-13 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `authService.resetPassword()` ném `InvalidTokenException("EXPIRED")`

**Expected:**
```
HTTP 400
{ "errorCode": "INVALID_TOKEN" }
```

---

### TC-A-03-06 — POST /api/auth/reset-password — mật khẩu quá yếu → HTTP 422

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-03-06 |
| **Tham chiếu** | AC-03-06 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:**
```json
{ "token": "valid-token", "newPassword": "abc", "confirmPassword": "abc" }
```

**Expected:**
```
HTTP 422
{ "errorCode": "WEAK_PASSWORD" }
```

---

### TC-A-03-07 — POST /api/auth/reset-password — mật khẩu mới = mật khẩu cũ → HTTP 422

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-03-07 |
| **Tham chiếu** | AC-03-07 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Mock:** `authService.resetPassword()` ném `SamePasswordException`

**Expected:**
```
HTTP 422
{ "errorCode": "SAME_PASSWORD", "message": "Mật khẩu mới không được giống mật khẩu cũ" }
```

---

### TC-A-03-08 — POST /api/auth/reset-password — token field rỗng → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-03-08 |
| **Tham chiếu** | UC-03 § 5 Validation |
| **Loại** | API — Validation |
| **Ưu tiên** | P1 |

**Request:**
```json
{ "token": "", "newPassword": "NewPass12", "confirmPassword": "NewPass12" }
```

**Expected:**
```
HTTP 400
{ "errorCode": "VALIDATION_FAILED", "errors": [{ "field": "token", "message": "Token là bắt buộc" }] }
```

---

## 4. VALIDATION MATRIX — POST /api/auth/reset-password

| TC ID | Field | Input | Expected HTTP | Error |
|:---|:---|:---|:---|:---|
| TC-A-03-V01 | token | null/rỗng | 400 | VALIDATION_FAILED |
| TC-A-03-V02 | newPassword | null | 400 | VALIDATION_FAILED |
| TC-A-03-V03 | newPassword | `"abc"` | 422 | WEAK_PASSWORD |
| TC-A-03-V04 | newPassword | `"abcdefgh"` (no uppercase, no digit) | 422 | WEAK_PASSWORD |
| TC-A-03-V05 | newPassword | `"Abcdefgh"` (no digit) | 422 | WEAK_PASSWORD |
| TC-A-03-V06 | newPassword | `"12345678"` (no uppercase) | 422 | WEAK_PASSWORD |
| TC-A-03-V07 | confirmPassword | không khớp newPassword | 400 | PASSWORD_MISMATCH |
| TC-A-03-V08 | tất cả hợp lệ | `"NewPass12"` | 200 (mock) | — |

---

## 5. SECURITY INVARIANT TESTS

---

### TC-S-03-01 — forgot-password response giống nhau cho email tồn tại và không tồn tại

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-03-01 |
| **Tham chiếu** | BR-03-01 — Anti-enumeration |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**
1. Gửi `POST /api/auth/forgot-password` với email tồn tại, capture response body và HTTP code
2. Gửi `POST /api/auth/forgot-password` với email không tồn tại, capture response body và HTTP code
3. So sánh

**Expected:**
- HTTP status code GIỐNG NHAU (đều 200)
- Response body structure GIỐNG NHAU
- Thời gian response KHÔNG khác biệt quá 200ms (timing attack prevention)

---

### TC-S-03-02 — Token reset password KHÔNG xuất hiện trong log

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-03-02 |
| **Tham chiếu** | BR-03-10 |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**
1. Enable log capture
2. Gọi `authService.forgotPassword(existingEmail, ip)`
3. Capture token value được tạo
4. Search toàn bộ log output cho token value đó

**Expected:**
- Token value KHÔNG xuất hiện trong bất kỳ log level nào (DEBUG, INFO, WARN, ERROR)

---

## 6. COVERAGE CHECKLIST

| Business Rule | Test Case | Covered? |
|:---|:---|:---|
| BR-03-01: Luôn trả HTTP 200, không tiết lộ email | TC-U-03-02, TC-A-03-02, TC-S-03-01 | ✅ |
| BR-03-02: Token hết hạn sau 15 phút | TC-U-03-01, TC-U-03-05 | ✅ |
| BR-03-03: Token ≥ 32 bytes | TC-U-03-01 | ✅ |
| BR-03-04: Token dùng một lần | TC-U-03-06 | ✅ |
| BR-03-05: Thu hồi tất cả session sau reset | TC-U-03-04, TC-I-03-02 | ✅ |
| BR-03-06: Rate limit 3 req/giờ | TC-U-03-10 | ✅ |
| BR-03-07: Mật khẩu mới đủ mạnh | TC-U-03-08, TC-A-03-06 | ✅ |
| BR-03-08: Mật khẩu mới ≠ mật khẩu cũ | TC-U-03-07, TC-A-03-07 | ✅ |
| BR-03-09: Thu hồi token cũ trước khi tạo mới | TC-U-03-03 | ✅ |
| BR-03-10: Token không được log | TC-U-03-09, TC-S-03-02 | ✅ |
