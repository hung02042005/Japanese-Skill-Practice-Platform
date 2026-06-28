# TC-UC-05 — Test Cases: Đổi Mật Khẩu (Change Password)

> **Feature:** `feat-auth` | **UC:** UC-05 | **Version:** 1.0
> **Nguồn AC:** UC-05 § 8 | **Nguồn FR-TEST:** SPEC.md § 3.1.1, § 3.3.2
> **Cập nhật:** 2026-05-30

---

## 1. UNIT TESTS — StudentService (JUnit 5 + Mockito)

> **File:** `StudentServiceTest.java` | **Tag:** `@Tag("unit")`

---

### TC-U-05-01 — Đổi mật khẩu thành công: session khác bị thu hồi, session hiện tại giữ nguyên

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-05-01 |
| **Tham chiếu** | AC-05-01, BR-05-03 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**

```java
// Session tokens hiện có
String currentSessionToken = "session-token-device-A";
String otherSessionToken = "session-token-device-B";

StudentUser user = aStudent()
    .withPasswordHash(bcrypt("OldPass12"))
    .withOauthProviderId(null) // tài khoản email/password
    .build();

when(userRepository.findById(1L)).thenReturn(Optional.of(user));
when(passwordEncoder.matches("OldPass12", user.getPasswordHash())).thenReturn(true);
when(passwordEncoder.matches("NewPass12", user.getPasswordHash())).thenReturn(false);
```

**Steps:**

1. Gọi `studentService.changePassword(1L, ChangePasswordRequest("OldPass12", "NewPass12", "NewPass12"), currentSessionToken)`

**Expected:**

- `userRepository.save()` được gọi với:
  - `passwordHash` = bcrypt hash của `"NewPass12"` (KHÔNG phải `"OldPass12"`)
  - `passwordChangedAt ≈ NOW()`
- `tokenRepository.revokeOtherSessions(studentId=1, exceptToken=currentSessionToken)` được gọi
- Session token `"session-token-device-B"` bị thu hồi
- Session token `"session-token-device-A"` (hiện tại) KHÔNG bị thu hồi
- Không ném exception

---

### TC-U-05-02 — Mật khẩu hiện tại sai: không cập nhật gì, không tăng login_attempts

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-05-02 |
| **Tham chiếu** | AC-05-02, BR-05-05 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**

```java
StudentUser user = aStudent().withLoginAttempts(0).build();
when(passwordEncoder.matches("WrongPass12", user.getPasswordHash())).thenReturn(false);
```

**Steps:**

1. Gọi `studentService.changePassword(1L, ChangePasswordRequest("WrongPass12", "NewPass12", "NewPass12"), "current-token")`

**Expected:**

- Ném `WrongPasswordException` (HTTP 400 / `WRONG_PASSWORD`)
- `userRepository.save()` KHÔNG được gọi (mật khẩu không thay đổi)
- `tokenRepository` KHÔNG được gọi (sessions không bị thu hồi)
- `user.loginAttempts` KHÔNG thay đổi (vẫn = 0)

---

### TC-U-05-03 — Mật khẩu mới giống mật khẩu cũ

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-05-03 |
| **Tham chiếu** | AC-05-03, BR-05-02, FR-TEST-U-06 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**

```java
when(passwordEncoder.matches("OldPass12", passwordHash)).thenReturn(true); // xác minh mật khẩu cũ đúng
when(passwordEncoder.matches("OldPass12", passwordHash)).thenReturn(true); // mật khẩu mới = cũ
```

**Steps:**

1. Gọi `studentService.changePassword(1L, ChangePasswordRequest("OldPass12", "OldPass12", "OldPass12"), "token")`

**Expected:**

- Ném `SamePasswordException` (HTTP 422 / `SAME_PASSWORD`)
- Message: "Mật khẩu mới không được giống mật khẩu cũ"
- `userRepository.save()` KHÔNG được gọi
- `tokenRepository` KHÔNG được gọi

---

### TC-U-05-04 — Tài khoản OAuth (password_hash = NULL): không thể đổi mật khẩu

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-05-04 |
| **Tham chiếu** | AC-05-06, BR-05-04 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**

