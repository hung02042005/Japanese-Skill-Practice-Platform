import { useState, useCallback, useEffect } from 'react';
import ManagerTopNav from '../../components/layout/ManagerTopNav';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './ManagerReports.css';

const MOCK_CONTENT_BY_TYPE = [
  { type: 'Bài học',  count: 45, color: 'var(--color-secondary)' },
  { type: 'Câu hỏi', count: 120, color: 'var(--color-primary)' },
  { type: 'Từ vựng', count: 88, color: '#1565C0' },
  { type: 'Ngữ pháp', count: 32, color: '#6A1B9A' },
  { type: 'Kanji',   count: 56, color: 'var(--color-error)' },
  { type: 'Đề thi',  count: 18, color: 'var(--color-warning)' },
];

const MOCK_CONTENT_BY_LEVEL = [
  { level: 'N5', count: 85,  color: '#2E7D32' },
  { level: 'N4', count: 78,  color: '#1565C0' },
  { level: 'N3', count: 62,  color: '#E65100' },
  { level: 'N2', count: 44,  color: '#6A1B9A' },
  { level: 'N1', count: 28,  color: '#C62828' },
];

const MOCK_MONTHLY = [
  { month: 'Tháng 1', published: 12, rejected: 2 },
  { month: 'Tháng 2', published: 18, rejected: 3 },
  { month: 'Tháng 3', published: 22, rejected: 1 },
  { month: 'Tháng 4', published: 15, rejected: 4 },
  { month: 'Tháng 5', published: 28, rejected: 2 },
  { month: 'Tháng 6', published: 23, rejected: 3 },
];

const MOCK_STAFF_STATS = [
  { name: 'Staff Lan',  published: 19, rejected: 2, rate: 90 },
  { name: 'Staff Minh', published: 14, rejected: 3, rate: 82 },
  { name: 'Staff Bình', published: 11, rejected: 1, rate: 92 },
  { name: 'Staff Hà',   published: 7,  rejected: 0, rate: 100 },
  { name: 'Staff Tuấn', published: 3,  rejected: 2, rate: 60 },
];

function BarChart({ data, maxValue, labelKey, valueKey, colorKey }) {
  return (
    <div className="mrp-bar-chart" role="list">
      {data.map((item) => {
        const pct = maxValue === 0 ? 0 : Math.round((item[valueKey] / maxValue) * 100);
        return (
          <div key={item[labelKey]} className="mrp-bar-row" role="listitem">
            <span className="mrp-bar-label">{item[labelKey]}</span>
            <div className="mrp-bar-track">
              <div
                className="mrp-bar-fill"
                style={{ width: `${pct}%`, background: item[colorKey] }}
                aria-label={`${item[labelKey]}: ${item[valueKey]}`}
              />
            </div>
            <span className="mrp-bar-value">{item[valueKey]}</span>
          </div>
        );
      })}
    </div>
  );
}

function MonthlyChart({ data }) {
  const maxVal = Math.max(...data.map((d) => d.published + d.rejected));
  return (
    <div className="mrp-monthly-chart" role="list">
      {data.map((item) => {
        const pubPct = maxVal === 0 ? 0 : Math.round((item.published / maxVal) * 100);
        const rejPct = maxVal === 0 ? 0 : Math.round((item.rejected  / maxVal) * 100);
        return (
          <div key={item.month} className="mrp-month-col" role="listitem">
            <div className="mrp-month-bars">
              <div
                className="mrp-month-bar mrp-month-bar--pub"
                style={{ height: `${pubPct}%` }}
                title={`Xuất bản: ${item.published}`}
              />
              <div
                className="mrp-month-bar mrp-month-bar--rej"
                style={{ height: `${rejPct}%` }}
                title={`Từ chối: ${item.rejected}`}
              />
            </div>
            <span className="mrp-month-label">{item.month.replace('Tháng ', 'T')}</span>
          </div>
        );
      })}
    </div>
  );
}

