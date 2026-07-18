import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { checkAccountTypeThunk, forgotPasswordThunk, clearError } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import AuthBanner from '../../components/auth/AuthBanner';
import { SakuraIcon } from '../../components/common/AppIcons';
import { emailError } from '../../utils/validation';
import './ForgotPassword.css';

function ForgotPassword() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);

  const [email, setEmail] = useState('');
  const [isSent, setIsSent] = useState(false);
  const [emailErr, setEmailErr] = useState('');

  const isLoading = status === 'loading';

  async function handleSubmit(e) {
    e.preventDefault();

    const emErr = emailError(email);
    setEmailErr(emErr);
    if (emErr) return;

    try {
      const account = await dispatch(checkAccountTypeThunk({ email })).unwrap();
      if (account.accountType === 'staff') {
        navigate(`/staff/forgot-password?email=${encodeURIComponent(email.trim())}`);
        return;
      }
      await dispatch(forgotPasswordThunk({ email })).unwrap();
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
            <h1 className="auth-title">Kiểm tra email của bạn</h1>
            <p className="fp-success-desc">
              Nếu email{' '}
              <strong className="fp-success-email">{email}</strong>{' '}
              tồn tại trong hệ thống, bạn sẽ nhận được link đặt lại mật khẩu trong vài phút.
            </p>
            <p className="fp-success-hint">
              Không nhận được email?{' '}
              <button
                className="fp-link-btn"
                onClick={() => { setIsSent(false); dispatch(clearError()); }}
                aria-label="Gửi lại email đặt lại mật khẩu"
              >
                Gửi lại
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

          <h1 className="auth-title">Quên mật khẩu?</h1>
          <p className="auth-subtitle">
            Nhập email đã đăng ký, chúng tôi sẽ gửi link đặt lại mật khẩu cho bạn.
          </p>

          {error && <AuthBanner type="error">{error}</AuthBanner>}

          <form className="auth-form" onSubmit={handleSubmit} noValidate aria-busy={isLoading}>
            <div className={`form-field${emailErr ? ' has-error' : ''}`}>
              <label className="form-label" htmlFor="fp-email">Email</label>
              <input
                id="fp-email"
                className="form-input"
                type="email"
                placeholder="example@email.com"
                value={email}
                onChange={(e) => { setEmail(e.target.value); if (emailErr) setEmailErr(''); if (error) dispatch(clearError()); }}
                autoComplete="email"
                autoFocus
                aria-invalid={!!emailErr}
              />
              {emailErr && <span className="field-error">{emailErr}</span>}
            </div>

            <button className="btn-submit" type="submit" disabled={isLoading}>
              {isLoading
                ? <><span className="btn-spinner" aria-hidden="true" />Đang gửi...</>
                : 'GỬI LINK ĐẶT LẠI MẬT KHẨU'
              }
            </button>
          </form>

          <Link to="/login" className="fp-back-link">Quay lại đăng nhập</Link>
        </div>
      </main>
    </div>
  );
}

export default ForgotPassword;
