import { Link } from 'react-router-dom';
import AppLogo from '@/shared/components/common/AppLogo';
import './AuthTopBar.css';

function AuthTopBar() {
  return (
    <header className="auth-topbar">
      <Link to="/" className="auth-topbar-logo" aria-label="SakuJi - về trang chủ">
        <AppLogo className="auth-topbar-icon" />
        <span className="auth-topbar-wordmark">
          <span className="auth-topbar-saku">Saku</span>
          <span className="auth-topbar-ji">Ji</span>
        </span>
      </Link>

      <Link to="/" className="auth-topbar-back">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <path d="M10 12L6 8l4-4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
        Trang chủ
      </Link>
    </header>
  );
}

export default AuthTopBar;
