# SPEC — Tiến độ học tập (`/progress`)
>
> **Sprint:** 3 — Assessment Loop
> **Prefix:** `prg-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §11.4` | **Backend ref:** `feat-learning-analytics/SPEC.md`

---

## 1. MÔ TẢ TRANG

Trang tổng hợp tiến độ học tập cá nhân: streak, số từ đã học, biểu đồ radar năng lực 5 kỹ năng, thống kê completion (Kanji/Vocab/Grammar/Kana), và lịch sử bài thi.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="")                                           │
├──────────────────────────────────────────────────────────────────┤
│  Tiến Độ Của Tôi                                                │
│                                                                  │
│  [🔥 12 ngày] [⭐ 245 từ] [📚 18 bài] [📅 8 ngày/tháng]        │
│              (StatRow — 4 card ngang)                            │
│                                                                  │
│  ┌────────────────────────────┐  ┌────────────────────────────┐ │
│  │                            │  │  Hoàn thành nội dung       │ │
│  │    Biểu đồ Radar           │  │  ────────────────────────  │ │
│  │    Từ vựng   Ngữ pháp      │  │  Kanji      [██████░░] 62% │ │
│  │         ◆                  │  │  Từ vựng    [████░░░░] 48% │ │
│  │  Nghe  ◆ ◆  Đọc            │  │  Ngữ pháp   [███░░░░░] 35% │ │
│  │         ◆                  │  │  Kana       [██████████]100%│ │
│  │      Phát âm               │  │                            │ │
│  └────────────────────────────┘  └────────────────────────────┘ │
│                                                                  │
│  Lịch sử bài thi                                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Ngày │ Đề thi     │ Điểm  │ Kết quả                    │  │
│  │  ───── │──────────── │────── │──────────────────────────── │  │
│  │  15/05 │ N5 Tháng 12│ 85/120│ ❌ Không đậu               │  │
│  │  10/05 │ N5 Tháng 7 │ 92/120│ ✅ Đậu                     │  │
│  └──────────────────────────────────────────────────────────┘  │
│  [Pagination]                                                    │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/progress/
├── Progress.jsx
└── Progress.css

components/student/
└── SkillRadarChart.jsx   ← SVG thuần, không import chart library
```

---

## 4. STATE

```js
const [stats,       setStats]   = useState(null);    // streak, wordCount, completions, radarData
const [examHistory, setHistory] = useState([]);
const [isLoading,   setLoading] = useState(true);
const [histLoading, setHistLoad]= useState(true);
const [error,       setError]   = useState('');
const [page,        setPage]    = useState(1);
const [totalPages,  setTotal]   = useState(1);

const { user } = useAppSelector((s) => s.auth);
```

---

## 5. API CALLS

```js
// GET /api/students/me/stats
// Response:
{
  "data": {
    "currentStreak": 12,
    "longestStreak": 30,
    "wordCount": 245,
    "lessonsCompleted": 18,
    "daysThisMonth": 8,
    "completions": {
      "kanji":    { "completed": 62,  "total": 100 },
      "vocabulary":{ "completed": 245, "total": 500 },
      "grammar":  { "completed": 28,  "total": 80  },
      "kana":     { "completed": 92,  "total": 92  }
    },
    "radarData": {
      "vocabulary":    72,
      "grammar":       58,
      "reading":       30,
      "listening":     45,
      "pronunciation": 60
    }
  }
}

// GET /api/quiz-attempts/me?page=0&size=10
// Response: { data: { content: AttemptSummary[], totalPages } }
// AttemptSummary: { attemptId, assessmentTitle, jlptLevel, score, maxScore, isPassed, attemptedAt }
```

---

## 6. SkillRadarChart spec (SVG thuần)

```jsx
// components/student/SkillRadarChart.jsx
// Props: data = { vocabulary, grammar, reading, listening, pronunciation } (0-100 mỗi giá trị)

const SKILLS = [
  { key: 'vocabulary',    label: 'Từ vựng' },
  { key: 'grammar',       label: 'Ngữ pháp' },
  { key: 'reading',       label: 'Đọc hiểu' },
  { key: 'listening',     label: 'Nghe hiểu' },
  { key: 'pronunciation', label: 'Phát âm' },
];
const SIZE    = 240;
const CENTER  = SIZE / 2;
const RADIUS  = 90;
const LEVELS  = [20, 40, 60, 80, 100];

