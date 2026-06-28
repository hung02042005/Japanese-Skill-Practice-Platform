# SPEC — Speaking Practice + AI Grading (`/speaking`)
>
> **UC:** UC-13 — Luyện Speaking với AI Grading (Async)
> **Sprint:** 4 — AI Modules
> **Prefix:** `spk-` | **activeTab:** `'speaking'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §13.1` | **Backend ref:** `feat-ai/SPEC.md UC-13`
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **ADR ref:** CLAUDE.md ADR-007 (AI async + fallback), LESSON-006 (AI không silent fail)

---

## 1. MÔ TẢ TRANG

Hai màn hình trong cùng route:

1. **Speaking List** — Danh sách bài nói theo level. Mỗi bài có nội dung mẫu cần đọc.
2. **Speaking Practice** — Chọn bài → nghe mẫu → record audio → submit async → poll kết quả AI (transcript + score).

**Luồng async quan trọng:**

- Submit audio → nhận `{ jobId, status: PENDING }` → poll `/api/speaking/{jobId}` mỗi 3s → hiển thị kết quả khi `status: COMPLETED`.
- Timeout 60s với error rõ ràng nếu AI không trả về (theo LESSON-006).

---

## 2. MOCKUP

### 2.1 Speaking List

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="speaking")                                   │
├──────────────────────────────────────────────────────────────────┤
│  スピーキング Speaking Practice                                  │
│  Luyện phát âm và hội thoại với phản hồi từ AI.                │
│                                                                  │
│  [N5]  [N4]  [N3]  [N2]  [N1]                                   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  🎤 Giới thiệu bản thân (N5 — Cơ bản)                  │   │
│  │  "はじめまして。わたしは〜です。"                          │   │
│  │  Đã luyện: 2 lần · Điểm tốt nhất: 82%  [Luyện tập →]  │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  🎤 Gọi đồ ăn tại nhà hàng (N5 — Tình huống)           │   │
│  │  "すみません、これをください。"                            │   │
│  │  Chưa luyện                               [Luyện tập →] │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 Speaking Practice (active session)

```
┌──────────────────────────────────────────────────────────────────┐
│  ← Danh sách                                                     │
│  Giới thiệu bản thân (N5)                                       │
├──────────────────────────────────────────────────────────────────┤
│  Bước 1 — Nghe mẫu                                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  はじめまして。わたしは田中です。よろしくお願いします。  │   │
│  │  [▶ Nghe mẫu]                                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  Bước 2 — Ghi âm giọng đọc của bạn                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  [🎙 Bắt đầu ghi âm]        ⏱ 0:00 / 0:30              │   │
│  │  ── Đang ghi âm... ──                                    │   │
│  │  ████████████░░░░░░░░ (waveform)                         │   │
│  │  [⏹ Dừng]                                               │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  [Ghi lại]   [Nộp bài →]                                       │
│                                                                  │
│  ── Sau khi nộp — đang chờ AI ──                                 │
│  ⏳ Đang phân tích giọng nói... (có thể mất 10–30 giây)         │
│                                                                  │
│  ── Kết quả AI ──                                               │
│  Điểm: 82%  ●●●●○                                              │
│  Bản phiên âm: "はじめまして わたしは たなか..."               │
│  Từ phát âm tốt: はじめまして ✓, わたし ✓                     │
│  Từ cần cải thiện: お願い → phát âm không rõ                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/speaking/
├── SpeakingPage.jsx
└── SpeakingPage.css

components/student/
├── SpeakingCard.jsx      ← card bài luyện tập trong danh sách
└── AudioRecorder.jsx     ← component ghi âm với waveform
```

---

## 4. STATE

### 4.1 List view

```js
const [view,      setView]    = useState('list');   // 'list' | 'practice'
const [level,     setLevel]   = useState(user?.jlptLevel ?? 'N5');
const [exercises, setExercises] = useState([]);
const [isLoading, setLoading] = useState(true);
const [error,     setError]   = useState('');
```

### 4.2 Practice view

