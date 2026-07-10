import api from './authService';

// ── UC-37-01: List users (server-side pagination + filtering) ───────────────
// Returns { content, totalElements, totalPages }
export async function listUsers({ type, q, status, jlptLevel, staffRole, page = 0, size = 20 } = {}) {
  const params = { type, page, size };
  if (q)          params.q         = q;
  if (status)     params.status     = status;
  if (jlptLevel)  params.jlptLevel  = jlptLevel;
  if (staffRole)  params.staffRole  = staffRole;
  const res = await api.get('/admin/users', { params });
  return res.data.data;
}

// ── UC-37-03: Create Staff ──────────────────────────────────────────────────
export async function createStaff({ fullName, email, staffRole }) {
  const res = await api.post('/admin/staff', { fullName, email, staffRole });
  return res.data.data;
}

// ── UC-37-05: Suspend user ──────────────────────────────────────────────────
export async function suspendUser(type, userId, reason) {
  const res = await api.post(`/admin/users/${type}/${userId}/suspend`, { reason });
  return res.data.data;
}

// ── UC-37-06: Activate user ─────────────────────────────────────────────────
export async function activateUser(type, userId) {
  const res = await api.post(`/admin/users/${type}/${userId}/activate`);
  return res.data.data;
}

// ── UC-37-07: Admin-initiated password reset ────────────────────────────────
export async function resetPassword(type, userId) {
  const res = await api.post(`/admin/users/${type}/${userId}/reset-password`);
  return res.data;
}

// ── UC-37-08: Soft delete user ──────────────────────────────────────────────
export async function softDeleteUser(type, userId) {
  const res = await api.delete(`/admin/users/${type}/${userId}`);
  return res.data.data;
}

// ── UC-37-08B: Restore deleted user ─────────────────────────────────────────
export async function restoreUser(type, userId) {
  const res = await api.post(`/admin/users/${type}/${userId}/restore`);
  return res.data.data;
}

// ── UC-37-09: Change staff role (staff ↔ staff_manager) ────────────────────
export async function changeStaffRole(staffId, staffRole) {
  const res = await api.put(`/admin/staff/${staffId}/role`, { staffRole });
  return res.data.data;
}

export async function listStaffResetRequests(status = 'pending') {
  const res = await api.get('/admin/staff/reset-requests', { params: { status } });
  return res.data.data;
}

export async function issueTempPassword(staffId, requestId) {
  const res = await api.post(`/admin/staff/${staffId}/issue-temp-password`, { requestId });
  return res.data.data;
}

// ── UC-36: Admin dashboard — summary + kpi gộp trong 1 call → { summary, kpi }
export async function getDashboardOverview() {
  const res = await api.get('/admin/dashboard');
  return res.data.data;
}

// ── UC-36: Audit log (server-side filtering by action / targetTable) ────────
export async function getAuditLog({ page = 0, size = 10, action, targetTable } = {}) {
  const params = { page, size };
  if (action)      params.action      = action;
  if (targetTable) params.targetTable = targetTable;
  const res = await api.get('/admin/audit-logs', { params });
  return res.data.data;
}

// ── UC-39: System settings (BE chỉ expose theo nhóm: /admin/settings/{group}) ─
export async function getSettings(group) {
  const res = await api.get(`/admin/settings/${group}`);
  return res.data.data;
}

export async function updateSetting(group, key, value) {
  const res = await api.put(`/admin/settings/${group}/${key}`, { settingValue: value });
  return res.data.data;
}

// Lưu nhiều setting cùng nhóm trong 1 request (atomic). settings: [{ settingKey, settingValue }]
export async function updateSettings(group, settings) {
  const res = await api.put(`/admin/settings/${group}`, { settings });
  return res.data.data;
}

export async function testSmtp() {
  const res = await api.post('/admin/settings/smtp/test');
  return res.data;
}

