# SPEC — Quản lý Flashcard (`/flashcard`)
> **Sprint:** 2 — Core Learning Loop
> **Prefix:** `fls-` | **activeTab:** `''` | **Guard:** PrivateRoute (STUDENT)
> **Phụ thuộc:** `USER-SPEC.md §10.4` | **Backend ref:** `feat-flashcard-srs/SPEC.md`

---

## 1. MÔ TẢ TRANG

Trang quản lý bộ thẻ Flashcard cá nhân. Xem danh sách decks, số thẻ due, tạo deck mới (không phải system deck). Click deck → xem thẻ trong deck. Nút "Ôn tập ngay" → `/review`.

---

## 2. MOCKUP

```
┌──────────────────────────────────────────────────────────────────┐
│  TopNav (activeTab="")                                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Bộ Thẻ Của Tôi                     [+ Tạo bộ thẻ mới]         │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  [⭐ SYSTEM] Từ vựng N5              [Due: 12] [Ôn tập] │  │
│  │  156 thẻ · Next review: Hôm nay                         │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  [👤] Bộ thẻ tự tạo                 [Due: 5]  [Ôn tập] │  │
│  │  23 thẻ · Next review: Ngày mai        [Xem thẻ]  [🗑]  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ─── khi mở xem thẻ của deck ────────────────────────────────  │
│  ← Quay lại bộ thẻ                                             │
│  Bộ thẻ tự tạo (23 thẻ)                                        │
│                                                                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                       │
│  │  日      │ │  食べる  │ │  学校    │                       │
│  │ (kanji)  │ │ (vocab)  │ │ (vocab)  │                       │
│  └──────────┘ └──────────┘ └──────────┘                       │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/flashcard/
├── Flashcard.jsx
└── Flashcard.css
```

---

## 4. STATE

```js
const [decks,       setDecks]   = useState([]);
const [activeDecks, setActive]  = useState(null);   // deck name khi xem thẻ
const [deckCards,   setCards]   = useState([]);
const [isLoading,   setLoading] = useState(true);
const [isCardLoad,  setCardLoad]= useState(false);
const [showCreate,  setCreate]  = useState(false);
const [newDeckName, setName]    = useState('');
const [isCreating,  setCreating]= useState(false);
const [confirmDelete, setDelete]= useState(null);  // deck name cần xoá
const [error,       setError]   = useState('');
const { toasts, addToast, removeToast } = useToast();
```

---

## 5. API CALLS

