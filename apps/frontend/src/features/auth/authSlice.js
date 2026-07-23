import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as authService from '@/shared/api/authService';

/**
 * Trích lỗi từ axios error → { message, code }. `code` là errorCode máy-đọc do
 * backend trả (INVALID_CREDENTIALS, EMAIL_NOT_VERIFIED, TOKEN_EXPIRED…), giúp
 * component switch trên code ổn định thay vì dò chuỗi message tiếng Việt.
 * `data` (chuỗi chi tiết validate) được nối vào message nếu có.
 */
function extractError(err, fallback) {
  if (!err.response) {
    return {
      message: 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối và thử lại.',
      code: 'NETWORK_ERROR',
    };
  }
  const d = err.response.data;
  const detail = typeof d?.data === 'string' ? `: ${d.data}` : '';
  return { message: (d?.message ?? fallback) + detail, code: d?.code ?? null };
}

/** Gán lỗi vào state, chấp nhận cả payload dạng { message, code } lẫn chuỗi cũ. */
function applyError(state, payload) {
  state.status = 'failed';
  if (payload && typeof payload === 'object') {
    state.error = payload.message ?? null;
    state.errorCode = payload.code ?? null;
  } else {
    state.error = payload ?? null;
    state.errorCode = null;
  }
}

export const verifyEmailThunk = createAsyncThunk(
  'auth/verifyEmail',
  async ({ email, otpCode }, { rejectWithValue }) => {
    try {
      const res = await authService.verifyEmail(email, otpCode);
      return res.data;
    } catch (err) {
      return rejectWithValue(extractError(err, 'Xác minh email thất bại'));
    }
  },
);

export const resendVerificationThunk = createAsyncThunk(
  'auth/resendVerification',
  async (email, { rejectWithValue }) => {
    try {
      const res = await authService.resendVerification(email);
      return res.data;
    } catch (err) {
      return rejectWithValue(extractError(err, 'Gửi lại email xác minh thất bại'));
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
        staffRole,
      } = res.data;
      if (requirePasswordChange) {
        const userData = { role, staffRole, requirePasswordChange: true };
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
      if (staffRole) userData.staffRole = staffRole;
      localStorage.setItem('accessToken', accessToken);
      if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('jlpt-user', JSON.stringify(userData));
      return { user: userData, role };
    } catch (err) {
      return rejectWithValue(extractError(err, 'Đăng nhập thất bại'));
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
      return rejectWithValue(extractError(err, 'Đăng ký thất bại'));
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
      return rejectWithValue(extractError(err, 'Gửi email thất bại'));
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
      return rejectWithValue(extractError(err, 'Đặt lại mật khẩu thất bại'));
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
      return rejectWithValue(extractError(err, 'Không thể kiểm tra tài khoản'));
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
      return rejectWithValue(extractError(err, 'Gửi yêu cầu thất bại'));
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
      return rejectWithValue(extractError(err, 'Kích hoạt tài khoản thất bại'));
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
      return rejectWithValue(extractError(err, 'Đổi mật khẩu tạm thất bại'));
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
      return rejectWithValue(extractError(err, 'Đăng nhập bằng Google thất bại'));
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
    errorCode: null,  // mã lỗi máy-đọc từ backend (để component switch không dò chuỗi)
  },
  reducers: {
    clearError(state) {
      state.error = null;
      state.errorCode = null;
    },
    // Cập nhật thông tin user hiện tại (vd sau onboarding) + đồng bộ localStorage.
    setUser(state, action) {
      state.user = { ...state.user, ...action.payload };
      state.isAuthenticated = true;
      localStorage.setItem('jlpt-user', JSON.stringify(state.user));
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
        state.errorCode = null;
      })
      .addCase(loginThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.user = action.payload.user;
        state.isAuthenticated = true;
      })
      .addCase(loginThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      })
      // register
      .addCase(registerThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
        state.errorCode = null;
      })
      .addCase(registerThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(registerThunk.rejected, (state, action) => {
        applyError(state, action.payload);
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
        state.errorCode = null;
      })
      .addCase(forgotPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(forgotPasswordThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      })

      // resetPassword
      .addCase(resetPasswordThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
        state.errorCode = null;
      })
      .addCase(resetPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(resetPasswordThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      })

      // setupStaffPassword
      .addCase(setupStaffPasswordThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
        state.errorCode = null;
      })
      .addCase(setupStaffPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(setupStaffPasswordThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      })

      // checkAccountType
      .addCase(checkAccountTypeThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
        state.errorCode = null;
      })
      .addCase(checkAccountTypeThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(checkAccountTypeThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      })

      // staffForgotPassword
      .addCase(staffForgotPasswordThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
        state.errorCode = null;
      })
      .addCase(staffForgotPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(staffForgotPasswordThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      })

      // changeTempPassword
      .addCase(changeTempPasswordThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
        state.errorCode = null;
      })
      .addCase(changeTempPasswordThunk.fulfilled, (state) => {
        state.status = 'succeeded';
        state.user = null;
        state.isAuthenticated = false;
      })
      .addCase(changeTempPasswordThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      })

      // verifyEmail
      .addCase(verifyEmailThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
        state.errorCode = null;
      })
      .addCase(verifyEmailThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(verifyEmailThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      })

      // resendVerification — BUG-09 FIX: thunk còn thiếu
      .addCase(resendVerificationThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
        state.errorCode = null;
      })
      .addCase(resendVerificationThunk.fulfilled, (state) => {
        state.status = 'succeeded';
      })
      .addCase(resendVerificationThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      })

      // loginWithGoogle
      .addCase(loginWithGoogleThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
        state.errorCode = null;
      })
      .addCase(loginWithGoogleThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.user = action.payload;
        state.isAuthenticated = true;
      })
      .addCase(loginWithGoogleThunk.rejected, (state, action) => {
        applyError(state, action.payload);
      });
  },
});

export const { clearError, logout, setUser } = authSlice.actions;
export default authSlice.reducer;
