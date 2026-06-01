import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchDashboardThunk } from '../../store/slices/studentSlice';
import TopNav from '../../components/layout/TopNav';
import SakuChan from '../../components/auth/SakuChan';
import StreakCard from './StreakCard';
import HeroBanner from './HeroBanner';
import LessonList from './LessonList';
import QuickActionCard from './QuickActionCard';
import StatCard from './StatCard';
import './Dashboard.css';

function SkeletonBlock({ className }) {
  return <div className={`db-skeleton ${className ?? ''}`} aria-hidden="true" />;
}

function Dashboard() {
  const dispatch  = useAppDispatch();
  const navigate  = useNavigate();
  const { streak, weekDays, course, lessons, wordCount, daysThisMonth, status } =
    useAppSelector((state) => state.student);

  const isLoading = status === 'loading';
  const hasLessons = Array.isArray(lessons) && lessons.length > 0;

  useEffect(() => {
    dispatch(fetchDashboardThunk());
  }, [dispatch]);

  return (
    <div className="dashboard-page">
      <TopNav activeTab="dashboard" />

      <div className="dashboard-body">
        {/* ── LEFT ── */}
        <aside className="dashboard-left" aria-label="Thống kê streak">
          {isLoading
            ? <SkeletonBlock className="db-skeleton--streak" />
            : <StreakCard streak={streak} weekDays={weekDays} />
          }
        </aside>

        {/* ── CENTER ── */}
        <main className="dashboard-center">
          {/* Hero banner */}
          {isLoading
            ? <SkeletonBlock className="db-skeleton--hero" />
            : <HeroBanner course={course} />
          }

          {/* Start Here label */}
          <div className="lesson-section-head">
            <span className="start-here-chip">▶ Start Here</span>
            <div className="lesson-divider" aria-hidden="true" />
          </div>

          {/* Lesson list */}
          {isLoading
            ? [1, 2, 3].map((i) => <SkeletonBlock key={i} className="db-skeleton--lesson" />)
            : hasLessons
              ? <LessonList lessons={lessons} />
              : (
                <div className="dashboard-empty" role="status" aria-live="polite">
                  <SakuChan variant="thinking" size={120} />
                  <p className="dashboard-empty-title">Chưa có bài học nào</p>
                  <p className="dashboard-empty-desc">
                    Bài học đang được cập nhật. Hãy quay lại sau nhé!
                  </p>
                  <button
                    type="button"
                    className="dashboard-empty-cta"
                    onClick={() => navigate('/courses')}
                  >
                    Xem khoá học khác
                  </button>
                </div>
              )
          }
        </main>

        {/* ── RIGHT ── */}
        <aside className="dashboard-right" aria-label="Chức năng nhanh">
          <div className="qa-list">
            <QuickActionCard type="flashcard" />
            <QuickActionCard type="exam" />
            <QuickActionCard type="dictionary" />
            <QuickActionCard type="progress" />
          </div>

          <div className="stat-list">
            {isLoading
              ? [1, 2].map((i) => <SkeletonBlock key={i} className="db-skeleton--stat" />)
              : <>
                  <StatCard type="words" value={wordCount} />
                  <StatCard type="days"  value={daysThisMonth} />
                </>
            }
          </div>
        </aside>
      </div>
    </div>
  );
}

export default Dashboard;
