import { useState, useEffect, useMemo, useCallback } from 'react';
import AdminTopNav from '../../components/layout/AdminTopNav';
import SakuChan from '../../components/auth/SakuChan';
import { fetchAdminUsers, updateUserStatus, updateUserRole } from '../../api/adminService';
import './ManageUsers.css';

/* ── Constants ── */
const PAGE_SIZE = 7;

const ROLE_LABELS   = { STUDENT: 'Học viên', STAFF: 'Nhân viên', ADMIN: 'Quản trị' };
const STATUS_LABELS = { ACTIVE: 'Hoạt động', BANNED: 'Bị khóa' };

/* ── Helpers ── */
function formatDate(d) {
  const dt = new Date(d);
  return `${dt.getDate().toString().padStart(2, '0')}/${(dt.getMonth() + 1).toString().padStart(2, '0')}/${dt.getFullYear()}`;
}

/* ═══════════════════════════════════════
   SUB-COMPONENTS
═══════════════════════════════════════ */

/* Floating petal decorations */
function PetalDecoration() {
  return (
    <div className="mu-petals" aria-hidden="true">
      <svg className="mu-petal mu-petal--1" width="32" height="32" viewBox="0 0 32 32">
        <ellipse cx="16" cy="16" rx="7" ry="14" fill="#E8637A" opacity="0.10" transform="rotate(-20 16 16)"/>
        <ellipse cx="16" cy="16" rx="7" ry="14" fill="#F4A7B3" opacity="0.08" transform="rotate(20 16 16)"/>
      </svg>
      <svg className="mu-petal mu-petal--2" width="22" height="22" viewBox="0 0 22 22">
        <ellipse cx="11" cy="11" rx="5" ry="10" fill="#F4A7B3" opacity="0.14" transform="rotate(15 11 11)"/>
      </svg>
      <svg className="mu-petal mu-petal--3" width="26" height="26" viewBox="0 0 26 26">
        <ellipse cx="13" cy="13" rx="6" ry="12" fill="#E8637A" opacity="0.09" transform="rotate(-30 13 13)"/>
      </svg>
      <svg className="mu-petal mu-petal--4" width="18" height="18" viewBox="0 0 18 18">
        <ellipse cx="9" cy="9" rx="4" ry="8" fill="#F4A7B3" opacity="0.16" transform="rotate(10 9 9)"/>
      </svg>
      <svg className="mu-petal mu-petal--5" width="14" height="14" viewBox="0 0 14 14">
        <ellipse cx="7" cy="7" rx="3" ry="6" fill="#E8637A" opacity="0.12" transform="rotate(-45 7 7)"/>
      </svg>
    </div>
  );
}

function RoleBadge({ role }) {
  const icon = role === 'STUDENT' ? '📚' : role === 'STAFF' ? '🌿' : '👑';
  return (
    <span className={`mu-badge mu-badge--role mu-badge--role-${role.toLowerCase()}`}>
      {icon} {ROLE_LABELS[role] ?? role}
    </span>
  );
}

function JlptBadge({ level }) {
  if (!level) return null;
  return <span className={`mu-badge jlpt-${level.toLowerCase()}`}>{level}</span>;
}

function StatusBadge({ status }) {
  return (
    <span className={`mu-badge mu-badge--status mu-badge--${status.toLowerCase()}`}>
      <span className={`mu-status-dot mu-status-dot--${status.toLowerCase()}`} />
      {STATUS_LABELS[status] ?? status}
    </span>
  );
}

function SubBadge({ sub }) {
  if (!sub) return null;
  return (
    <span className={`mu-badge mu-badge--sub mu-badge--sub-${sub.toLowerCase()}`}>
      {sub === 'VIP' ? '⭐ VIP' : 'FREE'}
    </span>
  );
}

function UserAvatar({ user }) {
  const initial = user.fullName?.charAt(0)?.toUpperCase() ?? '?';
  return (
    <div className={`mu-avatar mu-avatar--${user.role.toLowerCase()}`} aria-hidden="true">
      {initial}
      {user.status === 'ACTIVE' && <span className="mu-avatar-dot" />}
    </div>
  );
}

