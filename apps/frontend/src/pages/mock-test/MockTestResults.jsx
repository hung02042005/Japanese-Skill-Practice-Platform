import { useState, useEffect } from 'react';
import { useParams, useSearchParams, Link, useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { getQuizAttemptResult } from '../../api/studentService';
import './MockTestResults.css';

const SKILL_LABELS = {
  vocabulary: 'Từ vựng',
  grammar:    'Ngữ pháp',
  reading:    'Đọc hiểu',
  listening:  'Nghe hiểu',
  mixed:      'Tổng hợp',
};

export default function MockTestResults() {
  const { id }  = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const attemptId = searchParams.get('attemptId');

  const [result,    setResult]  = useState(null);
  const [isLoading, setLoading] = useState(true);
  const [error,     setError]   = useState('');

  useEffect(() => {
    if (!attemptId) { navigate('/mock-test'); return; }
    (async () => {
      setLoading(true);
      try {
        const data = await getQuizAttemptResult(attemptId);
        setResult(data);
      } catch (err) {
        setError(err?.response?.data?.message ?? 'Không thể tải kết quả.');
      } finally {
        setLoading(false);
      }
    })();
  }, [attemptId, navigate]);

  return (
    <div className="mxr-page">
      <TopNav activeTab="mock-test" />
      <main className="mxr-body">
        <Link to="/mock-test" className="mxr-back-link">← Về danh sách đề thi</Link>

        {error && <div className="mxr-error-banner" role="alert">{error}</div>}

        {isLoading && (
          <>
            <div className="mxr-skel mxr-skel--hero"  aria-hidden="true" />
            <div className="mxr-skel mxr-skel--table" aria-hidden="true" />
          </>
        )}

        {!isLoading && result && (
          <>
            <div className={`mxr-hero-card${result.isPassed ? ' mxr-hero-card--passed' : ' mxr-hero-card--failed'}`}>
              <div className="mxr-hero-header">
                <JlptBadge level={result.jlptLevel} />
                <h1 className="mxr-hero-title">{result.assessmentTitle}</h1>
                <time className="mxr-hero-date" dateTime={result.attemptedAt}>
                  {new Date(result.attemptedAt).toLocaleString('vi-VN')}
                </time>
              </div>

              <div className="mxr-hero-score-row">
                <div className="mxr-score-block">
                  <span className="mxr-score-num">{result.score}</span>
                  <span className="mxr-score-max">/ {result.maxScore}</span>
                  <span className="mxr-score-label">điểm</span>
                </div>
                <div className={`mxr-pass-badge${result.isPassed ? ' mxr-pass-badge--passed' : ' mxr-pass-badge--failed'}`}>
                  {result.isPassed ? '✅ ĐẬU' : '❌ KHÔNG ĐẬU'}
                  <span className="mxr-pass-note">Cần: {result.passScore} điểm</span>
                </div>
              </div>

              {result.sectionScores && (
                <div className="mxr-sections">
                  {Object.entries(result.sectionScores).map(([key, val]) => (
                    <div key={key} className="mxr-section-row">
                      <span className="mxr-section-name">{SKILL_LABELS[key] ?? key}</span>
                      <div className="mxr-section-bar-wrap">
                        <div className="mxr-section-bar">
                          <div className="mxr-section-fill" style={{ width: `${val.percent ?? 0}%` }} />
                        </div>
                      </div>
                      <span className="mxr-section-pct">{val.percent ?? 0}%</span>
                    </div>
                  ))}
                </div>
              )}

              {result.previousAttempt && (
                <div className="mxr-compare">
                  {result.score > result.previousAttempt.score
                    ? `📈 Tốt hơn lần trước: ↑ +${result.score - result.previousAttempt.score} điểm (${result.previousAttempt.score} → ${result.score})`
                    : result.score < result.previousAttempt.score
                    ? `📉 Thấp hơn lần trước: ↓ -${result.previousAttempt.score - result.score} điểm`
                    : `↔️ Bằng lần trước: ${result.score} điểm`
                  }
                </div>
              )}
            </div>

            {result.questionResults?.length > 0 && (
              <section aria-label="Chi tiết từng câu hỏi">
                <h2 className="mxr-section-heading">Chi tiết từng câu</h2>
                <div className="mxr-table-wrap">
                  <table className="mxr-table">
                    <thead>
                      <tr>
                        <th scope="col">Câu</th>
                        <th scope="col">Kỹ năng</th>
                        <th scope="col">Đáp án bạn</th>
                        <th scope="col">Đáp án đúng</th>
                        <th scope="col" aria-label="Kết quả" />
                      </tr>
                    </thead>
                    <tbody>
                      {result.questionResults.map((qr) => (
                        <tr
                          key={qr.questionNumber}
                          className={`mxr-tr${qr.isCorrect ? ' mxr-tr--correct' : ' mxr-tr--wrong'}`}
                        >
                          <td className="mxr-td-num">{qr.questionNumber}</td>
                          <td>{SKILL_LABELS[qr.skill] ?? qr.skill}</td>
                          <td className="mxr-td-option">
                            {qr.selectedOption ?? <span className="mxr-skipped">—</span>}
                          </td>
                          <td className="mxr-td-option mxr-td-correct">{qr.correctOption}</td>
                          <td className="mxr-td-result">
                            {qr.isCorrect
                              ? <span aria-label="Đúng">✅</span>
                              : <span aria-label={qr.selectedOption ? 'Sai' : 'Bỏ qua'}>❌</span>
                            }
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            )}

            <div className="mxr-footer-ctas">
              <button
                className="mxr-btn mxr-btn--outline"
                onClick={() => navigate(`/mock-test/${id}/attempt`)}
              >
                Thi lại
              </button>
              <Link to="/dashboard" className="mxr-btn mxr-btn--ghost">Về Dashboard</Link>
              <Link to="/learn/new"  className="mxr-btn mxr-btn--primary">Học bài liên quan</Link>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
