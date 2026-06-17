# SPEC — Quiz & Luyện Tập (`/quiz`)
> **UC:** UC-11 — Quiz luyện tập theo chủ đề (không phải thi Mock Exam)
> **Sprint:** 3 — Core Content
> **Prefix:** `qz-` | **activeTab:** `'quiz'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §11.1` | **Backend ref:** `feat-testing/SPEC.md UC-11`
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Phân biệt:** `/quiz` = luyện tập ngắn 10–20 câu không timer nghiêm ngặt. `/mock-tests` = thi thử có timer server-side.

---

## 1. MÔ TẢ TRANG

Hai màn hình trong cùng route:
1. **Quiz List** — Danh sách bộ quiz lọc theo level / kỹ năng. Click → chuyển sang Quiz Attempt.
2. **Quiz Attempt** — Làm bài quiz trong cùng page (không route mới). Hiển thị câu hỏi tuần tự, submit cuối → xem kết quả.

---

## 2. MOCKUP

### 2.1 Quiz List
```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="quiz")                                       │
├──────────────────────────────────────────────────────────────────┤
│  クイズ Quiz Luyện Tập                                           │
│  Luyện tập các kỹ năng với quiz ngắn.                            │
│                                                                  │
│  [N5]  [N4]  [N3]  [N2]  [N1]                                   │
│                                                                  │
│  Kỹ năng: [Tất cả▼]                                             │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  📝 Quiz Từ vựng N5 – Chủ đề Gia đình       [15 câu]    │   │
│  │  Từ vựng · N5 · Đã làm: 3 lần · Tốt nhất: 87%          │   │
│  │                                          [Làm quiz →]    │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  📝 Quiz Ngữ pháp N5 – て形                  [10 câu]   │   │
│  │  Ngữ pháp · N5 · Chưa làm                              │   │
│  │                                          [Làm quiz →]    │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 Quiz Attempt
```
┌──────────────────────────────────────────────────────────────────┐
│  ← Danh sách quiz      Quiz Từ vựng N5 – Gia đình               │
│  Câu 3 / 15                              [░░░░░████████░░░░] 20% │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Từ nào có nghĩa là "cha"?                                      │
│                                                                  │
│  ○  A.  おとうさん                                              │
│  ○  B.  おかあさん                                              │
│  ○  C.  おにいさん                                              │
│  ○  D.  いもうと                                                │
│                                                                  │
│                       [← Câu trước]  [Câu tiếp →]               │
│                                                                  │
│  ── Sau khi chọn đáp án ──                                       │
│  ✓ Đúng! おとうさん = cha / bố                                  │
│  [Giải thích thêm: ...]                                          │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

### 2.3 Quiz Result (inline, sau câu cuối)
```
┌──────────────────────────────────────────────────────────────────┐
│  🎉 Hoàn thành Quiz!                                            │
│                                                                  │
│  Điểm:  12 / 15  (80%)  ★★★★☆                                  │
│                                                                  │
│  ┌──────────┬──────────┐                                        │
│  │ Đúng: 12 │ Sai: 3   │                                        │
│  └──────────┴──────────┘                                        │
│                                                                  │
│  [Xem lại câu sai]    [Làm lại]    [Về danh sách]               │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/quiz/
├── QuizPage.jsx        ← list + attempt trong cùng component, dùng `view` state
└── QuizPage.css

components/student/
├── QuizCard.jsx        ← card trong danh sách quiz
└── QuizQuestion.jsx    ← câu hỏi + options khi đang làm bài
```

---

## 4. STATE

### 4.1 Quiz List state
```js
const [view,       setView]    = useState('list');  // 'list' | 'attempt' | 'result'
const [level,      setLevel]   = useState(user?.jlptLevel ?? 'N5');
const [skill,      setSkill]   = useState('');      // '' | 'vocabulary' | 'grammar' | 'reading' | 'listening'
const [quizzes,    setQuizzes] = useState([]);
const [isLoading,  setLoading] = useState(true);
const [error,      setError]   = useState('');
```

### 4.2 Quiz Attempt state
```js
const [activeQuiz,  setActiveQuiz] = useState(null);  // quiz object từ list
const [questions,   setQuestions]  = useState([]);    // Question[]
const [currentIdx,  setCurrentIdx] = useState(0);
const [answers,     setAnswers]    = useState({});    // { [questionId]: selectedOptionId }
const [feedback,    setFeedback]   = useState({});    // { [questionId]: { correct, explanation } }
const [isSubmitting, setSubmitting]= useState(false);
const [result,      setResult]     = useState(null);  // AttemptResult sau submit
const [isLoadingQ,  setLoadingQ]   = useState(false);
```

