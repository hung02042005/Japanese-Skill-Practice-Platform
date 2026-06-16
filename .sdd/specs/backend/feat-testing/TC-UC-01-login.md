# TC-UC-01 — Test Cases: Đăng Nhập (User Login)

> **Feature:** `feat-auth` | **UC:** UC-01 | **Version:** 1.0
> **Nguồn AC:** UC-01 § 8 | **Nguồn FR-TEST:** SPEC.md § 3.1.1, § 3.3.2, § 3.3.1
> **Cập nhật:** 2026-05-30

---

## 1. UNIT TESTS — AuthService (JUnit 5 + Mockito)

> **File:** `AuthServiceTest.java` | **Tag:** `@Tag("unit")`

---

### TC-U-01-01 — Đăng nhập email/mật khẩu thành công

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-01 |
| **Tham chiếu** | AC-01-01, FR-TEST-U-01 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
StudentUser user = aStudent()
    .withEmail("student@test.com")
    .withStatus("active")
    .withLoginAttempts(0)
    .build();
when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
when(passwordEncoder.matches("Abcdef12", user.getPasswordHash())).thenReturn(true);
when(jwtService.generateAccessToken(any())).thenReturn("access.jwt.token");
when(jwtService.generateRefreshToken()).thenReturn("refresh-token-value");
```

**Steps:**
1. Gọi `authService.loginWithPassword("student@test.com", "Abcdef12", "192.168.1.1")`

**Expected:**
- Trả về `LoginResponse` chứa `accessToken` != null và `refreshToken` != null
- `student.loginAttempts` được đặt về 0 (gọi `userRepository.save()` với `loginAttempts = 0`)
- `student.lastLoginAt` được cập nhật (≈ NOW())
- `student.lastLoginIp` = `"192.168.1.1"`
- `tokenRepository.save()` được gọi một lần với `tokenType = "refresh"`
- Không có exception nào được ném

---

### TC-U-01-02 — Sai mật khẩu, tăng login_attempts

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-02 |
| **Tham chiếu** | AC-01-02, FR-TEST-U-01 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
StudentUser user = aStudent().withLoginAttempts(0).build();
when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
when(passwordEncoder.matches("wrong", user.getPasswordHash())).thenReturn(false);
```

**Steps:**
1. Gọi `authService.loginWithPassword("student@test.com", "wrong", "127.0.0.1")`

**Expected:**
- Ném `InvalidCredentialsException`
- `userRepository.save()` được gọi với `user.loginAttempts = 1`
- Không tạo token nào (`tokenRepository.save()` không được gọi)

---

### TC-U-01-03 — Khóa tài khoản sau lần sai thứ 5

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-03 |
| **Tham chiếu** | AC-01-03, FR-TEST-U-02 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
StudentUser user = aStudent().withLoginAttempts(4).build(); // lần này là lần thứ 5
when(passwordEncoder.matches(any(), any())).thenReturn(false);
Clock fixedClock = Clock.fixed(Instant.parse("2026-05-30T08:00:00Z"), ZoneOffset.UTC);
// inject fixedClock vào AuthService
```

**Steps:**
1. Gọi `authService.loginWithPassword("student@test.com", "wrong", "127.0.0.1")`

**Expected:**
- Ném `AccountLockedException` (HTTP 429 equivalent)
- `user.loginAttempts` = 5
- `user.lockedUntil` = `2026-05-30T08:15:00Z` (NOW + 15 phút)
- `userRepository.save()` được gọi với `lockedUntil` được đặt

---

### TC-U-01-04 — Không đăng nhập được khi đang bị khóa (kể cả đúng mật khẩu)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-04 |
| **Tham chiếu** | AC-01-04, FR-TEST-U-02 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
Instant now = Instant.parse("2026-05-30T08:00:00Z");
Instant lockedUntil = now.plus(10, ChronoUnit.MINUTES); // còn 10 phút
StudentUser user = aStudent()
    .withLockedUntil(lockedUntil)
    .withLoginAttempts(5)
    .build();
when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
```

