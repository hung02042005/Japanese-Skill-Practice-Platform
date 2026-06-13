export default function SubscriptionPlanCard({ plan, onSelect, isLoading }) {
  const isFeatured = plan.badge === 'PHỔ BIẾN';

  return (
    <div className={`spc-card${isFeatured ? ' spc-card--featured' : ''}`}>
      {plan.badge && <div className="spc-badge">{plan.badge}</div>}
      <h3 className="spc-name">{plan.name}</h3>
      <div className="spc-price">
        <span className="spc-price-num">{plan.price.toLocaleString('vi-VN')}đ</span>
      </div>
      {plan.pricePerMonth !== plan.price && (
        <p className="spc-per-month">{plan.pricePerMonth.toLocaleString('vi-VN')}đ / tháng</p>
      )}
      {plan.savingPercent && (
        <p className="spc-saving">Tiết kiệm {plan.savingPercent}%</p>
      )}
      <button
        className={`spc-btn${isFeatured ? ' spc-btn--featured' : ''}`}
        onClick={onSelect}
        disabled={isLoading}
        aria-label={`Chọn gói ${plan.name}`}
      >
        {isLoading ? 'Đang xử lý...' : 'Chọn gói này'}
      </button>
    </div>
  );
}
