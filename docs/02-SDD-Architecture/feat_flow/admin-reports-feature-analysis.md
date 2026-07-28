# Phân Tích Feature: admin-reports (Analytics & Reporting — Report Screen / "Admin Reports Page")

> **Tác giả phân tích:** AI Senior Software Architect
> **Ngày phân tích:** 2026-07-27
> **Nguồn:** Đọc trực tiếp source code trong workspace

---

## 1. Tóm tắt tổng quan

Backlog task đặt tên feature này là "Analytics & Reporting — Report Screen", gợi ý một dashboard biểu đồ/thống kê. Sau khi đọc trực tiếp source code, thực tế **`AdminReports.jsx` không phải là dashboard phân tích (BI/charts)** — nó là một **trình xem nhật ký kiểm toán (audit log viewer)**: một bảng phân trang, liệt kê từng hành động (action) mà Admin/Manager/Staff/Học viên đã thực hiện trên hệ thống, có thể lọc theo loại hành động (`action`) qua dropdown. Không có biểu đồ, không có số liệu tổng hợp (KPI/aggregate) nào trên trang này — phần đó thuộc về `AdminDashboard.jsx` (feature khác, dùng `getDashboardOverview()`), không phải trang này.

Feature trải dài trên 3 tầng:

| Tầng | Mô tả |
|------|-------|
| **Frontend (React)** | Trang [AdminReports.jsx](../../../apps/frontend/src/pages/admin/AdminReports.jsx) — gọi `getAuditLog()` trong [adminService.js](../../../apps/frontend/src/api/adminService.js), hiển thị bảng phân trang + dropdown lọc theo `action`, dùng helper [auditMeta.js](../../../apps/frontend/src/utils/auditMeta.js) để dịch mã hành động (`create_staff`, `suspend_user`, ...) sang nhãn tiếng Việt và màu chip |
| **Backend (Spring Boot)** | [AdminAuditLogController.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogController.java) nhận `GET /api/admin/audit-logs` → [AdminAuditLogService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java) truy vấn + map sang DTO → [AdminAuditLogRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java) đọc bảng `admin_audit_logs` |
| **Database (MySQL)** | Bảng `admin_audit_logs`, entity [AdminAuditLog.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLog.java) — mỗi dòng là 1 hành động, có FK tùy chọn tới `admin_users`/`staff_users`/`student_users` (chỉ 1 trong 3 được set, tùy ai thực hiện hành động) |

**Entry point** của feature (phần đọc/hiển thị): route `/admin/reports` (khai báo tại [App.jsx](../../../apps/frontend/src/App.jsx) dòng 142) → `GET /api/admin/audit-logs` (backend).

**Điểm quan trọng cần hiểu đúng phạm vi feature này**: bảng `admin_audit_logs` là **bảng dùng chung (shared table)**, được **ghi vào (produce)** bởi rất nhiều feature khác trên toàn hệ thống (đăng nhập Admin, quản lý user, kiểm duyệt nội dung, ticket hỗ trợ, chấm bài, thông báo broadcast, quy tắc thông báo tự động...). **Code riêng của feature "admin-reports" (task này) chỉ là phía ĐỌC/HIỂN THỊ**: Controller + Service + Repository + Entity nói trên, cộng trang `AdminReports.jsx`. Các Service ở các feature khác **ghi** vào cùng bảng này ("producer") **không thuộc phạm vi feature admin-reports** — chúng được liệt kê đầy đủ ở mục 3 và 6 để người đọc hiểu luồng dữ liệu tổng thể, nhưng thuộc quyền sở hữu của feature tương ứng (xem link chéo ở mục 3.3).

Use case liên quan (theo comment trong code): **UC-38** (`AdminAuditLogController`/`AdminAuditLogService` ghi rõ "UC-38"), UC-36 (comment tại `adminService.js` gộp chung dashboard + audit log dưới UC-36).

---

## 2. Bản đồ cấu trúc (các "mảnh" và vai trò)

### 2.1 Frontend — phần đọc/hiển thị (thuộc feature này)

| File | Vai trò | Loại |
|------|---------|------|
| [AdminReports.jsx](../../../apps/frontend/src/pages/admin/AdminReports.jsx) | Trang chính: gọi `getAuditLog`, quản lý state phân trang/lọc/loading/error, render bảng | Page Component |
| [adminService.js](../../../apps/frontend/src/api/adminService.js) | Hàm `getAuditLog({ page, size, action, targetTable })` gọi `GET /admin/audit-logs` | API Service |
| [authService.js](../../../apps/frontend/src/api/authService.js) | Axios instance dùng chung (đính Bearer token, auto-refresh 401) mà `adminService.js` import (`import api from './authService'`) | Axios Config |
| [auditMeta.js](../../../apps/frontend/src/utils/auditMeta.js) | Bảng tra cứu tĩnh: `ACTION_LABELS`, `ACTION_COLORS`, `ROLE_META`, `ACTION_GROUPS` + hàm `getActionLabel`, `getActionColors`, `getRoleMeta` — dịch mã hành động/role từ BE sang nhãn + màu hiển thị | Utility |
| [ManageUsersIcons.jsx](../../../apps/frontend/src/components/admin/ManageUsersIcons.jsx) | Cung cấp icon `IcAdminChip` (hoa anh đào 5 cánh) dùng làm icon chip tiêu đề trang | Component (icon) |
| [AdminPageHeader.jsx](../../../apps/frontend/src/components/admin/AdminPageHeader.jsx) | Component tiêu đề trang dùng chung toàn khu Admin (chip icon, title, subtitle, mascot) | Component |
| [Pagination.jsx](../../../apps/frontend/src/components/common/Pagination.jsx) | Component phân trang dùng chung (props `currentPage`, `totalPages`, `onChange`) | Component |
| [EmptyState.jsx](../../../apps/frontend/src/components/common/EmptyState.jsx) | Hiển thị khi `logs.length === 0` (không tìm thấy trong file — chỉ xác nhận usage, chưa đọc nội dung file này) | Component |
| [AdminTopNav.jsx](../../../apps/frontend/src/components/layout/AdminTopNav.jsx) | Thanh điều hướng khu Admin, tab "reports" active khi ở trang này | Component (Nav) |
| [App.jsx](../../../apps/frontend/src/App.jsx) | Khai báo route `/admin/reports` → `AdminReports.jsx`, bọc trong `AdminRoute` | Router Config |
| [AdminRoute.jsx](../../../apps/frontend/src/components/common/AdminRoute.jsx) | Guard phía client: redirect `/login` nếu chưa đăng nhập, redirect `/dashboard` nếu `user.role !== 'ADMIN'` | Route Guard |

### 2.2 Backend — phần đọc/hiển thị (thuộc feature này)

| File | Vai trò | Loại |
|------|---------|------|
| [AdminAuditLogController.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogController.java) | `GET /api/admin/audit-logs` — nhận `action`, `targetTable`, `page`, `size` (validate `@Min`/`@Max`), `@PreAuthorize("hasRole('ADMIN')")` | Controller |
| [AdminAuditLogService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java) | Chuẩn hoá filter rỗng → `null`, gọi repository, map `AdminAuditLog` → `AuditLogItemResponse` (chọn actor email/tên/role từ 1 trong 3 FK) | Service |
| [AdminAuditLogRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java) | JPQL `findByFilters` (lọc action/targetTable, sort `createdAt DESC`) + `findFirstByTargetIdAndTargetTableAndActionInOrderByCreatedAtDesc` (dùng bởi feature khác, xem mục 3.3) | Repository |
| [AdminAuditLog.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLog.java) | Entity JPA bảng `admin_audit_logs`: 3 FK tùy chọn (`adminActor`/`staffActor`/`studentActor`), `action`, `targetTable`, `targetId`, `description`, `ipAddress`, `createdAt` | Entity |
| [AuditLogItemResponse.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/dto/AuditLogItemResponse.java) | DTO trả về FE: `logId`, `actionType`, `targetEmail`, `adminEmail`, `actorName`, `actorRole`, `description`, `createdAt` | DTO Response |
| [ApiResponse.java](../../../apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java) | Envelope response chung `{status, message, code, data}` — `ApiResponse.success(data)` bọc kết quả trước khi trả về FE | Wrapper (shared) |
| [SecurityConfig.java](../../../apps/backend/src/main/java/com/jlpt/shared/config/SecurityConfig.java) | Cấu hình toàn cục: mọi request khớp `/api/admin/**` phải có `hasRole("ADMIN")` (dòng 56-57) — lớp bảo vệ thứ 2 độc lập với `@PreAuthorize` trên controller | Security Config |

