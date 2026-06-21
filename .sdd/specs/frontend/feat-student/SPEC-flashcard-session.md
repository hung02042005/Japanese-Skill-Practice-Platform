# SPEC — Phiên học Flashcard Quizlet (NEW + REVIEW)
> **Sprint:** 2 — Core Learning Loop
> **Prefix:** `fss-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §10` · `DESIGN.md` (Hanami Theme) | **Backend ref:** `feat-flashcard-srs/SPEC.md` (§3.5/§3.6, `ALGO-session-ordering.md`, `SPEC-review-deck.md`)
> **Liên quan:** `SPEC-vocabulary.md` (entry theo topic) · `SPEC-flashcard.md` (entry theo deck) · `SPEC-notebook.md` (đích từ sai) · `SPEC-review.md` (bản lật due-only đơn giản)

---

## 1. MÔ TẢ TRANG

Phiên học **kiểu Quizlet** trên một phạm vi (theo **`deckId`** hoặc **`level + topic`**). Backend dựng sẵn **hàng đợi trộn** thẻ MỚI và thẻ ÔN TẬP theo nhịp §3.6 + điểm ưu tiên (`ALGO-session-ordering.md`); frontend chỉ **trình bày theo thứ tự queue trả về**, không tự sắp xếp.

Hai dạng thẻ trong cùng một phiên:
- **NEW (lật thẻ)** — học nghĩa: mặt trước là từ, lật ra xem nghĩa/ví dụ/audio, rồi tự đánh giá **Không nhớ / Khó / Dễ**.
- **REVIEW (trắc nghiệm)** — củng cố: hiện từ + 2–4 lựa chọn nghĩa; **server chấm đúng/sai** (FR-FC-55/56), không lộ đáp án.

Cuối phiên (**FR-FC-81**): nếu có từ VOCABULARY trả lời **sai**, hiện hộp gợi ý **"Thêm N từ vào Sổ tay?"** → lưu vào "Từ cần ôn lại".

Theo `DESIGN.md`: nền washi, thẻ trắng bo `--radius-xl`, Saku-chan phản ứng theo trạng thái (`correct` khi đúng, `wrong` khi sai, `celebrate` cuối phiên), pill button, progress bar. Pink chỉ accent; green `--color-secondary` cho CTA chính.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  ← Thoát        Động từ N5            [████████░░░░░] 8/15        │
├──────────────────────────────────────────────────────────────────┤
│  ─── thẻ MỚI (lật) ──────────────────────────────────────────────  │
│                   ┌──────────────────────────┐                    │
│                   │  [Mặt trước]   食べる     │                    │
│                   │              ・たべる     │                    │
│                   │         [↕ Lật thẻ]      │                    │
│                   └──────────────────────────┘                    │
│  sau khi lật:  にほんご / Ăn / 朝ごはんを食べる。 [▶]              │
│  [Không nhớ ✗]        [Khó △]        [Dễ ✓]                       │
│                                                                    │
│  ─── thẻ ÔN (trắc nghiệm) ───────────────────────────────────────  │
│                   食べる                                          │
│   Nghĩa của từ này là gì?                                         │
│   ┌────────────┐ ┌────────────┐                                  │
│   │  Uống      │ │  Ăn        │   ← chọn → server chấm            │
│   └────────────┘ └────────────┘                                  │
│   ┌────────────┐ ┌────────────┐                                  │
│   │  Đi        │ │  Ngủ       │                                  │
│   └────────────┘ └────────────┘                                  │
│   (đúng → viền xanh + SakuChan correct; sai → viền đỏ + đáp án)   │
│                                                                    │
│  ─── kết thúc ──────────────────────────────────────────────────  │
│            [SakuChan celebrate 180px]                             │
│            Hoàn thành phiên! 12 đúng · 3 sai                      │
│   ┌──────────────────────────────────────────────────────────┐   │
│   │ 📓 Có 3 từ trả lời sai. Thêm vào Sổ tay để ôn lại?       │   │
│   │            [Bỏ qua]        [Thêm vào Sổ tay]             │   │
│   └──────────────────────────────────────────────────────────┘   │
│            [Về Dashboard]   [Học tiếp]                            │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO / SỬA

```
pages/vocabulary/
├── VocabFlashcardSession.jsx   ← đã có; spec chuẩn hoá (mixed queue + FR-FC-81)
└── VocabFlashcardSession.css

components/student/
├── FlashcardCard.jsx     ← tái dùng (thẻ NEW lật) từ SPEC-review.md
└── QuizCard.jsx          ← MỚI: thẻ REVIEW trắc nghiệm
```

