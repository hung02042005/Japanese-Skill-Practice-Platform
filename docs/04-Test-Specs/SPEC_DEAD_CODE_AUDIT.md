# SPEC — Rà Soát Màn Frontend Thừa & Logic Backend Thừa (Dead / Redundant Code Audit)

> **Loại**: Audit spec (chưa code — mô tả phát hiện + hành động đề xuất)
> **Ngày**: 2026-07-06
> **Phạm vi**: `apps/frontend/src`, `apps/backend/src/main/java/com/jlpt`
> **Nhánh**: `branch_for_hung`
> **Liên quan**: `CLAUDE.md § ANTI-PATTERNS` (Dead Code, Client-trusted Data, Business Logic in Frontend), memory `project_quiz_exam_duplication`

---

## 0. Tóm tắt (TL;DR)

| # | Hạng mục | Loại | Mức độ | Hành động |
|---|----------|------|--------|-----------|
| F1 | `pages/learn/LearnNew.jsx` — page không được route, không ai import | FE chết | 🔴 Cao | Xóa page + CSS |
| F2 | `DEMO_MODE = true` — `LessonDetail` & `Progress` trả **mock** thay vì backend thật | FE giả/lệch | 🔴 Cao | Tắt DEMO_MODE, gỡ nhánh mock |
| F3 | `pages/listening/Listening.jsx` — 100% mock hardcode, **không có backend**, lộ đáp án ở client | FE giả + lỗ bảo mật | 🔴 Cao | Xây backend hoặc gỡ khỏi nav |
| F4 | `pages/speaking/SpeakingPage.jsx` — gọi `/speaking/*` nhưng **không có controller** | FE mồ côi | 🟠 Vừa | Gỡ / defer rõ ràng |
| F5 | `api/mockData.js` — ~11/16 export `MOCK_*` không ai dùng | FE chết | 🟡 Thấp | Thu gọn/xóa sau F2 |
| B1 | `StaffQuizController` (`/api/staff/assessments`) vs `StaffExamController` (`/api/staff/exams`) — 2 cây CRUD gần trùng khít trên **cùng bảng** `assessments` | BE trùng lặp | 🟠 Vừa | Gộp (viết test trước) |

**Không phải lỗi (đã kiểm chứng, ghi để tránh báo nhầm):**
`StudentAssessmentController` (`/api/assessments`) và `TestAttemptController` (`/api/test-attempts`) **bổ trợ** nhau (start/submit vs review), **không** trùng lặp — giữ nguyên. *(Cập nhật: endpoint `/status` của TestAttemptController đã bị xoá — xem B2.)*

---

## 0.1 Nhật ký thực thi (cập nhật 2026-07-06)

| Hạng mục | Quyết định | Trạng thái |
|----------|-----------|-----------|
| **F1** LearnNew | Xoá | ✅ **ĐÃ XOÁ** (page + css) — build+lint xanh |
| **F3** Listening | Gỡ hẳn (chọn Q1-b) | ✅ **ĐÃ XOÁ** — page + route + ô Dashboard + link `LISTENING` ở LessonCard |
| **F4** Speaking | **GIỮ source, defer công khai** | ✅ **ĐÃ GỠ ĐIỂM VÀO NAV** (2026-07-09) — Dashboard tile "Luyện nói" + StaffDashboard stat-card/action-card "Chấm Bài Nói" đã gỡ; route giữ + comment DEFER. Source `SpeakingPage`/`StaffGrading`/3 hàm service GIỮ nguyên; `/speaking/*` vẫn 404 nếu vào route trực tiếp |
| **Manager stubs** | Xoá | ✅ **ĐÃ XOÁ** `ManagerReports` + `ManagerStaffPerformance` (page + route + 2 tab nav + 2 action card) — build+lint xanh |
| **StaffGrading** | Gỡ điểm vào | ✅ Cùng F4 (2026-07-09) — link nav đã gỡ, page mock 100% giữ làm WIP |
| **B2** 4 endpoint chết | Xoá | ✅ **ĐÃ XOÁ** endpoint + service method + DTO + test — `mvn test-compile` BUILD SUCCESS |
| **F2 / F5** DEMO_MODE + mockData | Xoá | ✅ **ĐÃ LÀM** (2026-07-09) — gỡ `DEMO_MODE` khỏi `LessonDetail`+`Progress` (nối backend thật; `markProgress`/`addToFlashcard` không còn no-op); **xoá cả `api/mockData.js`** (682 dòng, hết nơi import) |
| **B1** Quiz/Exam trùng | Defer | ⏳ Giữ, gộp sau khi có test |

