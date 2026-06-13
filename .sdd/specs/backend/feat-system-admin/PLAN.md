# PLAN — System Administration (`feat-system-admin`)
> **Feature ID:** `feat-system-admin`
> **UC Coverage:** UC-35, UC-36, UC-37, UC-39, UC-40
> **Version:** 1.0 | **Status:** Ready to Implement
> **Author:** Team | **Last Updated:** 2026-06-14

---

## 1. User Intent

### 1.1 Vấn đề cần giải quyết
Admin cần một bảng điều khiển tập trung để:

1. **Xem tổng quan hệ thống (UC-36)** — số học viên, số ticket, số bài nộp chờ chấm — không cần truy cập DB trực tiếp.
2. **Thay đổi cấu hình hệ thống (UC-39)** — đổi SMTP host, bật/tắt maintenance mode, điều chỉnh threshold đăng nhập — mà không cần rebuild code.
3. **Quản lý quy tắc thông báo tự động (UC-40)** — tạo/sửa/tắt các quy tắc gửi notification khi học viên đạt mốc streak, thi đỗ, v.v.
4. **Theo dõi audit trail (UC-36)** — ai đã thay đổi gì, lúc nào.

### 1.2 Người dùng và kỳ vọng
| Actor | Kỳ vọng chính |
|:---|:---|
| **Admin** | Dashboard load nhanh (<1.5s), sửa cấu hình ngay và thấy kết quả, không thấy giá trị nhạy cảm (SMTP password) |

### 1.3 Ràng buộc nghiệp vụ quan trọng
- Settings có `is_editable = false` → **không thể** cập nhật, trả HTTP 422.
- Giá trị SMTP password, API key, secret trong response phải ẩn thành `"*****"` — detect qua keyword `password`, `secret`, `api_key` trong `setting_key`.
- Value type validation cứng trước khi save: `BOOLEAN` chỉ chấp nhận `"true"/"false"`, `INTEGER` phải parse được, `TIME` phải match `HH:mm`.
- Notification Rules **không xóa cứng** (hard delete) — chỉ set `isEnabled = false` trong JSON value.
- Admin **không được** tự sửa email hoặc status của account đang đăng nhập (prevent lockout).
- Mọi thay đổi setting, notification rule đều phải ghi vào `admin_audit_logs`.

---

## 2. Architectural Blueprint

### 2.1 Layer Stack

```
[React Frontend — Admin Panel]
     │
     │  GET/PUT /api/admin/settings/...
     │  GET/POST/PUT/DELETE /api/admin/notification-rules
     │  GET /api/admin/dashboard
     │  GET /api/admin/dashboard/audit-logs
     ▼
[AdminSystemController]         ← com.jlpt.controller.admin  [MỚI]
[AdminDashboardController]      ← com.jlpt.controller.admin  [MỚI]
     │  @PreAuthorize("hasRole('ADMIN')") — tất cả endpoints
     ▼
[SystemSettingService]          ← com.jlpt.service  [MỚI]
[NotificationRuleService]       ← com.jlpt.service  [MỚI]
     │
     ├── [SystemSettingRepository]        (read/write)  [MỚI]
     ├── [AdminAuditLogRepository]        (write)       (đã có)
     ├── [StudentUserRepository]          (READ)        (đã có)
     ├── [TicketRepository]              (READ)        (feat-support)
     └── [StudentSubmissionRepository]   (READ)        (Người 3/feat-support)

⚠️ KHÔNG sửa AdminController.java đã có — tạo riêng để tránh conflict.
```

### 2.2 File Structure

```
controller/admin/
├── AdminSystemController.java          [MỚI] — settings & notification-rules
├── AdminDashboardController.java       [MỚI] — dashboard & audit-logs
└── AdminController.java                [GIỮ NGUYÊN — đừng sửa]

service/
├── SystemSettingService.java           [MỚI]
└── NotificationRuleService.java        [MỚI]

repository/
└── SystemSettingRepository.java        [MỚI]

dto/
├── request/
│   ├── SystemSettingRequest.java       [MỚI]
│   └── NotificationRuleRequest.java    [MỚI]
└── response/
    ├── SystemSettingResponse.java      [MỚI]
    └── NotificationRuleResponse.java   [MỚI]
```

### 2.3 API Routes

| Method | Path | Mô tả |
|:---|:---|:---|
| GET | `/api/admin/settings` | List tất cả setting groups |
| GET | `/api/admin/settings/{group}` | List settings trong 1 group |
| GET | `/api/admin/settings/{group}/{key}` | Lấy 1 setting cụ thể |
| PUT | `/api/admin/settings/{group}/{key}` | Cập nhật giá trị setting |
| GET | `/api/admin/notification-rules` | Danh sách tất cả rules |
| POST | `/api/admin/notification-rules` | Tạo rule mới |
| PUT | `/api/admin/notification-rules/{ruleKey}` | Cập nhật rule |
| DELETE | `/api/admin/notification-rules/{ruleKey}` | Soft disable rule |
| GET | `/api/admin/dashboard` | Dashboard tổng quan |
| GET | `/api/admin/dashboard/audit-logs` | Xem audit log |

