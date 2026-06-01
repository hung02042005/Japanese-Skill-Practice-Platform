import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import AuthTopBar from '../../components/auth/AuthTopBar';
import SakuChan from '../../components/auth/SakuChan';
import { verifyEmail } from '../../api/authService';
import '../forgot-password/ResetPassword.css';

function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [status, setStatus] = useState('loading'); // 'loading' | 'success' | 'error'
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setMessage('Link xác minh không hợp lệ. Vui lòng đăng ký lại hoặc yêu cầu gửi lại email.');
      setStatus('error');
      return;
    }

    verifyEmail(token)
      .then(() => setStatus('success'))
      .catch((err) => {
        setMessage(err.response?.data?.message ?? 'Xác minh thất bại. Link có thể đã hết hạn.');
        setStatus('error');
      });
  }, [token]);

  return (
    <div className="rp-page">
      <AuthTopBar />
      <main className="rp-main">
        <div className="auth-card rp-status-card">

          {status === 'loading' && (
            <>
              <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
                <span className="btn-spinner" style={{ width: 40, height: 40 }} aria-label="Đang xử lý" />
              </div>
              <h1 className="auth-title">Đang xác minh email...</h1>
              <p className="rp-status-desc">Vui lòng đợi trong giây lát.</p>
            </>
          )}

          {status === 'success' && (
            <>
              <SakuChan variant="happy" />
              <h1 className="auth-title">Xác minh thành công!</h1>
              <p className="rp-status-desc">
                Tài khoản của bạn đã được kích hoạt. Hãy đăng nhập để bắt đầu hành trình học tiếng Nhật!
              </p>
              <Link to="/login" className="btn-submit rp-status-btn">
                ĐĂNG NHẬP NGAY
              </Link>
            </>
          )}

          {status === 'error' && (
            <>
              <div className="rp-status-icon" aria-hidden="true">
                <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
                  <rect width="56" height="56" rx="28" fill="#FDE0EC"/>
                  <path d="M20 20l16 16M36 20L20 36"
                        stroke="#E03131" strokeWidth="2.8" strokeLinecap="round"/>
                </svg>
              </div>
              <h1 className="auth-title">Xác minh thất bại</h1>
              <p className="rp-status-desc">{message}</p>
              <Link to="/register" className="btn-submit rp-status-btn">
                ĐĂNG KÝ LẠI
              </Link>
            </>
          )}

        </div>
      </main>
    </div>
  );
}

export default VerifyEmail;
