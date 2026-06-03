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
      // Backend LoginApiResponse: field is `user` (not `student`), plus `role` at top level
      const {
        requirePasswordChange,
        role,
        accessToken,
        refreshToken,
        user,
      } = res.data;
      if (requirePasswordChange) {
        const userData = { role, requirePasswordChange: true };
        localStorage.setItem('accessToken', accessToken);
        localStorage.removeItem('refreshToken');
        localStorage.setItem('jlpt-user', JSON.stringify(userData));
        return {
          requiresTwoFactor: false,
          requirePasswordChange: true,
          user: userData,
          role,
        };
      }
      // For ADMIN direct login, `user` is null — build a minimal object from role
      const userData = user ? { ...user, role } : { role };
      localStorage.setItem('accessToken', accessToken);
      if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('jlpt-user', JSON.stringify(userData));
      return { user: userData, role };
    } catch (err) {
      if (!err.response) {
        return rejectWithValue('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối và thử lại.');
      }
      return rejectWithValue(err.response.data?.message ?? 'Đăng nhập thất bại');
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

export const checkAccountTypeThunk = createAsyncThunk(
  'auth/checkAccountType',
  async ({ email }, { rejectWithValue }) => {
    try {
      const res = await authService.checkAccountType(email);
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Không thể kiểm tra tài khoản');
    }
  },
);

export const staffForgotPasswordThunk = createAsyncThunk(
  'auth/staffForgotPassword',
  async ({ email }, { rejectWithValue }) => {
    try {
      const res = await authService.staffForgotPassword(email);
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Gửi yêu cầu thất bại');
    }
  },
);

export const setupStaffPasswordThunk = createAsyncThunk(
  'auth/setupStaffPassword',
  async ({ token, newPassword, confirmPassword }, { rejectWithValue }) => {
    try {
      const res = await authService.setupStaffPassword({ token, newPassword, confirmPassword });
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Kích hoạt tài khoản thất bại');
    }
  },
);

export const changeTempPasswordThunk = createAsyncThunk(
  'auth/changeTempPassword',
  async ({ newPassword, confirmPassword }, { rejectWithValue }) => {
    try {
      const res = await authService.changeTempPassword({ newPassword, confirmPassword });
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('jlpt-user');
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Đổi mật khẩu tạm thất bại');
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

export const loginWithGoogleThunk = createAsyncThunk(
  'auth/loginWithGoogle',
  async (idToken, { rejectWithValue }) => {
    try {
      const res = await authService.googleLogin(idToken);
      // Backend AuthResponse: { accessToken, refreshToken, student }
      const { accessToken, refreshToken, student } = res.data;
      const userData = { ...student, role: 'STUDENT' };
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('jlpt-user', JSON.stringify(userData));
      return userData;
    } catch (err) {
      if (!err.response) {
        return rejectWithValue('Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối và thử lại.');
      }
      return rejectWithValue(err.response.data?.message ?? 'Đăng nhập bằng Google thất bại');
    }
  },
);

// ─── Rehydrate từ localStorage khi app khởi động ──────────────────────────────

function loadPersistedUser() {
  try {
    const raw = localStorage.getItem('jlpt-user');
    const token = localStorage.getItem('accessToken');
    if (raw && token) return JSON.parse(raw);
  } catch {
    return null;
  }
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
        state.user = action.payload.user;
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

      // setupStaffPassword
      .addCase(setupStaffPasswordThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(setupStaffPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(setupStaffPasswordThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })

      // checkAccountType
      .addCase(checkAccountTypeThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(checkAccountTypeThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(checkAccountTypeThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })

      // staffForgotPassword
      .addCase(staffForgotPasswordThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(staffForgotPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(staffForgotPasswordThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })

      // changeTempPassword
      .addCase(changeTempPasswordThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(changeTempPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
        state.user = null;
        state.isAuthenticated = false;
      })
      .addCase(changeTempPasswordThunk.rejected, (state, action) => {
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
      })

      // loginWithGoogle
      .addCase(loginWithGoogleThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(loginWithGoogleThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.user = action.payload;
        state.isAuthenticated = true;
      })
      .addCase(loginWithGoogleThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      });
  },
});

export const { clearError, logout } = authSlice.actions;
export default authSlice.reducer;
