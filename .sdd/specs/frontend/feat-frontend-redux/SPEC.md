# SPEC — Frontend Architecture (React + Redux Toolkit)
> **Feature ID:** `feat-frontend-redux`
> **UC Coverage:** UC-01, UC-02, UC-03, UC-18
> **Version:** 2.0 | **Status:** Implemented
> **Author:** Team | **Last Updated:** 2026-05-31

---

## 1. CONTEXT & GOAL

### 1.1 Bối cảnh
Frontend React 18 của hệ thống JLPT E-learning cần kiến trúc state management nhất quán để các component không bị prop-drill, API call không bị duplicate, và loading/error được xử lý đồng bộ. Đã migrate từ Zustand sang Redux Toolkit.

### 1.2 Mục tiêu
- Định nghĩa luồng dữ liệu một chiều: API → Redux Slice → Component
- Chuẩn hoá cách gọi API qua Axios instance duy nhất với interceptor
- Quản lý auth state (user, token) nhất quán xuyên suốt app
- Tất cả trang auth gọi API backend thực tế thay vì chỉ validate local

### 1.3 Tech Stack

| Layer | Công nghệ | Ghi chú |
|:---|:---|:---|
| UI | React 18 + JSX | Vite 5, không SSR |
| Styling | Tailwind CSS + Custom CSS | Per-page CSS files |
| State | Redux Toolkit + react-redux | `createAsyncThunk`, `createSlice` |
| HTTP | Axios | 1 instance + 2 interceptors |
| Routing | react-router-dom v6 | BrowserRouter |
| Validation | Client-side thủ công | Không dùng react-hook-form |

---

## 2. ACTOR

| Actor | Role | Điều kiện tiền quyết |
|:---|:---|:---|
| **Guest** | Người dùng chưa đăng nhập | Truy cập `/login`, `/register`, `/forgot-password`, `/reset-password` |
| **Student** | Đã đăng nhập | `isAuthenticated = true`, token hợp lệ trong localStorage |

---

## 3. FUNCTIONAL REQUIREMENTS (EARS)

### 3.1 Bootstrap & Store Initialization

| ID | EARS Requirement |
|:---|:---|
| FR-FE-01 | WHEN app khởi động, THE SYSTEM SHALL khởi tạo Redux store với `configureStore` và inject `<Provider store={store}>` bao ngoài toàn bộ cây component. |
| FR-FE-02 | WHEN store khởi tạo, THE SYSTEM SHALL rehydrate `auth.user` và `auth.isAuthenticated` bằng cách đọc `localStorage.getItem('jlpt-user')` và `localStorage.getItem('accessToken')`. |
| FR-FE-03 | IF `jlpt-user` hoặc `accessToken` không tồn tại trong localStorage, THEN THE SYSTEM SHALL khởi tạo `auth.user = null` và `auth.isAuthenticated = false`. |

### 3.2 Auth State Management

| ID | EARS Requirement |
|:---|:---|
| FR-FE-10 | THE SYSTEM SHALL duy trì `auth` slice với shape: `{ user, isAuthenticated, status, error }` trong Redux store. |
| FR-FE-11 | WHEN `loginThunk` fulfilled, THE SYSTEM SHALL set `user = student`, `isAuthenticated = true`, `status = 'succeeded'` và ghi `accessToken`, `refreshToken`, `jlpt-user` vào localStorage. |
| FR-FE-12 | WHEN `loginThunk` rejected, THE SYSTEM SHALL set `status = 'failed'` và `error = message từ backend`. |
| FR-FE-13 | WHEN `logoutThunk` fulfilled, THE SYSTEM SHALL reset `user = null`, `isAuthenticated = false`, `status = 'idle'` và xoá `accessToken`, `refreshToken`, `jlpt-user` khỏi localStorage. |
| FR-FE-14 | WHEN user bắt đầu nhập vào input, THE SYSTEM SHALL dispatch `clearError()` để xoá `auth.error` cũ nếu đang tồn tại. |

### 3.3 Axios Interceptor

