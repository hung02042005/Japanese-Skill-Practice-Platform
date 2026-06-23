# SPEC — Sổ Tay "Từ cần ôn lại" (`/notebook`)
>
> **Sprint:** 2 — Core Learning Loop
> **Prefix:** `ntb-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §10` · `DESIGN.md` (Hanami Theme) | **Backend ref:** `feat-flashcard-srs/SPEC.md` (FR-FC-81, §3.4/3.5) · `feat-dictionary-bookmark/SPEC.md`

---

## 1. MÔ TẢ TRANG

**Sổ Tay** là cuốn "Từ cần ôn lại" cá nhân của học viên — kho gom những từ **cần học / cần ôn lại**. Sổ tay **không phải** nơi học chính; nó là danh sách trung gian, đầu vào đến từ 2 nguồn:

1. **Tự động** — sau khi học xong một tập thẻ Flashcard/Review, **mọi từ trả lời SAI** sẽ được dồn vào sổ tay (theo `FR-FC-81`, gọi `POST /api/flashcards/review-deck/add` ở màn hình phiên học, **không** ở trang này).
2. **Thủ công** — học viên chủ động lưu một từ "muốn học" khi tra Từ điển hoặc xem danh sách từ vựng.

Từ Sổ tay, học viên bấm **"Ôn lại ngay"** để mở một phiên **Flashcard kiểu Quizlet** chỉ trên các từ trong sổ. Khi ôn xong, từ nào lại sai sẽ ở lại / quay lại sổ; từ nào nhớ rõ (SM-2 cho `easy`) sẽ giãn lịch và rời sổ theo thuật toán.

### 1.1 Quan hệ 3 tính năng (làm rõ ranh giới)

```
┌───────────────┐   tra cứu      ┌──────────────────────────────────┐
│   TỪ ĐIỂN      │──────────────▶ │  kho VOCABULARY (published)       │
│  (lookup)      │                └──────────────────────────────────┘
└──────┬─────────┘
       │  "Lưu vào sổ tay" (thủ công)
       ▼
┌──────────────────────────────┐    "Ôn lại ngay"     ┌──────────────────────────┐
│   SỔ TAY = "Từ cần ôn lại"   │ ───────────────────▶ │  FLASHCARD (kiểu Quizlet) │
│   (kho từ cần học/cần ôn)    │                      │  phiên theo deck/topic    │
└──────────────────────────────┘ ◀─────────────────── │  lật thẻ + trắc nghiệm    │
        ▲   từ SAI dồn về (tự động, FR-FC-81)          └──────────────────────────┘
        │   sau khi học xong tập thẻ
```

- **Từ điển** = tra cứu kho vocab, **không** lưu trữ.
- **Flashcard** = chế độ **học** Quizlet (lật thẻ nghĩa cho thẻ MỚI, trắc nghiệm chấm tự động cho thẻ ÔN), phạm vi phiên theo `deckId` hoặc `level + topic`.
- **Sổ tay** = **kho** từ cần ôn, là nguồn để mở phiên Flashcard và là đích nhận từ sai.

Theo `DESIGN.md`: nền **washi** (`--color-bg`), thẻ trắng bo góc, badge JLPT đúng cặp màu chuẩn, Saku-chan ở `EmptyState` khi sổ trống. Pink (`--color-primary`) **chỉ** dùng accent (badge "đến hạn", icon active); green (`--color-secondary`) cho CTA chính "Ôn lại ngay".

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────────┐
│  TopNav                                                              │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  📓 Sổ Tay — Từ cần ôn lại              [▶ Ôn lại ngay (12)]         │
│  12 từ cần ôn · 3 từ đến hạn hôm nay                                 │
│                                                                      │
│  [ Tất cả 12 ]  [ Đến hạn 3 ]              [🔍 Tìm trong sổ...    ]   │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │ [N5] 食べる  ・たべる        ● đến hạn            🗑           │ │
│  │ Ăn                                                              │ │
│  │ Nguồn: trả lời sai (Topic: Động từ N5)     Ôn tiếp: hôm nay     │ │
│  └────────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │ [N4] 経験  ・けいけん                            🗑           │ │
│  │ Kinh nghiệm                                                    │ │
│  │ Nguồn: tự lưu từ Từ điển                   Ôn tiếp: ngày mai    │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  ─── sổ trống ──────────────────────────────────────────────────────  │
│              [SakuChan thinking 160px]                               │
│              Sổ tay đang trống                                       │
│              Trả lời sai khi ôn Flashcard, hoặc lưu từ ở Từ điển,    │
│              các từ cần ôn sẽ xuất hiện ở đây.                       │
│              [Học Flashcard]   [Mở Từ điển]                          │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/notebook/
├── Notebook.jsx
└── Notebook.css

