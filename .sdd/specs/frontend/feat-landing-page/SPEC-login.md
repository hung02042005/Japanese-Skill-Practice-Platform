# SPEC — Trang Đăng Nhập (Login)
>
> **Feature ID:** `feat-auth` | **Page:** `Login`
> **Route:** `/login` (public, redirect về `/dashboard` nếu đã auth)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-05-31
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-auth/UC-01-login.md`

---

## 1. TỔNG QUAN TRANG

Trang đăng nhập dành cho học viên. Mục tiêu: **xác thực danh tính và đưa người dùng vào Dashboard nhanh nhất**. Thiết kế đơn giản, không sidebar, một card trung tâm trên nền washi.

**Hai luồng đăng nhập:**

1. **Email + Mật khẩu** — form truyền thống
2. **Google OAuth** — một cú nhấn

**Cấu trúc trang:**

```
[1] AuthTopBar    — Logo + link về trang chủ
[2] AuthCard      — Toàn bộ form đăng nhập (card trắng giữa màn hình)
[3] Background    — Washi canvas + petals trang trí (mờ)
```

**File structure:**

```
apps/frontend/src/
├── pages/
│   └── auth/
│       ├── LoginPage.jsx           ← page root
│       ├── LoginPage.css
│       ├── RegisterPage.jsx
│       ├── RegisterPage.css
│       ├── VerifyEmailPage.jsx     ← trang thông báo sau đăng ký
│       └── components/
│           ├── AuthTopBar.jsx      ← dùng chung cho Login + Register
│           ├── AuthTopBar.css
│           ├── AuthCard.jsx        ← wrapper card trắng
│           ├── AuthCard.css
│           ├── SocialLoginButton.jsx
│           └── PasswordInput.jsx   ← input mật khẩu có toggle show/hide
```

---

## 2. DESIGN TOKENS ÁP DỤNG

```css
/* Màu */
--color-primary:      #E89AAA;
--color-primary-light:#F7CBD4;
--color-primary-bg:   #FFF0F3;
--color-primary-dark: #D84F68;
--color-secondary:    #5DBB69;
--color-secondary-bg: #F4FBF5;
--color-bg:           #FAF7F4;
--color-card:         #FFFFFF;
--color-text:         #2D2D2D;
--color-text-sub:     #6B625E;
--color-text-disabled:#B7ABA5;
--color-border:       #E8E0DC;
--color-error:        #E57373;
--color-warning:      #F4A261;

/* Radius */
--radius-sm:   8px;
--radius-md:   12px;
--radius-lg:   16px;
--radius-xl:   24px;
--radius-full: 9999px;

/* Shadow */
--shadow-card:   0 4px 12px rgba(0,0,0,0.10);
--shadow-raised: 0 4px 12px rgba(0,0,0,0.10);
--shadow-float:  0 8px 24px rgba(0,0,0,0.12);
```

---

## 3. LAYOUT TỔNG THỂ

```
Background: var(--color-bg) — #FAF7F4 (washi)
Min-height: 100vh
Display:    flex, flex-direction: column
```

**Bố cục dọc:**

```
┌──────────────────────────────────────────────┐
│  [AuthTopBar — 64px]                         │
├──────────────────────────────────────────────┤
│                                              │
│     [petal trang trí — position absolute]    │
│                                              │
│         ┌──────────────────────┐             │
│         │      AuthCard        │             │
│         │  (440px, centered)   │             │
│         │                      │             │
│         └──────────────────────┘             │
│                                              │
└──────────────────────────────────────────────┘
```

**Main area — `.auth-main`**

```
flex: 1
display: flex
align-items: center
justify-content: center
padding: 40px 16px
position: relative   ← để các petal trang trí dùng absolute
```

---

## 4. AUTH TOP BAR — `.auth-topbar`

Thanh nav tối giản, không có các nav links như ở Landing page.

```
Height:     64px
Background: var(--color-card) — white
Border:     border-bottom: 1px solid var(--color-border)
Padding:    0 max(24px, calc((100vw - 1200px) / 2))
Display:    flex, align-items: center, justify-content: space-between
```

**Logo — `.auth-topbar-logo`**

```
Layout: flex, align-items: center, gap: 10px
Icon:   Saku-chan mini SVG (28×28px)
Text:   "SakuJi"  (Nunito 800, 22px)
  "Saku" → color: var(--color-primary)
  "Ji"   → color: var(--color-text)
