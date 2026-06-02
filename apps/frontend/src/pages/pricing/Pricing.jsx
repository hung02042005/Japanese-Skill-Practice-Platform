import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '../home/sections/TopBar';
import FooterSection from '../home/sections/FooterSection';
import './Pricing.css';

const PLANS = [
  {
    name: 'Miễn phí',
    price: '0',
    period: 'mãi mãi',
    tag: null,
    color: 'gray',
    features: [
      { label: 'Flashcard 100 thẻ đầu tiên', included: true },
      { label: 'Kanji N5 cơ bản', included: true },
      { label: 'Từ điển tra cứu', included: true },
      { label: '3 bài thi thử mỗi tháng', included: true },
      { label: 'Thời Điểm Vàng nâng cao', included: false },
      { label: 'OCR nhận diện Kanji AI', included: false },
      { label: 'Luyện phát âm Shadowing', included: false },
      { label: 'Đề thi N4 → N1 đầy đủ', included: false },
    ],
    cta: 'Bắt đầu miễn phí',
    ctaRoute: '/register',
  },
  {
    name: 'VIP',
    price: '99.000',
    period: '/ tháng',
    tag: 'Phổ biến nhất ⭐',
    color: 'pink',
    features: [
      { label: 'Toàn bộ tính năng Free', included: true },
      { label: 'Thời Điểm Vàng nâng cao', included: true },
      { label: 'OCR nhận diện Kanji AI', included: true },
      { label: 'Luyện phát âm Shadowing', included: true },
      { label: 'Đề thi N5 → N1 đầy đủ', included: true },
      { label: 'Flashcard & Từ điển không giới hạn', included: true },
      { label: 'Phân tích tiến độ chi tiết', included: true },
      { label: 'Hỗ trợ ưu tiên 24/7', included: true },
    ],
    cta: 'Nâng cấp VIP ngay',
    ctaRoute: '/register',
  },
];

const FAQS = [
  {
    q: 'Gói miễn phí có hết hạn không?',
    a: 'Không! Gói miễn phí tồn tại vĩnh viễn. Bạn có thể học Kanji N5 và Flashcard mà không cần trả bất kỳ khoản phí nào.',
  },
  {
    q: 'Tôi có thể hủy đăng ký VIP bất kỳ lúc nào không?',
    a: 'Có. Bạn có thể hủy bất kỳ lúc nào trong trang cài đặt tài khoản. Quyền VIP giữ nguyên đến hết kỳ đã thanh toán.',
  },
  {
    q: 'Phương thức thanh toán nào được hỗ trợ?',
    a: 'Chúng tôi hỗ trợ chuyển khoản ngân hàng, ví MoMo, ZaloPay và thẻ tín dụng/ghi nợ quốc tế (Visa, MasterCard).',
  },
  {
    q: 'Có ưu đãi cho học sinh/sinh viên không?',
    a: 'Có! Học sinh sinh viên được giảm 30% khi đăng ký VIP năm. Gửi email kèm ảnh thẻ sinh viên về support@sakuji.vn để nhận mã.',
  },
  {
    q: 'VIP có bao gồm tất cả cấp độ JLPT không?',
    a: 'Đúng vậy! VIP mở khóa toàn bộ nội dung từ N5 đến N1, bao gồm đề thi thử, flashcard, từ điển và các tính năng AI.',
  },
];

