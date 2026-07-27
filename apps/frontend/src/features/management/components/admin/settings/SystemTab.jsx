import { useState, useEffect } from 'react';
import { IcWrench }           from '../ManageUsersIcons';
import { getSettings, updateSetting } from '@/shared/api/adminService';

export function SystemTab({ addToast }) {
  const [maintenance, setMaintenance] = useState(false);
  const [isLoading,   setLoading]     = useState(true);
  const [isSaving,    setSaving]      = useState(false);

  useEffect(() => {
    getSettings('system')
      .then((data) => {
        const val = Array.isArray(data)
          ? data.find((s) => s.settingKey === 'maintenance_mode')?.settingValue
          : data?.maintenance_mode;
        setMaintenance(val === 'true' || val === true);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  async function handleToggle() {
    const next = !maintenance;
    const msg  = next
      ? 'Bạn có chắc muốn BẬT chế độ bảo trì? Học viên sẽ không thể đăng nhập.'
      : 'Bạn có chắc muốn TẮT chế độ bảo trì?';
    if (!window.confirm(msg)) return;
    setSaving(true);
    try {
      await updateSetting('system', 'maintenance_mode', String(next));
      setMaintenance(next);
      addToast('success', next ? 'Đã bật chế độ bảo trì' : 'Đã tắt chế độ bảo trì');
    } catch {
      addToast('error', 'Cập nhật thất bại. Thử lại sau.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className={`ast-maint-card${maintenance ? ' ast-maint-card--on' : ''}`}>
      <div className="ast-maint-left">
        <h3 className="ast-maint-title">Chế độ bảo trì</h3>
        <p className="ast-maint-desc">
          Khi bật, toàn bộ học viên bị chặn đăng nhập với thông báo bảo trì.
          Staff và Admin vẫn truy cập bình thường.
        </p>
        {maintenance && (
          <div className="ast-maint-banner">
            <IcWrench />
            <span>Hệ thống đang ở chế độ bảo trì. Học viên không thể đăng nhập.</span>
          </div>
        )}
      </div>

      <div className="ast-maint-right">
        {isLoading ? (
          <div className="ast-toggle-skel" aria-hidden="true" />
        ) : (
          <>
            <button
              role="switch"
              aria-checked={maintenance}
              aria-label={maintenance ? 'Tắt chế độ bảo trì' : 'Bật chế độ bảo trì'}
              className={`ast-toggle${maintenance ? ' ast-toggle--on' : ''}`}
              onClick={handleToggle}
              disabled={isSaving}
            />
            <span className="ast-toggle-label">{maintenance ? 'BẬT' : 'TẮT'}</span>
          </>
        )}
      </div>
    </div>
  );
}
