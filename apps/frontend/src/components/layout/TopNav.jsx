import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { logoutThunk } from '../../store/slices/authSlice';
import AppLogo from '../common/AppLogo';
import { VocabIcon, FlameIcon, StarIcon, CalendarIcon, MicIcon } from '../student/StudentIcons';
import { MoonIcon } from '../common/AppIcons';
import NotificationBell from '../notifications/NotificationBell';
import './TopNav.css';

const NAV_TABS = [
  {
    id: 'dashboard',
    label: 'Trang chủ',
    route: '/dashboard',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M3 11l9-8 9 8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        <path d="M5 10v9a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1v-9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
    ),
  },

  {
    id: 'vocabulary',
    label: 'Từ vựng',
    route: '/vocabulary',
    icon: <VocabIcon size={22} />,
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
  {
    id: 'speaking',
    label: 'Luyện nói',
    route: '/speaking',
    icon: <MicIcon size={22} />,
  },
];

function UserDropdown({ user, streak, wordCount, daysThisMonth, onClose, onLogout }) {
  const navigate = useNavigate();

  function handleAccount() {
    onClose();
    navigate('/profile');
  }

  function handleProfile() {
    onClose();
    navigate('/progress');
  }

  function handleNotebook() {
    onClose();
    navigate('/notebook');
  }

  function handleSupport() {
    onClose();
    navigate('/support');
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
            {hasStreak ? <FlameIcon size={22} /> : <MoonIcon size={22} />}
          </span>
          <div className="ud-stat-body">
            <div className="ud-stat-value">{streak} ngày</div>
            <div className="ud-stat-label">Streak hiện tại</div>
          </div>
          {hasStreak && <div className="ud-stat-badge">{streak}</div>}
        </div>

        <div className="ud-stat ud-stat--words">
          <span className="ud-stat-icon" aria-hidden="true"><StarIcon size={22} /></span>
          <div className="ud-stat-body">
            <div className="ud-stat-value">{wordCount}</div>
            <div className="ud-stat-label">Từ đã học</div>
          </div>
        </div>

        <div className="ud-stat ud-stat--days">
          <span className="ud-stat-icon" aria-hidden="true"><CalendarIcon size={22} /></span>
          <div className="ud-stat-body">
            <div className="ud-stat-value">{daysThisMonth} ngày</div>
            <div className="ud-stat-label">Học trong tháng</div>
          </div>
        </div>
      </div>

      <div className="ud-divider" />

      {/* ── Actions ── */}
      <div className="ud-actions">
        <button type="button" className="ud-action-btn ud-action-btn--account" onClick={handleAccount}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle cx="12" cy="8" r="4" stroke="currentColor" strokeWidth="2"/>
            <path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
          </svg>
          Hồ sơ của tôi
          <svg className="ud-action-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>

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

        <button type="button" className="ud-action-btn ud-action-btn--notebook" onClick={handleNotebook}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M4 4a2 2 0 0 1 2-2h12a1 1 0 0 1 1 1v18a1 1 0 0 1-1 1H6a2 2 0 0 1-2-2z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
            <path d="M4 18h14M8 7h7" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
          </svg>
          Sổ tay — Từ cần ôn lại
          <svg className="ud-action-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>

        <button type="button" className="ud-action-btn ud-action-btn--support" onClick={handleSupport}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
          </svg>
          Hỗ trợ — Gửi yêu cầu
          <svg className="ud-action-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
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
        <NotificationBell />

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
