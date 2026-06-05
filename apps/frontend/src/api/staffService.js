import api from './authService';

// ─── Staff Students ───────────────────────────────────────────────────────────
export async function getStaffStudents({ search, level, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (search) params.search = search;
  if (level)  params.level  = level;
  if (status) params.status = status;
  const res = await api.get('/staff/students', { params });
  return res.data.data;
}

export async function getStudentProgress(studentId) {
  const res = await api.get(`/staff/students/${studentId}/progress`);
  return res.data.data;
}

export async function suspendStudent(studentId, reason = '') {
  const res = await api.post(`/staff/students/${studentId}/suspend`, { reason });
  return res.data.data;
}

export async function activateStudent(studentId) {
  const res = await api.post(`/staff/students/${studentId}/activate`);
  return res.data.data;
}

// ─── Staff Dashboard ──────────────────────────────────────────────────────────
export async function getStaffDashboard() {
  const res = await api.get('/staff/dashboard');
  return res.data.data;
}
