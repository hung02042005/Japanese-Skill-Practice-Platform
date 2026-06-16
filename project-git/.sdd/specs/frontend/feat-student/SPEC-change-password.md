# SPEC — Đổi mật khẩu (`/settings/password`)
> **Sprint:** 1 — Foundation
> **Prefix:** `pwd-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §9.3` | **Backend ref:** `feat-auth/UC-05-change-password.md`

---

## 1. MÔ TẢ TRANG

Form 3 trường đổi mật khẩu: mật khẩu hiện tại, mật khẩu mới, xác nhận mật khẩu mới. Sau khi đổi thành công → logout + redirect `/login` (bảo mật: vô hiệu hoá token cũ). Tái sử dụng `EyeIcon` và `PasswordStrengthBar` từ `components/auth/`.

---

## 2. MOCKUP

```
┌────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="")                                         │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  ← Quay lại Hồ sơ                                            │
│                                                                │
│  Đổi Mật Khẩu                                                 │
│  ─────────────────────────────────────────────────────────── │
│                                                                │
│        ┌──────────────────────────────────────┐              │
│        │                                      │              │
│        │  Mật khẩu hiện tại *                 │              │
│        │  ┌─────────────────────────────┐[👁] │              │
│        │  │ ••••••••••                  │     │              │
│        │  └─────────────────────────────┘     │              │
│        │                                      │              │
│        │  Mật khẩu mới *                      │              │
│        │  ┌─────────────────────────────┐[👁] │              │
│        │  │ ••••••••••                  │     │              │
│        │  └─────────────────────────────┘     │              │
│        │  [███████░░░░░░░░] Trung bình         │              │
│        │                                      │              │
│        │  Xác nhận mật khẩu mới *             │              │
│        │  ┌─────────────────────────────┐[👁] │              │
│        │  │ ••••••••••                  │     │              │
│        │  └─────────────────────────────┘     │              │
│        │                                      │              │
│        │  ⚠️ Mật khẩu mới không khớp          │  (nếu lỗi)  │
│        │                                      │              │
│        │  ⓘ Sau khi đổi, bạn sẽ được đăng    │              │
│        │  xuất và cần đăng nhập lại.           │              │
│        │                                      │              │
│        │              [Đổi mật khẩu]           │              │
│        └──────────────────────────────────────┘              │
└────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/settings/
├── ChangePassword.jsx
└── ChangePassword.css
```

---

## 4. STATE

```js
const [form, setForm] = useState({
  currentPassword: '',
  newPassword:     '',
  confirmPassword: '',
});
const [showPass, setShowPass] = useState({
  current: false,
  newPass: false,
  confirm: false,
});
const [errors,   setErrors]  = useState({});    // field-level errors
const [apiError, setApiError]= useState('');    // server error message
const [isSaving, setSaving]  = useState(false);

const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

```js
// POST /api/auth/change-password
// Request:
{
  "currentPassword": "old_pass",
  "newPassword":     "New_Pass123"
}
// Response 200: { status, message: "Đổi mật khẩu thành công" }
// Response 401: { message: "Mật khẩu hiện tại không đúng" }
// Response 400: { message: "Mật khẩu mới không đủ mạnh" }

// Sau 200: dispatch(logoutThunk()) → navigate('/login')
```

---

## 6. COMPONENT BREAKDOWN

| Component | Nguồn | Notes |
|:---|:---|:---|
| `TopNav` | `components/layout/TopNav` | `activeTab=""` |
| `EyeIcon` | `components/auth/EyeIcon` | Toggle hiển thị password |
| `PasswordStrengthBar` | `components/auth/PasswordStrengthBar` | Chỉ hiển thị cho `newPassword` |
| `ToastContainer` | `components/common/Toast` | Feedback |

---

## 7. JSX STRUCTURE

```jsx
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch } from '../../store/hooks';
import { logoutThunk } from '../../store/slices/authSlice';
import TopNav from '../../components/layout/TopNav';
import EyeIcon from '../../components/auth/EyeIcon';
import PasswordStrengthBar from '../../components/auth/PasswordStrengthBar';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { changePassword } from '../../api/studentService';
import './ChangePassword.css';

function validateForm(form) {
  const errs = {};
  if (!form.currentPassword) errs.currentPassword = 'Vui lòng nhập mật khẩu hiện tại';
  if (!form.newPassword)     errs.newPassword = 'Vui lòng nhập mật khẩu mới';
  else if (form.newPassword.length < 8) errs.newPassword = 'Mật khẩu tối thiểu 8 ký tự';
  else if (!/[A-Z]/.test(form.newPassword)) errs.newPassword = 'Cần ít nhất 1 chữ hoa';
  else if (!/[a-z]/.test(form.newPassword)) errs.newPassword = 'Cần ít nhất 1 chữ thường';
  else if (!/\d/.test(form.newPassword))    errs.newPassword = 'Cần ít nhất 1 chữ số';
  if (form.newPassword && form.confirmPassword && form.newPassword !== form.confirmPassword)
    errs.confirmPassword = 'Mật khẩu xác nhận không khớp';
  if (!form.confirmPassword) errs.confirmPassword = 'Vui lòng xác nhận mật khẩu mới';
  return errs;
}

