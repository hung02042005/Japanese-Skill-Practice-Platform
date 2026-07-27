# Quy Định Cấu Trúc Frontend Hiện Tại

> **Dự án:** SakuJi JLPT Learning Platform  
> **Phạm vi:** `apps/frontend/src`  
> **Cập nhật:** 2026-07-27  
> **Nguồn đối chiếu:** `apps/frontend/src/App.jsx`, `vite.config.js`, `package.json`, `src/features`, `src/shared`, `src/store`

---

## 1. Tổng Quan Kiến Trúc Frontend

Frontend hiện dùng **React 18 + Vite + React Router v6 + Redux Toolkit + Axios + Vitest**. Cấu trúc code theo hướng **feature-first**: màn hình nghiệp vụ nằm trong `src/features`, tài nguyên dùng chung nằm trong `src/shared`, state global nằm trong `src/store`.

```txt
apps/frontend/src/
├── features/
│   ├── auth/
│   ├── courses/
│   ├── dashboard/
│   ├── dictionary/
│   ├── grammar/
│   ├── kana/
│   ├── kanji/
│   ├── management/
│   ├── mock-test/
│   ├── notebook/
│   ├── notifications/
│   ├── onboarding/
│   ├── profile/
│   ├── progress/
│   ├── public/
│   ├── quiz/
│   ├── settings/
│   ├── speaking/
│   └── vocabulary/
├── shared/
│   ├── api/
│   ├── components/
│   ├── context/
│   ├── data/
│   ├── hooks/
│   ├── test/
│   └── utils/
└── store/
    ├── hooks.js
    └── store.js
```

### Tác dụng của các phần cấp cao

| Phần | Tác dụng | Khi nào chỉnh sửa/thêm mới |
| :--- | :--- | :--- |
| `features` | Chứa toàn bộ màn hình và logic UI theo từng nghiệp vụ như auth, học tập, quiz, quản trị. Đây là nơi developer làm việc nhiều nhất khi thêm/sửa chức năng người dùng nhìn thấy. | Khi thêm màn hình, flow, modal, form hoặc UI thuộc một domain cụ thể. |
| `shared/api` | Đóng vai trò lớp giao tiếp với backend, gom các hàm Axios theo vai trò/nghiệp vụ. Giúp component không phụ thuộc trực tiếp vào URL và response shape. | Khi có endpoint mới hoặc cần chuẩn hóa cách gọi API. |
| `shared/components` | Chứa component dùng lại nhiều nơi như layout, route guard, badge, toast, icon, empty state. Giúp UI nhất quán và giảm duplicate. | Khi một component được dùng ở từ 2 feature trở lên hoặc là primitive/layout chung. |
| `shared/context` | Chứa React Context dùng cho trạng thái toàn app nhưng không nhất thiết đưa vào Redux, ví dụ toast/global provider. | Khi state cần bọc toàn app nhưng không phải domain data phức tạp. |
| `shared/hooks` | Chứa custom hook dùng lại, giúp tái sử dụng logic UI như countdown, debounce hoặc API interaction nhẹ. | Khi cùng một logic hook xuất hiện ở nhiều component. |
| `shared/utils` | Chứa hàm thuần xử lý format, message, validation UX, mapping label. Không chứa side effect hoặc nghiệp vụ backend-owned. | Khi cần helper dùng chung và có thể test độc lập. |
| `shared/data` | Chứa dữ liệu tĩnh dùng chung như option list, label map, config UI không nhạy cảm. | Khi nhiều màn hình dùng cùng danh sách hằng số hiển thị. |
| `shared/test` | Chứa setup test toàn frontend. Đảm bảo Vitest/RTL có môi trường thống nhất. | Khi cần cấu hình test global, matcher hoặc mock browser API. |
| `store` | Cấu hình Redux store và global hooks. Là nơi ghép các slice domain. | Khi thêm slice thật sự cần global state hoặc async workflow lớn. |
| `App.jsx` | Trung tâm khai báo route, route guard và lazy-load page. | Khi thêm/bỏ route hoặc thay đổi phân quyền truy cập màn hình. |
| `vite.config.js` | Cấu hình build/dev server, alias `@`, proxy `/api`, test environment. | Khi thay đổi alias, port, proxy backend, build/test config. |
Quy tắc nền:

- Mỗi tính năng chính có thư mục riêng trong `features`.
- Component/hook/API dùng lại nhiều nơi đặt trong `shared`.
- Không đặt business logic quan trọng ở frontend; frontend chỉ render, gọi API và xử lý UX state.
- Backend là nguồn quyết định cho điểm số, quyền truy cập, trạng thái bài nộp, audit và validation nghiệp vụ.
- Dùng alias `@` trỏ đến `apps/frontend/src` theo `vite.config.js`.

---

## 2. Quy Ước Thư Mục Theo Feature

**Tác dụng:** Giúp gom page, component, CSS và state theo đúng nghiệp vụ. Khi cần sửa một chức năng, developer có thể đi thẳng vào feature tương ứng thay vì tìm rải rác toàn app.

Mỗi feature nên giữ cùng pattern hiện tại:

```txt
features/<feature-name>/
├── <page-folder>/
│   ├── PageName.jsx
│   └── PageName.css
├── components/
│   └── FeatureComponent.jsx
└── featureSlice.js        # chỉ khi feature cần Redux global state
```

Ví dụ thực tế:

```txt
features/auth/login/Login.jsx
features/auth/login/Login.css
features/kanji/components/KanjiWritingCanvas.jsx
features/kanji/kanji/KanjiPractice.jsx
features/management/admin/AdminDashboard.jsx
features/management/components/staff/QuestionFormModal.jsx
```

Quy tắc:

- Page route chính dùng `PascalCase.jsx` và CSS cùng tên nếu page có style riêng.
- Component con đặt trong `components` của feature nếu chỉ phục vụ feature đó.
- Component dùng chung nhiều feature chuyển sang `shared/components`.
- File slice đặt cạnh feature nếu state thuộc feature đó.
- Không tạo thư mục `pages` riêng mới; hệ thống hiện đang dùng `features/<domain>/<screen>`.

---

## 3. Nhóm Management

**Tác dụng:** Tách rõ ba vùng quản trị Admin, Staff và StaffManager để tránh lẫn quyền, lẫn nghiệp vụ và lẫn UI. Đây là phần quan trọng nhất để bảo vệ các thao tác quản trị nhạy cảm.

Khu vực quản trị được gom trong `features/management` và chia theo vai trò:

```txt
features/management/
├── admin/
│   ├── AdminDashboard.jsx
│   ├── AdminReports.jsx
│   ├── AdminSettings.jsx
│   └── ManageUsers.jsx
├── manager/
│   ├── ManagerDashboard.jsx
│   ├── ManagerReviewQueue.jsx
│   ├── ManagerContentPipeline.jsx
│   ├── ManagerDeletedTopics.jsx
│   ├── ManagerNotifications.jsx
│   └── ManagerTickets.jsx
├── staff/
│   ├── StaffDashboard.jsx
│   ├── StaffContent.jsx
│   ├── StaffQuestions.jsx
│   ├── StaffAssessments.jsx
│   ├── StaffTickets.jsx
│   ├── StaffGrading.jsx
│   └── StaffStudents.jsx
└── components/
    ├── admin/
    ├── manager/
    └── staff/
```

Quy định:

- Không dùng chung màn hình Admin/Staff/Manager nếu nghiệp vụ hoặc quyền khác nhau.
- Component có tính vai trò rõ ràng đặt trong `components/admin`, `components/staff`, hoặc `components/manager`.
- StaffManager dùng `ManagerRoute` và backend kiểm tra `staffRole=staff_manager`.
- Không chỉ ẩn nút trên UI để coi là phân quyền; backend phải trả `401/403`.

---

## 4. Shared Layer

**Tác dụng:** Là lớp tái sử dụng chung, giúp UI nhất quán, giảm duplicate và giữ feature folder gọn. Chỉ đưa code vào đây khi nó thật sự dùng chung hoặc là hạ tầng frontend.

`shared` chứa tài nguyên dùng lại nhiều nơi:

```txt
shared/
├── api/
│   ├── authService.js
│   ├── studentService.js
│   ├── staffService.js
│   ├── managerService.js
│   ├── adminService.js
│   └── adminNotificationService.js
├── components/
│   ├── common/
│   └── layout/
├── context/
├── data/
├── hooks/
├── test/
└── utils/
```

Quy định:

- API function dùng chung đặt trong `shared/api`.
- Layout dùng lại đặt trong `shared/components/layout`.
- Component cơ bản như badge, route guard, toast, icon dùng chung đặt trong `shared/components/common`.
- Utility thuần đặt trong `shared/utils`.
- Hook dùng lại nhiều nơi đặt trong `shared/hooks`.
- Test setup đặt trong `shared/test/setup.js`.

---

## 5. API Service

**Tác dụng:** Là lớp biên giữa frontend và backend. Component chỉ gọi function có nghĩa nghiệp vụ, còn URL, params và unwrap response được gom ở service để dễ sửa và dễ test.

Frontend gọi backend qua Axios service trong `shared/api`. Vite proxy route `/api` sang backend `http://localhost:8080`.

Quy định:

- Không gọi `fetch`/`axios` trực tiếp rải rác trong component khi đã có service phù hợp.
- Tên function dùng `camelCase`: `getDashboard`, `updateSettings`, `submitSpeakingAudio`.
- Service trả về `res.data.data` nếu caller chỉ cần payload nghiệp vụ.
- Component phải xử lý đủ `loading`, `error`, `empty` nếu dữ liệu từ API.
- Không gửi điểm số cuối cùng từ client; gửi answers/audio/strokes, backend xử lý kết quả.

Ví dụ pattern:

```js
export async function getVocabularyList({ level, topic, search, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (level) params.level = level;
  if (topic) params.topic = topic;
  if (search) params.search = search;
  const res = await api.get('/vocabulary', { params });
  return res.data.data;
}
```

---

## 6. Routing

**Tác dụng:** Quản lý bản đồ màn hình của ứng dụng và lớp bảo vệ truy cập đầu tiên ở frontend. Route guard giúp điều hướng đúng UX, còn backend vẫn là nơi quyết định quyền cuối cùng.

Route được khai báo tập trung trong `src/App.jsx`.

Nhóm guard hiện tại:

- `PrivateRoute`: Student/authenticated user.
- `StaffRoute`: Staff.
- `ManagerRoute`: StaffManager.
- `AdminRoute`: Admin.

Quy định:

- Route mới phải đăng ký trong `App.jsx`.
- Admin/Staff/Manager page nên lazy-load như pattern hiện tại để giảm bundle ban đầu.
- Route protected phải dùng guard đúng vai trò.
- Không tạo route cho feature chưa có backend/API thực tế nếu không ghi rõ là dev/internal hoặc future.

Ví dụ:

```jsx
const StaffQuestions = lazy(() => import('./features/management/staff/StaffQuestions'));

<Route
  path="/staff/questions"
  element={<StaffRoute><StaffQuestions /></StaffRoute>}
/>
```

---

## 7. Redux Store

**Tác dụng:** Lưu trạng thái dùng chung qua nhiều màn hình hoặc workflow phức tạp. Redux không dùng cho mọi state; modal, tab, input tạm thời nên để local trong component.

Redux Toolkit hiện được cấu hình tại `src/store/store.js`.

Slice hiện có:

```txt
auth
student
staffGrammar
staffQuestion
staffQuiz
staffExam
managerReview
staffLearning
publishedContent
```

Quy định:

- Chỉ thêm Redux slice khi state cần dùng qua nhiều component/route hoặc có async workflow đủ lớn.
- UI state cục bộ như modal open, tab active, input form nên dùng `useState` trong component.
- Slice đặt trong feature sở hữu nghiệp vụ, sau đó import vào `store/store.js`.
- Hook Redux dùng qua `src/store/hooks.js`.

---

## 8. CSS Và Design System

**Tác dụng:** Giữ giao diện đồng bộ với Hanami/SakuJi design system, hạn chế lệch màu, lệch spacing và xung đột class. CSS nằm gần component giúp sửa UI nhanh mà vẫn dễ truy vết.

Frontend hiện dùng CSS file thường, đặt cạnh page/component. Không dùng CSS Modules.

Quy định:

- Page có CSS riêng cùng thư mục: `PageName.jsx` + `PageName.css`.
- Component lớn có CSS riêng nếu style phức tạp hoặc dùng lại nhiều.
- Ưu tiên CSS variable từ design system: `--color-primary`, `--color-bg`, `--radius-md`, `--shadow-sm`, v.v.
- Không hard-code màu nếu đã có token tương ứng.
- Class nên có prefix theo page/feature để tránh va chạm, ví dụ `adm-`, `stf-`, `mgr-`, `kana-`, `kanji-`, `voc-`.
- Luôn có responsive rule cho tablet/mobile với màn hình nhiều cột, bảng hoặc panel.
- Tôn trọng `prefers-reduced-motion` với animation.

