import { MicIcon } from '@/shared/components/common/StudentIcons';

export default function SpeakingCard({ exercise, onStart }) {
  const questions = exercise.questions ?? [];
  const previewText = questions[0]?.promptText ?? exercise.targetText;
  return (
    <div className="spk-exercise-card">
      <div className="spk-exercise-main">
        <div className="spk-exercise-top">
          <span className="spk-exercise-icon"><MicIcon size={20} /></span>
          <h3 className="spk-exercise-title">
            {exercise.title}{' '}
            <span className="spk-exercise-level">({exercise.level} — {exercise.category})</span>
          </h3>
        </div>
        <p className="spk-exercise-text" lang="ja">{previewText}</p>
        {questions.length > 1 && <p className="spk-exercise-meta">{questions.length} câu luyện nói</p>}
        <p className="spk-exercise-meta">
          {exercise.attemptCount > 0
            ? `Đã luyện: ${exercise.attemptCount} lần · Điểm tốt nhất: ${exercise.bestScore}%`
            : 'Chưa luyện'}
        </p>
      </div>
      <button
        className="spk-exercise-btn"
        onClick={() => onStart(exercise)}
        aria-label={`Luyện tập: ${exercise.title}`}
      >
        Luyện tập →
      </button>
    </div>
  );
}
