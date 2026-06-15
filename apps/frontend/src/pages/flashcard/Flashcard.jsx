import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import {
  fetchDecksThunk,
  fetchCardsByDeckThunk,
  clearActiveDeck,
} from '../../store/slices/flashcardSlice';
import { createDeck, deleteDeck } from '../../api/studentService';
import './Flashcard.css';

export default function Flashcard() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { toasts, addToast, removeToast } = useToast();

  const { decks, decksStatus, activeDeck, deckCards, deckCardsStatus, error } =
    useSelector((s) => s.flashcard);

  const [showCreate, setCreate]   = useState(false);
  const [newName,    setNewName]  = useState('');
  const [isCreating, setCreating] = useState(false);
  const [confirmDel, setConfirm]  = useState(null);

  useEffect(() => {
    if (decksStatus === 'idle') dispatch(fetchDecksThunk());
  }, [dispatch, decksStatus]);

  async function handleViewDeck(deck) {
    const res = await dispatch(fetchCardsByDeckThunk(deck));
    if (fetchCardsByDeckThunk.rejected.match(res)) addToast('error', res.payload);
  }

  async function handleCreateDeck() {
    if (!newName.trim()) return;
    if (newName.length > 100) { addToast('error', 'Tên bộ thẻ tối đa 100 ký tự.'); return; }
    setCreating(true);
    try {
      await createDeck(newName.trim());
      addToast('success', 'Tạo bộ thẻ thành công!');
      setCreate(false);
      setNewName('');
      dispatch(fetchDecksThunk());
    } catch (err) {
      if (err?.response?.status === 409) { addToast('error', 'Tên bộ thẻ đã tồn tại.'); return; }
      addToast('error', 'Không thể tạo bộ thẻ.');
    } finally {
      setCreating(false);
    }
  }

  async function handleDeleteDeck(deck) {
    try {
      await deleteDeck(deck.deckId);
      addToast('success', 'Đã xoá bộ thẻ!');
      setConfirm(null);
      if (activeDeck?.deckId === deck.deckId) dispatch(clearActiveDeck());
      dispatch(fetchDecksThunk());
    } catch {
      addToast('error', 'Không thể xoá bộ thẻ.');
    }
  }

  const isLoading   = decksStatus === 'loading';
  const isCardLoad  = deckCardsStatus === 'loading';

  return (
    <div className="fls-page">
      <TopNav activeTab="" />
      <main className="fls-body">
        {!activeDeck ? (
          <>
            <div className="fls-page-header">
              <h1 className="fls-title">Bộ Thẻ Của Tôi</h1>
              <button className="fls-create-btn" onClick={() => setCreate(true)}>
                + Tạo bộ thẻ mới
              </button>
            </div>

            {error && decksStatus === 'failed' && (
              <div className="fls-error" role="alert">{error}</div>
            )}

            {isLoading ? (
              <div className="fls-deck-list">
                {[1, 2, 3].map((i) => <div key={i} className="fls-skel" aria-hidden="true" />)}
              </div>
            ) : decks.length === 0 ? (
              <EmptyState
                title="Chưa có bộ thẻ nào"
                subtitle="Thêm Kanji hoặc từ vựng vào Flashcard từ bài học để bắt đầu."
                mascotVariant="thinking"
                mascotSize={140}
              >
                <a href="/learn" className="fls-cta">Học bài mới →</a>
              </EmptyState>
            ) : (
              <div className="fls-deck-list">
                {decks.map((deck) => (
                  <div key={deck.deckId ?? deck.deckName} className="fls-deck-card">
                    <div className="fls-deck-left">
                      <span className="fls-deck-icon" aria-hidden="true">
                        {deck.isSystem ? '⭐' : '👤'}
                      </span>
                      <div>
                        <div className="fls-deck-name">{deck.displayName ?? deck.deckName}</div>
                        <div className="fls-deck-meta">
                          {deck.totalCards} thẻ
                          {deck.dueToday > 0 && (
                            <span> · {deck.dueToday} thẻ cần ôn hôm nay</span>
                          )}
                        </div>
                      </div>
                    </div>
                    <div className="fls-deck-right">
                      {deck.dueToday > 0 && (
                        <span className="fls-due-badge" aria-label={`${deck.dueToday} thẻ cần ôn hôm nay`}>
                          Due: {deck.dueToday}
                        </span>
                      )}
                      <button
                        className="fls-review-btn"
                        onClick={() => navigate('/review')}
                        aria-label={`Ôn tập bộ ${deck.deckName}`}
                      >
                        Ôn tập
                      </button>
                      <button
                        className="fls-view-btn"
                        onClick={() => handleViewDeck(deck)}
                        aria-label={`Xem thẻ trong bộ ${deck.deckName}`}
                      >
                        Xem thẻ
                      </button>
                      {!deck.isSystem && (
                        <button
                          className="fls-del-btn"
                          onClick={() => setConfirm(deck)}
                          aria-label={`Xoá bộ ${deck.deckName}`}
                        >
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
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
              <button
                className="fls-back-btn"
                onClick={() => dispatch(clearActiveDeck())}
              >
                ← Quay lại bộ thẻ
              </button>
              <h1 className="fls-title">{activeDeck.displayName ?? activeDeck.deckName}</h1>
            </div>

            {isCardLoad ? (
              <div className="fls-cards-grid">
                {[1, 2, 3, 4, 5, 6].map((i) => (
                  <div key={i} className="fls-card-skel" aria-hidden="true" />
                ))}
              </div>
            ) : deckCards.length === 0 ? (
              <EmptyState
                title="Bộ thẻ trống"
                subtitle="Thêm thẻ từ bài học."
                mascotVariant="idle"
                mascotSize={120}
              />
            ) : (
              <div className="fls-cards-grid">
                {deckCards.map((c) => (
                  <div
                    key={c.flashcardId}
                    className={`fls-card-mini${c.isDue ? ' fls-card-mini--due' : ''}`}
                  >
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
          <div
            className="fls-modal-backdrop"
            role="dialog"
            aria-modal="true"
            aria-label="Tạo bộ thẻ mới"
            onClick={(e) => { if (e.target === e.currentTarget) setCreate(false); }}
          >
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
                <button
                  className="fls-modal-cancel"
                  onClick={() => { setCreate(false); setNewName(''); }}
                >
                  Hủy
                </button>
                <button
                  className="fls-modal-submit"
                  onClick={handleCreateDeck}
                  disabled={isCreating || !newName.trim()}
                  aria-busy={isCreating}
                >
                  {isCreating && <span className="fls-spinner fls-spinner--white" aria-hidden="true" />}
                  {isCreating ? 'Đang tạo…' : 'Tạo'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Delete confirm */}
        {confirmDel && (
          <div
            className="fls-modal-backdrop"
            role="dialog"
            aria-modal="true"
            aria-label="Xác nhận xoá bộ thẻ"
          >
            <div className="fls-modal">
              <h2 className="fls-modal-title">Xoá bộ thẻ</h2>
              <p className="fls-modal-body">
                Xoá bộ thẻ <strong>"{confirmDel.deckName}"</strong>? Tất cả thẻ trong bộ này sẽ bị xoá. Không thể khôi phục.
              </p>
              <div className="fls-modal-footer">
                <button className="fls-modal-cancel" onClick={() => setConfirm(null)}>Hủy</button>
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