| ID | EARS Requirement |
|:---|:---|
| FR-FE-20 | THE SYSTEM SHALL cấu hình một Axios instance duy nhất (`api`) với `baseURL = VITE_API_BASE_URL` hoặc `http://localhost:8080/api` và `timeout = 15000ms`. |
| FR-FE-21 | WHILE gửi bất kỳ request nào, THE SYSTEM SHALL request interceptor tự động đính `Authorization: Bearer <accessToken>` từ localStorage vào header nếu token tồn tại. |
| FR-FE-22 | WHEN response trả về HTTP 401 và request chưa được retry (`_retry = false`), THE SYSTEM SHALL gọi `POST /auth/refresh` bằng raw axios (không qua instance) với `refreshToken` từ localStorage. |
| FR-FE-23 | IF refresh thành công, THE SYSTEM SHALL cập nhật `accessToken` và `refreshToken` trong localStorage rồi retry request gốc với token mới. |
| FR-FE-24 | IF refresh thất bại (401), THE SYSTEM SHALL xoá `accessToken`, `refreshToken`, `jlpt-user` khỏi localStorage và redirect `window.location.href = '/login'`. |

### 3.4 Login Page (`/login`)

| ID | EARS Requirement |
|:---|:---|
| FR-FE-30 | WHEN Guest submit form login, THE SYSTEM SHALL validate client-side (email required, password required) trước khi dispatch `loginThunk`. |
| FR-FE-31 | WHILE `auth.status === 'loading'`, THE SYSTEM SHALL disable nút submit và hiển thị text "Đang đăng nhập...". |
| FR-FE-32 | IF `auth.error` không null, THE SYSTEM SHALL hiển thị `<div className="api-error">` với nội dung error phía trên nút submit. |
| FR-FE-33 | WHEN `loginThunk` fulfilled, THE SYSTEM SHALL navigate về `/dashboard`. |
| FR-FE-34 | WHEN Guest click "Đăng nhập với Google", THE SYSTEM SHALL redirect `window.location.href` về `/api/auth/oauth/google`. |

### 3.5 Register Page (`/register`)

| ID | EARS Requirement |
|:---|:---|
| FR-FE-40 | WHEN Guest submit form đăng ký, THE SYSTEM SHALL validate client-side: fullName required, email format, password ≥ 8 ký tự + 1 hoa + 1 số, confirmPassword khớp. |
| FR-FE-41 | WHEN `registerThunk` fulfilled (201), THE SYSTEM SHALL hiển thị success screen với email xác nhận. THE SYSTEM SHALL KHÔNG tự đăng nhập. |
| FR-FE-42 | IF backend trả 409 (email trùng), THE SYSTEM SHALL hiển thị `api-error` với message từ backend. |

### 3.6 Forgot Password Page (`/forgot-password`)

| ID | EARS Requirement |
|:---|:---|
| FR-FE-50 | WHEN Guest submit email, THE SYSTEM SHALL validate format email trước khi dispatch `forgotPasswordThunk`. |
| FR-FE-51 | WHEN `forgotPasswordThunk` fulfilled, THE SYSTEM SHALL hiển thị success screen. THE SYSTEM SHALL KHÔNG phân biệt email tồn tại hay không (backend đã handle). |
| FR-FE-52 | WHEN Guest click "Gửi lại" trên success screen, THE SYSTEM SHALL quay lại form và dispatch `clearError()`. |

### 3.7 Reset Password Page (`/reset-password`)

| ID | EARS Requirement |
|:---|:---|
| FR-FE-60 | WHEN trang load, THE SYSTEM SHALL đọc `token` từ query param `?token=`. |
| FR-FE-61 | IF `token` null hoặc không có, THE SYSTEM SHALL hiển thị màn hình "Link không hợp lệ" và KHÔNG render form. |
| FR-FE-62 | WHEN Guest submit form, THE SYSTEM SHALL validate password strength và confirm match trước khi dispatch `resetPasswordThunk({ token, newPassword, confirmPassword })`. |
| FR-FE-63 | WHEN `resetPasswordThunk` fulfilled, THE SYSTEM SHALL hiển thị success screen với link "Đăng nhập ngay". |

### 3.8 Profile — Fetch & Update

