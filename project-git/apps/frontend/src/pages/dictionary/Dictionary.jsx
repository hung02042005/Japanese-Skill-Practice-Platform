import { useState, useMemo, useCallback, useRef } from 'react';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import './Dictionary.css';

const MOCK_VOCAB = [
  { id: 1, word: '食べる',   furigana: 'たべる',       meaning: 'Ăn',              jlptLevel: 'N5' },
  { id: 2, word: '飲む',    furigana: 'のむ',         meaning: 'Uống',            jlptLevel: 'N5' },
  { id: 3, word: '勉強する', furigana: 'べんきょうする', meaning: 'Học bài',         jlptLevel: 'N5' },
  { id: 4, word: '電話する', furigana: 'でんわする',   meaning: 'Gọi điện thoại',   jlptLevel: 'N5' },
  { id: 5, word: '旅行',    furigana: 'りょこう',      meaning: 'Du lịch',         jlptLevel: 'N4' },
  { id: 6, word: '経験',    furigana: 'けいけん',      meaning: 'Kinh nghiệm',     jlptLevel: 'N3' },
  { id: 7, word: '努力',    furigana: 'どりょく',      meaning: 'Nỗ lực, cố gắng', jlptLevel: 'N3' },
];

const MOCK_KANJI = [
  { id: 1, characterValue: '食', meaning: 'Thực (ăn)',  onyomi: 'ショク・ジキ', kunyomi: 'た.べる',   jlptLevel: 'N5' },
  { id: 2, characterValue: '水', meaning: 'Thủy (nước)', onyomi: 'スイ',       kunyomi: 'みず',      jlptLevel: 'N5' },
  { id: 3, characterValue: '山', meaning: 'Sơn (núi)',  onyomi: 'サン',        kunyomi: 'やま',      jlptLevel: 'N5' },
  { id: 4, characterValue: '電', meaning: 'Điện',       onyomi: 'デン',        kunyomi: 'いなずま',   jlptLevel: 'N4' },
  { id: 5, characterValue: '旅', meaning: 'Lữ (du lịch)', onyomi: 'リョ',      kunyomi: 'たび',      jlptLevel: 'N4' },
];

const MOCK_GRAMMAR = [
  { id: 1, structure: '〜たい',        meaning: 'Muốn làm gì',                   jlptLevel: 'N5' },
  { id: 2, structure: '〜ている',      meaning: 'Đang làm / trạng thái',          jlptLevel: 'N4' },
  { id: 3, structure: '〜に違いない',   meaning: 'Chắc chắn là...',               jlptLevel: 'N2' },
];

const MOCK_LESSONS = [
  { id: 1, title: 'Bài 1: Giới thiệu bản thân',   jlptLevel: 'N5' },
  { id: 2, title: 'Bài 2: Đặt đồ ăn tại nhà hàng', jlptLevel: 'N5' },
  { id: 3, title: 'Bài 3: Mua sắm và hỏi giá',    jlptLevel: 'N4' },
];

const BK_TYPES = [
  { key: 'all',     label: 'Tất cả' },
  { key: 'vocab',   label: 'Từ vựng' },
  { key: 'kanji',   label: 'Kanji' },
  { key: 'grammar', label: 'Ngữ pháp' },
  { key: 'lesson',  label: 'Bài học' },
];

const INIT_BOOKMARKS = [
  { bkId: 'v-2', type: 'vocab',   word: '飲む',         furigana: 'のむ',    meaning: 'Uống',            jlptLevel: 'N5', note: 'Động từ nhóm 1' },
  { bkId: 'k-1', type: 'kanji',   characterValue: '食', meaning: 'Thực (ăn)',                            jlptLevel: 'N5', note: '' },
  { bkId: 'g-1', type: 'grammar', structure: '〜たい',   meaning: 'Muốn làm gì',                          jlptLevel: 'N5', note: 'Hay dùng trong hội thoại' },
];

function BookmarkIcon({ filled }) {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill={filled ? 'currentColor' : 'none'} aria-hidden="true">
      <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
    </svg>
  );
}

