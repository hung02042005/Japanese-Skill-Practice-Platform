import { useNavigate } from 'react-router-dom';

const JLPT_CLASS = { N5: 'jlpt-n5', N4: 'jlpt-n4', N3: 'jlpt-n3', N2: 'jlpt-n2', N1: 'jlpt-n1' };

const LockIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
    <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" strokeWidth="2"/>
    <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
  </svg>
);

const ChevronRight = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
    <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

function LessonCard({ lesson }) {
  const navigate = useNavigate();
  const { id, title, description, jlptLevel, status, progress, thumbnail } = lesson;

  const isLocked    = status === 'locked';
  const isActive    = status === 'active';
  const progressPct = Math.round((progress ?? 0) * 100);

  function handleClick() {
    if (!isLocked) navigate(`/learn/${id}`);
  }

  const Tag = isLocked ? 'div' : 'button';

  return (
    <Tag
      type={isLocked ? undefined : 'button'}
      className={`lesson-card lesson-card--${status}`}
      onClick={handleClick}
      aria-label={isLocked ? `Bài học bị khóa: ${title}` : title}
      aria-disabled={isLocked || undefined}
      aria-current={isActive ? 'true' : undefined}
    >
      {isActive && <div className="lesson-active-bar" aria-hidden="true" />}

      {/* Thumbnail */}
      <div className={`lesson-thumb${isActive ? ' lesson-thumb--active' : ''}${isLocked ? ' lesson-thumb--locked' : ''}`}>
        {isLocked ? <LockIcon /> : (
          <span className="lesson-thumb-char">{thumbnail}</span>
        )}
      </div>

      {/* Content */}
      <div className="lesson-content">
        <div className="lesson-title">{title}</div>
        <div className="lesson-desc">{description}</div>
        {progressPct > 0 && (
          <div className="lesson-progress-track" aria-label={`${progressPct}% hoàn thành`}>
            <div className="lesson-progress-fill" style={{ width: `${progressPct}%` }} />
          </div>
        )}
      </div>

      {/* Meta */}
      <div className="lesson-meta">
        <span className={`lesson-badge ${JLPT_CLASS[jlptLevel] ?? 'jlpt-n5'}`}>{jlptLevel}</span>
        {!isLocked && <ChevronRight />}
      </div>
    </Tag>
  );
}

export default LessonCard;
