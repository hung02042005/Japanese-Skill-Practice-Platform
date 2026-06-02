# SPEC — Admin Manage Users (Quản lý Người dùng)
> **Feature ID:** `feat-admin` | **Page:** `ManageUsers`
> **Route:** `/admin/users`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-02
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-system-admin/UC-37-manage-user.md`

---

## 1. TỔNG QUAN TRANG

Trang quản lý người dùng cho phép Admin xem, lọc và thực hiện toàn bộ thao tác trên 3 nhóm: **Student**, **Staff**, **Admin**. File `ManageUsers.jsx` đã tồn tại — spec này là tài liệu hóa chính thức để đồng bộ với UC-37 và chuẩn hóa thiết kế.

**Cấu trúc trang:**
```
[1] AdminTopNav       — activeTab="manage-users"
[2] AdminPageHeader   — "Người Dùng" + SakuChan
[3] Body
    [3a] Reset requests panel  — chỉ hiện khi tab Staff + có yêu cầu chờ
    [3b] Stat row (4 cards)    — tổng số theo loại tab hiện tại
    [3c] Type tab bar          — Student | Staff | Admin
    [3d] Filter bar            — search + dropdowns
    [3e] User table            — phân trang
```

**Files hiện có:**
```
apps/frontend/src/pages/admin/ManageUsers.jsx      ← trang chính
apps/frontend/src/pages/admin/ManageUsers.css
apps/frontend/src/components/admin/UserModals.jsx
apps/frontend/src/components/admin/SkeletonRow.jsx
apps/frontend/src/components/admin/ManageUsersIcons.jsx
```

---

## 2. DESIGN TOKENS ÁP DỤNG

Xem `SPEC.md § 3` — dùng toàn bộ token gốc, không override.

---

## 3. HEADER — AdminPageHeader

```jsx
<AdminPageHeader
  chipIcon={<IcAdminChip />}
  chipLabel="Quản lý người dùng"
  title="Người Dùng"
  subtitle="Quản lý tài khoản Student, Staff và Admin trong hệ thống"
  mascotVariant="happy"
  mascotSize={100}
/>
```

---

## 4. RESET REQUESTS PANEL — `.mu-reset-panel`

Chỉ hiển thị khi:
- Tab hiện tại = `staff`
- `resetRequests.length > 0` (có Staff yêu cầu đặt lại mật khẩu đang chờ)

```
background: var(--color-primary-bg)
border: 1px solid var(--color-primary-light)
border-radius: var(--radius-lg)
padding: 16px 20px
margin-bottom: 20px
display: flex, align-items: center, justify-content: space-between, gap: 16px, flex-wrap: wrap

[Left]:
  [Icon IcKey 16px, color: --color-primary]
  [Text]: "Có {n} yêu cầu đặt lại mật khẩu từ Staff đang chờ xử lý"
    font: 14px/600, color: --color-primary-dark

[Right — danh sách yêu cầu]:
  Mỗi item:
    [Email staff] + [Nút "Cấp mật khẩu tạm"] pill-shape, green
    Nút click → gọi POST /api/admin/staff/{id}/temp-password
                  → toast success "Đã cấp mật khẩu tạm thời và gửi email"
