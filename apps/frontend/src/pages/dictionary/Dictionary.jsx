import { useState, useCallback, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { searchDictionaryThunk, setQuery, clearResults } from '../../store/slices/dictionarySlice';
import { fetchBookmarksThunk, addBookmarkThunk, removeBookmarkThunk } from '../../store/slices/bookmarkSlice';
import { useEffect } from 'react';
import './Dictionary.css';

const BK_TYPES = [
  { key: 'all',        label: 'Tất cả' },
  { key: 'VOCABULARY', label: 'Từ vựng' },
  { key: 'KANJI',      label: 'Kanji' },
  { key: 'GRAMMAR',    label: 'Ngữ pháp' },
];

function BookmarkIcon({ filled }) {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill={filled ? 'currentColor' : 'none'} aria-hidden="true">
      <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
    </svg>
  );
}

export default function Dictionary() {
  const dispatch = useDispatch();
  const { toasts, addToast, removeToast } = useToast();

  const { results, query, status: searchStatus } = useSelector((s) => s.dictionary);
  const { items: bookmarks, status: bkStatus, addStatus, removeStatus } = useSelector((s) => s.bookmark);

  const [tab,      setTab]      = useState('search');
  const [bkFilter, setBkFilter] = useState('all');
  const [inputVal, setInputVal] = useState('');
  const timerRef = useRef(null);

  // Load bookmarks on mount
  useEffect(() => {
    if (bkStatus === 'idle') dispatch(fetchBookmarksThunk({}));
  }, [dispatch, bkStatus]);

  const handleQueryChange = useCallback((val) => {
    setInputVal(val);
    dispatch(setQuery(val));
    clearTimeout(timerRef.current);
    if (!val.trim()) { dispatch(clearResults()); return; }
    timerRef.current = setTimeout(() => {
      dispatch(searchDictionaryThunk({ q: val.trim() }));
    }, 350);
  }, [dispatch]);

  function isBookmarked(contentType, id) {
    return bookmarks.some((b) => b.contentType === contentType && b.contentId === id);
  }

  async function toggleBookmark(contentType, contentId) {
    if (isBookmarked(contentType, contentId)) {
      const res = await dispatch(removeBookmarkThunk({ contentType, contentId }));
      if (removeBookmarkThunk.rejected.match(res)) addToast('error', res.payload);
      else addToast('success', 'Đã gỡ bookmark.');
    } else {
      const res = await dispatch(addBookmarkThunk({ contentType, contentId }));
      if (addBookmarkThunk.rejected.match(res)) addToast('error', res.payload);
      else addToast('success', 'Đã lưu bookmark!');
    }
  }

  const filteredBookmarks =
    bkFilter === 'all' ? bookmarks : bookmarks.filter((b) => b.contentType === bkFilter);

  const totalResults = results
    ? (results.vocabulary?.length ?? 0) + (results.kanji?.length ?? 0) + (results.grammar?.length ?? 0) + (results.lessons?.length ?? 0)
    : 0;

  return (
    <div className="dct-page">
      <TopNav activeTab="dictionary" />
      <main className="dct-body">
        <div className="dct-header">
          <h1 className="dct-title">Từ Điển</h1>
          <p className="dct-subtitle">Tra cứu từ vựng, Kanji, ngữ pháp và lưu bookmark</p>
        </div>

        <div className="dct-tabs" role="tablist">
          <button
            role="tab"
            aria-selected={tab === 'search'}
            className={`dct-tab${tab === 'search' ? ' dct-tab--active' : ''}`}
            onClick={() => setTab('search')}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2" />
              <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
            Tra từ điển
          </button>
          <button
            role="tab"
            aria-selected={tab === 'bookmarks'}
            className={`dct-tab${tab === 'bookmarks' ? ' dct-tab--active' : ''}`}
            onClick={() => setTab('bookmarks')}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
            </svg>
            Bookmarks
            {bookmarks.length > 0 && <span className="dct-tab-badge">{bookmarks.length}</span>}
          </button>
        </div>

        {/* ── Search tab ── */}
        {tab === 'search' && (
          <div className="dct-search-panel">
            <div className="dct-search-wrap">
              <svg className="dct-si" width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2" />
                <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
              </svg>
              <input
                className="dct-search-input"
                type="search"
                placeholder="Nhập từ vựng, Kanji, ngữ pháp… (JP / Romaji / tiếng Việt)"
                value={inputVal}
                onChange={(e) => handleQueryChange(e.target.value)}
                autoFocus
                aria-label="Tìm kiếm từ điển"
              />
              {inputVal && (
                <button
                  className="dct-clear-btn"
                  onClick={() => { setInputVal(''); dispatch(clearResults()); }}
                  aria-label="Xóa tìm kiếm"
                >
                  ✕
                </button>
              )}
            </div>

            {!query && (
              <div className="dct-empty-prompt" aria-hidden="true">
                <svg width="52" height="52" viewBox="0 0 24 24" fill="none" style={{ color: 'var(--color-primary-light)' }}>
                  <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="1.5" />
                  <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                </svg>
                <p className="dct-ep-text">Nhập từ khóa để tra cứu</p>
                <p className="dct-ep-hint">Hỗ trợ tiếng Nhật, Romaji và tiếng Việt</p>
              </div>
            )}

            {searchStatus === 'loading' && query && (
              <div className="dct-empty-prompt" aria-live="polite">
                <div className="fls-spinner" aria-hidden="true" />
                <p className="dct-ep-text">Đang tìm kiếm...</p>
              </div>
            )}

            {searchStatus === 'failed' && (
              <div className="fls-error" role="alert">Không thể tìm kiếm. Thử lại sau.</div>
            )}

            {searchStatus === 'succeeded' && totalResults === 0 && (
              <EmptyState
                title={`Không tìm thấy kết quả cho "${results?.keyword ?? query}"`}
                subtitle="Thử từ khóa khác hoặc kiểm tra lại chính tả."
                mascotVariant="thinking"
                mascotSize={100}
              />
            )}

            {searchStatus === 'succeeded' && totalResults > 0 && (
              <div className="dct-results">
                {results.vocabulary?.length > 0 && (
                  <section className="dct-section">
                    <h2 className="dct-section-title">
                      Từ vựng <span className="dct-cnt">{results.vocabulary.length}</span>
                    </h2>
                    {results.vocabulary.map((v) => (
                      <div key={v.id} className="dct-result-row">
                        <div className="dct-result-info">
                          <JlptBadge level={v.jlptLevel} />
                          <span className="dct-result-word">{v.word}</span>
                          <span className="dct-result-furi">{v.furigana}</span>
                          <span className="dct-result-meaning">{v.meaning}</span>
                        </div>
                        <button
                          className={`dct-bk-btn${isBookmarked('VOCABULARY', v.id) ? ' dct-bk-btn--on' : ''}`}
                          onClick={() => toggleBookmark('VOCABULARY', v.id)}
                          disabled={addStatus === 'loading' || removeStatus === 'loading'}
                          aria-label={isBookmarked('VOCABULARY', v.id) ? 'Gỡ bookmark' : 'Lưu bookmark'}
                        >
                          <BookmarkIcon filled={isBookmarked('VOCABULARY', v.id)} />
                        </button>
                      </div>
                    ))}
                  </section>
                )}

                {results.kanji?.length > 0 && (
                  <section className="dct-section">
                    <h2 className="dct-section-title">
                      Kanji <span className="dct-cnt">{results.kanji.length}</span>
                    </h2>
                    {results.kanji.map((k) => (
                      <div key={k.id} className="dct-result-row">
                        <div className="dct-result-info">
                          <JlptBadge level={k.jlptLevel} />
                          <span className="dct-result-kanji">{k.character}</span>
                          <div className="dct-result-kanji-detail">
                            <span className="dct-result-meaning">{k.meaning}</span>
                            <span className="dct-result-readings">音: {k.onyomi} · 訓: {k.kunyomi}</span>
                          </div>
                        </div>
                        <button
                          className={`dct-bk-btn${isBookmarked('KANJI', k.id) ? ' dct-bk-btn--on' : ''}`}
                          onClick={() => toggleBookmark('KANJI', k.id)}
                          disabled={addStatus === 'loading' || removeStatus === 'loading'}
                          aria-label={isBookmarked('KANJI', k.id) ? 'Gỡ bookmark' : 'Lưu bookmark'}
                        >
                          <BookmarkIcon filled={isBookmarked('KANJI', k.id)} />
                        </button>
                      </div>
                    ))}
                  </section>
                )}

                {results.grammar?.length > 0 && (
                  <section className="dct-section">
                    <h2 className="dct-section-title">
                      Ngữ pháp <span className="dct-cnt">{results.grammar.length}</span>
                    </h2>
                    {results.grammar.map((g) => (
                      <div key={g.id} className="dct-result-row">
                        <div className="dct-result-info">
                          <JlptBadge level={g.jlptLevel} />
                          <span className="dct-result-word">{g.structure}</span>
                          <span className="dct-result-meaning">{g.meaning}</span>
                        </div>
                        <button
                          className={`dct-bk-btn${isBookmarked('GRAMMAR', g.id) ? ' dct-bk-btn--on' : ''}`}
                          onClick={() => toggleBookmark('GRAMMAR', g.id)}
                          disabled={addStatus === 'loading' || removeStatus === 'loading'}
                          aria-label={isBookmarked('GRAMMAR', g.id) ? 'Gỡ bookmark' : 'Lưu bookmark'}
                        >
                          <BookmarkIcon filled={isBookmarked('GRAMMAR', g.id)} />
                        </button>
                      </div>
                    ))}
                  </section>
                )}

                {results.lessons?.length > 0 && (
                  <section className="dct-section">
                    <h2 className="dct-section-title">
                      Bài học <span className="dct-cnt">{results.lessons.length}</span>
                    </h2>
                    {results.lessons.map((l) => (
                      <div key={l.id} className="dct-result-row">
                        <div className="dct-result-info">
                          <JlptBadge level={l.jlptLevel} />
                          <span className="dct-result-word">{l.title}</span>
                          <span className="dct-result-furi">{l.lessonType}</span>
                        </div>
                      </div>
                    ))}
                  </section>
                )}
              </div>
            )}
          </div>
        )}

        {/* ── Bookmarks tab ── */}
        {tab === 'bookmarks' && (
          <div className="dct-bk-panel">
            <div className="dct-bk-filters">
              {BK_TYPES.map((t) => {
                const count = t.key === 'all'
                  ? bookmarks.length
                  : bookmarks.filter((b) => b.contentType === t.key).length;
                return (
                  <button
                    key={t.key}
                    className={`dct-bkf-btn${bkFilter === t.key ? ' dct-bkf-btn--active' : ''}`}
                    onClick={() => setBkFilter(t.key)}
                  >
                    {t.label}
                    <span className="dct-bkf-cnt">{count}</span>
                  </button>
                );
              })}
            </div>

            {bkStatus === 'loading' && (
              <div className="dct-empty-prompt" aria-live="polite">
                <div className="fls-spinner" aria-hidden="true" />
                <p className="dct-ep-text">Đang tải bookmark...</p>
              </div>
            )}

            {bkStatus !== 'loading' && filteredBookmarks.length === 0 ? (
              <EmptyState
                title="Chưa có bookmark nào"
                subtitle="Tra từ điển và nhấn 🔖 để lưu những từ cần nhớ."
                mascotVariant="idle"
                mascotSize={120}
              />
            ) : (
              <div className="dct-bk-list">
                {filteredBookmarks.map((b) => (
                  <div key={`${b.contentType}-${b.contentId}`} className="dct-bk-item">
                    <div className="dct-bk-item-info">
                      {b.jlptLevel && <JlptBadge level={b.jlptLevel} />}
                      <div>
                        <p className="dct-bk-item-title">{b.displayText}</p>
                        {b.note && <p className="dct-bk-item-note">📝 {b.note}</p>}
                      </div>
                    </div>
                    <div className="dct-bk-item-actions">
                      <span className="dct-bk-type">{b.contentType}</span>
                      <button
                        className="dct-bk-btn dct-bk-btn--on"
                        onClick={() => toggleBookmark(b.contentType, b.contentId)}
                        disabled={removeStatus === 'loading'}
                        aria-label="Gỡ bookmark"
                      >
                        <BookmarkIcon filled />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
