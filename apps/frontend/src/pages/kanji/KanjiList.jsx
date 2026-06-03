import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { ProgressBar } from '../../components/common/ProgressBar';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import { getKanjiList } from '../../api/studentService';
import { DEMO_MODE, MOCK_KANJI_LIST } from '../../api/mockData';
import './KanjiList.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function KanjiList() {
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);

  const [level,    setLevel]  = useState(user?.jlptLevel ?? 'N5');
  const [kanji,    setKanji]  = useState([]);
  const [stats,    setStats]  = useState({ completed: 0, total: 0 });
  const [isLoading,setLoading]= useState(true);
  const [error,    setError]  = useState('');
  const [page,     setPage]   = useState(1);
  const [totalPages,setTotal] = useState(1);

  const fetchKanji = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      if (DEMO_MODE) {
        const mock = MOCK_KANJI_LIST[level] ?? { kanji: [], completedCount: 0, totalElements: 0 };
        setKanji(mock.kanji);
        setTotal(1);
        setStats({ completed: mock.completedCount, total: mock.totalElements });
        setLoading(false);
        return;
      }
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

  const progressPct = stats.total > 0 ? Math.round((stats.completed / stats.total) * 100) : 0;

  return (
    <div className="knj-page">
      <TopNav activeTab="kanji" />
      <main className="knj-body">
        <div className="knj-page-header">
          <h1 className="knj-title"><span lang="ja">漢字</span> Kanji</h1>
          <p className="knj-subtitle">Luyện tập và tra cứu Kanji theo cấp độ JLPT.</p>
        </div>

        <div className="knj-level-tabs" role="tablist" aria-label="Chọn cấp độ JLPT">
          {LEVELS.map((l) => (
            <button
              key={l}
              role="tab"
              aria-selected={level === l}
              className={`knj-level-tab${level === l ? ' knj-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}
            >
              {l}
            </button>
          ))}
        </div>

        {!isLoading && (
          <div className="knj-stats-bar">
            <JlptBadge level={level} />
            <span className="knj-stats-text">
              đã học <strong>{stats.completed}</strong> / {stats.total} kanji
            </span>
            <div className="knj-stats-progress">
              <ProgressBar value={progressPct} />
            </div>
            <span className="knj-stats-pct">{progressPct}%</span>
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
            {Array.from({ length: 40 }).map((_, i) => (
              <div key={i} className="knj-cell-skel" aria-hidden="true" />
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
                className={`knj-cell${k.isCompleted ? ' knj-cell--done' : ''}`}
                onClick={() => navigate(`/kanji/${k.kanjiId}`)}
                aria-label={`${k.characterValue} — ${k.meaning}${k.isCompleted ? ' (đã học)' : ''}`}
                title={k.meaning}
              >
                <span className="knj-char" lang="ja">{k.characterValue}</span>
                {k.isCompleted && <span className="knj-done-tick" aria-hidden="true">✓</span>}
              </button>
            ))}
          </div>
        )}

        {!isLoading && totalPages > 1 && (
          <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
        )}
      </main>
    </div>
  );
}
