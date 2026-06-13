# TASKS — Student Management (`feat-student-management`)
> **UC Coverage:** UC-21, UC-22, UC-23 | **Actor:** Staff, Admin
> **Branch:** `feature/JLPT-XX-student-management` | **Scope commit:** `student`

---

## Phase 0: Git Setup
- [ ] 0.1 Sync code mới nhất từ `main`:
  ```bash
  git fetch origin
  git rebase origin/main
  ```
- [ ] 0.2 Tạo branch đúng format GIT_RULES Section 6:
  ```bash
  git checkout -b feature/JLPT-XX-student-management
  ```
  > Thay `XX` bằng ticket ID thực tế trên board.
- [ ] 0.3 Xác nhận branch đang đúng: `git status` — working tree phải clean trước khi code.

---

## Phase 1: DTOs

- [ ] 1.1 Tạo `dto/request/SuspendUserRequest.java`
  - `@NotBlank @Size(min=10, max=500) String reason`
- [ ] 1.2 Mở rộng `dto/response/StudentDetailResponse.java` *(file đã có — chỉ thêm fields)*
  - Thêm: `String phone`, `String avatarUrl`, `String targetJlptLevel`, `LocalDate lastActivityDate`
  - Giữ nguyên toàn bộ fields hiện tại; dùng `@JsonInclude(NON_NULL)` để tránh breaking change
- [ ] 1.3 Tạo `dto/response/StudentProgressResponse.java` *(mới)*
  - `studentId`, `fullName`, `currentStreak`, `longestStreak`, `lastActivityDate`
  - Completions: `lessonsCompleted`, `kanjiCompleted`, `vocabularyCompleted`, `grammarCompleted`, `kanaCompleted`
  - Exam stats: `totalExamsTaken`, `averageExamScore`, `highestExamScore`
- [ ] 1.4 Tạo `dto/response/SubmissionSummaryResponse.java` *(mới — dùng chung với feat-support)*
  - `submissionId`, `submissionType`, `status`, `aiOverallScore`, `manualScore`, `finalScore`, `gradedByStaffName`, `submittedAt`
- [ ] 1.5 Tạo `dto/response/SuspendUserResponse.java` và `ActivateUserResponse.java`

**Commit sau phase này:**
```
feat(student): add DTOs for student management (suspend, progress, submission summary)
```

---

## Phase 2: Service Layer

- [ ] 2.1 Tạo `service/StudentManagementService.java`
  - Annotations: `@Service`, `@RequiredArgsConstructor`, `@Slf4j`
- [ ] 2.2 Implement `getStudentList(String q, String status, String jlptLevel, int page, int size)`
  - Gọi `studentUserRepository.findAllAdminFiltered()` với Specification hoặc custom JPQL
  - Map sang `StudentDetailResponse` — **tuyệt đối không expose `passwordHash`**
- [ ] 2.3 Implement `getStudentDetail(Long studentId)`
  - Throw `ResourceNotFoundException(404)` nếu không tìm thấy
  - Map sang DTO đầy đủ, verify DTO mapper không map `passwordHash`, `oauthProviderId`
- [ ] 2.4 Implement `getStudentProgressSummary(Long studentId)`
  - Inject `StudentContentProgressRepository` với `@Autowired(required = false)` (phụ thuộc Người 3)
  - Nếu repo null → trả về list rỗng (không crash)
  - Group by `contentType`, tính `completedCount` per type
  - Query `testAttemptRepository` (required = false) cho exam stats
- [ ] 2.5 Implement `getStudentSubmissions(Long studentId, String type, String status, int page, int size)`
  - Inject `StudentSubmissionRepository` với `@Autowired(required = false)`
  - Trả summary — **không expose AI error detail thô**
- [ ] 2.6 Implement `suspendStudent(String actorEmail, Long studentId, String reason)` — `@Transactional`
  - Validate `reason` [10–500 chars] → `400` nếu sai
  - Load student → `404` nếu không có
  - Check `status != 'suspended'` → `409` nếu đã suspended
  - Trong một transaction: set `SUSPENDED`, set `suspendReason`, revoke **toàn bộ** `auth_tokens` (bắt buộc sync, không async)
  - Ghi `admin_audit_logs` với action = `STUDENT_SUSPENDED`, `staff_actor_id`
  - Log SLF4J: `[INFO] Staff {} suspended student {} reason: {}`
