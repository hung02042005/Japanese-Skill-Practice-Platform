import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { logoutThunk } from '../../store/slices/authSlice';
import AppLogo from '../common/AppLogo';
import './TopNav.css';

const NAV_TABS = [
  {
    id: 'review',
    label: 'Ôn tập',
    route: '/review',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
        <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
      </svg>
    ),
  },
  {
    id: 'learn',
    label: 'Học từ mới',
    route: '/learn',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z" stroke="currentColor" strokeWidth="2"/>
        <path d="M12 8v4l3 3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    ),
  },
  {
    id: 'kanji',
    label: 'Kanji',
    route: '/kanji',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="2"/>
        <path d="M8 8h8M8 12h8M8 16h5" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      </svg>
    ),
  },
  {
    id: 'grammar',
    label: 'Ngữ pháp',
    route: '/grammar',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M4 6h16M4 12h10M4 18h12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      </svg>
    ),
  },
  {
    id: 'dictionary',
    label: 'Từ điển',
    route: '/dictionary',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2"/>
        <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      </svg>
    ),
  },
  {
    id: 'mock-test',
    label: 'Thi thử',
    route: '/mock-test',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
        <rect x="9" y="3" width="6" height="4" rx="1" stroke="currentColor" strokeWidth="2"/>
        <path d="M9 12l2 2 4-4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    ),
  },
];

function UserDropdown({ user, streak, wordCount, daysThisMonth, onClose, onLogout }) {
  const navigate = useNavigate();

  function handleProfile() {
    onClose();
    navigate('/progress');
  }

  async function handleLogout() {
    onClose();
    await onLogout();
  }

  const initial = user?.fullName?.charAt(0)?.toUpperCase() ?? '?';
  const hasStreak = streak > 0;

  return (
    <div className="user-dropdown" role="dialog" aria-label="Thông tin tài khoản">
      {/* ── User info ── */}
      <div className="ud-header">
        <div className="ud-avatar-lg" aria-hidden="true">{initial}</div>
        <div className="ud-user-info">
          <div className="ud-name">{user?.fullName ?? 'Học viên'}</div>
          <div className="ud-email">{user?.email ?? ''}</div>
        </div>
      </div>

      <div className="ud-divider" />

      {/* ── Thành tích ── */}
      <div className="ud-section-label">Thành tích</div>

      <div className="ud-stats">
        <div className="ud-stat ud-stat--streak">
          <span className="ud-stat-icon" aria-hidden="true">
            {hasStreak ? '🔥' : '💤'}
          </span>
          <div className="ud-stat-body">
            <div className="ud-stat-value">{streak} ngày</div>
            <div className="ud-stat-label">Streak hiện tại</div>
          </div>
          {hasStreak && <div className="ud-stat-badge">{streak}</div>}
        </div>

        <div className="ud-stat ud-stat--words">
          <span className="ud-stat-icon" aria-hidden="true">⭐</span>
          <div className="ud-stat-body">
            <div className="ud-stat-value">{wordCount}</div>
            <div className="ud-stat-label">Từ đã học</div>
          </div>
        </div>

        <div className="ud-stat ud-stat--days">
          <span className="ud-stat-icon" aria-hidden="true">📅</span>
          <div className="ud-stat-body">
            <div className="ud-stat-value">{daysThisMonth} ngày</div>
            <div className="ud-stat-label">Học trong tháng</div>
          </div>
        </div>
      </div>

      <div className="ud-divider" />

      {/* ── Actions ── */}
      <div className="ud-actions">
        <button type="button" className="ud-action-btn ud-action-btn--profile" onClick={handleProfile}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            <circle cx="12" cy="7" r="4" stroke="currentColor" strokeWidth="2"/>
          </svg>
          Xem thành tích đầy đủ
          <svg className="ud-action-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>

        <div className="ud-divider" />

        <button type="button" className="ud-action-btn ud-action-btn--logout" onClick={handleLogout}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <polyline points="16 17 21 12 16 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <line x1="21" y1="12" x2="9" y2="12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
          </svg>
          Đăng xuất
        </button>
      </div>
    </div>
  );
}

function TopNav({ activeTab = '' }) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { user } = useAppSelector((state) => state.auth);
  const { streak = 0, wordCount = 0, daysThisMonth = 0 } = useAppSelector((state) => state.student);

  const [dropdownOpen, setDropdownOpen] = useState(false);
  const wrapperRef = useRef(null);

  /* Close dropdown on outside click */
  useEffect(() => {
    if (!dropdownOpen) return;
    function handleOutside(e) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener('mousedown', handleOutside);
    return () => document.removeEventListener('mousedown', handleOutside);
  }, [dropdownOpen]);

  /* Close on Escape */
  useEffect(() => {
    function handleKey(e) {
      if (e.key === 'Escape') setDropdownOpen(false);
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, []);

  async function handleLogout() {
    await dispatch(logoutThunk());
    navigate('/login');
  }

  const avatarInitial = user?.fullName?.charAt(0)?.toUpperCase() ?? '?';
  const displayEmail  = user?.email ?? '';

  return (
    <header className="topnav" role="banner">
      {/* Logo */}
      <Link to="/dashboard" className="topnav-logo" aria-label="SakuJi — về trang Dashboard">
        <AppLogo size={32} />
        <span className="topnav-wordmark">
          <span className="topnav-saku">Saku</span>
          <span className="topnav-ji">Ji</span>
        </span>
      </Link>

      {/* Navigation tabs */}
      <nav className="topnav-tabs" aria-label="Điều hướng chính">
        {NAV_TABS.map((tab) => (
          <Link
            key={tab.id}
            to={tab.route}
            className={`topnav-tab ${activeTab === tab.id ? 'topnav-tab--active' : ''}`}
            aria-current={activeTab === tab.id ? 'page' : undefined}
          >
            {tab.icon}
            <span className="topnav-tab-label">{tab.label}</span>
          </Link>
        ))}
      </nav>

      {/* User area + Dropdown */}
      <div className="topnav-user" ref={wrapperRef}>
        <span className="topnav-email" title={displayEmail}>{displayEmail}</span>

        <button
          type="button"
          className={`topnav-avatar${dropdownOpen ? ' topnav-avatar--open' : ''}`}
          onClick={() => setDropdownOpen((v) => !v)}
          aria-label={`${user?.fullName ?? 'Người dùng'} — mở menu tài khoản`}
          aria-expanded={dropdownOpen}
          aria-haspopup="dialog"
        >
          {avatarInitial}
        </button>

        {dropdownOpen && (
          <UserDropdown
            user={user}
            streak={streak}
            wordCount={wordCount}
            daysThisMonth={daysThisMonth}
            onClose={() => setDropdownOpen(false)}
            onLogout={handleLogout}
          />
        )}
      </div>
    </header>
  );
}

export default TopNav;
