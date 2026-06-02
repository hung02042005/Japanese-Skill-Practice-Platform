import { useState, useEffect, useRef } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import AppLogo from '../../components/common/AppLogo';
import SakuChan from '../../components/auth/SakuChan';
import { verifySubscription } from '../../api/studentService';
import './Subscription.css';

export default function SubscriptionSuccess() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const orderId  = searchParams.get('orderId');

  const [state,     setState]  = useState('verifying');
  const [orderInfo, setOrder]  = useState(null);
  const retriesRef = useRef(0);

  useEffect(() => {
    if (!orderId) { navigate('/subscription'); return; }

    async function tryVerify() {
      try {
        const result = await verifySubscription(orderId);
        if (result.status === 'ACTIVE') {
          setState('success');
          setOrder(result);
        } else if (retriesRef.current < 3) {
          retriesRef.current += 1;
          setTimeout(tryVerify, 3000);
        } else {
          setState('failed');
        }
      } catch {
        if (retriesRef.current < 3) {
          retriesRef.current += 1;
          setTimeout(tryVerify, 3000);
        } else {
          setState('failed');
        }
      }
    }

    tryVerify();
  }, [orderId, navigate]);

  return (
    <div className="sus-page">
      <div className="sus-topbar">
        <AppLogo size={28} />
        <span className="sus-brand">SakuJi</span>
      </div>

      <div className="sus-card">
        {state === 'verifying' && (
          <div className="sus-verifying" role="status">
            <div className="sus-spinner-lg" />
            <h1 className="sus-title">Đang xác nhận thanh toán của bạn...</h1>
            <p className="sus-subtitle">Vui lòng không đóng trang này.</p>
          </div>
        )}

        {state === 'success' && orderInfo && (
          <div className="sus-success">
            <SakuChan variant="celebrate" size={180} />
            <h1 className="sus-title">⭐ Chúc mừng! Tài khoản VIP đã được kích hoạt!</h1>
            <p className="sus-subtitle">Bạn hiện có thể truy cập toàn bộ nội dung N1–N5.</p>
            <div className="sus-order-info">
              <div className="sus-info-row">
                <span>Gói:</span>
                <strong>{orderInfo.planName}</strong>
              </div>
              <div className="sus-info-row">
                <span>Hết hạn:</span>
                <strong>{new Date(orderInfo.expiresAt).toLocaleDateString('vi-VN')}</strong>
              </div>
            </div>
            <button className="sus-cta-btn" onClick={() => navigate('/dashboard')}>
              Bắt đầu học ngay →
            </button>
          </div>
        )}

        {state === 'failed' && (
          <div className="sus-pending">
            <SakuChan variant="thinking" size={160} />
            <h1 className="sus-title">Đang xử lý thanh toán của bạn...</h1>
            <p className="sus-subtitle">Có thể mất vài phút để kích hoạt gói VIP.</p>
            {orderId && (
              <p className="sus-order-id">
                Mã đơn hàng: <code>{orderId}</code>
              </p>
            )}
            <p className="sus-support-note">
              Vui lòng liên hệ hỗ trợ nếu không nhận được sau 10 phút.
            </p>
            <div className="sus-actions">
              <a href="mailto:support@sakuji.vn" className="sus-support-btn">
                Liên hệ hỗ trợ
              </a>
              <Link to="/dashboard" className="sus-ghost-btn">Về Dashboard</Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