- [ ] 2.7 Implement `activateStudent(String actorEmail, Long studentId)` — `@Transactional`
  - Check `status != 'active'` → `409` nếu đã active
  - Set `ACTIVE`, clear `suspendReason`
  - Ghi audit log action = `STUDENT_ACTIVATED`

**Commit sau phase này:**
```
feat(student): implement StudentManagementService with suspend/activate and progress query
```

---

## Phase 3: Controller Layer

- [ ] 3.1 Tạo `controller/admin/StaffStudentController.java`
  - Annotations: `@RestController`, `@RequestMapping("/api/staff/students")`, `@RequiredArgsConstructor`
  - Security class-level: `@PreAuthorize("hasAnyRole('STAFF','ADMIN')")`
- [ ] 3.2 Implement `GET /api/staff/students` — phân trang, filter `q`, `status`, `jlptLevel`
- [ ] 3.3 Implement `GET /api/staff/students/{studentId}` — profile đầy đủ
- [ ] 3.4 Implement `GET /api/staff/students/{studentId}/progress`
- [ ] 3.5 Implement `GET /api/staff/students/{studentId}/submissions` — filter `type`, `status`
- [ ] 3.6 Implement `POST /api/staff/students/{studentId}/suspend`
  - Lấy actor từ `Authentication auth` (JWT) — **không nhận staffId từ request body**
  - `@Valid @RequestBody SuspendUserRequest`
- [ ] 3.7 Implement `POST /api/staff/students/{studentId}/activate`
- [ ] 3.8 Kiểm tra response format thống nhất `ApiResponse<T>` cho tất cả endpoints

**Commit sau phase này:**
```
feat(student): add StaffStudentController with CRUD, suspend, and activate endpoints
```

---

## Phase 4: Testing

- [ ] 4.1 Unit test `StudentManagementServiceTest`:
  - `getStudentDetail_doesNotExposePasswordHash`
  - `suspendStudent_withValidReason_setsStatusAndRevokesTokens`
  - `suspendStudent_withAlreadySuspended_throws409Conflict`
  - `suspendStudent_withShortReason_throwsValidationException` (< 10 chars)
  - `activateStudent_withAlreadyActiveAccount_throws409Conflict`
  - `suspendStudent_writesAuditLog`
  - `getStudentProgressSummary_withNullProgressRepo_returnsEmptyList`
- [ ] 4.2 Integration test `StaffStudentControllerTest`:
  - `GET /staff/students/{id}` → response không chứa field `passwordHash`
  - `POST /suspend` với reason 5 chars → 400
  - `POST /suspend` tài khoản đã suspended → 409
  - `Student JWT gọi /staff/students` → 403
  - `Unauthenticated gọi /staff/students` → 401

**Commit sau phase này:**
```
test(student): add unit and integration tests for StudentManagementService and controller
```

---

## Phase 5: PR & Code Review

- [ ] 5.1 Tự review diff trước khi tạo PR:
  ```bash
  git diff origin/main...HEAD
  ```
  Kiểm tra checklist:
  - [ ] Không có file `.env`, `.vscode/`, `.idea/` bị commit nhầm
  - [ ] Không return Entity trực tiếp — chỉ DTO ra API
  - [ ] Soft delete đúng cách (`status = 'suspended'`/`'deleted'`) — không có `DELETE FROM`
  - [ ] `passwordHash` không xuất hiện trong bất kỳ response nào
  - [ ] Revoke token nằm trong `@Transactional` (không async)
- [ ] 5.2 Sync code mới nhất trước khi tạo PR:
  ```bash
  git fetch origin
  git rebase origin/main
  ```
- [ ] 5.3 Tạo PR trên GitHub:
  - **Title:** `feat(student): student management — view progress, suspend/activate account`
  - **Description:** Mô tả WHAT + WHY, link ticket
  - Yêu cầu ít nhất **1 reviewer** approve
- [ ] 5.4 Merge bằng **Squash and Merge** để giữ lịch sử `main` sạch
- [ ] 5.5 Xóa branch sau khi merge thành công