function polarToXY(angle, r) {
  const a = (angle - 90) * (Math.PI / 180);
  return { x: CENTER + r * Math.cos(a), y: CENTER + r * Math.sin(a) };
}

export default function SkillRadarChart({ data }) {
  const n = SKILLS.length;
  const angleStep = 360 / n;

  // Grid polygons
  const gridPolygons = LEVELS.map((lvl) => {
    const pts = SKILLS.map((_, i) => {
      const p = polarToXY(i * angleStep, (lvl / 100) * RADIUS);
      return `${p.x},${p.y}`;
    }).join(' ');
    return <polygon key={lvl} points={pts} fill="none" stroke="var(--color-border)" strokeWidth="1" />;
  });

  // Axis lines
  const axes = SKILLS.map((_, i) => {
    const end = polarToXY(i * angleStep, RADIUS);
    return <line key={i} x1={CENTER} y1={CENTER} x2={end.x} y2={end.y} stroke="var(--color-border)" strokeWidth="1" />;
  });

  // Data polygon
  const dataPoints = SKILLS.map((sk, i) => {
    const val = Math.min(100, Math.max(0, data?.[sk.key] ?? 0));
    const p   = polarToXY(i * angleStep, (val / 100) * RADIUS);
    return `${p.x},${p.y}`;
  }).join(' ');

  // Labels
  const labels = SKILLS.map((sk, i) => {
    const p = polarToXY(i * angleStep, RADIUS + 22);
    return (
      <text
        key={sk.key}
        x={p.x} y={p.y}
        textAnchor="middle" dominantBaseline="middle"
        fontSize="11" fill="var(--color-text-sub)" fontFamily="var(--font-base)"
      >
        {sk.label}
      </text>
    );
  });

  return (
    <svg
      width={SIZE} height={SIZE + 40}
      viewBox={`0 -20 ${SIZE} ${SIZE + 40}`}
      role="img"
      aria-label={`Biểu đồ radar năng lực: ${SKILLS.map((s) => `${s.label} ${data?.[s.key] ?? 0}%`).join(', ')}`}
    >
      {gridPolygons}
      {axes}
      <polygon
        points={dataPoints}
        fill="rgba(232,154,170,0.25)"
        stroke="var(--color-primary)"
        strokeWidth="2"
        strokeLinejoin="round"
      />
      {/* Dots */}
      {SKILLS.map((sk, i) => {
        const val = Math.min(100, Math.max(0, data?.[sk.key] ?? 0));
        const p   = polarToXY(i * angleStep, (val / 100) * RADIUS);
        return <circle key={sk.key} cx={p.x} cy={p.y} r="4" fill="var(--color-primary)" />;
      })}
      {labels}
    </svg>
  );
}
```

---

## 7. JSX STRUCTURE

```jsx
import { useState, useEffect } from 'react';
import TopNav from '../../components/layout/TopNav';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import { JlptBadge } from '../../components/common/Badges';
import SkillRadarChart from '../../components/student/SkillRadarChart';
import { getMyStats, getMyExamHistory } from '../../api/studentService';
import './Progress.css';

