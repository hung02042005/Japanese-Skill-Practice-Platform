import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { logoutThunk } from '@/features/auth/authSlice';
import AppLogo from '@/shared/components/common/AppLogo';
import { CrownIcon } from '@/shared/components/common/AppIcons';
import './AdminTopNav.css';

const ADMIN_TABS = [
  {
    id: 'admin-overview',
    label: 'Tổng quan',
    route: '/admin',
    icon: (
      // Hoa anh đào 5 cánh — biểu tượng thương hiệu, tổng quan
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
    id: 'manage-users',
    label: 'Người dùng',
    route: '/admin/users',
    icon: (
      // Người dùng + cánh hoa — quản lý học viên / nhân sự
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <circle cx="9" cy="7" r="3.5"/>
        <path d="M3 21v-2a5.5 5.5 0 0 1 11 0v2"/>
        <ellipse cx="19.5" cy="5" rx="1.2" ry="2.1" fill="currentColor" opacity="0.38" stroke="none" transform="rotate(0 19.5 7.5)"/>
        <ellipse cx="19.5" cy="5" rx="1.2" ry="2.1" fill="currentColor" opacity="0.38" stroke="none" transform="rotate(72 19.5 7.5)"/>
        <ellipse cx="19.5" cy="5" rx="1.2" ry="2.1" fill="currentColor" opacity="0.38" stroke="none" transform="rotate(144 19.5 7.5)"/>
        <ellipse cx="19.5" cy="5" rx="1.2" ry="2.1" fill="currentColor" opacity="0.38" stroke="none" transform="rotate(216 19.5 7.5)"/>
        <ellipse cx="19.5" cy="5" rx="1.2" ry="2.1" fill="currentColor" opacity="0.38" stroke="none" transform="rotate(288 19.5 7.5)"/>
        <circle cx="19.5" cy="7.5" r="1.1" fill="currentColor" stroke="none"/>
      </svg>
    ),
  },
  {
    id: 'reports',
    label: 'Báo cáo',
    route: '/admin/reports',
    icon: (
      // Biểu đồ cột + cánh hoa trang trí — phân tích dữ liệu
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <line x1="3" y1="20" x2="21" y2="20"/>
        <rect x="4" y="13" width="4" height="7" rx="1"/>
        <rect x="10" y="7" width="4" height="13" rx="1"/>
        <rect x="16" y="10" width="4" height="10" rx="1"/>
        <ellipse cx="3.5" cy="4.5" rx="1.1" ry="1.9" fill="currentColor" opacity="0.28" stroke="none" transform="rotate(-18 3.5 4.5)"/>
        <ellipse cx="3.5" cy="4.5" rx="1.1" ry="1.9" fill="currentColor" opacity="0.28" stroke="none" transform="rotate(54 3.5 4.5)"/>
        <circle cx="3.5" cy="4.5" r="0.9" fill="currentColor" opacity="0.4" stroke="none"/>
      </svg>
    ),
  },
  {
    id: 'settings',
    label: 'Cài đặt',
    route: '/admin/settings',
    icon: (
      // Bánh răng với trung tâm cánh hoa — cài đặt hệ thống
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <circle cx="12" cy="12" r="2.8"/>
        <path d="M12 2v2.5M12 19.5V22M2 12h2.5M19.5 12H22M4.93 4.93l1.77 1.77M17.3 17.3l1.77 1.77M4.93 19.07l1.77-1.77M17.3 6.7l1.77-1.77"/>
        <ellipse cx="21" cy="3.5" rx="1" ry="1.7" fill="currentColor" opacity="0.22" stroke="none" transform="rotate(-20 21 3.5)"/>
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
                <span className="atnd-role-chip"><CrownIcon size={13} /> Quản trị viên</span>
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
