import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { loginThunk, verifyMfaThunk, clearError, loginWithGoogleThunk } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import EyeIcon from '../../components/auth/EyeIcon';
import AuthBanner from '../../components/auth/AuthBanner';
import AuthDivider from '../../components/auth/AuthDivider';
import './Login.css';

function Login() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { status, error, requiresTwoFactor, mfaToken, tempRole } = useAppSelector((state) => state.auth);

  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [showPwd, setShowPwd]   = useState(false);
  const [otpCode, setOtpCode]   = useState('');

  const isLoading = status === 'loading';

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      const res = await dispatch(loginThunk({ email, password })).unwrap();
      if (!res.requiresTwoFactor) {
        if (res.role === 'ADMIN' || res.user?.role === 'ADMIN') {
          navigate('/admin/users');
        } else {
          navigate('/dashboard');
        }
      }
    } catch {
      /* lỗi API đã được set vào Redux state */
    }
  }

  async function handleOtpSubmit(e) {
    e.preventDefault();
    try {
      await dispatch(verifyMfaThunk({ mfaToken, totpCode: otpCode })).unwrap();
      if (tempRole === 'ADMIN') {
        navigate('/admin/users');
      } else {
        navigate('/dashboard');
      }
    } catch {
      /* Lỗi được set vào Redux state */
    }
  }

  async function handleGoogleSuccess(credentialResponse) {
    try {
      await dispatch(loginWithGoogleThunk(credentialResponse.credential)).unwrap();
      navigate('/dashboard');
    } catch {
      /* lỗi đã set vào Redux state */
    }
  }

  const isLocked   = error && (error.includes('khóa') || error.includes('locked'));
  const needVerify = error && (error.includes('xác minh') || error.includes('verified'));

  if (requiresTwoFactor) {
    return (
      <div className="login-page">
        <AuthTopBar />

        <main className="login-main">
          <span className="login-petal login-petal--1" aria-hidden="true">🌸</span>
          <span className="login-petal login-petal--2" aria-hidden="true">🌸</span>
          <span className="login-petal login-petal--3" aria-hidden="true">🌸</span>

          <div className="auth-card" role="main">
            <SakuChan />

            <h1 className="auth-title">Xác thực 2 yếu tố</h1>
            <p className="auth-subtitle">Nhập mã OTP 6 chữ số từ ứng dụng Authenticator của bạn</p>

            {error && <AuthBanner type="error">{error}</AuthBanner>}

            <form className="auth-form" onSubmit={handleOtpSubmit} noValidate aria-busy={isLoading}>
              <div className="form-field">
                <label className="form-label" htmlFor="otp-code">Mã OTP</label>
                <input
                  id="otp-code"
                  className="form-input text-center tracking-[0.5em] text-2xl font-bold"
                  type="text"
                  placeholder="000000"
                  maxLength={6}
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ''))}
                  autoFocus
                />
              </div>

              <button
                className="btn-submit"
                type="submit"
                disabled={isLoading}
                aria-label="Xác nhận mã OTP"
              >
                {isLoading ? (
                  <><span className="btn-spinner" aria-hidden="true"/>Đang xác thực...</>
                ) : 'XÁC NHẬN'}
              </button>
            </form>

            <p className="auth-redirect">
              <button
                onClick={() => {
                  dispatch(clearError());
                  setOtpCode('');
                }}
                className="auth-redirect-link bg-transparent border-0 cursor-pointer text-sm underline hover:text-sakura"
                style={{ background: 'none', border: 'none', cursor: 'pointer' }}
              >
                Quay lại đăng nhập
              </button>
            </p>
          </div>
        </main>
      </div>
    );
  }

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