```java
StudentUser oauthUser = aStudent()
    .withPasswordHash(null) // OAuth-only, không có password
    .withOauthProvider("google")
    .build();
when(userRepository.findById(1L)).thenReturn(Optional.of(oauthUser));
```

**Steps:**

1. Gọi `studentService.changePassword(1L, ChangePasswordRequest("any", "NewPass12", "NewPass12"), "token")`

**Expected:**

- Ném `OAuthAccountException` (HTTP 422 / `BUSINESS_RULE_VIOLATION`)
- Message chứa `"google"` (provider name)
- Hướng dẫn cách xử lý
- `passwordEncoder.matches()` KHÔNG được gọi (short-circuit)

---

### TC-U-05-05 — Mật khẩu mới không đủ mạnh

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-05-05 |
| **Tham chiếu** | AC-05-04, BR-05-06 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Steps:**

1. Gọi `studentService.changePassword(1L, ChangePasswordRequest("OldPass12", "abc", "abc"), "token")`

**Expected:**

- Ném `ValidationException` (HTTP 422 / `WEAK_PASSWORD`)
- Message: "Mật khẩu quá yếu..."
- `userRepository.findById()` KHÔNG được gọi (validation thất bại sớm)

---

### TC-U-05-06 — Xác nhận mật khẩu không khớp

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-05-06 |
| **Tham chiếu** | AC-05-05 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Steps:**

1. Gọi `studentService.changePassword(1L, ChangePasswordRequest("OldPass12", "NewPass12", "DiffPass12"), "token")`

**Expected:**

- Ném `ValidationException` (HTTP 400 / `PASSWORD_MISMATCH`)
- `userRepository.findById()` KHÔNG được gọi

---

### TC-U-05-07 — Mật khẩu KHÔNG được log

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-05-07 |
| **Tham chiếu** | BR-05-07 |
| **Loại** | Unit — Service (Log inspection) |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**

1. Enable log capture
2. Gọi `changePassword` với mật khẩu hiện tại sai `"WrongSecret99"`
3. Kiểm tra tất cả log entries

**Expected:**

- Không có log level nào (DEBUG, INFO, WARN, ERROR) chứa `"WrongSecret99"`
- Không có log nào chứa bcrypt hash

---

### TC-U-05-08 — Thứ tự kiểm tra đúng: validation → verify current password → check same password

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-05-08 |
| **Tham chiếu** | UC-05 § 5 — Thứ tự kiểm tra |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Verification (order check):**

1. Validation structure xảy ra TRƯỚC khi gọi `userRepository.findById()`
2. `passwordEncoder.matches(currentPassword, hash)` xảy ra TRƯỚC `passwordEncoder.matches(newPassword, hash)`

**Expected:**

- Khi `newPassword = "abc"` (yếu): ném `ValidationException` mà không call DB
- Khi `confirmPassword` không khớp: ném `ValidationException` mà không call DB

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `StudentRepositoryIT.java` | **Tag:** `@Tag("integration")`

---

### TC-I-05-01 — Đổi mật khẩu: password_hash thay đổi trong DB, password_changed_at được đặt

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-05-01 |
| **Tham chiếu** | AC-05-01, BR-05-08 |
| **Loại** | Integration — Full Flow |
| **Ưu tiên** | P1 |

**Steps:**

1. Seed user với `password_hash = bcrypt("OldPass12")`
2. Seed 2 session tokens (A và B) cho user, cả hai `revoked_at = NULL`
3. Gọi `studentService.changePassword(studentId, request, currentTokenA)`
4. Reload user và tokens từ DB

**Expected:**

- `BCrypt.checkpw("NewPass12", newHash)` = true
- `BCrypt.checkpw("OldPass12", newHash)` = false
- `password_changed_at` IS NOT NULL
- Token A: `revoked_at` = NULL (không bị thu hồi)
- Token B: `revoked_at` IS NOT NULL (bị thu hồi)

---

### TC-I-05-02 — Sau khi đổi mật khẩu: chỉ mật khẩu mới đăng nhập được

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-05-02 |
| **Tham chiếu** | AC-05-01 |
| **Loại** | Integration — End-to-End Logic |
| **Ưu tiên** | P1 |

