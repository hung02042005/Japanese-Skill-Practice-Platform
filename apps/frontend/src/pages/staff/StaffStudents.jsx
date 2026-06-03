import { useState, useEffect, useRef, useCallback } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import StaffPageHero from '../../components/staff/StaffPageHero';
import { JlptBadge } from '../../components/common/Badges';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import StudentDetailPanel from '../../components/staff/StudentDetailPanel';
import SuspendConfirmModal from '../../components/staff/SuspendConfirmModal';
import './StaffStudents.css';

// ─── Mock Data ────────────────────────────────────────────────────────────────

const MOCK_STUDENTS = [
  { studentId: 1,  fullName: 'Nguyễn Văn An',    email: 'nguyenvanan@email.com',   jlptLevel: 'N3', status: 'active',    subscription: 'VIP' },
  { studentId: 2,  fullName: 'Trần Thị Bảo',     email: 'tranthbao@email.com',     jlptLevel: 'N4', status: 'active',    subscription: 'FREE' },
  { studentId: 3,  fullName: 'Lê Minh Cường',    email: 'leminhcuong@email.com',   jlptLevel: 'N2', status: 'suspended', subscription: 'VIP' },
  { studentId: 4,  fullName: 'Phạm Thị Dung',    email: 'phamthidung@email.com',   jlptLevel: 'N5', status: 'active',    subscription: 'FREE' },
  { studentId: 5,  fullName: 'Hoàng Văn Em',     email: 'hoangvanem@email.com',    jlptLevel: 'N4', status: 'active',    subscription: 'VIP' },
  { studentId: 6,  fullName: 'Vũ Thanh Giang',   email: 'vuthanhhgiang@email.com', jlptLevel: 'N1', status: 'active',    subscription: 'VIP' },
  { studentId: 7,  fullName: 'Đặng Thị Hà',      email: 'dangthiha@email.com',     jlptLevel: 'N5', status: 'suspended', subscription: 'FREE' },
  { studentId: 8,  fullName: 'Bùi Văn Hùng',     email: 'buivanhung@email.com',    jlptLevel: 'N3', status: 'active',    subscription: 'FREE' },
  { studentId: 9,  fullName: 'Ngô Thị Lan',      email: 'ngothilan@email.com',     jlptLevel: 'N4', status: 'active',    subscription: 'VIP' },
  { studentId: 10, fullName: 'Trịnh Minh Khoa',  email: 'trinhminhkhoa@email.com', jlptLevel: 'N3', status: 'active',    subscription: 'FREE' },
  { studentId: 11, fullName: 'Đinh Thị Mai',     email: 'dinhthimai@email.com',    jlptLevel: 'N2', status: 'active',    subscription: 'VIP' },
  { studentId: 12, fullName: 'Lý Văn Nam',       email: 'lyvannam@email.com',      jlptLevel: 'N5', status: 'active',    subscription: 'FREE' },
];

