import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import StreakCard from '../../components/student/StreakCard';
import { EmptyState } from '../../components/common/EmptyState';
import VocabPathCard from '../../components/student/VocabPathCard';
import VocabTopicDetail from './VocabTopicDetail';
import VocabFlashcardSession from './VocabFlashcardSession';
import { getVocabPath } from '../../api/studentService';
import { DEMO_MODE, MOCK_VOCAB_PATH } from '../../api/mockData';
import './VocabularyList.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

function CourseIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 5a2 2 0 0 1 2-2h7v18H6a2 2 0 0 1-2-2V5z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
      <path d="M13 3h5a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-5" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" />
    </svg>
  );
}

function CourseArrow() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M9 6l6 6-6 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

/**
 * Route root cho `/vocabulary`.
 * - `?topic=` có giá trị → phiên học Flashcard SRS (§3.6/§3.7) của chủ đề.
 * - `?topic=…&view=list` → danh sách từ phẳng (v1.0, §11.2) cho ai muốn tra cứu.
 * - ngược lại → "Vocab Learning Hub" 3 cột (trang chính của spec này).
 */
export default function VocabularyList() {
  const [searchParams] = useSearchParams();
  if (searchParams.get('topic')) {
    return searchParams.get('view') === 'list' ? <VocabTopicDetail /> : <VocabFlashcardSession />;
  }
  return <VocabHub />;
}

function VocabHub() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAppSelector((s) => s.auth);
  const { streak = 0, weekDays = [] } = useAppSelector((s) => s.student);

  const [level,     setLevel]   = useState(searchParams.get('level') ?? user?.jlptLevel ?? 'N5');
  const [cards,     setCards]   = useState([]);
  const [isLoading, setLoading] = useState(true);
  const [error,     setError]   = useState('');

  // Trạng thái card (active/locked…) + quyền level/VIP do BACKEND tính (§A.3) — FE chỉ render.
  const fetchPath = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      if (DEMO_MODE) {
        setCards(MOCK_VOCAB_PATH[level] ?? []);
        return;
      }
      setCards(await getVocabPath(level));
    } catch (err) {
      const code = err?.response?.status;
      if (code === 401) { navigate('/login'); return; }
      setCards([]);
      setError(code === 403 || code === 422
        ? 'Cấp độ này chưa mở khóa hoặc cần nâng cấp tài khoản.'
        : (err?.response?.data?.message ?? 'Không thể tải lộ trình từ vựng.'));
    } finally {
      setLoading(false);
    }
  }, [level, navigate]);

  useEffect(() => { fetchPath(); }, [fetchPath]);

  const changeLevel = (l) => {
    setLevel(l);
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set('level', l);
      return next;
    }, { replace: true });
  };

  const openTopic = (card) => navigate(`/vocabulary?level=${level}&topic=${card.slug}`);

  return (
    <div className="voc-page">
      <TopNav activeTab="vocabulary" />

      <div className="voc-body">
        {/* ── LEFT — Streak ── */}
        <aside className="voc-left" aria-label="Thống kê streak">
          {isLoading
            ? <div className="voc-skeleton voc-skeleton--streak" aria-hidden="true" />
            : <StreakCard streak={streak} weekDays={weekDays} />}
        </aside>

        {/* ── CENTER — Title pill + Path ── */}
        <main className="voc-center" aria-busy={isLoading}>
          <div className="voc-levels" role="tablist" aria-label="Chọn cấp độ JLPT">
            {LEVELS.map((l) => (
              <button
                key={l}
                role="tab"
                aria-selected={level === l}
                className={`voc-level-pill${level === l ? ' voc-level-pill--active' : ''}`}
                onClick={() => changeLevel(l)}
              >
                {l}
              </button>
            ))}
          </div>

          <div className="voc-titlewrap">
            <span className="voc-title-blob voc-title-blob--left" aria-hidden="true" />
            <span className="voc-title-blob voc-title-blob--right" aria-hidden="true" />
            <h1 className="voc-title-pill">
              <span className="voc-title-lv">{level}</span> Kanji &amp; Vocab
            </h1>
          </div>

          {error ? (
            <div className="voc-error" role="alert">
              {error}
              <button className="voc-retry" onClick={fetchPath}>Thử lại</button>
            </div>
          ) : isLoading ? (
            <div className="voc-path-list">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="voc-skeleton voc-skeleton--path" aria-hidden="true" />
              ))}
            </div>
          ) : cards.length === 0 ? (
            <EmptyState
              mascotVariant="thinking"
              mascotSize={160}
              title="Chưa có chủ đề từ vựng"
              subtitle="Nội dung cấp độ này đang được cập nhật. Hãy quay lại sau nhé!"
            />
          ) : (
            <div className="voc-path-list">
              {cards.map((c) => (
                <VocabPathCard
                  key={c.topicId}
                  card={c}
                  active={c.status === 'active'}
                  onOpen={openTopic}
                />
              ))}
            </div>
          )}
        </main>

        {/* ── RIGHT — Course list shortcut ── */}
        <aside className="voc-right" aria-label="Điều hướng khóa học">
          <button
            type="button"
            className="voc-courselist-card"
            aria-label="Mở danh sách khóa học"
            onClick={() => navigate('/courses')}
          >
            <span className="voc-cl-icon" aria-hidden="true"><CourseIcon /></span>
            <span className="voc-cl-label">Danh sách khóa học</span>
            <span className="voc-cl-arrow" aria-hidden="true"><CourseArrow /></span>
          </button>
        </aside>
      </div>
    </div>
  );
}
