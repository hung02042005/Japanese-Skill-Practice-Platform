# SPEC — Trang Đặt Lại Mật Khẩu (Reset Password)
>
> **Feature ID:** `feat-auth` | **Page:** `ResetPassword`
> **Route:** `/reset-password?token={token}` (public, redirect về `/dashboard` nếu đã auth)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-05-31
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `.sdd/specs/backend/feat-auth/UC-03-reset-password.md`

---

## 1. TỔNG QUAN TRANG

Trang người dùng nhấp vào từ email để đặt lại mật khẩu. Nhận token qua query string `?token=...`, xác thực và cho phép đặt mật khẩu mới.

Mục tiêu: **hiển thị đúng trạng thái ngay khi tải trang, form gọn — chỉ 2 field, phản hồi rõ ràng**.

**Ba trạng thái:**

```
[State 1] Token không hợp lệ / thiếu — hiển thị ngay khi mount (không có ?token)
[State 2] Form đặt mật khẩu mới    — hiển thị khi token hợp lệ trên URL
[State 3] Thành công               — sau khi submit và API trả về 200
```

**File structure:**

```
apps/frontend/src/
└── pages/
    └── forgot-password/
        ├── ResetPassword.jsx     ← page root (state 1 + 2 + 3)
        └── ResetPassword.css
```

---

## 2. DESIGN TOKENS ÁP DỤNG

```css
/* Giống LoginPage — xem SPEC-login.md § 2 */

/* Bổ sung riêng cho ResetPassword */
--rp-error-icon-bg:   #FDE0EC;  /* nền icon lỗi token */
--rp-error-icon:      #E03131;  /* màu ✕ SVG */
--rp-success-icon-bg: #E8F5E9;  /* nền icon thành công */
--rp-success-icon:    #1AAE39;  /* màu checkmark SVG */
```

---

## 3. LAYOUT TỔNG THỂ

Dùng chung layout với LoginPage và ForgotPassword:

```
Background: var(--color-bg) — #FAF7F4
Min-height: 100vh
Display:    flex, flex-direction: column
```

```
┌──────────────────────────────────────────────┐
│  [AuthTopBar — 64px]                         │
├──────────────────────────────────────────────┤
│                                              │
│     [petal trang trí — position: absolute]   │
│                                              │
│         ┌──────────────────────┐             │
│         │      AuthCard        │             │
│         │  (440px, centered)   │             │
│         └──────────────────────┘             │
│                                              │
└──────────────────────────────────────────────┘
```

---

## 4. AUTH TOP BAR

Dùng chung component `AuthTopBar` — xem SPEC-login.md § 4.

---

## 5. BACKGROUND PETALS

Dùng chung logic với LoginPage — xem SPEC-login.md § 5.

---

## 6. AUTH CARD — `.auth-card`

```
Background:    var(--color-card) — #FFFFFF
Border-radius: var(--radius-xl)  — 24px
Shadow:        0 4px 12px rgba(0,0,0,0.10)
Padding:       40px
Width:         100%, max-width: 440px
Position:      relative, z-index: 1
```

---

## 7. STATE 1 — TOKEN KHÔNG HỢP LỆ / THIẾU

Hiển thị khi `useSearchParams()` không tìm thấy `?token=` hoặc token rỗng.
Không cần gọi API — render ngay phía client.

```
┌──────────────────────────────────────────┐
│                                          │
│    [Icon ✕ tròn đỏ — 48px]              │
│                                          │
│    Link không hợp lệ                    │
│                                          │
│  Link đặt lại mật khẩu không hợp lệ    │
│  hoặc đã hết hạn. Vui lòng yêu cầu     │
│  gửi lại link mới.                      │
│                                          │
│    [● Gửi lại link]                     │
│                                          │
└──────────────────────────────────────────┘
```

**Icon lỗi — `.rp-icon-error`**

```
SVG 48×48:
  Nền: <rect width="48" height="48" rx="24" fill="#FDE0EC"/>
  ✕:   <path d="M16 16l16 16M32 16l-16 16"
              stroke="#E03131" strokeWidth="2.5" strokeLinecap="round"/>
Display: block, margin: 0 auto 16px
```

**Tiêu đề — `.rp-invalid-title`**

```
Text:          "Link không hợp lệ"
Font:          Nunito 700, 24px (heading-lg)
Color:         var(--color-text)
Text-align:    center
Margin-bottom: 8px
```

**Mô tả — `.rp-invalid-desc`**

```
Text:        "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.
              Vui lòng yêu cầu gửi lại link mới."
Font:        Nunito 400, 14px
Color:       var(--color-text-sub)
Text-align:  center
Line-height: 1.7
Margin-bottom: 24px
```

**CTA — Nút gửi lại**

