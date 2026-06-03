import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { logoutThunk } from '../../store/slices/authSlice';
import AppLogo from '../common/AppLogo';
import './StaffTopNav.css';

const NAV_TABS = [
  {
    id: 'staff-dashboard',
    label: 'Tổng quan',
    route: '/staff',
    icon: (
      // Hoa anh đào 5 cánh — biểu tượng thương hiệu SakuJi
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <ellipse cx="12" cy="6.5" rx="2" ry="3.5" transform="rotate(0 12 12)"/>
        <ellipse cx="12" cy="6.5" rx="2" ry="3.5" transform="rotate(72 12 12)"/>
        <ellipse cx="12" cy="6.5" rx="2" ry="3.5" transform="rotate(144 12 12)"/>
        <ellipse cx="12" cy="6.5" rx="2" ry="3.5" transform="rotate(216 12 12)"/>
        <ellipse cx="12" cy="6.5" rx="2" ry="3.5" transform="rotate(288 12 12)"/>
        <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
      </svg>
    ),
  },
  {
    id: 'staff-content',
    label: 'Học liệu',
    route: '/staff/content',
    icon: (
      // Cuộn giấy Nhật (巻物) — tài liệu / học liệu
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <rect x="6" y="7" width="12" height="10" rx="1"/>
        <ellipse cx="6" cy="12" rx="1.5" ry="5"/>
        <ellipse cx="18" cy="12" rx="1.5" ry="5"/>
        <line x1="9" y1="10.5" x2="15" y2="10.5"/>
        <line x1="9" y1="13.5" x2="13" y2="13.5"/>
      </svg>
    ),
  },
  {
    id: 'staff-questions',
    label: 'Ngân hàng câu hỏi',
    route: '/staff/questions',
    icon: (
      // Cổng Torii (鳥居) — cửa vào kho câu hỏi
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M2 9 L5 7.5 L19 7.5 L22 9"/>
        <line x1="5" y1="11.5" x2="19" y2="11.5"/>
        <line x1="8" y1="9" x2="8" y2="21"/>
        <line x1="16" y1="9" x2="16" y2="21"/>
      </svg>
    ),
  },
  {
    id: 'staff-assessments',
    label: 'Đề thi & Quiz',
    route: '/staff/assessments',
    icon: (
      // Tờ giấy thi với dấu tích — kết quả & đề thi
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M5 3h9l5 5v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z"/>
        <polyline points="14 3 14 8 19 8"/>
        <polyline points="8 13 11 16 16 11"/>
        <circle cx="8" cy="8" r="1" fill="currentColor" stroke="none"/>
      </svg>
    ),
  },
  {
    id: 'staff-tickets',
    label: 'Hỗ trợ',
    route: '/staff/tickets',
    icon: (
      // Hạc giấy origami (折り鶴) — nhẹ nhàng, hỗ trợ
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M2 15 Q7 8 12 11 Q17 8 22 15"/>
        <path d="M12 11 L11 16 L9 20"/>
        <path d="M12 11 Q14 7 16 5"/>
      </svg>
    ),
  },
  {
    id: 'staff-grading',
    label: 'Chấm bài nói',
    route: '/staff/grading',
    icon: (
      // Quạt xếp (扇子) — âm thanh / bài nói
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <line x1="12" y1="18" x2="4" y2="8"/>
        <line x1="12" y1="18" x2="8" y2="5"/>
        <line x1="12" y1="18" x2="12" y2="4"/>
        <line x1="12" y1="18" x2="16" y2="5"/>
        <line x1="12" y1="18" x2="20" y2="8"/>
        <path d="M4 8 Q8 3.5 12 4 Q16 3.5 20 8"/>
        <circle cx="12" cy="18" r="1.5" fill="currentColor" stroke="none"/>
      </svg>
    ),
  },
  {
    id: 'staff-notifications',
    label: 'Thông báo',
    route: '/staff/notifications',
    icon: (
      // Chuông gió (風鈴) — thông báo rung nhẹ
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <line x1="12" y1="2" x2="12" y2="4"/>
        <path d="M9 10 C9 6.5 10.5 4 12 4 C13.5 4 15 6.5 15 10 L16 14 L8 14 Z"/>
        <path d="M8 14 Q12 16.5 16 14"/>
        <line x1="12" y1="16.5" x2="12" y2="18.5"/>
        <circle cx="12" cy="19.5" r="1" fill="currentColor" stroke="none"/>
        <line x1="10.5" y1="20.5" x2="13.5" y2="20.5"/>
        <line x1="12" y1="20.5" x2="12" y2="22"/>
      </svg>
    ),
  },
];

const MANAGER_TAB = {
  id: 'staff-review',
  label: 'Hàng đợi duyệt',
  route: '/staff/review-queue',
  icon: (
    // Con dấu Hanko (判子) — phê duyệt chính thức
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <rect x="10" y="2" width="4" height="6" rx="1.5"/>
      <rect x="7" y="13" width="10" height="8" rx="2"/>
      <line x1="10" y1="8" x2="10" y2="13"/>
      <line x1="14" y1="8" x2="14" y2="13"/>
      <line x1="9.5" y1="17" x2="14.5" y2="17"/>
      <line x1="12" y1="14.5" x2="12" y2="19.5"/>
    </svg>
  ),
};

function StaffTopNav({ activeTab }) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { user } = useAppSelector((state) => state.auth);

  const visibleTabs = [
    ...NAV_TABS,
    ...(user?.staffRole === 'staff_manager' ? [MANAGER_TAB] : []),
  ];

  const handleLogout = async () => {
    await dispatch(logoutThunk());
    navigate('/login');
  };

  const initial = user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'S';

  return (
    <nav className="stn-nav">
      <Link to="/staff" className="stn-logo">
        <AppLogo size={28} />
        <span className="stn-wordmark">
          <span className="stn-saku">Saku</span>
          <span className="stn-ji">Ji</span>
        </span>
      </Link>

      <span className="stn-role-badge">STAFF</span>

      <div className="stn-tabs">
        {visibleTabs.map((tab) => (
          <Link
            key={tab.id}
            to={tab.route}
            className={`stn-tab${activeTab === tab.id ? ' stn-tab--active' : ''}`}
          >
            {tab.icon}
            {tab.label}
          </Link>
        ))}
      </div>

      <div className="stn-user">
        <span className="stn-email">{user?.email ?? ''}</span>
        <button
          className="stn-avatar-btn"
          onClick={handleLogout}
          title="Đăng xuất"
          aria-label="Đăng xuất"
        >
          {initial}
        </button>
      </div>
    </nav>
  );
}

export default StaffTopNav;
