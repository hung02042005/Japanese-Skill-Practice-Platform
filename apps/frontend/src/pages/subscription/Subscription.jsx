import { useState, useEffect } from 'react';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import SubscriptionPlanCard from '../../components/student/SubscriptionPlanCard';
import { getSubscriptionPlans, getCurrentSubscription, checkoutSubscription } from '../../api/studentService';
import './Subscription.css';

const COMPARISON_FEATURES = [
  { label: 'Nội dung N5',                free: true,  vip: true },
  { label: 'Nội dung N4–N1',             free: false, vip: true },
  { label: 'Flashcard không giới hạn',   free: false, vip: true },
  { label: 'Đề thi thử N5',              free: true,  vip: true },
  { label: 'Đề thi thử N4–N1',           free: false, vip: true },
  { label: 'Luyện Kanji OCR (AI)',        free: false, vip: true },
  { label: 'Chứng chỉ hoàn thành',       free: false, vip: true },
  { label: 'Hỗ trợ ưu tiên',             free: false, vip: true },
];

export default function Subscription() {
  const [plans,      setPlans]    = useState([]);
  const [currentSub, setCurrent]  = useState(null);
  const [isLoading,  setLoading]  = useState(true);
  const [isRedirect, setRedirect] = useState(false);
  const [error,      setError]    = useState('');

  useEffect(() => {
    Promise.all([getSubscriptionPlans(), getCurrentSubscription()])
      .then(([p, s]) => { setPlans(p ?? []); setCurrent(s); })
      .catch(() => setError('Không thể tải thông tin gói. Thử lại sau.'))
      .finally(() => setLoading(false));
  }, []);

  async function handleCheckout(planId) {
    setRedirect(true);
    setError('');
    try {
      const { paymentUrl } = await checkoutSubscription(planId);
      window.location.href = paymentUrl;
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tiến hành thanh toán. Thử lại sau.');
      setRedirect(false);
    }
  }

  return (
    <div className="sub-page">
      <TopNav activeTab="" />
      <main className="sub-body">
        <div className="sub-page-header">
          <h1 className="sub-title">Nâng Cấp VIP</h1>
          <p className="sub-subtitle">Mở khoá toàn bộ nội dung N1–N5, không giới hạn.</p>
        </div>

        {currentSub && (
          <div className="sub-current-banner">
            <span className="sub-current-icon" aria-hidden="true">⭐</span>
            <span>
              Gói VIP của bạn hết hạn:{' '}
              <strong>{new Date(currentSub.expiresAt).toLocaleDateString('vi-VN')}</strong>
            </span>
            <button
              className="sub-renew-btn"
              onClick={() => handleCheckout(currentSub.planId)}
              disabled={isRedirect}
            >
              Gia hạn
            </button>
          </div>
        )}

        {error && <div className="sub-error" role="alert">{error}</div>}

        {isLoading ? (
          <div className="sub-plan-grid">
            {[1, 2, 3].map((i) => <div key={i} className="sub-skel" aria-hidden="true" />)}
          </div>
        ) : (
          <div className="sub-plan-grid">
            {plans.map((plan) => (
              <SubscriptionPlanCard
                key={plan.planId}
                plan={plan}
                onSelect={() => handleCheckout(plan.planId)}
                isLoading={isRedirect}
              />
            ))}
          </div>
        )}

        <section aria-label="So sánh FREE và VIP">
          <h2 className="sub-compare-title">FREE vs VIP</h2>
          <div className="sub-compare-table-wrap">
            <table className="sub-compare-table">
              <thead>
                <tr>
                  <th scope="col">Tính năng</th>
                  <th scope="col" className="sub-col-free">FREE</th>
                  <th scope="col" className="sub-col-vip">⭐ VIP</th>
                </tr>
              </thead>
              <tbody>
                {COMPARISON_FEATURES.map((f) => (
                  <tr key={f.label}>
                    <td>{f.label}</td>
                    <td className="sub-col-free">
                      <span aria-label={f.free ? 'Có' : 'Không có'}>{f.free ? '✅' : '❌'}</span>
                    </td>
                    <td className="sub-col-vip"><span aria-label="Có">✅</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  );
}
