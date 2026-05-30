import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './TopBar.css';

function TopBar() {
  const [menuOpen, setMenuOpen] = useState(false);
  const navigate = useNavigate();

  const closeMenu = () => setMenuOpen(false);

  return (
    <header className="topbar">
      <div className="topbar-inner">
        <a href="/" className="topbar-logo">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none" aria-hidden="true">
            <circle cx="14" cy="17" r="9" fill="#FFF0F3" stroke="#F4A7B3" strokeWidth="1.2"/>
            <ellipse cx="10" cy="9" rx="5" ry="8" fill="#E8637A" transform="rotate(-20 10 9)"/>
            <ellipse cx="14" cy="7" rx="5" ry="8.5" fill="#E8637A"/>
            <ellipse cx="18" cy="9" rx="5" ry="8" fill="#E8637A" transform="rotate(20 18 9)"/>
            <circle cx="14" cy="12" r="3" fill="#F4A7B3"/>
            <circle cx="11.5" cy="17" r="2" fill="#2D2D2D"/>
            <circle cx="16.5" cy="17" r="2" fill="#2D2D2D"/>
            <ellipse cx="9" cy="20" rx="3.5" ry="2" fill="#E8637A" opacity="0.25"/>
            <ellipse cx="19" cy="20" rx="3.5" ry="2" fill="#E8637A" opacity="0.25"/>
          </svg>
          <span className="topbar-logo-text">
            <span className="topbar-logo-saku">Saku</span>Ji
          </span>
        </a>

        <nav className="topbar-nav" aria-label="Điều hướng chính">
          <a href="#features" className="topbar-nav-link">Tính năng</a>
          <a href="#pricing" className="topbar-nav-link">Bảng giá</a>
          <a href="#blog" className="topbar-nav-link">Blog</a>
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
          <a href="#features" className="topbar-drawer-link" onClick={closeMenu}>Tính năng</a>
          <a href="#pricing" className="topbar-drawer-link" onClick={closeMenu}>Bảng giá</a>
          <a href="#blog" className="topbar-drawer-link" onClick={closeMenu}>Blog</a>
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