const MOCK_PROGRESS_MAP = {
  1: {
    jlptLevel: 'N3', currentStreak: 14, lessonsCompleted: 42, averageQuizScore: 78,
    recentAttempts: [
      { attemptId: 1, title: 'Quiz Từ Vựng N3 Bài 5',    score: 18, maxScore: 20, scorePct: 90, takenAt: '2026-06-01T10:00:00' },
      { attemptId: 2, title: 'Mock Test JLPT N3 — Đề 01', score: 85, maxScore: 110, scorePct: 77, takenAt: '2026-05-28T14:00:00' },
      { attemptId: 3, title: 'Quiz Ngữ Pháp N3',          score: 12, maxScore: 20, scorePct: 60, takenAt: '2026-05-25T09:00:00' },
    ],
  },
  2: {
    jlptLevel: 'N4', currentStreak: 5, lessonsCompleted: 28, averageQuizScore: 65,
    recentAttempts: [
      { attemptId: 4, title: 'Quiz Từ Vựng N4 Bài 2',    score: 13, maxScore: 20, scorePct: 65, takenAt: '2026-06-02T09:00:00' },
      { attemptId: 5, title: 'Quiz Kanji N4',             score: 14, maxScore: 20, scorePct: 70, takenAt: '2026-05-30T11:00:00' },
    ],
  },
  3: {
    jlptLevel: 'N2', currentStreak: 0, lessonsCompleted: 67, averageQuizScore: 82,
    recentAttempts: [
      { attemptId: 6, title: 'Mock Test JLPT N2 — Đề 02', score: 120, maxScore: 150, scorePct: 80, takenAt: '2026-05-20T15:00:00' },
    ],
  },
  4: {
    jlptLevel: 'N5', currentStreak: 3, lessonsCompleted: 12, averageQuizScore: 55,
    recentAttempts: [
      { attemptId: 7, title: 'Quiz Hiragana cơ bản',      score: 11, maxScore: 20, scorePct: 55, takenAt: '2026-06-03T08:00:00' },
    ],
  },
  5: {
    jlptLevel: 'N4', currentStreak: 22, lessonsCompleted: 35, averageQuizScore: 88,
    recentAttempts: [
      { attemptId: 8, title: 'Mock Test JLPT N4 Vol.2',   score: 95, maxScore: 110, scorePct: 86, takenAt: '2026-06-02T16:00:00' },
      { attemptId: 9, title: 'Quiz Ngữ Pháp N4 ～て形',   score: 18, maxScore: 20, scorePct: 90, takenAt: '2026-05-30T13:00:00' },
    ],
  },
  6: {
    jlptLevel: 'N1', currentStreak: 60, lessonsCompleted: 120, averageQuizScore: 91,
    recentAttempts: [
      { attemptId: 10, title: 'Mock Test JLPT N1 — Đề 01', score: 155, maxScore: 180, scorePct: 86, takenAt: '2026-06-01T14:00:00' },
    ],
  },
};

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
  const [students,      setStudents]    = useState(MOCK_STUDENTS);
  const [page,          setPage]        = useState(1);
  const [activeStudent, setActiveStudent] = useState(null);
  const [detail,        setDetail]        = useState(null);
  const [isLoadingDet,  setLoadingDet]    = useState(false);
  const [confirmModal,  setConfirmModal]  = useState(null);
  const [reason,        setReason]        = useState('');
  const [isActioning,   setActioning]     = useState(false);
  const timerRef = useRef(null);

  // Debounce search
  useEffect(() => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(search), 300);
    return () => clearTimeout(timerRef.current);
  }, [search]);

  useEffect(() => { setPage(1); }, [debounced, levelFilter, statusFilter]);

  // Client-side filter
  const filtered = students.filter((s) => {
    const q = debounced.toLowerCase();
    if (q && !s.fullName.toLowerCase().includes(q) && !s.email.toLowerCase().includes(q)) return false;
    if (levelFilter && s.jlptLevel !== levelFilter) return false;
    if (statusFilter && s.status !== statusFilter) return false;
    return true;
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage   = Math.min(page, totalPages);
  const pageSlice  = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const openDetail = useCallback((student) => {
    setActiveStudent(student);
    setLoadingDet(true);
    setDetail(null);
    setView('detail');
    setTimeout(() => {
      setDetail(MOCK_PROGRESS_MAP[student.studentId] ?? null);
      setLoadingDet(false);
    }, 300);
  }, []);

  const openConfirm = useCallback((student, action) => {
    setReason('');
    setConfirmModal({ student, action });
  }, []);

  const handleAction = useCallback(async () => {
    if (!confirmModal || isActioning) return;
    setActioning(true);
    const { student, action } = confirmModal;
    await new Promise((res) => setTimeout(res, 500));
    const newStatus = action === 'suspend' ? 'suspended' : 'active';
    setStudents((prev) =>
      prev.map((s) => s.studentId === student.studentId ? { ...s, status: newStatus } : s)
    );
    if (activeStudent?.studentId === student.studentId) {
      setActiveStudent((prev) => ({ ...prev, status: newStatus }));
    }
    setConfirmModal(null);
    setActioning(false);
  }, [confirmModal, isActioning, activeStudent]);

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
            <div className="sst-error" role="alert">Không có dữ liệu tiến độ cho học viên này.</div>
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
          <span className="sst-total-count">{filtered.length} học viên</span>
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

        {pageSlice.length === 0 ? (
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
              {pageSlice.map((s) => (
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
          <Pagination currentPage={safePage} totalPages={totalPages} onChange={setPage} />
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