Link:   href="/" — nhấn để về trang chủ
Hover:  opacity 0.85
```

**Back link — `.auth-topbar-back`**

```
Text:   "← Trang chủ"
Font:   Nunito 600, 14px
Color:  var(--color-text-sub)
Hover:  color: var(--color-text)
Text-decoration: none
```

---

## 5. BACKGROUND PETALS (trang trí)

```
6 cánh hoa SVG nhỏ, vị trí random trong viewport:
  Fill:      var(--color-primary-light) — #F7CBD4
  Opacity:   0.10 → 0.18 (mỗi cánh khác nhau)
  Size:      20px → 40px
  Position:  absolute, pointer-events: none, z-index: 0
  Animation: petalDrift 6–9s ease-in-out infinite alternate
             (mỗi cánh delay 0–2s khác nhau)

Chỉ hiện trên viewport ≥ 768px.
Tắt hoàn toàn khi prefers-reduced-motion: reduce.
```

---

## 6. AUTH CARD — `.auth-card`

```
Background:    var(--color-card) — white
Border-radius: var(--radius-xl) — 24px
Shadow:        var(--shadow-raised)
Padding:       40px
Width:         100%
Max-width:     440px
Position:      relative, z-index: 1
```

### 6.1 Mascot + Header

**Saku-chan — `.auth-mascot`**

```
Size:      80px (sm variant)
State:     idle (gentle sway) khi trang vừa load
           happy (spin-bounce 0.6s) sau khi đăng nhập thành công
Display:   block, margin: 0 auto 16px
Animation: sway 3s ease-in-out infinite (idle state)
  @keyframes sway {
    0%, 100% { transform: rotate(-4deg); }
    50%       { transform: rotate(4deg); }
  }
```

**Tiêu đề — `.auth-title`**

```
Text:       "Chào mừng trở lại"
Font:       Nunito 700, 24px (heading-lg)
Color:      var(--color-text)
Text-align: center
Margin-bottom: 4px
```

**Mô tả phụ — `.auth-subtitle`**

```
Text:       "Đăng nhập để tiếp tục hành trình học tiếng Nhật"
Font:       Nunito 400, 14px (body-md)
Color:      var(--color-text-sub)
Text-align: center
Margin-bottom: 28px
```

---

### 6.2 Banner Khóa Tài Khoản — `.auth-locked-banner`

Chỉ hiển thị khi backend trả về `TOO_MANY_REQUESTS` (HTTP 429).

```
Display:       flex, align-items: center, gap: 10px
Background:    #FFF8E1
Border:        1px solid var(--color-warning)
Border-radius: var(--radius-md)
Padding:       12px 16px
Margin-bottom: 16px

[Icon]: ⏰ (24px) hoặc SVG lock icon, color: var(--color-warning)
[Text]: "Tài khoản tạm thời bị khóa. Thử lại sau {X} phút."
        Font: Nunito 600, 13px
        Color: #E65100
```

---

### 6.3 Form Đăng Nhập — `.login-form`

```
Display:        flex, flex-direction: column, gap: 20px
```

**Field Email — `.form-field`**

```
[Label]:
  Text:   "Email"
  Font:   Nunito 600, 14px (label-md)
  Color:  var(--color-text)
  Margin-bottom: 6px

[Input — .form-input]:
  Type:          email
  Placeholder:   "email@example.com"
  Height:        48px
  Background:    #FAF7F4
  Border:        1.5px solid var(--color-border)
  Border-radius: var(--radius-md)
  Padding:       0 16px
  Font:          Nunito 400, 16px
  Color:         var(--color-text)

  Focus:
    border-color: var(--color-primary)
    box-shadow: 0 0 0 3px rgba(232,154,170,0.18)
    background: white
    outline: none

  Error (.has-error):
    border-color: var(--color-error)
    background:   #FEF2F2
    box-shadow:   0 0 0 3px rgba(229,115,115,0.12)

[Error message — .field-error]:
  Font:       Nunito 400, 12px
  Color:      var(--color-error)
  Margin-top: 4px
  Display:    flex, align-items: center, gap: 4px
  Icon:       ● (4px dot) hoặc ✕ nhỏ
```

**Field Mật Khẩu — `.form-field`**

```
[Label row]:
  Display: flex, justify-content: space-between, align-items: center
  Margin-bottom: 6px

  [Label text]:   "Mật khẩu"  (Nunito 600, 14px)
  [Quên MK link]: "Quên mật khẩu?"
    Font:  Nunito 600, 13px
    Color: var(--color-primary)
    Hover: color var(--color-primary-dark), text-decoration: underline
    Link:  href="/forgot-password"

