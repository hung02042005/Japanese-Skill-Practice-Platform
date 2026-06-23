# SPEC — Danh sách Kanji (`/kanji`)
>
> **Sprint:** 4 — Monetization & Retention
> **Prefix:** `knj-` | **activeTab:** `'kanji'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §12.3` | **Backend ref:** `feat-core-learning/SPEC.md UC-07`

---

## 1. MÔ TẢ TRANG

Lưới Kanji theo JLPT level. Filter tabs N5–N1. Mỗi ô hiển thị ký tự, tick khi đã học. Click → `/kanji/:id`. Thanh stats hiển thị đã học bao nhiêu / tổng số.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="kanji")                                      │
├──────────────────────────────────────────────────────────────────┤
│  漢字 Kanji                                                     │
│  Luyện tập và tra cứu Kanji theo cấp độ JLPT.                  │
│                                                                  │
│  [N5]  [N4]  [N3]  [N2]  [N1]                                   │
│                                                                  │
│  N5: đã học 62 / 103 kanji  [███████░░░░░░] 60%                │
│                                                                  │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐      │
│  │ 日 │ │ 月 ✓│ │ 火 │ │ 水 ✓│ │ 木 ✓│ │ 金 │ │ 土 │ │ 山 ✓│   │
│  └────┘ └────┘ └────┘ └────┘ └────┘ └────┘ └────┘ └────┘      │
│  (8 cột, lưới kanji)                                            │
│                                                                  │
│  [Pagination]                                                    │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/kanji/
├── KanjiList.jsx
└── KanjiList.css