/* Skeleton */
function SkeletonRow() {
  return (
    <tr className="mu-skel-row" aria-hidden="true">
      <td><div className="mu-skel mu-skel--xs" /></td>
      <td>
        <div className="mu-skel-user">
          <div className="mu-skel mu-skel--avatar" />
          <div>
            <div className="mu-skel mu-skel--name" />
            <div className="mu-skel mu-skel--email" />
          </div>
        </div>
      </td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td><div className="mu-skel mu-skel--sm" /></td>
      <td><div className="mu-skel mu-skel--md" /></td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td><div className="mu-skel mu-skel--xs mu-skel--circle" /></td>
    </tr>
  );
}

/* Toast */
function ToastContainer({ toasts, onRemove }) {
  if (toasts.length === 0) return null;
  return (
    <div className="mu-toasts" aria-live="polite">
      {toasts.map((t) => (
        <div key={t.id} className={`mu-toast mu-toast--${t.type}`} role="alert">
          <span className="mu-toast-icon" aria-hidden="true">
            {t.type === 'success' && '✅'}
            {t.type === 'error'   && '❌'}
            {t.type === 'info'    && '💬'}
          </span>
          <span className="mu-toast-msg">{t.message}</span>
          <button type="button" className="mu-toast-close" onClick={() => onRemove(t.id)} aria-label="Đóng">×</button>
        </div>
      ))}
    </div>
  );
}

