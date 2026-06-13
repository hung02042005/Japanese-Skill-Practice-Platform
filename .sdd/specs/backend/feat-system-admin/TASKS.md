# TASKS — System Administration (`feat-system-admin`)
> **UC Coverage:** UC-35, UC-36, UC-37, UC-39, UC-40 | **Actor:** Admin
> **Branch:** `feature/JLPT-XX-system-admin` | **Scope commit:** `admin`

---

## Phase 0: Git Setup
- [ ] 0.1 Sync code mới nhất từ `main`:
  ```bash
  git fetch origin
  git rebase origin/main
  ```
- [ ] 0.2 Tạo branch đúng format GIT_RULES Section 6:
  ```bash
  git checkout -b feature/JLPT-XX-system-admin
  ```
  > Thay `XX` bằng ticket ID thực tế trên board.
- [ ] 0.3 Xác nhận branch đang đúng: `git status` — working tree phải clean trước khi code.
- [ ] 0.4 ⚠️ Xác nhận `AdminController.java` hiện có **không bị sửa** trong suốt toàn bộ quá trình implement — tạo file mới hoàn toàn.

---

## Phase 1: DTOs

- [ ] 1.1 Tạo `dto/request/SystemSettingRequest.java`
  - `@NotBlank(message = "Giá trị setting không được để trống") String value`
  - `@Size(max=500) String changeReason` — optional, dành cho audit log
- [ ] 1.2 Tạo `dto/request/NotificationRuleRequest.java`
  - `@NotBlank @Pattern(regexp = "^[a-z][a-z0-9_]{2,49}$") String ruleKey`
  - `@NotBlank @Size(max=255) String description`
  - `@NotNull Boolean isEnabled`
  - `@Size(max=100) String triggerCondition`
  - `String channel` — `in_app|email|both`
  - `@Size(max=255) String templateTitle`
  - `@Size(max=2000) String templateContent` — giới hạn 2000 chars (NVARCHAR(MAX) safeguard)
- [ ] 1.3 Tạo `dto/response/SystemSettingResponse.java`
  - `settingId`, `settingGroup`, `settingKey`
  - `settingValue` — `"*****"` nếu key chứa sensitive keyword
  - `valueType` (`STRING`|`INTEGER`|`BOOLEAN`|`TIME`), `isEditable`
  - `updatedByAdminName`, `updatedAt`
- [ ] 1.4 Tạo `dto/response/NotificationRuleResponse.java`
  - `ruleKey`, `description`, `isEnabled`, `triggerCondition`, `channel`
  - `templateTitle`, `templateContent`, `updatedAt`, `updatedByAdminName`

**Commit sau phase này:**
```
feat(admin): add DTOs for system settings and notification rules
```

---

## Phase 2: Repository

- [ ] 2.1 Tạo `repository/SystemSettingRepository.java`
  - `findBySettingGroupAndSettingKey(String group, String key)`
  - `findBySettingGroup(String group)`
  - `findAllGroups()` — `@Query("SELECT DISTINCT s.settingGroup FROM SystemSetting s")`
  - `findAllBySettingGroup(String group)` — dùng cho notification rules: group = `notification`

**Commit sau phase này:**
```
feat(admin): add SystemSettingRepository
```

---

## Phase 3: Service Layer

### 3A — SystemSettingService
- [ ] 3.1 Tạo `service/SystemSettingService.java`
  - Annotations: `@Service`, `@RequiredArgsConstructor`, `@Slf4j`, `@Transactional`
- [ ] 3.2 Implement `getAllGroups()` → `List<String>`
- [ ] 3.3 Implement `getSettingsByGroup(String group)` → `List<SystemSettingResponse>`
  - Apply sensitive value masking cho mỗi item
- [ ] 3.4 Implement `getSetting(String group, String key)` → `SystemSettingResponse`
  - `404` nếu không tìm thấy
- [ ] 3.5 Implement `updateSetting(String group, String key, SystemSettingRequest req, Long adminId)`
  - Check `isEditable == true` → `422 SETTING_READ_ONLY` nếu false
  - Validate value theo `valueType`:
    - `BOOLEAN`: chỉ `"true"` / `"false"` (case-insensitive) — `400` nếu sai
    - `INTEGER`: `Integer.parseInt()` — `400` nếu `NumberFormatException`
    - `TIME`: regex `^([01]\d|2[0-3]):[0-5]\d$` — `400` nếu không match
    - `STRING`: không rỗng
  - Save, set `updatedBy`, `updatedAt`
  - Ghi audit log `SETTING_UPDATED` với `description = '{group}/{key} → {newValue}'`
