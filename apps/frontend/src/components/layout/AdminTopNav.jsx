import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { logoutThunk } from '../../store/slices/authSlice';
import AppLogo from '../common/AppLogo';
import './AdminTopNav.css';

const ADMIN_TABS = [
  {
    id: 'admin-overview',
    label: 'Tổng quan',
    route: '/admin',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <rect x="3" y="3" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="2"/>
        <rect x="14" y="3" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="2"/>
        <rect x="3" y="14" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="2"/>
        <rect x="14" y="14" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="2"/>
      </svg>
    ),
  },
  {
    id: 'manage-users',
    label: 'Người dùng',
    route: '/admin/users',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        <circle cx="9" cy="7" r="4" stroke="currentColor" strokeWidth="2"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      </svg>
    ),
  },
  {
    id: 'manage-content',
    label: 'Nội dung',
    route: '/admin/content',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
        <polyline points="14 2 14 8 20 8" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
        <path d="M9 12h6M9 16h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      </svg>
    ),
  },
  {
    id: 'reports',
    label: 'Báo cáo',
    route: '/admin/reports',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <line x1="18" y1="20" x2="18" y2="10" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        <line x1="12" y1="20" x2="12" y2="4" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        <line x1="6" y1="20" x2="6" y2="14" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      </svg>
    ),
  },
  {
    id: 'settings',
    label: 'Cài đặt',
    route: '/admin/settings',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
        <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" stroke="currentColor" strokeWidth="2"/>
      </svg>
    ),
  },
];

function AdminTopNav({ activeTab = '' }) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { user } = useAppSelector((state) => state.auth);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const wrapperRef = useRef(null);

  useEffect(() => {
    if (!dropdownOpen) return;
    function handleOutside(e) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) setDropdownOpen(false);
    }
    document.addEventListener('mousedown', handleOutside);
    return () => document.removeEventListener('mousedown', handleOutside);
  }, [dropdownOpen]);

  useEffect(() => {
    function handleKey(e) { if (e.key === 'Escape') setDropdownOpen(false); }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, []);

  async function handleLogout() {
    setDropdownOpen(false);
    await dispatch(logoutThunk());
    navigate('/login');
  }

  const initial = user?.fullName?.charAt(0)?.toUpperCase() ?? 'A';

  return (
    <header className="admin-topnav" role="banner">
      {/* Logo + Admin badge */}
      <Link to="/admin" className="atn-logo" aria-label="SakuJi Admin — Trang tổng quan">
        <AppLogo size={30} />
        <span className="atn-wordmark">
          <span className="atn-saku">Saku</span><span className="atn-ji">Ji</span>
        </span>
        <span className="atn-crown-badge" aria-label="Chế độ quản trị">
          <svg width="11" height="9" viewBox="0 0 11 9" fill="none" aria-hidden="true">
            <path d="M0.5 8.5L2 3.5L5.5 6.5L9 3.5L10.5 8.5H0.5Z" fill="currentColor"/>
            <circle cx="0.5" cy="2.5" r="1.5" fill="currentColor"/>
            <circle cx="5.5" cy="1" r="1.5" fill="currentColor"/>
            <circle cx="10.5" cy="2.5" r="1.5" fill="currentColor"/>
          </svg>
          ADMIN
        </span>
      </Link>

      {/* Nav tabs */}
      <nav className="atn-tabs" aria-label="Điều hướng quản trị">
        {ADMIN_TABS.map((tab) => (
          <Link
            key={tab.id}
            to={tab.route}
            className={`atn-tab${activeTab === tab.id ? ' atn-tab--active' : ''}`}
            aria-current={activeTab === tab.id ? 'page' : undefined}
          >
            {tab.icon}
            <span className="atn-tab-label">{tab.label}</span>
          </Link>
        ))}
      </nav>

      {/* User area */}
      <div className="atn-user" ref={wrapperRef}>
        <span className="atn-email" title={user?.email ?? ''}>{user?.email ?? ''}</span>
        <button
          type="button"
          className={`atn-avatar${dropdownOpen ? ' atn-avatar--open' : ''}`}
          onClick={() => setDropdownOpen((v) => !v)}
          aria-label={`${user?.fullName ?? 'Admin'} — mở menu tài khoản`}
          aria-expanded={dropdownOpen}
          aria-haspopup="dialog"
        >
          {initial}
        </button>

        {dropdownOpen && (
          <div className="atn-dropdown" role="dialog" aria-label="Menu tài khoản quản trị">
            <div className="atnd-header">
              <div className="atnd-avatar">{initial}</div>
              <div className="atnd-info">
                <div className="atnd-name">{user?.fullName ?? 'Administrator'}</div>
                <div className="atnd-email">{user?.email ?? ''}</div>
                <span className="atnd-role-chip">👑 Quản trị viên</span>
              </div>
            </div>
            <div className="atnd-divider" />
            <button type="button" className="atnd-logout" onClick={handleLogout}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <polyline points="16 17 21 12 16 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <line x1="21" y1="12" x2="9" y2="12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              Đăng xuất
            </button>
          </div>
        )}
      </div>
    </header>
  );
}

export default AdminTopNav;
