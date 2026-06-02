import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import StaffTopNav from '../../components/layout/StaffTopNav';
import './StaffDashboard.css';

const MOCK_STATS = {
  draftCount: 3,
  pendingReviewCount: 7,
  openTicketCount: 12,
  pendingGradingCount: 5,
};

const MOCK_ACTIVITY = [
  { id: 1, date: '03/06/2026', type: 'Bài học', title: 'Bài 3 — Kanji N5 cơ bản', status: 'draft' },
  { id: 2, date: '02/06/2026', type: 'Câu hỏi', title: "N5 Kanji: '水' đọc là gì?", status: 'pending_review' },
  { id: 3, date: '01/06/2026', type: 'Đề thi', title: 'Mock Test N4 Vol.2', status: 'published' },
  { id: 4, date: '31/05/2026', type: 'Từ vựng', title: 'Nhóm từ về gia đình N5', status: 'pending_review' },
  { id: 5, date: '30/05/2026', type: 'Ngữ pháp', title: '～てから (sau khi ~)', status: 'rejected' },
];

const STATUS_MAP = {
  draft: { label: 'Nháp', className: 'sfd-status--draft' },
  pending_review: { label: 'Chờ duyệt', className: 'sfd-status--pending' },
  published: { label: 'Đã xuất bản', className: 'sfd-status--published' },
  rejected: { label: 'Từ chối', className: 'sfd-status--rejected' },
};

// ---- Inline SVG icons ----

function IconPencil() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
  );
}

function IconClock() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="10" />
      <polyline points="12 6 12 12 16 14" />
    </svg>
  );
}

function IconMail() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
      <polyline points="22,6 12,13 2,6" />
    </svg>
  );
}

function IconMic() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
      <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
      <line x1="12" y1="19" x2="12" y2="23" />
      <line x1="8" y1="23" x2="16" y2="23" />
    </svg>
  );
}

function IconEdit() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
  );
}

function IconList() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <line x1="8" y1="6" x2="21" y2="6" />
      <line x1="8" y1="12" x2="21" y2="12" />
      <line x1="8" y1="18" x2="21" y2="18" />
      <line x1="3" y1="6" x2="3.01" y2="6" />
      <line x1="3" y1="12" x2="3.01" y2="12" />
      <line x1="3" y1="18" x2="3.01" y2="18" />
    </svg>
  );
}

function IconMessage() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
    </svg>
  );
}

function IconMicAction() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
      <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
      <line x1="12" y1="19" x2="12" y2="23" />
      <line x1="8" y1="23" x2="16" y2="23" />
    </svg>
  );
}

// ---- Component ----

