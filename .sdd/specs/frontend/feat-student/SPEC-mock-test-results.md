# SPEC — Kết quả thi (`/mock-test/:id/results`)
> **Sprint:** 3 — Assessment Loop
> **Prefix:** `mxr-` | **activeTab:** `'mock-test'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §11.3` | **Backend ref:** `feat-assessment/SPEC.md`
> **Query param:** `?attemptId=xxx`

---

## 1. MÔ TẢ TRANG

Hiển thị kết quả sau khi nộp bài thi. Bất biến — không thể sửa. Gồm: tổng điểm, section scores, so sánh lần trước, bảng review từng câu.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="mock-test")                                  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ← Về danh sách đề thi                                          │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Đề thi N5 — Tháng 12/2024          15/05/2026 10:30    │  │
│  │                                                          │  │
│  │       85 / 120                    [❌ KHÔNG ĐẬU]         │  │
│  │   điểm của bạn                  Cần: 90 điểm            │  │
│  │                                                          │  │
│  │  Từ vựng  [████████░░] 72%   Ngữ pháp [██████░░░░] 58%  │  │
│  │  Đọc hiểu [███░░░░░░░] 30%                               │  │
│  │                                                          │  │
│  │  So với lần trước: ↑ +5 điểm (80 → 85)                  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Chi tiết từng câu                                              │
│  ─────────────────────────────────────────────────────────────  │
│  Câu │ Kỹ năng │ Đáp án bạn │ Đáp án đúng │ Kết quả           │
│  ─── │──────── │─────────── │──────────── │──────────          │
│   1  │ Từ vựng │     B      │      B      │   ✅               │
│   2  │ Ngữ pháp│     A      │      C      │   ❌               │
│   3  │ Từ vựng │   —        │      D      │   ❌ (bỏ)          │
│   ...│         │            │             │                    │
│                                                                  │
│  [Thi lại]    [Về Dashboard]    [Xem bài học liên quan]        │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/mock-test/
├── MockTestResults.jsx
└── MockTestResults.css
```

---

## 4. STATE

```js
const { id } = useParams();
const [searchParams] = useSearchParams();
const attemptId = searchParams.get('attemptId');

