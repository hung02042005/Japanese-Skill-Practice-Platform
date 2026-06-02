import {
  IcAdminChip, IcAddStaff, IcBan, IcCheck,
  IcKey, IcSwap, IcTrash, IcEdit,
} from './ManageUsersIcons';

/* ── Helpers ── */
function relativeTime(iso) {
  const diff = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
  if (diff < 60)    return 'vừa xong';
  if (diff < 3600)  return `${Math.floor(diff / 60)} phút trước`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} giờ trước`;
  return `${Math.floor(diff / 86400)} ngày trước`;
}

const ACTION_LABELS = {
  create_staff:             'Tạo tài khoản Staff',
  suspend_user:             'Đình chỉ tài khoản',
  activate_user:            'Kích hoạt lại tài khoản',
  soft_delete_user:         'Xóa tài khoản (soft delete)',
  reset_password_initiated: 'Đặt lại mật khẩu',
  change_staff_role:        'Đổi vai trò Staff',
  update_setting:           'Cập nhật cài đặt hệ thống',
};

const LOG_STYLE = {
  create_staff:             { icon: <IcAddStaff />, bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
  suspend_user:             { icon: <IcBan />,      bg: '#FFF3E0',                   color: '#F57C00' },
  activate_user:            { icon: <IcCheck />,    bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
  soft_delete_user:         { icon: <IcTrash />,    bg: '#FFEAEA',                   color: 'var(--color-error)' },
  reset_password_initiated: { icon: <IcKey />,      bg: 'var(--color-primary-bg)',   color: 'var(--color-primary)' },
  change_staff_role:        { icon: <IcSwap />,     bg: '#F3E5F5',                   color: '#6A1B9A' },
  update_setting:           { icon: <IcEdit />,     bg: '#E3F2FD',                   color: '#1565C0' },
};

function getStyle(type) {
  return LOG_STYLE[type] ?? { icon: <IcAdminChip />, bg: 'var(--color-primary-bg)', color: 'var(--color-primary)' };
}

function getLabel(log) {
  const base = ACTION_LABELS[log.actionType] ?? log.actionType;
  return log.targetEmail ? `${base}: ${log.targetEmail}` : base;
}

/* ── Skeletons ── */
function LogSkeletonList() {
  return (
    <>
      {Array.from({ length: 6 }).map((_, i) => (
        <div key={i} className="adb-log-skel">
          <div className="adb-log-skel__icon" aria-hidden="true" />
          <div className="adb-log-skel__lines" aria-hidden="true">
            <div className="adb-log-skel__line adb-log-skel__line--main" />
            <div className="adb-log-skel__line adb-log-skel__line--sub" />
          </div>
        </div>
      ))}
    </>
  );
}

/**
 * Props:
 *   logs      — audit log array
 *   isLoading — bool
 *   onRetry   — fn
 */
export function DashboardActivityLog({ logs, isLoading, onRetry }) {
  return (
    <section className="adb-card adb-activity" aria-label="Hoạt động gần đây">
      <div className="adb-card-header">
        <h2 className="adb-card-title">Hoạt Động Gần Đây</h2>
        <span
          className="adb-reports-link adb-reports-link--disabled"
          title="Chức năng báo cáo đầy đủ sắp ra mắt"
        >
          Xem tất cả
        </span>
      </div>

      {isLoading ? (
        <LogSkeletonList />
      ) : !logs ? (
        <div className="adb-error-banner" role="alert">
          <span>Không thể tải lịch sử hoạt động.</span>
          <button className="adb-retry-btn" onClick={onRetry}>Thử lại</button>
        </div>
      ) : logs.length === 0 ? (
        <p className="adb-log-empty">Chưa có hoạt động nào được ghi nhận.</p>
      ) : (
        <ul className="adb-log-list" role="list">
          {logs.map((log) => {
            const { icon, bg, color } = getStyle(log.actionType);
            return (
              <li key={log.logId} className="adb-log-item">
                <div className="adb-log-icon" style={{ background: bg, color }} aria-hidden="true">
                  {icon}
                </div>
                <div className="adb-log-content">
                  <span className="adb-log-action">{getLabel(log)}</span>
                  <span className="adb-log-meta">
                    {log.adminEmail} · {relativeTime(log.createdAt)}
                  </span>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