### 2.3 Backend — "producer" ghi vào bảng `admin_audit_logs` (KHÔNG thuộc feature này — liệt kê để tham chiếu, xem mục 3.3/6)

| File | Vai trò (trong feature gốc của nó) | Loại |
|------|---------|------|
| [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) | Ghi `ADMIN_LOGIN_SUCCESS` / `ADMIN_LOGIN_REJECTED_SUSPENDED` khi Admin đăng nhập | Service (feature admin-panel-login) |
| [AdminUserService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java) | Ghi `create_staff`, `update_user`, `suspend_user`, `activate_user`, `reset_password_initiated`, `soft_delete_user`, `restore_user`, `change_staff_role` khi Admin thao tác trên user | Service (feature admin-user-management) |
| [StaffPasswordResetService.java](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java) | Ghi `issue_temp_password` khi Admin cấp mật khẩu tạm cho Staff | Service (feature staff-password-reset) |
| [ReviewAuditService.java](../../../apps/backend/src/main/java/com/jlpt/feature/contentreview/ReviewAuditService.java) | Ghi `approve_content` / `reject_content` / `request_changes_content` khi Manager kiểm duyệt nội dung (UC-33) | Service (feature content-review) |
| [StaffReviewFeedbackService.java](../../../apps/backend/src/main/java/com/jlpt/feature/staffcontent/feedback/StaffReviewFeedbackService.java) | Không ghi — chỉ **đọc lại** bản ghi `reject_content`/`request_changes_content` mới nhất qua `findFirstByTargetIdAndTargetTableAndActionInOrderByCreatedAtDesc` | Service (feature khác, đọc từ bảng chung) |
| [QuizService.java](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java) | Ghi `ASSESSMENT_SOFT_DELETED`, `QUIZ_SUBMITTED` | Service (feature assessment) |
| [MockExamService.java](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java) | Ghi `EXAM_SUBMITTED` | Service (feature assessment) |
| [SupportTicketService.java](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java) | Ghi `TICKET_ASSIGNED`, `TICKET_CLOSED`, `SUBMISSION_GRADED` | Service (feature student-ticket-support) |
| [NotificationRuleService.java](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java) | Ghi `NOTIFICATION_RULE_CREATED`, `NOTIFICATION_RULE_UPDATED` (không tìm thấy log cho DELETE dù `auditMeta.js` có nhãn `notification_rule_deleted` — xem mục 8) | Service (feature notification-rule) |
| [NotificationService.java](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java) | Ghi `BROADCAST_SENT` | Service (feature notification) |

**Ghi chú quan trọng**: `AdminSettingsService.java` (nhóm cài đặt hệ thống — `/api/admin/settings/**`) **KHÔNG ghi** vào `admin_audit_logs` trong source code hiện tại — đã grep toàn bộ backend cho `AdminAuditLog`/`update_setting`/`setting_updated` và không tìm thấy nơi nào tạo bản ghi audit khi cập nhật setting, dù `auditMeta.js` có định nghĩa sẵn nhãn `update_setting`/`setting_updated`. Đây khác với giả định ban đầu của task — được ghi rõ ở mục 8 thay vì suy diễn cho đủ.

---

## 3. Bản đồ kết nối (ai gọi ai, dữ liệu truyền qua đâu)

### 3.1 Diagram Mermaid — Kiến trúc phần đọc/hiển thị (thuộc feature admin-reports)

```mermaid
graph TD
    A["AdminReports.jsx (Page Component)"] -->|"gọi getAuditLog({page, size, action})"| B["adminService.js"]
    B -->|"import axios instance"| C["authService.js (axios + interceptor Bearer/refresh)"]
    C -->|"HTTP GET /api/admin/audit-logs Bearer JWT"| D["AdminAuditLogController.java"]

    D -->|"@PreAuthorize hasRole ADMIN + gọi getAuditLogs()"| E["AdminAuditLogService.java"]
    E -->|"findByFilters(action, targetTable, pageable)"| F["AdminAuditLogRepository.java"]
    F -->|"SELECT ... FROM admin_audit_logs"| G[("admin_audit_logs (MySQL)")]
    G -->|"trả về Page&lt;AdminAuditLog&gt;"| F
    F -->|"trả Page&lt;Entity&gt;"| E
    E -->|"map sang AuditLogItemResponse"| D
    D -->|"bọc ApiResponse.success(Map content/totalElements/totalPages)"| C
    C -->|"res.data.data"| B
    B -->|"{content, totalPages}"| A

    A -->|"import nhãn/màu"| H["auditMeta.js"]
    A -->|"import icon"| I["ManageUsersIcons.jsx (IcAdminChip)"]
    A -->|"render header"| J["AdminPageHeader.jsx"]
    A -->|"render phân trang"| K["Pagination.jsx"]
    A -->|"render khi rỗng"| L["EmptyState.jsx"]

    M["App.jsx"] -->|"route /admin/reports"| N["AdminRoute.jsx (guard role ADMIN, client-side)"]
    N -->|"render children"| A
```

### 3.2 Diagram Mermaid — Bảng `admin_audit_logs` là điểm hội tụ của nhiều feature (producers → bảng dùng chung → admin-reports đọc lại)

```mermaid
graph LR
    P1["AdminAuthService.java (đăng nhập Admin)"] -->|"save() ADMIN_LOGIN_SUCCESS"| DB[("admin_audit_logs")]
    P2["AdminUserService.java (quản lý user)"] -->|"save() suspend_user / activate_user / ..."| DB
    P3["StaffPasswordResetService.java"] -->|"save() issue_temp_password"| DB
    P4["ReviewAuditService.java (kiểm duyệt nội dung)"] -->|"save() approve/reject/request_changes_content"| DB
    P5["QuizService.java"] -->|"save() QUIZ_SUBMITTED"| DB
    P6["MockExamService.java"] -->|"save() EXAM_SUBMITTED"| DB
    P7["SupportTicketService.java"] -->|"save() TICKET_ASSIGNED / TICKET_CLOSED / SUBMISSION_GRADED"| DB
    P8["NotificationRuleService.java"] -->|"save() NOTIFICATION_RULE_CREATED / UPDATED"| DB
    P9["NotificationService.java"] -->|"save() BROADCAST_SENT"| DB

    DB -->|"findByFilters() — ĐỌC"| R["AdminAuditLogRepository.java (feature admin-reports)"]
    R --> S["AdminAuditLogService.java"]
    S --> T["AdminAuditLogController.java"]
    T -->|"GET /api/admin/audit-logs"| U["AdminReports.jsx"]

    DB -.->|"findFirstByTargetId... — ĐỌC (feature khác)"| V["StaffReviewFeedbackService.java"]
```

### 3.3 Bảng phụ: kết nối chi tiết