```js
const [activeEx,     setActiveEx]   = useState(null);  // exercise object
const [audioBlob,    setAudioBlob]  = useState(null);  // Blob sau khi ghi xong
const [isRecording,  setRecording]  = useState(false);
const [recordSecs,   setRecordSecs] = useState(0);
const [submitState,  setSubmitState]= useState('idle'); // 'idle' | 'uploading' | 'polling' | 'done' | 'error'
const [jobId,        setJobId]      = useState(null);
const [aiResult,     setAiResult]   = useState(null);
const [aiError,      setAiError]    = useState('');
const MAX_RECORD_SECS = 60;
const POLL_INTERVAL_MS = 3000;
const POLL_TIMEOUT_MS  = 60000;
```

---

## 5. API CALLS

```js
// GET /api/speaking/exercises?level=N5
// Response:
{
  "data": [
    {
      "exerciseId": 1,
      "title": "Giới thiệu bản thân",
      "level": "N5",
      "category": "basic",
      "targetText": "はじめまして。わたしは田中です。よろしくお願いします。",
      "sampleAudioUrl": "/uploads/speaking/ex1-sample.mp3",
      "bestScore": 82,
      "attemptCount": 2
    }
  ]
}

// POST /api/speaking/submit  (multipart/form-data)
// Request: FormData { exerciseId: 1, audio: <Blob mp3/webm> }
// Response: { "data": { "jobId": "spk_abc123", "status": "PENDING" } }

// GET /api/speaking/{jobId}
// Response (PENDING):  { "data": { "jobId": "spk_abc123", "status": "PENDING" } }
// Response (COMPLETED):
{
  "data": {
    "jobId": "spk_abc123",
    "status": "COMPLETED",
    "score": 82,
    "transcript": "はじめまして わたしは たなか",
    "wordResults": [
      { "word": "はじめまして", "correct": true },
      { "word": "わたし",       "correct": true },
      { "word": "お願い",       "correct": false, "feedback": "phát âm không rõ" }
    ]
  }
}
// Response (FAILED): { "data": { "jobId": "...", "status": "FAILED", "error": "Speech recognition unavailable" } }
```

API service (`studentService.js`):

