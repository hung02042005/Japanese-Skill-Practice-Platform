import api from './authService';

// --- UC-33: Review Queue (Manager) -------------------------------------------

export async function getReviewQueue({ type, jlptLevel, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (type) params.type = type;
  if (jlptLevel) params.jlptLevel = jlptLevel;
  const res = await api.get('/manager/review-queue', { params });
  return res.data.data; // { content, totalElements, totalPages }
}

export async function getReviewableContentDetail(contentId, contentType) {
  const res = await api.get(`/manager/contents/${contentId}`, { params: { contentType } });
  return res.data.data;
}

export async function reviewContent({ contentType, contentId, action, feedback }) {
  const res = await api.post('/manager/reviews', { contentType, contentId, action, feedback });
  return res.data.data; // ReviewResultResponse
}

export async function requestContentChanges({ contentType, contentId, targetStatus, feedback }) {
  const res = await api.post('/manager/reviews/request-changes', {
    contentType,
    contentId,
    targetStatus,
    feedback,
  });
  return res.data.data;
}

// --- UC-34: Published Content Status (Manager) -------------------------------

export async function getPublishedContents({ type, jlptLevel, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (type) params.type = type;
  if (jlptLevel) params.jlptLevel = jlptLevel;
  const res = await api.get('/manager/published-contents', { params });
  return res.data.data; // { content, totalElements, totalPages }
}

export async function getPublishedContentDetail(contentId, contentType) {
  const res = await api.get(`/manager/published-contents/${contentId}`, { params: { contentType } });
  return res.data.data;
}

export async function changePublishedContentStatus(contentId, payload) {
  const res = await api.put(`/manager/published-contents/${contentId}/status`, payload);
  return res.data.data;
}

export async function restorePublishedContent(contentId, payload) {
  const res = await api.post(`/manager/published-contents/${contentId}/restore`, payload);
  return res.data.data;
}

// --- UC-29: Ticket assignment (Staff Manager) --------------------------------
// Ghi chú: list/detail/reply/close dùng chung từ staffService (endpoint /staff/tickets).

// Phân công ticket cho 1 staff. OPEN → ASSIGNED. 403 nếu không phải manager/admin.
export async function assignTicket(ticketId, assignToStaffId) {
  const res = await api.post(`/staff/tickets/${ticketId}/assign`, { assignToStaffId });
  return res.data.data; // TicketResponse
}

// Danh sách nhân viên có thể được giao (assignee picker). STAFF_MANAGER only.
export async function getAssignableStaff() {
  const res = await api.get('/staff/members');
  return res.data.data; // [{ staffId, fullName, email, staffRole, assignedOpenCount }]
}

// --- Manager Deleted Contents (Trash bin) ------------------------------------

export async function getDeletedContents(type) {
  const params = type && type !== 'all' ? { type } : {};
  const res = await api.get('/manager/deleted-contents', { params });
  return res.data.data;
}

export async function restoreDeletedContent(type, id) {
  const res = await api.post(`/manager/deleted-contents/${type}/${id}/restore`);
  return res.data.data;
}

