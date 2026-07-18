import { useRef } from 'react';

export default function VocabCard({ word, actionState, onComplete }) {
  const audioRef  = useRef(null);
  const isCompl   = actionState[word.id] === 'completing';

  return (
    <div className={`voc-card${word.isCompleted ? ' voc-card--done' : ''}`}>
      <div className="voc-card-main">
        <div className="voc-card-word">
          <span className="voc-word" lang="ja">{word.word}</span>
          <span className="voc-reading" lang="ja">{word.furigana}</span>
          {word.audioUrl && (
            <>
              <button
                className="voc-btn-audio"
                onClick={() => audioRef.current?.play()}
                aria-label={`Nghe phát âm ${word.word}`}
              >▶</button>
              <audio ref={audioRef} src={word.audioUrl} preload="none" />
            </>
          )}
        </div>
        <div className="voc-card-info">
          <span className="voc-pos">{word.wordType}</span>
          <span className="voc-meaning">{word.meaning}</span>
        </div>
        {word.exampleSentenceJp && (
          <div className="voc-example">
            <span lang="ja">{word.exampleSentenceJp}</span>
            {word.exampleSentenceVi && (
              <span className="voc-example-trans"> ({word.exampleSentenceVi})</span>
            )}
          </div>
        )}
      </div>

      <div className="voc-card-actions">
        <button
          className={`voc-btn-done${word.isCompleted ? ' voc-btn-done--active' : ''}`}
          onClick={() => !word.isCompleted && !isCompl && onComplete(word.id)}
          disabled={word.isCompleted || isCompl}
          aria-label={word.isCompleted ? 'Đã học' : 'Đánh dấu đã học'}
        >
          {word.isCompleted ? '✓' : isCompl ? '...' : '✓'}
        </button>
      </div>
    </div>
  );
}