```js
export async function getSpeakingExercises(level) {
  const res = await api.get('/speaking/exercises', { params: { level } });
  return res.data.data;
}

export async function submitSpeakingAudio(exerciseId, audioBlob) {
  const formData = new FormData();
  formData.append('exerciseId', exerciseId);
  formData.append('audio', audioBlob, 'recording.webm');
  const res = await api.post('/speaking/submit', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000,
  });
  return res.data.data; // { jobId, status: 'PENDING' }
}

export async function getSpeakingResult(jobId) {
  const res = await api.get(`/speaking/${jobId}`, { timeout: 10000 });
  return res.data.data;
}
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useRef, useCallback } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import SpeakingCard from '../../components/student/SpeakingCard';
import AudioRecorder from '../../components/student/AudioRecorder';
import { getSpeakingExercises, submitSpeakingAudio, getSpeakingResult } from '../../api/studentService';
import './SpeakingPage.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];
const POLL_INTERVAL_MS = 3000;
const POLL_TIMEOUT_MS  = 60000;

export default function SpeakingPage() {
  const { user } = useAppSelector((s) => s.auth);
  const [view,        setView]       = useState('list');
  const [level,       setLevel]      = useState(user?.jlptLevel ?? 'N5');
  const [exercises,   setExercises]  = useState([]);
  const [isLoading,   setLoading]    = useState(true);
  const [error,       setError]      = useState('');
  const [activeEx,    setActiveEx]   = useState(null);
  const [audioBlob,   setAudioBlob]  = useState(null);
  const [submitState, setSubmitState]= useState('idle');
  const [jobId,       setJobId]      = useState(null);
  const [aiResult,    setAiResult]   = useState(null);
  const [aiError,     setAiError]    = useState('');
  const pollRef    = useRef(null);
  const timeoutRef = useRef(null);
  const sampleRef  = useRef(null);

  useEffect(() => {
    setLoading(true); setError('');
    getSpeakingExercises(level)
      .then(setExercises)
      .catch((err) => setError(err?.response?.data?.message ?? 'Không thể tải bài luyện tập.'))
      .finally(() => setLoading(false));
  }, [level]);

  // Cleanup poll on unmount
  useEffect(() => () => {
    clearInterval(pollRef.current);
    clearTimeout(timeoutRef.current);
  }, []);

  const startPolling = useCallback((id) => {
    setSubmitState('polling');
    const startTime = Date.now();

    pollRef.current = setInterval(async () => {
      try {
        const data = await getSpeakingResult(id);
        if (data.status === 'COMPLETED') {
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
    }, POLL_INTERVAL_MS);

    timeoutRef.current = setTimeout(() => {
      clearInterval(pollRef.current);
      setAiError('Hết thời gian chờ AI phân tích. Vui lòng thử lại.');
      setSubmitState('error');
    }, POLL_TIMEOUT_MS);
  }, []);

  const handleSubmit = async () => {
    if (!audioBlob || submitState !== 'idle') return;
    setSubmitState('uploading');
    setAiError('');
    try {
      const { jobId: id } = await submitSpeakingAudio(activeEx.exerciseId, audioBlob);
      setJobId(id);
      startPolling(id);
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
    setJobId(null);
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

  // ─── PRACTICE VIEW ───
  if (view === 'practice' && activeEx) {
    const scoreStars = aiResult ? Math.round(aiResult.score / 20) : 0;

    return (
      <div className="spk-page">
        <TopNav activeTab="speaking" />
        <main className="spk-body">
          <button className="spk-back-btn" onClick={backToList}>← Danh sách</button>
          <h2 className="spk-practice-title">{activeEx.title} <span className="spk-level-badge">{activeEx.level}</span></h2>

          {/* Step 1: Sample */}
          <div className="spk-step-card">
            <div className="spk-step-label">Bước 1 — Nghe mẫu</div>
            <p className="spk-target-text" lang="ja">{activeEx.targetText}</p>
            <button className="spk-btn-sample" onClick={() => sampleRef.current?.play()} aria-label="Nghe bản mẫu">▶ Nghe mẫu</button>
            <audio ref={sampleRef} src={activeEx.sampleAudioUrl} preload="none" />
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
              <div className="spk-result-header">
                <span className="spk-result-score">{aiResult.score}%</span>
                <span className="spk-result-stars" aria-label={`${scoreStars} sao`}>
                  {'●'.repeat(scoreStars)}{'○'.repeat(5 - scoreStars)}
                </span>
              </div>
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
                      <div key={i} className={`spk-word-chip${w.correct ? ' spk-word-chip--ok' : ' spk-word-chip--bad'}`} title={w.feedback ?? ''}>
                        <span lang="ja">{w.word}</span>
                        <span>{w.correct ? ' ✓' : ' ✗'}</span>
                      </div>
                    ))}
                  </div>
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

  // ─── LIST VIEW ───
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
            <button key={l} role="tab" aria-selected={level === l}
              className={`spk-level-tab${level === l ? ' spk-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}>{l}</button>
          ))}
        </div>

        {error && <div className="spk-error" role="alert">{error}</div>}

        {isLoading ? (
          <div className="spk-list">{Array.from({ length: 3 }).map((_, i) => <div key={i} className="spk-card-skel" aria-hidden="true" />)}</div>
        ) : exercises.length === 0 ? (
          <EmptyState title="Chưa có bài luyện tập" subtitle="Nội dung đang được cập nhật. Thử level khác nhé!" mascotVariant="thinking" mascotSize={120} />
        ) : (
          <div className="spk-list">
            {exercises.map((ex) => <SpeakingCard key={ex.exerciseId} exercise={ex} onStart={openPractice} />)}
          </div>
        )}
      </main>
    </div>
  );
}
```

### AudioRecorder component (skeleton — cần Web MediaRecorder API)

```jsx
// components/student/AudioRecorder.jsx
import { useState, useRef, useEffect } from 'react';