```

---

## 5. STAT ROW — `.mu-stats-row`

```
display: grid
grid-template-columns: repeat(4, 1fr)
gap: 16px
margin-bottom: 20px
```

Mỗi `.mu-stat-card` hiển thị số liệu của **tab đang active** (student / staff / admin):

| Index | Label | Dữ liệu | Icon (từ STAT_ICONS) |
|:---|:---|:---|:---|
| 0 | Tổng số | tổng tất cả trạng thái | `STAT_ICONS.total` |
| 1 | Đang hoạt động | status = active | `STAT_ICONS.active` |
| 2 | Bị đình chỉ | status = suspended | `STAT_ICONS.suspended` |
| 3 | Chờ kích hoạt | status = pending | `STAT_ICONS.pending` |

**Các stat này chỉ tính trên page hiện tại** (theo phân trang), không phải tổng toàn bộ.

**Loading:** skeleton card pulse thay thế từng card khi `isLoading`.

**Responsive:**
```
≥ 1200px: 4 cột
< 1200px: 2 cột
< 768px:  2 cột (thu nhỏ padding)
```

---

## 6. TYPE TAB BAR — `.mu-type-bar`

```
display: flex
justify-content: space-between
align-items: center
margin-bottom: 16px
```

**Tab group (trái):**
```
display: flex, gap: 6px
```

3 tabs dùng `TAB_ICONS` từ `ManageUsersIcons`:

```jsx
{[
  { key: 'student', label: 'Học viên',   icon: TAB_ICONS.student },
  { key: 'staff',   label: 'Nhân viên',  icon: TAB_ICONS.staff   },
  { key: 'admin',   label: 'Quản trị',   icon: TAB_ICONS.admin   },
].map(tab => (
  <button className={`mu-type-tab${activeType===tab.key?' mu-type-tab--on':''}`}>
    {tab.icon} {tab.label}
  </button>
))}
```

**Tab style:**
```
.mu-type-tab:
  height: 36px, padding: 0 14px
  border-radius: var(--radius-full)
  border: 1.5px solid var(--color-border)
  background: transparent
  font: 13px/600, color: --color-text-sub
  display: flex, align-items: center, gap: 6px
  cursor: pointer
  transition: all 150ms

hover:
  border-color: var(--color-primary-light)
  color: var(--color-primary)

.mu-type-tab--on:
  background: var(--color-primary)
  border-color: var(--color-primary)
  color: white
  box-shadow: 0 2px 8px rgba(232,154,170,0.30)
```

**Nút "Tạo Staff" (phải) — chỉ hiện khi tab = staff:**
```jsx
<button className="mu-btn-create-staff" onClick={() => openCreateStaffModal()}>
  <IcAddStaff /> Tạo Staff mới
</button>
```

```
.mu-btn-create-staff:
  height: 36px, padding: 0 16px
  border-radius: var(--radius-full)
  background: var(--color-secondary)
  color: white, border: none
  font: 13px/700
  display: flex, align-items: center, gap: 6px
  cursor: pointer
  shadow: 0 2px 8px rgba(93,187,105,0.25)
  transition: filter 150ms

hover: filter: brightness(1.07)
active: transform: scale(0.97)
```

---

## 7. FILTER BAR — `.mu-filter-bar`

```
display: flex, align-items: center, gap: 10px, flex-wrap: wrap
margin-bottom: 20px
padding: 12px 16px
background: var(--color-card)
border-radius: var(--radius-md)
shadow: var(--shadow-card)
```

### 7.1 Search box — `.mu-search-wrap`
```
position: relative, flex: 1, min-width: 200px

[Icon IcSearchGlass]:
  position: absolute, left: 12px, top: 50%, transform: translateY(-50%)
  color: --color-text-sub, pointer-events: none

[Input]:
  width: 100%, height: 38px
  padding: 0 36px 0 38px
  border: 1.5px solid var(--color-border)
  border-radius: var(--radius-full)
  background: var(--color-bg)
  font: 14px/400

  focus:
    border-color: var(--color-primary)
    box-shadow: 0 0 0 3px rgba(232,154,170,0.15)
    outline: none

[Clear button — ✕]:
  position: absolute, right: 10px, top: 50%, transform: translateY(-50%)
  width: 20px, height: 20px
  background: var(--color-border), border-radius: full
  border: none, color: --color-text-sub
  display: none khi input rỗng
```

Debounce 400ms trước khi gọi API.

### 7.2 Dropdowns

Mỗi dropdown là `<select>` styled:
```
height: 38px, padding: 0 10px
border: 1.5px solid var(--color-border)
border-radius: var(--radius-md)
background: var(--color-bg)
font: 13px/400, color: --color-text
cursor: pointer
appearance: none
background-image: URL (chevron SVG)
padding-right: 28px

focus:
  border-color: var(--color-primary)
  box-shadow: 0 0 0 3px rgba(232,154,170,0.15)
  outline: none
