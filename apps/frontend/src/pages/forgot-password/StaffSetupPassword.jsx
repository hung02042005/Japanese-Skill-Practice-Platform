import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { setupStaffPasswordThunk, clearError } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import AuthBanner from '../../components/auth/AuthBanner';
import EyeIcon from '../../components/auth/EyeIcon';
import PasswordStrengthBar from '../../components/auth/PasswordStrengthBar';
import { SakuraIcon } from '../../components/common/AppIcons';
import { passwordError, confirmError } from '../../utils/validation';
import './ResetPassword.css';

function StaffSetupPassword() {
  const dispatch = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);

  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [isDone, setIsDone] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});

  const isLoading = status === 'loading';
  const confirmOk = confirmPassword.length > 0 && newPassword === confirmPassword;

  async function handleSubmit(e) {
    e.preventDefault();
    if (!token) return;

    const errs = {};
    const pwErr = passwordError(newPassword);
    if (pwErr) errs.newPassword = pwErr;
    const cfErr = confirmError(newPassword, confirmPassword);
    if (cfErr) errs.confirmPassword = cfErr;
    setFieldErrors(errs);
    if (Object.keys(errs).length > 0) return;

    try {
      await dispatch(setupStaffPasswordThunk({ token, newPassword, confirmPassword })).unwrap();
      setIsDone(true);
    } catch {
      /* lỗi API đã được set vào Redux state */
    }
  }

  if (!token) {
    return (
      <div className="rp-page">
        <AuthTopBar />
        <main className="rp-main">
          <div className="auth-card rp-status-card">
            <div className="rp-status-icon" aria-hidden="true">
              <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
                <rect width="56" height="56" rx="28" fill="#FDE0EC"/>
                <path d="M20 20l16 16M36 20L20 36" stroke="#E03131" strokeWidth="2.8" strokeLinecap="round"/>
              </svg>
            </div>
            <h1 className="auth-title">Link không hợp lệ</h1>
            <p className="rp-status-desc">
              Link kích hoạt tài khoản không hợp lệ hoặc đã hết hạn.
              Vui lòng liên hệ Quản trị viên để được cấp lại.
            </p>
            <Link to="/login" className="btn-submit rp-status-btn">VỀ TRANG ĐĂNG NHẬP</Link>
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
            <h1 className="auth-title">Kích hoạt tài khoản thành công!</h1>
            <p className="rp-status-desc">
              Tài khoản nhân viên đã được kích hoạt. Hãy đăng nhập để bắt đầu làm việc.
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
          <h1 className="auth-title">Thiết lập mật khẩu nhân viên</h1>
          <p className="auth-subtitle">
            Chào mừng bạn đến với JLPT Platform! Hãy đặt mật khẩu để kích hoạt tài khoản.
          </p>

          {error && (
            <AuthBanner type="error">
              {error}
              {(error.includes('hết hạn') || error.includes('không hợp lệ')) && (
                <> Vui lòng liên hệ Quản trị viên để được cấp link mới.</>
              )}
            </AuthBanner>
          )}

          <form className="auth-form" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
            <div className={`form-field${fieldErrors.newPassword ? ' has-error' : ''}`}>
              <label className="form-label" htmlFor="ssp-new">Mật khẩu mới</label>
              <div className="form-input-wrapper">
                <input
                  id="ssp-new"
                  className="form-input"
                  type={showNew ? 'text' : 'password'}
                  placeholder="Ít nhất 8 ký tự, 1 hoa, 1 số"
                  value={newPassword}
                  onChange={(e) => {
                    setNewPassword(e.target.value);
                    if (fieldErrors.newPassword) setFieldErrors((p) => ({ ...p, newPassword: '' }));
                    if (error) dispatch(clearError());
                  }}
                  autoComplete="new-password"
                  autoFocus
                  aria-invalid={!!fieldErrors.newPassword}
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
              {fieldErrors.newPassword && <span className="field-error">{fieldErrors.newPassword}</span>}
            </div>

            <div className={`form-field${fieldErrors.confirmPassword ? ' has-error' : ''}`}>
              <label className="form-label" htmlFor="ssp-confirm">Xác nhận mật khẩu mới</label>
              <div className="form-input-wrapper">
                <input
                  id="ssp-confirm"
                  className="form-input"
                  type={showConfirm ? 'text' : 'password'}
                  placeholder="Nhập lại mật khẩu mới"
                  value={confirmPassword}
                  onChange={(e) => {
                    setConfirmPassword(e.target.value);
                    if (fieldErrors.confirmPassword) setFieldErrors((p) => ({ ...p, confirmPassword: '' }));
                    if (error) dispatch(clearError());
                  }}
                  autoComplete="new-password"
                  aria-invalid={!!fieldErrors.confirmPassword}
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
              {fieldErrors.confirmPassword && <span className="field-error">{fieldErrors.confirmPassword}</span>}
            </div>

            <button className="btn-submit" type="submit" disabled={isLoading || !confirmOk}>
              {isLoading ? <><span className="btn-spinner" aria-hidden="true" />Đang xử lý...</> : 'KÍCH HOẠT TÀI KHOẢN'}
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}

export default StaffSetupPassword;
