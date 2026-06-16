import { useNavigate } from 'react-router-dom';
import sakuChanFloat from '../assets/saku-chan-float.svg';
import sakuChanPhone from '../assets/saku-chan-phone.svg';
import spacedRepCurve from '../assets/spaced-rep-curve.svg';
import phoneMockup from '../assets/phone-mockup.svg';
import './FeatureASection.css';

const ORBIT_CARDS = [
  { kanji: '木', furigana: 'き', delay: '0s' },
  { kanji: '火', furigana: 'ひ', delay: '2s' },
  { kanji: '土', furigana: 'つち', delay: '4s' },
];

function FeatureASection() {
  const navigate = useNavigate();

  return (
    <section className="feat-a" id="features">
      <h2 className="feat-a-header reveal">
        Bạn học tiếng Nhật như thế nào?
      </h2>

      {/* Row 1: Spaced Repetition */}
      <div className="feat-a-row reveal">
        <div className="feat-a-illus">
          <div className="feat-a-orbit-wrapper" aria-hidden="true">
            <img
              src={sakuChanFloat}
              alt="Saku-chan ngồi thiền, vây quanh bởi các kanji"
              className="feat-a-mascot-float"
            />
            {ORBIT_CARDS.map((card) => (
              <div
                key={card.kanji}
                className="feat-a-orbit-card"
                style={{ animationDelay: card.delay }}
              >
                <span className="feat-a-orbit-kanji">{card.kanji}</span>
                <span className="feat-a-orbit-furi">{card.furigana}</span>
              </div>
            ))}
          </div>
          <img
            src={spacedRepCurve}
            alt="Biểu đồ đường cong ghi nhớ spaced repetition của SakuJi"
            className="feat-a-curve"
          />
        </div>

        <div className="feat-a-text">
          <span className="feat-a-tag feat-a-tag--green">Thời Điểm Vàng ✦</span>
          <h3 className="feat-a-heading feat-a-heading--green">
            Ghi nhớ Kanji và từ vựng với tính năng Thời Điểm Vàng
          </h3>
          <p className="feat-a-desc">
            Thuật toán Spaced Repetition của SakuJi tự động tính toán
            khoảng cách ôn tập tối ưu, giúp bạn nhớ lâu hơn mà học ít hơn.
            Saku-chan sẽ nhắc bạn đúng lúc bạn sắp quên.
          </p>
          <button className="feat-a-cta" onClick={() => navigate('/register')}>
            Thử ngay miễn phí →
          </button>
        </div>
      </div>

      {/* Row 2: Flashcard on phone */}
      <div className="feat-a-row feat-a-row--reversed reveal">
        <div className="feat-a-text">
          <span className="feat-a-tag feat-a-tag--pink">Flashcard thông minh 📱</span>
          <h3 className="feat-a-heading">
            Luyện Kanji mọi lúc, mọi nơi với Flashcard tương tác
          </h3>
          <p className="feat-a-desc">
            Lật thẻ, tự đánh giá, và Saku-chan sẽ ghi nhớ tiến độ của bạn.
            Hoạt động hoàn toàn trên trình duyệt — không cần tải app.
          </p>
          <button className="feat-a-cta" onClick={() => navigate('/register')}>
            Học ngay →
          </button>
        </div>

        <div className="feat-a-phone" aria-hidden="true">
          <div className="feat-a-ring" />
          <img
            src={phoneMockup}
            alt="Màn hình flashcard tiếng Nhật trên điện thoại"
            className="feat-a-phone-img"
          />
          <img
            src={sakuChanPhone}
            alt="Saku-chan nhảy vui nhìn vào điện thoại"
            className="feat-a-phone-mascot"
          />
        </div>
      </div>
    </section>
  );
}

export default FeatureASection;
