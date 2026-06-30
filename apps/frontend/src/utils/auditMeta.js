// Nhãn + màu cho audit log (dùng chung AdminDashboard ↔ AdminReports).
// Key luôn viết thường; action từ BE có thể HOA/thường nên normalize bằng toLowerCase().

export const ACTION_LABELS = {
  // ── Admin ──────────────────────────────────────────────
  create_staff:              'Tạo tài khoản Staff',
  suspend_user:              'Đình chỉ tài khoản',
  activate_user:             'Kích hoạt lại tài khoản',
  soft_delete_user:          'Xóa tài khoản',
  reset_password_initiated:  'Đặt lại mật khẩu',
  change_staff_role:         'Đổi vai trò Staff',
  update_setting:            'Cập nhật cài đặt',
  setting_updated:           'Cập nhật cài đặt',
  admin_login_success:       'Admin đăng nhập',
  notification_rule_created: 'Tạo quy tắc thông báo',
  notification_rule_updated: 'Sửa quy tắc thông báo',
  notification_rule_deleted: 'Xóa quy tắc thông báo',
  // ── Manager (kiểm duyệt nội dung) ──────────────────────
  approve_content:           'Duyệt nội dung',
  reject_content:            'Từ chối nội dung',
  request_changes_content:   'Yêu cầu chỉnh sửa nội dung',
  // ── Staff ──────────────────────────────────────────────
  ticket_assigned:           'Nhận xử lý ticket',
  ticket_closed:             'Đóng ticket',
  submission_graded:         'Chấm bài nộp',
  broadcast_sent:            'Gửi thông báo broadcast',
  assessment_soft_deleted:   'Xóa bài kiểm tra',
  // ── Học viên ───────────────────────────────────────────
  quiz_submitted:            'Nộp quiz',
  exam_submitted:            'Nộp bài thi',
};

const ACTION_COLORS = {
  create_staff:              { bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
  activate_user:             { bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
  approve_content:           { bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
  suspend_user:              { bg: '#FFF3E0',                   color: '#F57C00' },
  reject_content:            { bg: '#FFF3E0',                   color: '#F57C00' },
  soft_delete_user:          { bg: '#FFEAEA',                   color: 'var(--color-error)' },
  assessment_soft_deleted:   { bg: '#FFEAEA',                   color: 'var(--color-error)' },
  reset_password_initiated:  { bg: 'var(--color-primary-bg)',   color: 'var(--color-primary)' },
  change_staff_role:         { bg: '#F3E5F5',                   color: '#6A1B9A' },
  update_setting:            { bg: '#E3F2FD',                   color: '#1565C0' },
  setting_updated:           { bg: '#E3F2FD',                   color: '#1565C0' },
  request_changes_content:   { bg: '#E3F2FD',                   color: '#1565C0' },
  ticket_assigned:           { bg: '#E3F2FD',                   color: '#1565C0' },
  ticket_closed:             { bg: '#F5F5F5',                   color: '#555' },
  submission_graded:         { bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
  broadcast_sent:            { bg: 'var(--color-primary-bg)',   color: 'var(--color-primary)' },
};

export const ROLE_META = {
  ADMIN:   { label: 'Admin',    bg: '#F3E5F5',                   color: '#6A1B9A' },
  MANAGER: { label: 'Manager',  bg: '#E3F2FD',                   color: '#1565C0' },
  STAFF:   { label: 'Staff',    bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
  STUDENT: { label: 'Học viên', bg: '#FFF3E0',                   color: '#F57C00' },
  SYSTEM:  { label: 'Hệ thống', bg: '#F5F5F5',                   color: '#555' },
};

// Nhóm action theo vai trò — dùng cho <optgroup> bộ lọc.
export const ACTION_GROUPS = [
  { label: 'Admin',    actions: ['create_staff', 'suspend_user', 'activate_user', 'soft_delete_user', 'reset_password_initiated', 'change_staff_role', 'update_setting', 'admin_login_success', 'notification_rule_created', 'notification_rule_updated', 'notification_rule_deleted'] },
  { label: 'Manager',  actions: ['approve_content', 'reject_content', 'request_changes_content'] },
  { label: 'Staff',    actions: ['ticket_assigned', 'ticket_closed', 'submission_graded', 'broadcast_sent', 'assessment_soft_deleted'] },
  { label: 'Học viên', actions: ['quiz_submitted', 'exam_submitted'] },
];

export function getActionLabel(action) {
  if (!action) return '';
  return ACTION_LABELS[action.toLowerCase()] ?? action;
}

export function getActionColors(action) {
  if (!action) return { bg: '#F5F5F5', color: '#555' };
  return ACTION_COLORS[action.toLowerCase()] ?? { bg: '#F5F5F5', color: '#555' };
}

export function getRoleMeta(role) {
  return ROLE_META[role] ?? ROLE_META.SYSTEM;
}
