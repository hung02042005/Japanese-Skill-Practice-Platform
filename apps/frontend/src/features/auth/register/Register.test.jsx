import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '@/features/auth/authSlice';
import Register from './Register';

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
        <Register />
      </MemoryRouter>
    </Provider>,
  );
  return store;
}

describe('Register page', () => {
  // Regression test: cùng bug với Login — authSlice dùng chung field `error` cho nhiều thunk.
  // Lỗi còn sót từ trang khác (vd Login: "Sai mật khẩu") không được hiện lại khi Register mount.
  it('xoá lỗi còn sót từ trang khác ngay khi mount', () => {
    const store = renderWithStore({
      user: null,
      isAuthenticated: false,
      status: 'failed',
      error: 'Sai mật khẩu',
    });

    expect(screen.queryByText('Sai mật khẩu')).not.toBeInTheDocument();
    expect(store.getState().auth.error).toBeNull();
  });
});
