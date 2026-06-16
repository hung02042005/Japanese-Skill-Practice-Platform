# Prompt: Tái cơ cấu Backend Layer-based → Feature-based (Spring Boot)

> **Mục tiêu cốt lõi:** Không thay đổi bất kỳ business logic nào.
> Chỉ di chuyển file, cập nhật package declarations và import statements.

---

## ⚠️ TRƯỚC KHI BẮT ĐẦU — Git Safety Gate

Xác nhận rằng tôi đã tạo branch mới:

```bash
git checkout -b refactor/feature-based-structure
```

Nếu tôi chưa làm điều này, hãy nhắc tôi làm trước khi tiếp tục.

---

## Context kỹ thuật (BẮT BUỘC đọc trước khi làm)

**Stack:** Java 17 + Spring Boot [3.x] + Maven + Spring Data JPA + Spring Security  
**Root package:** `com.jlpt`  
**Main class:** `com.jlpt.JlptApplication` (có `@SpringBootApplication`)  
**Build tool:** Maven — dùng `mvn compile` để verify

**Cấu trúc hiện tại:**

```
src/main/java/com/jlpt/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
│   ├── request/
│   └── response/
├── config/         ← Spring Security, CORS, Bean configs
├── exception/      ← GlobalExceptionHandler, custom exceptions
└── security/       ← JWT filter, UserDetailsService impl
```

**[PASTE DANH SÁCH FILE THỰC TẾ TẠI ĐÂY — ví dụ output của lệnh sau:]**

```bash
find src -name "*.java" | sort
```

---

## Cấu trúc đích (Feature-based)

```
src/main/java/com/jlpt/
├── JlptApplication.java       ← PHẢI thêm @ComponentScan, @EntityScan, @EnableJpaRepositories
│
├── shared/                    ← Dùng chung, không phụ thuộc feature
│   ├── config/
│   ├── exception/
│   ├── security/
│   └── base/                  ← BaseEntity, abstract classes, interfaces chung
│
└── feature/
    ├── auth/
    │   ├── AuthController.java
    │   ├── AuthService.java
    │   └── dto/
    ├── user/
    │   ├── UserEntity.java
    │   ├── UserRepository.java
    │   ├── UserService.java
    │   ├── UserController.java
    │   └── dto/
    └── [các feature khác...]
```

---

## Quy tắc xử lý Cross-Feature Dependencies

Khi phân tích, nếu gặp trường hợp Service/Repository của feature A cần Entity/Service của feature B:

| Trường hợp | Cách xử lý |
|---|---|
| Tham chiếu JPA (`@ManyToOne`, `@OneToMany`) | Giữ nguyên, chỉ cập nhật import path |
| Service A inject Service B | Liệt kê rõ cặp dependency này trong báo cáo Bước 1, không tự ý tách |
| Utility/Helper dùng nhiều nơi | Chuyển vào `shared/` |

> **KHÔNG** tạo circular dependency giữa các feature packages.

---

## Tiêu chí phân loại: Shared vs Feature

Một class thuộc về `shared/` nếu **ít nhất 2 feature** sử dụng nó trực tiếp, HOẶC nếu nó là:

- Security configuration, JWT utilities, CORS config
- Global exception handler
- Base entity / audit entity
- Common response wrapper (`ApiResponse`, `PageResponse`…)
- Email service, File upload service (infrastructure concerns)

Ngược lại → thuộc về feature tương ứng.

---

## Kế hoạch thực hiện

### Bước 1 — Phân tích & Mapping

⏸ **DỪNG LẠI SAU BƯỚC NÀY — Chờ phê duyệt trước khi sang Bước 2.**

Đọc danh sách file tôi cung cấp và xuất ra bảng sau:

| Feature | Entity | Repository | Service | Controller | DTO |
|---------|--------|------------|---------|------------|-----|
| auth | - | - | AuthService | AuthController | LoginRequest, TokenResponse |
| user | UserEntity | UserRepository | UserService | UserController | ... |
| ... | | | | | |

**Thêm vào cuối báo cáo:**

1. Danh sách class đề xuất đưa vào `shared/`
2. Danh sách cross-feature dependencies phát hiện được:
   - Format: `[FeatureA.ClassX] → [FeatureB.ClassY]`
3. Cảnh báo nếu có class không rõ thuộc feature nào

---

### Bước 2 — Cập nhật Main Class & Di chuyển Shared

**Việc đầu tiên:** Cập nhật `JlptApplication.java`:

```java
@SpringBootApplication
@ComponentScan(basePackages = "com.jlpt")
@EntityScan(basePackages = "com.jlpt")
@EnableJpaRepositories(basePackages = "com.jlpt")
public class JlptApplication { ... }
```

Sau đó di chuyển tất cả class đã được duyệt vào `shared/`.  
Báo cáo: danh sách file đã di chuyển + import thay đổi tương ứng (`old path → new path`).

---

### Bước 3 — Di chuyển theo Feature (từng feature một)

Thực hiện **từng feature** theo thứ tự từ ít dependency nhất đến nhiều nhất.
Thứ tự thường gặp: `kanji` → `vocabulary` → `user` → `auth` → `lesson` → `course` → `quiz`

Với mỗi feature, báo cáo:

- File nào được tạo ở đâu
- Import nào thay đổi (`old → new`)
- Có cross-feature dependency nào cần lưu ý không

---

### Bước 4 — Migrate Test Directory

Áp dụng cấu trúc tương tự cho `src/test/java/com/jlpt/`:

```
src/test/java/com/jlpt/
├── shared/
└── feature/
    ├── auth/
    └── user/
```

---

### Bước 5 — Verify Build

```bash
# Bước 5a: Compile
mvn compile -q 2>&1 | head -50
```

- Nếu **BUILD SUCCESS** → báo cáo tổng kết
- Nếu có lỗi → phân tích từng lỗi, fix theo nhóm, chạy lại

```bash
# Bước 5b: Chạy unit test (skip integration test)
mvn test -Dtest="**/*UnitTest,**/*Test" -DfailIfNoTests=false -q
```

---

## Quy tắc bất biến (KHÔNG được vi phạm)

- Không **xóa** file — chỉ tạo mới + cập nhật content
- Không thay đổi logic bên trong method body
- Không thay đổi tên class, tên method, tên field
- Giữ nguyên tất cả annotation (`@RestController`, `@Service`, `@Entity`…)
- Không tự ý thêm design pattern mới (ví dụ: không tự thêm interface nếu không có sẵn)
- Nếu không chắc một class thuộc feature nào → **hỏi**, đừng tự phán đoán

---

## Checklist tự kiểm tra (sau khi hoàn thành)

- [ ] `mvn compile` không có lỗi
- [ ] `@ComponentScan`, `@EntityScan`, `@EnableJpaRepositories` đã được cập nhật
- [ ] Không còn file nào trong các package cũ (`controller/`, `service/`, `repository/`…)
- [ ] Mỗi feature có đủ: Entity, Repository, Service, Controller, dto/
- [ ] `shared/` chỉ chứa class được ít nhất 2 feature dùng
- [ ] Test directory đã được migrate song song
- [ ] Không có circular dependency giữa các feature packages
- [ ] Git branch đang là `refactor/feature-based-structure`
