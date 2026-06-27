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

// ── UC-37-02: Get user detail ───────────────────────────────────────────────
export async function getUserDetail(type, userId) {
  const res = await api.get(`/admin/users/${type}/${userId}`);
  return res.data.data;
}

// ── UC-37-03: Create Staff ──────────────────────────────────────────────────
export async function createStaff({ fullName, email, staffRole }) {
  const res = await api.post('/admin/staff', { fullName, email, staffRole });
  return res.data.data;
}

// ── UC-37-04: Edit student info ─────────────────────────────────────────────
export async function updateStudent(userId, data) {
  const res = await api.put(`/admin/users/student/${userId}`, data);
  return res.data.data;
}

// ── UC-37-04: Edit staff info ───────────────────────────────────────────────
export async function updateStaff(userId, data) {
  const res = await api.put(`/admin/users/staff/${userId}`, data);
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

// ── UC-36: Dashboard summary ────────────────────────────────────────────────
export async function getDashboardSummary() {
  const res = await api.get('/admin/dashboard/summary');
  return res.data.data;
}

// ── UC-36: Audit log ────────────────────────────────────────────────────────
export async function getAuditLog({ page = 0, size = 10 } = {}) {
  const res = await api.get('/admin/audit-log', { params: { page, size } });
  return res.data.data;
}

// ── UC-39: System settings ──────────────────────────────────────────────────
export async function getSettings(group) {
  const url = group ? `/admin/settings/${group}` : '/admin/settings';
  const res = await api.get(url);
  return res.data.data;
}

export async function updateSetting(group, key, value) {
  const res = await api.put(`/admin/settings/${group}/${key}`, { settingValue: value });
  return res.data.data;
}

export async function testSmtp() {
  const res = await api.post('/admin/settings/smtp/test');
  return res.data;
}

// ── Legacy: student ↔ staff promotion (old PATCH endpoints) ────────────────
export async function fetchAdminUsers() {
  const res = await api.get('/admin/users');
  return res.data.data;
}

export async function updateUserStatus(userType, id, action, reason = '') {
  const res = await api.patch(`/admin/users/${userType}/${id}/status`, { action, reason });
  return res.data.data;
}

export async function updateUserRole(userType, id, newRole) {
  const res = await api.patch(`/admin/users/${userType}/${id}/role`, { newRole });
  return res.data.data;
}