export default function ChangePassword() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [form,     setForm]   = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [showPass, setShow]   = useState({ current: false, newPass: false, confirm: false });
  const [errors,   setErrors] = useState({});
  const [apiError, setApiErr] = useState('');
  const [isSaving, setSaving] = useState(false);

  function handleChange(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
    if (errors[field]) setErrors((e) => ({ ...e, [field]: '' }));
    if (apiError) setApiErr('');
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errs = validateForm(form);
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }

    setSaving(true);
    setApiErr('');
    try {
      await changePassword({ currentPassword: form.currentPassword, newPassword: form.newPassword });
      addToast('success', 'Đổi mật khẩu thành công! Đang đăng xuất...');
      setTimeout(async () => {
        await dispatch(logoutThunk());
        navigate('/login');
      }, 1500);
    } catch (err) {
      const msg = err?.response?.data?.message ?? 'Đổi mật khẩu thất bại. Thử lại sau.';
      setApiErr(msg);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="pwd-page">
      <TopNav activeTab="" />

      <main className="pwd-body">
        <Link to="/profile" className="pwd-back-link">← Quay lại Hồ sơ</Link>
        <h1 className="pwd-title">Đổi Mật Khẩu</h1>

        <div className="pwd-card">
          {apiError && (
            <div className="pwd-api-error" role="alert">
              {apiError}
            </div>
          )}

          <form className="pwd-form" onSubmit={handleSubmit} noValidate>
            {/* Current password */}
            <div className="pwd-field">
              <label className="pwd-label" htmlFor="pwd-current">
                Mật khẩu hiện tại <span className="pwd-required">*</span>
              </label>
              <div className="pwd-input-wrap">
                <input
                  id="pwd-current"
                  className={`pwd-input${errors.currentPassword ? ' pwd-input--err' : ''}`}
                  type={showPass.current ? 'text' : 'password'}
                  value={form.currentPassword}
                  onChange={(e) => handleChange('currentPassword', e.target.value)}
                  autoComplete="current-password"
                  aria-invalid={!!errors.currentPassword}
                  aria-describedby={errors.currentPassword ? 'pwd-current-err' : undefined}
                />
                <button
                  type="button"
                  className="pwd-eye-btn"
                  onClick={() => setShow((s) => ({ ...s, current: !s.current }))}
                  aria-label={showPass.current ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  <EyeIcon open={showPass.current} />
                </button>
              </div>
              {errors.currentPassword && <span id="pwd-current-err" className="pwd-field-error">{errors.currentPassword}</span>}
            </div>

            {/* New password */}
            <div className="pwd-field">
              <label className="pwd-label" htmlFor="pwd-new">
                Mật khẩu mới <span className="pwd-required">*</span>
              </label>
              <div className="pwd-input-wrap">
                <input
                  id="pwd-new"
                  className={`pwd-input${errors.newPassword ? ' pwd-input--err' : ''}`}
                  type={showPass.newPass ? 'text' : 'password'}
                  value={form.newPassword}
                  onChange={(e) => handleChange('newPassword', e.target.value)}
                  autoComplete="new-password"
                  aria-invalid={!!errors.newPassword}
                  aria-describedby="pwd-strength-desc"
                />
                <button
                  type="button"
                  className="pwd-eye-btn"
                  onClick={() => setShow((s) => ({ ...s, newPass: !s.newPass }))}
                  aria-label={showPass.newPass ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  <EyeIcon open={showPass.newPass} />
                </button>
              </div>
              {form.newPassword && (
                <PasswordStrengthBar password={form.newPassword} id="pwd-strength-desc" />
              )}
              {errors.newPassword && <span className="pwd-field-error">{errors.newPassword}</span>}
            </div>

            {/* Confirm password */}
            <div className="pwd-field">
              <label className="pwd-label" htmlFor="pwd-confirm">
                Xác nhận mật khẩu mới <span className="pwd-required">*</span>
              </label>
              <div className="pwd-input-wrap">
                <input
                  id="pwd-confirm"
                  className={`pwd-input${errors.confirmPassword ? ' pwd-input--err' : ''}`}
                  type={showPass.confirm ? 'text' : 'password'}
                  value={form.confirmPassword}
                  onChange={(e) => handleChange('confirmPassword', e.target.value)}
                  autoComplete="new-password"
                  aria-invalid={!!errors.confirmPassword}
                  aria-describedby={errors.confirmPassword ? 'pwd-confirm-err' : undefined}
                />
                <button
                  type="button"
                  className="pwd-eye-btn"
                  onClick={() => setShow((s) => ({ ...s, confirm: !s.confirm }))}
                  aria-label={showPass.confirm ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  <EyeIcon open={showPass.confirm} />
                </button>
              </div>
              {errors.confirmPassword && <span id="pwd-confirm-err" className="pwd-field-error">{errors.confirmPassword}</span>}
            </div>

            {/* Info note */}
            <div className="pwd-info-note" role="note">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                <path d="M12 8v4M12 16h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              Sau khi đổi mật khẩu, bạn sẽ được đăng xuất và cần đăng nhập lại.
            </div>

            <button
              type="submit"
              className="pwd-submit-btn"
              disabled={isSaving}
              aria-busy={isSaving}
            >
              {isSaving && <span className="pwd-spinner pwd-spinner--white" aria-hidden="true" />}
              {isSaving ? 'Đang xử lý…' : 'Đổi mật khẩu'}
            </button>
          </form>
        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

## 8. CSS

```css
/* ===== Change Password (SakuJi Hanami Theme) ===== */

.pwd-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  font-family: var(--font-base);
}

.pwd-body {
  flex: 1;
  max-width: 520px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 32px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-sizing: border-box;
}

.pwd-back-link {
  font-size: 13px;
  color: var(--color-text-sub);
  text-decoration: none;
  font-weight: 600;
}
.pwd-back-link:hover { color: var(--color-primary); }

.pwd-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}

.pwd-card {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 28px;
}

.pwd-api-error {
  background: #FFEAEA;
  border: 1px solid var(--color-error);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  font-size: 13px;
  color: var(--color-error);
  margin-bottom: 20px;
}

.pwd-form { display: flex; flex-direction: column; gap: 20px; }

.pwd-field { display: flex; flex-direction: column; gap: 6px; }
.pwd-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: 4px;
}
.pwd-required { color: var(--color-error); }

.pwd-input-wrap { position: relative; }
.pwd-input {
  height: 44px;
  padding: 0 44px 0 14px;
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
.pwd-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232,154,170,0.15);
  background: var(--color-card);
}
.pwd-input--err { border-color: var(--color-error); background: #FEF2F2; }
.pwd-input--err:focus { box-shadow: 0 0 0 3px rgba(229,115,115,0.12); }

.pwd-eye-btn {
  position: absolute;
  right: 12px; top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--color-text-sub);
  padding: 4px;
  display: flex; align-items: center;
  transition: color var(--transition);
}
.pwd-eye-btn:hover { color: var(--color-text); }

.pwd-field-error { font-size: 12px; color: var(--color-error); }

.pwd-info-note {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: var(--color-primary-bg);
  border-radius: var(--radius-md);
  padding: 12px 14px;
  font-size: 13px;
  color: var(--color-text-sub);
  line-height: 1.5;
}
.pwd-info-note svg { flex-shrink: 0; color: var(--color-primary); margin-top: 1px; }

.pwd-submit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 44px;
  background: var(--color-secondary);
  color: white;
  border: none;
  border-radius: var(--radius-full);
  font-family: var(--font-base);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(93,187,105,0.25);
  transition: filter var(--transition), transform var(--transition);
}
.pwd-submit-btn:hover:not(:disabled) { filter: brightness(1.07); }
.pwd-submit-btn:active:not(:disabled) { transform: scale(0.97); }
.pwd-submit-btn:disabled { opacity: 0.60; cursor: not-allowed; }

.pwd-spinner { display: inline-block; width: 16px; height: 16px; border: 2.5px solid rgba(255,255,255,0.35); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 767px) {
  .pwd-body { padding: 16px 16px 32px; }
  .pwd-card { padding: 20px; }
}

@media (prefers-reduced-motion: reduce) {
  .pwd-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 9. 3 TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Loading (submit)** | Button disabled + spinner, text "Đang xử lý…" |
| **Error (wrong current password)** | `pwd-api-error` banner đỏ trên form (từ backend 401) |
| **Empty** | Không áp dụng — trang tĩnh |

---

## 10. FLOW

```
User điền form → click [Đổi mật khẩu]
→ validate client-side
  → lỗi? → hiện error messages, focus field đầu tiên có lỗi
  → ok? → POST /auth/change-password
      → 200: toast success + setTimeout 1.5s → logoutThunk → /login
      → 401: hiện api-error banner "Mật khẩu hiện tại không đúng"
      → 400: hiện api-error banner từ server message
```

---

## 11. DOMAIN RULES

- Mật khẩu mới phải ≥ 8 ký tự, có chữ hoa, thường, số — validate client-side trước khi gửi.
- Không gửi `confirmPassword` lên server — chỉ dùng để validate client-side.
- Sau khi đổi thành công → bắt buộc logout + redirect login. Không giữ session cũ.
