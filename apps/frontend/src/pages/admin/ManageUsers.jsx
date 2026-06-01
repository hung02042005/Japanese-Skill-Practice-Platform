import { useState, useEffect, useMemo, useCallback, useRef } from 'react';
import AdminTopNav from '../../components/layout/AdminTopNav';
import SakuChan from '../../components/auth/SakuChan';
import {
  listUsers, createStaff, suspendUser, activateUser,
  resetPassword, softDeleteUser, changeStaffRole,
} from '../../api/adminService';
import './ManageUsers.css';

/* ── Constants ── */
const PAGE_SIZE = 20;

const TYPE_TABS = [
  { value: 'student', label: 'Học viên' },
  { value: 'staff',   label: 'Nhân viên' },
  { value: 'admin',   label: 'Quản trị' },
];

const STATUS_LABELS = {
  active:    'Hoạt động',
  suspended: 'Đình chỉ',
  pending:   'Chờ kích hoạt',
  deleted:   'Đã xóa',
};

const ROLE_LABELS = { student: 'Học viên', staff: 'Nhân viên', admin: 'Quản trị' };
const STAFF_ROLE_LABELS = { staff: 'Nhân viên', staff_manager: 'Quản lý nhân viên' };

function formatDate(d) {
  if (!d) return '—';
  const dt = new Date(d);
  return `${String(dt.getDate()).padStart(2,'0')}/${String(dt.getMonth()+1).padStart(2,'0')}/${dt.getFullYear()}`;
}

/* ═══════════════════════════════════════
   STAT ICONS — sakura-themed SVG, not emoji
═══════════════════════════════════════ */

function StatIconUsers() {
  return (
    <svg width="26" height="26" viewBox="0 0 28 28" fill="none" aria-hidden="true">
      <circle cx="11" cy="9.5" r="4.5" fill="currentColor" opacity="0.15"/>
      <circle cx="11" cy="9.5" r="3.2" stroke="currentColor" strokeWidth="1.8" fill="none"/>
      <path d="M3 24c0-4.418 3.582-8 8-8s8 3.582 8 8" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" fill="none"/>
      <circle cx="20" cy="10.5" r="2.8" fill="currentColor" opacity="0.08"/>
      <circle cx="20" cy="10.5" r="1.9" stroke="currentColor" strokeWidth="1.3" fill="none" opacity="0.5"/>
      <path d="M16.5 24c.3-3 2.2-5.5 4.5-6.3" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" fill="none" opacity="0.45"/>
      <ellipse cx="23.5" cy="3.5" rx="1.5" ry="2.5" fill="currentColor" opacity="0.2" transform="rotate(-20 23.5 3.5)"/>
    </svg>
  );
}

function StatIconActive() {
  return (
    <svg width="26" height="26" viewBox="0 0 28 28" fill="none" aria-hidden="true">
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(0 14 14)"/>
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(72 14 14)"/>
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(144 14 14)"/>
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(216 14 14)"/>
      <ellipse cx="14" cy="8" rx="2.5" ry="4" fill="currentColor" opacity="0.22" transform="rotate(288 14 14)"/>
      <circle cx="14" cy="14" r="5.5" fill="currentColor" opacity="0.13"/>
      <circle cx="14" cy="14" r="4" fill="currentColor" opacity="0.28"/>
      <path d="M10.5 14l2.5 2.5 4.5-5" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

function StatIconSuspended() {
  return (
    <svg width="26" height="26" viewBox="0 0 28 28" fill="none" aria-hidden="true">
      <rect x="5" y="13" width="18" height="12" rx="3" fill="currentColor" opacity="0.1"/>
      <rect x="5" y="13" width="18" height="12" rx="3" stroke="currentColor" strokeWidth="1.8"/>
      <path d="M9.5 13V10a4.5 4.5 0 0 1 9 0v3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      <circle cx="14" cy="19.5" r="1.8" fill="currentColor" opacity="0.5"/>
      <path d="M14 19.5v1.8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
      <ellipse cx="21.5" cy="4" rx="1.4" ry="2.4" fill="currentColor" opacity="0.18" transform="rotate(-18 21.5 4)"/>
    </svg>
  );
}

function StatIconPending() {
  return (
    <svg width="26" height="26" viewBox="0 0 28 28" fill="none" aria-hidden="true">
      <circle cx="14" cy="14" r="10.5" fill="currentColor" opacity="0.07"/>
      <circle cx="14" cy="14" r="9.5" stroke="currentColor" strokeWidth="1.8"/>
      <path d="M14 7v7l4.5 2.5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
      <circle cx="8"  cy="25.5" r="1.1" fill="currentColor" opacity="0.3"/>
      <circle cx="12" cy="26.8" r="1.1" fill="currentColor" opacity="0.2"/>
      <circle cx="16" cy="27.2" r="1.1" fill="currentColor" opacity="0.12"/>
    </svg>
  );
}

const STAT_ICONS = {
  total:     <StatIconUsers />,
  active:    <StatIconActive />,
  suspended: <StatIconSuspended />,
  pending:   <StatIconPending />,
};

/* ═══════════════════════════════════════
   ROW ACTION ICONS — handcrafted SVG
═══════════════════════════════════════ */

function IcBan() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2.2"/>
      <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" stroke="currentColor" strokeWidth="2.2"/>
    </svg>
  );
}

