import { ClockIcon } from '../common/AppIcons';
import './SubmissionList.css';

const LEVEL_COLORS = {
  N5: { bg: '#E8F5E9', text: '#2E7D32' },
  N4: { bg: '#E3F2FD', text: '#1565C0' },
  N3: { bg: '#FFF3E0', text: '#E65100' },
  N2: { bg: '#F3E5F5', text: '#6A1B9A' },
  N1: { bg: '#FCE4EC', text: '#C62828' },
};

function formatDuration(seconds) {
  const m = Math.floor(seconds / 60);
  const s = String(seconds % 60).padStart(2, '0');
  return `${m}:${s} phút`;
}

function formatDate(iso) {
  const d = new Date(iso);
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function StatusPill({ status }) {
  if (status === 'pending')    return <span className="grd-sub-pill" style={{ background: '#FFF3E0', color: 'var(--color-warning)' }}>Chưa qua AI</span>;
  if (status === 'ai_graded') return <span className="grd-sub-pill" style={{ background: 'var(--color-primary-bg)', color: 'var(--color-primary-dark)' }}>Chờ chấm</span>;
  if (status === 'graded')    return <span className="grd-sub-pill" style={{ background: 'var(--color-secondary-bg)', color: 'var(--color-secondary)' }}>Đã chấm ✓</span>;
  return null;
}

export default function SubmissionList({ submissions, selectedId, statusTab, pendingCount, isLoading, onSelect, onTabChange }) {
  return (
    <div className="grd-list-col">
      <div className="grd-tabs" role="tablist">
        <button
          className={`grd-tab${statusTab === 'pending' ? ' grd-tab--active' : ''}`}
          role="tab"
          aria-selected={statusTab === 'pending'}
          onClick={() => onTabChange('pending')}
        >
          Chờ chấm
          {pendingCount > 0 && <span className="grd-tab-badge" aria-label={`${pendingCount} bài chờ chấm`}>{pendingCount}</span>}
        </button>
        <button
          className={`grd-tab${statusTab === 'graded' ? ' grd-tab--active' : ''}`}
          role="tab"
          aria-selected={statusTab === 'graded'}
          onClick={() => onTabChange('graded')}
        >
          Đã chấm
        </button>
      </div>

      <div className="grd-list-scroll" role="list">
        {isLoading ? (
          <div className="grd-empty-list">Đang tải...</div>
        ) : submissions.length === 0 ? (
          <div className="grd-empty-list">Không có bài nộp nào.</div>
        ) : (
          submissions.map((sub) => {
            const lc = LEVEL_COLORS[sub.jlptLevel] ?? {};
            return (
              <div
                key={sub.submissionId}
                className={`grd-sub-card${selectedId === sub.submissionId ? ' grd-sub-card--active' : ''}`}
                role="listitem"
                aria-selected={selectedId === sub.submissionId}
                onClick={() => onSelect(sub.submissionId)}
              >
                <div className="grd-sub-top">
                  <span className="grd-sub-name">{sub.studentName}</span>
                  <span
                    className="grd-level-chip"
                    style={{ background: lc.bg, color: lc.text }}
                  >
                    {sub.jlptLevel}
                  </span>
                </div>
                <div className="grd-sub-meta">
                  <span>{formatDate(sub.submittedAt)}</span>
                  <span><ClockIcon size={14} /> {formatDuration(sub.durationSeconds)}</span>
                </div>
                <div className="grd-sub-footer">
                  <span className="grd-sub-ai">
                    AI: {sub.aiOverallScore ?? '—'}/100
                  </span>
                  <StatusPill status={sub.status} />
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
