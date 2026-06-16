import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import ManagerTopNav from '../../components/layout/ManagerTopNav';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './ManagerDashboard.css';

const MOCK_STATS = {
  pendingReviewCount: 8,
  publishedThisMonth: 23,
  activeStaffCount: 5,
  totalStudents: 312,
};

const MOCK_RECENT_ACTIVITY = [
  { id: 1, staffName: 'Staff Lan', action: 'Gửi duyệt', contentType: 'Bài học', title: 'Kanji N5 — Bài 3', time: '03/06/2026 09:00', status: 'pending_review' },
  { id: 2, staffName: 'Manager', action: 'Đã duyệt', contentType: 'Câu hỏi', title: "N5 Kanji: 'おはよう'", time: '02/06/2026 16:20', status: 'approved' },
  { id: 3, staffName: 'Staff Minh', action: 'Gửi duyệt', contentType: 'Từ vựng', title: 'Nhóm từ gia đình N4', time: '02/06/2026 11:00', status: 'pending_review' },
  { id: 4, staffName: 'Manager', action: 'Từ chối', contentType: 'Ngữ pháp', title: '～ことができる (lỗi ví dụ)', time: '01/06/2026 14:00', status: 'rejected' },
  { id: 5, staffName: 'Staff Bình', action: 'Gửi duyệt', contentType: 'Đề thi', title: 'Mock Test N4 Vol.2', time: '01/06/2026 10:00', status: 'pending_review' },
];

const STATUS_MAP = {
  pending_review: { label: 'Chờ duyệt', className: 'mgd-status--pending' },
  approved: { label: 'Đã duyệt', className: 'mgd-status--approved' },
  rejected: { label: 'Từ chối', className: 'mgd-status--rejected' },
};

function IconStamp() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <rect x="10" y="2" width="4" height="6" rx="1.5"/>
      <rect x="7" y="13" width="10" height="8" rx="2"/>
      <line x1="10" y1="8" x2="10" y2="13"/>
      <line x1="14" y1="8" x2="14" y2="13"/>
      <line x1="9.5" y1="17" x2="14.5" y2="17"/>
      <line x1="12" y1="14.5" x2="12" y2="19.5"/>
    </svg>
  );
}

function IconPublish() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M5 3h9l5 5v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z"/>
      <polyline points="14 3 14 8 19 8"/>
      <polyline points="8 13 11 16 16 11"/>
    </svg>
  );
}

function IconStaff() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="9" cy="6" r="2.5"/>
      <path d="M3 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2"/>
      <circle cx="18" cy="9" r="2"/>
      <path d="M15 21v-1.5a3 3 0 0 1 6 0V21"/>
    </svg>
  );
}

function IconStudents() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>
  );
}

