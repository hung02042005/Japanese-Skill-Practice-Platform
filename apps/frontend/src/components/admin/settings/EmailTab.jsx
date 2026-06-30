import { useState, useEffect } from 'react';
import { getSettings, updateSettings, testSmtp } from '../../../api/adminService';

/* ─── SMTP Server ─────────────────────────────────────────────────────────── */

const SMTP_FIELDS = [
  { key: 'host',      label: 'SMTP Host',     type: 'text',     placeholder: 'smtp.gmail.com',    fullWidth: true  },
  { key: 'port',      label: 'SMTP Port',     type: 'number',   placeholder: '587',               fullWidth: false },
  { key: 'secure',    label: 'Bảo mật',       type: 'select',   options: ['STARTTLS','SSL/TLS','Không'], fullWidth: false },
  { key: 'username',  label: 'Tên đăng nhập', type: 'email',    placeholder: 'no-reply@jlpt.com', fullWidth: true  },
  { key: 'password',  label: 'Mật khẩu',      type: 'password', placeholder: '••••••••',          fullWidth: true  },
  { key: 'from_name', label: 'Tên hiển thị',  type: 'text',     placeholder: 'SakuJi Platform',   fullWidth: true  },
];

/* ─── 3 loại email ────────────────────────────────────────────────────────── */

const EMAIL_TYPE_FIELDS = [
  { key: 'from_email', label: 'Email người gửi', type: 'email', placeholder: 'noreply@jlpt.com', fullWidth: true  },
  { key: 'from_name',  label: 'Tên hiển thị',    type: 'text',  placeholder: 'JLPT Platform',   fullWidth: false },
  { key: 'subject',    label: 'Tiêu đề email',   type: 'text',  placeholder: 'Nhập tiêu đề...', fullWidth: false },
];

const EMAIL_TYPES = [
  {
    group: 'email_register',
    title: 'Email Xác Nhận Đăng Ký',
    description: 'Gửi tới học viên sau khi đăng ký tài khoản thành công để xác nhận email.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
        <polyline points="16 11 18 13 22 9"/>
      </svg>
    ),
  },
  {
    group: 'email_otp',
    title: 'Email Mã Xác Thực (OTP)',
    description: 'Gửi mã OTP để xác thực danh tính hoặc xác nhận thao tác bảo mật cho học viên.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <rect x="5" y="11" width="14" height="10" rx="2"/>
        <path d="M8 11V7a4 4 0 0 1 8 0v4"/>
        <line x1="12" y1="15" x2="12" y2="17"/>
      </svg>
    ),
  },
  {
    group: 'email_reset',
    title: 'Email Cấp Lại Mật Khẩu — Staff & Manager',
    description: 'Gửi mật khẩu tạm thời hoặc link đặt lại mật khẩu cho tài khoản Staff và Manager.',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
    ),
  },
];

/* ─── Icons ───────────────────────────────────────────────────────────────── */

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

/* ─── Card SMTP Server ────────────────────────────────────────────────────── */

function SmtpCard({ addToast }) {
  const [form,      setForm]    = useState({});
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
      // Gửi mọi field; BE tự bỏ qua mật khẩu để trống (giữ giá trị cũ).
      await updateSettings('smtp', SMTP_FIELDS.map((f) => ({
        settingKey: f.key,
        settingValue: form[f.key] ?? '',
      })));
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
      <h3 className="ast-section-title">Cấu Hình SMTP Server</h3>
      <p className="ast-section-desc">
        Máy chủ dùng chung để gửi tất cả các loại email từ hệ thống.
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
              {isSaving ? 'Đang lưu…' : 'Lưu cấu hình SMTP'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}

/* ─── Card dùng chung cho 3 loại email ───────────────────────────────────── */

function EmailTypeCard({ group, title, description, icon, addToast }) {
  const [form,      setForm]    = useState({});
  const [isLoading, setLoading]  = useState(true);
  const [isSaving,  setSaving]   = useState(false);

  useEffect(() => {
    getSettings(group)
      .then((data) => {
        const init = {};
        (Array.isArray(data) ? data : []).forEach((s) => { init[s.settingKey] = s.settingValue ?? ''; });
        setForm(init);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [group]);

  function set(key, val) { setForm((f) => ({ ...f, [key]: val })); }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    try {
      await updateSettings(group, EMAIL_TYPE_FIELDS.map((f) => ({
        settingKey: f.key,
        settingValue: form[f.key] ?? '',
      })));
      addToast('success', `Đã lưu cài đặt "${title}" thành công`);
    } catch {
      addToast('error', 'Lưu thất bại. Kiểm tra kết nối backend.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="ast-card">
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
        <span style={{ color: 'var(--color-primary)', flexShrink: 0 }}>{icon}</span>
        <h3 className="ast-section-title" style={{ margin: 0 }}>{title}</h3>
      </div>
      <p className="ast-section-desc">{description}</p>

      {isLoading ? (
        <div className="ast-form-grid">
          {EMAIL_TYPE_FIELDS.map((f) => (
            <div key={f.key} className={`ast-field${f.fullWidth ? ' ast-field--full' : ''}`}>
              <FieldSkeleton />
            </div>
          ))}
        </div>
      ) : (
        <form className="ast-form-grid" onSubmit={handleSave} noValidate>
          {EMAIL_TYPE_FIELDS.map((f) => (
            <div key={f.key} className={`ast-field${f.fullWidth ? ' ast-field--full' : ''}`}>
              <label className="ast-field-label" htmlFor={`${group}-${f.key}`}>{f.label}</label>
              <input
                id={`${group}-${f.key}`}
                className="ast-input"
                type={f.type}
                placeholder={f.placeholder}
                value={form[f.key] ?? ''}
                onChange={(e) => set(f.key, e.target.value)}
              />
            </div>
          ))}

          <div className="ast-form-footer ast-field--full">
            <button
              type="submit"
              className="ast-btn ast-btn--primary"
              disabled={isSaving}
            >
              {isSaving && <span className="ast-spinner ast-spinner--white" aria-hidden="true" />}
              {isSaving ? 'Đang lưu…' : 'Lưu cài đặt'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}

/* ─── EmailTab ────────────────────────────────────────────────────────────── */

export function EmailTab({ addToast }) {
  return (
    <>
      <SmtpCard addToast={addToast} />

      {EMAIL_TYPES.map(({ group, title, description, icon }) => (
        <EmailTypeCard
          key={group}
          group={group}
          title={title}
          description={description}
          icon={icon}
          addToast={addToast}
        />
      ))}
    </>
  );
}
