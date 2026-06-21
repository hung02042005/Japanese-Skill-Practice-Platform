import { JlptBadge } from '../common/Badges';

/**
 * Một nhóm kết quả tra cứu (vocab/kanji/grammar/lesson) — SPEC-dictionary §7.
 * Chỉ nhóm "vocabulary" mới có nút "Lưu vào sổ tay" (canSave); các loại khác
 * click để điều hướng tới trang chi tiết tương ứng.
 */
const FIELD = {
  vocabulary: (i) => ({ id: i.id, main: i.word,      sub: i.furigana, meaning: i.meaning, level: i.jlptLevel }),
  kanji:      (i) => ({ id: i.id, main: i.character, sub: '',         meaning: i.meaning, level: i.jlptLevel }),
  grammar:    (i) => ({ id: i.id, main: i.structure, sub: '',         meaning: i.meaning, level: i.jlptLevel }),
  lesson:     (i) => ({ id: i.id, main: i.title,     sub: '',         meaning: '',        level: i.jlptLevel }),
};

export default function DictResultGroup({ title, items = [], type, savedIds, savingId, onOpen, onSave, canSave }) {
  if (!items || items.length === 0) return null;
  const map = FIELD[type];

  return (
    <section className="dct-group">
      <h2 className="dct-group-title">
        {title} <span className="dct-group-count">({items.length})</span>
      </h2>
      <div className="dct-group-list">
        {items.map((raw) => {
          const it = map(raw);
          const key = `${type}:${it.id}`;
          const saved = savedIds?.has(key);
          return (
            <div key={key} className="dct-result-row">
              <button
                className="dct-result-main"
                onClick={() => onOpen(it.id, raw)}
                aria-label={`Mở ${it.main}`}
              >
                {it.level && <JlptBadge level={it.level} />}
                <span className="dct-result-word" lang="ja">{it.main}</span>
                {it.sub && <span className="dct-result-sub" lang="ja">・{it.sub}</span>}
                {it.meaning && <span className="dct-result-meaning">{it.meaning}</span>}
              </button>
              {canSave && (
                <button
                  className={`dct-save-btn${saved ? ' dct-save-btn--saved' : ''}`}
                  onClick={() => onSave(it.id)}
                  disabled={saved || savingId === key}
                  aria-label={saved ? 'Đã lưu vào sổ tay' : 'Lưu vào sổ tay'}
                  aria-pressed={saved}
                >
                  {savingId === key
                    ? <span className="dct-spinner dct-spinner--sm" aria-hidden="true" />
                    : (saved ? '♥ Đã lưu' : '♡ Lưu vào sổ tay')}
                </button>
              )}
            </div>
          );
        })}
      </div>
    </section>
  );
}
