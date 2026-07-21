import { useState, useEffect, useRef } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import SpeakingCard from '../../components/student/SpeakingCard';
import AudioRecorder from '../../components/student/AudioRecorder';
import { getSpeakingExercises, submitSpeakingAudio } from '../../api/studentService';
import './SpeakingPage.css';

const LEVELS          = ['N5', 'N4', 'N3', 'N2', 'N1'];
const MAX_RECORD_SECS = 60;

export default function SpeakingPage() {
  const { user } = useAppSelector((s) => s.auth);

  const [view,        setView]       = useState('list');
  const [level,       setLevel]      = useState(user?.jlptLevel ?? 'N5');
  const [exercises,   setExercises]  = useState([]);
  const [isLoading,   setLoading]    = useState(true);
  const [error,       setError]      = useState('');
  const [activeEx,    setActiveEx]   = useState(null);
  const [audioBlob,   setAudioBlob]  = useState(null);
  const [submitState, setSubmitState]= useState('idle'); // idle | uploading | submitted | error
  const [submitError, setSubmitError]= useState('');
  const [isSpeaking,  setSpeaking]   = useState(false);
  const [sampleNote,  setSampleNote] = useState('');
  const sampleRef = useRef(null);

  useEffect(() => {
    setLoading(true);
    setError('');
    getSpeakingExercises(level)
      .then(setExercises)
      .catch((err) => setError(err?.response?.data?.message ?? 'Không thể tải bài luyện tập.'))
      .finally(() => setLoading(false));
  }, [level]);

  // Dừng đọc mẫu khi rời trang
  useEffect(() => () => {
    if (typeof window !== 'undefined') window.speechSynthesis?.cancel();
  }, []);

  const handleSubmit = async () => {
    if (!audioBlob || submitState !== 'idle') return;
    setSubmitState('uploading');
    setSubmitError('');
    try {
      await submitSpeakingAudio(activeEx.exerciseId, audioBlob);
      setSubmitState('submitted');
    } catch (err) {
      setSubmitError(err?.response?.data?.message ?? 'Không thể gửi bài. Vui lòng thử lại.');
      setSubmitState('error');
    }
  };

  const resetPractice = () => {
    if (typeof window !== 'undefined') window.speechSynthesis?.cancel();
    setSpeaking(false);
    setAudioBlob(null);
    setSubmitState('idle');
    setSubmitError('');
  };

  // Phát mẫu: ưu tiên file audio nếu bài có; nếu không, đọc câu bằng Web Speech (TTS).
  const speakTargetText = () => {
    const synth = typeof window !== 'undefined' ? window.speechSynthesis : null;
    if (!synth || typeof window.SpeechSynthesisUtterance === 'undefined') {
      setSampleNote('Trình duyệt không hỗ trợ phát mẫu tự động. Hãy dùng Chrome hoặc Edge.');
      return;
    }
    setSampleNote('');
    synth.cancel();
    const utter = new window.SpeechSynthesisUtterance(activeEx.targetText || '');
    utter.lang = 'ja-JP';
    utter.rate = 0.9;
    const jaVoice = synth.getVoices().find((v) => v.lang?.toLowerCase().startsWith('ja'));
    if (jaVoice) utter.voice = jaVoice;
    utter.onend = () => setSpeaking(false);
    utter.onerror = () => setSpeaking(false);
    setSpeaking(true);
    synth.speak(utter);
  };

  const playSample = () => {
    if (!activeEx) return;
    if (isSpeaking) {
      window.speechSynthesis?.cancel();
      setSpeaking(false);
      return;
    }
    if (activeEx.sampleAudioUrl && sampleRef.current) {
      sampleRef.current
        .play()
        .then(() => setSpeaking(true))
        .catch(() => speakTargetText());
      return;
    }
    speakTargetText();
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
    return (
      <div className="spk-page">
        <TopNav activeTab="speaking" />
        <main className="spk-body">
          <button className="spk-back-btn" onClick={backToList}>← Danh sách</button>

          <h2 className="spk-practice-title">
            {activeEx.title}{' '}
            <span className="spk-level-badge">{activeEx.level}</span>
          </h2>

          {/* Step 1: Listen to sample */}
          <div className="spk-step-card">
            <div className="spk-step-label">Bước 1 — Nghe mẫu</div>
            <p className="spk-target-text" lang="ja">{activeEx.targetText}</p>
            <button
              className="spk-btn-sample"
              onClick={playSample}
              aria-label={isSpeaking ? 'Dừng phát mẫu' : 'Nghe bản mẫu'}
            >
              {isSpeaking ? '⏸ Đang phát...' : '▶ Nghe mẫu'}
            </button>
            {sampleNote && <p className="spk-sample-note">{sampleNote}</p>}
            {activeEx.sampleAudioUrl && (
              <audio
                ref={sampleRef}
                src={activeEx.sampleAudioUrl}
                preload="none"
                onEnded={() => setSpeaking(false)}
              />
            )}
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
            <div className="spk-status spk-status--loading">
              <span className="spk-spinner" aria-hidden="true" />
              Đang gửi bài...
            </div>
          )}

          {/* Error */}
          {submitState === 'error' && (
            <div className="spk-status spk-status--error" role="alert">
              {submitError}
              <button className="spk-btn-retry" onClick={resetPractice}>Thử lại</button>
            </div>
          )}

          {/* Submitted — chờ giáo viên chấm */}
          {submitState === 'submitted' && (
            <div className="spk-result">
              <div className="spk-submitted-head">
                <span className="spk-submitted-check" aria-hidden="true">✓</span>
                <h3 className="spk-submitted-title">Đã nộp bài thành công</h3>
              </div>
              <p className="spk-submitted-text">
                Bài nói của bạn đã được gửi tới giáo viên. Bạn sẽ nhận thông báo khi bài được chấm điểm.
              </p>
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
          <p className="spk-subtitle">Luyện phát âm và hội thoại, giáo viên sẽ chấm và phản hồi.</p>
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
