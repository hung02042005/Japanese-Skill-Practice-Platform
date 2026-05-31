import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { loginThunk, clearError } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import EyeIcon from '../../components/auth/EyeIcon';
import AuthBanner from '../../components/auth/AuthBanner';
import AuthDivider from '../../components/auth/AuthDivider';
import GoogleButton from '../../components/auth/GoogleButton';
import './Login.css';

function Login() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);

  const [email, setEmail]             = useState('');
  const [password, setPassword]       = useState('');
  const [showPwd, setShowPwd]         = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  const isLoading = status === 'loading';

  function validateForm() {
    const errors = {};
    if (!email.trim()) {
      errors.email = 'Email là bắt buộc';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      errors.email = 'Email không hợp lệ';
    }
    if (!password) errors.password = 'Mật khẩu là bắt buộc';
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  function handleEmailChange(e) {
    setEmail(e.target.value);
    if (fieldErrors.email) setFieldErrors((p) => ({ ...p, email: null }));
    if (error)             dispatch(clearError());
  }

  function handlePasswordChange(e) {
    setPassword(e.target.value);
    if (fieldErrors.password) setFieldErrors((p) => ({ ...p, password: null }));
    if (error)                dispatch(clearError());
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!validateForm()) return;
    try {
      await dispatch(loginThunk({ email, password })).unwrap();
      navigate('/dashboard');
    } catch {
      /* lỗi API đã được set vào Redux state */
    }
  }

  const isLocked   = error && (error.includes('khóa') || error.includes('locked'));
  const needVerify = error && (error.includes('xác minh') || error.includes('verified'));

  return (
    <div className="login-page">
      <AuthTopBar />

      <main className="login-main">
        <span className="login-petal login-petal--1" aria-hidden="true">🌸</span>
        <span className="login-petal login-petal--2" aria-hidden="true">🌸</span>
        <span className="login-petal login-petal--3" aria-hidden="true">🌸</span>

        <div className="auth-card" role="main">
          <SakuChan />

          <h1 className="auth-title">Chào mừng trở lại</h1>
          <p className="auth-subtitle">Đăng nhập để tiếp tục hành trình học tiếng Nhật</p>

          {isLocked && <AuthBanner type="warning">{error}</AuthBanner>}
          {needVerify && (
            <AuthBanner type="info">
              {error}{' '}
              <Link to="/register" className="auth-banner-link">Gửi lại email xác minh</Link>
            </AuthBanner>
          )}
          {error && !isLocked && !needVerify && (
            <AuthBanner type="error">{error}</AuthBanner>
          )}

          <form className="auth-form" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
            <div className={`form-field${fieldErrors.email ? ' has-error' : ''}`}>
              <label className="form-label" htmlFor="login-email">Email</label>
              <input
                id="login-email"
                className="form-input"
                type="email"
                placeholder="email@example.com"
                value={email}
                onChange={handleEmailChange}
                autoComplete="email"
                autoFocus
                aria-describedby={fieldErrors.email ? 'login-email-err' : undefined}
                aria-invalid={!!fieldErrors.email}
              />
              {fieldErrors.email && (
                <span id="login-email-err" className="field-error">{fieldErrors.email}</span>
              )}
            </div>

            <div className={`form-field${fieldErrors.password ? ' has-error' : ''}`}>
              <div className="form-label-row">
                <label className="form-label" htmlFor="login-password">Mật khẩu</label>
                <Link to="/forgot-password" className="form-forgot-link">Quên mật khẩu?</Link>
              </div>
              <div className="form-input-wrapper">
                <input
                  id="login-password"
                  className="form-input"
                  type={showPwd ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={password}
                  onChange={handlePasswordChange}
                  autoComplete="current-password"
                  aria-describedby={fieldErrors.password ? 'login-pwd-err' : undefined}
                  aria-invalid={!!fieldErrors.password}
                />
                <button
                  type="button"
                  className="form-eye-btn"
                  onClick={() => setShowPwd((v) => !v)}
                  aria-label={showPwd ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  <EyeIcon open={showPwd} />
                </button>
              </div>
              {fieldErrors.password && (
                <span id="login-pwd-err" className="field-error">{fieldErrors.password}</span>
              )}
            </div>

            <button
              className="btn-submit"
              type="submit"
              disabled={isLoading}
              aria-label="Đăng nhập vào tài khoản"
            >
              {isLoading ? (
                <><span className="btn-spinner" aria-hidden="true"/>Đang đăng nhập...</>
              ) : 'ĐĂNG NHẬP'}
            </button>
          </form>

          <AuthDivider />

          <GoogleButton
            onClick={() => { window.location.href = 'http://localhost:8080/api/auth/oauth/google'; }}
            ariaLabel="Đăng nhập bằng tài khoản Google"
          >
            Tiếp tục với Google
          </GoogleButton>

          <p className="auth-redirect">
            Chưa có tài khoản?{' '}
            <Link to="/register" className="auth-redirect-link">Đăng ký ngay</Link>
          </p>
        </div>
      </main>
    </div>
  );
}

export default Login;
