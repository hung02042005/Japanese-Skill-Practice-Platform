import { JlptBadge } from '../common/Badges';

/**
 * Renders the student progress panel.
 * Accepts `detail` shaped as StudentProgressResponse from GET /api/staff/students/{id}/progress:
 *   studentId, fullName, currentStreak, longestStreak, lastActivityDate,
 *   lessonsCompleted, kanjiCompleted, vocabularyCompleted, grammarCompleted, kanaCompleted,
 *   totalExamsTaken, averageExamScore, highestExamScore
 */
export default function StudentDetailPanel({ detail }) {
  if (!detail) return null;

  const completions = [
    { label: 'Bài học',   value: detail.lessonsCompleted    ?? 0 },
    { label: 'Kanji',     value: detail.kanjiCompleted      ?? 0 },
    { label: 'Từ vựng',  value: detail.vocabularyCompleted ?? 0 },
    { label: 'Ngữ pháp', value: detail.grammarCompleted    ?? 0 },
    { label: 'Kana',      value: detail.kanaCompleted       ?? 0 },
  ];

  return (
    <div className="sst-detail-body">
      <div className="sst-stats-grid">
        <div className="sst-stat-card">
          <div className="sst-stat-value">{detail.currentStreak ?? 0}</div>
          <div className="sst-stat-label">Streak hiện tại (ngày)</div>
        </div>
        <div className="sst-stat-card">
          <div className="sst-stat-value">{detail.longestStreak ?? 0}</div>
          <div className="sst-stat-label">Streak dài nhất (ngày)</div>
        </div>
        <div className="sst-stat-card">
          <div className="sst-stat-value">{detail.totalExamsTaken ?? 0}</div>
          <div className="sst-stat-label">Bài thi đã làm</div>
        </div>
        <div className="sst-stat-card">
          <div className="sst-stat-value">
            {detail.averageExamScore != null
              ? Number(detail.averageExamScore).toFixed(1)
              : '—'}
          </div>
          <div className="sst-stat-label">Điểm thi trung bình</div>
        </div>
      </div>

      <div className="sst-recent" style={{ marginTop: '1.5rem' }}>
        <h3 className="sst-recent-title">Tiến độ hoàn thành</h3>
        <div className="sst-attempts-wrap">
          <table className="sst-attempts-table">
            <thead>
              <tr>
                <th>Loại nội dung</th>
                <th>Số đã hoàn thành</th>
              </tr>
            </thead>
            <tbody>
              {completions.map(({ label, value }) => (
                <tr key={label}>
                  <td>{label}</td>
                  <td>{value}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {detail.lastActivityDate && (
        <p style={{ marginTop: '1rem', opacity: 0.7, fontSize: '0.85rem' }}>
          Hoạt động gần nhất: {new Date(detail.lastActivityDate).toLocaleDateString('vi-VN')}
        </p>
      )}
    </div>
  );
}