---

## 5. API CALLS

```js
// GET /api/quizzes?level=N5&skill=vocabulary&page=0&size=20
// Response:
{
  "data": {
    "content": [
      {
        "quizId": 1,
        "title": "Quiz Từ vựng N5 – Chủ đề Gia đình",
        "skill": "vocabulary",
        "jlptLevel": "N5",
        "questionCount": 15,
        "bestScore": 87,
        "attemptCount": 3
      }
    ],
    "totalPages": 2,
    "totalElements": 24
  }
}

// GET /api/quizzes/{quizId}/questions
// Response:
{
  "data": [
    {
      "questionId": 101,
      "order": 1,
      "content": "Từ nào có nghĩa là \"cha\"?",
      "options": [
        { "optionId": 401, "label": "A", "text": "おとうさん" },
        { "optionId": 402, "label": "B", "text": "おかあさん" },
        { "optionId": 403, "label": "C", "text": "おにいさん" },
        { "optionId": 404, "label": "D", "text": "いもうと" }
      ]
    }
  ]
}

// POST /api/quiz-attempts
// Request: { "quizId": 1, "answers": [{ "questionId": 101, "selectedOptionId": 401 }] }
// Response:
{
  "data": {
    "attemptId": 200,
    "score": 12,
    "totalQuestions": 15,
    "scorePct": 80,
    "results": [
      {
        "questionId": 101,
        "selectedOptionId": 401,
        "correctOptionId": 401,
        "isCorrect": true,
        "explanation": "おとうさん nghĩa là cha/bố trong tiếng Nhật."
      }
    ]
  }
}
```

