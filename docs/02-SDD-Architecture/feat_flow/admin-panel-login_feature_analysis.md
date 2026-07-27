# Phân Tích Cấu Trúc – Luồng – Kết Nối Của Feature: Authentication — Login System (Admin Panel)

## 1. Tóm tắt tổng quan

Tài liệu này **bổ sung** (không thay thế) tài liệu đã có [authen_feature_analysis.md](authen_feature_analysis.md), vốn phân tích chung toàn bộ feature Authentication (Student/Staff/Admin) nhưng chỉ khảo sát các file trong package `com.jlpt.feature.auth`. Phần còn thiếu — cũng là trọng tâm của tài liệu này — là nhánh xử lý nghiệp vụ **dành riêng cho Admin**, nằm trong package khác: `com.jlpt.feature.admin`, cụ thể là [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java).

Về mặt UI/API, Admin **dùng chung** một trang đăng nhập (`Login.jsx`), một hàm gọi API (`authService.login()`) và một endpoint (`POST /api/auth/login`) với Student/Staff — không có form hay route đăng nhập riêng cho Admin. Sự khác biệt chỉ bắt đầu **bên trong** [AuthenticationService.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthenticationService.java), tại hàm `login()`: đây là nơi hệ thống quyết định "email này có phải Admin không", và nếu có, ủy quyền toàn bộ phần còn lại (kiểm tra khóa tài khoản, khóa sau 5 lần sai, audit log, sinh JWT có `role=ADMIN`) cho `AdminAuthService.processAdminLogin(...)`.

- **Tầng Frontend**: [Login.jsx](../../../apps/frontend/src/pages/login/Login.jsx) (UI dùng chung, không có nhánh code riêng cho admin ngoài việc đọc `role` trả về để điều hướng) → [authSlice.js](../../../apps/frontend/src/store/slices/authSlice.js) (Redux Thunk dùng chung) → [authService.js](../../../apps/frontend/src/api/authService.js) (Axios, dùng chung).
- **Tầng Backend**: [AuthController.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthController.java) (endpoint dùng chung `/api/auth/login`) → [AuthenticationService.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthenticationService.java) (nơi rẽ nhánh) → [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) (**riêng cho Admin** — lockout, audit, JWT admin).
- **Tầng DB**: `admin_users` (qua [AdminUser.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUser.java) / [AdminUserRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserRepository.java)), `admin_audit_logs` (qua [AdminAuditLog.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLog.java) / [AdminAuditLogRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java)), `auth_tokens` với `actor_type = ADMIN` (qua [AuthToken.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthToken.java) / [AuthTokenRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthTokenRepository.java)).
- **Entry point thực sự của nhánh Admin**: không phải một endpoint riêng, mà là điều kiện `if (adminOpt.isPresent())` tại dòng 128 của `AuthenticationService.login()` — xem chi tiết Mục 5.
- **Điểm đến cuối cùng ở Frontend**: sau khi nhận `role: "ADMIN"`, `Login.jsx` gọi `navigate('/admin')`, và route `/admin` được bọc bởi `AdminRoute` ([AdminRoute.jsx](../../../apps/frontend/src/components/common/AdminRoute.jsx)) — component này kiểm tra lại `user.role === 'ADMIN'` phía client trước khi render `AdminDashboard`.

---

## 2. Bản đồ cấu trúc (các "mảnh" và vai trò)

| File | Vai trò | Loại |
|------|----------|------|
| [Login.jsx](../../../apps/frontend/src/pages/login/Login.jsx) | Form đăng nhập dùng chung cho Student/Staff/Admin; sau khi nhận response, đọc `role` để điều hướng (`role === 'ADMIN'` → `navigate('/admin')`). Không có logic hay UI riêng cho Admin. | Page Component |
| [authSlice.js](../../../apps/frontend/src/store/slices/authSlice.js) | `loginThunk` xử lý response `LoginApiResponse` dùng chung; với Admin, field `user` từ backend là `null` nên slice tự dựng object tối thiểu `{ role: 'ADMIN' }` để lưu vào Redux state và `localStorage`. | Redux Slice |
| [authService.js](../../../apps/frontend/src/api/authService.js) | Gọi `POST /auth/login` dùng chung; interceptor tự refresh token khi 401 áp dụng như nhau cho mọi role (kể cả Admin). | API Service |
| [AuthController.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthController.java) | Nhận HTTP request `/api/auth/login`, không có endpoint riêng cho Admin — chuyển thẳng toàn bộ logic cho `AuthenticationService`. | Controller |
| [AuthenticationService.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthenticationService.java) | Chứa **điểm rẽ nhánh** quyết định gọi `AdminAuthService` hay xử lý Staff/Student; tra `admin_users` trước tiên theo thứ tự ưu tiên. | Service |
| [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) | **Trọng tâm tài liệu này.** Xử lý toàn bộ nghiệp vụ đăng nhập Admin: kiểm tra khóa tài khoản (`lockedUntil`), kiểm tra trạng thái `SUSPENDED`, so khớp mật khẩu, đếm/khóa sau 5 lần sai, ghi audit log cho mọi kết quả, sinh access token + refresh token riêng cho Admin. | Service |
| [AdminUser.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUser.java) | Entity ánh xạ bảng `admin_users`; chứa state machine khóa tài khoản (`loginAttempts`, `lockedUntil`, `status`). | Entity |
| [AdminUserRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserRepository.java) | Truy vấn `admin_users`, đặc biệt `findByEmail()` — dùng để xác định "email này có phải Admin không" ngay bước đầu tiên của `login()`. | Repository |
| [AdminAuditLog.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLog.java) | Entity ánh xạ bảng `admin_audit_logs`; ghi lại mọi hành động đăng nhập Admin (thành công, thất bại, bị khóa, bị đình chỉ). | Entity |
| [AdminAuditLogRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java) | Lưu/truy vấn `admin_audit_logs`; được `AdminAuthService.audit()` gọi ở mọi nhánh kết quả đăng nhập. | Repository |
| [AuthToken.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthToken.java) | Entity dùng chung cho Student/Staff/Admin (`actorType` phân biệt); với Admin, cột `admin_id` được set, `actorType = ADMIN`. | Entity |
| [AuthTokenRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthTokenRepository.java) | Lưu refresh token của Admin; có sẵn `revokeAllActiveByAdminId()` dùng khi cần thu hồi token (VD: reset mật khẩu). | Repository |
| [JwtProvider.java](../../../apps/backend/src/main/java/com/jlpt/shared/security/JwtProvider.java) | Có hàm riêng `generateAdminAccessToken(adminId, email)` gắn claim `role=ADMIN` và `adminId` vào JWT, không dùng chung với hàm sinh token của Student/Staff. | Security Component |
| [AdminRoute.jsx](../../../apps/frontend/src/components/common/AdminRoute.jsx) | Route Guard phía Frontend: chặn truy cập `/admin/*` nếu chưa đăng nhập hoặc `user.role !== 'ADMIN'`. | Route Guard Component |

