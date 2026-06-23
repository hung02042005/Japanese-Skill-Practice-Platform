# SPEC — Profile (`/profile`)
>
> **Sprint:** 1 — Foundation
> **Prefix:** `prf-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §9.2` | **Backend ref:** `feat-auth/UC-04-user-profile.md`

---

## 1. MÔ TẢ TRANG

Trang hồ sơ cá nhân cho phép học viên xem và chỉnh sửa thông tin cá nhân: tên, số điện thoại, ngày sinh, bio. Avatar có thể upload. Email và JLPT level là read-only. Liên kết sang `/settings/password`.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="")                                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ← Quay lại Dashboard                                           │
│                                                                  │
│  Hồ Sơ Của Tôi                                                  │
│  ─────────────────────────────────────────────────────────────  │
│                                                                  │
│  ┌─────────────────────┐  ┌─────────────────────────────────┐  │
│  │                     │  │  Họ và tên *                    │  │
│  │   [Avatar 96px]     │  │  ┌─────────────────────────┐   │  │
│  │                     │  │  │ Nguyễn Văn A            │   │  │
│  │ [📷 Thay ảnh]       │  │  └─────────────────────────┘   │  │
│  │                     │  │                                 │  │
│  │ Nguyễn Văn A        │  │  Số điện thoại                  │  │
│  │ student@email.com   │  │  ┌─────────────────────────┐   │  │
│  │ [N5] JLPT N5        │  │  │ 0912345678              │   │  │
│  │                     │  │  └─────────────────────────┘   │  │
│  │ Thành viên từ       │  │                                 │  │
│  │ 01/01/2026          │  │  Ngày sinh                      │  │
│  │                     │  │  ┌─────────────────────────┐   │  │
│  └─────────────────────┘  │  │ 01/01/2000              │   │  │
│                            │  └─────────────────────────┘   │  │
│                            │                                 │  │
│                            │  Bio (tùy chọn)                 │  │
│                            │  ┌─────────────────────────┐   │  │
│                            │  │ Tôi đang học N5 để...   │   │  │
│                            │  │                         │   │  │
│                            │  └─────────────────────────┘   │  │
│                            │  0/500 ký tự                    │  │
│                            │                                 │  │
│                            │  Email (không thể thay đổi)     │  │
│                            │  student@email.com (disabled)   │  │
│                            │                                 │  │
│                            │  Đổi mật khẩu →                 │  │
│                            │                                 │  │
│                            │        [Lưu thay đổi]           │  │
│                            └─────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/profile/
├── Profile.jsx
└── Profile.css
```

---

## 4. STATE

```js
// Redux
const { user } = useAppSelector((s) => s.auth);

// Local form state
const [form, setForm] = useState({
  fullName:    '',
  phone:       '',
  dateOfBirth: '',
  bio:         '',
});
const [errors,       setErrors]  = useState({});       // { fullName: 'Tên không được trống', ... }
const [isSaving,     setSaving]  = useState(false);
const [isLoading,    setLoading] = useState(true);
const [avatarFile,   setAvatar]  = useState(null);     // File object
const [avatarPreview,setPreview] = useState(null);     // blob URL
const { toasts, addToast, removeToast } = useToast();
```

**Khởi tạo form từ Redux/API:**

```js
useEffect(() => {
  if (user) {
    setForm({
      fullName:    user.fullName    ?? '',
      phone:       user.phone       ?? '',
      dateOfBirth: user.dateOfBirth ?? '',
      bio:         user.bio         ?? '',
    });
    setPreview(user.avatarUrl ?? null);
    setLoading(false);
  }
}, [user]);
```

---

## 5. API CALLS

```js
// Lưu profile
// PUT /api/students/me
// Request body:
{
  "fullName":    "Nguyễn Văn A",
  "phone":       "0912345678",
  "dateOfBirth": "2000-01-01",
  "bio":         "Tôi đang học N5..."
}
// Response 200: { status, message, data: { ...userDto } }

