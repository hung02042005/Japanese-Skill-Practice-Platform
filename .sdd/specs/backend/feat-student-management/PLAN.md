# PLAN — Student Management (`feat-student-management`)
> **Feature ID:** `feat-student-management` | **UC:** UC-21, UC-22, UC-23 | **Version:** 1.0 | **Updated:** 2026-06-14

---

## 1. User Intent

### 1.1 Vấn đề cần giải quyết

Staff cần bộ công cụ để:

1. **Giám sát học viên** — xem danh sách, tìm kiếm, lọc theo trạng thái và JLPT level mà không cần truy cập DB trực tiếp.
2. **Phân tích tiến trình** — xem chi tiết từng học viên: streak, số bài hoàn thành, điểm thi, lịch sử bài nộp AI.
3. **Kiểm soát vi phạm** — tạm khóa kèm lý do bắt buộc, tự động đăng xuất khỏi mọi thiết bị, mở khóa khi cần.

### 1.2 Người dùng và kỳ vọng

| Actor | Kỳ vọng chính |
|:---|:---|
| **Staff** | Tìm học viên nhanh dưới 500ms, xem tiến độ đầy đủ, khóa/mở tài khoản và biết action đã được ghi nhận |
| **Staff Manager** | Tất cả quyền Staff, có thể xem audit log hành vi team |
| **Admin** | Tất cả quyền Staff, không bị giới hạn bởi bất kỳ filter nào |

### 1.3 Ràng buộc nghiệp vụ quan trọng

- Staff **không được** xem hoặc sửa `password_hash`, email, OAuth credentials của student.
- Khi khóa tài khoản: revoke **toàn bộ** `auth_tokens` trong cùng `@Transactional` — tuyệt đối không async.
- Mọi thao tác suspend/activate phải ghi vào `admin_audit_logs` với `staff_actor_id`.
- `suspend_reason` bắt buộc, độ dài từ 10 đến 500 ký tự.
- Không cho phép khóa tài khoản đã bị khóa hoặc mở tài khoản đã mở → trả HTTP 409.

---

## 2. Architectural Blueprint

### 2.1 Layer Stack

```
[React Frontend]
      |  GET/POST  HTTP + Bearer JWT
      v
[StaffStudentController]              <- com.jlpt.controller.admin  [MOI]
      |  @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
      |  @Valid DTO input
      v
[StudentManagementService]            <- com.jlpt.service  [MOI]
      |  Business logic + @Transactional (write ops)
      |  Ownership check + audit log
      v
+----------------------------------------------------------+
|  StudentUserRepository     (read/write)                  |  <- da co
|  AuthTokenRepository       (revoke write)                |  <- da co
|  AdminAuditLogRepository   (write)                       |  <- da co
|  StudentContentProgressRepo (READ-ONLY, Nguoi 3)         |  <- inject de doc
|  StudentSubmissionRepository (READ-ONLY, Nguoi 3)        |  <- inject de doc
+----------------------------------------------------------+
```

### 2.2 File Structure

```
controller/admin/
  StaffStudentController.java          [MOI]

service/
  StudentManagementService.java        [MOI]

dto/response/
  StudentDetailResponse.java           [MO RONG - them fields]
  StudentProgressResponse.java         [MOI]
  SubmissionSummaryResponse.java       [MOI]
```

### 2.3 API Routes

| Method | Path | Actor | Mo ta |
|:---|:---|:---|:---|
| GET | `/api/staff/students` | Staff/Admin | Danh sach hoc vien co filter + phan trang |
| GET | `/api/staff/students/{id}` | Staff/Admin | Chi tiet 1 hoc vien |
| GET | `/api/staff/students/{id}/progress` | Staff/Admin | Tien trinh hoc tap chi tiet |
| GET | `/api/staff/students/{id}/submissions` | Staff/Admin | Lich su bai nop AI |
| POST | `/api/staff/students/{id}/suspend` | Staff/Admin | Khoa tai khoan |
| POST | `/api/staff/students/{id}/activate` | Staff/Admin | Mo khoa tai khoan |

### 2.4 Data Flow — Suspend Account

```
POST /api/staff/students/{id}/suspend
  |
  +- [Controller] @Valid SuspendRequest (reason >= 10 chars)
  |
  +- [Service] StudentManagementService.suspendStudent()
  |     +-- Load StudentUser -> 404 neu khong co
  |     +-- Check status != 'suspended' -> 409 neu da suspended
  |     +-- @Transactional:
  |     |     +-- studentUser.setStatus(SUSPENDED)
  |     |     +-- studentUser.setSuspendReason(reason)
  |     |     +-- authTokenRepository.revokeAllByStudentId(studentId, now())
  |     |     +-- adminAuditLogRepository.save(STUDENT_SUSPENDED log)
  |     +-- Return SuspendUserResponse
  |
  +- [Controller] ResponseEntity<ApiResponse<SuspendUserResponse>> 200 OK
```

### 2.5 Key Implementation Details

**Dependency injection tu Nguoi 3 — su dung `required = false` de tranh compile error:**

```java
@Autowired(required = false)
private StudentContentProgressRepository progressRepository;

private List<?> getProgress(Long studentId) {
    if (progressRepository == null) return List.of(); // fallback
    return progressRepository.findByStudent_Id(studentId);
}
```

**Lay actor tu JWT trong Controller:**

