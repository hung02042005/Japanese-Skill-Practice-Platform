import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import TopBar from '../home/sections/TopBar';
import FooterSection from '../home/sections/FooterSection';
import { MicIcon, QuizIcon, VocabIcon } from '@/shared/components/common/StudentIcons';
import { SakuraIcon, PhoneIcon, ScanTextIcon } from '@/shared/components/common/AppIcons';
import './Features.css';

const FEATURES = [
  {
    icon: SakuraIcon,
    tag: 'Thời Điểm Vàng',
    tagColor: 'green',
    title: 'Spaced Repetition thông minh',
    desc: 'Thuật toán tự động tính khoảng cách ôn tập tối ưu. Saku-chan nhắc đúng lúc bạn sắp quên — nhớ lâu hơn, học ít hơn.',
  },
  {
    icon: PhoneIcon,
    tag: 'Flashcard',
    tagColor: 'pink',
    title: 'Flashcard tương tác',
    desc: 'Lật thẻ, tự đánh giá, học mọi lúc mọi nơi ngay trên trình duyệt. Tiến độ của bạn được lưu tự động theo từng từ.',
  },
  {
    icon: ScanTextIcon,
    tag: 'AI · OCR',
    tagColor: 'blue',
    title: 'Nhận diện Kanji bằng AI',
    desc: 'Chụp ảnh Kanji viết tay, AI phân tích và trả về điểm độ chính xác. Luyện viết thực hành hiệu quả hơn bao giờ hết.',
  },
  {
    icon: MicIcon,
    tag: 'AI · Speech',
    tagColor: 'purple',
    title: 'Luyện phát âm Shadowing',
    desc: 'Ghi âm câu nói, AI so sánh với phát âm chuẩn và chấm điểm tức thì. Nói tiếng Nhật tự nhiên như người bản ngữ.',
  },
  {
    icon: QuizIcon,
    tag: 'Mock Exam',
    tagColor: 'orange',
    title: 'Thi thử JLPT N5 → N1',
    desc: 'Đề thi mô phỏng cấu trúc chính thức. Chấm điểm tự động, phân tích điểm yếu theo từng kỹ năng và chủ đề.',
  },
  {
    icon: VocabIcon,
    tag: 'Từ điển',
    tagColor: 'teal',
    title: 'Từ điển thông minh',
    desc: 'Tra từ vựng và Kanji theo cấp độ JLPT. Lưu từ yêu thích vào bộ ôn luyện cá nhân chỉ với một click.',
  },
];

const HOW_IT_WORKS = [
  {
    step: '01',
    title: 'Chọn cấp độ JLPT',
    desc: 'Từ N5 cho người mới đến N1 cho chuyên gia. SakuJi cá nhân hóa lộ trình học dựa trên cấp độ hiện tại của bạn.',
  },
  {
    step: '02',
    title: 'Học theo Thời Điểm Vàng',
    desc: 'Mỗi ngày Saku-chan nhắc bạn ôn tập đúng lúc. 15–20 phút mỗi ngày là đủ để tiến bộ rõ rệt sau 30 ngày.',
  },
  {
    step: '03',
    title: 'Thi thử và cải thiện',
    desc: 'Đề thi mock chỉ ra chính xác điểm yếu của bạn. Biết cần ôn gì, không lãng phí thời gian vào thứ đã biết.',
  },
];

