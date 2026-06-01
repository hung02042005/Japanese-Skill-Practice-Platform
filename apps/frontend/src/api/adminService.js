import api from './authService';

export async function fetchAdminUsers() {
  const response = await api.get('/admin/users');
  return response.data.data;
}

export async function updateUserStatus(userType, id, action, reason = '') {
  const response = await api.patch(`/admin/users/${userType}/${id}/status`, { action, reason });
  return response.data.data;
}

export async function updateUserRole(userType, id, newRole) {
  const response = await api.patch(`/admin/users/${userType}/${id}/role`, { newRole });
  return response.data.data;
}