| ID | EARS Requirement |
|:---|:---|
| FR-FE-70 | WHEN trang profile mount, THE SYSTEM SHALL dispatch `fetchProfileThunk()` để lấy dữ liệu mới nhất từ backend (không dùng cache localStorage). |
| FR-FE-71 | WHEN `fetchProfileThunk` fulfilled, THE SYSTEM SHALL cập nhật `auth.user` trong Redux và ghi đè `jlpt-user` trong localStorage. |
| FR-FE-72 | WHEN Student submit form cập nhật hồ sơ, THE SYSTEM SHALL dispatch `updateProfileThunk(data)` với các field đã thay đổi. |
| FR-FE-73 | WHEN `updateProfileThunk` fulfilled, THE SYSTEM SHALL cập nhật `auth.user` trong Redux và ghi đè `jlpt-user` trong localStorage để đồng bộ session tiếp theo. |
| FR-FE-74 | THE SYSTEM SHALL KHÔNG cho phép Student tự thay đổi `email` hoặc `role` qua form profile — các field này chỉ hiển thị read-only. |

---

## 4. NON-FUNCTIONAL REQUIREMENTS

| ID | Category | Requirement |
|:---|:---|:---|
| NFR-FE-01 | Kiến trúc | Mọi API call phải đi qua Axios instance `api` — KHÔNG gọi `fetch` hay `axios` trực tiếp trong component |
| NFR-FE-02 | Kiến trúc | KHÔNG dùng `useDispatch`/`useSelector` thô — chỉ dùng `useAppDispatch`/`useAppSelector` từ `store/hooks.js` |
| NFR-FE-03 | Kiến trúc | Business logic (tính điểm, phân quyền) KHÔNG được đặt ở Frontend — chỉ ở Backend Service layer |
| NFR-FE-04 | Security | KHÔNG lưu token vào biến module-level hay component state — chỉ localStorage và qua interceptor |
| NFR-FE-05 | Security | KHÔNG log token ra console |
| NFR-FE-06 | UX | Mọi API call phải có loading state trên button (`disabled` + text thay đổi) |
| NFR-FE-07 | UX | Lỗi API hiện qua `auth.error` (Redux) — lỗi validation field hiện qua local `fieldErrors` state |
| NFR-FE-08 | Maintainability | Mỗi trang tối đa 150 dòng — tách component nếu vượt quá |

---

## 5. DATA MODEL

### 5.1 Redux State

```js
// Root State — store/store.js
{
  auth: AuthState
  // Sẽ thêm: course, quiz, flashcard, ui
}

// AuthState — store/slices/authSlice.js
{
  user: null | {
    id:               number,
    fullName:         string,
    email:            string,
    role:             'STUDENT' | 'STAFF' | 'ADMIN',
    currentJlptLevel: string | null,   // 'N5'...'N1'
    avatarUrl:        string | null,
    subscription:     'FREE' | 'VIP'
  },
  isAuthenticated: boolean,
  status:          'idle' | 'loading' | 'succeeded' | 'failed',
  error:           string | null
}
```

### 5.2 localStorage Keys

| Key | Kiểu | Mô tả | Set bởi | Xoá bởi |
|:---|:---|:---|:---|:---|
| `accessToken` | string | JWT (15 phút) | `loginThunk` | `logoutThunk`, interceptor fail |
| `refreshToken` | string | Refresh token (7 ngày) | `loginThunk` | `logoutThunk`, interceptor fail |
| `jlpt-user` | JSON string | User object | `loginThunk` | `logoutThunk`, interceptor fail |

### 5.3 File Structure

```
apps/frontend/src/
├── api/
│   └── authService.js        # Axios instance + interceptors + 7 API functions
├── store/
│   ├── store.js              # configureStore({ auth })
│   ├── hooks.js              # useAppDispatch, useAppSelector
│   └── slices/
│       └── authSlice.js      # state + 5 thunks + clearError + logout
├── pages/
│   ├── login/
│   │   ├── Login.jsx
│   │   └── Login.css
│   ├── register/
│   │   ├── Register.jsx
│   │   └── Register.css
│   └── forgot-password/
│       ├── ForgotPassword.jsx
│       ├── ForgotPassword.css
│       ├── ResetPassword.jsx
│       └── ResetPassword.css
├── App.jsx                   # BrowserRouter + Routes (5 route)
├── main.jsx                  # ReactDOM.createRoot + <Provider>
└── index.css                 # Tailwind directives
```

