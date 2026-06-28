# SPEC — Luyện viết Kanji OCR (`/kanji/:id`)
>
> **Sprint:** 4 — Monetization & Retention
> **Prefix:** `koc-` | **activeTab:** `'kanji'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §12.4` | **Backend ref:** `feat-core-learning/SPEC.md`, `feat-ai-skills/SPEC.md`

---

## 1. MÔ TẢ TRANG

Chi tiết một Kanji cụ thể: thông tin (onyomi, kunyomi, meaning, strokeCount), stroke order (ảnh tĩnh), ví dụ từ. Panel OCR: upload ảnh chữ viết tay → gọi AI async → hiển thị similarity %. Điều hướng sang kanji trước/sau. Nút thêm vào Flashcard.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="kanji")                                      │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ← Quay lại danh sách   [N5]                                    │
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                                                           │  │
│  │  日                                         [+ Flashcard] │  │
│  │  (80px, font-size 80px)                                   │  │
│  │                                                           │  │
│  │  Onyomi:  ニチ / ジツ                                     │  │
│  │  Kunyomi: ひ / -び / -か                                  │  │
│  │  Nghĩa:   Ngày, Mặt trời                                  │  │
│  │  Nét:     4 nét                                           │  │
│  │                                                           │  │
│  │  [Stroke order image]                                     │  │
│  │                                                           │  │
│  │  Ví dụ: 日本 (にほん) — Nhật Bản                         │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Luyện viết (OCR)                                               │
│  ──────────────────────────────────────────────────────────────  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                                                           │  │
│  │  [Upload ảnh chữ viết tay hoặc kéo thả vào đây]          │  │
│  │  [📷 Chọn ảnh]   hỗ trợ JPG, PNG, WEBP                   │  │
│  │                                                           │  │
│  │  [Ảnh preview nếu đã chọn]                                │  │
│  │                                                           │  │
│  │  [Phân tích ngay →]                                       │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Kết quả OCR (sau khi xong):                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  [Spinner] Đang phân tích... (khi PENDING)               │  │
│  │  ─── hoặc ───                                            │  │
│  │  Độ tương đồng: 78%  [████████░░] Tốt                    │  │
│  │  [Luyện lại]                                             │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  [← 水 Kanji trước]           [山 Kanji tiếp theo →]           │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/kanji/
├── KanjiPractice.jsx
└── KanjiPractice.css
```

---

## 4. STATE

```js
const { id } = useParams();

// Kanji data
const [kanji,      setKanji]    = useState(null);
const [isLoading,  setLoading]  = useState(true);
const [error,      setError]    = useState('');

// OCR
const [file,       setFile]     = useState(null);    // File object
const [preview,    setPreview]  = useState(null);    // blob URL
const [jobId,      setJobId]    = useState(null);    // polling job
const [ocrState,   setOcrState] = useState('idle');  // 'idle'|'pending'|'done'|'error'
const [similarity, setSimilarity]= useState(null);  // 0-100
const [ocrError,   setOcrError] = useState('');
const [isAnalyzing,setAnalyzing]= useState(false);
const pollRef = useRef(null);

// Flashcard
const [addedFlash, setAdded]    = useState(false);
const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

