import { useState, useEffect, useRef, useCallback } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import StaffPageHero from '../../components/staff/StaffPageHero';
import { JlptBadge } from '../../components/common/Badges';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import StudentDetailPanel from '../../components/staff/StudentDetailPanel';
import SuspendConfirmModal from '../../components/staff/SuspendConfirmModal';
import {
  getStaffStudents,
  getStudentDetail,
  getStudentProgress,
  suspendStudent,
  activateStudent,
} from '../../api/staffService';
import './StaffStudents.css';

const LEVELS = [
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
  const [view,          setView]        = useState('list');
  const [search,        setSearch]      = useState('');
  const [debounced,     setDebounced]   = useState('');
  const [levelFilter,   setLevel]       = useState('');
  const [statusFilter,  setStatus]      = useState('');
  const [students,      setStudents]    = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages,    setTotalPages]  = useState(1);
  const [page,          setPage]        = useState(1);
  const [isLoading,     setLoading]     = useState(false);
  const [listError,     setListError]   = useState(null);
  const [activeStudent, setActiveStudent] = useState(null);
  const [detail,        setDetail]        = useState(null);
  const [isLoadingDet,  setLoadingDet]    = useState(false);
  const [detError,      setDetError]      = useState(null);
  const [confirmModal,  setConfirmModal]  = useState(null);
  const [reason,        setReason]        = useState('');
  const [actionError,   setActionError]   = useState(null);
  const [isActioning,   setActioning]     = useState(false);
  const timerRef = useRef(null);

  // Debounce search
  useEffect(() => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(search), 300);
    return () => clearTimeout(timerRef.current);
  }, [search]);

  // Reset to page 1 when filters change
  useEffect(() => { setPage(1); }, [debounced, levelFilter, statusFilter]);

  // Fetch student list from real API
  useEffect(() => {
    if (view !== 'list') return;
    let cancelled = false;
    setLoading(true);
    setListError(null);
    getStaffStudents({
      search: debounced || undefined,
      level:  levelFilter || undefined,
      status: statusFilter || undefined,
      page:   page - 1,   // backend is 0-indexed
      size:   20,
    })
      .then((data) => {
        if (cancelled) return;
        setStudents(data?.content ?? []);
        setTotalElements(data?.totalElements ?? 0);
        setTotalPages(data?.totalPages ?? 1);
      })
      .catch((err) => {
        if (cancelled) return;
        setListError(err?.response?.data?.message ?? 'Không thể tải danh sách học viên.');
      })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [view, debounced, levelFilter, statusFilter, page]);

  const openDetail = useCallback(async (student) => {
    setActiveStudent(student);
    setLoadingDet(true);
    setDetError(null);
    setDetail(null);
    setView('detail');

    try {
      // Fetch both full profile and progress in parallel
      const [fullProfile, progress] = await Promise.all([
        getStudentDetail(student.studentId),
        getStudentProgress(student.studentId),
      ]);
      setActiveStudent(fullProfile);
      setDetail(progress);
    } catch (err) {
      setDetError(err?.response?.data?.message ?? 'Không thể tải thông tin học viên.');
    } finally {
      setLoadingDet(false);
    }
  }, []);

  const openConfirm = useCallback((student, action) => {
    setReason('');
    setActionError(null);
    setConfirmModal({ student, action });
  }, []);

  const handleAction = useCallback(async () => {
    if (!confirmModal || isActioning) return;
    const { student, action } = confirmModal;

    // Frontend UX validation for suspend reason (business validation is server-side)
    if (action === 'suspend' && reason.trim().length < 10) {
      setActionError('Lý do phải có ít nhất 10 ký tự.');
      return;
    }

    setActioning(true);
    setActionError(null);

    try {
      let updated;
      if (action === 'suspend') {
        updated = await suspendStudent(student.studentId, reason.trim());
      } else {
        updated = await activateStudent(student.studentId);
      }

      // Update list state
      setStudents((prev) =>
        prev.map((s) =>
          s.studentId === student.studentId
            ? { ...s, status: updated?.status ?? (action === 'suspend' ? 'suspended' : 'active') }
            : s
        )
      );

      // Update active student if we're in detail view
      if (activeStudent?.studentId === student.studentId) {
        setActiveStudent((prev) => ({ ...prev, status: updated?.status ?? prev.status }));
      }

      setConfirmModal(null);
    } catch (err) {
      const msg = err?.response?.data?.message ?? 'Thao tác thất bại. Vui lòng thử lại.';
      setActionError(msg);
    } finally {
      setActioning(false);
    }
  }, [confirmModal, isActioning, activeStudent, reason]);

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
              <JlptBadge level={activeStudent.currentJlptLevel} />
              <span className={`sst-status-badge sst-status-badge--${activeStudent.status}`}>
                {activeStudent.status === 'active' ? 'Active' : 'Suspended'}
              </span>
            </div>
            <button
              className={`sst-action-btn${activeStudent.status === 'active' ? ' sst-action-btn--suspend' : ' sst-action-btn--activate'}`}
              onClick={() =>
                openConfirm(
                  activeStudent,
                  activeStudent.status === 'active' ? 'suspend' : 'activate'
                )
              }
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
          ) : detError ? (
            <div className="sst-error" role="alert">{detError}</div>
          ) : detail ? (
            <StudentDetailPanel detail={detail} />
          ) : (
            <div className="sst-error" role="alert">Không có dữ liệu tiến độ cho học viên này.</div>
          )}
        </main>

        {confirmModal && (
          <SuspendConfirmModal
            modal={confirmModal}
            reason={reason}
            onReasonChange={setReason}
            isActioning={isActioning}
            actionError={actionError}
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
      <StaffTopNav activeTab="staff-students" />
      <main className="sst-body">
        <StaffPageHero
          accent="green"
          title="Quản Lý Học Viên"
          subtitle="Theo dõi tiến độ, quản lý tài khoản và hỗ trợ học viên trong quá trình học"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
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

        {listError && (
          <div className="sst-error" role="alert">{listError}</div>
        )}

        {isLoading ? (
          <div className="sst-detail-skel" aria-hidden="true" />
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
                  <td><JlptBadge level={s.currentJlptLevel} /></td>
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
                        >
                          🔒
                        </button>
                      ) : (
                        <button
                          className="sst-btn-activate"
                          onClick={() => openConfirm(s, 'activate')}
                          aria-label={`Kích hoạt ${s.fullName}`}
                          title="Kích hoạt"
                        >
                          🔓
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {totalPages > 1 && (
          <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
        )}
      </main>

      {confirmModal && (
        <SuspendConfirmModal
          modal={confirmModal}
          reason={reason}
          onReasonChange={setReason}
          isActioning={isActioning}
          actionError={actionError}
          onConfirm={handleAction}
          onClose={() => setConfirmModal(null)}
        />
      )}
    </div>
  );
}