export default function Dictionary() {
  const [tab,       setTab]       = useState('search');
  const [query,     setQuery]     = useState('');
  const [debounced, setDebounced] = useState('');
  const [bookmarks, setBookmarks] = useState(INIT_BOOKMARKS);
  const [bkFilter,  setBkFilter]  = useState('all');
  const timerRef = useRef(null);

  const handleQueryChange = useCallback((val) => {
    setQuery(val);
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(val.trim().toLowerCase()), 350);
  }, []);

  const results = useMemo(() => {
    if (!debounced) return null;
    const q = debounced;
    return {
      vocab:   MOCK_VOCAB.filter((v) => v.word.includes(q) || v.furigana.includes(q) || v.meaning.toLowerCase().includes(q)),
      kanji:   MOCK_KANJI.filter((k) => k.characterValue.includes(q) || k.meaning.toLowerCase().includes(q) || k.onyomi.toLowerCase().includes(q) || k.kunyomi.includes(q)),
      grammar: MOCK_GRAMMAR.filter((g) => g.structure.includes(q) || g.meaning.toLowerCase().includes(q)),
      lessons: MOCK_LESSONS.filter((l) => l.title.toLowerCase().includes(q)),
    };
  }, [debounced]);

  const totalResults = results
    ? results.vocab.length + results.kanji.length + results.grammar.length + results.lessons.length
    : 0;

  const filteredBookmarks = useMemo(
    () => bkFilter === 'all' ? bookmarks : bookmarks.filter((b) => b.type === bkFilter),
    [bookmarks, bkFilter]
  );

  function isBk(type, id) {
    return bookmarks.some((b) => b.bkId === `${type[0]}-${id}`);
  }

  function toggleBk(item, type) {
    const bkId = `${type[0]}-${item.id}`;
    setBookmarks((prev) => {
      if (prev.some((b) => b.bkId === bkId)) return prev.filter((b) => b.bkId !== bkId);
      const base = { bkId, type, jlptLevel: item.jlptLevel, note: '' };
      if (type === 'vocab')   return [...prev, { ...base, word: item.word, furigana: item.furigana, meaning: item.meaning }];
      if (type === 'kanji')   return [...prev, { ...base, characterValue: item.characterValue, meaning: item.meaning }];
      if (type === 'grammar') return [...prev, { ...base, structure: item.structure, meaning: item.meaning }];
      return [...prev, { ...base, title: item.title }];
    });
  }

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
                value={query}
                onChange={(e) => handleQueryChange(e.target.value)}
                autoFocus
                aria-label="Tìm kiếm từ điển"
              />
              {query && (
                <button
                  className="dct-clear-btn"
                  onClick={() => { setQuery(''); setDebounced(''); }}
                  aria-label="Xóa tìm kiếm"
                >
                  ✕
                </button>
              )}
            </div>

            {!debounced && (
              <div className="dct-empty-prompt" aria-hidden="true">
                <svg width="52" height="52" viewBox="0 0 24 24" fill="none" style={{ color: 'var(--color-primary-light)' }}>
                  <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="1.5" />
                  <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                </svg>
                <p className="dct-ep-text">Nhập từ khóa để tra cứu</p>
                <p className="dct-ep-hint">Hỗ trợ tiếng Nhật, Romaji và tiếng Việt</p>
              </div>
            )}

            {debounced && totalResults === 0 && (
              <EmptyState
                title={`Không tìm thấy kết quả cho "${debounced}"`}
                subtitle="Thử từ khóa khác hoặc kiểm tra lại chính tả."
                mascotVariant="thinking"
                mascotSize={100}
              />
            )}

            {debounced && totalResults > 0 && (
              <div className="dct-results">
                {results.vocab.length > 0 && (
                  <section className="dct-section">
                    <h2 className="dct-section-title">
                      Từ vựng <span className="dct-cnt">{results.vocab.length}</span>
                    </h2>
                    {results.vocab.map((v) => (
                      <div key={v.id} className="dct-result-row">
                        <div className="dct-result-info">
                          <JlptBadge level={v.jlptLevel} />
                          <span className="dct-result-word">{v.word}</span>
                          <span className="dct-result-furi">{v.furigana}</span>
                          <span className="dct-result-meaning">{v.meaning}</span>
                        </div>
                        <button
                          className={`dct-bk-btn${isBk('vocab', v.id) ? ' dct-bk-btn--on' : ''}`}
                          onClick={() => toggleBk(v, 'vocab')}
                          aria-label={isBk('vocab', v.id) ? 'Gỡ bookmark' : 'Lưu bookmark'}
                        >
                          <BookmarkIcon filled={isBk('vocab', v.id)} />
                        </button>
                      </div>
                    ))}
                  </section>
                )}

                {results.kanji.length > 0 && (
                  <section className="dct-section">
                    <h2 className="dct-section-title">
                      Kanji <span className="dct-cnt">{results.kanji.length}</span>
                    </h2>
                    {results.kanji.map((k) => (
                      <div key={k.id} className="dct-result-row">
                        <div className="dct-result-info">
                          <JlptBadge level={k.jlptLevel} />
                          <span className="dct-result-kanji">{k.characterValue}</span>
                          <div className="dct-result-kanji-detail">
                            <span className="dct-result-meaning">{k.meaning}</span>
                            <span className="dct-result-readings">音: {k.onyomi} · 訓: {k.kunyomi}</span>
                          </div>
                        </div>
                        <button
                          className={`dct-bk-btn${isBk('kanji', k.id) ? ' dct-bk-btn--on' : ''}`}
                          onClick={() => toggleBk(k, 'kanji')}
                          aria-label={isBk('kanji', k.id) ? 'Gỡ bookmark' : 'Lưu bookmark'}
                        >
                          <BookmarkIcon filled={isBk('kanji', k.id)} />
                        </button>
                      </div>
                    ))}
                  </section>
                )}

                {results.grammar.length > 0 && (
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
                          className={`dct-bk-btn${isBk('grammar', g.id) ? ' dct-bk-btn--on' : ''}`}
                          onClick={() => toggleBk(g, 'grammar')}
                          aria-label={isBk('grammar', g.id) ? 'Gỡ bookmark' : 'Lưu bookmark'}
                        >
                          <BookmarkIcon filled={isBk('grammar', g.id)} />
                        </button>
                      </div>
                    ))}
                  </section>
                )}

                {results.lessons.length > 0 && (
                  <section className="dct-section">
                    <h2 className="dct-section-title">
                      Bài học <span className="dct-cnt">{results.lessons.length}</span>
                    </h2>
                    {results.lessons.map((l) => (
                      <div key={l.id} className="dct-result-row">
                        <div className="dct-result-info">
                          <JlptBadge level={l.jlptLevel} />
                          <span className="dct-result-word">{l.title}</span>
                        </div>
                        <button
                          className={`dct-bk-btn${isBk('lesson', l.id) ? ' dct-bk-btn--on' : ''}`}
                          onClick={() => toggleBk(l, 'lesson')}
                          aria-label={isBk('lesson', l.id) ? 'Gỡ bookmark' : 'Lưu bookmark'}
                        >
                          <BookmarkIcon filled={isBk('lesson', l.id)} />
                        </button>
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
                const count = t.key === 'all' ? bookmarks.length : bookmarks.filter((b) => b.type === t.key).length;
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

            {filteredBookmarks.length === 0 ? (
              <EmptyState
                title="Chưa có bookmark nào"
                subtitle="Tra từ điển và nhấn 🔖 để lưu những từ cần nhớ."
                mascotVariant="idle"
                mascotSize={120}
              />
            ) : (
              <div className="dct-bk-list">
                {filteredBookmarks.map((b) => (
                  <div key={b.bkId} className="dct-bk-item">
                    <div className="dct-bk-item-info">
                      <JlptBadge level={b.jlptLevel} />
                      <div>
                        <p className="dct-bk-item-title">
                          {b.word || b.characterValue || b.structure || b.title}
                        </p>
                        <p className="dct-bk-item-sub">
                          {b.furigana || b.meaning || ''}
                        </p>
                        {b.note && <p className="dct-bk-item-note">📝 {b.note}</p>}
                      </div>
                    </div>
                    <span className="dct-bk-type">{b.type}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
