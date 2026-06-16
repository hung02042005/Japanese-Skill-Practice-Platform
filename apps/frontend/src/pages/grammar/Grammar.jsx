import { useState, useEffect, useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
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
          <EmptyState
            title={`Chưa có cấu trúc ${level}`}
            subtitle="Thử tìm kiếm với từ khóa khác hoặc đổi cấp độ."
            mascotVariant="thinking"
            mascotSize={120}
          />
        ) : (
          <div className="grm-list">
            {filtered.map((g) => {
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
                >
                  <div className="grm-card-main">
                    <div className="grm-card-left">
                      <JlptBadge level={g.jlptLevel} />
                      <span className="grm-structure">{g.structure}</span>
                      {g.isCompleted && <span className="grm-done-tick" aria-hidden="true">✓</span>}
                    </div>
                    <div className="grm-card-right">
                      <span className="grm-meaning">{g.meaning}</span>
                      <svg
                        className={`grm-chevron${isOpen ? ' grm-chevron--up' : ''}`}
                        width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"
                      >
                        <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    </div>
                  </div>

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