### 2.4 Data Flow — Update Setting

```
PUT /api/admin/settings/smtp/smtp_host
    Body: { "value": "smtp.gmail.com", "changeReason": "Đổi SMTP mới" }
  │
  ├─ [Controller] Extract adminId từ JWT, @Valid SystemSettingRequest
  │
  ├─ [Service] SystemSettingService.updateSetting("smtp", "smtp_host", req, adminId)
  │     ├── Load SystemSetting WHERE group='smtp' AND key='smtp_host' → 404 nếu không có
  │     ├── Check isEditable == true → 422 nếu false
  │     ├── Validate value theo valueType:
  │     │     BOOLEAN: "true"|"false" (ignore case)
  │     │     INTEGER: Integer.parseInt(value) → 400 nếu NumberFormatException
  │     │     TIME:    regex ^([01]\d|2[0-3]):[0-5]\d$ → 400 nếu sai
  │     │     STRING:  not blank
  │     ├── @Transactional:
  │     │     ├── setting.setValue(req.getValue())
  │     │     ├── setting.setUpdatedBy(adminUser)
  │     │     ├── setting.setUpdatedAt(now())
  │     │     └── auditLog.save(SETTING_UPDATED, "smtp/smtp_host → smtp.gmail.com")
  │     └── Return SystemSettingResponse (nếu key chứa 'password'|'secret'|'api_key' → value = "*****")
```

### 2.5 Data Flow — Notification Rule Storage

```
Notification Rules không có bảng riêng.
Lưu trong system_settings với:
  setting_group = 'notification'
  setting_key   = ruleKey (e.g. 'streak_10_days')
  setting_value = JSON string:
  {
    "enabled": true,
    "condition": "streak_10",
    "channel": "in_app",
    "templateTitle": "Chúc mừng streak 10 ngày!",
    "templateContent": "Bạn đã học...",
    "description": "..."
  }

POST /api/admin/notification-rules
  │
  ├─ [Service] NotificationRuleService.createRule(req, adminId)
  │     ├── Check key 'notification/{ruleKey}' chưa tồn tại → 400 nếu đã có
  │     ├── Build JSON value từ request fields
  │     ├── @Transactional:
  │     │     ├── systemSettingRepository.save(new SystemSetting(
  │     │     │       group='notification', key=ruleKey, value=jsonStr
  │     │     │   ))
  │     │     └── auditLog.save(NOTIFICATION_RULE_CREATED, ruleKey)
  │     └── Return NotificationRuleResponse

DELETE /api/admin/notification-rules/{ruleKey}
  │
  └─ [Service] NotificationRuleService.deleteRule(ruleKey, adminId)
        ├── Load setting → 404 nếu không có
        ├── Parse JSON, set "enabled": false
        ├── Update setting_value với JSON mới
        └── auditLog.save(NOTIFICATION_RULE_DELETED)
        // KHÔNG xóa record → soft disable
```

### 2.6 Sensitive Value Masking

```java
// SystemSettingService.java
private static final Set<String> SENSITIVE_KEYWORDS =
    Set.of("password", "secret", "api_key", "token", "private_key");

private SystemSettingResponse toResponse(SystemSetting setting) {
    String displayValue = setting.getSettingValue();
    String key = setting.getSettingKey().toLowerCase();
    if (SENSITIVE_KEYWORDS.stream().anyMatch(key::contains)) {
        displayValue = "*****";
    }
    return SystemSettingResponse.builder()
        .settingId(setting.getSettingId())
        .settingGroup(setting.getSettingGroup())
        .settingKey(setting.getSettingKey())
        .settingValue(displayValue)   // ← masked nếu sensitive
        .valueType(setting.getValueType())
        .isEditable(setting.getIsEditable())
        .build();
}
```

### 2.7 Maintenance Mode Integration

```java
// Trong JwtAuthFilter.java hoặc SecurityConfig.java — cần phối hợp với Người 1 (feat-auth)
// Khi Student cố đăng nhập mà maintenance = true:
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.addFilterBefore(new MaintenanceModeFilter(systemSettingService), 
                         UsernamePasswordAuthenticationFilter.class);
    // MaintenanceModeFilter:
    //   nếu systemSettingService.isMaintenanceMode() == true
    //   VÀ request là STUDENT role → trả 503 Service Unavailable
}
```

---

## 3. Risk Assessment

