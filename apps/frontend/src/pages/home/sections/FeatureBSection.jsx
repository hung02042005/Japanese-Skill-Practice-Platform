import featVocab from '../assets/feat-vocab.svg';
import featGlobe from '../assets/feat-globe.svg';
import featDict from '../assets/feat-dictionary.svg';
import './FeatureBSection.css';

const CARDS = [
  {
    id: 'vocab',
    modifier: 'feat-b-card--vocab',
    img: featVocab,
    imgAlt: 'Các bong bóng hội thoại tiếng Nhật — こんにちは, ありがとう, おいしい！',
    title: 'Từ vựng & Hội thoại',
    desc: 'Học từ vựng theo chủ đề từ N5 đến N1, luyện câu hội thoại thực tế với phát âm chuẩn của người bản ngữ.',
  },
  {
    id: 'globe',
    modifier: 'feat-b-card--globe',
    img: featGlobe,
    imgAlt: 'Địa cầu với các điểm kết nối Nhật Bản, Việt Nam và các nước trên thế giới',
    title: 'Giao tiếp & Kết nối',
    desc: 'Luyện nói, nghe hiểu với người học từ khắp nơi trên thế giới. Thực hành hội thoại trong môi trường an toàn, thân thiện.',
  },
  {
    id: 'dict',
    modifier: 'feat-b-card--dict',
    img: featDict,
    imgAlt: 'Từ điển mở với kính lúp phóng to chữ 桜',
    title: 'Từ điển tiếng Nhật',
    desc: 'Từ điển tích hợp hơn 100.000 từ, tra cứu tức thì, có audio phát âm, ví dụ câu và lưu từ yêu thích chỉ với 1 click.',
  },
];

function FeatureBSection() {
  return (
    <section className="feat-b">
      <div className="feat-b-header reveal">
        <h2 className="feat-b-title">Ngoài Kanji, bạn sẽ được học</h2>
        <p className="feat-b-subtitle">
          SakuJi không chỉ là luyện Kanji — đây là hành trình tiếng Nhật toàn diện.
        </p>
      </div>

      <div className="feat-b-grid">
        {CARDS.map((card) => (
          <article key={card.id} className={`feat-b-card ${card.modifier} reveal`}>
            <img
              src={card.img}
              alt={card.imgAlt}
              className="feat-b-card-img"
              loading="lazy"
            />
            <h3 className="feat-b-card-title">{card.title}</h3>
            <p className="feat-b-card-desc">{card.desc}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

export default FeatureBSection;
