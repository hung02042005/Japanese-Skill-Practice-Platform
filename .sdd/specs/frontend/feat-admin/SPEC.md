# SPEC — Admin Panel (Frontend Overview)
> **Feature ID:** `feat-admin`
> **UC Coverage:** UC-35 (Login), UC-36 (Dashboard), UC-37 (User Management), UC-39 (Settings), UC-40 (Notification Rules)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-02
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `.sdd/specs/backend/feat-system-admin/SPEC.md`

---

## 1. MỤC TIÊU

Admin Panel là bảng điều khiển trung tâm dành riêng cho role `ADMIN`. Giao diện theo chủ đề **SakuJi Hanami** nhất quán với toàn bộ hệ thống, nhưng có điểm nhận diện riêng:
- **Badge ADMIN** màu deep petal (`#E8637A`) cạnh logo
- **Crown icon** trong dropdown người dùng
- Không có sidebar trái/phải — layout full-width với max-width 1400px

---

## 2. ROUTE MAP

| Route | Page | File | Status |
|:---|:---|:---|:---|
| `/admin` | Dashboard tổng quan | `pages/admin/AdminDashboard.jsx` | ❌ Chưa có |
| `/admin/users` | Quản lý người dùng | `pages/admin/ManageUsers.jsx` | ✅ Có sẵn |
| `/admin/content` | Quản lý nội dung | `pages/admin/AdminContent.jsx` | ❌ Chưa có |
| `/admin/reports` | Báo cáo | `pages/admin/AdminReports.jsx` | ❌ Chưa có |
| `/admin/settings` | Cài đặt & Thông báo | `pages/admin/AdminSettings.jsx` | ❌ Chưa có |

**Guard:** Mọi route admin đều bọc trong `<AdminRoute>` — kiểm tra JWT role `ADMIN`.

**Login flow:** Admin dùng chung `/login` với các role khác. Sau khi backend trả token, redirect về `/admin`.

---

## 3. DESIGN TOKENS

Dùng toàn bộ token từ `DESIGN.md`. Không override, không thêm giá trị hex cứng.

```css
/* Màu chính */
--color-primary:      #E89AAA;   /* Sakura pink */
--color-primary-light:#F7CBD4;
--color-primary-bg:   #FFF0F3;
--color-primary-dark: #D84F68;
--color-secondary:    #5DBB69;   /* Green CTA */
--color-accent:       #F7C948;   /* Gold */
--color-bg:           #FAF7F4;   /* Washi canvas */
--color-card:         #FFFFFF;
--color-text:         #2D2D2D;
--color-text-sub:     #6B625E;
--color-border:       #E8E0DC;
--color-error:        #E57373;

/* Admin-specific accent */
--color-admin-crown:  #E8637A;   /* Deep petal — badge ADMIN, crown badge */

/* Radius */
--radius-sm:   8px;
--radius-md:   12px;
--radius-lg:   16px;
--radius-xl:   24px;
--radius-full: 9999px;

/* Shadow */
--shadow-card:  0 2px 8px rgba(0,0,0,0.07);
--shadow-raised:0 4px 12px rgba(0,0,0,0.10);
--shadow-float: 0 8px 24px rgba(0,0,0,0.12);
```

---

## 4. COMPONENT REUSE MAP

Tận dụng tối đa các component hiện có. **Không tạo mới nếu đã có**.

### 4.1 Layout

| Component | File | Dùng ở đâu |
|:---|:---|:---|
| `AdminTopNav` | `components/layout/AdminTopNav.jsx` | Mọi trang admin — truyền `activeTab` prop |
| `AdminPageHeader` | `components/admin/AdminPageHeader.jsx` | Header mỗi trang — truyền `chipIcon`, `title`, `subtitle`, `mascotVariant` |

**Props `AdminTopNav`:**
```jsx
<AdminTopNav activeTab="admin-overview" />   // Dashboard
<AdminTopNav activeTab="manage-users" />     // Users
<AdminTopNav activeTab="manage-content" />   // Content
<AdminTopNav activeTab="reports" />          // Reports
<AdminTopNav activeTab="settings" />         // Settings
```

**Props `AdminPageHeader`:**
```jsx
<AdminPageHeader
  chipIcon={<IcAdminChip />}      // icon từ ManageUsersIcons
  chipLabel="Tổng quan"
  title="Bảng Điều Khiển"
  subtitle="Theo dõi hoạt động hệ thống theo thời gian thực"
  mascotVariant="happy"            // idle | happy | thinking
  mascotSize={100}
/>
```

### 4.2 Data & UI

