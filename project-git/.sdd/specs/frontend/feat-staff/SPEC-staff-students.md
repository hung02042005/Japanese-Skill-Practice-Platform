# SPEC — Staff Quản Lý Học Viên (`/staff/students`)
> **UC:** UC-21 (Danh sách học viên), UC-22 (Xem tiến độ học viên), UC-23 (Suspend/Activate tài khoản)
> **Sprint:** 5 — Staff Tools
> **Prefix:** `sst-` | **activeTab:** `'staff-students'` | **Guard:** `<StaffRoute>`
> **Backend ref:** `feat-system-admin/SPEC.md UC-21, UC-22, UC-23` (Staff-level scope)
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Phân biệt:** Staff chỉ xem học viên trong phạm vi được giao / toàn bộ — không xóa user (Admin only). Suspend = tạm khóa, không phải xóa (ADR-004 Soft Delete).

---

## 1. MÔ TẢ TRANG

Ba sub-view trong cùng route:
1. **Student List** — Bảng học viên với filter level/status. Click tên → xem chi tiết tiến độ.
2. **Student Detail** — Tiến độ học của 1 học viên: stats tổng quan, lịch sử quiz, streak, subscription.
3. **Confirm Modal** — Xác nhận Suspend / Activate trước khi thực thi.

---

## 2. MOCKUP

### 2.1 Student List
```
┌──────────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-students"                         │
├──────────────────────────────────────────────────────────────────┤
│  [Page Header: "Quản Lý Học Viên"]                              │
│                                                                  │
│  [Search: "Tên, email..."] [Level: Tất cả▼] [Trạng thái: Tất cả▼]│
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Tên         │ Email          │ Level │ Status   │ Hành động│  │
│  ├─────────────┼────────────────┼───────┼──────────┼──────────┤  │
│  │ Nguyễn An   │ an@mail.com    │ N4    │ [Active] │ [Xem] [🔒]│ │
│  │ Trần B      │ b@mail.com     │ N5    │ [Suspend]│ [Xem] [🔓]│ │
│  └──────────────────────────────────────────────────────────┘   │
│  Tổng: 234 học viên   [← 1  2  3  →]                           │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 Student Detail
```
┌──────────────────────────────────────────────────────────────────┐
│  ← Danh sách học viên                                            │
│  Nguyễn An  <an@mail.com>  [N4]  [Active]   [🔒 Suspend]       │
├──────────────────────────────────────────────────────────────────┤
│  Tổng quan                                                       │
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐                │
│  │Streak  │  │Bài học │  │Quiz    │  │Đăng ký │                │
│  │ 12 ngày│  │ 48 done│  │ 82%    │  │  VIP   │                │
│  └────────┘  └────────┘  └────────┘  └────────┘                │
│                                                                  │
│  Lịch sử thi gần đây                                            │
│  ┌────────────────────────────────────────────────┐            │
│  │ Mock N4 – 2026/05/20  78/120  65%  [Xem chi tiết]│          │
│  │ Quiz Từ vựng N4        18/20   90%  [Xem chi tiết]│         │
│  └────────────────────────────────────────────────┘            │
└──────────────────────────────────────────────────────────────────┘
```

### 2.3 Confirm Modal
```
┌──────────────────────────────────────────────────────────────────┐
│                  ⚠️ Xác nhận Suspend                            │
│  Bạn sắp khoá tài khoản của Nguyễn An.                          │
│  Học viên sẽ không thể đăng nhập cho đến khi được kích hoạt lại.│
│                                                                  │
│  Lý do (không bắt buộc):  [____________]                        │
│                                                                  │
│        [Hủy]              [Xác nhận Suspend]                    │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE CẦN TẠO

```
pages/staff/
├── StaffStudents.jsx
└── StaffStudents.css

components/staff/
├── StudentDetailPanel.jsx    ← tiến độ chi tiết 1 học viên
└── SuspendConfirmModal.jsx   ← modal xác nhận suspend/activate
```

---

## 4. STATE

