# SPEC — Trang Đăng Ký (Register)
>
> **Feature ID:** `feat-auth` | **Page:** `Register`
> **Route:** `/register` (public, redirect về `/dashboard` nếu đã auth)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-05-31
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Backend ref:** `feat-auth/UC-02-register.md`

---

## 1. TỔNG QUAN TRANG

Trang đăng ký tài khoản học viên mới. Mục tiêu: **tạo tài khoản nhanh, hướng dẫn xác minh email rõ ràng**. Thiết kế đơn giản, không sidebar, một card trung tâm trên nền washi.

**Hai luồng đăng ký:**

1. **Email + Mật khẩu** — form điền thông tin
2. **Google OAuth** — tự động tạo tài khoản (xử lý qua UC-01 phía backend)

**Cấu trúc trang (3 trạng thái):**

```
[State 1] Form đăng ký     — mặc định khi vào trang
[State 2] Thông báo thành  — sau khi submit thành công, chờ xác minh email
[State 3] Xác minh thành   — route /verify-email?token=... sau khi bấm link email
```

**File structure:** (dùng chung với LoginPage, xem SPEC-login.md)

```
apps/frontend/src/
├── pages/
│   └── auth/
│       ├── RegisterPage.jsx       ← page root (state 1 + 2)
│       ├── RegisterPage.css
│       └── VerifyEmailPage.jsx    ← state 3 (route /verify-email)
│       └── VerifyEmailPage.css
```

---

## 2. DESIGN TOKENS ÁP DỤNG

```css
/* Giống LoginPage — xem SPEC-login.md § 2 */
/* Bổ sung dành riêng cho Register: */
--color-success-bg: var(--color-secondary-bg); /* #F4FBF5 */
--color-success:    var(--color-secondary);     /* #5DBB69 */
```

---

## 3. LAYOUT TỔNG THỂ

Giống LoginPage (AuthTopBar + auth-main centered), khác ở kích thước card và nội dung.

```
AuthCard width: 100%, max-width: 480px   ← rộng hơn login (440px) do có nhiều field hơn
```

---

## 4. AUTH TOP BAR

Dùng chung component `AuthTopBar` với LoginPage — xem SPEC-login.md § 4.

---

## 5. BACKGROUND PETALS

Dùng chung logic với LoginPage — xem SPEC-login.md § 5.

---

## 6. STATE 1 — FORM ĐĂNG KÝ

### 6.1 Mascot + Header

**Saku-chan — `.auth-mascot`**

```
Size:      80px (sm variant)
State:     idle (gentle sway) khi trang vừa load
           happy (spin-bounce) khi submit thành công và chuyển state 2
Margin:    0 auto 16px
```

**Tiêu đề — `.auth-title`**

```
Text:       "Tạo tài khoản mới"
Font:       Nunito 700, 24px (heading-lg)
Color:      var(--color-text)
Text-align: center
Margin-bottom: 4px
```

**Mô tả phụ — `.auth-subtitle`**

```
Text:       "Bắt đầu hành trình tiếng Nhật của bạn miễn phí"
Font:       Nunito 400, 14px (body-md)
Color:      var(--color-text-sub)
Text-align: center
Margin-bottom: 28px
```

---

### 6.2 Form Đăng Ký — `.register-form`

```
Display:        flex, flex-direction: column, gap: 20px
```

**Field Họ và tên — `.form-field`**

```
[Label]:
  Text: "Họ và tên"
  Font: Nunito 600, 14px
  Color: var(--color-text)
  Margin-bottom: 6px

[Input]:
  Type:        text
  Placeholder: "Nguyễn Văn A"
  autocomplete: name
  Cùng style với form-input (xem SPEC-login.md § 6.3)

[Error message]: Nunito 400 12px, color var(--color-error)
```

**Field Email — `.form-field`**

```
[Label]: "Email"
[Input]:
  Type:        email
  Placeholder: "email@example.com"
  autocomplete: email
  Cùng style với form-input

[Error message]: hiển thị khi:
  - Email rỗng: "Email là bắt buộc"
  - Sai format: "Email không hợp lệ"
  - Đã tồn tại: "Email này đã được sử dụng. Bạn có muốn đăng nhập không?"
                + [Link "Đăng nhập"] → /login, color var(--color-primary)
```

