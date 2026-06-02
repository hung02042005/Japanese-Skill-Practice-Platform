# MASTER-SPEC — Hướng dẫn tạo trang Admin (Frontend)
> **Phiên bản:** 1.0 | **Cập nhật:** 2026-06-02
> **Mục đích:** Đọc tài liệu này là đủ để tạo bất kỳ trang admin mới nào mà không cần hỏi thêm.
> **Design ref:** `DESIGN.md` | **Backend ref:** `.sdd/specs/backend/feat-system-admin/`

---

## 0. QUY TẮC SỐ 1

> **Tận dụng component có sẵn trước. Tạo mới chỉ khi thật sự không có.**

Mọi pattern trong tài liệu này đều dựa trên code thực tế trong codebase — không phải lý thuyết. Trước khi viết bất kỳ dòng nào, kiểm tra `components/admin/`, `components/common/`, `components/layout/`.

---

## 1. CẤU TRÚC FILE

### 1.1 Quy tắc đặt tên

| Loại | Convention | Ví dụ |
|:---|:---|:---|
| Page file | `PascalCase.jsx` + cùng tên `.css` | `AdminReports.jsx` |
| Component | `PascalCase.jsx`, không có `.css` riêng trừ khi tự dùng | `ReportsChart.jsx` |
| CSS class | `[prefix]-[block]` hoặc `[prefix]-[block]__[element]` | `.rpt-card`, `.rpt-card__title` |
| Prefix | 3 chữ cái viết tắt của page | `rpt-` cho Reports, `cnt-` cho Content |
| API function | `camelCase` trong `adminService.js` | `getReportsSummary` |

### 1.2 Vị trí file

```
apps/frontend/src/
├── pages/admin/
│   ├── AdminReports.jsx        ← page: chỉ layout + data fetching
│   └── AdminReports.css        ← tất cả CSS của trang + components con
│
├── components/admin/
│   ├── ReportsChart.jsx        ← sub-component (không có CSS riêng)
│   ├── ReportsFilterBar.jsx
│   └── settings/               ← sub-folder nếu nhiều components cùng nhóm
│       └── ReportsExportPanel.jsx
│
└── api/
    └── adminService.js         ← thêm function mới vào đây, không tạo file mới
```

### 1.3 Quy tắc tách component

**Tách ra file riêng khi:**
- Component > 60 dòng JSX
- Dùng lại ở ≥ 2 nơi
- Có state/logic riêng biệt hoàn toàn

**Giữ inline (trong cùng file) khi:**
- Component < 30 dòng
- Chỉ render UI thuần, không có state
- Là helper nhỏ (icon, skeleton row đơn giản)

**Quy tắc page file:** Page JSX chỉ chứa:
1. Imports
2. State management + data fetching
3. Return JSX: `AdminTopNav` + `AdminPageHeader` + `<main>` gọi components
4. Không có logic nghiệp vụ, không có sub-component dài

---

## 2. GIẢI PHẪU MỘT TRANG ADMIN

Mọi trang admin đều có cùng 3 phần:

```
┌──────────────────────────────────────────────┐
│  AdminTopNav (sticky 64px, white)            │  activeTab="[tab-id]"
├──────────────────────────────────────────────┤
│  AdminPageHeader (gradient, petals, SakuChan)│  chipIcon, title, subtitle, mascotVariant
├──────────────────────────────────────────────┤
│  <main className="[prefix]-body">            │
│    max-width: 1400px, margin: auto           │
│    padding: 28px 32px 48px                   │
│    [Nội dung trang]                          │
└──────────────────────────────────────────────┘
```

### 2.1 Template JSX chuẩn

```jsx
import AdminTopNav           from '../../components/layout/AdminTopNav';
import { AdminPageHeader }   from '../../components/admin/AdminPageHeader';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { IcAdminChip }       from '../../components/admin/ManageUsersIcons';
import { MyComponent }       from '../../components/admin/MyComponent';
import './AdminMyPage.css';

export default function AdminMyPage() {
  const [data,      setData]    = useState(null);
  const [isLoading, setLoading] = useState(true);
  const { toasts, addToast, removeToast } = useToast();

  useEffect(() => { /* fetch data */ }, []);

  return (
    <div className="myp-page">
      <AdminTopNav activeTab="[tab-id]" />

      <AdminPageHeader
        chipIcon={<IcAdminChip />}
        chipLabel="Nhãn chip"
        title="Tiêu Đề Trang"
        subtitle="Mô tả ngắn về trang này"
        mascotVariant={isLoading ? 'thinking' : 'happy'}
        mascotSize={100}
      />

      <main className="myp-body">
        <MyComponent data={data} isLoading={isLoading} addToast={addToast} />
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

### 2.2 Template CSS chuẩn

```css
/* ===== Admin [TênTrang] (SakuJi Hanami Theme) ===== */

