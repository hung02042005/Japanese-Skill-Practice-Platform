# SPEC — Làm bài thi (`/mock-test/:id/attempt`)
> **Sprint:** 3 — Assessment Loop
> **Prefix:** `mxa-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §11.2` | **Backend ref:** `feat-assessment/SPEC.md`
> **⚠️ Không dùng TopNav — Exam mode fullscreen**

---

## 1. MÔ TẢ TRANG

Trang làm bài thi thử JLPT fullscreen. Không có TopNav — thay bằng `ExamTopBar` tối giản. Đồng hồ đếm ngược hiển thị phía client nhưng backend validate thời gian. Câu trả lời backup vào localStorage phòng mất mạng. Có panel điều hướng câu.

---

## 2. MOCKUP

```
┌─────────────────────────────────────────────────────────────────────┐
│  ExamTopBar (sticky, 64px)                                          │
│  [🌸 SakuJi]                    [⏱ 89:23]        [Nộp bài]         │
├─────────────────────────────────────────────────────────────────────┤
│  [Progress: Câu 23 / 100   ████████████░░░░░░░░░░░░ 23%]           │
├──────────────────────────────────────┬──────────────────────────────┤
│                                      │  Điều hướng câu              │
│  Câu 23 / 100                        │  ─────────────────────────── │
│  [Từ vựng]                           │   1  2  3  4  5  6  7  8   │
│                                      │   9 10 11 12 13 14 15 16   │
│  Từ nào có nghĩa là "Xin chào"?      │  17 18 19 [23] 21 22       │
│                                      │                              │
│  ○ A. さようなら                     │  ● = đã trả lời (pink)       │
│  ● B. こんにちは  ← đã chọn          │  [23] = đang xem (bordered)  │
│  ○ C. ありがとう                     │  □ = chưa trả lời            │
│  ○ D. すみません                     │                              │
│                                      │  Chưa trả lời: 77 câu        │
│  [← Câu trước]   [Câu tiếp theo →]  │                              │
│                                      │  [Nộp bài →]                 │
└──────────────────────────────────────┴──────────────────────────────┘

Modal xác nhận nộp bài:
┌──────────────────────────────────────────┐
│  Xác nhận nộp bài                       │
│                                          │
│  Bạn chưa trả lời 5 câu.               │
│  Sau khi nộp, không thể thay đổi.       │
│                                          │
│  [Về làm tiếp]     [Nộp bài xác nhận]  │
└──────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/mock-test/
├── MockTestAttempt.jsx
└── MockTestAttempt.css

components/student/
├── ExamTopBar.jsx      ← top bar tối giản cho exam mode
└── ExamNavigator.jsx   ← panel điều hướng câu bên phải
```

---

## 4. STATE

```js
const { id } = useParams();
const navigate = useNavigate();

const [exam,        setExam]    = useState(null);     // { examId, title, jlptLevel, durationMin, sections[] }
const [questions,   setQs]      = useState([]);       // Question[]
const [answers,     setAnswers] = useState({});       // { [questionId]: 'A'|'B'|'C'|'D' }
const [currentIdx,  setIdx]     = useState(0);
const [timeLeft,    setTime]    = useState(null);     // seconds
const [isLoading,   setLoading] = useState(true);
const [isSubmitting,setSubmit]  = useState(false);
const [showConfirm, setConfirm] = useState(false);
const [error,       setError]   = useState('');

const timerRef    = useRef(null);
const DRAFT_KEY   = `exam_draft_${id}`;
```

---

## 5. API CALLS

