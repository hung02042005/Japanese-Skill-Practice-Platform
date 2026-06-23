import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import { getExamList } from '../../api/studentService';
import './MockTestList.css';

const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function MockTestList() {
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);

  const [exams,     setExams]  = useState([]);
  const [level,     setLevel]  = useState(user?.jlptLevel ?? 'N5');
  const [isLoading, setLoading]= useState(true);
  const [error,     setError]  = useState('');
  const [page,      setPage]   = useState(1);
  const [totalPages,setTotal]  = useState(1);

  const fetchExams = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getExamList({ level, page: page - 1, size: 10 });
      setExams(res.content ?? []);
      setTotal(res.totalPages ?? 1);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải danh sách đề thi.');
    } finally {
      setLoading(false);
    }
  }, [level, page]);

  useEffect(() => { fetchExams(); }, [fetchExams]);
  useEffect(() => { setPage(1); }, [level]);

  return (
    <div className="mkt-page">
      <TopNav activeTab="mock-test" />
      <main className="mkt-body">
        <div className="mkt-page-header">
          <h1 className="mkt-title">Thi Thử JLPT</h1>
          <p className="mkt-subtitle">Luyện đề thật, chuẩn bị tốt nhất cho kỳ thi.</p>
        </div>

        <div className="mkt-level-tabs" role="tablist" aria-label="Chọn cấp độ JLPT">
          {LEVELS.map((l) => (
            <button
              key={l}
              role="tab"
              aria-selected={level === l}
              className={`mkt-level-tab${level === l ? ' mkt-level-tab--active' : ''}`}
              onClick={() => setLevel(l)}
            >
              {l}
            </button>
          ))}
        </div>

        {error && (
          <div className="mkt-error-banner" role="alert">
            <span>{error}</span>
            <button className="mkt-retry-btn" onClick={fetchExams}>Thử lại</button>
          </div>
        )}

        {isLoading && (
          <div className="mkt-list">
            {[1, 2, 3].map((i) => <div key={i} className="mkt-skel" aria-hidden="true" />)}
          </div>
        )}

        {!isLoading && !error && exams.length === 0 && (
          <EmptyState
            title={`Chưa có đề thi ${level}`}
            subtitle="Đề thi đang được biên soạn. Hãy thử level khác hoặc quay lại sau."
            mascotVariant="thinking"
            mascotSize={140}
          />
        )}

        {!isLoading && !error && exams.length > 0 && (
          <>
            <div className="mkt-list">
              {exams.map((exam) => (
                <div key={exam.assessmentId} className="mkt-card">
                  <div className="mkt-card-header">
                    <JlptBadge level={exam.jlptLevel} />
                    <h2 className="mkt-card-title">{exam.title}</h2>
                  </div>

                  <div className="mkt-card-meta">
                    <span className="mkt-meta-item">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2" stroke="currentColor" strokeWidth="2"/>
                        <rect x="9" y="3" width="6" height="4" rx="1" stroke="currentColor" strokeWidth="2"/>
                      </svg>
                      {exam.questionCount} câu
                    </span>
                    <span className="mkt-meta-item">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                        <path d="M12 6v6l4 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                      </svg>
                      {exam.durationMin} phút
                    </span>
                    <span className="mkt-meta-item mkt-meta-item--pass">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M9 12l2 2 4-4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                      </svg>
                      Đậu: {exam.passScore}/{exam.totalScore}
                    </span>
                  </div>

                  <div className="mkt-card-footer">
                    <div className="mkt-last-attempt">
                      <span className="mkt-attempt-virgin">Chưa thi lần nào</span>
                    </div>
                    <button
                      className="mkt-start-btn"
                      onClick={() => navigate(`/mock-test/${exam.assessmentId}/attempt`)}
                    >
                      Bắt đầu thi
                    </button>
                  </div>
                </div>
              ))}
            </div>
            {totalPages > 1 && (
              <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
            )}
          </>
        )}
      </main>
    </div>
  );
}
