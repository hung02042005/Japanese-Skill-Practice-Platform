export default function ExamNavigator({ questions, answers, currentIdx, onJump, onSubmit, unansweredCount }) {
  return (
    <aside className="mxa-navigator-panel" aria-label="Điều hướng câu hỏi">
      <div className="mxa-nav-title">Điều hướng câu</div>

      <div className="mxa-nav-grid" role="list">
        {questions.map((q, i) => {
          const answered = !!answers[q.questionId];
          const current  = i === currentIdx;
          return (
            <button
              key={q.questionId}
              role="listitem"
              className={`mxa-nav-cell${answered ? ' mxa-nav-cell--answered' : ''}${current ? ' mxa-nav-cell--current' : ''}`}
              onClick={() => onJump(i)}
              aria-label={`Câu ${i + 1} — ${answered ? 'đã trả lời' : 'chưa trả lời'}${current ? ' (đang xem)' : ''}`}
              aria-current={current ? 'true' : undefined}
            >
              {i + 1}
            </button>
          );
        })}
      </div>

      <div className="mxa-nav-summary">
        Chưa trả lời: <strong>{unansweredCount}</strong> câu
      </div>

      <button className="mxa-nav-submit-btn" onClick={onSubmit}>
        Nộp bài →
      </button>
    </aside>
  );
}
