import api from './authService';

// ── UC-40: Quy tắc thông báo tự động (Admin) ────────────────────────────────

export async function listNotificationRules() {
  const res = await api.get('/admin/notifications/rules');
  return res.data.data;
}

export async function createNotificationRule(payload) {
  const res = await api.post('/admin/notifications/rules', payload);
  return res.data.data;
}

export async function updateNotificationRule(ruleKey, payload) {
  const res = await api.put(`/admin/notifications/rules/${ruleKey}`, payload);
  return res.data.data;
}
