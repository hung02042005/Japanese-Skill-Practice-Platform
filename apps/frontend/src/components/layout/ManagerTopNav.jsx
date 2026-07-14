import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { logoutThunk } from '../../store/slices/authSlice';
import AppLogo from '../common/AppLogo';
import './ManagerTopNav.css';

const NAV_TABS = [
  {
    id: 'manager-dashboard',
    label: 'Tổng quan',
    route: '/manager',
    icon: (
      // Ngôi nhà / tổng hành dinh
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M3 9.5L12 3l9 6.5V21a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9.5z"/>
        <rect x="9" y="13" width="6" height="8"/>
      </svg>
    ),
  },
  {
    id: 'manager-review',
    label: 'Hàng đợi duyệt',
    route: '/manager/review-queue',
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
  },
  {
    id: 'manager-pipeline',
    label: 'Pipeline nội dung',
    route: '/manager/content-pipeline',
    icon: (
      // Kanban / luồng nội dung
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <rect x="2" y="4" width="5" height="16" rx="1"/>
        <rect x="9.5" y="4" width="5" height="11" rx="1"/>
        <rect x="17" y="4" width="5" height="7" rx="1"/>
      </svg>
    ),
  },
  {
    id: 'manager-deleted-topics',
    label: 'Thư mục đã xóa',
    route: '/manager/deleted-topics',
    icon: (
      // Thùng rác / Trash bin icon
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <polyline points="3 6 5 6 21 6"/>
        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
        <line x1="10" y1="11" x2="10" y2="17"/>
        <line x1="14" y1="11" x2="14" y2="17"/>
      </svg>
    ),
  },

  {
    id: 'manager-tickets',
    label: 'Hỗ trợ',
    route: '/manager/tickets',
    icon: (
      // Hạc giấy / hỗ trợ học viên
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8z"/>
      </svg>
    ),
  },
  {
    id: 'manager-notifications',
    label: 'Thông báo',
    route: '/manager/notifications',
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

function ManagerTopNav({ activeTab }) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { user } = useAppSelector((state) => state.auth);

  const handleLogout = async () => {
    await dispatch(logoutThunk());
    navigate('/login');
  };

  const initial = user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'M';

  return (
    <nav className="mtn-nav">
      <Link to="/manager" className="mtn-logo">
        <AppLogo size={28} />
        <span className="mtn-wordmark">
          <span className="mtn-saku">Saku</span>
          <span className="mtn-ji">Ji</span>
        </span>
      </Link>

      <span className="mtn-role-badge">MANAGER</span>

      <div className="mtn-tabs">
        {NAV_TABS.map((tab) => (
          <Link
            key={tab.id}
            to={tab.route}
            className={`mtn-tab${activeTab === tab.id ? ' mtn-tab--active' : ''}`}
          >
            {tab.icon}
            {tab.label}
          </Link>
        ))}
      </div>

      <div className="mtn-user">
        <span className="mtn-email">{user?.email ?? ''}</span>
        <button
          className="mtn-avatar-btn"
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

export default ManagerTopNav;
