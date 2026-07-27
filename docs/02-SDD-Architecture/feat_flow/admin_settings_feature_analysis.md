# Settings Feature Analysis

## 1. Tóm tắt tổng quan

Admin Settings quản lý các nhóm system, SMTP, security và email template. Frontend chọn tab bằng query string; từng tab tải/lưu group riêng. Backend whitelist group, che password, upsert setting và áp dụng lại SMTP runtime.

## 2. Bản đồ cấu trúc

| File | Vai trò | Loại |
|---|---|---|
| [AdminSettings.jsx](apps/frontend/src/features/management/admin/AdminSettings.jsx) | Điều phối tab settings | React Page |
| [SystemTab.jsx](apps/frontend/src/features/management/components/admin/settings/SystemTab.jsx) | Maintenance/system setting | Component |
| [EmailTab.jsx](apps/frontend/src/features/management/components/admin/settings/EmailTab.jsx) | SMTP và email template | Component |
| [SecurityTab.jsx](apps/frontend/src/features/management/components/admin/settings/SecurityTab.jsx) | Security settings | Component |
| [adminService.js](apps/frontend/src/shared/api/adminService.js) | GET/PUT/test SMTP API | API Service |
| [AdminSettingsController.java](apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsController.java) | Settings endpoints | Controller |
| [AdminSettingsService.java](apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java) | Validate/upsert/mask/apply | Service |
| [SystemSetting.java](apps/backend/src/main/java/com/jlpt/feature/admin/SystemSetting.java) | Setting key/value | Entity |
| [SystemSettingRepository.java](apps/backend/src/main/java/com/jlpt/feature/admin/SystemSettingRepository.java) | Persistence theo group/key | Repository |

## 3. Bản đồ kết nối

```mermaid
flowchart LR
    Page["AdminSettings.jsx"] --> Tabs["System / Email / Security tabs"]
    Tabs --> API["adminService.js"]
    API --> Ctrl["AdminSettingsController.java"]
    Ctrl --> Svc["AdminSettingsService.java"]
    Svc --> DB[("system_settings")]
    Svc --> Mail["JavaMailSenderImpl"]
    System["SystemTab.jsx"] --> Maint["MaintenanceModeService.java"]
```

| Từ | Đến | Cách kết nối | Dữ liệu |
|---|---|---|---|
| AdminSettings | tab | render | query `tab` |
| Tab | adminService | JS call | group/key/value |
| Controller | service | DTO | setting payload |
| Service | repository | upsert | group/key/value |
| Service | mail sender | runtime apply/test | SMTP values |

## 4. Luồng xử lý theo trình tự

1. `/admin/settings?tab=...` chọn component tab.
2. Tab gọi `getSettings(group)`.
3. Backend xác nhận group thuộc whitelist và mask password thành `********`.
4. Khi lưu, UI gọi update một key hoặc batch.
5. Service bỏ qua password rỗng/masked để không ghi đè secret hiện tại.
6. Group SMTP được áp dụng lại vào `JavaMailSenderImpl`; nút test gọi endpoint riêng.

```mermaid
sequenceDiagram
    actor Admin
    participant Tab as "Settings Tab"
    participant API as "adminService.js"
    participant Ctrl as "AdminSettingsController.java"
    participant Svc as "AdminSettingsService.java"
    participant DB as "system_settings"
    Admin->>Tab: Mở tab
    Tab->>Svc: GET settings/{group}
    Svc->>DB: findBySettingGroup
    DB-->>Tab: values, password masked
    Admin->>Tab: Sửa và lưu
    Tab->>Svc: PUT group/key hoặc batch
    Svc->>DB: Upsert editable settings
    Svc-->>Admin: SettingResponse
```

## 5. Vai trò đoạn code quan trọng

[AdminSettingsService.java#L30](apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L30)

```java
public List<SettingResponse> getByGroup(String group) {
    validateGroup(group); // Chỉ các group whitelist mới được truy cập.
    return settingRepository.findBySettingGroup(group).stream()
            .map(s -> SettingResponse.builder()
                    // Password không bao giờ được trả nguyên văn về frontend.
                    .settingValue(isPassword(s.getSettingKey()) ? "********" : s.getSettingValue())
                    .build())
            .toList();
}
```

[AdminSettingsService.java#L58](apps/backend/src/main/java/com/jlpt/feature/admin/AdminSettingsService.java#L58)

```java
for (UpdateSettingsBatchRequest.Item item : items) {
    // Giá trị rỗng/masked mang nghĩa "giữ password cũ".
    if (isPassword(item.getSettingKey())
            && (item.getSettingValue() == null || item.getSettingValue().isBlank()
                    || "********".equals(item.getSettingValue()))) {
        continue;
    }
    result.add(upsert(group, item.getSettingKey(), item.getSettingValue()));
}
```

## 6. Dữ liệu di chuyển

Tab/group → list settings (secret masked) → form state → batch items → validated upsert → database/runtime mail sender → response/toast.

## 7. Bảng tra cứu tổng hợp

| Bước | File | Function | Kết nối tới | Dữ liệu | Ghi chú |
|---:|---|---|---|---|---|
| 1 | AdminSettings | `switchTab` | tab component | tab id | Query string |
| 2 | Tab | effect | API | group | Load |
| 3 | Service | `getByGroup` | repo | group | Mask password |
| 4 | Tab | save | update API | values | Single/batch |
| 5 | Service | `updateSettings` | repo/mail | items | Transaction |

## 8. Các mục cần bổ sung context

- Giá trị settings thực tế phụ thuộc dữ liệu DB/migration; tài liệu không đọc môi trường hoặc secret.
- Notification Rules dùng cùng bảng nhưng có JSON/domain service riêng, được tách thành tài liệu khác.