**Steps:**
1. Gọi `authService.loginWithPassword("student@test.com", "CorrectPass12", "127.0.0.1")`

**Expected:**
- Ném `AccountLockedException` với `remainingMinutes = 10`
- `passwordEncoder.matches()` **KHÔNG được gọi** (kiểm tra lock trước)
- `user.loginAttempts` KHÔNG thay đổi (vẫn = 5)
- Không tạo token

---

### TC-U-01-05 — Tài khoản bị đình chỉ (suspended)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-05 |
| **Tham chiếu** | AC-01-05, FR-TEST-U-03 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
StudentUser user = aStudent()
    .withStatus("suspended")
    .withSuspendReason("Vi phạm quy chế")
    .build();
when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
```

**Steps:**
1. Gọi `authService.loginWithPassword("student@test.com", "Abcdef12", "127.0.0.1")`

**Expected:**
- Ném `AccountSuspendedException` với message chứa `"Vi phạm quy chế"`
- HTTP equivalent: 403 / `ACCOUNT_SUSPENDED`
- `passwordEncoder.matches()` KHÔNG được gọi

---

### TC-U-01-06 — Email chưa xác minh (status = pending)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-06 |
| **Tham chiếu** | AC-01-06 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
StudentUser user = aStudent().withStatus("pending").build();
when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
```

**Steps:**
1. Gọi `authService.loginWithPassword("student@test.com", "Abcdef12", "127.0.0.1")`

**Expected:**
- Ném `EmailNotVerifiedException`
- HTTP equivalent: 403 / `EMAIL_NOT_VERIFIED`
- `passwordEncoder.matches()` KHÔNG được gọi

---

### TC-U-01-07 — Email không tồn tại (không tiết lộ)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-07 |
| **Tham chiếu** | BR-01-05 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
```

**Steps:**
1. Gọi `authService.loginWithPassword("ghost@test.com", "anyPass", "127.0.0.1")`

**Expected:**
- Ném `InvalidCredentialsException` (CÙNG exception như sai mật khẩu — không phân biệt)
- Message KHÔNG chứa "email không tồn tại" hay tương tự
- Message chứa "Email hoặc mật khẩu không đúng"

---

### TC-U-01-08 — Mật khẩu KHÔNG được log

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-08 |
| **Tham chiếu** | BR-01-04 |
| **Loại** | Unit — Service (Log inspection) |
| **Ưu tiên** | P0 (Security) |

**Steps:**
1. Capture log output trong quá trình login thất bại với mật khẩu `"Secret@123"`
2. Kiểm tra tất cả log entries

**Expected:**
- Không có log nào chứa chuỗi `"Secret@123"` (plaintext)
- Không có log nào chứa chuỗi bcrypt hash của mật khẩu

---

### TC-U-01-09 — Reset login_attempts khi đăng nhập thành công (sau lần sai trước)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-09 |
| **Tham chiếu** | BR-01-01 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
StudentUser user = aStudent().withLoginAttempts(3).withStatus("active").build();
when(passwordEncoder.matches("Abcdef12", any())).thenReturn(true);
```

**Steps:**
1. Gọi `authService.loginWithPassword("student@test.com", "Abcdef12", "127.0.0.1")`

**Expected:**
- `user.loginAttempts` được reset về 0
- Không ném exception
- Token được tạo thành công

---

### TC-U-01-10 — OAuth state parameter được xác minh (chống CSRF)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-01-10 |
| **Tham chiếu** | BR-01-07 |
| **Loại** | Unit — OAuthService |
| **Ưu tiên** | P0 (Security) |

**Setup:**
```java
String expectedState = "abc123";
oauthService.storeState(expectedState, sessionId);
```

**Steps:**
1. Gọi `oauthService.handleCallback(code="valid_code", state="tampered_state", sessionId)`

