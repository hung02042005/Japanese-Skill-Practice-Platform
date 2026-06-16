# SPEC — Nâng cấp VIP (`/subscription`)
> **Sprint:** 4 — Monetization & Retention
> **Prefix:** `sub-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §12.1`

---

## 1. MÔ TẢ TRANG

Trang nâng cấp VIP. Hiển thị 3 gói (Monthly/Quarterly/Annual), bảng so sánh FREE vs VIP. Nếu user đã VIP → hiển thị ngày hết hạn + nút gia hạn. Click "Đăng ký" → `POST /subscriptions/checkout` → redirect sang payment gateway.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="")                                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Nâng Cấp VIP                                                   │
│  Mở khoá toàn bộ nội dung N1–N5, không giới hạn.               │
│                                                                  │
│  [Nếu đang VIP]: ⭐ Gói VIP của bạn hết hạn: 15/12/2026 [Gia hạn]│
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │  1 Tháng     │  │  3 Tháng     │  │  1 Năm   "Tiết kiệm  │  │
│  │              │  │  [PHỔ BIẾN]  │  │           33%"       │  │
│  │  99.000đ     │  │  249.000đ    │  │  799.000đ            │  │
│  │  /tháng      │  │  83.000đ/th  │  │  66.000đ/tháng      │  │
│  │              │  │              │  │                      │  │
│  │  [Chọn gói]  │  │  [Chọn gói] │  │  [Chọn gói]         │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
│                                                                  │
│  FREE vs VIP                                                    │
│  ┌──────────────────────┬────────────┬────────────┐            │
│  │ Tính năng            │   FREE     │    VIP     │            │
│  ├──────────────────────┼────────────┼────────────┤            │
│  │ Nội dung N5          │    ✅      │    ✅      │            │
│  │ Nội dung N4–N1       │    ❌      │    ✅      │            │
│  │ Flashcard không giới hạn│ ❌     │    ✅      │            │
│  │ Đề thi thử N5        │    ✅      │    ✅      │            │
│  │ Đề thi thử N4–N1     │    ❌      │    ✅      │            │
│  │ Chứng chỉ hoàn thành │    ❌      │    ✅      │            │
│  └──────────────────────┴────────────┴────────────┘            │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/subscription/
├── Subscription.jsx
└── Subscription.css

components/student/
└── SubscriptionPlanCard.jsx
```

---

## 4. STATE

```js
const [plans,        setPlans]    = useState([]);
const [currentSub,   setCurrent]  = useState(null);   // null = FREE
const [isLoading,    setLoading]  = useState(true);
const [isRedirecting,setRedirect] = useState(false);  // khi đang redirect payment
const [selectedPlan, setSelected] = useState(null);
const [error,        setError]    = useState('');

const { user } = useAppSelector((s) => s.auth);
```

---

## 5. API CALLS

```js
// GET /api/subscriptions/plans
// Response:
{
  "data": [
    {
      "planId": "monthly",
      "name": "1 Tháng",
      "price": 99000,
      "pricePerMonth": 99000,
      "durationMonths": 1,
      "badge": null,
      "savingPercent": null
    },
    {
      "planId": "quarterly",
      "name": "3 Tháng",
      "price": 249000,
      "pricePerMonth": 83000,
      "durationMonths": 3,
      "badge": "PHỔ BIẾN",
      "savingPercent": 17
    },
    {
      "planId": "annual",
      "name": "1 Năm",
      "price": 799000,
      "pricePerMonth": 66000,
      "durationMonths": 12,
      "badge": "Tiết kiệm 33%",
      "savingPercent": 33
    }
  ]
}

// GET /api/subscriptions/me
// Response: { data: { planId, expiresAt } | null }

// POST /api/subscriptions/checkout
// Request: { "planId": "quarterly" }
// Response: { data: { paymentUrl: "https://..." } }
// → window.location.href = paymentUrl
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import SubscriptionPlanCard from '../../components/student/SubscriptionPlanCard';
import { getSubscriptionPlans, getCurrentSubscription, checkoutSubscription } from '../../api/studentService';
import './Subscription.css';

