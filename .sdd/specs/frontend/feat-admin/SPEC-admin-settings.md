# SPEC — Admin Settings (Cài đặt)
>
> **Feature ID:** `feat-admin` | **Page:** `AdminSettings`
> **Route:** `/admin/settings`
> **Version:** 1.1 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-28
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-system-admin/SPEC.md § UC-39`

> ⚠️ **Đã gỡ tab "Thông báo" (UC-40):** Giao diện quản trị Notification Rules (milestone) cùng `NotificationsTab`/`RulePanelModal` đã được loại bỏ khỏi FE. Trang chỉ còn 3 tab: Hệ thống · Email · Bảo mật. Các phần milestone bên dưới (§ liên quan UC-40, mock chip milestone, API notification-rules) giữ lại chỉ để tham khảo lịch sử, **không còn áp dụng**. Backend rule CRUD nếu cần xem `feat-system-admin/SPEC.md` (endpoint `/api/admin/notifications/rules`).

---

## 1. TỔNG QUAN TRANG

Trang cài đặt cấu hình hệ thống (UC-39) theo giao diện tab-based: SMTP, tham số bảo mật và chế độ bảo trì.

**Cấu trúc trang:**

```
[1] AdminTopNav       — activeTab="settings"
[2] AdminPageHeader   — "Cài đặt hệ thống" + SakuChan idle
[3] Body
    [3a] Tab bar      — 4 tabs: Hệ thống | Email | Bảo mật | Thông báo
    [3b] Tab content  — nội dung theo tab active
```

**File:**

```
apps/frontend/src/pages/admin/
├── AdminSettings.jsx
└── AdminSettings.css
```

---

## 2. HEADER — AdminPageHeader

```jsx
<AdminPageHeader
  chipIcon={<IcWrench />}
  chipLabel="Cài đặt"
  title="Cài Đặt Hệ Thống"
  subtitle="Cấu hình SMTP, bảo mật, bảo trì và quy tắc thông báo tự động"
  mascotVariant="idle"
  mascotSize={100}
/>
```

---

## 3. TAB BAR — `.ast-tabs`

```
display: flex, gap: 4px
border-bottom: 1px solid var(--color-border)
margin-bottom: 28px
```

Mỗi `.ast-tab`:

```
display: flex, align-items: center, gap: 8px
padding: 10px 20px
border: none, background: transparent
border-bottom: 2px solid transparent
font: 14px/600, color: --color-text-sub
cursor: pointer
margin-bottom: -1px   ← overlap border-bottom parent
transition: color 150ms, border-color 150ms

hover:
  color: var(--color-primary)
  background: var(--color-primary-bg)
  border-radius: var(--radius-md) var(--radius-md) 0 0

.ast-tab--active:
  color: var(--color-primary)
  border-bottom-color: var(--color-primary)
  background: transparent

[Icon]: 18px, color: currentColor
```

**4 tabs:**

| ID | Label | Icon | Query param |
|:---|:---|:---|:---|
| `system` | Hệ thống | `IcWrench` | `?tab=system` |
| `email` | Email / SMTP | `IcMail` | `?tab=email` |
| `security` | Bảo mật | `IcShield` | `?tab=security` |
| `notifications` | Thông báo | `IcBell` | `?tab=notifications` |

Tab active được đọc từ URL query `?tab=...`. Mặc định: `system`. Khi chuyển tab → `history.pushState` không reload trang.

---

## 4. TAB 1 — HỆ THỐNG (`?tab=system`)

### 4.1 Card Chế độ bảo trì — `.ast-maintenance-card`

```
background: var(--color-card)
border-radius: var(--radius-lg)
shadow: var(--shadow-card)
padding: 24px
margin-bottom: 20px
border-left: 4px solid var(--color-warning)    ← khi maintenance = true
             4px solid var(--color-border)     ← khi maintenance = false
```

**Layout bên trong:**

```
display: flex, align-items: flex-start, justify-content: space-between, gap: 20px

