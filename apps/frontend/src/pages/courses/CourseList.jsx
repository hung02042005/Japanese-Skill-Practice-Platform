import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchCoursesThunk } from '../../store/slices/studentSlice';
import TopNav from '../../components/layout/TopNav';
import CourseGrid from './CourseGrid';
import { EmptyState } from '../../components/common/EmptyState';
import './CourseList.css';

/**
 * CourseList — chọn khoá học theo cấp độ JLPT (SPEC-course-list, route /courses).
 * VIP/subscription đã bỏ khỏi phạm vi → mọi khoá đều mở được.
 * Mỗi kỹ năng (Từ vựng / Kanji / Ngữ pháp) là một nhóm thẻ N5→N1 riêng.
 */
const SKILLS = [
  { key: 'vocabulary', title: 'Từ vựng', route: '/vocabulary' },
  { key: 'kanji',      title: 'Kanji',   route: '/kanji' },
  { key: 'grammar',    title: 'Ngữ pháp', route: '/grammar' },
];

export default function CourseList() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const { courses, currentLevel, coursesStatus, coursesError } =
    useAppSelector((s) => s.student);

  const isLoading  = coursesStatus === 'loading' || coursesStatus === 'idle';
  const isFailed   = coursesStatus === 'failed';
  const hasCourses = Array.isArray(courses) && courses.length > 0;

  useEffect(() => { dispatch(fetchCoursesThunk()); }, [dispatch]);

  return (
    <div className="cl-page">
      <TopNav activeTab="courses" />

      <main className="cl-content">
        <nav className="cl-breadcrumb" aria-label="Breadcrumb">
          <button type="button" onClick={() => navigate('/vocabulary')}>Từ vựng</button>
          <span aria-hidden="true"> › </span>
          <span aria-current="page">Khoá học</span>
        </nav>

        <h1 className="cl-title">Khoá học theo cấp độ</h1>
        <p className="cl-subtitle">Chọn cấp JLPT bạn muốn học cho từng kỹ năng</p>

        {isLoading ? (
          SKILLS.map((skill) => (
            <section key={skill.key} className="cl-section">
              <h2 className="cl-section-title">{skill.title}</h2>
              <div className="cl-grid" aria-busy="true">
                {[1, 2, 3, 4, 5].map((i) => (
                  <div key={i} className="cl-skel cl-skel--card" aria-hidden="true" />
                ))}
              </div>
            </section>
          ))
        ) : isFailed ? (
          <div className="cl-error" role="alert">
            <p>{coursesError || 'Không thể tải danh sách khoá học.'}</p>
            <button type="button" onClick={() => dispatch(fetchCoursesThunk())}>Thử lại</button>
          </div>
        ) : hasCourses ? (
          SKILLS.map((skill) => (
            <section key={skill.key} className="cl-section">
              <h2 className="cl-section-title">{skill.title}</h2>
              <CourseGrid
                courses={courses}
                currentLevel={currentLevel}
                onOpen={(level) =>
                  navigate(`${skill.route}?level=${encodeURIComponent(level)}`)
                }
              />
            </section>
          ))
        ) : (
          <EmptyState
            mascotVariant="thinking"
            mascotSize={160}
            title="Chưa có khoá học nào"
            subtitle="Khoá học đang được cập nhật. Hãy quay lại sau nhé! 🌸"
          >
            <button type="button" className="cl-empty-cta" onClick={() => navigate('/vocabulary')}>
              Về trang Từ vựng
            </button>
          </EmptyState>
        )}
      </main>
    </div>
  );
}
