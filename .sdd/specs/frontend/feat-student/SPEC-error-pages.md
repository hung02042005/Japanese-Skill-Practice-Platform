# SPEC — Trang Lỗi (`/403` và `/404`)
>
> **Sprint:** 1 — Foundation
> **Prefix:** `err-` | **activeTab:** `''` | **Guard:** Public
> **Phụ thuộc:** `USER-SPEC.md §9.5`

---

## 1. MÔ TẢ TRANG

Hai trang lỗi tĩnh dùng chung CSS. `/404` cho route không tồn tại. `/403` cho route bị chặn do thiếu quyền (redirect từ `PrivateRoute` / `AdminRoute`).

---

## 2. MOCKUP

```
/404:
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  [Logo SakuJi]                                               │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                                                      │   │
│  │     [SakuChan thinking 160px]                        │   │
│  │                                                      │   │
│  │   404                                                │   │
│  │   Trang không tìm thấy                               │   │
│  │   Trang bạn tìm không tồn tại hoặc đã bị di chuyển. │   │
│  │                                                      │   │
│  │   [← Quay lại]   [Về Dashboard]                     │   │
│  │                                                      │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘

/403:
│     [SakuChan wrong 160px]
│
│   403
│   Không có quyền truy cập
│   Bạn không có quyền xem trang này.
│
│   [← Quay lại]   [Về Dashboard]
```

---

## 3. FILE CẦN TẠO

```
pages/error/
├── NotFound.jsx      ← /404
├── Forbidden.jsx     ← /403
└── Error.css         ← dùng chung
```

---

## 4. JSX

```jsx
// NotFound.jsx
import { useNavigate, Link } from 'react-router-dom';
import AppLogo from '../../components/common/AppLogo';
import SakuChan from '../../components/auth/SakuChan';
import './Error.css';

export default function NotFound() {
  const navigate = useNavigate();
  return (
    <div className="err-page">
      <div className="err-topbar">
        <Link to="/" aria-label="SakuJi — về trang chủ">
          <AppLogo size={28} />
        </Link>
      </div>
      <div className="err-content">
        <SakuChan variant="thinking" size={160} />
        <div className="err-code" aria-hidden="true">404</div>
        <h1 className="err-title">Trang không tìm thấy</h1>
        <p className="err-subtitle">Trang bạn tìm không tồn tại hoặc đã bị di chuyển.</p>
        <div className="err-actions">
          <button className="err-btn err-btn--ghost" onClick={() => navigate(-1)}>← Quay lại</button>
          <Link to="/dashboard" className="err-btn err-btn--primary">Về Dashboard</Link>
        </div>
      </div>
    </div>
  );
}

// Forbidden.jsx
import { useNavigate, Link } from 'react-router-dom';
import AppLogo from '../../components/common/AppLogo';
import SakuChan from '../../components/auth/SakuChan';
import './Error.css';

export default function Forbidden() {
  const navigate = useNavigate();
  return (
    <div className="err-page">
      <div className="err-topbar">
        <Link to="/" aria-label="SakuJi — về trang chủ"><AppLogo size={28} /></Link>
      </div>
      <div className="err-content">
        <SakuChan variant="wrong" size={160} />
        <div className="err-code" aria-hidden="true">403</div>
        <h1 className="err-title">Không có quyền truy cập</h1>
        <p className="err-subtitle">Bạn không có quyền xem trang này.</p>
        <div className="err-actions">
          <button className="err-btn err-btn--ghost" onClick={() => navigate(-1)}>← Quay lại</button>
          <Link to="/dashboard" className="err-btn err-btn--primary">Về Dashboard</Link>
        </div>
      </div>
    </div>
  );
}
```

---

## 5. CSS

```css
/* ===== Error Pages (SakuJi Hanami Theme) ===== */

.err-page {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  font-family: var(--font-base);
}

.err-topbar {
  width: 100%;
  padding: 16px 24px;
}

.err-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
  padding: 32px 24px;
  max-width: 440px;
}

.err-code {
  font-size: 80px;
  font-weight: 800;
  color: var(--color-primary-light);
  line-height: 1;
  letter-spacing: -4px;
}

.err-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}

.err-subtitle {
  font-size: 14px;
  color: var(--color-text-sub);
  line-height: 1.6;
  margin: 0;
}

.err-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.err-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 42px;
  padding: 0 22px;
  border-radius: var(--radius-full);
  font-family: var(--font-base);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
  transition: filter var(--transition), transform var(--transition);
}
.err-btn--primary { background: var(--color-secondary); color: white; border: none; }
.err-btn--primary:hover { filter: brightness(1.07); }
.err-btn--ghost { background: transparent; border: 1.5px solid var(--color-border); color: var(--color-text-sub); }
.err-btn--ghost:hover { color: var(--color-text); background: var(--color-bg); }

@media (prefers-reduced-motion: reduce) {
  .err-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 6. ROUTE REGISTRATION (App.jsx)

```jsx
import NotFound   from './pages/error/NotFound';
import Forbidden  from './pages/error/Forbidden';

// Trong Routes:
<Route path="/403" element={<Forbidden />} />
<Route path="*"    element={<NotFound />} />   {/* catch-all phải là route cuối cùng */}
```

---

## 7. ACCESSIBILITY

- [ ] `err-code` (404/403) có `aria-hidden="true"` — số này decorative, `<h1>` mới là heading thực sự
- [ ] "Quay lại" button dùng `navigate(-1)` — nếu không có history, fallback về `/dashboard`
- [ ] Logo link có `aria-label`