function StaffDashboard() {
  const navigate = useNavigate();
  const { user } = useAppSelector((state) => state.auth);
  const [stats] = useState(MOCK_STATS);
  const [activity] = useState(MOCK_ACTIVITY);

  return (
    <div className="sfd-page">
      <StaffTopNav activeTab="staff-dashboard" />

      <main className="sfd-body">
        {/* Greeting */}
        <div className="sfd-header">
          <h1 className="sfd-greeting">Xin chào, {user?.fullName ?? 'Staff'} 👋</h1>
          <span className="sfd-date">Thứ Ba, 03/06/2026</span>
        </div>

        {/* Stat row */}
        <div className="sfd-stat-row">
          <div
            className="sfd-stat-card sfd-stat-card--draft"
            onClick={() => navigate('/staff/content?status=draft')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && navigate('/staff/content?status=draft')}
          >
            <div className="sfd-stat-icon" style={{ background: 'var(--color-bg)', color: 'var(--color-text-sub)' }}>
              <IconPencil />
            </div>
            <div>
              <span className="sfd-stat-value">{stats.draftCount}</span>
              <span className="sfd-stat-label">Bài đang soạn</span>
            </div>
          </div>

          <div
            className="sfd-stat-card sfd-stat-card--pending"
            onClick={() => navigate('/staff/content?status=pending_review')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && navigate('/staff/content?status=pending_review')}
          >
            <div className="sfd-stat-icon" style={{ background: 'var(--color-accent-bg)', color: 'var(--color-warning)' }}>
              <IconClock />
            </div>
            <div>
              <span className="sfd-stat-value">{stats.pendingReviewCount}</span>
              <span className="sfd-stat-label">Chờ duyệt</span>
            </div>
          </div>

          <div
            className="sfd-stat-card sfd-stat-card--ticket"
            onClick={() => navigate('/staff/tickets')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && navigate('/staff/tickets')}
          >
            <div className="sfd-stat-icon" style={{ background: 'var(--color-primary-bg)', color: 'var(--color-primary)' }}>
              <IconMail />
            </div>
            <div>
              <span className="sfd-stat-value">{stats.openTicketCount}</span>
              <span className="sfd-stat-label">Tickets mở</span>
            </div>
          </div>

          <div
            className="sfd-stat-card sfd-stat-card--grading"
            onClick={() => navigate('/staff/grading')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && navigate('/staff/grading')}
          >
            <div className="sfd-stat-icon" style={{ background: 'var(--color-secondary-bg)', color: 'var(--color-secondary)' }}>
              <IconMic />
            </div>
            <div>
              <span className="sfd-stat-value">{stats.pendingGradingCount}</span>
              <span className="sfd-stat-label">Bài nói chờ chấm</span>
            </div>
          </div>
        </div>

        {/* Quick actions */}
        <p className="sfd-section-title">Tác vụ nhanh</p>
        <div className="sfd-actions-grid">
          <Link to="/staff/content" className="sfd-action-card">
            <div className="sfd-action-icon">
              <IconEdit />
            </div>
            <div>
              <p className="sfd-action-title">Soạn Nội Dung</p>
              <p className="sfd-action-desc">Tạo bài học, từ vựng, ngữ pháp mới</p>
            </div>
          </Link>

          <Link to="/staff/questions" className="sfd-action-card">
            <div className="sfd-action-icon">
              <IconList />
            </div>
            <div>
              <p className="sfd-action-title">Ngân Hàng Câu Hỏi</p>
              <p className="sfd-action-desc">Thêm và quản lý câu hỏi trắc nghiệm</p>
            </div>
          </Link>

          <Link to="/staff/tickets" className="sfd-action-card">
            <div className="sfd-action-icon">
              <IconMessage />
            </div>
            <div>
              <p className="sfd-action-title">Hỗ Trợ Học Viên</p>
              <p className="sfd-action-desc">Xử lý tickets đang chờ phản hồi</p>
            </div>
          </Link>

          <Link to="/staff/grading" className="sfd-action-card">
            <div className="sfd-action-icon">
              <IconMicAction />
            </div>
            <div>
              <p className="sfd-action-title">Chấm Bài Nói</p>
              <p className="sfd-action-desc">Chấm điểm thủ công bài luyện speaking</p>
            </div>
          </Link>
        </div>

        {/* Activity table */}
        <p className="sfd-section-title">Hoạt động gần đây</p>
        <div className="sfd-activity-wrap">
          {activity.length === 0 ? (
            <p className="sfd-empty-activity">Chưa có hoạt động nào.</p>
          ) : (
            <table className="sfd-table">
              <thead>
                <tr>
                  <th>Thời gian</th>
                  <th>Loại</th>
                  <th>Tiêu đề</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {activity.map((item) => {
                  const s = STATUS_MAP[item.status] ?? { label: item.status, className: '' };
                  return (
                    <tr key={item.id}>
                      <td>{item.date}</td>
                      <td>{item.type}</td>
                      <td>{item.title}</td>
                      <td>
                        <span className={`sfd-status ${s.className}`}>{s.label}</span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </main>
    </div>
  );
}

export default StaffDashboard;
