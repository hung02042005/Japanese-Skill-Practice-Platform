# SPEC — Onboarding (`/onboarding`)
>
> **Sprint:** 1 — Foundation
> **Prefix:** `onb-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §9.1` | **Backend ref:** `feat-auth/UC-04-user-profile.md`
> **Trigger:** Redirect từ `/login` khi `user.onboardingCompleted === false`

---

## 1. MÔ TẢ TRANG

Bước thiết lập lần đầu sau khi học viên đăng nhập lần đầu. Thu thập 3 thông tin: JLPT level mục tiêu, thời gian học mỗi ngày, kỹ năng ưu tiên. Sau khi submit, gọi API lưu preferences và redirect vào `/dashboard`.

**Điều kiện:** Chỉ hiển thị khi `user.onboardingCompleted === false`. Nếu đã hoàn thành → redirect `/dashboard` ngay.

---

## 2. MOCKUP

```
┌───────────────────────────────────────────────────────────────────┐
│                         [AppLogo]  SakuJi                         │
│                                                                   │
│              ┌────────────────────────────────────┐              │
│              │                                    │              │
│              │  ● ────────── ○ ────────── ○       │  step dots  │
│              │    Bước 1 / 3                      │              │
│              │                                    │              │
│              │  [Saku-chan happy 120px]            │              │
│              │                                    │              │
│              │  Bạn muốn thi JLPT cấp độ nào?    │              │
│              │  Chọn mục tiêu để chúng tôi cá     │              │
│              │  nhân hoá lộ trình học.             │              │
│              │                                    │              │
│              │  [ N5 ]  [ N4 ]  [ N3 ]            │              │
│              │  [ N2 ]  [ N1 ]                    │              │
│              │                                    │              │
│              │        [Tiếp theo →]               │              │
│              └────────────────────────────────────┘              │
└───────────────────────────────────────────────────────────────────┘

Bước 2:
              │  Mỗi ngày bạn có bao nhiêu thời gian? │
              │                                        │
              │  ○ 5 phút    ○ 10 phút (Đề xuất)       │
              │  ○ 15 phút   ○ 20 phút                 │
              │                                        │
              │  [← Quay lại]    [Tiếp theo →]         │

Bước 3:
              │  Kỹ năng nào bạn muốn tập trung?      │
              │                                        │
              │  [✓ Kanji]  [✓ Từ vựng]  [ Ngữ pháp]  │
              │  [ Tất cả kỹ năng]                     │
              │                                        │
              │  [← Quay lại]    [Bắt đầu học! 🌸]     │
```

---

## 3. FILE CẦN TẠO

```
pages/onboarding/
├── Onboarding.jsx
└── Onboarding.css
```

Không cần component riêng — trang đủ ngắn để giữ inline.

---

## 4. STATE

```js
const [step,         setStep]    = useState(1);           // 1 | 2 | 3
const [jlptGoal,     setJlpt]    = useState('N5');         // 'N5'|'N4'|'N3'|'N2'|'N1'
const [dailyMinutes, setMinutes] = useState(10);           // 5|10|15|20
const [focusSkills,  setSkills]  = useState(['all']);      // string[]
const [isSubmitting, setSubmit]  = useState(false);

// Từ Redux — không fetch lại
const { user } = useAppSelector((s) => s.auth);
```

---

## 5. API CALLS

```js
// POST /api/students/onboarding
// Request:
{
  "jlptGoal": "N5",         // string enum
  "dailyMinutes": 10,       // int
  "focusSkills": ["kanji", "vocabulary"]  // string[]
}
// Response 200:
{
  "status": 200,
  "message": "Thiết lập hoàn tất",
  "data": { "onboardingCompleted": true }
}
```

Sau khi thành công: `dispatch(updateOnboardingStatus(true))` → `navigate('/dashboard')`.

---

## 6. COMPONENT BREAKDOWN

| Component | Nguồn | Dùng để |
|:---|:---|:---|
| `AppLogo` | `components/common/AppLogo` | Logo góc trên |
| `SakuChan` | `components/auth/SakuChan` | Mascot animated |
| `JlptBadge` | `components/common/Badges` | Hiển thị level đã chọn |

Không cần `TopNav` — đây là trang onboarding tập trung.

---

## 7. JSX STRUCTURE

```jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import AppLogo from '../../components/common/AppLogo';
import SakuChan from '../../components/auth/SakuChan';
import { submitOnboarding } from '../../api/studentService';
import './Onboarding.css';

const JLPT_LEVELS   = ['N5', 'N4', 'N3', 'N2', 'N1'];
const DAILY_OPTIONS = [
  { value: 5,  label: '5 phút' },
  { value: 10, label: '10 phút', recommended: true },
  { value: 15, label: '15 phút' },
  { value: 20, label: '20 phút' },
];
const SKILL_OPTIONS = [
  { id: 'kanji',      label: 'Kanji' },
  { id: 'vocabulary', label: 'Từ vựng' },
  { id: 'grammar',    label: 'Ngữ pháp' },
  { id: 'all',        label: 'Tất cả' },
];

export default function Onboarding() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();

  const [step,         setStep]   = useState(1);
  const [jlptGoal,     setJlpt]   = useState('N5');
  const [dailyMinutes, setMins]   = useState(10);
  const [focusSkills,  setSkills] = useState(['all']);
  const [isSubmitting, setSubmit] = useState(false);

  function toggleSkill(id) {
    if (id === 'all') { setSkills(['all']); return; }
    setSkills((prev) => {
      const next = prev.filter((s) => s !== 'all');
      return next.includes(id) ? next.filter((s) => s !== id) : [...next, id];
    });
  }

  async function handleFinish() {
    setSubmit(true);
    try {
      await submitOnboarding({ jlptGoal, dailyMinutes, focusSkills });
      // dispatch update onboarding flag
      navigate('/dashboard');
    } catch {
      /* addToast error nếu muốn — nhưng thường hiếm lỗi ở bước này */
    } finally {
      setSubmit(false);
    }
  }

  return (
    <div className="onb-page">
      {/* Top logo bar */}
      <div className="onb-topbar">
        <AppLogo size={28} />
        <span className="onb-brand">SakuJi</span>
      </div>

      <div className="onb-card">
        {/* Step dots */}
        <div className="onb-steps" aria-label={`Bước ${step} trên 3`}>
          {[1, 2, 3].map((s) => (
            <div key={s} className={`onb-dot${s === step ? ' onb-dot--active' : s < step ? ' onb-dot--done' : ''}`} />
          ))}
        </div>

        {/* Mascot */}
        <div className="onb-mascot">
          <SakuChan variant={step === 3 ? 'celebrate' : 'happy'} size={120} />
        </div>

        {/* Step 1 — JLPT goal */}
        {step === 1 && (
          <div className="onb-step" key="step1">
            <h1 className="onb-title">Bạn muốn thi JLPT cấp độ nào?</h1>
            <p className="onb-subtitle">Chúng tôi sẽ cá nhân hoá lộ trình học cho bạn.</p>
            <div className="onb-level-grid" role="radiogroup" aria-label="Chọn JLPT level">
              {JLPT_LEVELS.map((lvl) => (
                <button
                  key={lvl}
                  type="button"
                  role="radio"
                  aria-checked={jlptGoal === lvl}
                  className={`onb-level-btn${jlptGoal === lvl ? ' onb-level-btn--active' : ''}`}
                  onClick={() => setJlpt(lvl)}
                >
                  {lvl}
                </button>
              ))}
            </div>
            <button className="onb-btn onb-btn--primary" onClick={() => setStep(2)}>
              Tiếp theo →
            </button>
          </div>
        )}

        {/* Step 2 — Daily time */}
        {step === 2 && (
          <div className="onb-step" key="step2">
            <h1 className="onb-title">Mỗi ngày bạn có bao nhiêu thời gian?</h1>
            <p className="onb-subtitle">Chúng tôi sẽ gợi ý số bài học phù hợp.</p>
            <div className="onb-time-grid" role="radiogroup" aria-label="Chọn thời gian học">
              {DAILY_OPTIONS.map((opt) => (
                <label key={opt.value} className={`onb-time-opt${dailyMinutes === opt.value ? ' onb-time-opt--active' : ''}`}>
                  <input
                    type="radio"
                    name="daily"
                    value={opt.value}
                    checked={dailyMinutes === opt.value}
                    onChange={() => setMins(opt.value)}
                    className="onb-sr-only"
                  />
                  {opt.label}
                  {opt.recommended && <span className="onb-recommend-badge">Đề xuất</span>}
                </label>
              ))}
            </div>
            <div className="onb-footer-btns">
              <button className="onb-btn onb-btn--ghost" onClick={() => setStep(1)}>← Quay lại</button>
              <button className="onb-btn onb-btn--primary" onClick={() => setStep(3)}>Tiếp theo →</button>
            </div>
          </div>
        )}

        {/* Step 3 — Skills */}
        {step === 3 && (
          <div className="onb-step" key="step3">
            <h1 className="onb-title">Kỹ năng nào bạn muốn tập trung?</h1>
            <p className="onb-subtitle">Có thể chọn nhiều hoặc chọn "Tất cả".</p>
            <div className="onb-skill-grid" role="group" aria-label="Chọn kỹ năng">
              {SKILL_OPTIONS.map((sk) => (
                <button
                  key={sk.id}
                  type="button"
                  aria-pressed={focusSkills.includes(sk.id)}
                  className={`onb-skill-btn${focusSkills.includes(sk.id) ? ' onb-skill-btn--active' : ''}`}
                  onClick={() => toggleSkill(sk.id)}
                >
                  {focusSkills.includes(sk.id) && <span aria-hidden="true">✓ </span>}
                  {sk.label}
                </button>
              ))}
            </div>
            <div className="onb-footer-btns">
              <button className="onb-btn onb-btn--ghost" onClick={() => setStep(2)}>← Quay lại</button>
              <button
                className="onb-btn onb-btn--primary"
                onClick={handleFinish}
                disabled={isSubmitting || focusSkills.length === 0}
              >
                {isSubmitting && <span className="onb-spinner onb-spinner--white" aria-hidden="true" />}
                {isSubmitting ? 'Đang lưu…' : 'Bắt đầu học! 🌸'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
```

