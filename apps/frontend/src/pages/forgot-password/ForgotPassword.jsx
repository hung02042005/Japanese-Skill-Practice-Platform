import { useState } from 'react';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { forgotPasswordThunk, clearError } from '../../store/slices/authSlice';
import './ForgotPassword.css';

function ForgotPassword() {
  const dispatch = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);

  const [email, setEmail] = useState('');
  const [fieldError, setFieldError] = useState(null);
  const [isSent, setIsSent] = useState(false);

  const isLoading = status === 'loading';

  function validate() {
    if (!email.trim()) { setFieldError('Vui lòng nhập email'); return false; }
    if (!/\S+@\S+\.\S+/.test(email)) { setFieldError('Email không hợp lệ'); return false; }
    setFieldError(null);
    return true;
  }

  function handleEmailChange(e) {
    setEmail(e.target.value);
    if (fieldError) setFieldError(null);
    if (error) dispatch(clearError());
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;
    try {
      await dispatch(forgotPasswordThunk({ email })).unwrap();
      setIsSent(true);
    } catch {
      // lỗi API đã được set vào Redux state
    }
  }

  if (isSent) {
    return (
      <div className="fp-page">
        <div className="fp-container">
          <div className="fp-brand">
            <div className="brand-logo">日</div>
            <h1 className="brand-title">JLPT Platform</h1>
          </div>
          <div className="fp-card">
            <div className="fp-icon-success">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <rect width="48" height="48" rx="24" fill="#E8F5E9"/>
                <path d="M14 24l7 7 13-13" stroke="#1AAE39" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </div>
            <h2 className="fp-card-title">Kiểm tra email của bạn</h2>
            <p className="fp-card-desc">
              Nếu email <strong>{email}</strong> tồn tại trong hệ thống,
              bạn sẽ nhận được link đặt lại mật khẩu trong vài phút.
            </p>
            <p className="fp-card-hint">
              Không nhận được email?{' '}
              <button className="link-btn" onClick={() => { setIsSent(false); dispatch(clearError()); }}>
                Gửi lại
              </button>
            </p>
            <a className="fp-back-link" href="/login">Quay lại đăng nhập</a>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fp-page">
      <div className="fp-container">
        <div className="fp-brand">
          <div className="brand-logo">日</div>
          <h1 className="brand-title">JLPT Platform</h1>
        </div>
        <div className="fp-card">
          <h2 className="fp-card-title">Quên mật khẩu?</h2>
          <p className="fp-card-desc">
            Nhập email đã đăng ký, chúng tôi sẽ gửi link đặt lại mật khẩu cho bạn.
          </p>

          {error && <div className="api-error">{error}</div>}

          <form onSubmit={handleSubmit} noValidate>
            <div className={`form-group ${fieldError ? 'has-error' : ''}`}>
              <label className="form-label" htmlFor="fp-email">Email</label>
              <input
                id="fp-email"
                className="form-input"
                type="email"
                placeholder="example@email.com"
                value={email}
                onChange={handleEmailChange}
                autoComplete="email"
                autoFocus
              />
              {fieldError && <span className="field-error">{fieldError}</span>}
            </div>
            <button className="btn btn-primary" type="submit" disabled={isLoading}>
              {isLoading ? 'Đang gửi...' : 'Gửi link đặt lại mật khẩu'}
            </button>
          </form>

          <a className="fp-back-link" href="/login">Quay lại đăng nhập</a>
        </div>
      </div>
    </div>
  );
}

export default ForgotPassword;
