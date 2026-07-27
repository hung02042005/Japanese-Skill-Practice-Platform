import { JlptBadge } from '@/shared/components/common/Badges';
import { SpeakerIcon } from '@/shared/components/common/AppIcons';

/**
 * Một từ trong Sổ tay "Từ cần ôn lại".
 * Hiển thị từ + cách đọc + nghĩa + nguồn (vì sao vào sổ), kèm nút gỡ.
 */
const REASON_LABEL = {
  wrong:  'trả lời sai',
  manual: 'tự lưu từ Từ điển',
  learn:  'thêm từ bài học',
};

export default function NotebookWordCard({
  word, onRemove, selectable = false, selected = false, onToggleSelect,
}) {
  const {
    frontText, meaning, jlptLevel,
    addedReason, furigana, audioUrl,
  } = word;

  function playAudio(e) {
    e.stopPropagation();
    if (audioUrl) new Audio(audioUrl).play().catch(() => {});
  }

  return (
    <article className={`nwc-card${selected ? ' nwc-card--selected' : ''}`}>
      {selectable && (
        <label className="nwc-select" aria-label={`Chọn ${frontText}`}>
          <input type="checkbox" checked={selected} onChange={onToggleSelect} />
        </label>
      )}
      <div className="nwc-main">
        <div className="nwc-head">
          {jlptLevel && <JlptBadge level={jlptLevel} />}
          <span className="nwc-word" lang="ja">{frontText}</span>
          {furigana && <span className="nwc-furi" lang="ja">・{furigana}</span>}
          {audioUrl && (
            <button type="button" className="nwc-audio" onClick={playAudio} aria-label="Nghe phát âm">
              <SpeakerIcon size={18} />
            </button>
          )}
          {!selectable && (
            <button className="nwc-del" onClick={onRemove} aria-label="Gỡ khỏi sổ tay">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
          )}
        </div>
        {meaning && <p className="nwc-meaning">{meaning}</p>}
        {addedReason && (
          <p className="nwc-meta">
            Nguồn: {REASON_LABEL[addedReason] ?? addedReason}
          </p>
        )}
      </div>
    </article>
  );
}
