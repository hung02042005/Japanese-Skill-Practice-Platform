import CourseCard from './CourseCard';

/**
 * CourseGrid — lưới responsive các CourseCard (SPEC-course-list §6.1).
 * Giữ nguyên thứ tự `courses` do BE trả (N5 → N1).
 *
 * Props:
 *   courses      — mảng khoá học
 *   currentLevel — cấp đang học (đánh dấu chip "Đang học")
 *   onOpen       — (jlptLevel) => void
 */
export default function CourseGrid({ courses, currentLevel, onOpen }) {
  return (
    <div className="cl-grid">
      {courses.map((course) => (
        <CourseCard
          key={course.jlptLevel}
          course={course}
          isCurrent={course.jlptLevel === currentLevel}
          onOpen={onOpen}
        />
      ))}
    </div>
  );
}
