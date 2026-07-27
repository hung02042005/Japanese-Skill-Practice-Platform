import { describe, it, expect, beforeEach } from 'vitest';
import reducer, { clearError, logout, loginThunk } from './authSlice';

// Test trực tiếp reducer (pure function) bằng cách tự tạo action, KHÔNG dispatch
// thunk thật (không gọi axios) — createAsyncThunk vẫn expose loginThunk.pending/
// .fulfilled/.rejected như action creator bình thường, tạo đúng shape action mà
// reducer thực sự nhận trong lúc chạy thật, mà không cần mock network.

const requestId = 'test-request-id';
const credentials = { email: 'a@b.com', password: 'x' };

describe('authSlice reducer', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('state khởi tạo: chưa đăng nhập khi localStorage rỗng', () => {
    const state = reducer(undefined, { type: '@@INIT' });
    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
    expect(state.status).toBe('idle');
  });

  it('loginThunk.pending: chuyển status sang loading, xoá error cũ', () => {
    const prev = { user: null, isAuthenticated: false, status: 'idle', error: 'lỗi cũ' };
    const state = reducer(prev, loginThunk.pending(requestId, credentials));
    expect(state.status).toBe('loading');
    expect(state.error).toBeNull();
  });

  it('loginThunk.fulfilled: lưu user, đánh dấu đã đăng nhập', () => {
    const prev = { user: null, isAuthenticated: false, status: 'loading', error: null };
    const payload = { user: { email: 'a@b.com', role: 'STUDENT' }, role: 'STUDENT' };
    const state = reducer(prev, loginThunk.fulfilled(payload, requestId, credentials));
    expect(state.status).toBe('succeeded');
    expect(state.isAuthenticated).toBe(true);
    expect(state.user).toEqual(payload.user);
  });

  it('loginThunk.rejected: set status failed + giữ lại message lỗi từ rejectWithValue', () => {
    const prev = { user: null, isAuthenticated: false, status: 'loading', error: null };
    const state = reducer(
      prev,
      loginThunk.rejected(new Error('rejected'), requestId, credentials, 'Email hoặc mật khẩu không đúng'),
    );
    expect(state.status).toBe('failed');
    expect(state.error).toBe('Email hoặc mật khẩu không đúng');
    // Sai đăng nhập không được để lộ user cũ hoặc tự bật isAuthenticated.
    expect(state.isAuthenticated).toBe(false);
  });

  it('clearError: xoá error mà không đụng tới các field khác', () => {
    const prev = { user: { email: 'a@b.com' }, isAuthenticated: true, status: 'failed', error: 'lỗi' };
    const state = reducer(prev, clearError());
    expect(state.error).toBeNull();
    expect(state.user).toEqual(prev.user);
    expect(state.isAuthenticated).toBe(true);
  });

  it('logout: xoá sạch state VÀ xoá hết token/user khỏi localStorage', () => {
    localStorage.setItem('accessToken', 'abc');
    localStorage.setItem('refreshToken', 'def');
    localStorage.setItem('jlpt-user', JSON.stringify({ email: 'a@b.com' }));

    const prev = { user: { email: 'a@b.com' }, isAuthenticated: true, status: 'succeeded', error: null };
    const state = reducer(prev, logout());

    expect(state.user).toBeNull();
    expect(state.isAuthenticated).toBe(false);
    expect(state.status).toBe('idle');
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('jlpt-user')).toBeNull();
  });
});
