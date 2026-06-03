import { useNavigate } from 'react-router-dom';
import SakuChan from '../auth/SakuChan';
import { JlptBadge } from '../common/Badges';
import { ProgressBar } from '../common/ProgressBar';
import './HeroBanner.css';

function HeroBanner({ course }) {
  const navigate = useNavigate();

  if (!course) return null;

  const { title, jlptLevel, description, completedLessons, totalLessons } = course;

  return (
    <div className="hero-banner">
      <span className="hero-petal hero-petal--1" aria-hidden="true">🌸</span>
      <span className="hero-petal hero-petal--2" aria-hidden="true">🌸</span>
      <span className="hero-petal hero-petal--3" aria-hidden="true">🌸</span>

      <div className="hero-content">
        <JlptBadge level={jlptLevel} />
        <h1 className="hero-title">{title}</h1>
        <p className="hero-desc">{description}</p>

        <div
          className="hero-progress"
          aria-label={`Đã hoàn thành ${completedLessons} trên ${totalLessons} bài`}
        >
          <ProgressBar value={completedLessons} max={totalLessons} height={8} />
          <span className="hero-progress-label">{completedLessons} / {totalLessons} bài</span>
        </div>

        <button className="hero-cta" type="button" onClick={() => navigate('/learn')}>
          HỌC TỪ MỚI
        </button>
      </div>

      <div className="hero-mascot" aria-hidden="true">
        <SakuChan variant="idle" size={120} />
      </div>
    </div>
  );
}

export default HeroBanner;