**Steps:**

1. Thực hiện đổi mật khẩu từ `"OldPass12"` sang `"NewPass12"`
2. Thử đăng nhập với `"OldPass12"`
3. Thử đăng nhập với `"NewPass12"`

**Expected:**

- Đăng nhập với `"OldPass12"` → `InvalidCredentialsException` (thất bại)
- Đăng nhập với `"NewPass12"` → thành công

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `StudentControllerTest.java` | **Tag:** `@Tag("api")`

---

### TC-A-05-01 — PUT /api/students/me/password — không có JWT → HTTP 401

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-05-01 |
| **Tham chiếu** | FR-TEST-A-01 |
| **Loại** | API — Security |
| **Ưu tiên** | P0 |

**Steps:**

1. Gửi `PUT /api/students/me/password` không có header `Authorization`

**Expected:**

```
HTTP 401
```

---

### TC-A-05-02 — PUT /api/students/me/password — thành công → HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-05-02 |
| **Tham chiếu** | AC-05-01 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**

```json
{ "currentPassword": "OldPass12", "newPassword": "NewPass12", "confirmPassword": "NewPass12" }
```

**Mock:** `studentService.changePassword()` thành công

**Expected:**

```
HTTP 200
```

---

### TC-A-05-03 — PUT /api/students/me/password — mật khẩu hiện tại sai → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-05-03 |
| **Tham chiếu** | AC-05-02 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `studentService.changePassword()` ném `WrongPasswordException`

**Expected:**

```
HTTP 400
{ "errorCode": "WRONG_PASSWORD", "message": "Mật khẩu hiện tại không đúng" }
```

---

### TC-A-05-04 — PUT /api/students/me/password — mật khẩu mới = cũ → HTTP 422

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-05-04 |
| **Tham chiếu** | AC-05-03 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `studentService.changePassword()` ném `SamePasswordException`

**Expected:**

```
HTTP 422
{ "errorCode": "SAME_PASSWORD", "message": "Mật khẩu mới không được giống mật khẩu cũ" }
```

---

### TC-A-05-05 — PUT /api/students/me/password — mật khẩu mới quá yếu → HTTP 422

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-05-05 |
| **Tham chiếu** | AC-05-04 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:**

```json
{ "currentPassword": "OldPass12", "newPassword": "abc", "confirmPassword": "abc" }
```

**Expected:**

```
HTTP 422
{ "errorCode": "WEAK_PASSWORD" }
```

---

### TC-A-05-06 — PUT /api/students/me/password — xác nhận không khớp → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-05-06 |
| **Tham chiếu** | AC-05-05 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:**

```json
{ "currentPassword": "OldPass12", "newPassword": "NewPass12", "confirmPassword": "DiffPass12" }
```

**Expected:**

```
HTTP 400
{ "errorCode": "PASSWORD_MISMATCH" }
```

---

### TC-A-05-07 — PUT /api/students/me/password — tài khoản OAuth → HTTP 422

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-05-07 |
| **Tham chiếu** | AC-05-06 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Mock:** `studentService.changePassword()` ném `OAuthAccountException("google")`

**Expected:**

```
HTTP 422
{ "errorCode": "BUSINESS_RULE_VIOLATION", "message": "...google..." }
```

---

### TC-A-05-08 — PUT /api/students/me/password — currentPassword rỗng → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-05-08 |
| **Tham chiếu** | UC-05 § 5 Validation |
| **Loại** | API — Validation |
| **Ưu tiên** | P1 |

**Request:**

```json
{ "currentPassword": "", "newPassword": "NewPass12", "confirmPassword": "NewPass12" }
```

**Expected:**

```
HTTP 400
{ "errorCode": "VALIDATION_FAILED", "errors": [{ "field": "currentPassword", "message": "Mật khẩu hiện tại là bắt buộc" }] }
```

- `studentService.changePassword()` KHÔNG được gọi

---

## 4. VALIDATION MATRIX — PUT /api/students/me/password