---

## 3. Bản đồ kết nối (ai gọi ai, dữ liệu truyền qua đâu)

```mermaid
graph TD
    UI["Login.jsx"] -->|"dispatch(loginThunk(credentials))"| Slice("authSlice.js")
    Slice -->|"gọi authService.login()"| API("authService.js")
    API -.->|"HTTP POST /api/auth/login"| Controller("AuthController.java")

    Controller -->|"login(request, ip)"| AuthSvc("AuthenticationService.java")

    AuthSvc -->|"1. findByEmail() — tra trước tiên"| AdminRepo[("AdminUserRepository\n→ bảng admin_users")]
    AdminRepo -->|"Optional&lt;AdminUser&gt;"| AuthSvc

    AuthSvc -->|"nếu tìm thấy: processAdminLogin(admin, pass, ip)"| AdminSvc("AdminAuthService.java")

    AdminSvc -->|"đọc/ghi loginAttempts, lockedUntil, status"| AdminRepo
    AdminSvc -->|"ghi audit() mọi kết quả"| AuditRepo[("AdminAuditLogRepository\n→ bảng admin_audit_logs")]
    AdminSvc -->|"generateAdminAccessToken(id, email)"| JWT("JwtProvider.java")
    AdminSvc -->|"lưu refresh token, actorType=ADMIN"| TokenRepo[("AuthTokenRepository\n→ bảng auth_tokens")]

    AdminSvc -->|"LoginApiResponse { role: ADMIN, accessToken, refreshToken }"| AuthSvc
    AuthSvc -->|"trả nguyên response"| Controller
    Controller -->|"200 OK + JSON"| API
    API -->|"res.data"| Slice
    Slice -->|"lưu localStorage + Redux state.user.role = ADMIN"| UI2["Login.jsx đọc kết quả"]
    UI2 -->|"navigate('/admin')"| Guard("AdminRoute.jsx")
    Guard -->|"kiểm tra user.role === 'ADMIN'"| Dashboard["AdminDashboard (App.jsx route)"]
```

**Bảng phụ — kết nối cụ thể của nhánh Admin:**

| Từ (File A) | Đến (File B) | Cách kết nối | Dữ liệu truyền |
|---|---|---|---|
| `Login.jsx` | `authSlice.js` | `dispatch(loginThunk({ email, password }))` | `{ email, password }` |
| `authSlice.js` | `authService.js` | gọi hàm async `login(credentials)` | `credentials` object |
| `authService.js` | `AuthController.java` | HTTP POST `/api/auth/login` | body JSON `LoginRequest` |
| `AuthController.java` | `AuthenticationService.java` | gọi `authenticationService.login(request, ip)` (Dependency Injection) | `LoginRequest`, `ip` (String) |
| `AuthenticationService.java` | `AdminUserRepository` | `adminUserRepository.findByEmail(email)` | `email` → `Optional<AdminUser>` |
| `AuthenticationService.java` | `AdminAuthService.java` | gọi `adminAuthService.processAdminLogin(admin, password, ip)` (chỉ khi `adminOpt.isPresent()`) | `AdminUser` entity, mật khẩu thô, IP |
| `AdminAuthService.java` | `AdminUserRepository` | `adminUserRepository.save(admin)` | cập nhật `loginAttempts`, `lockedUntil`, `lastLoginAt` |
| `AdminAuthService.java` | `AdminAuditLogRepository` | `adminAuditLogRepository.save(log)` | `AdminAuditLog` (action, targetTable, targetId, ip, description) |
| `AdminAuthService.java` | `JwtProvider.java` | `jwtProvider.generateAdminAccessToken(adminId, email)` | `adminId` (Long), `email` (String) → JWT string |
| `AdminAuthService.java` | `AuthTokenRepository` | `authTokenRepository.save(AuthToken...)` | `AuthToken` với `actorType=ADMIN`, `adminId`, `tokenValue` (random 32-byte base64) |
| `authSlice.js` (loginThunk) | `localStorage` / Redux store | ghi trực tiếp | `accessToken`, `refreshToken`, `jlpt-user` (JSON), `state.auth.user` |
| `Login.jsx` | `AdminRoute.jsx` (gián tiếp qua React Router) | `navigate('/admin')` rồi route guard đọc `state.auth.user.role` | không truyền dữ liệu trực tiếp — guard đọc lại Redux state |