---

## 6. LUỒNG DỮ LIỆU

### 6.1 Bootstrap

```
Browser load → Vite inject main.jsx
  └── import store → authSlice init:
        localStorage('jlpt-user') + localStorage('accessToken') tồn tại?
        ├── CÓ  → user = parsed, isAuthenticated = true
        └── KHÔNG → user = null, isAuthenticated = false
  └── render <Provider store><App /></Provider>
  └── App.jsx: render Routes theo URL hiện tại
```

### 6.2 Luồng tổng quát gọi API

```
Component
  │ dispatch(someThunk(payload))
  ▼
authSlice: status = 'loading', error = null
  │ gọi authService.fn()
  ▼
Axios request interceptor
  └── đính Authorization: Bearer <localStorage.accessToken>
  │ HTTP → Backend
  ▼
┌─ 2xx ──────────────────────────────────────────────────┐
│  response interceptor pass-through                     │
│  thunk fulfilled → authSlice: status='succeeded'       │
│  Component: navigate / setIsDone(true)                 │
└────────────────────────────────────────────────────────┘
┌─ 401 (lần đầu) ────────────────────────────────────────┐
│  _retry = true                                         │
│  raw axios POST /auth/refresh                          │
│  ├── OK  → update localStorage tokens → retry request  │
│  └── FAIL → clear localStorage → redirect /login       │
└────────────────────────────────────────────────────────┘
┌─ 4xx/5xx ──────────────────────────────────────────────┐
│  thunk rejectWithValue(message)                        │
│  authSlice: status='failed', error=message             │
│  Component: <div className="api-error">{error}</div>   │
└────────────────────────────────────────────────────────┘
```

### 6.3 Thunks & Side Effects

| Thunk | Endpoint | localStorage (thành công) | Sau khi xong |
|:---|:---|:---|:---|
| `loginThunk` | POST `/auth/login` | set 3 keys | navigate `/dashboard` |
| `registerThunk` | POST `/auth/register` | — | success screen |
| `logoutThunk` | POST `/auth/logout` | remove 3 keys | redirect `/login` |
| `forgotPasswordThunk` | POST `/auth/forgot-password` | — | success screen |
| `resetPasswordThunk` | POST `/auth/reset-password` | — | success screen |
| `fetchProfileThunk` | GET `/students/me` | ghi đè `jlpt-user` | cập nhật `auth.user` |
| `updateProfileThunk` | PUT `/students/me` | ghi đè `jlpt-user` | cập nhật `auth.user` |

---

## 7. API SPEC

### Các endpoint Frontend gọi

| Function | Method | URL | Auth |
|:---|:---|:---|:---|
| `login` | POST | `/auth/login` | Không |
| `register` | POST | `/auth/register` | Không |
| `logout` | POST | `/auth/logout` | Bearer JWT |
| `forgotPassword` | POST | `/auth/forgot-password` | Không |
| `resetPassword` | POST | `/auth/reset-password` | Không |
| `getProfile` | GET | `/students/me` | Bearer JWT |
| `updateProfile` | PUT | `/students/me` | Bearer JWT |

> Request/Response format đầy đủ: xem `feat-auth/SPEC.md § 6`

---

## 8. ERROR HANDLING

| Tình huống | Nơi xử lý | Hiển thị |
|:---|:---|:---|
| Field trống / sai format | Local `fieldErrors` state | `<span className="field-error">` inline dưới input |
| Lỗi API (4xx/5xx) | `auth.error` trong Redux | `<div className="api-error">` phía trên button submit |
| Token hết hạn (401) | Axios interceptor | Tự động refresh — user không thấy gì |
| Refresh thất bại | Axios interceptor | Redirect `/login` — page reload |
| Không có token URL (`/reset-password`) | Component local check | Màn hình "Link không hợp lệ" |

---

