# Backend Task Assignment — JLPT Platform

**Phiên bản:** 1.0 | **Ngày:** 05/06/2026  
**Đã loại bỏ Speaking feature — Zero file conflict giữa các nhóm**

---

## Hướng dẫn

- **Mới** = file cần tạo mới
- **Có sẵn** = file đã tồn tại
- **Chỉ đọc** = inject interface, không sửa file

Mỗi người chỉ tạo file mới, **KHÔNG** sửa file có sẵn hay file của người khác.  
Dependency giữa các nhóm chỉ theo hướng **READ** (inject `@Autowired` — không sửa file của nhau).

---

## Admin — Đã hoàn thành (không động vào)

Các file sau đã code xong, không cần sửa:

- `AdminController.java` — list/detail/suspend/activate/reset-password/delete user, CRUD staff, change role
- `AdminUserService.java` — logic quản lý user, staff
- `AdminAuthService.java` — login admin, `StaffPasswordResetService`
- `AuthController.java` + `AuthService.java` — login/register/logout/refresh/verify-email/forgot-password/reset
- `StaffAuthController.java` — auth cho staff
- `StudentController.java` — profile, update profile, change password
- `JwtProvider`, `JwtAuthenticationFilter`, `UserDetailsServiceImpl`, `SecurityConfig`
- Tất cả 22 Entity, Exception, ApiResponse, Converter, DevDataSeeder
- Toàn bộ DTO Request/Response cho Auth + Admin (23 request, 17 response)
- `AdminUserRepository`, `AuthTokenRepository`, `StaffUserRepository`, `StudentUserRepository`, `AdminAuditLogRepository`, `StaffPasswordResetRequestRepository`

> **Phần Admin còn thiếu** (System Settings, Notification Rules, Dashboard) được gom vào **Người 5**, tạo controller **MỚI**, không sửa `AdminController` cũ.

---

## Người 1 — Content Management + Content Review

**SPEC:** `feat-content-management`, `feat-content-review`  
**Entities liên quan (đã có sẵn):** Lesson, Kanji, Vocabulary, GrammarPoint, KanaCharacter, AdminUser  
**Phụ thuộc từ người khác:** Inject `AdminUserService` (có sẵn), `AdminAuditLogRepository` (có sẵn) — chỉ đọc

### Danh sách file cần tạo

| File Name | Loại | Trạng thái |
|---|---|---|
| `LessonRepository.java` | Repository | Mới |
| `KanjiRepository.java` | Repository | Mới |
| `VocabularyRepository.java` | Repository | Mới |
| `GrammarPointRepository.java` | Repository | Mới |
| `KanaCharacterRepository.java` | Repository | Mới |
| `ContentManagementService.java` | Service | Mới |
| `ContentReviewService.java` | Service | Mới |
| `StaffContentController.java` | Controller | Mới |
| `LessonRequest.java` | DTO Request | Mới |
| `LessonResponse.java` | DTO Response | Mới |
| `KanjiRequest.java` | DTO Request | Mới |
| `KanjiResponse.java` | DTO Response | Mới |
| `VocabularyRequest.java` | DTO Request | Mới |
| `VocabularyResponse.java` | DTO Response | Mới |
| `GrammarRequest.java` | DTO Request | Mới |
| `GrammarResponse.java` | DTO Response | Mới |
| `KanaCharacterResponse.java` | DTO Response | Mới |
| `ReviewActionRequest.java` | DTO Request | Mới |

### Luồng file trong module

```
📦 LessonRepository + KanjiRepository + VocabularyRepository + GrammarPointRepository + KanaCharacterRepository
    ↓
⚙️ ContentManagementService — CRUD: createLesson, updateKanji...
    ↓ inject các Repo mới + AdminUserService (có sẵn)
⚙️ ContentReviewService — submitForReview, approve, reject, publish
    ↓ inject các Repo mới + AdminAuditLogRepository (có sẵn)
🎮 StaffContentController — endpoints REST
```

### API endpoints