API service (`studentService.js`):
```js
export async function getQuizList({ level, skill, page = 0, size = 20 } = {}) {
  const params = { level, page, size };
  if (skill) params.skill = skill;
  const res = await api.get('/quizzes', { params });
  return res.data.data;
}

export async function getQuizQuestions(quizId) {
  const res = await api.get(`/quizzes/${quizId}/questions`);
  return res.data.data;
}

export async function submitQuizAttempt(quizId, answers) {
  const res = await api.post('/quiz-attempts', {
    quizId,
    answers: answers.map(([questionId, selectedOptionId]) => ({ questionId, selectedOptionId })),
  });
  return res.data.data;
}
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useCallback } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import QuizCard from '../../components/student/QuizCard';
import QuizQuestion from '../../components/student/QuizQuestion';
import { getQuizList, getQuizQuestions, submitQuizAttempt } from '../../api/studentService';
import './QuizPage.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];
const SKILLS = [
  { id: '', label: 'Tất cả' },
  { id: 'vocabulary', label: 'Từ vựng' },
  { id: 'grammar', label: 'Ngữ pháp' },
  { id: 'reading', label: 'Đọc hiểu' },
  { id: 'listening', label: 'Nghe' },
];

export default function QuizPage() {
  const { user } = useAppSelector((s) => s.auth);
  // List state
  const [view,      setView]    = useState('list');
  const [level,     setLevel]   = useState(user?.jlptLevel ?? 'N5');
  const [skill,     setSkill]   = useState('');
  const [quizzes,   setQuizzes] = useState([]);
  const [isLoading, setLoading] = useState(true);
  const [error,     setError]   = useState('');
  // Attempt state
  const [activeQuiz,   setActiveQuiz]  = useState(null);
  const [questions,    setQuestions]   = useState([]);
  const [currentIdx,   setCurrentIdx] = useState(0);
  const [answers,      setAnswers]    = useState({});
  const [result,       setResult]     = useState(null);
  const [isSubmitting, setSubmitting] = useState(false);
  const [isLoadingQ,   setLoadingQ]   = useState(false);

  const fetchQuizzes = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const data = await getQuizList({ level, skill });
      setQuizzes(data.content);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải danh sách quiz.');
    } finally {
      setLoading(false);
    }
  }, [level, skill]);

  useEffect(() => { fetchQuizzes(); }, [fetchQuizzes]);

  const startQuiz = async (quiz) => {
    setLoadingQ(true);
    try {
      const qs = await getQuizQuestions(quiz.quizId);
      setActiveQuiz(quiz);
      setQuestions(qs);
      setCurrentIdx(0);
      setAnswers({});
      setResult(null);
      setView('attempt');
    } catch {
      setError('Không thể tải câu hỏi. Vui lòng thử lại.');
    } finally {
      setLoadingQ(false);
    }
  };

  const handleAnswer = (questionId, optionId) => {
    setAnswers((prev) => ({ ...prev, [questionId]: optionId }));
  };

  const handleSubmit = async () => {
    setSubmitting(true);
    try {
      const answerEntries = Object.entries(answers).map(([qId, oId]) => [Number(qId), oId]);
      const res = await submitQuizAttempt(activeQuiz.quizId, answerEntries);
      setResult(res);
      setView('result');
    } catch {
      setError('Không thể nộp bài. Vui lòng thử lại.');
    } finally {
      setSubmitting(false);
    }
  };

  const resetToList = () => {
    setView('list');
    setActiveQuiz(null);
    setQuestions([]);
    setResult(null);
    fetchQuizzes();
  };

  const progressPct = questions.length > 0 ? Math.round(((currentIdx + 1) / questions.length) * 100) : 0;

  // ─── VIEWS ───
  if (view === 'attempt' && activeQuiz) {
    const q = questions[currentIdx];
    const answeredCount = Object.keys(answers).length;
    const allAnswered = answeredCount === questions.length;

    return (
      <div className="qz-page">
        <TopNav activeTab="quiz" />
        <main className="qz-body">
          <div className="qz-attempt-header">
            <button className="qz-back-btn" onClick={resetToList} aria-label="Quay lại danh sách">← Danh sách quiz</button>
            <h2 className="qz-attempt-title">{activeQuiz.title}</h2>
          </div>
          <div className="qz-progress-row">
            <span className="qz-progress-label">Câu {currentIdx + 1} / {questions.length}</span>
            <div className="qz-progress-track">
              <div className="qz-progress-fill" style={{ width: `${progressPct}%` }} />
            </div>
          </div>
          {q && (
            <QuizQuestion
              key={q.questionId}
              question={q}
              selectedOptionId={answers[q.questionId] ?? null}
              onAnswer={handleAnswer}
            />
          )}
          <div className="qz-nav-row">
            <button className="qz-nav-btn" onClick={() => setCurrentIdx((i) => Math.max(0, i - 1))} disabled={currentIdx === 0}>← Câu trước</button>
            {currentIdx < questions.length - 1 ? (
              <button className="qz-nav-btn qz-nav-btn--next" onClick={() => setCurrentIdx((i) => i + 1)}>Câu tiếp →</button>
            ) : (
              <button
                className="qz-btn-submit"
                onClick={handleSubmit}
                disabled={!allAnswered || isSubmitting}
              >
                {isSubmitting ? 'Đang nộp...' : `Nộp bài (${answeredCount}/${questions.length})`}
              </button>
            )}
          </div>
        </main>
      </div>
    );
  }

  if (view === 'result' && result) {
    const stars = result.scorePct >= 90 ? 5 : result.scorePct >= 75 ? 4 : result.scorePct >= 60 ? 3 : result.scorePct >= 40 ? 2 : 1;
    return (
      <div className="qz-page">
        <TopNav activeTab="quiz" />
        <main className="qz-body">
          <div className="qz-result-card">
            <div className="qz-result-emoji">🎉</div>
            <h2 className="qz-result-title">Hoàn thành Quiz!</h2>
            <div className="qz-result-score">{result.score} / {result.totalQuestions} <span className="qz-result-pct">({result.scorePct}%)</span></div>
            <div className="qz-result-stars" aria-label={`${stars} sao`}>{'★'.repeat(stars)}{'☆'.repeat(5 - stars)}</div>
            <div className="qz-result-stats">
              <div className="qz-result-stat qz-result-stat--correct">Đúng: {result.results.filter((r) => r.isCorrect).length}</div>
              <div className="qz-result-stat qz-result-stat--wrong">Sai: {result.results.filter((r) => !r.isCorrect).length}</div>
            </div>
            <div className="qz-result-actions">
              <button className="qz-btn-secondary" onClick={() => { setCurrentIdx(0); setAnswers({}); setResult(null); setView('attempt'); }}>Làm lại</button>
              <button className="qz-btn-primary" onClick={resetToList}>Về danh sách</button>
            </div>
          </div>
        </main>
      </div>
    );
  }

  // Default: list view
  return (
    <div className="qz-page">
      <TopNav activeTab="quiz" />
      <main className="qz-body">
        <div className="qz-header">
          <h1 className="qz-title"><span lang="ja">クイズ</span> Quiz Luyện Tập</h1>
          <p className="qz-subtitle">Luyện tập các kỹ năng với quiz ngắn.</p>
        </div>

        <div className="qz-level-tabs" role="tablist" aria-label="Chọn cấp độ JLPT">
          {LEVELS.map((l) => (
            <button key={l} role="tab" aria-selected={level === l}
              className={`qz-level-tab${level === l ? ' qz-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}>{l}</button>
          ))}
        </div>

        <div className="qz-skill-filter">
          <label className="visually-hidden" htmlFor="qz-skill-select">Kỹ năng</label>
          <select id="qz-skill-select" className="qz-select" value={skill} onChange={(e) => setSkill(e.target.value)}>
            {SKILLS.map((s) => <option key={s.id} value={s.id}>{s.label}</option>)}
          </select>
        </div>

        {error && <div className="qz-error" role="alert">{error}<button className="qz-retry" onClick={fetchQuizzes}>Thử lại</button></div>}

        {isLoading || isLoadingQ ? (
          <div className="qz-list">{Array.from({ length: 4 }).map((_, i) => <div key={i} className="qz-card-skel" aria-hidden="true" />)}</div>
        ) : quizzes.length === 0 ? (
          <EmptyState title="Không có quiz nào" subtitle="Thử chọn level hoặc kỹ năng khác." mascotVariant="thinking" mascotSize={120} />
        ) : (
          <div className="qz-list">
            {quizzes.map((q) => <QuizCard key={q.quizId} quiz={q} onStart={startQuiz} />)}
          </div>
        )}
      </main>
    </div>
  );
}
```

### QuizQuestion component

```jsx
// components/student/QuizQuestion.jsx
export default function QuizQuestion({ question, selectedOptionId, onAnswer }) {
  return (
    <div className="qz-question-card">
      <p className="qz-question-text">{question.content}</p>
      <div className="qz-options" role="radiogroup" aria-label="Chọn đáp án">
        {question.options.map((opt) => (
          <button
            key={opt.optionId}
            role="radio"
            aria-checked={selectedOptionId === opt.optionId}
            className={`qz-option${selectedOptionId === opt.optionId ? ' qz-option--selected' : ''}`}
            onClick={() => onAnswer(question.questionId, opt.optionId)}
          >
            <span className="qz-option-label">{opt.label}.</span>
            <span className="qz-option-text" lang="ja">{opt.text}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Quiz Page ===== */
.qz-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.qz-body { flex: 1; max-width: 780px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.qz-header   { display: flex; flex-direction: column; gap: 4px; }
.qz-title    { font-size: 26px; font-weight: 700; color: var(--color-text); margin: 0; }
.qz-subtitle { font-size: 14px; color: var(--color-text-sub); margin: 0; }

.qz-level-tabs  { display: flex; gap: 0; border-bottom: 2px solid var(--color-border); }
.qz-level-tab   { padding: 10px 20px; font-size: 15px; font-weight: 700; color: var(--color-text-sub); background: transparent; border: none; border-bottom: 2px solid transparent; margin-bottom: -2px; cursor: pointer; transition: color var(--transition), border-color var(--transition); }
.qz-level-tab--active { color: var(--color-primary); border-bottom-color: var(--color-primary); }

.qz-skill-filter { display: flex; gap: 12px; }
.qz-select { height: 40px; padding: 0 12px; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; background: var(--color-bg); min-width: 180px; }

.qz-error { display: flex; align-items: center; justify-content: space-between; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.qz-retry { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }

/* Quiz card (list) */
.qz-list     { display: flex; flex-direction: column; gap: 12px; }
.qz-card-skel{ height: 90px; border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }

/* Attempt header */
.qz-attempt-header { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.qz-back-btn       { background: transparent; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); color: var(--color-text-sub); font-size: 13px; font-weight: 600; padding: 6px 14px; cursor: pointer; transition: all var(--transition); }
.qz-back-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.qz-attempt-title  { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; }

/* Progress track */
.qz-progress-row   { display: flex; align-items: center; gap: 12px; }
.qz-progress-label { font-size: 13px; color: var(--color-text-sub); white-space: nowrap; }
.qz-progress-track { flex: 1; height: 8px; background: var(--color-border); border-radius: var(--radius-full); overflow: hidden; }
.qz-progress-fill  { height: 100%; background: var(--color-primary); border-radius: var(--radius-full); transition: width 0.3s ease; }

/* Question card */
.qz-question-card { background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: var(--radius-lg); padding: 24px; display: flex; flex-direction: column; gap: 16px; }
.qz-question-text { font-size: 17px; color: var(--color-text); line-height: 1.6; margin: 0; }

/* Options */
.qz-options { display: flex; flex-direction: column; gap: 10px; }
.qz-option  { display: flex; align-items: center; gap: 12px; padding: 14px 18px; background: var(--color-bg); border: 2px solid var(--color-border); border-radius: var(--radius-md); cursor: pointer; text-align: left; transition: all var(--transition); }
.qz-option:hover { border-color: var(--color-primary-light); background: var(--color-primary-bg); }
.qz-option--selected { border-color: var(--color-primary); background: var(--color-primary-bg); }
.qz-option-label { font-size: 14px; font-weight: 700; color: var(--color-text-sub); width: 20px; flex-shrink: 0; }
.qz-option--selected .qz-option-label { color: var(--color-primary); }
.qz-option-text  { font-size: 15px; color: var(--color-text); }

/* Navigation */
.qz-nav-row  { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.qz-nav-btn  { height: 42px; padding: 0 20px; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); background: transparent; color: var(--color-text-sub); font-size: 14px; font-weight: 600; cursor: pointer; transition: all var(--transition); }
.qz-nav-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.qz-nav-btn--next { border-color: var(--color-primary); color: var(--color-primary); }
.qz-btn-submit { height: 42px; padding: 0 24px; background: var(--color-primary); color: white; border: none; border-radius: var(--radius-full); font-size: 14px; font-weight: 700; cursor: pointer; transition: filter var(--transition); }
.qz-btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

/* Result */
.qz-result-card    { background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: var(--radius-xl); padding: 40px 32px; display: flex; flex-direction: column; align-items: center; gap: 16px; max-width: 480px; margin: 0 auto; text-align: center; box-shadow: var(--shadow-sm); }
.qz-result-emoji   { font-size: 48px; }
.qz-result-title   { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0; }
.qz-result-score   { font-size: 36px; font-weight: 800; color: var(--color-primary); }
.qz-result-pct     { font-size: 20px; color: var(--color-text-sub); }
.qz-result-stars   { font-size: 28px; color: var(--color-warning); letter-spacing: 4px; }
.qz-result-stats   { display: flex; gap: 24px; }
.qz-result-stat    { font-size: 15px; font-weight: 700; padding: 8px 20px; border-radius: var(--radius-md); }
.qz-result-stat--correct { background: var(--color-secondary-bg); color: var(--color-secondary); }
.qz-result-stat--wrong   { background: #FFEAEA; color: var(--color-error); }
.qz-result-actions { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; margin-top: 8px; }
.qz-btn-primary   { height: 44px; padding: 0 28px; background: var(--color-primary); color: white; border: none; border-radius: var(--radius-full); font-size: 15px; font-weight: 700; cursor: pointer; }
.qz-btn-secondary { height: 44px; padding: 0 24px; background: transparent; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); font-size: 15px; font-weight: 600; color: var(--color-text-sub); cursor: pointer; }

@media (max-width: 767px) {
  .qz-body { padding: 16px 16px 32px; }
  .qz-result-card { padding: 28px 20px; }
}
@media (prefers-reduced-motion: reduce) { .qz-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 8. ACCESSIBILITY

- [ ] Level tabs dùng `role="tablist"` + `role="tab"` + `aria-selected`
- [ ] Options dùng `role="radiogroup"` + `role="radio"` + `aria-checked`
- [ ] Kết quả sao có `aria-label` chứa số sao
- [ ] Nút nộp bài disable rõ khi chưa đủ câu trả lời
- [ ] Back button có `aria-label` rõ ràng

## 9. GHI CHÚ QUAN TRỌNG

- Điểm tính hoàn toàn **backend** — frontend chỉ gửi danh sách `{ questionId, selectedOptionId }`.
- Không có server-side timer → user có thể nghĩ lại bao lâu tùy thích (khác với Mock Exam).
- Kết quả submit cần trả về toàn bộ `results[]` để frontend có thể render "xem lại câu sai" sau này.
