import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { staffForgotPasswordThunk, clearError } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import AuthBanner from '../../components/auth/AuthBanner';
import { SakuraIcon } from '../../components/common/AppIcons';
import { emailError } from '../../utils/validation';
import './ForgotPassword.css';

function StaffForgotPassword() {
  const dispatch = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);
  const [searchParams] = useSearchParams();

  const [email, setEmail] = useState(searchParams.get('email') ?? '');
  const [isSent, setIsSent] = useState(false);
  const [emailErr, setEmailErr] = useState('');
  const isLoading = status === 'loading';

  async function handleSubmit(e) {
    e.preventDefault();

    const emErr = emailError(email);
    setEmailErr(emErr);
    if (emErr) return;

    try {
      await dispatch(staffForgotPasswordThunk({ email })).unwrap();
      setIsSent(true);
    } catch {
      /* lỗi API đã được set vào Redux state */
    }
  }

  if (isSent) {
    return (
      <div className="fp-page">
        <AuthTopBar />
        <main className="fp-main">
          <div className="auth-card fp-success-card">
            <SakuChan variant="happy" />
            <h1 className="auth-title">Yêu cầu đã gửi đến quản trị viên</h1>
            <p className="fp-success-desc">
              Nếu email <strong className="fp-success-email">{email}</strong> là tài khoản nhân viên đang hoạt động,
              quản trị viên sẽ xác minh và gửi mật khẩu tạm thời qua email.
            </p>
            <p className="fp-success-hint">
              Chưa nhận được phản hồi?{' '}
              <button
                className="fp-link-btn"
                onClick={() => {
                  setIsSent(false);
                  dispatch(clearError());
                }}
                type="button"
              >
                Gửi lại yêu cầu
              </button>
            </p>
            <Link to="/login" className="fp-back-link">Quay lại đăng nhập</Link>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="fp-page">
      <AuthTopBar />
      <main className="fp-main">
        <span className="fp-petal fp-petal--1" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="fp-petal fp-petal--2" aria-hidden="true"><SakuraIcon size={18} /></span>
        <span className="fp-petal fp-petal--3" aria-hidden="true"><SakuraIcon size={18} /></span>

        <div className="auth-card" role="main">
          <SakuChan />
          <h1 className="auth-title">Khôi phục mật khẩu nhân viên</h1>
          <p className="auth-subtitle">
            Xác nhận email nhân viên để gửi yêu cầu cấp mật khẩu tạm thời đến quản trị viên.
          </p>

          {error && <AuthBanner type="error">{error}</AuthBanner>}

          <form className="auth-form" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
            <div className={`form-field${emailErr ? ' has-error' : ''}`}>
              <label className="form-label" htmlFor="staff-fp-email">Email nhân viên</label>
              <input
                id="staff-fp-email"
                className="form-input"
                type="email"
                placeholder="staff@jlpt.com"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (emailErr) setEmailErr('');
                  if (error) dispatch(clearError());
                }}
                autoComplete="email"
                autoFocus
                aria-invalid={!!emailErr}
              />
              {emailErr && <span className="field-error">{emailErr}</span>}
            </div>

            <button className="btn-submit" type="submit" disabled={isLoading}>
              {isLoading ? <><span className="btn-spinner" aria-hidden="true" />Đang gửi...</> : 'GỬI YÊU CẦU ĐẾN ADMIN'}
            </button>
          </form>

          <Link to="/forgot-password" className="fp-back-link">Dùng khôi phục tài khoản học viên</Link>
        </div>
      </main>
    </div>
  );
}

export default StaffForgotPassword;