export default function AudioRecorder({ maxSeconds = 60, onRecordingComplete, disabled }) {
  const [isRecording,  setRecording]  = useState(false);
  const [elapsed,      setElapsed]    = useState(0);
  const [audioUrl,     setAudioUrl]   = useState(null);
  const mediaRecRef   = useRef(null);
  const chunksRef     = useRef([]);
  const timerRef      = useRef(null);

  useEffect(() => () => { stopAll(); }, []);

  const stopAll = () => {
    clearInterval(timerRef.current);
    if (mediaRecRef.current?.state !== 'inactive') mediaRecRef.current?.stop();
  };

  const startRecording = async () => {
    if (disabled) return;
    setAudioUrl(null);
    chunksRef.current = [];
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const rec = new MediaRecorder(stream);
      mediaRecRef.current = rec;
      rec.ondataavailable = (e) => { if (e.data.size > 0) chunksRef.current.push(e.data); };
      rec.onstop = () => {
        stream.getTracks().forEach((t) => t.stop());
        const blob = new Blob(chunksRef.current, { type: 'audio/webm' });
        const url  = URL.createObjectURL(blob);
        setAudioUrl(url);
        onRecordingComplete(blob);
      };
      rec.start();
      setRecording(true);
      setElapsed(0);
      timerRef.current = setInterval(() => {
        setElapsed((s) => {
          if (s + 1 >= maxSeconds) { stopRecording(); return s + 1; }
          return s + 1;
        });
      }, 1000);
    } catch {
      // microphone permission denied or not available
    }
  };

  const stopRecording = () => {
    clearInterval(timerRef.current);
    if (mediaRecRef.current?.state === 'recording') mediaRecRef.current.stop();
    setRecording(false);
  };

  const fmt = (s) => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;

  return (
    <div className="spk-recorder">
      {!isRecording && !audioUrl && (
        <button className="spk-btn-record" onClick={startRecording} disabled={disabled} aria-label="Bắt đầu ghi âm">
          🎙 Bắt đầu ghi âm
        </button>
      )}
      {isRecording && (
        <div className="spk-recording-active">
          <span className="spk-rec-dot" aria-hidden="true" />
          <span className="spk-rec-label">Đang ghi âm...</span>
          <span className="spk-rec-timer" aria-live="polite">{fmt(elapsed)} / {fmt(maxSeconds)}</span>
          <button className="spk-btn-stop" onClick={stopRecording} aria-label="Dừng ghi âm">⏹ Dừng</button>
        </div>
      )}
      {audioUrl && !isRecording && (
        <div className="spk-playback">
          <audio controls src={audioUrl} aria-label="Nghe lại bản ghi âm của bạn" />
        </div>
      )}
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Speaking Page ===== */
.spk-page  { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.spk-body  { flex: 1; max-width: 780px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.spk-header   { display: flex; flex-direction: column; gap: 4px; }
.spk-title    { font-size: 26px; font-weight: 700; color: var(--color-text); margin: 0; }
.spk-subtitle { font-size: 14px; color: var(--color-text-sub); margin: 0; }

.spk-level-tabs  { display: flex; gap: 0; border-bottom: 2px solid var(--color-border); }
.spk-level-tab   { padding: 10px 20px; font-size: 15px; font-weight: 700; color: var(--color-text-sub); background: transparent; border: none; border-bottom: 2px solid transparent; margin-bottom: -2px; cursor: pointer; transition: color var(--transition), border-color var(--transition); }
.spk-level-tab--active { color: var(--color-primary); border-bottom-color: var(--color-primary); }

.spk-error { background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }

.spk-list     { display: flex; flex-direction: column; gap: 12px; }
.spk-card-skel{ height: 96px; border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }

/* Practice */
.spk-back-btn     { background: transparent; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); color: var(--color-text-sub); font-size: 13px; font-weight: 600; padding: 6px 14px; cursor: pointer; align-self: flex-start; transition: all var(--transition); }
.spk-back-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.spk-practice-title { font-size: 20px; font-weight: 700; color: var(--color-text); margin: 0; display: flex; align-items: center; gap: 10px; }
.spk-level-badge  { font-size: 12px; font-weight: 700; background: var(--color-primary-bg); color: var(--color-primary); border: 1.5px solid var(--color-primary-light); border-radius: var(--radius-sm); padding: 3px 9px; }

.spk-step-card    { background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: var(--radius-lg); padding: 20px 24px; display: flex; flex-direction: column; gap: 12px; }
.spk-step-label   { font-size: 12px; font-weight: 700; color: var(--color-text-sub); text-transform: uppercase; letter-spacing: 0.05em; }
.spk-target-text  { font-size: 18px; color: var(--color-text); line-height: 1.8; margin: 0; }
.spk-btn-sample   { align-self: flex-start; height: 36px; padding: 0 16px; background: var(--color-primary-bg); border: 1.5px solid var(--color-primary-light); border-radius: var(--radius-full); color: var(--color-primary); font-size: 13px; font-weight: 700; cursor: pointer; }

/* Recorder */
.spk-recorder       { display: flex; flex-direction: column; gap: 12px; align-items: flex-start; }
.spk-btn-record     { height: 48px; padding: 0 24px; background: var(--color-primary); color: white; border: none; border-radius: var(--radius-full); font-size: 15px; font-weight: 700; cursor: pointer; display: flex; align-items: center; gap: 8px; }
.spk-btn-record:disabled { opacity: 0.5; cursor: not-allowed; }
.spk-recording-active { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.spk-rec-dot        { width: 10px; height: 10px; border-radius: 50%; background: var(--color-error); animation: blink 1s ease infinite; }
.spk-rec-label      { font-size: 14px; font-weight: 600; color: var(--color-error); }
.spk-rec-timer      { font-size: 13px; color: var(--color-text-sub); font-variant-numeric: tabular-nums; }
.spk-btn-stop       { height: 36px; padding: 0 16px; background: var(--color-error); color: white; border: none; border-radius: var(--radius-full); font-size: 13px; font-weight: 700; cursor: pointer; }
.spk-playback audio { width: 100%; max-width: 420px; }

/* Submit actions */
.spk-action-row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.spk-btn-primary   { height: 44px; padding: 0 24px; background: var(--color-primary); color: white; border: none; border-radius: var(--radius-full); font-size: 15px; font-weight: 700; cursor: pointer; }
.spk-btn-secondary { height: 44px; padding: 0 20px; background: transparent; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); color: var(--color-text-sub); font-size: 14px; font-weight: 600; cursor: pointer; }

/* Status */
.spk-status         { display: flex; align-items: center; gap: 10px; font-size: 14px; padding: 14px 18px; border-radius: var(--radius-md); }
.spk-status--loading{ background: var(--color-primary-bg); color: var(--color-primary); border: 1px solid var(--color-primary-light); }
.spk-status--error  { background: #FFEAEA; color: var(--color-error); border: 1px solid var(--color-error); flex-wrap: wrap; gap: 8px; }
.spk-btn-retry      { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }
.spk-spinner        { width: 16px; height: 16px; border: 2px solid var(--color-primary-light); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.7s linear infinite; flex-shrink: 0; }

/* AI Result */
.spk-result         { background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: var(--radius-lg); padding: 24px; display: flex; flex-direction: column; gap: 16px; }
.spk-result-header  { display: flex; align-items: center; gap: 16px; }
.spk-result-score   { font-size: 36px; font-weight: 800; color: var(--color-primary); }
.spk-result-stars   { font-size: 22px; color: var(--color-primary); letter-spacing: 2px; }
.spk-result-section { display: flex; flex-direction: column; gap: 6px; }
.spk-result-section-label { font-size: 12px; font-weight: 700; color: var(--color-text-sub); text-transform: uppercase; letter-spacing: 0.05em; }
.spk-transcript     { font-size: 16px; color: var(--color-text); line-height: 1.7; margin: 0; }
.spk-word-results   { display: flex; flex-wrap: wrap; gap: 8px; }
.spk-word-chip      { display: flex; align-items: center; gap: 4px; padding: 5px 12px; border-radius: var(--radius-full); font-size: 14px; }
.spk-word-chip--ok  { background: var(--color-secondary-bg); color: var(--color-secondary); }
.spk-word-chip--bad { background: #FFEAEA; color: var(--color-error); }

@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }
@keyframes spin  { to { transform: rotate(360deg); } }

@media (max-width: 767px) {
  .spk-body { padding: 16px 16px 32px; }
  .spk-practice-title { font-size: 17px; }
}
@media (prefers-reduced-motion: reduce) { .spk-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 8. ACCESSIBILITY

- [ ] Level tabs dùng `role="tablist"` + `role="tab"` + `aria-selected`
- [ ] Timer ghi âm dùng `aria-live="polite"` để screen reader thông báo
- [ ] Trạng thái lỗi AI có `role="alert"` và message rõ ràng
- [ ] Audio playback có `aria-label` mô tả
- [ ] Kết quả sao có `aria-label` chứa số sao

## 9. GHI CHÚ QUAN TRỌNG

- Luồng **upload → poll** là bắt buộc. KHÔNG xử lý AI đồng bộ.
- Timeout 60s → show error rõ ràng (theo LESSON-006). KHÔNG silent fail.
- `MediaRecorder` API cần HTTPS hoặc localhost. Cần handle trường hợp browser không hỗ trợ.
- Không lưu audio blob vào DB — chỉ gửi lên server, server trả về URL tạm hoặc xử lý trực tiếp.
