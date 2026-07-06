import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAppDispatch } from '../../store/hooks';
import { verifyEmailThunk, resendVerificationThunk } from '../../store/slices/authSlice';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import './VerifyEmail.css';

function VerifyEmail() {
  const dispatch = useAppDispatch();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [state, setState] = useState('loading'); // 'loading' | 'success' | 'error'
  const [errorMsg, setErrorMsg] = useState('');
  const hasVerified = useRef(false);

  // BUG-08 FIX: State cho chức năng gửi lại email xác minh
  const [resendEmail, setResendEmail] = useState('');
  const [resendStatus, setResendStatus] = useState('idle'); // 'idle' | 'loading' | 'sent' | 'error'
  const [resendError, setResendError] = useState('');

  useEffect(() => {
    if (hasVerified.current) return;
    hasVerified.current = true;

    if (!token) {
      setState('error');
      setErrorMsg('Link xác minh không hợp lệ.');
      return;
    }

    dispatch(verifyEmailThunk(token))
      .unwrap()
      .then(() => setState('success'))
      .catch((msg) => {
        setState('error');
        setErrorMsg(msg);
      });
  }, [token, dispatch]);

  async function handleResend(e) {
    e.preventDefault();
    if (!resendEmail.trim()) return;
    setResendStatus('loading');
    setResendError('');
    try {
      await dispatch(resendVerificationThunk(resendEmail.trim())).unwrap();
      setResendStatus('sent');
    } catch (err) {
      setResendStatus('error');
      setResendError(typeof err === 'string' ? err : 'Gửi lại email thất bại. Vui lòng thử lại.');
    }
  }

  return (
    <div className="ve-page">
      <AuthTopBar />
      <main className="ve-main">
        {state === 'loading' && (
          <div className="auth-card ve-card">
            <div className="ve-spinner" aria-label="Đang xác minh..." />
            <p className="ve-loading-text">Đang xác minh email của bạn...</p>
          </div>
        )}

        {state === 'success' && (
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
        )}

        {state === 'error' && (
          <div className="auth-card ve-card">
            <div className="ve-error-icon" aria-hidden="true">
              <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
                <rect width="56" height="56" rx="28" fill="#FDE0EC" />
                <path d="M20 20l16 16M36 20L20 36"
                      stroke="#E03131" strokeWidth="2.8" strokeLinecap="round" />
              </svg>
            </div>
            <h1 className="auth-title">Xác minh thất bại</h1>
            <p className="ve-desc">
              {errorMsg || 'Link xác minh không hợp lệ hoặc đã hết hạn.'}
            </p>

            {/* BUG-08 FIX: Cung cấp tùy chọn gửi lại email ngay tại trang lỗi */}
            {resendStatus === 'sent' ? (
              <div className="ve-resend-success" role="status">
                <span aria-hidden="true">✅</span>{' '}
                Đã gửi lại email xác minh đến <strong>{resendEmail}</strong>. Vui lòng kiểm tra hộp thư.
              </div>
            ) : (
              <form className="ve-resend-form" onSubmit={handleResend} noValidate>
                <p className="ve-resend-label">Nhập email để nhận lại link xác minh:</p>
                <input
                  id="ve-resend-email"
                  className="form-input ve-resend-input"
                  type="email"
                  placeholder="email@example.com"
                  value={resendEmail}
                  onChange={(e) => { setResendEmail(e.target.value); setResendError(''); }}
                  autoComplete="email"
                  aria-label="Email để gửi lại link xác minh"
                  disabled={resendStatus === 'loading'}
                />
                {resendStatus === 'error' && (
                  <p className="ve-resend-error" role="alert">{resendError}</p>
                )}
                <button
                  type="submit"
                  className="btn-submit ve-btn"
                  disabled={resendStatus === 'loading' || !resendEmail.trim()}
                  id="ve-resend-btn"
                >
                  {resendStatus === 'loading'
                    ? <><span className="btn-spinner" aria-hidden="true" />Đang gửi...</>
                    : '📧 Gửi lại email xác minh'
                  }
                </button>
              </form>
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