> Component này phục vụ **2 entry**: `?level=&topic=` (từ `/vocabulary`) và `?deckId=` (từ `/flashcard`, `/notebook`).

---

## 4. STATE

```js
const [queue,     setQueue]   = useState([]);     // queue từ API (đã xếp thứ tự)
const [idx,       setIdx]      = useState(0);
const [isFlipped, setFlipped]  = useState(false);  // thẻ NEW
const [picked,    setPicked]   = useState(null);   // optionId đã chọn (thẻ REVIEW)
const [verdict,   setVerdict]  = useState(null);   // {correct, correctOptionId} sau khi chấm
const [isLoading, setLoading]  = useState(true);
const [isBusy,    setBusy]     = useState(false);   // submit review
const [isDone,    setDone]     = useState(false);
const [wrongItems,setWrong]    = useState([]);      // [{contentType,contentId,frontText}] để FR-FC-81
const [stats,     setStats]    = useState({ correct: 0, wrong: 0 });
const [showSavePrompt, setSavePrompt] = useState(false);
const [error,     setError]    = useState('');

const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

```js
// 1. Mở phiên (FR-FC-50) — theo deckId HOẶC level+topic
// POST /api/flashcards/session?deckId={id}              (POST vì build phiên có side-effect)
// POST /api/flashcards/session?level=N5&topic=...&newLimit=10
// Response.data: { deckId, newCount, reviewCount, queue: SessionCard[] }
// SessionCard: { flashcardId, stage:'NEW'|'REVIEW',
//                front:{word,furigana},
//                learn?:{meaning,exampleJp,exampleVi,audioUrl},   // chỉ NEW
//                quiz?:{options:[{optionId,meaning}]} }            // chỉ REVIEW (KHÔNG có đáp án đúng)
export async function getVocabFlashcardSession({ level, topic, newLimit, deckId } = {}) { /* đã có, thêm deckId */ }

// 2. Nộp đánh giá / đáp án (POST /api/flashcards/{id}/review)
//   - NEW: { rating:'easy'|'hard'|'wrong', isLastCardInSession }
//   - REVIEW: { selectedOptionId, isLastCardInSession }   ← server chấm, trả verdict
// Response.data: { correct?, correctOptionId?, newIntervalDays, nextReviewDate, newEaseFactor }
export async function submitFlashcardReview(flashcardId, body) { /* đã có */ }

// 3. FR-FC-81 — cuối phiên, thêm từ sai vào Sổ tay
// POST /api/flashcards/review-deck/add  { items:[{contentType:'VOCABULARY', contentId}] }
export async function addWrongWordsToReviewDeck(items) { /* đã có */ }
```

> Thẻ NEW lấy mặt sau trực tiếp từ `card.learn` (không cần `/reveal` riêng vì session đã trả). `isLastCardInSession=true` ở lượt cuối để backend kích hoạt logic FR-FC-81.

---

## 6. JSX STRUCTURE (rút gọn — luồng chính)

```jsx
import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { ProgressBar } from '../../components/common/ProgressBar';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import FlashcardCard from '../../components/student/FlashcardCard';
import QuizCard from '../../components/student/QuizCard';
import { getVocabFlashcardSession, submitFlashcardReview, addWrongWordsToReviewDeck } from '../../api/studentService';
import './VocabFlashcardSession.css';