- `GET/POST/PUT/DELETE /api/staff/lessons`
- `GET/POST/PUT/DELETE /api/staff/kanji`
- `GET/POST/PUT/DELETE /api/staff/vocabulary`
- `GET/POST/PUT/DELETE /api/staff/grammar`
- `GET/POST/PUT/DELETE /api/staff/kana`
- `POST /api/staff/content/{type}/{id}/submit-review / approve / reject / publish`

---

## Người 2 — Assessment & Mock Tests

**SPEC:** `feat-assessment`, `feat-mock-test`  
**Entities liên quan (đã có sẵn):** Assessment, Question, QuestionAssignment, TestAttempt, AttemptAnswer  
**Phụ thuộc từ người khác:** Không — hoàn toàn độc lập

### Danh sách file cần tạo

| File Name | Loại | Trạng thái |
|---|---|---|
| `QuestionRepository.java` | Repository | Mới |
| `QuestionAssignmentRepository.java` | Repository | Mới |
| `AssessmentRepository.java` | Repository | Mới |
| `TestAttemptRepository.java` | Repository | Mới |
| `AttemptAnswerRepository.java` | Repository | Mới |
| `QuizService.java` | Service | Mới |
| `MockTestService.java` | Service | Mới |
| `StaffAssessmentController.java` | Controller | Mới |
| `StudentAssessmentController.java` | Controller | Mới |
| `QuizRequest.java` | DTO Request | Mới |
| `QuizResponse.java` | DTO Response | Mới |
| `QuestionRequest.java` | DTO Request | Mới |
| `QuestionResponse.java` | DTO Response | Mới |
| `AnswerSubmissionRequest.java` | DTO Request | Mới |
| `AttemptResponse.java` | DTO Response | Mới |
| `ScoreResponse.java` | DTO Response | Mới |
| `MockTestRequest.java` | DTO Request | Mới |
| `MockTestResponse.java` | DTO Response | Mới |

### Luồng file trong module

```
📦 QuestionRepository + QuestionAssignmentRepository + AssessmentRepository + TestAttemptRepository + AttemptAnswerRepository
    ↓
⚙️ QuizService — createQuiz, addQuestions, submitQuiz, calculateScore
    ↓ inject 5 Repo mới
⚙️ MockTestService — generateMockTest, gradeMockTest, mapScoreToJLPT
    ↓ inject AssessmentRepo, TestAttemptRepo
🎮 StaffAssessmentController — quản lý đề thi
🎮 StudentAssessmentController — làm bài, nộp bài
```

### API endpoints

- `GET/POST/PUT/DELETE /api/staff/assessments`
- `GET/POST /api/staff/assessments/{id}/questions`
- `GET /api/assessments` (student)
- `POST /api/assessments/{id}/submit`
- `GET /api/assessments/{id}/result`
- `GET/POST /api/mock-tests`
- `POST /api/mock-tests/{id}/submit`

---

## Người 3 — Core Learning + Reading/Listening + Handwriting OCR

**SPEC:** `feat-core-learning`, `feat-reading-listening`, `feat-ai-skills` (UC-20 OCR only)  
**Entities liên quan (đã có sẵn):** StudentContentProgress, StudentSubmission, Lesson, Kanji, KanaCharacter, StudentUser  
**Phụ thuộc từ người khác:** `LessonRepository` (Người 1), `KanjiRepository` (Người 1), `KanaCharacterRepository` (Người 1) — chỉ inject đọc

### Danh sách file cần tạo

| File Name | Loại | Trạng thái |
|---|---|---|
| `StudentContentProgressRepository.java` | Repository | Mới |
| `StudentSubmissionRepository.java` | Repository | Mới |
| `LessonProgressService.java` | Service | Mới |
| `ReadingListeningService.java` | Service | Mới |
| `OcrService.java` | Service | Mới |
| `StudentLearningController.java` | Controller | Mới |
| `AiHandwritingController.java` | Controller | Mới |
| `ProgressRequest.java` | DTO Request | Mới |
| `ProgressResponse.java` | DTO Response | Mới |
| `ReadingExerciseResponse.java` | DTO Response | Mới |
| `ListeningExerciseResponse.java` | DTO Response | Mới |
| `HandwritingSubmissionRequest.java` | DTO Request | Mới |
| `OcrResultResponse.java` | DTO Response | Mới |

### Luồng file trong module

