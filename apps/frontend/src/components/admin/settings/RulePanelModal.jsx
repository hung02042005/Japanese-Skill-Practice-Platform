import { useState, useEffect, useRef } from 'react';
import { createNotificationRule, updateNotificationRule } from '../../../api/adminService';

export const MILESTONES = [
  { value: 'streak_7',  label: 'Đạt chuỗi học tập 7 ngày' },
  { value: 'streak_10', label: 'Đạt chuỗi học tập 10 ngày' },
  { value: 'streak_30', label: 'Đạt chuỗi học tập 30 ngày' },
  { value: 'exam_pass', label: 'Thi thử đạt điểm vượt ngưỡng' },
  { value: 'level_up',  label: 'Hoàn thành khóa học một cấp' },
  { value: 'words_100', label: 'Học được 100 từ vựng' },
  { value: 'words_500', label: 'Học được 500 từ vựng' },
];

const EMPTY = { milestone: '', templateTitle: '', templateContent: '', channel: 'in_app' };

function CloseIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <line x1="18" y1="6" x2="6"  y2="18" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
      <line x1="6"  y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
    </svg>
  );
}

/**
 * Props:
 *   rule    — null (create) | ruleObj (edit)
 *   onClose — fn
 *   onSaved — fn(savedRule, isEdit)
 *   addToast — fn
 */
export function RulePanelModal({ rule, onClose, onSaved, addToast }) {
  const isEdit      = Boolean(rule);
  const [form,   setForm]  = useState(isEdit ? { ...rule } : { ...EMPTY });
  const [errors, setErrors]= useState({});
  const [saving, setSaving]= useState(false);
  const backdropRef = useRef(null);

  useEffect(() => {
    function onKey(e) { if (e.key === 'Escape') onClose(); }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  function set(key, val) { setForm((f) => ({ ...f, [key]: val })); }

  function validate() {
    const e = {};
    if (!form.milestone)               e.milestone        = 'Vui lòng chọn điều kiện';
    if (!form.templateTitle?.trim())   e.templateTitle    = 'Tiêu đề không được để trống';
    else if (form.templateTitle.length > 150) e.templateTitle = 'Tối đa 150 ký tự';
    if (!form.templateContent?.trim()) e.templateContent  = 'Nội dung không được để trống';
    else if (form.templateContent.length > 500) e.templateContent = 'Tối đa 500 ký tự';
    return e;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setSaving(true);
    try {
      let saved;
      if (isEdit) {
        saved = await updateNotificationRule(rule.ruleId, form);
        saved = saved ?? { ...rule, ...form };
      } else {
        saved = await createNotificationRule(form);
      }
      onSaved(saved, isEdit);
    } catch {
      addToast('error', 'Lưu quy tắc thất bại');
    } finally {
      setSaving(false);
    }
  }

  function handleBackdrop(e) { if (e.target === backdropRef.current) onClose(); }

  return (
    <div
      className="ast-modal-backdrop"
      ref={backdropRef}
      onClick={handleBackdrop}
      role="dialog"
      aria-modal="true"
      aria-label={isEdit ? 'Chỉnh sửa quy tắc' : 'Tạo quy tắc mới'}
    >
      <div className="ast-modal">
        <div className="ast-modal-header">
          <h3 className="ast-modal-title">{isEdit ? 'Chỉnh sửa quy tắc' : 'Tạo quy tắc mới'}</h3>
          <button className="ast-modal-close" onClick={onClose} aria-label="Đóng">
            <CloseIcon />
          </button>
        </div>

        <form className="ast-modal-form" onSubmit={handleSubmit} noValidate>
          {/* Milestone */}
          <div className="ast-field">
            <label className="ast-field-label" htmlFor="rule-milestone">Điều kiện kích hoạt</label>
            <select
              id="rule-milestone"
              className={`ast-input${errors.milestone ? ' ast-input--err' : ''}`}
              value={form.milestone}
              onChange={(e) => set('milestone', e.target.value)}
            >
              <option value="">-- Chọn điều kiện --</option>
              {MILESTONES.map((m) => (
                <option key={m.value} value={m.value}>{m.label}</option>
              ))}
            </select>
            {errors.milestone && <span className="ast-field-error">{errors.milestone}</span>}
          </div>

          {/* Title */}
          <div className="ast-field">
            <label className="ast-field-label" htmlFor="rule-title">
              Tiêu đề thông báo
              <span className="ast-char-count">{form.templateTitle?.length ?? 0}/150</span>
            </label>
            <input
              id="rule-title"
              className={`ast-input${errors.templateTitle ? ' ast-input--err' : ''}`}
              type="text"
              maxLength={150}
              placeholder="Chúc mừng chuỗi học tập 10 ngày!"
              value={form.templateTitle ?? ''}
              onChange={(e) => set('templateTitle', e.target.value)}
            />
            {errors.templateTitle && <span className="ast-field-error">{errors.templateTitle}</span>}
          </div>

          {/* Content */}
          <div className="ast-field">
            <label className="ast-field-label" htmlFor="rule-content">
              Nội dung thông báo
              <span className="ast-char-count">{form.templateContent?.length ?? 0}/500</span>
            </label>
            <textarea
              id="rule-content"
              className={`ast-textarea${errors.templateContent ? ' ast-input--err' : ''}`}
              rows={4}
              maxLength={500}
              placeholder="Sử dụng {tên} để chèn tên người dùng"
              value={form.templateContent ?? ''}
              onChange={(e) => set('templateContent', e.target.value)}
            />
            {errors.templateContent && <span className="ast-field-error">{errors.templateContent}</span>}
          </div>

          {/* Channel */}
          <div className="ast-field">
            <span className="ast-field-label">Kênh gửi</span>
            <div className="ast-radio-group">
              {[
                { value: 'in_app', label: 'Trong ứng dụng' },
                { value: 'email',  label: 'Email' },
              ].map((opt) => (
                <label key={opt.value} className="ast-radio-label">
                  <input
                    type="radio"
                    name="rule-channel"
                    value={opt.value}
                    checked={form.channel === opt.value}
                    onChange={() => set('channel', opt.value)}
                  />
                  {opt.label}
                </label>
              ))}
            </div>
          </div>

          <div className="ast-modal-footer">
            <button type="button" className="ast-btn ast-btn--ghost" onClick={onClose}>Hủy</button>
            <button type="submit" className="ast-btn ast-btn--primary" disabled={saving}>
              {saving && <span className="ast-spinner ast-spinner--white" aria-hidden="true" />}
              {saving ? 'Đang lưu…' : isEdit ? 'Lưu thay đổi' : 'Tạo quy tắc'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
