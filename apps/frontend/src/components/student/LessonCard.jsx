import { useNavigate } from 'react-router-dom';
import { JlptBadge } from '../common/Badges';
import { ProgressBar } from '../common/ProgressBar';
import './LessonCard.css';

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

const TYPE_ROUTES = {
  KANJI:     (level) => `/kanji?level=${level}`,
  VOCAB:     (level) => `/vocabulary?level=${level}`,
  GRAMMAR:   (level) => `/grammar?level=${level}`,
  SPEAKING:  (level) => `/speaking?level=${level}`,
  KANA:      (_,     title) => title?.toLowerCase().includes('katakana') ? '/kana?script=katakana' : '/kana?script=hiragana',
};

function LessonCard({ lesson }) {
  const navigate = useNavigate();
  const { id, title, description, jlptLevel, lessonType, status, progress, thumbnail } = lesson;

  const isLocked    = status === 'locked';
  const isActive    = status === 'active';
  const progressPct = Math.round((progress ?? 0) * 100);

  function handleClick() {
    if (isLocked) return;
    const routeFn = TYPE_ROUTES[lessonType];
    if (routeFn) {
      navigate(routeFn(jlptLevel, title));
    } else {
      navigate(`/lessons/${id}`);
    }
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

      <div className={`lesson-thumb${isActive ? ' lesson-thumb--active' : ''}${isLocked ? ' lesson-thumb--locked' : ''}`}>
        {isLocked
          ? <LockIcon />
          : <span className="lesson-thumb-char">{thumbnail}</span>
        }
      </div>

      <div className="lesson-content">
        <div className="lesson-title">{title}</div>
        <div className="lesson-desc">{description}</div>
        {progressPct > 0 && (
          <ProgressBar
            value={progressPct}
            max={100}
            height={4}
            color="var(--color-secondary)"
            label={`${progressPct}% hoàn thành`}
          />
        )}
      </div>

      <div className="lesson-meta">
        <JlptBadge level={jlptLevel} />
        {!isLocked && <ChevronRight />}
      </div>
    </Tag>
  );
}

export default LessonCard;
