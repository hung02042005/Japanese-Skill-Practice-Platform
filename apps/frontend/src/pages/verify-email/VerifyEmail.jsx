import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAppDispatch } from '../../store/hooks';
import { verifyEmailThunk, resendVerificationThunk } from '../../store/slices/authSlice';
import { useCountdown } from '../../hooks/useCountdown';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import './VerifyEmail.css';

function VerifyEmail() {
  const dispatch = useAppDispatch();
  const [searchParams] = useSearchParams();

  const [email, setEmail] = useState(searchParams.get('email') ?? '');
  const [otpCode, setOtpCode] = useState('');
  const [state, setState] = useState('idle'); // 'idle' | 'verifying' | 'success' | 'error'
  const [errorMsg, setErrorMsg] = useState('');

  const [resendStatus, setResendStatus] = useState('idle'); // idle | loading | sent | error
  const [resendMsg, setResendMsg] = useState('');
  const [cooldown, startCooldown] = useCountdown();

  async function handleVerify(e) {
    e.preventDefault();
    if (!email.trim() || !otpCode.trim()) return;
    setState('verifying');
    setErrorMsg('');
    try {
      await dispatch(verifyEmailThunk({ email: email.trim(), otpCode: otpCode.trim() })).unwrap();
      setState('success');
    } catch (err) {
      setState('error');
      setErrorMsg(typeof err === 'string' ? err : 'Mã xác minh không đúng hoặc đã hết hạn.');
    }
  }

  async function handleResend() {
    if (!email.trim() || resendStatus === 'loading' || cooldown > 0) return;
    setResendStatus('loading');
    setResendMsg('');
    try {
      await dispatch(resendVerificationThunk(email.trim())).unwrap();
      setResendStatus('sent');
      startCooldown(60);
    } catch (err) {
      setResendStatus('error');
      setResendMsg(typeof err === 'string' ? err : 'Gửi lại mã thất bại. Vui lòng thử lại.');
    }
  }

  return (
    <div className="ve-page">
      <AuthTopBar />
      <main className="ve-main">
        {state === 'success' ? (
          <div className="auth-card ve-card">
            <SakuChan variant="happy" />
            <h1 className="auth-title">Xác minh thành công!</h1>
            <p className="ve-desc">
              Email của bạn đã được xác minh. Bạn có thể đăng nhập ngay bây giờ.
            </p>
            <Link to="/login" className="btn-submit ve-btn">
              ĐĂNG NHẬP NGAY
            </Link>
          </div>
        ) : (
          <div className="auth-card ve-card">
            <SakuChan />
            <h1 className="auth-title">Nhập mã xác minh</h1>
            <p className="ve-desc">
              Chúng tôi đã gửi mã gồm 6 chữ số đến email đăng ký của bạn. Nhập mã bên dưới để kích hoạt tài khoản.
            </p>

            <form className="ve-resend-form" onSubmit={handleVerify} noValidate>
              <p className="ve-resend-label">Email</p>
              <input
                id="ve-email"
                className="form-input ve-resend-input"
                type="email"
                placeholder="email@example.com"
                value={email}
                onChange={(e) => { setEmail(e.target.value); setState('idle'); setErrorMsg(''); }}
                autoComplete="email"
                aria-label="Email"
              />

              <p className="ve-resend-label">Mã xác minh</p>
              <input
                id="ve-otp"
                className="form-input ve-resend-input ve-otp-input"
                type="text"
                inputMode="numeric"
                maxLength={6}
                placeholder="6 chữ số"
                value={otpCode}
                onChange={(e) => { setOtpCode(e.target.value.replace(/\D/g, '')); setState('idle'); setErrorMsg(''); }}
                autoComplete="one-time-code"
                aria-label="Mã xác minh"
              />

              {state === 'error' && (
                <p className="ve-resend-error" role="alert">{errorMsg}</p>
              )}

              <button
                type="submit"
                className="btn-submit ve-btn"
                disabled={state === 'verifying' || !email.trim() || !otpCode.trim()}
              >
                {state === 'verifying'
                  ? <><span className="btn-spinner" aria-hidden="true" />Đang xác minh...</>
                  : 'Xác minh'
                }
              </button>
            </form>

            {resendStatus === 'sent' ? (
              <div className="ve-resend-success" role="status">
                <span aria-hidden="true">✅</span>{' '}
                Đã gửi lại mã xác minh đến <strong>{email}</strong>.
              </div>
            ) : (
              <>
                <button
                  type="button"
                  className="ve-back-link ve-resend-btn"
                  onClick={handleResend}
                  disabled={resendStatus === 'loading' || cooldown > 0 || !email.trim()}
                >
                  {resendStatus === 'loading'
                    ? 'Đang gửi...'
                    : cooldown > 0
                      ? `Gửi lại mã (${cooldown}s)`
                      : '📧 Gửi lại mã xác minh'}
                </button>
                {resendStatus === 'error' && <p className="ve-resend-error" role="alert">{resendMsg}</p>}
              </>
            )}

            <Link to="/login" className="ve-back-link">
              ← Về trang đăng nhập
            </Link>
          </div>
        )}
      </main>
    </div>
  );
}

export default VerifyEmail;