- [ ] 3.6 Implement `isMaintenanceMode()` → `boolean`
  - Tìm `system/maintenance_mode`, parse boolean
  - Cache kết quả TTL 30 giây (Spring Cache) để tránh query mọi request
- [ ] 3.7 Implement `setMaintenanceMode(boolean enabled, Long adminId)`
  - Update setting, ghi audit log `MAINTENANCE_MODE_TOGGLED`
- [ ] 3.8 Tạo method private `toResponse(SystemSetting setting)` — **tập trung masking tại đây**:
  - Kiểm tra `setting_key.toLowerCase()` chứa bất kỳ keyword nào trong: `password`, `secret`, `api_key`, `token`, `private_key` → trả `"*****"`

### 3B — NotificationRuleService
- [ ] 3.9 Tạo `service/NotificationRuleService.java`
  - Annotations: `@Service`, `@RequiredArgsConstructor`, `@Slf4j`, `@Transactional`
- [ ] 3.10 Implement `listRules()` → `List<NotificationRuleResponse>`
  - Lấy tất cả settings `settingGroup = 'notification'`
  - Parse JSON value thành `NotificationRuleResponse` — bọc trong `try-catch`, log warning nếu JSON malformed
- [ ] 3.11 Implement `createRule(NotificationRuleRequest req, Long adminId)`
  - Check `notification/{ruleKey}` chưa tồn tại → `400 DUPLICATE_RULE_KEY`
  - Build JSON: `{ "enabled": true, "condition": "...", "channel": "...", "templateTitle": "...", "templateContent": "..." }`
  - Save `SystemSetting(group='notification', key=ruleKey, value=jsonStr, valueType='string')`
  - Ghi audit log `NOTIFICATION_RULE_CREATED`
- [ ] 3.12 Implement `updateRule(String ruleKey, NotificationRuleRequest req, Long adminId)`
  - Load `notification/{ruleKey}` → `404` nếu không có
  - Update JSON value, ghi audit log `NOTIFICATION_RULE_UPDATED`
- [ ] 3.13 Implement `deleteRule(String ruleKey, Long adminId)` — **SOFT DISABLE, không xóa record**
  - Load setting → `404` nếu không có
  - Parse JSON, set `"enabled": false`, update `setting_value`
  - Ghi audit log `NOTIFICATION_RULE_DELETED`
  - Record **vẫn tồn tại** trong DB — tuyệt đối không gọi `delete()`
- [ ] 3.14 Implement `triggerMilestone(Long studentId, String milestone)`
  - Kiểm tra rule `notification/milestone_{milestone}` enabled
  - Check duplicate 24h: `notificationRepository.existsByStudentIdAndRuleKeyAndCreatedAtAfter()`
  - Nếu pass: tạo `Notification`, ghi log `MILESTONE_NOTIFICATION_SENT`

**Commit sau phase này:**
```
feat(admin): implement SystemSettingService with value masking, validation, and NotificationRuleService
```

---

## Phase 4: Controller Layer

- [ ] 4.1 Tạo `controller/admin/AdminSystemController.java` — **file mới, KHÔNG chỉnh AdminController.java**
  - `@RequestMapping("/api/admin")`, security: `@PreAuthorize("hasRole('ADMIN')")`
  - `GET /settings` — list tất cả groups
  - `GET /settings/{group}` — settings trong group
  - `GET /settings/{group}/{key}` — 1 setting cụ thể
  - `PUT /settings/{group}/{key}` — cập nhật value
  - `GET /notification-rules` — danh sách rules
  - `POST /notification-rules` — tạo rule mới (201)
  - `PUT /notification-rules/{ruleKey}` — cập nhật rule
  - `DELETE /notification-rules/{ruleKey}` — soft disable
- [ ] 4.2 Tạo `controller/admin/AdminDashboardController.java` — **file mới**
  - `GET /api/admin/dashboard` — dashboard tổng quan (delegate sang `AnalyticsService`)
  - `GET /api/admin/dashboard/audit-logs` — paginated audit logs với filter `action`, `adminId`
- [ ] 4.3 Verify: Staff JWT gọi bất kỳ `/api/admin/*` → `403 FORBIDDEN`
- [ ] 4.4 Verify: Admin thử sửa email/status của chính mình → nhận lỗi phù hợp (prevent lockout)

