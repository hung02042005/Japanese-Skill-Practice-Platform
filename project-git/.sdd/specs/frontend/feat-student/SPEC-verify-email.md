# SPEC — Xác nhận Email (`/verify-email`)
> **Sprint:** 1 — Foundation
> **Prefix:** `vem-` | **activeTab:** `''` | **Guard:** Public (không cần đăng nhập)
> **Phụ thuộc:** `USER-SPEC.md §9.4` | **Backend ref:** `feat-auth/UC-02-register.md`

---

## 1. MÔ TẢ TRANG

Trang trung gian xác nhận email sau khi user click link trong email đăng ký. Đọc `?token=xxx` từ URL, gọi API verify, hiển thị 4 trạng thái: verifying / success / expired / error. Là điểm đến của link email — không có navigation phức tạp.

---

## 2. MOCKUP

```
Trạng thái "verifying":
┌──────────────────────────────────────────────────────────────┐
│                   [Logo] SakuJi                              │
│                                                              │
│          ┌─────────────────────────────────────┐            │
│          │                                     │            │
│          │     [Spinner 48px sakura pink]       │            │
│          │                                     │            │
│          │   Đang xác nhận tài khoản của bạn   │            │
│          │   Vui lòng chờ trong giây lát...    │            │
│          │                                     │            │
│          └─────────────────────────────────────┘            │
└──────────────────────────────────────────────────────────────┘

Trạng thái "success":
│          │  [SakuChan celebrate 160px]          │
│          │                                     │
│          │  🌸 Tài khoản đã được xác nhận!     │
│          │  Chào mừng bạn đến với SakuJi.      │
│          │                                     │
│          │     [Đăng nhập ngay →]              │

Trạng thái "expired":
│          │  [SakuChan thinking 140px]           │
│          │                                     │
│          │  ⏰ Link xác nhận đã hết hạn         │
│          │  Link có hiệu lực trong 24 giờ.     │
│          │                                     │
│          │     [Gửi lại email xác nhận]        │
│          │     (Nhập email để gửi lại)          │

Trạng thái "error":
│          │  [SakuChan wrong 140px]              │
│          │                                     │
│          │  ❌ Link không hợp lệ                │
│          │  Link đã được dùng hoặc không đúng.  │
│          │                                     │
│          │    [← Về trang đăng nhập]            │
```

---

## 3. FILE CẦN TẠO

```
pages/verify-email/
├── VerifyEmail.jsx    (file có sẵn, cần refactor theo spec này)
└── VerifyEmail.css    (file có sẵn, cần update)
```

---

## 4. STATE

```js
// 4 states
type VerifyState = 'verifying' | 'success' | 'expired' | 'error';

const [state,       setState]   = useState('verifying');
const [email,       setEmail]   = useState('');           // cho form resend
const [isSending,   setSending] = useState(false);
const [resendDone,  setResent]  = useState(false);
const [searchParams] = useSearchParams();
const token = searchParams.get('token');
```

---

## 5. API CALLS

