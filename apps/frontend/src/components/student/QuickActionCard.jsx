import { useNavigate } from 'react-router-dom';
import { FlashcardIcon, ExamIcon, DictionaryIcon, ChartIcon } from './StudentIcons';
import './QuickActionCard.css';

const ACTIONS = {
  flashcard: {
    label: 'Ôn Flashcard',
    desc:  'Spaced repetition',
    route: '/flashcard',
    Icon:  FlashcardIcon,
    mod:   'flashcard',
  },
  exam: {
    label: 'Thi Thử JLPT',
    desc:  'Mock exam đầy đủ',
    route: '/mock-test',
    Icon:  ExamIcon,
    mod:   'exam',
  },
  dictionary: {
    label: 'Từ Điển',
    desc:  'Tra từ nhanh',
    route: '/dictionary',
    Icon:  DictionaryIcon,
    mod:   'dictionary',
  },
  progress: {
    label: 'Tiến Độ',
    desc:  'Xem kết quả học',
    route: '/progress',
    Icon:  ChartIcon,
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
        <action.Icon size={20} />
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
