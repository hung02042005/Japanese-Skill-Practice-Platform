# SPEC — Admin Dashboard (Tổng quan)
>
> **Feature ID:** `feat-admin` | **Page:** `AdminDashboard`
> **Route:** `/admin` (redirect về đây sau login thành công)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-02
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-system-admin/SPEC.md § UC-36`

---

## 1. TỔNG QUAN TRANG

Dashboard là trang đầu tiên admin thấy sau khi đăng nhập. Mục tiêu: **nhìn một lần biết ngay trạng thái hệ thống** — số người dùng, hoạt động hôm nay, log hành động gần nhất, và lối tắt đến các phần quản trị.

**Không cần charts phức tạp** — stat cards + activity log là đủ cho giai đoạn này.

**Cấu trúc trang:**

```
[1] AdminTopNav      — activeTab="admin-overview"
[2] AdminPageHeader  — "Bảng Điều Khiển" + SakuChan happy
[3] Body
    [3a] Stat row    — 4 thẻ số liệu tổng hợp
    [3b] Content row — [Activity log | Quick actions]
```

**File:**

```
apps/frontend/src/pages/admin/
├── AdminDashboard.jsx
└── AdminDashboard.css
```

---

## 2. LAYOUT TỔNG THỂ

```
background: var(--color-bg)   min-height: 100vh

┌────────────────────────────────────────────────┐
│  AdminTopNav (sticky 64px)                     │
├────────────────────────────────────────────────┤
│  AdminPageHeader                               │
│  ┌ chip: 👑 Tổng quan ──────────────────────┐ │
│  │ title: "Bảng Điều Khiển"                 │ │
│  │ sub:   "Theo dõi hoạt động hệ thống"     │ │
│  │                        [SakuChan happy]   │ │
│  └──────────────────────────────────────────┘ │
├────────────────────────────────────────────────┤
│  .adb-body  (max-width 1400px, padding 28/32) │
│                                               │
│  [Stat row — 4 cards]                         │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐         │
│  │Total │ │Today │ │Quizzes│ │System│         │
│  └──────┘ └──────┘ └──────┘ └──────┘         │
│                                               │
│  [Content row — 2 col: 60% / 40%]            │
│  ┌────────────────────┐ ┌──────────────┐     │
│  │  Activity Log      │ │ Quick Actions│     │
│  │  (10 items latest) │ │ (5 links)    │     │
│  └────────────────────┘ └──────────────┘     │
└────────────────────────────────────────────────┘
```

---

## 3. HEADER — AdminPageHeader

Dùng component `AdminPageHeader` có sẵn, không tạo mới.

```jsx
<AdminPageHeader
  chipIcon={<IcAdminChip />}
  chipLabel="Tổng quan"
  title="Bảng Điều Khiển"
  subtitle="Theo dõi hoạt động hệ thống theo thời gian thực"
  mascotVariant="happy"
  mascotSize={100}
/>
```

SakuChan hiển thị variant `thinking` khi đang tải (`isLoading: true`), trở về `happy` sau khi load xong.

---

## 4. STAT ROW — `.adb-stats`

```
display: grid
grid-template-columns: repeat(4, 1fr)
gap: 16px
margin-bottom: 28px
```

Mỗi card là `.adb-stat-card`:

```
background: var(--color-card)
border-radius: var(--radius-lg)
shadow: var(--shadow-card)
padding: 20px
display: flex, align-items: flex-start, justify-content: space-between
transition: box-shadow 200ms

hover:
  box-shadow: 0 4px 16px rgba(0,0,0,0.12)
```

### 4.1 Bốn stat cards

| # | Tiêu đề | Dữ liệu | Icon | Border màu |
|:---|:---|:---|:---|:---|
| 1 | Tổng người dùng | Tổng `student + staff + admin` | `StatIconUsers` | `--color-primary` |
| 2 | Hoạt động hôm nay | Distinct users đăng nhập 24h qua | `StatIconActive` | `--color-secondary` |
| 3 | Bài thi / Quiz | Quiz attempts hôm nay | `IcChart` (mới) | `--color-accent` |
| 4 | Trạng thái | `OK` / `BẢO TRÌ` / `LỖI` | `IcSystemHealth` (mới) | tuỳ trạng thái |

**Card layout bên trong:**

```
.adb-stat-left:
  .adb-stat-label — "Tổng người dùng", font: 12px/600, color: --color-text-sub, uppercase
  .adb-stat-value — số lớn, font: 30px/800, color: --color-text, margin-top: 4px
  .adb-stat-delta — "+12 so với hôm qua", font: 12px/400, color: --color-secondary (tăng) / --color-error (giảm)

.adb-stat-icon:
  width: 44px, height: 44px
  border-radius: var(--radius-md)
  background: tuỳ card (xem bảng trên tại opacity 0.12)
  display: flex, align-items: center, justify-content: center
  color: tuỳ card
