# SPEC — UI Cleanup: Bỏ UI Thừa Staff & Manager
>
> **Feature ID:** `feat-ui-cleanup-staff-manager`
> **Version:** 1.1 | **Status:** ✅ Implemented
> **Author:** Team | **Last Updated:** 2026-06-21

---

## 1. BỐI CẢNH & MỤC TIÊU

### 1.1 Bối cảnh

Qua rà soát frontend, phát hiện 4 nhóm vấn đề UI không nhất quán với phân quyền và usecase:

1. **Trang chết** (`/staff/review-queue`): Không có nav link, Staff Manager bị redirect đi mất, chỉ Staff thường vào được nhưng trang lại ghi "Chỉ dành cho Staff Manager".
2. **Staff thường có quyền Suspend học viên** (`/staff/students`): Suspension là quyền quản lý, không phải hỗ trợ.
3. **Staff gửi thông báo toàn hệ thống** (`/staff/notifications`): Broadcast tới toàn bộ học viên nên là quyền Manager/Admin.
4. **Duyệt nội dung bị nhân đôi**: Cả `ManagerReviewQueue` và `AdminContent` đều có nút Approve/Reject, không rõ ai phụ trách giai đoạn nào.

### 1.2 Mục tiêu

- Xóa / tắt các UI thừa khỏi frontend
- Đảm bảo mỗi quyền chỉ xuất hiện đúng một nơi và đúng role
- Không thay đổi backend API, không ảnh hưởng luồng học viên

### 1.3 Out of scope

- ❌ Thay đổi backend permissions / JWT claims
- ❌ Tạo thêm trang mới
- ❌ Refactor navigation toàn bộ
- ❌ Xử lý Course creation quyền hạn (để sau, cần thêm thảo luận)

---

## 2. ACTOR & ROLE HIERARCHY

| Role | `role` claim | `staffRole` claim | Vào được |
|:---|:---|:---|:---|
| Student | `STUDENT` | — | `/dashboard/*` |
| Staff | `STAFF` | ≠ `staff_manager` | `/staff/*` |
| Staff Manager | `STAFF` | `staff_manager` | `/manager/*` |
| Admin | `ADMIN` | — | `/admin/*` + `/manager/*` |

**Quy tắc hiện tại của `StaffRoute`:** Nếu `staffRole === 'staff_manager'` → redirect về `/manager`. Tức là Staff Manager **không bao giờ vào được** `/staff/*`.

---

## 3. CÁC THAY ĐỔI CẦN LÀM

---

### 3.1 Xóa trang `/staff/review-queue`

**Lý do:** Trang chết — không có nav link trong `StaffTopNav`, Staff Manager bị redirect trước khi vào được, chỉ Staff thường vào được bằng URL thẳng trong khi trang ghi "Chỉ dành cho Staff Manager". Chức năng duyệt đã có đầy đủ tại `/manager/review-queue`.

**Việc cần làm:**

| File | Hành động |
|:---|:---|
| [App.jsx](../../apps/frontend/src/App.jsx) line 113 | Xóa route `<Route path="/staff/review-queue" .../>` |
| [apps/frontend/src/pages/staff/StaffReviewQueue.jsx](../../apps/frontend/src/pages/staff/StaffReviewQueue.jsx) | Xóa file |
| [apps/frontend/src/pages/staff/StaffReviewQueue.css](../../apps/frontend/src/pages/staff/StaffReviewQueue.css) | Xóa file |
| [App.jsx](../../apps/frontend/src/App.jsx) line 55 | Xóa import `StaffReviewQueue` |

**Verify:** URL `/staff/review-queue` phải trả về trang 404.

---

### 3.2 Xóa nút Suspend khỏi `StaffStudents`

**Lý do:** Theo LESSON-001 (`CLAUDE.md`), màn hình Staff và Admin phải tách biệt. Khóa tài khoản học viên là quyền quản lý (Manager/Admin), không phải hỗ trợ (Staff). Staff chỉ cần xem thông tin để hỗ trợ học viên qua ticket.

**Việc cần làm:**

| File | Hành động |
|:---|:---|
| [StaffStudents.jsx](../../apps/frontend/src/pages/staff/StaffStudents.jsx) | Xóa nút "Tạm khóa / Kích hoạt lại" khỏi student detail panel |
| [StaffStudents.jsx](../../apps/frontend/src/pages/staff/StaffStudents.jsx) | Xóa modal `SuspendConfirmModal` và handler `handleSuspend` |
| [StaffStudents.jsx](../../apps/frontend/src/pages/staff/StaffStudents.jsx) | Nếu import `SuspendConfirmModal` chỉ dùng ở đây, xóa import |

**Giữ lại:** Bảng danh sách học viên, panel xem thông tin, bảng lịch sử làm bài.

**Verify:** Màn hình `/staff/students` chỉ còn chức năng xem. Không có action nào thay đổi trạng thái học viên.

---

### 3.3 Chuyển `StaffNotifications` sang Manager

