# SPEC — Thanh toán thành công (`/subscription/success`)
> **Sprint:** 4 — Monetization & Retention
> **Prefix:** `sus-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §12.2`
> **Query param:** `?orderId=xxx`

---

## 1. MÔ TẢ TRANG

Trang xác nhận sau khi thanh toán VIP thành công. Verify order với backend, hiển thị thông tin gói đã kích hoạt và ngày hết hạn. Nếu verify chưa xong (webhook chưa xử lý) → retry tối đa 3 lần với delay 3s.

---

## 2. MOCKUP

```
Trạng thái "verifying":
┌──────────────────────────────────────────────────────────────┐
│  [Spinner 48px]                                              │
│  Đang xác nhận thanh toán của bạn...                        │
│  Vui lòng không đóng trang này.                             │
└──────────────────────────────────────────────────────────────┘

Trạng thái "success":
┌──────────────────────────────────────────────────────────────┐
│  [SakuChan celebrate 180px]                                  │
│                                                              │
│  ⭐ Chúc mừng! Tài khoản VIP đã được kích hoạt!             │
│  Bạn hiện có thể truy cập toàn bộ nội dung N1–N5.           │
│                                                              │
│  Gói: 3 Tháng VIP                                           │
│  Hết hạn: 15/09/2026                                        │
│                                                              │
│  [Bắt đầu học ngay →]                                       │
└──────────────────────────────────────────────────────────────┘

Trạng thái "failed" (sau 3 retries):
┌──────────────────────────────────────────────────────────────┐
│  [SakuChan thinking 160px]                                   │
│                                                              │
│  Đang xử lý thanh toán của bạn...                           │
│  Có thể mất vài phút để kích hoạt.                          │
│                                                              │
│  Mã đơn hàng: #ORD-12345                                    │
│  Vui lòng liên hệ hỗ trợ nếu không nhận được sau 10 phút.  │
│                                                              │
│  [Liên hệ hỗ trợ]    [Về Dashboard]                        │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/subscription/
└── SubscriptionSuccess.jsx  (chung CSS với Subscription.css)
```

---

## 4. STATE

```js
const [searchParams] = useSearchParams();
const orderId = searchParams.get('orderId');

const [state,     setState]  = useState('verifying'); // 'verifying'|'success'|'failed'
const [orderInfo, setOrder]  = useState(null);        // { planName, expiresAt }
const [retries,   setRetries]= useState(0);
const MAX_RETRIES = 3;
```

---

## 5. API CALLS

```js
// GET /api/subscriptions/verify?orderId=xxx
// Response 200: { data: { planName, expiresAt, status: 'ACTIVE' } }
// Response 202: { data: { status: 'PENDING' } }  ← webhook chưa xong

// Logic:
// 1. Gọi verify ngay khi mount
// 2. Nếu PENDING → wait 3s → retry (max 3 lần)
// 3. Nếu ACTIVE → setState('success')
// 4. Sau 3 retries vẫn PENDING → setState('failed')
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useRef } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import AppLogo from '../../components/common/AppLogo';
import SakuChan from '../../components/auth/SakuChan';
import { verifySubscription } from '../../api/studentService';
import './Subscription.css';

export default function SubscriptionSuccess() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const orderId  = searchParams.get('orderId');

  const [state,    setState]  = useState('verifying');
  const [orderInfo,setOrder]  = useState(null);
  const retriesRef = useRef(0);

  useEffect(() => {
    if (!orderId) { navigate('/subscription'); return; }

    async function tryVerify() {
      try {
        const result = await verifySubscription(orderId);
        if (result.status === 'ACTIVE') {
          setState('success');
          setOrder(result);
        } else if (retriesRef.current < 3) {
          retriesRef.current += 1;
          setTimeout(tryVerify, 3000);
        } else {
          setState('failed');
        }
      } catch {
        if (retriesRef.current < 3) {
          retriesRef.current += 1;
          setTimeout(tryVerify, 3000);
        } else {
          setState('failed');
        }
      }
    }

    tryVerify();
  }, [orderId, navigate]);

  return (
    <div className="sus-page">
      <div className="sus-topbar">
        <AppLogo size={28} />
        <span className="sus-brand">SakuJi</span>
      </div>

      <div className="sus-card">
        {state === 'verifying' && (
          <div className="sus-verifying" role="status">
            <div className="sus-spinner-lg" />
            <h1 className="sus-title">Đang xác nhận thanh toán của bạn...</h1>
            <p className="sus-subtitle">Vui lòng không đóng trang này.</p>
          </div>
        )}

        {state === 'success' && orderInfo && (
          <div className="sus-success">
            <SakuChan variant="celebrate" size={180} />
            <h1 className="sus-title">⭐ Chúc mừng! Tài khoản VIP đã được kích hoạt!</h1>
            <p className="sus-subtitle">Bạn hiện có thể truy cập toàn bộ nội dung N1–N5.</p>
            <div className="sus-order-info">
              <div className="sus-info-row"><span>Gói:</span><strong>{orderInfo.planName}</strong></div>
              <div className="sus-info-row"><span>Hết hạn:</span><strong>{new Date(orderInfo.expiresAt).toLocaleDateString('vi-VN')}</strong></div>
            </div>
            <button className="sus-cta-btn" onClick={() => navigate('/dashboard')}>
              Bắt đầu học ngay →
            </button>
          </div>
        )}

        {state === 'failed' && (
          <div className="sus-pending">
            <SakuChan variant="thinking" size={160} />
            <h1 className="sus-title">Đang xử lý thanh toán của bạn...</h1>
            <p className="sus-subtitle">Có thể mất vài phút để kích hoạt gói VIP.</p>
            {orderId && <p className="sus-order-id">Mã đơn hàng: <code>{orderId}</code></p>}
            <p className="sus-support-note">Vui lòng liên hệ hỗ trợ nếu không nhận được sau 10 phút.</p>
            <div className="sus-actions">
              <a href="mailto:support@sakuji.vn" className="sus-support-btn">Liên hệ hỗ trợ</a>
              <Link to="/dashboard" className="sus-ghost-btn">Về Dashboard</Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
```

