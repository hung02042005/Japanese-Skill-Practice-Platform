import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { registerThunk, clearError } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import EyeIcon from '../../components/auth/EyeIcon';
import AuthBanner from '../../components/auth/AuthBanner';
import AuthDivider from '../../components/auth/AuthDivider';
import GoogleButton from '../../components/auth/GoogleButton';
import PasswordStrengthBar from '../../components/auth/PasswordStrengthBar';
import './Register.css';

function Register() {
  const dispatch = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);

  const [form, setForm]               = useState({ fullName: '', email: '', password: '', confirmPassword: '' });
  const [fieldErrors, setFieldErrors] = useState({});
  const [showPwd, setShowPwd]         = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [isDone, setIsDone]           = useState(false);

  const isLoading = status === 'loading';
  const confirmOk = form.confirmPassword.length > 0 && form.password === form.confirmPassword;

  const setField = useCallback((name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) setFieldErrors((prev) => ({ ...prev, [name]: null }));
    if (error)             dispatch(clearError());
  }, [fieldErrors, error, dispatch]);

  function validate() {
    const errors = {};
    if (!form.fullName.trim()) {
      errors.fullName = 'Họ tên là bắt buộc';
    } else if (form.fullName.trim().length < 2) {
      errors.fullName = 'Họ tên phải có ít nhất 2 ký tự';
    }
    if (!form.email.trim()) {
      errors.email = 'Email là bắt buộc';
    } else if (!/\S+@\S+\.\S+/.test(form.email)) {
      errors.email = 'Email không hợp lệ';
    }
    if (!form.password) {
      errors.password = 'Mật khẩu là bắt buộc';
    } else if (form.password.length < 8) {
      errors.password = 'Mật khẩu phải có ít nhất 8 ký tự';
    } else if (!/[A-Z]/.test(form.password)) {
      errors.password = 'Mật khẩu cần có ít nhất 1 chữ hoa';
    } else if (!/[0-9]/.test(form.password)) {
      errors.password = 'Mật khẩu cần có ít nhất 1 chữ số';
    }
    if (!form.confirmPassword) {
      errors.confirmPassword = 'Xác nhận mật khẩu là bắt buộc';
    } else if (form.password !== form.confirmPassword) {
      errors.confirmPassword = 'Mật khẩu xác nhận không khớp';
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;
    try {
      await dispatch(
        registerThunk({ fullName: form.fullName, email: form.email, password: form.password }),
      ).unwrap();
      setIsDone(true);
    } catch {
      /* lỗi API đã được set vào Redux state */
    }
  }

  if (isDone) {
    return (
      <div className="reg-page">
        <AuthTopBar />
        <main className="reg-main">
          <div className="auth-card reg-success-card">
            <SakuChan variant="happy" />
            <h1 className="auth-title">Kiểm tra hộp thư của bạn!</h1>
            <p className="reg-success-desc">
              Chúng tôi đã gửi email xác minh đến<br/>
              <strong className="reg-success-email">{form.email}</strong>
            </p>
            <p className="reg-success-hint">
              Nhấp vào liên kết trong email để kích hoạt tài khoản.
            </p>
            <Link to="/login" className="btn-submit reg-success-btn">
              ← Quay lại đăng nhập
            </Link>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="reg-page">
      <AuthTopBar />

      <main className="reg-main">
        <span className="reg-petal reg-petal--1" aria-hidden="true">🌸</span>
        <span className="reg-petal reg-petal--2" aria-hidden="true">🌸</span>
        <span className="reg-petal reg-petal--3" aria-hidden="true">🌸</span>

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
                aria-describedby={fieldErrors.fullName ? 'reg-name-err' : undefined}
                aria-invalid={!!fieldErrors.fullName}
              />
              {fieldErrors.fullName && (
                <span id="reg-name-err" className="field-error">{fieldErrors.fullName}</span>
              )}
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
                aria-describedby={fieldErrors.email ? 'reg-email-err' : undefined}
                aria-invalid={!!fieldErrors.email}
              />
              {fieldErrors.email && (
                <span id="reg-email-err" className="field-error">{fieldErrors.email}</span>
              )}
            </div>

            <div className={`form-field${fieldErrors.password ? ' has-error' : ''}`}>
              <label className="form-label" htmlFor="reg-pass">Mật khẩu</label>
              <div className="form-input-wrapper">
                <input
                  id="reg-pass"
                  className="form-input"
                  type={showPwd ? 'text' : 'password'}
                  placeholder="Tối thiểu 8 ký tự"
                  value={form.password}
                  onChange={(e) => setField('password', e.target.value)}
                  autoComplete="new-password"
                  aria-describedby="reg-pwd-strength reg-pass-err"
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
              <PasswordStrengthBar password={form.password} />
              {fieldErrors.password ? (
                <span id="reg-pass-err" className="field-error">{fieldErrors.password}</span>
              ) : (
                !form.password && <span className="form-hint">Tối thiểu 8 ký tự, 1 chữ hoa, 1 chữ số</span>
              )}
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
                  aria-describedby={fieldErrors.confirmPassword ? 'reg-confirm-err' : undefined}
                  aria-invalid={!!fieldErrors.confirmPassword}
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
              {fieldErrors.confirmPassword && (
                <span id="reg-confirm-err" className="field-error">{fieldErrors.confirmPassword}</span>
              )}
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

          <GoogleButton
            onClick={() => { window.location.href = 'http://localhost:8080/api/auth/oauth/google'; }}
            ariaLabel="Đăng ký bằng tài khoản Google"
          >
            Đăng ký với Google
          </GoogleButton>

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
