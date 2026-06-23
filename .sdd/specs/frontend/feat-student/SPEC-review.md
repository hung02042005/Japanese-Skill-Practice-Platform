# SPEC — Ôn tập Flashcard SRS (`/review`)
>
> **Sprint:** 2 — Core Learning Loop
> **Prefix:** `rev-` | **activeTab:** `'review'` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §10.3` | **Backend ref:** `feat-flashcard-srs/SPEC.md`

---

## 1. MÔ TẢ TRANG

Phiên ôn tập flashcard hàng ngày theo thuật toán SM-2. Hiển thị từng thẻ đến hạn, học viên lật thẻ rồi đánh giá mức nhớ (Dễ / Khó / Không nhớ). Backend tính lịch ôn tập tiếp theo. Khi hết queue → màn hình "Xong hôm nay!".

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="review")                                     │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Ôn tập hôm nay                           3 / 12 thẻ còn lại   │
│  [██████░░░░░░░░░░░░░░░] 25%                                    │
│                                                                  │
│                   ┌──────────────────────────┐                  │
│                   │                          │                  │
│                   │  [Mặt trước — chưa lật]  │                  │
│                   │                          │                  │
│                   │       日本語             │                  │
│                   │                          │                  │
│                   │  [Chữ Kanji 48px]        │                  │
│                   │                          │                  │
│                   │                          │                  │
│                   │   [↕ Lật thẻ]           │                  │
│                   └──────────────────────────┘                  │
│                                                                  │
│  ─── sau khi lật ───────────────────────────────────────────   │
│                                                                  │
│                   ┌──────────────────────────┐                  │
│                   │  [Mặt sau — đã lật]      │                  │
│                   │                          │                  │
│                   │  にほんご               │                  │
│                   │  Nihongo                 │                  │
│                   │  Tiếng Nhật              │                  │
│                   │                          │                  │
│                   │  Ví dụ: 日本語を勉強します │                  │
│                   │  Tôi học tiếng Nhật.     │                  │
│                   │  [▶ Phát âm]             │                  │
│                   └──────────────────────────┘                  │
│                                                                  │
│  [Không nhớ ✗]          [Khó △]          [Dễ ✓]               │
│                                                                  │
│  ─── kết thúc queue ────────────────────────────────────────   │
│                                                                  │
│            [SakuChan celebrate 160px]                           │
│            Tuyệt vời! Hết thẻ hôm nay rồi 🎉                   │
│            Lần ôn tập tiếp theo: Ngày mai, 08:00               │
│            [Về Dashboard]  [Xem tất cả bộ thẻ]                 │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/review/
├── Review.jsx
└── Review.css

components/student/
└── FlashcardCard.jsx   ← card lật được (dùng lại ở /flashcard)
```

---

## 4. STATE

```js
const [queue,        setQueue]    = useState([]);    // flashcard[] — chỉ front data
const [currentIdx,   setIdx]      = useState(0);
const [isFlipped,    setFlipped]  = useState(false);
const [backContent,  setBack]     = useState(null);  // từ API /reveal
const [isLoading,    setLoading]  = useState(true);  // loading queue
const [isFetching,   setFetching] = useState(false); // loading reveal
const [isRating,     setRating]   = useState(false); // submitting rating
const [isDone,       setDone]     = useState(false);
const [nextReview,   setNext]     = useState(null);  // "Ngày mai" hoặc date string
const [error,        setError]    = useState('');

const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

```js
// 1. Load queue
// GET /api/flashcards?dueOnly=true&size=50
// Response: { data: { content: FlashcardItem[], totalElements } }
// FlashcardItem: { flashcardId, contentType, frontText, nextReviewDate, isDue }

// 2. Reveal (khi lật thẻ)
// GET /api/flashcards/:flashcardId/reveal
// Response: { data: { flashcardId, contentType, backContent: { meaning, reading, exampleSentence, audioUrl }, currentInterval, easeFactor } }

// 3. Submit rating
// POST /api/flashcards/:flashcardId/review
// Request: { "rating": "easy"|"hard"|"wrong" }
// Response: { data: { newIntervalDays, nextReviewDate } }
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import FlashcardCard from '../../components/student/FlashcardCard';
import { getFlashcardsDue, revealFlashcard, rateFlashcard } from '../../api/studentService';
import './Review.css';