---

## 1. FRONTEND

### F1 — `LearnNew.jsx`: page chết hoàn toàn 🔴

**Bằng chứng**
- Không xuất hiện trong `App.jsx` (không có `<Route>` nào, không có `import`).
- Grep toàn `src`: `LearnNew` chỉ tự tham chiếu (`pages/learn/LearnNew.jsx`, `.css`) và một comment trong `mockData.js`.
- Toàn bộ dữ liệu lấy từ `MOCK_NEXT_LESSON`, `MOCK_SUGGESTED_LESSONS` dưới cờ `DEMO_MODE`.

**Vi phạm**: `CLAUDE.md § Code Anti-Patterns → Dead Code` ("Xóa ngay, dùng Git recover").

**Hành động**
- Xóa `pages/learn/LearnNew.jsx` và `pages/learn/LearnNew.css`.
- Xóa export `MOCK_NEXT_LESSON`, `MOCK_SUGGESTED_LESSONS` (chỉ dùng bởi page này) — xem F5.

---

### F2 — `DEMO_MODE = true`: 2 màn thật đang chạy mock 🔴

**Bằng chứng** — [`api/mockData.js:5`](../../apps/frontend/src/api/mockData.js#L5)
```js
export const DEMO_MODE = true; // "Set = false khi kết nối backend thật"
```
Cờ này đang bật, khiến:
- [`pages/lessons/LessonDetail.jsx:32`](../../apps/frontend/src/pages/lessons/LessonDetail.jsx#L32) — `if (DEMO_MODE)` trả `MOCK_LESSON_DETAIL_MAP`, **bỏ qua** call backend; đồng thời `if (!DEMO_MODE)` bọc luôn `markProgress()` và `addToFlashcard()` → **tiến độ học & flashcard KHÔNG được ghi**.
- [`pages/progress/Progress.jsx:28,48`](../../apps/frontend/src/pages/progress/Progress.jsx#L28) — trả `MOCK_STATS`, `MOCK_EXAM_HISTORY` thay vì thống kê thật.

Backend cho 2 màn này **đã tồn tại** (`StudentLessonController`, `StudentLearningProgressController`) → mock là scaffolding cũ chưa gỡ.

**Rủi ro**: Người dùng thấy dữ liệu giả; hành vi "hoàn thành bài học" là no-op. Đây là bug im lặng, không phải chỉ là code thừa.

**Hành động**
1. Đặt `DEMO_MODE = false` (hoặc bỏ hẳn cờ).
2. Gỡ mọi nhánh `if (DEMO_MODE) { ... }`, giữ lại đường đi backend thật.
3. Chạy `/verify` màn `LessonDetail` + `Progress` với `student1@sakuji.com` để xác nhận số liệu thật hiển thị và tiến độ được ghi.

---

### F3 — `Listening.jsx`: màn giả toàn phần + lộ đáp án client 🔴

**Bằng chứng** — [`pages/listening/Listening.jsx:12`](../../apps/frontend/src/pages/listening/Listening.jsx#L12)
- Toàn bộ nội dung là hằng `MOCK_LISTENINGS` hardcode trong file, **không** import service API nào.
- Backend **không có** `ListeningController` (không có endpoint `/listening/*`).
- Mỗi câu hỏi nhúng `correctOption: 'B'` + `explanation` ngay trong bundle client.

**Vi phạm kép** — `CLAUDE.md § React Anti-Patterns`:
- *Client-trusted Data* / *Business Logic in Frontend*: chấm điểm bằng đáp án lộ ở client.
- *Dead Code*: không nối backend.

**Hành động** (chọn 1, cần quyết định — xem §3)
- **(a)** Xây `ListeningController` + service chấm điểm server-side (giống Reading), chuyển đáp án về DB.
- **(b)** Gỡ route `/listening` + mục nav cho tới khi có backend, tránh phát hành màn giả.

---

### F4 — `SpeakingPage.jsx`: gọi API không tồn tại 🟠

**Bằng chứng**
- [`api/studentService.js:343-360`](../../apps/frontend/src/api/studentService.js#L343) khai báo `getSpeakingExercises` → `GET /speaking/exercises`, `submitSpeakingAudio` → `POST /speaking/submit`, `getSpeakingResult` → `GET /speaking/{jobId}`.
- Backend **không có** controller Speaking/Speech nào (grep `Speaking|Speech|/speaking` chỉ trúng field entity, không có endpoint).

**Kết luận**: tính năng Speaking (AI shadowing) chưa có backend → mọi call sẽ 404. Đây là feature dở dang, không phải chỉ mock.

**Hành động** (cần quyết định — xem §3)
- **(a)** Defer công khai: gỡ route `/speaking` + 3 hàm service, đưa vào backlog AI.
- **(b)** Nếu ưu tiên: viết `SpeakingController` async (job-id + poll) theo `CLAUDE.md § OCR Flow`.

---

### F5 — `mockData.js`: export chết 🟡

**Bằng chứng** — grep `.jsx` cho các export sau: **0 nơi import**
`MOCK_LESSON_DETAIL`, `MOCK_KANJI_LIST`, `MOCK_KANJI_DETAIL_MAP`, `MOCK_KANJI_DETAIL_DEFAULT`, `MOCK_FLASHCARD_DECKS`, `MOCK_DECK_CARDS`, `MOCK_FLASHCARDS_DUE`, `MOCK_BACK_CONTENT_MAP`, `MOCK_EXAM_LIST`, `MOCK_ASSESSMENT_DETAIL`, `MOCK_QUIZ_RESULT`.

Chỉ 5 export còn dùng — và chỉ vì `DEMO_MODE=true` (F2) + `LearnNew` (F1).

**Hành động**: Sau khi xử lý F1 + F2, **toàn bộ** `mockData.js` (660+ dòng) trở thành mồ côi → xóa cả file. Nếu cần giữ cho demo, tách ra và thêm ghi chú "không đưa vào production build".

---

## 2. BACKEND

### B1 — `StaffQuizController` vs `StaffExamController`: 2 cây CRUD song song 🟠

**Bằng chứng** — cùng tập endpoint, cùng bảng `assessments`:

| Endpoint | `StaffQuizController` `/api/staff/assessments` | `StaffExamController` `/api/staff/exams` |
|----------|:---:|:---:|
| `POST` (tạo) | ✅ [L42](../../apps/backend/src/main/java/com/jlpt/feature/staffcontent/quiz/StaffQuizController.java#L42) | ✅ [L43](../../apps/backend/src/main/java/com/jlpt/feature/staffcontent/exam/StaffExamController.java#L43) |
| `GET` (list) | ✅ L54 | ✅ L55 |
| `GET /{id}` | ✅ L70 | ✅ L69 |
| `PUT /{id}` | ✅ L77 | ✅ L76 |
| `POST /{id}/assign-questions` | ✅ L86 | ✅ L85 |

Khác biệt thực chất chỉ là `type` (QUIZ vs EXAM) của cùng entity `Assessment`.

**Vi phạm**: `CLAUDE.md § God Class / DRY` — 2 controller + 2 service gần trùng, sửa 1 chỗ dễ quên chỗ kia (lộ trình lệch logic lock câu hỏi — `LESSON-005`).

**Hành động** (đã DEFER theo memory `project_quiz_exam_duplication`, ngày 2026-06-29 — **vì chưa có test phủ**)
1. **Trước tiên**: viết integration test phủ cả 2 luồng (tạo/sửa/assign-questions/lock-after-attempt).
2. Gộp về 1 controller `/api/staff/assessments` nhận `type` là tham số/enum, hoặc tách phần chung ra base service, giữ 2 endpoint mỏng nếu cần path khác nhau.
3. Không gộp khi test đỏ.

---

### B2 — 4 endpoint chết (không client nào gọi) ✅ ĐÃ XOÁ

Rà toàn bộ 37 controller vs mọi lời gọi từ FE (`api.get/post/put/delete`). 4 endpoint sau **không client nào gọi** → đã xoá cả chuỗi endpoint → service method → DTO → test. `mvn test-compile`: **BUILD SUCCESS**.

| Endpoint (đã xoá) | Controller | Service method đã xoá | DTO đã xoá | Lý do chết |
|-------------------|-----------|----------------------|-----------|-----------|
| `GET /api/test-attempts/{id}/status` | TestAttemptController | `MockExamService.getExamStatus` (+ 3 unit test + 1 integration test) | `ExamStatusResponse` | FE chỉ gọi `/review` + list |
| `GET /api/flashcards/{id}/reveal` | StudentFlashcardController | `FlashcardSrsService.revealCard` + `buildRevealResponse` | `FlashcardRevealResponse` | FE lật thẻ client-side (live-resolve) |
| `POST /api/kana/{kanaId}/complete` | StudentKanaController | `KanaService.markKanaComplete` (iface + impl) + field `learningProgressService` | — | FE dùng `/learning-progress` chung |
| `GET /api/kanji/progress-summary` | StudentKanjiController | `StudentKanjiService.getProgressSummary` (iface + impl) | `KanjiProgressSummaryResponse` | Không nơi nào trong FE tham chiếu |

**Đã verify KHÔNG thừa** (từng nghi ngờ, thực ra có gọi): `POST /auth/refresh` (interceptor 401), `POST /flashcards/session` (studentService).

---

### B3 — Logic chấm Speaking thủ công (UC-31): service chết ⏳ CHƯA XOÁ

- [`SupportTicketService`](../../apps/backend/src/main/java/com/jlpt/feature/support/service/SupportTicketService.java): `getAllSubmissions` / `getSubmissionDetail` / `manualGrade` + DTO `SubmissionResponse`/`ManualGradeRequest`/`GradeResponse` — **không controller nào expose** (đã verify: `StaffSupportController` chỉ có endpoint ticket).
- Đây là backend đối ứng của màn **StaffGrading** (FE 100% mock). Cả hai đầu đều chưa nối → toàn bộ luồng "staff chấm speaking" là code chết ở cả FE lẫn BE.
- **Chưa xoá**: gắn với tính năng Speaking đang được giữ (F4). Xoá hay giữ tuỳ định hướng: nếu Speaking sẽ có backend async thì UC-31 là mảnh ghép tương lai; nếu bỏ Speaking thì xoá cả cụm này + StaffGrading.

---

## 3. Câu hỏi cần quyết định trước khi code

| ID | Quyết định | Kết quả |
|----|-----------|---------|
| Q1 | Màn **Listening** (F3) | ✅ **Chốt (b)** — đã gỡ hẳn khỏi nav + xoá page |
| Q2 | Màn **Speaking** (F4) | ↩️ **Chốt GIỮ** — không gỡ; backend Speaking để làm sau (nếu làm) |
| Q3 | Thứ tự thực thi | Đã làm: F1, F3, Manager stubs, B2. Còn lại: F2/F5 (dọn mock), B1 (sau test), B3 (theo Speaking) |

---

## 4. Định nghĩa "Done"

- [x] F1: `LearnNew.*` bị xóa; `npm run build` xanh.
- [x] F2: `DEMO_MODE` gỡ bỏ khỏi `LessonDetail`+`Progress`; nhánh mock xoá, `markProgress`/`addToFlashcard` không còn bọc cờ. Còn lại: `/verify` với `student1@sakuji.com` để xác nhận số liệu thật.
- [x] F3: route `/listening` + page đã gỡ; không còn đáp án đúng trong bundle client.
- [x] F4: **GIỮ source, defer công khai** — điểm vào nav (Dashboard tile + StaffDashboard cards) đã gỡ; route giữ + comment DEFER; backend chưa có → còn 404 nếu vào route trực tiếp.
- [x] F5: `api/mockData.js` đã **xóa** (682 dòng, hết nơi import sau F2).
- [x] **Manager stubs**: `ManagerReports` + `ManagerStaffPerformance` xoá; nav + action card dọn; build+lint xanh.
- [x] **B2**: 4 endpoint chết + service + DTO + test đã xoá; `mvn test-compile` BUILD SUCCESS.
- [ ] B1: có integration test phủ cả 2 luồng **trước khi** gộp.
- [ ] B3 (UC-31): quyết theo định hướng Speaking (F4).