function Features() {
  const navigate = useNavigate();

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

  return (
    <div className="features-root">
      <TopBar />
      <main>
        <section className="features-hero">
          <div className="features-hero-inner">
            <span className="features-hero-badge">✦ Tính năng</span>
            <h1 className="features-hero-title">
              Mọi công cụ bạn cần để
              <br />
              <span className="features-hero-accent">chinh phục tiếng Nhật</span>
            </h1>
            <p className="features-hero-sub">
              Từ Kanji đến hội thoại — SakuJi hỗ trợ toàn bộ lộ trình JLPT N5 → N1
              với AI và khoa học ghi nhớ hiện đại.
            </p>
            <div className="features-hero-actions">
              <button
                className="features-hero-btn-start"
                onClick={() => navigate('/register')}
                aria-label="Đăng ký học miễn phí"
              >
                Bắt đầu miễn phí →
              </button>
              <button
                className="features-hero-btn-pricing"
                onClick={() => navigate('/bang-gia')}
              >
                Xem bảng giá
              </button>
            </div>
          </div>
          <div className="features-hero-stats" aria-label="Thống kê nền tảng">
            <div className="features-hero-stat">
              <span className="features-hero-stat-num">50K+</span>
              <span className="features-hero-stat-label">Học viên</span>
            </div>
            <div className="features-hero-divider" aria-hidden="true" />
            <div className="features-hero-stat">
              <span className="features-hero-stat-num">1.000</span>
              <span className="features-hero-stat-label">Kanji</span>
            </div>
            <div className="features-hero-divider" aria-hidden="true" />
            <div className="features-hero-stat">
              <span className="features-hero-stat-num">4.9 ★</span>
              <span className="features-hero-stat-label">Đánh giá</span>
            </div>
          </div>
        </section>

        <section className="features-grid-section">
          <div className="features-section-header reveal">
            <h2 className="features-section-title">Tất cả tính năng</h2>
            <p className="features-section-sub">
              6 công cụ học tiếng Nhật được xây dựng đặc biệt cho người Việt muốn đạt JLPT
            </p>
          </div>
          <div className="features-grid">
            {FEATURES.map((feat, i) => (
              <article
                key={feat.title}
                className="features-card reveal"
                style={{ transitionDelay: `${i * 0.07}s` }}
              >
                <div className={`features-card-icon features-card-icon--${feat.tagColor}`}>
                  <feat.icon size={30} />
                </div>
                <span className={`features-card-tag features-card-tag--${feat.tagColor}`}>
                  {feat.tag}
                </span>
                <h3 className="features-card-title">{feat.title}</h3>
                <p className="features-card-desc">{feat.desc}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="features-how-section">
          <div className="features-how-inner">
            <div className="features-section-header reveal">
              <h2 className="features-section-title">Cách SakuJi hoạt động</h2>
              <p className="features-section-sub">
                3 bước đơn giản để bắt đầu hành trình tiếng Nhật của bạn
              </p>
            </div>
            <div className="features-how-steps">
              {HOW_IT_WORKS.map((item, i) => (
                <div
                  key={item.step}
                  className="features-how-step reveal"
                  style={{ transitionDelay: `${i * 0.1}s` }}
                >
                  <div className="features-how-step-num" aria-hidden="true">{item.step}</div>
                  <div>
                    <h3 className="features-how-step-title">{item.title}</h3>
                    <p className="features-how-step-desc">{item.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        <section className="features-cta">
          <div className="features-cta-inner reveal">
            <div className="features-cta-mascot" aria-hidden="true">
              <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
                <circle cx="40" cy="46" r="26" fill="#FFF0F3" stroke="#F4A7B3" strokeWidth="1.5"/>
                <ellipse cx="28" cy="19" rx="10" ry="16" fill="#E8637A" transform="rotate(-22 28 19)"/>
                <ellipse cx="40" cy="15" rx="10" ry="17" fill="#E8637A"/>
                <ellipse cx="52" cy="19" rx="10" ry="16" fill="#E8637A" transform="rotate(22 52 19)"/>
                <circle cx="40" cy="27" r="6" fill="#F4A7B3"/>
                <circle cx="33" cy="45" r="2.5" fill="#2D2D2D"/>
                <circle cx="47" cy="45" r="2.5" fill="#2D2D2D"/>
                <path d="M34 53 Q40 58 46 53" stroke="#2D2D2D" strokeWidth="2" strokeLinecap="round" fill="none"/>
              </svg>
            </div>
            <h2 className="features-cta-title">Sẵn sàng bắt đầu chưa?</h2>
            <p className="features-cta-sub">
              Tạo tài khoản miễn phí và học ngay hôm nay — không cần thẻ tín dụng.
            </p>
            <div className="features-cta-actions">
              <button
                className="features-cta-btn-start"
                onClick={() => navigate('/register')}
                aria-label="Đăng ký học miễn phí"
              >
                Bắt đầu miễn phí →
              </button>
              <button
                className="features-cta-btn-secondary"
                onClick={() => navigate('/bang-gia')}
              >
                Xem bảng giá
              </button>
            </div>
          </div>
        </section>
      </main>
      <FooterSection />
    </div>
  );
}

export default Features;
