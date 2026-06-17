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
    id: 'manager-staff-performance',
    label: 'Hiệu suất Staff',
    route: '/manager/staff-performance',
    icon: (
      // Biểu đồ tăng trưởng — hiệu suất nhóm
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <rect x="2" y="13" width="4" height="8" rx="1"/>
        <rect x="9" y="8" width="4" height="13" rx="1"/>
        <rect x="16" y="3" width="4" height="18" rx="1"/>
        <polyline points="3 11 11 6 19 2" strokeDasharray="2 1"/>
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
    id: 'manager-reports',
    label: 'Báo cáo',
    route: '/manager/reports',
    icon: (
      // Tài liệu báo cáo với biểu đồ
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M5 3h9l5 5v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z"/>
        <polyline points="14 3 14 8 19 8"/>
        <polyline points="8 17 10 15 12.5 17.5 16 13"/>
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