```
Text:          "Gửi lại link"
Element:       <a> thẻ (href="/forgot-password")
Style:         btn-primary (xem SPEC-login.md § 6.4)
  background:    var(--color-secondary)
  color:         white
  width:         100%
  height:        50px
  border-radius: var(--radius-full)
  font:          Nunito 800, 15px uppercase
```

---

## 8. STATE 2 — FORM ĐẶT MẬT KHẨU MỚI

Hiển thị khi URL có `?token=` hợp lệ (không rỗng). Frontend không pre-validate token — backend sẽ kiểm tra khi submit.

### 8.1 Mascot + Header

**Saku-chan — `.auth-mascot`**

```
Size:  80px (sm variant)
State: idle    — khi trang vừa load (sway 3s ease-in-out infinite)
       thinking — khi đang submit (peek 2s ease infinite)
Display: block, margin: 0 auto 16px
```

**Tiêu đề — `.auth-title`**

```
Text:          "Đặt lại mật khẩu"
Font:          Nunito 700, 24px (heading-lg)
Color:         var(--color-text)
Text-align:    center
Margin-bottom: 4px
```

**Mô tả phụ — `.auth-subtitle`**

```
Text:          "Nhập mật khẩu mới cho tài khoản của bạn."
Font:          Nunito 400, 14px (body-md)
Color:         var(--color-text-sub)
Text-align:    center
Margin-bottom: 28px
```

---

### 8.2 Banner Lỗi API — `.auth-error-banner`

Hiển thị khi backend trả về lỗi (token hết hạn sau submit, lỗi server, v.v.).

```
Display:       flex, align-items: flex-start, gap: 10px
Background:    #FFEAEA
Border:        1px solid var(--color-error)
Border-radius: var(--radius-md)
Padding:       12px 16px
Margin-bottom: 16px

[Icon]: SVG ✕ 16px, color: var(--color-error)
[Text]: error message từ Redux state
        Font: Nunito 600, 13px, color: var(--color-error)

Khi error là TOKEN_EXPIRED / INVALID_TOKEN:
  Text: "Link đặt lại mật khẩu đã hết hạn. "
        [Link "Gửi lại link mới"] → href="/forgot-password"
        Font link: Nunito 600, color: var(--color-primary)

Saku-chan: chuyển sang variant `wrong` — wilt 0.3s rồi về idle
```

---

### 8.3 Form — `.rp-form`

```
Display: flex, flex-direction: column, gap: 20px
```

**Field Mật Khẩu Mới — `.form-field`**

```
[Label]:
  Text:          "Mật khẩu mới"
  htmlFor:       "rp-new"
  Font:          Nunito 600, 14px (label-md)
  Color:         var(--color-text)
  Margin-bottom: 6px

[PasswordInput — dùng component PasswordInput]:
  id:            "rp-new"
  type:          password (toggle được)
  placeholder:   "Ít nhất 8 ký tự, 1 hoa, 1 số"
  autocomplete:  new-password
  autoFocus:     true
  Style input:   giống form-input (xem SPEC-login.md § 6.3)
  Padding-right: 48px ← chừa chỗ cho icon toggle

  Focus:
    border-color: var(--color-primary)
    box-shadow:   0 0 0 3px rgba(232,154,170,0.18)
    background:   white

  Error (.has-error):
    border-color: var(--color-error)
    background:   #FEF2F2
    box-shadow:   0 0 0 3px rgba(229,115,115,0.12)

[Error message — .field-error]:
  id:         "rp-new-error"
  Font:       Nunito 400, 12px
  Color:      var(--color-error)
  Margin-top: 4px

  Thông điệp:
    - Rỗng:          "Vui lòng nhập mật khẩu mới"
    - < 8 ký tự:     "Mật khẩu phải có ít nhất 8 ký tự"
    - Thiếu chữ hoa: "Mật khẩu phải có ít nhất 1 chữ hoa"
    - Thiếu số:      "Mật khẩu phải có ít nhất 1 chữ số"

[Password strength bar — .password-strength]:
  Hiện khi user bắt đầu gõ (length > 0) — xem SPEC-register.md § 6.2 để biết
  chi tiết 4 mức weak/fair/good/strong và cách tính điểm.
```

**Field Xác Nhận Mật Khẩu — `.form-field`**

```
[Label]:
  Text:          "Xác nhận mật khẩu mới"
  htmlFor:       "rp-confirm"
  Font:          Nunito 600, 14px
  Color:         var(--color-text)
  Margin-bottom: 6px

[PasswordInput]:
  id:           "rp-confirm"
  type:         password (toggle được)
  placeholder:  "Nhập lại mật khẩu mới"
  autocomplete: new-password

  [Validation inline — khi confirmPassword không rỗng]:
    Khớp     → icon ✓ màu var(--color-secondary) xuất hiện bên phải input
    Không khớp → input .has-error + field-error "Mật khẩu xác nhận không khớp"

[Error message — .field-error]:
  id:  "rp-confirm-error"
  Thông điệp:
    - Rỗng:       "Vui lòng xác nhận mật khẩu"
    - Không khớp: "Mật khẩu xác nhận không khớp"
```

