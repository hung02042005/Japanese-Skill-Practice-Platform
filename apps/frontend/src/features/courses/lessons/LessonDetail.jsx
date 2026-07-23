import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import TopNav from '@/shared/components/layout/TopNav';
import { ProgressBar } from '@/shared/components/common/ProgressBar';
import { JlptBadge } from '@/shared/components/common/Badges';
import { ToastContainer, useToast } from '@/shared/components/common/Toast';
import LessonVocabCard from '@/features/dashboard/student/LessonVocabCard';
import LessonGrammarPoint from '@/features/dashboard/student/LessonGrammarPoint';
import { getLessonDetail, markProgress } from '@/shared/api/studentService';
import './LessonDetail.css';

const TABS = [
  { id: 'content', label: 'Nội dung' },
  { id: 'vocab',   label: 'Từ vựng' },
  { id: 'grammar', label: 'Ngữ pháp' },
];

export default function LessonDetail() {
  const { id }   = useParams();
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [lesson,       setLesson]   = useState(null);
  const [activeTab,    setTab]      = useState('content');
  const [isLoading,    setLoading]  = useState(true);
  const [error,        setError]    = useState('');
  const [isCompleting, setComplete] = useState(false);
  const [isCompleted,  setCompleted]= useState(false);

  const fetchLesson = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getLessonDetail(id);
      if (data.isLocked) { navigate('/dashboard'); return; }
      setLesson(data);
      setCompleted(data.progressStatus === 'completed');
    } catch (err) {
      if (err?.response?.status === 404) { navigate('/404'); return; }
      setError(err?.response?.data?.message ?? 'Không thể tải bài học.');
    } finally {
      setLoading(false);
    }
  }, [id, navigate]);

  useEffect(() => { fetchLesson(); }, [fetchLesson]);

  async function handleMarkComplete() {
    if (isCompleted) return;
    setComplete(true);
    try {
      await markProgress('lesson', id, 'completed');
      setCompleted(true);
      addToast('success', 'Đã đánh dấu hoàn thành!');
    } catch {
      addToast('error', 'Không thể lưu tiến độ. Thử lại sau.');
    } finally {
      setComplete(false);
    }
  }

  const visibleTabs = TABS.filter((t) => {
    if (t.id === 'vocab'   && !lesson?.vocabulary?.length)    return false;
    if (t.id === 'grammar' && !lesson?.grammarPoints?.length) return false;
    return true;
  });

  return (
    <div className="lsn-page">
      <TopNav activeTab="learn" />

      <main className="lsn-body">
        {!isLoading && lesson && (
          <nav className="lsn-breadcrumb" aria-label="Điều hướng">
            <Link to="/dashboard">Dashboard</Link>
            <span aria-hidden="true"> › </span>
            <span>{lesson.jlptLevel} — {lesson.title}</span>
          </nav>
        )}

        {error && (
          <div className="lsn-error-banner" role="alert">
            <span>{error}</span>
            <button className="lsn-retry-btn" onClick={fetchLesson}>Thử lại</button>
          </div>
        )}

        {isLoading && (
          <>
            <div className="lsn-skel lsn-skel--header" aria-hidden="true" />
            <div className="lsn-skel lsn-skel--content" aria-hidden="true" />
          </>
        )}

        {!isLoading && !error && lesson && (
          <>
            <div className="lsn-header-card">
              <div className="lsn-header-meta">
                <JlptBadge level={lesson.jlptLevel} />
                <h1 className="lsn-title">{lesson.title}</h1>
                <p className="lsn-meta-row">
                  <span className="lsn-meta-chip">{lesson.lessonType}</span>
                  <span className="lsn-meta-dot" aria-hidden="true">·</span>
                  <span className="lsn-meta-time">{lesson.estimatedMinutes} phút</span>
                </p>
              </div>
              <div className="lsn-progress-row">
                <span className="lsn-progress-label">Tiến độ</span>
                <ProgressBar value={isCompleted ? 100 : lesson.progressPercent} />
                <span className="lsn-progress-pct">{isCompleted ? 100 : lesson.progressPercent}%</span>
              </div>
            </div>

            <div className="lsn-tabs" role="tablist" aria-label="Nội dung bài học">
              {visibleTabs.map((t) => (
                <button
                  key={t.id}
                  role="tab"
                  aria-selected={activeTab === t.id}
                  className={`lsn-tab${activeTab === t.id ? ' lsn-tab--active' : ''}`}
                  onClick={() => setTab(t.id)}
                >
                  {t.label}
                  {t.id === 'vocab'   && <span className="lsn-tab-count"> ({lesson.vocabulary?.length})</span>}
                  {t.id === 'grammar' && <span className="lsn-tab-count"> ({lesson.grammarPoints?.length})</span>}
                </button>
              ))}
            </div>

            <div className="lsn-tab-panel" role="tabpanel">
              {activeTab === 'content' && (
                <div className="lsn-content-area">
                  {lesson.audioUrl && (
                    <div className="lsn-audio-bar">
                      <span className="lsn-audio-label">Nghe phát âm:</span>
                      <audio controls src={lesson.audioUrl} aria-label="Phát âm bài học" />
                    </div>
                  )}
                  {lesson.imageUrl && (
                    <img
                      className="lsn-content-img"
                      src={lesson.imageUrl}
                      alt={`Minh hoạ bài: ${lesson.title}`}
                    />
                  )}
                  <div
                    className="lsn-content-html"
                    dangerouslySetInnerHTML={{ __html: lesson.contentHtml }}
                  />
                </div>
              )}

              {activeTab === 'vocab' && (
                <div className="lsn-vocab-list">
                  {lesson.vocabulary.map((v) => (
                    <LessonVocabCard key={v.vocabId} vocab={v} />
                  ))}
                </div>
              )}

              {activeTab === 'grammar' && (
                <div className="lsn-grammar-list">
                  {lesson.grammarPoints.map((g) => (
                    <LessonGrammarPoint key={g.grammarId} grammar={g} />
                  ))}
                </div>
              )}
            </div>

            <div className="lsn-footer">
              {lesson.prevLessonId ? (
                <Link to={`/lessons/${lesson.prevLessonId}`} className="lsn-nav-btn lsn-nav-btn--prev">
                  ← Bài trước
                </Link>
              ) : <div />}

              <button
                className={`lsn-complete-btn${isCompleted ? ' lsn-complete-btn--done' : ''}`}
                onClick={handleMarkComplete}
                disabled={isCompleting || isCompleted}
                aria-pressed={isCompleted}
              >
                {isCompleting && <span className="lsn-spinner" aria-hidden="true" />}
                {isCompleted ? '✓ Đã hoàn thành' : isCompleting ? 'Đang lưu…' : '✓ Đánh dấu hoàn thành'}
              </button>

              {lesson.nextLessonId ? (
                <Link to={`/lessons/${lesson.nextLessonId}`} className="lsn-nav-btn lsn-nav-btn--next">
                  Bài tiếp theo →
                </Link>
              ) : <div />}
            </div>
          </>
        )}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