| Từ (File A) | Đến (File B) | Cách kết nối | Dữ liệu truyền |
|---|---|---|---|
| [AdminReports.jsx](../../../apps/frontend/src/pages/admin/AdminReports.jsx) | [adminService.js](../../../apps/frontend/src/api/adminService.js) | import hàm `getAuditLog` | `{ page, size, action }` → Promise trả `{ content, totalPages }` |
| [adminService.js](../../../apps/frontend/src/api/adminService.js) | [authService.js](../../../apps/frontend/src/api/authService.js) | import instance axios `api` | request có sẵn header `Authorization: Bearer <token>` |
| [adminService.js](../../../apps/frontend/src/api/adminService.js) | [AdminAuditLogController.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogController.java) | gọi API `GET /admin/audit-logs` | query params `action`, `targetTable`, `page`, `size` |
| [AdminAuditLogController.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogController.java) | [AdminAuditLogService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java) | gọi phương thức `getAuditLogs(action, targetTable, page, size)` | tham số nguyên thủy đã validate |
| [AdminAuditLogService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java) | [AdminAuditLogRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java) | gọi `findByFilters(a, t, PageRequest.of(page, size))` | trả `Page<AdminAuditLog>` |
| [AdminAuditLogRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java) | Bảng `admin_audit_logs` (MySQL) | JPQL query (Hibernate) | đọc toàn bộ cột entity, kèm join LAZY tới `admin_users`/`staff_users`/`student_users` khi cần |
| **AdminAuthService / AdminUserService / ReviewAuditService / QuizService / MockExamService / SupportTicketService / NotificationRuleService / NotificationService.java** (10 file, feature khác) | Bảng `admin_audit_logs` (MySQL) | `adminAuditLogRepository.save(AdminAuditLog.builder()...)` | ghi hành động của feature đó — **không thuộc code của feature admin-reports**, xem link chéo bên dưới |
| [StaffReviewFeedbackService.java](../../../apps/backend/src/main/java/com/jlpt/feature/staffcontent/feedback/StaffReviewFeedbackService.java) | [AdminAuditLogRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java) | gọi `findFirstByTargetIdAndTargetTableAndActionInOrderByCreatedAtDesc` | đọc lại 1 bản ghi reject/request_changes cụ thể (feature khác dùng bảng chung, không phải trang Reports) |

**Link chéo tới tài liệu phân tích của các feature "producer"** (một số đã tồn tại, một số đang được viết song song nên có thể chưa tồn tại tại thời điểm đọc file này):

- `docs/02-SDD-Architecture/feat_flow/admin-panel-login-feature-analysis.md` — phân tích [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java) (producer `ADMIN_LOGIN_SUCCESS`)
- `docs/02-SDD-Architecture/feat_flow/admin-user-management-feature-analysis.md` — phân tích [AdminUserService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java) (producer `suspend_user`, `activate_user`, `soft_delete_user`, `create_staff`, `change_staff_role`, `restore_user`, `update_user`, `reset_password_initiated`)
- [student-ticket-support-feature-analysis.md](../../../docs/02-SDD-Architecture/feat_flow/student-ticket-support-feature-analysis.md) — đã tồn tại, liên quan tới `SupportTicketService.java` (producer `TICKET_ASSIGNED`/`TICKET_CLOSED`/`SUBMISSION_GRADED`) — chưa xác nhận nội dung file này có đề cập audit log hay không (nằm ngoài phạm vi đọc của phân tích này)

---

## 4. Luồng xử lý theo trình tự

Kịch bản: Admin đã đăng nhập mở trang `/admin/reports`, chọn bộ lọc "Hành động" trong dropdown, xem trang 1 danh sách log.

1. **Điều hướng route** — `App.jsx` dòng 142 khớp path `/admin/reports`, bọc `AdminReports` trong `AdminRoute`. `AdminRoute.jsx` kiểm tra `isAuthenticated` và `user.role === 'ADMIN'` (redux state); nếu không thoả thì `Navigate` sang `/login` hoặc `/dashboard`, ngược lại render `AdminReports`.
2. **Mount trang, gọi API lần đầu** — `AdminReports.jsx` dòng 52: `useEffect(() => { fetchPage(page, actionFilter); }, ...)` chạy khi mount với `page=1`, `actionFilter=''`.
3. **Gọi service** — hàm `fetchPage` (dòng 40-50) gọi `getAuditLog({ page: p - 1, size: PAGE_SIZE, action: action || undefined })` — chuyển từ trang 1-based (UI) sang 0-based (BE), `PAGE_SIZE = 10`.
4. **HTTP request** — `getAuditLog` trong `adminService.js` (dòng 74-80) gọi `api.get('/admin/audit-logs', { params })`; `authService.js` đính kèm `Authorization: Bearer <accessToken>` từ `localStorage` qua request interceptor.
5. **Backend nhận request** — `AdminAuditLogController.list()` (dòng 29-43): Spring Security xác thực JWT trước (theo `SecurityConfig` — mọi `/api/admin/**` cần `hasRole("ADMIN")`), rồi `@PreAuthorize("hasRole('ADMIN')")` kiểm tra lần 2 ở tầng controller; `@Validated` + `@Min`/`@Max` kiểm tra `page >= 0`, `1 <= size <= 100`.
6. **Business logic** — `AdminAuditLogService.getAuditLogs()` (dòng 20-26): chuẩn hoá chuỗi rỗng thành `null` cho `action`/`targetTable`, gọi repository.
7. **Truy vấn DB** — `AdminAuditLogRepository.findByFilters()` (dòng 17-25): JPQL `WHERE (:action IS NULL OR LOWER(action)=LOWER(:action)) AND (...) ORDER BY createdAt DESC`, phân trang bằng `Pageable`.
8. **Map Entity → DTO** — `AdminAuditLogService.toResponse()` (dòng 28-38) gọi 3 hàm private `actorEmail`/`actorName`/`actorRole` để chọn đúng 1 trong 3 FK actor (`adminActor`/`staffActor`/`studentActor`) — đây là rẽ nhánh chính của luồng dữ liệu, xem chi tiết mục 5.
9. **Trả response** — Controller bọc `Map.of("content", ..., "totalElements", ..., "totalPages", ...)` trong `ApiResponse.success(...)`, trả HTTP 200.
10. **Nhận ở FE** — `getAuditLog` trả `res.data.data` (bóc envelope `ApiResponse`) → `AdminReports.jsx` set `logs` và `totalPages`.
11. **Render bảng** — mỗi dòng log hiển thị qua `RoleChip`/`ActionChip` (tra `auditMeta.js`), cột "Chi tiết" ưu tiên `log.targetEmail` rồi tới `log.description` (xem lưu ý mục 5 — `targetEmail` hiện luôn `null` từ BE).
12. **Người dùng đổi filter** — `handleAction(val)` (dòng 54-57) set `actionFilter` + reset `page = 1`, `useEffect` trigger lại bước 2-11 với `action` mới.
13. **Người dùng đổi trang** — `Pagination` gọi `onChange(page)` → `setPage` → `useEffect` trigger lại bước 2-11 với `page` mới.

### Sequence Diagram

