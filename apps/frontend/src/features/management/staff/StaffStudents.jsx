import { useState, useEffect, useRef, useCallback } from 'react';
import StaffTopNav from '@/shared/components/layout/StaffTopNav';
import StaffPageHero from '@/features/management/components/staff/StaffPageHero';
import { JlptBadge } from '@/shared/components/common/Badges';
import { Pagination } from '@/shared/components/common/Pagination';
import { EmptyState } from '@/shared/components/common/EmptyState';
import StudentDetailPanel from '@/features/management/components/staff/StudentDetailPanel';
import { getStaffStudents, getStudentProgress } from '@/shared/api/staffService';
import './StaffStudents.css';

// ─── Mock Data ────────────────────────────────────────────────────────────────

const LEVELS   = [
  { id: '', label: 'Tất cả' },
  { id: 'N5', label: 'N5' },
  { id: 'N4', label: 'N4' },
  { id: 'N3', label: 'N3' },
  { id: 'N2', label: 'N2' },
  { id: 'N1', label: 'N1' },
];
const STATUSES = [
  { id: '',          label: 'Tất cả'    },
  { id: 'active',    label: 'Active'    },
  { id: 'suspended', label: 'Suspended' },
];

const PAGE_SIZE = 10;

export default function StaffStudents() {
  const [view,          setView]        = useState('list');
  const [search,        setSearch]      = useState('');
  const [debounced,     setDebounced]   = useState('');
  const [levelFilter,   setLevel]       = useState('');
  const [statusFilter,  setStatus]      = useState('');
  const [students,      setStudents]    = useState([]);
  const [totalElements, setTotalEl]     = useState(0);
  const [totalPages,    setTotalPages]  = useState(1);
  const [isLoading,     setLoading]     = useState(true);
  const [listError,     setListError]   = useState('');
  const [page,          setPage]        = useState(1);
  const [activeStudent, setActiveStudent] = useState(null);
  const [detail,        setDetail]        = useState(null);
  const [isLoadingDet,  setLoadingDet]    = useState(false);
  const timerRef = useRef(null);

  // Debounce search
  useEffect(() => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(search), 300);
    return () => clearTimeout(timerRef.current);
  }, [search]);

  useEffect(() => { setPage(1); }, [debounced, levelFilter, statusFilter]);

  // Server-side fetch (lọc + phân trang ở backend)
  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setListError('');
    getStaffStudents({
      search: debounced || undefined,
      level: levelFilter || undefined,
      status: statusFilter || undefined,
      page: page - 1,
      size: PAGE_SIZE,
    })
      .then((data) => {
        if (cancelled) return;
        setStudents(data.content ?? []);
        setTotalEl(data.totalElements ?? 0);
        setTotalPages(Math.max(1, data.totalPages ?? 1));
      })
      .catch((err) => { if (!cancelled) setListError(err?.response?.data?.message ?? 'Không thể tải danh sách học viên.'); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [debounced, levelFilter, statusFilter, page]);

  const safePage = page;

  const openDetail = useCallback((student) => {
    setActiveStudent(student);
    setLoadingDet(true);
    setDetail(null);
    setView('detail');
    getStudentProgress(student.studentId)
      .then((d) => setDetail(d))
      .catch(() => setDetail(null))
      .finally(() => setLoadingDet(false));
  }, []);

  // ── Detail view ──
  if (view === 'detail' && activeStudent) {
    return (
      <div className="sst-page">
        <StaffTopNav activeTab="staff-students" />
        <main className="sst-body">
          <button className="sst-back-btn" onClick={() => setView('list')}>
            ← Danh sách học viên
          </button>

          <div className="sst-detail-header">
            <div className="sst-detail-info">
              <h2 className="sst-detail-name">{activeStudent.fullName}</h2>
              <span className="sst-detail-email">{activeStudent.email}</span>
              <JlptBadge level={activeStudent.jlptLevel} />
              <span className={`sst-status-badge sst-status-badge--${activeStudent.status}`}>
                {activeStudent.status === 'active' ? 'Active' : 'Suspended'}
              </span>
            </div>
          </div>

          {isLoadingDet ? (
            <div className="sst-detail-skel" aria-hidden="true" />
          ) : detail ? (
            <StudentDetailPanel detail={detail} />
          ) : (
            <div className="sst-error" role="alert">Không có dữ liệu tiến độ cho học viên này.</div>
          )}
        </main>
      </div>
    );
  }

  // ── List view ──
  return (
    <div className="sst-page">
      <StaffTopNav activeTab="staff-students" />
      <main className="sst-body">
        <StaffPageHero
          accent="green"
          title="Quản Lý Học Viên"
          subtitle="Theo dõi tiến độ, quản lý tài khoản và hỗ trợ học viên trong quá trình học"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              {/* Cây hoa anh đào với học viên */}
              <line x1="24" y1="44" x2="24" y2="28"/>
              <path d="M24 28 L12 18"/>
              <path d="M24 28 L36 18"/>
              <path d="M24 28 L24 16"/>
              <ellipse cx="24" cy="11" rx="2" ry="3" transform="rotate(0 24 16)"/>
              <ellipse cx="24" cy="11" rx="2" ry="3" transform="rotate(72 24 16)"/>
              <ellipse cx="24" cy="11" rx="2" ry="3" transform="rotate(144 24 16)"/>
              <ellipse cx="24" cy="11" rx="2" ry="3" transform="rotate(216 24 16)"/>
              <ellipse cx="24" cy="11" rx="2" ry="3" transform="rotate(288 24 16)"/>
              <circle cx="12" cy="35" r="3.5"/>
              <line x1="12" y1="38.5" x2="12" y2="44"/>
              <line x1="8" y1="41.5" x2="16" y2="41.5"/>
              <circle cx="36" cy="35" r="3.5"/>
              <line x1="36" y1="38.5" x2="36" y2="44"/>
              <line x1="32" y1="41.5" x2="40" y2="41.5"/>
            </svg>
          }
        />

        <div className="sst-page-header">
          <h1 className="sst-page-title">Quản Lý Học Viên</h1>
          <span className="sst-total-count">{totalElements} học viên</span>
        </div>

        <div className="sst-filters">
          <label className="visually-hidden" htmlFor="sst-search">Tìm học viên</label>
          <input
            id="sst-search"
            type="search"
            className="sst-search"
            placeholder="Tên, email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <label className="visually-hidden" htmlFor="sst-level-select">Level</label>
          <select
            id="sst-level-select"
            className="sst-select"
            value={levelFilter}
            onChange={(e) => setLevel(e.target.value)}
          >
            {LEVELS.map((l) => <option key={l.id} value={l.id}>{l.label}</option>)}
          </select>
          <label className="visually-hidden" htmlFor="sst-status-select">Trạng thái</label>
          <select
            id="sst-status-select"
            className="sst-select"
            value={statusFilter}
            onChange={(e) => setStatus(e.target.value)}
          >
            {STATUSES.map((s) => <option key={s.id} value={s.id}>{s.label}</option>)}
          </select>
        </div>

        {isLoading ? (
          <EmptyState title="Đang tải…" subtitle="Vui lòng chờ trong giây lát." mascotVariant="thinking" mascotSize={100} />
        ) : listError ? (
          <EmptyState title="Lỗi tải dữ liệu" subtitle={listError} mascotVariant="thinking" mascotSize={100} />
        ) : students.length === 0 ? (
          <EmptyState
            title="Không có học viên nào"
            subtitle="Thử thay đổi bộ lọc tìm kiếm."
            mascotVariant="thinking"
            mascotSize={120}
          />
        ) : (
          <table className="sst-table">
            <caption className="visually-hidden">Danh sách học viên</caption>
            <thead>
              <tr>
                <th>Tên</th>
                <th>Email</th>
                <th>Level</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {students.map((s) => (
                <tr key={s.studentId}>
                  <td className="sst-td-name">
                    <button className="sst-name-btn" onClick={() => openDetail(s)}>
                      {s.fullName}
                    </button>
                  </td>
                  <td className="sst-td-email">{s.email}</td>
                  <td><JlptBadge level={s.jlptLevel} /></td>
                  <td>
                    <span className={`sst-status-badge sst-status-badge--${s.status}`}>
                      {s.status === 'active' ? 'Active' : 'Suspended'}
                    </span>
                  </td>
                  <td>
                    <div className="sst-td-actions">
                      <button
                        className="sst-btn-icon"
                        onClick={() => openDetail(s)}
                        aria-label={`Xem tiến độ ${s.fullName}`}
                        title="Xem tiến độ"
                      >
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                          <circle cx="12" cy="12" r="3"/>
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                        </svg>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {totalPages > 1 && (
          <Pagination currentPage={safePage} totalPages={totalPages} onChange={setPage} />
        )}
      </main>
    </div>
  );
}
