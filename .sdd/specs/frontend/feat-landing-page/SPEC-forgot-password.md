# SPEC — Trang Quên Mật Khẩu (Forgot Password)
>
> **Feature ID:** `feat-auth` | **Page:** `ForgotPassword`
> **Route:** `/forgot-password` (public, redirect về `/dashboard` nếu đã auth)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-05-31
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `.sdd/specs/backend/feat-auth/UC-03-reset-password.md`

---

## 1. TỔNG QUAN TRANG

Trang quên mật khẩu cho phép học viên yêu cầu gửi link đặt lại mật khẩu qua email. Mục tiêu: **quy trình nhanh, một bước, không làm lộ thông tin tài khoản**.

Thiết kế không sidebar, một card trung tâm trên nền washi — dùng chung layout với Login và Register.

**Hai trạng thái của trang:**

```
[State 1] Form nhập email     — mặc định khi vào trang
[State 2] Thông báo gửi email — sau khi submit (thay thế form trong cùng AuthCard, không điều hướng)
```

> **Security note:** Backend luôn trả HTTP 200 dù email tồn tại hay không (tránh email enumeration).
> Frontend hiển thị thông báo thành công trong cả hai trường hợp.

**File structure:**

```
apps/frontend/src/
└── pages/
    └── forgot-password/
        ├── ForgotPassword.jsx     ← page root (state 1 + 2)
        └── ForgotPassword.css
```

---

## 2. DESIGN TOKENS ÁP DỤNG

```css
/* Giống LoginPage — xem SPEC-login.md § 2 */

/* Bổ sung riêng cho ForgotPassword */
--fp-success-icon-bg: #E8F5E9;  /* nền hình tròn icon checkmark */
--fp-success-icon:    #1AAE39;  /* màu đường checkmark SVG */
```

---

## 3. LAYOUT TỔNG THỂ

Dùng chung layout với LoginPage:

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

**`.auth-main`**

```
flex: 1
display: flex
align-items: center
justify-content: center
padding: 40px 16px
position: relative
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
Background:    var(--color-card)      — #FFFFFF
Border-radius: var(--radius-xl)       — 24px
Shadow:        0 4px 12px rgba(0,0,0,0.10)
Padding:       40px
Width:         100%, max-width: 440px
Position:      relative, z-index: 1
```

---

## 7. STATE 1 — FORM NHẬP EMAIL

### 7.1 Mascot + Header

**Saku-chan — `.auth-mascot`**

```
Size:      80px (sm variant)
State:     idle   — khi trang vừa load (sway 3s ease-in-out infinite)
           thinking — khi đang submit (peek 2s ease infinite)
Display:   block, margin: 0 auto 16px
```

**Tiêu đề — `.auth-title`**

```
Text:          "Quên mật khẩu?"
Font:          Nunito 700, 24px (heading-lg)
Color:         var(--color-text)
Text-align:    center
Margin-bottom: 4px
```

**Mô tả phụ — `.auth-subtitle`**

```
Text:          "Nhập email đã đăng ký, chúng tôi sẽ gửi link đặt lại mật khẩu cho bạn."
Font:          Nunito 400, 14px (body-md)
Color:         var(--color-text-sub)
Text-align:    center
Margin-bottom: 28px
Max-width:     340px, margin-left: auto, margin-right: auto
```

---

### 7.2 Banner Lỗi API — `.auth-error-banner`

Chỉ hiện khi backend trả lỗi không mong đợi (5xx, network error).
Không hiện khi email không tồn tại — backend ẩn điều này theo security note ở § 1.

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
```

---

### 7.3 Form — `.fp-form`

```
Display:        flex, flex-direction: column, gap: 20px
```

**Field Email — `.form-field`**

```
[Label]:
  Text:          "Email"
  htmlFor:       "fp-email"
  Font:          Nunito 600, 14px (label-md)
  Color:         var(--color-text)
  Margin-bottom: 6px

[Input — .form-input]:
  id:            "fp-email"
  type:          email
  placeholder:   "example@email.com"
  autocomplete:  email
  autoFocus:     true
  Height:        48px
  Background:    #FAF7F4
  Border:        1.5px solid var(--color-border)
  Border-radius: var(--radius-md)
  Padding:       0 16px
  Font:          Nunito 400, 16px
  Color:         var(--color-text)

  Focus:
    border-color: var(--color-primary)
    box-shadow:   0 0 0 3px rgba(232,154,170,0.18)
    background:   white
    outline:      none

  Error (.has-error):
    border-color: var(--color-error)
    background:   #FEF2F2
    box-shadow:   0 0 0 3px rgba(229,115,115,0.12)

