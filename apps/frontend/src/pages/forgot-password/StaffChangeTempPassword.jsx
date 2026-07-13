import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { changeTempPasswordThunk, clearError } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import AuthBanner from '../../components/auth/AuthBanner';
import EyeIcon from '../../components/auth/EyeIcon';
import PasswordStrengthBar from '../../components/auth/PasswordStrengthBar';
import { SakuraIcon } from '../../components/common/AppIcons';
import './ResetPassword.css';

function StaffChangeTempPassword() {
  const dispatch = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [isDone, setIsDone] = useState(false);

  // Tránh hiện lại lỗi còn sót từ trang auth khác khi điều hướng client-side sang đây.
  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  const isLoading = status === 'loading';
  const confirmOk = confirmPassword.length > 0 && newPassword === confirmPassword;

  function readStoredUser() {
    try { return JSON.parse(localStorage.getItem('jlpt-user')); } catch { return null; }
  }
  const storedUser = readStoredUser();
  const hasLimitedToken =
    storedUser?.requirePasswordChange === true && Boolean(localStorage.getItem('accessToken'));

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      await dispatch(changeTempPasswordThunk({ newPassword, confirmPassword })).unwrap();
      setIsDone(true);
    } catch {
      /* lỗi API đã được set vào Redux state */
    }
  }

  if (!hasLimitedToken && !isDone) {
    return (
      <div className="rp-page">
        <AuthTopBar />
        <main className="rp-main">
          <div className="auth-card rp-status-card">
            <SakuChan variant="sleepy" />
            <h1 className="auth-title">Phiên đổi mật khẩu không hợp lệ</h1>
            <p className="rp-status-desc">
              Vui lòng đăng nhập bằng mật khẩu tạm thời đã được gửi qua email để đặt mật khẩu mới.
            </p>
            <Link to="/login" className="btn-submit rp-status-btn">ĐĂNG NHẬP LẠI</Link>
          </div>
        </main>
      </div>
    );
  }

  if (isDone) {
    return (
      <div className="rp-page">
        <AuthTopBar />
        <main className="rp-main">
          <div className="auth-card rp-status-card">
            <SakuChan variant="happy" />
            <h1 className="auth-title">Đặt mật khẩu mới thành công</h1>
            <p className="rp-status-desc">
              Phiên tạm thời đã được thu hồi. Hãy đăng nhập lại bằng mật khẩu mới.
            </p>
            <Link to="/login" className="btn-submit rp-status-btn">ĐĂNG NHẬP NGAY</Link>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="rp-page">
      <AuthTopBar />
      <main className="rp-main">
        <span className="rp-petal rp-petal--1" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="rp-petal rp-petal--2" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="rp-petal rp-petal--3" aria-hidden="true"><SakuraIcon size={18} /></span>

        <div className="auth-card" role="main">
          <SakuChan />
          <h1 className="auth-title">Đặt mật khẩu nhân viên mới</h1>
          <p className="auth-subtitle">
            Mật khẩu tạm thời chỉ dùng để vào màn hình này. Sau khi đổi thành công, bạn cần đăng nhập lại.
          </p>

          {error && <AuthBanner type="error">{error}</AuthBanner>}

          <form className="auth-form" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
            <div className="form-field">
              <label className="form-label" htmlFor="staff-rp-new">Mật khẩu mới</label>
              <div className="form-input-wrapper">
                <input
                  id="staff-rp-new"
                  className="form-input"
                  type={showNew ? 'text' : 'password'}
                  placeholder="Ít nhất 8 ký tự, 1 hoa, 1 số"
                  value={newPassword}
                  onChange={(e) => {
                    setNewPassword(e.target.value);
                    if (error) dispatch(clearError());
                  }}
                  autoComplete="new-password"
                  autoFocus
                />
                <button
                  type="button"
                  className="form-eye-btn"
                  onClick={() => setShowNew((v) => !v)}
                  aria-label={showNew ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  <EyeIcon open={showNew} />
                </button>
              </div>
              <PasswordStrengthBar password={newPassword} />
            </div>

            <div className="form-field">
              <label className="form-label" htmlFor="staff-rp-confirm">Xác nhận mật khẩu mới</label>
              <div className="form-input-wrapper">
                <input
                  id="staff-rp-confirm"
                  className="form-input"
                  type={showConfirm ? 'text' : 'password'}
                  placeholder="Nhập lại mật khẩu mới"
                  value={confirmPassword}
                  onChange={(e) => {
                    setConfirmPassword(e.target.value);
                    if (error) dispatch(clearError());
                  }}
                  autoComplete="new-password"
                />
                {!confirmPassword ? (
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
                      <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  </span>
                ) : (
                  <span className="form-match-icon form-match-icon--err" aria-label="Mật khẩu không khớp">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
                    </svg>
                  </span>
                )}
              </div>
            </div>

            <button className="btn-submit" type="submit" disabled={isLoading}>
              {isLoading ? <><span className="btn-spinner" aria-hidden="true" />Đang xử lý...</> : 'CẬP NHẬT MẬT KHẨU'}
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}

export default StaffChangeTempPassword;