/* Confirm modal (ban / activate) */
function ConfirmModal({ modal, onConfirm, onClose, isSubmitting }) {
  if (!modal.open) return null;
  const isBan = modal.action === 'ban';
  return (
    <div className="mu-overlay" role="dialog" aria-modal="true" aria-labelledby="confirm-title" onClick={onClose}>
      <div className="mu-modal" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="mu-modal-x" onClick={onClose} aria-label="Đóng">×</button>
        <div className="mu-modal-mascot">
          <SakuChan variant={isBan ? 'thinking' : 'happy'} size={84} />
        </div>
        <h2 id="confirm-title" className="mu-modal-title">
          {isBan ? '🔒 Khóa tài khoản?' : '✅ Mở khóa tài khoản?'}
        </h2>
        <p className="mu-modal-desc">
          {isBan
            ? <>Bạn có chắc muốn khóa tài khoản của <strong>"{modal.userName}"</strong>? Học viên sẽ không thể đăng nhập cho đến khi mở khóa.</>
            : <>Mở khóa tài khoản cho <strong>"{modal.userName}"</strong>? Học viên sẽ có thể đăng nhập và học tập trở lại.</>
          }
        </p>
        <div className="mu-modal-row">
          <button type="button" className="mu-btn mu-btn--ghost" onClick={onClose}>Huỷ bỏ</button>
          <button
            type="button"
            className={`mu-btn ${isBan ? 'mu-btn--danger' : 'mu-btn--success'}`}
            onClick={onConfirm}
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Đang xử lý...' : isBan ? 'Khóa ngay' : 'Mở khóa'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* Edit role modal */
function EditRoleModal({ modal, onConfirm, onClose, isSubmitting }) {
  const [selected, setSelected] = useState('STUDENT');

  useEffect(() => {
    if (modal.open) setSelected(modal.currentRole ?? 'STUDENT');
  }, [modal.open, modal.currentRole]);

  if (!modal.open) return null;

  const ROLE_OPTS = [
    { value: 'STUDENT', icon: '📚', label: 'Học viên',  desc: 'Học và luyện thi JLPT N5→N1' },
    { value: 'STAFF',   icon: '🌿', label: 'Nhân viên', desc: 'Quản lý và cập nhật nội dung khoá học' },
    { value: 'ADMIN',   icon: '👑', label: 'Quản trị',  desc: 'Toàn quyền quản trị hệ thống' },
  ];

  return (
    <div className="mu-overlay" role="dialog" aria-modal="true" aria-labelledby="edit-role-title" onClick={onClose}>
      <div className="mu-modal mu-modal--edit" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="mu-modal-x" onClick={onClose} aria-label="Đóng">×</button>
        <div className="mu-modal-mascot">
          <SakuChan variant="idle" size={72} />
        </div>
        <h2 id="edit-role-title" className="mu-modal-title">✏️ Đổi vai trò</h2>
        <p className="mu-modal-desc">
          Thay đổi vai trò cho <strong>"{modal.userName}"</strong>
        </p>
        <div className="mu-role-opts">
          {ROLE_OPTS.map((r) => (
            <label
              key={r.value}
              className={`mu-role-opt${selected === r.value ? ' mu-role-opt--on' : ''}`}
            >
              <input
                type="radio"
                name="edit-role"
                value={r.value}
                checked={selected === r.value}
                onChange={() => setSelected(r.value)}
                className="mu-role-radio"
              />
              <span className="mu-role-opt-icon">{r.icon}</span>
              <div className="mu-role-opt-body">
                <span className="mu-role-opt-label">{r.label}</span>
                <span className="mu-role-opt-desc">{r.desc}</span>
              </div>
              {selected === r.value && (
                <svg className="mu-role-check" width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="12" cy="12" r="11" fill="var(--color-primary)" opacity="0.12"/>
                  <polyline points="7 12 10.5 15.5 17 8.5" stroke="var(--color-primary)" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              )}
            </label>
          ))}
        </div>
        <div className="mu-modal-row">
          <button type="button" className="mu-btn mu-btn--ghost" onClick={onClose} disabled={isSubmitting}>Huỷ</button>
          <button type="button" className="mu-btn mu-btn--primary" onClick={() => onConfirm(selected)} disabled={isSubmitting}>
            {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ═══════════════════════════════════════
   MAIN PAGE
═══════════════════════════════════════ */
function ManageUsers() {
  const [users, setUsers]           = useState([]);
  const [isLoading, setIsLoading]   = useState(true);
  const [search, setSearch]         = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [statusFilter, setStatus]   = useState('ALL');
  const [jlptFilter, setJlpt]       = useState('ALL');
  const [page, setPage]             = useState(1);
  const [openMenuId, setOpenMenuId] = useState(null);
  const [confirmModal, setConfirm]    = useState({ open: false, userId: null, userType: null, userName: '', action: 'ban' });
  const [editModal, setEditModal]     = useState({ open: false, userId: null, userType: null, userName: '', currentRole: 'STUDENT' });
  const [toasts, setToasts]           = useState([]);
  const [isSubmitting, setSubmitting] = useState(false);

  /* Toast helpers — defined first so they can be used in effects below */
  const addToast = useCallback((type, message) => {
    const id = Date.now() + Math.random();
    setToasts((p) => [...p, { id, type, message }]);
    setTimeout(() => setToasts((p) => p.filter((t) => t.id !== id)), 3200);
  }, []);

  const removeToast = useCallback((id) => setToasts((p) => p.filter((t) => t.id !== id)), []);

  /* Load users from API */
  useEffect(() => {
    let cancelled = false;
    setIsLoading(true);
    fetchAdminUsers()
      .then((data) => { if (!cancelled) { setUsers(data); setIsLoading(false); } })
      .catch(() => {
        if (!cancelled) {
          setIsLoading(false);
          addToast('error', 'Không thể tải danh sách người dùng, vui lòng thử lại!');
        }
      });
    return () => { cancelled = true; };
  }, [addToast]);

  /* Close action menu on Escape */
  useEffect(() => {
    function onKey(e) { if (e.key === 'Escape') { setOpenMenuId(null); } }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, []);

  /* Computed stats */
  const stats = useMemo(() => {
    const total    = users.length;
    const active   = users.filter((u) => u.status === 'ACTIVE').length;
    const banned   = users.filter((u) => u.status === 'BANNED').length;
    const now = new Date();
    const newMonth = users.filter((u) => {
      const d = new Date(u.createdAt);
      return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth();
    }).length;
    return { total, active, banned, newMonth };
  }, [users]);

  /* Filtered users */
  const filtered = useMemo(() => {
    const q = search.toLowerCase().trim();
    return users.filter((u) => {
      if (q && !u.fullName.toLowerCase().includes(q) && !u.email.toLowerCase().includes(q)) return false;
      if (roleFilter !== 'ALL'   && u.role      !== roleFilter)   return false;
      if (statusFilter !== 'ALL' && u.status    !== statusFilter)  return false;
      if (jlptFilter !== 'ALL'   && u.jlptLevel !== jlptFilter)    return false;
      return true;
    });
  }, [users, search, roleFilter, statusFilter, jlptFilter]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage   = Math.min(page, totalPages);
  const pageSlice  = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  /* Reset page on filter change */
  useEffect(() => { setPage(1); }, [search, roleFilter, statusFilter, jlptFilter]);

  /* Action handlers */
  function openBan(userId) {
    const u = users.find((x) => x.id === userId);
    setConfirm({ open: true, userId, userType: u?.userType, userName: u?.fullName ?? '', action: 'ban' });
    setOpenMenuId(null);
  }

  function openActivate(userId) {
    const u = users.find((x) => x.id === userId);
    setConfirm({ open: true, userId, userType: u?.userType, userName: u?.fullName ?? '', action: 'activate' });
    setOpenMenuId(null);
  }

  async function handleConfirmStatus() {
    const { userId, userType, action } = confirmModal;
    setSubmitting(true);
    try {
      const updated = await updateUserStatus(userType, userId, action === 'ban' ? 'BAN' : 'ACTIVATE');
      setUsers((p) => p.map((u) => (u.id === userId && u.userType === userType ? { ...u, ...updated } : u)));
      addToast('success', action === 'ban' ? 'Đã khóa tài khoản thành công!' : 'Đã mở khóa tài khoản!');
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally {
      setSubmitting(false);
      setConfirm({ open: false, userId: null, userType: null, userName: '', action: 'ban' });
    }
  }

  function openEditRole(userId) {
    const u = users.find((x) => x.id === userId);
    setEditModal({ open: true, userId, userType: u?.userType, userName: u?.fullName ?? '', currentRole: u?.role ?? 'STUDENT' });
    setOpenMenuId(null);
  }

  async function handleConfirmRole(newRole) {
    const { userId, userType } = editModal;
    setSubmitting(true);
    try {
      const updated = await updateUserRole(userType, userId, newRole);
      setUsers((p) => p.map((u) => (u.id === userId && u.userType === userType ? { ...u, ...updated } : u)));
      addToast('success', `Đã cập nhật vai trò thành ${ROLE_LABELS[newRole]}!`);
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally {
      setSubmitting(false);
      setEditModal({ open: false, userId: null, userType: null, userName: '', currentRole: 'STUDENT' });
    }
  }

  /* ── Render ── */
  return (
    /* Clicking outside any action menu closes it */
    <div className="mu-page" onClick={() => setOpenMenuId(null)}>
      <AdminTopNav activeTab="manage-users" />

      {/* ── Header ── */}
      <div className="mu-header">
        <PetalDecoration />
        <div className="mu-header-inner">
          <div className="mu-header-text">
            <span className="mu-header-chip">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                <circle cx="9" cy="7" r="4" stroke="currentColor" strokeWidth="2"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              Quản trị viên
            </span>
            <h1 className="mu-header-title">Quản lý Người Dùng</h1>
            <p className="mu-header-sub">Theo dõi, phân quyền và quản lý toàn bộ tài khoản trên hệ thống</p>
          </div>
          <div className="mu-header-mascot" aria-hidden="true">
            <SakuChan variant="happy" size={100} />
          </div>
        </div>
      </div>

      {/* ── Body ── */}
      <div className="mu-body">

        {/* Stats row */}
        <div className="mu-stats-row">
          {[
            { key: 'total',    icon: '👥', value: stats.total,    label: 'Tổng người dùng',    mod: 'total'  },
            { key: 'active',   icon: '✅', value: stats.active,   label: 'Đang hoạt động',     mod: 'active' },
            { key: 'banned',   icon: '🔒', value: stats.banned,   label: 'Bị khóa tài khoản',  mod: 'banned' },
            { key: 'newMonth', icon: '🌸', value: stats.newMonth, label: 'Mới trong tháng',     mod: 'new'    },
          ].map((s) => (
            <div key={s.key} className={`mu-stat mu-stat--${s.mod}`}>
              <div className="mu-stat-icon-wrap">{s.icon}</div>
              <div className="mu-stat-body">
                <div className="mu-stat-val">{isLoading ? '–' : s.value}</div>
                <div className="mu-stat-lbl">{s.label}</div>
              </div>
            </div>
          ))}
        </div>

        {/* Filter bar */}
        <div className="mu-filter-bar">
          <div className="mu-search">
            <svg className="mu-search-ic" width="17" height="17" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2"/>
              <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            <input
              type="text"
              className="mu-search-input"
              placeholder="Tìm theo tên hoặc email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              aria-label="Tìm kiếm người dùng"
            />
            {search && (
              <button type="button" className="mu-search-clear" onClick={() => setSearch('')} aria-label="Xoá">
                ×
              </button>
            )}
          </div>

          <div className="mu-selects">
            <select className="mu-sel" value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)} aria-label="Lọc vai trò">
              <option value="ALL">Tất cả vai trò</option>
              <option value="STUDENT">📚 Học viên</option>
              <option value="STAFF">🌿 Nhân viên</option>
              <option value="ADMIN">👑 Quản trị</option>
            </select>
            <select className="mu-sel" value={statusFilter} onChange={(e) => setStatus(e.target.value)} aria-label="Lọc trạng thái">
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">✅ Hoạt động</option>
              <option value="BANNED">🔒 Bị khóa</option>
            </select>
            <select className="mu-sel" value={jlptFilter} onChange={(e) => setJlpt(e.target.value)} aria-label="Lọc cấp độ JLPT">
              <option value="ALL">Tất cả cấp độ</option>
              <option value="N5">N5</option>
              <option value="N4">N4</option>
              <option value="N3">N3</option>
              <option value="N2">N2</option>
              <option value="N1">N1</option>
            </select>
          </div>

          {!isLoading && (
            <span className="mu-count">
              {filtered.length} người dùng
            </span>
          )}
        </div>

        {/* Table card */}
        <div className="mu-table-card">
          <div className="mu-table-scroll">
            <table className="mu-table">
              <thead>
                <tr>
                  <th className="mu-th mu-th--idx">#</th>
                  <th className="mu-th">Người dùng</th>
                  <th className="mu-th">Vai trò</th>
                  <th className="mu-th">Cấp độ</th>
                  <th className="mu-th">Gói</th>
                  <th className="mu-th">Streak</th>
                  <th className="mu-th">Ngày đăng ký</th>
                  <th className="mu-th">Trạng thái</th>
                  <th className="mu-th mu-th--act"></th>
                </tr>
              </thead>
              <tbody>
                {isLoading
                  ? Array.from({ length: PAGE_SIZE }, (_, i) => <SkeletonRow key={i} />)
                  : pageSlice.length === 0
                    ? (
                      <tr>
                        <td colSpan={9} className="mu-empty-cell">
                          <div className="mu-empty">
                            <SakuChan variant="thinking" size={104} />
                            <p className="mu-empty-title">Không tìm thấy ai cả!</p>
                            <p className="mu-empty-sub">Thử thay đổi bộ lọc hoặc từ khoá tìm kiếm nhé 🌸</p>
                          </div>
                        </td>
                      </tr>
                    )
                    : pageSlice.map((u, idx) => (
                      <tr
                        key={u.id}
                        className={`mu-tr${u.status === 'BANNED' ? ' mu-tr--banned' : ''}`}
                        style={{ '--row-delay': `${idx * 35}ms` }}
                      >
                        <td className="mu-td mu-td--idx">{(safePage - 1) * PAGE_SIZE + idx + 1}</td>

                        <td className="mu-td">
                          <div className="mu-user-cell">
                            <UserAvatar user={u} />
                            <div className="mu-user-text">
                              <span className="mu-user-name">{u.fullName}</span>
                              <span className="mu-user-email">{u.email}</span>
                            </div>
                          </div>
                        </td>

                        <td className="mu-td"><RoleBadge role={u.role} /></td>
                        <td className="mu-td"><JlptBadge level={u.jlptLevel} /></td>
                        <td className="mu-td"><SubBadge sub={u.subscription} /></td>

                        <td className="mu-td">
                          <span className="mu-streak">
                            <span className={u.streak > 0 ? 'mu-fire mu-fire--on' : 'mu-fire'} aria-hidden="true">🔥</span>
                            {u.streak}
                          </span>
                        </td>

                        <td className="mu-td mu-td--date">{formatDate(u.createdAt)}</td>
                        <td className="mu-td"><StatusBadge status={u.status} /></td>

                        {/* Action menu — stopPropagation so clicking inside won't hit page-level onClick */}
                        <td className="mu-td mu-td--act" onClick={(e) => e.stopPropagation()}>
                          <div className="mu-act-wrap">
                            <button
                              type="button"
                              className={`mu-act-btn${openMenuId === u.id ? ' mu-act-btn--open' : ''}`}
                              onClick={() => setOpenMenuId(openMenuId === u.id ? null : u.id)}
                              aria-label={`Hành động cho ${u.fullName}`}
                              aria-expanded={openMenuId === u.id}
                            >
                              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                <circle cx="12" cy="5"  r="1.5" fill="currentColor"/>
                                <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                                <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
                              </svg>
                            </button>

                            {openMenuId === u.id && (
                              <div className="mu-act-menu" role="menu">
                                <button
                                  type="button" className="mu-act-item" role="menuitem"
                                  onClick={() => { addToast('info', `👁 Xem chi tiết: ${u.fullName}`); setOpenMenuId(null); }}
                                >
                                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" strokeWidth="2"/>
                                    <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
                                  </svg>
                                  Xem chi tiết
                                </button>
                                <button
                                  type="button" className="mu-act-item" role="menuitem"
                                  onClick={() => openEditRole(u.id)}
                                >
                                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                  </svg>
                                  Đổi vai trò
                                </button>
                                <div className="mu-act-divider" />
                                {u.status === 'ACTIVE' ? (
                                  <button
                                    type="button" className="mu-act-item mu-act-item--danger" role="menuitem"
                                    onClick={() => openBan(u.id)}
                                  >
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                      <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                                      <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" stroke="currentColor" strokeWidth="2"/>
                                    </svg>
                                    Khóa tài khoản
                                  </button>
                                ) : (
                                  <button
                                    type="button" className="mu-act-item mu-act-item--success" role="menuitem"
                                    onClick={() => openActivate(u.id)}
                                  >
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                      <polyline points="20 6 9 17 4 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                    Mở khóa tài khoản
                                  </button>
                                )}
                              </div>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))
                }
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {!isLoading && filtered.length > PAGE_SIZE && (
            <div className="mu-pagination">
              <button
                type="button" className="mu-pg-btn"
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={safePage === 1}
                aria-label="Trang trước"
              >
                ‹
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((n) => (
                <button
                  key={n} type="button"
                  className={`mu-pg-btn${n === safePage ? ' mu-pg-btn--on' : ''}`}
                  onClick={() => setPage(n)}
                  aria-current={n === safePage ? 'page' : undefined}
                  aria-label={`Trang ${n}`}
                >
                  {n}
                </button>
              ))}
              <button
                type="button" className="mu-pg-btn"
                onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                disabled={safePage === totalPages}
                aria-label="Trang sau"
              >
                ›
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Modals */}
      <ConfirmModal
        modal={confirmModal}
        onConfirm={handleConfirmStatus}
        onClose={() => !isSubmitting && setConfirm((m) => ({ ...m, open: false }))}
        isSubmitting={isSubmitting}
      />
      <EditRoleModal
        modal={editModal}
        onConfirm={handleConfirmRole}
        onClose={() => !isSubmitting && setEditModal((m) => ({ ...m, open: false }))}
        isSubmitting={isSubmitting}
      />

      {/* Toasts */}
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}

export default ManageUsers;
