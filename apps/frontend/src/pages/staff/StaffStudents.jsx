import { useState, useEffect, useRef, useCallback } from 'react';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import StudentDetailPanel from '../../components/staff/StudentDetailPanel';
import SuspendConfirmModal from '../../components/staff/SuspendConfirmModal';
import {
  getStaffStudents,
  getStudentProgress,
  suspendStudent,
  activateStudent,
} from '../../api/staffService';
import './StaffStudents.css';

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

export default function StaffStudents() {
  // List state
  const [view,          setView]        = useState('list');
  const [search,        setSearch]      = useState('');
  const [debounced,     setDebounced]   = useState('');
  const [levelFilter,   setLevel]       = useState('');
  const [statusFilter,  setStatus]      = useState('');
  const [students,      setStudents]    = useState([]);
  const [isLoading,     setLoading]     = useState(true);
  const [error,         setError]       = useState('');
  const [page,          setPage]        = useState(1);
  const [totalPages,    setTotal]       = useState(1);
  const [totalEl,       setTotalEl]     = useState(0);
  // Detail state
  const [activeStudent, setActiveStudent] = useState(null);
  const [detail,        setDetail]        = useState(null);
  const [isLoadingDet,  setLoadingDet]    = useState(false);
  // Confirm modal state
  const [confirmModal,  setConfirmModal]  = useState(null);
  const [reason,        setReason]        = useState('');
  const [isActioning,   setActioning]     = useState(false);
  const timerRef = useRef(null);

  // Debounce search
  useEffect(() => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(search), 400);
    return () => clearTimeout(timerRef.current);
  }, [search]);

  useEffect(() => { setPage(1); }, [debounced, levelFilter, statusFilter]);

  const fetchStudents = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getStaffStudents({
        search: debounced,
        level: levelFilter,
        status: statusFilter,
        page: page - 1,
        size: 20,
      });
      setStudents(data.content);
      setTotal(data.totalPages);
      setTotalEl(data.totalElements);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải danh sách học viên.');
    } finally {
      setLoading(false);
    }
  }, [debounced, levelFilter, statusFilter, page]);

  useEffect(() => { fetchStudents(); }, [fetchStudents]);

  const openDetail = async (student) => {
    setActiveStudent(student);
    setLoadingDet(true);
    setDetail(null);
    setView('detail');
    try {
      const data = await getStudentProgress(student.studentId);
      setDetail(data);
    } catch {
      setDetail(null);
    } finally {
      setLoadingDet(false);
    }
  };

  const openConfirm = (student, action) => {
    setReason('');
    setConfirmModal({ student, action });
  };

  const handleAction = async () => {
    if (!confirmModal || isActioning) return;
    setActioning(true);
    const { student, action } = confirmModal;
    try {
      const updated = action === 'suspend'
        ? await suspendStudent(student.studentId, reason)
        : await activateStudent(student.studentId);
      setStudents((prev) => prev.map((s) =>
        s.studentId === updated.studentId ? { ...s, status: updated.status } : s
      ));
      if (activeStudent?.studentId === updated.studentId) {
        setActiveStudent((prev) => ({ ...prev, status: updated.status }));
      }
      setConfirmModal(null);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Thao tác thất bại. Vui lòng thử lại.');
    } finally {
      setActioning(false);
    }
  };

  // ── Detail view ──
  if (view === 'detail' && activeStudent) {
    return (
      <div className="sst-page">
        <TopNav activeTab="" />
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
            <button
              className={`sst-action-btn${activeStudent.status === 'active' ? ' sst-action-btn--suspend' : ' sst-action-btn--activate'}`}
              onClick={() => openConfirm(
                activeStudent,
                activeStudent.status === 'active' ? 'suspend' : 'activate'
              )}
              aria-label={
                activeStudent.status === 'active'
                  ? `Suspend ${activeStudent.fullName}`
                  : `Kích hoạt lại ${activeStudent.fullName}`
              }
            >
              {activeStudent.status === 'active' ? '🔒 Suspend' : '🔓 Kích hoạt lại'}
            </button>
          </div>

          {isLoadingDet ? (
            <div className="sst-detail-skel" aria-hidden="true" />
          ) : detail ? (
            <StudentDetailPanel detail={detail} />
          ) : (
            <div className="sst-error" role="alert">Không thể tải dữ liệu học viên.</div>
          )}
        </main>

        {confirmModal && (
          <SuspendConfirmModal
            modal={confirmModal}
            reason={reason}
            onReasonChange={setReason}
            isActioning={isActioning}
            onConfirm={handleAction}
            onClose={() => setConfirmModal(null)}
          />
        )}
      </div>
    );
  }

  // ── List view ──
  return (
    <div className="sst-page">
      <TopNav activeTab="" />
      <main className="sst-body">
        <div className="sst-page-header">
          <h1 className="sst-page-title">Quản Lý Học Viên</h1>
          <span className="sst-total-count">{totalEl} học viên</span>
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

        {error && (
          <div className="sst-error" role="alert">
            {error}
            <button className="sst-retry" onClick={fetchStudents}>Thử lại</button>
          </div>
        )}

        {isLoading ? (
          <table className="sst-table" aria-busy="true">
            <thead>
              <tr>
                <th>Tên</th><th>Email</th><th>Level</th>
                <th>Trạng thái</th><th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {Array.from({ length: 5 }).map((_, i) => (
                <tr key={i}>
                  {Array.from({ length: 5 }).map((__, j) => (
                    <td key={j}><div className="sst-cell-skel" aria-hidden="true" /></td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
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
                      {s.status === 'active' ? (
                        <button
                          className="sst-btn-suspend"
                          onClick={() => openConfirm(s, 'suspend')}
                          aria-label={`Suspend ${s.fullName}`}
                          title="Suspend"
                        >🔒</button>
                      ) : (
                        <button
                          className="sst-btn-activate"
                          onClick={() => openConfirm(s, 'activate')}
                          aria-label={`Kích hoạt ${s.fullName}`}
                          title="Kích hoạt"
                        >🔓</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {!isLoading && totalPages > 1 && (
          <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
        )}
      </main>

      {confirmModal && (
        <SuspendConfirmModal
          modal={confirmModal}
          reason={reason}
          onReasonChange={setReason}
          isActioning={isActioning}
          onConfirm={handleAction}
          onClose={() => setConfirmModal(null)}
        />
      )}
    </div>
  );
}
