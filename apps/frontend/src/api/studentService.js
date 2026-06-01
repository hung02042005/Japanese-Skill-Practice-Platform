import api from './authService';

export async function getDashboard() {
  const res = await api.get('/students/dashboard');
  return res.data;
}
