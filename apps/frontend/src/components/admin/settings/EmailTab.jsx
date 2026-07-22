import { useState, useEffect, useRef } from 'react';
import { getSettings, updateSettings, testSmtp } from '../../../api/adminService';
import { emailError, requiredError, portError } from '../../../utils/validation';

/* ─── Validators L1 (client-side; BE vẫn validate lại) ────────────────────── */

function smtpFieldError(key, value) {
  switch (key) {
    case 'host':
      return requiredError(value, 'SMTP Host');
    case 'port':
      return portError(value);
    case 'from_name':
      return requiredError(value, 'Tên hiển thị');
    default:
      return ''; // password (optional), secure (select), username (can be email or string like "resend")
  }
}

function emailTypeFieldError(key, value) {
  switch (key) {
    case 'from_email':
      return emailError(value);
    case 'from_name':
      return requiredError(value, 'Tên hiển thị');
    case 'subject':
      return requiredError(value, 'Tiêu đề email');
    case 'body_text': {
      const req = requiredError(value, 'Nội dung email');
      if (req) return req;
      if ((value ?? '').trim().length < 10) return 'Nội dung email quá ngắn (tối thiểu 10 ký tự)';
      return '';
    }
    default:
      return '';
  }
}

/* ─── SMTP Server ─────────────────────────────────────────────────────────── */

const SMTP_FIELDS = [
  { key: 'host',       label: 'SMTP Host',     type: 'text',     placeholder: 'smtp.gmail.com',    fullWidth: true  },
  { key: 'port',       label: 'SMTP Port',     type: 'number',   placeholder: '587',               fullWidth: false },
  { key: 'secure',     label: 'Bảo mật',       type: 'select',   options: ['STARTTLS','SSL/TLS','Không'], fullWidth: false },
  { key: 'username',   label: 'Tên đăng nhập', type: 'text',     placeholder: 'Nhập tên đăng nhập (vd: abc@gmail.com hoặc resend)...', fullWidth: true  },
  { key: 'password',   label: 'Mật khẩu',      type: 'password', placeholder: 'Nhập mật khẩu ứng dụng...', fullWidth: true  },
  { key: 'from_email', label: 'Email người gửi', type: 'email',  placeholder: '(để trống = dùng Tên đăng nhập ở trên)', fullWidth: true,
    hint: 'Với Gmail, địa chỉ này bắt buộc phải trùng với Tên đăng nhập ở trên — nếu khác, Gmail sẽ từ chối gửi dù kết nối vẫn thành công.' },
  { key: 'from_name',  label: 'Tên hiển thị',  type: 'text',     placeholder: 'JLPT Platform',   fullWidth: true  },
];

/* ─── 3 loại email ────────────────────────────────────────────────────────── */

const EMAIL_TYPE_FIELDS = [
  { key: 'from_email', label: 'Email người gửi', type: 'email',    placeholder: 'jlptelearningplatform@gmail.com', fullWidth: true  },
  { key: 'from_name',  label: 'Tên hiển thị',    type: 'text',     placeholder: 'JLPT Platform',   fullWidth: false },
  { key: 'subject',    label: 'Tiêu đề email',   type: 'text',     placeholder: 'Nhập tiêu đề...', fullWidth: false },
  { key: 'body_text',  label: 'Nội dung email',  type: 'textarea', placeholder: 'Nhập lời nhắn hiển thị trong email…', fullWidth: true },
];

/* ─── Biến placeholder khả dụng theo từng nhóm email ──────────────────────────
 * Chỉ là chữ thay thế trong LỜI NHẮN. Mã OTP / nút bấm / link do hệ thống tự chèn
 * vào khung email, admin không cần nhập. */

const PLACEHOLDERS_BY_GROUP = {
  email_register: [
    { token: '{{expiry_minutes}}', label: 'Số phút hết hạn' },
  ],
  email_otp: [],
  email_reset: [
    { token: '{{expiry_hours}}',   label: 'Số giờ hết hạn' },
  ],
};

