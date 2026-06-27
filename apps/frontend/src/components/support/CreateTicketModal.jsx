import { useState, useEffect, useRef } from 'react';
import { createTicket } from '../../api/studentService';
import './CreateTicketModal.css';

const CATEGORIES = ['Tài khoản', 'Kỹ thuật', 'Học tập', 'Thanh toán', 'Khác'];
const PRIORITIES = [
  { value: 'low', label: 'Thấp' },
  { value: 'normal', label: 'Bình thường' },
  { value: 'high', label: 'Cao' },
  { value: 'urgent', label: 'Khẩn' },
];

function validate(form) {
  const errs = {};
  if (!form.subject.trim()) errs.subject = 'Tiêu đề không được để trống';
  else if (form.subject.length > 255) errs.subject = 'Tiêu đề tối đa 255 ký tự';
  if (!form.content.trim()) errs.content = 'Nội dung không được để trống';
  if (form.category && form.category.length > 50) errs.category = 'Danh mục tối đa 50 ký tự';
  return errs;
}

export default function CreateTicketModal({ onClose, onCreated }) {
  const [form, setForm] = useState({
    subject: '',
    category: 'Tài khoản',
    priority: 'normal',
    content: '',
  });
  const [errors, setErrors] = useState({});
  const [isSaving, setSaving] = useState(false);
  const [serverError, setServerError] = useState('');
  const firstFieldRef = useRef(null);

  useEffect(() => {
    firstFieldRef.current?.focus();
    function onKey(e) {
      if (e.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  function handleChange(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
    if (errors[field]) setErrors((e) => ({ ...e, [field]: '' }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errs = validate(form);
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }
    setSaving(true);
    setServerError('');
    try {
      const created = await createTicket(form);
      onCreated(created);
    } catch (err) {
      setServerError(err?.response?.data?.message ?? 'Không thể gửi yêu cầu. Vui lòng thử lại.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="ctk-overlay" onMouseDown={onClose}>
      <div
        className="ctk-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="ctk-title"
        onMouseDown={(e) => e.stopPropagation()}
      >
        <div className="ctk-head">
          <h2 id="ctk-title" className="ctk-title">Tạo yêu cầu hỗ trợ</h2>
          <button type="button" className="ctk-close" onClick={onClose} aria-label="Đóng">×</button>
        </div>

        <form className="ctk-body" onSubmit={handleSubmit}>
          {serverError && <div className="ctk-server-error" role="alert">{serverError}</div>}

          <div className="ctk-field">
            <label className="ctk-label" htmlFor="ctk-subject">
              Tiêu đề <span className="ctk-required">*</span>
              <span className="ctk-count">{form.subject.length}/255</span>
            </label>
            <input
              id="ctk-subject"
              ref={firstFieldRef}
              className={`ctk-input${errors.subject ? ' ctk-input--err' : ''}`}
              type="text"
              maxLength={255}
              value={form.subject}
              onChange={(e) => handleChange('subject', e.target.value)}
              placeholder="Tóm tắt ngắn gọn vấn đề của bạn…"
              aria-invalid={!!errors.subject}
              aria-describedby={errors.subject ? 'ctk-subject-err' : undefined}
            />
            {errors.subject && <span id="ctk-subject-err" className="ctk-field-error">{errors.subject}</span>}
          </div>

          <div className="ctk-row">
            <div className="ctk-field">
              <label className="ctk-label" htmlFor="ctk-category">Danh mục</label>
              <select
                id="ctk-category"
                className="ctk-input"
                value={form.category}
                onChange={(e) => handleChange('category', e.target.value)}
              >
                {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>

            <fieldset className="ctk-field ctk-fieldset">
              <legend className="ctk-label">Mức độ ưu tiên</legend>
              <div className="ctk-radios">
                {PRIORITIES.map((p) => (
                  <label key={p.value} className={`ctk-radio${form.priority === p.value ? ' ctk-radio--on' : ''}`}>
                    <input
                      type="radio"
                      name="priority"
                      value={p.value}
                      checked={form.priority === p.value}
                      onChange={(e) => handleChange('priority', e.target.value)}
                    />
                    {p.label}
                  </label>
                ))}
              </div>
            </fieldset>
          </div>

          <div className="ctk-field">
            <label className="ctk-label" htmlFor="ctk-content">Nội dung <span className="ctk-required">*</span></label>
            <textarea
              id="ctk-content"
              className={`ctk-textarea${errors.content ? ' ctk-input--err' : ''}`}
              rows={5}
              value={form.content}
              onChange={(e) => handleChange('content', e.target.value)}
              placeholder="Mô tả chi tiết vấn đề bạn gặp phải…"
              aria-invalid={!!errors.content}
              aria-describedby={errors.content ? 'ctk-content-err' : undefined}
            />
            {errors.content && <span id="ctk-content-err" className="ctk-field-error">{errors.content}</span>}
          </div>

          <div className="ctk-actions">
            <button type="button" className="ctk-cancel" onClick={onClose} disabled={isSaving}>Hủy</button>
            <button type="submit" className="ctk-submit" disabled={isSaving} aria-busy={isSaving}>
              {isSaving ? 'Đang gửi…' : 'Gửi yêu cầu →'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