```

**Card 4 — Trạng thái hệ thống:**

```
value text:
  "Bình thường" — color: --color-secondary
  "Bảo trì"     — color: --color-warning
  "Sự cố"       — color: --color-error

icon-bg: tương ứng màu trạng thái tại opacity 0.12
```

**Loading state:** Thay card bằng skeleton pulse (giống pattern SkeletonRow):

```css
.adb-stat-skel {
  height: 96px;
  border-radius: var(--radius-lg);
  background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%);
  background-size: 200% 100%;
  animation: skelPulse 1.4s ease infinite;
}
```

**Responsive:**

```
≥ 768px:  4 cột
< 768px:  2 cột
< 480px:  1 cột
```

---

## 5. ACTIVITY LOG — `.adb-activity`

Card trắng bên trái của content row (chiếm 60% width trên desktop).

```
background: var(--color-card)
border-radius: var(--radius-lg)
shadow: var(--shadow-card)
padding: 24px
```

### 5.1 Header card

```
display: flex, justify-content: space-between, align-items: center
margin-bottom: 16px

[Title]: "Hoạt Động Gần Đây"
  font: 16px/700, color: --color-text

[Link "Xem tất cả"]:
  font: 13px/600, color: --color-primary
  hover: text-decoration underline
  href: /admin/reports (Phase 2 — hiện tại disabled + tooltip "Sắp có")
```

### 5.2 Danh sách log — `.adb-log-list`

Hiển thị tối đa 10 bản ghi mới nhất từ `GET /api/admin/audit-log?page=0&size=10`.

Mỗi item `.adb-log-item`:

```
display: flex, align-items: flex-start, gap: 12px
padding: 10px 0
border-bottom: 1px solid var(--color-border)

:last-child { border-bottom: none }

[Icon bubble — .adb-log-icon]:
  width: 32px, height: 32px, flex-shrink: 0
  border-radius: var(--radius-full)
  display: flex, align-items: center, justify-content: center
  background: tuỳ action_type (xem bảng dưới)

[Content — .adb-log-content]:
  flex: 1

  [.adb-log-action]: font 13px/600, color: --color-text
    Text mẫu: "Đình chỉ tài khoản student@example.com"

  [.adb-log-meta]: font 12px/400, color: --color-text-sub, margin-top: 2px
    Text mẫu: "admin@jlpt.com · 5 phút trước"
    Dùng relative time (dayjs hoặc date-fns)
```

**Icon bubble theo action_type:**

| action_type | Icon | Background |
|:---|:---|:---|
| `create_staff` | `IcAddStaff` | `var(--color-secondary-bg)`, color green |
| `suspend_user` | `IcBan` | `#FFF3E0`, color `--color-warning` |
| `activate_user` | `IcCheck` | `var(--color-secondary-bg)`, color green |
| `soft_delete_user` | `IcTrash` | `#FFEAEA`, color `--color-error` |
| `reset_password_initiated` | `IcKey` | `var(--color-primary-bg)`, color primary |
| `change_staff_role` | `IcSwap` | `#F3E5F5`, color `#6A1B9A` |
| `update_setting` | `IcEdit` | `#E3F2FD`, color `#1565C0` |
| _(khác)_ | `IcAdminChip` | `var(--color-primary-bg)`, color primary |

**Loading state:** 5 skeleton rows (giống SkeletonRow nhưng dạng list, không table).

**Empty state:**

```
SakuChan variant="thinking" size={80}
Text: "Chưa có hoạt động nào được ghi nhận"
font: 14px/400, color: --color-text-sub, text-align: center
```

---

## 6. QUICK ACTIONS — `.adb-quick`

Card trắng bên phải (chiếm 40% width trên desktop).

```
background: var(--color-card)
border-radius: var(--radius-lg)
shadow: var(--shadow-card)
padding: 24px
```

### 6.1 Header card

```
[Title]: "Truy cập nhanh"
  font: 16px/700, color: --color-text
  margin-bottom: 16px
```

### 6.2 Danh sách link — `.adb-quick-list`

5 link cards dẫn đến các trang admin khác:

```
display: flex, flex-direction: column, gap: 8px
```

Mỗi `.adb-quick-item` (là `<Link>`):

```
display: flex, align-items: center, gap: 14px
padding: 12px 14px
border-radius: var(--radius-md)
border: 1px solid var(--color-border)
text-decoration: none
transition: background 150ms, border-color 150ms, box-shadow 150ms

hover:
  background: var(--color-primary-bg)
  border-color: var(--color-primary-light)
  box-shadow: 0 2px 8px rgba(232,154,170,0.12)

[Icon bubble — .adb-qi-icon]:
  width: 36px, height: 36px, flex-shrink: 0
  border-radius: var(--radius-sm)
  display: flex, align-items: center, justify-content: center

[Text block]:
  [.adb-qi-label]: font 14px/600, color: --color-text
  [.adb-qi-desc]:  font 12px/400, color: --color-text-sub, margin-top: 1px

[Chevron — →]:
  margin-left: auto
  color: --color-text-sub
  font-size: 16px
```