```js
// Load đề thi
// GET /api/assessments/:id
// Response: {
//   data: {
//     assessmentId, title, jlptLevel, durationMin, passScore, totalQuestions,
//     sections: [{ sectionName, questions: [{ questionId, questionText, questionType,
//                 skill, optionA, optionB, optionC, optionD, audioUrl }] }]
//   }
// }
// NOTE: correct_option KHÔNG có trong response

// Submit bài thi
// POST /api/quiz-attempts
// Request: {
//   "assessmentId": id,
//   "answers": [{ "questionId": 1, "selectedOption": "B" }]
// }
// Response 201: { data: { attemptId, score, isPassed, sectionScores } }
// → navigate(`/mock-test/${id}/results?attemptId=${result.attemptId}`)
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ExamTopBar from '../../components/student/ExamTopBar';
import ExamNavigator from '../../components/student/ExamNavigator';
import { getAssessmentDetail, submitQuizAttempt } from '../../api/studentService';
import './MockTestAttempt.css';

export default function MockTestAttempt() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [exam,        setExam]    = useState(null);
  const [questions,   setQs]      = useState([]);
  const [answers,     setAnswers] = useState({});
  const [currentIdx,  setIdx]     = useState(0);
  const [timeLeft,    setTime]    = useState(null);
  const [isLoading,   setLoading] = useState(true);
  const [isSubmitting,setSubmit]  = useState(false);
  const [showConfirm, setConfirm] = useState(false);
  const [error,       setError]   = useState('');

  const timerRef  = useRef(null);
  const DRAFT_KEY = `exam_draft_${id}`;

  // Load đề thi
  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const data = await getAssessmentDetail(id);
        const allQs = data.sections.flatMap((s) =>
          s.questions.map((q) => ({ ...q, sectionName: s.sectionName }))
        );
        setExam(data);
        setQs(allQs);
        setTime(data.durationMin * 60);
        // Restore draft
        const draft = localStorage.getItem(DRAFT_KEY);
        if (draft) setAnswers(JSON.parse(draft));
      } catch {
        setError('Không thể tải đề thi. Vui lòng thử lại.');
      } finally {
        setLoading(false);
      }
    })();
    return () => clearInterval(timerRef.current);
  }, [id]);

  // Start timer after exam loaded
  useEffect(() => {
    if (timeLeft === null || timeLeft === 0) return;
    timerRef.current = setInterval(() => {
      setTime((t) => {
        if (t <= 1) {
          clearInterval(timerRef.current);
          handleAutoSubmit();
          return 0;
        }
        return t - 1;
      });
    }, 1000);
    return () => clearInterval(timerRef.current);
  }, [timeLeft !== null]);

  function handleSelectAnswer(questionId, option) {
    const updated = { ...answers, [questionId]: option };
    setAnswers(updated);
    localStorage.setItem(DRAFT_KEY, JSON.stringify(updated));
  }

  async function handleAutoSubmit() {
    if (isSubmitting) return;
    await submitAnswers();
  }

  async function submitAnswers() {
    setSubmit(true);
    clearInterval(timerRef.current);
    try {
      const payload = questions.map((q) => ({
        questionId:     q.questionId,
        selectedOption: answers[q.questionId] ?? null,
      }));
      const result = await submitQuizAttempt({ assessmentId: exam.assessmentId, answers: payload });
      localStorage.removeItem(DRAFT_KEY);
      navigate(`/mock-test/${id}/results?attemptId=${result.attemptId}`);
    } catch {
      setError('Nộp bài thất bại. Vui lòng thử lại.');
      setSubmit(false);
    }
  }

  const unansweredCount = questions.filter((q) => !answers[q.questionId]).length;
  const currentQ        = questions[currentIdx];

  if (isLoading) {
    return (
      <div className="mxa-loading" role="status">
        <div className="mxa-spinner-lg" />
        <span>Đang tải đề thi...</span>
      </div>
    );
  }

  if (error && !exam) {
    return (
      <div className="mxa-error-page">
        <div className="mxa-error-card" role="alert">
          <p>{error}</p>
          <button onClick={() => navigate(-1)}>← Quay lại</button>
        </div>
      </div>
    );
  }

  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;
  const timeStr = `${String(minutes).padStart(2,'0')}:${String(seconds).padStart(2,'0')}`;
  const isUrgent = timeLeft < 300; // < 5 phút

  return (
    <div className="mxa-page">
      {/* ExamTopBar */}
      <ExamTopBar
        title={exam.title}
        timeString={timeStr}
        isUrgent={isUrgent}
        onSubmit={() => setConfirm(true)}
        isSubmitting={isSubmitting}
      />

      {/* Progress bar */}
      <div className="mxa-progress-bar-wrap">
        <div
          className="mxa-progress-fill"
          style={{ width: `${((currentIdx + 1) / questions.length) * 100}%` }}
          role="progressbar"
          aria-valuenow={currentIdx + 1}
          aria-valuemin={1}
          aria-valuemax={questions.length}
          aria-label={`Câu ${currentIdx + 1} trên ${questions.length}`}
        />
      </div>

      <div className="mxa-layout">
        {/* Question panel */}
        <main className="mxa-question-panel">
          {currentQ && (
            <>
              <div className="mxa-q-meta">
                <span className="mxa-q-num">Câu {currentIdx + 1} / {questions.length}</span>
                <span className="mxa-q-skill">{currentQ.sectionName}</span>
              </div>

              {/* Audio (listening section) */}
              {currentQ.audioUrl && (
                <audio
                  className="mxa-audio"
                  controls
                  src={currentQ.audioUrl}
                  aria-label={`Audio câu ${currentIdx + 1}`}
                />
              )}

              <p className="mxa-q-text">{currentQ.questionText}</p>

              {/* Options */}
              <div className="mxa-options" role="radiogroup" aria-label={`Đáp án câu ${currentIdx + 1}`}>
                {['A', 'B', 'C', 'D'].map((opt) => {
                  const text = currentQ[`option${opt}`];
                  if (!text) return null;
                  const selected = answers[currentQ.questionId] === opt;
                  return (
                    <label
                      key={opt}
                      className={`mxa-option${selected ? ' mxa-option--selected' : ''}`}
                    >
                      <input
                        type="radio"
                        name={`q-${currentQ.questionId}`}
                        value={opt}
                        checked={selected}
                        onChange={() => handleSelectAnswer(currentQ.questionId, opt)}
                        className="mxa-sr-only"
                        aria-label={`Đáp án ${opt}: ${text}`}
                      />
                      <span className="mxa-opt-letter" aria-hidden="true">{opt}</span>
                      <span className="mxa-opt-text" lang="ja">{text}</span>
                    </label>
                  );
                })}
              </div>

              {/* Navigation */}
              <div className="mxa-q-nav">
                <button
                  className="mxa-nav-btn"
                  onClick={() => setIdx((i) => Math.max(0, i - 1))}
                  disabled={currentIdx === 0}
                  aria-label="Câu trước"
                >
                  ← Câu trước
                </button>
                <button
                  className="mxa-nav-btn mxa-nav-btn--next"
                  onClick={() => setIdx((i) => Math.min(questions.length - 1, i + 1))}
                  disabled={currentIdx === questions.length - 1}
                  aria-label="Câu tiếp theo"
                >
                  Câu tiếp theo →
                </button>
              </div>
            </>
          )}
        </main>

        {/* Navigator panel */}
        <ExamNavigator
          questions={questions}
          answers={answers}
          currentIdx={currentIdx}
          onJump={setIdx}
          onSubmit={() => setConfirm(true)}
          unansweredCount={unansweredCount}
        />
      </div>

      {/* Error toast */}
      {error && (
        <div className="mxa-error-banner" role="alert">
          <span>{error}</span>
          <button onClick={() => setError('')}>✕</button>
        </div>
      )}

      {/* Confirm modal */}
      {showConfirm && (
        <div
          className="mxa-modal-backdrop"
          role="dialog"
          aria-modal="true"
          aria-label="Xác nhận nộp bài"
          onClick={(e) => { if (e.target === e.currentTarget) setConfirm(false); }}
        >
          <div className="mxa-confirm-modal">
            <h2 className="mxa-modal-title">Xác nhận nộp bài</h2>
            {unansweredCount > 0
              ? <p className="mxa-modal-body">Bạn còn <strong>{unansweredCount} câu chưa trả lời</strong>. Sau khi nộp, bạn không thể thay đổi.</p>
              : <p className="mxa-modal-body">Bạn đã trả lời tất cả {questions.length} câu. Xác nhận nộp bài?</p>
            }
            <div className="mxa-modal-footer">
              <button className="mxa-modal-cancel" onClick={() => setConfirm(false)}>Về làm tiếp</button>
              <button className="mxa-modal-submit" onClick={submitAnswers} disabled={isSubmitting}>
                {isSubmitting && <span className="mxa-spinner mxa-spinner--white" aria-hidden="true" />}
                {isSubmitting ? 'Đang nộp…' : 'Nộp bài'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
```