---

## 4. Luồng xử lý theo trình tự

**Kịch bản: Admin đăng nhập tại trang Login dùng chung**

1. Admin nhập email/password vào form của [Login.jsx](../../../apps/frontend/src/pages/login/Login.jsx) và submit. Component gọi `dispatch(loginThunk({ email, password }))` — **không có bước nào phân biệt đây là Admin** ở phía UI.
2. `loginThunk` (trong [authSlice.js](../../../apps/frontend/src/store/slices/authSlice.js)) gọi `authService.login(credentials)`.
3. `authService.js` gửi `POST /api/auth/login` — cùng một endpoint dùng cho mọi role.
4. `AuthController.login()` ([AuthController.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthController.java) dòng 43-49) nhận `LoginRequest` đã validate, lấy `ip` từ `HttpServletRequest`, gọi `authenticationService.login(request, ip)`.
5. `AuthenticationService.login()` ([AuthenticationService.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthenticationService.java) dòng 125-144) **tra bảng `admin_users` trước tiên** bằng `adminUserRepository.findByEmail(request.getEmail())`.
6. **Nếu email thuộc về Admin** (`adminOpt.isPresent()` = true) → gọi ngay `adminAuthService.processAdminLogin(adminOpt.get(), request.getPassword(), ip)` và **trả kết quả luôn**, không kiểm tra bảng Staff/Student nữa. Đây chính là điểm rẽ nhánh mà đề bài yêu cầu xác định.
7. Trong `AdminAuthService.processAdminLogin()` ([AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) dòng 34-112), thứ tự kiểm tra là:
   a. Nếu `admin.getLockedUntil()` còn hiệu lực (chưa qua) → ghi audit `ADMIN_LOGIN_BLOCKED_LOCKED` → ném `BusinessException(429, TOO_MANY_REQUESTS, ...)`.
   b. Nếu `admin.getStatus() == SUSPENDED` → ghi audit `ADMIN_LOGIN_REJECTED_SUSPENDED` → ném `BusinessException(403, ACCOUNT_SUSPENDED, ...)`.
   c. Nếu mật khẩu sai (`passwordEncoder.matches` trả false) → tăng `loginAttempts`; nếu đạt 5 → set `lockedUntil = now + 15 phút`, ghi audit `ADMIN_ACCOUNT_LOCKED`, ném lỗi 429; nếu chưa đạt 5 → ghi audit `ADMIN_LOGIN_FAILED`, ném `BusinessException(401, INVALID_CREDENTIALS, ...)`.
   d. Nếu mật khẩu đúng → reset `loginAttempts = 0`, cập nhật `lastLoginAt`, ghi audit `ADMIN_LOGIN_SUCCESS`, sinh `accessToken` qua `jwtProvider.generateAdminAccessToken()`, sinh `refreshToken` ngẫu nhiên, lưu vào `auth_tokens` với `actorType=ADMIN`, trả về `LoginApiResponse{ role: "ADMIN", accessToken, refreshToken }` (field `user` để `null`).
8. `AuthenticationService` trả nguyên `LoginApiResponse` này ngược lên `AuthController` → HTTP 200 với body `ApiResponse.success(...)`.
9. `loginThunk.fulfilled` (trong `authSlice.js`) nhận `res.data`; vì `user` là `null`, slice tự dựng `userData = { role: "ADMIN" }` (dòng 86-87 của `authSlice.js`), lưu `accessToken`/`refreshToken` vào `localStorage`, lưu `userData` vào `localStorage['jlpt-user']` và `state.auth.user`.
10. `Login.jsx` (dòng 53) kiểm tra `res.role === 'ADMIN' || res.user?.role === 'ADMIN'` → gọi `navigate('/admin')`.
11. React Router render route `/admin`, được bọc bởi `AdminRoute` ([AdminRoute.jsx](../../../apps/frontend/src/components/common/AdminRoute.jsx)): guard đọc lại `state.auth.user.role` — nếu đúng `'ADMIN'` → render `AdminDashboard`; nếu không → redirect `/dashboard`; nếu chưa đăng nhập → redirect `/login`.

