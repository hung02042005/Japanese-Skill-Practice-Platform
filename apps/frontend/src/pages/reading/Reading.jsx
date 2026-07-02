import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { ReadingIcon } from '../../components/student/StudentIcons';
import { ConfettiIcon, ThumbsUpIcon, LightbulbIcon } from '../../components/common/AppIcons';
import { getReadingLessons, getReadingDetail, submitReading } from '../../api/studentService';
import './Reading.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function Reading() {
  const [searchParams] = useSearchParams();
  const { user } = useAppSelector((s) => s.auth);

  const [level,   setLevel]   = useState(searchParams.get('level') ?? user?.jlptLevel ?? 'N5');
  const [view,    setView]    = useState('list');

  const [lessons,     setLessons]     = useState([]);
  const [listLoading, setListLoading] = useState(true);
  const [listError,   setListError]   = useState('');

  const [lesson,        setLesson]        = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError,   setDetailError]   = useState('');

  const [answers,   setAnswers]   = useState({});
  const [results,   setResults]   = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  // ── Load lesson list cho level đang chọn ──
  const loadList = useCallback(async () => {
    setListLoading(true);
    setListError('');
    try {
      const page = await getReadingLessons({ level, page: 0, size: 50 });
      setLessons(page?.content ?? []);
    } catch (err) {
      setListError(err?.response?.data?.message ?? 'Không tải được danh sách bài đọc.');
      setLessons([]);
    } finally {
      setListLoading(false);
    }
  }, [level]);

  useEffect(() => { loadList(); }, [loadList]);

  // ── Mở 1 bài đọc → tải chi tiết ──
  async function openLesson(summary) {
    setView('practice');
    setLesson(null);
    setAnswers({});
    setResults(null);
    setSubmitError('');
    setDetailLoading(true);
    setDetailError('');
    try {
      const detail = await getReadingDetail(summary.id);
      setLesson(detail);
    } catch (err) {
      setDetailError(err?.response?.data?.message ?? 'Không tải được nội dung bài đọc.');
    } finally {
      setDetailLoading(false);
    }
  }

  function backToList() {
    setView('list');
    setLesson(null);
    setResults(null);
    setSubmitError('');
  }

  function pickAnswer(questionId, option) {
    if (results) return;
    setAnswers((prev) => ({ ...prev, [questionId]: option }));
  }

  async function submit() {
    if (!lesson) return;
    setSubmitting(true);
    setSubmitError('');
    try {
      const payload = lesson.questions.map((q) => ({
        questionId: q.questionId,
        selectedOption: answers[q.questionId],
      }));
      const res = await submitReading(lesson.id, payload);

      // Backend chỉ trả { questionId, isCorrect, correctOption, explanation }.
      // Merge với chi tiết câu hỏi (nội dung + đáp án) và lựa chọn của học viên.
      const byQuestion = new Map(lesson.questions.map((q) => [q.questionId, q]));
      const items = res.results.map((r) => {
        const q = byQuestion.get(r.questionId) ?? {};
        return {
          questionId:  r.questionId,
          content:     q.content,
          selected:    answers[r.questionId] ?? null,
          correct:     r.correctOption,
          isCorrect:   r.isCorrect,
          explanation: r.explanation,
          options: { A: q.optionA, B: q.optionB, C: q.optionC, D: q.optionD },
        };
      });
      const score = Number(res.score);
      const total = Number(res.maxScore);
      setResults({ score, total, items });
      setView('results');
    } catch (err) {
      setSubmitError(err?.response?.data?.message ?? 'Nộp bài thất bại. Vui lòng thử lại.');
    } finally {
      setSubmitting(false);
    }
  }

  const allAnswered = lesson && Object.keys(answers).length === lesson.questions.length;

  return (
    <div className="rdg-page">
      <TopNav activeTab="" />
      <main className="rdg-body">

        {/* ── LIST view ── */}
        {view === 'list' && (
          <>
            <div className="rdg-header">
              <h1 className="rdg-title">Luyện Đọc Hiểu</h1>
              <p className="rdg-subtitle">Đọc đoạn văn tiếng Nhật và trả lời câu hỏi trắc nghiệm</p>
            </div>

            <div className="rdg-levels">
              {LEVELS.map((l) => (
                <button
                  key={l}
                  className={`rdg-lvl-btn${level === l ? ' rdg-lvl-btn--active' : ''}`}
                  onClick={() => setLevel(l)}
                >
                  {l}
                </button>
              ))}
            </div>

            {listLoading ? (
              <p className="rdg-loading">Đang tải bài đọc…</p>
            ) : listError ? (
              <EmptyState
                title="Đã xảy ra lỗi"
                subtitle={listError}
                mascotVariant="thinking"
                mascotSize={120}
              />
            ) : lessons.length === 0 ? (
              <EmptyState
                title="Chưa có bài đọc"
                subtitle={`Chưa có bài đọc hiểu cho cấp độ ${level}.`}
                mascotVariant="thinking"
                mascotSize={120}
              />
            ) : (
              <div className="rdg-card-grid">
                {lessons.map((r) => (
                  <div key={r.id} className="rdg-card" onClick={() => openLesson(r)}>
                    <div className="rdg-card-head">
                      <JlptBadge level={r.jlptLevel} />
                      {r.hasAttempted && <span className="rdg-done-badge">Đã làm</span>}
                    </div>
                    <h2 className="rdg-card-title">{r.title}</h2>
                    <p className="rdg-card-meta">{r.questionCount} câu hỏi</p>
                    <button className="rdg-start-btn">
                      {r.hasAttempted ? 'Làm lại' : 'Bắt đầu'}
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {/* ── PRACTICE view ── */}
        {view === 'practice' && (
          <>
            <div className="rdg-practice-header">
              <button className="rdg-back-btn" onClick={backToList} aria-label="Quay lại">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M15 18l-6-6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                Danh sách
              </button>
              {lesson && (
                <div className="rdg-practice-meta">
                  <JlptBadge level={lesson.jlptLevel} />
                  <h1 className="rdg-practice-title">{lesson.title}</h1>
                </div>
              )}
            </div>

            {detailLoading ? (
              <p className="rdg-loading">Đang tải nội dung…</p>
            ) : detailError ? (
              <EmptyState
                title="Đã xảy ra lỗi"
                subtitle={detailError}
                mascotVariant="thinking"
                mascotSize={120}
              />
            ) : lesson && (
              <>
                <div className="rdg-passage-box">
                  <p className="rdg-passage-label">Đoạn văn</p>
                  <p className="rdg-passage-text">{lesson.passageText}</p>
                </div>

                <div className="rdg-questions">
                  {lesson.questions.map((q, idx) => (
                    <div key={q.questionId} className="rdg-question">
                      <p className="rdg-q-text">
                        <span className="rdg-q-num">{idx + 1}</span>
                        {q.content}
                      </p>
                      <div className="rdg-options">
                        {['A', 'B', 'C', 'D'].map((opt) => (
                          <button
                            key={opt}
                            className={`rdg-opt${answers[q.questionId] === opt ? ' rdg-opt--selected' : ''}`}
                            onClick={() => pickAnswer(q.questionId, opt)}
                            disabled={!!results}
                          >
                            <span className="rdg-opt-label">{opt}</span>
                            {q[`option${opt}`]}
                          </button>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>

                {submitError && <p className="rdg-error">{submitError}</p>}

                <div className="rdg-submit-row">
                  <p className="rdg-answered-note">
                    Đã trả lời: {Object.keys(answers).length} / {lesson.questions.length}
                  </p>
                  <button
                    className="rdg-submit-btn"
                    disabled={!allAnswered || submitting}
                    onClick={submit}
                  >
                    {submitting ? 'Đang nộp…' : 'Nộp bài'}
                  </button>
                </div>
              </>
            )}
          </>
        )}

        {/* ── RESULTS view ── */}
        {view === 'results' && results && lesson && (
          <>
            <div className="rdg-practice-header">
              <button className="rdg-back-btn" onClick={backToList} aria-label="Về danh sách">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M15 18l-6-6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                Danh sách
              </button>
            </div>

            <div className={`rdg-score-card${results.score === results.total ? ' rdg-score-card--perfect' : ''}`}>
              <div className="rdg-score-num">{results.score}<span>/{results.total}</span></div>
              <div className="rdg-score-label">
                {results.score === results.total
                  ? <><ConfettiIcon size={18} /> Hoàn hảo!</>
                  : results.score >= results.total / 2
                  ? <><ThumbsUpIcon size={18} /> Làm tốt!</>
                  : <><ReadingIcon size={18} /> Cần ôn thêm</>}
              </div>
              <div className="rdg-score-pct">
                {results.total > 0 ? Math.round((results.score / results.total) * 100) : 0}% chính xác
              </div>
            </div>

            <div className="rdg-results-list">
              {results.items.map((r, idx) => (
                <div key={r.questionId} className={`rdg-result-item${r.isCorrect ? ' rdg-result-item--ok' : ' rdg-result-item--wrong'}`}>
                  <div className="rdg-ri-header">
                    <span className="rdg-ri-icon" aria-hidden="true">{r.isCorrect ? '✓' : '✗'}</span>
                    <p className="rdg-ri-q">
                      <span className="rdg-q-num">{idx + 1}</span>
                      {r.content}
                    </p>
                  </div>
                  <div className="rdg-ri-opts">
                    {['A', 'B', 'C', 'D'].map((opt) => (
                      <div
                        key={opt}
                        className={[
                          'rdg-ri-opt',
                          opt === r.correct  ? 'rdg-ri-opt--correct'  : '',
                          opt === r.selected && opt !== r.correct ? 'rdg-ri-opt--wrong' : '',
                        ].join(' ').trim()}
                      >
                        <span className="rdg-opt-label">{opt}</span>
                        {r.options[opt]}
                      </div>
                    ))}
                  </div>
                  {r.explanation && <p className="rdg-ri-explain"><LightbulbIcon size={14} /> {r.explanation}</p>}
                </div>
              ))}
            </div>

            <div className="rdg-result-actions">
              <button className="rdg-retry-btn" onClick={() => openLesson(lesson)}>Làm lại</button>
              <button className="rdg-back2-btn" onClick={backToList}>Bài khác</button>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
