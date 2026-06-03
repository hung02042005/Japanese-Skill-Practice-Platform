import { JlptBadge } from '../common/Badges';

export default function StudentDetailPanel({ detail }) {
  return (
    <div className="sst-detail-body">
      <div className="sst-stats-grid">
        <div className="sst-stat-card">
          <div className="sst-stat-value">{detail.currentStreak}</div>
          <div className="sst-stat-label">Streak hiện tại (ngày)</div>
        </div>
        <div className="sst-stat-card">
          <div className="sst-stat-value">{detail.lessonsCompleted}</div>
          <div className="sst-stat-label">Bài học hoàn thành</div>
        </div>
        <div className="sst-stat-card">
          <div className="sst-stat-value">{detail.averageQuizScore}%</div>
          <div className="sst-stat-label">Điểm quiz trung bình</div>
        </div>
        <div className="sst-stat-card">
          <div className="sst-stat-value">{detail.jlptLevel ?? '—'}</div>
          <div className="sst-stat-label">Cấp độ JLPT</div>
        </div>
      </div>

      {detail.recentAttempts?.length > 0 && (
        <div className="sst-recent">
          <h3 className="sst-recent-title">Lịch sử thi gần đây</h3>
          <div className="sst-attempts-wrap">
            <table className="sst-attempts-table">
              <thead>
                <tr>
                  <th>Bài thi / Quiz</th>
                  <th>Điểm</th>
                  <th>%</th>
                  <th>Ngày</th>
                </tr>
              </thead>
              <tbody>
                {detail.recentAttempts.map((a) => (
                  <tr key={a.attemptId}>
                    <td>{a.title}</td>
                    <td>{a.score}/{a.maxScore}</td>
                    <td className={a.scorePct >= 70 ? 'sst-score--pass' : 'sst-score--fail'}>
                      {a.scorePct}%
                    </td>
                    <td>{new Date(a.takenAt).toLocaleDateString('vi-VN')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
