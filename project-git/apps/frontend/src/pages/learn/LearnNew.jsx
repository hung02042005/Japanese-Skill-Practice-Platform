import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { ProgressBar } from '../../components/common/ProgressBar';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { getNextLesson } from '../../api/studentService';
import { DEMO_MODE, MOCK_NEXT_LESSON, MOCK_SUGGESTED_LESSONS } from '../../api/mockData';
import './LearnNew.css';

export default function LearnNew() {
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);

  const [nextLesson,  setNext]   = useState(null);
  const [suggestions, setSuggest]= useState([]);
  const [isLoading,   setLoading]= useState(true);
  const [error,       setError]  = useState('');

  useEffect(() => {
    if (DEMO_MODE) {
      setNext(MOCK_NEXT_LESSON);
      setSuggest(MOCK_SUGGESTED_LESSONS);
      setLoading(false);
      return;
    }
    (async () => {
      setLoading(true);
      setError('');
      try {
        const data = await getNextLesson();
        setNext(data.nextLesson ?? null);
        setSuggest(data.suggestedLessons ?? []);
      } catch {
        setError('Không thể tải bài học. Thử lại sau.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <div className="lnw-page">
      <TopNav activeTab="learn" />
      <main className="lnw-body">
        <div className="lnw-page-header">
          <h1 className="lnw-title">Học Từ Mới</h1>
          <p className="lnw-subtitle">
            Tiếp tục lộ trình {user?.jlptLevel ?? 'N5'} của bạn
          </p>
        </div>

        {error && (
          <div className="lnw-error" role="alert">
            <span>{error}</span>
            <button onClick={() => window.location.reload()}>Thử lại</button>
          </div>
        )}

        {isLoading ? (
          <>
            <div className="lnw-skel lnw-skel--hero" aria-hidden="true" />
            <div className="lnw-suggest-grid">
              {[1, 2].map((i) => <div key={i} className="lnw-skel lnw-skel--card" aria-hidden="true" />)}
            </div>
          </>
        ) : nextLesson ? (
          <>
            <div className="lnw-next-card">
              <div className="lnw-next-label">Bài tiếp theo của bạn</div>
              <div className="lnw-next-header">
                <JlptBadge level={nextLesson.jlptLevel} />
                <h2 className="lnw-next-title">{nextLesson.title}</h2>
                <span className="lnw-next-meta">
                  {nextLesson.lessonType} · ~{nextLesson.estimatedMinutes} phút
                </span>
              </div>
              {nextLesson.description && (
                <p className="lnw-next-desc">"{nextLesson.description}"</p>
              )}
              <div className="lnw-next-progress">
                <span className="lnw-prog-label">Tiến độ</span>
                <ProgressBar value={nextLesson.progressPercent} />
                <span className="lnw-prog-pct">{nextLesson.progressPercent}%</span>
              </div>
              <button
                className="lnw-start-btn"
                onClick={() => navigate(`/lessons/${nextLesson.lessonId}`)}
              >
                ▶ Bắt đầu học →
              </button>
            </div>

            {suggestions.length > 0 && (
              <section>
                <h2 className="lnw-suggest-title">Bài học khác trong level</h2>
                <div className="lnw-suggest-grid">
                  {suggestions.map((s) => (
                    <Link
                      key={s.lessonId}
                      to={`/lessons/${s.lessonId}`}
                      className="lnw-suggest-card"
                    >
                      <div className="lnw-sug-header">
                        <JlptBadge level={s.jlptLevel} />
                        <span className="lnw-sug-type">{s.lessonType}</span>
                      </div>
                      <div className="lnw-sug-title">{s.title}</div>
                      <div className="lnw-sug-meta">~{s.estimatedMinutes} phút</div>
                      <ProgressBar value={s.progressPercent} />
                    </Link>
                  ))}
                </div>
              </section>
            )}
          </>
        ) : (
          <EmptyState
            title="Xuất sắc! 🎉 Bạn đã học hết bài của level này!"
            subtitle="Chúc mừng! Hãy tiếp tục với level tiếp theo hoặc ôn tập lại."
            mascotVariant="celebrate"
            mascotSize={180}
          >
            <Link to="/dashboard" className="lnw-cta-btn">Về Dashboard</Link>
            <Link to="/mock-test" className="lnw-cta-btn lnw-cta-btn--outline">Thi thử →</Link>
          </EmptyState>
        )}
      </main>
    </div>
  );
}
