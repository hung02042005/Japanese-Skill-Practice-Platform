# SPEC — Học từ mới (`/learn/new`)
> **Sprint:** 2 — Core Learning Loop
> **Prefix:** `lnw-` | **activeTab:** `'learn'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §10.2` | **Backend ref:** `feat-core-learning/SPEC.md`

---

## 1. MÔ TẢ TRANG

Điểm vào học bài tiếp theo. Hiển thị bài học chưa hoàn thành tiếp theo theo lộ trình của user (theo JLPT level), cùng danh sách 3–5 bài gợi ý. Click "Bắt đầu học" → navigate `/lessons/:id`.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="learn")                                      │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Học Từ Mới                                                     │
│  Tiếp tục lộ trình N5 của bạn                                   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Bài tiếp theo của bạn                                   │  │
│  │                                                          │  │
│  │  [N5]  Chào hỏi cơ bản  · Từ vựng · ~15 phút           │  │
│  │  "Học các từ ngữ chào hỏi phổ biến trong tiếng Nhật"   │  │
│  │                                                          │  │
│  │  Tiến độ: [████░░░░░░░░░] 35%                           │  │
│  │                                                          │  │
│  │                      [▶ Bắt đầu học →]                  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ─── Bài học khác trong level ──────────────────────────────── │
│                                                                  │
│  ┌────────────────────────┐  ┌────────────────────────┐        │
│  │  [N5] Màu sắc · Từ vựng│  │  [N5] Gia đình · Từ vựng│       │
│  │  ~10 phút · 20%        │  │  ~12 phút · 0%          │       │
│  └────────────────────────┘  └────────────────────────┘        │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/learn/
├── LearnNew.jsx
└── LearnNew.css
```

---

## 4. STATE

```js
const [nextLesson,  setNext]    = useState(null);
const [suggestions, setSuggest] = useState([]);
const [isLoading,   setLoading] = useState(true);
const [error,       setError]   = useState('');
const { user } = useAppSelector((s) => s.auth);
```

---

## 5. API CALLS

```js
// GET /api/students/next-lesson
// Response:
{
  "data": {
    "nextLesson": {
      "lessonId": "long",
      "title": "string",
      "description": "string",
      "lessonType": "string",
      "jlptLevel": "N5",
      "estimatedMinutes": 15,
      "progressPercent": 35
    } | null,  // null = hoàn thành hết
    "suggestedLessons": [
      { "lessonId", "title", "lessonType", "jlptLevel", "estimatedMinutes", "progressPercent" }
    ]
  }
}
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { getNextLesson } from '../../api/studentService';
import './LearnNew.css';

