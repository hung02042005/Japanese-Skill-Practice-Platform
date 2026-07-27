import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch } from '@/store/hooks';
import { logoutThunk } from '@/features/auth/authSlice';
import TopNav from '@/shared/components/layout/TopNav';
import EyeIcon from '@/features/auth/components/EyeIcon';
import PasswordStrengthBar from '@/features/auth/components/PasswordStrengthBar';
import { ToastContainer, useToast } from '@/shared/components/common/Toast';
import { changePassword } from '@/shared/api/studentService';
import './ChangePassword.css';

function validate(form) {
  const errs = {};
  if (!form.currentPassword) errs.currentPassword = 'Vui lòng nhập mật khẩu hiện tại';
  if (!form.newPassword) errs.newPassword = 'Vui lòng nhập mật khẩu mới';
  else if (form.newPassword.length < 8) errs.newPassword = 'Mật khẩu tối thiểu 8 ký tự';
  else if (!/[A-Z]/.test(form.newPassword)) errs.newPassword = 'Cần ít nhất 1 chữ hoa';
  else if (!/[a-z]/.test(form.newPassword)) errs.newPassword = 'Cần ít nhất 1 chữ thường';
  else if (!/\d/.test(form.newPassword)) errs.newPassword = 'Cần ít nhất 1 chữ số';
  if (!form.confirmPassword) errs.confirmPassword = 'Vui lòng xác nhận mật khẩu mới';
  else if (form.newPassword && form.newPassword !== form.confirmPassword)
    errs.confirmPassword = 'Mật khẩu xác nhận không khớp';
  return errs;
}

export default function ChangePassword() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [form,     setForm]   = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [showPass, setShow]   = useState({ current: false, newPass: false, confirm: false });
  const [errors,   setErrors] = useState({});
  const [apiError, setApiErr] = useState('');
  const [isSaving, setSaving] = useState(false);

  function handleChange(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
    if (errors[field]) setErrors((e) => ({ ...e, [field]: '' }));
    if (apiError) setApiErr('');
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errs = validate(form);
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }

    setSaving(true);
    setApiErr('');
    try {
      await changePassword({
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
        confirmPassword: form.confirmPassword,
      });
      addToast('success', 'Đổi mật khẩu thành công! Đang đăng xuất...');
      setTimeout(async () => {
        await dispatch(logoutThunk());
        navigate('/login');
      }, 1500);
    } catch (err) {
      setApiErr(err?.response?.data?.message ?? 'Đổi mật khẩu thất bại. Thử lại sau.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="pwd-page">
      <TopNav activeTab="" />

      <main className="pwd-body">
        <Link to="/profile" className="pwd-back-link">← Quay lại Hồ sơ</Link>
        <h1 className="pwd-title">Đổi Mật Khẩu</h1>

        <div className="pwd-card">
          {apiError && (
            <div className="pwd-api-error" role="alert">{apiError}</div>
          )}

          <form className="pwd-form" onSubmit={handleSubmit} noValidate>
            <div className="pwd-field">
              <label className="pwd-label" htmlFor="pwd-current">
                Mật khẩu hiện tại <span className="pwd-required">*</span>
              </label>
              <div className="pwd-input-wrap">
                <input
                  id="pwd-current"
                  className={`pwd-input${errors.currentPassword ? ' pwd-input--err' : ''}`}
                  type={showPass.current ? 'text' : 'password'}
                  value={form.currentPassword}
                  onChange={(e) => handleChange('currentPassword', e.target.value)}
                  autoComplete="current-password"
                  aria-invalid={!!errors.currentPassword}
                  aria-describedby={errors.currentPassword ? 'pwd-current-err' : undefined}
                />
                <button
                  type="button"
                  className="pwd-eye-btn"
                  onClick={() => setShow((s) => ({ ...s, current: !s.current }))}
                  aria-label={showPass.current ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  <EyeIcon open={showPass.current} />
                </button>
              </div>
              {errors.currentPassword && (
                <span id="pwd-current-err" className="pwd-field-error">{errors.currentPassword}</span>
              )}
            </div>

            <div className="pwd-field">
              <label className="pwd-label" htmlFor="pwd-new">
                Mật khẩu mới <span className="pwd-required">*</span>
              </label>
              <div className="pwd-input-wrap">
                <input
                  id="pwd-new"
                  className={`pwd-input${errors.newPassword ? ' pwd-input--err' : ''}`}
                  type={showPass.newPass ? 'text' : 'password'}
                  value={form.newPassword}
                  onChange={(e) => handleChange('newPassword', e.target.value)}
                  autoComplete="new-password"
                  aria-invalid={!!errors.newPassword}
                />
                <button
                  type="button"
                  className="pwd-eye-btn"
                  onClick={() => setShow((s) => ({ ...s, newPass: !s.newPass }))}
                  aria-label={showPass.newPass ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  <EyeIcon open={showPass.newPass} />
                </button>
              </div>
              {form.newPassword && <PasswordStrengthBar password={form.newPassword} />}
              {errors.newPassword && <span className="pwd-field-error">{errors.newPassword}</span>}
            </div>

            <div className="pwd-field">
              <label className="pwd-label" htmlFor="pwd-confirm">
                Xác nhận mật khẩu mới <span className="pwd-required">*</span>
              </label>
              <div className="pwd-input-wrap">
                <input
                  id="pwd-confirm"
                  className={`pwd-input${errors.confirmPassword ? ' pwd-input--err' : ''}`}
                  type={showPass.confirm ? 'text' : 'password'}
                  value={form.confirmPassword}
                  onChange={(e) => handleChange('confirmPassword', e.target.value)}
                  autoComplete="new-password"
                  aria-invalid={!!errors.confirmPassword}
                  aria-describedby={errors.confirmPassword ? 'pwd-confirm-err' : undefined}
                />
                <button
                  type="button"
                  className="pwd-eye-btn"
                  onClick={() => setShow((s) => ({ ...s, confirm: !s.confirm }))}
                  aria-label={showPass.confirm ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  <EyeIcon open={showPass.confirm} />
                </button>
              </div>
              {errors.confirmPassword && (
                <span id="pwd-confirm-err" className="pwd-field-error">{errors.confirmPassword}</span>
              )}
            </div>

            <div className="pwd-info-note" role="note">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                <path d="M12 8v4M12 16h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              </svg>
              Sau khi đổi mật khẩu, bạn sẽ được đăng xuất và cần đăng nhập lại.
            </div>

            <button
              type="submit"
              className="pwd-submit-btn"
              disabled={isSaving}
              aria-busy={isSaving}
            >
              {isSaving && <span className="pwd-spinner pwd-spinner--white" aria-hidden="true" />}
              {isSaving ? 'Đang xử lý…' : 'Đổi mật khẩu'}
            </button>
          </form>
        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