.myp-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  font-family: var(--font-base);
}

.myp-body {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 32px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-sizing: border-box;
}

@media (max-width: 1199px) { .myp-body { padding: 24px 20px 40px; } }
@media (max-width: 767px)  { .myp-body { padding: 16px 16px 32px; } }

@media (prefers-reduced-motion: reduce) {
  /* tắt toàn bộ animation trong trang */
  .myp-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 3. DESIGN TOKENS — BẢNG THAM CHIẾU NHANH

**Không bao giờ hard-code màu hex.** Luôn dùng CSS variable.

```css
/* === Màu brand === */
--color-primary:       #E89AAA   /* sakura pink — active, accent */
--color-primary-light: #F7CBD4   /* hover, tint */
--color-primary-dark:  #D84F68   /* pressed, focus ring deep */
--color-primary-bg:    #FFF0F3   /* chip bg, tab hover bg */
--color-secondary:     #5DBB69   /* green — CTA button, success */
--color-secondary-bg:  #F4FBF5   /* green tint bg */
--color-accent:        #F7C948   /* gold — achievement */
--color-accent-bg:     #FFF7DD   /* gold tint bg */

/* === Surface === */
--color-bg:            #FAF7F4   /* page background (washi) */
--color-card:          #FFFFFF   /* card/panel background */
--color-border:        #E8E0DC   /* dividers, input border */

/* === Text === */
--color-text:          #2D2D2D   /* body text */
--color-text-sub:      #6B625E   /* labels, captions */
--color-text-disabled: #B7ABA5   /* disabled state */

/* === Semantic === */
--color-error:         #E57373
--color-warning:       #F4A261

/* === Shape === */
--radius-sm:    8px    /* small chips, action buttons */
--radius-md:    12px   /* inputs, cards nhỏ */
--radius-lg:    16px   /* cards, panels */
--radius-xl:    24px   /* modals, auth card */
--radius-full:  9999px /* pills, avatars */

/* === Shadow === */
--shadow-sm:    0 2px 8px rgba(0,0,0,0.07)    /* card mặc định */
--shadow-md:    0 4px 12px rgba(0,0,0,0.10)   /* card hover, stat cards */
--shadow-lg:    0 8px 24px rgba(0,0,0,0.12)   /* modal, toast */

/* === Misc === */
--font-base:    'Nunito', 'Noto Sans JP', system-ui, sans-serif
--transition:   200ms ease
```

---

## 4. COMPONENT INVENTORY — CÓ SẴN, DÙNG LUÔN

### 4.1 Layout

| Component | Import | Props chính |
|:---|:---|:---|
| `AdminTopNav` | `components/layout/AdminTopNav` | `activeTab: string` |
| `AdminPageHeader` | `components/admin/AdminPageHeader` | `chipIcon`, `chipLabel`, `title`, `subtitle`, `mascotVariant`, `mascotSize` |

**activeTab values** (phải khớp với ADMIN_TABS trong AdminTopNav.jsx):
```
'admin-overview'  → /admin
'manage-users'    → /admin/users
'manage-content'  → /admin/content
'reports'         → /admin/reports
'settings'        → /admin/settings
```

**mascotVariant values:** `'idle'` | `'happy'` | `'thinking'` | `'correct'` | `'wrong'` | `'celebrate'`
- Loading → `'thinking'`
- Xong xuôi → `'happy'`
- Trang tĩnh → `'idle'`

### 4.2 Data & Feedback

| Component | Import | Props |
|:---|:---|:---|
| `StatCard` | `components/admin/StatCard` | `icon`, `value`, `label`, `variant`, `loading` |
| `EmptyState` | `components/common/EmptyState` | `title`, `subtitle`, `mascotVariant`, `mascotSize`, `children` |
| `Pagination` | `components/common/Pagination` | `currentPage` (1-based), `totalPages`, `onChange(page)` |
| `SkeletonRow` | `components/admin/SkeletonRow` | `cols: number` |
| `StatusBadge` | `components/common/Badges` | `status: 'active'\|'suspended'\|'pending'\|'deleted'` |
| `JlptBadge` | `components/common/Badges` | `level: 'N5'\|'N4'\|'N3'\|'N2'\|'N1'` |
| `RoleBadge` | `components/common/Badges` | `role: string` |
| `UserAvatar` | `components/common/UserAvatar` | `src`, `name`, `size` |
| `useToast` | `components/common/Toast` | returns `{ toasts, addToast, removeToast }` |
| `ToastContainer` | `components/common/Toast` | `toasts`, `onRemove` |

**StatCard variants:** `'total'` | `'active'` | `'banned'` | `'new'`
- `total`  → border primary-light, icon bg primary-bg
- `active` → border secondary,     icon bg secondary-bg
- `banned` → border error,         icon bg #FEF2F2
- `new`    → border accent,        icon bg accent-bg

**addToast usage:**
```js
addToast('success', 'Thành công rồi!');
addToast('error',   'Có lỗi xảy ra: ' + err.message);
addToast('info',    'Đang xử lý...');
```

### 4.3 Modals (ManageUsers)

```jsx
import { ConfirmModal, SuspendModal, CreateStaffModal, ChangeStaffRoleModal }
  from '../../components/admin/UserModals';
```
CSS của các modal này nằm trong `ManageUsers.css`, dùng các class `mu-overlay`, `mu-modal`, v.v.

### 4.4 Icons — `ManageUsersIcons.jsx`

```jsx
import {
  /* Stat card icons */
  STAT_ICONS,          // { total, active, suspended, pending }

  /* Tab icons */
  TAB_ICONS,           // { student, staff, admin }

  /* Settings tab icons */
  SETTINGS_TAB_ICONS,  // { system, email, security, notifications }

  /* Row action icons */
  IcBan, IcCheck, IcKey, IcSwap, IcTrash,

  /* Header icons */
  IcAdminChip, IcAddStaff, IcSearchGlass,

  /* Dashboard icons */
  IcChart, IcSystemHealth,

  /* Settings icons */
  IcMail, IcShield, IcWrench, IcBell, IcPlus, IcEdit,

  /* Modal confirm icon */
  IcBloomCheck,
} from '../../components/admin/ManageUsersIcons';
```

**Quy tắc icon:**
- Không import từ thư viện ngoài (lucide-react, heroicons, v.v.)
- Thêm icon mới → thêm vào `ManageUsersIcons.jsx`, export named
- Mọi icon `aria-hidden="true"` — văn bản mô tả nằm trong `aria-label` của button cha
- Style: 2px stroke, round cap, round join, `currentColor`, petal ellipse trang trí opacity ≤ 0.22

---

## 5. STATE MANAGEMENT PATTERN

### 5.1 Data fetching chuẩn

```jsx
const [data,      setData]    = useState(null);   // null = chưa tải / lỗi
const [isLoading, setLoading] = useState(true);
const [error,     setError]   = useState('');

const fetchData = useCallback(async () => {
  setLoading(true);
  setError('');
  try {
    const result = await apiCall();
    setData(result);
  } catch (err) {
    setError(err?.response?.data?.message ?? 'Không thể tải dữ liệu.');
    setData(null);
  } finally {
    setLoading(false);
  }
}, [/* deps */]);

useEffect(() => { fetchData(); }, [fetchData]);
```

### 5.2 Pagination state

```jsx
const [currentPage, setCurrentPage] = useState(1);  // 1-based cho UI
const [totalPages,  setTotalPages]  = useState(1);
const [totalElements]               = useState(0);
const PAGE_SIZE = 20;

// Reset page khi filter thay đổi
useEffect(() => { setCurrentPage(1); }, [filter1, filter2]);

// API call dùng page - 1 (0-based cho backend)
const params = { page: currentPage - 1, size: PAGE_SIZE };
```

### 5.3 Debounce search

```jsx
const [search,   setSearch]   = useState('');
const [debounced, setDebounced]= useState('');
const timerRef = useRef(null);

useEffect(() => {
  clearTimeout(timerRef.current);
  timerRef.current = setTimeout(() => {
    setDebounced(search);
    setCurrentPage(1);
  }, 400);
  return () => clearTimeout(timerRef.current);
}, [search]);
```

### 5.4 Submit action

```jsx
const [isSubmitting, setSubmitting] = useState(false);

async function handleAction() {
  setSubmitting(true);
  try {
    await apiAction();
    addToast('success', 'Thành công!');
    fetchData(); // refresh
  } catch (err) {
    addToast('error', err?.response?.data?.message ?? 'Thao tác thất bại');
  } finally {
    setSubmitting(false);
  }
}
```

---

## 6. LOADING / ERROR / EMPTY — 3 TRẠNG THÁI BẮT BUỘC

**Mọi API-backed section đều phải xử lý đủ 3:**

### 6.1 Loading — Skeleton

```jsx
// Bảng
{isLoading && Array.from({ length: 8 }).map((_, i) => (
  <SkeletonRow key={i} cols={6} />
))}

// Cards
{isLoading && Array.from({ length: 4 }).map((_, i) => (
  <div key={i} className="myp-card-skel" aria-hidden="true" />
))}
```

```css
/* CSS skeleton — dùng lại pattern này */
.myp-card-skel {
  height: 96px;
  border-radius: var(--radius-lg);
  background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%);
  background-size: 200% 100%;
  animation: skelPulse 1.4s ease infinite;
}
@keyframes skelPulse {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
```

### 6.2 Error — Banner + Retry

```jsx
{error && (
  <div className="myp-error-banner" role="alert">
    <span>{error}</span>
    <button className="myp-retry-btn" onClick={fetchData}>Thử lại</button>
  </div>
)}
```

```css
.myp-error-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #FFEAEA;
  border: 1px solid var(--color-error);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  font-size: 13px;
  color: var(--color-error);
}
.myp-retry-btn {
  background: transparent;
  border: 1.5px solid var(--color-error);
  border-radius: var(--radius-full);
  color: var(--color-error);
  font-size: 12px;
  font-weight: 700;
  padding: 4px 12px;
  cursor: pointer;
  white-space: nowrap;
}
```

### 6.3 Empty — EmptyState component

```jsx
{!isLoading && !error && data.length === 0 && (
  <EmptyState
    title="Chưa có dữ liệu"
    subtitle="Mô tả gợi ý hành động tiếp theo."
    mascotVariant="thinking"
    mascotSize={120}
  >
    {/* CTA button tùy chọn */}
    <button className="myp-btn myp-btn--primary" onClick={handleCreate}>
      Tạo mới
    </button>
  </EmptyState>
)}
```

---

## 7. FORM PATTERN

### 7.1 Input field chuẩn

```jsx
<div className="myp-field">
  <label className="myp-field-label" htmlFor="field-name">
    Tên trường
    {/* Optional: char counter */}
    <span className="myp-char-count">{value.length}/100</span>
  </label>
  <input
    id="field-name"
    className={`myp-input${error ? ' myp-input--err' : ''}`}
    type="text"
    value={value}
    onChange={(e) => setValue(e.target.value)}
    placeholder="Placeholder..."
    aria-invalid={!!error}
    aria-describedby={error ? 'field-name-err' : undefined}
  />
  {error && (
    <span id="field-name-err" className="myp-field-error">{error}</span>
  )}
</div>
```

```css
.myp-field { display: flex; flex-direction: column; gap: 6px; }

.myp-field-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.myp-char-count { font-size: 11px; font-weight: 400; color: var(--color-text-sub); }

.myp-input {
  height: 44px;
  padding: 0 14px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg);
  font-family: var(--font-base);
  font-size: 14px;
  color: var(--color-text);
  width: 100%;
  box-sizing: border-box;
  transition: border-color var(--transition), box-shadow var(--transition);
}
.myp-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232,154,170,0.15);
  background: var(--color-card);
}
.myp-input--err {
  border-color: var(--color-error);
  background: #FEF2F2;
}
.myp-input--err:focus { box-shadow: 0 0 0 3px rgba(229,115,115,0.12); }

.myp-field-error { font-size: 12px; color: var(--color-error); }
```

### 7.2 Textarea

```css
.myp-textarea {
  padding: 10px 14px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg);
  font-family: var(--font-base);
  font-size: 14px;
  color: var(--color-text);
  width: 100%;
  box-sizing: border-box;
  resize: vertical;
  line-height: 1.5;
  /* same focus/error states as input */
}
```

### 7.3 Buttons chuẩn

```css
/* Primary CTA — green */
.myp-btn--primary {
  background: var(--color-secondary);
  color: white;
  border: none;
  box-shadow: 0 2px 8px rgba(93,187,105,0.25);
}
.myp-btn--primary:hover:not(:disabled) { filter: brightness(1.07); }

/* Outline — pink border */
.myp-btn--outline {
  background: transparent;
  border: 1.5px solid var(--color-primary);
  color: var(--color-primary);
}
.myp-btn--outline:hover:not(:disabled) { background: var(--color-primary-bg); }

/* Ghost — no border */
.myp-btn--ghost {
  background: transparent;
  border: 1.5px solid var(--color-border);
  color: var(--color-text-sub);
}
.myp-btn--ghost:hover:not(:disabled) { color: var(--color-text); background: var(--color-bg); }

/* Danger */
.myp-btn--danger { background: var(--color-error); color: white; border: none; }
.myp-btn--danger:hover:not(:disabled) { filter: brightness(0.95); }

/* Base shared styles */
.myp-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 42px;
  padding: 0 22px;
  border-radius: var(--radius-full);
  font-family: var(--font-base);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: filter var(--transition), transform var(--transition), box-shadow var(--transition);
  white-space: nowrap;
}
.myp-btn:disabled    { opacity: 0.60; cursor: not-allowed; filter: none !important; }
.myp-btn:active:not(:disabled) { transform: scale(0.97); }
```

### 7.4 Loading spinner trong button

```jsx
{isSaving && <span className="myp-spinner myp-spinner--white" aria-hidden="true" />}
{isSaving ? 'Đang lưu…' : 'Lưu'}
```

```css
.myp-spinner {
  display: inline-block;
  width: 18px; height: 18px;
  border: 2.5px solid var(--color-primary-light);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}
.myp-spinner--white { border-color: rgba(255,255,255,0.35); border-top-color: white; }
.myp-spinner--small { width: 14px; height: 14px; border-width: 2px; }

@keyframes spin { to { transform: rotate(360deg); } }
```

---

## 8. MODAL PATTERN

### 8.1 Template modal

```jsx
function MyModal({ onClose, onSaved, addToast }) {
  const backdropRef = useRef(null);

  // Escape key
  useEffect(() => {
    function onKey(e) { if (e.key === 'Escape') onClose(); }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  function handleBackdrop(e) { if (e.target === backdropRef.current) onClose(); }

  return (
    <div
      className="myp-modal-backdrop"
      ref={backdropRef}
      onClick={handleBackdrop}
      role="dialog"
      aria-modal="true"
      aria-label="Tiêu đề modal"
    >
      <div className="myp-modal">
        <div className="myp-modal-header">
          <h3 className="myp-modal-title">Tiêu đề</h3>
          <button className="myp-modal-close" onClick={onClose} aria-label="Đóng">
            {/* CloseIcon SVG */}
          </button>
        </div>

        <form className="myp-modal-form" onSubmit={handleSubmit}>
          {/* fields */}
          <div className="myp-modal-footer">
            <button type="button" className="myp-btn myp-btn--ghost" onClick={onClose}>Hủy</button>
            <button type="submit" className="myp-btn myp-btn--primary" disabled={saving}>
              {saving && <span className="myp-spinner myp-spinner--white" aria-hidden="true" />}
              {saving ? 'Đang lưu…' : 'Lưu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
```

### 8.2 Modal CSS

```css
.myp-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.40);
  backdrop-filter: blur(2px);
  z-index: 300;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.myp-modal {
  background: var(--color-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: 28px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  animation: modalIn 0.22s ease;
}
@keyframes modalIn {
  from { opacity: 0; transform: scale(0.93) translateY(8px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}
.myp-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
}
.myp-modal-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; }
.myp-modal-close {
  width: 32px; height: 32px;
  border-radius: var(--radius-full);
  border: none;
  background: var(--color-bg);
  color: var(--color-text-sub);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.myp-modal-close:hover { background: #FFEAEA; color: var(--color-error); }
.myp-modal-form { display: flex; flex-direction: column; gap: 18px; }
.myp-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 8px;
  border-top: 1px solid var(--color-border);
}
```

---

## 9. TABLE PATTERN

### 9.1 Cấu trúc bảng

```jsx
<div className="myp-table-wrap">
  <table className="myp-table">
    <thead>
      <tr>
        <th scope="col">Cột 1</th>
        <th scope="col">Cột 2</th>
        <th scope="col" aria-label="Thao tác" />
      </tr>
    </thead>
    <tbody>
      {isLoading ? (
        Array.from({ length: 8 }).map((_, i) => <SkeletonRow key={i} cols={5} />)
      ) : rows.map((row, i) => (
        <tr
          key={row.id}
          className="myp-tr"
          style={{ '--row-i': i }}
        >
          <td>...</td>
          <td>
            <div className="myp-actions">
              <button
                className="myp-act-ic myp-act-ic--edit"
                onClick={() => handleEdit(row)}
                aria-label={`Chỉnh sửa: ${row.name}`}
              >
                <IcEdit />
              </button>
            </div>
          </td>
        </tr>
      ))}
    </tbody>
  </table>
</div>
```

### 9.2 Table CSS

```css
.myp-table-wrap {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  overflow-x: auto;
}
.myp-table { width: 100%; border-collapse: collapse; }
.myp-table thead { background: var(--color-bg); border-bottom: 1px solid var(--color-border); }
.myp-table th {
  padding: 10px 16px;
  font-size: 11px; font-weight: 700;
  color: var(--color-text-sub);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  text-align: left;
  white-space: nowrap;
}
.myp-tr {
  border-bottom: 1px solid var(--color-border);
  transition: background var(--transition);
  animation: rowIn 0.25s ease both;
  animation-delay: calc(var(--row-i) * 30ms);
}
.myp-tr:last-child { border-bottom: none; }
.myp-tr:hover { background: var(--color-bg); }

@keyframes rowIn {
  from { opacity: 0; transform: translateX(-6px); }
  to   { opacity: 1; transform: translateX(0); }
}

.myp-table td { padding: 12px 16px; font-size: 14px; color: var(--color-text); }

/* Action column */
.myp-actions { display: flex; gap: 4px; align-items: center; }
.myp-act-ic {
  width: 30px; height: 30px;
  border-radius: var(--radius-sm);
  border: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  opacity: 0;
  transition: opacity var(--transition), transform var(--transition);
}
.myp-tr:hover .myp-act-ic { opacity: 1; }
.myp-act-ic:hover { transform: scale(1.10); }
.myp-act-ic:active { transform: scale(0.93); }
.myp-act-ic:disabled { opacity: 0.35; cursor: not-allowed; }

/* Variants */
.myp-act-ic--edit   { background: var(--color-bg);         color: var(--color-text-sub); }
.myp-act-ic--edit:hover   { background: var(--color-primary-bg); color: var(--color-primary); }
.myp-act-ic--delete { background: var(--color-bg);         color: var(--color-text-sub); }
.myp-act-ic--delete:hover { background: #FFEAEA;            color: var(--color-error); }
```

---

## 10. API LAYER PATTERN

Mọi function API đều thêm vào `api/adminService.js`. Không tạo file mới.

```js
// Pattern chuẩn
export async function getMyData({ param1, param2, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (param1) params.param1 = param1;
  if (param2) params.param2 = param2;
  const res = await api.get('/admin/my-endpoint', { params });
  return res.data.data;
}

export async function createMyItem(data) {
  const res = await api.post('/admin/my-endpoint', data);
  return res.data.data;
}

export async function updateMyItem(id, data) {
  const res = await api.put(`/admin/my-endpoint/${id}`, data);
  return res.data.data;
}

export async function deleteMyItem(id) {
  const res = await api.delete(`/admin/my-endpoint/${id}`);
  return res.data;
}
```

**`api` là axios instance từ `authService.js`** — đã có interceptor JWT tự động.

---

## 11. RESPONSIVE PATTERN

| Breakpoint | Class name | Thay đổi thường gặp |
|:---|:---|:---|
| ≥ 1200px | mặc định | Full layout |
| 768–1199px | `@media (max-width: 1199px)` | Grid → 2 cột, padding giảm |
| < 768px | `@media (max-width: 767px)` | 1 cột, padding nhỏ, table scroll ngang |
| < 480px | `@media (max-width: 479px)` | Card full-width, modal full-screen |

```css
/* Responsive grid chuẩn */
.myp-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

@media (max-width: 1199px) { .myp-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px)  { .myp-grid { grid-template-columns: 1fr; gap: 10px; } }
```

---

## 12. ACCESSIBILITY CHECKLIST

Trước khi coi trang là xong, kiểm tra:

- [ ] Mỗi trang có đúng **1 thẻ `<h1>`** (nằm trong `AdminPageHeader`)
- [ ] Mọi `<input>` có `<label>` liên kết bằng `htmlFor` / `id`
- [ ] Mọi action icon button có `aria-label` mô tả đủ (tên hành động + tên đối tượng)
- [ ] Modal có `role="dialog"`, `aria-modal="true"`, `aria-label`
- [ ] Escape key đóng modal
- [ ] Click backdrop đóng modal
- [ ] Loading state có `aria-busy="true"` trên container
- [ ] Error message có `role="alert"`
- [ ] Table dùng `<th scope="col">` và `aria-label` cho cột icon
- [ ] Màu sắc không phải thông tin duy nhất (luôn kèm text)
- [ ] `@media (prefers-reduced-motion: reduce)` tắt animation

---

## 13. JLPT LEVEL COLORS

Dùng cho `JlptBadge` và filter UI:

| Level | Background | Text |
|:---|:---|:---|
| N5 | `#E8F5E9` | `#2E7D32` |
| N4 | `#E3F2FD` | `#1565C0` |
| N3 | `#FFF3E0` | `#E65100` |
| N2 | `#F3E5F5` | `#6A1B9A` |
| N1 | `#FCE4EC` | `#C62828` |

---

## 14. CHECKLIST TẠO TRANG MỚI

Khi nhận yêu cầu tạo một trang admin mới:

**Bước 1 — Chuẩn bị:**
- [ ] Xác định `activeTab` string cho AdminTopNav
- [ ] Xác định prefix CSS (3 chữ cái, ví dụ `rpt-`)
- [ ] Xác định mascot variant phù hợp với context trang
- [ ] Xác định API endpoints cần (CRUD gì?)

**Bước 2 — Thêm API:**
- [ ] Thêm functions vào `api/adminService.js`

**Bước 3 — Tạo file:**
- [ ] `pages/admin/AdminXxx.jsx` — page shell
- [ ] `pages/admin/AdminXxx.css` — CSS đầy đủ
- [ ] `components/admin/XxxComponent.jsx` — mỗi section lớn = 1 file

**Bước 4 — Đăng ký route:**
- [ ] Thêm `<Route path="/admin/xxx" element={<AdminRoute><AdminXxx /></AdminRoute>} />` vào `App.jsx`

**Bước 5 — Kiểm tra:**
- [ ] 3 trạng thái: loading skeleton / error banner / empty state
- [ ] Responsive 3 breakpoint
- [ ] Accessibility checklist (§12)
- [ ] Không hard-code hex color
- [ ] Không dùng icon từ thư viện ngoài

---

## 15. VÍ DỤ NHANH — Trang `/admin/content`

Tham chiếu cách áp dụng tài liệu này:

```
Prefix:     cnt-
activeTab:  'manage-content'
mascot:     'idle' khi tải, 'happy' khi xong
API:        getCourses(), createCourse(), updateCourse(), softDeleteCourse()
Components: ContentFilterBar, CourseTable, CourseFormModal
States:     courses[], isLoading, error, currentPage, totalPages, search, jlptFilter
```

Page file (`AdminContent.jsx`) chỉ có:
```jsx
export default function AdminContent() {
  // state + fetch
  return (
    <div className="cnt-page">
      <AdminTopNav activeTab="manage-content" />
      <AdminPageHeader chipIcon={...} title="Nội Dung" ... />
      <main className="cnt-body">
        <ContentFilterBar ... />
        <CourseTable ... />
        <Pagination ... />
      </main>
      <ToastContainer ... />
    </div>
  );
}
```