// Biến dùng chung cho mọi loại email (BE tự thay khi gửi).
const COMMON_PLACEHOLDERS = [
  { token: '{{platform_name}}', label: 'Tên nền tảng' },
  { token: '{{current_year}}',  label: 'Năm hiện tại' },
  { token: '{{support_email}}', label: 'Email hỗ trợ' },
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
  const [errors,    setErrors]  = useState({});
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

  function set(key, val) {
    setForm((f) => ({ ...f, [key]: val }));
    if (errors[key]) setErrors((e) => ({ ...e, [key]: '' }));
  }

  function handleBlur(key) {
    setErrors((e) => ({ ...e, [key]: smtpFieldError(key, form[key] ?? '') }));
  }

  /** Trả true nếu hợp lệ; đồng thời set errors để hiện inline. */
  function validate() {
    const next = {};
    SMTP_FIELDS.forEach((f) => {
      const msg = smtpFieldError(f.key, form[f.key] ?? '');
      if (msg) next[f.key] = msg;
    });
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSave(e) {
    e.preventDefault();
    if (!validate()) { addToast('error', 'Vui lòng sửa các trường được đánh dấu.'); return; }
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

  async function handleTest(e) {
    if (e) e.preventDefault();
    if (!validate()) { addToast('error', 'Vui lòng sửa các trường được đánh dấu.'); return; }
    setTesting(true);
    try {
      // Gửi cấu hình hiện tại để test, KHÔNG lưu vào DB
      const payload = {
        host: form.host,
        port: form.port,
        secure: form.secure,
        username: form.username,
        password: form.password
      };
      await testSmtp(payload);
      addToast('success', 'Kết nối SMTP thành công ✓ (Hãy bấm Lưu cấu hình nếu muốn áp dụng)');
    } catch (err) {
      addToast('error', `Lỗi kết nối SMTP: ${err?.response?.data?.message ?? 'Vui lòng kiểm tra lại cấu hình (hoặc Mật khẩu ứng dụng)'}`);
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
                  <p className="ast-field-hint">
                    {form[f.key] === '********' 
                      ? <span style={{ color: 'green' }}>✓ Đã thiết lập mật khẩu (để trống nếu không muốn đổi)</span>
                      : <span style={{ color: 'red' }}>⚠️ Chưa thiết lập mật khẩu!</span>
                    }
                  </p>
                  <p className="ast-field-hint" style={{ color: '#d97706', marginTop: '4px' }}>
                    * Nếu dùng Gmail, bạn bắt buộc phải dùng <strong>Mật khẩu ứng dụng (App Password)</strong> thay vì mật khẩu đăng nhập thông thường.
                  </p>
                </div>

              ) : (
                <>
                  <input
                    id={`smtp-${f.key}`}
                    className={`ast-input${errors[f.key] ? ' ast-input--err' : ''}`}
                    type={f.type}
                    placeholder={f.placeholder}
                    value={form[f.key] ?? ''}
                    onChange={(e) => set(f.key, e.target.value)}
                    onBlur={() => handleBlur(f.key)}
                    aria-invalid={errors[f.key] ? 'true' : undefined}
                  />
                  {f.hint && <p className="ast-field-hint">{f.hint}</p>}
                </>
              )}
              {errors[f.key] && <p className="ast-field-error">{errors[f.key]}</p>}
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
  const [errors,    setErrors]  = useState({});
  const [isLoading, setLoading]  = useState(true);
  const [isSaving,  setSaving]   = useState(false);
  const bodyRef = useRef(null);

  const placeholders = [...(PLACEHOLDERS_BY_GROUP[group] ?? []), ...COMMON_PLACEHOLDERS];

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

  function set(key, val) {
    setForm((f) => ({ ...f, [key]: val }));
    if (errors[key]) setErrors((e) => ({ ...e, [key]: '' }));
  }

  /** Chèn token biến vào ô Nội dung tại vị trí con trỏ (hoặc cuối nếu chưa focus). */
  function insertPlaceholder(token) {
    const cur = form.body_text ?? '';
    const el = bodyRef.current;
    if (!el) { set('body_text', cur + token); return; }
    const start = el.selectionStart ?? cur.length;
    const end   = el.selectionEnd ?? cur.length;
    set('body_text', cur.slice(0, start) + token + cur.slice(end));
    requestAnimationFrame(() => {
      el.focus();
      const pos = start + token.length;
      el.setSelectionRange(pos, pos);
    });
  }

  function handleBlur(key) {
    setErrors((e) => ({ ...e, [key]: emailTypeFieldError(key, form[key] ?? '') }));
  }

  function validate() {
    const next = {};
    EMAIL_TYPE_FIELDS.forEach((f) => {
      const msg = emailTypeFieldError(f.key, form[f.key] ?? '');
      if (msg) next[f.key] = msg;
    });
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSave(e) {
    e.preventDefault();
    if (!validate()) { addToast('error', 'Vui lòng sửa các trường được đánh dấu.'); return; }
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

              {f.type === 'textarea' ? (
                <>
                  <textarea
                    id={`${group}-${f.key}`}
                    ref={bodyRef}
                    className={`ast-input ast-textarea${errors[f.key] ? ' ast-input--err' : ''}`}
                    rows={5}
                    placeholder={f.placeholder}
                    value={form[f.key] ?? ''}
                    onChange={(e) => set(f.key, e.target.value)}
                    onBlur={() => handleBlur(f.key)}
                    aria-invalid={errors[f.key] ? 'true' : undefined}
                    style={{ minHeight: 120, resize: 'vertical', lineHeight: 1.6 }}
                  />
                  <div className="ast-placeholder-chips" style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 8 }}>
                    {placeholders.map((p) => (
                      <button
                        key={p.token}
                        type="button"
                        className="ast-ph-chip"
                        title={`Chèn ${p.label}`}
                        onClick={() => insertPlaceholder(p.token)}
                        style={{
                          padding: '3px 10px', borderRadius: 999, cursor: 'pointer',
                          border: '1px solid var(--color-border)', background: 'var(--color-bg)',
                          font: '12px/1.4 var(--font-base)', color: 'var(--color-text-sub)',
                        }}
                      >
                        {p.label}
                      </button>
                    ))}
                  </div>
                  <p className="ast-field-hint">
                    Chỉ nhập lời nhắn dạng chữ. Mã OTP, nút bấm và link do hệ thống tự chèn. Bấm nhãn để chèn biến động (tự thay khi gửi).
                  </p>
                </>
              ) : (
                <input
                  id={`${group}-${f.key}`}
                  className={`ast-input${errors[f.key] ? ' ast-input--err' : ''}`}
                  type={f.type}
                  placeholder={f.placeholder}
                  value={form[f.key] ?? ''}
                  onChange={(e) => set(f.key, e.target.value)}
                  onBlur={() => handleBlur(f.key)}
                  aria-invalid={errors[f.key] ? 'true' : undefined}
                />
              )}
              {errors[f.key] && <p className="ast-field-error">{errors[f.key]}</p>}
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