### 4.1 List view
```js
const [view,        setView]      = useState('list');   // 'list' | 'detail'
const [search,      setSearch]    = useState('');
const [debounced,   setDebounced] = useState('');
const [levelFilter, setLevel]     = useState('');       // '' | 'N5'|'N4'|'N3'|'N2'|'N1'
const [statusFilter,setStatus]    = useState('');       // '' | 'active' | 'suspended'
const [students,    setStudents]  = useState([]);
const [isLoading,   setLoading]   = useState(true);
const [error,       setError]     = useState('');
const [page,        setPage]      = useState(1);
const [totalPages,  setTotal]     = useState(1);
const [totalElements, setTotalEl] = useState(0);
const timerRef = useRef(null);
const PAGE_SIZE = 20;
```

### 4.2 Detail view
```js
const [activeStudent, setActiveStudent] = useState(null);  // StudentSummary
const [detail,        setDetail]        = useState(null);  // StudentDetail
const [isLoadingDetail, setLoadingDetail] = useState(false);
```

### 4.3 Confirm Modal
```js
const [confirmModal, setConfirmModal] = useState(null);
// { student, action: 'suspend'|'activate' }
const [reason,       setReason]       = useState('');
const [isActioning,  setActioning]    = useState(false);
```

---

## 5. API CALLS

```js
// GET /api/staff/students?search=an&level=N4&status=active&page=0&size=20
// Response:
{
  "data": {
    "content": [
      {
        "studentId": 101,
        "fullName": "Nguyễn An",
        "email": "an@mail.com",
        "jlptLevel": "N4",
        "status": "active",               // 'active' | 'suspended'
        "subscriptionType": "VIP",        // 'FREE' | 'VIP'
        "createdAt": "2026-01-15T10:00:00Z",
        "lastActiveAt": "2026-06-01T09:30:00Z"
      }
    ],
    "totalElements": 234,
    "totalPages": 12
  }
}

// GET /api/staff/students/{studentId}/progress
// Response:
{
  "data": {
    "studentId": 101,
    "fullName": "Nguyễn An",
    "email": "an@mail.com",
    "jlptLevel": "N4",
    "status": "active",
    "subscriptionType": "VIP",
    "currentStreak": 12,
    "longestStreak": 24,
    "lessonsCompleted": 48,
    "averageQuizScore": 82,
    "recentAttempts": [
      {
        "attemptId": 200,
        "type": "exam",                   // 'exam' | 'quiz'
        "title": "Mock N4 – Tháng 5",
        "score": 78,
        "maxScore": 120,
        "scorePct": 65,
        "takenAt": "2026-05-20T14:00:00Z"
      }
    ]
  }
}

// POST /api/staff/students/{studentId}/suspend
// Request: { "reason": "Vi phạm nội quy" }
// Response: { "data": { "studentId": 101, "status": "suspended" } }

// POST /api/staff/students/{studentId}/activate
// Request: {}
// Response: { "data": { "studentId": 101, "status": "active" } }
```

API service (`staffService.js`):
```js
export async function getStaffStudents({ search, level, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (search) params.search = search;
  if (level)  params.level  = level;
  if (status) params.status = status;
  const res = await api.get('/staff/students', { params });
  return res.data.data;
}

export async function getStudentProgress(studentId) {
  const res = await api.get(`/staff/students/${studentId}/progress`);
  return res.data.data;
}

export async function suspendStudent(studentId, reason = '') {
  const res = await api.post(`/staff/students/${studentId}/suspend`, { reason });
  return res.data.data;
}

export async function activateStudent(studentId) {
  const res = await api.post(`/staff/students/${studentId}/activate`);
  return res.data.data;
}
```

---

## 6. JSX STRUCTURE