| # | Rủi ro | Xác suất | Mức độ | Biện pháp giảm thiểu |
|:--|:---|:---:|:---:|:---|
| R1 | Conflict với `AdminController.java` hiện có khi thêm file mới | Trung bình | Cao | Tạo hoàn toàn file mới `AdminSystemController`, `AdminDashboardController` — KHÔNG chỉnh sửa file cũ |
| R2 | JSON parse lỗi khi setting_value của rule bị malformed | Trung bình | Cao | Bọc Jackson `objectMapper.readValue()` trong try-catch, log warning, trả fallback response |
| R3 | Admin tự khóa tài khoản của mình (edit email/status của chính mình) | Thấp | Cao | Check trong service: `if (adminId.equals(targetAdminId)) throw ForbiddenException` |
| R4 | Sensitive value bị lộ khi Admin đọc settings | Trung bình | Cao | Tập trung masking logic trong 1 method `toResponse()`, unit test verify masking |
| R5 | Setting type validation không đồng nhất giữa import/export | Thấp | Trung bình | Enum `ValueType` { STRING, INTEGER, BOOLEAN, TIME } + factory method validate |
| R6 | Maintenance mode filter làm chậm tất cả requests | Thấp | Trung bình | Cache `isMaintenanceMode()` với Spring Cache TTL 30 giây |
| R7 | `TicketRepository` chưa tồn tại (feat-support chưa merge) khi build dashboard | Cao | Trung bình | Inject với `required = false`; dashboard trả `openTickets = null` nếu repo chưa sẵn |
| R8 | Notification Rule JSON quá lớn gây issue với `NVARCHAR(MAX)` trên SQL Server | Thấp | Thấp | Giới hạn `templateContent` ≤ 2000 ký tự ở validation layer |

---

## 4. Verification Plan

### 4.1 Unit Tests (Service Layer)
```
SystemSettingServiceTest:
  ✓ getAllGroups_returnsDistinctGroups
  ✓ getSettingsByGroup_masksSensitiveValues
  ✓ getSetting_withValidKey_returnsResponse
  ✓ getSetting_withInvalidKey_throws404
  ✓ updateSetting_withBooleanType_acceptsTrueAndFalse
  ✓ updateSetting_withBooleanType_rejectsYesNo
  ✓ updateSetting_withIntegerType_acceptsValidNumber
  ✓ updateSetting_withIntegerType_rejectsAbc
  ✓ updateSetting_withTimeType_accepts2359
  ✓ updateSetting_withTimeType_rejects2599
  ✓ updateSetting_withReadOnlySetting_throws422
  ✓ updateSetting_writesAuditLog
  ✓ isMaintenanceMode_returnsTrueWhenEnabled
  ✓ isMaintenanceMode_returnsFalseWhenDisabled

NotificationRuleServiceTest:
  ✓ createRule_withNewKey_savesAsSystemSetting
  ✓ createRule_withDuplicateKey_throws400
  ✓ updateRule_withExistingKey_updatesJsonValue
  ✓ deleteRule_setsEnabledFalseInJson_doesNotDeleteRecord
  ✓ listRules_parsesJsonAndReturnsAllRules
  ✓ triggerMilestone_withEnabledRule_createsNotification
  ✓ triggerMilestone_withEnabledRule_alreadySentIn24h_skips
```

### 4.2 Integration Tests (Controller Layer)
```
AdminSystemControllerTest:
  ✓ GET /admin/settings → 200 list of groups
  ✓ GET /admin/settings/smtp → 200, smtp_password = "*****"
  ✓ GET /admin/settings/smtp/smtp_host → 200 correct value
  ✓ GET /admin/settings/smtp/nonexistent → 404
  ✓ PUT /admin/settings/system/maintenance_mode {value:"true"} → 200
  ✓ PUT /admin/settings/system/maintenance_mode {value:"yes"} → 400
  ✓ PUT /admin/settings/{readOnlyGroup}/{key} → 422 SETTING_READ_ONLY
  ✓ POST /admin/notification-rules → 201
  ✓ POST /admin/notification-rules (duplicate ruleKey) → 400
  ✓ PUT /admin/notification-rules/{key} → 200
  ✓ DELETE /admin/notification-rules/{key} → 200, record still exists in DB
  ✓ DELETE /admin/notification-rules/nonexistent → 404
  ✓ Staff JWT gọi /admin/settings → 403 FORBIDDEN
  ✓ Unauthenticated gọi /admin/settings → 401 UNAUTHORIZED

AdminDashboardControllerTest:
  ✓ GET /admin/dashboard → 200 với Admin data
  ✓ GET /admin/dashboard/audit-logs → 200 paginated
  ✓ GET /admin/dashboard/audit-logs?action=SETTING_UPDATED → filter đúng
```

### 4.3 Security Verification
- [ ] Gọi `GET /api/admin/settings/smtp` → field `smtp_password` trả về `"*****"` (không phải giá trị thực)
- [ ] Staff JWT gọi bất kỳ `/api/admin/*` endpoint → nhận 403 FORBIDDEN
- [ ] Admin thử sửa email/status của chính mình → nhận lỗi phù hợp

### 4.4 Manual Verification Checklist
- [ ] Admin đăng nhập → vào trang System Settings → thấy danh sách groups
- [ ] Sửa `system/maintenance_mode = true` → Student thử đăng nhập → nhận thông báo bảo trì
- [ ] Admin vẫn đăng nhập được khi maintenance mode bật
- [ ] Tạo notification rule `streak_10_days` → học viên đạt 10 ngày streak → nhận notification
- [ ] Delete rule → `setting_value` JSON có `"enabled": false`, record vẫn còn trong DB
- [ ] Xem audit log → tất cả thao tác settings và rule đều có bản ghi tương ứng
