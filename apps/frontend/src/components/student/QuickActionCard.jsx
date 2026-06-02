import { useNavigate } from 'react-router-dom';
import './QuickActionCard.css';

const ACTIONS = {
  flashcard: {
    label: 'Ôn Flashcard',
    desc:  'Spaced repetition',
    route: '/flashcard',
    emoji: '🃏',
    mod:   'flashcard',
  },
  exam: {
    label: 'Thi Thử JLPT',
    desc:  'Mock exam đầy đủ',
    route: '/mock-test',
    emoji: '📋',
    mod:   'exam',
  },
  dictionary: {
    label: 'Từ Điển',
    desc:  'Tra từ nhanh',
    route: '/dictionary',
    emoji: '🔍',
    mod:   'dictionary',
  },
  progress: {
    label: 'Tiến Độ',
    desc:  'Xem kết quả học',
    route: '/progress',
    emoji: '📊',
    mod:   'progress',
  },
};

const ChevronRight = () => (
  <svg className="qa-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
    <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

function QuickActionCard({ type }) {
  const navigate = useNavigate();
  const action   = ACTIONS[type];

  if (!action) return null;

  return (
    <button
      type="button"
      className="qa-card"
      onClick={() => navigate(action.route)}
      aria-label={action.label}
    >
      <div className={`qa-icon qa-icon--${action.mod}`} aria-hidden="true">
        <span>{action.emoji}</span>
      </div>
      <div className="qa-content">
        <div className="qa-title">{action.label}</div>
        <div className="qa-desc">{action.desc}</div>
      </div>
      <ChevronRight />
    </button>
  );
}

export default QuickActionCard;
