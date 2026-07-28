# Assessment Mock Test Feature Analysis

## 1. Tóm tắt tổng quan

Luồng Mock Test cho Student xem đề Exam đã published, bắt đầu attempt, làm bài theo section, nộp bài và xem review. Frontend gồm ba màn hình List–Attempt–Results; backend dùng `StudentAssessmentController` và `MockExamService`, lưu attempt/answer trong database và tính điểm phía server.

## 2. Bản đồ cấu trúc

| File | Vai trò | Loại |
|---|---|---|
| [MockTestList.jsx](apps/frontend/src/features/mock-test/mock-test/MockTestList.jsx) | Liệt kê Exam theo JLPT level | React Page |
| [MockTestAttempt.jsx](apps/frontend/src/features/mock-test/mock-test/MockTestAttempt.jsx) | Khởi tạo attempt, timer và câu trả lời | React Page |
| [MockTestResults.jsx](apps/frontend/src/features/mock-test/mock-test/MockTestResults.jsx) | Hiển thị kết quả/review | React Page |
| [studentService.js](apps/frontend/src/shared/api/studentService.js) | Gọi list/start/submit/review API | API Service |
| [StudentAssessmentController.java](apps/backend/src/main/java/com/jlpt/feature/assessment/StudentAssessmentController.java) | API list/start/submit | Controller |
| [MockExamService.java](apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java) | Kiểm tra thời gian, chấm điểm, lưu attempt | Service |
| [Assessment.java](apps/backend/src/main/java/com/jlpt/feature/assessment/Assessment.java) | Đề thi | Entity |
| [TestAttempt.java](apps/backend/src/main/java/com/jlpt/feature/assessment/TestAttempt.java) | Một lần làm bài | Entity |
| [AttemptAnswer.java](apps/backend/src/main/java/com/jlpt/feature/assessment/AttemptAnswer.java) | Câu trả lời trong attempt | Entity |

## 3. Bản đồ kết nối

```mermaid
flowchart LR
    List["MockTestList.jsx"] -->|"GET assessments"| API["studentService.js"]
    Attempt["MockTestAttempt.jsx"] -->|"start / submit"| API
    Result["MockTestResults.jsx"] -->|"GET review"| API
    API --> Ctrl["StudentAssessmentController.java"]
    Ctrl --> Svc["MockExamService.java"]
    Svc --> Assess[("assessments / question_assignments")]
    Svc --> Attempts[("test_attempts / attempt_answers")]
```

| Từ | Đến | Cách kết nối | Dữ liệu |
|---|---|---|---|
| List | API | `getExamList` | level/page |
| Attempt | API | `startAssessment` | assessmentId |
| Attempt | API | `submitAssessment` | attemptId/answers |
| Controller | Service | method call | studentId + DTO |
| Service | DB | JPA | exam/questions/attempt |

## 4. Luồng xử lý theo trình tự