components/student/
└── KanjiGrid.jsx   ← lưới kanji cells
```

---

## 4. STATE

```js
const [level,       setLevel]  = useState(user?.jlptLevel ?? 'N5');
const [kanji,       setKanji]  = useState([]);
const [stats,       setStats]  = useState({ completed: 0, total: 0 });
const [isLoading,   setLoading]= useState(true);
const [error,       setError]  = useState('');
const [page,        setPage]   = useState(1);
const [totalPages,  setTotal]  = useState(1);
const PAGE_SIZE = 50;
```

---

## 5. API CALLS

```js
// GET /api/kanji?level=N5&page=0&size=50
// Response:
{
  "data": {
    "content": [
      {
        "kanjiId": 1,
        "characterValue": "日",
        "meaning": "Ngày, Mặt trời",
        "jlptLevel": "N5",
        "isCompleted": false
      }
    ],
    "totalElements": 103,
    "totalPages": 3,
    "completedCount": 62   // đã học ở level này
  }
}
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { ProgressBar } from '../../components/common/ProgressBar';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import { getKanjiList } from '../../api/studentService';
import './KanjiList.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function KanjiList() {
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);

  const [level,    setLevel]  = useState(user?.jlptLevel ?? 'N5');
  const [kanji,    setKanji]  = useState([]);
  const [stats,    setStats]  = useState({ completed: 0, total: 0 });
  const [isLoading,setLoading]= useState(true);
  const [error,    setError]  = useState('');
  const [page,     setPage]   = useState(1);
  const [totalPages,setTotal] = useState(1);

  const fetchKanji = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const res = await getKanjiList({ level, page: page - 1, size: 50 });
      setKanji(res.content);
      setTotal(res.totalPages);
      setStats({ completed: res.completedCount ?? 0, total: res.totalElements });
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải danh sách Kanji.');
    } finally {
      setLoading(false);
    }
  }, [level, page]);

  useEffect(() => { fetchKanji(); }, [fetchKanji]);
  useEffect(() => { setPage(1); }, [level]);

  const progressPct = stats.total > 0 ? Math.round((stats.completed / stats.total) * 100) : 0;

  return (
    <div className="knj-page">
      <TopNav activeTab="kanji" />
      <main className="knj-body">
        <div className="knj-page-header">
          <h1 className="knj-title"><span lang="ja">漢字</span> Kanji</h1>
          <p className="knj-subtitle">Luyện tập và tra cứu Kanji theo cấp độ JLPT.</p>
        </div>

        {/* Level filter */}
        <div className="knj-level-tabs" role="tablist" aria-label="Chọn cấp độ JLPT">
          {LEVELS.map((l) => (
            <button
              key={l}
              role="tab"
              aria-selected={level === l}
              className={`knj-level-tab${level === l ? ' knj-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}
            >
              {l}
            </button>
          ))}
        </div>

        {/* Progress stats */}
        {!isLoading && (
          <div className="knj-stats-bar">
            <JlptBadge level={level} />
            <span className="knj-stats-text">đã học <strong>{stats.completed}</strong> / {stats.total} kanji</span>
            <div className="knj-stats-progress">
              <ProgressBar value={progressPct} />
            </div>
            <span className="knj-stats-pct">{progressPct}%</span>
          </div>
        )}

        {/* Error */}
        {error && (
          <div className="knj-error" role="alert">
            <span>{error}</span>
            <button className="knj-retry-btn" onClick={fetchKanji}>Thử lại</button>
          </div>
        )}

        {/* Grid */}
        {isLoading ? (
          <div className="knj-grid">
            {Array.from({ length: 40 }).map((_, i) => (
              <div key={i} className="knj-cell-skel" aria-hidden="true" />
            ))}
          </div>
        ) : kanji.length === 0 ? (
          <EmptyState
            title={`Chưa có Kanji ${level}`}
            subtitle="Nội dung đang được cập nhật. Thử level khác nhé!"
            mascotVariant="thinking"
            mascotSize={140}
          />
        ) : (
          <div className="knj-grid" role="list" aria-label={`Danh sách Kanji ${level}`}>
            {kanji.map((k) => (
              <button
                key={k.kanjiId}
                role="listitem"
                className={`knj-cell${k.isCompleted ? ' knj-cell--done' : ''}`}
                onClick={() => navigate(`/kanji/${k.kanjiId}`)}
                aria-label={`${k.characterValue} — ${k.meaning}${k.isCompleted ? ' (đã học)' : ''}`}
                title={k.meaning}
              >
                <span className="knj-char" lang="ja">{k.characterValue}</span>
                {k.isCompleted && <span className="knj-done-tick" aria-hidden="true">✓</span>}
              </button>
            ))}
          </div>
        )}

        {!isLoading && totalPages > 1 && (
          <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
        )}
      </main>
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Kanji List (SakuJi Hanami Theme) ===== */
.knj-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.knj-body { flex: 1; max-width: 1100px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.knj-page-header { display: flex; flex-direction: column; gap: 4px; }
.knj-title { font-size: 26px; font-weight: 700; color: var(--color-text); margin: 0; }
.knj-subtitle { font-size: 14px; color: var(--color-text-sub); margin: 0; }

/* Level filter */
.knj-level-tabs { display: flex; gap: 0; border-bottom: 2px solid var(--color-border); }
.knj-level-tab { padding: 10px 20px; font-size: 15px; font-weight: 700; color: var(--color-text-sub); background: transparent; border: none; border-bottom: 2px solid transparent; margin-bottom: -2px; cursor: pointer; transition: color var(--transition), border-color var(--transition); }
.knj-level-tab:hover { color: var(--color-text); }
.knj-level-tab--active { color: var(--color-primary); border-bottom-color: var(--color-primary); }

/* Stats bar */
.knj-stats-bar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.knj-stats-text { font-size: 14px; color: var(--color-text-sub); }
.knj-stats-progress { flex: 1; min-width: 120px; max-width: 300px; }
.knj-stats-pct { font-size: 14px; font-weight: 700; color: var(--color-text); }

/* Error */
.knj-error { display: flex; align-items: center; justify-content: space-between; gap: 12px; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.knj-retry-btn { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }

/* Kanji grid */
.knj-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 8px; }

/* Kanji cell */
.knj-cell {
  position: relative;
  aspect-ratio: 1;
  background: var(--color-card);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: border-color var(--transition), background var(--transition), transform var(--transition), box-shadow var(--transition);
}
.knj-cell:hover { border-color: var(--color-primary-light); background: var(--color-primary-bg); transform: scale(1.06); box-shadow: var(--shadow-sm); }
.knj-cell:active { transform: scale(0.97); }
.knj-cell--done { background: var(--color-primary-bg); border-color: var(--color-primary-light); }

.knj-char { font-size: 26px; font-weight: 700; color: var(--color-text); line-height: 1; }
.knj-cell--done .knj-char { color: var(--color-primary); }

.knj-done-tick {
  position: absolute; bottom: 3px; right: 5px;
  font-size: 11px; color: var(--color-primary); font-weight: 700;
}

/* Skeleton cells */
.knj-cell-skel { aspect-ratio: 1; border-radius: var(--radius-md); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

@media (max-width: 1199px) { .knj-grid { grid-template-columns: repeat(6, 1fr); } .knj-char { font-size: 22px; } }
@media (max-width: 767px)  { .knj-body { padding: 16px 16px 32px; } .knj-grid { grid-template-columns: repeat(4, 1fr); gap: 6px; } .knj-char { font-size: 20px; } }
@media (max-width: 400px)  { .knj-grid { grid-template-columns: repeat(4, 1fr); } }

@media (prefers-reduced-motion: reduce) { .knj-page * { animation: none !important; transition-duration: 0ms !important; } }
```
