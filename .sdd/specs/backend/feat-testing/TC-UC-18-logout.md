# TC-UC-18 — Test Cases: Đăng Xuất (Logout)

> **Feature:** `feat-auth` | **UC:** UC-18 | **Version:** 1.0
> **Nguồn AC:** UC-18 § 8 | **Nguồn FR-TEST:** SPEC.md § 3.1.1, § 3.3.2
> **Cập nhật:** 2026-05-30

---

## 1. UNIT TESTS — AuthService (JUnit 5 + Mockito)

> **File:** `AuthServiceTest.java` | **Tag:** `@Tag("unit")`

---

### TC-U-18-01 — Logout thành công: chỉ thu hồi session hiện tại, không ảnh hưởng session khác

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-18-01 |
| **Tham chiếu** | AC-18-01, BR-18-01, FR-TEST-U-07 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
String sessionTokenA = "session-token-device-A"; // thiết bị đang logout
String sessionTokenB = "session-token-device-B"; // thiết bị khác

AuthToken tokenA = new AuthToken();
tokenA.setTokenValue(sessionTokenA);
tokenA.setStudentId(1L);
tokenA.setTokenType("session");
tokenA.setRevokedAt(null);

when(tokenRepository.findByTokenValueAndTokenType(sessionTokenA, "session"))
    .thenReturn(Optional.of(tokenA));
```

**Steps:**
1. Gọi `authService.logout(1L, sessionTokenA, null)`

**Expected:**
- `tokenRepository.save()` được gọi với `tokenA.revokedAt ≈ NOW()`
- `tokenRepository.findByTokenValueAndTokenType(sessionTokenB, ...)` KHÔNG được gọi
- Session token B KHÔNG bị thu hồi
- KHÔNG ném exception

---

### TC-U-18-02 — Logout cùng Refresh Token: cả session token VÀ refresh token bị thu hồi

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-18-02 |
| **Tham chiếu** | BR-18-03 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
AuthToken sessionToken = buildToken("session", studentId, "access-token-abc");
AuthToken refreshToken = buildToken("refresh", studentId, "refresh-token-xyz");

when(tokenRepository.findByTokenValueAndTokenType("access-token-abc", "session"))
    .thenReturn(Optional.of(sessionToken));
when(tokenRepository.findByTokenValueAndTokenType("refresh-token-xyz", "refresh"))
    .thenReturn(Optional.of(refreshToken));
```

**Steps:**
1. Gọi `authService.logout(1L, "access-token-abc", "refresh-token-xyz")`

**Expected:**
- `sessionToken.revokedAt` được đặt (thu hồi)
- `refreshToken.revokedAt` được đặt (thu hồi)
- `tokenRepository.save()` được gọi 2 lần (một cho mỗi token)

---

### TC-U-18-03 — Logout ghi Audit Log đầy đủ

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-18-03 |
| **Tham chiếu** | BR-18-04 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
// hoặc dùng log appender mock
```

**Steps:**
1. Gọi `authService.logout(studentId=1L, sessionToken, refreshToken=null)`
2. Capture log output

**Expected:**
- Log entry với level INFO chứa tất cả: `studentId = 1`, timestamp, IP (nếu có), `"LOGOUT_SUCCESS"`

---

### TC-U-18-04 — Logout khi session token đã được thu hồi (idempotent)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-18-04 |
| **Tham chiếu** | BR-18-02 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P2 |

**Setup:**
```java
AuthToken alreadyRevokedToken = new AuthToken();
alreadyRevokedToken.setRevokedAt(Instant.now().minus(5, ChronoUnit.MINUTES)); // đã thu hồi
when(tokenRepository.findByTokenValueAndTokenType("old-token", "session"))
    .thenReturn(Optional.of(alreadyRevokedToken));
```

**Steps:**
1. Gọi `authService.logout(1L, "old-token", null)`

**Expected:**
- Xử lý thành công (KHÔNG ném exception)
- `revokedAt` không được cập nhật lại (đã có giá trị)
- Hoặc có thể idempotent: cập nhật không gây hại

---

### TC-U-18-05 — Logout khi không tìm thấy session token trong DB (graceful handling)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-18-05 |
| **Tham chiếu** | BR-18-02 (fallback) |
| **Loại** | Unit — Service |
| **Ưu tiên** | P2 |

**Setup:**
```java
when(tokenRepository.findByTokenValueAndTokenType("orphan-token", "session"))
    .thenReturn(Optional.empty()); // token không tồn tại trong DB