```
📦 StudentContentProgressRepository + StudentSubmissionRepository
    ↓
⚙️ LessonProgressService — startLesson, completeLesson, trackProgress, unlockNext
    ↓ inject StudentContentProgressRepo + LessonRepo (Người 1)
⚙️ ReadingListeningService — getExercise, submitAnswer, checkComprehension
    ↓ inject LessonRepo (Người 1) + StudentContentProgressRepo
⚙️ OcrService — submitHandwriting (async), pollResult, processOcrResult, retry 3 lần
    ↓ inject StudentSubmissionRepo + KanjiRepo (Người 1) + KanaCharacterRepo (Người 1)
🎮 StudentLearningController — endpoints học bài, reading, listening
🎮 AiHandwritingController — endpoints OCR
```

### API endpoints

- `GET /api/lessons/{id}/study`
- `POST /api/lessons/{id}/complete`
- `GET/POST /api/progress`
- `GET /api/reading/{lessonId}`
- `POST /api/reading/{lessonId}/submit`
- `GET /api/listening/{lessonId}`
- `POST /api/listening/{lessonId}/submit`
- `POST /api/ai/handwriting`
- `GET /api/ai/submissions/{id}`

---

## Người 4 — Flashcard SRS + Dictionary + Bookmark

**SPEC:** `feat-flashcard-srs`, `feat-dictionary-bookmark`  
**Entities liên quan (đã có sẵn):** Flashcard, StudentContentProgress, Kanji, Vocabulary, GrammarPoint, Lesson, KanaCharacter, StudentUser  
**Phụ thuộc từ người khác:** `VocabularyRepo` (Người 1), `KanjiRepo` (Người 1), `GrammarPointRepo` (Người 1), `LessonRepo` (Người 1), `StudentContentProgressRepo` (Người 3) — chỉ inject đọc

### Danh sách file cần tạo

| File Name | Loại | Trạng thái |
|---|---|---|
| `FlashcardRepository.java` | Repository | Mới |
| `FlashcardSrsService.java` | Service | Mới |
| `DictionaryService.java` | Service | Mới |
| `BookmarkService.java` | Service | Mới |
| `StudentFlashcardController.java` | Controller | Mới |
| `StudentDictionaryController.java` | Controller | Mới |
| `StudentBookmarkController.java` | Controller | Mới |
| `FlashcardResponse.java` | DTO Response | Mới |
| `FlashcardRevealResponse.java` | DTO Response | Mới |
| `ReviewRequest.java` | DTO Request | Mới |
| `ReviewResultResponse.java` | DTO Response | Mới |
| `SearchResponse.java` | DTO Response | Mới |
| `BookmarkRequest.java` | DTO Request | Mới |
| `BookmarkResponse.java` | DTO Response | Mới |

### Luồng file trong module

```
📦 FlashcardRepository
    ↓
⚙️ FlashcardSrsService — getDecks, getCards, revealCard, submitReview (SM-2 algorithm)
    ↓ inject FlashcardRepo
⚙️ DictionaryService — search(q) → vocab + kanji + grammar + lesson
    ↓ inject các Repo Người 1
⚙️ BookmarkService — addBookmark, removeBookmark, listBookmarks
    ↓ inject StudentContentProgressRepo (Người 3) + các Repo Người 1
🎮 StudentFlashcardController — endpoints flashcard
🎮 StudentDictionaryController — endpoints tra cứu
🎮 StudentBookmarkController — endpoints bookmark
```

### API endpoints

- `GET /api/flashcard-decks`
- `GET /api/flashcards?deckName=&dueOnly=true`
- `GET /api/flashcards/{id}/reveal`
- `POST /api/flashcards/{id}/review`
- `POST /api/flashcards` (add card)
- `GET /api/dictionary/search?q=...`
- `GET /api/bookmarks?type=...`
- `POST/DELETE /api/bookmarks`

---

## Người 5 — Student Management + Support/Ticket + Analytics + Admin System (còn thiếu)

