# TC-UC-04 — Test Cases: Hồ Sơ Cá Nhân (User Profile)

> **Feature:** `feat-auth` | **UC:** UC-04 | **Version:** 1.0
> **Nguồn AC:** UC-04 § 8 | **Nguồn FR-TEST:** SPEC.md § 3.3.1, § 3.3.2
> **Cập nhật:** 2026-05-30

---

## 1. UNIT TESTS — StudentService (JUnit 5 + Mockito)

> **File:** `StudentServiceTest.java` | **Tag:** `@Tag("unit")`

---

### TC-U-04-01 — Lấy hồ sơ cá nhân thành công: response KHÔNG chứa sensitive fields

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-04-01 |
| **Tham chiếu** | AC-04-01, BR-04-10 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
StudentUser user = aStudent()
    .withStudentId(1L)
    .withFullName("Trần Thị A")
    .withEmail("student@test.com")
    .withPasswordHash("$2a$10$secrethash")
    .withLoginAttempts(2)
    .withLockedUntil(null)
    .withOauthProviderId("google-sub-12345")
    .build();
when(userRepository.findById(1L)).thenReturn(Optional.of(user));
```

**Steps:**
1. Gọi `studentService.getMyProfile(1L)`

**Expected:**
- Trả về `StudentProfileResponse` chứa: `studentId`, `fullName`, `email`, `phone`, `avatarUrl`, `currentJlptLevel`, `targetJlptLevel`, `createdAt`
- Response KHÔNG có field `passwordHash`
- Response KHÔNG có field `loginAttempts`
- Response KHÔNG có field `lockedUntil`
- Response KHÔNG có field `oauthProviderId`

---

### TC-U-04-02 — Cập nhật hồ sơ: partial update — chỉ update trường được gửi

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-04-02 |
| **Tham chiếu** | AC-04-02, BR-04-06 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:**
```java
StudentUser user = aStudent()
    .withFullName("Nguyễn Văn A")
    .withPhone("0901234567")
    .build();
UpdateProfileRequest request = new UpdateProfileRequest();
request.setFullName("Trần Thị B"); // chỉ cập nhật fullName
// phone KHÔNG có trong request
```

**Steps:**
1. Gọi `studentService.updateMyProfile(1L, request)`

**Expected:**
- `user.fullName` = `"Trần Thị B"`
- `user.phone` = `"0901234567"` (KHÔNG thay đổi)
- `userRepository.save()` được gọi với entity đã cập nhật

---

### TC-U-04-03 — Cập nhật hồ sơ: trường bất biến bị bỏ qua (email không thay đổi)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-04-03 |
| **Tham chiếu** | AC-04-03, BR-04-02 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 — CRITICAL |

**Setup:**
```java
StudentUser user = aStudent().withEmail("original@test.com").build();
UpdateProfileRequest request = new UpdateProfileRequest();
request.setEmail("hacked@evil.com"); // thử thay đổi email
```

**Steps:**
1. Gọi `studentService.updateMyProfile(1L, request)`

**Expected:**
- `user.email` vẫn = `"original@test.com"` (KHÔNG thay đổi)
- `userRepository.save()` có thể được gọi nhưng email entity KHÔNG bị modify

---

### TC-U-04-04 — Cập nhật hồ sơ: currentJlptLevel không thể thay đổi qua endpoint này

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-04-04 |
| **Tham chiếu** | BR-04-03 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
StudentUser user = aStudent().withCurrentJlptLevel("N4").build();
UpdateProfileRequest request = new UpdateProfileRequest();
request.setCurrentJlptLevel("N2"); // thử thay đổi currentJlptLevel
```

**Steps:**
1. Gọi `studentService.updateMyProfile(1L, request)`

**Expected:**
- `user.currentJlptLevel` vẫn = `"N4"`
- `targetJlptLevel` có thể thay đổi nếu có trong request (khác với `currentJlptLevel`)

---

### TC-U-04-05 — Học viên chỉ xem/sửa hồ sơ của mình (không thể truyền studentId khác)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-04-05 |
| **Tham chiếu** | BR-04-01 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 — CRITICAL |