```mermaid
sequenceDiagram
    participant UI as "Login.jsx"
    participant Slice as "authSlice.js"
    participant API as "authService.js"
    participant Ctrl as "AuthController.java"
    participant AuthSvc as "AuthenticationService.java"
    participant AdminSvc as "AdminAuthService.java"
    participant AdminDB as "admin_users"
    participant AuditDB as "admin_audit_logs"
    participant TokenDB as "auth_tokens"
    participant Guard as "AdminRoute.jsx"

    UI->>Slice: dispatch(loginThunk({email, password}))
    Slice->>API: login(credentials)
    API->>Ctrl: POST /api/auth/login
    Ctrl->>AuthSvc: login(LoginRequest, ip)
    AuthSvc->>AdminDB: adminUserRepository.findByEmail(email)
    AdminDB-->>AuthSvc: Optional<AdminUser> (present)
    AuthSvc->>AdminSvc: processAdminLogin(admin, password, ip)
    AdminSvc->>AdminSvc: kiểm tra lockedUntil / status SUSPENDED
    AdminSvc->>AdminSvc: passwordEncoder.matches(password, hash)
    alt mật khẩu đúng
        AdminSvc->>AdminDB: save(admin) — loginAttempts=0, lastLoginAt=now
        AdminSvc->>AuditDB: save(ADMIN_LOGIN_SUCCESS)
        AdminSvc->>AdminSvc: jwtProvider.generateAdminAccessToken(id, email)
        AdminSvc->>TokenDB: save(AuthToken actorType=ADMIN)
        AdminSvc-->>AuthSvc: LoginApiResponse{role:"ADMIN", accessToken, refreshToken}
    else mật khẩu sai / bị khóa / bị đình chỉ
        AdminSvc->>AuditDB: save(ADMIN_LOGIN_FAILED / _BLOCKED_LOCKED / _REJECTED_SUSPENDED / ADMIN_ACCOUNT_LOCKED)
        AdminSvc-->>AuthSvc: throw BusinessException(401/403/429)
    end
    AuthSvc-->>Ctrl: LoginApiResponse hoặc exception
    Ctrl-->>API: 200 OK (hoặc lỗi qua Global Exception Handler)
    API-->>Slice: res.data
    Slice->>Slice: lưu localStorage + state.user = {role:"ADMIN"}
    Slice-->>UI: Promise resolved (role: "ADMIN")
    UI->>Guard: navigate('/admin')
    Guard->>Guard: kiểm tra state.auth.user.role === 'ADMIN'
    Guard-->>UI: render AdminDashboard
```

---

## 5. Vai trò từng đoạn code quan trọng

### 5.1. Điểm rẽ nhánh Admin (Backend) — quyết định gọi `AdminAuthService` hay không

**File**: [AuthenticationService.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthenticationService.java) (dòng 121-144)

```java
/**
 * Unified login endpoint for all roles.
 * Lookup order: admin_users → staff_users → student_users.
 */
@Transactional
public LoginApiResponse login(LoginRequest request, String ip) {
    // Bước 1: Tra bảng admin_users TRƯỚC TIÊN theo email — đây chính là "cổng rẽ nhánh"
    // của toàn bộ nhánh Admin. Không có endpoint /admin/login riêng — mọi phân biệt
    // Admin/Staff/Student đều dựa vào việc email đó tồn tại ở bảng nào.
    Optional<AdminUser> adminOpt = adminUserRepository.findByEmail(request.getEmail());
    if (adminOpt.isPresent()) {
        // Nếu tìm thấy: ỦY QUYỀN HOÀN TOÀN cho AdminAuthService xử lý phần còn lại
        // (lockout, audit, sinh JWT riêng cho Admin) — AuthenticationService không
        // tự kiểm tra mật khẩu hay trạng thái tài khoản Admin.
        return adminAuthService.processAdminLogin(adminOpt.get(), request.getPassword(), ip);
    }

    // Bước 2: Chỉ khi KHÔNG phải Admin mới tra tiếp Staff
    Optional<StaffUser> staffOpt = staffUserRepository.findByEmail(request.getEmail());
    if (staffOpt.isPresent()) {
        return handleStaffLogin(staffOpt.get(), request.getPassword(), ip);
    }

    // Bước 3: Cuối cùng mới tra Student
    Optional<StudentUser> studentOpt = studentUserRepository.findByEmail(request.getEmail());
    if (studentOpt.isPresent()) {
        return handleStudentLogin(studentOpt.get(), request.getPassword(), ip);
    }

    // BR-35-09: lỗi chung, không tiết lộ bảng nào đã được kiểm tra (chống dò tài khoản)
    throw new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
}
```

**Giải thích**: Đây là **toàn bộ logic branching** mà đề bài yêu cầu xác định. Không có DTO riêng, không có endpoint riêng cho Admin ở tầng Controller — sự phân biệt hoàn toàn nằm ở **thứ tự tra cứu theo bảng** (`admin_users` → `staff_users` → `student_users`) ngay trong `AuthenticationService.login()`. Vì `AdminUserRepository.findByEmail()` được gọi trước tiên và có `return` ngay khi tìm thấy, nếu một email tồn tại đồng thời ở `admin_users` và bảng khác (về lý thuyết, do không có ràng buộc unique giữa 3 bảng ở tầng code này), tài khoản Admin sẽ luôn "thắng". Dữ liệu nhận vào: `LoginRequest` (email, password) + `ip`. Dữ liệu đưa tiếp: `AdminUser` entity + mật khẩu thô + ip, chuyển cho `AdminAuthService`.