components/student/
└── NotebookWordCard.jsx     ← 1 từ trong sổ (hiển thị + gỡ)
```

> Route: `<Route path="/notebook" element={<PrivateRoute roles={['STUDENT']}><Notebook /></PrivateRoute>} />`.
> Entry: nút "Sổ tay" trong dropdown user của TopNav; nút "📓 Sổ tay" ở trang `/dictionary` và `/flashcard`.

---

## 4. STATE

```js
const [reviewDeckId, setDeckId]  = useState(null);  // id deck "Từ cần ôn lại"
const [words,        setWords]   = useState([]);     // flashcard[] trong sổ
const [filter,       setFilter]  = useState('all');  // 'all' | 'due'
const [query,        setQuery]   = useState('');     // tìm client-side
const [isLoading,    setLoading]  = useState(true);
const [confirmDel,   setConfirm]  = useState(null);   // { flashcardId, label }
const [error,        setError]    = useState('');

const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

> Toàn bộ ở `api/studentService.js`. Backend tự ẩn từ có nguồn `status != 'published'` hoặc bị soft-delete (`FR-FC-34`).

```js
// 1. Tìm deck "Từ cần ôn lại" trong danh sách deck (deck hệ thống của riêng student)
// GET /api/flashcard-decks  → [{ deckId, deckName, isSystem, totalCards, dueToday, nextReviewDate }]
//   → chọn deck có deckName === 'Từ cần ôn lại' (isSystem = true)
export async function getFlashcardDecks() { /* đã có */ }

// 2. Liệt kê từ trong sổ
// GET /api/flashcards?deckId={reviewDeckId}&page=0&size=200
// Response: { content: [{ flashcardId, contentType, frontText, nextReviewDate, isDue,
//                          sourceTopic?, sourceLevel?, addedReason? }] }
export async function getFlashcardsByDeck(deckId, page = 0, size = 200) { /* đã có */ }

// 3. Gỡ một từ khỏi sổ (soft-delete card)  ⚠ ASSUMPTION — xác nhận contract
// DELETE /api/flashcards/{flashcardId}
export async function removeFlashcardCard(flashcardId) {
  await api.delete(`/flashcards/${flashcardId}`);
}

// ── Đầu vào của sổ (KHÔNG gọi ở trang này, ghi để truy vết luồng) ──
// Tự động (FR-FC-81): cuối phiên review → addWrongWordsToReviewDeck(items)  → POST /api/flashcards/review-deck/add
// Thủ công (Từ điển): "Lưu vào sổ tay"   → addWrongWordsToReviewDeck([{ contentType:'VOCABULARY', contentId }])
```

**Học (Ôn lại ngay):** không có API riêng — điều hướng sang phiên Flashcard theo `deckId`:
`navigate('/review?deckId=' + reviewDeckId)` (phiên trộn NEW+REVIEW theo `POST /api/flashcards/session?deckId=...`, xem `SPEC-review.md`).

> **2 điểm cần xác nhận với backend trước khi code:**
>
> 1. **Định danh deck "Từ cần ôn lại"** — đang giả định nhận diện qua `deckName`/`isSystem`. Tốt hơn nếu API trả cờ riêng (vd `deckType: 'review'`).
> 2. **Gỡ 1 thẻ** — `DELETE /api/flashcards/{id}` chưa thấy trong `studentService.js`; cần bổ sung hoặc thống nhất endpoint.

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import NotebookWordCard from '../../components/student/NotebookWordCard';
import { getFlashcardDecks, getFlashcardsByDeck, removeFlashcardCard } from '../../api/studentService';
import './Notebook.css';

const REVIEW_DECK_NAME = 'Từ cần ôn lại';

