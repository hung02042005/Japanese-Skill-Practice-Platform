import { useState, useCallback, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { registerThunk, clearError, loginWithGoogleThunk } from '@/features/auth/authSlice';
import AuthTopBar from '@/features/auth/components/AuthTopBar';
import SakuChan from '@/shared/components/common/SakuChan';
import EyeIcon from '@/features/auth/components/EyeIcon';
import AuthBanner from '@/features/auth/components/AuthBanner';
import AuthDivider from '@/features/auth/components/AuthDivider';
import { SakuraIcon } from '@/shared/components/common/AppIcons';
import { emailError, passwordError, confirmError, isBlank } from '@/shared/utils/validation';
import './Register.css';

function Register() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { status, error, errorCode } = useAppSelector((state) => state.auth);

  const [form, setForm]               = useState({ fullName: '', email: '', password: '', confirmPassword: '' });
  const [showPwd, setShowPwd]         = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  // Tránh hiện lại lỗi còn sót từ trang auth khác (login/forgot-password...)
  // khi điều hướng client-side sang đây — state.auth.error dùng chung cho nhiều thunk.
  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  const isLoading = status === 'loading';
  const confirmOk = form.confirmPassword.length > 0 && form.password === form.confirmPassword;

  const setField = useCallback((name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
    setFieldErrors((prev) => (prev[name] ? { ...prev, [name]: '' } : prev));
    if (error) dispatch(clearError());
  }, [error, dispatch]);

  function validate() {
    const errs = {};
    if (isBlank(form.fullName)) errs.fullName = 'Họ tên là bắt buộc';
    else if (form.fullName.trim().length < 2) errs.fullName = 'Họ tên phải có ít nhất 2 ký tự';
    const emErr = emailError(form.email);
    if (emErr) errs.email = emErr;
    const pwErr = passwordError(form.password);
    if (pwErr) errs.password = pwErr;
    const cfErr = confirmError(form.password, form.confirmPassword);
    if (cfErr) errs.confirmPassword = cfErr;
    return errs;
  }

  async function handleSubmit(e) {
    e.preventDefault();

    const errs = validate();
    setFieldErrors(errs);
    if (Object.keys(errs).length > 0) return;

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
              {errorCode === 'EMAIL_EXISTS' && (
                <> <Link to="/login" className="auth-banner-link">Đăng nhập ngay?</Link></>
              )}
            </AuthBanner>
          )}

          <form className="auth-form" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
            <div className={`form-field${fieldErrors.fullName ? ' has-error' : ''}`}>
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
                aria-invalid={!!fieldErrors.fullName}
              />
              {fieldErrors.fullName && <span className="field-error">{fieldErrors.fullName}</span>}
            </div>

            <div className={`form-field${fieldErrors.email ? ' has-error' : ''}`}>
              <label className="form-label" htmlFor="reg-email">Email</label>
              <input
                id="reg-email"
                className="form-input"
                type="email"
                placeholder="email@example.com"
                value={form.email}
                onChange={(e) => setField('email', e.target.value)}
                autoComplete="email"
                aria-invalid={!!fieldErrors.email}
              />
              {fieldErrors.email && <span className="field-error">{fieldErrors.email}</span>}
            </div>

            <div className={`form-field${fieldErrors.password ? ' has-error' : ''}`}>
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
              {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
            </div>

            <div className={`form-field${fieldErrors.confirmPassword ? ' has-error' : ''}`}>
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
              {fieldErrors.confirmPassword && <span className="field-error">{fieldErrors.confirmPassword}</span>}
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