**Expected:**
- Ném `OAuthStateException` (CSRF detected)
- `oauthService.exchangeCode()` KHÔNG được gọi

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `AuthRepositoryIT.java` | **Tag:** `@Tag("integration")`
> **Stack:** Testcontainers (SQL Server), Flyway migrations

---

### TC-I-01-01 — Tạo refresh token và truy vấn lại từ DB

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-01-01 |
| **Tham chiếu** | AC-01-01, FR-TEST-I-01 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P1 |

**Steps:**
1. Seed `student_users` với user hợp lệ
2. Tạo `auth_tokens` record với `tokenType = "refresh"`, `studentId = 1`
3. Query `tokenRepository.findByTokenValue(tokenValue, "refresh")`

**Expected:**
- Record tìm thấy với đúng `studentId`, `tokenType`, `expiresAt`
- `revokedAt` = NULL

---

### TC-I-01-02 — Unique constraint: auth_tokens không có cả admin_id và student_id

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-01-02 |
| **Tham chiếu** | FR-TEST-I-03 |
| **Loại** | Integration — DB Constraint |
| **Ưu tiên** | P0 (Security) |

**Steps:**
1. Thực hiện INSERT vào `auth_tokens` với cả `student_id = 1` VÀ `admin_id = 1` không NULL

**Expected:**
- `DataIntegrityViolationException` được ném (vi phạm `CK_auth_token_actor`)

---

### TC-I-01-03 — login_attempts và locked_until persist đúng sang DB

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-01-03 |
| **Tham chiếu** | AC-01-03 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P1 |

**Steps:**
1. Seed user với `login_attempts = 4`
2. Gọi `userRepository.save(user)` sau khi cập nhật `loginAttempts = 5` và `lockedUntil = NOW() + 15min`
3. Reload entity từ DB: `userRepository.findById(1)`

**Expected:**
- `loginAttempts = 5` trong DB
- `lockedUntil ≈ NOW() + 15 phút` (sai số < 2 giây)

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `AuthControllerTest.java` | **Tag:** `@Tag("api")`
> **Stack:** `@WebMvcTest(AuthController.class)`, MockMvc, Mockito

---

### TC-A-01-01 — POST /api/auth/login — thành công

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-01 |
| **Tham chiếu** | AC-01-01, FR-TEST-A-10 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**
```
POST /api/auth/login
Content-Type: application/json

{ "email": "student@test.com", "password": "Abcdef12" }
```

**Mock:** `authService.loginWithPassword()` trả về `LoginResponse(accessToken="...", refreshToken="...")`

**Expected:**
```
HTTP 200
{
  "accessToken": "<non-null JWT string>",
  "refreshToken": "<non-null string>",
  "student": {
    "studentId": 1,
    "fullName": "Test Student",
    "email": "student@test.com"
  }
}
```
- Response body KHÔNG chứa key `passwordHash`
- Response body KHÔNG chứa key `loginAttempts`

---

### TC-A-01-02 — POST /api/auth/login — mật khẩu sai → HTTP 401

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-02 |
| **Tham chiếu** | AC-01-02 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `authService.loginWithPassword()` ném `InvalidCredentialsException`

**Expected:**
```
HTTP 401
{ "errorCode": "INVALID_CREDENTIALS", "message": "Email hoặc mật khẩu không đúng" }
```

---

### TC-A-01-03 — POST /api/auth/login — tài khoản bị khóa → HTTP 429

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-03 |
| **Tham chiếu** | AC-01-04 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `authService.loginWithPassword()` ném `AccountLockedException(remainingMinutes=10)`

**Expected:**
```
HTTP 429
{ "errorCode": "TOO_MANY_REQUESTS", "message": "...10 phút..." }
```

---

### TC-A-01-04 — POST /api/auth/login — tài khoản bị đình chỉ → HTTP 403

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-04 |
| **Tham chiếu** | AC-01-05 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `authService.loginWithPassword()` ném `AccountSuspendedException("Vi phạm quy chế")`