**Setup:**
```java
// JWT của student_id = 1
// Thử gọi với studentId = 99 (không phải của mình)
```

**Steps:**
1. `studentService.getMyProfile(studentId)` — studentId được lấy từ JWT, không từ URL/body

**Verify (Architecture check):**
- Controller KHÔNG nhận `id` từ path parameter cho endpoint `/api/students/me`
- `student_id` chỉ được lấy từ `SecurityContextHolder` / JWT claims
- Service method nhận `studentId` từ controller đã xác thực, không từ request body

---

### TC-U-04-06 — Upload avatar: MIME type thực sự được kiểm tra (magic bytes)

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-04-06 |
| **Tham chiếu** | BR-04-04, UC-04 § 5 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 — Security |

**Setup:**
```java
// File có extension .jpg nhưng content thực sự là .exe (magic bytes sai)
byte[] fakeContent = new byte[]{ 0x4D, 0x5A, 0x90, 0x00 }; // MZ header (Windows EXE)
MockMultipartFile fakeFile = new MockMultipartFile("file", "malicious.jpg", "image/jpeg", fakeContent);
```

**Steps:**
1. Gọi `studentService.uploadAvatar(1L, fakeFile)`

**Expected:**
- Ném `ValidationException` với message "File không phải là ảnh hợp lệ"
- `fileStorageService.store()` KHÔNG được gọi
- `userRepository.updateAvatarUrl()` KHÔNG được gọi

---

### TC-U-04-07 — Upload avatar: KHÔNG lưu BLOB vào DB

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-04-07 |
| **Tham chiếu** | BR-04-04 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 — Architecture Invariant |

**Setup:**
```java
// File JPG hợp lệ 1MB
when(fileStorageService.store(any(), any())).thenReturn("https://storage.example.com/1_uuid.jpg");
```

**Steps:**
1. Gọi `studentService.uploadAvatar(1L, validJpgFile)`

**Expected:**
- `userRepository.save()` được gọi với `avatarUrl = "https://..."` (URL string, không phải byte[])
- `fileStorageService.store()` được gọi (file được lưu ra ngoài DB)
- Entity `avatarUrl` là String (KHÔNG phải `byte[]` hay `Blob`)

---

### TC-U-04-08 — Upload avatar: tên file được tạo an toàn ({studentId}_{uuid}.{ext})

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-04-08 |
| **Tham chiếu** | UC-04 § 3.3 Bước 6 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:**
```java
ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
```

**Steps:**
1. Gọi `studentService.uploadAvatar(1L, validJpgFile)`
2. Verify: `fileStorageService.store(any(), fileNameCaptor.capture())`

**Expected:**
- `fileName` khớp pattern: `"1_[a-f0-9-]{36}\\.(jpg|jpeg|png|webp|gif)"` (studentId + UUID + ext)
- `fileName` KHÔNG chứa tên file gốc từ client (chống path traversal)

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `StudentRepositoryIT.java` | **Tag:** `@Tag("integration")`

---

### TC-I-04-01 — Cập nhật avatar_url trong DB

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-04-01 |
| **Tham chiếu** | AC-04-04 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P1 |

**Steps:**
1. Seed user với `avatar_url = NULL`
2. Gọi `userRepository.updateAvatarUrl(1L, "https://cdn.example.com/new-avatar.jpg")`
3. Reload entity

**Expected:**
- `avatar_url = "https://cdn.example.com/new-avatar.jpg"` trong DB
- `updated_at` được cập nhật

---

### TC-I-04-02 — Partial update: chỉ cập nhật trường được cung cấp

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-I-04-02 |
| **Tham chiếu** | BR-04-06 |
| **Loại** | Integration — Repository |
| **Ưu tiên** | P1 |

**Steps:**
1. Seed user với `full_name="Cũ", phone="0901234567"`
2. Gọi update với chỉ `fullName = "Mới"`
3. Reload entity

**Expected:**
- `full_name = "Mới"`
- `phone = "0901234567"` (giữ nguyên)

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `StudentControllerTest.java` | **Tag:** `@Tag("api")`

