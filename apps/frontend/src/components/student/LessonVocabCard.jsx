import { useState } from 'react';

export default function LessonVocabCard({ vocab, onAddFlashcard }) {
  const [audioPlaying, setPlaying] = useState(false);

  function playAudio() {
    if (!vocab.audioUrl) return;
    const a = new Audio(vocab.audioUrl);
    a.play();
    setPlaying(true);
    a.onended = () => setPlaying(false);
  }

  return (
    <div className="lvc-card">
      <div className="lvc-main">
        <span className="lvc-word" lang="ja">{vocab.word}</span>
        {vocab.furigana && (
          <span className="lvc-furigana" lang="ja">{vocab.furigana}</span>
        )}
        <span className="lvc-meaning">{vocab.meaning}</span>
        {vocab.audioUrl && (
          <button
            className={`lvc-audio-btn${audioPlaying ? ' lvc-audio-btn--playing' : ''}`}
            onClick={playAudio}
            aria-label={`Nghe phát âm: ${vocab.word}`}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M11 5L6 9H2v6h4l5 4V5z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
              {audioPlaying
                ? <path d="M19.07 4.93a10 10 0 010 14.14M15.54 8.46a5 5 0 010 7.07" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                : <path d="M15.54 8.46a5 5 0 010 7.07" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              }
            </svg>
          </button>
        )}
      </div>
      {vocab.exampleSentenceJp && (
        <div className="lvc-example">
          <span className="lvc-ex-jp" lang="ja">{vocab.exampleSentenceJp}</span>
          {vocab.exampleSentenceVi && (
            <span className="lvc-ex-vi">{vocab.exampleSentenceVi}</span>
          )}
        </div>
      )}
      <button
        className="lvc-add-btn"
        onClick={onAddFlashcard}
        aria-label={`Thêm "${vocab.word}" vào Flashcard`}
      >
        + Flashcard
      </button>
    </div>
  );
}