**5 quick action items:**

| Icon | Background | Label | Mô tả | Route |
|:---|:---|:---|:---|:---|
| `TAB_ICONS.student` (dùng người dùng tab) | `var(--color-primary-bg)` | Quản lý người dùng | Xem và quản lý tài khoản | `/admin/users` |
| `IcMail` | `#E3F2FD` | Cài đặt SMTP | Cấu hình email hệ thống | `/admin/settings?tab=email` |
| `IcShield` | `#F3E5F5` | Bảo mật | Giới hạn đăng nhập, JWT | `/admin/settings?tab=security` |
| `IcBell` | `var(--color-accent-bg)` | Thông báo tự động | Quản lý quy tắc thông báo | `/admin/settings?tab=notifications` |
| `IcWrench` | `#FFF3E0` | Chế độ bảo trì | Bật/tắt bảo trì hệ thống | `/admin/settings?tab=system` |

---

## 7. CONTENT ROW RESPONSIVE

```
.adb-content-row:
  display: grid
  grid-template-columns: 3fr 2fr
  gap: 20px
  align-items: start

@media (max-width: 1024px):
  grid-template-columns: 1fr
  (activity log trên, quick actions dưới)
```

---

## 8. API

### `GET /api/admin/dashboard/summary`

**Auth:** Bearer JWT

**Response (200):**

```json
{
  "status": 200,
  "data": {
    "totalUsers":      1420,
    "activeToday":       87,
    "quizAttemptsToday": 243,
    "systemStatus":    "OK"
  }
}
```

`systemStatus`: `"OK"` | `"MAINTENANCE"` | `"ERROR"`

---

### `GET /api/admin/audit-log?page=0&size=10`

**Auth:** Bearer JWT

**Response (200):**

```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "logId":       54,
        "adminEmail":  "admin@jlpt.com",
        "actionType":  "suspend_user",
        "targetType":  "student",
        "targetId":    12,
        "targetEmail": "student@example.com",
        "description": "Vi phạm điều khoản sử dụng",
        "createdAt":   "2026-06-02T08:34:00Z"
      }
    ],
    "totalElements": 340,
    "totalPages":    34
  }
}
```

---

## 9. COMPONENT STRUCTURE

```jsx
// pages/admin/AdminDashboard.jsx
import AdminTopNav        from '../../components/layout/AdminTopNav';
import { AdminPageHeader} from '../../components/admin/AdminPageHeader';
import {
  IcAdminChip, IcAddStaff, IcBan, IcCheck, IcKey,
  IcSwap, IcTrash, IcEdit, STAT_ICONS, TAB_ICONS,
  // icons mới (thêm vào ManageUsersIcons.jsx):
  IcChart, IcSystemHealth, IcMail, IcShield, IcWrench, IcBell,
} from '../../components/admin/ManageUsersIcons';
import './AdminDashboard.css';

function AdminDashboard() {
  // state: summary{totalUsers, activeToday, quizAttemptsToday, systemStatus}
  // state: activityLog[], isLoadingSummary, isLoadingLog, error

  return (
    <div className="adb-page">
      <AdminTopNav activeTab="admin-overview" />
      <AdminPageHeader
        chipIcon={<IcAdminChip />} chipLabel="Tổng quan"
        title="Bảng Điều Khiển"
        subtitle="Theo dõi hoạt động hệ thống theo thời gian thực"
        mascotVariant={isLoadingSummary ? 'thinking' : 'happy'}
      />
      <main className="adb-body">
        {/* Stat Row */}
        <div className="adb-stats">{/* 4 stat cards */}</div>

        {/* Content Row */}
        <div className="adb-content-row">
          <div className="adb-activity">{/* activity log */}</div>
          <div className="adb-quick">{/* quick actions */}</div>
        </div>
      </main>
    </div>
  );
}
```

---

## 10. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 1200px | Stat: 4 cột. Content: 3fr/2fr |
| 768–1199px | Stat: 2 cột. Content: 1 cột |
| < 768px | Stat: 2 cột. Content: 1 cột. Padding giảm |
| < 480px | Stat: 1 cột |

---

## 11. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Heading | `<h1>` nằm trong AdminPageHeader (title prop) |
| Stat values | `aria-label="Tổng người dùng: 1420"` trên mỗi card |
| Activity log | `<ul role="list">` với `<li>` cho mỗi log item |
| Quick actions | `<nav aria-label="Truy cập nhanh">` |
| Loading | `aria-busy="true"` trên container đang tải |
| System status | `role="status"` trên badge trạng thái |

---

## 12. OUT OF SCOPE

- ❌ Biểu đồ đường/cột (line/bar chart) — Phase 2
- ❌ Số liệu theo tuần/tháng — chỉ hôm nay
- ❌ Real-time WebSocket updates — polling 60s là đủ
- ❌ Export báo cáo — trang Reports Phase 2
