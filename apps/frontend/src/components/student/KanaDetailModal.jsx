import { useRef, useEffect } from 'react';

export default function KanaDetailModal({ kana, isSaving, onComplete, onClose }) {
  const audioRef = useRef(null);

  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  return (
    <div
      className="kna-modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-label={`Chi tiết: ${kana.character}`}
      onClick={onClose}
    >
      <div className="kna-modal" onClick={(e) => e.stopPropagation()}>
        <button className="kna-modal-close" onClick={onClose} aria-label="Đóng">×</button>
        <div className="kna-modal-char" lang="ja">{kana.character}</div>
        <div className="kna-modal-romaji">({kana.romaji})</div>

        <div className="kna-modal-actions-row">
          <button
            className="kna-btn-audio"
            onClick={() => audioRef.current?.play()}
            aria-label={`Nghe phát âm ${kana.character}`}
          >
            ▶ Phát âm
          </button>
          <audio ref={audioRef} src={kana.audioUrl} preload="none" />
        </div>

        {kana.strokeGifUrl && (
          <img
            className="kna-stroke-gif"
            src={kana.strokeGifUrl}
            alt={`Thứ tự nét của ${kana.character}`}
          />
        )}

        {kana.exampleWord && (
          <div className="kna-example">
            <span className="kna-example-word" lang="ja">{kana.exampleWord}</span>
            <span className="kna-example-reading">({kana.exampleReading})</span>
            <span className="kna-example-meaning"> — {kana.exampleMeaning}</span>
          </div>
        )}

        <button
          className={`kna-btn-complete${kana.isCompleted ? ' kna-btn-complete--done' : ''}`}
          onClick={() => onComplete(kana)}
          disabled={kana.isCompleted || isSaving}
          aria-label={kana.isCompleted ? 'Đã học' : 'Đánh dấu đã học'}
        >
          {kana.isCompleted ? '✓ Đã học' : isSaving ? 'Đang lưu...' : '✓ Đánh dấu đã học'}
        </button>
      </div>
    </div>
  );
}