**Field Mật khẩu — `.form-field`**

```
[Label]: "Mật khẩu"

[PasswordInput]: Dùng component PasswordInput (xem SPEC-login.md § 6.3)
  Placeholder: "Tối thiểu 8 ký tự"
  autocomplete: new-password

[Password strength bar — .password-strength]:
  Hiện ra ngay dưới input khi user bắt đầu gõ (length > 0)
  Transition: width 300ms ease, background 300ms ease

  Layout:
    Bar track: height 4px, background var(--color-border), border-radius var(--radius-full), margin-top: 6px
    Bar fill:  height 100%, border-radius var(--radius-full)

  4 mức độ:
    weak   (1–3 điểm): fill 25%, background #E57373 (đỏ)
    fair   (4–5 điểm): fill 50%, background var(--color-warning) (cam)
    good   (6–7 điểm): fill 75%, background var(--color-accent) (vàng)
    strong (8+ điểm):  fill 100%, background var(--color-secondary) (xanh)

  Điểm tính:
    +2 nếu length ≥ 8
    +2 nếu có ít nhất 1 chữ hoa
    +2 nếu có ít nhất 1 chữ số
    +2 nếu có ít nhất 1 ký tự đặc biệt

  Label bên phải bar:
    weak → "Yếu",  fair → "Tạm được",  good → "Khá",  strong → "Mạnh"
    Font: Nunito 600, 11px, màu tương ứng với bar fill

[Hint text dưới bar]:
  "Tối thiểu 8 ký tự, bao gồm ít nhất 1 chữ hoa và 1 chữ số"
  Font: Nunito 400, 12px, color var(--color-text-sub)
  Margin-top: 4px
  Ẩn khi đã đạt đủ điều kiện (strength ≥ good)
```

**Field Xác nhận mật khẩu — `.form-field`**

```
[Label]: "Xác nhận mật khẩu"

[PasswordInput]:
  Placeholder: "Nhập lại mật khẩu"
  autocomplete: new-password

[Validation inline]:
  Khi confirmPassword không rỗng:
    Khớp → icon ✓ màu var(--color-secondary) xuất hiện bên phải input
    Không khớp → input thêm .has-error + message "Mật khẩu xác nhận không khớp"
```

---

### 6.3 Nút Đăng Ký — `.btn-register`

```
Text:          "Tạo tài khoản"
Style:         Giống .btn-login (xem SPEC-login.md § 6.4)
  background:  var(--color-secondary)
  width:       100%
  height:      50px
  border-radius: var(--radius-full)
  font:        Nunito 800, 15px uppercase

Loading state:
  Text: "Đang tạo tài khoản..."
  Spinner trắng 20px

Disabled khi:
  - Bất kỳ field nào rỗng
  - Mật khẩu không đạt weak+ (strength = 0)
  - Confirm password không khớp
```

---

### 6.4 Divider + Google Button

Giống LoginPage — xem SPEC-login.md § 6.5 và § 6.6.

```
Google button text: "Đăng ký với Google"
```

---

### 6.5 Link Đăng Nhập — `.auth-redirect`

```
Text: "Đã có tài khoản? Đăng nhập"
[Link "Đăng nhập"]: href="/login", color var(--color-primary), font 600
Text-align: center, font: Nunito 400, 14px, color var(--color-text-sub)
```

---

### 6.6 Điều Khoản (Terms) — `.auth-terms`

```
Text:       "Bằng cách đăng ký, bạn đồng ý với "
            [Điều khoản dịch vụ] và [Chính sách bảo mật]
Font:       Nunito 400, 12px
Color:      var(--color-text-sub)
Text-align: center
Margin-top: 8px

Links:
  Color:           var(--color-primary)
  Text-decoration: underline
  Font-weight:     600
```

---

## 7. STATE 2 — THÔNG BÁO GỬI EMAIL THÀNH CÔNG

