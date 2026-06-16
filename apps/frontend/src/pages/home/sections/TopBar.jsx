import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import AppLogo from '../../../components/common/AppLogo';
import './TopBar.css';

function TopBar() {
  const [menuOpen, setMenuOpen] = useState(false);
  const navigate = useNavigate();

  const closeMenu = () => setMenuOpen(false);

  return (
    <header className="topbar">
      <div className="topbar-inner">
        <a href="/" className="topbar-logo">
          <AppLogo />
          <span className="topbar-logo-text">
            <span className="topbar-logo-saku">Saku</span>Ji
          </span>
        </a>

        <nav className="topbar-nav" aria-label="Điều hướng chính">
          <Link to="/tinh-nang" className="topbar-nav-link">Tính năng</Link>
          <Link to="/blog" className="topbar-nav-link">Blog</Link>
        </nav>

        <div className="topbar-actions">
          <button className="topbar-btn-login" onClick={() => navigate('/login')}>
            Đăng nhập
          </button>
          <button
            className="topbar-btn-start"
            onClick={() => navigate('/register')}
            aria-label="Đăng ký học miễn phí"
          >
            Get started →
          </button>
        </div>

        <button
          className="topbar-hamburger"
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label={menuOpen ? 'Đóng menu' : 'Mở menu'}
          aria-expanded={menuOpen}
        >
          {menuOpen ? '✕' : '☰'}
        </button>
      </div>

      {menuOpen && (
        <nav className="topbar-drawer" aria-label="Menu điều hướng di động">
          <Link to="/tinh-nang" className="topbar-drawer-link" onClick={closeMenu}>Tính năng</Link>
          <Link to="/blog" className="topbar-drawer-link" onClick={closeMenu}>Blog</Link>
          <div className="topbar-drawer-actions">
            <button className="topbar-btn-login" onClick={() => { navigate('/login'); closeMenu(); }}>
              Đăng nhập
            </button>
            <button className="topbar-btn-start" onClick={() => { navigate('/register'); closeMenu(); }}>
              Get started →
            </button>
          </div>
        </nav>
      )}
    </header>
  );
}

export default TopBar;