---

### TC-A-04-01 — GET /api/students/me — không có JWT → HTTP 401

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-04-01 |
| **Tham chiếu** | AC-04-06, FR-TEST-A-01 |
| **Loại** | API — Security |
| **Ưu tiên** | P0 |

**Steps:**
1. Gửi `GET /api/students/me` không có header `Authorization`

**Expected:**
```
HTTP 401
{ "errorCode": "UNAUTHORIZED" }
```

---

### TC-A-04-02 — GET /api/students/me — JWT hợp lệ → HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-04-02 |
| **Tham chiếu** | AC-04-01 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Mock:** `studentService.getMyProfile(1L)` trả về `StudentProfileResponse`

**Expected:**
```
HTTP 200
{
  "studentId": 1,
  "fullName": "Test Student",
  "email": "student@test.com",
  "currentJlptLevel": "N3"
}
```
- Response body KHÔNG chứa `passwordHash`, `loginAttempts`, `lockedUntil`, `oauthProviderId`

---

### TC-A-04-03 — PUT /api/students/me — cập nhật hợp lệ → HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-04-03 |
| **Tham chiếu** | AC-04-02 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:**
```json
{ "fullName": "Trần Thị B", "targetJlptLevel": "N3" }
```

**Mock:** `studentService.updateMyProfile()` thành công, trả về updated profile

**Expected:**
```
HTTP 200
```
- Response chứa dữ liệu đã được cập nhật

---

### TC-A-04-04 — PUT /api/students/me — targetJlptLevel không hợp lệ → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-04-04 |
| **Tham chiếu** | UC-04 § 5 Validation |
| **Loại** | API — Validation |
| **Ưu tiên** | P1 |

**Request:**
```json
{ "targetJlptLevel": "N6" }
```

**Expected:**
```
HTTP 400
{ "errorCode": "VALIDATION_FAILED", "errors": [{ "field": "targetJlptLevel", "message": "Cấp độ JLPT không hợp lệ..." }] }
```

---

### TC-A-04-05 — POST /api/students/me/avatar — file hợp lệ → HTTP 200

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-04-08 |
| **Tham chiếu** | AC-04-04 |
| **Loại** | API — Controller |
| **Ưu tiên** | P0 |

**Request:** `multipart/form-data` với file JPG 2MB hợp lệ

**Mock:** `studentService.uploadAvatar()` trả về `AvatarUploadResponse(avatarUrl = "https://...")`

**Expected:**
```
HTTP 200
{ "avatarUrl": "https://..." }
```
- `avatarUrl` bắt đầu bằng `"https://"`

---

### TC-A-04-09 — POST /api/students/me/avatar — file quá lớn (> 5MB) → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-04-09 |
| **Tham chiếu** | AC-04-05 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Mock:** `studentService.uploadAvatar()` ném `FileTooLargeException`

**Expected:**
```
HTTP 400
{ "errorCode": "VALIDATION_FAILED", "message": "File quá lớn. Kích thước tối đa là 5 MB" }
```

---

### TC-A-04-10 — POST /api/students/me/avatar — định dạng không hỗ trợ → HTTP 400

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-04-10 |
| **Tham chiếu** | UC-04 § 5 |
| **Loại** | API — Controller |
| **Ưu tiên** | P1 |

**Request:** `multipart/form-data` với file .pdf

**Mock:** `studentService.uploadAvatar()` ném `InvalidFileTypeException`

**Expected:**
```
HTTP 400
{ "errorCode": "VALIDATION_FAILED", "message": "Định dạng ảnh không được hỗ trợ..." }
```

---

### TC-A-04-11 — GET /api/students/me — Staff JWT không có quyền truy cập endpoint Student

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-A-04-11 |
| **Tham chiếu** | FR-TEST-A-02 |
| **Loại** | API — Security |
| **Ưu tiên** | P0 |

**Steps:**
1. Gửi `GET /api/students/me` với JWT của role `STAFF`

**Expected:**
```
HTTP 403
```

---

## 4. VALIDATION MATRIX — PUT /api/students/me