export default function Notebook() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [deckId,    setDeckId]  = useState(null);
  const [words,     setWords]   = useState([]);
  const [filter,    setFilter]  = useState('all');
  const [query,     setQuery]   = useState('');
  const [isLoading, setLoading] = useState(true);
  const [confirmDel,setConfirm] = useState(null);
  const [error,     setError]   = useState('');

  async function load() {
    setLoading(true);
    try {
      const decks = await getFlashcardDecks();
      const deck  = decks.find((d) => d.deckName === REVIEW_DECK_NAME);
      if (!deck) { setWords([]); setDeckId(null); setError(''); return; }
      setDeckId(deck.deckId);
      const res = await getFlashcardsByDeck(deck.deckId, 0, 200);
      setWords(res.content ?? []);
      setError('');
    } catch {
      setError('Không thể tải sổ tay. Thử lại sau.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  const dueCount = useMemo(() => words.filter((w) => w.isDue).length, [words]);

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase();
    return words
      .filter((w) => filter === 'all' || w.isDue)
      .filter((w) => !q || w.frontText?.toLowerCase().includes(q));
  }, [words, filter, query]);

  async function handleRemove(card) {
    try {
      await removeFlashcardCard(card.flashcardId);
      setWords((prev) => prev.filter((w) => w.flashcardId !== card.flashcardId));
      setConfirm(null);
      addToast('success', 'Đã gỡ từ khỏi sổ tay.');
    } catch {
      addToast('error', 'Không thể gỡ từ này. Thử lại.');
    }
  }

  function handleStudy() {
    if (!deckId || words.length === 0) return;
    navigate(`/review?deckId=${deckId}`);
  }

  return (
    <div className="ntb-page">
      <TopNav activeTab="" />

      <main className="ntb-body">
        {/* Header */}
        <div className="ntb-header">
          <div>
            <h1 className="ntb-title"><span aria-hidden="true">📓</span> Sổ Tay — Từ cần ôn lại</h1>
            <p className="ntb-subtitle">
              {isLoading ? 'Đang tải…' : `${words.length} từ cần ôn · ${dueCount} từ đến hạn hôm nay`}
            </p>
          </div>
          <button
            className="ntb-btn ntb-btn--primary"
            onClick={handleStudy}
            disabled={isLoading || words.length === 0}
          >
            ▶ Ôn lại ngay{words.length > 0 ? ` (${words.length})` : ''}
          </button>
        </div>

        {/* Filter + search */}
        <div className="ntb-controls">
          <div className="ntb-tabs" role="tablist" aria-label="Lọc từ trong sổ">
            <button role="tab" aria-selected={filter === 'all'} className={`ntb-tab${filter === 'all' ? ' ntb-tab--active' : ''}`} onClick={() => setFilter('all')}>
              Tất cả <span className="ntb-tab-count">{words.length}</span>
            </button>
            <button role="tab" aria-selected={filter === 'due'} className={`ntb-tab${filter === 'due' ? ' ntb-tab--active' : ''}`} onClick={() => setFilter('due')}>
              Đến hạn <span className="ntb-tab-count">{dueCount}</span>
            </button>
          </div>
          <div className="ntb-search">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"><circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="2"/><path d="M21 21l-4-4" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/></svg>
            <input type="search" className="ntb-search-input" placeholder="Tìm trong sổ..." value={query} onChange={(e) => setQuery(e.target.value)} aria-label="Tìm trong sổ tay" />
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="ntb-error" role="alert">
            <span>{error}</span>
            <button className="ntb-retry" onClick={load}>Thử lại</button>
          </div>
        )}

        {/* Loading */}
        {isLoading && !error && (
          <div className="ntb-list">{[1,2,3,4].map((i) => <div key={i} className="ntb-skel" aria-hidden="true" />)}</div>
        )}

        {/* Empty */}
        {!isLoading && !error && visible.length === 0 && (
          <EmptyState
            title={query ? 'Không tìm thấy từ nào' : 'Sổ tay đang trống'}
            subtitle={query
              ? 'Thử từ khóa khác.'
              : 'Trả lời sai khi ôn Flashcard, hoặc lưu từ ở Từ điển — các từ cần ôn sẽ xuất hiện ở đây.'}
            mascotVariant="thinking"
            mascotSize={160}
          >
            {!query && (
              <>
                <button className="ntb-btn ntb-btn--primary" onClick={() => navigate('/flashcard')}>Học Flashcard</button>
                <button className="ntb-btn ntb-btn--ghost" onClick={() => navigate('/dictionary')}>Mở Từ điển</button>
              </>
            )}
          </EmptyState>
        )}

        {/* List */}
        {!isLoading && !error && visible.length > 0 && (
          <div className="ntb-list">
            {visible.map((w) => (
              <NotebookWordCard
                key={w.flashcardId}
                word={w}
                onRemove={() => setConfirm({ flashcardId: w.flashcardId, label: w.frontText })}
              />
            ))}
          </div>
        )}
      </main>

      {/* Confirm gỡ */}
      {confirmDel && (
        <div className="ntb-modal-backdrop" role="dialog" aria-modal="true" aria-label="Xác nhận gỡ từ"
             onClick={(e) => { if (e.target === e.currentTarget) setConfirm(null); }}>
          <div className="ntb-modal">
            <h2 className="ntb-modal-title">Gỡ khỏi sổ tay</h2>
            <p className="ntb-modal-body">Gỡ <strong lang="ja">"{confirmDel.label}"</strong> khỏi sổ tay? Từ này sẽ không còn trong danh sách cần ôn.</p>
            <div className="ntb-modal-footer">
              <button className="ntb-modal-cancel" onClick={() => setConfirm(null)}>Hủy</button>
              <button className="ntb-modal-danger" onClick={() => handleRemove(confirmDel)}>Gỡ</button>
            </div>
          </div>
        </div>
      )}

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

