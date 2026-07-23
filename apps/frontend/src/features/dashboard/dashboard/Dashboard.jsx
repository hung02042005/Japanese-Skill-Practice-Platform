import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { fetchDashboardThunk } from '@/features/dashboard/studentSlice';
import TopNav from '@/shared/components/layout/TopNav';
import { EmptyState } from '@/shared/components/common/EmptyState';
import StreakCard from '@/features/dashboard/student/StreakCard';
import CoursePickerBanner from '@/features/dashboard/student/CoursePickerBanner';
import LessonList from '@/features/dashboard/student/LessonList';
import QuickActionCard from '@/features/dashboard/student/QuickActionCard';
import MiniStatCard from '@/features/dashboard/student/MiniStatCard';
import {
  KanaIcon, VocabIcon, QuizIcon,
  MicIcon,
} from '@/shared/components/common/StudentIcons';
import './Dashboard.css';

const FEATURE_SHORTCUTS = [
  { Icon: KanaIcon,        label: 'Bảng chữ Kana', route: '/kana' },
  { Icon: VocabIcon,       label: 'Từ vựng',        route: '/vocabulary' },
  { Icon: QuizIcon,        label: 'Bài tập Quiz',   route: '/quiz' },
  { Icon: MicIcon,         label: 'Luyện nói',      route: '/speaking' },
];

function SkeletonBlock({ className }) {
  return <div className={`db-skeleton ${className ?? ''}`} aria-hidden="true" />;
}

function Dashboard() {
  const dispatch  = useAppDispatch();
  const navigate  = useNavigate();
  const { streak, weekDays, lessons, wordCount, daysThisMonth, status, error, selectedLevel } =
    useAppSelector((state) => state.student);

  const isLoading      = status === 'loading';
  const isFailed        = status === 'failed';
  const filteredLessons = Array.isArray(lessons)
    ? lessons.filter((l) => l.jlptLevel === selectedLevel)
    : [];
  const hasLessons = filteredLessons.length > 0;

  useEffect(() => {
    dispatch(fetchDashboardThunk());
  }, [dispatch]);

  return (
    <div className="dashboard-page">
      <TopNav activeTab="dashboard" />

      {isFailed ? (
        <div className="dashboard-body">
          <EmptyState
            title="Không tải được Dashboard"
            subtitle={error || 'Đã xảy ra lỗi khi tải dữ liệu. Vui lòng thử lại.'}
            mascotVariant="wrong"
            mascotSize={120}
          >
            <button
              type="button"
              className="db-empty-cta"
              onClick={() => dispatch(fetchDashboardThunk())}
            >
              Thử lại
            </button>
          </EmptyState>
        </div>
      ) : (
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
            : <CoursePickerBanner />
          }

          <div className="lesson-section-head">
            <span className="start-here-chip">▶ Start Here</span>
            <div className="lesson-divider" aria-hidden="true" />
          </div>

          {isLoading
            ? [1, 2, 3].map((i) => <SkeletonBlock key={i} className="db-skeleton--lesson" />)
            : hasLessons
              ? <LessonList lessons={filteredLessons} />
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
                    onClick={() => navigate('/vocabulary')}
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
                <span className="db-feature-icon" aria-hidden="true">
                  <f.Icon size={22} />
                </span>
                <span className="db-feature-label">{f.label}</span>
              </button>
            ))}
          </div>
        </main>

        {/* ── RIGHT ── */}
        <aside className="dashboard-right" aria-label="Chức năng nhanh">
          <div className="qa-list">
            <QuickActionCard type="notebook" />
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
      )}
    </div>
  );
}

export default Dashboard;
