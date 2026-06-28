import { JlptBadge } from '../common/Badges';

/**
 * Một từ trong Sổ tay "Từ cần ôn lại" — SPEC-notebook §7.
 * Hiển thị từ + nghĩa + nguồn (vì sao vào sổ) + lịch ôn tiếp, kèm nút gỡ.
 */
const REASON_LABEL = {
  wrong:  'trả lời sai',
  manual: 'tự lưu từ Từ điển',
  learn:  'thêm từ bài học',
};

function nextLabel(iso, isDue) {
  if (isDue) return 'hôm nay';
  if (!iso) return '—';
  const days = Math.ceil((new Date(iso) - Date.now()) / 86400000);
  if (days <= 1) return 'ngày mai';
  return new Date(iso).toLocaleDateString('vi-VN');
}

export default function NotebookWordCard({
  word, onRemove, selectable = false, selected = false, onToggleSelect,
}) {
  const {
    frontText, meaning, jlptLevel, isDue,
    nextReviewDate, addedReason, sourceTopic, furigana, audioUrl,
  } = word;

  function playAudio(e) {
    e.stopPropagation();
    if (audioUrl) new Audio(audioUrl).play().catch(() => {});
  }

  return (
    <article className={`nwc-card${isDue ? ' nwc-card--due' : ''}${selected ? ' nwc-card--selected' : ''}`}>
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
              🔊
            </button>
          )}
          {isDue && <span className="nwc-due" aria-label="Đến hạn ôn">● đến hạn</span>}
          {!selectable && (
            <button className="nwc-del" onClick={onRemove} aria-label="Gỡ khỏi sổ tay">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
          )}
        </div>
        {meaning && <p className="nwc-meaning">{meaning}</p>}
        <p className="nwc-meta">
          {addedReason && (
            <>Nguồn: {REASON_LABEL[addedReason] ?? addedReason}{sourceTopic ? ` (Topic: ${sourceTopic})` : ''} · </>
          )}
          Ôn tiếp: {nextLabel(nextReviewDate, isDue)}
        </p>
      </div>
    </article>
  );
}