Hiển thị sau khi POST `/api/auth/register` trả về HTTP 201. Toàn bộ form được **thay thế** bằng màn hình thông báo trong cùng AuthCard — không điều hướng trang.

```
Transition: fade in — opacity 0 → 1, 300ms ease
```

```
┌─────────────────────────────────────┐
│                                     │
│     [Saku-chan — happy 160px]       │
│                                     │
│   Kiểm tra hộp thư của bạn!        │
│                                     │
│  Chúng tôi đã gửi email xác minh   │
│  đến  {email}                       │
│                                     │
│  Nhấp vào liên kết trong email để  │
│  kích hoạt tài khoản.              │
│                                     │
│  [Không nhận được? Gửi lại]        │
│                                     │
│  [← Quay lại đăng nhập]            │
│                                     │
└─────────────────────────────────────┘
```

**Saku-chan — `.auth-mascot`**

```
Size:  160px (md variant)
State: happy → sau 0.6s spin-bounce, chuyển về idle (sway)
```

**Tiêu đề — `.auth-success-title`**

```
Text:       "Kiểm tra hộp thư của bạn!"
Font:       Nunito 700, 24px
Color:      var(--color-text)
Text-align: center
Margin-top: 16px, Margin-bottom: 8px
```

**Mô tả — `.auth-success-desc`**

```
Line 1: "Chúng tôi đã gửi email xác minh đến"
Line 2: {email} — Nunito 700, màu var(--color-primary)
Line 3: "Nhấp vào liên kết trong email để kích hoạt tài khoản."

Font line 1 & 3: Nunito 400, 14px, color var(--color-text-sub)
Text-align: center
Line-height: 1.7
Max-width: 340px, margin: 0 auto 24px
```

**Resend button — `.btn-resend`**

```
Text:          "Không nhận được email? Gửi lại"
Background:    transparent
Border:        1.5px solid var(--color-border)
Border-radius: var(--radius-full)
Font:          Nunito 600, 14px
Color:         var(--color-text-sub)
Padding:       11px 28px
Width:         100%
Cursor:        pointer
Hover:         border-color var(--color-primary), color var(--color-primary)

Disabled state (sau khi gửi, cooldown 60s):
  opacity: 0.50, cursor: not-allowed
  Text: "Gửi lại (chờ {n}s)" — countdown hiển thị

Rate-limit state (backend 429):
  Hiển thị inline message: "Đã gửi quá nhiều lần. Vui lòng thử lại sau."
  Font: Nunito 400, 12px, color var(--color-warning)
  Margin-top: 8px
```

**Link về đăng nhập**

```
Text: "← Quay lại đăng nhập"
Font: Nunito 600, 14px
Color: var(--color-text-sub)
Hover: color var(--color-text)
Text-align: center
Display: block, margin-top: 16px
Link: href="/login"
```

---

## 8. STATE 3 — TRANG XÁC MINH EMAIL

**Route:** `/verify-email?token={token}` — component riêng `VerifyEmailPage.jsx`

Khi component mount: tự động gọi `POST /api/auth/verify-email {token}`.
Không yêu cầu người dùng nhấn thêm nút.

### 8.1 Trạng thái Loading (đang xác minh)

```
┌─────────────────────────────┐
│  [Spinner 40px — sakura]    │
│  "Đang xác minh..."         │
└─────────────────────────────┘

Spinner: LoadingSpinner md (40px), color var(--color-primary)
Text: Nunito 400, 14px, color var(--color-text-sub)
Text-align: center, margin-top: 16px
```

### 8.2 Trạng thái Thành Công

```
┌───────────────────────────────────────┐
│                                       │
│    [Saku-chan — celebrate 160px]      │
│    [confetti petals SVG]              │
│                                       │
│   Xác minh thành công!               │
│                                       │
│  Tài khoản của bạn đã được kích      │
│  hoạt. Hãy bắt đầu học thôi!        │
│                                       │
│  [● Đăng nhập ngay]                  │
│                                       │
└───────────────────────────────────────┘
```

**Saku-chan:**

```
Size:  160px (md)
State: celebrate — spin 0.8s ease + falling petals SVG (6–8 petals, color var(--color-primary-light))
Sau 1.5s celebrate: chuyển sang happy (idle)
```

