import { useState, useEffect, useCallback } from 'react';
import {
  listNotificationRules,
  createNotificationRule,
  updateNotificationRule,
} from '@/shared/api/adminNotificationService';
import { IcPlus, IcEdit } from '../ManageUsersIcons';
import { isBlank } from '@/shared/utils/validation';

const CHANNEL_LABELS = { in_app: 'Trong ứng dụng', email: 'Email', both: 'Cả hai' };
const CHANNEL_OPTIONS = [
  { value: 'in_app', label: 'Trong ứng dụng' },
  { value: 'email',  label: 'Email' },
  { value: 'both',   label: 'Cả hai' },
];
const RULE_KEY_RE = /^[a-z][a-z0-9_]{2,49}$/;

const EMPTY_FORM = {
  ruleKey: '', description: '', isEnabled: true,
  triggerCondition: '', channel: 'in_app',
  templateTitle: '', templateContent: '',
};

function buildPayload(rule, overrides = {}) {
  const p = {
    ruleKey:         rule.ruleKey,
    description:     rule.description || '',
    isEnabled:       rule.isEnabled ?? false,
    triggerCondition: rule.triggerCondition || '',
    templateTitle:   rule.templateTitle || '',
    templateContent: rule.templateContent || '',
    ...overrides,
  };
  const ch = overrides.channel ?? rule.channel;
  if (ch) p.channel = ch;
  return p;
}

/* ─── Form tạo / sửa (inline) ─────────────────────────────────────────────── */