| TC ID | Field | Input | Expected HTTP | Error |
|:---|:---|:---|:---|:---|
| TC-A-04-V01 | fullName | 1 ký tự | 400 | VALIDATION_FAILED |
| TC-A-04-V02 | fullName | 151 ký tự | 400 | VALIDATION_FAILED |
| TC-A-04-V03 | phone | `"0123456"` (< 10 digits) | 400 | VALIDATION_FAILED |
| TC-A-04-V04 | phone | `"01234567890123456"` (> 15 digits) | 400 | VALIDATION_FAILED |
| TC-A-04-V05 | targetJlptLevel | `"N6"` | 400 | VALIDATION_FAILED |
| TC-A-04-V06 | targetJlptLevel | `"N1"` (hợp lệ) | 200 | — |

---

## 5. SECURITY INVARIANT TESTS

---

### TC-S-04-01 — GET /api/students/me KHÔNG bao giờ trả về passwordHash

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-04-01 |
| **Tham chiếu** | FR-TEST-S-03, BR-04-10 |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**
1. Gọi `GET /api/students/me` với JWT hợp lệ
2. Parse toàn bộ JSON response (đệ quy)

**Expected:**
- KHÔNG có key nào tên: `password`, `passwordHash`, `password_hash`
- KHÔNG có key nào tên: `loginAttempts`, `login_attempts`, `lockedUntil`, `oauthProviderId`

---

### TC-S-04-02 — Thay đổi email bị từ chối tại Service layer

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-S-04-02 |
| **Tham chiếu** | AC-04-03, BR-04-02 |
| **Loại** | Security Invariant |
| **Ưu tiên** | P0 — CRITICAL |

**Steps:**
1. Gọi `PUT /api/students/me` với `{ "email": "hacked@evil.com" }` (JWT của student@test.com)
2. Reload dữ liệu user từ DB

**Expected:**
- `email` trong DB vẫn là `"student@test.com"`
- Email không thể bị thay đổi qua endpoint này

---

## 6. FRONTEND COMPONENT TESTS

> **File:** `ProfilePage.test.tsx`

---

### TC-F-04-01 — Redirect về login khi gọi profile mà không có token

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-04-01 |
| **Tham chiếu** | AC-04-06, FR-TEST-F-05 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P0 |

**Mock:** API → HTTP 401

**Steps:**
1. Render `<ProfilePage />` khi không có token trong state

**Expected:**
- Chuyển hướng đến `/login`
- Token state được xóa

---

### TC-F-04-02 — Hiển thị đúng thông tin profile từ API

| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-F-04-02 |
| **Tham chiếu** | AC-04-01 |
| **Loại** | Frontend — Component |
| **Ưu tiên** | P1 |

**Mock:** `GET /api/students/me` → `{ fullName: "Trần Thị A", email: "a@test.com", ... }`

**Steps:**
1. Render `<ProfilePage />`

**Expected:**
- "Trần Thị A" xuất hiện trong DOM
- "a@test.com" xuất hiện trong DOM
- Không hiển thị loading state sau khi data loaded

---

## 7. COVERAGE CHECKLIST

| Business Rule | Test Case | Covered? |
|:---|:---|:---|
| BR-04-01: Chỉ xem/sửa hồ sơ của mình | TC-U-04-05 | ✅ |
| BR-04-02: Trường bất biến không thay đổi | TC-U-04-03, TC-S-04-02 | ✅ |
| BR-04-03: currentJlptLevel không sửa được | TC-U-04-04 | ✅ |
| BR-04-04: Avatar không lưu BLOB | TC-U-04-07 | ✅ |
| BR-04-06: Partial update | TC-U-04-02, TC-I-04-02 | ✅ |
| BR-04-07: phone format validation | TC-A-04-V04 | ✅ |
| BR-04-08: Sensitive fields không xuất hiện | TC-U-04-01, TC-S-04-01 | ✅ |
| File upload security (magic bytes) | TC-U-04-06 | ✅ |
| File name không chứa original name (traversal) | TC-U-04-08 | ✅ |