---

### 8.4 Nút Submit — `.btn-rp-submit`

```
Text:          "Đặt lại mật khẩu"
Width:         100%
Height:        50px
Background:    var(--color-secondary) — #5DBB69
Color:         white
Font:          Nunito 800, 15px, text-transform: uppercase, letter-spacing: 0.5px
Border-radius: var(--radius-full)
Border:        none
Cursor:        pointer
Shadow:        0 4px 12px rgba(93,187,105,0.30)
Transition:    filter 150ms, transform 100ms, box-shadow 150ms

Hover:
  filter:     brightness(1.08)
  box-shadow: 0 6px 16px rgba(93,187,105,0.38)

Active:
  transform: scale(0.97)

Loading (.is-loading):
  opacity: 0.80
  cursor:  not-allowed
  Text:    "Đang xử lý..."
  [Spinner] 20px trắng, animation: spin 0.8s linear infinite

Disabled (.is-disabled) — khi password không hợp lệ hoặc confirm không khớp:
  opacity: 0.60
  cursor:  not-allowed
  filter:  none
```

---

## 9. STATE 3 — ĐẶT LẠI THÀNH CÔNG

Hiển thị sau khi `POST /api/auth/reset-password` trả về HTTP 200.
Toàn bộ form được **thay thế** trong cùng card — không điều hướng.

```
Transition: opacity 0 → 1, translateY(8px → 0), 300ms ease
```

```
┌──────────────────────────────────────────┐
│                                          │
│   [Saku-chan — celebrate/happy 80px]     │
│                                          │
│   Đặt lại mật khẩu thành công           │
│                                          │
│  Mật khẩu của bạn đã được cập nhật.    │
│  Hãy đăng nhập để tiếp tục học!        │
│                                          │
│    [● Đăng nhập ngay]                   │
│                                          │
└──────────────────────────────────────────┘
```

**Saku-chan — `.auth-mascot`**

```
Size:  80px (sm variant)
State: happy (spin-bounce 0.6s ease) → sau 0.8s chuyển về idle (sway)
```

**Icon thành công — `.rp-icon-success`** *(hiển thị thay Saku-chan nếu chưa có component)*

```
SVG 48×48:
  Nền:  <rect width="48" height="48" rx="24" fill="#E8F5E9"/>
  Check: <path d="M14 24l7 7 13-13"
               stroke="#1AAE39" strokeWidth="2.5"
               strokeLinecap="round" strokeLinejoin="round"/>
Display: block, margin: 0 auto 16px
```

**Tiêu đề — `.rp-success-title`**

```
Text:          "Đặt lại mật khẩu thành công"
Font:          Nunito 700, 24px
Color:         var(--color-text)
Text-align:    center
Margin-bottom: 8px
```

**Mô tả — `.rp-success-desc`**

```
Text:        "Mật khẩu của bạn đã được cập nhật."
Font:        Nunito 400, 14px
Color:       var(--color-text-sub)
Text-align:  center
Margin-bottom: 24px
```

**CTA — Nút đăng nhập**

```
Text:          "Đăng nhập ngay"
Element:       <a> thẻ (href="/login")
Style:         btn-primary
  background:    var(--color-secondary)
  color:         white
  width:         100%
  height:        50px
  border-radius: var(--radius-full)
  font:          Nunito 800, 15px uppercase
```

---

## 10. LOGIC TRẠNG THÁI (State Machine)

```
mount
  ↓
Đọc ?token từ URL
  ↓
token rỗng / null? ──→ [State 1: Invalid Token]
  ↓ Có token
[State 2: Form]
  ↓ submit
validate() → lỗi? → highlight fields, Saku-chan wilt
  ↓ hợp lệ
dispatch(resetPasswordThunk({ token, newPassword, confirmPassword }))
  ↓ unwrap() thành công
[State 3: Success]
  ↓ unwrap() thất bại
error vào Redux → hiện .auth-error-banner + Saku-chan wilt
```

---

## 11. ANIMATIONS

```css
/* ResetPassword.css — tái sử dụng từ LoginPage.css: sway, spin-bounce, wilt, spin */

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Áp dụng khi State 3 xuất hiện */
.rp-success-content {
  animation: fadeSlideIn 300ms ease forwards;
}

@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 12. ROUTE & COMPONENT

```jsx
// App.jsx
<Route path="/reset-password" element={<ResetPassword />} />
// Nếu đã auth: <Navigate to="/dashboard" replace />
```

```jsx
// pages/forgot-password/ResetPassword.jsx
import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { resetPasswordThunk, clearError } from '../../store/slices/authSlice';
import AppLogo from '../../components/common/AppLogo';
import './ResetPassword.css';