```js
// 1. Verify token (on mount)
// GET /api/auth/verify-email?token=xxx
// Response 200: { message: "Xác nhận thành công" }
// Response 400: { code: "TOKEN_EXPIRED" | "TOKEN_INVALID" }

// 2. Resend verification email
// POST /api/auth/resend-verification
// Request: { "email": "user@example.com" }
// Response 200: { message: "Email đã được gửi" }
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AppLogo from '../../components/common/AppLogo';
import SakuChan from '../../components/auth/SakuChan';
import { verifyEmail, resendVerification } from '../../api/authService';
import './VerifyEmail.css';

const STATES = {
  verifying: {
    title: null,
    subtitle: null,
  },
  success: {
    title: '🌸 Tài khoản đã được xác nhận!',
    subtitle: 'Chào mừng bạn đến với SakuJi. Bắt đầu hành trình học tiếng Nhật ngay!',
    mascot: 'celebrate',
  },
  expired: {
    title: '⏰ Link xác nhận đã hết hạn',
    subtitle: 'Link xác nhận có hiệu lực trong 24 giờ. Vui lòng yêu cầu gửi lại.',
    mascot: 'thinking',
  },
  error: {
    title: '❌ Link không hợp lệ',
    subtitle: 'Link đã được sử dụng hoặc không đúng định dạng.',
    mascot: 'wrong',
  },
};

export default function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [state,     setState]  = useState('verifying');
  const [email,     setEmail]  = useState('');
  const [isSending, setSending]= useState(false);
  const [resendDone,setResent] = useState(false);

  useEffect(() => {
    if (!token) { setState('error'); return; }
    verifyEmail(token)
      .then(() => setState('success'))
      .catch((err) => {
        const code = err?.response?.data?.code;
        setState(code === 'TOKEN_EXPIRED' ? 'expired' : 'error');
      });
  }, [token]);

  async function handleResend(e) {
    e.preventDefault();
    if (!email) return;
    setSending(true);
    try {
      await resendVerification(email);
      setResent(true);
    } catch {
      /* ignore — hiển thị thành công luôn để tránh user enumeration */
      setResent(true);
    } finally {
      setSending(false);
    }
  }

  const cfg = STATES[state];

  return (
    <div className="vem-page">
      <div className="vem-topbar">
        <AppLogo size={28} />
        <span className="vem-brand">SakuJi</span>
      </div>

      <div className="vem-card">
        {/* Verifying */}
        {state === 'verifying' && (
          <div className="vem-state vem-state--verifying">
            <div className="vem-spinner-lg" aria-label="Đang xác nhận" role="status" />
            <p className="vem-verifying-text">Đang xác nhận tài khoản của bạn...</p>
          </div>
        )}

        {/* Success / Expired / Error */}
        {state !== 'verifying' && (
          <div className="vem-state">
            <SakuChan variant={cfg.mascot} size={160} />
            <h1 className="vem-title">{cfg.title}</h1>
            <p className="vem-subtitle">{cfg.subtitle}</p>

            {state === 'success' && (
              <Link to="/login" className="vem-btn vem-btn--primary">
                Đăng nhập ngay →
              </Link>
            )}

            {state === 'expired' && (
              resendDone ? (
                <div className="vem-resent-note" role="status">
                  ✅ Email đã được gửi! Kiểm tra hộp thư của bạn.
                </div>
              ) : (
                <form className="vem-resend-form" onSubmit={handleResend}>
                  <input
                    id="vem-email"
                    type="email"
                    className="vem-input"
                    placeholder="Nhập email đăng ký..."
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    aria-label="Email đăng ký"
                  />
                  <button
                    type="submit"
                    className="vem-btn vem-btn--primary"
                    disabled={isSending}
                    aria-busy={isSending}
                  >
                    {isSending && <span className="vem-spinner-sm" aria-hidden="true" />}
                    {isSending ? 'Đang gửi…' : 'Gửi lại email xác nhận'}
                  </button>
                </form>
              )
            )}

            {state === 'error' && (
              <Link to="/login" className="vem-btn vem-btn--ghost">
                ← Về trang đăng nhập
              </Link>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Verify Email (SakuJi Hanami Theme) ===== */

.vem-page {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  font-family: var(--font-base);
}

.vem-topbar {
  width: 100%;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.vem-brand { font-size: 20px; font-weight: 800; color: var(--color-primary); }

.vem-card {
  background: var(--color-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  padding: 48px 40px;
  width: 100%;
  max-width: 440px;
  margin: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vem-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
  width: 100%;
}

/* Spinner large */
.vem-spinner-lg {
  width: 48px; height: 48px;
  border: 4px solid var(--color-primary-light);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.vem-verifying-text { font-size: 15px; color: var(--color-text-sub); margin: 0; }

.vem-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}
.vem-subtitle {
  font-size: 14px;
  color: var(--color-text-sub);
  line-height: 1.6;
  max-width: 320px;
  margin: 0;
}

/* Buttons */
.vem-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 44px;
  padding: 0 28px;
  border-radius: var(--radius-full);
  font-family: var(--font-base);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
  border: none;
  transition: filter var(--transition), transform var(--transition);
}
.vem-btn--primary { background: var(--color-secondary); color: white; box-shadow: 0 2px 8px rgba(93,187,105,0.25); }
.vem-btn--primary:hover { filter: brightness(1.07); }
.vem-btn--primary:disabled { opacity: 0.60; cursor: not-allowed; }
.vem-btn--ghost { background: transparent; border: 1.5px solid var(--color-border); color: var(--color-text-sub); }
.vem-btn--ghost:hover { color: var(--color-text); background: var(--color-bg); }

/* Resend form */
.vem-resend-form { display: flex; flex-direction: column; gap: 10px; width: 100%; }
.vem-input {
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
.vem-input:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(232,154,170,0.15); background: var(--color-card); }

.vem-resent-note {
  background: var(--color-secondary-bg);
  border: 1px solid var(--color-secondary);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  font-size: 14px;
  color: var(--color-secondary);
  font-weight: 600;
}

/* Spinner small */
.vem-spinner-sm { display: inline-block; width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.35); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 480px) { .vem-card { padding: 32px 20px; } }
@media (prefers-reduced-motion: reduce) { .vem-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 8. 3 TRẠNG THÁI

| Trạng thái | Mô tả |
|:---|:---|
| `verifying` | Spinner, chờ API verify |
| `success` | Mascot celebrate, link đến /login |
| `expired` | Form gửi lại email |
| `error` | Link về /login |

---

## 9. DOMAIN RULES

- Resend luôn trả thành công (kể cả email không tồn tại) để tránh user enumeration.
- Token chỉ dùng 1 lần — sau khi verify xong, gọi lại sẽ trả `TOKEN_INVALID`.
- Không tự redirect vào `/dashboard` sau verify — user phải đăng nhập lại.