const COMPARISON_FEATURES = [
  { label: 'Nội dung N5',               free: true,  vip: true },
  { label: 'Nội dung N4–N1',            free: false, vip: true },
  { label: 'Flashcard không giới hạn',  free: false, vip: true },
  { label: 'Đề thi thử N5',             free: true,  vip: true },
  { label: 'Đề thi thử N4–N1',          free: false, vip: true },
  { label: 'Luyện Kanji OCR (AI)',       free: false, vip: true },
  { label: 'Chứng chỉ hoàn thành',      free: false, vip: true },
  { label: 'Hỗ trợ ưu tiên',            free: false, vip: true },
];

export default function Subscription() {
  const [plans,       setPlans]    = useState([]);
  const [currentSub,  setCurrent]  = useState(null);
  const [isLoading,   setLoading]  = useState(true);
  const [isRedirect,  setRedirect] = useState(false);
  const [error,       setError]    = useState('');

  useEffect(() => {
    Promise.all([getSubscriptionPlans(), getCurrentSubscription()])
      .then(([plansData, subData]) => { setPlans(plansData); setCurrent(subData); })
      .catch(() => setError('Không thể tải thông tin gói. Thử lại sau.'))
      .finally(() => setLoading(false));
  }, []);

  async function handleCheckout(planId) {
    setRedirect(true);
    setError('');
    try {
      const { paymentUrl } = await checkoutSubscription(planId);
      window.location.href = paymentUrl;
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tiến hành thanh toán. Thử lại sau.');
      setRedirect(false);
    }
  }

  return (
    <div className="sub-page">
      <TopNav activeTab="" />
      <main className="sub-body">
        <div className="sub-page-header">
          <h1 className="sub-title">Nâng Cấp VIP</h1>
          <p className="sub-subtitle">Mở khoá toàn bộ nội dung N1–N5, không giới hạn.</p>
        </div>

        {/* Current subscription banner */}
        {currentSub && (
          <div className="sub-current-banner">
            <span className="sub-current-icon" aria-hidden="true">⭐</span>
            <span>Gói VIP của bạn hết hạn: <strong>{new Date(currentSub.expiresAt).toLocaleDateString('vi-VN')}</strong></span>
            <button className="sub-renew-btn" onClick={() => handleCheckout(currentSub.planId)} disabled={isRedirect}>
              Gia hạn
            </button>
          </div>
        )}

        {error && <div className="sub-error" role="alert">{error}</div>}

        {/* Plan cards */}
        {isLoading
          ? <div className="sub-plan-grid">{[1,2,3].map((i) => <div key={i} className="sub-skel" aria-hidden="true" />)}</div>
          : (
            <div className="sub-plan-grid">
              {plans.map((plan) => (
                <SubscriptionPlanCard
                  key={plan.planId}
                  plan={plan}
                  onSelect={() => handleCheckout(plan.planId)}
                  isLoading={isRedirect}
                />
              ))}
            </div>
          )
        }

        {/* Comparison table */}
        <section aria-label="So sánh FREE và VIP">
          <h2 className="sub-compare-title">FREE vs VIP</h2>
          <div className="sub-compare-table-wrap">
            <table className="sub-compare-table">
              <thead>
                <tr>
                  <th scope="col">Tính năng</th>
                  <th scope="col" className="sub-col-free">FREE</th>
                  <th scope="col" className="sub-col-vip">⭐ VIP</th>
                </tr>
              </thead>
              <tbody>
                {COMPARISON_FEATURES.map((f) => (
                  <tr key={f.label}>
                    <td>{f.label}</td>
                    <td className="sub-col-free">
                      <span aria-label={f.free ? 'Có' : 'Không có'}>{f.free ? '✅' : '❌'}</span>
                    </td>
                    <td className="sub-col-vip">
                      <span aria-label="Có">✅</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  );
}
```

---

## 7. SubscriptionPlanCard component

```jsx
// components/student/SubscriptionPlanCard.jsx
export default function SubscriptionPlanCard({ plan, onSelect, isLoading }) {
  const isFeatured = plan.badge === 'PHỔ BIẾN';
  return (
    <div className={`spc-card${isFeatured ? ' spc-card--featured' : ''}`}>
      {plan.badge && <div className="spc-badge">{plan.badge}</div>}
      <h3 className="spc-name">{plan.name}</h3>
      <div className="spc-price">
        <span className="spc-price-num">{plan.price.toLocaleString('vi-VN')}đ</span>
      </div>
      {plan.pricePerMonth !== plan.price && (
        <p className="spc-per-month">{plan.pricePerMonth.toLocaleString('vi-VN')}đ / tháng</p>
      )}
      {plan.savingPercent && (
        <p className="spc-saving">Tiết kiệm {plan.savingPercent}%</p>
      )}
      <button
        className={`spc-btn${isFeatured ? ' spc-btn--featured' : ''}`}
        onClick={onSelect}
        disabled={isLoading}
        aria-label={`Chọn gói ${plan.name}`}
      >
        {isLoading ? 'Đang xử lý...' : 'Chọn gói này'}
      </button>
    </div>
  );
}
```

---

## 8. CSS

```css
/* ===== Subscription (SakuJi Hanami Theme) ===== */
.sub-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.sub-body { flex: 1; max-width: 1000px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 28px; box-sizing: border-box; }

.sub-page-header { display: flex; flex-direction: column; gap: 4px; }
.sub-title { font-size: 28px; font-weight: 700; color: var(--color-text); margin: 0; }
.sub-subtitle { font-size: 15px; color: var(--color-text-sub); margin: 0; }

.sub-current-banner { background: var(--color-accent-bg); border: 1.5px solid var(--color-accent); border-radius: var(--radius-md); padding: 14px 20px; display: flex; align-items: center; gap: 10px; font-size: 14px; color: #7A4A00; }
.sub-current-icon { font-size: 20px; }
.sub-renew-btn { margin-left: auto; height: 34px; padding: 0 16px; background: var(--color-accent); color: #7A4A00; border: none; border-radius: var(--radius-full); font-size: 13px; font-weight: 700; cursor: pointer; }

.sub-error { background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }

/* Plan grid */
.sub-plan-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.sub-skel { height: 200px; border-radius: var(--radius-xl); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* Plan card */
.spc-card {
  position: relative; background: var(--color-card);
  border: 2px solid var(--color-border); border-radius: var(--radius-xl);
  padding: 28px 24px; display: flex; flex-direction: column; align-items: center; gap: 10px;
  text-align: center; transition: box-shadow var(--transition);
}
.spc-card:hover { box-shadow: var(--shadow-md); }
.spc-card--featured { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-light); }
.spc-badge {
  position: absolute; top: -14px; left: 50%; transform: translateX(-50%);
  background: var(--color-primary); color: white; border-radius: var(--radius-full);
  font-size: 11px; font-weight: 800; padding: 4px 14px; white-space: nowrap;
  text-transform: uppercase; letter-spacing: 0.5px;
}
.spc-name  { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; }
.spc-price-num { font-size: 32px; font-weight: 800; color: var(--color-text); }
.spc-per-month { font-size: 13px; color: var(--color-text-sub); margin: 0; }
.spc-saving    { font-size: 12px; color: var(--color-secondary); font-weight: 700; margin: 0; }
.spc-btn {
  width: 100%; height: 44px; border-radius: var(--radius-full);
  background: var(--color-bg); border: 1.5px solid var(--color-primary);
  color: var(--color-primary); font-size: 14px; font-weight: 700; cursor: pointer;
  transition: background var(--transition);
}
.spc-btn:hover:not(:disabled) { background: var(--color-primary-bg); }
.spc-btn--featured { background: var(--color-primary); color: white; border-color: transparent; }
.spc-btn--featured:hover:not(:disabled) { filter: brightness(1.07); }
.spc-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* Compare table */
.sub-compare-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0 0 12px; }
.sub-compare-table-wrap { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); overflow: hidden; }
.sub-compare-table { width: 100%; border-collapse: collapse; }
.sub-compare-table thead { background: var(--color-bg); border-bottom: 1px solid var(--color-border); }
.sub-compare-table th { padding: 12px 16px; font-size: 13px; font-weight: 700; color: var(--color-text-sub); text-align: center; }
.sub-compare-table th:first-child { text-align: left; }
.sub-compare-table td { padding: 12px 16px; font-size: 14px; color: var(--color-text); border-bottom: 1px solid var(--color-border); text-align: center; }
.sub-compare-table td:first-child { text-align: left; }
.sub-compare-table tr:last-child td { border-bottom: none; }
.sub-col-free { color: var(--color-text-sub); }
.sub-col-vip  { color: var(--color-accent); font-weight: 700; }

@media (max-width: 767px) {
  .sub-plan-grid { grid-template-columns: 1fr; }
  .sub-body { padding: 16px 16px 32px; }
}

@media (prefers-reduced-motion: reduce) {
  .sub-page * { animation: none !important; transition-duration: 0ms !important; }
}
```
