import { useState } from 'react';
import ManagerTopNav from '@/shared/components/layout/ManagerTopNav';
import StaffPageHero from '@/features/management/components/staff/StaffPageHero';
import { useToast, ToastContainer } from '@/shared/components/common/Toast';
import { sendBroadcast } from '@/shared/api/staffService';
import './ManagerNotifications.css';

const TYPES = [
  { value: 'news', label: 'Tin tức' }, { value: 'warning', label: 'Cảnh báo' },
  { value: 'promotion', label: 'Khuyến mãi' }, { value: 'system', label: 'Hệ thống' },
  { value: 'achievement', label: 'Thành tích' }, { value: 'reminder', label: 'Nhắc nhở' },
];
const CHANNELS = [
  { value: 'in_app', label: 'Chỉ In-app' }, { value: 'email', label: 'Chỉ Email' }, { value: 'both', label: 'Cả hai' },
];
const LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];
const today = new Date().toISOString().slice(0, 10);

export default function ManagerNotifications() {
  const { toasts, addToast, removeToast } = useToast();
  const [form, setForm] = useState({
    title: '', content: '', notificationType: 'system', channel: 'in_app',
    targetAll: true, targetLevel: 'N5', scheduleNow: true, scheduledDate: '', scheduledTime: '',
  });
  const [errors, setErrors] = useState({});
  const [isSending, setSending] = useState(false);
  const [confirm, setConfirm] = useState(false);

  const set = (k, v) => { setForm((f) => ({ ...f, [k]: v })); if (errors[k]) setErrors((e) => ({ ...e, [k]: '' })); };

  function validate() {
    const er = {};
    if (!form.title.trim()) er.title = 'Tiêu đề không được để trống';
    else if (form.title.length > 255) er.title = 'Tối đa 255 ký tự';
    if (!form.content.trim()) er.content = 'Nội dung không được để trống';
    if (!form.scheduleNow) {
      if (!form.scheduledDate || !form.scheduledTime) er.schedule = 'Chọn ngày và giờ gửi';
      else if (new Date(`${form.scheduledDate}T${form.scheduledTime}`) <= new Date()) er.schedule = 'Thời gian hẹn phải ở tương lai';
    }
    return er;
  }

  function attemptSend() {
    const er = validate();
    if (Object.keys(er).length) { setErrors(er); return; }
    if (form.targetAll) { setConfirm(true); return; }
    doSend();
  }

  async function doSend() {
    setConfirm(false);
    setSending(true);
    try {
      const payload = {
        title: form.title.trim(), content: form.content.trim(),
        notificationType: form.notificationType, channel: form.channel,
        targetJlptLevel: form.targetAll ? 'ALL' : form.targetLevel,
      };
      if (!form.scheduleNow) payload.scheduledAt = `${form.scheduledDate}T${form.scheduledTime}:00`;
      const { jobId } = await sendBroadcast(payload);
      addToast('success', `Đã gửi yêu cầu broadcast (Job ${jobId}). Hệ thống đang xử lý trong nền.`);
      setForm((f) => ({ ...f, title: '', content: '', scheduledDate: '', scheduledTime: '', scheduleNow: true }));
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Không thể gửi thông báo.');
    } finally { setSending(false); }
  }

  return (
    <div className="nfs-page">
      <ManagerTopNav activeTab="manager-notifications" />
      <main className="nfs-body">
        <StaffPageHero accent="pink" title="Gửi Thông Báo" subtitle="Soạn và broadcast thông báo hệ thống đến học viên theo cấp độ và kênh"
          icon={<svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M9 20a15 15 0 0 1 30 0v8l4 6H5l4-6z"/><path d="M20 40a4 4 0 0 0 8 0"/></svg>} />

        <form className="nfs-card nfs-form" onSubmit={(e) => { e.preventDefault(); attemptSend(); }}>
          <div className="nfs-field">
            <label className="nfs-field-label" htmlFor="nfs-title">Tiêu đề <span className="nfs-req">*</span>
              <span className="nfs-char">{form.title.length}/255</span></label>
            <input id="nfs-title" className={`nfs-input${errors.title ? ' nfs-input--err' : ''}`} maxLength={255}
              value={form.title} onChange={(e) => set('title', e.target.value)} placeholder="Tiêu đề thông báo…" />
            {errors.title && <span className="nfs-field-error">{errors.title}</span>}
          </div>

          <div className="nfs-field">
            <label className="nfs-field-label" htmlFor="nfs-content">Nội dung <span className="nfs-req">*</span></label>
            <textarea id="nfs-content" className={`nfs-textarea${errors.content ? ' nfs-input--err' : ''}`} rows={5}
              value={form.content} onChange={(e) => set('content', e.target.value)} placeholder="Nội dung thông báo…" />
            {errors.content && <span className="nfs-field-error">{errors.content}</span>}
          </div>

          <fieldset className="nfs-field nfs-fieldset">
            <legend className="nfs-field-label">Loại thông báo <span className="nfs-req">*</span></legend>
            <div className="nfs-radios">
              {TYPES.map((t) => (
                <label key={t.value} className={`nfs-radio${form.notificationType === t.value ? ' nfs-radio--on' : ''}`}>
                  <input type="radio" name="ntype" value={t.value} checked={form.notificationType === t.value} onChange={(e) => set('notificationType', e.target.value)} />{t.label}
                </label>
              ))}
            </div>
          </fieldset>

          <fieldset className="nfs-field nfs-fieldset">
            <legend className="nfs-field-label">Kênh gửi <span className="nfs-req">*</span></legend>
            <div className="nfs-radios">
              {CHANNELS.map((c) => (
                <label key={c.value} className={`nfs-radio${form.channel === c.value ? ' nfs-radio--on' : ''}`}>
                  <input type="radio" name="channel" value={c.value} checked={form.channel === c.value} onChange={(e) => set('channel', e.target.value)} />{c.label}
                </label>
              ))}
            </div>
          </fieldset>

          <fieldset className="nfs-field nfs-fieldset">
            <legend className="nfs-field-label">Đối tượng nhận</legend>
            <div className="nfs-radios">
              <label className={`nfs-radio${form.targetAll ? ' nfs-radio--on' : ''}`}>
                <input type="radio" name="target" checked={form.targetAll} onChange={() => set('targetAll', true)} />Tất cả học viên
              </label>
              <label className={`nfs-radio${!form.targetAll ? ' nfs-radio--on' : ''}`}>
                <input type="radio" name="target" checked={!form.targetAll} onChange={() => set('targetAll', false)} />Theo Level
              </label>
              {!form.targetAll && (
                <select className="nfs-select nfs-select--inline" value={form.targetLevel} onChange={(e) => set('targetLevel', e.target.value)} aria-label="Chọn JLPT level">
                  {LEVELS.map((l) => <option key={l} value={l}>{l}</option>)}
                </select>
              )}
            </div>
          </fieldset>

          <fieldset className="nfs-field nfs-fieldset">
            <legend className="nfs-field-label">Thời gian gửi</legend>
            <div className="nfs-radios">
              <label className={`nfs-radio${form.scheduleNow ? ' nfs-radio--on' : ''}`}>
                <input type="radio" name="sched" checked={form.scheduleNow} onChange={() => set('scheduleNow', true)} />Gửi ngay
              </label>
              <label className={`nfs-radio${!form.scheduleNow ? ' nfs-radio--on' : ''}`}>
                <input type="radio" name="sched" checked={!form.scheduleNow} onChange={() => set('scheduleNow', false)} />Hẹn giờ
              </label>
              {!form.scheduleNow && (
                <>
                  <input className="nfs-input nfs-input--inline" type="date" min={today} value={form.scheduledDate} onChange={(e) => set('scheduledDate', e.target.value)} aria-label="Ngày gửi" />
                  <input className="nfs-input nfs-input--inline" type="time" value={form.scheduledTime} onChange={(e) => set('scheduledTime', e.target.value)} aria-label="Giờ gửi" />
                </>
              )}
            </div>
            {errors.schedule && <span className="nfs-field-error">{errors.schedule}</span>}
          </fieldset>

          <div className="nfs-form-footer">
            <button type="submit" className="nfs-btn-send" disabled={isSending} aria-busy={isSending}>
              {isSending && <span className="nfs-spinner" aria-hidden="true" />}
              {isSending ? 'Đang gửi…' : 'Gửi thông báo →'}
            </button>
          </div>
        </form>
      </main>

      {confirm && (
        <div className="nfs-confirm-overlay" onMouseDown={() => setConfirm(false)}>
          <div className="nfs-confirm" role="alertdialog" aria-modal="true" aria-labelledby="nfs-confirm-title" onMouseDown={(e) => e.stopPropagation()}>
            <h3 id="nfs-confirm-title" className="nfs-confirm-title">Gửi đến tất cả học viên?</h3>
            <p className="nfs-confirm-text">Bạn sắp broadcast đến TẤT CẢ học viên. Hành động này không thể hoàn tác.</p>
            <div className="nfs-confirm-actions">
              <button type="button" className="nfs-confirm-cancel" onClick={() => setConfirm(false)}>Hủy</button>
              <button type="button" className="nfs-btn-send" onClick={doSend}>Xác nhận gửi</button>
            </div>
          </div>
        </div>
      )}

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
