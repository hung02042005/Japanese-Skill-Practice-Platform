import { useRef, useEffect, useState } from 'react';

export default function KanaDetailModal({ kana, isSaving, onComplete, onClose }) {
  const audioRef = useRef(null);
  const [audioError, setAudioError] = useState(false);

  const hasAudio = Boolean(kana.audioUrl);

  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  const playAudio = async () => {
    if (!audioRef.current) return;
    setAudioError(false);
    try {
      audioRef.current.currentTime = 0;
      await audioRef.current.play();
    } catch {
      setAudioError(true);
    }
  };

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
            onClick={playAudio}
            disabled={!hasAudio}
            title={hasAudio ? undefined : 'Chưa có file phát âm cho ký tự này'}
            aria-label={`Nghe phát âm ${kana.character}`}
          >
            ▶ Phát âm
          </button>
          {hasAudio && (
            <audio
              ref={audioRef}
              src={kana.audioUrl}
              preload="none"
              onError={() => setAudioError(true)}
            />
          )}
        </div>
        {audioError && (
          <div className="kna-audio-error" role="alert">
            Không phát được âm thanh. Vui lòng thử lại sau.
          </div>
        )}

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
