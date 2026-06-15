import { JlptBadge } from '../common/Badges';

/* Mũi tên điều hướng + ổ khóa — SVG inline, kế thừa currentColor để CSS đổi màu theo state */
function ArrowIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M9 6l6 6-6 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="5" y="11" width="14" height="9" rx="2" stroke="currentColor" strokeWidth="2" />
      <path d="M8 11V8a4 4 0 0 1 8 0v3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

/**
 * Một card trong lộ trình từ vựng. CÙNG component cho card active & inactive (NFR-VOC-02).
 * Trạng thái khóa/mở/active đến từ backend qua `card.status` — KHÔNG suy luận ở client (§A.3).
 *
 * Props:
 *   card   — { topicId, slug, titleJa, titleVi, order, level, totalWords, completedWords, status }
 *   active — true khi card.status === 'active' (card "START HERE")
 *   onOpen — (card) => void; chỉ gọi khi card không bị khóa
 */
export default function VocabPathCard({ card, active = false, onOpen }) {
  const locked    = card.status === 'locked';
  const completed = card.status === 'completed';
  const pct = card.totalWords > 0
    ? Math.round((card.completedWords / card.totalWords) * 100)
    : 0;

  const cls = [
    'voc-path-card',
    active    && 'voc-path-card--active',
    locked    && 'voc-path-card--locked',
    completed && 'voc-path-card--completed',
  ].filter(Boolean).join(' ');

  return (
    <button
      type="button"
      className={cls}
      aria-current={active ? 'true' : undefined}
      aria-disabled={locked || undefined}
      aria-label={locked
        ? `Bài ${card.order}: ${card.titleVi} — bị khóa, hoàn thành bài trước`
        : `Bài ${card.order}: ${card.titleVi}`}
      onClick={() => !locked && onOpen?.(card)}
    >
      {active && <span className="voc-start-tag" aria-hidden="true">Start Here</span>}

      <span className={`voc-pc-avatar${completed ? ' voc-pc-avatar--done' : ''}`} lang="ja" aria-hidden="true">
        {locked ? <LockIcon /> : completed ? '✓' : card.titleJa.charAt(0)}
      </span>

      <span className="voc-pc-body">
        <span className="voc-pc-title" lang="ja">{card.titleJa}</span>
        <span className="voc-pc-sub">
          Bài {card.order} · {card.titleVi} · {card.completedWords}/{card.totalWords} từ
        </span>
        {card.completedWords > 0 && !locked && (
          <span className="voc-pc-progress" aria-hidden="true">
            <span className="voc-pc-progress-fill" style={{ width: `${pct}%` }} />
          </span>
        )}
      </span>

      <span className="voc-pc-meta">
        <JlptBadge level={card.level} />
        <span className="voc-pc-arrow" aria-hidden="true"><ArrowIcon /></span>
      </span>
    </button>
  );
}
