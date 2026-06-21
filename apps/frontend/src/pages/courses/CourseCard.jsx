/**
 * CourseCard — một khoá học theo cấp độ JLPT (SPEC-course-list §6.2).
 * VIP/locked đã bỏ khỏi phạm vi → mọi thẻ đều mở được.
 *
 * Props:
 *   course     — { jlptLevel, title, description, completedLessons, totalLessons }
 *   isCurrent  — true nếu jlptLevel === currentLevel (chip "Đang học", FR-CL-08)
 *   onOpen     — (jlptLevel) => void, mở khoá (FR-CL-06)
 */
export default function CourseCard({ course, isCurrent, onOpen }) {
  const { jlptLevel, title, description, completedLessons = 0, totalLessons = 0 } = course;
  const pct = totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0;
  const levelClass = `cl-card-level--${String(jlptLevel).toLowerCase()}`;

  return (
    <button
      type="button"
      className="cl-card"
      onClick={() => onOpen(jlptLevel)}
      aria-label={`Khoá ${jlptLevel}: ${title}`}
    >
      <span className={`cl-card-level ${levelClass}`}>{jlptLevel}</span>

      <span className="cl-card-title">{title}</span>
      <span className="cl-card-desc">{description}</span>

      <span className="cl-card-progress">
        <span
          className="cl-card-progress-track"
          role="progressbar"
          aria-valuenow={pct}
          aria-valuemin={0}
          aria-valuemax={100}
        >
          <span
            className={`cl-card-progress-fill${pct >= 80 ? ' cl-card-progress-fill--high' : ''}`}
            style={{ width: `${pct}%` }}
          />
        </span>
        <span className="cl-card-progress-label">{completedLessons}/{totalLessons} bài</span>
      </span>

      <span className="cl-card-footer">
        {isCurrent && (
          <span className="cl-card-current" aria-current="true">● Đang học</span>
        )}
        <span className="cl-card-arrow" aria-hidden="true">→</span>
      </span>
    </button>
  );
}
