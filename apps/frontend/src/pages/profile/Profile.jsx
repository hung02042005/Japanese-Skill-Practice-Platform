import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAppSelector, useAppDispatch } from '../../store/hooks';
import { setUser } from '../../store/slices/authSlice';
import TopNav from '../../components/layout/TopNav';
import { UserAvatar } from '../../components/common/UserAvatar';
import { JlptBadge } from '../../components/common/Badges';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { updateProfile, uploadAvatar } from '../../api/studentService';
import './Profile.css';

function validate(form) {
  const errs = {};
  if (!form.fullName.trim()) errs.fullName = 'Tên không được để trống';
  if (form.fullName.trim().length > 100) errs.fullName = 'Tên tối đa 100 ký tự';
  if (form.phone && !/^\d{9,15}$/.test(form.phone)) errs.phone = 'Số điện thoại không hợp lệ';
  return errs;
}

export default function Profile() {
  const { user }   = useAppSelector((s) => s.auth);
  const dispatch   = useAppDispatch();
  const { toasts, addToast, removeToast } = useToast();

  const [form, setForm] = useState({ fullName: '', phone: '' });
  const [errors,       setErrors]  = useState({});
  const [isSaving,     setSaving]  = useState(false);
  const [isLoading,    setLoading] = useState(true);
  const [avatarFile,   setAvatar]  = useState(null);
  const [avatarPreview,setPreview] = useState(null);

  useEffect(() => {
    if (user) {
      setForm({
        fullName: user.fullName ?? '',
        phone:    user.phone    ?? '',
      });
      setPreview(user.avatarUrl ?? null);
      setLoading(false);
    }
  }, [user]);

  function handleChange(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
    if (errors[field]) setErrors((e) => ({ ...e, [field]: '' }));
  }

  function handleAvatarChange(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
      addToast('error', 'Ảnh tối đa 2MB.');
      return;
    }
    setAvatar(file);
    setPreview(URL.createObjectURL(file));
  }

  async function handleSave() {
    const errs = validate(form);
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }

    setSaving(true);
    try {
      // Upload avatar trước (persist avatarUrl), rồi updateProfile (BE đã guard không ghi đè avatar).
      if (avatarFile) await uploadAvatar(avatarFile);
      const updated = await updateProfile(form);
      // Đồng bộ store để TopNav/sidebar cập nhật tên + avatar ngay, không cần reload.
      dispatch(setUser(updated));
      setAvatar(null);
      addToast('success', 'Hồ sơ đã được cập nhật!');
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Không thể lưu. Thử lại sau.');
    } finally {
      setSaving(false);
    }
  }

  if (isLoading) {
    return (
      <div className="prf-page">
        <TopNav activeTab="" />
        <div className="prf-body"><div className="prf-skel" aria-hidden="true" /></div>
      </div>
    );
  }

  return (
    <div className="prf-page">
      <TopNav activeTab="" />

      <main className="prf-body">
        <Link to="/dashboard" className="prf-back-link">← Quay lại Dashboard</Link>
        <h1 className="prf-title">Hồ Sơ Của Tôi</h1>

        <div className="prf-layout">
          <aside className="prf-sidebar">
            <div className="prf-avatar-wrap">
              <UserAvatar src={avatarPreview} name={form.fullName} size={96} />
              <label className="prf-avatar-btn" htmlFor="avatar-upload" aria-label="Thay ảnh đại diện">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
                  <circle cx="12" cy="13" r="4" stroke="currentColor" strokeWidth="2"/>
                </svg>
                Thay ảnh
              </label>
              <input
                id="avatar-upload"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                className="prf-sr-only"
                onChange={handleAvatarChange}
              />
            </div>
            <div className="prf-sidebar-name">{form.fullName || '—'}</div>
            <div className="prf-sidebar-email">{user?.email}</div>
            <JlptBadge level={user?.currentJlptLevel ?? 'N5'} />
            <div className="prf-sidebar-joined">
              Thành viên từ{' '}
              {user?.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : '—'}
            </div>
          </aside>

          <section className="prf-form-section">
            <div className="prf-field">
              <label className="prf-label" htmlFor="prf-fullname">
                Họ và tên <span className="prf-required">*</span>
              </label>
              <input
                id="prf-fullname"
                className={`prf-input${errors.fullName ? ' prf-input--err' : ''}`}
                type="text"
                value={form.fullName}
                onChange={(e) => handleChange('fullName', e.target.value)}
                placeholder="Nhập họ và tên..."
                aria-invalid={!!errors.fullName}
                aria-describedby={errors.fullName ? 'prf-fullname-err' : undefined}
              />
              {errors.fullName && <span id="prf-fullname-err" className="prf-field-error">{errors.fullName}</span>}
            </div>

            <div className="prf-field">
              <label className="prf-label" htmlFor="prf-phone">Số điện thoại</label>
              <input
                id="prf-phone"
                className={`prf-input${errors.phone ? ' prf-input--err' : ''}`}
                type="tel"
                value={form.phone}
                onChange={(e) => handleChange('phone', e.target.value)}
                placeholder="0912345678"
                aria-invalid={!!errors.phone}
                aria-describedby={errors.phone ? 'prf-phone-err' : undefined}
              />
              {errors.phone && <span id="prf-phone-err" className="prf-field-error">{errors.phone}</span>}
            </div>

            <div className="prf-field">
              <label className="prf-label" htmlFor="prf-email">
                Email
                <span className="prf-readonly-badge">Không thể thay đổi</span>
              </label>
              <input
                id="prf-email"
                className="prf-input prf-input--readonly"
                type="email"
                value={user?.email ?? ''}
                disabled
                aria-disabled="true"
              />
            </div>

            {/* ── Bảo mật ── */}
            <div className="prf-security">
              <div className="prf-security-info">
                <span className="prf-security-icon" aria-hidden="true">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                    <rect x="4" y="11" width="16" height="10" rx="2" stroke="currentColor" strokeWidth="2"/>
                    <path d="M8 11V7a4 4 0 0 1 8 0v4" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  </svg>
                </span>
                <div>
                  <div className="prf-security-title">Mật khẩu</div>
                  <div className="prf-security-desc">Đổi mật khẩu đăng nhập của bạn</div>
                </div>
              </div>
              <Link to="/settings/change-password" className="prf-security-btn">
                Đổi mật khẩu
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </Link>
            </div>

            {/* ── Footer ── */}
            <div className="prf-form-footer">
              <button
                className="prf-save-btn"
                onClick={handleSave}
                disabled={isSaving}
                aria-busy={isSaving}
              >
                {isSaving && <span className="prf-spinner prf-spinner--white" aria-hidden="true" />}
                {isSaving ? 'Đang lưu…' : 'Lưu thay đổi'}
              </button>
            </div>
          </section>
        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