```jsx
import { useState, useEffect, useRef, useCallback } from 'react';
import TopNav from '../../components/layout/TopNav';
import { JlptBadge } from '../../components/common/Badges';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import StudentDetailPanel from '../../components/staff/StudentDetailPanel';
import SuspendConfirmModal from '../../components/staff/SuspendConfirmModal';
import { getStaffStudents, getStudentProgress, suspendStudent, activateStudent } from '../../api/staffService';
import './StaffStudents.css';

const LEVELS  = [{ id: '', label: 'Tất cả' }, { id: 'N5', label: 'N5' }, { id: 'N4', label: 'N4' }, { id: 'N3', label: 'N3' }, { id: 'N2', label: 'N2' }, { id: 'N1', label: 'N1' }];
const STATUSES = [{ id: '', label: 'Tất cả' }, { id: 'active', label: 'Active' }, { id: 'suspended', label: 'Suspended' }];

export default function StaffStudents() {
  const [view,         setView]       = useState('list');
  const [search,       setSearch]     = useState('');
  const [debounced,    setDebounced]  = useState('');
  const [levelFilter,  setLevel]      = useState('');
  const [statusFilter, setStatus]     = useState('');
  const [students,     setStudents]   = useState([]);
  const [isLoading,    setLoading]    = useState(true);
  const [error,        setError]      = useState('');
  const [page,         setPage]       = useState(1);
  const [totalPages,   setTotal]      = useState(1);
  const [totalEl,      setTotalEl]    = useState(0);
  const [activeStudent, setActiveStudent] = useState(null);
  const [detail,        setDetail]       = useState(null);
  const [isLoadingDetail, setLoadingDet] = useState(false);
  const [confirmModal,  setConfirmModal] = useState(null);
  const [reason,        setReason]       = useState('');
  const [isActioning,   setActioning]    = useState(false);
  const timerRef = useRef(null);

  // Debounce
  useEffect(() => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(search), 400);
    return () => clearTimeout(timerRef.current);
  }, [search]);

  useEffect(() => { setPage(1); }, [debounced, levelFilter, statusFilter]);

  const fetchStudents = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const data = await getStaffStudents({ search: debounced, level: levelFilter, status: statusFilter, page: page - 1, size: 20 });
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
      // Update local list
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

  // ─── DETAIL VIEW ───
  if (view === 'detail' && activeStudent) {
    return (
      <div className="sst-page">
        <TopNav activeTab="staff-students" />
        <main className="sst-body">
          <button className="sst-back-btn" onClick={() => setView('list')}>← Danh sách học viên</button>
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
              onClick={() => openConfirm(activeStudent, activeStudent.status === 'active' ? 'suspend' : 'activate')}
            >
              {activeStudent.status === 'active' ? '🔒 Suspend' : '🔓 Kích hoạt lại'}
            </button>
          </div>
          {isLoadingDetail ? (
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

  // ─── LIST VIEW ───
  return (
    <div className="sst-page">
      <TopNav activeTab="staff-students" />
      <main className="sst-body">
        <div className="sst-page-header">
          <h1 className="sst-page-title">Quản Lý Học Viên</h1>
          <span className="sst-total-count">{totalEl} học viên</span>
        </div>

        <div className="sst-filters">
          <label className="visually-hidden" htmlFor="sst-search">Tìm học viên</label>
          <input id="sst-search" type="search" className="sst-search" placeholder="Tên, email..."
            value={search} onChange={(e) => setSearch(e.target.value)} />
          <label className="visually-hidden" htmlFor="sst-level-select">Level</label>
          <select id="sst-level-select" className="sst-select" value={levelFilter} onChange={(e) => setLevel(e.target.value)}>
            {LEVELS.map((l) => <option key={l.id} value={l.id}>{l.label}</option>)}
          </select>
          <label className="visually-hidden" htmlFor="sst-status-select">Trạng thái</label>
          <select id="sst-status-select" className="sst-select" value={statusFilter} onChange={(e) => setStatus(e.target.value)}>
            {STATUSES.map((s) => <option key={s.id} value={s.id}>{s.label}</option>)}
          </select>
        </div>

        {error && <div className="sst-error" role="alert">{error}<button className="sst-retry" onClick={fetchStudents}>Thử lại</button></div>}

        {isLoading ? (
          <table className="sst-table" aria-busy="true">
            <thead><tr><th>Tên</th><th>Email</th><th>Level</th><th>Trạng thái</th><th>Hành động</th></tr></thead>
            <tbody>
              {Array.from({ length: 5 }).map((_, i) => (
                <tr key={i}>{Array.from({ length: 5 }).map((__, j) => <td key={j}><div className="sst-cell-skel" aria-hidden="true" /></td>)}</tr>
              ))}
            </tbody>
          </table>
        ) : students.length === 0 ? (
          <EmptyState title="Không có học viên nào" subtitle="Thử thay đổi bộ lọc tìm kiếm." mascotVariant="thinking" mascotSize={120} />
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
                    <button className="sst-name-btn" onClick={() => openDetail(s)}>{s.fullName}</button>
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
                      <button className="sst-btn-icon" onClick={() => openDetail(s)} aria-label={`Xem tiến độ ${s.fullName}`} title="Xem tiến độ">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="3"/><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/></svg>
                      </button>
                      {s.status === 'active' ? (
                        <button className="sst-btn-suspend" onClick={() => openConfirm(s, 'suspend')} aria-label={`Suspend ${s.fullName}`} title="Suspend">🔒</button>
                      ) : (
                        <button className="sst-btn-activate" onClick={() => openConfirm(s, 'activate')} aria-label={`Kích hoạt ${s.fullName}`} title="Kích hoạt">🔓</button>
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
```

