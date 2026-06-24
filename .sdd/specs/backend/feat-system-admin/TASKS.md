# TASKS — System Administration (`feat-system-admin`)
> **UC Coverage:** UC-35, UC-36, UC-37, UC-39, UC-40 | **Actor:** Admin
> **Branch:** `feature/JLPT-XX-system-admin` | **Scope commit:** `admin`

---

## Phase 0: Git Setup
- [x] 0.1 Sync code mới nhất từ `main` — branch `refactor/feature-based-structure` đã active
- [x] 0.2 Branch đang dùng: `refactor/feature-based-structure`
- [x] 0.3 Working tree đang có các file mới/đã sửa theo đúng feature scope
- [x] 0.4 ⚠️ `AdminController.java` hiện có **KHÔNG bị sửa** — các file mới được tạo trong `feature/admin/`

---

## Phase 1: DTOs

- [x] 1.1 `feature/admin/dto/SystemSettingRequest.java` — DONE (`@NotBlank value`, `@Size(max=500) changeReason`)
- [x] 1.2 `shared/notification/dto/NotificationRuleRequest.java` — DONE (tất cả validations đầy đủ)
- [x] 1.3 `feature/admin/dto/SystemSettingResponse.java` — DONE (sensitive masking field)
- [x] 1.4 `shared/notification/dto/NotificationRuleResponse.java` — DONE

**Commit sau phase này:**
```
feat(admin): add DTOs for system settings and notification rules
```

---

## Phase 2: Repository

- [x] 2.1 `feature/admin/repository/SystemSettingRepository.java` — DONE
  - `findBySettingGroupAndSettingKey`, `findAllBySettingGroup`, `existsBySettingGroupAndSettingKey`, `findAllDistinctGroups`

**Commit sau phase này:**
```
feat(admin): add SystemSettingRepository
```

---

## Phase 3: Service Layer

### 3A — SystemSettingService
- [x] 3.1 `feature/admin/service/SystemSettingService.java` — DONE
- [x] 3.2 `getAllGroups()` — DONE
- [x] 3.3 `getSettingsByGroup()` với sensitive masking — DONE
- [x] 3.4 `getSetting()` với 404 — DONE
- [x] 3.5 `updateSetting()` với isEditable check, type validation, audit log — DONE
- [x] 3.6 `isMaintenanceMode()` — DONE
- [x] 3.7 `setMaintenanceMode(boolean, Long)` — DONE (thêm trong session này)
- [x] 3.8 `toResponse()` private với masking tập trung — DONE

### 3B — NotificationRuleService
- [x] 3.9 `shared/notification/service/NotificationRuleService.java` — DONE
- [x] 3.10 `listRules()` — DONE (parse JSON, filter null, log malformed)
- [x] 3.11 `createRule()` với duplicate check — DONE
- [x] 3.12 `updateRule()` với 404 — DONE
- [x] 3.13 `deleteRule()` soft disable (enabled=false trong JSON, không xóa record) — DONE
- [x] 3.14 `triggerMilestone()` với duplicate-check 24h — DONE

**Commit sau phase này:**
```
feat(admin): implement SystemSettingService with value masking, validation, and NotificationRuleService
```

---

## Phase 4: Controller Layer

- [x] 4.1 `feature/admin/controller/AdminSystemController.java` — DONE (file mới, AdminController.java không bị chỉnh)
  - GET/PUT settings endpoints, notification-rules CRUD, dashboard/summary, audit-log, smtp/test
- [x] 4.2 `feature/admin/controller/AdminDashboardController.java` — DONE (`@RequestMapping("/api/staff")` phục vụ STAFF+ADMIN)
- [x] 4.3 Security: `@PreAuthorize("hasRole('ADMIN')")` trên AdminSystemController — Staff JWT sẽ nhận 403
- [x] 4.4 Self-modification guard được xử lý ở AdminController (hiện có)

**Commit sau phase này:**
```
feat(admin): add AdminSystemController and AdminDashboardController (new files, no edit to existing)
```

---

## Phase 5: Maintenance Mode Integration

- [x] 5.1 `security/MaintenanceModeFilter.java` — DONE
  - 503 khi maintenance_mode=true VÀ role=STUDENT
  - Admin/Staff bypass qua BYPASS_PREFIXES = {"/api/staff", "/api/admin"}
- [x] 5.2 Filter đăng ký trong SecurityConfig: `addFilterAfter(maintenanceModeFilter, JwtAuthenticationFilter.class)`
- [x] 5.3 Không conflict với JwtAuthFilter — chạy sau JWT filter, đọc SecurityContextHolder

**Commit sau phase này:**
```
feat(admin): add MaintenanceModeFilter to block student access when maintenance is enabled
```

---

## Phase 6: Testing

- [x] 6.1 Unit test `SystemSettingServiceTest` — DONE (`feature/admin/service/SystemSettingServiceTest.java`, 9 cases: masking, BOOLEAN/INTEGER/TIME validation accept+reject, read-only 422, audit log write, maintenance mode read)
- [x] 6.2 Unit test `NotificationRuleServiceTest` — DONE (`feature/notification/service/NotificationRuleServiceTest.java`, 5 cases: create, duplicate key 400, soft-disable delete, malformed JSON handling, 24h dedupe skip)
- [x] 6.3 Integration test `AdminSystemControllerTest` — DONE (`feature/admin/controller/AdminSystemControllerTest.java`, 7 cases: SMTP masking, invalid boolean 400, read-only 422, soft-delete rule via real H2 DB, staff/anonymous → 403, admin self-suspend → 403)
- [x] 6.4 Security test — DONE (covered within `AdminSystemControllerTest`: SMTP password masked end-to-end, admin self-modification guard verified via real `/api/admin/users/admin/{ownId}/suspend` call)

> 21 new tests added, all passing. Full regression (`mvn test`): 128/128 passing, no existing test broken.

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