### 5.2. State machine khóa tài khoản + audit trail (Backend) — phần lõi chưa từng được tài liệu hóa

**File**: [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) (dòng 34-112)

```java
@Transactional
public LoginApiResponse processAdminLogin(AdminUser admin, String rawPassword, String ip) {
    // (1) State: ĐANG BỊ KHÓA — lockedUntil còn ở tương lai nghĩa là chưa hết hạn khóa.
    // Không reset loginAttempts ở đây; tài khoản vẫn giữ nguyên số lần sai cho tới khi
    // đăng nhập thành công (xem nhánh thành công bên dưới).
    if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
        long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), admin.getLockedUntil()) + 1;
        audit(admin, "ADMIN_LOGIN_BLOCKED_LOCKED", "admin_users", admin.getId(), ip,
                "Account locked, email=" + admin.getEmail());
        throw new BusinessException(429, "TOO_MANY_REQUESTS",
                "Tài khoản tạm thời bị khóa. Vui lòng thử lại sau " + minutesLeft + " phút");
    }

    // (2) State: SUSPENDED — do Admin khác (hoặc quy trình quản trị) đình chỉ thủ công,
    // độc lập hoàn toàn với cơ chế lockout tự động ở (1)/(3).
    if (admin.getStatus() == AdminUser.AdminStatus.SUSPENDED) {
        log.warn("[AdminAuthService] Login rejected — suspended email={}", admin.getEmail());
        audit(admin, "ADMIN_LOGIN_REJECTED_SUSPENDED", "admin_users", admin.getId(), ip, null);
        throw new BusinessException(403, "ACCOUNT_SUSPENDED",
                "Tài khoản bị đình chỉ. Lý do: " + admin.getSuspendReason());
    }

    // (3) Sai mật khẩu: tăng dần loginAttempts; đạt ngưỡng MAX_LOGIN_ATTEMPTS (5) thì
    // chuyển sang state LOCKED bằng cách set lockedUntil = now + 15 phút.
    if (!passwordEncoder.matches(rawPassword, admin.getPasswordHash())) {
        int attempts = admin.getLoginAttempts() + 1;
        admin.setLoginAttempts(attempts);
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            admin.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            adminUserRepository.save(admin);
            audit(admin, "ADMIN_ACCOUNT_LOCKED", "admin_users", admin.getId(), ip,
                    "Locked after " + attempts + " failed attempts");
            throw new BusinessException(429, "TOO_MANY_REQUESTS", "...");
        }
        adminUserRepository.save(admin);
        audit(admin, "ADMIN_LOGIN_FAILED", "admin_users", admin.getId(), ip,
                "Bad credentials, attempt=" + attempts);
        throw new BusinessException(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
    }

    // (4) Thành công: reset counter về 0 — đây là điểm DUY NHẤT trong code
    // đưa state machine trở lại "sạch" (không có API/nghiệp vụ unlock thủ công
    // nào khác được tìm thấy trong package feature.admin — xem Mục 8).
    admin.setLoginAttempts(0);
    admin.setLastLoginAt(LocalDateTime.now());
    adminUserRepository.save(admin);
    audit(admin, "ADMIN_LOGIN_SUCCESS", "admin_users", admin.getId(), ip, "Login success");

    // Sinh token: JWT ngắn hạn (claim role=ADMIN + adminId) và refresh token ngẫu nhiên
    // (KHÔNG phải JWT — 32 byte random, base64 url-safe), lưu refresh token vào DB riêng
    // biệt với actorType=ADMIN để phân biệt với refresh token của Staff/Student.
    String accessToken = jwtProvider.generateAdminAccessToken(admin.getId(), admin.getEmail());
    String refreshToken = generateToken();
    authTokenRepository.save(AuthToken.builder()
            .actorType(AuthToken.ActorType.ADMIN)
            .adminId(admin.getId())
            .tokenType(AuthToken.TokenType.REFRESH)
            .tokenValue(refreshToken)
            .ipAddress(ip)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build());

    return LoginApiResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .role("ADMIN")
            .build();
}
```

**Giải thích**: Đây là phần nghiệp vụ **hoàn toàn không trùng lặp** với tài liệu Authentication tổng quát đã có. So với nhánh Staff (`handleStaffLogin`, cũng khóa sau 5 lần sai/15 phút) và Student (`handleStudentLogin`), nhánh Admin khác biệt ở 2 điểm quan trọng: (a) **ghi audit log cho MỌI kết quả** (kể cả các lần thất bại/bị chặn) vào bảng riêng `admin_audit_logs` — Staff/Student không có audit log tương đương; (b) refresh token của Admin là **chuỗi ngẫu nhiên tự sinh** (`generateToken()`, 32 byte `SecureRandom` + Base64 URL-safe), không dùng `jwtProvider.generateRefreshToken()`/`generateTokenFromUsername()` như Student. Access token thì vẫn là JWT nhưng qua hàm riêng `generateAdminAccessToken()` gắn claim `adminId`.

### 5.3. Sinh JWT chuyên biệt cho Admin (Backend)

**File**: [JwtProvider.java](../../../apps/backend/src/main/java/com/jlpt/shared/security/JwtProvider.java) (dòng 50-59)

