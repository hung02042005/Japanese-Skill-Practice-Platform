import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// ─── Request interceptor: đính Bearer token ────────────────────────────────────

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error),
);

// ─── Response interceptor: tự động refresh khi 401 ────────────────────────────

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error('No refresh token');

        // Dùng axios thuần để tránh vòng lặp interceptor
        const { data } = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken });

        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);

        originalRequest.headers.Authorization = `Bearer ${data.data.accessToken}`;
        return api(originalRequest);
      } catch {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('jlpt-user');
        window.location.href = '/login';
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  },
);

// ─── Auth API functions ────────────────────────────────────────────────────────

export async function login(credentials) {
  const response = await api.post('/auth/login', credentials);
  return response.data;
}

export async function verifyMfa({ mfaToken, totpCode }) {
  const response = await api.post('/auth/verify-mfa', { mfaToken, totpCode });
  return response.data;
}

export async function register(data) {
  const response = await api.post('/auth/register', data);
  return response.data;
}

export async function logout() {
  const refreshToken = localStorage.getItem('refreshToken');
  try {
    const response = await api.post('/auth/logout', { refreshToken });
    return response.data;
  } finally {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('jlpt-user');
  }
}

export async function verifyEmail(token) {
  const response = await api.post('/auth/verify-email', { token });
  return response.data;
}

export async function resendVerification(email) {
  const response = await api.post('/auth/resend-verification', { email });
  return response.data;
}

export async function forgotPassword(email) {
  const response = await api.post('/auth/forgot-password', { email });
  return response.data;
}

export async function resetPassword({ token, newPassword, confirmPassword }) {
  const response = await api.post('/auth/reset-password', {
    token,
    newPassword,
    confirmPassword,
  });
  return response.data;
}

export async function getProfile() {
  const response = await api.get('/students/me');
  return response.data;
}

export async function updateProfile(data) {
  const response = await api.put('/students/me', data);
  return response.data;
}

export default api;