function ResetPassword() {
  const [searchParams]  = useSearchParams();
  const token           = searchParams.get('token');
  // state: newPassword, confirmPassword, fieldErrors{}, isDone
  // isLoading ← status === 'loading'
  // error     ← state.auth.error

  // validate() → kiểm tra rules: rỗng, length ≥ 8, chữ hoa, số, khớp confirm
  // handleSubmit() → validate → dispatch(resetPasswordThunk) → setIsDone(true)

  // State 1: không có token
  if (!token) {
    return (
      <div className="rp-page">
        <div className="rp-container">
          {/* AuthTopBar */}
          <div className="fp-card">
            {/* Icon ✕ SVG */}
            <h2 className="rp-invalid-title">Link không hợp lệ</h2>
            <p className="rp-invalid-desc">...</p>
            <a className="btn btn-primary" href="/forgot-password">Gửi lại link</a>
          </div>
        </div>
      </div>
    );
  }

  // State 3: thành công
  if (isDone) {
    return (
      <div className="rp-page">
        <div className="rp-container">
          {/* AuthTopBar */}
          <div className="fp-card rp-success-content">
            {/* Saku-chan happy / Icon ✓ SVG */}
            <h2 className="rp-success-title">Đặt lại mật khẩu thành công</h2>
            <p className="rp-success-desc">Mật khẩu của bạn đã được cập nhật.</p>
            <a className="btn btn-primary" href="/login">Đăng nhập ngay</a>
          </div>
        </div>
      </div>
    );
  }

  // State 2: form
  return (
    <div className="rp-page">
      <div className="rp-container">
        {/* AuthTopBar */}
        <div className="fp-card">
          {/* <SakuChan size="sm" variant={isLoading ? 'thinking' : 'idle'} /> */}
          <h2 className="auth-title">Đặt lại mật khẩu</h2>
          <p className="auth-subtitle">...</p>
          {error && <div className="auth-error-banner">{error}</div>}
          <form className="rp-form" onSubmit={handleSubmit} noValidate
                aria-busy={isLoading}>
            {/* Field newPassword — PasswordInput + strength bar */}
            {/* Field confirmPassword — PasswordInput + match icon */}
            <button className="btn-rp-submit" type="submit" disabled={isLoading}>
              {isLoading ? 'Đang xử lý...' : 'Đặt lại mật khẩu'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
```

---

## 13. API

| Action | Method | Endpoint | Request body | Response thành công |
|:---|:---|:---|:---|:---|
| Đặt lại mật khẩu | `POST` | `/api/auth/reset-password` | `{ token, newPassword, confirmPassword }` | HTTP 200 |

**Các lỗi backend trả về:**

| HTTP | Error code | Hiển thị |
|:---|:---|:---|
| 400 | `VALIDATION_ERROR` | Field errors tương ứng |
| 400/410 | `INVALID_TOKEN` | Banner lỗi + link "Gửi lại link mới" |
| 410 | `TOKEN_EXPIRED` | Banner lỗi + link "Gửi lại link mới" |
| 5xx | Server error | Banner lỗi generic |

---

## 14. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 768px | Card 440px centered, petals hiển thị |
| < 768px | Card full-width (margin 16px mỗi bên), petals ẩn, padding card giảm còn 24px |
| < 480px | Font `auth-title` giảm xuống 20px; strength bar label ẩn (chỉ giữ màu bar) |

---

## 15. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Heading | `<h2>` cho tiêu đề card |
| Form labels | Mỗi input có `<label htmlFor>` riêng |
| Error states | `aria-invalid="true"` + `aria-describedby` trỏ vào field-error tương ứng |
| Password toggle | `aria-label` thay đổi: "Hiện mật khẩu" / "Ẩn mật khẩu" |
| Confirm match | `aria-label="Mật khẩu khớp"` / `aria-label="Mật khẩu không khớp"` trên icon |
| Strength bar | `aria-live="polite"` trên label mức độ (đọc khi thay đổi) |
| Loading | `aria-busy="true"` trên `<form>` khi submit |
| Focus State 3 | Focus vào `.rp-success-title` khi chuyển sang màn hình thành công |
| Focus ring | `outline: 2px solid var(--color-primary)` trên mọi interactive element |
| Tab order | Mật khẩu mới → Xác nhận → Submit (tự nhiên theo DOM) |

---

## 16. OUT OF SCOPE

- ❌ Pre-validate token khi page mount (gọi API riêng để kiểm tra token trước) — Phase 2
- ❌ Countdown tự redirect về `/login` sau thành công (xem SPEC-register.md § 8.2 để tham khảo pattern)
- ❌ Reset mật khẩu khi đang đăng nhập (trang profile riêng)
- ❌ Dark mode
