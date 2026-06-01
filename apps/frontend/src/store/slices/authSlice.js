import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as authService from '../../api/authService';

export const verifyEmailThunk = createAsyncThunk(
  'auth/verifyEmail',
  async (token, { rejectWithValue }) => {
    try {
      const res = await authService.verifyEmail(token);
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Xác minh email thất bại');
    }
  },
);

// ─── Thunks ───────────────────────────────────────────────────────────────────

export const loginThunk = createAsyncThunk(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const res = await authService.login(credentials);
      const { accessToken, refreshToken, student } = res.data;
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('jlpt-user', JSON.stringify(student));
      return student;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Đăng nhập thất bại');
    }
  },
);

export const registerThunk = createAsyncThunk(
  'auth/register',
  async (formData, { rejectWithValue }) => {
    try {
      const res = await authService.register(formData);
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Đăng ký thất bại');
    }
  },
);

export const logoutThunk = createAsyncThunk('auth/logout', async () => {
  try {
    await authService.logout();
  } catch {
    // API lỗi vẫn clean up local
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('jlpt-user');
  }
});

export const forgotPasswordThunk = createAsyncThunk(
  'auth/forgotPassword',
  async ({ email }, { rejectWithValue }) => {
    try {
      const res = await authService.forgotPassword(email);
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Gửi email thất bại');
    }
  },
);

export const resetPasswordThunk = createAsyncThunk(
  'auth/resetPassword',
  async ({ token, newPassword, confirmPassword }, { rejectWithValue }) => {
    try {
      const res = await authService.resetPassword({ token, newPassword, confirmPassword });
      return res.data;
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message ?? 'Đặt lại mật khẩu thất bại',
      );
    }
  },
);

export const fetchProfileThunk = createAsyncThunk(
  'auth/fetchProfile',
  async (_, { rejectWithValue }) => {
    try {
      const res = await authService.getProfile();
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Tải hồ sơ thất bại');
    }
  },
);

export const updateProfileThunk = createAsyncThunk(
  'auth/updateProfile',
  async (data, { rejectWithValue }) => {
    try {
      const res = await authService.updateProfile(data);
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Cập nhật hồ sơ thất bại');
    }
  },
);

// ─── Rehydrate từ localStorage khi app khởi động ──────────────────────────────

function loadPersistedUser() {
  try {
    const raw = localStorage.getItem('jlpt-user');
    const token = localStorage.getItem('accessToken');
    if (raw && token) return JSON.parse(raw);
  } catch {}
  return null;
}

const persistedUser = loadPersistedUser();

// ─── Slice ────────────────────────────────────────────────────────────────────

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: persistedUser,
    isAuthenticated: persistedUser !== null,
    status: 'idle',   // 'idle' | 'loading' | 'succeeded' | 'failed'
    error: null,
  },
  reducers: {
    clearError(state) {
      state.error = null;
    },
    logout(state) {
      state.user = null;
      state.isAuthenticated = false;
      state.status = 'idle';
      state.error = null;
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('jlpt-user');
    },
  },
  extraReducers: (builder) => {
    builder
      // login
      .addCase(loginThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(loginThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.user = action.payload;
        state.isAuthenticated = true;
      })
      .addCase(loginThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })

      // register
      .addCase(registerThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(registerThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(registerThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })

      // logout
      .addCase(logoutThunk.fulfilled, (state) => {
        state.user = null;
        state.isAuthenticated = false;
        state.status = 'idle';
        state.error = null;
      })

      // forgotPassword
      .addCase(forgotPasswordThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(forgotPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(forgotPasswordThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })

      // resetPassword
      .addCase(resetPasswordThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(resetPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(resetPasswordThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })

      // fetchProfile
      .addCase(fetchProfileThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchProfileThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.user = action.payload;
        localStorage.setItem('jlpt-user', JSON.stringify(action.payload));
      })
      .addCase(fetchProfileThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })

      // updateProfile
      .addCase(updateProfileThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(updateProfileThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.user = action.payload;
        localStorage.setItem('jlpt-user', JSON.stringify(action.payload));
      })
      .addCase(updateProfileThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })

      // verifyEmail
      .addCase(verifyEmailThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(verifyEmailThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(verifyEmailThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      });
  },
});

export const { clearError, logout } = authSlice.actions;
export default authSlice.reducer;
