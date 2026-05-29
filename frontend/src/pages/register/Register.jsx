import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Register.css';

function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: '', email: '', password: '', confirmPassword: '' });
  const [fieldErrors, setFieldErrors] = useState({});
  const [isDone, setIsDone] = useState(false);

  function setField(name, value) {
    setForm((prev) => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) setFieldErrors((prev) => ({ ...prev, [name]: null }));
  }

  function validate() {
    const errors = {};
    if (!form.fullName.trim()) errors.fullName = 'Vui lòng nhập họ tên';
    if (!form.email.trim()) errors.email = 'Vui lòng nhập email';
    else if (!/\S+@\S+\.\S+/.test(form.email)) errors.email = 'Email không hợp lệ';
    if (!form.password) errors.password = 'Vui lòng nhập mật khẩu';
    else if (form.password.length < 8) errors.password = 'Mật khẩu phải có ít nhất 8 ký tự';
    else if (!/[A-Z]/.test(form.password)) errors.password = 'Mật khẩu phải có ít nhất 1 chữ hoa';
    else if (!/[0-9]/.test(form.password)) errors.password = 'Mật khẩu phải có ít nhất 1 số';
    if (!form.confirmPassword) errors.confirmPassword = 'Vui lòng xác nhận mật khẩu';
    else if (form.password !== form.confirmPassword) errors.confirmPassword = 'Mật khẩu xác nhận không khớp';
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (validate()) setIsDone(true);
  }

  if (isDone) {
    return (
      <div className="reg-page">
        <div className="reg-container">
          <div className="fp-brand">
            <div className="brand-logo">日</div>
            <h1 className="brand-title">JLPT Platform</h1>
          </div>
          <div className="fp-card">
            <div className="reg-success">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <rect width="48" height="48" rx="24" fill="#E8F5E9"/>
                <path d="M14 24l7 7 13-13" stroke="#1AAE39" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              <h2 className="fp-card-title">Đăng ký thành công</h2>
              <p className="fp-card-desc">
                Vui lòng kiểm tra email <strong>{form.email}</strong> để xác minh tài khoản.
              </p>
              <a className="btn btn-primary" href="/login">Đi tới đăng nhập</a>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="reg-page">
      <div className="reg-container">
        <div className="fp-brand">
          <div className="brand-logo">日</div>
          <h1 className="brand-title">JLPT Platform</h1>
        </div>
        <div className="fp-card">
          <h2 className="fp-card-title">Tạo tài khoản</h2>
          <p className="fp-card-desc">Bắt đầu hành trình học tiếng Nhật của bạn.</p>

          <form onSubmit={handleSubmit} noValidate>
            <div className={`form-group ${fieldErrors.fullName ? 'has-error' : ''}`}>
              <label className="form-label" htmlFor="reg-name">Họ và tên</label>
              <input id="reg-name" className="form-input" type="text" placeholder="Nguyễn Văn A"
                value={form.fullName} onChange={(e) => setField('fullName', e.target.value)} autoFocus />
              {fieldErrors.fullName && <span className="field-error">{fieldErrors.fullName}</span>}
            </div>

            <div className={`form-group ${fieldErrors.email ? 'has-error' : ''}`}>
              <label className="form-label" htmlFor="reg-email">Email</label>
              <input id="reg-email" className="form-input" type="email" placeholder="example@email.com"
                value={form.email} onChange={(e) => setField('email', e.target.value)} autoComplete="email" />
              {fieldErrors.email && <span className="field-error">{fieldErrors.email}</span>}
            </div>

            <div className={`form-group ${fieldErrors.password ? 'has-error' : ''}`}>
              <label className="form-label" htmlFor="reg-pass">Mật khẩu</label>
              <input id="reg-pass" className="form-input" type="password" placeholder="Ít nhất 8 ký tự, 1 hoa, 1 số"
                value={form.password} onChange={(e) => setField('password', e.target.value)} autoComplete="new-password" />
              {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
            </div>

            <div className={`form-group ${fieldErrors.confirmPassword ? 'has-error' : ''}`}>
              <label className="form-label" htmlFor="reg-confirm">Xác nhận mật khẩu</label>
              <input id="reg-confirm" className="form-input" type="password" placeholder="Nhập lại mật khẩu"
                value={form.confirmPassword} onChange={(e) => setField('confirmPassword', e.target.value)} autoComplete="new-password" />
              {fieldErrors.confirmPassword && <span className="field-error">{fieldErrors.confirmPassword}</span>}
            </div>

            <button className="btn btn-primary" type="submit">Đăng ký</button>
          </form>

          <p className="reg-login-link">
            Đã có tài khoản? <a href="/login">Đăng nhập</a>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Register;