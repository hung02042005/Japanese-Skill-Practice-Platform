import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import TopNav from '../../components/layout/TopNav';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import { JlptBadge } from '../../components/common/Badges';
import { ToastContainer, useToast } from '../../components/common/Toast';
import SkillRadarChart from '../../components/student/SkillRadarChart';
import { getMyStats, getMyExamHistory } from '../../api/studentService';
import { DEMO_MODE, MOCK_STATS, MOCK_EXAM_HISTORY } from '../../api/mockData';
import './Progress.css';

const COMPLETION_LABELS = { lesson: 'Bài học', kanji: 'Kanji', vocabulary: 'Từ vựng', grammar: 'Ngữ pháp', kana: 'Kana' };
const RADAR_KEY_MAP = { speaking: 'pronunciation' };

export default function Progress() {
  const { toasts, addToast, removeToast } = useToast();

  const [stats,     setStats]    = useState(null);
  const [history,   setHistory]  = useState([]);
  const [isLoading, setLoading]  = useState(true);
  const [histLoad,  setHistLoad] = useState(true);
  const [page,      setPage]     = useState(1);
  const [totalPages,setTotal]    = useState(1);

  useEffect(() => {
    if (DEMO_MODE) {
      setStats(MOCK_STATS);
      setLoading(false);
      return;
    }
    (async () => {
      setLoading(true);
      try {
        const data = await getMyStats();
        setStats(data);
      } catch {
        addToast('error', 'Không thể tải thống kê.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  useEffect(() => {
    if (DEMO_MODE) {
      setHistory(MOCK_EXAM_HISTORY.content);
      setTotal(MOCK_EXAM_HISTORY.totalPages);
      setHistLoad(false);
      return;
    }
    (async () => {
      setHistLoad(true);
      try {
        const res = await getMyExamHistory({ page: page - 1, size: 10 });
        setHistory(res.content ?? []);
        setTotal(res.totalPages ?? 1);
      } catch {
        /* silent — hiện empty state */
      } finally {
        setHistLoad(false);
      }
    })();
  }, [page]);

  const statItems = stats ? [
    { icon: '🔥', value: stats.currentStreak, label: 'ngày streak', sub: `Dài nhất: ${stats.longestStreak} ngày` },
    { icon: '📚', value: Object.values(stats.completions ?? {}).reduce((sum, v) => sum + v, 0), label: 'mục đã hoàn thành' },
  ] : [];

  const radarData = Object.fromEntries(
    Object.entries(stats?.skillsRadar ?? {}).map(([key, val]) => [RADAR_KEY_MAP[key] ?? key, val])
  );

  return (
    <div className="prg-page">
      <TopNav activeTab="" />
      <main className="prg-body">
        <h1 className="prg-title">Tiến Độ Của Tôi</h1>

        {/* Stat row */}
        <div className="prg-stat-row">
          {isLoading
            ? [1, 2].map((i) => <div key={i} className="prg-skel prg-skel--stat" aria-hidden="true" />)
            : statItems.map((s, i) => (
              <div key={i} className="prg-stat-card">
                <span className="prg-stat-icon" aria-hidden="true">{s.icon}</span>
                <div className="prg-stat-body">
                  <span className="prg-stat-value">{s.value}</span>
                  <span className="prg-stat-label">{s.label}</span>
                  {s.sub && <span className="prg-stat-sub">{s.sub}</span>}
                </div>
              </div>
            ))
          }
        </div>

        {/* Charts */}
        {!isLoading && stats && (
          <div className="prg-charts-row">
            <div className="prg-chart-card">
              <h2 className="prg-card-title">Năng lực kỹ năng</h2>
              <div className="prg-radar-wrap">
                <SkillRadarChart data={radarData} />
              </div>
            </div>

            <div className="prg-chart-card">
              <h2 className="prg-card-title">Hoàn thành nội dung</h2>
              <div className="prg-completions">
                {Object.entries(stats.completionRates ?? {}).map(([key, rate]) => {
                  const pct = Math.round(rate);
                  const count = stats.completions?.[key] ?? 0;
                  return (
                    <div key={key} className="prg-comp-row">
                      <span className="prg-comp-label">{COMPLETION_LABELS[key] ?? key}</span>
                      <div className="prg-comp-bar-wrap">
                        <div className="prg-comp-bar">
                          <div className="prg-comp-fill" style={{ width: `${pct}%` }} />
                        </div>
                      </div>
                      <span className="prg-comp-count">{count} hoàn thành</span>
                      <span className="prg-comp-pct">{pct}%</span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* Exam history */}
        <section>
          <h2 className="prg-section-title">Lịch sử bài thi</h2>
          {histLoad
            ? <div className="prg-skel prg-skel--table" aria-hidden="true" />
            : history.length === 0
              ? (
                <EmptyState
                  title="Chưa có bài thi nào"
                  subtitle="Thử sức với đề thi thử JLPT ngay!"
                  mascotVariant="thinking"
                  mascotSize={120}
                >
                  <Link to="/mock-test" className="prg-cta-btn">Xem đề thi →</Link>
                </EmptyState>
              )
              : (
                <>
                  <div className="prg-table-wrap">
                    <table className="prg-table">
                      <thead>
                        <tr>
                          <th scope="col">Ngày thi</th>
                          <th scope="col">Đề thi</th>
                          <th scope="col">Điểm</th>
                          <th scope="col">Kết quả</th>
                          <th scope="col" aria-label="Chi tiết" />
                        </tr>
                      </thead>
                      <tbody>
                        {history.map((h, i) => (
                          <tr key={h.attemptId} className="prg-tr" style={{ '--row-i': i }}>
                            <td>
                              <time dateTime={h.attemptedAt}>
                                {new Date(h.attemptedAt).toLocaleDateString('vi-VN')}
                              </time>
                            </td>
                            <td>
                              <div className="prg-exam-cell">
                                <JlptBadge level={h.jlptLevel} />
                                <span>{h.assessmentTitle}</span>
                              </div>
                            </td>
                            <td className="prg-td-score">{h.score}/{h.maxScore}</td>
                            <td>
                              <span className={`prg-result-badge${h.isPassed ? ' prg-result-badge--pass' : ' prg-result-badge--fail'}`}>
                                {h.isPassed ? '✅ Đậu' : '❌ Không đậu'}
                              </span>
                            </td>
                            <td>
                              <Link
                                to={`/mock-test/${h.assessmentId}/results?attemptId=${h.attemptId}`}
                                className="prg-detail-link"
                              >
                                Xem →
                              </Link>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  {totalPages > 1 && (
                    <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
                  )}
                </>
              )
          }
        </section>
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