## 9. ACCEPTANCE CRITERIA

| ID | Scenario | Given | When | Then |
|:---|:---|:---|:---|:---|
| AC-FE-01 | App rehydrate session | accessToken + jlpt-user trong localStorage | F5 reload | `isAuthenticated=true`, user object khôi phục từ localStorage |
| AC-FE-02 | Login thành công | Backend trả 200 + token | Submit đúng credentials | 3 keys set vào localStorage, Redux user cập nhật, navigate /dashboard |
| AC-FE-03 | Login sai mật khẩu | Backend trả 401 | Submit sai credentials | `api-error` hiện message từ backend, button enable lại |
| AC-FE-04 | Login loading state | Submit form | Đang chờ API | Button disabled + text "Đang đăng nhập..." |
| AC-FE-05 | Clear error khi gõ | `auth.error` đang có giá trị | User gõ vào email/password | `auth.error` = null, `api-error` ẩn |
| AC-FE-06 | Register email trùng | Backend trả 409 | Submit email đã có | `api-error` hiện "Email đã được sử dụng" |
| AC-FE-07 | Register thành công | Backend trả 201 | Submit form hợp lệ | Success screen hiện email xác nhận, KHÔNG tự đăng nhập |
| AC-FE-08 | Forgot password gửi | Backend trả 200 | Submit email | Success screen hiện — bất kể email có tồn tại hay không |
| AC-FE-09 | Reset password không có token | URL `/reset-password` không có `?token=` | Mở URL | Màn hình "Link không hợp lệ", không có form |
| AC-FE-10 | Reset password thành công | Token hợp lệ, form hợp lệ | Submit | Success screen + link "Đăng nhập ngay" |
| AC-FE-11 | Auto refresh token | accessToken hết hạn | Gửi bất kỳ request | Interceptor refresh tự động, request retry thành công |
| AC-FE-12 | Refresh thất bại | refreshToken hết hạn | Gửi request | localStorage clear, redirect /login |
| AC-FE-13 | Fetch profile đồng bộ | Student mở trang profile | Component mount | `fetchProfileThunk` gọi API, `auth.user` + `jlpt-user` cập nhật từ backend |
| AC-FE-14 | Update profile thành công | Student submit form profile hợp lệ | `updateProfileThunk` fulfilled | `auth.user` trong Redux cập nhật, `jlpt-user` localStorage ghi đè |
| AC-FE-15 | Email/role read-only | Student ở trang profile | Xem form | Field email và role chỉ hiển thị, không có input chỉnh sửa |

---

## 10. GOLDEN PATTERN — Component mới

```jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { someThunk, clearError } from '../../store/slices/authSlice';

function SomePage() {
  const navigate    = useNavigate();
  const dispatch    = useAppDispatch();
  const { status, error } = useAppSelector((state) => state.auth);
  const isLoading   = status === 'loading';

  const [fieldErrors, setFieldErrors] = useState({});

  function validate() { /* client-side rules */ }

  function handleChange(e) {
    // cập nhật local state
    if (error) dispatch(clearError());   // xoá API error cũ
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;
    try {
      await dispatch(someThunk(payload)).unwrap();
      navigate('/next-page');
    } catch {
      // error đã vào Redux state qua rejected case
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      {error && <div className="api-error">{error}</div>}

      <input onChange={handleChange} />
      {fieldErrors.field && <span className="field-error">{fieldErrors.field}</span>}

      <button type="submit" disabled={isLoading}>
        {isLoading ? 'Đang xử lý...' : 'Submit'}
      </button>
    </form>
  );
}
```

---

## OUT OF SCOPE

- ❌ `ProtectedRoute` / `RoleGuard` — chưa có trang sau login; thêm khi implement dashboard
- ❌ Refresh token race condition (failedQueue) — xử lý khi có authenticated routes
- ❌ Redux slice khác (`course`, `quiz`, `flashcard`, `ui`) — thêm theo từng feature
- ❌ `redux-persist` middleware — hiện rehydrate thủ công từ localStorage
- ❌ TypeScript — project dùng JavaScript thuần
- ❌ React Hook Form / Zod — validation đang viết thủ công