**Tiêu đề:**

```
Text:  "Xác minh thành công! 🌸"
Font:  Nunito 700, 26px
Color: var(--color-text)
Text-align: center
```

**Mô tả:**

```
Text:  "Tài khoản của bạn đã được kích hoạt. Hãy bắt đầu học thôi!"
Font:  Nunito 400, 15px
Color: var(--color-text-sub)
Text-align: center, line-height: 1.7
Margin-bottom: 28px
```

**CTA Button:**

```
Text:          "Đăng nhập ngay"
Style:         Giống btn-primary (xem SPEC-login.md § 6.4)
Width:         100%
onClick:       navigate('/login')
Auto redirect: Sau 3 giây tự động chuyển về /login
               Countdown hiển thị bên dưới button:
               "Tự động chuyển hướng sau {n} giây..."
               Font: Nunito 400, 12px, color var(--color-text-sub), text-align: center
```

### 8.3 Trạng thái Lỗi (token hết hạn / không hợp lệ)

```
┌─────────────────────────────────────────┐
│                                         │
│  [Saku-chan — wrong/thinking 160px]    │
│                                         │
│  Liên kết không hợp lệ                 │
│                                         │
│  Liên kết xác minh đã hết hạn hoặc    │
│  không hợp lệ.                          │
│                                         │
│  [Gửi lại email xác minh]              │
│  [← Về trang đăng nhập]               │
│                                         │
└─────────────────────────────────────────┘
```

**Saku-chan:** size md (160px), variant `thinking` (peek 2s ease infinite)

**Tiêu đề:**

```
Text:  "Liên kết không hợp lệ"
Font:  Nunito 700, 22px
Color: var(--color-text)
Text-align: center
```

**Mô tả:**

```
Text:  "Liên kết xác minh đã hết hạn hoặc không hợp lệ.
        Vui lòng yêu cầu gửi lại email xác minh."
Font:  Nunito 400, 14px, color var(--color-text-sub), text-align: center
```

**Resend link:**

```
Hiển thị input email để người dùng nhập lại:
  [Input email placeholder "Nhập email đăng ký của bạn"]
  [Nút "Gửi lại email xác minh"] — style btn-secondary (outline, pink)
    → POST /api/auth/resend-verification {email}

Hoặc link về đăng nhập: "← Về trang đăng nhập" → href="/login"
```

---

## 9. TRẠNG THÁI LỖI FORM

**Email đã tồn tại (HTTP 409 `EMAIL_EXISTS`):**

```
Field email thêm .has-error
Error message: "Email này đã được sử dụng. "
               [Link "Đăng nhập ngay?"] → /login
               Font: 12px, color var(--color-error)
               Link: color var(--color-primary), font 600
```

**Validation errors (HTTP 400/422):**

```
Mỗi field có error tương ứng thêm .has-error + hiển thị field-error message
Không có global error banner — lỗi gắn trực tiếp vào field
Saku-chan chuyển variant `wrong` → wilt 0.3s rồi về idle
```

---

## 10. ANIMATIONS

```css
/* Khai báo trong RegisterPage.css */
/* Tái sử dụng từ LoginPage.css: sway, spin-bounce, wilt, petalDrift */

@keyframes celebrate-petals {
  0%   { opacity: 1; transform: translateY(0) rotate(0deg); }
  100% { opacity: 0; transform: translateY(80px) rotate(40deg); }
}

/* Falling petals trong celebrate state */
.saku-celebrate-petals .petal {
  position: absolute;
  width: 12px; height: 8px;
  background: var(--color-primary-light);
  border-radius: 50% 0 50% 0;
  animation: celebrate-petals 1s ease-out forwards;
}
.saku-celebrate-petals .petal:nth-child(1) { animation-delay: 0s;    left: 20%; }
.saku-celebrate-petals .petal:nth-child(2) { animation-delay: 0.1s;  left: 40%; }
.saku-celebrate-petals .petal:nth-child(3) { animation-delay: 0.2s;  left: 60%; }
.saku-celebrate-petals .petal:nth-child(4) { animation-delay: 0.05s; left: 30%; }
.saku-celebrate-petals .petal:nth-child(5) { animation-delay: 0.15s; left: 70%; }
.saku-celebrate-petals .petal:nth-child(6) { animation-delay: 0.25s; left: 50%; }

@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 11. ROUTE & COMPONENT

```jsx
// App.jsx
import RegisterPage from './pages/auth/RegisterPage';
import VerifyEmailPage from './pages/auth/VerifyEmailPage';

