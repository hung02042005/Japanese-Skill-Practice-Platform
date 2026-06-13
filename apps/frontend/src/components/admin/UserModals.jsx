import { useState, useEffect } from 'react';
import SakuChan from '../auth/SakuChan';
import { STAFF_ROLE_LABELS } from '../common/Badges';
import { IcBloomCheck } from './ManageUsersIcons';
/* CSS: classes are defined in pages/admin/ManageUsers.css */

export function ConfirmModal({ modal, onConfirm, onClose, isSubmitting }) {
  if (!modal.open) return null;
  const cfg = {
    activate:      { icon: '✅', title: 'Kích hoạt tài khoản?',        variant: 'success', confirmLabel: 'Kích hoạt',     sakuVariant: 'happy'    },
    'reset-pass':  { icon: '🔑', title: 'Gửi email đặt lại mật khẩu?', variant: 'primary', confirmLabel: 'Gửi ngay',      sakuVariant: 'idle'     },
    delete:        { icon: '🗑️', title: 'Xóa tài khoản?',              variant: 'danger',  confirmLabel: 'Xóa tài khoản', sakuVariant: 'thinking' },
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

export function SuspendModal({ modal, onConfirm, onClose, isSubmitting }) {
  const [reason, setReason] = useState('');
  useEffect(() => { if (modal.open) setReason(''); }, [modal.open]);
  if (!modal.open) return null;
  const len   = reason.trim().length;
  const tooShort = len > 0 && len < 10;
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
          <textarea className={`mu-form-textarea${tooShort ? ' mu-form-input--err' : ''}`} placeholder="Nhập lý do đình chỉ (10–500 ký tự)..." value={reason} onChange={(e) => setReason(e.target.value)} maxLength={500} rows={3} />
          {tooShort && <span className="mu-form-error">Lý do cần ít nhất 10 ký tự (còn thiếu {10 - len} ký tự)</span>}
          <span className={`mu-char-count${tooShort ? ' mu-char-count--err' : ''}`}>{reason.length}/500</span>
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

export function CreateStaffModal({ open, onConfirm, onClose, isSubmitting }) {
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

export function ChangeStaffRoleModal({ modal, onConfirm, onClose, isSubmitting }) {
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