### StudentDetailPanel component

```jsx
// components/staff/StudentDetailPanel.jsx
import { JlptBadge } from '../common/Badges';

export default function StudentDetailPanel({ detail }) {
  return (
    <div className="sst-detail-body">
      {/* Stats */}
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
          <div className="sst-stat-value">{detail.subscriptionType}</div>
          <div className="sst-stat-label">Gói đăng ký</div>
        </div>
      </div>

      {/* Recent attempts */}
      {detail.recentAttempts?.length > 0 && (
        <div className="sst-recent">
          <h3 className="sst-recent-title">Lịch sử thi gần đây</h3>
          <table className="sst-attempts-table">
            <thead>
              <tr><th>Bài thi / Quiz</th><th>Điểm</th><th>%</th><th>Ngày</th></tr>
            </thead>
            <tbody>
              {detail.recentAttempts.map((a) => (
                <tr key={a.attemptId}>
                  <td>{a.title}</td>
                  <td>{a.score}/{a.maxScore}</td>
                  <td className={a.scorePct >= 70 ? 'sst-score--pass' : 'sst-score--fail'}>{a.scorePct}%</td>
                  <td>{new Date(a.takenAt).toLocaleDateString('vi-VN')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
```

### SuspendConfirmModal component

```jsx
// components/staff/SuspendConfirmModal.jsx
import { useEffect } from 'react';

export default function SuspendConfirmModal({ modal, reason, onReasonChange, isActioning, onConfirm, onClose }) {
  const { student, action } = modal;
  const isSuspend = action === 'suspend';

  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  return (
    <div className="sst-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="sst-modal-title" onClick={onClose}>
      <div className="sst-modal" onClick={(e) => e.stopPropagation()}>
        <div className="sst-modal-icon">{isSuspend ? '⚠️' : '✅'}</div>
        <h3 id="sst-modal-title" className="sst-modal-title">
          {isSuspend ? 'Xác nhận Suspend' : 'Kích hoạt lại tài khoản'}
        </h3>
        <p className="sst-modal-desc">
          {isSuspend
            ? `Bạn sắp khoá tài khoản của ${student.fullName}. Học viên sẽ không thể đăng nhập cho đến khi được kích hoạt lại.`
            : `Bạn sắp kích hoạt lại tài khoản của ${student.fullName}. Học viên sẽ có thể đăng nhập bình thường.`}
        </p>
        {isSuspend && (
          <div className="sst-modal-reason">
            <label htmlFor="sst-reason-input" className="sst-modal-reason-label">Lý do (không bắt buộc):</label>
            <input
              id="sst-reason-input"
              type="text"
              className="sst-modal-reason-input"
              placeholder="Nhập lý do..."
              value={reason}
              onChange={(e) => onReasonChange(e.target.value)}
              maxLength={255}
            />
          </div>
        )}
        <div className="sst-modal-actions">
          <button className="sst-modal-btn-cancel" onClick={onClose} disabled={isActioning}>Hủy</button>
          <button
            className={`sst-modal-btn-confirm${isSuspend ? ' sst-modal-btn-confirm--danger' : ''}`}
            onClick={onConfirm}
            disabled={isActioning}
            aria-label={isSuspend ? `Xác nhận suspend ${student.fullName}` : `Xác nhận kích hoạt ${student.fullName}`}
          >
            {isActioning ? 'Đang xử lý...' : isSuspend ? 'Xác nhận Suspend' : 'Kích hoạt lại'}
          </button>
        </div>
      </div>
    </div>
  );
}
```

---

## 7. CSS