```

**Dropdown theo tab:**

| Tab | Dropdowns |
|:---|:---|
| student | Trạng thái (all / active / suspended / pending / deleted) + Cấp độ JLPT (all / N5–N1) |
| staff | Trạng thái + Vai trò (all / staff / staff_manager) |
| admin | Trạng thái |

### 7.3 Result count badge
```
.mu-count-badge:
  padding: 4px 10px
  border-radius: var(--radius-full)
  background: var(--color-primary-bg)
  color: var(--color-primary-dark)
  font: 12px/700
  white-space: nowrap

Text: "{totalElements} kết quả"
```

---

## 8. USER TABLE — `.mu-table-wrap`

```
background: var(--color-card)
border-radius: var(--radius-lg)
shadow: var(--shadow-card)
overflow: hidden
```

### 8.1 Table header — `<thead>`
```
background: var(--color-bg)
border-bottom: 1px solid var(--color-border)

th:
  padding: 10px 16px
  font: 11px/700, color: --color-text-sub, text-transform: uppercase, letter-spacing: 0.5px
  text-align: left
  white-space: nowrap
```

**Cột bảng theo tab:**

**Student:**
| # | Cột | Nội dung |
|:---|:---|:---|
| 1 | Người dùng | Avatar + Tên + Email (dùng `UserAvatar`) |
| 2 | Trạng thái | `StatusBadge` từ Badges.jsx |
| 3 | Cấp độ | `JlptBadge` hiện tại + mục tiêu |
| 4 | Streak | Số ngày + icon lửa nhỏ |
| 5 | Đăng nhập lần cuối | Relative time |
| 6 | Ngày tạo | DD/MM/YYYY |
| 7 | Thao tác | Action buttons (xem §8.3) |

**Staff:**
| # | Cột | Nội dung |
|:---|:---|:---|
| 1 | Người dùng | Avatar + Tên + Email |
| 2 | Trạng thái | `StatusBadge` |
| 3 | Vai trò | Chip `staff` / `staff_manager` |
| 4 | Đăng nhập lần cuối | Relative time |
| 5 | Ngày tạo | DD/MM/YYYY |
| 6 | Thao tác | Action buttons |

**Admin:**
| # | Cột | Nội dung |
|:---|:---|:---|
| 1 | Người dùng | Avatar + Tên + Email |
| 2 | Trạng thái | `StatusBadge` |
| 3 | 2FA | `Bật` (green chip) / `Tắt` (gray chip) |
| 4 | Đăng nhập lần cuối | Relative time |
| 5 | Ngày tạo | DD/MM/YYYY |
| — | Thao tác | **Không có** (Admin không chỉnh sửa Admin khác) |

### 8.2 Table rows — `.mu-tr`

```
border-bottom: 1px solid var(--color-border)
transition: background 120ms
animation: muRowIn 0.25s ease both
animation-delay: calc(var(--row-index) * 35ms)

hover: background: var(--color-bg)

.mu-tr--banned:  (status = suspended / deleted)
  opacity: 0.62
```

**Ô "Người dùng":**
```jsx
<td className="mu-td-user">
  <UserAvatar src={user.avatarUrl} name={user.fullName} size={34} />
  <div className="mu-user-info">
    <span className="mu-user-name">{user.fullName}</span>
    <span className="mu-user-email">{user.email}</span>
  </div>
</td>
```
```
.mu-td-user:
  display: flex, align-items: center, gap: 10px

.mu-user-name: font 14px/600, color: --color-text
.mu-user-email: font 12px/400, color: --color-text-sub
```

**Ô "Streak" (Student):**
```
[Flame SVG nhỏ 14px, color: --color-accent]
[Số ngày]: font 13px/700, color: --color-text
  → 0 ngày: opacity 0.4