[Left]:
  [.ast-card-title]: "Chế độ bảo trì" — font 16px/700, color: --color-text
  [.ast-card-desc]:
    font 13px/400, color: --color-text-sub, margin-top: 4px, max-width: 480px
    Text: "Khi bật, toàn bộ học viên bị chặn đăng nhập với thông báo bảo trì.
           Staff và Admin vẫn truy cập bình thường."

  [.ast-maintenance-banner] (chỉ hiện khi maintenance = true):
    margin-top: 10px
    background: #FFF8E1
    border: 1px solid var(--color-warning)
    border-radius: var(--radius-md)
    padding: 8px 12px
    display: flex, gap: 8px
    [Icon IcWrench 14px, color: --color-warning]
    [Text]: "Hệ thống đang ở chế độ bảo trì. Học viên không thể đăng nhập."
            font 12px/600, color: #E65100

[Right — Toggle switch]:
  .ast-toggle-wrap:
    display: flex, flex-direction: column, align-items: flex-end, gap: 6px

    [.ast-toggle] (input checkbox hidden, label styled):
      width: 48px, height: 26px
      background: --color-border (OFF) / --color-warning (ON)
      border-radius: 13px
      transition: background 200ms
      position: relative
      cursor: pointer

      [::after — thumb]:
        width: 20px, height: 20px
        border-radius: full, background: white
        position: absolute, top: 3px
        left: 3px (OFF) / left: 25px (ON)
        transition: left 200ms
        box-shadow: 0 1px 4px rgba(0,0,0,0.15)

    [.ast-toggle-label]: "BẬT" / "TẮT"
      font: 11px/700, color: --color-text-sub
      text-transform: uppercase
```

**Confirm dialog** trước khi toggle: "Bạn có chắc muốn bật/tắt chế độ bảo trì?" → dùng `window.confirm` đơn giản, không cần modal riêng.

**API:** `PUT /api/admin/settings/system/maintenance_mode { settingValue: "true" | "false" }`

---

## 5. TAB 2 — EMAIL / SMTP (`?tab=email`)

### 5.1 SMTP Settings Card

```
background: var(--color-card)
border-radius: var(--radius-lg)
shadow: var(--shadow-card)
padding: 24px
```

**Card header:**

```
[Title]: "Cấu hình SMTP" — font 18px/700
[Desc]:  "Máy chủ gửi email cho đặt lại mật khẩu, xác minh email và thông báo hệ thống."
         font 13px/400, color: --color-text-sub, margin-bottom: 24px
```

**Form — `.ast-setting-form`:**

```
display: grid
grid-template-columns: repeat(2, 1fr)
gap: 20px
```

Mỗi `.ast-field`:

```
display: flex, flex-direction: column, gap: 6px

[Label]: font 13px/600, color: --color-text
[Input / Select]:
  height: 44px, padding: 0 14px
  border: 1.5px solid var(--color-border)
  border-radius: var(--radius-md)
  background: var(--color-bg)
  font: 14px/400, color: --color-text
  width: 100%

  focus:
    border-color: var(--color-primary)
    box-shadow: 0 0 0 3px rgba(232,154,170,0.15)
    outline: none

  [.ast-field--password input]:
    padding-right: 44px   ← chừa chỗ icon toggle

[.ast-field-hint]: font 11px/400, color: --color-text-sub, margin-top: 2px
```

**6 fields SMTP:**

| Field | Label | Type | Placeholder | Full-width? |
|:---|:---|:---|:---|:---|
| `smtp_host` | SMTP Host | text | `smtp.gmail.com` | ✅ |
| `smtp_port` | SMTP Port | number | `587` | ❌ |
| `smtp_secure` | Bảo mật | select (STARTTLS / SSL/TLS / Không) | — | ❌ |
| `smtp_username` | Tên đăng nhập | email | `no-reply@jlpt.com` | ✅ |
| `smtp_password` | Mật khẩu | password (masked) | `••••••••` | ✅ |
| `smtp_from_name` | Tên hiển thị | text | `SakuJi Platform` | ✅ |

**Password field:**

```
- Hiển thị `••••••••` (không trả về giá trị thật từ backend — NFR-ADMIN-03)
- Icon toggle hiện/ẩn (giống PasswordInput ở login)
- Khi admin muốn đổi: xóa placeholder, nhập giá trị mới
- Gợi ý: "Để trống để giữ nguyên mật khẩu hiện tại"
```

**Footer form:**

```
display: flex, justify-content: space-between, align-items: center
margin-top: 24px
padding-top: 20px
border-top: 1px solid var(--color-border)