<Route path="/register"     element={<RegisterPage />} />
<Route path="/verify-email" element={<VerifyEmailPage />} />
// Nếu đã auth: <Navigate to="/dashboard" replace />
```

```jsx
// pages/auth/RegisterPage.jsx
import AuthTopBar from './components/AuthTopBar';
import PasswordInput from './components/PasswordInput';
import SocialLoginButton from './components/SocialLoginButton';
import SakuChan from '../../components/SakuChan';
import './RegisterPage.css';

function RegisterPage() {
  const [step, setStep] = useState('form'); // 'form' | 'success'
  // state: fullName, email, password, confirmPassword
  // state: errors, isLoading, registeredEmail
  // handleSubmit → POST /api/auth/register → onSuccess: setStep('success')
  // handleResend → POST /api/auth/resend-verification
  // handleGoogleRegister → GET /api/auth/oauth/google

  if (step === 'success') return <SuccessScreen email={registeredEmail} />;

  return (
    <div className="auth-page">
      <AuthTopBar />
      <main className="auth-main">
        <div className="auth-card">
          <SakuChan size="sm" variant={sakuVariant} />
          <h1 className="auth-title">Tạo tài khoản mới</h1>
          <p className="auth-subtitle">...</p>
          <form className="register-form" onSubmit={handleSubmit}>
            {/* fullName field */}
            {/* email field */}
            {/* password field + strength bar */}
            {/* confirmPassword field */}
            <button className="btn-register" type="submit">Tạo tài khoản</button>
          </form>
          <div className="auth-divider">...</div>
          <SocialLoginButton provider="google" onClick={handleGoogleRegister} />
          <p className="auth-redirect">Đã có tài khoản? <a href="/login">Đăng nhập</a></p>
          <p className="auth-terms">...</p>
        </div>
      </main>
    </div>
  );
}
```

```jsx
// pages/auth/VerifyEmailPage.jsx
// - Đọc token từ useSearchParams()
// - useEffect: POST /api/auth/verify-email {token} ngay khi mount
// - state: 'loading' | 'success' | 'error'
// - Auto-redirect về /login sau 3s khi success
```

---

## 12. RESPONSIVE

| Breakpoint | Thay đổi |
|:---|:---|
| ≥ 768px | Card 480px centered, petals hiển thị |
| < 768px | Card full-width (margin 16px mỗi bên), petals ẩn, padding card giảm xuống 24px |
| < 480px | Font auth-title giảm xuống 20px; strength bar text ẩn đi (chỉ hiện màu) |

---

## 13. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Heading | `<h1>` cho auth-title, duy nhất trên trang |
| Form labels | Mỗi input có `<label>` liên kết bằng `htmlFor` |
| Error states | `aria-invalid="true"` + `aria-describedby` trỏ vào field-error |
| Password strength | `aria-live="polite"` trên strength label (đọc khi thay đổi) |
| Confirm match icon | `aria-label="Mật khẩu khớp"` / `aria-label="Mật khẩu không khớp"` |
| Loading state | `aria-busy="true"` trên form khi đang submit |
| Auto-redirect | `aria-live="assertive"` trên countdown text |
| Focus management | Sau khi chuyển sang state 2 (success), focus vào `.auth-success-title` |
| Tab order | Họ tên → Email → Mật khẩu → Xác nhận → Submit → Google (tự nhiên) |

---

## 14. OUT OF SCOPE

- ❌ Đăng ký số điện thoại / OTP — Phase 2
- ❌ reCAPTCHA — Phase 2
- ❌ Admin/Staff tạo tài khoản — xem `feat-system-admin`
- ❌ Dark mode
- ❌ Đăng ký qua Facebook / Apple