// Upload avatar (nếu có file mới)
// POST /api/students/me/avatar
// Content-Type: multipart/form-data
// Body: FormData { avatar: File }
// Response 200: { data: { avatarUrl: "string" } }
```

**Thứ tự submit:**

1. Nếu có `avatarFile` → `POST avatar` trước
2. Sau đó `PUT /api/students/me`
3. Dispatch update Redux state
4. Toast success

---

## 6. COMPONENT BREAKDOWN

| Component | Nguồn | Notes |
|:---|:---|:---|
| `TopNav` | `components/layout/TopNav` | `activeTab=""` |
| `UserAvatar` | `components/common/UserAvatar` | Hiển thị ảnh hiện tại |
| `JlptBadge` | `components/common/Badges` | Hiển thị level (read-only) |
| `EyeIcon` | `components/auth/EyeIcon` | Không cần ở đây |
| `PasswordStrengthBar` | `components/auth/PasswordStrengthBar` | Không cần ở đây |
| `useToast` + `ToastContainer` | `components/common/Toast` | Feedback |

---

## 7. JSX STRUCTURE

```jsx
import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAppSelector, useAppDispatch } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { UserAvatar } from '../../components/common/UserAvatar';
import { JlptBadge } from '../../components/common/Badges';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { updateProfile, uploadAvatar } from '../../api/studentService';
import './Profile.css';

function validate(form) {
  const errs = {};
  if (!form.fullName.trim()) errs.fullName = 'Tên không được để trống';
  if (form.fullName.trim().length > 100) errs.fullName = 'Tên tối đa 100 ký tự';
  if (form.phone && !/^\d{9,15}$/.test(form.phone)) errs.phone = 'Số điện thoại không hợp lệ';
  if (form.bio.length > 500) errs.bio = 'Bio tối đa 500 ký tự';
  return errs;
}

