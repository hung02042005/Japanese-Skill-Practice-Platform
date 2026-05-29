import { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import './ResetPassword.css';

function ResetPassword() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [isDone, setIsDone] = useState(false);

  function validate() {
    const errors = {};
    if (!newPassword) errors.newPassword = 'Vui lòng nhập mật khẩu mới';
    else if (newPassword.length < 8) errors.newPassword = 'Mật khẩu phải có ít nhất 8 ký tự';
    else if (!/[A-Z]/.test(newPassword)) errors.newPassword = 'Mật khẩu phải có ít nhất 1 chữ hoa';
    else if (!/[0-9]/.test(newPassword)) errors.newPassword = 'Mật khẩu phải có ít nhất 1 số';
    if (!confirmPassword) errors.confirmPassword = 'Vui lòng xác nhận mật khẩu';
    else if (newPassword !== confirmPassword) errors.confirmPassword = 'Mật khẩu xác nhận không khớp';
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (validate() && token) setIsDone(true);
  }

  if (!token) {
    return (
      <div className="rp-page">
        <div className="rp-container">
          <div className="fp-brand">
            <div className="brand-logo">日</div>
            <h1 className="brand-title">JLPT Platform</h1>
          </div>
          <div className="fp-card">
            <div className="rp-invalid">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <rect width="48" height="48" rx="24" fill="#FDE0EC"/>
                <path d="M16 16l16 16M32 16l-16 16" stroke="#E03131" strokeWidth="2.5" strokeLinecap="round"/>
              </svg>
              <h2 className="fp-card-title">Link không hợp lệ</h2>
              <p className="fp-card-desc">
                Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.
                Vui lòng yêu cầu gửi lại link mới.
              </p>
              <a className="btn btn-primary" href="/forgot-password">Gửi lại link</a>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (isDone) {
    return (
      <div className="rp-page">
        <div className="rp-container">
          <div className="fp-brand">
            <div className="brand-logo">日</div>
            <h1 className="brand-title">JLPT Platform</h1>
          </div>
          <div className="fp-card">
            <div className="rp-success">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <rect width="48" height="48" rx="24" fill="#E8F5E9"/>
                <path d="M14 24l7 7 13-13" stroke="#1AAE39" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              <h2 className="fp-card-title">Đặt lại mật khẩu thành công</h2>
              <p className="fp-card-desc">Mật khẩu của bạn đã được cập nhật.</p>
              <a className="btn btn-primary" href="/login">Đăng nhập ngay</a>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="rp-page">
      <div className="rp-container">
        <div className="fp-brand">
          <div className="brand-logo">日</div>
          <h1 className="brand-title">JLPT Platform</h1>
        </div>
        <div className="fp-card">
          <h2 className="fp-card-title">Đặt lại mật khẩu</h2>
          <p className="fp-card-desc">Nhập mật khẩu mới cho tài khoản của bạn.</p>

          <form onSubmit={handleSubmit} noValidate>
            <div className={`form-group ${fieldErrors.newPassword ? 'has-error' : ''}`}>
              <label className="form-label" htmlFor="rp-new">Mật khẩu mới</label>
              <input
                id="rp-new"
                className="form-input"
                type="password"
                placeholder="Ít nhất 8 ký tự, 1 hoa, 1 số"
                value={newPassword}
                onChange={(e) => { setNewPassword(e.target.value); if (fieldErrors.newPassword) setFieldErrors((p) => ({ ...p, newPassword: null })); }}
                autoComplete="new-password"
                autoFocus
              />
              {fieldErrors.newPassword && <span className="field-error">{fieldErrors.newPassword}</span>}
            </div>

            <div className={`form-group ${fieldErrors.confirmPassword ? 'has-error' : ''}`}>
              <label className="form-label" htmlFor="rp-confirm">Xác nhận mật khẩu mới</label>
              <input
                id="rp-confirm"
                className="form-input"
                type="password"
                placeholder="Nhập lại mật khẩu mới"
                value={confirmPassword}
                onChange={(e) => { setConfirmPassword(e.target.value); if (fieldErrors.confirmPassword) setFieldErrors((p) => ({ ...p, confirmPassword: null })); }}
                autoComplete="new-password"
              />
              {fieldErrors.confirmPassword && <span className="field-error">{fieldErrors.confirmPassword}</span>}
            </div>

            <button className="btn btn-primary" type="submit">Đặt lại mật khẩu</button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default ResetPassword;