function IcCheck() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

function IcKey() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="7.5" cy="15.5" r="5.5" stroke="currentColor" strokeWidth="2"/>
      <path d="M21 2l-9.5 9.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      <path d="M19 4l-2 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      <path d="M16 7l-2 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
    </svg>
  );
}

function IcSwap() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M7 16H3l4-4 4 4H7V8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M17 8h4l-4 4-4-4h4v8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

function IcTrash() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6M10 11v6M14 11v6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

/* Header chip — mini crown to replace generic user-group icon */
function IcAdminChip() {
  return (
    <svg width="13" height="13" viewBox="0 0 22 18" fill="none" aria-hidden="true">
      <path d="M2 14L5 5l4.5 4L11 2l1.5 7L17 5l3 9H2z" fill="currentColor" opacity="0.82" strokeLinejoin="round"/>
      <rect x="2" y="14.5" width="18" height="2.5" rx="1.25" fill="currentColor" opacity="0.82"/>
      <circle cx="11" cy="2" r="1.3" fill="currentColor"/>
    </svg>
  );
}

/* Add staff button — sprouting leaf + plus mark */
function IcAddStaff() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 21v-8M12 13C9 13 5.5 10.5 4 6c3.5.5 6.5 3.5 8 7M12 13c3 0 6.5-2.5 8-7-3.5.5-6.5 3.5-8 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M12 7V2M9.5 4.5h5" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
    </svg>
  );
}

/* Search — magnifying glass with soft inner bloom */
function IcSearchGlass() {
  return (
    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="10.5" cy="10.5" r="7.5" stroke="currentColor" strokeWidth="2"/>
      <circle cx="10.5" cy="10.5" r="3" fill="currentColor" opacity="0.1"/>
      <ellipse cx="10.5" cy="7.5" rx="1.1" ry="1.8" fill="currentColor" opacity="0.18"/>
      <ellipse cx="10.5" cy="7.5" rx="1.1" ry="1.8" fill="currentColor" opacity="0.18" transform="rotate(90 10.5 10.5)"/>
      <path d="M16.5 16.5l4.2 4.2" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round"/>
    </svg>
  );
}

/* ═══════════════════════════════════════
   TYPE TAB ICONS
   Dẫn xuất từ heroicons nhưng thêm
   chi tiết riêng theo theme SakuJi.
═══════════════════════════════════════ */

/* Student — graduation cap + petal tassel
   Base: heroicons academic-cap, simplified */
function TabIconStudent() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Diamond cap top */}
      <path d="M12 3L2 9l10 6 10-6-10-6z"
        stroke="currentColor" strokeWidth="1.8"
        strokeLinejoin="round"
        fill="currentColor" opacity="0.12"/>
      {/* Cap body */}
      <path d="M6 12.5v4C6 18.43 8.686 20 12 20s6-1.57 6-3.5v-4"
        stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      {/* Tassel string */}
      <path d="M20.5 9.5v5"
        stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      {/* Petal at tassel tip */}
      <ellipse cx="20.5" cy="15.5" rx="1.3" ry="2"
        fill="currentColor" opacity="0.45"
        transform="rotate(12 20.5 15.5)"/>
    </svg>
  );
}

/* Staff — person silhouette + leaf sprouting from shoulder
   Base: heroicons user, added leaf accent */
function TabIconStaff() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Head */}
      <circle cx="12" cy="7" r="3.5"
        stroke="currentColor" strokeWidth="1.8"/>
      {/* Shoulders */}
      <path d="M4.5 21c0-4.14 3.36-7.5 7.5-7.5s7.5 3.36 7.5 7.5"
        stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
      {/* Leaf sprouting from right shoulder — organic accent */}
      <path d="M17.5 11c0-2.5 3-4.5 5.5-4.5C22.5 9 20.5 11 17.5 11z"
        fill="currentColor" opacity="0.38"
        stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round"/>
      <path d="M17.5 11c.5-1.5 2.5-3 4-3"
        stroke="currentColor" strokeWidth="1" strokeLinecap="round" opacity="0.5"/>
    </svg>
  );
}

/* Admin — 5-point star + small petal crown tip
   Base: heroicons star, drawn with exact geometry */
function TabIconAdmin() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* 5-point star — outer r=10, inner r=4.5 */}
      <path d="M12 2l2.6 8.4H22l-6.3 4.6 2.4 8-6.1-4.4-6.1 4.4 2.4-8L2 10.4h7.4L12 2z"
        stroke="currentColor" strokeWidth="1.8"
        strokeLinejoin="round" strokeLinecap="round"
        fill="currentColor" opacity="0.13"/>
      {/* Petal accent at crown tip */}
      <ellipse cx="12" cy="2.8" rx="1.1" ry="1.7"
        fill="currentColor" opacity="0.42"
        transform="rotate(0 12 2.8)"/>
    </svg>
  );
}

