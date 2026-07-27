import { useState, useEffect, useCallback, useMemo } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useAppSelector, useAppDispatch } from '@/store/hooks';
import { fetchVocabHomeThunk } from '@/features/dashboard/studentSlice';
import TopNav from '@/shared/components/layout/TopNav';
import { JlptBadge } from '@/shared/components/common/Badges';
import { EmptyState } from '@/shared/components/common/EmptyState';
import { Pagination } from '@/shared/components/common/Pagination';
import StreakCard from '@/features/dashboard/student/StreakCard';
import AccountPanel from '@/features/vocabulary/vocabulary/AccountPanel';
import CourseListCard from '@/features/vocabulary/vocabulary/CourseListCard';
import { getGrammarList, getGrammarDetail, markProgress } from '@/shared/api/studentService';
import './Grammar.css';

export default function Grammar() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { user } = useAppSelector((s) => s.auth);

  // Streak data từ vocabHome slice
  const { vocabHome, vocabHomeStatus } = useAppSelector((s) => s.student);
  const { streak, weekDays } = vocabHome;
  const isSidebarLoading = vocabHomeStatus === 'loading' || vocabHomeStatus === 'idle';

  const [level]                     = useState(searchParams.get('level') ?? user?.jlptLevel ?? 'N5');
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

  // Fetch streak/account data nếu chưa có
  useEffect(() => {
    if (vocabHomeStatus === 'idle') dispatch(fetchVocabHomeThunk());
  }, [dispatch, vocabHomeStatus]);

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

      <div className="grm-layout">
        {/* ─── LEFT: Streak ─── */}
        <aside className="grm-left" aria-label="Tiến độ streak">
          {isSidebarLoading
            ? <div className="grm-skel grm-skel--streak" aria-hidden="true" />
            : <StreakCard streak={streak} weekDays={weekDays} />}
        </aside>

        {/* ─── CENTER: Grammar content ─── */}
        <main className="grm-body">
        <div className="grm-header">
          <h1 className="grm-title">Ngữ Pháp</h1>
          <p className="grm-subtitle">Tổng hợp cấu trúc ngữ pháp theo cấp độ JLPT</p>
        </div>

        <div className="grm-controls">
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
                  className={`grm-card${isOpen ? ' grm-card--open' : ''}${g.isCompleted ? ' grm-card--done' : ''}`}
                  onClick={() => toggle(g)}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => e.key === 'Enter' && toggle(g)}
                  aria-expanded={isOpen}
                >
                  <div className="grm-card-main">
                    <span className="grm-card-thumb" aria-hidden="true">
                      <span className="grm-card-thumb-char" lang="ja">{(g.structure ?? '').trim().charAt(0)}</span>
                    </span>
                    <div className="grm-card-content">
                      <span className="grm-structure" lang="ja">{g.structure}</span>
                      <span className="grm-meaning">{g.meaning}</span>
                    </div>
                    <div className="grm-card-right">
                      <JlptBadge level={g.jlptLevel} />
                      {g.isCompleted && <span className="grm-done-tick" aria-hidden="true">✓</span>}
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

        {/* ─── RIGHT: Account ─── */}
        <aside className="grm-right" aria-label="Tài khoản">
          {isSidebarLoading
            ? <div className="grm-skel grm-skel--account" aria-hidden="true" />
            : <AccountPanel user={user} />}
          <CourseListCard onClick={() => navigate('/courses')} />
        </aside>
      </div>
    </div>
  );
}