export default function Progress() {
  const [stats,     setStats]    = useState(null);
  const [history,   setHistory]  = useState([]);
  const [isLoading, setLoading]  = useState(true);
  const [histLoad,  setHistLoad] = useState(true);
  const [error,     setError]    = useState('');
  const [page,      setPage]     = useState(1);
  const [totalPages,setTotal]    = useState(1);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const data = await getMyStats();
        setStats(data);
      } catch {
        setError('Không thể tải thống kê.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  useEffect(() => {
    (async () => {
      setHistLoad(true);
      try {
        const res = await getMyExamHistory({ page: page - 1, size: 10 });
        setHistory(res.content);
        setTotal(res.totalPages);
      } catch {
        /* ignore — hiện empty state */
      } finally {
        setHistLoad(false);
      }
    })();
  }, [page]);

  const statItems = stats ? [
    { icon: '🔥', value: stats.currentStreak, label: 'ngày streak', sub: `Dài nhất: ${stats.longestStreak} ngày` },
    { icon: '⭐', value: stats.wordCount,      label: 'từ đã học' },
    { icon: '📚', value: stats.lessonsCompleted, label: 'bài đã học' },
    { icon: '📅', value: stats.daysThisMonth,  label: 'ngày học tháng này' },
  ] : [];

  return (
    <div className="prg-page">
      <TopNav activeTab="" />
      <main className="prg-body">
        <h1 className="prg-title">Tiến Độ Của Tôi</h1>

        {error && <div className="prg-error" role="alert">{error}</div>}

        {/* Stat row */}
        {isLoading
          ? <div className="prg-stat-row">{[1,2,3,4].map((i)=><div key={i} className="prg-skel prg-skel--stat" aria-hidden="true"/>)}</div>
          : (
            <div className="prg-stat-row">
              {statItems.map((s, i) => (
                <div key={i} className="prg-stat-card">
                  <span className="prg-stat-icon" aria-hidden="true">{s.icon}</span>
                  <div className="prg-stat-body">
                    <span className="prg-stat-value">{s.value}</span>
                    <span className="prg-stat-label">{s.label}</span>
                    {s.sub && <span className="prg-stat-sub">{s.sub}</span>}
                  </div>
                </div>
              ))}
            </div>
          )
        }

        {/* Charts row */}
        {!isLoading && stats && (
          <div className="prg-charts-row">
            {/* Radar */}
            <div className="prg-chart-card">
              <h2 className="prg-card-title">Năng lực kỹ năng</h2>
              <div className="prg-radar-wrap">
                <SkillRadarChart data={stats.radarData} />
              </div>
            </div>

            {/* Completions */}
            <div className="prg-chart-card">
              <h2 className="prg-card-title">Hoàn thành nội dung</h2>
              <div className="prg-completions">
                {Object.entries(stats.completions).map(([key, val]) => {
                  const pct = Math.round((val.completed / val.total) * 100);
                  const labels = { kanji: 'Kanji', vocabulary: 'Từ vựng', grammar: 'Ngữ pháp', kana: 'Kana' };
                  return (
                    <div key={key} className="prg-comp-row">
                      <span className="prg-comp-label">{labels[key] ?? key}</span>
                      <div className="prg-comp-bar-wrap">
                        <div className="prg-comp-bar">
                          <div className="prg-comp-fill" style={{ width: `${pct}%` }} />
                        </div>
                      </div>
                      <span className="prg-comp-count">{val.completed}/{val.total}</span>
                      <span className="prg-comp-pct">{pct}%</span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* Exam history */}
        <section>
          <h2 className="prg-section-title">Lịch sử bài thi</h2>
          {histLoad
            ? <div className="prg-skel prg-skel--table" aria-hidden="true" />
            : history.length === 0
              ? (
                <EmptyState
                  title="Chưa có bài thi nào"
                  subtitle="Thử sức với đề thi thử JLPT ngay!"
                  mascotVariant="thinking"
                  mascotSize={120}
                >
                  <a href="/mock-test" className="prg-cta-btn">Xem đề thi →</a>
                </EmptyState>
              )
              : (
                <>
                  <div className="prg-table-wrap">
                    <table className="prg-table">
                      <thead>
                        <tr>
                          <th scope="col">Ngày thi</th>
                          <th scope="col">Đề thi</th>
                          <th scope="col">Điểm</th>
                          <th scope="col">Kết quả</th>
                          <th scope="col" aria-label="Xem chi tiết" />
                        </tr>
                      </thead>
                      <tbody>
                        {history.map((h, i) => (
                          <tr key={h.attemptId} className="prg-tr" style={{ '--row-i': i }}>
                            <td><time dateTime={h.attemptedAt}>{new Date(h.attemptedAt).toLocaleDateString('vi-VN')}</time></td>
                            <td>
                              <div className="prg-exam-cell">
                                <JlptBadge level={h.jlptLevel} />
                                <span>{h.assessmentTitle}</span>
                              </div>
                            </td>
                            <td className="prg-td-score">{h.score}/{h.maxScore}</td>
                            <td>
                              <span className={`prg-result-badge${h.isPassed ? ' prg-result-badge--pass' : ' prg-result-badge--fail'}`}>
                                {h.isPassed ? '✅ Đậu' : '❌ Không đậu'}
                              </span>
                            </td>
                            <td>
                              <a href={`/mock-test/${h.assessmentId}/results?attemptId=${h.attemptId}`} className="prg-detail-link">
                                Xem chi tiết →
                              </a>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  {totalPages > 1 && <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />}
                </>
              )
          }
        </section>
      </main>
    </div>
  );
}
```

---

## 8. CSS

```css
/* ===== Progress (SakuJi Hanami Theme) ===== */
.prg-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.prg-body { flex: 1; max-width: 1100px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 24px; box-sizing: border-box; }
.prg-title { font-size: 26px; font-weight: 700; color: var(--color-text); margin: 0; }
.prg-error { background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }

/* Stat row */
.prg-stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.prg-stat-card { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); padding: 18px 20px; display: flex; align-items: flex-start; gap: 12px; }
.prg-stat-icon { font-size: 28px; flex-shrink: 0; }
.prg-stat-body { display: flex; flex-direction: column; gap: 2px; }
.prg-stat-value { font-size: 28px; font-weight: 800; color: var(--color-text); line-height: 1; }
.prg-stat-label { font-size: 13px; color: var(--color-text-sub); }
.prg-stat-sub   { font-size: 11px; color: var(--color-text-disabled); }

/* Charts row */
.prg-charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.prg-chart-card { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); padding: 20px 24px; }
.prg-card-title { font-size: 16px; font-weight: 700; color: var(--color-text); margin: 0 0 16px; }
.prg-radar-wrap { display: flex; justify-content: center; }

/* Completions */
.prg-completions { display: flex; flex-direction: column; gap: 12px; }
.prg-comp-row { display: flex; align-items: center; gap: 10px; }
.prg-comp-label { width: 70px; font-size: 13px; font-weight: 600; color: var(--color-text-sub); flex-shrink: 0; }
.prg-comp-bar-wrap { flex: 1; }
.prg-comp-bar { height: 8px; background: var(--color-border); border-radius: var(--radius-full); overflow: hidden; }
.prg-comp-fill { height: 100%; background: var(--color-primary); border-radius: var(--radius-full); transition: width 0.8s ease; }
.prg-comp-count { font-size: 12px; color: var(--color-text-sub); white-space: nowrap; }
.prg-comp-pct   { width: 36px; font-size: 13px; font-weight: 700; color: var(--color-text); text-align: right; }

/* Section title */
.prg-section-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; }

/* Table */
.prg-table-wrap { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); overflow: hidden; overflow-x: auto; }
.prg-table { width: 100%; border-collapse: collapse; }
.prg-table thead { background: var(--color-bg); border-bottom: 1px solid var(--color-border); }
.prg-table th { padding: 10px 16px; font-size: 11px; font-weight: 700; color: var(--color-text-sub); text-transform: uppercase; letter-spacing: 0.5px; text-align: left; }
.prg-table td { padding: 12px 16px; font-size: 14px; color: var(--color-text); border-bottom: 1px solid var(--color-border); }
.prg-tr:last-child td { border-bottom: none; }
.prg-tr { transition: background var(--transition); animation: rowIn 0.25s ease both; animation-delay: calc(var(--row-i) * 30ms); }
.prg-tr:hover { background: var(--color-bg); }
@keyframes rowIn { from { opacity: 0; transform: translateX(-6px); } to { opacity: 1; transform: translateX(0); } }

.prg-exam-cell { display: flex; align-items: center; gap: 8px; }
.prg-td-score { font-weight: 700; }
.prg-result-badge { font-size: 12px; font-weight: 700; padding: 3px 10px; border-radius: var(--radius-full); }
.prg-result-badge--pass { background: var(--color-secondary-bg); color: var(--color-secondary); }
.prg-result-badge--fail { background: #FEF2F2; color: var(--color-error); }
.prg-detail-link { font-size: 13px; color: var(--color-primary); text-decoration: none; font-weight: 600; }
.prg-detail-link:hover { text-decoration: underline; }
.prg-cta-btn { display: inline-flex; align-items: center; height: 40px; padding: 0 20px; background: var(--color-secondary); color: white; border-radius: var(--radius-full); text-decoration: none; font-size: 14px; font-weight: 700; }

/* Skeletons */
.prg-skel { border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
.prg-skel--stat  { height: 88px; }
.prg-skel--table { height: 250px; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

@media (max-width: 1199px) {
  .prg-stat-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 767px) {
  .prg-body { padding: 16px 16px 32px; }
  .prg-stat-row { grid-template-columns: 1fr 1fr; gap: 10px; }
  .prg-charts-row { grid-template-columns: 1fr; }
}

@media (prefers-reduced-motion: reduce) {
  .prg-page * { animation: none !important; transition-duration: 0ms !important; }
  .prg-comp-fill { transition: none; }
}
```