```java
/** Issues a 15-min JWT with role=ADMIN claim for Admin Panel access (BR-35-07). */
public String generateAdminAccessToken(Long adminId, String email) {
    return Jwts.builder()
            .subject(email)
            .claim("role", "ADMIN")     // Claim này được Spring Security filter đọc để
                                        // gán quyền truy cập API /api/admin/**
            .claim("adminId", adminId)  // Nhúng adminId thẳng vào token — tránh phải
                                        // query lại DB để biết "ai đang gọi" ở các API sau
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtAccessExpirationMs))
            .signWith(getSigningKey())
            .compact();
}
```

**Giải thích**: Hàm này tách biệt hoàn toàn khỏi hàm sinh token cho Student (`generateAccessToken(authentication)`) và Staff (`generateStaffAccessToken(staffId, email)`), dù cả ba đều dùng chung `getSigningKey()`. Nhận vào `adminId` + `email` từ `AdminAuthService`, trả ra chuỗi JWT được `authSlice.js` lưu vào `localStorage.accessToken` và mọi request sau đó tự động đính `Authorization: Bearer <token>` (xem interceptor tại `authService.js`, đã mô tả ở tài liệu gốc).

### 5.4. Điều hướng theo `role` sau đăng nhập (Frontend)

**File**: [Login.jsx](../../../apps/frontend/src/pages/login/Login.jsx) (dòng 49-64)

```javascript
try {
  const res = await dispatch(loginThunk({ email, password })).unwrap();
  if (res.requirePasswordChange) {
    navigate('/staff/change-temp-password');
  } else if (res.role === 'ADMIN' || res.user?.role === 'ADMIN') {
    // Không có form/route login riêng cho Admin — chỉ dựa vào giá trị `role`
    // trả về từ backend (LoginApiResponse.role = "ADMIN") để điều hướng.
    navigate('/admin');
  } else if (res.role === 'STAFF' || res.user?.role === 'STAFF') {
    // ... nhánh Staff (đã có ở tài liệu gốc) ...
  } else {
    // ... nhánh Student ...
  }
} catch {
  /* lỗi API đã được set vào Redux state (bao gồm cả lỗi từ AdminAuthService) */
}
```

**Giải thích**: Xác nhận rằng UI **không có code riêng biệt** xử lý Admin ngoài điều kiện rẽ nhánh điều hướng này. Toàn bộ khác biệt nghiệp vụ (lockout, audit, JWT riêng) đều xảy ra ở backend và chỉ "lộ" ra frontend qua field `role` trong response.

### 5.5. Xử lý field `user: null` riêng cho Admin (Frontend Redux)

**File**: [authSlice.js](../../../apps/frontend/src/store/slices/authSlice.js) (dòng 60-93, trích đoạn liên quan)

```javascript
// Backend LoginApiResponse: field is `user` (not `student`), plus `role` at top level
const { requirePasswordChange, role, accessToken, refreshToken, user, staffRole } = res.data;
// ...
// For ADMIN direct login, `user` is null — build a minimal object from role
const userData = user ? { ...user, role } : { role };
if (staffRole) userData.staffRole = staffRole;
localStorage.setItem('accessToken', accessToken);
if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
localStorage.setItem('jlpt-user', JSON.stringify(userData));
return { user: userData, role };
```

**Giải thích**: Vì `AdminAuthService.processAdminLogin()` (Mục 5.2) build `LoginApiResponse` **không set field `user`** (không có `StudentResponse` cho Admin), comment trong code xác nhận rõ ràng đây là hành vi cố ý (`// For ADMIN direct login, user is null`). Slice phải tự dựng object `{ role: "ADMIN" }` để Redux state và `AdminRoute` có thể đọc `user.role`.

### 5.6. Route Guard phía Frontend cho khu vực Admin

**File**: [AdminRoute.jsx](../../../apps/frontend/src/components/common/AdminRoute.jsx) (toàn bộ, dòng 10-23)

```javascript
function AdminRoute({ children }) {
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Kiểm tra lại role phía CLIENT — đây chỉ là UX (ẩn UI), KHÔNG thay thế cho việc
  // backend phải tự trả 403/401 khi gọi các API /api/admin/** mà không có JWT
  // role=ADMIN hợp lệ (xem CLAUDE.md — Anti-pattern "Authorization by UI hide").
  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
```

**Giải thích**: Đây là bước cuối trong luồng — nhận `user.role` đã được lưu ở Mục 5.5, quyết định có cho render `AdminDashboard` (component lazy-load qua `App.jsx`, dòng 50 và 139) hay không. Việc bảo vệ thực sự (không thể bypass bằng sửa localStorage) nằm ở tầng Spring Security filter đọc claim `role=ADMIN` trong JWT (Mục 5.3) khi gọi các API `/api/admin/**` — không đọc được trong phạm vi khảo sát (xem Mục 8).

---

## 6. Dữ liệu di chuyển như thế nào

Theo dõi cụ thể trạng thái **lockout** (`loginAttempts` / `lockedUntil`) của một `AdminUser` xuyên suốt hệ thống:

1. **Khởi tạo**: Khi tạo `AdminUser` mới (không thuộc phạm vi tài liệu này), `loginAttempts` mặc định = `0` (`@Builder.Default`, [AdminUser.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUser.java) dòng 41-43), `lockedUntil` mặc định = `null`.
2. **Mỗi lần sai mật khẩu**: `AdminAuthService.processAdminLogin()` đọc `admin.getLoginAttempts()` (kiểu `Integer`, từ entity đã được JPA load từ cột `login_attempts` trong bảng `admin_users`), cộng 1, gọi `admin.setLoginAttempts(attempts)`, rồi `adminUserRepository.save(admin)` — Hibernate `UPDATE admin_users SET login_attempts = ? WHERE admin_id = ?` (dirty checking, cùng transaction `@Transactional`).
3. **Khi đạt ngưỡng 5**: thêm `admin.setLockedUntil(LocalDateTime.now().plusMinutes(15))` — giá trị `LocalDateTime` này được lưu vào cột `locked_until` (kiểu DATETIME, MySQL). Theo `CLAUDE.md ADR-009`, container MySQL chạy UTC, nên giá trị `LocalDateTime.now()` ở đây là thời điểm server (JVM chạy trong container, cùng UTC) — nhất quán, không lệch múi giờ trong phạm vi backend.
4. **Ở request đăng nhập tiếp theo (khi đang khóa)**: `admin.getLockedUntil().isAfter(LocalDateTime.now())` được so sánh ngay tại Java, số phút còn lại (`minutesLeft`) được tính bằng `ChronoUnit.MINUTES.between(...)` rồi **nhúng trực tiếp vào message lỗi** trả cho client dưới dạng chuỗi tiếng Việt (VD: "Tài khoản tạm thời bị khóa. Vui lòng thử lại sau 12 phút") — không trả về dưới dạng số/timestamp riêng cho frontend tính toán.
5. **Ra đến Frontend**: chuỗi lỗi này đi qua `BusinessException` → Global Exception Handler (ADR-008, không đọc chi tiết trong phạm vi khảo sát này) → JSON response → `authService.js` (bị bắt bởi `catch` trong `axios`, không phải interceptor 401 vì đây là lỗi 429/403) → `authSlice.js` (`loginThunk.rejected`, dùng `extractError()`) → hiển thị trong `Login.jsx` qua biến `error`/`errorCode` (`isLocked = errorCode === 'TOO_MANY_REQUESTS' || errorCode === 'ACCOUNT_SUSPENDED'`, dòng 81) → render trong `<AuthBanner type="warning">`.
6. **Khi đăng nhập thành công sau đó**: `admin.setLoginAttempts(0)` — state machine quay về trạng thái sạch; `lockedUntil` **không được set lại về `null` một cách tường minh** trong đoạn code đã đọc (chỉ để nguyên giá trị cũ trong quá khứ, không còn hiệu lực vì đã qua hạn) — xem ghi chú ở Mục 8.
7. **Song song**, mọi lần đổi trạng thái (`ADMIN_LOGIN_FAILED`, `ADMIN_ACCOUNT_LOCKED`, `ADMIN_LOGIN_BLOCKED_LOCKED`, `ADMIN_LOGIN_SUCCESS`, `ADMIN_LOGIN_REJECTED_SUSPENDED`) được ghi thành 1 row mới trong `admin_audit_logs` qua `AdminAuditLog.builder()...adminAuditLogRepository.save(log)` — dữ liệu này **không quay lại Frontend của Login.jsx**, chỉ dùng để tra cứu sau này (có thể qua [AdminAuditLogController.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogController.java), không thuộc phạm vi khảo sát của tài liệu này).

---

## 7. Bảng tra cứu tổng hợp

