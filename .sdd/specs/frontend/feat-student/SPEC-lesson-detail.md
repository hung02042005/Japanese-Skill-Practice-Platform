# SPEC — Chi tiết bài học (`/lessons/:id`)
>
> **Sprint:** 2 — Core Learning Loop
> **Prefix:** `lsn-` | **activeTab:** `'learn'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §10.1` | **Backend ref:** `feat-core-learning/SPEC.md`

---

## 1. MÔ TẢ TRANG

Trang hiển thị nội dung một bài học cụ thể. Bài học có thể chứa: Từ vựng, Kanji, Ngữ pháp, Kana — tuỳ `lesson.type`. Sau khi đọc xong, học viên đánh dấu hoàn thành. Bài học khoá VIP sẽ redirect `/subscription` nếu user FREE.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="learn")                                      │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Dashboard > Khoá học N5 > Chào hỏi cơ bản      (breadcrumb)  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                                                          │   │
│  │  [N5]  Chào hỏi cơ bản                                  │   │
│  │  Bài 3 · Từ vựng · 15 phút đọc                          │   │
│  │                                                          │   │
│  │  Tiến độ: [████████░░░░░░░░] 55%                        │   │
│  │                                                          │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  [Nội dung] [Từ vựng (12)] [Ngữ pháp (3)] [Bài tập]           │
│  ─────────────────────────────────────────────────────────────  │
│                                                                  │
│  [Tab: Nội dung]                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  [Lesson content rich text area]                         │   │
│  │  Âm thanh: [▶ Phát âm] [Nghe ví dụ]                     │   │
│  │  Hình minh hoạ: [img nếu có]                             │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ──────────────────────────────────────────────────────────────  │
│  [← Bài trước]   [✓ Đánh dấu hoàn thành]   [Bài tiếp theo →]   │
└──────────────────────────────────────────────────────────────────┘

Tab "Từ vựng":
  ┌────────────────────────────────────────────────────────────┐
  │  VocabCard: おはよう   [▶]  おはようございます              │
  │  Good morning · Chào buổi sáng · N5 · [+ Flashcard]       │
  │  Ví dụ: おはようございます。→ Chào buổi sáng.              │
  └────────────────────────────────────────────────────────────┘
  (danh sách VocabCard)

Tab "Ngữ pháp":
  GrammarPoint: ~は~です / ~wa ~desu
  Ý nghĩa: "[Chủ thể] là [vị ngữ]"
  Ví dụ: わたしはがくせいです。 → Tôi là học sinh.
```

---

## 3. FILE CẦN TẠO

```
pages/lessons/
├── LessonDetail.jsx
└── LessonDetail.css

components/student/
├── LessonVocabCard.jsx   ← card từ vựng có audio + add-to-flashcard
└── LessonGrammarPoint.jsx ← điểm ngữ pháp expanded
```

---

## 4. STATE

```js
const { id } = useParams();

const [lesson,       setLesson]   = useState(null);
const [activeTab,    setActiveTab]= useState('content');  // 'content'|'vocab'|'grammar'|'practice'
const [isLoading,    setLoading]  = useState(true);
const [error,        setError]    = useState('');
const [isCompleting, setComplete] = useState(false);
const [isCompleted,  setCompleted]= useState(false);  // optimistic UI

const { user } = useAppSelector((s) => s.auth);
const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