/* Role modal checkmark — sakura bloom check */
function IcBloomCheck() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
      <ellipse cx="12" cy="7"  rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(0 12 12)"/>
      <ellipse cx="12" cy="7"  rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(72 12 12)"/>
      <ellipse cx="12" cy="7"  rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(144 12 12)"/>
      <ellipse cx="12" cy="7"  rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(216 12 12)"/>
      <ellipse cx="12" cy="7"  rx="1.8" ry="3.2" fill="var(--color-primary)" opacity="0.26" transform="rotate(288 12 12)"/>
      <circle cx="12" cy="12" r="4.5" fill="var(--color-primary)" opacity="0.14"/>
      <path d="M9 12l2.2 2.2 4-4.5" stroke="var(--color-primary)" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}

/* TAB_ICONS declared here, after all TabIcon* components */
const TAB_ICONS = {
  student: <TabIconStudent />,
  staff:   <TabIconStaff />,
  admin:   <TabIconAdmin />,
};

/* ═══════════════════════════════════════
   SHARED VISUAL SUB-COMPONENTS
═══════════════════════════════════════ */

function PetalDecoration() {
  return (
    <div className="mu-petals" aria-hidden="true">
      <svg className="mu-petal mu-petal--1" width="32" height="32" viewBox="0 0 32 32"><ellipse cx="16" cy="16" rx="7" ry="14" fill="#E8637A" opacity="0.10" transform="rotate(-20 16 16)"/><ellipse cx="16" cy="16" rx="7" ry="14" fill="#F4A7B3" opacity="0.08" transform="rotate(20 16 16)"/></svg>
      <svg className="mu-petal mu-petal--2" width="22" height="22" viewBox="0 0 22 22"><ellipse cx="11" cy="11" rx="5" ry="10" fill="#F4A7B3" opacity="0.14" transform="rotate(15 11 11)"/></svg>
      <svg className="mu-petal mu-petal--3" width="26" height="26" viewBox="0 0 26 26"><ellipse cx="13" cy="13" rx="6" ry="12" fill="#E8637A" opacity="0.09" transform="rotate(-30 13 13)"/></svg>
      <svg className="mu-petal mu-petal--4" width="18" height="18" viewBox="0 0 18 18"><ellipse cx="9" cy="9" rx="4" ry="8" fill="#F4A7B3" opacity="0.16" transform="rotate(10 9 9)"/></svg>
      <svg className="mu-petal mu-petal--5" width="14" height="14" viewBox="0 0 14 14"><ellipse cx="7" cy="7" rx="3" ry="6" fill="#E8637A" opacity="0.12" transform="rotate(-45 7 7)"/></svg>
    </div>
  );
}

function RoleBadge({ userType, staffRole }) {
  const icon = userType === 'student' ? '📚' : userType === 'staff' ? '🌿' : '👑';
  const label = userType === 'staff' && staffRole
    ? STAFF_ROLE_LABELS[staffRole] ?? staffRole
    : (ROLE_LABELS[userType] ?? userType);
  return <span className={`mu-badge mu-badge--role mu-badge--role-${userType}`}>{icon} {label}</span>;
}

function JlptBadge({ level }) {
  if (!level) return null;
  return <span className={`mu-badge jlpt-${level.toLowerCase()}`}>{level}</span>;
}

function StatusBadge({ status }) {
  return (
    <span className={`mu-badge mu-badge--status mu-badge--${status}`}>
      <span className={`mu-status-dot mu-status-dot--${status}`} />
      {STATUS_LABELS[status] ?? status}
    </span>
  );
}

function UserAvatar({ user }) {
  const initial = user.fullName?.charAt(0)?.toUpperCase() ?? '?';
  return (
    <div className={`mu-avatar mu-avatar--${user.userType}`} aria-hidden="true">
      {initial}
      {user.status === 'active' && <span className="mu-avatar-dot" />}
    </div>
  );
}

function SkeletonRow() {
  return (
    <tr className="mu-skel-row" aria-hidden="true">
      <td><div className="mu-skel mu-skel--xs" /></td>
      <td><div className="mu-skel-user"><div className="mu-skel mu-skel--avatar" /><div><div className="mu-skel mu-skel--name" /><div className="mu-skel mu-skel--email" /></div></div></td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td><div className="mu-skel mu-skel--sm" /></td>
      <td><div className="mu-skel mu-skel--md" /></td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td>
        <div className="mu-skel-acts">
          <div className="mu-skel mu-skel--act" />
          <div className="mu-skel mu-skel--act" />
          <div className="mu-skel mu-skel--act" />
        </div>
      </td>
    </tr>
  );
}

function ToastContainer({ toasts, onRemove }) {
  if (!toasts.length) return null;
  return (
    <div className="mu-toasts" aria-live="polite">
      {toasts.map((t) => (
        <div key={t.id} className={`mu-toast mu-toast--${t.type}`} role="alert">
          <span className="mu-toast-icon">{t.type==='success'?'✅':t.type==='error'?'❌':'💬'}</span>
          <span className="mu-toast-msg">{t.message}</span>
          <button type="button" className="mu-toast-close" onClick={() => onRemove(t.id)} aria-label="Đóng">×</button>
        </div>
      ))}
    </div>
  );
}

