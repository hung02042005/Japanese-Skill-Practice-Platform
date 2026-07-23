import { JlptBadge } from '@/shared/components/common/Badges';

/**
 * Chi tiết một từ vựng từ kết quả tra cứu — SPEC-dictionary §6/§7.
 * Hiển thị từ + furigana + nghĩa + ví dụ song ngữ (nếu payload có) + nút Phát âm,
 * và nút Lưu/Đã lưu vào Sổ tay. Dữ liệu lấy trực tiếp từ item kết quả search.
 */
export default function DictDetailPanel({ item, isSaved, isSaving, onSave, onBack }) {
  const { word, furigana, meaning, jlptLevel, wordType, exampleJp, exampleVi, audioUrl } = item;

  function playAudio() {
    if (audioUrl) new Audio(audioUrl).play().catch(() => {});
  }

  return (
    <section className="dct-detail" aria-label={`Chi tiết ${word}`}>
      <button className="dct-detail-back" onClick={onBack}>← Kết quả</button>

      <div className="dct-detail-head">
        <div className="dct-detail-word-wrap">
          {jlptLevel && <JlptBadge level={jlptLevel} />}
          <span className="dct-detail-word" lang="ja">{word}</span>
          {furigana && <span className="dct-detail-furi" lang="ja">・{furigana}</span>}
        </div>
        <button
          className={`dct-save-btn${isSaved ? ' dct-save-btn--saved' : ''}`}
          onClick={onSave}
          disabled={isSaved || isSaving}
          aria-pressed={isSaved}
        >
          {isSaving
            ? <span className="dct-spinner dct-spinner--sm" aria-hidden="true" />
            : (isSaved ? '♥ Đã lưu' : '♡ Lưu vào sổ tay')}
        </button>
      </div>

      {(meaning || wordType) && (
        <p className="dct-detail-meaning">
          {meaning}
          {wordType && <span className="dct-detail-type"> · {wordType}</span>}
        </p>
      )}

      {exampleJp && (
        <div className="dct-detail-example">
          <p lang="ja">{exampleJp}</p>
          {exampleVi && <p className="dct-detail-example-vi">{exampleVi}</p>}
        </div>
      )}

      {audioUrl && (
        <button className="dct-detail-audio" onClick={playAudio} aria-label="Phát âm">
          ▶ Phát âm
        </button>
      )}
    </section>
  );
}
