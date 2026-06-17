export default function SpeakingCard({ exercise, onStart }) {
  return (
    <div className="spk-exercise-card">
      <div className="spk-exercise-main">
        <div className="spk-exercise-top">
          <span className="spk-exercise-icon">🎤</span>
          <h3 className="spk-exercise-title">
            {exercise.title}{' '}
            <span className="spk-exercise-level">({exercise.level} — {exercise.category})</span>
          </h3>
        </div>
        <p className="spk-exercise-text" lang="ja">{exercise.targetText}</p>
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
