<<<<<<< Updated upstream
import { useState, useEffect, useCallback, useMemo } from 'react';
=======
import { useState, useEffect, useMemo } from 'react';
>>>>>>> Stashed changes
import { useSearchParams } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
<<<<<<< Updated upstream
import { Pagination } from '../../components/common/Pagination';
import { getGrammarList, getGrammarDetail, markProgress } from '../../api/studentService';
import './Grammar.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function Grammar() {
  const [searchParams] = useSearchParams();
  const { user } = useAppSelector((s) => s.auth);

  const [level,      setLevel]      = useState(searchParams.get('level') ?? user?.jlptLevel ?? 'N5');
  const [search,     setSearch]     = useState('');
  const [grammar,    setGrammar]    = useState([]);
  const [isLoading,  setLoading]    = useState(true);
  const [error,      setError]      = useState('');
  const [page,       setPage]       = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  const [expandedId,    setExpandedId]    = useState(null);
  const [detailMap,     setDetailMap]     = useState({});
  const [detailLoading, setDetailLoading] = useState(false);
  const [markingId,     setMarkingId]     = useState(null);

  const fetchGrammar = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getGrammarList({ level, page: page - 1, size: 20 });
      setGrammar(res.content ?? []);
      setTotalPages(res.totalPages ?? 1);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải danh sách ngữ pháp.');
    } finally {
      setLoading(false);
    }
  }, [level, page]);

  useEffect(() => { fetchGrammar(); }, [fetchGrammar]);
  useEffect(() => { setPage(1); }, [level]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return grammar;
    return grammar.filter(
      (g) =>
        g.structure.toLowerCase().includes(q) ||
        g.meaning.toLowerCase().includes(q) ||
        (g.formula ?? '').toLowerCase().includes(q)
    );
  }, [grammar, search]);
=======
import { getGrammarList, getGrammarDetail } from '../../api/studentService';
import './Grammar.css';