| Component | File | Dùng ở đâu |
|:---|:---|:---|
| `Badges` (StatusBadge, JlptBadge) | `components/common/Badges.jsx` | Trạng thái user, cấp độ JLPT |
| `UserAvatar` | `components/common/UserAvatar.jsx` | Ảnh đại diện trong bảng |
| `Pagination` | `components/common/Pagination.jsx` | Phân trang bảng user |
| `SkeletonRow` | `components/admin/SkeletonRow.jsx` | Loading state bảng |
| `UserModals` | `components/admin/UserModals.jsx` | Modal xác nhận thao tác |
| `STAT_ICONS` | `components/admin/ManageUsersIcons.jsx` | Icon stat card |
| `TAB_ICONS` | `components/admin/ManageUsersIcons.jsx` | Icon tab student/staff/admin |
| `IcBan, IcCheck, IcKey, IcSwap, IcTrash` | `components/admin/ManageUsersIcons.jsx` | Action buttons bảng |
| `IcAdminChip` | `components/admin/ManageUsersIcons.jsx` | Chip header |
| `IcAddStaff` | `components/admin/ManageUsersIcons.jsx` | Nút tạo Staff |
| `IcSearchGlass` | `components/admin/ManageUsersIcons.jsx` | Thanh tìm kiếm |
| `IcBloomCheck` | `components/admin/ManageUsersIcons.jsx` | Icon xác nhận modal |
| `AppLogo` | `components/common/AppLogo.jsx` | Đã dùng trong AdminTopNav |
| `SakuChan` | `components/auth/SakuChan.jsx` | Đã dùng trong AdminPageHeader |

### 4.3 Icon mới cần thêm vào ManageUsersIcons

Các icon chưa có, cần export thêm vào file `ManageUsersIcons.jsx`:

```
IcChart         — icon biểu đồ/analytics (dashboard stat)
IcShield        — icon bảo mật (settings tab)
IcMail          — icon email/SMTP (settings tab)
IcWrench        — icon bảo trì (maintenance toggle)
IcBell          — icon thông báo (notification rules tab)
IcPlus          — icon thêm mới (create notification rule)
IcEdit          — icon chỉnh sửa (edit setting value)
IcSystemHealth  — icon trạng thái hệ thống (dashboard)
```

Xem SVG paths chi tiết: `SPEC-admin-icon-system.md`

---

## 5. LAYOUT PATTERN

Mọi trang admin theo cùng một cấu trúc:

```
min-height: 100vh
background: var(--color-bg)   ← washi canvas
display: flex, flex-direction: column

┌──────────────────────────────────────────────────────┐
│  AdminTopNav (64px, sticky, z-index: 100)            │
├──────────────────────────────────────────────────────┤
│  AdminPageHeader (gradient, petals, SakuChan)        │
├──────────────────────────────────────────────────────┤
│                                                      │
│  .admin-body                                         │
│    max-width: 1400px                                 │
│    margin: 0 auto                                    │
│    padding: 28px 32px 48px                           │
│    flex: 1                                           │
│                                                      │
│    [Page-specific content]                           │
│                                                      │
└──────────────────────────────────────────────────────┘
```

**Responsive:**
| Breakpoint | Padding |
|:---|:---|
| ≥ 1200px | `28px 32px` |
| 768–1199px | `24px 20px` |
| < 768px | `16px 16px` |

---

## 6. FILE STRUCTURE

```
apps/frontend/src/
├── pages/admin/
│   ├── AdminDashboard.jsx        ← UC-36 (tạo mới)
│   ├── AdminDashboard.css
│   ├── ManageUsers.jsx           ← UC-37 (đã có)
│   ├── ManageUsers.css
│   ├── AdminSettings.jsx         ← UC-39 + UC-40 (tạo mới)
│   └── AdminSettings.css
├── components/admin/
│   ├── AdminPageHeader.jsx       ← đã có, dùng chung
│   ├── AdminPageHeader.css
│   ├── ManageUsersIcons.jsx      ← đã có, thêm icon mới
│   ├── SkeletonRow.jsx           ← đã có
│   └── UserModals.jsx            ← đã có
└── components/layout/
    ├── AdminTopNav.jsx            ← đã có
    └── AdminTopNav.css
```

---

## 7. TRẠNG THÁI LOADING / ERROR / EMPTY

**Mọi API-backed component phải xử lý 3 trạng thái:**

| Trạng thái | Cách xử lý |
|:---|:---|
| `isLoading: true` | Dùng `<SkeletonRow />` hoặc skeleton cards (pulse animation) |
| `error` | Banner đỏ với icon `✕`, message từ backend, nút "Thử lại" |
| Data rỗng | `<EmptyState />` với SakuChan variant `thinking`, message phù hợp |

**Không hiển thị trang trắng** — luôn có skeleton hoặc empty state.

---

## 8. OUT OF SCOPE

- ❌ Trang `/admin/content` và `/admin/reports` — Phase 2
- ❌ Dark mode
- ❌ Quản lý tài khoản Admin khác qua UI (chỉ xem)
- ❌ Export/Import CSV người dùng hàng loạt