```js
// GET /api/kanji/:id
// Response: { data: { kanjiId, characterValue, onyomi, kunyomi, meaning, strokeCount, strokeOrderUrl, jlptLevel, exampleWord, exampleReading, exampleMeaning, progressStatus, prevKanjiId, nextKanjiId } }

// POST /api/ai/ocr/submit (multipart)
// Body: FormData { kanjiId, imageFile }
// Response: { data: { jobId } }

// GET /api/ai/ocr/:jobId  (polling 1s, max 30s)
// Response: { data: { status: 'PENDING'|'COMPLETED'|'FAILED', similarity: 0-100 } }

// POST /api/flashcards
// Request: { contentType: 'kanji', contentId: kanjiId }
// Response: 201 | 409

// POST /api/learning-progress
// Request: { contentType: 'kanji', contentId: kanjiId, status: 'completed', progressPercent: 100 }
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { getKanjiDetail, submitOcr, getOcrResult, addToFlashcard, markProgress } from '../../api/studentService';
import './KanjiPractice.css';

function SimilarityGauge({ value }) {
  const level = value >= 80 ? 'excellent' : value >= 60 ? 'good' : 'practice';
  const labels = { excellent: 'Xuất sắc 🌸', good: 'Tốt ✓', practice: 'Cần luyện thêm' };
  return (
    <div className={`koc-gauge koc-gauge--${level}`}>
      <div className="koc-gauge-bar-wrap">
        <div className="koc-gauge-bar">
          <div className="koc-gauge-fill" style={{ width: `${value}%` }} />
        </div>
      </div>
      <div className="koc-gauge-info">
        <span className="koc-gauge-pct">{value}%</span>
        <span className="koc-gauge-label">{labels[level]}</span>
      </div>
    </div>
  );
}

export default function KanjiPractice() {
  const { id }   = useParams();
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [kanji,      setKanji]    = useState(null);
  const [isLoading,  setLoading]  = useState(true);
  const [error,      setError]    = useState('');
  const [file,       setFile]     = useState(null);
  const [preview,    setPreview]  = useState(null);
  const [ocrState,   setOcrState] = useState('idle');
  const [similarity, setSimilarity]= useState(null);
  const [ocrError,   setOcrError] = useState('');
  const [isAnalyzing,setAnalyzing]= useState(false);
  const [addedFlash, setAdded]    = useState(false);
  const pollRef = useRef(null);

  // Load kanji
  useEffect(() => {
    (async () => {
      setLoading(true); setError('');
      try {
        const data = await getKanjiDetail(id);
        setKanji(data);
      } catch (err) {
        if (err?.response?.status === 404) { navigate('/404'); return; }
        setError('Không thể tải thông tin Kanji.');
      } finally {
        setLoading(false);
      }
    })();
    return () => clearInterval(pollRef.current);
  }, [id, navigate]);

  // Reset OCR khi đổi kanji
  useEffect(() => {
    setFile(null); setPreview(null);
    setOcrState('idle'); setSimilarity(null); setOcrError('');
  }, [id]);

  function handleFileChange(e) {
    const f = e.target.files?.[0];
    if (!f) return;
    if (f.size > 5 * 1024 * 1024) { addToast('error', 'Ảnh tối đa 5MB.'); return; }
    setFile(f);
    setPreview(URL.createObjectURL(f));
    setOcrState('idle'); setSimilarity(null); setOcrError('');
  }

  async function handleAnalyze() {
    if (!file) return;
    setAnalyzing(true); setOcrError(''); setOcrState('pending');
    try {
      const res = await submitOcr(kanji.kanjiId, file);
      startPolling(res.jobId);
    } catch {
      setOcrState('error'); setOcrError('Không thể gửi ảnh. Thử lại sau.');
      setAnalyzing(false);
    }
  }

  function startPolling(jobId) {
    let tries = 0;
    pollRef.current = setInterval(async () => {
      tries++;
      if (tries > 30) {
        clearInterval(pollRef.current);
        setOcrState('error'); setOcrError('Phân tích quá lâu. Vui lòng thử lại.');
        setAnalyzing(false);
        return;
      }
      try {
        const result = await getOcrResult(jobId);
        if (result.status === 'COMPLETED') {
          clearInterval(pollRef.current);
          setSimilarity(result.similarity);
          setOcrState('done');
          setAnalyzing(false);
          // Mark as reviewed
          await markProgress('kanji', kanji.kanjiId, 'completed').catch(() => {});
        } else if (result.status === 'FAILED') {
          clearInterval(pollRef.current);
          setOcrState('error'); setOcrError('AI không thể phân tích ảnh này.');
          setAnalyzing(false);
        }
      } catch { /* keep polling */ }
    }, 1000);
  }

  async function handleAddFlashcard() {
    try {
      await addToFlashcard('kanji', kanji.kanjiId);
      setAdded(true);
      addToast('success', `Đã thêm "${kanji.characterValue}" vào Flashcard!`);
    } catch (err) {
      if (err?.response?.status === 409) { addToast('info', 'Kanji này đã có trong Flashcard.'); return; }
      addToast('error', 'Không thể thêm vào Flashcard.');
    }
  }

  if (isLoading) return <div className="koc-page"><TopNav activeTab="kanji" /><div className="koc-loading"><div className="koc-spinner-lg" /></div></div>;
  if (error) return <div className="koc-page"><TopNav activeTab="kanji" /><div className="koc-body"><div className="koc-error" role="alert">{error}</div></div></div>;

  return (
    <div className="koc-page">
      <TopNav activeTab="kanji" />
      <main className="koc-body">
        <div className="koc-nav-header">
          <Link to="/kanji" className="koc-back-link">← Danh sách Kanji</Link>
          <JlptBadge level={kanji.jlptLevel} />
        </div>

        {/* Kanji info card */}
        <div className="koc-info-card">
          <div className="koc-main-row">
            <div className="koc-char-display">
              <span className="koc-char" lang="ja">{kanji.characterValue}</span>
              <span className="koc-stroke-count">{kanji.strokeCount} nét</span>
            </div>
            <div className="koc-readings">
              {kanji.onyomi  && <div className="koc-reading-row"><span className="koc-reading-label">On'yomi:</span><span className="koc-reading-val" lang="ja">{kanji.onyomi}</span></div>}
              {kanji.kunyomi && <div className="koc-reading-row"><span className="koc-reading-label">Kun'yomi:</span><span className="koc-reading-val" lang="ja">{kanji.kunyomi}</span></div>}
              <div className="koc-reading-row"><span className="koc-reading-label">Nghĩa:</span><span className="koc-reading-val">{kanji.meaning}</span></div>
            </div>
            <button
              className={`koc-flash-btn${addedFlash ? ' koc-flash-btn--added' : ''}`}
              onClick={handleAddFlashcard}
              disabled={addedFlash}
              aria-label={`${addedFlash ? 'Đã thêm' : 'Thêm'} "${kanji.characterValue}" vào Flashcard`}
            >
              {addedFlash ? '✓ Đã thêm' : '+ Flashcard'}
            </button>
          </div>

          {/* Stroke order */}
          {kanji.strokeOrderUrl && (
            <div className="koc-stroke-wrap">
              <img
                src={kanji.strokeOrderUrl}
                alt={`Thứ tự nét viết của ${kanji.characterValue}`}
                className="koc-stroke-img"
              />
            </div>
          )}

          {/* Example word */}
          {kanji.exampleWord && (
            <div className="koc-example">
              <span className="koc-ex-label">Ví dụ từ:</span>
              <span className="koc-ex-word" lang="ja">{kanji.exampleWord}</span>
              {kanji.exampleReading && <span className="koc-ex-reading" lang="ja">({kanji.exampleReading})</span>}
              {kanji.exampleMeaning && <span className="koc-ex-meaning">— {kanji.exampleMeaning}</span>}
            </div>
          )}
        </div>

        {/* OCR Panel */}
        <section className="koc-ocr-section">
          <h2 className="koc-ocr-title">Luyện viết (OCR)</h2>

          <div className="koc-upload-area">
            {!preview ? (
              <label className="koc-upload-label" htmlFor="koc-file-input">
                <div className="koc-upload-icon" aria-hidden="true">📷</div>
                <span className="koc-upload-text">Tải ảnh chữ viết tay của bạn</span>
                <span className="koc-upload-hint">Hỗ trợ JPG, PNG, WEBP · Tối đa 5MB</span>
                <input
                  id="koc-file-input"
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  className="koc-sr-only"
                  onChange={handleFileChange}
                />
              </label>
            ) : (
              <div className="koc-preview-wrap">
                <img src={preview} alt="Ảnh chữ viết tay" className="koc-preview-img" />
                <button className="koc-change-btn" onClick={() => { setFile(null); setPreview(null); setOcrState('idle'); }}>Đổi ảnh</button>
              </div>
            )}
          </div>

          {file && ocrState === 'idle' && (
            <button className="koc-analyze-btn" onClick={handleAnalyze} disabled={isAnalyzing}>
              {isAnalyzing && <span className="koc-spinner" aria-hidden="true" />}
              Phân tích ngay →
            </button>
          )}

          {ocrState === 'pending' && (
            <div className="koc-ocr-pending" role="status">
              <div className="koc-spinner-lg" />
              <span>AI đang phân tích chữ viết của bạn...</span>
            </div>
          )}

          {ocrState === 'done' && similarity !== null && (
            <div className="koc-ocr-result">
              <SimilarityGauge value={similarity} />
              <button className="koc-retry-ocr-btn" onClick={() => { setOcrState('idle'); setFile(null); setPreview(null); setSimilarity(null); }}>
                Luyện lại
              </button>
            </div>
          )}

          {ocrState === 'error' && (
            <div className="koc-ocr-error" role="alert">
              <span>{ocrError}</span>
              <button className="koc-retry-btn" onClick={() => { setOcrState('idle'); setSimilarity(null); }}>Thử lại</button>
            </div>
          )}
        </section>

        {/* Footer navigation */}
        <div className="koc-footer">
          {kanji.prevKanjiId
            ? <button className="koc-nav-btn" onClick={() => navigate(`/kanji/${kanji.prevKanjiId}`)}>← Kanji trước</button>
            : <div />
          }
          {kanji.nextKanjiId
            ? <button className="koc-nav-btn koc-nav-btn--next" onClick={() => navigate(`/kanji/${kanji.nextKanjiId}`)}>Kanji tiếp theo →</button>
            : <div />
          }
        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Kanji Practice OCR (SakuJi Hanami Theme) ===== */
.koc-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.koc-body { flex: 1; max-width: 760px; width: 100%; margin: 0 auto; padding: 24px 32px 48px; display: flex; flex-direction: column; gap: 24px; box-sizing: border-box; }

.koc-nav-header { display: flex; align-items: center; gap: 12px; }
.koc-back-link { font-size: 13px; color: var(--color-text-sub); text-decoration: none; font-weight: 600; }
.koc-back-link:hover { color: var(--color-primary); }

/* Info card */
.koc-info-card { background: var(--color-card); border-radius: var(--radius-xl); box-shadow: var(--shadow-md); padding: 28px; display: flex; flex-direction: column; gap: 20px; }
.koc-main-row { display: flex; align-items: flex-start; gap: 24px; }
.koc-char-display { display: flex; flex-direction: column; align-items: center; gap: 4px; flex-shrink: 0; }
.koc-char { font-size: 80px; font-weight: 700; color: var(--color-text); line-height: 1; }
.koc-stroke-count { font-size: 12px; color: var(--color-text-sub); }
.koc-readings { display: flex; flex-direction: column; gap: 8px; flex: 1; }
.koc-reading-row { display: flex; align-items: baseline; gap: 8px; }
.koc-reading-label { font-size: 12px; font-weight: 700; color: var(--color-text-sub); width: 70px; flex-shrink: 0; }
.koc-reading-val { font-size: 15px; color: var(--color-text); }
.koc-flash-btn { height: 36px; padding: 0 16px; border-radius: var(--radius-full); border: 1.5px solid var(--color-primary); background: transparent; color: var(--color-primary); font-size: 13px; font-weight: 700; cursor: pointer; flex-shrink: 0; transition: background var(--transition); }
.koc-flash-btn:hover:not(:disabled) { background: var(--color-primary-bg); }
.koc-flash-btn--added { border-color: var(--color-secondary); color: var(--color-secondary); background: var(--color-secondary-bg); }
.koc-stroke-wrap { display: flex; justify-content: center; }
.koc-stroke-img { max-width: 300px; max-height: 200px; object-fit: contain; border-radius: var(--radius-md); border: 1px solid var(--color-border); }
.koc-example { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; background: var(--color-bg); border-radius: var(--radius-md); padding: 10px 14px; font-size: 14px; }
.koc-ex-label { font-weight: 600; color: var(--color-text-sub); }
.koc-ex-word  { font-size: 18px; font-weight: 700; color: var(--color-text); }
.koc-ex-reading { color: var(--color-text-sub); }
.koc-ex-meaning { color: var(--color-text-sub); }

/* OCR section */
.koc-ocr-section { background: var(--color-card); border-radius: var(--radius-xl); box-shadow: var(--shadow-sm); padding: 24px 28px; display: flex; flex-direction: column; gap: 16px; }
.koc-ocr-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; }
.koc-upload-label {
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px;
  padding: 32px; border: 2px dashed var(--color-border); border-radius: var(--radius-lg);
  cursor: pointer; background: var(--color-bg); transition: border-color var(--transition), background var(--transition);
}
.koc-upload-label:hover { border-color: var(--color-primary); background: var(--color-primary-bg); }
.koc-upload-icon { font-size: 32px; }
.koc-upload-text { font-size: 15px; font-weight: 600; color: var(--color-text); }
.koc-upload-hint { font-size: 13px; color: var(--color-text-sub); }
.koc-preview-wrap { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.koc-preview-img { max-width: 300px; max-height: 300px; object-fit: contain; border-radius: var(--radius-md); border: 2px solid var(--color-border); }
.koc-change-btn { font-size: 13px; color: var(--color-primary); background: transparent; border: none; cursor: pointer; font-weight: 600; }
.koc-analyze-btn { display: inline-flex; align-items: center; gap: 7px; height: 44px; padding: 0 24px; background: var(--color-secondary); color: white; border: none; border-radius: var(--radius-full); font-size: 14px; font-weight: 700; cursor: pointer; align-self: center; transition: filter var(--transition); }
.koc-analyze-btn:hover:not(:disabled) { filter: brightness(1.07); }
.koc-analyze-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.koc-ocr-pending { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 20px; color: var(--color-text-sub); font-size: 14px; }
.koc-spinner-lg { width: 32px; height: 32px; border: 3px solid var(--color-primary-light); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
.koc-ocr-result { display: flex; flex-direction: column; align-items: center; gap: 16px; }

/* Gauge */
.koc-gauge { width: 100%; display: flex; flex-direction: column; gap: 8px; }
.koc-gauge-bar-wrap { width: 100%; }
.koc-gauge-bar { height: 12px; background: var(--color-border); border-radius: var(--radius-full); overflow: hidden; }
.koc-gauge-fill { height: 100%; border-radius: var(--radius-full); transition: width 0.8s ease; }
.koc-gauge--excellent .koc-gauge-fill { background: var(--color-secondary); }
.koc-gauge--good .koc-gauge-fill { background: var(--color-accent); }
.koc-gauge--practice .koc-gauge-fill { background: var(--color-error); }
.koc-gauge-info { display: flex; align-items: center; gap: 10px; }
.koc-gauge-pct  { font-size: 28px; font-weight: 800; color: var(--color-text); }
.koc-gauge-label { font-size: 15px; font-weight: 600; }
.koc-gauge--excellent .koc-gauge-label { color: var(--color-secondary); }
.koc-gauge--good .koc-gauge-label { color: #B7670A; }
.koc-gauge--practice .koc-gauge-label { color: var(--color-error); }
.koc-retry-ocr-btn { height: 38px; padding: 0 20px; border-radius: var(--radius-full); border: 1.5px solid var(--color-border); background: transparent; color: var(--color-text-sub); font-size: 14px; font-weight: 600; cursor: pointer; }
.koc-ocr-error { display: flex; align-items: center; justify-content: space-between; gap: 12px; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.koc-retry-btn { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }

/* Footer */
.koc-footer { display: flex; justify-content: space-between; padding-top: 8px; }
.koc-nav-btn { height: 40px; padding: 0 18px; border-radius: var(--radius-full); border: 1.5px solid var(--color-border); background: var(--color-card); color: var(--color-text-sub); font-size: 14px; font-weight: 600; cursor: pointer; transition: color var(--transition), border-color var(--transition); }
.koc-nav-btn:hover { color: var(--color-primary); border-color: var(--color-primary-light); }

.koc-loading { flex: 1; display: flex; align-items: center; justify-content: center; }
.koc-error { background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.koc-spinner { display: inline-block; width: 15px; height: 15px; border: 2.5px solid rgba(255,255,255,0.35); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; }
.koc-sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0,0,0,0); }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 767px) {
  .koc-body { padding: 16px 16px 32px; }
  .koc-main-row { flex-wrap: wrap; }
  .koc-char { font-size: 60px; }
}

@media (prefers-reduced-motion: reduce) {
  .koc-page * { animation: none !important; transition-duration: 0ms !important; }
  .koc-gauge-fill { transition: none; }
}
```

---

## 8. DOMAIN RULES

- File upload → `multipart/form-data`, KHÔNG base64 encode vào JSON body.
- `ai_score_suggestion` hiển thị raw % — student không override được.
- Polling: 1s interval, max 30 retries (30s timeout) → fallback error message.
- Cleanup `clearInterval(pollRef.current)` khi unmount và khi đổi kanji.
- Upload file validation: max 5MB, chỉ chấp nhận `image/jpeg,image/png,image/webp`.