| TC ID | Field | Input | Expected HTTP | Error |
|:---|:---|:---|:---|:---|
| TC-A-05-V01 | currentPassword | null/rỗng | 400 | VALIDATION_FAILED |
| TC-A-05-V02 | newPassword | null/rỗng | 400 | VALIDATION_FAILED |
| TC-A-05-V03 | newPassword | `"abc"` (< 8) | 422 | WEAK_PASSWORD |
| TC-A-05-V04 | newPassword | `"abcdefgh"` (no uppercase, no digit) | 422 | WEAK_PASSWORD |
| TC-A-05-V05 | newPassword | `"Abcdefgh"` (no digit) | 422 | WEAK_PASSWORD |
| TC-A-05-V06 | confirmPassword | null/rỗng | 400 | VALIDATION_FAILED |
| TC-A-05-V07 | confirmPassword | không khớp newPassword | 400 | PASSWORD_MISMATCH |

---

## 5. SECURITY INVARIANT TESTS

---

### TC-S-05-01 — PUT /api/students/me/password KHÔNG tăng login_attempts khi sai mật khẩu hiện tại

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-05-01 |
| **Tham chiếu** | BR-05-05 |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**

1. Seed user với `login_attempts = 0`
2. Gọi `PUT /api/students/me/password` với `currentPassword` sai (nhiều lần)
3. Reload user từ DB

**Expected:**

- `login_attempts` vẫn = 0 (không tăng)
- User KHÔNG bị khóa tài khoản

---

### TC-S-05-02 — Session hiện tại KHÔNG bị thu hồi sau khi đổi mật khẩu thành công

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-05-02 |
| **Tham chiếu** | BR-05-03 |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**

1. Đổi mật khẩu thành công từ thiết bị A (session token A)
2. Thực hiện API request có xác thực từ thiết bị A bằng session token A

**Expected:**

- Request từ thiết bị A vẫn thành công (session token A không bị thu hồi)
- Session token của thiết bị khác (B, C) BỊ thu hồi

---

## 6. FRONTEND COMPONENT TESTS

> **File:** `ChangePasswordForm.test.tsx`

---

### TC-F-05-01 — Form không submit khi có trường rỗng

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-05-01 |
| **Tham chiếu** | FR-TEST-F-02 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Steps:**

1. Render `<ChangePasswordForm />`
2. Click submit không điền gì

**Expected:**

- Hiển thị lỗi validation cho các trường required
- `PUT /api/students/me/password` KHÔNG được gọi

---

### TC-F-05-02 — Hiển thị thông báo thành công và đăng xuất các thiết bị khác

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-05-02 |
| **Tham chiếu** | AC-05-01 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Mock:** `PUT /api/students/me/password` → 200

**Steps:**

1. Điền form hợp lệ → submit

**Expected:**

- Hiển thị "Đổi mật khẩu thành công! Các thiết bị khác đã bị đăng xuất."
- Form được reset hoặc ẩn

---

## 7. COVERAGE CHECKLIST

| Business Rule | Test Case | Covered? |
|:---|:---|:---|
| BR-05-01: Phải xác minh mật khẩu hiện tại | TC-U-05-02, TC-A-05-03 | ✅ |
| BR-05-02: Mật khẩu mới ≠ cũ | TC-U-05-03, FR-TEST-U-06 | ✅ |
| BR-05-03: Thu hồi session khác, giữ session hiện tại | TC-U-05-01, TC-I-05-01, TC-S-05-02 | ✅ |
| BR-05-04: OAuth account không thể đổi mật khẩu | TC-U-05-04, TC-A-05-07 | ✅ |
| BR-05-05: KHÔNG tăng login_attempts | TC-U-05-02, TC-S-05-01 | ✅ |
| BR-05-06: Mật khẩu mới đủ mạnh | TC-U-05-05, TC-A-05-05 | ✅ |
| BR-05-07: Mật khẩu không được log | TC-U-05-07 | ✅ |
| BR-05-08: Cập nhật password_changed_at | TC-U-05-01, TC-I-05-01 | ✅ |
| Thứ tự validation | TC-U-05-08 | ✅ |
