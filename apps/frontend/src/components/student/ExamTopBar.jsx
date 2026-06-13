import AppLogo from '../common/AppLogo';
import './ExamTopBar.css';

export default function ExamTopBar({ title, timeString, isUrgent, onSubmit, isSubmitting }) {
  return (
    <header className="etb-bar" role="banner">
      <div className="etb-logo">
        <AppLogo size={28} />
      </div>
      <div className="etb-title">{title}</div>
      <div
        className={`etb-timer${isUrgent ? ' etb-timer--urgent' : ''}`}
        aria-live={isUrgent ? 'assertive' : 'off'}
        aria-atomic="true"
        aria-label={`Thời gian còn lại: ${timeString}`}
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
          <path d="M12 6v6l4 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        </svg>
        {timeString}
      </div>
      <button
        className="etb-submit-btn"
        onClick={onSubmit}
        disabled={isSubmitting}
        aria-label="Nộp bài thi"
      >
        {isSubmitting ? 'Đang nộp...' : 'Nộp bài'}
      </button>
    </header>
  );
}
