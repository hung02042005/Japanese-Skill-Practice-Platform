import { useState, useEffect } from 'react';
import './AssignPanel.css';

const CLOSED_STATUSES = ['resolved', 'closed'];

/**
 * Panel phân công ticket cho 1 staff (Manager).
 * Props:
 *   detail        — ticket đang xem (assignedToStaffId/Name, status)
 *   staffList     — [{ staffId, fullName, assignedOpenCount }]
 *   staffError    — string nếu không tải được danh sách
 *   isAssigning
 *   onAssign(staffId) => Promise<void>
 */
export default function AssignPanel({ detail, staffList, staffError, isAssigning, onAssign }) {
  const [assignTo, setAssignTo] = useState('');
  useEffect(() => { setAssignTo(''); }, [detail?.ticketId]);

  if (CLOSED_STATUSES.includes(detail.status)) {
    return (
      <div className="mtk-assign-panel mtk-assign-panel--closed" role="status">
        Ticket đã đóng — không thể phân công.
      </div>
    );
  }

  return (
    <div className="mtk-assign-panel">
      <label className="mtk-assign-label" htmlFor="mtk-assign-select">Giao cho nhân viên</label>
      {staffError ? (
        <p className="mtk-assign-error">{staffError}</p>
      ) : (
        <div className="mtk-assign-row">
          <select
            id="mtk-assign-select"
            className="mtk-assign-select"
            value={assignTo}
            onChange={(e) => setAssignTo(e.target.value)}
          >
            <option value="">— Chọn nhân viên hỗ trợ —</option>
            {staffList.map((s) => (
              <option key={s.staffId} value={s.staffId}>
                {s.fullName} — đang giữ {s.assignedOpenCount ?? 0} ticket
              </option>
            ))}
          </select>
          <button
            type="button"
            className="mtk-assign-btn"
            disabled={!assignTo || isAssigning}
            aria-busy={isAssigning}
            onClick={() => onAssign(Number(assignTo))}
          >
            {isAssigning ? 'Đang giao…' : 'Phân công →'}
          </button>
        </div>
      )}
      {detail.assignedToStaffName && (
        <p className="mtk-assign-current">Hiện đang giao cho: <strong>{detail.assignedToStaffName}</strong></p>
      )}
    </div>
  );
}