**Commit sau phase này:**
```
feat(admin): add AdminSystemController and AdminDashboardController (new files, no edit to existing)
```

---

## Phase 5: Maintenance Mode Integration

- [ ] 5.1 Tạo `security/MaintenanceModeFilter.java` — phối hợp với Người 1 (feat-auth)
  - Nếu `systemSettingService.isMaintenanceMode() == true`
  - VÀ request role là `STUDENT` → trả `503 Service Unavailable` với message bảo trì
  - Admin và Staff vẫn đăng nhập/truy cập bình thường
- [ ] 5.2 Đăng ký filter vào `SecurityFilterChain` (vị trí trước `UsernamePasswordAuthenticationFilter`)
- [ ] 5.3 Confirm với Người 1 (feat-auth) để tránh conflict với `JwtAuthFilter`

**Commit sau phase này:**
```
feat(admin): add MaintenanceModeFilter to block student access when maintenance is enabled
```

---

## Phase 6: Testing

- [ ] 6.1 Unit test `SystemSettingServiceTest`:
  - `getSettingsByGroup_masksSensitiveValues` — key `smtp_password` → `"*****"`
  - `updateSetting_withBooleanType_acceptsTrueAndFalse`
  - `updateSetting_withBooleanType_rejectsYesNo` → 400
  - `updateSetting_withIntegerType_rejectsAbc` → 400
  - `updateSetting_withTimeType_accepts2359`
  - `updateSetting_withTimeType_rejects2599` → 400
  - `updateSetting_withReadOnlySetting_throws422`
  - `updateSetting_writesAuditLog`
  - `isMaintenanceMode_returnsTrueWhenEnabled`
- [ ] 6.2 Unit test `NotificationRuleServiceTest`:
  - `createRule_withNewKey_savesAsSystemSetting`
  - `createRule_withDuplicateKey_throws400`
  - `deleteRule_setsEnabledFalseInJson_doesNotDeleteRecord`
  - `listRules_handlesJsonMalformed_withoutException`
  - `triggerMilestone_alreadySentIn24h_skips`
- [ ] 6.3 Integration test `AdminSystemControllerTest`:
  - `GET /admin/settings/smtp` → `smtp_password = "*****"`
  - `PUT /admin/settings/system/maintenance_mode {value:"yes"}` → 400
  - `PUT /admin/settings/{readOnlyGroup}/{key}` → 422 SETTING_READ_ONLY
  - `DELETE /admin/notification-rules/{key}` → 200, record vẫn còn trong DB
  - `Staff JWT gọi /admin/settings` → 403
  - `Unauthenticated gọi /admin/settings` → 401
- [ ] 6.4 Security test:
  - `GET /api/admin/settings/smtp` → field `smtp_password` = `"*****"` (không phải giá trị thực)
  - Admin thử sửa email/status của chính mình → nhận lỗi phù hợp

**Commit sau phase này:**
```
test(admin): add unit and integration tests for SystemSettingService, NotificationRuleService, controllers
```

---

## Phase 7: PR & Code Review

- [ ] 7.1 Tự review diff trước khi tạo PR:
  ```bash
  git diff origin/main...HEAD
  ```
  Kiểm tra checklist:
  - [ ] Không có file `.env`, `.vscode/`, `.idea/` bị commit nhầm
  - [ ] `AdminController.java` hiện có **không bị chỉnh sửa**
  - [ ] Không return Entity trực tiếp — chỉ DTO ra API
  - [ ] Soft disable notification rules — không có `delete()` nào được gọi
  - [ ] SMTP password, API key luôn hiển thị là `"*****"` trong response
  - [ ] Mọi thay đổi setting và rule đều có audit log
- [ ] 7.2 Sync code mới nhất trước khi tạo PR:
  ```bash
  git fetch origin
  git rebase origin/main
  ```
- [ ] 7.3 Tạo PR trên GitHub:
  - **Title:** `feat(admin): system settings, notification rules, maintenance mode UC-39/40`
  - **Description:** Mô tả WHAT + WHY, link ticket, note rõ không sửa `AdminController.java`
  - Yêu cầu ít nhất **1 reviewer** approve
- [ ] 7.4 Merge bằng **Squash and Merge**
- [ ] 7.5 Xóa branch sau khi merge thành công
