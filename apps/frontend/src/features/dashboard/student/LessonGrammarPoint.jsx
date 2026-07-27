import { useState } from 'react';

export default function LessonGrammarPoint({ grammar }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div className="lgp-card">
      <button
        className="lgp-header"
        onClick={() => setExpanded((v) => !v)}
        aria-expanded={expanded}
        aria-controls={`lgp-body-${grammar.grammarId}`}
      >
        <div className="lgp-header-left">
          <span className="lgp-structure" lang="ja">{grammar.structure}</span>
          {grammar.formula && (
            <span className="lgp-formula">{grammar.formula}</span>
          )}
        </div>
        <span className="lgp-meaning">{grammar.meaning}</span>
        <svg
          className={`lgp-chevron${expanded ? ' lgp-chevron--open' : ''}`}
          width="16" height="16" viewBox="0 0 24 24" fill="none"
          aria-hidden="true"
        >
          <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </button>

      {expanded && (
        <div id={`lgp-body-${grammar.grammarId}`} className="lgp-body">
          {grammar.usageExplanation && (
            <p className="lgp-usage">{grammar.usageExplanation}</p>
          )}
          {grammar.exampleSentenceJp && (
            <div className="lgp-example">
              <p className="lgp-ex-jp" lang="ja">{grammar.exampleSentenceJp}</p>
              {grammar.exampleSentenceVi && (
                <p className="lgp-ex-vi">{grammar.exampleSentenceVi}</p>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
