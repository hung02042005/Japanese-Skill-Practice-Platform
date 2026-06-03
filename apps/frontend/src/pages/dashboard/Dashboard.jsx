import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchDashboardThunk } from '../../store/slices/studentSlice';
import TopNav from '../../components/layout/TopNav';
import { EmptyState } from '../../components/common/EmptyState';
import StreakCard from '../../components/student/StreakCard';
import HeroBanner from '../../components/student/HeroBanner';
import LessonList from '../../components/student/LessonList';
import QuickActionCard from '../../components/student/QuickActionCard';
import MiniStatCard from '../../components/student/MiniStatCard';
import './Dashboard.css';

const FEATURE_SHORTCUTS = [
  { icon: '🔤', label: 'Bảng chữ Kana',  route: '/kana' },
  { icon: '📖', label: 'Từ vựng',         route: '/vocabulary' },
  { icon: '✏️', label: 'Bài tập Quiz',    route: '/quiz' },
  { icon: '🎤', label: 'Luyện nói',       route: '/speaking' },
  { icon: '📚', label: 'Đọc hiểu',        route: '/reading' },
  { icon: '🎧', label: 'Nghe hiểu',       route: '/listening' },
  { icon: '🏆', label: 'Chứng chỉ',       route: '/certificates' },
  { icon: '⭐', label: 'Nâng cấp VIP',    route: '/subscription' },
];

function SkeletonBlock({ className }) {
  return <div className={`db-skeleton ${className ?? ''}`} aria-hidden="true" />;
}

function Dashboard() {
  const dispatch  = useAppDispatch();
  const navigate  = useNavigate();
  const { streak, weekDays, course, lessons, wordCount, daysThisMonth, status } =
    useAppSelector((state) => state.student);

  const isLoading  = status === 'loading';
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
          {isLoading
            ? <SkeletonBlock className="db-skeleton--hero" />
            : <HeroBanner course={course} />
          }

          <div className="lesson-section-head">
            <span className="start-here-chip">▶ Start Here</span>
            <div className="lesson-divider" aria-hidden="true" />
          </div>

          {isLoading
            ? [1, 2, 3].map((i) => <SkeletonBlock key={i} className="db-skeleton--lesson" />)
            : hasLessons
              ? <LessonList lessons={lessons} />
              : (
                <EmptyState
                  title="Chưa có bài học nào"
                  subtitle="Bài học đang được cập nhật. Hãy quay lại sau nhé!"
                  mascotVariant="thinking"
                  mascotSize={120}
                >
                  <button
                    type="button"
                    className="db-empty-cta"
                    onClick={() => navigate('/learn')}
                  >
                    Bắt đầu học ngay
                  </button>
                </EmptyState>
              )
          }

          {/* ── Feature shortcuts ── */}
          <div className="db-features-head">
            <span className="start-here-chip">Tính năng</span>
            <div className="lesson-divider" aria-hidden="true" />
          </div>
          <div className="db-feature-grid">
            {FEATURE_SHORTCUTS.map((f) => (
              <button
                key={f.route}
                type="button"
                className="db-feature-card"
                onClick={() => navigate(f.route)}
              >
                <span className="db-feature-icon" aria-hidden="true">{f.icon}</span>
                <span className="db-feature-label">{f.label}</span>
              </button>
            ))}
          </div>
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
                  <MiniStatCard type="words" value={wordCount} />
                  <MiniStatCard type="days"  value={daysThisMonth} />
                </>
            }
          </div>
        </aside>
      </div>
    </div>
  );
}

export default Dashboard;
