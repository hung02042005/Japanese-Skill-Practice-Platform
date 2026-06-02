import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';
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
  if (form.bio.length > 500) errs.bio = 'Bio tối đa 500 ký tự';
  return errs;
}

export default function Profile() {
  const { user }   = useAppSelector((s) => s.auth);
  const { toasts, addToast, removeToast } = useToast();

  const [form, setForm] = useState({ fullName: '', phone: '', dateOfBirth: '', bio: '' });
  const [errors,       setErrors]  = useState({});
  const [isSaving,     setSaving]  = useState(false);
  const [isLoading,    setLoading] = useState(true);
  const [avatarFile,   setAvatar]  = useState(null);
  const [avatarPreview,setPreview] = useState(null);

  useEffect(() => {
    if (user) {
      setForm({
        fullName:    user.fullName    ?? '',
        phone:       user.phone       ?? '',
        dateOfBirth: user.dateOfBirth ?? '',
        bio:         user.bio         ?? '',
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
      if (avatarFile) await uploadAvatar(avatarFile);
      await updateProfile(form);
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
            <JlptBadge level={user?.jlptLevel ?? 'N5'} />
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
              <label className="prf-label" htmlFor="prf-dob">Ngày sinh</label>
              <input
                id="prf-dob"
                className="prf-input"
                type="date"
                value={form.dateOfBirth}
                onChange={(e) => handleChange('dateOfBirth', e.target.value)}
              />
            </div>

            <div className="prf-field">
              <label className="prf-label" htmlFor="prf-bio">
                Bio
                <span className="prf-char-count">{form.bio.length}/500</span>
              </label>
              <textarea
                id="prf-bio"
                className={`prf-textarea${errors.bio ? ' prf-input--err' : ''}`}
                rows={4}
                value={form.bio}
                onChange={(e) => handleChange('bio', e.target.value)}
                placeholder="Giới thiệu về bản thân..."
                aria-invalid={!!errors.bio}
                aria-describedby={errors.bio ? 'prf-bio-err' : undefined}
              />
              {errors.bio && <span id="prf-bio-err" className="prf-field-error">{errors.bio}</span>}
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

            <Link to="/settings/password" className="prf-pwd-link">Đổi mật khẩu →</Link>

            <button
              className="prf-save-btn"
              onClick={handleSave}
              disabled={isSaving}
              aria-busy={isSaving}
            >
              {isSaving && <span className="prf-spinner prf-spinner--white" aria-hidden="true" />}
              {isSaving ? 'Đang lưu…' : 'Lưu thay đổi'}
            </button>
          </section>
        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