```

**Ô "Vai trò" (Staff):**
```
staff:         chip background: var(--color-primary-bg), color: --color-primary-dark
staff_manager: chip background: #FFF3E0, color: #E65100
padding: 3px 10px, border-radius: full, font: 11px/700
```

**Ô "2FA" (Admin):**
```
Bật:  background: var(--color-secondary-bg), color: --color-secondary,  font: 11px/700, border-radius: full, padding: 3px 8px
Tắt:  background: var(--color-bg),           color: --color-text-disabled, border: 1px solid --color-border
```

### 8.3 Action buttons — `.mu-actions`

```
display: flex, gap: 4px, align-items: center

.mu-act-ic:
  width: 30px, height: 30px
  border-radius: var(--radius-sm)
  border: none, cursor: pointer
  display: flex, align-items: center, justify-content: center
  transition: opacity 150ms, transform 150ms, box-shadow 150ms

  opacity: 0             ← ẩn mặc định
  .mu-tr:hover &: opacity: 1   ← hiện khi hover row

  animation: muActIn 0.2s ease both
  (stagger delay per button)

hover: transform: scale(1.10), box-shadow: 0 2px 6px rgba(0,0,0,0.12)
active: transform: scale(0.94)

disabled: opacity: 0.3, cursor: not-allowed
```

**5 action buttons và khi nào hiển thị:**

| Button | Class | Icon | Background | Hiển thị khi |
|:---|:---|:---|:---|:---|
| Đình chỉ | `mu-act-ic--suspend` | `<IcBan/>` | `#FFF3E0` color `#F57C00` | status = active / pending |
| Kích hoạt | `mu-act-ic--activate` | `<IcCheck/>` | `var(--color-secondary-bg)` color green | status = suspended |
| Đặt lại mật khẩu | `mu-act-ic--reset` | `<IcKey/>` | `var(--color-bg)` border 1px | status ≠ deleted |
| Đổi vai trò | `mu-act-ic--role` | `<IcSwap/>` | `var(--color-primary-bg)` color primary | tab = staff, status ≠ deleted |
| Xóa mềm | `mu-act-ic--delete` | `<IcTrash/>` | `#FFEAEA` color `--color-error` | tab ≠ admin, status ≠ deleted |

**Tooltip:** Mỗi button có `title="Đình chỉ tài khoản"` (native tooltip).

**Loading state khi submit:** Icon trong button thay bằng spinner 14px, `pointer-events: none`.

---

## 9. PHÂN TRANG — Pagination

```jsx
<Pagination
  currentPage={currentPage}
  totalPages={totalPages}
  onPageChange={setCurrentPage}
/>
```

Dùng component `Pagination` có sẵn. Đặt dưới bảng, canh giữa.

---

## 10. LOADING STATE — SkeletonRow

Khi `isLoading: true`:
```jsx
{Array.from({ length: 8 }).map((_, i) => (
  <SkeletonRow key={i} cols={7} />  // hoặc cols=6 cho staff
))}
```

`SkeletonRow` tạo ra một `<tr>` với các `<td>` skeleton pulse.

---

## 11. MODALS — UserModals

Dùng `UserModals.jsx` có sẵn. Các modal:

### 11.1 Modal Xác nhận tổng quát (Confirm)
- Dùng cho: Kích hoạt, Đặt lại mật khẩu, Xóa mềm
- Props: `title`, `message`, `confirmLabel`, `confirmClass`, `onConfirm`, `onCancel`
- Icon: `IcBloomCheck` (modal xác nhận thường) / `IcTrash` màu đỏ (modal xóa)

### 11.2 Modal Đình chỉ (Suspend)
- Input textarea: lý do đình chỉ (10–500 ký tự, bắt buộc)
- Counter ký tự còn lại
- Validation: bật nút Confirm chỉ khi length ≥ 10

### 11.3 Modal Tạo Staff
```
Fields:
  - Họ tên  (input, bắt buộc, 2–150 ký tự)
  - Email   (input email, bắt buộc)
  - Vai trò (radio: Nhân viên / Quản lý nhân viên)

Nút submit: "Tạo tài khoản" — green pill
Loading: spinner trong nút, disabled form
Error: banner đỏ nếu email đã tồn tại
```

