import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { resetPasswordThunk, clearError } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import AuthBanner from '../../components/auth/AuthBanner';
import EyeIcon from '../../components/auth/EyeIcon';
import { SakuraIcon } from '../../components/common/AppIcons';
import { passwordError, confirmError } from '../../utils/validation';
import './ResetPassword.css';

function ResetPassword() {
  const dispatch = useAppDispatch();
  const { status, error, errorCode } = useAppSelector((state) => state.auth);

  const [searchParams]  = useSearchParams();
  const token           = searchParams.get('token');

  const [newPassword, setNewPassword]         = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNew, setShowNew]                 = useState(false);
  const [showConfirm, setShowConfirm]         = useState(false);
  const [isDone, setIsDone]                   = useState(false);
  const [fieldErrors, setFieldErrors]         = useState({});

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
      await dispatch(resetPasswordThunk({ token, newPassword, confirmPassword })).unwrap();
      setIsDone(true);
    } catch {
      /* lỗi API đã được set vào Redux state */
    }
  }

  /* ── State 1: không có token ── */
  if (!token) {
    return (
      <div className="rp-page">
        <AuthTopBar />
        <main className="rp-main">
          <div className="auth-card rp-status-card">
            <div className="rp-status-icon" aria-hidden="true">
              <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
                <rect width="56" height="56" rx="28" fill="#FDE0EC"/>
                <path d="M20 20l16 16M36 20L20 36"
                      stroke="#E03131" strokeWidth="2.8" strokeLinecap="round"/>
              </svg>
            </div>
            <h1 className="auth-title">Link không hợp lệ</h1>
            <p className="rp-status-desc">
              Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.
              Vui lòng yêu cầu gửi lại link mới.
            </p>
            <Link to="/forgot-password" className="btn-submit rp-status-btn">
              GỬI LẠI LINK
            </Link>
          </div>
        </main>
      </div>
    );
  }

  /* ── State 3: thành công ── */
  if (isDone) {
    return (
      <div className="rp-page">
        <AuthTopBar />
        <main className="rp-main">
          <div className="auth-card rp-status-card">
            <SakuChan variant="happy" />
            <h1 className="auth-title">Đặt lại mật khẩu thành công</h1>
            <p className="rp-status-desc">
              Mật khẩu của bạn đã được cập nhật. Hãy đăng nhập để tiếp tục học!
            </p>
            <Link to="/login" className="btn-submit rp-status-btn">
              ĐĂNG NHẬP NGAY
            </Link>
          </div>
        </main>
      </div>
    );
  }

  /* ── State 2: form ── */
  return (
    <div className="rp-page">
      <AuthTopBar />

      <main className="rp-main">
        <span className="rp-petal rp-petal--1" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="rp-petal rp-petal--2" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="rp-petal rp-petal--3" aria-hidden="true"><SakuraIcon size={18} /></span>

        <div className="auth-card" role="main">
          <SakuChan />

          <h1 className="auth-title">Đặt lại mật khẩu</h1>
          <p className="auth-subtitle">Nhập mật khẩu mới cho tài khoản của bạn.</p>

          {error && (
            <AuthBanner type="error">
              {error}
              {(errorCode === 'TOKEN_EXPIRED' || errorCode === 'INVALID_TOKEN') && (
                <>{' '}<Link to="/forgot-password" className="auth-banner-link">Gửi lại link mới</Link></>
              )}
            </AuthBanner>
          )}

          <form className="auth-form" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
            {/* Mật khẩu mới */}
            <div className={`form-field${fieldErrors.newPassword ? ' has-error' : ''}`}>
              <label className="form-label" htmlFor="rp-new">Mật khẩu mới</label>
              <div className="form-input-wrapper">
                <input
                  id="rp-new"
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
              {fieldErrors.newPassword && <span className="field-error">{fieldErrors.newPassword}</span>}
            </div>

            {/* Xác nhận mật khẩu */}
            <div className={`form-field${fieldErrors.confirmPassword ? ' has-error' : ''}`}>
              <label className="form-label" htmlFor="rp-confirm">Xác nhận mật khẩu mới</label>
              <div className="form-input-wrapper">
                <input
                  id="rp-confirm"
                  className="form-input"
                  type={showConfirm ? 'text' : 'password'}
                  placeholder="Nhập lại mật khẩu mới"
                  value={confirmPassword}
                  onChange={(e) => {
                    setConfirmPassword(e.target.value);
                    if (fieldErrors.confirmPassword) setFieldErrors((p) => ({ ...p, confirmPassword: '' }));
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
                      <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2.5"
                            strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  </span>
                ) : (
                  <span className="form-match-icon form-match-icon--err" aria-label="Mật khẩu không khớp">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2.5"
                            strokeLinecap="round"/>
                    </svg>
                  </span>
                )}
              </div>
              {fieldErrors.confirmPassword && <span className="field-error">{fieldErrors.confirmPassword}</span>}
            </div>

            <button className="btn-submit" type="submit" disabled={isLoading}>
              {isLoading
                ? <><span className="btn-spinner" aria-hidden="true" />Đang xử lý...</>
                : 'ĐẶT LẠI MẬT KHẨU'
              }
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}

export default ResetPassword;
