export default function ContentStatusActions({ item, onEdit, onSubmit, onView }) {
  const canEdit = item.status === 'draft' || item.status === 'rejected';
  const canSubmit = item.status === 'draft' || item.status === 'rejected';
  const canView = item.status === 'pending_review' || item.status === 'published';
  return (
    <div className="sfc-actions">
      {canEdit && (
        <button className="sfc-btn-icon" onClick={() => onEdit(item)} aria-label={`Sửa ${item.title || item.word || item.pattern || item.character}`} title="Sửa">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>
      )}
      {canSubmit && (
        <button className="sfc-btn--submit" onClick={() => onSubmit(item)} aria-label={`Gửi duyệt ${item.title || item.word || item.pattern || item.character}`}>
          Gửi duyệt
        </button>
      )}
      {canView && (
        <button className="sfc-btn-icon" onClick={() => onView(item)} aria-label={`Xem ${item.title || item.word || item.pattern || item.character}`} title="Xem">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
          </svg>
        </button>
      )}
    </div>
  );
}
