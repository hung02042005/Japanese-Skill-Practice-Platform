import { useState, useEffect, useRef, useCallback } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import SpeakingCard from '../../components/student/SpeakingCard';
import AudioRecorder from '../../components/student/AudioRecorder';
import { getSpeakingExercises, submitSpeakingAudio, getSpeakingResult } from '../../api/studentService';
import './SpeakingPage.css';

const LEVELS          = ['N5', 'N4', 'N3', 'N2', 'N1'];
const MAX_RECORD_SECS = 60;
const POLL_INTERVAL   = 3000;
const POLL_TIMEOUT    = 60000;

export default function SpeakingPage() {
  const { user } = useAppSelector((s) => s.auth);

  const [view,        setView]       = useState('list');
  const [level,       setLevel]      = useState(user?.jlptLevel ?? 'N5');
  const [exercises,   setExercises]  = useState([]);
  const [isLoading,   setLoading]    = useState(true);
  const [error,       setError]      = useState('');
  const [activeEx,    setActiveEx]   = useState(null);
  const [audioBlob,   setAudioBlob]  = useState(null);
  const [submitState, setSubmitState]= useState('idle'); // idle|uploading|polling|done|error
  const [aiResult,    setAiResult]   = useState(null);
  const [aiError,     setAiError]    = useState('');
  const pollRef    = useRef(null);
  const timeoutRef = useRef(null);

  useEffect(() => {
    setLoading(true);
    setError('');
    getSpeakingExercises(level)
      .then(setExercises)
      .catch((err) => setError(err?.response?.data?.message ?? 'Không thể tải bài luyện tập.'))
      .finally(() => setLoading(false));
  }, [level]);

  // Cleanup polling on unmount
  useEffect(() => () => {
    clearInterval(pollRef.current);
    clearTimeout(timeoutRef.current);
  }, []);

  const startPolling = useCallback((id) => {
    setSubmitState('polling');

    pollRef.current = setInterval(async () => {
      try {
        const data = await getSpeakingResult(id);
        if (data.status === 'COMPLETED' || data.status === 'AWAITING_REVIEW') {
          clearInterval(pollRef.current);
          clearTimeout(timeoutRef.current);
          setAiResult(data);
          setSubmitState('done');
        } else if (data.status === 'FAILED') {
          clearInterval(pollRef.current);
          clearTimeout(timeoutRef.current);
          setAiError(data.error ?? 'AI xử lý thất bại. Vui lòng thử lại.');
          setSubmitState('error');
        }
      } catch {
        // network hiccup — keep polling until timeout
      }
    }, POLL_INTERVAL);

    timeoutRef.current = setTimeout(() => {
      clearInterval(pollRef.current);
      setAiError('Hết thời gian chờ AI phân tích. Vui lòng thử lại.');
      setSubmitState('error');
    }, POLL_TIMEOUT);
  }, []);

  const handleSubmit = async () => {
    if (!audioBlob || submitState !== 'idle') return;
    setSubmitState('uploading');
    setAiError('');
    try {
      const { jobId } = await submitSpeakingAudio(activeEx.exerciseId, audioBlob);
      startPolling(jobId);
    } catch (err) {
      setAiError(err?.response?.data?.message ?? 'Không thể gửi bài. Vui lòng thử lại.');
      setSubmitState('error');
    }
  };

  const resetPractice = () => {
    clearInterval(pollRef.current);
    clearTimeout(timeoutRef.current);
    setAudioBlob(null);
    setSubmitState('idle');
    setAiResult(null);
    setAiError('');
  };

  const openPractice = (ex) => {
    resetPractice();
    setActiveEx(ex);
    setView('practice');
  };

  const backToList = () => {
    resetPractice();
    setView('list');
    setActiveEx(null);
  };

  // ── Practice view ──
  if (view === 'practice' && activeEx) {
    const questions = activeEx.questions?.length
      ? activeEx.questions
      : [{ promptText: activeEx.targetText, sampleAudioUrl: activeEx.sampleAudioUrl }];
    const hasScore = Number.isFinite(aiResult?.score);
    const scoreStars = hasScore ? Math.round(aiResult.score / 20) : 0;

    return (
      <div className="spk-page">
        <TopNav activeTab="speaking" />
        <main className="spk-body">
          <button className="spk-back-btn" onClick={backToList}>← Danh sách</button>

          <h2 className="spk-practice-title">
            {activeEx.title}{' '}
            <span className="spk-level-badge">{activeEx.level}</span>
          </h2>

          {/* Step 1: Review prompts and samples */}
          <div className="spk-step-card">
            <div className="spk-step-label">Bước 1 — Đọc câu hỏi và nghe mẫu</div>
            <div className="spk-question-list">
              {questions.map((question, index) => (
                <div className="spk-question-item" key={question.speakingQuestionId ?? index}>
                  <strong>Câu {index + 1}</strong>
                  <p className="spk-target-text" lang="ja">{question.promptText}</p>
                  {question.instruction && <p className="spk-question-instruction">{question.instruction}</p>}
                  {question.sampleAudioUrl && (
                    <audio controls preload="none" src={question.sampleAudioUrl} aria-label={`Nghe mẫu câu ${index + 1}`} />
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Step 2: Record */}
          <div className="spk-step-card">
            <div className="spk-step-label">Bước 2 — Ghi âm giọng đọc của bạn</div>
            <AudioRecorder
              maxSeconds={MAX_RECORD_SECS}
              onRecordingComplete={(blob) => setAudioBlob(blob)}
              disabled={submitState !== 'idle'}
            />
          </div>

          {/* Submit */}
          {audioBlob && submitState === 'idle' && (
            <div className="spk-action-row">
              <button className="spk-btn-secondary" onClick={resetPractice}>Ghi lại</button>
              <button className="spk-btn-primary" onClick={handleSubmit}>Nộp bài →</button>
            </div>
          )}

          {/* Uploading */}
          {submitState === 'uploading' && (
            <div className="spk-status spk-status--loading">⏳ Đang gửi bài...</div>
          )}

          {/* Polling */}
          {submitState === 'polling' && (
            <div className="spk-status spk-status--loading">
              <span className="spk-spinner" aria-hidden="true" />
              Đang phân tích giọng nói... (có thể mất 10–30 giây)
            </div>
          )}

          {/* Error */}
          {submitState === 'error' && (
            <div className="spk-status spk-status--error" role="alert">
              {aiError}
              <button className="spk-btn-retry" onClick={resetPractice}>Thử lại</button>
            </div>
          )}

          {/* AI Result */}
          {submitState === 'done' && aiResult && (
            <div className="spk-result">
              {aiResult.provisional && (
                <div className="spk-provisional">Điểm AI tạm thời — bài đang chờ giáo viên chấm.</div>
              )}
              {hasScore ? (
                <div className="spk-result-header">
                  <span className="spk-result-score">{aiResult.score}%</span>
                  <span className="spk-result-stars" aria-label={`${scoreStars} sao`}>
                    {'●'.repeat(scoreStars)}{'○'.repeat(5 - scoreStars)}
                  </span>
                </div>
              ) : (
                <div className="spk-status spk-status--loading">Bài đã được ghi nhận và đang chờ giáo viên đánh giá.</div>
              )}

              {aiResult.transcript && (
                <div className="spk-result-section">
                  <div className="spk-result-section-label">Bản phiên âm AI nghe được:</div>
                  <p className="spk-transcript" lang="ja">{aiResult.transcript}</p>
                </div>
              )}

              {aiResult.wordResults?.length > 0 && (
                <div className="spk-result-section">
                  <div className="spk-result-section-label">Chi tiết từng từ:</div>
                  <div className="spk-word-results">
                    {aiResult.wordResults.map((w, i) => (
                      <div
                        key={i}
                        className={`spk-word-chip${w.correct ? ' spk-word-chip--ok' : ' spk-word-chip--bad'}`}
                        title={w.feedback ?? ''}
                      >
                        <span lang="ja">{w.word}</span>
                        <span>{w.correct ? ' ✓' : ' ✗'}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {aiResult.feedback && (
                <div className="spk-result-section">
                  <div className="spk-result-section-label">Nhận xét:</div>
                  <p>{aiResult.feedback}</p>
                </div>
              )}

              <div className="spk-action-row">
                <button className="spk-btn-secondary" onClick={resetPractice}>Luyện lại</button>
                <button className="spk-btn-primary" onClick={backToList}>Chọn bài khác</button>
              </div>
            </div>
          )}
        </main>
      </div>
    );
  }

  // ── List view ──
  return (
    <div className="spk-page">
      <TopNav activeTab="speaking" />
      <main className="spk-body">
        <div className="spk-header">
          <h1 className="spk-title"><span lang="ja">スピーキング</span> Speaking Practice</h1>
          <p className="spk-subtitle">Luyện phát âm và hội thoại với phản hồi từ AI.</p>
        </div>

        <div className="spk-level-tabs" role="tablist" aria-label="Chọn cấp độ JLPT">
          {LEVELS.map((l) => (
            <button
              key={l}
              role="tab"
              aria-selected={level === l}
              className={`spk-level-tab${level === l ? ' spk-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}
            >{l}</button>
          ))}
        </div>

        {error && <div className="spk-error" role="alert">{error}</div>}

        {isLoading ? (
          <div className="spk-list">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="spk-card-skel" aria-hidden="true" />
            ))}
          </div>
        ) : exercises.length === 0 ? (
          <EmptyState
            title="Chưa có bài luyện tập"
            subtitle="Nội dung đang được cập nhật. Thử level khác nhé!"
            mascotVariant="thinking"
            mascotSize={120}
          />
        ) : (
          <div className="spk-list">
            {exercises.map((ex) => (
              <SpeakingCard key={ex.exerciseId} exercise={ex} onStart={openPractice} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