export default function LearnNew() {
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);

  const [nextLesson,  setNext]   = useState(null);
  const [suggestions, setSuggest]= useState([]);
  const [isLoading,   setLoading]= useState(true);
  const [error,       setError]  = useState('');

  useEffect(() => {
    (async () => {
      setLoading(true); setError('');
      try {
        const data = await getNextLesson();
        setNext(data.nextLesson);
        setSuggest(data.suggestedLessons ?? []);
      } catch {
        setError('Không thể tải bài học. Thử lại sau.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <div className="lnw-page">
      <TopNav activeTab="learn" />
      <main className="lnw-body">
        <div className="lnw-page-header">
          <h1 className="lnw-title">Học Từ Mới</h1>
          <p className="lnw-subtitle">Tiếp tục lộ trình {user?.jlptLevel ?? 'N5'} của bạn</p>
        </div>

        {error && <div className="lnw-error" role="alert"><span>{error}</span><button onClick={() => window.location.reload()}>Thử lại</button></div>}

        {isLoading ? (
          <>
            <div className="lnw-skel lnw-skel--hero" aria-hidden="true" />
            <div className="lnw-suggest-grid">{[1,2].map((i) => <div key={i} className="lnw-skel lnw-skel--card" aria-hidden="true" />)}</div>
          </>
        ) : nextLesson ? (
          <>
            {/* Next lesson card */}
            <div className="lnw-next-card">
              <div className="lnw-next-label">Bài tiếp theo của bạn</div>
              <div className="lnw-next-header">
                <JlptBadge level={nextLesson.jlptLevel} />
                <h2 className="lnw-next-title">{nextLesson.title}</h2>
                <span className="lnw-next-meta">{nextLesson.lessonType} · ~{nextLesson.estimatedMinutes} phút</span>
              </div>
              {nextLesson.description && <p className="lnw-next-desc">"{nextLesson.description}"</p>}
              <div className="lnw-next-progress">
                <span className="lnw-prog-label">Tiến độ</span>
                <ProgressBar value={nextLesson.progressPercent} />
                <span className="lnw-prog-pct">{nextLesson.progressPercent}%</span>
              </div>
              <button
                className="lnw-start-btn"
                onClick={() => navigate(`/lessons/${nextLesson.lessonId}`)}
              >
                ▶ Bắt đầu học →
              </button>
            </div>

            {/* Suggestions */}
            {suggestions.length > 0 && (
              <section>
                <h2 className="lnw-suggest-title">Bài học khác trong level</h2>
                <div className="lnw-suggest-grid">
                  {suggestions.map((s) => (
                    <Link
                      key={s.lessonId}
                      to={`/lessons/${s.lessonId}`}
                      className="lnw-suggest-card"
                    >
                      <div className="lnw-sug-header">
                        <JlptBadge level={s.jlptLevel} />
                        <span className="lnw-sug-type">{s.lessonType}</span>
                      </div>
                      <div className="lnw-sug-title">{s.title}</div>
                      <div className="lnw-sug-meta">~{s.estimatedMinutes} phút</div>
                      <ProgressBar value={s.progressPercent} />
                    </Link>
                  ))}
                </div>
              </section>
            )}
          </>
        ) : (
          <EmptyState
            title="Xuất sắc! 🎉 Bạn đã học hết bài của level này!"
            subtitle="Chúc mừng! Hãy tiếp tục với level tiếp theo hoặc ôn tập lại."
            mascotVariant="celebrate"
            mascotSize={180}
          >
            <Link to="/dashboard" className="lnw-cta-btn">Về Dashboard</Link>
            <Link to="/mock-test" className="lnw-cta-btn lnw-cta-btn--outline">Thi thử →</Link>
          </EmptyState>
        )}
      </main>
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Learn New (SakuJi Hanami Theme) ===== */
.lnw-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.lnw-body { flex: 1; max-width: 800px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 24px; box-sizing: border-box; }
.lnw-page-header { display: flex; flex-direction: column; gap: 4px; }
.lnw-title    { font-size: 26px; font-weight: 700; color: var(--color-text); margin: 0; }
.lnw-subtitle { font-size: 14px; color: var(--color-text-sub); margin: 0; }
.lnw-error { display: flex; align-items: center; justify-content: space-between; gap: 12px; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.lnw-next-card { background: var(--color-card); border-radius: var(--radius-xl); box-shadow: var(--shadow-md); padding: 28px 32px; display: flex; flex-direction: column; gap: 16px; }
.lnw-next-label { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: var(--color-text-sub); }
.lnw-next-header { display: flex; flex-direction: column; gap: 6px; }
.lnw-next-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin: 0; }
.lnw-next-meta  { font-size: 13px; color: var(--color-text-sub); }
.lnw-next-desc  { font-size: 14px; color: var(--color-text-sub); font-style: italic; margin: 0; line-height: 1.6; }
.lnw-next-progress { display: flex; align-items: center; gap: 10px; }
.lnw-prog-label { font-size: 12px; font-weight: 600; color: var(--color-text-sub); white-space: nowrap; }
.lnw-prog-pct   { font-size: 12px; font-weight: 700; color: var(--color-text-sub); white-space: nowrap; }
.lnw-start-btn { display: inline-flex; align-items: center; justify-content: center; gap: 7px; height: 48px; padding: 0 32px; background: var(--color-secondary); color: white; border: none; border-radius: var(--radius-full); font-family: var(--font-base); font-size: 15px; font-weight: 700; cursor: pointer; box-shadow: 0 2px 8px rgba(93,187,105,0.25); align-self: flex-start; transition: filter var(--transition), transform var(--transition); }
.lnw-start-btn:hover { filter: brightness(1.07); }
.lnw-start-btn:active { transform: scale(0.97); }
.lnw-suggest-title { font-size: 17px; font-weight: 700; color: var(--color-text); margin: 0 0 12px; }
.lnw-suggest-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.lnw-suggest-card { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); padding: 16px 18px; display: flex; flex-direction: column; gap: 8px; text-decoration: none; transition: box-shadow var(--transition), transform var(--transition); }
.lnw-suggest-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.lnw-sug-header { display: flex; align-items: center; gap: 8px; }
.lnw-sug-type   { font-size: 11px; font-weight: 700; color: var(--color-text-sub); text-transform: uppercase; }
.lnw-sug-title  { font-size: 14px; font-weight: 700; color: var(--color-text); }
.lnw-sug-meta   { font-size: 12px; color: var(--color-text-sub); }
.lnw-cta-btn { display: inline-flex; align-items: center; height: 42px; padding: 0 24px; background: var(--color-secondary); color: white; border: none; border-radius: var(--radius-full); text-decoration: none; font-size: 14px; font-weight: 700; transition: filter var(--transition); }
.lnw-cta-btn:hover { filter: brightness(1.07); }
.lnw-cta-btn--outline { background: transparent; border: 1.5px solid var(--color-primary); color: var(--color-primary); }
.lnw-cta-btn--outline:hover { background: var(--color-primary-bg); filter: none; }
.lnw-skel { border-radius: var(--radius-xl); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
.lnw-skel--hero { height: 200px; }
.lnw-skel--card { height: 130px; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
@media (max-width: 767px) { .lnw-body { padding: 16px 16px 32px; } .lnw-suggest-grid { grid-template-columns: 1fr 1fr; } .lnw-start-btn { width: 100%; } }
@media (prefers-reduced-motion: reduce) { .lnw-page * { animation: none !important; transition-duration: 0ms !important; } }
```