**SPEC:** `feat-student-management`, `feat-support`, `feat-learning-analytics`, `feat-system-admin` (settings, dashboard)  
**Entities liên quan (đã có sẵn):** Ticket, TicketReply, Notification, SystemSetting, StudentUser, StaffUser, StudentContentProgress, StudentSubmission, AdminAuditLog, AdminUser  
**Phụ thuộc từ người khác:** `StudentContentProgressRepo` (Người 3), `StudentSubmissionRepo` (Người 3) — chỉ inject đọc

### Danh sách file cần tạo

| File Name | Loại | Trạng thái |
|---|---|---|
| `TicketRepository.java` | Repository | Mới |
| `TicketReplyRepository.java` | Repository | Mới |
| `NotificationRepository.java` | Repository | Mới |
| `SystemSettingRepository.java` | Repository | Mới |
| `StudentManagementService.java` | Service | Mới |
| `SupportTicketService.java` | Service | Mới |
| `AnalyticsService.java` | Service | Mới |
| `SystemSettingService.java` | Service | Mới |
| `NotificationRuleService.java` | Service | Mới |
| `StaffStudentController.java` | Controller | Mới |
| `SupportController.java` | Controller | Mới |
| `StudentAnalyticsController.java` | Controller | Mới |
| `AdminSystemController.java` | Controller (mới — không sửa file cũ) | Mới |
| `AdminDashboardController.java` | Controller (mới — không sửa file cũ) | Mới |
| `StudentDetailResponse.java` | DTO Response | Mới |
| `TicketRequest.java` | DTO Request | Mới |
| `TicketResponse.java` | DTO Response | Mới |
| `ManualGradeRequest.java` | DTO Request | Mới |
| `GradeResponse.java` | DTO Response | Mới |
| `AnalyticsResponse.java` | DTO Response | Mới |
| `DashboardResponse.java` | DTO Response | Mới |
| `SystemSettingRequest.java` | DTO Request | Mới |
| `SystemSettingResponse.java` | DTO Response | Mới |
| `NotificationRuleRequest.java` | DTO Request | Mới |
| `NotificationRuleResponse.java` | DTO Response | Mới |

### Luồng file trong module

```
📦 TicketRepository + TicketReplyRepository + NotificationRepository + SystemSettingRepository
    ↓
⚙️ StudentManagementService — getStudentDetail, getProgressHistory, updateStudentInfo
    ↓ inject StudentUserRepo (có sẵn) + StudentContentProgressRepo (Người 3)
⚙️ SupportTicketService — createTicket, replyTicket, closeTicket, manualGradeSubmission
    ↓ inject TicketRepo, TicketReplyRepo, StudentSubmissionRepo (Người 3)
⚙️ AnalyticsService — studyStreak, timeSpent, completionRate, dashboard
    ↓ inject StudentContentProgressRepo (Người 3) + StudentSubmissionRepo (Người 3)
⚙️ SystemSettingService — getSetting, updateSetting, maintenance mode
⚙️ NotificationRuleService — createRule, listRules, triggerMilestone
🎮 StaffStudentController — quản lý học viên
🎮 SupportController — ticket, chấm bài
🎮 StudentAnalyticsController — dashboard học viên
🎮 AdminSystemController — settings + notification rules
🎮 AdminDashboardController — thống kê admin
```

### API endpoints

- `GET/PUT /api/staff/students`
- `GET /api/staff/students/{id}/progress`
- `GET/POST /api/support/tickets`
- `POST /api/support/tickets/{id}/reply|close`
- `POST /api/submissions/{id}/grade`
- `GET /api/analytics/dashboard|progress`
- `GET/PUT /api/admin/settings/{group}/{key}`
- `GET/POST /api/admin/notification-rules`
- `PUT/DELETE /api/admin/notification-rules/{id}`
- `GET /api/admin/dashboard`

---

## Sơ đồ dependency toàn bộ dự án

Các mũi tên chỉ theo hướng **READ** (inject interface — không sửa file của nhau).

```
Người 1 (Content Repos)  ←──  Người 4 (Dictionary, Flashcard inject để tra cứu)
                          ←──  Người 3 (OCR tra Kanji/Kana)
Người 3 (Progress Repo)  ←──  Người 5 (Analytics đọc thống kê)
                          ←──  Người 4 (BookmarkService đọc progress)
```

**Tổng kết:** ~90 file mới, chia 5 người. **Zero conflict.**
