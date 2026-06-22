import { Fragment, useState, useEffect } from 'react';
import { useParams, useSearchParams, Link, useNavigate } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { getExamReview } from '../../api/studentService';
import { DEMO_MODE, MOCK_QUIZ_RESULT } from '../../api/mockData';
import './MockTestResults.css';

const SECTION_LABELS = {
  languageKnowledge: 'Từ vựng - Ngữ pháp',
  reading:           'Đọc hiểu',
  listening:         'Nghe hiểu',
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
    if (DEMO_MODE) {
      setResult(MOCK_QUIZ_RESULT);
      setLoading(false);
      return;
    }
    (async () => {
      setLoading(true);
      try {
        const data = await getExamReview(attemptId);
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
                <h1 className="mxr-hero-title">Kết quả bài thi</h1>
              </div>

              <div className="mxr-hero-score-row">
                <div className="mxr-score-block">
                  <span className="mxr-score-num">{result.totalScore}</span>
                  <span className="mxr-score-max">/ {result.maxScore}</span>
                  <span className="mxr-score-label">điểm</span>
                </div>
                <div className={`mxr-pass-badge${result.isPassed ? ' mxr-pass-badge--passed' : ' mxr-pass-badge--failed'}`}>
                  {result.isPassed ? '✅ ĐẬU' : '❌ KHÔNG ĐẬU'}
                </div>
              </div>

              {result.sectionScores && (
                <div className="mxr-sections">
                  {Object.entries(result.sectionScores).map(([key, val]) => (
                    <div key={key} className="mxr-section-row">
                      <span className="mxr-section-name">{SECTION_LABELS[key] ?? key}</span>
                      <span className="mxr-section-pct">{val} điểm</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {result.results?.length > 0 && (
              <section aria-label="Chi tiết từng câu hỏi">
                <h2 className="mxr-section-heading">Chi tiết từng câu</h2>
                <div className="mxr-table-wrap">
                  <table className="mxr-table">
                    <thead>
                      <tr>
                        <th scope="col">Câu</th>
                        <th scope="col">Đáp án bạn</th>
                        <th scope="col">Đáp án đúng</th>
                        <th scope="col" aria-label="Kết quả" />
                      </tr>
                    </thead>
                    <tbody>
                      {result.results.map((qr, i) => (
                        <Fragment key={qr.questionId}>
                          <tr
                            className={`mxr-tr${qr.isCorrect ? ' mxr-tr--correct' : ' mxr-tr--wrong'}`}
                          >
                            <td className="mxr-td-num">{i + 1}</td>
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
                          {!qr.isCorrect && qr.explanation && (
                            <tr className="mxr-tr-explanation">
                              <td />
                              <td colSpan={3} className="mxr-td-explanation">💡 {qr.explanation}</td>
                            </tr>
                          )}
                        </Fragment>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            )}

            <div className="mxr-footer-ctas">
              {id && (
                <button
                  className="mxr-btn mxr-btn--outline"
                  onClick={() => navigate(`/mock-test/${id}/attempt`)}
                >
                  Thi lại
                </button>
              )}
              <Link to="/dashboard" className="mxr-btn mxr-btn--ghost">Về Dashboard</Link>
              <Link to="/learn"      className="mxr-btn mxr-btn--primary">Học bài liên quan</Link>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