```css
/* ===== Staff Students ===== */
.sst-page { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.sst-body { flex: 1; max-width: 1200px; width: 100%; margin: 0 auto; padding: 28px 32px 48px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

.sst-page-header  { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.sst-page-title   { font-size: 24px; font-weight: 700; color: var(--color-text); margin: 0; }
.sst-total-count  { font-size: 14px; color: var(--color-text-sub); }

/* Filters */
.sst-filters { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.sst-search  { flex: 1; min-width: 240px; height: 40px; padding: 0 14px; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; background: var(--color-bg); }
.sst-select  { height: 40px; padding: 0 12px; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; background: var(--color-bg); min-width: 140px; }

.sst-error { display: flex; align-items: center; justify-content: space-between; gap: 10px; background: #FFEAEA; border: 1px solid var(--color-error); border-radius: var(--radius-md); padding: 12px 16px; font-size: 13px; color: var(--color-error); }
.sst-retry { background: transparent; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 12px; font-weight: 700; padding: 4px 12px; cursor: pointer; }

/* Table */
.sst-table { width: 100%; border-collapse: collapse; background: var(--color-card); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); overflow: hidden; }
.sst-table th { font-size: 12px; font-weight: 700; color: var(--color-text-sub); padding: 12px 16px; text-align: left; border-bottom: 1px solid var(--color-border); background: #FAFAFA; }
.sst-table td { font-size: 14px; color: var(--color-text); padding: 14px 16px; border-bottom: 1px solid var(--color-border); vertical-align: middle; }
.sst-table tr:last-child td { border-bottom: none; }
.sst-table tr:hover td { background: var(--color-primary-bg); }

.sst-td-name  { }
.sst-td-email { color: var(--color-text-sub); font-size: 13px; }
.sst-name-btn { background: transparent; border: none; color: var(--color-primary); font-size: 14px; font-weight: 600; cursor: pointer; padding: 0; text-decoration: underline; text-underline-offset: 3px; }
.sst-name-btn:hover { color: var(--color-primary-dark); }

/* Status badge */
.sst-status-badge { font-size: 12px; font-weight: 700; padding: 3px 10px; border-radius: var(--radius-full); }
.sst-status-badge--active    { background: var(--color-secondary-bg); color: var(--color-secondary); }
.sst-status-badge--suspended { background: #FFEAEA; color: var(--color-error); }

/* Action buttons */
.sst-td-actions { display: flex; gap: 8px; align-items: center; }
.sst-btn-icon   { width: 32px; height: 32px; border-radius: var(--radius-sm); border: 1.5px solid var(--color-border); background: transparent; cursor: pointer; display: flex; align-items: center; justify-content: center; color: var(--color-text-sub); transition: all var(--transition); }
.sst-btn-icon:hover { background: var(--color-primary-bg); border-color: var(--color-primary-light); color: var(--color-primary); }
.sst-btn-suspend  { height: 32px; padding: 0 12px; background: #FFF3F0; border: 1.5px solid var(--color-error); border-radius: var(--radius-full); color: var(--color-error); font-size: 13px; cursor: pointer; }
.sst-btn-activate { height: 32px; padding: 0 12px; background: var(--color-secondary-bg); border: 1.5px solid var(--color-secondary); border-radius: var(--radius-full); color: var(--color-secondary); font-size: 13px; cursor: pointer; }

/* Skeleton */
.sst-cell-skel { height: 18px; border-radius: var(--radius-sm); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }

/* Detail view */
.sst-back-btn { background: transparent; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); color: var(--color-text-sub); font-size: 13px; font-weight: 600; padding: 6px 14px; cursor: pointer; align-self: flex-start; transition: all var(--transition); }
.sst-back-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }

.sst-detail-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.sst-detail-info   { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.sst-detail-name   { font-size: 22px; font-weight: 700; color: var(--color-text); margin: 0; }
.sst-detail-email  { font-size: 14px; color: var(--color-text-sub); }

.sst-action-btn          { height: 38px; padding: 0 18px; border-radius: var(--radius-full); font-size: 13px; font-weight: 700; cursor: pointer; border: 1.5px solid; }
.sst-action-btn--suspend  { background: #FFF3F0; border-color: var(--color-error); color: var(--color-error); }
.sst-action-btn--activate { background: var(--color-secondary-bg); border-color: var(--color-secondary); color: var(--color-secondary); }

.sst-detail-skel { height: 280px; border-radius: var(--radius-lg); background: linear-gradient(90deg, #f0ebe8 25%, #f8f4f2 50%, #f0ebe8 75%); background-size: 200% 100%; animation: skelPulse 1.4s ease infinite; }

/* Stats grid */
.sst-stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.sst-stat-card  { background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: var(--radius-lg); padding: 20px; text-align: center; }
.sst-stat-value { font-size: 28px; font-weight: 800; color: var(--color-primary); }
.sst-stat-label { font-size: 12px; color: var(--color-text-sub); margin-top: 4px; }

/* Recent attempts */
.sst-recent       { display: flex; flex-direction: column; gap: 10px; }
.sst-recent-title { font-size: 16px; font-weight: 700; color: var(--color-text); margin: 0; }
.sst-attempts-table { width: 100%; border-collapse: collapse; background: var(--color-card); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }
.sst-attempts-table th { font-size: 12px; font-weight: 700; color: var(--color-text-sub); padding: 10px 14px; background: #FAFAFA; border-bottom: 1px solid var(--color-border); text-align: left; }
.sst-attempts-table td { font-size: 13px; padding: 12px 14px; border-bottom: 1px solid var(--color-border); }
.sst-attempts-table tr:last-child td { border-bottom: none; }
.sst-score--pass { color: var(--color-secondary); font-weight: 700; }
.sst-score--fail { color: var(--color-error); font-weight: 700; }

/* Confirm Modal */
.sst-modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 16px; }
.sst-modal { background: var(--color-card); border-radius: var(--radius-xl); padding: 32px 28px 24px; max-width: 440px; width: 100%; display: flex; flex-direction: column; gap: 16px; box-shadow: var(--shadow-xl); }
.sst-modal-icon  { font-size: 36px; text-align: center; }
.sst-modal-title { font-size: 20px; font-weight: 700; color: var(--color-text); margin: 0; text-align: center; }
.sst-modal-desc  { font-size: 14px; color: var(--color-text-sub); line-height: 1.6; margin: 0; text-align: center; }
.sst-modal-reason       { display: flex; flex-direction: column; gap: 6px; }
.sst-modal-reason-label { font-size: 13px; font-weight: 600; color: var(--color-text); }
.sst-modal-reason-input { height: 40px; padding: 0 12px; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; background: var(--color-bg); }
.sst-modal-actions      { display: flex; gap: 10px; justify-content: flex-end; }
.sst-modal-btn-cancel   { height: 40px; padding: 0 20px; background: transparent; border: 1.5px solid var(--color-border); border-radius: var(--radius-full); font-size: 14px; font-weight: 600; color: var(--color-text-sub); cursor: pointer; }
.sst-modal-btn-confirm  { height: 40px; padding: 0 20px; background: var(--color-primary); color: white; border: none; border-radius: var(--radius-full); font-size: 14px; font-weight: 700; cursor: pointer; }
.sst-modal-btn-confirm--danger { background: var(--color-error); }
.sst-modal-btn-confirm:disabled, .sst-modal-btn-cancel:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 1199px) { .sst-stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 767px)  {
  .sst-body    { padding: 16px 16px 32px; }
  .sst-filters { flex-direction: column; align-items: stretch; }
  .sst-stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (prefers-reduced-motion: reduce) { .sst-page * { animation: none !important; transition-duration: 0ms !important; } }
```

---

## 8. ACCESSIBILITY

- [ ] Bảng học viên có `<caption className="visually-hidden">` mô tả
- [ ] Modal có `role="dialog"`, `aria-modal="true"`, `aria-labelledby`, Escape đóng, focus trap
- [ ] Nút Suspend / Activate có `aria-label` chứa tên học viên
- [ ] Bảng trạng thái loading dùng `aria-busy="true"`
- [ ] `<select>` filter có `<label>` ẩn liên kết bằng `htmlFor`

## 9. GHI CHÚ QUAN TRỌNG

- **Suspend ≠ Delete.** Backend dùng soft-delete flag `status = 'suspended'` (ADR-004). Frontend không được gọi bất kỳ API xóa nào.
- Staff **không thể xóa** học viên — chức năng này chỉ dành cho Admin (UC-37).
- Confirm modal bắt buộc trước mọi thao tác suspend/activate (LESSON-001).
- `reason` lưu vào audit log phía backend — frontend chỉ gửi string tùy chọn.
