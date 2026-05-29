import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  function validateForm() {
    const errors = {};
    if (!email.trim()) errors.email = 'Vui lòng nhập email';
    if (!password) errors.password = 'Vui lòng nhập mật khẩu';
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (validateForm()) {
      navigate('/dashboard');
    }
  }

  function handleGoogleLogin() {
    window.location.href = 'http://localhost:8080/api/auth/oauth/google';
  }

  return (
    <div className="login-page">
      <div className="login-container">
        <div className="login-brand">
          <div className="brand-logo">日</div>
          <h1 className="brand-title">JLPT Platform</h1>
          <p className="brand-subtitle">Hệ thống học tiếng Nhật trực tuyến</p>
        </div>

        <form className="login-form" onSubmit={handleSubmit} noValidate>
          <h2 className="form-title">Đăng nhập</h2>
          <p className="form-description">
            Chào mừng bạn trở lại! Vui lòng đăng nhập để tiếp tục.
          </p>

          <div className={`form-group ${fieldErrors.email ? 'has-error' : ''}`}>
            <label className="form-label" htmlFor="email">Email</label>
            <input
              id="email"
              className="form-input"
              type="email"
              placeholder="example@email.com"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (fieldErrors.email) setFieldErrors((prev) => ({ ...prev, email: null }));
              }}
              autoComplete="email"
              autoFocus
            />
            {fieldErrors.email && <span className="field-error">{fieldErrors.email}</span>}
          </div>

          <div className={`form-group ${fieldErrors.password ? 'has-error' : ''}`}>
            <div className="form-label-row">
              <label className="form-label" htmlFor="password">Mật khẩu</label>
              <a className="forgot-link" href="/forgot-password">Quên mật khẩu?</a>
            </div>
            <input
              id="password"
              className="form-input"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (fieldErrors.password) setFieldErrors((prev) => ({ ...prev, password: null }));
              }}
              autoComplete="current-password"
            />
            {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
          </div>

          <div className="form-group form-group-checkbox">
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
              />
              <span className="checkbox-custom"></span>
              Ghi nhớ đăng nhập
            </label>
          </div>

          <button className="btn btn-primary" type="submit">
            Đăng nhập
          </button>

          <div className="divider">
            <span className="divider-text">Hoặc</span>
          </div>

          <button className="btn btn-google" type="button" onClick={handleGoogleLogin}>
            <svg className="google-icon" viewBox="0 0 24 24" width="20" height="20">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            Đăng nhập với Google
          </button>

          <p className="register-link">
            Chưa có tài khoản?{' '}
            <a href="/register">Đăng ký ngay</a>
          </p>
        </form>
      </div>
    </div>
  );
}

export default Login;