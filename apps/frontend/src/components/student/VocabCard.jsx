import { useRef } from 'react';

export default function VocabCard({ word, actionState, onComplete, onAddFlashcard }) {
  const audioRef  = useRef(null);
  const isAdding  = actionState[`fc_${word.vocabId}`] === 'adding';
  const isAdded   = word.isInFlashcard || actionState[`fc_${word.vocabId}`] === 'added';
  const isCompl   = actionState[word.vocabId] === 'completing';

  return (
    <div className={`voc-card${word.isCompleted ? ' voc-card--done' : ''}`}>
      <div className="voc-card-main">
        <div className="voc-card-word">
          <span className="voc-word" lang="ja">{word.word}</span>
          <span className="voc-reading" lang="ja">{word.furigana || word.reading}</span>
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
          <span className="voc-pos">{word.wordType || word.partOfSpeech}</span>
          <span className="voc-meaning">{word.meaning}</span>
        </div>
        {(word.exampleSentenceJp || word.exampleSentence) && (
          <div className="voc-example">
            <span lang="ja">{word.exampleSentenceJp || word.exampleSentence}</span>
            {(word.exampleSentenceVi || word.exampleTranslation) && (
              <span className="voc-example-trans"> ({word.exampleSentenceVi || word.exampleTranslation})</span>
            )}
          </div>
        )}
      </div>

      <div className="voc-card-actions">
        <button
          className={`voc-btn-fc${isAdded ? ' voc-btn-fc--added' : ''}`}
          onClick={() => !isAdded && !isAdding && onAddFlashcard(word.vocabId)}
          disabled={isAdded || isAdding}
          aria-label={isAdded ? 'Đã thêm Flashcard' : 'Thêm vào Flashcard'}
        >
          {isAdding ? '...' : isAdded ? '✓ FC' : '+ FC'}
        </button>
        <button
          className={`voc-btn-done${word.isCompleted ? ' voc-btn-done--active' : ''}`}
          onClick={() => !word.isCompleted && !isCompl && onComplete(word.vocabId)}
          disabled={word.isCompleted || isCompl}
          aria-label={word.isCompleted ? 'Đã học' : 'Đánh dấu đã học'}
        >
          {word.isCompleted ? '✓' : isCompl ? '...' : '✓'}
        </button>
      </div>
    </div>
  );
}
