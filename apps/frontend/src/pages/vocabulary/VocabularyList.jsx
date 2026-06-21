import { useState, useEffect, useRef, useCallback } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { ProgressBar } from '../../components/common/ProgressBar';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import VocabCard from '../../components/student/VocabCard';
import {
  getVocabularyList,
  getVocabTopics,
  markVocabComplete,
  addVocabToFlashcard,
} from '../../api/studentService';
import './VocabularyList.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function VocabularyList() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);
  const [level,       setLevel]      = useState(searchParams.get('level') ?? user?.jlptLevel ?? 'N5');
  const [topicId,     setTopicId]    = useState('');
  const [search,      setSearch]     = useState('');
  const [debounced,   setDebounced]  = useState('');
  const [words,       setWords]      = useState([]);
  const [stats,       setStats]      = useState({ completed: 0, total: 0 });
  const [topics,      setTopics]     = useState([]);
  const [isLoading,   setLoading]    = useState(true);
  const [error,       setError]      = useState('');
  const [page,        setPage]       = useState(1);
  const [totalPages,  setTotal]      = useState(1);
  const [totalEl,     setTotalEl]    = useState(0);
  const [actionState, setAction]     = useState({});
  const timerRef = useRef(null);

  // Debounce search
  useEffect(() => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(search), 400);
    return () => clearTimeout(timerRef.current);
  }, [search]);

  // Reset page when filters change
  useEffect(() => { setPage(1); }, [level, topicId, debounced]);

  // Fetch topics when level changes
  useEffect(() => {
    setTopicId('');
    getVocabTopics(level).then(setTopics).catch(() => {});
  }, [level]);

  const fetchWords = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getVocabularyList({
        level,
        topicId: topicId || undefined,
        search: debounced,
        page: page - 1,
        size: 20,
      });
      setWords(data.content);
      setTotal(data.totalPages);
      setTotalEl(data.totalElements);
      setStats({ completed: data.completedCount ?? 0, total: data.totalElements });
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải từ vựng.');
    } finally {
      setLoading(false);
    }
  }, [level, topicId, debounced, page]);

  useEffect(() => { fetchWords(); }, [fetchWords]);

  const handleComplete = async (vocabId) => {
    setAction((prev) => ({ ...prev, [vocabId]: 'completing' }));
    try {
      await markVocabComplete(vocabId);
      setWords((prev) => prev.map((w) => w.vocabId === vocabId ? { ...w, isCompleted: true } : w));
      setStats((prev) => ({ ...prev, completed: prev.completed + 1 }));
      setAction((prev) => ({ ...prev, [vocabId]: 'done' }));
    } catch {
      setAction((prev) => { const s = { ...prev }; delete s[vocabId]; return s; });
    }
  };

  const handleAddFlashcard = async (vocabId) => {
    setAction((prev) => ({ ...prev, [`fc_${vocabId}`]: 'adding' }));
    try {
      await addVocabToFlashcard(vocabId);
      setWords((prev) => prev.map((w) => w.vocabId === vocabId ? { ...w, isInFlashcard: true } : w));
      setAction((prev) => ({ ...prev, [`fc_${vocabId}`]: 'added' }));
    } catch {
      setAction((prev) => { const s = { ...prev }; delete s[`fc_${vocabId}`]; return s; });
    }
  };

  // Click thẳng 1 chủ đề → mở phiên flashcard ôn tập (NEW + REVIEW) theo topicId.
  const startFlashcard = (tid) =>
    navigate(`/vocabulary/flashcard?topicId=${encodeURIComponent(tid)}&level=${level}`);

  const progressPct = stats.total > 0 ? Math.round((stats.completed / stats.total) * 100) : 0;

  return (
    <div className="voc-page">
      <TopNav activeTab="vocabulary" />
      <main className="voc-body">
        <div className="voc-header">
          <h1 className="voc-title"><span lang="ja">語彙</span> Từ Vựng</h1>
          <p className="voc-subtitle">Học từ vựng JLPT theo cấp độ và chủ đề.</p>
        </div>

        <div className="voc-level-tabs" role="tablist" aria-label="Chọn cấp độ JLPT">
          {LEVELS.map((l) => (
            <button
              key={l}
              role="tab"
              aria-selected={level === l}
              className={`voc-level-tab${level === l ? ' voc-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}
            >{l}</button>
          ))}
        </div>

        {!isLoading && (
          <div className="voc-stats">
            <JlptBadge level={level} />
            <span className="voc-stats-text">
              đã học <strong>{stats.completed}</strong> / {stats.total} từ
            </span>
            <div className="voc-stats-bar"><ProgressBar value={progressPct} /></div>
            <span className="voc-stats-pct">{progressPct}%</span>
          </div>
        )}

        <div className="voc-filters">
          <label className="visually-hidden" htmlFor="voc-topic-select">Chủ đề</label>
          <select
            id="voc-topic-select"
            className="voc-select"
            value={topicId}
            onChange={(e) => setTopicId(e.target.value)}
          >
            <option value="">Tất cả chủ đề</option>
            {topics.map((t) => <option key={t.topicId} value={t.topicId}>{t.titleVi}</option>)}
          </select>
          <div className="voc-search-wrap">
            <label className="visually-hidden" htmlFor="voc-search">Tìm từ vựng</label>
            <input
              id="voc-search"
              type="search"
              className="voc-search"
              placeholder="🔍 Tìm từ vựng..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        {topics.length > 0 && (
          <section className="voc-topics" aria-label="Học Flashcard theo chủ đề">
            <h2 className="voc-topics-title">⚡ Học Flashcard theo chủ đề</h2>
            <p className="voc-topics-hint">Bấm vào một chủ đề để bắt đầu phiên ôn tập (thẻ mới + ôn lại).</p>
            <div className="voc-topics-grid">
              {topics.map((t) => (
                <button
                  key={t.topicId}
                  type="button"
                  className="voc-topic-chip"
                  onClick={() => startFlashcard(t.topicId)}
                  aria-label={`Học flashcard chủ đề ${t.titleVi}`}
                >
                  <span className="voc-topic-name" lang="ja">{t.titleVi}</span>
                  <span className="voc-topic-go" aria-hidden="true">⚡ Học</span>
                </button>
              ))}
            </div>
          </section>
        )}

        {error && (
          <div className="voc-error" role="alert">
            {error}
            <button className="voc-retry" onClick={fetchWords}>Thử lại</button>
          </div>
        )}

        {isLoading ? (
          <div className="voc-list">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="voc-card-skel" aria-hidden="true" />
            ))}
          </div>
        ) : words.length === 0 ? (
          <EmptyState
            title="Không tìm thấy từ vựng"
            subtitle="Thử thay đổi bộ lọc hoặc từ khóa."
            mascotVariant="thinking"
            mascotSize={120}
          />
        ) : (
          <div className="voc-list">
            {words.map((w) => (
              <VocabCard
                key={w.vocabId}
                word={w}
                actionState={actionState}
                onComplete={handleComplete}
                onAddFlashcard={handleAddFlashcard}
              />
            ))}
          </div>
        )}

        {!isLoading && totalPages > 1 && (
          <>
            <p className="voc-count-text">
              Hiển thị {(page - 1) * 20 + 1}–{Math.min(page * 20, totalEl)} / {totalEl} từ
            </p>
            <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
          </>
        )}
      </main>
    </div>
  );
}