---

## 7. ExamTopBar component

```jsx
// components/student/ExamTopBar.jsx
import AppLogo from '../common/AppLogo';

export default function ExamTopBar({ title, timeString, isUrgent, onSubmit, isSubmitting }) {
  return (
    <header className="etb-bar" role="banner">
      <div className="etb-logo">
        <AppLogo size={28} />
      </div>
      <div className="etb-title">{title}</div>
      <div className={`etb-timer${isUrgent ? ' etb-timer--urgent' : ''}`} aria-live="off" aria-label={`Thời gian còn lại: ${timeString}`}>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
          <path d="M12 6v6l4 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        </svg>
        {timeString}
      </div>
      <button
        className="etb-submit-btn"
        onClick={onSubmit}
        disabled={isSubmitting}
        aria-label="Nộp bài thi"
      >
        {isSubmitting ? 'Đang nộp...' : 'Nộp bài'}
      </button>
    </header>
  );
}
```

---

## 8. CSS

```css
/* ===== Mock Test Attempt (SakuJi Hanami Theme) ===== */

.mxa-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  font-family: var(--font-base);
}

/* ExamTopBar */
.etb-bar {
  position: sticky; top: 0; z-index: 100;
  height: 64px;
  background: var(--color-card);
  border-bottom: 1px solid var(--color-border);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px;
  gap: 16px;
  box-shadow: var(--shadow-sm);
}
.etb-logo { flex-shrink: 0; }
.etb-title { font-size: 15px; font-weight: 700; color: var(--color-text); flex: 1; text-align: center; }
.etb-timer {
  display: flex; align-items: center; gap: 6px;
  font-size: 18px; font-weight: 700; color: var(--color-text);
  min-width: 80px; justify-content: flex-end;
}
.etb-timer--urgent { color: var(--color-error); animation: timerPulse 1s ease infinite; }
@keyframes timerPulse { 0%,100% { opacity: 1; } 50% { opacity: 0.6; } }
.etb-submit-btn {
  height: 36px; padding: 0 18px;
  background: var(--color-secondary); color: white;
  border: none; border-radius: var(--radius-full);
  font-size: 13px; font-weight: 700; cursor: pointer;
}
.etb-submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* Progress bar */
.mxa-progress-bar-wrap { height: 4px; background: var(--color-border); }
.mxa-progress-fill { height: 100%; background: var(--color-primary); transition: width 0.3s ease; }

/* Layout */
.mxa-layout {
  flex: 1;
  display: flex;
  gap: 0;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 0 0 40px;
  box-sizing: border-box;
}

/* Question panel */
.mxa-question-panel {
  flex: 1;
  padding: 28px 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mxa-q-meta { display: flex; align-items: center; gap: 10px; }
.mxa-q-num  { font-size: 14px; font-weight: 700; color: var(--color-text-sub); }
.mxa-q-skill {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  border-radius: var(--radius-full);
  padding: 3px 10px;
  font-size: 12px; font-weight: 700;
}
.mxa-audio { width: 100%; }
.mxa-q-text { font-size: 17px; font-weight: 600; color: var(--color-text); line-height: 1.6; margin: 0; }

.mxa-options { display: flex; flex-direction: column; gap: 10px; }
.mxa-option {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 18px;
  border-radius: var(--radius-md);
  border: 2px solid var(--color-border);
  background: var(--color-card);
  cursor: pointer;
  transition: border-color var(--transition), background var(--transition);
}
.mxa-option:hover { border-color: var(--color-primary-light); background: var(--color-primary-bg); }
.mxa-option--selected { border-color: var(--color-primary); background: var(--color-primary-bg); }
.mxa-opt-letter {
  width: 28px; height: 28px;
  border-radius: var(--radius-full);
  border: 2px solid var(--color-border);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0;
  color: var(--color-text-sub);
}
.mxa-option--selected .mxa-opt-letter {
  border-color: var(--color-primary); background: var(--color-primary); color: white;
}
.mxa-opt-text { font-size: 15px; color: var(--color-text); }

.mxa-q-nav { display: flex; justify-content: space-between; margin-top: 8px; }
.mxa-nav-btn {
  height: 38px; padding: 0 18px;
  border-radius: var(--radius-full); border: 1.5px solid var(--color-border);
  background: var(--color-card); color: var(--color-text-sub);
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: color var(--transition), border-color var(--transition);
}
.mxa-nav-btn:hover:not(:disabled) { color: var(--color-primary); border-color: var(--color-primary-light); }
.mxa-nav-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* Navigator panel */
.mxa-navigator-panel {
  width: 280px; flex-shrink: 0;
  padding: 24px 20px;
  border-left: 1px solid var(--color-border);
  display: flex; flex-direction: column; gap: 16px;
  position: sticky; top: 68px;
  height: calc(100vh - 68px);
  overflow-y: auto;
}
.mxa-nav-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 4px; }
.mxa-nav-cell {
  width: 28px; height: 28px;
  border-radius: 6px;
  border: 1.5px solid var(--color-border);
  background: var(--color-bg);
  font-size: 11px; font-weight: 600; color: var(--color-text-sub);
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: border-color var(--transition), background var(--transition);
}
.mxa-nav-cell--answered { background: var(--color-primary-bg); border-color: var(--color-primary); color: var(--color-primary); }
.mxa-nav-cell--current  { border: 2.5px solid var(--color-primary); font-weight: 800; }

/* Error banner */
.mxa-error-banner {
  position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%);
  background: #FEF2F2; border: 1px solid var(--color-error); border-radius: var(--radius-md);
  padding: 12px 20px; font-size: 13px; color: var(--color-error);
  display: flex; align-items: center; gap: 12px;
  box-shadow: var(--shadow-lg); z-index: 200;
}

/* Loading */
.mxa-loading { min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; color: var(--color-text-sub); font-size: 14px; background: var(--color-bg); }
.mxa-spinner-lg { width: 48px; height: 48px; border: 4px solid var(--color-primary-light); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.8s linear infinite; }

/* Confirm modal */
.mxa-modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.40); backdrop-filter: blur(2px);
  z-index: 300; display: flex; align-items: center; justify-content: center;
  padding: 16px;
}
.mxa-confirm-modal {
  background: var(--color-card); border-radius: var(--radius-xl); padding: 28px;
  width: 100%; max-width: 420px;
  animation: modalIn 0.22s ease;
}
@keyframes modalIn { from { opacity: 0; transform: scale(0.93) translateY(8px); } to { opacity: 1; transform: scale(1) translateY(0); } }
.mxa-modal-title { font-size: 20px; font-weight: 700; color: var(--color-text); margin: 0 0 12px; }
.mxa-modal-body  { font-size: 14px; color: var(--color-text-sub); line-height: 1.6; margin: 0 0 20px; }
.mxa-modal-footer { display: flex; gap: 10px; justify-content: flex-end; }
.mxa-modal-cancel { height: 40px; padding: 0 18px; border-radius: var(--radius-full); border: 1.5px solid var(--color-border); background: transparent; color: var(--color-text-sub); font-size: 14px; font-weight: 600; cursor: pointer; }
.mxa-modal-submit { height: 40px; padding: 0 22px; border-radius: var(--radius-full); background: var(--color-secondary); color: white; border: none; font-size: 14px; font-weight: 700; cursor: pointer; display: flex; align-items: center; gap: 7px; }
.mxa-modal-submit:disabled { opacity: 0.6; cursor: not-allowed; }

/* Spinner */
.mxa-spinner { display: inline-block; width: 15px; height: 15px; border: 2.5px solid rgba(255,255,255,0.35); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; }
.mxa-sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0,0,0,0); }
@keyframes spin { to { transform: rotate(360deg); } }

/* Responsive */
@media (max-width: 1024px) {
  .mxa-navigator-panel { width: 220px; }
  .mxa-nav-grid { grid-template-columns: repeat(6, 1fr); }
}
@media (max-width: 767px) {
  .mxa-layout { flex-direction: column; }
  .mxa-navigator-panel { width: 100%; position: static; height: auto; border-left: none; border-top: 1px solid var(--color-border); order: 2; }
  .mxa-question-panel { order: 1; padding: 20px 16px; }
  .mxa-nav-grid { grid-template-columns: repeat(10, 1fr); }
}

@media (prefers-reduced-motion: reduce) {
  .mxa-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 9. 3 TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Loading** | Fullscreen spinner (không có TopNav) |
| **Error (load)** | Error card toàn trang + nút quay lại |
| **Error (submit)** | Banner toast phía dưới + user có thể thử lại |
| **Timeout auto-submit** | `handleAutoSubmit()` tự gọi khi `timeLeft === 0` |

---

## 10. DOMAIN RULES

- **Không gửi score từ client** — chỉ gửi `{ questionId, selectedOption }[]`.
- **Backend validate thời gian** — client timer chỉ để hiển thị.
- **localStorage backup** — lưu sau mỗi lần chọn đáp án, xóa sau khi submit thành công.
- **Timer urgent** — khi `timeLeft < 300s` (5 phút), đồng hồ đổi màu đỏ và blink.
- **Confirm modal** — luôn hiện khi bấm "Nộp bài", dù đã trả lời hết.
- **Khi mất mạng** — hiện banner "Mất kết nối...", retry submit khi reconnect.

---

## 11. ACCESSIBILITY

- [ ] `role="radiogroup"` cho nhóm đáp án, mỗi `<label>` wrap `<input type="radio">`
- [ ] Timer: `aria-live="off"` (không đọc từng giây); khi còn 5 phút: `aria-live="assertive"` + `aria-atomic="true"`
- [ ] Navigator cells: `aria-label="Câu X — đã/chưa trả lời"`
- [ ] Confirm modal: `role="dialog"`, `aria-modal="true"`, Escape đóng modal
- [ ] Progress bar: `role="progressbar"` với `aria-valuenow`, `aria-valuemin`, `aria-valuemax`
- [ ] Audio: native `<audio controls>` — accessible mặc định