---

## 9. Component Rules

**Tác dụng:** Giữ page dễ đọc, component có trách nhiệm rõ và UI có thể tái sử dụng. Quy tắc này giảm rủi ro tạo god component, duplicate modal/table/form và lỗi accessibility.

Quy tắc tách component:

- Page component chịu trách nhiệm lấy dữ liệu, điều hướng và ghép layout.
- Component con chịu trách nhiệm render một phần UI rõ ràng.
- Modal/drawer/table/form lớn nên tách thành component riêng.
- Icon dùng `lucide-react` hoặc icon nội bộ hiện có trong `shared/components/common/icons`/`ManageUsersIcons.jsx`; không copy SVG rời rạc nhiều nơi.
- Component API-backed phải có loading/error/empty.
- Button icon phải có `aria-label`.
- Form input phải có label liên kết qua `htmlFor`/`id`.

Không nên:

- Viết page quá dài chứa nhiều modal/table/form inline.
- Trộn logic Admin, Staff và Manager trong cùng một component lớn.
- Để component tự quyết định quyền truy cập quan trọng chỉ bằng JavaScript.
- Tính điểm quiz/exam/AI ở frontend.

---

## 10. Test

**Tác dụng:** Bảo vệ UX frontend khỏi regression ở form, route, render state và Redux slice. Test frontend không thay thế backend test cho nghiệp vụ quan trọng.

Stack test hiện tại: **Vitest + React Testing Library + jsdom**.

Vị trí test hiện có:

```txt
features/auth/login/Login.test.jsx
features/auth/register/Register.test.jsx
features/auth/authSlice.test.js
shared/utils/apiMessage.test.js
```

Quy định:

- Test component đặt cạnh feature/component được test.
- Test utility đặt cạnh utility hoặc trong khu vực shared tương ứng.
- Frontend test chỉ kiểm UX: render, validation client, loading/error, redirect, gọi handler/API mock.
- Business rule quan trọng phải test ở backend.

Lệnh:

```bash
npm run test
npm run test:watch
npm run build
```

---

## 11. Checklist Khi Tạo Feature Frontend Mới

**Tác dụng:** Là danh sách kiểm trước khi bàn giao một màn hình/feature mới, giúp không quên route guard, API service, state, responsive, accessibility và build.

- [ ] Xác định actor: Student, Staff, StaffManager, Admin hay Guest.
- [ ] Xác định route trong `App.jsx` và guard đúng vai trò.
- [ ] Tạo thư mục trong `features/<feature>/<screen>`.
- [ ] Tạo page `PascalCase.jsx` và CSS cùng tên nếu cần.
- [ ] Thêm hoặc dùng lại API function trong `shared/api`.
- [ ] Thêm slice Redux chỉ khi state thật sự dùng xuyên route/component.
- [ ] Có loading, error, empty state cho API-backed UI.
- [ ] Không đặt business logic quan trọng ở frontend.
- [ ] Dùng token design trong `DESIGN.md`/`DESIGN.vi.md`.
- [ ] Responsive desktop/tablet/mobile.
- [ ] Button icon có `aria-label`, form có label, error có thông báo rõ.
- [ ] Chạy `npm run build` trước khi bàn giao nếu có sửa code frontend.

---

## 12. Quy Tắc Đồng Bộ Tài Liệu

**Tác dụng:** Giữ tài liệu, route và code không lệch nhau. Khi frontend đổi mà docs không đổi, test/spec rất dễ nghiệm thu nhầm feature chưa tồn tại hoặc bỏ sót feature đã có.

Khi frontend thay đổi:

- Thêm route mới: cập nhật tài liệu use case/spec tương ứng.
- Thêm API service mới: đối chiếu backend controller và response format.
- Thêm màn hình quản trị mới: ghi rõ actor, route guard và quyền backend.
- Thêm design pattern mới: cập nhật `DESIGN.vi.md` hoặc tài liệu này.
- Bỏ route/feature: đánh dấu legacy/future trong feature spec, không để tài liệu claim là implemented.
