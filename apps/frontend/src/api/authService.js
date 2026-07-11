import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

let refreshPromise = null;

function clearSessionAndRedirect() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('jlpt-user');
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

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

    if (!originalRequest) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        if (!refreshPromise) {
          const refreshToken = localStorage.getItem('refreshToken');
          if (!refreshToken) throw new Error('No refresh token');

          // Use plain axios to avoid interceptor recursion.
          refreshPromise = axios
            .post(`${API_BASE_URL}/auth/refresh`, { refreshToken })
            .then(({ data }) => {
              localStorage.setItem('accessToken', data.data.accessToken);
              localStorage.setItem('refreshToken', data.data.refreshToken);
              return data.data.accessToken;
            })
            .finally(() => {
              refreshPromise = null;
            });
        }

        const accessToken = await refreshPromise;
        originalRequest.headers = originalRequest.headers ?? {};
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch {
        clearSessionAndRedirect();
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

export async function checkAccountType(email) {
  const response = await api.post('/auth/check-account-type', { email });
  return response.data;
}

export async function staffForgotPassword(email) {
  const response = await api.post('/staff/auth/forgot-password', { email });
  return response.data;
}

export async function setupStaffPassword({ token, newPassword, confirmPassword }) {
  const response = await api.post('/staff/auth/setup-password', {
    token,
    newPassword,
    confirmPassword,
  });
  return response.data;
}

export async function changeTempPassword({ newPassword, confirmPassword }) {
  const response = await api.post('/staff/auth/change-temp-password', {
    newPassword,
    confirmPassword,
  });
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

export async function resendVerification(email) {
  const response = await api.post('/auth/resend-verification', { email });
  return response.data;
}

export async function googleLogin(idToken) {
  const response = await api.post('/auth/google', { idToken });
  return response.data;
}

export default api;