function Pricing() {
  const navigate = useNavigate();
  const [openFaq, setOpenFaq] = useState(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 }
    );
    document.querySelectorAll('.reveal').forEach((el) => observer.observe(el));
    return () => observer.disconnect();
  }, []);

  const toggleFaq = (i) => setOpenFaq(openFaq === i ? null : i);

  return (
    <div className="pricing-root">
      <TopBar />
      <main>
        <section className="pricing-hero">
          <div className="pricing-hero-inner">
            <span className="pricing-hero-badge">💎 Bảng giá</span>
            <h1 className="pricing-hero-title">
              Chọn gói phù hợp
              <br />
              <span className="pricing-hero-accent">với bạn</span>
            </h1>
            <p className="pricing-hero-sub">
              Bắt đầu miễn phí, nâng cấp khi bạn cần thêm sức mạnh.
              Không có phí ẩn, không ràng buộc dài hạn.
            </p>
          </div>
        </section>

        <section className="pricing-cards-section">
          <div className="pricing-cards">
            {PLANS.map((plan) => (
              <div
                key={plan.name}
                className={`pricing-card pricing-card--${plan.color}`}
                aria-label={`Gói ${plan.name}`}
              >
                {plan.tag && (
                  <span className="pricing-card-popular">{plan.tag}</span>
                )}
                <div className="pricing-card-header">
                  <h2 className="pricing-card-name">{plan.name}</h2>
                  <div className="pricing-card-price-wrap">
                    <span className="pricing-card-price">
                      {plan.price === '0' ? 'Miễn phí' : `${plan.price}đ`}
                    </span>
                    <span className="pricing-card-period">{plan.period}</span>
                  </div>
                </div>

                <ul
                  className="pricing-card-features"
                  aria-label={`Tính năng gói ${plan.name}`}
                >
                  {plan.features.map((feat) => (
                    <li
                      key={feat.label}
                      className={`pricing-card-feat ${
                        feat.included
                          ? 'pricing-card-feat--yes'
                          : 'pricing-card-feat--no'
                      }`}
                    >
                      <span className="pricing-card-feat-icon" aria-hidden="true">
                        {feat.included ? '✓' : '✗'}
                      </span>
                      {feat.label}
                    </li>
                  ))}
                </ul>

                <button
                  className={`pricing-card-cta pricing-card-cta--${plan.color}`}
                  onClick={() => navigate(plan.ctaRoute)}
                >
                  {plan.cta}
                </button>
              </div>
            ))}
          </div>
        </section>

        <section className="pricing-guarantee">
          <div className="pricing-guarantee-inner">
            <span className="pricing-guarantee-icon" aria-hidden="true">🛡️</span>
            <div>
              <h3 className="pricing-guarantee-title">Bảo đảm hoàn tiền 7 ngày</h3>
              <p className="pricing-guarantee-desc">
                Không hài lòng? Chúng tôi hoàn tiền 100% trong 7 ngày đầu tiên — không
                cần giải thích lý do.
              </p>
            </div>
          </div>
        </section>

        <section className="pricing-faq">
          <div className="pricing-faq-inner">
            <h2 className="pricing-faq-title reveal">Câu hỏi thường gặp</h2>
            <div className="pricing-faq-list">
              {FAQS.map((faq, i) => (
                <div key={i} className="pricing-faq-item reveal">
                  <button
                    className="pricing-faq-question"
                    onClick={() => toggleFaq(i)}
                    aria-expanded={openFaq === i}
                  >
                    <span>{faq.q}</span>
                    <span
                      className={`pricing-faq-chevron ${
                        openFaq === i ? 'pricing-faq-chevron--open' : ''
                      }`}
                      aria-hidden="true"
                    >
                      ▾
                    </span>
                  </button>
                  {openFaq === i && (
                    <p className="pricing-faq-answer">{faq.a}</p>
                  )}
                </div>
              ))}
            </div>
          </div>
        </section>

        <section className="pricing-cta">
          <div className="pricing-cta-inner reveal">
            <h2 className="pricing-cta-title">Bắt đầu hành trình của bạn 🌸</h2>
            <p className="pricing-cta-sub">
              Hơn 50.000 học viên đang học tiếng Nhật cùng SakuJi mỗi ngày.
            </p>
            <div className="pricing-cta-actions">
              <button
                className="pricing-cta-btn-free"
                onClick={() => navigate('/register')}
              >
                Bắt đầu miễn phí
              </button>
              <button
                className="pricing-cta-btn-vip"
                onClick={() => navigate('/register')}
              >
                Nâng cấp VIP →
              </button>
            </div>
          </div>
        </section>
      </main>
      <FooterSection />
    </div>
  );
}

export default Pricing;