```js
// GET /api/flashcard-decks
// Response: [{ deckName, isSystem, totalCards, dueToday, nextReviewDate }]

// GET /api/flashcards?deckName=xxx&page=0&size=50
// Response: { content: [{ flashcardId, contentType, frontText, nextReviewDate, isDue }] }

// POST /api/flashcard-decks
// Request: { deckName: 'string' }
// Response 201: { deckName }

// DELETE /api/flashcard-decks/:deckName  (chỉ non-system)
// Response 200 | 403 SYSTEM_DECK_IMMUTABLE
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { getFlashcardDecks, getFlashcardsByDeck, createDeck, deleteDeck } from '../../api/studentService';
import './Flashcard.css';

export default function Flashcard() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [decks,      setDecks]   = useState([]);
  const [activeDeck, setActive]  = useState(null);
  const [deckCards,  setCards]   = useState([]);
  const [isLoading,  setLoading] = useState(true);
  const [isCardLoad, setCardLoad]= useState(false);
  const [showCreate, setCreate]  = useState(false);
  const [newName,    setNewName] = useState('');
  const [isCreating, setCreating]= useState(false);
  const [confirmDel, setConfirmDel] = useState(null);
  const [error,      setError]   = useState('');

  async function loadDecks() {
    setLoading(true);
    try {
      const data = await getFlashcardDecks();
      setDecks(data);
    } catch {
      setError('Không thể tải bộ thẻ. Thử lại sau.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadDecks(); }, []);

  async function handleViewDeck(deckName) {
    setActive(deckName); setCardLoad(true);
    try {
      const res = await getFlashcardsByDeck(deckName);
      setCards(res.content);
    } catch {
      addToast('error', 'Không thể tải thẻ trong bộ này.');
    } finally {
      setCardLoad(false);
    }
  }

  async function handleCreateDeck() {
    if (!newName.trim()) return;
    if (newName.length > 100) { addToast('error', 'Tên bộ thẻ tối đa 100 ký tự.'); return; }
    setCreating(true);
    try {
      await createDeck(newName.trim());
      addToast('success', 'Tạo bộ thẻ thành công!');
      setCreate(false); setNewName('');
      await loadDecks();
    } catch (err) {
      if (err?.response?.status === 409) { addToast('error', 'Tên bộ thẻ đã tồn tại.'); return; }
      addToast('error', 'Không thể tạo bộ thẻ.');
    } finally {
      setCreating(false);
    }
  }

  async function handleDeleteDeck(deckName) {
    try {
      await deleteDeck(deckName);
      addToast('success', 'Đã xoá bộ thẻ!');
      setConfirmDel(null);
      if (activeDeck === deckName) setActive(null);
      await loadDecks();
    } catch {
      addToast('error', 'Không thể xoá bộ thẻ.');
    }
  }

  return (
    <div className="fls-page">
      <TopNav activeTab="" />
      <main className="fls-body">
        {!activeDeck ? (
          <>
            <div className="fls-page-header">
              <h1 className="fls-title">Bộ Thẻ Của Tôi</h1>
              <button className="fls-create-btn" onClick={() => setCreate(true)}>+ Tạo bộ thẻ mới</button>
            </div>

            {error && <div className="fls-error" role="alert">{error}</div>}

            {isLoading ? (
              <div className="fls-deck-list">{[1,2,3].map((i) => <div key={i} className="fls-skel" aria-hidden="true" />)}</div>
            ) : decks.length === 0 ? (
              <EmptyState
                title="Chưa có bộ thẻ nào"
                subtitle="Thêm Kanji hoặc từ vựng vào Flashcard từ bài học để bắt đầu."
                mascotVariant="thinking"
                mascotSize={140}
              >
                <a href="/learn/new" className="fls-cta">Học bài mới →</a>
              </EmptyState>
            ) : (
              <div className="fls-deck-list">
                {decks.map((deck) => (
                  <div key={deck.deckName} className="fls-deck-card">
                    <div className="fls-deck-left">
                      <span className="fls-deck-icon" aria-hidden="true">{deck.isSystem ? '⭐' : '👤'}</span>
                      <div>
                        <div className="fls-deck-name">{deck.deckName}</div>
                        <div className="fls-deck-meta">
                          {deck.totalCards} thẻ
                          {deck.nextReviewDate && <span> · Ôn tiếp: {new Date(deck.nextReviewDate).toLocaleDateString('vi-VN')}</span>}
                        </div>
                      </div>
                    </div>
                    <div className="fls-deck-right">
                      {deck.dueToday > 0 && (
                        <span className="fls-due-badge" aria-label={`${deck.dueToday} thẻ cần ôn hôm nay`}>Due: {deck.dueToday}</span>
                      )}
                      <button className="fls-review-btn" onClick={() => navigate('/review')} aria-label={`Ôn tập bộ ${deck.deckName}`}>Ôn tập</button>
                      <button className="fls-view-btn" onClick={() => handleViewDeck(deck.deckName)} aria-label={`Xem thẻ trong bộ ${deck.deckName}`}>Xem thẻ</button>
                      {!deck.isSystem && (
                        <button className="fls-del-btn" onClick={() => setConfirmDel(deck.deckName)} aria-label={`Xoá bộ ${deck.deckName}`}>
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        ) : (
          <>
            <div className="fls-deck-header">
              <button className="fls-back-btn" onClick={() => { setActive(null); setCards([]); }}>← Quay lại bộ thẻ</button>
              <h1 className="fls-title">{activeDeck}</h1>
            </div>
            {isCardLoad ? (
              <div className="fls-cards-grid">{[1,2,3,4,5,6].map((i) => <div key={i} className="fls-card-skel" aria-hidden="true" />)}</div>
            ) : deckCards.length === 0 ? (
              <EmptyState title="Bộ thẻ trống" subtitle="Thêm thẻ từ bài học." mascotVariant="idle" mascotSize={120} />
            ) : (
              <div className="fls-cards-grid">
                {deckCards.map((c) => (
                  <div key={c.flashcardId} className={`fls-card-mini${c.isDue ? ' fls-card-mini--due' : ''}`}>
                    <span className="fls-card-front" lang="ja">{c.frontText}</span>
                    {c.isDue && <span className="fls-due-dot" aria-label="Đến hạn ôn" />}
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {/* Create deck modal */}
        {showCreate && (
          <div className="fls-modal-backdrop" role="dialog" aria-modal="true" aria-label="Tạo bộ thẻ mới" onClick={(e) => { if (e.target === e.currentTarget) setCreate(false); }}>
            <div className="fls-modal">
              <h2 className="fls-modal-title">Tạo bộ thẻ mới</h2>
              <input
                className="fls-modal-input"
                type="text"
                placeholder="Tên bộ thẻ..."
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                maxLength={100}
                autoFocus
                onKeyDown={(e) => e.key === 'Enter' && handleCreateDeck()}
                aria-label="Tên bộ thẻ mới"
              />
              <div className="fls-modal-footer">
                <button className="fls-modal-cancel" onClick={() => { setCreate(false); setNewName(''); }}>Hủy</button>
                <button className="fls-modal-submit" onClick={handleCreateDeck} disabled={isCreating || !newName.trim()}>
                  {isCreating && <span className="fls-spinner fls-spinner--white" aria-hidden="true" />}
                  {isCreating ? 'Đang tạo…' : 'Tạo'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Delete confirm */}
        {confirmDel && (
          <div className="fls-modal-backdrop" role="dialog" aria-modal="true" aria-label="Xác nhận xoá">
            <div className="fls-modal">
              <h2 className="fls-modal-title">Xoá bộ thẻ</h2>
              <p className="fls-modal-body">Xoá bộ thẻ <strong>"{confirmDel}"</strong>? Tất cả thẻ trong bộ này sẽ bị xoá. Không thể khôi phục.</p>
              <div className="fls-modal-footer">
                <button className="fls-modal-cancel" onClick={() => setConfirmDel(null)}>Hủy</button>
                <button className="fls-modal-danger" onClick={() => handleDeleteDeck(confirmDel)}>Xoá</button>
              </div>
            </div>
          </div>
        )}
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
```