export default function VocabFlashcardSession() {
  const [sp] = useSearchParams();
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [queue, setQueue]   = useState([]);
  const [idx, setIdx]       = useState(0);
  const [isFlipped, setFlip]= useState(false);
  const [picked, setPicked] = useState(null);
  const [verdict, setVerdict]= useState(null);
  const [isLoading, setLoad]= useState(true);
  const [isBusy, setBusy]   = useState(false);
  const [isDone, setDone]   = useState(false);
  const [wrong, setWrong]   = useState([]);
  const [stats, setStats]   = useState({ correct: 0, wrong: 0 });
  const [showSave, setShowSave] = useState(false);
  const [error, setError]   = useState('');

  useEffect(() => {
    (async () => {
      setLoad(true);
      try {
        const params = sp.get('deckId')
          ? { deckId: Number(sp.get('deckId')) }
          : { level: sp.get('level'), topic: sp.get('topic'), newLimit: Number(sp.get('newLimit')) || undefined };
        const data = await getVocabFlashcardSession(params);
        setQueue(data.queue ?? []);
        if ((data.queue ?? []).length === 0) finish();
      } catch { setError('Không thể tải phiên học. Thử lại sau.'); }
      finally { setLoad(false); }
    })();
  }, []);

  const card = queue[idx];
  const isLast = idx >= queue.length - 1;

  function advance() {
    if (isLast) { finish(); return; }
    setIdx((i) => i + 1); setFlip(false); setPicked(null); setVerdict(null);
  }

  function finish() {
    setDone(true);
    if (wrong.length > 0) setShowSave(true);   // FR-FC-81
  }

  // Thẻ NEW: tự đánh giá
  async function handleRate(rating) {
    if (isBusy) return; setBusy(true);
    try {
      await submitFlashcardReview(card.flashcardId, { rating, isLastCardInSession: isLast });
      if (rating === 'wrong') collectWrong(card);
      setStats((s) => ({ ...s, [rating === 'wrong' ? 'wrong' : 'correct']: s[rating === 'wrong' ? 'wrong' : 'correct'] + 1 }));
      advance();
    } catch { addToast('error', 'Không lưu được. Thử lại.'); }
    finally { setBusy(false); }
  }

  // Thẻ REVIEW: chọn đáp án → server chấm
  async function handlePick(optionId) {
    if (isBusy || verdict) return; setBusy(true); setPicked(optionId);
    try {
      const res = await submitFlashcardReview(card.flashcardId, { selectedOptionId: optionId, isLastCardInSession: isLast });
      setVerdict({ correct: res.correct, correctOptionId: res.correctOptionId });
      if (res.correct) setStats((s) => ({ ...s, correct: s.correct + 1 }));
      else { setStats((s) => ({ ...s, wrong: s.wrong + 1 })); collectWrong(card); }
      // tự chuyển sau 1.1s để người học thấy phản hồi
      setTimeout(advance, 1100);
    } catch { addToast('error', 'Không chấm được. Thử lại.'); setPicked(null); }
    finally { setBusy(false); }
  }

  function collectWrong(c) {
    setWrong((w) => [...w, { contentType: 'VOCABULARY', contentId: c.contentId ?? c.flashcardId, frontText: c.front.word }]);
  }

  async function handleSaveWrong() {
    try {
      await addWrongWordsToReviewDeck(wrong.map((w) => ({ contentType: w.contentType, contentId: w.contentId })));
      addToast('success', `Đã thêm ${wrong.length} từ vào Sổ tay.`);
      setShowSave(false);
    } catch { addToast('error', 'Không thể thêm vào Sổ tay. Thử lại.'); }
  }

  const progress = queue.length ? Math.round((idx / queue.length) * 100) : 0;

  return (
    <div className="fss-page">
      {/* Top bar */}
      <header className="fss-top">
        <button className="fss-exit" onClick={() => navigate(-1)} aria-label="Thoát phiên học">← Thoát</button>
        <span className="fss-scope">{sp.get('topic') || 'Ôn tập'}</span>
        <span className="fss-counter" aria-live="polite">{Math.min(idx + 1, queue.length)}/{queue.length}</span>
      </header>
      <ProgressBar value={progress} />

      <main className="fss-body">
        {isLoading && <div className="fss-loading" role="status"><div className="fss-spinner" /></div>}
        {!isLoading && error && <div className="fss-error" role="alert">{error}</div>}

        {/* Đang học */}
        {!isLoading && !isDone && card && (
          card.stage === 'NEW' ? (
            <>
              <FlashcardCard card={{ frontText: card.front.word }} isFlipped={isFlipped}
                backContent={card.learn} isFetching={false} onFlip={() => setFlip(true)} />
              {isFlipped && (
                <div className="fss-rating">
                  <button className="fss-rate fss-rate--wrong" disabled={isBusy} onClick={() => handleRate('wrong')}>✗ Không nhớ</button>
                  <button className="fss-rate fss-rate--hard"  disabled={isBusy} onClick={() => handleRate('hard')}>△ Khó</button>
                  <button className="fss-rate fss-rate--easy"  disabled={isBusy} onClick={() => handleRate('easy')}>✓ Dễ</button>
                </div>
              )}
            </>
          ) : (
            <QuizCard card={card} picked={picked} verdict={verdict} disabled={isBusy} onPick={handlePick} />
          )
        )}

        {/* Kết thúc */}
        {!isLoading && isDone && (
          <EmptyState title="Hoàn thành phiên! 🎉"
            subtitle={`${stats.correct} đúng · ${stats.wrong} sai`} mascotVariant="celebrate" mascotSize={180}>
            {showSave && (
              <div className="fss-save-prompt" role="dialog" aria-label="Thêm từ sai vào Sổ tay">
                <p>📓 Có <strong>{wrong.length}</strong> từ trả lời sai. Thêm vào Sổ tay để ôn lại?</p>
                <div className="fss-save-actions">
                  <button className="fss-btn fss-btn--ghost" onClick={() => setShowSave(false)}>Bỏ qua</button>
                  <button className="fss-btn fss-btn--primary" onClick={handleSaveWrong}>Thêm vào Sổ tay</button>
                </div>
              </div>
            )}
            <div className="fss-end-actions">
              <button className="fss-btn fss-btn--ghost" onClick={() => navigate('/dashboard')}>Về Dashboard</button>
              <button className="fss-btn fss-btn--primary" onClick={() => window.location.reload()}>Học tiếp</button>
            </div>
          </EmptyState>
        )}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

## 7. QuizCard component

```jsx
// components/student/QuizCard.jsx
export default function QuizCard({ card, picked, verdict, disabled, onPick }) {
  return (
    <div className="qc-wrap">
      <div className="qc-prompt">
        <span className="qc-word" lang="ja">{card.front.word}</span>
        {card.front.furigana && <span className="qc-furi" lang="ja">・{card.front.furigana}</span>}
        <p className="qc-ask">Nghĩa của từ này là gì?</p>
      </div>
      <div className="qc-options" role="listbox" aria-label="Chọn nghĩa đúng">
        {card.quiz.options.map((o) => {
          let cls = 'qc-opt';
          if (verdict) {
            if (o.optionId === verdict.correctOptionId) cls += ' qc-opt--correct';
            else if (o.optionId === picked) cls += ' qc-opt--wrong';
          } else if (o.optionId === picked) cls += ' qc-opt--picked';
          return (
            <button key={o.optionId} className={cls} disabled={disabled || !!verdict}
              onClick={() => onPick(o.optionId)} role="option" aria-selected={o.optionId === picked}>
              {o.meaning}
            </button>
          );
        })}
      </div>
    </div>
  );
}
```

---

## 8. CSS (rút gọn — điểm chính)

```css
/* ===== Flashcard Session (SakuJi Hanami Theme) ===== */
.fss-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.fss-top { display: flex; align-items: center; justify-content: space-between; height: 56px; padding: 0 20px; background: var(--color-card); border-bottom: 1px solid var(--color-border); }
.fss-exit { background: transparent; border: none; color: var(--color-text-sub); font-size: 14px; font-weight: 600; cursor: pointer; }
.fss-scope { font-size: 15px; font-weight: 700; color: var(--color-text); }
.fss-counter { font-size: 14px; font-weight: 700; color: var(--color-text-sub); }
.fss-body { flex: 1; max-width: 640px; width: 100%; margin: 0 auto; padding: 28px 24px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

/* Rating (thẻ NEW) — tái dùng cảm giác rev-rate ở SPEC-review */
.fss-rating { display: flex; gap: 12px; justify-content: center; }
.fss-rate { flex: 1; max-width: 180px; padding: 14px 10px; border-radius: var(--radius-lg); border: 2px solid transparent; font-family: var(--font-base); font-size: 14px; font-weight: 700; cursor: pointer; transition: filter var(--transition), transform var(--transition); }
.fss-rate:active:not(:disabled) { transform: scale(0.96); }
.fss-rate:disabled { opacity: 0.5; cursor: not-allowed; }
.fss-rate--wrong { background: #FEF2F2; color: var(--color-error); border-color: var(--color-error); }
.fss-rate--hard  { background: var(--color-accent-bg); color: #B7670A; border-color: var(--color-accent); }
.fss-rate--easy  { background: var(--color-secondary-bg); color: var(--color-secondary); border-color: var(--color-secondary); }

/* Quiz (thẻ REVIEW) */
.qc-wrap { display: flex; flex-direction: column; gap: 20px; }
.qc-prompt { text-align: center; }
.qc-word { font-size: 40px; font-weight: 800; color: var(--color-text); }
.qc-furi { font-size: 16px; color: var(--color-text-sub); }
.qc-ask  { font-size: 15px; color: var(--color-text-sub); margin: 8px 0 0; }
.qc-options { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.qc-opt { min-height: 56px; padding: 14px 16px; border-radius: var(--radius-md); border: 2px solid var(--color-border); background: var(--color-card); font-family: var(--font-base); font-size: 15px; font-weight: 600; color: var(--color-text); cursor: pointer; transition: all var(--transition); }
.qc-opt:hover:not(:disabled) { border-color: var(--color-primary); background: var(--color-primary-bg); }
.qc-opt--picked  { border-color: var(--color-primary); background: var(--color-primary-bg); }
.qc-opt--correct { border-color: var(--color-secondary); background: var(--color-secondary-bg); color: var(--color-secondary); }
.qc-opt--wrong   { border-color: var(--color-error); background: #FEF2F2; color: var(--color-error); }

/* Save prompt cuối phiên (FR-FC-81) */
.fss-save-prompt { background: var(--color-primary-bg); border: 1.5px solid var(--color-primary-light); border-radius: var(--radius-lg); padding: 16px 18px; margin-bottom: 12px; }
.fss-save-prompt p { font-size: 14px; color: var(--color-text); margin: 0 0 12px; }
.fss-save-actions, .fss-end-actions { display: flex; gap: 10px; justify-content: center; }

.fss-btn { height: 42px; padding: 0 22px; border-radius: var(--radius-full); font-family: var(--font-base); font-size: 14px; font-weight: 700; cursor: pointer; border: none; }
.fss-btn--primary { background: var(--color-secondary); color: white; }
.fss-btn--primary:hover { filter: brightness(1.07); }
.fss-btn--ghost { background: transparent; border: 1.5px solid var(--color-border); color: var(--color-text-sub); }

.fss-loading { display: flex; justify-content: center; padding: 60px; }
.fss-spinner { width: 44px; height: 44px; border: 4px solid var(--color-primary-light); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
.fss-error { background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 767px) { .fss-body { padding: 16px; } .qc-options { grid-template-columns: 1fr; } .fss-rating { gap: 8px; } }
@media (prefers-reduced-motion: reduce) { .fss-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 9. CÁC TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Loading** | Spinner giữa khung |
| **Error** | Banner đỏ |
| **Thẻ NEW chưa lật** | Mặt trước + "Lật thẻ" |
| **Thẻ NEW đã lật** | Mặt sau + 3 nút đánh giá |
| **Thẻ REVIEW chưa chọn** | Từ + 2–4 lựa chọn |
| **Thẻ REVIEW đã chọn** | Server chấm → viền xanh (đúng) / đỏ (sai) + lộ đáp án đúng, tự chuyển sau 1.1s |
| **Kết thúc** | Saku-chan `celebrate` + thống kê đúng/sai |
| **FR-FC-81** | Nếu có từ sai → hộp "Thêm N từ vào Sổ tay?" |
| **Queue rỗng** | Vào thẳng màn kết thúc (không có từ để học) |

---

## 10. DOMAIN RULES

- **Thứ tự queue do backend quyết** (§3.6 + `ALGO-session-ordering.md`) — frontend KHÔNG sắp xếp lại, chỉ render tuần tự.
- **Thẻ REVIEW chấm server-side** (FR-FC-55/56): client gửi `selectedOptionId`, KHÔNG tự so đáp án; payload quiz không chứa đáp án đúng.
- **`isLastCardInSession=true`** ở lượt cuối để backend kích hoạt FR-FC-81.
- **Thu thập từ sai** (`wrong[]`) gồm cả NEW rated `wrong` lẫn REVIEW chấm sai; cuối phiên gộp gửi `review-deck/add` 1 lần.
- **409 khi add** (từ đã có trong sổ) coi như thành công, không báo lỗi đỏ.
- **Saku-chan** phải đúng cảm xúc: `correct`/`wrong` theo phản hồi, `celebrate` cuối phiên (DESIGN.md §Saku-chan).
- **Chữ Nhật** render `lang="ja"`. **Không trang trắng** — queue rỗng → màn kết thúc.
- **Reduced motion**: tắt animation lật thẻ + spinner.

---

## 11. ENTRY POINTS

| Từ đâu | Query | Phạm vi |
|:---|:---|:---|
| `/flashcard` → chọn Course → Topic → "⚡ Học" | `/vocabulary/flashcard?level=&topic=` | theo topic (level+topic) — entry chính |
| `/vocabulary?level=&topic=` → "Học Flashcard" | `/vocabulary/flashcard?level=&topic=` | theo topic |
| `/notebook` → "Ôn lại ngay" | `?deckId={reviewDeckId}` | Sổ tay "Từ cần ôn lại" |

> Sau khi thêm từ sai vào Sổ tay (FR-FC-81), phiên kế tiếp mở từ `/notebook` sẽ thấy các từ này.

---

## OUT OF SCOPE
- ❌ Sắp xếp/nhịp phiên ở client — hoàn toàn backend (`ALGO-session-ordering.md`).
- ❌ Thẻ ÔN dạng tự luận / nhập đáp án — bản này chỉ trắc nghiệm chọn nghĩa.
- ❌ Undo sau khi đánh giá/chọn — record đã ghi SM-2.
- ❌ Chế độ học gõ chữ (typing), nghe (audio-only) — tương lai.
