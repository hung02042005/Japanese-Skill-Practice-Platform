import { useState } from 'react';
import ManagerTopNav from '../../components/layout/ManagerTopNav';
import { EmptyState } from '../../components/common/EmptyState';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './ManagerStaffPerformance.css';

const MOCK_STAFF = [
  { id: 1, name: 'Staff Lan',  email: 'lan@sakuji.vn',   submitted: 24, approved: 19, rejected: 2, pending: 3, lastActivity: '03/06/2026', activeStreak: 12 },
  { id: 2, name: 'Staff Minh', email: 'minh@sakuji.vn',  submitted: 18, approved: 14, rejected: 3, pending: 1, lastActivity: '03/06/2026', activeStreak: 8  },
  { id: 3, name: 'Staff Bình', email: 'binh@sakuji.vn',  submitted: 15, approved: 11, rejected: 1, pending: 3, lastActivity: '02/06/2026', activeStreak: 5  },
  { id: 4, name: 'Staff Hà',   email: 'ha@sakuji.vn',    submitted: 9,  approved: 7,  rejected: 0, pending: 2, lastActivity: '01/06/2026', activeStreak: 3  },
  { id: 5, name: 'Staff Tuấn', email: 'tuan@sakuji.vn',  submitted: 6,  approved: 3,  rejected: 2, pending: 1, lastActivity: '30/05/2026', activeStreak: 0  },
];

function ApprovalBar({ approved, total }) {
  const pct = total === 0 ? 0 : Math.round((approved / total) * 100);
  const color = pct >= 80 ? 'var(--color-secondary)' : pct >= 60 ? 'var(--color-accent)' : 'var(--color-error)';
  return (
    <div className="msp-bar-wrap">
      <div className="msp-bar-track">
        <div className="msp-bar-fill" style={{ width: `${pct}%`, background: color }} />
      </div>
      <span className="msp-bar-pct" style={{ color }}>{pct}%</span>
    </div>
  );
}

export default function ManagerStaffPerformance() {
  const [sortField, setSortField] = useState('submitted');
  const [sortDir, setSortDir] = useState('desc');
  const [search, setSearch] = useState('');

  function toggleSort(field) {
    if (sortField === field) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortField(field);
      setSortDir('desc');
    }
  }

  const filtered = MOCK_STAFF
    .filter((s) => s.name.toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => {
      const av = a[sortField] ?? 0;
      const bv = b[sortField] ?? 0;
      return sortDir === 'asc' ? (av > bv ? 1 : -1) : (av < bv ? 1 : -1);
    });

  const totalSubmitted = MOCK_STAFF.reduce((s, m) => s + m.submitted, 0);
  const totalApproved  = MOCK_STAFF.reduce((s, m) => s + m.approved, 0);
  const totalPending   = MOCK_STAFF.reduce((s, m) => s + m.pending, 0);

  function SortIcon({ field }) {
    if (sortField !== field) return <span className="msp-sort-icon msp-sort-icon--inactive">↕</span>;
    return <span className="msp-sort-icon">{sortDir === 'asc' ? '↑' : '↓'}</span>;
  }

  return (
    <div className="msp-page">
      <ManagerTopNav activeTab="manager-staff-performance" />

      <main className="msp-body">
        <StaffPageHero
          accent="green"
          title="Hiệu Suất Staff"
          subtitle="Theo dõi năng suất, tỷ lệ duyệt và hoạt động của từng thành viên trong nhóm"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <rect x="4" y="26" width="8" height="18" rx="2"/>
              <rect x="19" y="16" width="8" height="28" rx="2"/>
              <rect x="34" y="6" width="8" height="38" rx="2"/>
              <polyline points="6 24 23 14 38 5" strokeDasharray="3 2"/>
            </svg>
          }
        />

        {/* Summary stats */}
        <div className="msp-summary-row">
          <div className="msp-summary-card">
            <span className="msp-summary-value">{totalSubmitted}</span>
            <span className="msp-summary-label">Tổng đã gửi</span>
          </div>
          <div className="msp-summary-card msp-summary-card--green">
            <span className="msp-summary-value">{totalApproved}</span>
            <span className="msp-summary-label">Đã duyệt</span>
          </div>
          <div className="msp-summary-card msp-summary-card--amber">
            <span className="msp-summary-value">{totalPending}</span>
            <span className="msp-summary-label">Đang chờ</span>
          </div>
          <div className="msp-summary-card msp-summary-card--pink">
            <span className="msp-summary-value">{MOCK_STAFF.length}</span>
            <span className="msp-summary-label">Thành viên</span>
          </div>
        </div>

        {/* Search + table */}
        <div className="msp-toolbar">
          <input
            className="msp-search"
            placeholder="Tìm staff theo tên..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            aria-label="Tìm kiếm staff"
          />
        </div>

        {filtered.length === 0 ? (
          <EmptyState title="Không tìm thấy staff" subtitle="Thử tìm kiếm với từ khóa khác." mascotVariant="thinking" />
        ) : (
          <div className="msp-table-wrap">
            <table className="msp-table">
              <thead>
                <tr>
                  <th>Thành viên</th>
                  <th className="msp-th-sort" onClick={() => toggleSort('submitted')}>
                    Đã gửi <SortIcon field="submitted" />
                  </th>
                  <th className="msp-th-sort" onClick={() => toggleSort('approved')}>
                    Được duyệt <SortIcon field="approved" />
                  </th>
                  <th>Tỷ lệ duyệt</th>
                  <th className="msp-th-sort" onClick={() => toggleSort('rejected')}>
                    Từ chối <SortIcon field="rejected" />
                  </th>
                  <th className="msp-th-sort" onClick={() => toggleSort('pending')}>
                    Đang chờ <SortIcon field="pending" />
                  </th>
                  <th>Hoạt động cuối</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((staff) => {
                  const total = staff.approved + staff.rejected;
                  return (
                    <tr key={staff.id}>
                      <td>
                        <div className="msp-staff-cell">
                          <div className="msp-avatar">
                            {staff.name.split(' ').pop().charAt(0)}
                          </div>
                          <div>
                            <p className="msp-staff-name">{staff.name}</p>
                            <p className="msp-staff-email">{staff.email}</p>
                          </div>
                        </div>
                      </td>
                      <td className="msp-td-num">{staff.submitted}</td>
                      <td className="msp-td-num msp-td-green">{staff.approved}</td>
                      <td style={{ minWidth: 130 }}>
                        <ApprovalBar approved={staff.approved} total={total} />
                      </td>
                      <td className="msp-td-num msp-td-red">{staff.rejected}</td>
                      <td>
                        {staff.pending > 0 ? (
                          <span className="msp-badge-pending">{staff.pending} chờ</span>
                        ) : (
                          <span className="msp-badge-clear">—</span>
                        )}
                      </td>
                      <td className="msp-td-date">{staff.lastActivity}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
}
