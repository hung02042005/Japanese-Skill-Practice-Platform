import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ExamTopBar from '../../components/student/ExamTopBar';
import ExamNavigator from '../../components/student/ExamNavigator';
import { startAssessment, submitAssessment } from '../../api/studentService';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage } from '../../utils/apiMessage';
import './MockTestAttempt.css';

export default function MockTestAttempt() {
  const { id }   = useParams();
  const navigate = useNavigate();
  const { success, error: toastError } = useToast();

  const [exam,        setExam]    = useState(null);
  const [attemptId,   setAttemptId] = useState(null);
  const [questions,   setQs]      = useState([]);
  const [answers,     setAnswers] = useState({});
  const [currentIdx,  setIdx]     = useState(0);
  const [timeLeft,    setTime]    = useState(null);
  const [isLoading,   setLoading] = useState(true);
  const [isSubmitting,setSubmit]  = useState(false);
  const [showConfirm, setConfirm] = useState(false);
  const [error,       setError]   = useState('');

  const timerRef  = useRef(null);
  const submitted = useRef(false);
  const DRAFT_KEY = `exam_draft_${id}`;

  // Load exam
  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const data = await startAssessment(id);
        const allQs = data.sections?.flatMap((s) =>
          s.questions.map((q) => ({ ...q, sectionName: s.sectionName }))
        ) ?? data.questions ?? [];
        setExam(data);
        setAttemptId(data.attemptId);
        setQs(allQs);
        setTime(Math.max(0, Math.floor((new Date(data.expiresAt).getTime() - Date.now()) / 1000)));
        const draft = localStorage.getItem(DRAFT_KEY);
        if (draft) {
          try { setAnswers(JSON.parse(draft)); } catch { /* ignore */ }
        }
      } catch {
        setError('Không thể tải đề thi. Vui lòng thử lại.');
      } finally {
        setLoading(false);
      }
    })();
    return () => clearInterval(timerRef.current);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  // Timer
  const isRunning = timeLeft !== null && timeLeft > 0;
  useEffect(() => {
    if (!isRunning) return;
    timerRef.current = setInterval(() => {
      setTime((t) => {
        if (t <= 1) {
          clearInterval(timerRef.current);
          if (!submitted.current) doSubmit(true);
          return 0;
        }
        return t - 1;
      });
    }, 1000);
    return () => clearInterval(timerRef.current);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isRunning]);

  function handleSelect(questionId, option) {
    const updated = { ...answers, [questionId]: option };
    setAnswers(updated);
    localStorage.setItem(DRAFT_KEY, JSON.stringify(updated));
  }

  async function doSubmit(isAutoSubmit = false) {
    if (submitted.current || isSubmitting) return;
    submitted.current = true;
    setSubmit(true);
    clearInterval(timerRef.current);
    try {
      const payload = questions.map((q) => ({
        questionId:     q.questionId,
        selectedOption: answers[q.questionId] ?? null,
      }));
      const result = await submitAssessment(id, { attemptId, isAutoSubmit, answers: payload });
      localStorage.removeItem(DRAFT_KEY);
      success(isAutoSubmit ? 'Hết giờ — bài thi đã được nộp tự động.' : 'Đã nộp bài thi thành công.');
      navigate(`/mock-test/${id}/results?attemptId=${result.attemptId}`);
    } catch (err) {
      const msg = getErrorMessage(err, 'Nộp bài thất bại. Vui lòng thử lại.');
      setError(msg);
      toastError(msg);
      submitted.current = false;
      setSubmit(false);
    }
  }

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

  const minutes = Math.floor((timeLeft ?? 0) / 60);
  const seconds = (timeLeft ?? 0) % 60;
  const timeStr = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  const isUrgent = (timeLeft ?? 0) < 300;
  const unansweredCount = questions.filter((q) => !answers[q.questionId]).length;
  const currentQ = questions[currentIdx];

  return (
    <div className="mxa-page">
      <ExamTopBar
        title={exam?.title ?? ''}
        timeString={timeStr}
        isUrgent={isUrgent}
        onSubmit={() => setConfirm(true)}
        isSubmitting={isSubmitting}
      />

      <div className="mxa-progress-bar-wrap">
        <div
          className="mxa-progress-fill"
          style={{ width: `${questions.length ? ((currentIdx + 1) / questions.length) * 100 : 0}%` }}
          role="progressbar"
          aria-valuenow={currentIdx + 1}
          aria-valuemin={1}
          aria-valuemax={questions.length}
          aria-label={`Câu ${currentIdx + 1} trên ${questions.length}`}
        />
      </div>

      <div className="mxa-layout">
        <main className="mxa-question-panel">
          {currentQ && (
            <>
              <div className="mxa-q-meta">
                <span className="mxa-q-num">Câu {currentIdx + 1} / {questions.length}</span>
                {currentQ.sectionName && (
                  <span className="mxa-q-skill">{currentQ.sectionName}</span>
                )}
              </div>

              {currentQ.audioUrl && (
                <audio
                  className="mxa-audio"
                  controls
                  src={currentQ.audioUrl}
                  aria-label={`Audio câu ${currentIdx + 1}`}
                />
              )}

              <p className="mxa-q-text">{currentQ.questionText}</p>

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
                        onChange={() => handleSelect(currentQ.questionId, opt)}
                        className="mxa-sr-only"
                        aria-label={`Đáp án ${opt}: ${text}`}
                      />
                      <span className="mxa-opt-letter" aria-hidden="true">{opt}</span>
                      <span className="mxa-opt-text">{text}</span>
                    </label>
                  );
                })}
              </div>

              <div className="mxa-q-nav">
                <button
                  className="mxa-nav-btn"
                  onClick={() => setIdx((i) => Math.max(0, i - 1))}
                  disabled={currentIdx === 0}
                >
                  ← Câu trước
                </button>
                <button
                  className="mxa-nav-btn mxa-nav-btn--next"
                  onClick={() => setIdx((i) => Math.min(questions.length - 1, i + 1))}
                  disabled={currentIdx === questions.length - 1}
                >
                  Câu tiếp theo →
                </button>
              </div>
            </>
          )}
        </main>

        <ExamNavigator
          questions={questions}
          answers={answers}
          currentIdx={currentIdx}
          onJump={setIdx}
          onSubmit={() => setConfirm(true)}
          unansweredCount={unansweredCount}
        />
      </div>

      {error && (
        <div className="mxa-error-banner" role="alert">
          <span>{error}</span>
          <button onClick={() => setError('')}>✕</button>
        </div>
      )}

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
            {unansweredCount > 0 ? (
              <p className="mxa-modal-body">
                Bạn còn <strong>{unansweredCount} câu chưa trả lời</strong>. Sau khi nộp, bạn không thể thay đổi.
              </p>
            ) : (
              <p className="mxa-modal-body">
                Bạn đã trả lời tất cả {questions.length} câu. Xác nhận nộp bài?
              </p>
            )}
            <div className="mxa-modal-footer">
              <button className="mxa-modal-cancel" onClick={() => setConfirm(false)}>Về làm tiếp</button>
              <button
                className="mxa-modal-submit"
                onClick={() => doSubmit(false)}
                disabled={isSubmitting}
                aria-busy={isSubmitting}
              >
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
