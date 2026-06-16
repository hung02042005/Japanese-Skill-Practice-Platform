import './Badges.css';

export const STATUS_LABELS = {
  active:    'Hoạt động',
  suspended: 'Đình chỉ',
  pending:   'Chờ kích hoạt',
  deleted:   'Đã xóa',
};

export const ROLE_LABELS = {
  student: 'Học viên',
  staff:   'Nhân viên',
  admin:   'Quản trị',
};

export const STAFF_ROLE_LABELS = {
  staff:         'Nhân viên',
  staff_manager: 'Quản lý nhân viên',
};

const ROLE_ICONS = { student: '📚', staff: '🌿', admin: '👑' };

export function StatusBadge({ status }) {
  return (
    <span className={`badge badge--status badge--${status}`}>
      <span className={`status-dot status-dot--${status}`} />
      {STATUS_LABELS[status] ?? status}
    </span>
  );
}

export function RoleBadge({ userType, staffRole }) {
  const icon  = ROLE_ICONS[userType] ?? '👤';
  const label = userType === 'staff' && staffRole
    ? (STAFF_ROLE_LABELS[staffRole] ?? staffRole)
    : (ROLE_LABELS[userType] ?? userType);
  return (
    <span className={`badge badge--role badge--role-${userType}`}>
      {icon} {label}
    </span>
  );
}

export function JlptBadge({ level, className = '' }) {
  if (!level) return null;
  return (
    <span className={`badge jlpt-${level.toLowerCase()} ${className}`.trim()}>
      {level}
    </span>
  );
}