## 7. NotebookWordCard component

```jsx
// components/student/NotebookWordCard.jsx
const REASON_LABEL = { wrong: 'trả lời sai', manual: 'tự lưu từ Từ điển', learn: 'thêm từ bài học' };

function nextLabel(iso, isDue) {
  if (isDue) return 'hôm nay';
  const days = Math.ceil((new Date(iso) - Date.now()) / 86400000);
  if (days <= 1) return 'ngày mai';
  return new Date(iso).toLocaleDateString('vi-VN');
}

export default function NotebookWordCard({ word, onRemove }) {
  const { frontText, meaning, jlptLevel, isDue, nextReviewDate, addedReason, sourceTopic } = word;
  return (
    <article className={`nwc-card${isDue ? ' nwc-card--due' : ''}`}>
      <div className="nwc-main">
        <div className="nwc-head">
          {jlptLevel && <span className={`jlpt-badge jlpt-${jlptLevel}`}>{jlptLevel}</span>}
          <span className="nwc-word" lang="ja">{frontText}</span>
          {isDue && <span className="nwc-due" aria-label="Đến hạn ôn">● đến hạn</span>}
          <button className="nwc-del" onClick={onRemove} aria-label="Gỡ khỏi sổ tay">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
          </button>
        </div>
        {meaning && <p className="nwc-meaning">{meaning}</p>}
        <p className="nwc-meta">
          {addedReason && <>Nguồn: {REASON_LABEL[addedReason] ?? addedReason}{sourceTopic ? ` (Topic: ${sourceTopic})` : ''} · </>}
          Ôn tiếp: {nextLabel(nextReviewDate, isDue)}
        </p>
      </div>
    </article>
  );
}
```

---

## 8. CSS