const [result,    setResult]  = useState(null);
const [isLoading, setLoading] = useState(true);
const [error,     setError]   = useState('');
```

---

## 5. API CALLS

```js
// GET /api/quiz-attempts/:attemptId
// Response:
{
  "data": {
    "attemptId": "long",
    "assessmentId": "long",
    "assessmentTitle": "string",
    "jlptLevel": "N5",
    "attemptedAt": "datetime",
    "score": 85,
    "maxScore": 120,
    "passScore": 90,
    "isPassed": false,
    "sectionScores": {
      "vocabulary": { "score": 30, "max": 40, "percent": 72 },
      "grammar":    { "score": 23, "max": 40, "percent": 58 },
      "reading":    { "score": 12, "max": 40, "percent": 30 }
    },
    "previousAttempt": { "score": 80, "attemptedAt": "..." } | null,
    "questionResults": [
      {
        "questionNumber": 1,
        "questionText": "...",
        "skill": "vocabulary",
        "selectedOption": "B",
        "correctOption": "B",
        "isCorrect": true,
        "explanation": "..."
      }
    ]
  }
}
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect } from 'react';
import { useParams, useSearchParams, Link, useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { getQuizAttemptResult } from '../../api/studentService';
import './MockTestResults.css';

const SKILL_LABELS = {
  vocabulary: 'Từ vựng',
  grammar:    'Ngữ pháp',
  reading:    'Đọc hiểu',
  listening:  'Nghe hiểu',
};

export default function MockTestResults() {
  const { id }  = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const attemptId = searchParams.get('attemptId');

  const [result,    setResult]  = useState(null);
  const [isLoading, setLoading] = useState(true);
  const [error,     setError]   = useState('');

  useEffect(() => {
    if (!attemptId) { navigate('/mock-test'); return; }
    (async () => {
      setLoading(true);
      try {
        const data = await getQuizAttemptResult(attemptId);
        setResult(data);
      } catch (err) {
        setError(err?.response?.data?.message ?? 'Không thể tải kết quả.');
      } finally {
        setLoading(false);
      }
    })();
  }, [attemptId, navigate]);

  return (
    <div className="mxr-page">
      <TopNav activeTab="mock-test" />
      <main className="mxr-body">
        <Link to="/mock-test" className="mxr-back-link">← Về danh sách đề thi</Link>

        {/* Error */}
        {error && (
          <div className="mxr-error-banner" role="alert">{error}</div>
        )}

        {/* Loading */}
        {isLoading && (
          <>
            <div className="mxr-skel mxr-skel--hero"  aria-hidden="true" />
            <div className="mxr-skel mxr-skel--table" aria-hidden="true" />
          </>
        )}

        {/* Result */}
        {!isLoading && result && (
          <>
            {/* Hero card */}
            <div className={`mxr-hero-card${result.isPassed ? ' mxr-hero-card--passed' : ' mxr-hero-card--failed'}`}>
              <div className="mxr-hero-header">
                <JlptBadge level={result.jlptLevel} />
                <h1 className="mxr-hero-title">{result.assessmentTitle}</h1>
                <time className="mxr-hero-date" dateTime={result.attemptedAt}>
                  {new Date(result.attemptedAt).toLocaleString('vi-VN')}
                </time>
              </div>

              <div className="mxr-hero-score-row">
                <div className="mxr-score-block">
                  <span className="mxr-score-num">{result.score}</span>
                  <span className="mxr-score-max">/ {result.maxScore}</span>
                  <span className="mxr-score-label">điểm</span>
                </div>
                <div className={`mxr-pass-badge${result.isPassed ? ' mxr-pass-badge--passed' : ' mxr-pass-badge--failed'}`}>
                  {result.isPassed ? '✅ ĐẬU' : '❌ KHÔNG ĐẬU'}
                  <span className="mxr-pass-note">Cần: {result.passScore} điểm</span>
                </div>
              </div>

              {/* Section scores */}
              <div className="mxr-sections">
                {Object.entries(result.sectionScores).map(([key, val]) => (
                  <div key={key} className="mxr-section-row">
                    <span className="mxr-section-name">{SKILL_LABELS[key] ?? key}</span>
                    <div className="mxr-section-bar-wrap">
                      <div className="mxr-section-bar">
                        <div className="mxr-section-fill" style={{ width: `${val.percent}%` }} />
                      </div>
                    </div>
                    <span className="mxr-section-pct">{val.percent}%</span>
                  </div>
                ))}
              </div>

              {/* Compare previous */}
              {result.previousAttempt && (
                <div className="mxr-compare">
                  {result.score > result.previousAttempt.score
                    ? `📈 Tốt hơn lần trước: ↑ +${result.score - result.previousAttempt.score} điểm (${result.previousAttempt.score} → ${result.score})`
                    : result.score < result.previousAttempt.score
                    ? `📉 Thấp hơn lần trước: ↓ -${result.previousAttempt.score - result.score} điểm`
                    : `↔️ Bằng lần trước: ${result.score} điểm`
                  }
                </div>
              )}
            </div>

            {/* Question review table */}
            <section aria-label="Chi tiết từng câu hỏi">
              <h2 className="mxr-section-heading">Chi tiết từng câu</h2>
              <div className="mxr-table-wrap">
                <table className="mxr-table">
                  <thead>
                    <tr>
                      <th scope="col">Câu</th>
                      <th scope="col">Kỹ năng</th>
                      <th scope="col">Đáp án bạn</th>
                      <th scope="col">Đáp án đúng</th>
                      <th scope="col" aria-label="Kết quả" />
                    </tr>
                  </thead>
                  <tbody>
                    {result.questionResults.map((qr) => (
                      <tr key={qr.questionNumber} className={`mxr-tr${qr.isCorrect ? ' mxr-tr--correct' : ' mxr-tr--wrong'}`}>
                        <td className="mxr-td-num">{qr.questionNumber}</td>
                        <td>{SKILL_LABELS[qr.skill] ?? qr.skill}</td>
                        <td className="mxr-td-option">{qr.selectedOption ?? <span className="mxr-skipped">—</span>}</td>
                        <td className="mxr-td-option mxr-td-correct">{qr.correctOption}</td>
                        <td className="mxr-td-result">
                          {qr.isCorrect
                            ? <span className="mxr-result-icon mxr-result-icon--ok" aria-label="Đúng">✅</span>
                            : <span className="mxr-result-icon mxr-result-icon--fail" aria-label={qr.selectedOption ? 'Sai' : 'Bỏ qua'}>❌</span>
                          }
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>

            {/* CTA footer */}
            <div className="mxr-footer-ctas">
              <button className="mxr-btn mxr-btn--outline" onClick={() => navigate(`/mock-test/${id}/attempt`)}>
                Thi lại
              </button>
              <Link to="/dashboard" className="mxr-btn mxr-btn--ghost">Về Dashboard</Link>
              <Link to="/learn/new" className="mxr-btn mxr-btn--primary">Học bài liên quan</Link>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Mock Test Results (SakuJi Hanami Theme) ===== */

.mxr-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.mxr-body { flex: 1; max-width: 900px; width: 100%; margin: 0 auto; padding: 24px 32px 48px; display: flex; flex-direction: column; gap: 24px; box-sizing: border-box; }
.mxr-back-link { font-size: 13px; color: var(--color-text-sub); text-decoration: none; font-weight: 600; }
.mxr-back-link:hover { color: var(--color-primary); }

/* Hero card */
.mxr-hero-card {
  background: var(--color-card); border-radius: var(--radius-xl); box-shadow: var(--shadow-md);
  padding: 28px 32px; display: flex; flex-direction: column; gap: 20px;
  border-top: 4px solid var(--color-border);
}
.mxr-hero-card--passed { border-top-color: var(--color-secondary); }
.mxr-hero-card--failed { border-top-color: var(--color-error); }

.mxr-hero-header { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.mxr-hero-title  { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; flex: 1; }
.mxr-hero-date   { font-size: 13px; color: var(--color-text-sub); }

.mxr-hero-score-row { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; }
.mxr-score-block { display: flex; align-items: baseline; gap: 4px; }
.mxr-score-num  { font-size: 52px; font-weight: 800; color: var(--color-text); line-height: 1; }
.mxr-score-max  { font-size: 24px; font-weight: 400; color: var(--color-text-sub); }
.mxr-score-label{ font-size: 14px; color: var(--color-text-sub); margin-left: 4px; }

.mxr-pass-badge {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 12px 20px; border-radius: var(--radius-lg);
  font-size: 18px; font-weight: 800;
}
.mxr-pass-badge--passed { background: var(--color-secondary-bg); color: var(--color-secondary); }
.mxr-pass-badge--failed { background: #FEF2F2; color: var(--color-error); }
.mxr-pass-note { font-size: 12px; font-weight: 400; }

/* Section bars */
.mxr-sections { display: flex; flex-direction: column; gap: 10px; }
.mxr-section-row { display: flex; align-items: center; gap: 12px; }
.mxr-section-name { width: 80px; font-size: 13px; font-weight: 600; color: var(--color-text-sub); flex-shrink: 0; }
.mxr-section-bar-wrap { flex: 1; }
.mxr-section-bar { height: 8px; background: var(--color-border); border-radius: var(--radius-full); overflow: hidden; }
.mxr-section-fill { height: 100%; background: var(--color-primary); border-radius: var(--radius-full); transition: width 0.8s ease; }
.mxr-section-pct { width: 40px; font-size: 13px; font-weight: 700; color: var(--color-text); text-align: right; }

.mxr-compare { font-size: 13px; color: var(--color-text-sub); background: var(--color-bg); border-radius: var(--radius-md); padding: 10px 14px; }

/* Table */
.mxr-section-heading { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; }
.mxr-table-wrap { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); overflow: hidden; overflow-x: auto; }
.mxr-table { width: 100%; border-collapse: collapse; }
.mxr-table thead { background: var(--color-bg); border-bottom: 1px solid var(--color-border); }
.mxr-table th { padding: 10px 16px; font-size: 11px; font-weight: 700; color: var(--color-text-sub); text-transform: uppercase; letter-spacing: 0.5px; text-align: left; }
.mxr-table td { padding: 12px 16px; font-size: 14px; color: var(--color-text); border-bottom: 1px solid var(--color-border); }
.mxr-tr:last-child td { border-bottom: none; }
.mxr-tr--correct { background: rgba(93,187,105,0.04); }
.mxr-tr--wrong   { background: rgba(229,115,115,0.04); }
.mxr-td-num     { font-weight: 600; color: var(--color-text-sub); font-size: 13px; }
.mxr-td-option  { font-weight: 700; text-align: center; }
.mxr-td-correct { color: var(--color-secondary); }
.mxr-td-result  { text-align: center; }
.mxr-result-icon { font-size: 16px; }
.mxr-skipped    { color: var(--color-text-disabled); font-style: italic; }

/* CTA footer */
.mxr-footer-ctas { display: flex; gap: 12px; flex-wrap: wrap; justify-content: flex-end; }
.mxr-btn { display: inline-flex; align-items: center; justify-content: center; height: 42px; padding: 0 22px; border-radius: var(--radius-full); font-family: var(--font-base); font-size: 14px; font-weight: 700; cursor: pointer; text-decoration: none; transition: filter var(--transition); }
.mxr-btn--primary { background: var(--color-secondary); color: white; border: none; }
.mxr-btn--primary:hover { filter: brightness(1.07); }
.mxr-btn--outline { background: transparent; border: 1.5px solid var(--color-primary); color: var(--color-primary); }
.mxr-btn--outline:hover { background: var(--color-primary-bg); }
.mxr-btn--ghost { background: transparent; border: 1.5px solid var(--color-border); color: var(--color-text-sub); }
.mxr-btn--ghost:hover { color: var(--color-text); }

/* Skeletons */
.mxr-skel { border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
.mxr-skel--hero  { height: 220px; }
.mxr-skel--table { height: 300px; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* Error */
.mxr-error-banner { background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }

@media (max-width: 767px) {
  .mxr-body { padding: 16px 16px 32px; }
  .mxr-hero-card { padding: 20px; }
  .mxr-score-num { font-size: 40px; }
  .mxr-footer-ctas { justify-content: center; }
  .mxr-btn { flex: 1; }
}

@media (prefers-reduced-motion: reduce) {
  .mxr-page * { animation: none !important; transition-duration: 0ms !important; }
  .mxr-section-fill { transition: none; }
}
```

---

## 8. DOMAIN RULES

- Kết quả bất biến — không có nút "Sửa" hay "Xoá".
- `correct_option` có thể được trả về sau khi submit (vì bài đã nộp).
- `selectedOption === null` = câu bỏ qua — hiển thị "—" trong bảng.
- Section fill animation: chạy khi component mount, chỉ 1 lần.
