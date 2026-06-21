import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import NotebookWordCard from '../../components/student/NotebookWordCard';
import {
  getFlashcardDecks,
  getFlashcardsByDeck,
  removeFlashcardCard,
} from '../../api/studentService';
import './Notebook.css';

/**
 * Sổ Tay "Từ cần ôn lại" (SPEC-notebook.md) — kho gom từ cần học/ôn lại.
 * Đầu vào: tự động (từ sai cuối phiên, FR-FC-81) + thủ công (lưu từ Từ điển).
 * Trang chỉ liệt kê + điều hướng "Ôn lại ngay" → phiên Flashcard theo deckId.
 */
const REVIEW_DECK_NAME = 'Từ cần ôn lại';
const PAGE_SIZE = 30;   // 1A: tải theo trang + cuộn vô hạn, thay vì cứng 200 (mất từ khi >200)

export default function Notebook() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [deckId,    setDeckId]  = useState(null);
  const [deckReady, setDeckReady] = useState(false);
  const [deckTotal, setDeckTotal] = useState(0);     // tổng/đến hạn lấy từ deck summary (toàn deck)
  const [deckDue,   setDeckDue]   = useState(0);
  const [words,     setWords]   = useState([]);
  const [filter,    setFilter]  = useState('all');   // 'all' | 'due'
  const [query,     setQuery]   = useState('');
  const [debounced, setDebounced] = useState('');    // từ khóa đã debounce → gửi server
  const [isLoading, setLoading] = useState(true);
  const [confirmDel, setConfirm] = useState(null);   // { flashcardId, label }
  const [error,     setError]   = useState('');
  const [page,      setPage]    = useState(0);        // 1A: trang hiện tại đã tải
  const [hasMore,   setHasMore] = useState(false);    // còn trang sau không
  const [loadingMore, setLoadingMore] = useState(false);
  const timerRef = useRef(null);
  const sentinelRef = useRef(null);                   // mốc cuối danh sách → kích hoạt tải thêm

  // Tìm deck "Từ cần ôn lại" một lần.
  const loadDeck = useCallback(async () => {
    setLoading(true);
    try {
      const decks = await getFlashcardDecks();
      const deck  = (decks ?? []).find(
        (d) => d.isReviewDeck || d.deckName === REVIEW_DECK_NAME,
      );
      setDeckId(deck?.deckId ?? null);
      setDeckTotal(deck?.totalCards ?? 0);
      setDeckDue(deck?.dueToday ?? 0);
      setError('');
    } catch {
      setError('Không thể tải sổ tay. Thử lại sau.');
    } finally {
      setDeckReady(true);
    }
  }, []);

  // Tải thẻ theo trang — tìm kiếm + lọc "đến hạn" chạy ở server (SPEC-notebook): không bỏ sót thẻ.
  // append=true → nối thêm trang kế (cuộn vô hạn); ngược lại thay danh sách (đổi filter/từ khóa).
  const loadCards = useCallback(async (id, q, dueOnly, pageNum = 0, append = false) => {
    if (!id) { setWords([]); setHasMore(false); setLoading(false); return; }
    if (append) setLoadingMore(true); else setLoading(true);
    try {
      const res = await getFlashcardsByDeck(id, pageNum, PAGE_SIZE, q, dueOnly);
      const content = res.content ?? [];
      setWords((prev) => (append ? [...prev, ...content] : content));
      setHasMore(!res.last);
      setPage(pageNum);
      setError('');
    } catch {
      if (!append) setError('Không thể tải sổ tay. Thử lại sau.');
    } finally {
      if (append) setLoadingMore(false); else setLoading(false);
    }
  }, []);

  // Thử lại: nếu chưa có deck thì tải lại deck, ngược lại tải lại thẻ.
  const reload = useCallback(() => {
    if (deckId) loadCards(deckId, debounced, filter === 'due');
    else loadDeck();
  }, [deckId, debounced, filter, loadCards, loadDeck]);

  useEffect(() => { loadDeck(); }, [loadDeck]);

  // Debounce ô tìm kiếm → cập nhật `debounced`.
  const onQueryChange = useCallback((val) => {
    setQuery(val);
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(val.trim()), 350);
  }, []);
  useEffect(() => () => clearTimeout(timerRef.current), []);

  // Tải/làm mới thẻ (trang 0) khi deck sẵn sàng, từ khóa hoặc bộ lọc thay đổi.
  useEffect(() => {
    if (deckReady) loadCards(deckId, debounced, filter === 'due', 0, false);
  }, [deckReady, deckId, debounced, filter, loadCards]);

  // Cuộn vô hạn: khi mốc cuối danh sách lọt vào khung nhìn thì tải trang kế.
  const loadMore = useCallback(() => {
    if (!deckId || isLoading || loadingMore || !hasMore) return;
    loadCards(deckId, debounced, filter === 'due', page + 1, true);
  }, [deckId, isLoading, loadingMore, hasMore, page, debounced, filter, loadCards]);

  useEffect(() => {
    const node = sentinelRef.current;
    if (!node) return;
    const obs = new IntersectionObserver(
      (entries) => { if (entries[0].isIntersecting) loadMore(); },
      { rootMargin: '200px' },
    );
    obs.observe(node);
    return () => obs.disconnect();
  }, [loadMore]);

  // Đếm tab lấy từ deck summary (toàn deck), độc lập với danh sách đã lọc.
  const visible = words;

  async function handleRemove(card) {
    try {
      await removeFlashcardCard(card.flashcardId);
      setWords((prev) => prev.filter((w) => w.flashcardId !== card.flashcardId));
      setDeckTotal((n) => Math.max(0, n - 1));
      if (card.isDue) setDeckDue((n) => Math.max(0, n - 1));
      setConfirm(null);
      addToast('success', 'Đã gỡ từ khỏi sổ tay.');
    } catch {
      addToast('error', 'Không thể gỡ từ này. Thử lại.');
    }
  }

  function handleStudy() {
    if (!deckId || deckTotal === 0) return;
    navigate(`/vocabulary/flashcard?deckId=${deckId}`);
  }

  return (
    <div className="ntb-page">
      <TopNav activeTab="" />

      <main className="ntb-body">
        {/* Header */}
        <div className="ntb-header">
          <div>
            <h1 className="ntb-title">
              <span aria-hidden="true">📓</span> Sổ Tay — Từ cần ôn lại
            </h1>
            <p className="ntb-subtitle">
              {isLoading ? 'Đang tải…' : `${deckTotal} từ cần ôn · ${deckDue} từ đến hạn hôm nay`}
            </p>
          </div>
          <button
            className="ntb-btn ntb-btn--primary"
            onClick={handleStudy}
            disabled={isLoading || deckTotal === 0}
          >
            ▶ Ôn lại ngay{deckTotal > 0 ? ` (${deckTotal})` : ''}
          </button>
        </div>

        {/* Filter + search */}
        <div className="ntb-controls">
          <div className="ntb-tabs" role="tablist" aria-label="Lọc từ trong sổ">
            <button
              role="tab"
              aria-selected={filter === 'all'}
              className={`ntb-tab${filter === 'all' ? ' ntb-tab--active' : ''}`}
              onClick={() => setFilter('all')}
            >
              Tất cả <span className="ntb-tab-count">{deckTotal}</span>
            </button>
            <button
              role="tab"
              aria-selected={filter === 'due'}
              className={`ntb-tab${filter === 'due' ? ' ntb-tab--active' : ''}`}
              onClick={() => setFilter('due')}
            >
              Đến hạn <span className="ntb-tab-count">{deckDue}</span>
            </button>
          </div>
          <div className="ntb-search">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="2" />
              <path d="M21 21l-4-4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
            <input
              type="search"
              className="ntb-search-input"
              placeholder="Tìm trong sổ..."
              value={query}
              onChange={(e) => onQueryChange(e.target.value)}
              aria-label="Tìm trong sổ tay"
            />
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="ntb-error" role="alert">
            <span>{error}</span>
            <button className="ntb-retry" onClick={reload}>Thử lại</button>
          </div>
        )}

        {/* Loading */}
        {isLoading && !error && (
          <div className="ntb-list">
            {[1, 2, 3, 4].map((i) => <div key={i} className="ntb-skel" aria-hidden="true" />)}
          </div>
        )}

        {/* Empty */}
        {!isLoading && !error && visible.length === 0 && (
          <EmptyState
            title={query
              ? 'Không tìm thấy từ nào'
              : filter === 'due' ? 'Không có từ đến hạn' : 'Sổ tay đang trống'}
            subtitle={query
              ? 'Thử từ khóa khác.'
              : filter === 'due'
                ? 'Bạn đã ôn hết các từ đến hạn hôm nay — quay lại sau nhé! 🌸'
                : 'Trả lời sai khi ôn Flashcard, hoặc lưu từ ở Từ điển — các từ cần ôn sẽ xuất hiện ở đây.'}
            mascotVariant={filter === 'due' && !query ? 'celebrate' : 'thinking'}
            mascotSize={160}
          >
            {!query && filter === 'all' && deckTotal === 0 && (
              <div className="ntb-empty-actions">
                <button className="ntb-btn ntb-btn--primary" onClick={() => navigate('/vocabulary')}>Học Flashcard</button>
                <button className="ntb-btn ntb-btn--ghost" onClick={() => navigate('/dictionary')}>Mở Từ điển</button>
              </div>
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
            {/* Mốc cuộn vô hạn + chỉ báo tải thêm */}
            {hasMore && <div ref={sentinelRef} className="ntb-sentinel" aria-hidden="true" />}
            {loadingMore && [1, 2].map((i) => <div key={`m${i}`} className="ntb-skel" aria-hidden="true" />)}
          </div>
        )}
      </main>

      {/* Confirm gỡ */}
      {confirmDel && (
        <div
          className="ntb-modal-backdrop"
          role="dialog"
          aria-modal="true"
          aria-label="Xác nhận gỡ từ"
          onClick={(e) => { if (e.target === e.currentTarget) setConfirm(null); }}
        >
          <div className="ntb-modal">
            <h2 className="ntb-modal-title">Gỡ khỏi sổ tay</h2>
            <p className="ntb-modal-body">
              Gỡ <strong lang="ja">&quot;{confirmDel.label}&quot;</strong> khỏi sổ tay? Từ này sẽ không còn trong danh sách cần ôn.
            </p>
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
