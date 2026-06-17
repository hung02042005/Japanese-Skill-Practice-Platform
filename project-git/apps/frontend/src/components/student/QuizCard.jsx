import { JlptBadge } from '../common/Badges';

const SKILL_LABELS = {
  vocabulary: 'Từ vựng',
  grammar:    'Ngữ pháp',
  reading:    'Đọc hiểu',
  listening:  'Nghe',
};

export default function QuizCard({ quiz, onStart }) {
  return (
    <div className="qz-card">
      <div className="qz-card-main">
        <div className="qz-card-top">
          <span className="qz-card-icon">📝</span>
          <JlptBadge level={quiz.jlptLevel} />
          <span className="qz-card-skill">{SKILL_LABELS[quiz.skill] ?? quiz.skill}</span>
          <span className="qz-card-count">{quiz.questionCount} câu</span>
        </div>
        <h3 className="qz-card-title">{quiz.title}</h3>
        <p className="qz-card-meta">
          {quiz.attemptCount > 0
            ? `Đã làm: ${quiz.attemptCount} lần · Tốt nhất: ${quiz.bestScore}%`
            : 'Chưa làm'}
        </p>
      </div>
      <button
        className="qz-card-btn"
        onClick={() => onStart(quiz)}
        aria-label={`Làm quiz: ${quiz.title}`}
      >
        Làm quiz →
      </button>
    </div>
  );
}