[Error message — .field-error]:
  id:         "fp-email-error"
  Font:       Nunito 400, 12px
  Color:      var(--color-error)
  Margin-top: 4px

  Thông điệp:
    - Rỗng:       "Vui lòng nhập email"
    - Sai format: "Email không hợp lệ"
```

---

### 7.4 Nút Gửi — `.btn-fp-submit`

```
Text:          "Gửi link đặt lại mật khẩu"
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
  filter:    brightness(1.08)
  box-shadow: 0 6px 16px rgba(93,187,105,0.38)

Active:
  transform: scale(0.97)

Loading (.is-loading):
  opacity:   0.80
  cursor:    not-allowed
  Text:      "Đang gửi..."
  [Spinner]: 20px trắng, animation: spin 0.8s linear infinite

Disabled (.is-disabled):
  opacity: 0.60
  cursor:  not-allowed
  filter:  none
```

---

### 7.5 Link Quay Lại — `.fp-back-link`

```
Text:            "Quay lại đăng nhập"
Display:         block, text-align: center
Margin-top:      20px
Font:            Nunito 600, 14px
Color:           var(--color-text-sub)
Text-decoration: none
href:            "/login"

Hover: color: var(--color-text)
```

---

## 8. STATE 2 — THÔNG BÁO GỬI EMAIL THÀNH CÔNG

Hiển thị sau khi `POST /api/auth/forgot-password` trả về HTTP 200.
Toàn bộ form được **thay thế** bằng màn hình thông báo trong cùng card — không điều hướng.

```
Transition: opacity 0 → 1, translateY(8px → 0), 300ms ease
```

```
┌──────────────────────────────────────────┐
│                                          │
│    [Icon checkmark tròn — 48px]          │
│                                          │
│    Kiểm tra email của bạn               │
│                                          │
│  Nếu email {email} tồn tại trong hệ    │
│  thống, bạn sẽ nhận được link đặt lại  │
│  mật khẩu trong vài phút.              │
│                                          │
│  Không nhận được email?  [Gửi lại]      │
│                                          │
│       Quay lại đăng nhập                │
│                                          │
└──────────────────────────────────────────┘
```

**Icon thành công — `.fp-icon-success`**

```
SVG 48×48:
  Nền:  <rect width="48" height="48" rx="24" fill="#E8F5E9"/>
  Check: <path d="M14 24l7 7 13-13"
               stroke="#1AAE39" strokeWidth="2.5"
               strokeLinecap="round" strokeLinejoin="round"/>
Display: block, margin: 0 auto 16px
```

**Tiêu đề — `.fp-card-title`**

```
Text:          "Kiểm tra email của bạn"
Font:          Nunito 700, 24px
Color:         var(--color-text)
Text-align:    center
Margin-bottom: 12px
```

**Mô tả — `.fp-card-desc`**

```
Nội dung: "Nếu email " + <strong>{email}</strong> +
          " tồn tại trong hệ thống, bạn sẽ nhận được
           link đặt lại mật khẩu trong vài phút."

Font:        Nunito 400, 14px
Color:       var(--color-text-sub)
Text-align:  center
Line-height: 1.7
Max-width:   340px, margin: 0 auto 20px

<strong>{email}>:
  Font-weight: 700
  Color:       var(--color-text)
```

**Gửi lại — `.fp-card-hint`**

```
Layout: text-align center, font Nunito 400 14px, color var(--color-text-sub)

"Không nhận được email? "
[button.link-btn "Gửi lại"]:
  background:      transparent
  border:          none
  font:            Nunito 600, 14px
  color:           var(--color-primary)
  cursor:          pointer
  text-decoration: underline
  onClick:         setIsSent(false) + clearError() → quay về State 1

  Hover: color: var(--color-primary-dark)
```

**Link quay lại — `.fp-back-link`**

```
Text:            "Quay lại đăng nhập"
Display:         block, text-align: center
Margin-top:      12px
Font:            Nunito 600, 14px
Color:           var(--color-text-sub)
Text-decoration: none
href:            "/login"

Hover: color: var(--color-text)
```

---

## 9. ANIMATIONS

```css
/* ForgotPassword.css — tái sử dụng từ LoginPage.css: sway, thinking/peek, spin */

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Áp dụng khi State 2 xuất hiện */
.fp-success-content {
  animation: fadeSlideIn 300ms ease forwards;
}

