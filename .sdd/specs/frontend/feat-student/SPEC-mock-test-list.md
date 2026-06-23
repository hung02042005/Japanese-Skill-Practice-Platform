# SPEC — Danh sách đề thi (`/mock-test`)
>
> **Sprint:** 3 — Assessment Loop
> **Prefix:** `mkt-` | **activeTab:** `'mock-test'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §11.1` | **Backend ref:** `feat-assessment/SPEC.md`

---

## 1. MÔ TẢ TRANG

Trang chọn đề thi thử JLPT. Filter theo level N5–N1. Mỗi ExamCard hiển thị: tên đề, level badge, số câu, thời gian, điểm đậu, và lần thi gần nhất. Nút "Bắt đầu thi" navigate sang `/mock-test/:id/attempt`.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="mock-test")                                  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Thi Thử JLPT                                                   │
│  Luyện đề thật, chuẩn bị tốt nhất cho kỳ thi.                  │
│                                                                  │
│  [N5]  [N4]  [N3]  [N2]  [N1]  (filter tabs)                   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  [N5]  Đề thi N5 — Tháng 12/2024                        │  │
│  │                                                          │  │
│  │  📋 100 câu    ⏱ 90 phút    ✅ Đậu: 90/120 điểm         │  │
│  │                                                          │  │
│  │  Lần thi gần nhất: 15/05/2026 — 85/120 ❌ Không đậu     │  │
│  │                                                          │  │
│  │                              [Bắt đầu thi]              │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  [N5]  Đề thi N5 — Tháng 7/2024                         │  │
│  │  📋 100 câu    ⏱ 90 phút    ✅ Đậu: 90/120 điểm         │  │
│  │  Lần thi gần nhất: Chưa thi                              │  │
│  │                              [Bắt đầu thi]              │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  [Pagination]                                                    │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/mock-test/
├── MockTestList.jsx
└── MockTestList.css
```

---

## 4. STATE

```js
const [exams,       setExams]  = useState([]);
const [level,       setLevel]  = useState('N5');   // default = user's jlpt level
const [isLoading,   setLoading]= useState(true);
const [error,       setError]  = useState('');
const [currentPage, setPage]   = useState(1);
const [totalPages,  setTotal]  = useState(1);