```java
@PostMapping("/{studentId}/suspend")
public ResponseEntity<?> suspendStudent(
        Authentication auth,
        @PathVariable Long studentId,
        @Valid @RequestBody SuspendUserRequest request) {
    return ResponseEntity.ok(ApiResponse.success(
        studentManagementService.suspendStudent(
            auth.getName(), studentId, request.getReason())
    ));
}
```

**Mo rong `StudentDetailResponse` — them cac fields sau vao class hien co:**

```java
private String phone;
private String avatarUrl;
private String targetJlptLevel;
private LocalDate lastActivityDate;
// Giu nguyen tat ca fields da co: studentId, fullName, email,
// status, suspendReason, currentJlptLevel, currentStreak,
// longestStreak, lastLoginAt, createdAt
```

### 2.6 Service Method Signatures

| Method | Signature | Logic tom tat |
|:---|:---|:---|
| `getStudentList` | `Page<StudentDetailResponse> getStudentList(String q, String status, String jlptLevel, int page, int size)` | Query co filter, map sang DTO |
| `getStudentDetail` | `StudentDetailResponse getStudentDetail(Long studentId)` | Load student, throw 404, map DTO (khong include passwordHash) |
| `getStudentProgressSummary` | `StudentProgressResponse getStudentProgressSummary(Long studentId)` | Group by contentType, tinh completedCount + examStats |
| `getStudentSubmissions` | `Page<SubmissionSummaryResponse> getStudentSubmissions(Long studentId, String type, String status, int page, int size)` | Query submissions, tra summary |
| `suspendStudent` | `@Transactional SuspendUserResponse suspendStudent(String actorEmail, Long studentId, String reason)` | Validate reason, set suspended, revoke tokens, ghi audit |
| `activateStudent` | `@Transactional ActivateUserResponse activateStudent(String actorEmail, Long studentId)` | Check != active, set active, clear reason, ghi audit |

---

## 3. Risk Assessment

| # | Rui ro | Xac suat | Muc do | Bien phap |
|:--|:---|:---:|:---:|:---|
| R1 | `StudentContentProgressRepository` chua ton tai (Nguoi 3) khi compile | Cao | Trung binh | Dung `@Autowired(required = false)` + return empty list neu null |
| R2 | Revoke token khong dong bo -> student dung duoc session cu | Trung binh | Cao | Dat revoke trong cung `@Transactional`; JwtAuthFilter check token validity moi request |
| R3 | N+1 query khi load student list kem statistics | Trung binh | Trung binh | Dung `JOIN FETCH` hoac tach 2 query (basic info + count aggregate) |
| R4 | DTO map kem `passwordHash` ra response | Thap | Cao | DTO Mapper khong map truong passwordHash, oauthProviderId; unit test kiem tra |
| R5 | Race condition: 2 Staff cung suspend 1 student | Thap | Thap | `@Transactional` + check status truoc update du xu ly |
| R6 | Mo rong `StudentDetailResponse` lam breaking change cho team khac | Trung binh | Trung binh | Dung `@JsonInclude(NON_NULL)` -> fields moi null-safe, khong pha response cu |

---

## 4. Verification Plan

### 4.1 Unit Tests (Service Layer)

```
StudentManagementServiceTest:
  getStudentList_withStatusFilter_returnsOnlyMatchingStudents
  getStudentList_withJlptLevelFilter_returnsFilteredResults
  getStudentDetail_withValidId_returnsFullProfile
  getStudentDetail_withInvalidId_throwsResourceNotFoundException
  getStudentDetail_doesNotExposePasswordHash
  suspendStudent_withValidReason_setsStatusAndRevokesTokens
  suspendStudent_withAlreadySuspended_throws409Conflict
  suspendStudent_withShortReason_throwsValidationException
  activateStudent_withSuspendedAccount_setsActiveStatus
  activateStudent_withAlreadyActiveAccount_throws409Conflict
  suspendStudent_writesAuditLog
  getStudentProgressSummary_withNullProgressRepo_returnsEmptyList
```

### 4.2 Integration Tests (Controller Layer)

```
StaffStudentControllerTest:
  GET /staff/students                          -> 200 paginated
  GET /staff/students?status=suspended         -> chi tra suspended students
  GET /staff/students/{id}                     -> 200 full profile (khong co passwordHash)
  GET /staff/students/9999                     -> 404 STUDENT_NOT_FOUND
  GET /staff/students/{id}/progress            -> 200
  GET /staff/students/{id}/submissions         -> 200 paginated
  POST /staff/students/{id}/suspend (10chars)  -> 200
  POST /staff/students/{id}/suspend (5chars)   -> 400
  POST /staff/students/{id}/suspend (da suspended) -> 409
  POST /staff/students/{id}/activate           -> 200
  POST /staff/students/{id}/activate (da active)   -> 409
  Student JWT goi /staff/students              -> 403 FORBIDDEN
  Unauthenticated goi /staff/students          -> 401 UNAUTHORIZED
```

### 4.3 Manual Verification Checklist

- [ ] Dang nhap Staff -> vao trang danh sach hoc vien -> tim kiem bang ten/email -> hien thi dung ket qua
- [ ] Click vao 1 hoc vien -> xem day du profile + tien trinh (khong thay passwordHash trong network tab)
- [ ] Thuc hien suspend voi reason hop le -> hoc vien bi dang xuat -> thu dang nhap lai -> nhan 403
- [ ] Thuc hien activate -> hoc vien dang nhap lai binh thuong
- [ ] Kiem tra `admin_audit_logs` co record tuong ung sau moi thao tac suspend/activate
- [ ] Thu suspend 1 tai khoan da suspended -> nhan thong bao loi phu hop (khong crash)
