import { useState, useEffect, useRef } from 'react';
import { IcEdit, IcBloomCheck }        from '../ManageUsersIcons';
import { getSettings, updateSetting }  from '@/shared/api/adminService';

const SECURITY_SETTINGS = [
  {
    key:   'max_login_attempts',
    group: 'security',
    label: 'Số lần đăng nhập tối đa',
    desc:  'Tài khoản bị khóa sau N lần nhập sai mật khẩu liên tiếp',
    min: 3, max: 20,
  },
  {
    key:   'lockout_duration_minutes',
    group: 'security',
    label: 'Thời gian khóa (phút)',
    desc:  'Tài khoản tự mở khóa sau N phút',
    min: 5, max: 1440,
  },
  {
    key:   'jwt_expiry_minutes',
    group: 'security',
    label: 'Thời hạn JWT (phút)',
    desc:  'Phiên đăng nhập hết hạn sau N phút không hoạt động',
    min: 5, max: 1440,
  },
];

function CancelIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <line x1="18" y1="6"  x2="6"  y2="18" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
      <line x1="6"  y1="6"  x2="18" y2="18" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
    </svg>
  );
}

function SecurityRow({ setting, addToast }) {
  const [value,   setValue]  = useState('');
  const [editVal, setEdit]   = useState('');
  const [editing, setEditing]= useState(false);
  const [loading, setLoading]= useState(true);
  const [saving,  setSaving] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    getSettings(setting.group)
      .then((data) => {
        const found = Array.isArray(data)
          ? data.find((s) => s.settingKey === setting.key)?.settingValue
          : data?.[setting.key];
        setValue(found ?? '—');
      })
      .catch(() => setValue('—'))
      .finally(() => setLoading(false));
  }, [setting]);

  function startEdit() {
    setEdit(value === '—' ? '' : value);
    setEditing(true);
    setTimeout(() => inputRef.current?.focus(), 0);
  }

  function cancelEdit() { setEditing(false); }

  async function saveEdit() {
    const num = Number(editVal);
    if (!editVal || isNaN(num) || num < setting.min || num > setting.max) {
      addToast('error', `Giá trị phải từ ${setting.min} đến ${setting.max}`);
      return;
    }
    setSaving(true);
    try {
      await updateSetting(setting.group, setting.key, editVal);
      setValue(editVal);
      setEditing(false);
      addToast('success', `Đã cập nhật "${setting.label}"`);
    } catch {
      addToast('error', 'Cập nhật thất bại');
    } finally {
      setSaving(false);
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter')  saveEdit();
    if (e.key === 'Escape') cancelEdit();
  }

  return (
    <div className="ast-sec-row">
      <div className="ast-sec-left">
        <span className="ast-sec-label">{setting.label}</span>
        <span className="ast-sec-desc">{setting.desc}</span>
      </div>
      <div className="ast-sec-right">
        {loading ? (
          <div className="ast-sec-skel" aria-hidden="true" />
        ) : editing ? (
          <div className="ast-sec-edit">
            <input
              ref={inputRef}
              type="number"
              className="ast-sec-input"
              value={editVal}
              min={setting.min}
              max={setting.max}
              onChange={(e) => setEdit(e.target.value)}
              onKeyDown={handleKeyDown}
              aria-label={`Nhập ${setting.label}`}
            />
            <button
              className="ast-sec-btn ast-sec-btn--save"
              onClick={saveEdit}
              disabled={saving}
              aria-label="Lưu"
            >
              {saving
                ? <span className="ast-spinner ast-spinner--small" aria-hidden="true" />
                : <IcBloomCheck />}
            </button>
            <button
              className="ast-sec-btn ast-sec-btn--cancel"
              onClick={cancelEdit}
              aria-label="Hủy"
            >
              <CancelIcon />
            </button>
          </div>
        ) : (
          <div className="ast-sec-view">
            <span className="ast-sec-value">{value}</span>
            <button
              className="ast-sec-btn ast-sec-btn--edit"
              onClick={startEdit}
              aria-label={`Chỉnh sửa ${setting.label}`}
            >
              <IcEdit />
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export function SecurityTab({ addToast }) {
  return (
    <div className="ast-card ast-card--no-pad">
      {SECURITY_SETTINGS.map((s) => (
        <SecurityRow key={s.key} setting={s} addToast={addToast} />
      ))}
    </div>
  );
}