```mermaid
sequenceDiagram
    participant U as "Admin (Browser)"
    participant P as "AdminReports.jsx"
    participant S as "adminService.js"
    participant AX as "authService.js (axios)"
    participant C as "AdminAuditLogController.java"
    participant SV as "AdminAuditLogService.java"
    participant R as "AdminAuditLogRepository.java"
    participant DB as "admin_audit_logs (MySQL)"

    U->>P: "mở /admin/reports"
    P->>P: "useEffect fetchPage(1, '')"
    P->>S: "getAuditLog({page:0, size:10})"
    S->>AX: "api.get('/admin/audit-logs', {params})"
    AX->>C: "GET /api/admin/audit-logs?page=0&size=10 (Bearer JWT)"
    C->>C: "@PreAuthorize hasRole ADMIN + validate page/size"
    C->>SV: "getAuditLogs(null, null, 0, 10)"
    SV->>R: "findByFilters(null, null, Pageable)"
    R->>DB: "SELECT ... ORDER BY created_at DESC"
    DB-->>R: "Page<AdminAuditLog>"
    R-->>SV: "Page<AdminAuditLog>"
    SV->>SV: "map -> AuditLogItemResponse (chọn actor email/tên/role)"
    SV-->>C: "Page<AuditLogItemResponse>"
    C-->>AX: "200 ApiResponse{data:{content, totalElements, totalPages}}"
    AX-->>S: "res.data.data"
    S-->>P: "{content, totalPages}"
    P->>P: "setLogs / setTotalPages"
    P-->>U: "render bảng + chip màu (auditMeta.js)"

    U->>P: "chọn filter action = 'suspend_user'"
    P->>P: "handleAction() -> setActionFilter + setPage(1)"
    P->>S: "getAuditLog({page:0, size:10, action:'suspend_user'})"
    Note over S,DB: "lặp lại luồng GET như trên với filter action"
```

---

## 5. Vai trò từng đoạn code quan trọng

### 5.1 Frontend — gọi API + chuyển đổi phân trang 1-based ↔ 0-based

[AdminReports.jsx](../../../apps/frontend/src/pages/admin/AdminReports.jsx), dòng 40-50:

```jsx
const fetchPage = useCallback((p, action) => {
    setLoading(true);
    setHasError(false);
    // p là số trang 1-based (UI hiển thị "trang 1"), nhưng BE dùng page 0-based
    // => phải trừ 1 khi gửi lên; action rỗng thì gửi undefined để axios bỏ qua param
    getAuditLog({ page: p - 1, size: PAGE_SIZE, action: action || undefined })
      .then((data) => {
        setLogs(data?.content ?? []);       // optional chaining: nếu data null thì fallback mảng rỗng
        setTotalPages(data?.totalPages ?? 1); // tránh Pagination render lỗi khi totalPages undefined
      })
      .catch(() => setHasError(true))        // KHÔNG log chi tiết lỗi ra UI, chỉ set cờ hasError
      .finally(() => setLoading(false));
  }, []);
```

Đây là điểm quyết định luồng dữ liệu chính ở FE: nhận input từ user (số trang, filter), gọi tầng service, và là nơi duy nhất xử lý 3 trạng thái loading/error/success cho toàn bộ bảng.

### 5.2 Backend — endpoint + 2 lớp bảo vệ quyền

[AdminAuditLogController.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogController.java), dòng 20-43:

```java
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // Lớp bảo vệ #1: chỉ role ADMIN được gọi bất kỳ method nào trong class này
@Validated                          // Bật validate cho @RequestParam bên dưới (page/size)
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(required = false) String action,       // optional: lọc theo loại hành động
            @RequestParam(required = false) String targetTable,   // optional: lọc theo bảng bị tác động
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100") // chặn client yêu cầu size quá lớn (DoS nhẹ)
                    int size) {
        Page<AuditLogItemResponse> result = adminAuditLogService.getAuditLogs(action, targetTable, page, size);
        // Trả cấu trúc phẳng {content, totalElements, totalPages} thay vì trả nguyên Page<> của Spring
        // (Page<> serialize ra JSON có nhiều field thừa như "pageable", "sort" — FE không cần)
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages())));
    }
}
```

Lớp bảo vệ #2 nằm ở [SecurityConfig.java](../../../apps/backend/src/main/java/com/jlpt/shared/config/SecurityConfig.java) dòng 56-57 (`requestMatchers("/api/admin/**").hasRole("ADMIN")`) — độc lập với `@PreAuthorize`, áp dụng ở tầng filter chain trước khi request chạm tới Controller.

### 5.3 Backend — rẽ nhánh chọn actor (Admin/Staff/Student) từ 3 FK tùy chọn

[AdminAuditLogService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java), dòng 40-62:

```java
private String actorEmail(AdminAuditLog l) {
    // Entity có 3 FK riêng biệt (adminActor/staffActor/studentActor), chỉ đúng 1 cái != null
    // tùy ai là người thực hiện hành động khi producer ghi log (xem mục 6).
    if (l.getAdminActor() != null) return l.getAdminActor().getEmail();
    if (l.getStaffActor() != null) return l.getStaffActor().getEmail();
    if (l.getStudentActor() != null) return l.getStudentActor().getEmail();
    return null; // hành động do hệ thống tự sinh (không gắn actor cụ thể)
}

/** Phân biệt MANAGER vs STAFF qua staffRole (JWT chỉ có ROLE_STAFF). */
private String actorRole(AdminAuditLog l) {
    if (l.getAdminActor() != null) return "ADMIN";
    if (l.getStaffActor() != null) {
        // StaffUser dùng chung 1 role JWT (ROLE_STAFF) cho cả Staff thường và Staff Manager;
        // phải phân biệt lại bằng field nghiệp vụ staffRole để hiển thị đúng chip "Manager" hay "Staff"
        return l.getStaffActor().getStaffRole() == StaffUser.StaffRole.STAFF_MANAGER ? "MANAGER" : "STAFF";
    }
    if (l.getStudentActor() != null) return "STUDENT";
    return "SYSTEM"; // không xác định được actor -> gán role giả "SYSTEM" cho FE hiển thị nhãn "Hệ thống"
}
```

Đoạn này quan trọng vì đây là nơi duy nhất "giải mã" dữ liệu thô (3 FK rời rạc trong DB) thành 1 field `actorRole` duy nhất mà FE hiển thị — nếu producer nào đó quên set đúng actor, log sẽ hiện "Hệ thống" một cách âm thầm (không có cảnh báo/log lỗi ở đây).

### 5.4 Lưu ý phát hiện được (không phải bug được yêu cầu sửa, chỉ ghi nhận để người đọc hiểu đúng hành vi thật)

[AuditLogItemResponse.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/dto/AuditLogItemResponse.java) dòng 13 khai báo field `targetEmail`, và FE tại [AdminReports.jsx](../../../apps/frontend/src/pages/admin/AdminReports.jsx) dòng 130 ưu tiên hiển thị `log.targetEmail || log.description`. Tuy nhiên trong `AdminAuditLogService.toResponse()` (dòng 29-37), **không có dòng nào set `.targetEmail(...)`** khi build `AuditLogItemResponse` — nghĩa là field này luôn `null` khi trả về, và cột "Chi tiết" trên UI trong thực tế luôn rơi vào nhánh `log.description` (hoặc dấu `—` nếu cả hai đều rỗng). Đây là quan sát trực tiếp từ code, không phải suy đoán.

### 5.5 Producer mẫu — ghi audit log khi Admin đăng nhập thành công

[AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java), dòng 120-130:

```java
private void audit(AdminUser admin, String action, String targetTable, Long targetId, String ip, String desc) {
    AdminAuditLog log = AdminAuditLog.builder()
            .adminActor(admin)      // set đúng 1 FK (adminActor) vì actor luôn là Admin trong service này
            .action(action)         // ví dụ "ADMIN_LOGIN_SUCCESS" (chữ HOA — khác style "suspend_user" của AdminUserService)
            .targetTable(targetTable)
            .targetId(targetId)
            .ipAddress(ip)          // ghi IP để phục vụ điều tra bảo mật (đăng nhập đáng ngờ)
            .description(desc)
            .build();
    adminAuditLogRepository.save(log); // ghi trực tiếp vào bảng dùng chung — feature admin-reports chỉ đọc lại sau này
}
```