### 11.4 Modal Đổi vai trò Staff
```
Text: "Chuyển từ [vai trò hiện tại] sang [vai trò mới]"
Radio: Nhân viên / Quản lý nhân viên
Confirm: "Cập nhật vai trò"
```

**Modal style (chung):**
```
.mu-modal-backdrop:
  position: fixed, inset: 0
  background: rgba(0,0,0,0.40)
  z-index: 200
  display: flex, align-items: center, justify-content: center
  backdrop-filter: blur(2px)

.mu-modal:
  background: var(--color-card)
  border-radius: var(--radius-xl)
  padding: 28px
  width: 100%, max-width: 390px
  shadow: var(--shadow-float)
  animation: modalIn 0.22s ease
    @keyframes modalIn: scale(0.93) → scale(1), opacity 0 → 1
```

---

## 12. TOAST NOTIFICATIONS

Sau mỗi thao tác thành công/thất bại, hiển thị toast (dùng pattern sẵn có):

| Thao tác | Toast |
|:---|:---|
| Tạo Staff | ✅ "Đã tạo tài khoản Staff. Email mời đã được gửi." |
| Đình chỉ | ✅ "Đã đình chỉ tài khoản thành công" |
| Kích hoạt | ✅ "Đã kích hoạt lại tài khoản" |
| Đặt lại mật khẩu | ✅ "Email đặt lại mật khẩu đã được gửi" |
| Đổi vai trò | ✅ "Đã cập nhật vai trò thành công" |
| Xóa mềm | ✅ "Đã xóa tài khoản (soft delete)" |
| Bất kỳ lỗi | ❌ Message từ backend |

---

## 13. STATE MANAGEMENT

```jsx
// State
const [activeType, setActiveType] = useState('student'); // 'student'|'staff'|'admin'
const [search, setSearch]         = useState('');
const [debouncedSearch]           = useDebounce(search, 400);
const [statusFilter, setStatus]   = useState('');
const [jlptFilter, setJlpt]       = useState('');
const [staffRoleFilter, setRole]  = useState('');
const [currentPage, setPage]      = useState(0);
const [totalPages, setTotalPages] = useState(1);
const [totalElements, setTotal]   = useState(0);
const [users, setUsers]           = useState([]);
const [isLoading, setLoading]     = useState(false);
const [isSubmitting, setSub]      = useState(false);
// Modal states
const [confirmModal, setConfirm]  = useState(null); // { type, user }
const [suspendModal, setSuspend]  = useState(null); // { user }
const [createModal, setCreate]    = useState(false);
const [roleModal, setRole2]       = useState(null); // { user }
```

**Reset page khi filter thay đổi:**
```js
useEffect(() => { setPage(0); }, [activeType, debouncedSearch, statusFilter, jlptFilter, staffRoleFilter]);
```

---

## 14. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 1200px | Bảng đầy đủ cột, stat 4 cột |
| 768–1199px | Stat 2 cột, bảng scroll ngang |
| < 768px | Filter bar dọc (flex-column), stat 2 cột, ẩn cột phụ trong bảng |

**Cột ẩn trên mobile (< 768px):**
- Student: ẩn "Đăng nhập lần cuối", "Streak"
- Staff: ẩn "Đăng nhập lần cuối"

---

## 15. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Table | `<table>` + `<thead>` + `<th scope="col">` |
| Action buttons | `aria-label="Đình chỉ tài khoản: {tên}"` |
| Modals | `role="dialog"`, `aria-modal="true"`, `aria-labelledby` |
| Loading | `aria-busy="true"` trên `<tbody>` |
| Status filter | `<label>` liên kết với `<select>` |
| Toast | `role="alert"`, `aria-live="polite"` |

---

## 16. OUT OF SCOPE

- ❌ Xem hồ sơ chi tiết từng user (UC-37-02) — chỉ xem inline trong bảng
- ❌ Bulk actions (chọn nhiều) — Phase 2
- ❌ Export CSV — Phase 2
- ❌ Tạo tài khoản Admin mới
- ❌ Phục hồi tài khoản đã xóa mềm