```

**Steps:**
1. Gọi `authService.logout(1L, "orphan-token", null)`

**Expected:**
- KHÔNG ném exception (graceful handling)
- Log warning: "Token không tìm thấy khi logout" (optional)

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `AuthRepositoryIT.java` | **Tag:** `@Tag("integration")`

---

### TC-I-18-01 — Logout: revoked_at được set trong DB cho session hiện tại

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-18-01 |
| **Tham chiếu** | AC-18-01 |
| **Loại** | Integration — Full Flow |
| **Ưu tiên** | P1 |

**Steps:**
1. Seed user + 2 session tokens (A và B), cả hai `revoked_at = NULL`
2. Gọi `authService.logout(studentId, tokenA_value, null)`
3. Query `auth_tokens` từ DB

**Expected:**
- Token A: `revoked_at` IS NOT NULL (≈ NOW())
- Token B: `revoked_at` = NULL (KHÔNG thay đổi)

---

### TC-I-18-02 — Sau logout: token A không thể dùng để gọi API bảo mật

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-18-02 |
| **Tham chiếu** | AC-18-01 — "Dùng token A sau đó nhận HTTP 401" |
| **Loại** | Integration — End-to-End Logic |
| **Ưu tiên** | P1 |

**Steps:**
1. Login → nhận `sessionTokenA`
2. Logout với `sessionTokenA`
3. Gọi `GET /api/students/me` với `Authorization: Bearer <JWT từ sessionTokenA>`

**Expected:**
- Nhận HTTP 401
- (Nếu có blacklist/revocation check: JWT bị từ chối vì refresh token/session đã bị revoke)

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `AuthControllerTest.java` | **Tag:** `@Tag("api")`

---

### TC-A-18-01 — POST /api/auth/logout — JWT hợp lệ → HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-18-01 |
| **Tham chiếu** | AC-18-01 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**
```
POST /api/auth/logout
Authorization: Bearer <valid-jwt>
Cookie: refreshToken=<refresh-token-value>
```

**Mock:** `authService.logout()` thành công

**Expected:**
```
HTTP 200
{ "message": "Đăng xuất thành công" }
```
- Response header chứa `Set-Cookie: refreshToken=; Max-Age=0` (xóa cookie)

---

### TC-A-18-02 — POST /api/auth/logout — không có JWT → HTTP 401

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-18-02 |
| **Tham chiếu** | FR-TEST-A-01, AC-18-02 |
| **Loại** | API — Security |
| **Ưu tiên** | P0 |

**Steps:**
1. Gửi `POST /api/auth/logout` không có header `Authorization`

**Expected:**
```
HTTP 401
```

---

### TC-A-18-03 — POST /api/auth/logout — JWT hết hạn → HTTP 401 (client cleanup vẫn chạy)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-18-03 |
| **Tham chiếu** | AC-18-02 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Steps:**
1. Gửi `POST /api/auth/logout` với JWT đã hết hạn

**Expected:**
```
HTTP 401
```
- (Frontend sẽ nhận 401 và tự cleanup local state — xem TC-F-18-02)

---

### TC-A-18-04 — POST /api/auth/logout — Student JWT không thể gọi endpoint Staff

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-18-04 |
| **Tham chiếu** | FR-TEST-A-02 |
| **Loại** | API — Security |
| **Ưu tiên** | P0 |

**Steps:**
1. Gửi `POST /api/auth/logout` với JWT role `STUDENT`

**Expected:**
- `POST /api/auth/logout` được xử lý bình thường (200) — đây là endpoint student
- Staff/Admin endpoints sẽ trả 403 với Student JWT

---

### TC-A-18-05 — POST /api/auth/logout không có Refresh Token cookie: vẫn thành công

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-18-05 |
| **Tham chiếu** | UC-18 § 5 — Cookie là tùy chọn |
| **Loại** | API — Controller |
| **Ưu tiên** | P2 |

**Request:**
```
POST /api/auth/logout
Authorization: Bearer <valid-jwt>
// Không có Cookie header
```

**Mock:** `authService.logout()` thành công (với refreshToken = null)

**Expected:**
```
HTTP 200
```

---

## 4. SECURITY INVARIANT TESTS

---

### TC-S-18-01 — Sau logout: token bị thu hồi không thể dùng để re-authenticate

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-18-01 |
| **Tham chiếu** | BR-18-01, AC-18-01 |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**
1. Login lấy `refreshToken_A`
2. Logout (thu hồi `refreshToken_A`)
3. Thử `POST /api/auth/refresh` với `refreshToken_A`

**Expected:**
- HTTP 401 (refresh token bị thu hồi, không thể lấy access token mới)

---

### TC-S-18-02 — Logout một thiết bị KHÔNG ảnh hưởng thiết bị khác

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-18-02 |
| **Tham chiếu** | BR-18-01 |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**
1. Seed user với 3 session tokens (A, B, C) — đều active
2. Logout session A
3. Thực hiện API call với session B và C

**Expected:**
- Session A: bị thu hồi → API calls thất bại (401)
- Session B: vẫn active → API calls thành công (200)
- Session C: vẫn active → API calls thành công (200)

---

## 5. FRONTEND COMPONENT TESTS

> **File:** `LogoutButton.test.tsx` hoặc `App.test.tsx`

---

### TC-F-18-01 — Nhấn "Đăng xuất": gọi API, xóa token, redirect về login

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-18-01 |
| **Tham chiếu** | AC-18-01, BR-18-02 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P0 |

**Mock:** `POST /api/auth/logout` → 200

**Steps:**
1. Render component có nút "Đăng xuất" với user đã đăng nhập
2. Click "Đăng xuất"

**Expected:**
- `POST /api/auth/logout` được gọi
- Access token bị xóa khỏi memory/store
- Thông tin user bị xóa khỏi state
- Chuyển hướng đến `/login`

---

### TC-F-18-02 — Nhấn "Đăng xuất" khi API trả 401: vẫn cleanup và redirect

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-18-02 |
| **Tham chiếu** | AC-18-02, BR-18-02 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P0 |

**Mock:** `POST /api/auth/logout` → 401 (JWT đã hết hạn)

**Steps:**
1. Click "Đăng xuất" với JWT đã hết hạn

**Expected:**
- Nhận 401 từ server
- Frontend vẫn xóa token và state cục bộ
- Vẫn chuyển hướng về `/login`
- Không xuất hiện màn hình trắng hay vòng lặp vô tận

---

### TC-F-18-03 — Nhấn "Đăng xuất" khi API trả 500: vẫn cleanup và redirect (resilient)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-18-03 |
| **Tham chiếu** | BR-18-02 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Mock:** `POST /api/auth/logout` → 500 (server error)

**Steps:**
1. Click "Đăng xuất" khi server lỗi

**Expected:**
- Bất kể server trả gì, frontend xóa local state và redirect về `/login`
- KHÔNG bị kẹt trạng thái

---

### TC-F-18-04 — Sau logout: không còn thể navigate về protected routes

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-18-04 |
| **Tham chiếu** | AC-18-01 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Steps:**
1. Login → navigate tới `/dashboard`
2. Logout
3. Thử navigate về `/dashboard`

**Expected:**
- Redirect về `/login` (route guard hoạt động)
- KHÔNG thể truy cập protected routes khi không có token

---

## 6. COVERAGE CHECKLIST

| Business Rule | Test Case | Covered? |
|:---|:---|:---|
| BR-18-01: Chỉ thu hồi session hiện tại | TC-U-18-01, TC-I-18-01, TC-S-18-02 | ✅ |
| BR-18-02: Client cleanup dù server lỗi | TC-F-18-02, TC-F-18-03 | ✅ |
| BR-18-03: Refresh token cũng bị thu hồi | TC-U-18-02, TC-S-18-01 | ✅ |
| BR-18-04: Audit log đầy đủ | TC-U-18-03 | ✅ |
| JWT hết hạn: client vẫn cleanup | TC-A-18-03, TC-F-18-02 | ✅ |
| Token không thể dùng sau logout | TC-I-18-02, TC-S-18-01 | ✅ |