Việc `action` không thống nhất chữ hoa/thường giữa các producer (`ADMIN_LOGIN_SUCCESS` vs `suspend_user`) được `auditMeta.js` xử lý bằng cách luôn `toLowerCase()` khi tra cứu nhãn (dòng 70/75 file đó) — đây là lý do thiết kế `ACTION_LABELS`/`ACTION_COLORS` dùng key viết thường.

---

## 6. Dữ liệu di chuyển như thế nào

Theo dõi 1 bản ghi audit log cụ thể — ví dụ hành động "Admin đình chỉ tài khoản học viên" — xuyên suốt hệ thống:

1. **Sinh ra (producer, ngoài phạm vi feature này)** — khi Admin gọi API suspend user, [AdminUserService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java) dòng 279 gọi `auditLog(actor, "suspend_user", "student_users", userId, request.getReason())`. Hàm `auditLog` (dòng 629-637) build `AdminAuditLog` với `adminActor = actor`, `action = "suspend_user"`, `targetTable = "student_users"`, `targetId = userId`, `description = lý do đình chỉ (input do Admin nhập ở form)`.
2. **Lưu DB** — `auditLogRepository.save(...)` → Hibernate INSERT vào bảng `admin_audit_logs`, tự sinh `audit_id` (IDENTITY), `created_at = LocalDateTime.now()` (giá trị default của field, xem `AdminAuditLog.java` dòng 51-53).
3. **Đọc lại (thuộc feature admin-reports)** — khi Admin mở `/admin/reports`, `AdminAuditLogRepository.findByFilters()` SELECT toàn bộ cột entity, trong đó FK `student_actor_id` được join LAZY sang `StudentUser` khi `AdminAuditLogService` gọi `l.getStudentActor()`.
4. **Biến đổi tên field entity → DTO** — `AdminAuditLogService.toResponse()`: `l.getId()` → `logId`, `l.getAction()` → `actionType` (tên field đổi từ `action` sang `actionType`), `l.getDescription()` → `description` (giữ nguyên tên = "reason" gốc do Admin nhập, không đổi nội dung), actor's email/fullName → `adminEmail`/`actorName`, suy ra thêm `actorRole = "ADMIN"` (field hoàn toàn mới, không có trong entity, được tính toán chứ không lưu trong DB).
5. **Trả JSON qua HTTP** — Controller bọc trong `ApiResponse.success(Map.of("content", [...DTO], ...))`; tên field JSON giữ nguyên camelCase của DTO Java (`actionType`, `actorName`, `actorRole`, `createdAt`...).
6. **FE nhận và hiển thị** — `AdminReports.jsx`: `log.actionType` → tra `auditMeta.getActionLabel()` để đổi `"suspend_user"` thành nhãn tiếng Việt "Đình chỉ tài khoản" + màu cam (`getActionColors`); `log.actorRole` ("ADMIN") → tra `getRoleMeta()` ra nhãn "Admin" + màu tím; `log.createdAt` (chuỗi ISO LocalDateTime) → `new Date(...).toLocaleString('vi-VN', {...})` để hiển thị giờ Việt Nam; `log.description` (chính là "reason" Admin nhập ban đầu) hiển thị nguyên văn ở cột "Chi tiết".
7. **Không có bước ghi ngược** — trang `/admin/reports` là read-only hoàn toàn; không có action nào trên trang này ghi lại vào `admin_audit_logs` (xem trang không hề gọi API POST/PUT nào).

Tóm lại: field `reason` (input tự do do Admin gõ ở 1 form khác) đi xuyên suốt hệ thống mà **không đổi tên** (`reason` → `description` trong entity/DTO → hiển thị nguyên văn ở FE) — chỉ đổi *vị trí* (từ request body của 1 API khác, thành 1 cột trong bảng chung, thành 1 dòng hiển thị ở trang khác).

---

## 7. Bảng tra cứu tổng hợp

| Bước | File | Function | Kết nối tới | Dữ liệu | Ghi chú |
|---|---|---|---|---|---|
| 1 | [App.jsx](../../../apps/frontend/src/App.jsx) | route `/admin/reports` | `AdminRoute.jsx` | — | Guard client-side theo `user.role` |
| 2 | [AdminReports.jsx](../../../apps/frontend/src/pages/admin/AdminReports.jsx) | `fetchPage()` | `adminService.js` | `{page, size, action}` | `useEffect` trigger khi `page`/`actionFilter` đổi |
| 3 | [adminService.js](../../../apps/frontend/src/api/adminService.js) | `getAuditLog()` | `authService.js` (axios) | query params | Bóc `res.data.data` (envelope `ApiResponse`) |
| 4 | [AdminAuditLogController.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogController.java) | `list()` | `AdminAuditLogService` | `action, targetTable, page, size` | `@PreAuthorize hasRole('ADMIN')` + validate |
| 5 | [AdminAuditLogService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java) | `getAuditLogs()` | `AdminAuditLogRepository` | chuẩn hoá filter rỗng→null | `@Transactional(readOnly = true)` |
| 6 | [AdminAuditLogService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java) | `toResponse()` | — | `AdminAuditLog` → `AuditLogItemResponse` | Chọn actor từ 1 trong 3 FK |
| 7 | [AdminAuditLogRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java) | `findByFilters()` | Bảng `admin_audit_logs` | JPQL + Pageable | Sort `createdAt DESC` |
| 8 | (10 file producer, xem mục 2.3) | `save(AdminAuditLog.builder()...)` | Bảng `admin_audit_logs` | tuỳ feature | **Không thuộc code feature admin-reports** |
| 9 | [auditMeta.js](../../../apps/frontend/src/utils/auditMeta.js) | `getActionLabel/getActionColors/getRoleMeta` | — | mã → nhãn/màu | Key luôn `toLowerCase()` |
| 10 | [AdminReports.jsx](../../../apps/frontend/src/pages/admin/AdminReports.jsx) | render bảng | `Pagination.jsx`, `EmptyState.jsx` | `logs[]`, `totalPages` | Hiển thị cuối cùng cho Admin |

---

## 8. Các mục cần bổ sung context