**Lý do:** Gửi thông báo broadcast tới toàn bộ hoặc theo cấp độ JLPT là hành động ảnh hưởng toàn hệ thống — cần Manager/Admin phê duyệt. Staff thường không có ngữ cảnh để quyết định ai cần nhận thông báo gì.

**Việc cần làm:**

| File | Hành động |
|:---|:---|
| [App.jsx](../../apps/frontend/src/App.jsx) line 112 | Xóa route `/staff/notifications` khỏi `StaffRoute` |
| [App.jsx](../../apps/frontend/src/App.jsx) | Thêm route `/manager/notifications` dưới `ManagerRoute` trỏ tới cùng component |
| [StaffTopNav.jsx](../../apps/frontend/src/components/layout/StaffTopNav.jsx) | Xóa tab `staff-notifications` khỏi `NAV_TABS` |
| [ManagerTopNav.jsx](../../apps/frontend/src/components/layout/ManagerTopNav.jsx) | Thêm tab "Thông báo" trỏ tới `/manager/notifications` |
| [StaffNotifications.jsx](../../apps/frontend/src/pages/staff/StaffNotifications.jsx) | Đổi import nav từ `StaffTopNav` sang `ManagerTopNav` |
| [App.jsx](../../apps/frontend/src/App.jsx) | Cập nhật import: thêm `ManagerNotifications` hoặc giữ `StaffNotifications` với tên mới |

> **Lưu ý tên file:** Có thể giữ nguyên file `StaffNotifications.jsx` và chỉ đổi nav bên trong. Hoặc đổi tên sang `ManagerNotifications.jsx` để nhất quán với convention. Ưu tiên đổi tên cho rõ ràng.

**Verify:** URL `/staff/notifications` trả về 404. URL `/manager/notifications` hiển thị đúng trang và có ManagerTopNav.

---

### 3.4 Tách rõ quyền Approve giữa Manager và Admin

**Lý do:** Hiện tại cả `ManagerReviewQueue` và `AdminContent` đều có nút Approve/Reject/Publish. Điều này gây nhầm lẫn và tạo hai luồng duyệt song song.

**Phân công rõ ràng:**

| Role | Quyền trên nội dung |
|:---|:---|
| Manager | Approve (publish) / Reject nội dung Staff submit |
| Admin | Archive / Delete nội dung đã published; **không** Approve thay Manager |

**Việc cần làm:**

| File | Hành động |
|:---|:---|
| [AdminContent.jsx](../../apps/frontend/src/pages/admin/AdminContent.jsx) | Xóa nút **Approve** khỏi hàng "Pending Review" |
| [AdminContent.jsx](../../apps/frontend/src/pages/admin/AdminContent.jsx) | Xóa nút **Reject** khỏi hàng "Pending Review" |
| [AdminContent.jsx](../../apps/frontend/src/pages/admin/AdminContent.jsx) | Giữ lại: nút **Archive** (published → archived) và **Delete** (draft/rejected/archived) |

**Verify:** Admin vào `/admin/content`, tab bất kỳ, hàng có status "Pending Review" không còn nút Approve/Reject. Chỉ còn text "Chờ Manager duyệt" hoặc empty action.

---

## 4. ACCEPTANCE CRITERIA

| ID | Scenario | Given | When | Then |
|:---|:---|:---|:---|:---|
| AC-01 | Review queue Staff bị xóa | Staff đã login | Truy cập `/staff/review-queue` | Hiển thị trang 404 |
| AC-02 | Staff Manager không thấy review queue ở Staff nav | Staff Manager login | Xem `/manager` nav | Không có link tới `/staff/review-queue` |
| AC-03 | Staff không thể suspend học viên | Staff login, vào `/staff/students` | Chọn một học viên bất kỳ | Không có nút "Tạm khóa" hoặc "Kích hoạt lại" |
| AC-04 | Notifications không có trong Staff nav | Staff login | Xem StaffTopNav | Không có tab "Thông báo" |
| AC-05 | Manager có trang Notifications | Staff Manager login | Truy cập `/manager/notifications` | Trang hiển thị đúng với ManagerTopNav |
| AC-06 | Admin không Approve nội dung | Admin login, vào `/admin/content` | Lọc status "Pending Review" | Không có nút Approve hoặc Reject; chỉ thấy label trạng thái |
| AC-07 | Manager vẫn Approve bình thường | Staff Manager login, vào `/manager/review-queue` | Có item Pending | Nút Approve và Reject hiển thị và hoạt động |

---

## 5. THỨ TỰ TRIỂN KHAI

```
Bước 1: Xóa /staff/review-queue (thấp risk, không ảnh hưởng feature)
Bước 2: Xóa nút Suspend khỏi StaffStudents
Bước 3: Move Notifications sang Manager
Bước 4: Xóa Approve/Reject khỏi AdminContent
```

Mỗi bước là một commit độc lập, dễ rollback nếu cần.