[PasswordInput — .password-input-wrapper]:
  Position: relative

  [Input]:
    Cùng style với email input
    Type: password (toggle được)
    Padding-right: 48px   ← chừa chỗ cho icon toggle

  [Toggle show/hide — .password-toggle]:
    Position: absolute, right: 14px, top: 50%, transform: translateY(-50%)
    Width: 24px, Height: 24px
    Background: transparent, Border: none, Cursor: pointer
    Color: var(--color-text-sub)
    Hover: color var(--color-text)
    Icon: 👁 (eye) khi type=password / 👁‍🗨 (eye-slash) khi type=text
    aria-label: "Hiện mật khẩu" / "Ẩn mật khẩu"
```

---

### 6.4 Nút Đăng Nhập — `.btn-login`

```
Text:          "Đăng nhập"
Width:         100%
Height:        50px
Background:    var(--color-secondary) — #5DBB69
Color:         white
Font:          Nunito 800, 15px, text-transform: uppercase, letter-spacing: 0.5px
Border-radius: var(--radius-full) — pill
Border:        none
Cursor:        pointer
Shadow:        0 4px 12px rgba(93,187,105,0.30)
Transition:    filter 150ms, transform 100ms, box-shadow 150ms

Hover:
  filter: brightness(1.08)
  box-shadow: 0 6px 16px rgba(93,187,105,0.38)

Active:
  transform: scale(0.97)

Loading state (.is-loading):
  opacity: 0.80
  cursor: not-allowed
  [Spinner] 20px trắng thay thế text — animation: spin 0.8s linear infinite
  Text thay bằng "Đang đăng nhập..."

Disabled (.is-disabled):
  opacity: 0.60
  cursor: not-allowed
  filter: none
```

---

### 6.5 Divider "hoặc" — `.auth-divider`

```
Display:    flex, align-items: center, gap: 12px
Margin:     4px 0

[Line]:   flex: 1, height: 1px, background: var(--color-border)
[Text]:   "hoặc"
          Font: Nunito 400, 13px
          Color: var(--color-text-sub)
          White-space: nowrap
```

---

### 6.6 Nút Đăng Nhập Google — `.btn-social-google`

```
Width:         100%
Height:        48px
Background:    white
Border:        1.5px solid var(--color-border)
Border-radius: var(--radius-full)
Display:       flex, align-items: center, justify-content: center, gap: 10px
Font:          Nunito 600, 14px
Color:         var(--color-text)
Cursor:        pointer
Transition:    border-color 150ms, box-shadow 150ms
Shadow:        0 1px 4px rgba(0,0,0,0.06)

[Google logo SVG]:
  Width: 20px, Height: 20px
  Bên trái text

[Text]: "Tiếp tục với Google"

Hover:
  border-color: var(--color-primary-light)
  box-shadow: 0 2px 8px rgba(232,154,170,0.12)

Active:
  transform: scale(0.98)
```

---

### 6.7 Link Đăng Ký — `.auth-redirect`

```
Text-align: center
Margin-top: 8px
Font:       Nunito 400, 14px
Color:      var(--color-text-sub)

[Link "Đăng ký ngay"]:
  Color:           var(--color-primary)
  Font-weight:     600
  Text-decoration: none
  Hover:           text-decoration: underline

Full text: "Chưa có tài khoản? Đăng ký ngay"
```

---

## 7. TRẠNG THÁI ERROR TOÀN FORM

Khi backend trả về `INVALID_CREDENTIALS` (HTTP 401):

```
Banner lỗi — .auth-error-banner:
  Display:       flex, align-items: flex-start, gap: 10px
  Background:    #FFEAEA
  Border:        1px solid var(--color-error)
  Border-radius: var(--radius-md)
  Padding:       12px 16px
  Margin-bottom: 16px

  [Icon]: ✕ (16px SVG), color: var(--color-error)
  [Text]: "Email hoặc mật khẩu không đúng."
          Font: Nunito 600, 13px, color: var(--color-error)