**Expected:**
```
HTTP 403
{ "errorCode": "ACCOUNT_SUSPENDED", "message": "...Vi phạm quy chế..." }
```

---

### TC-A-01-05 — POST /api/auth/login — email chưa xác minh → HTTP 403

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-05 |
| **Tham chiếu** | AC-01-06 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `authService.loginWithPassword()` ném `EmailNotVerifiedException`

**Expected:**
```
HTTP 403
{ "errorCode": "EMAIL_NOT_VERIFIED" }
```

---

### TC-A-01-06 — POST /api/auth/login — input validation: email rỗng → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-06 |
| **Tham chiếu** | UC-01 § 5 (Validation Rules) |
| **Loại** | API — Validation |
| **Ưu tiên** | P1 |

**Request:**
```json
{ "email": "", "password": "Abcdef12" }
```

**Expected:**
```
HTTP 400
{ "errorCode": "VALIDATION_FAILED", "errors": [{ "field": "email", "message": "Email là bắt buộc" }] }
```
- `authService.loginWithPassword()` KHÔNG được gọi

---

### TC-A-01-07 — POST /api/auth/login — email sai định dạng → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-07 |
| **Tham chiếu** | UC-01 § 5 |
| **Loại** | API — Validation |
| **Ưu tiên** | P1 |

**Request:**
```json
{ "email": "not-an-email", "password": "Abcdef12" }
```

**Expected:**
```
HTTP 400
{ "errorCode": "VALIDATION_FAILED", "errors": [{ "field": "email", "message": "Email không hợp lệ" }] }
```

---

### TC-A-01-08 — POST /api/auth/login — mật khẩu rỗng → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-08 |
| **Tham chiếu** | UC-01 § 5 |
| **Loại** | API — Validation |
| **Ưu tiên** | P1 |

**Request:**
```json
{ "email": "student@test.com", "password": "" }
```

**Expected:**
```
HTTP 400
{ "errorCode": "VALIDATION_FAILED", "errors": [{ "field": "password", "message": "Mật khẩu là bắt buộc" }] }
```

---

### TC-A-01-09 — Endpoint /api/auth/login không có Authorization header → vẫn public (không cần JWT)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-09 |
| **Tham chiếu** | FR-TEST-A-01 (login là public endpoint) |
| **Loại** | API — Security |
| **Ưu tiên** | P0 |

**Steps:**
1. Gọi `POST /api/auth/login` không có header `Authorization`

**Expected:**
- Không nhận HTTP 401 (endpoint là public)
- Request được xử lý bình thường

---

### TC-A-01-10 — Rate limiting: sau 5 request/phút/IP → lần thứ 6 bị chặn

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-01-10 |
| **Tham chiếu** | BR-01-09, FR-TEST-A-05 |
| **Loại** | API — Rate Limit |
| **Ưu tiên** | P0 (Security) |

**Steps:**
1. Gửi 5 request `POST /api/auth/login` liên tiếp từ cùng IP (`X-Forwarded-For: 10.0.0.1`)
2. Gửi request thứ 6 từ cùng IP

**Expected:**
- Request 1–5: xử lý bình thường (200 hoặc 401)
- Request thứ 6: HTTP 429

---

## 4. SECURITY INVARIANT TESTS

> **Tag:** `@Tag("security")` — KHÔNG được `@Disabled`

---

### TC-S-01-01 — Response login KHÔNG chứa passwordHash

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-01-01 |
| **Tham chiếu** | FR-TEST-S-03, BR-01-04 |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**
1. Gọi `POST /api/auth/login` với credentials hợp lệ
2. Parse toàn bộ JSON response (đệ quy qua tất cả keys)

**Expected:**
- Không có key nào tên `password`, `passwordHash`, `password_hash` trong response
- Không có key `twoFactorSecret`, `loginAttempts`, `lockedUntil`

---