export default function Review() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [queue,      setQueue]   = useState([]);
  const [currentIdx, setIdx]     = useState(0);
  const [isFlipped,  setFlipped] = useState(false);
  const [backContent,setBack]    = useState(null);
  const [isLoading,  setLoading] = useState(true);
  const [isFetching, setFetch]   = useState(false);
  const [isRating,   setRating]  = useState(false);
  const [isDone,     setDone]    = useState(false);
  const [nextReview, setNext]    = useState(null);
  const [error,      setError]   = useState('');

  // Tổng số đã ôn (để tính progress)
  const [totalStart, setTotal] = useState(0);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await getFlashcardsDue(50);
        const cards = res.content ?? [];
        setQueue(cards);
        setTotal(cards.length);
        if (cards.length === 0) setDone(true);
      } catch {
        setError('Không thể tải bộ thẻ. Thử lại sau.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  async function handleFlip() {
    if (isFlipped) return;
    setFetch(true);
    try {
      const card = queue[currentIdx];
      const back = await revealFlashcard(card.flashcardId);
      setBack(back.backContent);
      setFlipped(true);
    } catch {
      addToast('error', 'Không thể tải nội dung thẻ. Thử lại.');
    } finally {
      setFetch(false);
    }
  }

  async function handleRate(rating) {
    if (isRating) return;
    setRating(true);
    try {
      const card = queue[currentIdx];
      const res  = await rateFlashcard(card.flashcardId, rating);
      // Advance to next card
      const nextIdx = currentIdx + 1;
      if (nextIdx >= queue.length) {
        setDone(true);
        setNext(res.nextReviewDate);
      } else {
        setIdx(nextIdx);
        setFlipped(false);
        setBack(null);
      }
    } catch {
      addToast('error', 'Không thể lưu đánh giá. Thử lại.');
    } finally {
      setRating(false);
    }
  }

  const progress     = totalStart > 0 ? Math.round((currentIdx / totalStart) * 100) : 0;
  const remaining    = queue.length - currentIdx;
  const currentCard  = queue[currentIdx];

  return (
    <div className="rev-page">
      <TopNav activeTab="review" />

      <main className="rev-body">
        {/* Loading */}
        {isLoading && (
          <div className="rev-loading" role="status" aria-label="Đang tải bộ thẻ">
            <div className="rev-spinner-lg" />
            <span>Đang tải bộ thẻ ôn tập...</span>
          </div>
        )}

        {/* Error */}
        {!isLoading && error && (
          <div className="rev-error-banner" role="alert">
            <span>{error}</span>
            <button className="rev-retry-btn" onClick={() => window.location.reload()}>Thử lại</button>
          </div>
        )}

        {/* Empty (no cards at all) */}
        {!isLoading && !error && !isDone && queue.length === 0 && (
          <EmptyState
            title="Chưa có thẻ nào để ôn tập"
            subtitle="Thêm từ vựng hoặc Kanji vào Flashcard từ bài học để bắt đầu ôn tập."
            mascotVariant="idle"
            mascotSize={160}
          >
            <button className="rev-btn rev-btn--primary" onClick={() => navigate('/flashcard')}>Quản lý bộ thẻ</button>
            <button className="rev-btn rev-btn--ghost"   onClick={() => navigate('/learn/new')}>Học bài mới</button>
          </EmptyState>
        )}

        {/* Done */}
        {!isLoading && isDone && totalStart > 0 && (
          <EmptyState
            title="Tuyệt vời! Hết thẻ hôm nay rồi 🎉"
            subtitle={nextReview ? `Lần ôn tập tiếp theo: ${new Date(nextReview).toLocaleDateString('vi-VN')}` : 'Bạn đã ôn tập xong bộ thẻ hôm nay!'}
            mascotVariant="celebrate"
            mascotSize={180}
          >
            <button className="rev-btn rev-btn--primary" onClick={() => navigate('/dashboard')}>Về Dashboard</button>
            <button className="rev-btn rev-btn--ghost"   onClick={() => navigate('/flashcard')}>Xem bộ thẻ</button>
          </EmptyState>
        )}

        {/* Active session */}
        {!isLoading && !isDone && !error && currentCard && (
          <>
            {/* Header */}
            <div className="rev-header">
              <span className="rev-title">Ôn tập hôm nay</span>
              <span className="rev-counter" aria-live="polite">{remaining} thẻ còn lại</span>
            </div>
            <ProgressBar value={progress} />

            {/* Card */}
            <FlashcardCard
              card={currentCard}
              isFlipped={isFlipped}
              backContent={backContent}
              isFetching={isFetching}
              onFlip={handleFlip}
            />

            {/* Rating buttons — chỉ hiện sau khi lật */}
            {isFlipped && (
              <div className="rev-rating-row" aria-label="Đánh giá mức nhớ">
                <button
                  className="rev-rate-btn rev-rate-btn--wrong"
                  onClick={() => handleRate('wrong')}
                  disabled={isRating}
                  aria-label="Không nhớ"
                >
                  <span className="rev-rate-icon" aria-hidden="true">✗</span>
                  Không nhớ
                </button>
                <button
                  className="rev-rate-btn rev-rate-btn--hard"
                  onClick={() => handleRate('hard')}
                  disabled={isRating}
                  aria-label="Khó, nhớ mờ"
                >
                  <span className="rev-rate-icon" aria-hidden="true">△</span>
                  Khó
                </button>
                <button
                  className="rev-rate-btn rev-rate-btn--easy"
                  onClick={() => handleRate('easy')}
                  disabled={isRating}
                  aria-label="Dễ, nhớ rõ"
                >
                  <span className="rev-rate-icon" aria-hidden="true">✓</span>
                  Dễ
                </button>
              </div>
            )}
          </>
        )}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

## 7. FlashcardCard component

```jsx
// components/student/FlashcardCard.jsx
export default function FlashcardCard({ card, isFlipped, backContent, isFetching, onFlip }) {
  return (
    <div className="fcc-scene" aria-live="polite" aria-atomic="true">
      <div className={`fcc-card${isFlipped ? ' fcc-card--flipped' : ''}`}>
        {/* Front */}
        <div className="fcc-face fcc-face--front" aria-hidden={isFlipped}>
          <div className="fcc-front-content">
            <span className="fcc-front-text" lang="ja">{card.frontText}</span>
            {!isFlipped && (
              <button
                className="fcc-flip-btn"
                onClick={onFlip}
                disabled={isFetching}
                aria-label="Lật thẻ để xem đáp án"
              >
                {isFetching
                  ? <span className="fcc-spinner" aria-hidden="true" />
                  : <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M17 1l4 4-4 4M3 11V9a4 4 0 014-4h14M7 23l-4-4 4-4M21 13v2a4 4 0 01-4 4H3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
                }
                {isFetching ? 'Đang tải...' : '↕ Lật thẻ'}
              </button>
            )}
          </div>
        </div>

        {/* Back */}
        {isFlipped && backContent && (
          <div className="fcc-face fcc-face--back" aria-hidden={!isFlipped}>
            <div className="fcc-back-content">
              {backContent.reading && <p className="fcc-reading" lang="ja">{backContent.reading}</p>}
              <p className="fcc-meaning">{backContent.meaning}</p>
              {backContent.exampleSentence && (
                <div className="fcc-example">
                  <p className="fcc-ex-jp" lang="ja">{backContent.exampleSentence}</p>
                </div>
              )}
              {backContent.audioUrl && (
                <button
                  className="fcc-audio-btn"
                  onClick={() => new Audio(backContent.audioUrl).play()}
                  aria-label="Nghe phát âm"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M11 5L6 9H2v6h4l5 4V5z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
                    <path d="M15.54 8.46a5 5 0 010 7.07" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  </svg>
                  Phát âm
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
```

---

## 8. CSS

```css
/* ===== Review (SakuJi Hanami Theme) ===== */

.rev-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  font-family: var(--font-base);
}

.rev-body {
  flex: 1;
  max-width: 680px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 24px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-sizing: border-box;
}

/* Header */
.rev-header { display: flex; align-items: center; justify-content: space-between; }
.rev-title  { font-size: 20px; font-weight: 700; color: var(--color-text); }
.rev-counter{ font-size: 14px; color: var(--color-text-sub); font-weight: 600; }

/* Loading */
.rev-loading {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 16px;
  color: var(--color-text-sub); font-size: 14px;
}
.rev-spinner-lg { width: 48px; height: 48px; border: 4px solid var(--color-primary-light); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.8s linear infinite; }

/* Error */
.rev-error-banner { display: flex; align-items: center; justify-content: space-between; gap: 12px; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.rev-retry-btn { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }

/* --- FlashcardCard CSS --- */
.fcc-scene {
  width: 100%;
  perspective: 1000px;
}
.fcc-card {
  position: relative;
  width: 100%;
  min-height: 280px;
  transform-style: preserve-3d;
  transition: transform 0.5s ease;
}
.fcc-card--flipped { transform: rotateY(180deg); }

.fcc-face {
  position: absolute;
  inset: 0;
  backface-visibility: hidden;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  display: flex; align-items: center; justify-content: center;
}
.fcc-face--front { background: var(--color-card); }
.fcc-face--back  { background: var(--color-card); transform: rotateY(180deg); }

/* Fix: Back content phải dùng position relative, không absolute */
.fcc-face--back {
  position: relative;
  transform: rotateY(180deg);
}

.fcc-front-content {
  display: flex; flex-direction: column; align-items: center; gap: 24px;
  padding: 32px;
}
.fcc-front-text {
  font-size: 52px;
  font-weight: 800;
  color: var(--color-text);
  line-height: 1.2;
}
.fcc-flip-btn {
  display: inline-flex; align-items: center; gap: 8px;
  height: 40px; padding: 0 20px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-primary);
  background: transparent; color: var(--color-primary);
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: background var(--transition);
}
.fcc-flip-btn:hover:not(:disabled) { background: var(--color-primary-bg); }
.fcc-flip-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.fcc-back-content {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 32px; text-align: center; width: 100%;
}
.fcc-reading  { font-size: 20px; color: var(--color-text-sub); margin: 0; }
.fcc-meaning  { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0; }
.fcc-example  { background: var(--color-bg); border-radius: var(--radius-md); padding: 10px 14px; width: 100%; }
.fcc-ex-jp    { font-size: 14px; color: var(--color-text); margin: 0; }
.fcc-audio-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px; border-radius: var(--radius-full);
  border: 1px solid var(--color-border); background: var(--color-bg);
  font-size: 13px; color: var(--color-text-sub); cursor: pointer;
  transition: border-color var(--transition), color var(--transition);
}
.fcc-audio-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.fcc-spinner { display: inline-block; width: 14px; height: 14px; border: 2px solid var(--color-primary-light); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.7s linear infinite; }

/* Rating buttons */
.rev-rating-row {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.rev-rate-btn {
  flex: 1;
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 14px 10px;
  border-radius: var(--radius-lg);
  border: 2px solid transparent;
  font-family: var(--font-base);
  font-size: 14px; font-weight: 700;
  cursor: pointer;
  transition: filter var(--transition), transform var(--transition);
  max-width: 180px;
}
.rev-rate-btn:active:not(:disabled) { transform: scale(0.96); }
.rev-rate-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.rev-rate-icon { font-size: 22px; }

.rev-rate-btn--wrong { background: #FEF2F2; color: var(--color-error); border-color: var(--color-error); }
.rev-rate-btn--wrong:hover:not(:disabled) { filter: brightness(0.96); }
.rev-rate-btn--hard  { background: var(--color-accent-bg); color: #B7670A; border-color: var(--color-accent); }
.rev-rate-btn--hard:hover:not(:disabled)  { filter: brightness(0.97); }
.rev-rate-btn--easy  { background: var(--color-secondary-bg); color: var(--color-secondary); border-color: var(--color-secondary); }
.rev-rate-btn--easy:hover:not(:disabled)  { filter: brightness(1.05); }

/* CTA buttons */
.rev-btn { display: inline-flex; align-items: center; justify-content: center; gap: 7px; height: 42px; padding: 0 22px; border-radius: var(--radius-full); font-family: var(--font-base); font-size: 14px; font-weight: 700; cursor: pointer; border: none; transition: filter var(--transition); text-decoration: none; }
.rev-btn--primary { background: var(--color-secondary); color: white; }
.rev-btn--primary:hover { filter: brightness(1.07); }
.rev-btn--ghost { background: transparent; border: 1.5px solid var(--color-border); color: var(--color-text-sub); }
.rev-btn--ghost:hover { color: var(--color-text); background: var(--color-bg); }

@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 767px) {
  .rev-body { padding: 16px 16px 32px; gap: 16px; }
  .rev-rating-row { gap: 8px; }
  .rev-rate-btn { padding: 12px 8px; font-size: 13px; }
}

@media (prefers-reduced-motion: reduce) {
  .rev-page * { animation: none !important; transition-duration: 0ms !important; }
  .fcc-card { transition: none; }
}
```

---

## 9. 3 TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Loading** | Spinner toàn trang |
| **Error** | Banner đỏ + reload |
| **Empty (chưa có thẻ)** | EmptyState → link /flashcard, /learn/new |
| **Done (hết queue)** | EmptyState celebrate + nextReviewDate |

---

## 10. DOMAIN RULES

- Thuật toán SM-2 hoàn toàn ở backend — frontend chỉ gửi `'easy'|'hard'|'wrong'`.
- Không thể "Undo" sau khi đánh giá — record đã ghi.
- `aria-live="polite"` trên counter để screen reader thông báo khi thay đổi số thẻ còn lại.
- FlashcardCard flip animation tắt khi `prefers-reduced-motion`.