---

## 8. CSS

```css
/* ===== Onboarding (SakuJi Hanami Theme) ===== */

.onb-page {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  font-family: var(--font-base);
}

/* Top bar */
.onb-topbar {
  width: 100%;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.onb-brand {
  font-size: 20px;
  font-weight: 800;
  color: var(--color-primary);
}

/* Card container */
.onb-card {
  background: var(--color-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  padding: 36px 40px 40px;
  width: 100%;
  max-width: 480px;
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
}

/* Step dots */
.onb-steps {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}
.onb-dot {
  width: 10px; height: 10px;
  border-radius: var(--radius-full);
  background: var(--color-border);
  transition: background var(--transition), width var(--transition);
}
.onb-dot--active { background: var(--color-primary); width: 24px; }
.onb-dot--done   { background: var(--color-primary-light); }

/* Mascot */
.onb-mascot { margin-bottom: 20px; }

/* Step content */
.onb-step {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  animation: onbStepIn 0.25s ease;
}
@keyframes onbStepIn {
  from { opacity: 0; transform: translateX(12px); }
  to   { opacity: 1; transform: translateX(0); }
}

.onb-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
  text-align: center;
  margin: 0;
}
.onb-subtitle {
  font-size: 14px;
  color: var(--color-text-sub);
  text-align: center;
  margin: 0;
}

/* JLPT level grid */
.onb-level-grid {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
  margin: 4px 0;
}
.onb-level-btn {
  width: 72px; height: 72px;
  border-radius: var(--radius-lg);
  border: 2px solid var(--color-border);
  background: var(--color-bg);
  font-size: 18px;
  font-weight: 800;
  color: var(--color-text-sub);
  cursor: pointer;
  transition: border-color var(--transition), background var(--transition), color var(--transition), transform var(--transition);
}
.onb-level-btn:hover { border-color: var(--color-primary-light); color: var(--color-primary); }
.onb-level-btn--active {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  transform: scale(1.06);
}

/* Daily time options */
.onb-time-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  width: 100%;
}
.onb-time-opt {
  position: relative;
  padding: 14px 16px;
  border-radius: var(--radius-md);
  border: 2px solid var(--color-border);
  background: var(--color-bg);
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-sub);
  cursor: pointer;
  text-align: center;
  transition: border-color var(--transition), background var(--transition), color var(--transition);
}
.onb-time-opt:hover { border-color: var(--color-primary-light); color: var(--color-text); }
.onb-time-opt--active {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
  color: var(--color-primary);
}
.onb-recommend-badge {
  position: absolute;
  top: -10px; left: 50%;
  transform: translateX(-50%);
  background: var(--color-primary);
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  white-space: nowrap;
}

/* Skill chips */
.onb-skill-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}
.onb-skill-btn {
  padding: 10px 20px;
  border-radius: var(--radius-full);
  border: 2px solid var(--color-border);
  background: var(--color-bg);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-sub);
  cursor: pointer;
  transition: border-color var(--transition), background var(--transition), color var(--transition);
}
.onb-skill-btn:hover { border-color: var(--color-primary-light); }
.onb-skill-btn--active {
  border-color: var(--color-secondary);
  background: var(--color-secondary-bg);
  color: var(--color-secondary);
}

/* Footer buttons */
.onb-footer-btns {
  display: flex;
  gap: 12px;
  width: 100%;
  justify-content: flex-end;
  margin-top: 4px;
}

/* Buttons */
.onb-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 44px;
  padding: 0 24px;
  border-radius: var(--radius-full);
  font-family: var(--font-base);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: filter var(--transition), transform var(--transition);
  border: none;
}
.onb-btn:disabled { opacity: 0.60; cursor: not-allowed; }
.onb-btn:active:not(:disabled) { transform: scale(0.97); }

.onb-btn--primary {
  background: var(--color-secondary);
  color: white;
  box-shadow: 0 2px 8px rgba(93,187,105,0.25);
  width: 100%;
}
.onb-btn--primary:hover:not(:disabled) { filter: brightness(1.07); }
.onb-btn--ghost {
  background: transparent;
  border: 1.5px solid var(--color-border);
  color: var(--color-text-sub);
}
.onb-btn--ghost:hover:not(:disabled) { color: var(--color-text); background: var(--color-bg); }

/* Spinner */
.onb-spinner {
  display: inline-block;
  width: 16px; height: 16px;
  border: 2.5px solid rgba(255,255,255,0.35);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Screen-reader only */
.onb-sr-only {
  position: absolute;
  width: 1px; height: 1px;
  overflow: hidden;
  clip: rect(0,0,0,0);
  white-space: nowrap;
}

/* Responsive */
@media (max-width: 520px) {
  .onb-card { padding: 24px 20px 28px; border-radius: var(--radius-lg); }
  .onb-level-grid { gap: 8px; }
  .onb-level-btn { width: 60px; height: 60px; font-size: 16px; }
  .onb-time-grid { grid-template-columns: 1fr 1fr; }
}

@media (prefers-reduced-motion: reduce) {
  .onb-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 9. 3 TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Loading (submit)** | Button disabled + spinner, text "Đang lưu…" |
| **Error (submit fail)** | Toast `error` từ `addToast` — trang không unmount |
| **Empty** | Không áp dụng — trang tĩnh, luôn có nội dung |

---

## 10. INTERACTIONS / FLOW

```
Mount → check user.onboardingCompleted → true → navigate('/dashboard')
                                       → false → hiển thị step 1

Step 1: Chọn JLPT level → [Tiếp theo] → step 2
Step 2: Chọn thời gian → [Tiếp theo] → step 3
                       → [← Quay lại] → step 1
Step 3: Chọn kỹ năng → [Bắt đầu học!]
          → POST /api/students/onboarding
          → success → dispatch(updateOnboarding) → navigate('/dashboard')
          → error   → toast error, ở lại step 3
        → [← Quay lại] → step 2
```

---

## 11. DOMAIN RULES

- Không cho phép `focusSkills = []` — button submit disabled nếu mảng rỗng.
- Khi chọn "Tất cả" → xóa hết lựa chọn khác.
- Không cần validate jlptGoal (mặc định N5, luôn có giá trị).

---

## 12. ACCESSIBILITY

- [ ] Step dots có `aria-label="Bước X trên 3"`
- [ ] JLPT buttons dùng `role="radio"` + `aria-checked`
- [ ] Skill buttons dùng `aria-pressed`
- [ ] Time options dùng `<input type="radio">` ẩn + label visible
- [ ] Button submit có `disabled` khi loading
- [ ] Mascot `SakuChan` có `aria-hidden="true"`
