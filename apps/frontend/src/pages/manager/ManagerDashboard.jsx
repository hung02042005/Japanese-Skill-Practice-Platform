import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { useAppSelector } from '../../store/hooks';
import ManagerTopNav from '../../components/layout/ManagerTopNav';
import StaffPageHero from '../../components/staff/StaffPageHero';
import { fetchReviewQueueThunk } from '../../store/slices/managerReviewSlice';
import { fetchPublishedContentsThunk } from '../../store/slices/publishedContentSlice';
import './ManagerDashboard.css';

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

function ManagerDashboard() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user } = useAppSelector((state) => state.auth);
  const { totalElements: pendingReviewCount, status: reviewStatus } = useAppSelector(
    (state) => state.managerReview
  );
  const { totalElements: publishedTotal, status: publishedStatus } = useAppSelector(
    (state) => state.publishedContent
  );
  const isLoading =
    reviewStatus === 'idle' || reviewStatus === 'loading' || publishedStatus === 'idle' || publishedStatus === 'loading';

  useEffect(() => {
    dispatch(fetchReviewQueueThunk({ size: 1 }));
    dispatch(fetchPublishedContentsThunk({ size: 1 }));
  }, [dispatch]);

  const today = new Date().toLocaleDateString('vi-VN', {
    weekday: 'long',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

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
          <span className="mgd-date">{today}</span>
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
              <span className="mgd-stat-value">{isLoading ? '—' : pendingReviewCount}</span>
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
              <span className="mgd-stat-value">{isLoading ? '—' : publishedTotal}</span>
              <span className="mgd-stat-label">Tổng đã xuất bản</span>
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
              <p className="mgd-action-desc">
                {isLoading ? 'Đang tải...' : `${pendingReviewCount} mục đang chờ phê duyệt`}
              </p>
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
      </main>
    </div>
  );
}

export default ManagerDashboard;
