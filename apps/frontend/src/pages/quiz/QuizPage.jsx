import { useState, useEffect, useCallback } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import QuizCard from '../../components/student/QuizCard';
import QuizQuestion from '../../components/student/QuizQuestion';
import { getQuizzes, startAssessment, submitAssessment } from '../../api/studentService';
import './QuizPage.css';

// Chuyển câu hỏi từ payload assessment (optionA..D) sang shape QuizQuestion mong đợi.
function toQuizQuestion(q) {
  const options = ['A', 'B', 'C', 'D']
    .map((label) => ({ optionId: label, label, text: q[`option${label}`] }))
    .filter((o) => o.text != null && o.text !== '');
  return { questionId: q.questionId, content: q.questionText, options };
}

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];
const SKILLS = [
  { id: '',           label: 'Tất cả'   },
  { id: 'vocabulary', label: 'Từ vựng'  },
  { id: 'grammar',    label: 'Ngữ pháp' },
  { id: 'reading',    label: 'Đọc hiểu' },
  { id: 'listening',  label: 'Nghe'     },
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
  const [currentIdx,   setCurrentIdx]  = useState(0);
  const [answers,      setAnswers]     = useState({});
  const [result,       setResult]      = useState(null);
  const [isSubmitting, setSubmitting]  = useState(false);
  const [isLoadingQ,   setLoadingQ]    = useState(false);

  const fetchQuizzes = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getQuizzes({ level });
      setQuizzes(data.content ?? []);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải danh sách quiz.');
    } finally {
      setLoading(false);
    }
  }, [level]);

  useEffect(() => { fetchQuizzes(); }, [fetchQuizzes]);

  const startQuiz = async (quiz) => {
    setLoadingQ(true);
    setError('');
    try {
      const data = await startAssessment(quiz.assessmentId);
      const raw = data.sections?.flatMap((s) => s.questions) ?? data.questions ?? [];
      setActiveQuiz({ ...quiz, attemptId: data.attemptId });
      setQuestions(raw.map(toQuizQuestion));
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
      const answerArr = questions.map((q) => ({
        questionId: q.questionId,
        selectedOption: answers[q.questionId] ?? null,
      }));
      const res = await submitAssessment(activeQuiz.assessmentId, {
        attemptId: activeQuiz.attemptId,
        isAutoSubmit: false,
        answers: answerArr,
      });
      const correct = res.results?.filter((r) => r.isCorrect).length ?? 0;
      const total = res.results?.length ?? questions.length;
      const pct = Number(res.maxScore) > 0
        ? Math.round((Number(res.totalScore) / Number(res.maxScore)) * 100)
        : 0;
      setResult({ score: correct, totalQuestions: total, scorePct: pct, results: res.results });
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

  const progressPct = questions.length > 0
    ? Math.round(((currentIdx + 1) / questions.length) * 100)
    : 0;

  // ── Attempt view ──
  if (view === 'attempt' && activeQuiz) {
    const q = questions[currentIdx];
    const answeredCount = Object.keys(answers).length;
    const allAnswered   = answeredCount === questions.length;

    return (
      <div className="qz-page">
        <TopNav activeTab="quiz" />
        <main className="qz-body">
          <div className="qz-attempt-header">
            <button className="qz-back-btn" onClick={resetToList} aria-label="Quay lại danh sách">
              ← Danh sách quiz
            </button>
            <h2 className="qz-attempt-title">{activeQuiz.title}</h2>
          </div>

          <div className="qz-progress-row">
            <span className="qz-progress-label">Câu {currentIdx + 1} / {questions.length}</span>
            <div className="qz-progress-track">
              <div className="qz-progress-fill" style={{ width: `${progressPct}%` }} />
            </div>
          </div>

          {error && <div className="qz-error" role="alert">{error}</div>}

          {q && (
            <QuizQuestion
              key={q.questionId}
              question={q}
              selectedOptionId={answers[q.questionId] ?? null}
              onAnswer={handleAnswer}
            />
          )}

          <div className="qz-nav-row">
            <button
              className="qz-nav-btn"
              onClick={() => setCurrentIdx((i) => Math.max(0, i - 1))}
              disabled={currentIdx === 0}
            >
              ← Câu trước
            </button>
            {currentIdx < questions.length - 1 ? (
              <button
                className="qz-nav-btn qz-nav-btn--next"
                onClick={() => setCurrentIdx((i) => i + 1)}
              >
                Câu tiếp →
              </button>
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

  // ── Result view ──
  if (view === 'result' && result) {
    const stars = result.scorePct >= 90 ? 5
                : result.scorePct >= 75 ? 4
                : result.scorePct >= 60 ? 3
                : result.scorePct >= 40 ? 2
                : 1;
    const correctCount = result.results?.filter((r) => r.isCorrect).length ?? result.score;
    const wrongCount   = (result.totalQuestions ?? 0) - correctCount;

    return (
      <div className="qz-page">
        <TopNav activeTab="quiz" />
        <main className="qz-body">
          <div className="qz-result-card">
            <div className="qz-result-emoji">🎉</div>
            <h2 className="qz-result-title">Hoàn thành Quiz!</h2>
            <div className="qz-result-score">
              {result.score} / {result.totalQuestions}
              <span className="qz-result-pct"> ({result.scorePct}%)</span>
            </div>
            <div className="qz-result-stars" aria-label={`${stars} sao`}>
              {'★'.repeat(stars)}{'☆'.repeat(5 - stars)}
            </div>
            <div className="qz-result-stats">
              <div className="qz-result-stat qz-result-stat--correct">Đúng: {correctCount}</div>
              <div className="qz-result-stat qz-result-stat--wrong">Sai: {wrongCount}</div>
            </div>
            <div className="qz-result-actions">
              <button
                className="qz-btn-secondary"
                onClick={() => startQuiz(activeQuiz)}
              >
                Làm lại
              </button>
              <button className="qz-btn-primary" onClick={resetToList}>Về danh sách</button>
            </div>
          </div>
        </main>
      </div>
    );
  }

  // ── List view ──
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
            <button
              key={l}
              role="tab"
              aria-selected={level === l}
              className={`qz-level-tab${level === l ? ' qz-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}
            >{l}</button>
          ))}
        </div>

        <div className="qz-skill-filter">
          <label className="visually-hidden" htmlFor="qz-skill-select">Kỹ năng</label>
          <select
            id="qz-skill-select"
            className="qz-select"
            value={skill}
            onChange={(e) => setSkill(e.target.value)}
          >
            {SKILLS.map((s) => <option key={s.id} value={s.id}>{s.label}</option>)}
          </select>
        </div>

        {error && (
          <div className="qz-error" role="alert">
            {error}
            <button className="qz-retry" onClick={fetchQuizzes}>Thử lại</button>
          </div>
        )}

        {isLoading || isLoadingQ ? (
          <div className="qz-list">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="qz-card-skel" aria-hidden="true" />
            ))}
          </div>
        ) : quizzes.length === 0 ? (
          <EmptyState
            title="Không có quiz nào"
            subtitle="Thử chọn level hoặc kỹ năng khác."
            mascotVariant="thinking"
            mascotSize={120}
          />
        ) : (
          <div className="qz-list">
            {quizzes.map((q) => (
              <QuizCard key={q.assessmentId} quiz={q} onStart={startQuiz} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