1. [MockTestList.jsx#L24](apps/frontend/src/features/mock-test/mock-test/MockTestList.jsx#L24) tải Exam published theo level.
2. Student chọn đề và đi tới `/mock-test/{id}/attempt`.
3. [MockTestAttempt.jsx#L35](apps/frontend/src/features/mock-test/mock-test/MockTestAttempt.jsx#L35) gọi start; backend tạo/khôi phục attempt và trả sections.
4. UI lưu lựa chọn tạm theo `exam_draft_{id}`; đây chỉ là phục hồi UX.
5. `doSubmit` tạo mảng `{questionId, selectedOption}` và gửi backend.
6. `MockExamService.submitExam` kiểm tra attempt/student/thời gian, tính điểm server-side và lưu answers.
7. UI chuyển tới results kèm `attemptId`; Results gọi review API để hiển thị đúng/sai.

```mermaid
sequenceDiagram
    actor Student
    participant FE as "MockTest List / Attempt / Results"
    participant API as "studentService.js"
    participant Ctrl as "StudentAssessmentController.java"
    participant Svc as "MockExamService.java"
    participant DB as "Assessment / Attempt / Answers"
    Student->>FE: Chọn đề
    FE->>API: startAssessment(id)
    API->>Ctrl: POST /assessments/{id}/start
    Ctrl->>Svc: startExam(id, studentId)
    Svc->>DB: Tạo attempt
    DB-->>FE: attemptId + sections
    Student->>FE: Chọn đáp án và nộp
    FE->>Svc: submit attempt + answers
    Svc->>DB: Chấm và lưu
    FE->>Svc: get review(attemptId)
    Svc-->>Student: Kết quả chi tiết
```

## 5. Vai trò đoạn code quan trọng

[MockTestAttempt.jsx#L81](apps/frontend/src/features/mock-test/mock-test/MockTestAttempt.jsx#L81)

```jsx
const payload = questions.map((q) => ({
  // Frontend chỉ gửi lựa chọn; không gửi điểm để tránh client tự chấm.
  questionId: q.questionId,
  selectedOption: answers[q.questionId] ?? null,
}));
const result = await submitAssessment(id, { attemptId, isAutoSubmit, answers: payload });
```

[studentService.js#L250](apps/frontend/src/shared/api/studentService.js#L250)

```js
export async function startAssessment(assessmentId) {
  // Endpoint tạo trạng thái làm bài phía server trước khi timer bắt đầu ở UI.
  const res = await api.post(`/assessments/${assessmentId}/start`);
  return res.data.data;
}
```

## 6. Dữ liệu di chuyển

Filter level → danh sách Exam → assessmentId → attemptId/sections → answer map ở UI → answer DTO → điểm và review do server tạo → Results UI.

## 7. Bảng tra cứu tổng hợp

| Bước | File | Function | Kết nối tới | Dữ liệu | Ghi chú |
|---:|---|---|---|---|---|
| 1 | `MockTestList` | `fetchExams` | list API | level/page | Chỉ Exam |
| 2 | `MockTestAttempt` | start effect | start API | assessmentId | Tạo attempt |
| 3 | `MockTestAttempt` | `handleSelect` | local state | answer | Draft UX |
| 4 | `MockTestAttempt` | `doSubmit` | submit API | answers | Server chấm |
| 5 | `MockTestResults` | effect | review API | attemptId | Chi tiết kết quả |

## 8. Các mục cần bổ sung context

- LocalStorage draft không phải nguồn sự thật; attempt server mới quyết định kết quả.
- Feature có trên 15 file nếu tính toàn bộ DTO/converter/repository; tài liệu chỉ liệt kê file lõi theo giới hạn workflow.

<!-- BACKEND-METHOD-INVENTORY:START -->

## Phụ lục — Danh mục đầy đủ hàm backend

> Phần này được đối chiếu trực tiếp từ source backend hiện tại. Chỉ liệt kê các hàm khai báo tường minh trong những file Java mà tài liệu này tham chiếu; các hàm do Lombok/JPA sinh tự động không xuất hiện trong source nên không liệt kê.

### `Assessment`

Nguồn: [Assessment.java](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/Assessment.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`void onUpdate()`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/Assessment.java#L84) | `—` | Thực hiện xử lý backend `on update` trong `Assessment`. |
| 2 | [`String getValue()`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/Assessment.java#L98) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get value`. |

### `MockExamService`

Nguồn: [MockExamService.java](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`String canonicalSection(String raw)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L56) | `—` | Kiểm tra điều kiện/trạng thái phục vụ `canonical section`. |
| 2 | [`ExamStartResponse startExam(Long assessmentId, StudentUser student)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L77) | `—` | Thực hiện xử lý backend `start exam` trong `MockExamService`. |
| 3 | [`ExamSubmitResponse submitExam(Long assessmentId, Long studentId, SubmitExamRequest request)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L117) | `—` | Tạo hoặc ghi dữ liệu cho nghiệp vụ `submit exam`. |
| 4 | [`Page<ExamHistoryResponse> getExamHistory(Long studentId, Pageable pageable)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L154) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get exam history`. |
| 5 | [`ExamReviewResponse getExamReview(Long attemptId, Long studentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L173) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get exam review`. |
| 6 | [`ExamSubmitResponse gradeAndPersist(TestAttempt attempt, Assessment assessment, List<QuestionAssignment> assignments, List<AnswerRequest> answers, boolean isAutoSubmit, LocalDateTime now)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L193) | `—` | Thực hiện xử lý backend `grade and persist` trong `MockExamService`. |
| 7 | [`TestAttempt findOwnedAttempt(Long attemptId, Long studentId)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L305) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `find owned attempt`. |
| 8 | [`LocalDateTime computeExpiresAt(Assessment assessment, LocalDateTime startedAt)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L315) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `compute expires at`. |
| 9 | [`SectionScoresResponse toSectionScores(TestAttempt attempt)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L319) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to section scores`. |
| 10 | [`ExamHistoryResponse toHistoryResponse(TestAttempt attempt, Assessment assessment)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L327) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to history response`. |
| 11 | [`ExamReviewItem toReviewItem(AttemptAnswer answer)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/MockExamService.java#L345) | `—` | Biến đổi/tổng hợp dữ liệu nội bộ cho `to review item`. |

### `StudentAssessmentController`

Nguồn: [StudentAssessmentController.java](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/StudentAssessmentController.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`ResponseEntity<ApiResponse<Object>> startAssessment(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/StudentAssessmentController.java#L67) | `POST /{id}/start` | Xử lý endpoint `POST /{id}/start`; thực hiện nghiệp vụ `start assessment`. |
| 2 | [`ResponseEntity<ApiResponse<Object>> submitAssessment(@PathVariable Long id, @Valid @RequestBody SubmitExamRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails)`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/StudentAssessmentController.java#L84) | `POST /{id}/submit` | Xử lý endpoint `POST /{id}/submit`; thực hiện nghiệp vụ `submit assessment`. |

### `TestAttempt`

Nguồn: [TestAttempt.java](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/TestAttempt.java)

| # | Hàm backend (đầy đủ chữ ký) | Endpoint | Tác dụng/chức năng phục vụ |
|---:|---|---|---|
| 1 | [`String getValue()`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/TestAttempt.java#L84) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get value`. |
| 2 | [`String getValue()`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/TestAttempt.java#L99) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get value`. |
| 3 | [`String getValue()`](../../../apps/backend/src/main/java/com/jlpt/feature/assessment/TestAttempt.java#L115) | `—` | Đọc hoặc tra cứu dữ liệu phục vụ `get value`. |

**Tổng cộng:** `18` hàm backend trong `5` file Java được tham chiếu.

<!-- BACKEND-METHOD-INVENTORY:END -->