@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 10. ROUTE & COMPONENT

```jsx
// App.jsx
<Route path="/forgot-password" element={<ForgotPassword />} />
// Nếu đã auth: <Navigate to="/dashboard" replace />
```

```jsx
// pages/forgot-password/ForgotPassword.jsx
import { useState } from 'react';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { forgotPasswordThunk, clearError } from '../../store/slices/authSlice';
import AppLogo from '../../components/common/AppLogo';
import './ForgotPassword.css';

function ForgotPassword() {
  // state: email, fieldError, isSent
  // isLoading ← status === 'loading' từ Redux
  // error     ← state.auth.error (lỗi API không mong đợi)

  // validate()          → kiểm tra rỗng + format email
  // handleEmailChange() → clear fieldError + clearError Redux khi gõ
  // handleSubmit()      → validate → dispatch(forgotPasswordThunk) → setIsSent(true)

  if (isSent) {
    return (
      <div className="fp-page">
        <div className="fp-container">
          {/* AuthTopBar */}
          <div className="fp-card fp-success-content">
            {/* Icon checkmark SVG */}
            <h2 className="fp-card-title">Kiểm tra email của bạn</h2>
            <p className="fp-card-desc">
              Nếu email <strong>{email}</strong> tồn tại...
            </p>
            <p className="fp-card-hint">
              Không nhận được email?{' '}
              <button className="link-btn"
                onClick={() => { setIsSent(false); dispatch(clearError()); }}>
                Gửi lại
              </button>
            </p>
            <a className="fp-back-link" href="/login">Quay lại đăng nhập</a>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fp-page">
      <div className="fp-container">
        {/* AuthTopBar */}
        <div className="fp-card">
          {/* <SakuChan size="sm" variant={isLoading ? 'thinking' : 'idle'} /> */}
          <h2 className="auth-title">Quên mật khẩu?</h2>
          <p className="auth-subtitle">...</p>
          {error && <div className="auth-error-banner">{error}</div>}
          <form className="fp-form" onSubmit={handleSubmit} noValidate
                aria-busy={isLoading}>
            <div className={`form-field ${fieldError ? 'has-error' : ''}`}>
              <label htmlFor="fp-email">Email</label>
              <input id="fp-email" type="email"
                     aria-invalid={!!fieldError}
                     aria-describedby={fieldError ? 'fp-email-error' : undefined}
                     value={email} onChange={handleEmailChange}
                     autoComplete="email" autoFocus />
              {fieldError && <span id="fp-email-error" className="field-error">{fieldError}</span>}
            </div>
            <button className="btn-fp-submit" type="submit" disabled={isLoading}>
              {isLoading ? 'Đang gửi...' : 'Gửi link đặt lại mật khẩu'}
            </button>
          </form>
          <a className="fp-back-link" href="/login">Quay lại đăng nhập</a>
        </div>
      </div>
    </div>
  );
}
```

---

## 11. API

| Action | Method | Endpoint | Request body | Response |
|:---|:---|:---|:---|:---|
| Yêu cầu reset | `POST` | `/api/auth/forgot-password` | `{ email }` | HTTP 200 (luôn, bất kể email có tồn tại) |

---

## 12. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 768px | Card 440px centered, petals hiển thị |
| < 768px | Card full-width (margin 16px mỗi bên), petals ẩn, padding card giảm còn 24px |
| < 480px | Font `auth-title` giảm xuống 20px |

---

## 13. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Heading | `<h2>` cho tiêu đề card (không cạnh tranh với `<h1>` AuthTopBar) |
| Form label | `<label htmlFor="fp-email">` liên kết trực tiếp với input |
| Error state | `aria-invalid="true"` + `aria-describedby="fp-email-error"` trên input |
| Loading | `aria-busy="true"` trên `<form>` khi đang submit |
| Focus sau State 2 | Focus vào `.fp-card-title` khi chuyển sang màn hình thành công |
| Nút Gửi lại | `aria-label="Gửi lại email đặt lại mật khẩu"` |
| Focus ring | `outline: 2px solid var(--color-primary)` trên mọi interactive element |
| Tab order | Email input → Submit → Back link (tự nhiên theo DOM) |

---

## 14. OUT OF SCOPE

- ❌ Reset mật khẩu qua số điện thoại / OTP — Phase 2
- ❌ Cooldown timer trên nút Gửi lại (xem SPEC-reset-password.md)
- ❌ Thay đổi mật khẩu khi đã đăng nhập (trang profile riêng)
- ❌ Dark mode
