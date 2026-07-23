import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import Login from './Login';

// GoogleLogin chỉ hoạt động trong <GoogleOAuthProvider> (bọc ở main.jsx, không có ở đây) — mock để
// component render được trong test mà không cần provider thật.
vi.mock('@react-oauth/google', () => ({
  GoogleLogin: () => null,
}));

function renderWithStore(preloadedState) {
  const store = configureStore({
    reducer: { auth: authReducer },
    preloadedState: { auth: preloadedState },
  });
  render(
    <Provider store={store}>
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Login />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('Login page', () => {
  it('không hiện banner lỗi khi store sạch', () => {
    renderWithStore({ user: null, isAuthenticated: false, status: 'idle', error: null });
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  // Regression test: authSlice dùng chung field `error` cho nhiều thunk (login/register/...).
  // Trước khi sửa, lỗi từ trang khác (vd Register) còn sót lại trong Redux state sẽ hiện lại
  // ngay khi Login mount, dù người dùng chưa hề bấm gì. Login.jsx phải tự clearError() khi mount.
  it('xoá lỗi còn sót từ trang khác (vd Register) ngay khi mount, không hiện banner sai ngữ cảnh', () => {
    const store = renderWithStore({
      user: null,
      isAuthenticated: false,
      status: 'failed',
      error: 'Email đã được sử dụng',
    });

    expect(screen.queryByText('Email đã được sử dụng')).not.toBeInTheDocument();
    expect(store.getState().auth.error).toBeNull();
  });
});