Đồng thời: cả hai input email + password thêm class .has-error (viền đỏ)
Saku-chan: chuyển sang variant `wrong` — wilt 0.3s rồi trở về idle
```

---

## 8. TRẠNG THÁI EMAIL CHƯA XÁC MINH

Khi backend trả về `EMAIL_NOT_VERIFIED` (HTTP 403):

```
Banner — .auth-verify-banner:
  Background:    var(--color-primary-bg)
  Border:        1px solid var(--color-primary-light)
  Border-radius: var(--radius-md)
  Padding:       14px 16px
  Margin-bottom: 16px

  [Text line 1]: "Tài khoản chưa xác minh email."
                 Font: Nunito 600, 13px, color: var(--color-primary-dark)
  [Text line 2]: "Kiểm tra hộp thư hoặc "
                 [Link "gửi lại email xác minh"]: color var(--color-primary), font 600
                 Font: Nunito 400, 13px
```

---

## 9. ANIMATIONS

```css
/* Khai báo trong LoginPage.css */

@keyframes sway {
  0%, 100% { transform: rotate(-4deg); }
  50%       { transform: rotate(4deg); }
}

@keyframes spin-bounce {
  0%   { transform: rotate(0deg) scale(1); }
  40%  { transform: rotate(180deg) scale(1.15); }
  70%  { transform: rotate(320deg) scale(0.95); }
  100% { transform: rotate(360deg) scale(1); }
}

@keyframes wilt {
  0%   { transform: rotate(0deg) translateY(0); }
  30%  { transform: rotate(-8deg) translateY(4px); }
  70%  { transform: rotate(-6deg) translateY(3px); }
  100% { transform: rotate(0deg) translateY(0); }
}

@keyframes petalDrift {
  0%   { transform: translateY(0px) rotate(0deg); }
  100% { transform: translateY(-10px) rotate(15deg); }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 10. ROUTE & COMPONENT

```jsx
// App.jsx
import LoginPage from './pages/auth/LoginPage';
<Route path="/login" element={<LoginPage />} />
// Nếu đã auth: <Navigate to="/dashboard" replace />
```

```jsx
// pages/auth/LoginPage.jsx
import AuthTopBar from './components/AuthTopBar';
import PasswordInput from './components/PasswordInput';
import SocialLoginButton from './components/SocialLoginButton';
import SakuChan from '../../components/SakuChan';
import './LoginPage.css';

function LoginPage() {
  // state: email, password, isLoading, error, lockRemainingMinutes
  // handleSubmit → POST /api/auth/login
  // handleGoogleLogin → GET /api/auth/oauth/google
  return (
    <div className="auth-page">
      <AuthTopBar />
      <main className="auth-main">
        {/* background petals */}
        <div className="auth-card">
          <SakuChan size="sm" variant={sakuVariant} />
          <h1 className="auth-title">Chào mừng trở lại</h1>
          <p className="auth-subtitle">...</p>
          {lockedBanner}
          {errorBanner}
          {verifyBanner}
          <form className="login-form" onSubmit={handleSubmit}>
            {/* email field */}
            {/* password field with forgot link */}
            <button className="btn-login" type="submit">Đăng nhập</button>
          </form>
          <div className="auth-divider">...</div>
          <SocialLoginButton provider="google" onClick={handleGoogleLogin} />
          <p className="auth-redirect">
            Chưa có tài khoản? <a href="/register">Đăng ký ngay</a>
          </p>
        </div>
      </main>
    </div>
  );
}
```

---

## 11. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 768px | Card 440px centered, petals hiển thị |
| < 768px | Card full-width (margin 16px mỗi bên), petals ẩn, padding card giảm xuống 24px |
| < 480px | Font size auth-title giảm xuống 20px |

---

## 12. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Heading | `<h1>` cho auth-title (1 h1 duy nhất trên trang) |
| Form labels | Mỗi input có `<label>` liên kết bằng `htmlFor` |
| Error states | `aria-invalid="true"` + `aria-describedby` trỏ vào field-error |
| Password toggle | `aria-label` thay đổi theo trạng thái |
| Loading state | `aria-busy="true"` trên form khi đang submit |
| Focus ring | `outline: 2px solid var(--color-primary)` trên mọi interactive element |
| Google button | `aria-label="Đăng nhập bằng tài khoản Google"` |
| Tab order | Email → Password → Submit → Google (tự nhiên theo DOM) |

---

## 13. OUT OF SCOPE

- ❌ Đăng nhập cho Admin/Staff (trang riêng)

- ❌ "Ghi nhớ đăng nhập" checkbox
- ❌ OAuth Facebook / Apple
- ❌ Dark mode