---

## 7. CSS (rút gọn)

```css
/* ===== Flashcard Browser (SakuJi Hanami Theme) ===== */
.fls-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); font-family: var(--font-base); }
.fls-body { flex: 1; max-width: 860px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }
.fls-page-header { display: flex; align-items: center; justify-content: space-between; }
.fls-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin: 0; }
.fls-create-btn { height: 38px; padding: 0 18px; background: var(--color-secondary); color: white; border: none; border-radius: var(--radius-full); font-size: 14px; font-weight: 700; cursor: pointer; transition: filter var(--transition); }
.fls-create-btn:hover { filter: brightness(1.07); }
.fls-error { background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.fls-deck-list { display: flex; flex-direction: column; gap: 12px; }
.fls-deck-card { background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); padding: 16px 20px; display: flex; align-items: center; justify-content: space-between; gap: 12px; transition: box-shadow var(--transition); }
.fls-deck-card:hover { box-shadow: var(--shadow-md); }
.fls-deck-left { display: flex; align-items: center; gap: 14px; flex: 1; min-width: 0; }
.fls-deck-icon { font-size: 22px; flex-shrink: 0; }
.fls-deck-name { font-size: 16px; font-weight: 700; color: var(--color-text); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.fls-deck-meta { font-size: 12px; color: var(--color-text-sub); }
.fls-deck-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.fls-due-badge { background: var(--color-primary-bg); color: var(--color-primary); border-radius: var(--radius-full); padding: 3px 10px; font-size: 12px; font-weight: 700; }
.fls-review-btn { height: 34px; padding: 0 14px; background: var(--color-primary); color: white; border: none; border-radius: var(--radius-full); font-size: 13px; font-weight: 700; cursor: pointer; }
.fls-view-btn { height: 34px; padding: 0 14px; background: transparent; border: 1.5px solid var(--color-border); color: var(--color-text-sub); border-radius: var(--radius-full); font-size: 13px; font-weight: 600; cursor: pointer; }
.fls-del-btn { width: 34px; height: 34px; background: transparent; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); color: var(--color-text-sub); cursor: pointer; display: flex; align-items: center; justify-content: center; }
.fls-del-btn:hover { border-color: var(--color-error); color: var(--color-error); background: #FEF2F2; }
.fls-deck-header { display: flex; align-items: center; gap: 16px; }
.fls-back-btn { font-size: 13px; color: var(--color-text-sub); background: transparent; border: none; cursor: pointer; font-weight: 600; }
.fls-back-btn:hover { color: var(--color-primary); }
.fls-cards-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.fls-card-mini { position: relative; background: var(--color-card); border: 2px solid var(--color-border); border-radius: var(--radius-lg); padding: 20px 16px; display: flex; align-items: center; justify-content: center; aspect-ratio: 3/2; }
.fls-card-mini--due { border-color: var(--color-primary-light); background: var(--color-primary-bg); }
.fls-card-front { font-size: 24px; font-weight: 700; color: var(--color-text); }
.fls-due-dot { position: absolute; top: 8px; right: 8px; width: 8px; height: 8px; background: var(--color-primary); border-radius: 50%; }
.fls-skel { height: 70px; border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
.fls-card-skel { aspect-ratio: 3/2; border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }
@keyframes skelPulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.fls-cta { display: inline-flex; align-items: center; height: 40px; padding: 0 20px; background: var(--color-secondary); color: white; border-radius: var(--radius-full); text-decoration: none; font-size: 14px; font-weight: 700; }
.fls-modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.40); backdrop-filter: blur(2px); z-index: 300; display: flex; align-items: center; justify-content: center; padding: 16px; }
.fls-modal { background: var(--color-card); border-radius: var(--radius-xl); box-shadow: var(--shadow-lg); padding: 28px; width: 100%; max-width: 400px; display: flex; flex-direction: column; gap: 16px; animation: modalIn 0.22s ease; }
@keyframes modalIn { from { opacity: 0; transform: scale(0.93); } to { opacity: 1; transform: scale(1); } }
.fls-modal-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin: 0; }
.fls-modal-body { font-size: 14px; color: var(--color-text-sub); line-height: 1.6; margin: 0; }
.fls-modal-input { height: 44px; padding: 0 14px; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); background: var(--color-bg); font-family: var(--font-base); font-size: 14px; color: var(--color-text); box-sizing: border-box; width: 100%; }
.fls-modal-input:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(232,154,170,0.15); }
.fls-modal-footer { display: flex; justify-content: flex-end; gap: 10px; }
.fls-modal-cancel { height: 40px; padding: 0 18px; border-radius: var(--radius-full); border: 1.5px solid var(--color-border); background: transparent; color: var(--color-text-sub); font-size: 14px; font-weight: 600; cursor: pointer; }
.fls-modal-submit { height: 40px; padding: 0 22px; border-radius: var(--radius-full); background: var(--color-secondary); color: white; border: none; font-size: 14px; font-weight: 700; cursor: pointer; display: flex; align-items: center; gap: 7px; }
.fls-modal-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.fls-modal-danger { height: 40px; padding: 0 22px; border-radius: var(--radius-full); background: var(--color-error); color: white; border: none; font-size: 14px; font-weight: 700; cursor: pointer; }
.fls-spinner { display: inline-block; width: 15px; height: 15px; border: 2.5px solid rgba(255,255,255,0.35); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 767px) { .fls-body { padding: 16px 16px 32px; } .fls-cards-grid { grid-template-columns: repeat(3, 1fr); } .fls-deck-right { flex-wrap: wrap; justify-content: flex-end; } }
@media (prefers-reduced-motion: reduce) { .fls-page * { animation: none !important; transition-duration: 0ms !important; } }
```