export default function Profile() {
  const { user }   = useAppSelector((s) => s.auth);
  const dispatch   = useAppDispatch();
  const { toasts, addToast, removeToast } = useToast();

  const [form,         setForm]    = useState({ fullName: '', phone: '', dateOfBirth: '', bio: '' });
  const [errors,       setErrors]  = useState({});
  const [isSaving,     setSaving]  = useState(false);
  const [isLoading,    setLoading] = useState(true);
  const [avatarFile,   setAvatar]  = useState(null);
  const [avatarPreview,setPreview] = useState(null);

  useEffect(() => {
    if (user) {
      setForm({ fullName: user.fullName ?? '', phone: user.phone ?? '', dateOfBirth: user.dateOfBirth ?? '', bio: user.bio ?? '' });
      setPreview(user.avatarUrl ?? null);
      setLoading(false);
    }
  }, [user]);

  function handleChange(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
    if (errors[field]) setErrors((e) => ({ ...e, [field]: '' }));
  }

  function handleAvatarChange(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setAvatar(file);
    setPreview(URL.createObjectURL(file));
  }

  async function handleSave() {
    const errs = validate(form);
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }

    setSaving(true);
    try {
      if (avatarFile) {
        await uploadAvatar(avatarFile);
      }
      const updated = await updateProfile(form);
      dispatch(/* updateUserProfile(updated) */);
      addToast('success', 'Hồ sơ đã được cập nhật!');
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Không thể lưu. Thử lại sau.');
    } finally {
      setSaving(false);
    }
  }

  if (isLoading) return <div className="prf-page"><TopNav /><div className="prf-body"><div className="prf-skel" aria-hidden="true" /></div></div>;

  return (
    <div className="prf-page">
      <TopNav activeTab="" />

      <main className="prf-body">
        <Link to="/dashboard" className="prf-back-link">← Quay lại Dashboard</Link>
        <h1 className="prf-title">Hồ Sơ Của Tôi</h1>

        <div className="prf-layout">
          {/* Sidebar */}
          <aside className="prf-sidebar">
            <div className="prf-avatar-wrap">
              <UserAvatar src={avatarPreview} name={form.fullName} size={96} />
              <label className="prf-avatar-btn" htmlFor="avatar-upload" aria-label="Thay ảnh đại diện">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
                  <circle cx="12" cy="13" r="4" stroke="currentColor" strokeWidth="2"/>
                </svg>
                Thay ảnh
              </label>
              <input id="avatar-upload" type="file" accept="image/*" className="prf-sr-only" onChange={handleAvatarChange} />
            </div>
            <div className="prf-sidebar-name">{form.fullName || '—'}</div>
            <div className="prf-sidebar-email">{user?.email}</div>
            <JlptBadge level={user?.jlptLevel ?? 'N5'} />
            <div className="prf-sidebar-joined">Thành viên từ {user?.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : '—'}</div>
          </aside>

          {/* Form */}
          <section className="prf-form-section">
            {/* fullName */}
            <div className="prf-field">
              <label className="prf-label" htmlFor="prf-fullname">Họ và tên <span className="prf-required">*</span></label>
              <input
                id="prf-fullname"
                className={`prf-input${errors.fullName ? ' prf-input--err' : ''}`}
                type="text"
                value={form.fullName}
                onChange={(e) => handleChange('fullName', e.target.value)}
                placeholder="Nhập họ và tên..."
                aria-invalid={!!errors.fullName}
                aria-describedby={errors.fullName ? 'prf-fullname-err' : undefined}
              />
              {errors.fullName && <span id="prf-fullname-err" className="prf-field-error">{errors.fullName}</span>}
            </div>

            {/* phone */}
            <div className="prf-field">
              <label className="prf-label" htmlFor="prf-phone">Số điện thoại</label>
              <input
                id="prf-phone"
                className={`prf-input${errors.phone ? ' prf-input--err' : ''}`}
                type="tel"
                value={form.phone}
                onChange={(e) => handleChange('phone', e.target.value)}
                placeholder="0912345678"
                aria-invalid={!!errors.phone}
                aria-describedby={errors.phone ? 'prf-phone-err' : undefined}
              />
              {errors.phone && <span id="prf-phone-err" className="prf-field-error">{errors.phone}</span>}
            </div>

            {/* dateOfBirth */}
            <div className="prf-field">
              <label className="prf-label" htmlFor="prf-dob">Ngày sinh</label>
              <input
                id="prf-dob"
                className="prf-input"
                type="date"
                value={form.dateOfBirth}
                onChange={(e) => handleChange('dateOfBirth', e.target.value)}
              />
            </div>

            {/* bio */}
            <div className="prf-field">
              <label className="prf-label" htmlFor="prf-bio">
                Bio
                <span className="prf-char-count">{form.bio.length}/500</span>
              </label>
              <textarea
                id="prf-bio"
                className={`prf-textarea${errors.bio ? ' prf-input--err' : ''}`}
                rows={4}
                value={form.bio}
                onChange={(e) => handleChange('bio', e.target.value)}
                placeholder="Giới thiệu về bản thân..."
                aria-invalid={!!errors.bio}
                aria-describedby={errors.bio ? 'prf-bio-err' : undefined}
              />
              {errors.bio && <span id="prf-bio-err" className="prf-field-error">{errors.bio}</span>}
            </div>

            {/* email read-only */}
            <div className="prf-field">
              <label className="prf-label" htmlFor="prf-email">Email <span className="prf-readonly-badge">Không thể thay đổi</span></label>
              <input id="prf-email" className="prf-input prf-input--readonly" type="email" value={user?.email ?? ''} disabled aria-disabled="true" />
            </div>

            {/* change password link */}
            <Link to="/settings/password" className="prf-pwd-link">Đổi mật khẩu →</Link>

            {/* Save button */}
            <button
              className="prf-save-btn"
              onClick={handleSave}
              disabled={isSaving}
              aria-busy={isSaving}
            >
              {isSaving && <span className="prf-spinner prf-spinner--white" aria-hidden="true" />}
              {isSaving ? 'Đang lưu…' : 'Lưu thay đổi'}
            </button>
          </section>
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
/* ===== Profile (SakuJi Hanami Theme) ===== */

.prf-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  font-family: var(--font-base);
}

.prf-body {
  flex: 1;
  max-width: 900px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 32px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-sizing: border-box;
}

.prf-back-link {
  font-size: 13px;
  color: var(--color-text-sub);
  text-decoration: none;
  font-weight: 600;
  width: fit-content;
}
.prf-back-link:hover { color: var(--color-primary); }

.prf-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}

/* 2-column layout */
.prf-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 32px;
  align-items: start;
}

/* Sidebar */
.prf-sidebar {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 28px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.prf-avatar-wrap { position: relative; }
.prf-avatar-btn {
  position: absolute;
  bottom: 0; right: 0;
  width: 32px; height: 32px;
  background: var(--color-card);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-full);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  font-size: 11px;
  color: var(--color-text-sub);
  gap: 4px;
  white-space: nowrap;
  transition: border-color var(--transition), color var(--transition);
}
.prf-avatar-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }

