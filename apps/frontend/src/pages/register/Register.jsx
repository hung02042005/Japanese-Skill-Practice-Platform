import { useState, useCallback, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { registerThunk, clearError, loginWithGoogleThunk } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import EyeIcon from '../../components/auth/EyeIcon';
import AuthBanner from '../../components/auth/AuthBanner';
import AuthDivider from '../../components/auth/AuthDivider';
import { SakuraIcon } from '../../components/common/AppIcons';
import './Register.css';

function Register() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);

  const [form, setForm]               = useState({ fullName: '', email: '', password: '', confirmPassword: '' });
  const [showPwd, setShowPwd]         = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  // Tránh hiện lại lỗi còn sót từ trang auth khác (login/forgot-password...)
  // khi điều hướng client-side sang đây — state.auth.error dùng chung cho nhiều thunk.
  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  const isLoading = status === 'loading';
  const confirmOk = form.confirmPassword.length > 0 && form.password === form.confirmPassword;

  const setField = useCallback((name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
    if (error) dispatch(clearError());
  }, [error, dispatch]);

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      await dispatch(
        registerThunk({ fullName: form.fullName, email: form.email, password: form.password, confirmPassword: form.confirmPassword }),
      ).unwrap();
      // Đăng ký đã tự gửi mã OTP xác minh — chuyển sang trang nhập mã.
      navigate(`/verify-email?email=${encodeURIComponent(form.email)}`);
    } catch {
      /* lỗi API đã được set vào Redux state */
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

  return (
    <div className="reg-page">
      <AuthTopBar />

      <main className="reg-main">
        <span className="reg-petal reg-petal--1" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="reg-petal reg-petal--2" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="reg-petal reg-petal--3" aria-hidden="true"><SakuraIcon size={18} /></span>

        <div className="auth-card" role="main">
          <SakuChan />

          <h1 className="auth-title">Tạo tài khoản mới</h1>
          <p className="auth-subtitle">Bắt đầu hành trình học tiếng Nhật của bạn miễn phí</p>

          {error && (
            <AuthBanner type="error">
              {error}
              {error.includes('đã được sử dụng') && (
                <> <Link to="/login" className="auth-banner-link">Đăng nhập ngay?</Link></>
              )}
            </AuthBanner>
          )}

          <form className="auth-form" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
            <div className="form-field">
              <label className="form-label" htmlFor="reg-name">Họ và tên</label>
              <input
                id="reg-name"
                className="form-input"
                type="text"
                placeholder="Nguyễn Văn A"
                value={form.fullName}
                onChange={(e) => setField('fullName', e.target.value)}
                autoComplete="name"
                autoFocus
              />
            </div>

            <div className="form-field">
              <label className="form-label" htmlFor="reg-email">Email</label>
              <input
                id="reg-email"
                className="form-input"
                type="email"
                placeholder="email@example.com"
                value={form.email}
                onChange={(e) => setField('email', e.target.value)}
                autoComplete="email"
              />
            </div>

            <div className="form-field">
              <label className="form-label" htmlFor="reg-pass">Mật khẩu</label>
              <div className="form-input-wrapper">
                <input
                  id="reg-pass"
                  className="form-input"
                  type={showPwd ? 'text' : 'password'}
                  placeholder="Tối thiểu 8 ký tự, 1 chữ hoa, 1 chữ số"
                  value={form.password}
                  onChange={(e) => setField('password', e.target.value)}
                  autoComplete="new-password"
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

            <div className="form-field">
              <label className="form-label" htmlFor="reg-confirm">Xác nhận mật khẩu</label>
              <div className="form-input-wrapper">
                <input
                  id="reg-confirm"
                  className="form-input"
                  type={showConfirm ? 'text' : 'password'}
                  placeholder="Nhập lại mật khẩu"
                  value={form.confirmPassword}
                  onChange={(e) => setField('confirmPassword', e.target.value)}
                  autoComplete="new-password"
                />
                {!form.confirmPassword ? (
                  <button
                    type="button"
                    className="form-eye-btn"
                    onClick={() => setShowConfirm((v) => !v)}
                    aria-label={showConfirm ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  >
                    <EyeIcon open={showConfirm} />
                  </button>
                ) : confirmOk ? (
                  <span className="form-match-icon form-match-icon--ok" aria-label="Mật khẩu khớp">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  </span>
                ) : (
                  <span className="form-match-icon form-match-icon--err" aria-label="Mật khẩu không khớp">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
                    </svg>
                  </span>
                )}
              </div>
            </div>

            <button
              className="btn-submit"
              type="submit"
              disabled={isLoading}
              aria-label="Tạo tài khoản"
            >
              {isLoading ? (
                <><span className="btn-spinner" aria-hidden="true"/>Đang tạo tài khoản...</>
              ) : 'TẠO TÀI KHOẢN'}
            </button>
          </form>

          <AuthDivider />

          <div className="google-login-wrapper">
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={() => dispatch(clearError())}
              text="signup_with"
              shape="rectangular"
              locale="vi"
              width="100%"
            />
          </div>

          <p className="auth-redirect">
            Đã có tài khoản?{' '}
            <Link to="/login" className="auth-redirect-link">Đăng nhập</Link>
          </p>

          <p className="reg-terms">
            Bằng cách đăng ký, bạn đồng ý với{' '}
            <a href="/terms" className="reg-terms-link">Điều khoản dịch vụ</a>
            {' '}và{' '}
            <a href="/privacy" className="reg-terms-link">Chính sách bảo mật</a>
          </p>
        </div>
      </main>
    </div>
  );
}

export default Register;
