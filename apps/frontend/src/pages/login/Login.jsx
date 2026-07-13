import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { loginThunk, clearError, loginWithGoogleThunk } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import EyeIcon from '../../components/auth/EyeIcon';
import AuthBanner from '../../components/auth/AuthBanner';
import AuthDivider from '../../components/auth/AuthDivider';
import { SakuraIcon, WrenchIcon } from '../../components/common/AppIcons';
import './Login.css';

function Login() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);

  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');

  // Tránh hiện lại lỗi còn sót từ trang auth khác (register/forgot-password...)
  // khi điều hướng client-side sang đây — state.auth.error dùng chung cho nhiều thunk.
  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);
  const [showPwd, setShowPwd]   = useState(false);

  const isLoading = status === 'loading';

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      const res = await dispatch(loginThunk({ email, password })).unwrap();
      if (res.requirePasswordChange) {
        navigate('/staff/change-temp-password');
      } else if (res.role === 'ADMIN' || res.user?.role === 'ADMIN') {
        navigate('/admin');
      } else if (res.role === 'STAFF' || res.user?.role === 'STAFF') {
        if (res.user?.staffRole === 'staff_manager') {
          navigate('/manager');
        } else {
          navigate('/staff');
        }
      } else {
        // Student chưa hoàn thành onboarding → vào màn onboarding trước.
        navigate(res.user?.onboardingCompleted === false ? '/onboarding' : '/dashboard');
      }
    } catch {
      /* lỗi API đã được set vào Redux state */
    }
  }

  async function handleGoogleSuccess(credentialResponse) {
    try {
      const user = await dispatch(loginWithGoogleThunk(credentialResponse.credential)).unwrap();
      navigate(user?.onboardingCompleted === false ? '/onboarding' : '/dashboard');
    } catch {
      /* lỗi đã set vào Redux state */
    }
  }

  const isMaintenance = error && error.includes('bảo trì');
  const isLocked   = error && !isMaintenance && (error.includes('khóa') || error.includes('locked'));
  const needVerify = error && !isMaintenance && !isLocked && (error.includes('xác minh') || error.toLowerCase().includes('verified'));

  return (
    <div className="login-page">
      <AuthTopBar />

      <main className="login-main">
        <span className="login-petal login-petal--1" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="login-petal login-petal--2" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="login-petal login-petal--3" aria-hidden="true"><SakuraIcon size={18} /></span>

        <div className="auth-card" role="main">
          <SakuChan />

          <h1 className="auth-title">Chào mừng trở lại</h1>
          <p className="auth-subtitle">Đăng nhập để tiếp tục hành trình học tiếng Nhật</p>

          {isMaintenance && <AuthBanner type="warning"><WrenchIcon size={16} /> {error}</AuthBanner>}
          {isLocked && <AuthBanner type="warning">{error}</AuthBanner>}
          {needVerify && (
            <AuthBanner type="info">
              {error}{' '}
              <Link
                to={`/verify-email?email=${encodeURIComponent(email)}`}
                className="auth-banner-link"
              >
                Nhập mã xác minh
              </Link>
            </AuthBanner>
          )}
          {error && !isMaintenance && !isLocked && !needVerify && (
            <AuthBanner type="error">{error}</AuthBanner>
          )}

          <form className="auth-form" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
            <div className="form-field">
              <label className="form-label" htmlFor="login-email">Email</label>
              <input
                id="login-email"
                className="form-input"
                type="email"
                placeholder="email@example.com"
                value={email}
                onChange={(e) => { setEmail(e.target.value); if (error) dispatch(clearError()); }}
                autoComplete="email"
                autoFocus
              />
            </div>

            <div className="form-field">
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
                  onChange={(e) => { setPassword(e.target.value); if (error) dispatch(clearError()); }}
                  autoComplete="current-password"
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

          <div className="google-login-wrapper">
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={() => dispatch(clearError())}
              text="signin_with"
              shape="rectangular"
              locale="vi"
              width="100%"
            />
          </div>

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
