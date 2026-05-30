import { useNavigate } from 'react-router-dom';
import sakuChanHero from '../assets/saku-chan-hero.svg';
import './HeroSection.css';

const KANJI_CARDS = [
  { kanji: '山', furigana: 'やま', meaning: 'Núi',      style: { top: '8%',  left: '4%' },  delay: '0s' },
  { kanji: '水', furigana: 'みず', meaning: 'Nước',     style: { top: '4%',  right: '4%' }, delay: '0.5s' },
  { kanji: '花', furigana: 'はな', meaning: 'Hoa',      style: { bottom: '28%', left: '0%' }, delay: '1s' },
  { kanji: '空', furigana: 'そら', meaning: 'Bầu trời', style: { top: '44%', right: '1%' }, delay: '1.5s' },
  { kanji: '日', furigana: 'にち', meaning: 'Mặt trời', style: { bottom: '10%', right: '6%' }, delay: '2s' },
];

const PETAL_POSITIONS = [
  { top: '12%',  left: '10%',  size: 28, delay: '0s',    duration: '6s' },
  { top: '18%',  right: '18%', size: 22, delay: '1s',    duration: '7s' },
  { bottom: '22%', left: '12%', size: 32, delay: '2s',   duration: '5.5s' },
  { top: '55%',  right: '14%', size: 20, delay: '0.5s',  duration: '6.5s' },
  { top: '30%',  left: '18%',  size: 18, delay: '1.5s',  duration: '7.5s' },
  { bottom: '35%', right: '20%', size: 26, delay: '2.5s', duration: '6s' },
];

function HeroSection() {
  const navigate = useNavigate();

  return (
    <section className="hero">
      <div className="hero-grid">
        <div className="hero-content">
          <span className="hero-tag">🌸 Học tiếng Nhật theo cách của bạn</span>

          <h1 className="hero-headline">
            Memorize 1000 Kanji<br />
            in <span className="hero-headline-accent">1 month</span>
          </h1>

          <p className="hero-sub">
            Phương pháp Thời Điểm Vàng giúp bạn nhớ lâu hơn,
            học ít hơn, và đạt JLPT N1 nhanh hơn bao giờ hết.
          </p>

          <div className="hero-cta">
            <button
              className="hero-btn-start"
              onClick={() => navigate('/register')}
              aria-label="Đăng ký học miễn phí"
            >
              Get started →
            </button>
            <button className="hero-btn-demo">
              Xem thử demo
            </button>
          </div>

          <div className="hero-stats" aria-label="Thống kê nền tảng">
            <div className="hero-stat">
              <span className="hero-stat-number">50K+</span>
              <span className="hero-stat-label">Học viên đang học</span>
            </div>
            <div className="hero-stat">
              <span className="hero-stat-number">1.000</span>
              <span className="hero-stat-label">Kanji được luyện tập</span>
            </div>
            <div className="hero-stat">
              <span className="hero-stat-number">4.9 ★</span>
              <span className="hero-stat-label">Đánh giá trung bình</span>
            </div>
          </div>
        </div>

        <div className="hero-illustration" aria-hidden="true">
          <div className="hero-blob" />

          <img
            src={sakuChanHero}
            alt="Saku-chan mascot cầm bút lông, sẵn sàng học"
            className="hero-mascot"
          />

          <div className="hero-kanji-cards">
            {KANJI_CARDS.map((card) => (
              <div
                key={card.kanji}
                className="hero-kanji-card"
                style={{ ...card.style, animationDelay: card.delay }}
              >
                <span className="hero-kanji-card-kanji">{card.kanji}</span>
                <span className="hero-kanji-card-furigana">{card.furigana}</span>
                <span className="hero-kanji-card-meaning">{card.meaning}</span>
              </div>
            ))}
          </div>

          <div className="hero-petals">
            {PETAL_POSITIONS.map((p, i) => (
              <svg
                key={i}
                className="hero-petal"
                width={p.size}
                height={p.size}
                viewBox="0 0 32 32"
                style={{
                  top: p.top,
                  left: p.left,
                  right: p.right,
                  bottom: p.bottom,
                  animationDelay: p.delay,
                  animationDuration: p.duration,
                }}
              >
                <ellipse cx="16" cy="16" rx="10" ry="15" fill="#F4A7B3" opacity="0.18" transform="rotate(-20 16 16)"/>
              </svg>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

export default HeroSection;
