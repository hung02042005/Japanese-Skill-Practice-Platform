import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import StaffTopNav from '../../components/layout/StaffTopNav';
import StaffPageHero from '../../components/staff/StaffPageHero';
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

// ---- Inline SVG icons (chủ đề Nhật Bản / Hanami) ----

// Bút lông thư pháp (筆) — bài đang soạn
function IconPencil() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M17 3 L21 7 L10 18 Q7 21 4 19 Q2 17 5 14 Z"/>
      <line x1="14" y1="6" x2="18" y2="10"/>
      <path d="M5 14 Q4 16 4 19"/>
    </svg>
  );
}

// Đồng hồ cát (砂時計) — chờ duyệt
function IconClock() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <line x1="5" y1="3" x2="19" y2="3"/>
      <line x1="5" y1="21" x2="19" y2="21"/>
      <path d="M5 3 L12 11 L19 3"/>
      <path d="M5 21 L12 13 L19 21"/>
      <line x1="9" y1="18.5" x2="15" y2="18.5"/>
    </svg>
  );
}

// Hạc giấy origami (折り鶴) — hỗ trợ học viên
function IconMail() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M2 15 Q7 8 12 11 Q17 8 22 15"/>
      <path d="M12 11 L11 16 L9 20"/>
      <path d="M12 11 Q14 7 16 5"/>
    </svg>
  );
}

// Quạt xếp (扇子) — bài nói / chấm điểm
function IconMic() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <line x1="12" y1="18" x2="4" y2="8"/>
      <line x1="12" y1="18" x2="8" y2="5"/>
      <line x1="12" y1="18" x2="12" y2="4"/>
      <line x1="12" y1="18" x2="16" y2="5"/>
      <line x1="12" y1="18" x2="20" y2="8"/>
      <path d="M4 8 Q8 3.5 12 4 Q16 3.5 20 8"/>
      <circle cx="12" cy="18" r="1.5" fill="currentColor" stroke="none"/>
    </svg>
  );
}

// Cuộn giấy Nhật (巻物) — soạn nội dung
function IconEdit() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <rect x="6" y="7" width="12" height="10" rx="1"/>
      <ellipse cx="6" cy="12" rx="1.5" ry="5"/>
      <ellipse cx="18" cy="12" rx="1.5" ry="5"/>
      <line x1="9" y1="10.5" x2="15" y2="10.5"/>
      <line x1="9" y1="13.5" x2="13" y2="13.5"/>
    </svg>
  );
}

// Cổng Torii (鳥居) — ngân hàng câu hỏi
function IconList() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M2 9 L5 7.5 L19 7.5 L22 9"/>
      <line x1="5" y1="11.5" x2="19" y2="11.5"/>
      <line x1="8" y1="9" x2="8" y2="21"/>
      <line x1="16" y1="9" x2="16" y2="21"/>
    </svg>
  );
}

// Hạc giấy origami (折り鶴) — hỗ trợ học viên (quick action)
function IconMessage() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M2 15 Q7 8 12 11 Q17 8 22 15"/>
      <path d="M12 11 L11 16 L9 20"/>
      <path d="M12 11 Q14 7 16 5"/>
    </svg>
  );
}

// Quạt xếp (扇子) — chấm bài nói (quick action)
function IconMicAction() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <line x1="12" y1="18" x2="4" y2="8"/>
      <line x1="12" y1="18" x2="8" y2="5"/>
      <line x1="12" y1="18" x2="12" y2="4"/>
      <line x1="12" y1="18" x2="16" y2="5"/>
      <line x1="12" y1="18" x2="20" y2="8"/>
      <path d="M4 8 Q8 3.5 12 4 Q16 3.5 20 8"/>
      <circle cx="12" cy="18" r="1.5" fill="currentColor" stroke="none"/>
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
        <StaffPageHero
          accent="pink"
          title="Bảng Điều Hành"
          subtitle="Tổng quan hoạt động soạn bài, duyệt nội dung và hỗ trợ học viên"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              {/* Hoa anh đào 5 cánh */}
              <ellipse cx="24" cy="13" rx="4.5" ry="8" transform="rotate(0 24 24)"/>
              <ellipse cx="24" cy="13" rx="4.5" ry="8" transform="rotate(72 24 24)"/>
              <ellipse cx="24" cy="13" rx="4.5" ry="8" transform="rotate(144 24 24)"/>
              <ellipse cx="24" cy="13" rx="4.5" ry="8" transform="rotate(216 24 24)"/>
              <ellipse cx="24" cy="13" rx="4.5" ry="8" transform="rotate(288 24 24)"/>
              <circle cx="24" cy="24" r="3.5" fill="currentColor"/>
              <line x1="24" y1="20.5" x2="24" y2="18"/>
              <line x1="27.3" y1="21.8" x2="29" y2="19.5"/>
              <line x1="20.7" y1="21.8" x2="19" y2="19.5"/>
            </svg>
          }
        />
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
