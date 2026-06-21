import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { ToastContainer, useToast } from '../../components/common/Toast';
import DictResultGroup from '../../components/student/DictResultGroup';
import DictDetailPanel from '../../components/student/DictDetailPanel';
import DictGrammarPanel from '../../components/student/DictGrammarPanel';
import { searchDictionary, saveToNotebook } from '../../api/studentService';
import './Dictionary.css';

/**
 * Từ Điển (SPEC-dictionary.md) — tra cứu kho nội dung published, gom nhóm theo loại.
 * "Lưu vào sổ tay" (chỉ từ vựng) → POST /flashcards/review-deck/add. Bookmark cũ bỏ;
 * thay bằng nút "📓 Sổ tay" điều hướng /notebook.
 * Chip lọc theo loại (Tất cả/Từ vựng/Kanji/Ngữ pháp/Bài học) → param `type` của backend.
 * Vocab & Grammar mở panel chi tiết inline; Kanji/Bài học điều hướng trang chi tiết.
 */
const TYPE_CHIPS = [
  { key: '',           label: 'Tất cả'   },
  { key: 'VOCABULARY', label: 'Từ vựng'  },
  { key: 'KANJI',      label: 'Kanji'    },
  { key: 'GRAMMAR',    label: 'Ngữ pháp' },
  { key: 'LESSON',     label: 'Bài học'  },
];

export default function Dictionary() {
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [query,     setQuery]     = useState('');
  const [debounced, setDebounced] = useState('');
  const [activeType, setActiveType] = useState('');   // '' = tất cả loại
  const [results,   setResults]   = useState(null);   // null = chưa tìm
  const [isLoading, setLoading]   = useState(false);
  const [selected,  setSelected]  = useState(null);   // { kind: 'vocabulary'|'grammar', item }
  const [savedIds,  setSavedIds]  = useState(new Set());
  const [savingId,  setSavingId]  = useState(null);
  const [error,     setError]     = useState('');
  const timerRef = useRef(null);

  const onQueryChange = useCallback((val) => {
    setQuery(val);
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(val.trim()), 350);
  }, []);

  useEffect(() => () => clearTimeout(timerRef.current), []);

  useEffect(() => {
    if (!debounced) { setResults(null); setError(''); return; }
    let active = true;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const data = await searchDictionary(debounced, undefined, activeType || undefined);
        if (active) setResults(data ?? {});
      } catch {
        if (active) setError('Không thể tìm kiếm. Thử lại sau.');
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => { active = false; };
  }, [debounced, activeType]);

  async function handleSave(id) {
    const key = `vocabulary:${id}`;
    if (savedIds.has(key)) return;
    setSavingId(key);
    try {
      await saveToNotebook('VOCABULARY', id);
      setSavedIds((prev) => new Set(prev).add(key));
      addToast('success', 'Đã lưu vào Sổ tay.');
    } catch (err) {
      if (err?.response?.status === 409) {
        setSavedIds((prev) => new Set(prev).add(key));
        addToast('info', 'Từ này đã có trong sổ tay.');
        return;
      }
      addToast('error', 'Không thể lưu. Thử lại.');
    } finally {
      setSavingId(null);
    }
  }

  const total = results
    ? (results.vocabulary?.length ?? 0) + (results.kanji?.length ?? 0) +
      (results.grammar?.length ?? 0) + (results.lessons?.length ?? 0)
    : 0;

  return (
    <div className="dct-page">
      <TopNav activeTab="dictionary" />
      <main className="dct-body">
        {/* Header */}
        <div className="dct-header">
          <div>
            <h1 className="dct-title">Từ Điển</h1>
            <p className="dct-subtitle">Tra cứu từ vựng, Kanji, ngữ pháp</p>
          </div>
          <button className="dct-notebook-btn" onClick={() => navigate('/notebook')}>
            📓 Sổ tay
          </button>
        </div>

        {/* Search box */}
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
            onChange={(e) => onQueryChange(e.target.value)}
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

        {/* Chip lọc theo loại */}
        <div className="dct-filter-chips" role="tablist" aria-label="Lọc theo loại">
          {TYPE_CHIPS.map((c) => (
            <button
              key={c.key || 'all'}
              role="tab"
              aria-selected={activeType === c.key}
              className={`dct-chip${activeType === c.key ? ' dct-chip--active' : ''}`}
              onClick={() => { setActiveType(c.key); setSelected(null); }}
            >
              {c.label}
            </button>
          ))}
        </div>

        {/* Chi tiết một mục (vocab/grammar) chồng lên */}
        {selected?.kind === 'vocabulary' && (
          <DictDetailPanel
            item={selected.item}
            isSaved={savedIds.has(`vocabulary:${selected.item.id}`)}
            isSaving={savingId === `vocabulary:${selected.item.id}`}
            onSave={() => handleSave(selected.item.id)}
            onBack={() => setSelected(null)}
          />
        )}
        {selected?.kind === 'grammar' && (
          <DictGrammarPanel item={selected.item} onBack={() => setSelected(null)} />
        )}

        {/* Trạng thái body */}
        {!selected && (
          <>
            {error && <div className="dct-error" role="alert">{error}</div>}

            {!debounced && !error && (
              <EmptyState
                title="Gõ để tra cứu"
                subtitle="Tìm trong kho từ vựng, Kanji, ngữ pháp của SakuJi."
                mascotVariant="thinking"
                mascotSize={140}
              />
            )}

            {isLoading && (
              <div className="dct-loading" role="status" aria-label="Đang tìm kiếm">
                <div className="dct-spinner" />
              </div>
            )}

            {!isLoading && debounced && results && total === 0 && !error && (
              <EmptyState
                title={`Không tìm thấy "${debounced}"`}
                subtitle="Thử từ khóa khác (chữ Nhật, Romaji hoặc tiếng Việt)."
                mascotVariant="idle"
                mascotSize={140}
              />
            )}

            {!isLoading && results && total > 0 && (
              <div className="dct-results">
                <DictResultGroup
                  title="Từ vựng" items={results.vocabulary} type="vocabulary"
                  savedIds={savedIds} savingId={savingId}
                  onOpen={(id, raw) => setSelected({ kind: 'vocabulary', item: raw })}
                  onSave={(id) => handleSave(id)}
                  canSave
                />
                <DictResultGroup
                  title="Kanji" items={results.kanji} type="kanji"
                  onOpen={(id) => navigate(`/kanji/${id}`)}
                />
                <DictResultGroup
                  title="Ngữ pháp" items={results.grammar} type="grammar"
                  onOpen={(id, raw) => setSelected({ kind: 'grammar', item: raw })}
                />
                <DictResultGroup
                  title="Bài học" items={results.lessons} type="lesson"
                  onOpen={(id) => navigate(`/lessons/${id}`)}
                />
              </div>
            )}
          </>
        )}
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