[Left]:
  Nút "Kiểm tra kết nối" — secondary outline
  → POST /api/admin/settings/smtp/test
  → Toast: "Kết nối SMTP thành công ✓" / "Lỗi kết nối: {message}"

[Right]:
  Nút "Lưu cài đặt SMTP" — green primary pill
  Loading: spinner
  → PUT mỗi setting key một lần hoặc batch
```

---

## 6. TAB 3 — BẢO MẬT (`?tab=security`)

### 6.1 Security Settings Card

Dạng bảng cài đặt key-value đơn giản:

```
background: var(--color-card)
border-radius: var(--radius-lg)
shadow: var(--shadow-card)
padding: 0
overflow: hidden
```

Mỗi setting row `.ast-setting-row`:

```
display: grid
grid-template-columns: 1fr auto
align-items: center
gap: 20px
padding: 20px 24px
border-bottom: 1px solid var(--color-border)
:last-child { border-bottom: none }

hover: background: var(--color-bg)

[Left]:
  [.ast-sr-label]: "Số lần đăng nhập tối đa" — font 14px/600, color: --color-text
  [.ast-sr-desc]:  "Tài khoản bị khóa sau N lần nhập sai mật khẩu liên tiếp"
                   font 12px/400, color: --color-text-sub, margin-top: 2px

[Right — edit zone]:
  Hiển thị theo mode:
  
  [View mode]: 
    [.ast-sr-value]: giá trị hiện tại, font 16px/700, color: --color-primary
    [Nút IcEdit, 28x28, ghost]: hover hiện, click → edit mode

  [Edit mode]:
    [input số/text, width: 100px]
    [Nút ✓ save, 28x28, green]
    [Nút ✕ cancel, 28x28, gray]
    Input focus tự động khi vào edit mode
```

**3 settings bảo mật:**

| setting_key | Label | Mô tả | value_type | Mặc định |
|:---|:---|:---|:---|:---|
| `max_login_attempts` | Số lần đăng nhập tối đa | Khóa tài khoản sau N lần sai | integer | 5 |
| `lockout_duration_minutes` | Thời gian khóa (phút) | Tự mở khóa sau N phút | integer | 15 |
| `jwt_expiry_minutes` | Thời hạn JWT (phút) | Phiên đăng nhập hết hạn sau N phút | integer | 15 |

**Validation phía client:**

- `max_login_attempts`: 3–20 (integer)
- `lockout_duration_minutes`: 5–1440 (integer)
- `jwt_expiry_minutes`: 5–1440 (integer)

**Lưu:** Inline, ngay sau khi nhấn ✓. Toast xác nhận.

---

## 7. TAB 4 — THÔNG BÁO (`?tab=notifications`)

### 7.1 Layout

```
[Header bar]:
  [Title]: "Quy tắc thông báo tự động"
           font 18px/700, color: --color-text
  [Desc]:  "Cấu hình các thông báo tự động gửi đến người dùng khi đạt mốc học tập"
           font 13px/400, color: --color-text-sub

  [Nút "Tạo quy tắc mới"]:
    Green pill, icon IcPlus
    → mở CreateRulePanel (slide in từ phải hoặc modal)

[List rules — .ast-rule-list]
  Danh sách các notification rule từ GET /api/admin/notification-rules
```

### 7.2 Rule Card — `.ast-rule-card`

```
background: var(--color-card)
border-radius: var(--radius-lg)
shadow: var(--shadow-card)
padding: 20px 24px
margin-bottom: 12px
display: grid
grid-template-columns: 1fr auto
gap: 16px
align-items: flex-start
transition: box-shadow 200ms