- **Nội dung file `EmptyState.jsx`** ([apps/frontend/src/components/common/EmptyState.jsx](../../../apps/frontend/src/components/common/EmptyState.jsx)) chưa được đọc trực tiếp trong phân tích này — chỉ xác nhận được cách nó được gọi (props `title`, `subtitle`, `mascotVariant`, `mascotSize`) từ `AdminReports.jsx`. Không tìm thấy trong source code đã đọc.
- **`update_setting` / `setting_updated`** được định nghĩa sẵn trong [auditMeta.js](../../../apps/frontend/src/utils/auditMeta.js) (`ACTION_LABELS`, `ACTION_COLORS`, `ACTION_GROUPS`) nhưng đã grep toàn bộ `apps/backend/src/main/java` cho các chuỗi này và cho `AdminAuditLog` trong [AdminSettingsService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java) — **không tìm thấy nơi nào thực sự ghi log này**. Không rõ đây là tính năng chưa hoàn thiện (backlog còn dở) hay đã bị gỡ bỏ producer nhưng quên xoá nhãn ở FE. Cần hỏi lại team để xác nhận.
- **`notification_rule_deleted`** cũng có nhãn sẵn trong `auditMeta.js` nhưng grep [NotificationRuleService.java](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java) chỉ tìm thấy `.action("NOTIFICATION_RULE_CREATED")` và `.action("NOTIFICATION_RULE_UPDATED")` (dòng 69, 101), không thấy action DELETE nào được ghi. Không tìm thấy trong source code.
- **`quiz_submitted` / `exam_submitted`** nằm trong `ACTION_GROUPS` dưới nhóm "Học viên" trong `auditMeta.js`, nhưng producer thật (`QuizService.java` dòng 344-346, `MockExamService.java` dòng 271-278) ghi action bằng chữ HOA (`"QUIZ_SUBMITTED"`, `"EXAM_SUBMITTED"`) — khớp đúng nhờ cơ chế `toLowerCase()` khi tra cứu nhãn, không phải bug, nhưng ghi nhận vì có khác biệt style so với các action khác (`quiz_submitted` chữ thường ở entry map `ACTION_LABELS` nhưng giá trị thật trong DB là chữ hoa).
- **`update_user` và `restore_user`** (ghi bởi `AdminUserService.java` dòng 236/248/543/560) **không có trong `ACTION_LABELS`/`ACTION_GROUPS`** của `auditMeta.js` — nghĩa là khi các hành động này xuất hiện trong bảng, `getActionLabel()` sẽ fallback trả nguyên chuỗi thô (`"update_user"`) thay vì nhãn tiếng Việt, và người dùng **không thể lọc riêng 2 loại action này** qua dropdown (vì không có trong `ACTION_GROUPS`). Đây là quan sát từ code, chưa xác nhận có phải là thiếu sót cần fix hay là chủ đích (có thể do các action này ít quan trọng hơn để hiển thị nhãn đẹp).
- **Nội dung 2 tài liệu phân tích chéo** `admin-panel-login-feature-analysis.md` và `admin-user-management-feature-analysis.md` (đường dẫn được nêu ở mục 3.3) **chưa tồn tại tại thời điểm viết tài liệu này** (đã kiểm tra bằng liệt kê thư mục `docs/02-SDD-Architecture/feat_flow/`) — chúng được cho biết là đang được viết song song bởi task khác; nội dung liên kết là dự đoán theo quy ước đặt tên, không phải xác nhận từ file thật.
- **`targetTable` filter** (`GET /api/admin/audit-logs?targetTable=...`) được BE hỗ trợ đầy đủ (`AdminAuditLogController`, `AdminAuditLogService`, `AdminAuditLogRepository` đều xử lý param này) nhưng **`AdminReports.jsx` không có UI nào để chọn `targetTable`** — chỉ `adminService.js` khai báo tham số này trong hàm `getAuditLog`, không được trang gọi tới. Không rõ đây là tính năng đã bỏ dở ở FE hay chủ đích chỉ dùng nội bộ (VD: từ trang khác gọi kèm `targetTable` để xem log 1 đối tượng cụ thể) — không tìm thấy nơi nào trong FE truyền `targetTable` khi gọi `getAuditLog`.

<!-- BACKEND-METHOD-INVENTORY:START -->

## Phụ lục — Danh mục đầy đủ hàm backend

> Phần này được đối chiếu trực tiếp từ source backend hiện tại. Chỉ liệt kê các hàm khai báo tường minh trong những file Java mà tài liệu này tham chiếu; các hàm do Lombok/JPA sinh tự động không xuất hiện trong source nên không liệt kê.

### `AdminAuditLogRepository`