.prf-sidebar-name   { font-size: 16px; font-weight: 700; color: var(--color-text); }
.prf-sidebar-email  { font-size: 12px; color: var(--color-text-sub); word-break: break-all; }
.prf-sidebar-joined { font-size: 12px; color: var(--color-text-disabled); }

/* Form section */
.prf-form-section {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 28px 28px 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Fields */
.prf-field { display: flex; flex-direction: column; gap: 6px; }
.prf-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: 6px;
}
.prf-required { color: var(--color-error); }
.prf-char-count { margin-left: auto; font-size: 11px; font-weight: 400; color: var(--color-text-sub); }
.prf-readonly-badge {
  font-size: 10px;
  font-weight: 700;
  background: var(--color-bg);
  color: var(--color-text-disabled);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: 2px 8px;
}

.prf-input {
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
.prf-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232,154,170,0.15);
  background: var(--color-card);
}
.prf-input--err { border-color: var(--color-error); background: #FEF2F2; }
.prf-input--err:focus { box-shadow: 0 0 0 3px rgba(229,115,115,0.12); }
.prf-input--readonly { opacity: 0.6; cursor: not-allowed; }

.prf-textarea {
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
  transition: border-color var(--transition), box-shadow var(--transition);
}
.prf-textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232,154,170,0.15);
  background: var(--color-card);
}

.prf-field-error { font-size: 12px; color: var(--color-error); }

.prf-pwd-link {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
  text-decoration: none;
  width: fit-content;
}
.prf-pwd-link:hover { text-decoration: underline; }

/* Save button */
.prf-save-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 44px;
  padding: 0 28px;
  background: var(--color-secondary);
  color: white;
  border: none;
  border-radius: var(--radius-full);
  font-family: var(--font-base);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  align-self: flex-end;
  box-shadow: 0 2px 8px rgba(93,187,105,0.25);
  transition: filter var(--transition), transform var(--transition);
}
.prf-save-btn:hover:not(:disabled) { filter: brightness(1.07); }
.prf-save-btn:active:not(:disabled) { transform: scale(0.97); }
.prf-save-btn:disabled { opacity: 0.60; cursor: not-allowed; }

/* Skeleton */
.prf-skel {
  height: 400px;
  border-radius: var(--radius-lg);
  background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%);
  background-size: 200% 100%;
  animation: skelPulse 1.4s ease infinite;
}
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* Spinner */
.prf-spinner { display: inline-block; width: 16px; height: 16px; border: 2.5px solid rgba(255,255,255,0.35); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; flex-shrink: 0; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Hidden file input */
.prf-sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0,0,0,0); }

/* Responsive */
@media (max-width: 767px) {
  .prf-layout { grid-template-columns: 1fr; }
  .prf-sidebar { flex-direction: row; text-align: left; gap: 16px; }
  .prf-save-btn { width: 100%; }
  .prf-body { padding: 16px 16px 32px; }
}

@media (prefers-reduced-motion: reduce) {
  .prf-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 9. 3 TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Loading (init)** | Skeleton block thay thế toàn bộ form |
| **Error (save)** | Toast `error` — form giữ nguyên |
| **Empty** | Không áp dụng — form luôn có dữ liệu từ Redux |

---

## 10. INTERACTIONS / FLOW

```
Mount → load user từ Redux → prefill form
Avatar change → FileReader preview → set avatarFile
Field change → clear error đó
Save click → validate → errors? → set errors, stop
           → POST avatar (nếu có file) → PUT profile
           → success: dispatch Redux update + toast success
           → error: toast error
```

---

## 11. DOMAIN RULES

- `email` và `jlptLevel` là read-only — không gửi trong request body.
- `role` không hiển thị trên form này.
- Avatar tối đa 2MB — validate trước upload: `file.size > 2 * 1024 * 1024 → toast error`.
- Avatar accept: `image/jpeg, image/png, image/webp`.

---

## 12. ACCESSIBILITY

- [ ] Tất cả `<input>` có `<label>` liên kết bằng `htmlFor`/`id`
- [ ] Avatar upload: `<label htmlFor="avatar-upload">` là button accessible
- [ ] Read-only field có `aria-disabled="true"` và `disabled`
- [ ] Char counter là text thường, không dùng `aria-live` (không cần announce từng ký tự)
- [ ] Save button có `aria-busy` khi đang lưu
