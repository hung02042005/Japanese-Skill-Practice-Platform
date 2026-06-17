import { useState, useEffect } from 'react';
import { getSettings, updateSetting, testSmtp } from '../../../api/adminService';

const SMTP_FIELDS = [
  { key: 'smtp_host',      label: 'SMTP Host',     type: 'text',     placeholder: 'smtp.gmail.com',    fullWidth: true  },
  { key: 'smtp_port',      label: 'SMTP Port',     type: 'number',   placeholder: '587',               fullWidth: false },
  { key: 'smtp_secure',    label: 'Bảo mật',       type: 'select',   options: ['STARTTLS','SSL/TLS','Không'], fullWidth: false },
  { key: 'smtp_username',  label: 'Tên đăng nhập', type: 'email',    placeholder: 'no-reply@jlpt.com', fullWidth: true  },
  { key: 'smtp_password',  label: 'Mật khẩu',      type: 'password', placeholder: '••••••••',          fullWidth: true  },
  { key: 'smtp_from_name', label: 'Tên hiển thị',  type: 'text',     placeholder: 'SakuJi Platform',   fullWidth: true  },
];

function EyeOpenIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" strokeWidth="2"/>
      <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
    </svg>
  );
}
function EyeOffIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
      <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
    </svg>
  );
}

function FieldSkeleton() {
  return <div className="ast-field-skel" aria-hidden="true" />;
}

export function EmailTab({ addToast }) {
  const [form,      setForm]     = useState({});
  const [showPass,  setShowPass] = useState(false);
  const [isLoading, setLoading]  = useState(true);
  const [isSaving,  setSaving]   = useState(false);
  const [isTesting, setTesting]  = useState(false);

  useEffect(() => {
    getSettings('smtp')
      .then((data) => {
        const init = {};
        (Array.isArray(data) ? data : []).forEach((s) => { init[s.settingKey] = s.settingValue ?? ''; });
        init.smtp_password = '';
        setForm(init);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  function set(key, val) { setForm((f) => ({ ...f, [key]: val })); }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    try {
      await Promise.all(
        SMTP_FIELDS
          .filter((f) => f.key !== 'smtp_password' || form.smtp_password)
          .map((f) => updateSetting('smtp', f.key, form[f.key] ?? ''))
      );
      addToast('success', 'Đã lưu cài đặt SMTP thành công');
    } catch {
      addToast('error', 'Lưu thất bại. Kiểm tra kết nối backend.');
    } finally {
      setSaving(false);
    }
  }

  async function handleTest() {
    setTesting(true);
    try {
      await testSmtp();
      addToast('success', 'Kết nối SMTP thành công ✓');
    } catch (err) {
      addToast('error', `Lỗi kết nối SMTP: ${err?.response?.data?.message ?? 'Không xác định'}`);
    } finally {
      setTesting(false);
    }
  }

  return (
    <div className="ast-card">
      <h3 className="ast-section-title">Cấu hình SMTP</h3>
      <p className="ast-section-desc">
        Máy chủ gửi email cho đặt lại mật khẩu, xác minh email và thông báo hệ thống.
      </p>

      {isLoading ? (
        <div className="ast-form-grid">
          {SMTP_FIELDS.map((f) => (
            <div key={f.key} className={`ast-field${f.fullWidth ? ' ast-field--full' : ''}`}>
              <FieldSkeleton />
            </div>
          ))}
        </div>
      ) : (
        <form className="ast-form-grid" onSubmit={handleSave} noValidate>
          {SMTP_FIELDS.map((f) => (
            <div key={f.key} className={`ast-field${f.fullWidth ? ' ast-field--full' : ''}`}>
              <label className="ast-field-label" htmlFor={`smtp-${f.key}`}>{f.label}</label>

              {f.type === 'select' ? (
                <select
                  id={`smtp-${f.key}`}
                  className="ast-input"
                  value={form[f.key] ?? ''}
                  onChange={(e) => set(f.key, e.target.value)}
                >
                  {f.options.map((o) => <option key={o} value={o}>{o}</option>)}
                </select>

              ) : f.type === 'password' ? (
                <div className="ast-pass-wrap">
                  <input
                    id={`smtp-${f.key}`}
                    className="ast-input"
                    type={showPass ? 'text' : 'password'}
                    placeholder={f.placeholder}
                    value={form[f.key] ?? ''}
                    onChange={(e) => set(f.key, e.target.value)}
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    className="ast-pass-toggle"
                    onClick={() => setShowPass((v) => !v)}
                    aria-label={showPass ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  >
                    {showPass ? <EyeOffIcon /> : <EyeOpenIcon />}
                  </button>
                  <p className="ast-field-hint">Để trống để giữ nguyên mật khẩu hiện tại</p>
                </div>

              ) : (
                <input
                  id={`smtp-${f.key}`}
                  className="ast-input"
                  type={f.type}
                  placeholder={f.placeholder}
                  value={form[f.key] ?? ''}
                  onChange={(e) => set(f.key, e.target.value)}
                />
              )}
            </div>
          ))}

          <div className="ast-form-footer ast-field--full">
            <button
              type="button"
              className="ast-btn ast-btn--outline"
              onClick={handleTest}
              disabled={isTesting || isSaving}
            >
              {isTesting && <span className="ast-spinner" aria-hidden="true" />}
              {isTesting ? 'Đang kiểm tra…' : 'Kiểm tra kết nối'}
            </button>
            <button
              type="submit"
              className="ast-btn ast-btn--primary"
              disabled={isSaving || isTesting}
            >
              {isSaving && <span className="ast-spinner ast-spinner--white" aria-hidden="true" />}
              {isSaving ? 'Đang lưu…' : 'Lưu cài đặt SMTP'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
