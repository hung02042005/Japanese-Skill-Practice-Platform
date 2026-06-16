export default function QuizQuestion({ question, selectedOptionId, onAnswer }) {
  return (
    <div className="qz-question-card">
      <p className="qz-question-text">{question.content}</p>
      <div className="qz-options" role="radiogroup" aria-label="Chọn đáp án">
        {question.options.map((opt) => (
          <button
            key={opt.optionId}
            role="radio"
            aria-checked={selectedOptionId === opt.optionId}
            className={`qz-option${selectedOptionId === opt.optionId ? ' qz-option--selected' : ''}`}
            onClick={() => onAnswer(question.questionId, opt.optionId)}
          >
            <span className="qz-option-label">{opt.label}.</span>
            <span className="qz-option-text" lang="ja">{opt.text}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
