import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppDispatch } from '../../store/hooks';
import { logoutThunk } from '../../store/slices/authSlice';
import { useCountdown } from '../../hooks/useCountdown';
import TopNav from '../../components/layout/TopNav';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { requestEmailChange, confirmEmailChange } from '../../api/studentService';
import './ChangeEmail.css';

function extractMessage(err, fallback) {
  return err?.response?.data?.message ?? fallback;
}

export default function ChangeEmail() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { toasts, addToast, removeToast } = useToast();

  const [step, setStep] = useState('request'); // 'request' | 'confirm'
  const [newEmail, setNewEmail] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [apiError, setApiErr] = useState('');
  const [isSubmitting, setSubmitting] = useState(false);
  const [cooldown, startCooldown] = useCountdown();

  async function handleRequestOtp(e) {
    e.preventDefault();
    if (!newEmail.trim() || !currentPassword) return;
    setSubmitting(true);
    setApiErr('');
    try {
      await requestEmailChange({ newEmail: newEmail.trim(), currentPassword });
      setStep('confirm');
      startCooldown(60);
    } catch (err) {
      setApiErr(extractMessage(err, 'Gửi mã OTP thất bại. Vui lòng thử lại.'));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleResendOtp() {
    if (cooldown > 0 || isSubmitting) return;
    setSubmitting(true);
    setApiErr('');
    try {
      await requestEmailChange({ newEmail: newEmail.trim(), currentPassword });
      startCooldown(60);
    } catch (err) {
      setApiErr(extractMessage(err, 'Gửi lại mã OTP thất bại. Vui lòng thử lại.'));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleConfirm(e) {
    e.preventDefault();
    if (!otpCode.trim()) return;
    setSubmitting(true);
    setApiErr('');
    try {
      await confirmEmailChange({ newEmail: newEmail.trim(), otpCode: otpCode.trim() });
      addToast('success', 'Đổi email thành công! Đang đăng xuất...');
      setTimeout(async () => {
        await dispatch(logoutThunk());
        navigate('/login');
      }, 1500);
    } catch (err) {
      setApiErr(extractMessage(err, 'Mã OTP không đúng hoặc đã hết hạn.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="ce-page">
      <TopNav activeTab="" />

      <main className="ce-body">
        <Link to="/profile" className="ce-back-link">← Quay lại Hồ sơ</Link>
        <h1 className="ce-title">Đổi Email</h1>

        <div className="ce-card">
          {apiError && <div className="ce-api-error" role="alert">{apiError}</div>}

          {step === 'request' ? (
            <form className="ce-form" onSubmit={handleRequestOtp} noValidate>
              <div className="ce-field">
                <label className="ce-label" htmlFor="ce-new-email">
                  Email mới <span className="ce-required">*</span>
                </label>
                <input
                  id="ce-new-email"
                  className="ce-input"
                  type="email"
                  value={newEmail}
                  onChange={(e) => setNewEmail(e.target.value)}
                  autoComplete="email"
                  placeholder="email-moi@example.com"
                />
              </div>

              <div className="ce-field">
                <label className="ce-label" htmlFor="ce-current-password">
                  Mật khẩu hiện tại <span className="ce-required">*</span>
                </label>
                <input
                  id="ce-current-password"
                  className="ce-input"
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  autoComplete="current-password"
                />
              </div>

              <button type="submit" className="ce-submit-btn" disabled={isSubmitting} aria-busy={isSubmitting}>
                {isSubmitting && <span className="ce-spinner" aria-hidden="true" />}
                {isSubmitting ? 'Đang gửi…' : 'Gửi mã OTP'}
              </button>
            </form>
          ) : (
            <form className="ce-form" onSubmit={handleConfirm} noValidate>
              <p className="ce-otp-hint">
                Đã gửi mã OTP đến <strong>{newEmail}</strong>. Vui lòng kiểm tra hộp thư.
              </p>

              <div className="ce-field">
                <label className="ce-label" htmlFor="ce-otp">
                  Mã OTP <span className="ce-required">*</span>
                </label>
                <input
                  id="ce-otp"
                  className="ce-input ce-input--otp"
                  type="text"
                  inputMode="numeric"
                  maxLength={6}
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ''))}
                  autoComplete="one-time-code"
                  placeholder="6 chữ số"
                />
              </div>

              <button
                type="button"
                className="ce-resend-link"
                onClick={handleResendOtp}
                disabled={isSubmitting || cooldown > 0}
              >
                {cooldown > 0 ? `Gửi lại mã (${cooldown}s)` : 'Gửi lại mã OTP'}
              </button>

              <button type="submit" className="ce-submit-btn" disabled={isSubmitting} aria-busy={isSubmitting}>
                {isSubmitting && <span className="ce-spinner" aria-hidden="true" />}
                {isSubmitting ? 'Đang xử lý…' : 'Xác nhận đổi email'}
              </button>
            </form>
          )}
        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