### TC-S-01-02 — JWT không lưu vào DB (chỉ refresh token được lưu)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-01-02 |
| **Tham chiếu** | BR-01-06 |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**
1. Thực hiện login thành công
2. Kiểm tra bảng `auth_tokens` trong DB

**Expected:**
- Trong `auth_tokens`, chỉ có bản ghi với `token_type = "refresh"`
- Không có bản ghi với `token_type = "access"` hoặc `token_type = "jwt"`

---

## 5. FRONTEND COMPONENT TESTS

> **File:** `LoginForm.test.tsx` | **Stack:** Jest 29, RTL, `@testing-library/user-event`

---

### TC-F-01-01 — Form không submit khi email rỗng

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-01-01 |
| **Tham chiếu** | FR-TEST-F-02 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Steps:**
1. Render `<LoginForm />`
2. Để trống email, điền password = `"Abcdef12"`
3. Click nút "Đăng nhập"

**Expected:**
- Hiển thị validation error "Email là bắt buộc"
- `POST /api/auth/login` KHÔNG được gọi (verify axios mock)

---

### TC-F-01-02 — Hiển thị lỗi "Email hoặc mật khẩu không đúng" khi API trả 401

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-01-02 |
| **Tham chiếu** | AC-01-02 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Setup:** Mock API `POST /api/auth/login` → 401 `{ errorCode: "INVALID_CREDENTIALS" }`

**Steps:**
1. Điền email + password hợp lệ về format
2. Click "Đăng nhập"

**Expected:**
- Hiển thị message lỗi "Email hoặc mật khẩu không đúng" trong DOM
- Form KHÔNG bị ẩn

---

### TC-F-01-03 — Chuyển hướng đến Dashboard sau khi đăng nhập thành công

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-01-03 |
| **Tham chiếu** | AC-01-01 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Setup:** Mock API → 200 `{ accessToken: "...", refreshToken: "..." }`

**Steps:**
1. Điền email + password → click "Đăng nhập"

**Expected:**
- `navigate("/dashboard")` hoặc `router.push("/dashboard")` được gọi
- Token được lưu vào memory/store (KHÔNG vào `localStorage` nếu design dùng memory)

---

### TC-F-01-04 — Nút "Đăng nhập với Google" dẫn đến đúng endpoint

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-01-04 |
| **Tham chiếu** | AC-01-07 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P2 |

**Steps:**
1. Render `<LoginForm />`
2. Click "Đăng nhập với Google"

**Expected:**
- Browser hoặc mocked navigation đến `/api/auth/oauth/google` (hoặc có redirect header về Google)

---

## 6. TEST DATA SUMMARY

| Fixture | Email | Status | login_attempts | locked_until |
|:---|:---|:---|:---|:---|
| `activeStudent` | student@test.com | active | 0 | NULL |
| `almostLockedStudent` | almost@test.com | active | 4 | NULL |
| `lockedStudent` | locked@test.com | active | 5 | NOW+10m |
| `suspendedStudent` | suspended@test.com | suspended | 0 | NULL |
| `pendingStudent` | pending@test.com | pending | 0 | NULL |

---

## 7. COVERAGE CHECKLIST

| Business Rule | Test Case | Covered? |
|:---|:---|:---|
| BR-01-01: Reset attempts chỉ khi login thành công | TC-U-01-09 | ✅ |
| BR-01-02: Kiểm tra locked_until TRƯỚC khi so sánh password | TC-U-01-04 | ✅ |
| BR-01-03: Khóa 15 phút sau 5 lần sai | TC-U-01-03 | ✅ |
| BR-01-04: Password KHÔNG được log | TC-U-01-08 | ✅ |
| BR-01-05: Thông báo lỗi chung chung (không phân biệt) | TC-U-01-07 | ✅ |
| BR-01-06: JWT không lưu trong DB | TC-S-01-02 | ✅ |
| BR-01-07: OAuth state chống CSRF | TC-U-01-10 | ✅ |
| BR-01-09: Rate limit 10 req/phút/IP | TC-A-01-10 | ✅ |