| Bước | File | Function | Kết nối tới | Dữ liệu | Ghi chú |
|---|---|---|---|---|---|
| Đăng nhập (UI) | [Login.jsx](../../../apps/frontend/src/pages/login/Login.jsx) | `handleSubmit()` | `authSlice.js` | `{ email, password }` | UI dùng chung, không phân biệt Admin |
| Đăng nhập (Redux) | [authSlice.js](../../../apps/frontend/src/store/slices/authSlice.js) | `loginThunk` | `authService.js` | `credentials` | Thunk dùng chung |
| Đăng nhập (API) | [authService.js](../../../apps/frontend/src/api/authService.js) | `login()` | `AuthController` | `LoginRequest` | POST `/api/auth/login` |
| Nhận request (BE) | [AuthController.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthController.java) | `login()` | `AuthenticationService` | `LoginRequest`, `ip` | Không có endpoint riêng cho Admin |
| **Rẽ nhánh Admin** | [AuthenticationService.java](../../../apps/backend/src/main/java/com/jlpt/feature/auth/AuthenticationService.java) | `login()` (dòng 127-129) | `AdminUserRepository`, `AdminAuthService` | `Optional<AdminUser>` | **Điểm quyết định chính** — tra `admin_users` trước Staff/Student |
| Kiểm tra lockout | [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) | `processAdminLogin()` (dòng 36-49) | `AdminUser.getLockedUntil()` | `LocalDateTime` | Chặn nếu còn trong thời gian khóa |
| Kiểm tra suspend | [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) | `processAdminLogin()` (dòng 51-56) | `AdminUser.getStatus()` | `AdminStatus.SUSPENDED` | Chặn tài khoản bị đình chỉ |
| Đếm sai mật khẩu | [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) | `processAdminLogin()` (dòng 58-87) | `AdminUserRepository.save()` | `loginAttempts`, `lockedUntil` | Khóa sau 5 lần sai, 15 phút |
| Ghi audit | [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) | `audit()` (dòng 120-130) | `AdminAuditLogRepository` | `AdminAuditLog` entity | Ghi ở MỌI nhánh kết quả |
| Sinh JWT Admin | [JwtProvider.java](../../../apps/backend/src/main/java/com/jlpt/shared/security/JwtProvider.java) | `generateAdminAccessToken()` (dòng 51-59) | trả về `AdminAuthService` | `adminId`, `email` → JWT | Claim `role=ADMIN`, `adminId` |
| Lưu refresh token | [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) | `processAdminLogin()` (dòng 97-105) | `AuthTokenRepository` | `AuthToken{actorType=ADMIN}` | Refresh token random, KHÔNG phải JWT |
| Nhận response (FE) | [authSlice.js](../../../apps/frontend/src/store/slices/authSlice.js) | `loginThunk.fulfilled` (dòng 60-93) | `localStorage`, Redux state | `{ role: "ADMIN" }` | `user` là `null` từ backend, tự dựng object |
| Điều hướng | [Login.jsx](../../../apps/frontend/src/pages/login/Login.jsx) | `handleSubmit()` (dòng 53-54) | React Router | `res.role === 'ADMIN'` | `navigate('/admin')` |
| Route Guard | [AdminRoute.jsx](../../../apps/frontend/src/components/common/AdminRoute.jsx) | `AdminRoute()` | `AdminDashboard` (qua `App.jsx`) | `state.auth.user.role` | Redirect `/dashboard` nếu không phải ADMIN |

---

## 8. Các mục cần bổ sung context (nếu có)

- **Không tìm thấy trong source code**: cơ chế **mở khóa thủ công** (unlock) tài khoản Admin bị `lockedUntil`. Đã grep toàn bộ package `com.jlpt.feature.admin` cho `loginAttempts`/`lockedUntil` và chỉ thấy xuất hiện trong [AdminUser.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUser.java) và [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) — không có endpoint/service nào khác set lại `lockedUntil = null` hoặc `loginAttempts = 0` ngoài luồng đăng nhập thành công. Nếu có tính năng "Admin khác mở khóa cho tài khoản bị khóa", nó nằm ngoài phạm vi các file đã khảo sát hoặc chưa được implement — cần người dùng xác nhận.
- **Không xác nhận được**: `lockedUntil` có được set về `null` một cách tường minh khi đăng nhập thành công hay không. Đoạn code đọc được (dòng 89-91 của `AdminAuthService.java`) chỉ có `admin.setLoginAttempts(0)` và `admin.setLastLoginAt(...)`, không có dòng `admin.setLockedUntil(null)`. Về mặt logic, giá trị `lockedUntil` cũ (đã ở quá khứ) sẽ không còn chặn được lần đăng nhập sau vì điều kiện là `isAfter(LocalDateTime.now())`, nhưng đây là suy luận từ logic điều kiện, không phải xác nhận trực tiếp ý đồ thiết kế — không tìm thấy comment hay test nào làm rõ ý đồ.
- **Không đọc trong phạm vi khảo sát này**: [GlobalExceptionHandler](../../../apps/backend/src/main/java) (`@ControllerAdvice`, ADR-008) — cách chính xác `BusinessException(429, "TOO_MANY_REQUESTS", ...)` được chuyển thành JSON response cho client. Đã suy luận từ format `{ status, message, data }` mô tả trong `CLAUDE.md`, nhưng không trích dẫn được số dòng thật.
- **Không đọc trong phạm vi khảo sát này**: tầng Spring Security filter (`JwtAuthenticationFilter` hoặc tương đương) — nơi thực sự đọc claim `role=ADMIN` từ JWT để cấp quyền truy cập `/api/admin/**`. `AdminRoute.jsx` (Mục 5.6) chỉ là bảo vệ UI phía client; phần backend-side authorization tương ứng chưa được xác minh trực tiếp trong tài liệu này — theo `CLAUDE.md` (anti-pattern "Authorization by UI hide"), phần này bắt buộc phải tồn tại ở backend, nhưng file cụ thể nằm ngoài danh sách đã đọc.
- **Không tìm thấy trong source code**: endpoint/route riêng dạng `/admin/login` hay DTO `AdminLoginRequest` — đã xác nhận (Mục 5.1, 5.4) rằng không tồn tại; Admin dùng chung 100% endpoint `/api/auth/login` và component `Login.jsx` với Student/Staff. Đây là kết luận tích cực (đã xác minh), không phải thiếu context, nhưng ghi lại ở đây để tránh hiểu nhầm khi đọc tài liệu.
- **Chưa xác minh**: nội dung chi tiết của [AdminUserService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java) và [AdminAuditLogService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java) (có tồn tại trong thư mục nhưng không được đọc vì nằm ngoài phạm vi "login flow" được giao) — có thể chứa nghiệp vụ liên quan (VD: admin tạo/suspend admin khác) nhưng không thuộc luồng đăng nhập nên không đưa vào tài liệu này.