---

## 7. CSS (bổ sung vào Subscription.css)

```css
/* ===== Subscription Success ===== */
.sus-page { min-height: 100vh; background: var(--color-bg); display: flex; flex-direction: column; align-items: center; font-family: var(--font-base); }
.sus-topbar { width: 100%; padding: 16px 24px; display: flex; align-items: center; gap: 10px; }
.sus-brand { font-size: 20px; font-weight: 800; color: var(--color-primary); }
.sus-card { flex: 1; display: flex; align-items: center; justify-content: center; width: 100%; max-width: 480px; padding: 32px 24px; }
.sus-verifying, .sus-success, .sus-pending { display: flex; flex-direction: column; align-items: center; gap: 16px; text-align: center; width: 100%; }
.sus-spinner-lg { width: 48px; height: 48px; border: 4px solid var(--color-primary-light); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
.sus-title    { font-size: 22px; font-weight: 700; color: var(--color-text); margin: 0; }
.sus-subtitle { font-size: 14px; color: var(--color-text-sub); line-height: 1.6; margin: 0; }
.sus-order-info { background: var(--color-accent-bg); border: 1.5px solid var(--color-accent); border-radius: var(--radius-lg); padding: 16px 24px; width: 100%; display: flex; flex-direction: column; gap: 8px; }
.sus-info-row { display: flex; justify-content: space-between; font-size: 14px; color: var(--color-text); }
.sus-cta-btn { display: inline-flex; align-items: center; height: 48px; padding: 0 32px; background: var(--color-secondary); color: white; border: none; border-radius: var(--radius-full); font-size: 15px; font-weight: 700; cursor: pointer; box-shadow: 0 2px 8px rgba(93,187,105,0.25); transition: filter var(--transition); }
.sus-cta-btn:hover { filter: brightness(1.07); }
.sus-order-id { font-size: 13px; color: var(--color-text-sub); }
.sus-order-id code { background: var(--color-bg); border: 1px solid var(--color-border); border-radius: 4px; padding: 2px 6px; font-size: 13px; }
.sus-support-note { font-size: 13px; color: var(--color-text-sub); }
.sus-actions { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; }
.sus-support-btn { display: inline-flex; align-items: center; height: 40px; padding: 0 20px; background: var(--color-primary); color: white; border-radius: var(--radius-full); text-decoration: none; font-size: 14px; font-weight: 700; }
.sus-ghost-btn { display: inline-flex; align-items: center; height: 40px; padding: 0 20px; background: transparent; border: 1.5px solid var(--color-border); color: var(--color-text-sub); border-radius: var(--radius-full); text-decoration: none; font-size: 14px; font-weight: 600; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (prefers-reduced-motion: reduce) { .sus-page * { animation: none !important; transition-duration: 0ms !important; } }
```