```css
/* ===== Sổ Tay "Từ cần ôn lại" (SakuJi Hanami Theme) ===== */

.ntb-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.ntb-body { flex: 1; max-width: 820px; width: 100%; margin: 0 auto; padding: 28px 24px 48px; display: flex; flex-direction: column; gap: 18px; box-sizing: border-box; }

/* Header */
.ntb-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.ntb-title { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0; display: flex; align-items: center; gap: 8px; }
.ntb-subtitle { font-size: 14px; color: var(--color-text-sub); margin: 4px 0 0; }

/* Controls */
.ntb-controls { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.ntb-tabs { display: flex; gap: 8px; }
.ntb-tab { display: inline-flex; align-items: center; gap: 6px; height: 36px; padding: 0 14px; border-radius: var(--radius-full); border: 1.5px solid var(--color-border); background: var(--color-card); color: var(--color-text-sub); font-family: var(--font-base); font-size: 13px; font-weight: 600; cursor: pointer; transition: all var(--transition); }
.ntb-tab:hover { background: var(--color-primary-bg); }
.ntb-tab--active { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-bg); }
.ntb-tab-count { font-size: 11px; font-weight: 700; background: var(--color-bg); border-radius: var(--radius-full); padding: 1px 7px; }
.ntb-tab--active .ntb-tab-count { background: var(--color-card); }

.ntb-search { display: flex; align-items: center; gap: 8px; height: 38px; padding: 0 14px; background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: var(--radius-full); color: var(--color-text-sub); min-width: 200px; }
.ntb-search:focus-within { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(232,154,170,0.15); }
.ntb-search-input { border: none; outline: none; background: transparent; font-family: var(--font-base); font-size: 14px; color: var(--color-text); width: 100%; }

/* Error */
.ntb-error { display: flex; align-items: center; justify-content: space-between; gap: 12px; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.ntb-retry { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }

/* List */
.ntb-list { display: flex; flex-direction: column; gap: 12px; }
.ntb-skel { height: 84px; border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* NotebookWordCard */
.nwc-card { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); padding: 16px 20px; transition: box-shadow var(--transition); }
.nwc-card:hover { box-shadow: var(--shadow-md); }
.nwc-card--due { border-left: 3px solid var(--color-primary); }
.nwc-main { display: flex; flex-direction: column; gap: 6px; }
.nwc-head { display: flex; align-items: center; gap: 10px; }
.nwc-word { font-size: 22px; font-weight: 700; color: var(--color-text); }
.nwc-due { font-size: 12px; font-weight: 700; color: var(--color-primary); }
.nwc-del { margin-left: auto; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; background: transparent; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); color: var(--color-text-sub); cursor: pointer; }
.nwc-del:hover { border-color: var(--color-error); color: var(--color-error); background: #FEF2F2; }
.nwc-meaning { font-size: 14px; color: var(--color-text-sub); margin: 0; }
.nwc-meta { font-size: 12px; color: var(--color-text-disabled); margin: 0; }

/* CTA + modal */
.ntb-btn { display: inline-flex; align-items: center; justify-content: center; gap: 6px; height: 42px; padding: 0 22px; border-radius: var(--radius-full); font-family: var(--font-base); font-size: 14px; font-weight: 700; cursor: pointer; border: none; transition: filter var(--transition); }
.ntb-btn--primary { background: var(--color-secondary); color: white; }
.ntb-btn--primary:hover:not(:disabled) { filter: brightness(1.07); }
.ntb-btn--primary:disabled { opacity: 0.55; cursor: not-allowed; }
.ntb-btn--ghost { background: transparent; border: 1.5px solid var(--color-border); color: var(--color-text-sub); }
.ntb-btn--ghost:hover { color: var(--color-text); background: var(--color-bg); }

.ntb-modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.40); backdrop-filter: blur(2px); z-index: 300; display: flex; align-items: center; justify-content: center; padding: 16px; }
.ntb-modal { background: var(--color-card); border-radius: var(--radius-xl); box-shadow: var(--shadow-lg); padding: 28px; width: 100%; max-width: 400px; display: flex; flex-direction: column; gap: 16px; animation: modalIn 0.22s ease; }
@keyframes modalIn { from { opacity: 0; transform: scale(0.93); } to { opacity: 1; transform: scale(1); } }
.ntb-modal-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; }
.ntb-modal-body { font-size: 14px; color: var(--color-text-sub); line-height: 1.6; margin: 0; }
.ntb-modal-footer { display: flex; justify-content: flex-end; gap: 10px; }
.ntb-modal-cancel { height: 40px; padding: 0 18px; border-radius: var(--radius-full); border: 1.5px solid var(--color-border); background: transparent; color: var(--color-text-sub); font-size: 14px; font-weight: 600; cursor: pointer; }
.ntb-modal-danger { height: 40px; padding: 0 22px; border-radius: var(--radius-full); background: var(--color-error); color: white; border: none; font-size: 14px; font-weight: 700; cursor: pointer; }

@media (max-width: 767px) {
  .ntb-body { padding: 16px 16px 32px; }
  .ntb-header { flex-direction: column; align-items: stretch; }
  .ntb-controls { flex-direction: column; align-items: stretch; }
  .ntb-search { width: 100%; }
}
@media (prefers-reduced-motion: reduce) {
  .ntb-page * { animation: none !important; transition-duration: 0ms !important; }
}
```

> **JLPT badge** (`.jlpt-badge`, `.jlpt-N5…N1`) tái dùng class chung global theo cặp màu `DESIGN.md §JLPT Level Colours`. Không hard-code hex.

---

## 9. CÁC TRẠNG THÁI

| Trạng thái | Xử lý |
|:---|:---|
| **Loading** | 4 skeleton card xám pulse |
| **Error** | Banner đỏ + nút "Thử lại" gọi lại `load()` |
| **Empty (sổ trống)** | `EmptyState` Saku-chan `thinking` + CTA "Học Flashcard" / "Mở Từ điển" |
| **Empty (tìm không ra)** | `EmptyState` "Không tìm thấy từ nào", không CTA |
| **Có dữ liệu** | Danh sách `NotebookWordCard`, lọc Tất cả / Đến hạn + tìm kiếm client-side |
| **Gỡ từ** | Modal confirm trước khi `DELETE` |