function ManagerDashboard() {
  const navigate = useNavigate();
  const { user } = useAppSelector((state) => state.auth);
  const [stats] = useState(MOCK_STATS);
  const [activity] = useState(MOCK_RECENT_ACTIVITY);

  return (
    <div className="mgd-page">
      <ManagerTopNav activeTab="manager-dashboard" />

      <main className="mgd-body">
        <StaffPageHero
          accent="gold"
          title="Bảng Quản Lý"
          subtitle="Tổng quan hoạt động nhóm, hàng đợi duyệt và hiệu suất Staff"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M6 19L24 6l18 13V42a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V19z"/>
              <rect x="18" y="26" width="12" height="16" rx="1"/>
              <rect x="10" y="22" width="8" height="8" rx="1"/>
              <rect x="30" y="22" width="8" height="8" rx="1"/>
            </svg>
          }
        />

        <div className="mgd-header">
          <h1 className="mgd-greeting">Xin chào, {user?.fullName ?? 'Manager'} 👋</h1>
          <span className="mgd-date">Thứ Tư, 04/06/2026</span>
        </div>

        {/* Stat row */}
        <div className="mgd-stat-row">
          <div
            className="mgd-stat-card mgd-stat-card--pending"
            onClick={() => navigate('/manager/review-queue')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && navigate('/manager/review-queue')}
          >
            <div className="mgd-stat-icon mgd-stat-icon--amber">
              <IconStamp />
            </div>
            <div>
              <span className="mgd-stat-value">{stats.pendingReviewCount}</span>
              <span className="mgd-stat-label">Chờ phê duyệt</span>
            </div>
          </div>

          <div
            className="mgd-stat-card mgd-stat-card--published"
            onClick={() => navigate('/manager/content-pipeline')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && navigate('/manager/content-pipeline')}
          >
            <div className="mgd-stat-icon mgd-stat-icon--green">
              <IconPublish />
            </div>
            <div>
              <span className="mgd-stat-value">{stats.publishedThisMonth}</span>
              <span className="mgd-stat-label">Xuất bản tháng này</span>
            </div>
          </div>

          <div
            className="mgd-stat-card mgd-stat-card--staff"
            onClick={() => navigate('/manager/staff-performance')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && navigate('/manager/staff-performance')}
          >
            <div className="mgd-stat-icon mgd-stat-icon--indigo">
              <IconStaff />
            </div>
            <div>
              <span className="mgd-stat-value">{stats.activeStaffCount}</span>
              <span className="mgd-stat-label">Staff đang hoạt động</span>
            </div>
          </div>

          <div
            className="mgd-stat-card mgd-stat-card--students"
            onClick={() => navigate('/manager/reports')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && navigate('/manager/reports')}
          >
            <div className="mgd-stat-icon mgd-stat-icon--pink">
              <IconStudents />
            </div>
            <div>
              <span className="mgd-stat-value">{stats.totalStudents.toLocaleString()}</span>
              <span className="mgd-stat-label">Tổng học viên</span>
            </div>
          </div>
        </div>

        {/* Quick actions */}
        <p className="mgd-section-title">Tác vụ nhanh</p>
        <div className="mgd-actions-grid">
          <Link to="/manager/review-queue" className="mgd-action-card mgd-action-card--amber">
            <div className="mgd-action-icon">
              <IconStamp />
            </div>
            <div>
              <p className="mgd-action-title">Duyệt Nội Dung</p>
              <p className="mgd-action-desc">{stats.pendingReviewCount} mục đang chờ phê duyệt</p>
            </div>
          </Link>

          <Link to="/manager/staff-performance" className="mgd-action-card mgd-action-card--indigo">
            <div className="mgd-action-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <rect x="2" y="13" width="4" height="8" rx="1"/>
                <rect x="9" y="8" width="4" height="13" rx="1"/>
                <rect x="16" y="3" width="4" height="18" rx="1"/>
              </svg>
            </div>
            <div>
              <p className="mgd-action-title">Hiệu Suất Staff</p>
              <p className="mgd-action-desc">Theo dõi năng suất từng thành viên</p>
            </div>
          </Link>

          <Link to="/manager/content-pipeline" className="mgd-action-card mgd-action-card--pink">
            <div className="mgd-action-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <rect x="2" y="4" width="5" height="16" rx="1"/>
                <rect x="9.5" y="4" width="5" height="11" rx="1"/>
                <rect x="17" y="4" width="5" height="7" rx="1"/>
              </svg>
            </div>
            <div>
              <p className="mgd-action-title">Pipeline Nội Dung</p>
              <p className="mgd-action-desc">Toàn bộ nội dung theo trạng thái</p>
            </div>
          </Link>

          <Link to="/manager/reports" className="mgd-action-card mgd-action-card--green">
            <div className="mgd-action-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                <path d="M5 3h9l5 5v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z"/>
                <polyline points="14 3 14 8 19 8"/>
                <polyline points="8 17 10 15 12.5 17.5 16 13"/>
              </svg>
            </div>
            <div>
              <p className="mgd-action-title">Xem Báo Cáo</p>
              <p className="mgd-action-desc">Phân tích nội dung và học viên</p>
            </div>
          </Link>
        </div>

        {/* Recent activity */}
        <p className="mgd-section-title">Hoạt động gần đây</p>
        <div className="mgd-activity-wrap">
          <table className="mgd-table">
            <thead>
              <tr>
                <th>Staff</th>
                <th>Hành động</th>
                <th>Loại</th>
                <th>Nội dung</th>
                <th>Thời gian</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {activity.map((item) => {
                const s = STATUS_MAP[item.status] ?? { label: item.status, className: '' };
                return (
                  <tr key={item.id}>
                    <td className="mgd-td-staff">{item.staffName}</td>
                    <td>{item.action}</td>
                    <td className="mgd-td-type">{item.contentType}</td>
                    <td className="mgd-td-title">{item.title}</td>
                    <td className="mgd-td-time">{item.time}</td>
                    <td>
                      <span className={`mgd-status ${s.className}`}>{s.label}</span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}

export default ManagerDashboard;
