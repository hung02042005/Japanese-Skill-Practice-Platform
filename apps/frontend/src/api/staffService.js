import api from './authService';

// ─── Staff Students ───────────────────────────────────────────────────────────
export async function getStaffStudents({ search, level, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (search) params.q         = search;    // backend @RequestParam "q"
  if (level)  params.jlptLevel = level;     // backend @RequestParam "jlptLevel"
  if (status) params.status    = status;
  const res = await api.get('/staff/students', { params });
  return res.data.data;
}

export async function getStudentDetail(studentId) {
  const res = await api.get(`/staff/students/${studentId}`);
  return res.data.data;
}

export async function getStudentSubmissions(studentId, { type, status, page = 0, size = 10 } = {}) {
  const params = { page, size };
  if (type)   params.type   = type;
  if (status) params.status = status;
  const res = await api.get(`/staff/students/${studentId}/submissions`, { params });
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

// ─── Staff Tickets ────────────────────────────────────────────────────────────
export async function getTickets({ status, category, priority, q, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (status)   params.status = status;
  if (category) params.category = category;
  if (priority) params.priority = priority;
  if (q)        params.q = q;
  const res = await api.get('/staff/tickets', { params });
  return res.data.data;
}

export async function getTicketDetail(ticketId) {
  const res = await api.get(`/staff/tickets/${ticketId}`);
  return res.data.data;
}

export async function replyTicket(ticketId, message) {
  const res = await api.post(`/staff/tickets/${ticketId}/reply`, { message });
  return res.data.data;
}

export async function closeTicket(ticketId) {
  const res = await api.post(`/staff/tickets/${ticketId}/close`);
  return res.data.data;
}

export async function assignTicket(ticketId, assignToStaffId) {
  const res = await api.post(`/staff/tickets/${ticketId}/assign`, { assignToStaffId });
  return res.data.data;
}

// ─── Staff Notifications (broadcast) ─────────────────────────────────────────
export async function sendNotification(data) {
  const res = await api.post('/staff/notifications', data);
  return res.data.data; // { jobId }
}

// ─── Staff Grading (speaking submissions) ────────────────────────────────────
export async function getSpeakingSubmissions({ status, page = 0, size = 20 } = {}) {
  const params = { submissionType: 'speaking', page, size };
  if (status) params.status = status;
  const res = await api.get('/staff/submissions', { params });
  return res.data.data;
}

export async function getSubmissionDetail(submissionId) {
  const res = await api.get(`/staff/submissions/${submissionId}`);
  return res.data.data;
}

export async function gradeSubmission(submissionId, { manualScore, manualFeedback }) {
  const res = await api.post(`/staff/submissions/${submissionId}/grade`, {
    manualScore: manualScore !== '' && manualScore != null ? parseFloat(manualScore) : null,
    manualFeedback,
  });
  return res.data.data;
}