const LEVELS = ['All', 'N5', 'N4', 'N3', 'N2', 'N1'];
const JLPT_LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function Grammar() {
  const [searchParams] = useSearchParams();
  const [level,    setLevel]    = useState(searchParams.get('level') ?? 'All');
  const [search,   setSearch]   = useState('');
  const [expanded, setExpanded] = useState(null);
  const [grammarList, setGrammarList] = useState([]);
  const [loading, setLoading]         = useState(false);
  const [error, setError]             = useState(null);
  // Cache detail data keyed by grammarId
  const [detailCache, setDetailCache] = useState({});
  const [loadingDetail, setLoadingDetail] = useState(false);

  // Fetch list whenever level changes
  useEffect(() => {
    let cancelled = false;
    async function fetchGrammar() {
      setLoading(true);
      setError(null);
      setExpanded(null);
      try {
        let items = [];
        if (level === 'All') {
          // Backend requires a specific level; call all 5 in parallel
          const results = await Promise.allSettled(
            JLPT_LEVELS.map((lvl) => getGrammarList({ level: lvl, size: 50 }))
          );
          for (const r of results) {
            if (r.status === 'fulfilled' && r.value?.content) {
              items = items.concat(r.value.content);
            }
          }
        } else {
          const data = await getGrammarList({ level, size: 50 });
          items = data?.content ?? [];
        }
        if (!cancelled) setGrammarList(items);
      } catch (err) {
        if (!cancelled) setError('Không thể tải danh sách ngữ pháp. Vui lòng thử lại.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    fetchGrammar();
    return () => { cancelled = true; };
  }, [level]);

  // Fetch detail when a card is expanded
  useEffect(() => {
    if (expanded == null) return;
    if (detailCache[expanded]) return; // already cached
    let cancelled = false;
    async function fetchDetail() {
      setLoadingDetail(true);
      try {
        const data = await getGrammarDetail(expanded);
        if (!cancelled) setDetailCache((prev) => ({ ...prev, [expanded]: data }));
      } catch {
        // detail failed — keep expanded open but show no extra info
      } finally {
        if (!cancelled) setLoadingDetail(false);
      }
    }
    fetchDetail();
    return () => { cancelled = true; };
  }, [expanded]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return grammarList;
    return grammarList.filter(
      (g) =>
        (g.structure ?? '').toLowerCase().includes(q) ||
        (g.meaning   ?? '').toLowerCase().includes(q) ||
        (g.title     ?? '').toLowerCase().includes(q)
    );
  }, [grammarList, search]);
>>>>>>> Stashed changes

  async function toggle(g) {
    if (expandedId === g.grammarId) {
      setExpandedId(null);
      return;
    }
    setExpandedId(g.grammarId);
    if (detailMap[g.grammarId]) return;
    setDetailLoading(true);
    try {
      const detail = await getGrammarDetail(g.grammarId);
      setDetailMap((prev) => ({ ...prev, [g.grammarId]: detail }));
    } catch {
      setDetailMap((prev) => ({ ...prev, [g.grammarId]: null }));
    } finally {
      setDetailLoading(false);
    }
  }

  async function handleMarkComplete(g) {
    if (g.isCompleted || markingId === g.grammarId) return;
    setMarkingId(g.grammarId);
    try {
      await markProgress('grammar', g.grammarId, 'completed', 100);
      setGrammar((prev) =>
        prev.map((item) => (item.grammarId === g.grammarId ? { ...item, isCompleted: true } : item))
      );
    } finally {
      setMarkingId(null);
    }
  }

  return (
    <div className="grm-page">
      <TopNav activeTab="grammar" />
      <main className="grm-body">
        <div className="grm-header">
          <h1 className="grm-title">Ngữ Pháp</h1>
          <p className="grm-subtitle">Tổng hợp cấu trúc ngữ pháp theo cấp độ JLPT</p>
        </div>

        <div className="grm-controls">
          <div className="grm-levels">
            {LEVELS.map((l) => (
              <button
                key={l}
                className={`grm-lvl-btn${level === l ? ' grm-lvl-btn--active' : ''}`}
                onClick={() => setLevel(l)}
              >
                {l}
              </button>
            ))}
          </div>
          <div className="grm-search-wrap">
            <svg className="grm-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2" />
              <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            </svg>
            <input
              className="grm-search"
              type="search"
              placeholder="Tìm cấu trúc, ý nghĩa..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

<<<<<<< Updated upstream
        {error && (
          <div className="grm-error" role="alert">
            <span>{error}</span>
            <button className="grm-retry-btn" onClick={fetchGrammar}>Thử lại</button>
          </div>
        )}

        {!isLoading && <p className="grm-count-label">{filtered.length} cấu trúc</p>}

        {isLoading ? (
          <div className="grm-list" aria-hidden="true">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="grm-card grm-card--skel" />
            ))}
          </div>
        ) : filtered.length === 0 ? (
=======
        {loading ? (
          <p className="grm-count-label">Đang tải...</p>
        ) : error ? (
          <p className="grm-count-label" style={{ color: 'var(--color-error, #f87171)' }}>{error}</p>
        ) : (
          <p className="grm-count-label">{filtered.length} cấu trúc</p>
        )}

        {!loading && !error && filtered.length === 0 ? (
>>>>>>> Stashed changes
          <EmptyState
            title={`Chưa có cấu trúc ${level}`}
            subtitle="Thử tìm kiếm với từ khóa khác hoặc đổi cấp độ."
            mascotVariant="thinking"
            mascotSize={120}
          />
        ) : (
          <div className="grm-list">
            {filtered.map((g) => {
<<<<<<< Updated upstream
              const detail = detailMap[g.grammarId];
              const isOpen = expandedId === g.grammarId;
              return (
                <div
                  key={g.grammarId}
                  className={`grm-card${isOpen ? ' grm-card--open' : ''}`}
                  onClick={() => toggle(g)}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => e.key === 'Enter' && toggle(g)}
                  aria-expanded={isOpen}
=======
              const detail = detailCache[g.grammarId];
              return (
                <div
                  key={g.grammarId}
                  className={`grm-card${expanded === g.grammarId ? ' grm-card--open' : ''}`}
                  onClick={() => toggle(g.grammarId)}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => e.key === 'Enter' && toggle(g.grammarId)}
                  aria-expanded={expanded === g.grammarId}
>>>>>>> Stashed changes
                >
                  <div className="grm-card-main">
                    <div className="grm-card-left">
                      <JlptBadge level={g.jlptLevel} />
                      <span className="grm-structure">{g.structure}</span>
<<<<<<< Updated upstream
                      {g.isCompleted && <span className="grm-done-tick" aria-hidden="true">✓</span>}
=======
>>>>>>> Stashed changes
                    </div>
                    <div className="grm-card-right">
                      <span className="grm-meaning">{g.meaning}</span>
                      <svg
<<<<<<< Updated upstream
                        className={`grm-chevron${isOpen ? ' grm-chevron--up' : ''}`}
=======
                        className={`grm-chevron${expanded === g.grammarId ? ' grm-chevron--up' : ''}`}
>>>>>>> Stashed changes
                        width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"
                      >
                        <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    </div>
                  </div>

<<<<<<< Updated upstream
                  {isOpen && (
                    <div className="grm-card-detail" onClick={(e) => e.stopPropagation()}>
                      {g.formula && (
                        <div className="grm-formula-row">
                          <span className="grm-formula-label">Công thức:</span>
                          <code className="grm-formula">{g.formula}</code>
                        </div>
                      )}
                      {detailLoading && !detail ? (
                        <p className="grm-example-vi">Đang tải...</p>
                      ) : detail ? (
                        <div className="grm-example">
                          {detail.exampleSentenceJp && <p className="grm-example-jp">{detail.exampleSentenceJp}</p>}
                          {detail.exampleSentenceVi && <p className="grm-example-vi">{detail.exampleSentenceVi}</p>}
                          {detail.usageExplanation && <p className="grm-example-vi">{detail.usageExplanation}</p>}
                        </div>
                      ) : null}

                      <button
                        className={`grm-mark-btn${g.isCompleted ? ' grm-mark-btn--done' : ''}`}
                        onClick={() => handleMarkComplete(g)}
                        disabled={g.isCompleted || markingId === g.grammarId}
                      >
                        {g.isCompleted
                          ? '✓ Đã học xong'
                          : markingId === g.grammarId
                            ? 'Đang lưu...'
                            : 'Đánh dấu đã học xong'}
                      </button>
=======
                  {expanded === g.grammarId && (
                    <div className="grm-card-detail" onClick={(e) => e.stopPropagation()}>
                      {loadingDetail && !detail ? (
                        <p style={{ opacity: 0.6, fontSize: '0.85rem' }}>Đang tải chi tiết...</p>
                      ) : detail ? (
                        <>
                          {detail.formula && (
                            <div className="grm-formula-row">
                              <span className="grm-formula-label">Công thức:</span>
                              <code className="grm-formula">{detail.formula}</code>
                            </div>
                          )}
                          {detail.usageExplanation && (
                            <p style={{ fontSize: '0.875rem', opacity: 0.85, marginBottom: '0.5rem' }}>
                              {detail.usageExplanation}
                            </p>
                          )}
                          <div className="grm-example">
                            {detail.exampleSentenceJp && (
                              <p className="grm-example-jp">{detail.exampleSentenceJp}</p>
                            )}
                            {detail.exampleSentenceVi && (
                              <p className="grm-example-vi">{detail.exampleSentenceVi}</p>
                            )}
                          </div>
                        </>
                      ) : null}
>>>>>>> Stashed changes
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}

        {!isLoading && totalPages > 1 && (
          <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
        )}
      </main>
    </div>
  );
}