hover: box-shadow: var(--shadow-raised)
```

**Nội dung:**

```
[Left]:
  [.ast-rule-header]:
    display: flex, align-items: center, gap: 10px, margin-bottom: 8px

    [Chip milestone — .ast-rule-milestone]:
      padding: 3px 10px, border-radius: full
      font: 11px/700
      background: var(--color-accent-bg), color: #B45309
      Text: milestone key (ví dụ: "streak_10_ngày")

    [Channel chip — .ast-rule-channel]:
      padding: 3px 8px, border-radius: full, font: 11px/600
      in_app:  background: var(--color-primary-bg), color: --color-primary-dark, text: "Trong app"
      email:   background: #E3F2FD, color: #1565C0, text: "Email"

  [.ast-rule-title]: font 15px/700, color: --color-text
    Ví dụ: "Chúc mừng chuỗi học tập 10 ngày!"

  [.ast-rule-content]: font 13px/400, color: --color-text-sub, margin-top: 4px
    Nội dung template (truncate 2 dòng, ellipsis)

[Right]:
  display: flex, gap: 6px

  [Nút IcEdit — 32x32, ghost]:
    → mở EditRulePanel với dữ liệu rule đó

  [Nút IcTrash — 32x32, ghost, hover red]:
    → confirm dialog → DELETE /api/admin/notification-rules/{ruleId}
```

**Empty state khi chưa có rule:**

```
SakuChan variant="thinking" size={120}
[Title]:  "Chưa có quy tắc thông báo"
[Desc]:   "Tạo quy tắc đầu tiên để tự động gửi thông báo đến học viên khi họ đạt mốc học tập."
[Nút]:    "Tạo quy tắc đầu tiên" — green pill
```

### 7.3 Create / Edit Rule Panel — `.ast-rule-panel`

Modal 500px (desktop) hoặc full-width (mobile):

```
background: var(--color-card)
border-radius: var(--radius-xl)
shadow: var(--shadow-float)
padding: 28px
max-width: 500px, width: 100%
```

**Form:**

```
[Field: Trigger / Milestone]
  Label: "Điều kiện kích hoạt"
  Type: Select
  Options:
    - streak_7     → "Đạt chuỗi học tập 7 ngày"
    - streak_10    → "Đạt chuỗi học tập 10 ngày"
    - streak_30    → "Đạt chuỗi học tập 30 ngày"
    - exam_pass    → "Thi thử đạt điểm vượt ngưỡng"
    - level_up     → "Hoàn thành khóa học một cấp"
    - words_100    → "Học được 100 từ vựng"
    - words_500    → "Học được 500 từ vựng"

[Field: Tiêu đề thông báo]
  Type: input text, max 150 ký tự
  Counter: "{n}/150" — góc phải dưới

[Field: Nội dung thông báo]
  Type: textarea, rows=4, max 500 ký tự
  Counter: "{n}/500"
  Hint: "Sử dụng {tên} để chèn tên người dùng"

[Field: Kênh gửi]
  Type: radio group
  Options:
    ● Trong ứng dụng (in_app)  — default
    ○ Email
```

**Footer:**

```
display: flex, gap: 10px, justify-content: flex-end

[Nút Hủy] — ghost
[Nút "Tạo quy tắc" / "Lưu thay đổi"] — green pill
  Loading: spinner, disabled