const { user } = useAppSelector((s) => s.auth);
// Set level default từ user.jlptLevel
useEffect(() => { if (user?.jlptLevel) setLevel(user.jlptLevel); }, [user]);
```

---

## 5. API CALLS

```js
// GET /api/assessments?type=exam&level=N5&page=0&size=10
// Response:
{
  "data": {
    "content": [
      {
        "assessmentId": "long",
        "title": "string",
        "jlptLevel": "N5",
        "totalQuestions": 100,
        "durationMin": 90,
        "passScore": 90,
        "maxScore": 120,
        "lastAttempt": {    // null nếu chưa thi
          "score": 85,
          "isPassed": false,
          "attemptedAt": "2026-05-15T10:00:00Z",
          "attemptId": "long"
        }
      }
    ],
    "totalPages": 2,
    "totalElements": 12
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
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import { getExamList } from '../../api/studentService';
import './MockTestList.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function MockTestList() {
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);

  const [exams,     setExams]  = useState([]);
  const [level,     setLevel]  = useState(user?.jlptLevel ?? 'N5');
  const [isLoading, setLoading]= useState(true);
  const [error,     setError]  = useState('');
  const [page,      setPage]   = useState(1);
  const [totalPages,setTotal]  = useState(1);

  const fetchExams = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const res = await getExamList({ level, page: page - 1, size: 10 });
      setExams(res.content);
      setTotal(res.totalPages);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải danh sách đề thi.');
    } finally {
      setLoading(false);
    }
  }, [level, page]);

  useEffect(() => { fetchExams(); }, [fetchExams]);
  useEffect(() => { setPage(1); }, [level]);

  return (
    <div className="mkt-page">
      <TopNav activeTab="mock-test" />
      <main className="mkt-body">
        <div className="mkt-page-header">
          <h1 className="mkt-title">Thi Thử JLPT</h1>
          <p className="mkt-subtitle">Luyện đề thật, chuẩn bị tốt nhất cho kỳ thi.</p>
        </div>

        {/* Level filter */}
        <div className="mkt-level-tabs" role="tablist" aria-label="Chọn cấp độ JLPT">
          {LEVELS.map((l) => (
            <button
              key={l}
              role="tab"
              aria-selected={level === l}
              className={`mkt-level-tab${level === l ? ' mkt-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}
            >
              {l}
            </button>
          ))}
        </div>

        {/* Error */}
        {error && (
          <div className="mkt-error-banner" role="alert">
            <span>{error}</span>
            <button className="mkt-retry-btn" onClick={fetchExams}>Thử lại</button>
          </div>
        )}

        {/* Loading */}
        {isLoading && (
          <div className="mkt-list">
            {[1, 2, 3].map((i) => <div key={i} className="mkt-skel" aria-hidden="true" />)}
          </div>
        )}

        {/* Empty */}
        {!isLoading && !error && exams.length === 0 && (
          <EmptyState
            title={`Chưa có đề thi ${level}`}
            subtitle="Đề thi đang được biên soạn. Hãy thử level khác hoặc quay lại sau."
            mascotVariant="thinking"
            mascotSize={140}
          />
        )}

        {/* List */}
        {!isLoading && !error && exams.length > 0 && (
          <>
            <div className="mkt-list">
              {exams.map((exam) => (
                <div key={exam.assessmentId} className="mkt-card">
                  <div className="mkt-card-header">
                    <JlptBadge level={exam.jlptLevel} />
                    <h2 className="mkt-card-title">{exam.title}</h2>
                  </div>
                  <div className="mkt-card-meta">
                    <span className="mkt-meta-item">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2" stroke="currentColor" strokeWidth="2"/><rect x="9" y="3" width="6" height="4" rx="1" stroke="currentColor" strokeWidth="2"/></svg>
                      {exam.totalQuestions} câu
                    </span>
                    <span className="mkt-meta-item">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/><path d="M12 6v6l4 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/></svg>
                      {exam.durationMin} phút
                    </span>
                    <span className="mkt-meta-item mkt-meta-item--pass">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M9 12l2 2 4-4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/><circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/></svg>
                      Đậu: {exam.passScore}/{exam.maxScore}
                    </span>
                  </div>
                  <div className="mkt-card-footer">
                    <div className="mkt-last-attempt">
                      {exam.lastAttempt
                        ? (
                          <span className={`mkt-attempt-result${exam.lastAttempt.isPassed ? ' mkt-attempt-result--passed' : ' mkt-attempt-result--failed'}`}>
                            Lần gần nhất: {new Date(exam.lastAttempt.attemptedAt).toLocaleDateString('vi-VN')} — {exam.lastAttempt.score}/{exam.maxScore} {exam.lastAttempt.isPassed ? '✅ Đậu' : '❌ Không đậu'}
                          </span>
                        )
                        : <span className="mkt-attempt-virgin">Chưa thi lần nào</span>
                      }
                    </div>
                    <button
                      className="mkt-start-btn"
                      onClick={() => navigate(`/mock-test/${exam.assessmentId}/attempt`)}
                    >
                      Bắt đầu thi
                    </button>
                  </div>
                </div>
              ))}
            </div>
            {totalPages > 1 && (
              <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
            )}
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
/* ===== Mock Test List (SakuJi Hanami Theme) ===== */

.mkt-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.mkt-body { flex: 1; max-width: 860px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.mkt-page-header { display: flex; flex-direction: column; gap: 4px; }
.mkt-title    { font-size: 26px; font-weight: 700; color: var(--color-text); margin: 0; }
.mkt-subtitle { font-size: 14px; color: var(--color-text-sub); margin: 0; }

/* Level filter */
.mkt-level-tabs { display: flex; gap: 0; border-bottom: 2px solid var(--color-border); }
.mkt-level-tab {
  padding: 10px 20px; font-size: 15px; font-weight: 700;
  color: var(--color-text-sub); background: transparent;
  border: none; border-bottom: 2px solid transparent;
  margin-bottom: -2px; cursor: pointer;
  transition: color var(--transition), border-color var(--transition);
}
.mkt-level-tab:hover { color: var(--color-text); }
.mkt-level-tab--active { color: var(--color-primary); border-bottom-color: var(--color-primary); }

/* Error */
.mkt-error-banner { display: flex; align-items: center; justify-content: space-between; gap: 12px; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.mkt-retry-btn { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }

/* List */
.mkt-list { display: flex; flex-direction: column; gap: 16px; }

/* Card */
.mkt-card {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 20px 24px;
  display: flex; flex-direction: column; gap: 14px;
  transition: box-shadow var(--transition);
}
.mkt-card:hover { box-shadow: var(--shadow-md); }

.mkt-card-header { display: flex; align-items: center; gap: 12px; }
.mkt-card-title { font-size: 17px; font-weight: 700; color: var(--color-text); margin: 0; }

.mkt-card-meta { display: flex; gap: 20px; flex-wrap: wrap; }
.mkt-meta-item { display: flex; align-items: center; gap: 5px; font-size: 13px; color: var(--color-text-sub); font-weight: 600; }
.mkt-meta-item--pass { color: var(--color-secondary); }

.mkt-card-footer { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; padding-top: 8px; border-top: 1px solid var(--color-border); }
.mkt-attempt-result { font-size: 13px; font-weight: 600; }
.mkt-attempt-result--passed { color: var(--color-secondary); }
.mkt-attempt-result--failed { color: var(--color-error); }
.mkt-attempt-virgin { font-size: 13px; color: var(--color-text-disabled); }

.mkt-start-btn {
  height: 38px; padding: 0 20px;
  background: var(--color-secondary); color: white;
  border: none; border-radius: var(--radius-full);
  font-size: 14px; font-weight: 700; cursor: pointer;
  transition: filter var(--transition);
}
.mkt-start-btn:hover { filter: brightness(1.07); }

/* Skeleton */
.mkt-skel { height: 130px; border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

@media (max-width: 767px) {
  .mkt-body { padding: 16px 16px 32px; }
  .mkt-level-tab { padding: 8px 14px; font-size: 14px; }
  .mkt-card-footer { flex-direction: column; align-items: flex-start; }
  .mkt-start-btn { width: 100%; justify-content: center; }
}

@media (prefers-reduced-motion: reduce) {
  .mkt-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 8. 3 TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Loading** | 3 skeleton cards |
| **Error** | Banner đỏ + retry |
| **Empty** | EmptyState + gợi ý chọn level khác |
