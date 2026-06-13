import { useState, useEffect, useRef, useCallback } from 'react';

const STROKE_DELAY_MS = 900;

/**
 * Hiển thị animation viết từng nét Kanji.
 * @param {{ character: string, strokeCount: number, strokes: null|Array, onStrokeChange: (n:number)=>void }} props
 * strokes: null = placeholder (chờ tích hợp KanjiVG), Array = dữ liệu path SVG thực
 */
export default function StrokeOrderPlayer({ character, strokeCount, strokes = null, onStrokeChange }) {
  const total = Math.max(strokeCount || 1, 1);
  const [current, setCurrent] = useState(0);
  const [playing, setPlaying] = useState(false);
  const timerRef = useRef(null);

  const stopTimer = useCallback(() => {
    clearInterval(timerRef.current);
  }, []);

  // Reset khi đổi kanji
  useEffect(() => {
    stopTimer();
    setCurrent(0);
    setPlaying(false);
  }, [character, stopTimer]);

  useEffect(() => {
    onStrokeChange?.(current);
  }, [current, onStrokeChange]);

  useEffect(() => {
    if (!playing) {
      stopTimer();
      return;
    }
    timerRef.current = setInterval(() => {
      setCurrent(prev => {
        const next = prev + 1;
        if (next >= total) {
          clearInterval(timerRef.current);
          setPlaying(false);
          return total;
        }
        return next;
      });
    }, STROKE_DELAY_MS);
    return () => clearInterval(timerRef.current);
  }, [playing, total, stopTimer]);

  const handlePlay = () => {
    if (current >= total) setCurrent(0);
    setPlaying(true);
  };

  const handlePause = () => {
    stopTimer();
    setPlaying(false);
  };

  const handleReplay = () => {
    stopTimer();
    setCurrent(0);
    setPlaying(true);
  };

  const revealRatio = total > 0 ? current / total : 0;

  return (
    <div className="kd-player">
      {/* Stage */}
      <div className="kd-player-stage" aria-label={`Hướng dẫn viết ${character}, nét ${current}/${total}`}>
        {/* Guide character (mờ) */}
        <span className="kd-player-guide" lang="ja" aria-hidden="true">{character}</span>

        {/* Overlay character (lộ dần theo tỉ lệ nét) */}
        <span
          className="kd-player-reveal"
          lang="ja"
          aria-hidden="true"
          style={{ clipPath: `inset(0 ${Math.round((1 - revealRatio) * 100)}% 0 0)` }}
        >
          {character}
        </span>

        {/* Dot indicators */}
        <div className="kd-player-dots" aria-hidden="true">
          {Array.from({ length: total }, (_, i) => (
            <span
              key={i}
              className={
                'kd-player-dot' +
                (i < current ? ' kd-player-dot--done' : '') +
                (i === current - 1 ? ' kd-player-dot--latest' : '')
              }
            />
          ))}
        </div>

        {/* Progress label */}
        <span className="kd-player-counter" aria-live="polite">
          {current === 0 ? '—' : `${current} / ${total}`}
        </span>
      </div>

      {/* Controls */}
      <div className="kd-player-controls">
        {!playing ? (
          <button className="kd-ctrl-btn kd-ctrl-btn--play" onClick={handlePlay} aria-label="Phát animation">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
              <polygon points="4,2 14,8 4,14" />
            </svg>
            {current === 0 ? 'Phát' : current >= total ? 'Phát lại' : 'Tiếp tục'}
          </button>
        ) : (
          <button className="kd-ctrl-btn kd-ctrl-btn--pause" onClick={handlePause} aria-label="Dừng animation">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
              <rect x="3" y="2" width="4" height="12" /><rect x="9" y="2" width="4" height="12" />
            </svg>
            Dừng
          </button>
        )}

        <button className="kd-ctrl-btn kd-ctrl-btn--replay" onClick={handleReplay} aria-label="Xem lại từ đầu">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <polyline points="1 4 1 10 7 10" />
            <path d="M3.51 15a9 9 0 1 0 .49-3.51" />
          </svg>
          Xem lại
        </button>
      </div>

      {strokes === null && (
        <p className="kd-player-note">
          Animation chi tiết từng nét sẽ được tích hợp qua dữ liệu KanjiVG.
        </p>
      )}
    </div>
  );
}
