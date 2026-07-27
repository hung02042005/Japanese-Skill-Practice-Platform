import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { fetchVocabHomeThunk } from '@/features/dashboard/studentSlice';
import TopNav from '@/shared/components/layout/TopNav';
import StreakCard from '@/features/dashboard/student/StreakCard';
import { EmptyState } from '@/shared/components/common/EmptyState';
import VocabLessonList from './VocabLessonList';
import AccountPanel from './AccountPanel';
import CourseListCard from './CourseListCard';
import './VocabHome.css';

export default function VocabHome() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  // Level do người dùng chọn từ màn Course List (?level=N4). Không có → BE dùng level học viên.
  const selectedLevel = params.get('level');

  const { user } = useAppSelector((s) => s.auth);
  const { vocabHome, vocabHomeStatus, vocabHomeError } = useAppSelector((s) => s.student);
  const { streak, weekDays, courseTitle, level, lessons } = vocabHome;

  const isLoading = vocabHomeStatus === 'loading' || vocabHomeStatus === 'idle';
  const isFailed  = vocabHomeStatus === 'failed';
  const hasLessons = Array.isArray(lessons) && lessons.length > 0;

  useEffect(() => { dispatch(fetchVocabHomeThunk(selectedLevel)); }, [dispatch, selectedLevel]);

  return (
    <div className="vh-page">
      <TopNav activeTab="vocabulary" />

      <div className="vh-body">
        {/* ─── LEFT: Streak ─── */}
        <aside className="vh-left" aria-label="Tiến độ streak">
          {isLoading
            ? <div className="vh-skel vh-skel--streak" aria-hidden="true" />
            : <StreakCard streak={streak} weekDays={weekDays} />}
        </aside>

        {/* ─── CENTER: Lessons ─── */}
        <main className="vh-center" aria-busy={isLoading}>
          <h1 className="vh-section-title">{courseTitle || 'N5 Kanji & Vocab'}</h1>

          {isLoading ? (
            <div className="vh-lesson-list">
              {[1, 2, 3].map((i) => (
                <div key={i} className="vh-skel vh-skel--lesson" aria-hidden="true" />
              ))}
            </div>
          ) : isFailed ? (
            <div className="vh-error" role="alert">
              <p className="vh-error-text">{vocabHomeError || 'Không thể tải trang Từ vựng.'}</p>
              <button
                type="button"
                className="vh-error-retry"
                onClick={() => dispatch(fetchVocabHomeThunk())}
              >
                Thử lại
              </button>
            </div>
          ) : hasLessons ? (
            <VocabLessonList
              lessons={lessons}
              onOpen={(lesson) => {
                // Click bài → mở phiên flashcard ôn tập của chủ đề (theo topicId — khoá duy nhất).
                navigate(
                  `/vocabulary/flashcard?topicId=${encodeURIComponent(lesson.topicId)}` +
                  `&level=${encodeURIComponent(level || 'N5')}`,
                );
              }}
            />
          ) : (
            <EmptyState
              mascotVariant="thinking"
              mascotSize={160}
              title="Chưa có bài học nào"
              subtitle="Bài học đang được cập nhật. Hãy quay lại sau nhé!"
            >
              <button
                type="button"
                className="vh-empty-cta"
                onClick={() => navigate('/courses')}
              >
                Xem khoá học khác
              </button>
            </EmptyState>
          )}
        </main>

        {/* ─── RIGHT: Account + Course List ─── */}
        <aside className="vh-right" aria-label="Tài khoản & khoá học">
          {isLoading
            ? <div className="vh-skel vh-skel--account" aria-hidden="true" />
            : <AccountPanel user={user} />}
          <CourseListCard onClick={() => navigate('/courses')} />
        </aside>
      </div>
    </div>
  );
}