export default function ManagerReports() {
  const [isLoading, setLoading] = useState(true);
  const [error,     setError]   = useState('');

  const fetchData = useCallback(() => {
    setLoading(true);
    setError('');
    setTimeout(() => setLoading(false), 500);
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const maxTypeCount  = Math.max(...MOCK_CONTENT_BY_TYPE.map((d) => d.count));
  const maxLevelCount = Math.max(...MOCK_CONTENT_BY_LEVEL.map((d) => d.count));
  const totalContent  = MOCK_CONTENT_BY_TYPE.reduce((s, d) => s + d.count, 0);
  const totalApproved = MOCK_STAFF_STATS.reduce((s, d) => s + d.published, 0);
  const totalRejected = MOCK_STAFF_STATS.reduce((s, d) => s + d.rejected, 0);
  const approvalRate  = Math.round((totalApproved / (totalApproved + totalRejected)) * 100);

  return (
    <div className="mrp-page">
      <ManagerTopNav activeTab="manager-reports" />

      <main className="mrp-body">
        <StaffPageHero
          accent="gold"
          title="Báo Cáo & Phân Tích"
          subtitle="Tổng quan hiệu quả nội dung, năng suất nhóm và phân bố theo cấp độ JLPT"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M10 6h22l8 8v28a2 2 0 0 1-2 2H10a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z"/>
              <polyline points="32 6 32 14 40 14"/>
              <polyline points="16 30 20 26 24.5 30 32 22"/>
              <line x1="16" y1="35" x2="28" y2="35"/>
            </svg>
          }
        />

        {/* Error */}
        {error && (
          <div className="mrp-error-banner" role="alert">
            <span>{error}</span>
            <button className="mrp-retry-btn" onClick={fetchData}>Thử lại</button>
          </div>
        )}

        {isLoading ? (
          <div className="mrp-skel-grid" aria-hidden="true">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="mrp-skel-card" />
            ))}
          </div>
        ) : (
          <>
            {/* KPI row */}
            <div className="mrp-kpi-row">
              <div className="mrp-kpi-card">
                <span className="mrp-kpi-value">{totalContent}</span>
                <span className="mrp-kpi-label">Tổng nội dung</span>
              </div>
              <div className="mrp-kpi-card mrp-kpi-card--green">
                <span className="mrp-kpi-value">{totalApproved}</span>
                <span className="mrp-kpi-label">Đã xuất bản</span>
              </div>
              <div className="mrp-kpi-card mrp-kpi-card--amber">
                <span className="mrp-kpi-value">{approvalRate}%</span>
                <span className="mrp-kpi-label">Tỷ lệ duyệt</span>
              </div>
              <div className="mrp-kpi-card mrp-kpi-card--pink">
                <span className="mrp-kpi-value">297</span>
                <span className="mrp-kpi-label">Học viên hoạt động</span>
              </div>
            </div>

            {/* Charts row */}
            <div className="mrp-charts-row">
              {/* Content by type */}
              <div className="mrp-chart-card">
                <h2 className="mrp-chart-title">Nội dung theo loại</h2>
                <BarChart
                  data={MOCK_CONTENT_BY_TYPE}
                  maxValue={maxTypeCount}
                  labelKey="type"
                  valueKey="count"
                  colorKey="color"
                />
              </div>

              {/* Content by level */}
              <div className="mrp-chart-card">
                <h2 className="mrp-chart-title">Nội dung theo cấp độ JLPT</h2>
                <BarChart
                  data={MOCK_CONTENT_BY_LEVEL}
                  maxValue={maxLevelCount}
                  labelKey="level"
                  valueKey="count"
                  colorKey="color"
                />
              </div>
            </div>

            {/* Monthly trend */}
            <div className="mrp-chart-card mrp-chart-card--full">
              <h2 className="mrp-chart-title">Xu hướng xuất bản theo tháng</h2>
              <div className="mrp-chart-legend">
                <span className="mrp-legend-item mrp-legend-item--pub">Xuất bản</span>
                <span className="mrp-legend-item mrp-legend-item--rej">Từ chối</span>
              </div>
              <MonthlyChart data={MOCK_MONTHLY} />
            </div>

            {/* Staff approval rate */}
            <div className="mrp-chart-card mrp-chart-card--full">
              <h2 className="mrp-chart-title">Tỷ lệ duyệt theo Staff</h2>
              <div className="mrp-staff-table-wrap">
                <table className="mrp-staff-table">
                  <thead>
                    <tr>
                      <th scope="col">Staff</th>
                      <th scope="col">Đã xuất bản</th>
                      <th scope="col">Từ chối</th>
                      <th scope="col">Tỷ lệ duyệt</th>
                    </tr>
                  </thead>
                  <tbody>
                    {MOCK_STAFF_STATS.map((staff, i) => {
                      const rateColor = staff.rate >= 80
                        ? 'var(--color-secondary)'
                        : staff.rate >= 60
                        ? 'var(--color-accent)'
                        : 'var(--color-error)';
                      return (
                        <tr key={staff.name} className="mrp-tr" style={{ '--row-i': i }}>
                          <td className="mrp-td-name">{staff.name}</td>
                          <td className="mrp-td-num mrp-td-green">{staff.published}</td>
                          <td className="mrp-td-num mrp-td-red">{staff.rejected}</td>
                          <td>
                            <div className="mrp-rate-cell">
                              <div className="mrp-rate-track">
                                <div
                                  className="mrp-rate-fill"
                                  style={{ width: `${staff.rate}%`, background: rateColor }}
                                />
                              </div>
                              <span className="mrp-rate-pct" style={{ color: rateColor }}>
                                {staff.rate}%
                              </span>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
