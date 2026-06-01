import { useNavigate } from 'react-router-dom';
import SakuChan from '../../components/auth/SakuChan';

const JLPT_CLASS = { N5: 'jlpt-n5', N4: 'jlpt-n4', N3: 'jlpt-n3', N2: 'jlpt-n2', N1: 'jlpt-n1' };

function HeroBanner({ course }) {
  const navigate = useNavigate();

  if (!course) return null;

  const { title, jlptLevel, description, completedLessons, totalLessons } = course;
  const pct = totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0;

  return (
    <div className="hero-banner">
      {/* Petal decorations */}
      <span className="hero-petal hero-petal--1" aria-hidden="true">🌸</span>
      <span className="hero-petal hero-petal--2" aria-hidden="true">🌸</span>
      <span className="hero-petal hero-petal--3" aria-hidden="true">🌸</span>

      {/* Text content */}
      <div className="hero-content">
        <span className={`hero-badge ${JLPT_CLASS[jlptLevel] ?? 'jlpt-n5'}`}>{jlptLevel}</span>
        <h1 className="hero-title">{title}</h1>
        <p className="hero-desc">{description}</p>

        <div className="hero-progress" aria-label={`Đã hoàn thành ${completedLessons} trên ${totalLessons} bài`}>
          <div className="hero-progress-track">
            <div
              className="hero-progress-fill"
              style={{ width: `${pct}%` }}
              role="progressbar"
              aria-valuenow={pct}
              aria-valuemin={0}
              aria-valuemax={100}
            />
          </div>
          <span className="hero-progress-label">{completedLessons} / {totalLessons} bài</span>
        </div>

        <button
          className="hero-cta"
          type="button"
          onClick={() => navigate('/learn/new')}
        >
          HỌC TỪ MỚI
        </button>
      </div>

      {/* Mascot */}
      <div className="hero-mascot" aria-hidden="true">
        <SakuChan variant="idle" size={120} />
      </div>
    </div>
  );
}

export default HeroBanner;