function RuleForm({ mode, initial, onSubmit, onCancel, isSaving }) {
  const [form, setForm] = useState(initial);
  const [errors, setErrors] = useState({});
  const isEdit = mode === 'edit';

  function set(key, val) {
    setForm((f) => ({ ...f, [key]: val }));
    if (errors[key]) setErrors((e) => ({ ...e, [key]: '' }));
  }

  function validate() {
    const next = {};
    if (!isEdit && !RULE_KEY_RE.test(form.ruleKey)) {
      next.ruleKey = 'Rule key không hợp lệ (chữ thường/số/gạch dưới, bắt đầu bằng chữ, 3–50 ký tự).';
    }
    if (isBlank(form.description)) {
      next.description = 'Mô tả là bắt buộc.';
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;
    onSubmit(form);
  }

  return (
    <form className="ast-card ast-form-grid" onSubmit={handleSubmit} noValidate>
      <h3 className="ast-section-title ast-field--full" style={{ margin: 0 }}>
        {isEdit ? `Sửa quy tắc: ${initial.ruleKey}` : 'Tạo quy tắc thông báo mới'}
      </h3>

      <div className="ast-field">
        <label className="ast-field-label" htmlFor="nr-key">Rule key</label>
        <input
          id="nr-key"
          className={`ast-input${errors.ruleKey ? ' ast-input--err' : ''}`}
          value={form.ruleKey}
          onChange={(e) => set('ruleKey', e.target.value)}
          placeholder="vd: streak_reminder"
          disabled={isEdit}
          aria-invalid={errors.ruleKey ? 'true' : undefined}
        />
        {errors.ruleKey
          ? <p className="ast-field-error">{errors.ruleKey}</p>
          : (!isEdit && <p className="ast-field-hint">Chữ thường, số, gạch dưới; bắt đầu bằng chữ cái (3–50 ký tự)</p>)}
      </div>

      <div className="ast-field">
        <label className="ast-field-label" htmlFor="nr-channel">Kênh gửi</label>
        <select
          id="nr-channel"
          className="ast-input"
          value={form.channel || 'in_app'}
          onChange={(e) => set('channel', e.target.value)}
        >
          {CHANNEL_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      </div>

      <div className="ast-field ast-field--full">
        <label className="ast-field-label" htmlFor="nr-desc">Mô tả</label>
        <input
          id="nr-desc"
          className={`ast-input${errors.description ? ' ast-input--err' : ''}`}
          value={form.description}
          onChange={(e) => set('description', e.target.value)}
          placeholder="Mô tả ngắn về quy tắc (≤ 255 ký tự)"
          maxLength={255}
          aria-invalid={errors.description ? 'true' : undefined}
        />
        {errors.description && <p className="ast-field-error">{errors.description}</p>}
      </div>

      <div className="ast-field ast-field--full">
        <label className="ast-field-label" htmlFor="nr-cond">Điều kiện kích hoạt</label>
        <input
          id="nr-cond"
          className="ast-input"
          value={form.triggerCondition}
          onChange={(e) => set('triggerCondition', e.target.value)}
          placeholder="vd: streak_days >= 7 (≤ 100 ký tự)"
          maxLength={100}
        />
      </div>

      <div className="ast-field ast-field--full">
        <label className="ast-field-label" htmlFor="nr-title">Tiêu đề template</label>
        <input
          id="nr-title"
          className="ast-input"
          value={form.templateTitle}
          onChange={(e) => set('templateTitle', e.target.value)}
          placeholder="Tiêu đề thông báo (≤ 255 ký tự)"
          maxLength={255}
        />
      </div>

      <div className="ast-field ast-field--full">
        <label className="ast-field-label" htmlFor="nr-content">Nội dung template</label>
        <textarea
          id="nr-content"
          className="ast-input"
          style={{ minHeight: 90, resize: 'vertical', fontFamily: 'inherit' }}
          value={form.templateContent}
          onChange={(e) => set('templateContent', e.target.value)}
          placeholder="Nội dung thông báo (≤ 2000 ký tự)"
          maxLength={2000}
        />
      </div>

      <label className="ast-field ast-field--full" style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>
        <input
          type="checkbox"
          checked={!!form.isEnabled}
          onChange={(e) => set('isEnabled', e.target.checked)}
        />
        <span className="ast-field-label" style={{ margin: 0 }}>Kích hoạt quy tắc</span>
      </label>

      <div className="ast-form-footer ast-field--full">
        <button type="button" className="ast-btn ast-btn--outline" onClick={onCancel} disabled={isSaving}>
          Hủy
        </button>
        <button type="submit" className="ast-btn ast-btn--primary" disabled={isSaving}>
          {isSaving && <span className="ast-spinner ast-spinner--white" aria-hidden="true" />}
          {isSaving ? 'Đang lưu…' : (isEdit ? 'Lưu thay đổi' : 'Tạo quy tắc')}
        </button>
      </div>
    </form>
  );
}

/* ─── NotificationTab ─────────────────────────────────────────────────────── */

export function NotificationTab({ addToast }) {
  const [rules,     setRules]   = useState([]);
  const [isLoading, setLoading] = useState(true);
  const [hasError,  setError]   = useState(false);
  const [editing,   setEditing] = useState(null);   // null | { mode, initial }
  const [isSaving,  setSaving]  = useState(false);
  const [togglingKey, setToggling] = useState(null);

  const fetchRules = useCallback(() => {
    setLoading(true);
    setError(false);
    listNotificationRules()
      .then((data) => setRules(Array.isArray(data) ? data : []))
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchRules(); }, [fetchRules]);

  function openCreate() {
    setEditing({ mode: 'create', initial: { ...EMPTY_FORM } });
  }
  function openEdit(rule) {
    setEditing({
      mode: 'edit',
      initial: {
        ruleKey:         rule.ruleKey,
        description:     rule.description || '',
        isEnabled:       rule.isEnabled ?? false,
        triggerCondition: rule.triggerCondition || '',
        channel:         rule.channel || 'in_app',
        templateTitle:   rule.templateTitle || '',
        templateContent: rule.templateContent || '',
      },
    });
  }

  async function handleSubmit(form) {
    // Validate L1 đã chạy trong RuleForm (inline per-field); ở đây chỉ xử lý gọi API.
    setSaving(true);
    try {
      const payload = { ...form, description: form.description.trim() };
      if (editing.mode === 'create') {
        await createNotificationRule(payload);
        addToast('success', `Đã tạo quy tắc "${form.ruleKey}"`);
      } else {
        await updateNotificationRule(form.ruleKey, payload);
        addToast('success', `Đã cập nhật quy tắc "${form.ruleKey}"`);
      }
      setEditing(null);
      fetchRules();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Lưu thất bại. Thử lại sau.');
    } finally {
      setSaving(false);
    }
  }

  async function handleToggle(rule) {
    setToggling(rule.ruleKey);
    try {
      await updateNotificationRule(rule.ruleKey, buildPayload(rule, { isEnabled: !rule.isEnabled }));
      setRules((prev) => prev.map((r) =>
        r.ruleKey === rule.ruleKey ? { ...r, isEnabled: !r.isEnabled } : r));
      addToast('success', !rule.isEnabled
        ? `Đã bật quy tắc "${rule.ruleKey}"`
        : `Đã tắt quy tắc "${rule.ruleKey}"`);
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Cập nhật thất bại.');
    } finally {
      setToggling(null);
    }
  }

  return (
    <>
      <div className="ast-card">
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
          <div>
            <h3 className="ast-section-title" style={{ margin: 0 }}>Quy Tắc Thông Báo Tự Động</h3>
            <p className="ast-section-desc" style={{ margin: '4px 0 0' }}>
              Cấu hình các thông báo gửi tự động cho học viên theo điều kiện kích hoạt.
            </p>
          </div>
          <button type="button" className="ast-btn ast-btn--primary" onClick={openCreate} disabled={!!editing}>
            <IcPlus /> Tạo quy tắc
          </button>
        </div>
      </div>

      {editing && (
        <RuleForm
          mode={editing.mode}
          initial={editing.initial}
          onSubmit={handleSubmit}
          onCancel={() => setEditing(null)}
          isSaving={isSaving}
        />
      )}

      <div className="ast-card ast-card--no-pad">
        {isLoading ? (
          <div style={{ padding: 24 }}>
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="ast-field-skel" style={{ marginBottom: 12 }} aria-hidden="true" />
            ))}
          </div>
        ) : hasError ? (
          <div className="ast-sec-row">
            <span>Không thể tải danh sách quy tắc.</span>
            <button type="button" className="ast-btn ast-btn--outline" onClick={fetchRules}>Thử lại</button>
          </div>
        ) : rules.length === 0 ? (
          <p style={{ padding: 24, color: 'var(--color-text-muted, #777)', margin: 0 }}>
            Chưa có quy tắc nào. Nhấn “Tạo quy tắc” để thêm mới.
          </p>
        ) : (
          rules.map((rule) => (
            <div key={rule.ruleKey} className="ast-sec-row">
              <div className="ast-sec-left">
                <span className="ast-sec-label">
                  {rule.ruleKey}
                  <span style={{
                    marginLeft: 8, fontSize: 12, padding: '1px 8px', borderRadius: 10,
                    background: '#E3F2FD', color: '#1565C0', fontWeight: 600,
                  }}>
                    {CHANNEL_LABELS[rule.channel] ?? rule.channel ?? '—'}
                  </span>
                  <span style={{
                    marginLeft: 6, fontSize: 12, padding: '1px 8px', borderRadius: 10, fontWeight: 600,
                    background: rule.isEnabled ? 'var(--color-secondary-bg)' : '#F5F5F5',
                    color: rule.isEnabled ? '#2E7D32' : '#888',
                  }}>
                    {rule.isEnabled ? 'Đang bật' : 'Đã tắt'}
                  </span>
                </span>
                <span className="ast-sec-desc">{rule.description || '—'}</span>
              </div>
              <div className="ast-sec-right" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <button
                  role="switch"
                  aria-checked={!!rule.isEnabled}
                  aria-label={rule.isEnabled ? `Tắt ${rule.ruleKey}` : `Bật ${rule.ruleKey}`}
                  className={`ast-toggle${rule.isEnabled ? ' ast-toggle--on' : ''}`}
                  onClick={() => handleToggle(rule)}
                  disabled={togglingKey === rule.ruleKey || !!editing}
                />
                <button
                  type="button"
                  className="ast-sec-btn ast-sec-btn--edit"
                  onClick={() => openEdit(rule)}
                  disabled={!!editing}
                  aria-label={`Sửa ${rule.ruleKey}`}
                >
                  <IcEdit />
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </>
  );
}