```

**Validation:**

- Milestone: bắt buộc
- Tiêu đề: bắt buộc, 1–150 ký tự
- Nội dung: bắt buộc, 1–500 ký tự
- Kênh: bắt buộc

---

## 8. API

### UC-39 — System Settings

```
GET  /api/admin/settings                          — lấy tất cả settings
GET  /api/admin/settings/{group}                  — lấy theo group
PUT  /api/admin/settings/{group}/{key}            — cập nhật 1 setting
POST /api/admin/settings/smtp/test                — test kết nối SMTP
```

### UC-40 — Notification Rules ~~(đã gỡ khỏi FE)~~

> Không còn FE gọi các endpoint này. Backend thực tế (tham khảo `feat-system-admin/SPEC.md`):
>
> ```
> GET    /api/admin/notifications/rules            — danh sách rules
> POST   /api/admin/notifications/rules            — tạo rule mới
> PUT    /api/admin/notifications/rules/{ruleKey}  — cập nhật rule
> DELETE /api/admin/notifications/rules/{ruleKey}  — vô hiệu hóa mềm
> ```

---

## 9. COMPONENT STRUCTURE

```jsx
// pages/admin/AdminSettings.jsx
import AdminTopNav        from '../../components/layout/AdminTopNav';
import { AdminPageHeader} from '../../components/admin/AdminPageHeader';
import {
  IcAdminChip, IcEdit, IcTrash, IcBloomCheck,
  // icons mới thêm vào ManageUsersIcons:
  IcWrench, IcMail, IcShield, IcBell, IcPlus,
} from '../../components/admin/ManageUsersIcons';
import './AdminSettings.css';

function AdminSettings() {
  const [activeTab, setActiveTab] = useState('system'); // từ URL query
  const [settings, setSettings]  = useState({});
  const [rules, setRules]         = useState([]);
  const [isLoading, setLoading]   = useState(false);
  const [rulePanel, setRulePanel] = useState(null); // null | 'create' | ruleObj

  // đọc tab từ URL: new URLSearchParams(location.search).get('tab') ?? 'system'
  // push tab mới vào history khi chuyển tab

  return (
    <div className="ast-page">
      <AdminTopNav activeTab="settings" />
      <AdminPageHeader
        chipIcon={<IcWrench />} chipLabel="Cài đặt"
        title="Cài Đặt Hệ Thống"
        subtitle="Cấu hình SMTP, bảo mật, bảo trì và quy tắc thông báo tự động"
        mascotVariant="idle"
      />
      <main className="ast-body">
        <nav className="ast-tabs">{/* 4 tabs */}</nav>
        {activeTab === 'system'        && <SystemTab ... />}
        {activeTab === 'email'         && <EmailTab  ... />}
        {activeTab === 'security'      && <SecurityTab ... />}
        {activeTab === 'notifications' && <NotificationsTab ... />}
      </main>

      {rulePanel && <RulePanelModal ... />}
    </div>
  );
}
```

**Sub-components (tạo trong cùng file hoặc tách nhỏ):**

- `SystemTab` — maintenance toggle
- `EmailTab` — SMTP form
- `SecurityTab` — setting rows
- `NotificationsTab` — rule list + create button
- `RulePanelModal` — create/edit modal

---

## 10. LOADING / ERROR / EMPTY

| Case | Xử lý |
|:---|:---|
| Đang tải settings | Skeleton lines trong card thay thế inputs |
| Lỗi tải settings | Banner đỏ + nút "Thử lại" |
| Đang save | Spinner trong nút, disabled form/row |
| Lỗi save | Toast đỏ với message từ backend |
| Rules rỗng | EmptyState với SakuChan thinking |
| Lỗi tải rules | Banner đỏ |

---

## 11. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 1024px | SMTP form 2 cột |
| < 1024px | SMTP form 1 cột |
| < 768px | Tab labels ẩn icon trên mobile nhỏ, chỉ text; Rule panel fullscreen |

---

## 12. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Tab navigation | `role="tablist"`, `role="tab"`, `aria-selected`, `aria-controls` |
| Tab content | `role="tabpanel"`, `aria-labelledby` |
| Toggle | `role="switch"`, `aria-checked` |
| Password fields | `autocomplete="new-password"`, toggle có `aria-label` |
| Rule panel | `role="dialog"`, `aria-modal="true"`, focus trap |
| Edit inline | `aria-label="Chỉnh sửa {setting_label}"` |

---

## 13. OUT OF SCOPE

- ❌ Import/Export cấu hình (backup settings)
- ❌ Lịch sử thay đổi cài đặt (audit log settings) — Phase 2
- ❌ Template email HTML đầy đủ — chỉ text thuần
- ❌ Notification channel: Push notification / SMS
- ❌ A/B test notification templates
