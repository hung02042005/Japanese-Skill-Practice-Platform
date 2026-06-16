import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAppDispatch } from '../../store/hooks';
import { verifyEmailThunk } from '../../store/slices/authSlice';
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
            <Link to="/login" className="btn-submit ve-btn">
              Về trang đăng nhập
            </Link>
          </div>
        )}
      </main>
    </div>
  );
}

export default VerifyEmail;