/* ═══════════════════════════════════════
   MODALS
═══════════════════════════════════════ */

function ConfirmModal({ modal, onConfirm, onClose, isSubmitting }) {
  if (!modal.open) return null;
  const cfg = {
    activate:      { icon: '✅', title: 'Kích hoạt tài khoản?',          variant: 'success', confirmLabel: 'Kích hoạt',      sakuVariant: 'happy'   },
    'reset-pass':  { icon: '🔑', title: 'Gửi email đặt lại mật khẩu?',   variant: 'primary', confirmLabel: 'Gửi ngay',       sakuVariant: 'idle'    },
    delete:        { icon: '🗑️', title: 'Xóa tài khoản?',                variant: 'danger',  confirmLabel: 'Xóa tài khoản',  sakuVariant: 'thinking'},
  }[modal.action] ?? {};

  return (
    <div className="mu-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="mu-modal" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="mu-modal-x" onClick={onClose}>×</button>
        <div className="mu-modal-mascot"><SakuChan variant={cfg.sakuVariant} size={80} /></div>
        <h2 className="mu-modal-title">{cfg.icon} {cfg.title}</h2>
        <p className="mu-modal-desc">
          {modal.action === 'activate'   && <>Kích hoạt lại tài khoản <strong>"{modal.userName}"</strong>? Người dùng có thể đăng nhập trở lại.</>}
          {modal.action === 'reset-pass' && <>Email đặt lại mật khẩu sẽ được gửi đến <strong>"{modal.userName}"</strong>. Liên kết có hiệu lực 15 phút.</>}
          {modal.action === 'delete'     && <>Xóa tài khoản <strong>"{modal.userName}"</strong>? Dữ liệu học tập vẫn được giữ lại. Không thể khôi phục qua giao diện này.</>}
        </p>
        <div className="mu-modal-row">
          <button type="button" className="mu-btn mu-btn--ghost" onClick={onClose}>Huỷ</button>
          <button type="button" className={`mu-btn mu-btn--${cfg.variant}`} onClick={onConfirm} disabled={isSubmitting}>
            {isSubmitting ? 'Đang xử lý...' : cfg.confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

function SuspendModal({ modal, onConfirm, onClose, isSubmitting }) {
  const [reason, setReason] = useState('');
  useEffect(() => { if (modal.open) setReason(''); }, [modal.open]);
  if (!modal.open) return null;
  const len = reason.trim().length;
  const valid = len >= 10 && len <= 500;
  return (
    <div className="mu-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="mu-modal mu-modal--form" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="mu-modal-x" onClick={onClose}>×</button>
        <div className="mu-modal-mascot"><SakuChan variant="thinking" size={80} /></div>
        <h2 className="mu-modal-title">🔒 Đình chỉ tài khoản?</h2>
        <p className="mu-modal-desc">Đình chỉ tài khoản <strong>"{modal.userName}"</strong>. Người dùng sẽ không thể đăng nhập cho đến khi được kích hoạt lại.</p>
        <div className="mu-form-field">
          <label className="mu-form-label">Lý do đình chỉ <span className="mu-required">*</span></label>
          <textarea
            className="mu-form-textarea"
            placeholder="Nhập lý do đình chỉ (10–500 ký tự)..."
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            maxLength={500}
            rows={3}
          />
          <span className={`mu-char-count${len > 0 && !valid ? ' mu-char-count--err' : ''}`}>{reason.length}/500</span>
        </div>
        <div className="mu-modal-row">
          <button type="button" className="mu-btn mu-btn--ghost" onClick={onClose}>Huỷ</button>
          <button type="button" className="mu-btn mu-btn--danger" onClick={() => onConfirm(reason.trim())} disabled={!valid || isSubmitting}>
            {isSubmitting ? 'Đang xử lý...' : 'Đình chỉ ngay'}
          </button>
        </div>
      </div>
    </div>
  );
}

function CreateStaffModal({ open, onConfirm, onClose, isSubmitting }) {
  const [form, setForm]     = useState({ fullName: '', email: '', staffRole: 'staff' });
  const [errors, setErrors] = useState({});
  useEffect(() => { if (open) { setForm({ fullName: '', email: '', staffRole: 'staff' }); setErrors({}); } }, [open]);
  if (!open) return null;

  function validate() {
    const e = {};
    if (form.fullName.trim().length < 2) e.fullName = 'Họ tên tối thiểu 2 ký tự';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) e.email = 'Email không hợp lệ';
    return e;
  }

  function handleSubmit() {
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    onConfirm({ fullName: form.fullName.trim(), email: form.email.trim().toLowerCase(), staffRole: form.staffRole });
  }

  return (
    <div className="mu-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="mu-modal mu-modal--form" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="mu-modal-x" onClick={onClose}>×</button>
        <div className="mu-modal-mascot"><SakuChan variant="happy" size={80} /></div>
        <h2 className="mu-modal-title">🌿 Tạo nhân viên mới</h2>
        <p className="mu-modal-desc">Email mời sẽ được gửi để nhân viên thiết lập mật khẩu.</p>
        <div className="mu-form-fields">
          <div className="mu-form-field">
            <label className="mu-form-label">Họ và tên <span className="mu-required">*</span></label>
            <input className={`mu-form-input${errors.fullName?' mu-form-input--err':''}`} placeholder="Nguyễn Văn A" value={form.fullName} onChange={(e) => setForm(p=>({...p,fullName:e.target.value}))} maxLength={150} />
            {errors.fullName && <span className="mu-form-error">{errors.fullName}</span>}
          </div>
          <div className="mu-form-field">
            <label className="mu-form-label">Email <span className="mu-required">*</span></label>
            <input className={`mu-form-input${errors.email?' mu-form-input--err':''}`} type="email" placeholder="staff@jlpt.com" value={form.email} onChange={(e) => setForm(p=>({...p,email:e.target.value}))} maxLength={255} />
            {errors.email && <span className="mu-form-error">{errors.email}</span>}
          </div>
          <div className="mu-form-field">
            <label className="mu-form-label">Vai trò</label>
            <select className="mu-form-select" value={form.staffRole} onChange={(e) => setForm(p=>({...p,staffRole:e.target.value}))}>
              <option value="staff">🌿 Nhân viên</option>
              <option value="staff_manager">⭐ Quản lý nhân viên</option>
            </select>
          </div>
        </div>
        <div className="mu-modal-row">
          <button type="button" className="mu-btn mu-btn--ghost" onClick={onClose}>Huỷ</button>
          <button type="button" className="mu-btn mu-btn--primary" onClick={handleSubmit} disabled={isSubmitting}>
            {isSubmitting ? 'Đang tạo...' : 'Tạo & Gửi email'}
          </button>
        </div>
      </div>
    </div>
  );
}

function ChangeStaffRoleModal({ modal, onConfirm, onClose, isSubmitting }) {
  const [selected, setSelected] = useState('staff');
  useEffect(() => { if (modal.open) setSelected(modal.currentStaffRole ?? 'staff'); }, [modal.open, modal.currentStaffRole]);
  if (!modal.open) return null;

  const OPTS = [
    { value: 'staff',         icon: '🌿', label: 'Nhân viên',         desc: 'Quản lý và cập nhật nội dung khoá học' },
    { value: 'staff_manager', icon: '⭐', label: 'Quản lý nhân viên', desc: 'Giám sát nhóm nhân viên và phân công'   },
  ];

  return (
    <div className="mu-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="mu-modal mu-modal--edit" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="mu-modal-x" onClick={onClose}>×</button>
        <div className="mu-modal-mascot"><SakuChan variant="idle" size={72} /></div>
        <h2 className="mu-modal-title">✏️ Đổi vai trò nhân viên</h2>
        <p className="mu-modal-desc">Thay đổi vai trò cho <strong>"{modal.userName}"</strong></p>
        <div className="mu-role-opts">
          {OPTS.map((r) => (
            <label key={r.value} className={`mu-role-opt${selected===r.value?' mu-role-opt--on':''}`}>
              <input type="radio" name="staff-role" value={r.value} checked={selected===r.value} onChange={() => setSelected(r.value)} className="mu-role-radio" />
              <span className="mu-role-opt-icon">{r.icon}</span>
              <div className="mu-role-opt-body">
                <span className="mu-role-opt-label">{r.label}</span>
                <span className="mu-role-opt-desc">{r.desc}</span>
              </div>
              {selected===r.value && <span className="mu-role-check"><IcBloomCheck /></span>}
            </label>
          ))}
        </div>
        <div className="mu-modal-row">
          <button type="button" className="mu-btn mu-btn--ghost" onClick={onClose}>Huỷ</button>
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
  const [activeType, setActiveType]       = useState('student');
  const [users, setUsers]                 = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages]       = useState(1);
  const [isLoading, setIsLoading]         = useState(true);
  const [currentPage, setCurrentPage]     = useState(1);

  const [search, setSearch]               = useState('');
  const [debouncedSearch, setDebounced]   = useState('');
  const [statusFilter, setStatusFilter]   = useState('');
  const [jlptFilter, setJlptFilter]       = useState('');
  const [staffRoleFilter, setStaffRole]   = useState('');

  const [isSubmitting, setSubmitting]     = useState(false);
  const [toasts, setToasts]               = useState([]);

  const [confirmModal, setConfirmModal]   = useState({ open: false, action: '', userId: null, userType: null, userName: '' });
  const [suspendModal, setSuspendModal]   = useState({ open: false, userId: null, userType: null, userName: '' });
  const [staffRoleModal, setStaffRoleMod] = useState({ open: false, userId: null, userName: '', currentStaffRole: 'staff' });
  const [createStaffOpen, setCreateStaff] = useState(false);

  /* Toast helpers */
  const addToast = useCallback((type, message) => {
    const id = Date.now() + Math.random();
    setToasts((p) => [...p, { id, type, message }]);
    setTimeout(() => setToasts((p) => p.filter((t) => t.id !== id)), 3200);
  }, []);
  const removeToast = useCallback((id) => setToasts((p) => p.filter((t) => t.id !== id)), []);

  /* Debounce search input */
  const debounceRef = useRef(null);
  useEffect(() => {
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => { setDebounced(search); setCurrentPage(1); }, 400);
    return () => clearTimeout(debounceRef.current);
  }, [search]);

  /* Reset page when filters/type change */
  useEffect(() => { setCurrentPage(1); }, [activeType, statusFilter, jlptFilter, staffRoleFilter]);

  /* Fetch users */
  useEffect(() => {
    let cancelled = false;
    setIsLoading(true);
    const params = { type: activeType, page: currentPage - 1, size: PAGE_SIZE };
    if (debouncedSearch)  params.q         = debouncedSearch;
    if (statusFilter)     params.status     = statusFilter;
    if (activeType === 'student' && jlptFilter)      params.jlptLevel = jlptFilter;
    if (activeType === 'staff'   && staffRoleFilter)  params.staffRole = staffRoleFilter;

    listUsers(params)
      .then((data) => {
        if (cancelled) return;
        setUsers(data.content ?? []);
        setTotalElements(data.totalElements ?? 0);
        setTotalPages(data.totalPages ?? 1);
        setIsLoading(false);
      })
      .catch(() => {
        if (cancelled) return;
        setIsLoading(false);
        addToast('error', 'Không thể tải danh sách người dùng, vui lòng thử lại!');
      });
    return () => { cancelled = true; };
  }, [activeType, debouncedSearch, statusFilter, jlptFilter, staffRoleFilter, currentPage, addToast]);

  /* Stats from current page */
  const stats = useMemo(() => ({
    total:     totalElements,
    active:    users.filter((u) => u.status === 'active').length,
    suspended: users.filter((u) => u.status === 'suspended').length,
    pending:   users.filter((u) => u.status === 'pending').length,
  }), [users, totalElements]);

  /* Reload after mutation */
  function reload() {
    setUsers([]);
    setIsLoading(true);
    const params = { type: activeType, page: currentPage - 1, size: PAGE_SIZE };
    if (debouncedSearch)  params.q         = debouncedSearch;
    if (statusFilter)     params.status     = statusFilter;
    if (activeType === 'student' && jlptFilter)      params.jlptLevel = jlptFilter;
    if (activeType === 'staff'   && staffRoleFilter)  params.staffRole = staffRoleFilter;
    listUsers(params)
      .then((data) => { setUsers(data.content ?? []); setTotalElements(data.totalElements ?? 0); setTotalPages(data.totalPages ?? 1); })
      .catch(() => addToast('error', 'Không thể làm mới danh sách'))
      .finally(() => setIsLoading(false));
  }

  /* ── Action openers ── */
  function openSuspend(u)       { setSuspendModal({ open: true, userId: u.userId, userType: u.userType, userName: u.fullName }); }
  function openConfirm(action, u) { setConfirmModal({ open: true, action, userId: u.userId, userType: u.userType, userName: u.fullName }); }
  function openChangeRole(u)    { setStaffRoleMod({ open: true, userId: u.userId, userName: u.fullName, currentStaffRole: u.staffRole ?? 'staff' }); }

  /* ── Handlers ── */
  async function handleSuspend(reason) {
    setSubmitting(true);
    try {
      await suspendUser(suspendModal.userType, suspendModal.userId, reason);
      addToast('success', 'Đã đình chỉ tài khoản thành công!');
      setSuspendModal((m) => ({ ...m, open: false }));
      reload();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally { setSubmitting(false); }
  }

  async function handleConfirm() {
    const { action, userType, userId } = confirmModal;
    setSubmitting(true);
    try {
      if (action === 'activate')   await activateUser(userType, userId);
      if (action === 'reset-pass') await resetPassword(userType, userId);
      if (action === 'delete')     await softDeleteUser(userType, userId);
      const msgs = { activate: 'Đã kích hoạt lại tài khoản!', 'reset-pass': 'Email đặt lại mật khẩu đã được gửi!', delete: 'Đã xóa tài khoản thành công!' };
      addToast('success', msgs[action]);
      setConfirmModal((m) => ({ ...m, open: false }));
      reload();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally { setSubmitting(false); }
  }

  async function handleCreateStaff(data) {
    setSubmitting(true);
    try {
      await createStaff(data);
      addToast('success', 'Tạo tài khoản nhân viên thành công! Email mời đã được gửi.');
      setCreateStaff(false);
      if (activeType !== 'staff') setActiveType('staff');
      else reload();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally { setSubmitting(false); }
  }

  async function handleChangeStaffRole(newRole) {
    setSubmitting(true);
    try {
      await changeStaffRole(staffRoleModal.userId, newRole);
      addToast('success', `Đã cập nhật vai trò thành "${STAFF_ROLE_LABELS[newRole]}"!`);
      setStaffRoleMod((m) => ({ ...m, open: false }));
      reload();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally { setSubmitting(false); }
  }

  /* ── Render ── */
  return (
    <div className="mu-page">
      <AdminTopNav activeTab="manage-users" />

      {/* Header */}
      <div className="mu-header">
        <PetalDecoration />
        <div className="mu-header-inner">
          <div className="mu-header-text">
            <span className="mu-header-chip">
              <IcAdminChip />
              Quản trị viên
            </span>
            <h1 className="mu-header-title">Quản lý Người Dùng</h1>
            <p className="mu-header-sub">Theo dõi, phân quyền và quản lý toàn bộ tài khoản trên hệ thống</p>
          </div>
          <div className="mu-header-mascot" aria-hidden="true"><SakuChan variant="happy" size={100} /></div>
        </div>
      </div>

      {/* Body */}
      <div className="mu-body">

        {/* Stats */}
        <div className="mu-stats-row">
          {[
            { key:'total',     value: isLoading?'–':totalElements,   label:`Tổng ${ROLE_LABELS[activeType]}`, mod:'total'  },
            { key:'active',    value: isLoading?'–':stats.active,    label:'Đang hoạt động (trang này)',      mod:'active' },
            { key:'suspended', value: isLoading?'–':stats.suspended, label:'Đình chỉ (trang này)',            mod:'banned' },
            { key:'pending',   value: isLoading?'–':stats.pending,   label:'Chờ kích hoạt (trang này)',       mod:'new'    },
          ].map((s) => (
            <div key={s.key} className={`mu-stat mu-stat--${s.mod}`}>
              <div className="mu-stat-icon-wrap">{STAT_ICONS[s.key]}</div>
              <div className="mu-stat-body">
                <div className="mu-stat-val">{s.value}</div>
                <div className="mu-stat-lbl">{s.label}</div>
              </div>
            </div>
          ))}
        </div>

        {/* Type tabs + Create Staff */}
        <div className="mu-type-bar">
          <div className="mu-type-tabs">
            {TYPE_TABS.map((tab) => (
              <button
                key={tab.value}
                type="button"
                className={`mu-type-tab${activeType===tab.value?' mu-type-tab--on':''}`}
                onClick={() => { setActiveType(tab.value); setSearch(''); setStatusFilter(''); setJlptFilter(''); setStaffRole(''); }}
              >
                <span className="mu-tab-icon">{TAB_ICONS[tab.value]}</span>
                {tab.label}
              </button>
            ))}
          </div>
          {activeType === 'staff' && (
            <button type="button" className="mu-btn mu-btn--primary mu-btn--add" onClick={() => setCreateStaff(true)}>
              <IcAddStaff />
              Tạo nhân viên mới
            </button>
          )}
        </div>

        {/* Filter bar */}
        <div className="mu-filter-bar">
          <div className="mu-search">
            <span className="mu-search-ic"><IcSearchGlass /></span>
            <input
              type="text"
              className="mu-search-input"
              placeholder="Tìm theo tên hoặc email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              aria-label="Tìm kiếm người dùng"
            />
            {search && <button type="button" className="mu-search-clear" onClick={() => setSearch('')} aria-label="Xoá tìm kiếm">×</button>}
          </div>

          <div className="mu-selects">
            <select className="mu-sel" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="">Tất cả trạng thái</option>
              <option value="active">✅ Hoạt động</option>
              <option value="suspended">🔒 Đình chỉ</option>
              <option value="pending">⏳ Chờ kích hoạt</option>
              <option value="deleted">🗑️ Đã xóa</option>
            </select>

            {activeType === 'student' && (
              <select className="mu-sel" value={jlptFilter} onChange={(e) => setJlptFilter(e.target.value)}>
                <option value="">Tất cả cấp độ</option>
                {['N5','N4','N3','N2','N1'].map((l) => <option key={l} value={l}>{l}</option>)}
              </select>
            )}

            {activeType === 'staff' && (
              <select className="mu-sel" value={staffRoleFilter} onChange={(e) => setStaffRole(e.target.value)}>
                <option value="">Tất cả vai trò staff</option>
                <option value="staff">🌿 Nhân viên</option>
                <option value="staff_manager">⭐ Quản lý nhân viên</option>
              </select>
            )}
          </div>

          {!isLoading && (
            <span className="mu-count">{totalElements} người dùng</span>
          )}
        </div>

        {/* Table */}
        <div className="mu-table-card">
          <div className="mu-table-scroll">
            <table className="mu-table">
              <thead>
                <tr>
                  <th className="mu-th mu-th--idx">#</th>
                  <th className="mu-th">Người dùng</th>
                  <th className="mu-th">Vai trò</th>
                  {activeType === 'student' && <th className="mu-th">Cấp độ JLPT</th>}
                  {activeType === 'student' && <th className="mu-th">Streak</th>}
                  <th className="mu-th">Ngày đăng ký</th>
                  <th className="mu-th">Trạng thái</th>
                  <th className="mu-th mu-th--act">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {isLoading
                  ? Array.from({ length: 8 }, (_, i) => <SkeletonRow key={i} />)
                  : users.length === 0
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
                    : users.map((u, idx) => (
                      <tr
                        key={u.userId}
                        className={`mu-tr${u.status==='suspended'||u.status==='deleted'?' mu-tr--banned':''}`}
                        style={{ '--row-delay': `${idx * 30}ms` }}
                      >
                        <td className="mu-td mu-td--idx">{(currentPage - 1) * PAGE_SIZE + idx + 1}</td>

                        <td className="mu-td">
                          <div className="mu-user-cell">
                            <UserAvatar user={u} />
                            <div className="mu-user-text">
                              <span className="mu-user-name">{u.fullName}</span>
                              <span className="mu-user-email">{u.email}</span>
                            </div>
                          </div>
                        </td>

                        <td className="mu-td"><RoleBadge userType={u.userType} staffRole={u.staffRole} /></td>
                        {activeType === 'student' && <td className="mu-td"><JlptBadge level={u.currentJlptLevel} /></td>}
                        {activeType === 'student' && (
                          <td className="mu-td">
                            <span className="mu-streak">
                              <span className={u.currentStreak > 0 ? 'mu-fire mu-fire--on' : 'mu-fire'} aria-hidden="true">🔥</span>
                              {u.currentStreak ?? 0}
                            </span>
                          </td>
                        )}
                        <td className="mu-td mu-td--date">{formatDate(u.createdAt)}</td>
                        <td className="mu-td"><StatusBadge status={u.status} /></td>

                        {/* Inline action buttons — no dropdown, no overlap */}
                        <td className="mu-td mu-td--act" onClick={(e) => e.stopPropagation()}>
                          <div className="mu-acts">
                            {(u.status === 'active' || u.status === 'pending') && (
                              <button
                                type="button"
                                className="mu-act-ic mu-act-ic--suspend"
                                onClick={() => openSuspend(u)}
                                title="Đình chỉ tài khoản"
                                aria-label="Đình chỉ tài khoản"
                              >
                                <IcBan />
                              </button>
                            )}
                            {u.status === 'suspended' && (
                              <button
                                type="button"
                                className="mu-act-ic mu-act-ic--activate"
                                onClick={() => openConfirm('activate', u)}
                                title="Kích hoạt lại"
                                aria-label="Kích hoạt lại tài khoản"
                              >
                                <IcCheck />
                              </button>
                            )}
                            {u.status !== 'deleted' && (
                              <button
                                type="button"
                                className="mu-act-ic mu-act-ic--reset"
                                onClick={() => openConfirm('reset-pass', u)}
                                title="Đặt lại mật khẩu"
                                aria-label="Đặt lại mật khẩu"
                              >
                                <IcKey />
                              </button>
                            )}
                            {u.userType === 'staff' && u.status !== 'deleted' && (
                              <button
                                type="button"
                                className="mu-act-ic mu-act-ic--role"
                                onClick={() => openChangeRole(u)}
                                title="Đổi vai trò Staff"
                                aria-label="Đổi vai trò Staff"
                              >
                                <IcSwap />
                              </button>
                            )}
                            {u.userType !== 'admin' && u.status !== 'deleted' && (
                              <button
                                type="button"
                                className="mu-act-ic mu-act-ic--delete"
                                onClick={() => openConfirm('delete', u)}
                                title="Xóa tài khoản"
                                aria-label="Xóa tài khoản"
                              >
                                <IcTrash />
                              </button>
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
          {!isLoading && totalPages > 1 && (
            <div className="mu-pagination">
              <button type="button" className="mu-pg-btn" onClick={() => setCurrentPage((p)=>Math.max(1,p-1))} disabled={currentPage===1} aria-label="Trang trước">‹</button>
              {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
                const n = totalPages <= 7 ? i + 1
                  : currentPage <= 4 ? i + 1
                  : currentPage >= totalPages - 3 ? totalPages - 6 + i
                  : currentPage - 3 + i;
                return (
                  <button key={n} type="button" className={`mu-pg-btn${n===currentPage?' mu-pg-btn--on':''}`} onClick={() => setCurrentPage(n)} aria-current={n===currentPage?'page':undefined}>
                    {n}
                  </button>
                );
              })}
              <button type="button" className="mu-pg-btn" onClick={() => setCurrentPage((p)=>Math.min(totalPages,p+1))} disabled={currentPage===totalPages} aria-label="Trang sau">›</button>
            </div>
          )}
        </div>
      </div>

      {/* Modals */}
      <SuspendModal
        modal={suspendModal}
        onConfirm={handleSuspend}
        onClose={() => !isSubmitting && setSuspendModal((m) => ({ ...m, open: false }))}
        isSubmitting={isSubmitting}
      />
      <ConfirmModal
        modal={confirmModal}
        onConfirm={handleConfirm}
        onClose={() => !isSubmitting && setConfirmModal((m) => ({ ...m, open: false }))}
        isSubmitting={isSubmitting}
      />
      <ChangeStaffRoleModal
        modal={staffRoleModal}
        onConfirm={handleChangeStaffRole}
        onClose={() => !isSubmitting && setStaffRoleMod((m) => ({ ...m, open: false }))}
        isSubmitting={isSubmitting}
      />
      <CreateStaffModal
        open={createStaffOpen}
        onConfirm={handleCreateStaff}
        onClose={() => !isSubmitting && setCreateStaff(false)}
        isSubmitting={isSubmitting}
      />

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}

export default ManageUsers;
