import { useState } from 'react';
import './FlashcardCard.css';

export default function FlashcardCard({ card, isFlipped, backContent, isFetching, onFlip }) {
  return (
    <div className="fcc-scene" aria-live="polite" aria-atomic="true">
      <div className={`fcc-card${isFlipped ? ' fcc-card--flipped' : ''}`}>
        {/* Front */}
        <div className="fcc-face fcc-face--front" aria-hidden={isFlipped}>
          <div className="fcc-front-content">
            <span className="fcc-front-text" lang="ja">{card.frontText}</span>
            {!isFlipped && (
              <button
                className="fcc-flip-btn"
                onClick={onFlip}
                disabled={isFetching}
                aria-label="Lật thẻ để xem đáp án"
              >
                {isFetching ? (
                  <span className="fcc-spinner" aria-hidden="true" />
                ) : (
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M17 1l4 4-4 4M3 11V9a4 4 0 014-4h14M7 23l-4-4 4-4M21 13v2a4 4 0 01-4 4H3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                )}
                {isFetching ? 'Đang tải...' : '↕ Lật thẻ'}
              </button>
            )}
          </div>
        </div>

        {/* Back */}
        {isFlipped && backContent && (
          <div className="fcc-face fcc-face--back">
            <div className="fcc-back-content">
              {(backContent.furigana || backContent.reading) && (
                <p className="fcc-reading" lang="ja">{backContent.furigana || backContent.reading}</p>
              )}
              <p className="fcc-meaning">{backContent.backText || backContent.meaning}</p>
              {(backContent.exampleSentenceJp || backContent.exampleSentence) && (
                <div className="fcc-example">
                  <p className="fcc-ex-jp" lang="ja">{backContent.exampleSentenceJp || backContent.exampleSentence}</p>
                </div>
              )}
              {backContent.audioUrl && (
                <button
                  className="fcc-audio-btn"
                  onClick={() => new Audio(backContent.audioUrl).play()}
                  aria-label="Nghe phát âm"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M11 5L6 9H2v6h4l5 4V5z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
                    <path d="M15.54 8.46a5 5 0 010 7.07" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  </svg>
                  Phát âm
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