---

## 10. DOMAIN RULES

- **Sổ tay ≠ chế độ học.** Học diễn ra ở Flashcard (phiên Quizlet). Trang này chỉ liệt kê + điều hướng "Ôn lại ngay" (`/review?deckId=`).
- **Đầu vào tự động (FR-FC-81):** cuối phiên review, từ VOCABULARY trả lời `wrong`/`again` được thêm vào deck "Từ cần ôn lại" qua `POST /api/flashcards/review-deck/add`. Lời gọi này nằm ở **màn hình phiên học** (`SPEC-review.md` / `VocabFlashcardSession`), **không** ở trang Sổ tay.
- **Đầu vào thủ công:** từ Từ điển/danh sách vocab, nút "Lưu vào sổ tay" gọi cùng endpoint `review-deck/add` với `{ contentType:'VOCABULARY', contentId }`.
- **Ra khỏi sổ:** (a) học viên gỡ thủ công; hoặc (b) theo SM-2, khi từ đã nhớ rõ và giãn lịch — hiển thị "Ôn tiếp: <ngày>" nhưng vẫn nằm trong deck cho tới khi đạt ngưỡng/bị gỡ (logic ở backend).
- **Bảo mật:** chỉ thấy thẻ của chính mình (`NFR-FC-04`); từ có nguồn `status != 'published'` hoặc đã xoá bị backend ẩn (`FR-FC-34`).
- **Furigana / chữ Nhật** render `lang="ja"` (Noto Sans JP — `DESIGN.md §Typography`).
- **Không trang trắng:** mọi nhánh rỗng render `EmptyState` + Saku-chan (`DESIGN.md §Do`).
- **Accent pink** chỉ cho badge "đến hạn", tab active, viền trái thẻ due; CTA chính dùng green `--color-secondary`.
- **Reduced motion:** tắt animation khi `prefers-reduced-motion`.

---

## 11. ENTRY POINTS & ĐIỀU HƯỚNG

| Từ đâu | Tới | Ghi chú |
|:---|:---|:---|
| TopNav user dropdown → "Sổ tay" | `/notebook` | mục mới trong dropdown |
| `/dictionary`, `/flashcard` → nút "📓 Sổ tay" | `/notebook` | góc header |
| Sổ tay → "Ôn lại ngay" | `/review?deckId={reviewDeckId}` | phiên Flashcard chỉ trên từ trong sổ |
| EmptyState → "Học Flashcard" | `/flashcard` | khi sổ trống |
| EmptyState → "Mở Từ điển" | `/dictionary` | khi sổ trống |
| (đầu vào) cuối phiên `/review` | thêm vào sổ | tự động qua `review-deck/add` (FR-FC-81) |
| (đầu vào) `/dictionary` "Lưu vào sổ tay" | thêm vào sổ | thủ công, cùng endpoint |

---

## 12. CẦN BACKEND XÁC NHẬN (trước khi code)

1. **Cách nhận diện deck "Từ cần ôn lại"** trong `GET /api/flashcard-decks` — hiện giả định theo `deckName`/`isSystem`; đề xuất thêm cờ `deckType: 'review'` hoặc endpoint `GET /api/flashcards/review-deck`.
2. **Metadata mỗi thẻ** cần thêm `addedReason` (`wrong|manual|learn`) và `sourceTopic`/`sourceLevel` để hiển thị cột "Nguồn". Nếu backend chưa trả → ẩn dòng meta nguồn.
3. **Gỡ 1 thẻ** — thống nhất `DELETE /api/flashcards/{flashcardId}` (soft delete).
4. **"Lưu vào sổ tay" thủ công từ Từ điển** — xác nhận dùng chung `POST /api/flashcards/review-deck/add` hay endpoint riêng.

---

## OUT OF SCOPE

- ❌ Học/ôn ngay trong trang Sổ tay (lật thẻ tại chỗ) — học diễn ra ở Flashcard, xem `SPEC-flashcard.md` / `SPEC-review.md`.
- ❌ Bookmark ngữ pháp / bài học / Kanji dạng ghi chú — Sổ tay bản này tập trung **từ vựng cần ôn**; bookmark Từ điển tổng quát thuộc `feat-dictionary-bookmark`.
- ❌ Export / chia sẻ sổ tay.
- ❌ Sắp xếp thủ công — danh sách theo `nextReviewDate` / thứ tự backend.
