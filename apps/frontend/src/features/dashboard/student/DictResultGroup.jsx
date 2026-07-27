import { JlptBadge } from '@/shared/components/common/Badges';
import { SpeakerIcon, HeartIcon } from '@/shared/components/common/AppIcons';

/**
 * Một nhóm kết quả tra cứu (vocab/kanji/grammar/lesson) — SPEC-dictionary §7.
 * Chỉ nhóm "vocabulary" mới có nút "Lưu vào sổ tay" (canSave); các loại khác
 * click để điều hướng tới trang chi tiết tương ứng.
 */
const FIELD = {
  vocabulary: (i) => ({ id: i.id, main: i.word,      sub: i.furigana, meaning: i.meaning, level: i.jlptLevel, audio: i.audioUrl }),
  kanji:      (i) => ({ id: i.id, main: i.character, sub: '',         meaning: i.meaning, level: i.jlptLevel }),
  grammar:    (i) => ({ id: i.id, main: i.structure, sub: '',         meaning: i.meaning, level: i.jlptLevel }),
  lesson:     (i) => ({ id: i.id, main: i.title,     sub: '',         meaning: '',        level: i.jlptLevel }),
};

function playAudio(url) {
  if (url) new Audio(url).play().catch(() => {});
}

export default function DictResultGroup({
  title, items = [], type, savedIds, savingId, onOpen, onSave, canSave,
  hasMore = false, loadingMore = false, onMore,
}) {
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
              {it.audio && (
                <button
                  className="dct-audio-btn"
                  onClick={() => playAudio(it.audio)}
                  aria-label={`Nghe phát âm ${it.main}`}
                >
                  <SpeakerIcon size={18} />
                </button>
              )}
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
                    : (saved
                        ? <><HeartIcon size={15} filled /> Đã lưu</>
                        : <><HeartIcon size={15} filled={false} /> Lưu vào sổ tay</>)}
                </button>
              )}
            </div>
          );
        })}
      </div>
      {/* 1B: nút "Xem thêm" — nối thêm trang kế cho riêng nhóm này */}
      {hasMore && onMore && (
        <button className="dct-more-btn" onClick={onMore} disabled={loadingMore}>
          {loadingMore ? 'Đang tải…' : 'Xem thêm'}
        </button>
      )}
    </section>
  );
}