Nguồn: [AdminAuditLogRepository.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`Optional<AdminAuditLog> findFirstByTargetIdAndTargetTableAndActionInOrderByCreatedAtDesc(Long targetId, String targetTable, List<String> actions)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogRepository.java#L28) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `find first by target id and target table and action in order by created at desc`. |

### `AdminAuditLogService`

Nguồn: [AdminAuditLogService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`Page<AuditLogItemResponse> getAuditLogs(String action, String targetTable, int page, int size)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java#L19) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get audit logs`. |
| 2 | [`AuditLogItemResponse toResponse(AdminAuditLog l)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java#L28) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to response`. |
| 3 | [`String actorEmail(AdminAuditLog l)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java#L40) | `—` | Thực hiện xử lý backend `actor email` trong `AdminAuditLogService`. |
| 4 | [`String actorName(AdminAuditLog l)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java#L47) | `—` | Thực hiện xử lý backend `actor name` trong `AdminAuditLogService`. |
| 5 | [`String actorRole(AdminAuditLog l)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuditLogService.java#L55) | `—` | Thực hiện xử lý backend `actor role` trong `AdminAuditLogService`. |

### `AdminAuthService`

Nguồn: [AdminAuthService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`LoginApiResponse processAdminLogin(AdminUser admin, String rawPassword, String ip)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java#L34) | `—` | Thực hiện xử lý backend `process admin login` trong `AdminAuthService`. |
| 2 | [`String generateToken()`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java#L114) | `—` | Thực hiện xử lý backend `generate token` trong `AdminAuthService`. |
| 3 | [`void audit(AdminUser admin, String action, String targetTable, Long targetId, String ip, String desc)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminAuthService.java#L120) | `—` | Thực hiện xử lý backend `audit` trong `AdminAuthService`. |

### `AdminSettingsService`

Nguồn: [AdminSettingsService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`List<SettingResponse> getByGroup(String group)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L31) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get by group`. |
| 2 | [`SettingResponse updateSetting(String group, String key, String value)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L50) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `update setting`. |
| 3 | [`List<SettingResponse> updateSettings(String group, List<UpdateSettingsBatchRequest.Item> items)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L57) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `update settings`. |
| 4 | [`SettingResponse upsert(String group, String key, String value)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L77) | `—` | Thực hiện xử lý backend `upsert` trong `AdminSettingsService`. |
| 5 | [`void testSmtpConnection(SmtpTestRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L111) | `—` | Thực hiện xử lý backend `test smtp connection` trong `AdminSettingsService`. |
| 6 | [`void applySmtpSettingsToMailSender()`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L202) | `—` | Thực hiện xử lý backend `apply smtp settings to mail sender` trong `AdminSettingsService`. |
| 7 | [`void validateGroup(String group)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L273) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `validate group`. |
| 8 | [`boolean isPassword(String key)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L279) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `is password`. |

### `AdminUserService`

Nguồn: [AdminUserService.java](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`Page<UserSummaryResponse> listUsers(String type, String q, String status, String jlptLevel, String staffRole, int page, int size)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L62) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `list users`. |
| 2 | [`Object getUserDetail(String type, Long userId)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L93) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get user detail`. |
| 3 | [`yield toStudentDetail(s)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L100) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to student detail`. |
| 4 | [`yield toStaffDetail(st)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L106) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to staff detail`. |
| 5 | [`yield toAdminDetail(a)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L112) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to admin detail`. |
| 6 | [`CreateStaffResponse createStaff(String adminEmail, CreateStaffRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L120) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `create staff`. |
| 7 | [`void setupStaffPassword(com.jlpt.feature.staff.dto.request.StaffSetupPasswordRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L166) | `—` | Thực hiện xử lý backend `setup staff password` trong `AdminUserService`. |
| 8 | [`Object updateUser(String adminEmail, String type, Long userId, Object request)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L208) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `update user`. |
| 9 | [`yield toStudentDetail(s)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L237) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to student detail`. |
| 10 | [`yield toStaffDetail(st)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L249) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to staff detail`. |
| 11 | [`SuspendUserResponse suspendUser(String adminEmail, String type, Long userId, SuspendUserRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L259) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `suspend user`. |
| 12 | [`ActivateUserResponse activateUser(String adminEmail, String type, Long userId)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L336) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `activate user`. |
| 13 | [`void resetPassword(String adminEmail, String type, Long userId)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L403) | `—` | Thực hiện xử lý backend `reset password` trong `AdminUserService`. |
| 14 | [`SoftDeleteUserResponse softDeleteUser(String adminEmail, String type, Long userId)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L476) | `—` | Thực hiện xử lý backend `soft delete user` trong `AdminUserService`. |
| 15 | [`RestoreUserResponse restoreUser(String adminEmail, String type, Long userId)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L527) | `—` | Thực hiện xử lý backend `restore user` trong `AdminUserService`. |
| 16 | [`ChangeStaffRoleResponse changeStaffRole(String adminEmail, Long staffId, ChangeStaffRoleRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L575) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `change staff role`. |
| 17 | [`String normalizeType(String type)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L611) | `—` | Thực hiện xử lý backend `normalize type` trong `AdminUserService`. |
| 18 | [`AdminUser resolveAdmin(String email)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L616) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `resolve admin`. |
| 19 | [`void checkSelfModification(Long actorAdminId, String type, Long targetId)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L623) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `check self modification`. |
| 20 | [`void auditLog(AdminUser actor, String action, String targetTable, Long targetId, String description)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L629) | `—` | Thực hiện xử lý backend `audit log` trong `AdminUserService`. |
| 21 | [`String generateUrlSafeToken(int bytes)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L639) | `—` | Thực hiện xử lý backend `generate url safe token` trong `AdminUserService`. |
| 22 | [`UserSummaryResponse toStudentSummary(StudentUser s)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L647) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to student summary`. |
| 23 | [`UserSummaryResponse toStaffSummary(StaffUser st)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L663) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to staff summary`. |
| 24 | [`UserSummaryResponse toAdminSummary(AdminUser a)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L675) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to admin summary`. |
| 25 | [`StudentDetailResponse toStudentDetail(StudentUser s)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L686) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to student detail`. |
| 26 | [`StaffDetailResponse toStaffDetail(StaffUser st)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L708) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to staff detail`. |
| 27 | [`AdminDetailResponse toAdminDetail(AdminUser a)`](../../../apps/backend/src/main/java/com/jlpt/feature/admin/AdminUserService.java#L721) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to admin detail`. |

### `MockExamService`

Nguồn: [MockExamService.java](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`String canonicalSection(String raw)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L56) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `canonical section`. |
| 2 | [`ExamStartResponse startExam(Long assessmentId, StudentUser student)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L77) | `—` | Thực hiện xử lý backend `start exam` trong `MockExamService`. |
| 3 | [`ExamSubmitResponse submitExam(Long assessmentId, Long studentId, SubmitExamRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L117) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `submit exam`. |
| 4 | [`Page<ExamHistoryResponse> getExamHistory(Long studentId, Pageable pageable)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L154) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get exam history`. |
| 5 | [`ExamReviewResponse getExamReview(Long attemptId, Long studentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L173) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get exam review`. |
| 6 | [`ExamSubmitResponse gradeAndPersist(TestAttempt attempt, Assessment assessment, List<QuestionAssignment> assignments, List<AnswerRequest> answers, boolean isAutoSubmit, LocalDateTime now)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L193) | `—` | Thực hiện xử lý backend `grade and persist` trong `MockExamService`. |
| 7 | [`TestAttempt findOwnedAttempt(Long attemptId, Long studentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L305) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `find owned attempt`. |
| 8 | [`LocalDateTime computeExpiresAt(Assessment assessment, LocalDateTime startedAt)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L315) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `compute expires at`. |
| 9 | [`SectionScoresResponse toSectionScores(TestAttempt attempt)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L319) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to section scores`. |
| 10 | [`ExamHistoryResponse toHistoryResponse(TestAttempt attempt, Assessment assessment)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L327) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to history response`. |
| 11 | [`ExamReviewItem toReviewItem(AttemptAnswer answer)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L345) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to review item`. |

### `QuizService`

Nguồn: [QuizService.java](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`QuizResponse createQuiz(QuizRequest request, StaffUser staffUser)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L52) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `create quiz`. |
| 2 | [`QuizResponse updateAssessment(Long assessmentId, QuizRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L74) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `update assessment`. |
| 3 | [`void softDeleteAssessment(Long assessmentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L93) | `—` | Thực hiện xử lý backend `soft delete assessment` trong `QuizService`. |
| 4 | [`void addQuestions(Long quizId, List<QuestionRequest> questions, StaffUser staffUser)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L110) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `add questions`. |
| 5 | [`Page<AssessmentSummaryResponse> listAssessmentsForStaff(Assessment.AssessmentType type, Kanji.ContentStatus status, StudentUser.JlptLevel jlptLevel, Pageable pageable)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L171) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `list assessments for staff`. |
| 6 | [`List<QuestionResponse> getQuestionsOfAssessment(Long assessmentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L182) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get questions of assessment`. |
| 7 | [`ExamStartResponse startQuiz(Long quizId, StudentUser student)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L199) | `—` | Thực hiện xử lý backend `start quiz` trong `QuizService`. |
| 8 | [`ScoreResponse submitQuiz(Long quizId, Long studentId, Long attemptId, List<AnswerRequest> answers)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L233) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `submit quiz`. |
| 9 | [`AssessmentSummaryResponse toSummaryResponse(Assessment assessment)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L257) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to summary response`. |
| 10 | [`ScoreResponse calculateScore(TestAttempt attempt, Assessment assessment, List<AnswerRequest> answers)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L279) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `calculate score`. |
| 11 | [`QuizResponse mapToQuizResponse(Assessment a)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/QuizService.java#L368) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `map to quiz response`. |

### `NotificationService`

Nguồn: [NotificationService.java](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`void notifyStudent(StudentUser student, String title, String content, Notification.NotificationType type, String ruleKey, StaffUser staffCreator)`](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java#L44) | `—` | Gửi hoặc phân phối thông tin cho nghiệp vụ `notify student`. |
| 2 | [`Page<NotificationResponse> getMyNotifications(Long studentId, int page, int size)`](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java#L66) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get my notifications`. |
| 3 | [`long getUnreadCount(Long studentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java#L73) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get unread count`. |
| 4 | [`void markNotificationRead(Long notificationId, Long studentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java#L78) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `mark notification read`. |
| 5 | [`int markAllNotificationsRead(Long studentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java#L91) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `mark all notifications read`. |
| 6 | [`String broadcast(String actorEmail, SendNotificationRequest req)`](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java#L98) | `—` | Gửi hoặc phân phối thông tin cho nghiệp vụ `broadcast`. |
| 7 | [`List<StudentUser> resolveTargets(String targetJlptLevel)`](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java#L118) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `resolve targets`. |
| 8 | [`NotificationResponse toNotificationResponse(Notification n)`](../../../apps/backend/src/main/java/com/jlpt/feature/notification/service/NotificationService.java#L131) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to notification response`. |

### `StaffPasswordResetService`

Nguồn: [StaffPasswordResetService.java](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`void requestReset(StaffForgotPasswordRequest request, String ip)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L50) | `—` | Thực hiện xử lý backend `request reset` trong `StaffPasswordResetService`. |
| 2 | [`List<StaffResetRequestResponse> listRequests(String status)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L56) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `list requests`. |
| 3 | [`IssueTempPasswordResponse issueTempPassword(String adminEmail, Long staffId, IssueTempPasswordRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L64) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `issue temp password`. |
| 4 | [`void changeTempPassword(String limitedToken, ChangeTempPasswordRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L107) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `change temp password`. |
| 5 | [`void createRequestForActiveStaff(StaffUser staff, String ip)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L139) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `create request for active staff`. |
| 6 | [`void validateIssueRequest(StaffPasswordResetRequest resetRequest, Long staffId)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L161) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `validate issue request`. |
| 7 | [`void validateLimitedToken(AuthToken token)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L176) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `validate limited token`. |
| 8 | [`void validateStrongPassword(String password)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L182) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `validate strong password`. |
| 9 | [`String generateTempPassword()`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L190) | `—` | Thực hiện xử lý backend `generate temp password` trong `StaffPasswordResetService`. |
| 10 | [`char randomChar(String source)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L206) | `—` | Thực hiện xử lý backend `random char` trong `StaffPasswordResetService`. |
| 11 | [`StaffPasswordResetRequest.ResetStatus parseStatus(String status)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L210) | `—` | Thực hiện xử lý backend `parse status` trong `StaffPasswordResetService`. |
| 12 | [`StaffResetRequestResponse toResponse(StaffPasswordResetRequest resetRequest)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L221) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to response`. |
| 13 | [`void audit(AdminUser admin, String action, String targetTable, Long targetId, String description)`](../../../apps/backend/src/main/java/com/jlpt/feature/staff/StaffPasswordResetService.java#L235) | `—` | Thực hiện xử lý backend `audit` trong `StaffPasswordResetService`. |

### `SupportTicketService`

Nguồn: [SupportTicketService.java](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`TicketResponse createTicket(Long studentId, TicketRequest req)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L63) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `create ticket`. |
| 2 | [`Page<TicketResponse> getMyTickets(Long studentId, String status, int page, int size)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L85) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get my tickets`. |
| 3 | [`TicketDetailResponse getStudentTicketDetail(Long ticketId, Long studentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L104) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get student ticket detail`. |
| 4 | [`TicketDetailResponse getStaffTicketDetail(Long ticketId)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L116) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get staff ticket detail`. |
| 5 | [`TicketReplyResponse addStudentReply(Long ticketId, Long studentId, TicketReplyRequest req)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L125) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `add student reply`. |
| 6 | [`TicketReplyResponse addStaffReply(Long ticketId, String staffEmail, TicketReplyRequest req)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L147) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `add staff reply`. |
| 7 | [`TicketResponse closeTicket(Long ticketId, String actorEmail)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L184) | `—` | Thực hiện xử lý backend `close ticket` trong `SupportTicketService`. |
| 8 | [`TicketResponse closeStudentTicket(Long ticketId, Long studentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L215) | `—` | Thực hiện xử lý backend `close student ticket` trong `SupportTicketService`. |
| 9 | [`TicketResponse assignTicket(Long ticketId, Long assignToStaffId, String actorEmail, boolean isAdmin)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L231) | `—` | Thực hiện xử lý backend `assign ticket` trong `SupportTicketService`. |
| 10 | [`Page<TicketResponse> getAllTickets(String status, String category, String priority, String q, int page, int size)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L268) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get all tickets`. |
| 11 | [`Page<com.jlpt.feature.support.dto.SubmissionResponse> getAllSubmissions(String submissionType, String status, int page, int size)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L278) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get all submissions`. |
| 12 | [`com.jlpt.feature.support.dto.SubmissionResponse getSubmissionDetail(Long submissionId)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L295) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get submission detail`. |
| 13 | [`com.jlpt.feature.support.dto.SubmissionResponse toSubmissionResponse(StudentSubmission s)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L306) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to submission response`. |
| 14 | [`GradeResponse manualGrade(Long submissionId, String actorEmail, ManualGradeRequest req)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L335) | `—` | Thực hiện xử lý backend `manual grade` trong `SupportTicketService`. |
| 15 | [`Ticket findTicketOrThrow(Long id)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L398) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `find ticket or throw`. |
| 16 | [`StudentUser findStudentOrThrow(Long id)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L402) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `find student or throw`. |
| 17 | [`StaffUser findStaffOrThrow(String email)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L408) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `find staff or throw`. |
| 18 | [`void checkTicketNotClosed(Ticket ticket)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L414) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `check ticket not closed`. |
| 19 | [`TicketResponse toTicketResponse(Ticket t)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L420) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to ticket response`. |
| 20 | [`TicketDetailResponse toTicketDetailResponse(Ticket t, List<TicketReply> replies)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L442) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to ticket detail response`. |
| 21 | [`TicketReplyResponse toReplyResponse(TicketReply r, String senderName, String role)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L475) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to reply response`. |
| 22 | [`Ticket.TicketStatus parseStatus(String status)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L486) | `—` | Thực hiện xử lý backend `parse status` trong `SupportTicketService`. |
| 23 | [`Ticket.Priority parsePriority(String priority)`](../../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java#L495) | `—` | Thực hiện xử lý backend `parse priority` trong `SupportTicketService`. |

### `ApiResponse`

Nguồn: [ApiResponse.java](../../../apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`static <T> ApiResponse<T> success(T data)`](../../../apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java#L22) | `—` | Thực hiện xử lý backend `success` trong `ApiResponse`. |
| 2 | [`static <T> ApiResponse<T> success(String message, T data)`](../../../apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java#L30) | `—` | Thực hiện xử lý backend `success` trong `ApiResponse`. |
| 3 | [`static <T> ApiResponse<T> created(T data)`](../../../apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java#L34) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `created`. |
| 4 | [`static <T> ApiResponse<T> created(String message, T data)`](../../../apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java#L42) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `created`. |
| 5 | [`static <T> ApiResponse<T> error(int status, String message)`](../../../apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java#L46) | `—` | Thực hiện xử lý backend `error` trong `ApiResponse`. |
| 6 | [`static <T> ApiResponse<T> error(int status, String message, T data)`](../../../apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java#L50) | `—` | Thực hiện xử lý backend `error` trong `ApiResponse`. |
| 7 | [`static <T> ApiResponse<T> errorWithCode(int status, String message, String code)`](../../../apps/backend/src/main/java/com/jlpt/shared/common/ApiResponse.java#L63) | `—` | Thực hiện xử lý backend `error with code` trong `ApiResponse`. |

### `SecurityConfig`

Nguồn: [SecurityConfig.java](../../../apps/backend/src/main/java/com/jlpt/shared/config/SecurityConfig.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`SecurityFilterChain securityFilterChain(HttpSecurity http)`](../../../apps/backend/src/main/java/com/jlpt/shared/config/SecurityConfig.java#L48) | `—` | Thực hiện xử lý backend `security filter chain` trong `SecurityConfig`. |
| 2 | [`PasswordEncoder passwordEncoder()`](../../../apps/backend/src/main/java/com/jlpt/shared/config/SecurityConfig.java#L86) | `—` | Thực hiện xử lý backend `password encoder` trong `SecurityConfig`. |
| 3 | [`CorsConfigurationSource corsConfigurationSource()`](../../../apps/backend/src/main/java/com/jlpt/shared/config/SecurityConfig.java#L91) | `—` | Thực hiện xử lý backend `cors configuration source` trong `SecurityConfig`. |

### `NotificationRuleService`

Nguồn: [NotificationRuleService.java](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`List<NotificationRuleResponse> listRules()`](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java#L39) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `list rules`. |
| 2 | [`NotificationRuleResponse createRule(NotificationRuleRequest req, Long adminId)`](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java#L49) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `create rule`. |
| 3 | [`NotificationRuleResponse updateRule(String ruleKey, NotificationRuleRequest req, Long adminId)`](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java#L80) | `—` | Cập nhật trạng thái/dữ liệu cho nghiệp vụ `update rule`. |
| 4 | [`SystemSetting findRuleOrThrow(String ruleKey)`](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java#L112) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `find rule or throw`. |
| 5 | [`AdminUser findAdminOrThrow(Long adminId)`](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java#L118) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `find admin or throw`. |
| 6 | [`String buildJson(NotificationRuleRequest req)`](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java#L124) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `build json`. |
| 7 | [`NotificationRuleResponse parseRule(SystemSetting setting)`](../../../apps/backend/src/main/java/com/jlpt/shared/notification/service/NotificationRuleService.java#L139) | `—` | Thực hiện xử lý backend `parse rule` trong `NotificationRuleService`. |

**Tổng cộng:** `127` hàm backend trong `16` file Java được tham chiếu.

<!-- BACKEND-METHOD-INVENTORY:END -->
