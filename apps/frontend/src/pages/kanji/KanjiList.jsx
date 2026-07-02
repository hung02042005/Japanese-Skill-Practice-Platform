import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppSelector, useAppDispatch } from '../../store/hooks';
import { fetchVocabHomeThunk } from '../../store/slices/studentSlice';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { ProgressBar } from '../../components/common/ProgressBar';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import StreakCard from '../../components/student/StreakCard';
import AccountPanel from '../vocabulary/AccountPanel';
import CourseListCard from '../vocabulary/CourseListCard';
import { getKanjiList, getKanjiDetail, resetProgress } from '../../api/studentService';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage } from '../../utils/apiMessage';
import './KanjiList.css';

export default function KanjiList() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [searchParams] = useSearchParams();
  const { user } = useAppSelector((s) => s.auth);
  const { success, error: toastError } = useToast();

  // Streak data từ vocabHome slice (cùng nguồn với VocabHome)
  const { vocabHome, vocabHomeStatus } = useAppSelector((s) => s.student);
  const { streak, weekDays } = vocabHome;
  const isSidebarLoading = vocabHomeStatus === 'loading' || vocabHomeStatus === 'idle';

  const [level]               = useState(searchParams.get('level') ?? user?.jlptLevel ?? 'N5');
  const [kanji,    setKanji]  = useState([]);
  const [stats,    setStats]  = useState({ completed: 0, total: 0 });
  const [isLoading,setLoading]= useState(true);
  const [error,    setError]  = useState('');
  const [page,     setPage]   = useState(1);
  const [totalPages,setTotal] = useState(1);
  const [selected, setSelected] = useState(null);
  const [detail,   setDetail]   = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const modalRef = useRef(null);

  const fetchKanji = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getKanjiList({ level, page: page - 1, size: 50 });
      setKanji(res.content ?? []);
      setTotal(res.totalPages ?? 1);
      setStats({ completed: res.completedCount ?? 0, total: res.totalElements ?? 0 });
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải danh sách Kanji.');
    } finally {
      setLoading(false);
    }
  }, [level, page]);

  useEffect(() => { fetchKanji(); }, [fetchKanji]);
  useEffect(() => { setPage(1); }, [level]);

  // Fetch streak/account data nếu chưa có
  useEffect(() => {
    if (vocabHomeStatus === 'idle') dispatch(fetchVocabHomeThunk());
  }, [dispatch, vocabHomeStatus]);

  const openKanji = useCallback(async (k) => {
    setSelected(k);
    setDetail(null);
    setDetailLoading(true);
    try {
      const d = await getKanjiDetail(k.kanjiId);
      setDetail(d);
    } catch {
      setDetail(null);
    } finally {
      setDetailLoading(false);
    }
  }, []);

  const closeModal = useCallback(() => { setSelected(null); setDetail(null); }, []);

  useEffect(() => {
    if (!selected) return;
    const onKey = (e) => { if (e.key === 'Escape') closeModal(); };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [selected, closeModal]);

  useEffect(() => {
    if (selected) modalRef.current?.focus();
  }, [selected]);

  const progressPct = stats.total > 0 ? Math.round((stats.completed / stats.total) * 100) : 0;

  return (
    <div className="knj-page">
      <TopNav activeTab="kanji" />

      <div className="knj-layout">
        {/* ─── LEFT: Streak ─── */}
        <aside className="knj-left" aria-label="Tiến độ streak">
          {isSidebarLoading
            ? <div className="knj-skel knj-skel--streak" aria-hidden="true" />
            : <StreakCard streak={streak} weekDays={weekDays} />}
        </aside>

        {/* ─── CENTER: Kanji content ─── */}
        <main className="knj-body">
        <div className="knj-page-header">
          <h1 className="knj-title"><span lang="ja">漢字</span> Kanji</h1>
          <p className="knj-subtitle">Luyện tập và tra cứu Kanji theo cấp độ JLPT.</p>
        </div>

        {!isLoading && (
          <div className="knj-stats-bar">
            <JlptBadge level={level} />
            <span className="knj-stats-text">
              đã học <strong>{stats.completed}</strong> / {stats.total} kanji
            </span>
            <div className="kl-progress-wrapper" style={{ display: 'flex', gap: '1rem', alignItems: 'center', width: '100%', maxWidth: '400px' }}>
              <div style={{ flex: 1 }}>
                <ProgressBar value={progressPct} />
              </div>
              <span className="knj-stats-pct">{progressPct}%</span>
              {stats.completed > 0 && (
                <button 
                  onClick={async () => {
                    if (window.confirm('Bạn có chắc muốn reset toàn bộ tiến độ Kanji?')) {
                      try {
                        await resetProgress('KANJI');
                        success('Đã reset tiến độ học Kanji.');
                        fetchKanji();
                      } catch (e) {
                        toastError(getErrorMessage(e, 'Lỗi khi reset tiến độ.'));
                      }
                    }
                  }}
                  style={{
                    padding: '6px 12px',
                    background: '#fef2f2',
                    color: '#ef4444',
                    border: '1px solid #fca5a5',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontWeight: '500',
                    fontSize: '0.85rem'
                  }}
                >
                  Reset
                </button>
              )}
            </div>
          </div>
        )}

        {error && (
          <div className="knj-error" role="alert">
            <span>{error}</span>
            <button className="knj-retry-btn" onClick={fetchKanji}>Thử lại</button>
          </div>
        )}

        {isLoading ? (
          <div className="knj-grid">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="knj-card-skel" aria-hidden="true" />
            ))}
          </div>
        ) : kanji.length === 0 ? (
          <EmptyState
            title={`Chưa có Kanji ${level}`}
            subtitle="Nội dung đang được cập nhật. Thử level khác nhé!"
            mascotVariant="thinking"
            mascotSize={140}
          />
        ) : (
          <div className="knj-grid" role="list" aria-label={`Danh sách Kanji ${level}`}>
            {kanji.map((k) => (
              <button
                key={k.kanjiId}
                role="listitem"
                className={`knj-card${k.isCompleted ? ' knj-card--done' : ''}`}
                onClick={() => openKanji(k)}
                aria-label={`${k.characterValue} — ${k.meaning}${k.isCompleted ? ' (đã học)' : ''}`}
              >
                <span className="knj-card-thumb" aria-hidden="true">
                  <span className="knj-card-char" lang="ja">{k.characterValue}</span>
                </span>
                <span className="knj-card-content">
                  <span className="knj-card-title" lang="ja">{k.characterValue}</span>
                  <span className="knj-card-sub">{k.meaning}</span>
                </span>
                {k.isCompleted && <span className="knj-done-tick" aria-hidden="true">✓</span>}
                <span className="knj-card-trail" aria-hidden="true">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M9 6l6 6-6 6" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </span>
              </button>
            ))}
          </div>
        )}

        {!isLoading && totalPages > 1 && (
          <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
        )}
        </main>

        {/* ─── RIGHT: Account ─── */}
        <aside className="knj-right" aria-label="Tài khoản">
          {isSidebarLoading
            ? <div className="knj-skel knj-skel--account" aria-hidden="true" />
            : <AccountPanel user={user} />}
          <CourseListCard onClick={() => navigate('/courses')} />
        </aside>
      </div>

      {selected && (
        <div className="knj-overlay" role="dialog" aria-modal="true" aria-label={`Chi tiết kanji ${selected.characterValue}`} onClick={closeModal}>
          <div
            className="knj-modal"
            ref={modalRef}
            tabIndex={-1}
            onClick={(e) => e.stopPropagation()}
          >
            <button className="knj-modal-close" onClick={closeModal} aria-label="Đóng">×</button>

            <div className="knj-modal-char" lang="ja">{selected.characterValue}</div>
            <div className="knj-modal-meaning">{selected.meaning}</div>

            {detailLoading ? (
              <div className="knj-modal-loading">Đang tải...</div>
            ) : detail ? (
              <div className="knj-modal-detail">
                <div className="knj-modal-row">
                  <span className="knj-modal-label">On&apos;yomi</span>
                  <span className="knj-modal-val" lang="ja">{detail.onyomi || '—'}</span>
                </div>
                <div className="knj-modal-row">
                  <span className="knj-modal-label">Kun&apos;yomi</span>
                  <span className="knj-modal-val" lang="ja">{detail.kunyomi || '—'}</span>
                </div>
                <div className="knj-modal-row">
                  <span className="knj-modal-label">Số nét</span>
                  <span className="knj-modal-val">{detail.strokeCount}</span>
                </div>
                {detail.exampleWord && (
                  <div className="knj-modal-example">
                    <span className="knj-modal-label">Ví dụ</span>
                    <span className="knj-modal-val" lang="ja">{detail.exampleWord}</span>
                    <span className="knj-modal-reading" lang="ja">（{detail.exampleReading}）</span>
                    <span className="knj-modal-exmeaning">{detail.exampleMeaning}</span>
                  </div>
                )}
              </div>
            ) : null}

            <button className="knj-modal-practice-btn" onClick={() => navigate(`/kanji/${selected.kanjiId}`)}>
              Luyện tập
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