```js
// GET /api/lessons/:id
// Response 200:
{
  "data": {
    "lessonId": "long",
    "title": "string",
    "description": "string",
    "lessonType": "vocabulary|kanji|grammar|kana|mixed",
    "jlptLevel": "N5",
    "estimatedMinutes": 15,
    "contentHtml": "string",    // rich text HTML (sanitized server-side)
    "audioUrl": "string|null",
    "imageUrl": "string|null",
    "isVipOnly": false,
    "isLocked": false,          // true = bài trước chưa hoàn thành
    "progressStatus": "learning|completed|null",
    "progressPercent": 55,
    "vocabulary": [...],        // array nếu có vocab trong bài
    "grammarPoints": [...],     // array nếu có grammar
    "prevLessonId": "long|null",
    "nextLessonId": "long|null"
  }
}

// Response 403 VIP_REQUIRED → redirect /subscription
// Response 404 → redirect /404

// POST /api/learning-progress
// Request: { contentType: 'lesson', contentId: id, status: 'completed', progressPercent: 100 }
// Response 200: { data: { progressId, status, progressPercent } }

// POST /api/flashcards  (từ LessonVocabCard)
// Request: { contentType: 'vocabulary', contentId: vocabId }
// Response 201 | 409 (đã có)
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import LessonVocabCard from '../../components/student/LessonVocabCard';
import LessonGrammarPoint from '../../components/student/LessonGrammarPoint';
import { getLessonDetail, markProgress, addToFlashcard } from '../../api/studentService';
import './LessonDetail.css';

const TABS = [
  { id: 'content', label: 'Nội dung' },
  { id: 'vocab',   label: 'Từ vựng' },
  { id: 'grammar', label: 'Ngữ pháp' },
];

export default function LessonDetail() {
  const { id }   = useParams();
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);
  const { toasts, addToast, removeToast } = useToast();

  const [lesson,       setLesson]   = useState(null);
  const [activeTab,    setTab]      = useState('content');
  const [isLoading,    setLoading]  = useState(true);
  const [error,        setError]    = useState('');
  const [isCompleting, setComplete] = useState(false);
  const [isCompleted,  setCompleted]= useState(false);

  const fetchLesson = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const data = await getLessonDetail(id);
      // VIP check
      if (data.isVipOnly && !user?.isVip) { navigate('/subscription'); return; }
      // Locked check
      if (data.isLocked) { navigate('/dashboard'); return; }
      setLesson(data);
      setCompleted(data.progressStatus === 'completed');
    } catch (err) {
      if (err?.response?.status === 404) { navigate('/404'); return; }
      setError(err?.response?.data?.message ?? 'Không thể tải bài học.');
    } finally {
      setLoading(false);
    }
  }, [id, user, navigate]);

  useEffect(() => { fetchLesson(); }, [fetchLesson]);

  async function handleMarkComplete() {
    if (isCompleted) return;
    setComplete(true);
    try {
      await markProgress('lesson', id, 'completed');
      setCompleted(true);
      addToast('success', 'Đã đánh dấu hoàn thành! 🌸');
    } catch {
      addToast('error', 'Không thể lưu tiến độ. Thử lại sau.');
    } finally {
      setComplete(false);
    }
  }

  async function handleAddFlashcard(vocabId) {
    try {
      await addToFlashcard('vocabulary', vocabId);
      addToast('success', 'Đã thêm vào Flashcard!');
    } catch (err) {
      if (err?.response?.status === 409) { addToast('info', 'Từ này đã có trong Flashcard.'); return; }
      addToast('error', 'Không thể thêm. Thử lại sau.');
    }
  }

  const visibleTabs = TABS.filter((t) => {
    if (t.id === 'vocab'   && !lesson?.vocabulary?.length)   return false;
    if (t.id === 'grammar' && !lesson?.grammarPoints?.length) return false;
    return true;
  });

  return (
    <div className="lsn-page">
      <TopNav activeTab="learn" />

      <main className="lsn-body">
        {/* Breadcrumb */}
        {!isLoading && lesson && (
          <nav className="lsn-breadcrumb" aria-label="Điều hướng">
            <Link to="/dashboard">Dashboard</Link>
            <span aria-hidden="true"> › </span>
            <span>{lesson.jlptLevel} — {lesson.title}</span>
          </nav>
        )}

        {/* Error */}
        {error && (
          <div className="lsn-error-banner" role="alert">
            <span>{error}</span>
            <button className="lsn-retry-btn" onClick={fetchLesson}>Thử lại</button>
          </div>
        )}

        {/* Loading skeleton */}
        {isLoading && (
          <>
            <div className="lsn-skel lsn-skel--header" aria-hidden="true" />
            <div className="lsn-skel lsn-skel--content" aria-hidden="true" />
          </>
        )}

        {/* Content */}
        {!isLoading && !error && lesson && (
          <>
            {/* Header card */}
            <div className="lsn-header-card">
              <div className="lsn-header-meta">
                <JlptBadge level={lesson.jlptLevel} />
                <h1 className="lsn-title">{lesson.title}</h1>
                <p className="lsn-meta-row">
                  <span className="lsn-meta-chip">{lesson.lessonType}</span>
                  <span className="lsn-meta-dot" aria-hidden="true">·</span>
                  <span className="lsn-meta-time">{lesson.estimatedMinutes} phút</span>
                </p>
              </div>
              <div className="lsn-progress-row">
                <span className="lsn-progress-label">Tiến độ</span>
                <ProgressBar value={isCompleted ? 100 : lesson.progressPercent} />
                <span className="lsn-progress-pct">{isCompleted ? 100 : lesson.progressPercent}%</span>
              </div>
            </div>

            {/* Tabs */}
            <div className="lsn-tabs" role="tablist" aria-label="Nội dung bài học">
              {visibleTabs.map((t) => (
                <button
                  key={t.id}
                  role="tab"
                  aria-selected={activeTab === t.id}
                  className={`lsn-tab${activeTab === t.id ? ' lsn-tab--active' : ''}`}
                  onClick={() => setTab(t.id)}
                >
                  {t.label}
                  {t.id === 'vocab'   && <span className="lsn-tab-count"> ({lesson.vocabulary?.length})</span>}
                  {t.id === 'grammar' && <span className="lsn-tab-count"> ({lesson.grammarPoints?.length})</span>}
                </button>
              ))}
            </div>

            {/* Tab panels */}
            <div className="lsn-tab-panel" role="tabpanel">
              {activeTab === 'content' && (
                <div className="lsn-content-area">
                  {lesson.audioUrl && (
                    <div className="lsn-audio-bar">
                      <span className="lsn-audio-label">Nghe phát âm:</span>
                      <audio controls src={lesson.audioUrl} aria-label="Phát âm bài học" />
                    </div>
                  )}
                  {lesson.imageUrl && (
                    <img className="lsn-content-img" src={lesson.imageUrl} alt={`Minh hoạ bài: ${lesson.title}`} />
                  )}
                  <div
                    className="lsn-content-html"
                    dangerouslySetInnerHTML={{ __html: lesson.contentHtml }}
                  />
                </div>
              )}

              {activeTab === 'vocab' && (
                <div className="lsn-vocab-list">
                  {lesson.vocabulary.map((v) => (
                    <LessonVocabCard
                      key={v.vocabId}
                      vocab={v}
                      onAddFlashcard={() => handleAddFlashcard(v.vocabId)}
                    />
                  ))}
                </div>
              )}

              {activeTab === 'grammar' && (
                <div className="lsn-grammar-list">
                  {lesson.grammarPoints.map((g) => (
                    <LessonGrammarPoint key={g.grammarId} grammar={g} />
                  ))}
                </div>
              )}
            </div>

            {/* Footer navigation */}
            <div className="lsn-footer">
              {lesson.prevLessonId
                ? <Link to={`/lessons/${lesson.prevLessonId}`} className="lsn-nav-btn lsn-nav-btn--prev">← Bài trước</Link>
                : <div />
              }

              <button
                className={`lsn-complete-btn${isCompleted ? ' lsn-complete-btn--done' : ''}`}
                onClick={handleMarkComplete}
                disabled={isCompleting || isCompleted}
                aria-pressed={isCompleted}
              >
                {isCompleting && <span className="lsn-spinner" aria-hidden="true" />}
                {isCompleted ? '✓ Đã hoàn thành' : isCompleting ? 'Đang lưu…' : '✓ Đánh dấu hoàn thành'}
              </button>

              {lesson.nextLessonId
                ? <Link to={`/lessons/${lesson.nextLessonId}`} className="lsn-nav-btn lsn-nav-btn--next">Bài tiếp theo →</Link>
                : <div />
              }
            </div>
          </>
        )}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Lesson Detail (SakuJi Hanami Theme) ===== */

.lsn-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  font-family: var(--font-base);
}

.lsn-body {
  flex: 1;
  max-width: 860px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 32px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-sizing: border-box;
}

/* Breadcrumb */
.lsn-breadcrumb {
  font-size: 13px;
  color: var(--color-text-sub);
  display: flex;
  align-items: center;
  gap: 4px;
}
.lsn-breadcrumb a { color: var(--color-primary); text-decoration: none; font-weight: 600; }
.lsn-breadcrumb a:hover { text-decoration: underline; }

/* Header card */
.lsn-header-card {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.lsn-header-meta { display: flex; flex-direction: column; gap: 8px; }
.lsn-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin: 0; }
.lsn-meta-row { display: flex; align-items: center; gap: 6px; margin: 0; font-size: 13px; color: var(--color-text-sub); }
.lsn-meta-chip {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: 2px 10px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.lsn-meta-dot { color: var(--color-border); }

.lsn-progress-row { display: flex; align-items: center; gap: 10px; }
.lsn-progress-label { font-size: 12px; font-weight: 600; color: var(--color-text-sub); white-space: nowrap; }
.lsn-progress-pct { font-size: 12px; font-weight: 700; color: var(--color-text-sub); white-space: nowrap; }

/* Tabs */
.lsn-tabs {
  display: flex;
  gap: 0;
  border-bottom: 2px solid var(--color-border);
}
.lsn-tab {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-sub);
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  cursor: pointer;
  transition: color var(--transition), border-color var(--transition);
}
.lsn-tab:hover { color: var(--color-text); }
.lsn-tab--active { color: var(--color-primary); border-bottom-color: var(--color-primary); }
.lsn-tab-count { font-weight: 400; font-size: 12px; color: var(--color-text-disabled); }

/* Tab panel */
.lsn-tab-panel {
  background: var(--color-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 24px 28px;
  min-height: 200px;
}

/* Content area */
.lsn-audio-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 16px;
}
.lsn-audio-label { font-size: 13px; font-weight: 600; color: var(--color-text-sub); }
.lsn-content-img {
  width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}
.lsn-content-html {
  font-size: 15px;
  line-height: 1.7;
  color: var(--color-text);
}
.lsn-content-html h2 { font-size: 18px; font-weight: 700; margin: 20px 0 8px; }
.lsn-content-html p  { margin: 0 0 12px; }

/* Vocab list */
.lsn-vocab-list { display: flex; flex-direction: column; gap: 12px; }
/* Grammar list */
.lsn-grammar-list { display: flex; flex-direction: column; gap: 16px; }

/* Footer navigation */
.lsn-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid var(--color-border);
}

.lsn-nav-btn {
  display: inline-flex;
  align-items: center;
  height: 40px;
  padding: 0 18px;
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-sub);
  text-decoration: none;
  border: 1.5px solid var(--color-border);
  background: var(--color-card);
  transition: color var(--transition), border-color var(--transition);
}
.lsn-nav-btn:hover { color: var(--color-primary); border-color: var(--color-primary-light); }

.lsn-complete-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 44px;
  padding: 0 28px;
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  border: none;
  background: var(--color-secondary);
  color: white;
  box-shadow: 0 2px 8px rgba(93,187,105,0.25);
  transition: filter var(--transition), transform var(--transition), background var(--transition);
}
.lsn-complete-btn:hover:not(:disabled) { filter: brightness(1.07); }
.lsn-complete-btn:active:not(:disabled) { transform: scale(0.97); }
.lsn-complete-btn:disabled { cursor: not-allowed; }
.lsn-complete-btn--done {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  box-shadow: none;
  border: 1.5px solid var(--color-primary-light);
}

/* Error banner */
.lsn-error-banner {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md);
  padding: 12px 16px; font-size: 13px; color: var(--color-error);
}
.lsn-retry-btn {
  background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full);
  color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer;
}

/* Skeletons */
.lsn-skel { border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
.lsn-skel--header  { height: 120px; }
.lsn-skel--content { height: 300px; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* Spinner */
.lsn-spinner { display: inline-block; width: 16px; height: 16px; border: 2.5px solid rgba(255,255,255,0.35); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 767px) {
  .lsn-body { padding: 16px 16px 32px; }
  .lsn-footer { flex-wrap: wrap; justify-content: center; }
  .lsn-nav-btn { flex: 1; justify-content: center; }
  .lsn-complete-btn { width: 100%; justify-content: center; order: -1; }
}

@media (prefers-reduced-motion: reduce) {
  .lsn-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

---

## 8. LessonVocabCard component

```jsx
// components/student/LessonVocabCard.jsx
export default function LessonVocabCard({ vocab, onAddFlashcard }) {
  const [audioPlaying, setPlaying] = useState(false);

  function playAudio() {
    if (!vocab.audioUrl) return;
    const a = new Audio(vocab.audioUrl);
    a.play();
    setPlaying(true);
    a.onended = () => setPlaying(false);
  }

  return (
    <div className="lvc-card">
      <div className="lvc-main">
        <span className="lvc-word" lang="ja">{vocab.word}</span>
        {vocab.furigana && <span className="lvc-furigana" lang="ja">{vocab.furigana}</span>}
        <span className="lvc-meaning">{vocab.meaning}</span>
        {vocab.audioUrl && (
          <button
            className={`lvc-audio-btn${audioPlaying ? ' lvc-audio-btn--playing' : ''}`}
            onClick={playAudio}
            aria-label={`Nghe phát âm: ${vocab.word}`}
          >
            {/* Speaker SVG icon */}
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M11 5L6 9H2v6h4l5 4V5z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
              {audioPlaying
                ? <path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                : <path d="M15.54 8.46a5 5 0 0 1 0 7.07" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              }
            </svg>
          </button>
        )}
      </div>
      {vocab.exampleSentenceJp && (
        <div className="lvc-example">
          <span className="lvc-ex-jp" lang="ja">{vocab.exampleSentenceJp}</span>
          <span className="lvc-ex-vi">{vocab.exampleSentenceVi}</span>
        </div>
      )}
      <button className="lvc-add-btn" onClick={onAddFlashcard} aria-label={`Thêm "${vocab.word}" vào Flashcard`}>
        + Flashcard
      </button>
    </div>
  );
}
```

---

## 9. 3 TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Loading** | 2 skeleton blocks (header + content) |
| **Error** | Banner đỏ + Retry button |
| **VIP Required** | Redirect `/subscription` ngay khi fetch |
| **Locked** | Redirect `/dashboard` ngay khi fetch |

---

## 10. DOMAIN RULES

- `progressPercent` chỉ tăng — không gửi giảm. Frontend kiểm tra: `if (newPercent < current) skip`.
- `is_vip_only = true` + user FREE → redirect `/subscription` trước khi render nội dung.
- `isLocked = true` → redirect `/dashboard` (bài trước chưa xong).
- `dangerouslySetInnerHTML` chỉ dùng với content đã sanitize server-side — không sanitize thêm client-side.
- Flashcard 409 → toast info, không toast error.

---

## 11. ACCESSIBILITY

- [ ] Breadcrumb có `role="navigation"` và `aria-label`
- [ ] Tabs: `role="tablist"`, mỗi tab có `role="tab"` và `aria-selected`
- [ ] Tab panel có `role="tabpanel"`
- [ ] Audio player dùng native `<audio controls>` — accessible mặc định
- [ ] Từ tiếng Nhật bọc trong `<span lang="ja">`
- [ ] Complete button có `aria-pressed`
