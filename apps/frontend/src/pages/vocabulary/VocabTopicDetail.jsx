import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { ProgressBar } from '../../components/common/ProgressBar';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import VocabCard from '../../components/student/VocabCard';
import {
  getVocabularyList,
  markVocabComplete,
  addVocabToFlashcard,
} from '../../api/studentService';
import './VocabTopicDetail.css';

/**
 * Topic detail — danh sách từ phẳng (reading/audio/+FC/đánh dấu đã học) của một chủ đề.
 * Tái dùng màn v1.0 (SPEC-vocabulary §11.2), điều hướng tới qua `/vocabulary?level=&topic=`.
 * Mọi tính toán điểm/tiến độ do backend; trang chỉ render.
 */
export default function VocabTopicDetail() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAppSelector((s) => s.auth);

  const level = searchParams.get('level') ?? user?.jlptLevel ?? 'N5';
  const topic = searchParams.get('topic') ?? '';

  const [search,      setSearch]    = useState('');
  const [debounced,   setDebounced] = useState('');
  const [words,       setWords]     = useState([]);
  const [stats,       setStats]     = useState({ completed: 0, total: 0 });
  const [isLoading,   setLoading]   = useState(true);
  const [error,       setError]     = useState('');
  const [page,        setPage]      = useState(1);
  const [totalPages,  setTotal]     = useState(1);
  const [totalEl,     setTotalEl]   = useState(0);
  const [actionState, setAction]    = useState({});
  const timerRef = useRef(null);

  useEffect(() => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(search), 400);
    return () => clearTimeout(timerRef.current);
  }, [search]);

  useEffect(() => { setPage(1); }, [debounced, topic, level]);

  const fetchWords = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getVocabularyList({
        level, topic, search: debounced, page: page - 1, size: 20,
      });
      setWords(data.content);
      setTotal(data.totalPages);
      setTotalEl(data.totalElements);
      setStats({ completed: data.completedCount ?? 0, total: data.totalElements });
    } catch (err) {
      if (err?.response?.status === 401) { navigate('/login'); return; }
      setError(err?.response?.data?.message ?? 'Không thể tải từ vựng.');
    } finally {
      setLoading(false);
    }
  }, [level, topic, debounced, page, navigate]);

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

  const progressPct = stats.total > 0 ? Math.round((stats.completed / stats.total) * 100) : 0;

  return (
    <div className="vtd-page">
      <TopNav activeTab="vocabulary" />
      <main className="vtd-body">
        <button
          type="button"
          className="vtd-back"
          onClick={() => navigate(`/vocabulary?level=${level}`)}
        >
          ← Lộ trình từ vựng
        </button>

        <div className="vtd-header">
          <h1 className="vtd-title">
            <JlptBadge level={level} />
            <span lang="ja">{topic || 'Từ vựng'}</span>
          </h1>
          {!isLoading && (
            <div className="voc-stats">
              <span className="voc-stats-text">
                đã học <strong>{stats.completed}</strong> / {stats.total} từ
              </span>
              <div className="voc-stats-bar"><ProgressBar value={progressPct} /></div>
              <span className="voc-stats-pct">{progressPct}%</span>
            </div>
          )}
        </div>

        <div className="voc-search-wrap">
          <label className="visually-hidden" htmlFor="vtd-search">Tìm từ vựng</label>
          <input
            id="vtd-search"
            type="search"
            className="voc-search"
            placeholder="🔍 Tìm từ vựng..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

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
            subtitle="Thử thay đổi từ khóa tìm kiếm nhé 🌸"
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
