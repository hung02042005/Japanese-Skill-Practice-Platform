# FEATURE DOUBLE-CHECK PROMPT
>
> Dùng sau mỗi sprint / sau khi code xong một feature mới.
> Mục tiêu: Verify từng feature mới KHÔNG bị broken, KHÔNG làm hỏng feature cũ.

---

## CONTEXT (ĐIỀN TRƯỚC KHI CHẠY)

```
Tech Stack    : [Spring Boot 3.x / NestJS / FastAPI / ...]
Database      : [PostgreSQL / MySQL / MongoDB]
Frontend      : [React / Vue / Next.js / ...]
Auth Method   : [JWT / Session / OAuth2]
New Features  : [Liệt kê tên các feature vừa code]
Changed Files : [Liệt kê file đã thay đổi / commit hash]
```

---

## REQUIRED PROCESS

### PHASE 0 — Xác định phạm vi kiểm tra

Trước mọi thứ khác, hãy xác định:

1. **Feature mới** nào vừa được thêm vào?
2. **File nào bị thay đổi?** (controller, service, repo, migration, component, route, config)
3. **Feature cũ nào có thể bị ảnh hưởng?** (tìm dependencies, shared services, shared DB tables)
4. **Breaking change có xảy ra không?** (API response shape thay đổi, DB schema thay đổi, contract thay đổi)

Tạo bảng phạm vi:

| Feature | Loại | Files bị ảnh hưởng | Feature cũ liên quan |
|---------|------|-------------------|---------------------|
| [Tên]   | NEW / MODIFIED / DELETED | [...] | [...] |

---

### PHASE 1 — Feature Completeness Check

Với MỖI feature mới, kiểm tra:

#### 1.1 Happy Path

- [ ] Chức năng chính hoạt động đúng theo spec?
- [ ] Tất cả required fields được validate?
- [ ] Response trả đúng shape và HTTP status code?
- [ ] Data được lưu/cập nhật/xóa đúng trong DB?

#### 1.2 Error Path

- [ ] Input không hợp lệ → trả 400 với message rõ ràng?
- [ ] Resource không tồn tại → trả 404?
- [ ] Unauthorized → trả 401? Forbidden → trả 403?
- [ ] Server error → KHÔNG expose stack trace ra ngoài?

#### 1.3 Boundary Cases

- [ ] Empty string / null / undefined inputs?
- [ ] Số âm, số 0, số rất lớn?
- [ ] String quá dài (vượt DB column limit)?
- [ ] Duplicate data (unique constraint violations)?
- [ ] Concurrent requests cùng lúc?

Kết quả:

| Feature | Happy Path | Error Path | Boundary | Verdict |
|---------|-----------|-----------|----------|---------|
| [Tên]   | PASS/FAIL | PASS/FAIL | PASS/FAIL | ✅/❌ |

---

### PHASE 2 — Regression Check (Feature Cũ Còn Hoạt Động Không?)

Đây là bước **quan trọng nhất** mà hầu hết audit prompt bỏ qua.

#### 2.1 Kiểm tra shared dependencies

Với mỗi file đã sửa, tìm tất cả chỗ khác đang dùng file đó:

```
Câu hỏi cần trả lời với từng thay đổi:
- Service này bị sửa → những endpoint nào khác đang inject service này?
- DB table bị thêm column → query cũ có bị break không?
- Shared utility bị sửa logic → ai khác đang gọi utility này?
- Config bị thay đổi → môi trường nào khác bị ảnh hưởng?
```

#### 2.2 Regression test list

Liệt kê TẤT CẢ feature cũ có thể bị ảnh hưởng và verify từng cái:

| Feature Cũ | Lý do nghi ngờ bị ảnh hưởng | Status |
|-----------|---------------------------|--------|
| [Tên feature] | [Service X được share / Table Y bị thay đổi] | PASS/FAIL/NOT CHECKED |

#### 2.3 Database Migration Safety

Nếu có migration mới:

- [ ] Migration UP chạy được không?
- [ ] Migration DOWN (rollback) có safe không?
- [ ] Migration có idempotent không (chạy 2 lần không bị lỗi)?
- [ ] Column mới có DEFAULT value không (để không break existing rows)?
- [ ] Index mới có gây lock table quá lâu trên production data lớn không?
- [ ] Foreign key mới có cascade delete không mong muốn không?

---

### PHASE 3 — API Contract Verification

Với MỖI endpoint mới hoặc bị sửa đổi:

#### 3.1 Request Contract

| Endpoint | Method | Auth Required | Request Body | Query Params | Frontend gửi đúng chưa? |
|---------|--------|--------------|-------------|-------------|----------------------|

#### 3.2 Response Contract

| Endpoint | Response Shape | HTTP Status | Frontend đọc đúng field không? | Breaking change với client cũ? |
|---------|--------------|------------|-------------------------------|-------------------------------|

**Mỗi field trong response phải verify:**

- Tên field: đúng chính xác (case-sensitive)?
- Kiểu dữ liệu: string/number/boolean/object/array?
- Nullable: có thể null không, frontend handle null chưa?
- Nested object: frontend access đúng path chưa (vd: `data.user.id` vs `data.userId`)?

#### 3.3 Breaking Change Detection

```
So sánh TRƯỚC và SAU:

TRƯỚC: { "userId": 1, "userName": "John" }
SAU:   { "id": 1, "user": { "name": "John" } }

→ Breaking change! Field rename + restructure.
→ Client cũ sẽ bị undefined ở userId và userName.
```

---

### PHASE 4 — Security Spot-Check

Tập trung vào feature mới, không audit lại toàn bộ hệ thống:

#### 4.1 Authentication & Authorization

- [ ] Endpoint mới có yêu cầu auth không? Có bị bỏ sót `@AuthGuard` / `@Secured` không?
- [ ] Role-based access đúng không? (Admin endpoint có ai dùng được không?)
- [ ] JWT/token validation có được áp dụng không?

#### 4.2 Ownership / IDOR

- [ ] User A có thể xem/sửa/xóa data của User B không?
- [ ] ID trong request có được kiểm tra ownership không?

```java
// SAI — IDOR vulnerability
Order order = orderRepo.findById(id); // Không kiểm tra owner
return order;

// ĐÚNG
Order order = orderRepo.findByIdAndUserId(id, currentUser.getId());
if (order == null) throw new ForbiddenException();
```

#### 4.3 Input Injection

- [ ] Query có dùng parameterized statement không (không string concatenation)?
- [ ] Input có được sanitize trước khi render HTML không?
- [ ] File upload có validate type/size/extension không?

#### 4.4 Sensitive Data

- [ ] Response có leak password hash, token, PII không cần thiết không?
- [ ] Log có ghi sensitive data (password, credit card, token) không?

Severity Matrix:

| Loại lỗi | Severity | Cần fix trước deploy? |
|---------|---------|----------------------|
| Missing auth trên endpoint | CRITICAL | YES |
| IDOR | HIGH | YES |
| SQL injection | CRITICAL | YES |
| Sensitive data in response | HIGH | YES |
| Missing input validation | MEDIUM | RECOMMEND |
| Verbose error message | LOW | OPTIONAL |

---

### PHASE 5 — Frontend Integration Spot-Check

Chỉ kiểm tra phần frontend liên quan đến feature mới:

#### 5.1 API Call Correctness

- [ ] URL đúng không? (typo trong path, missing slash)
- [ ] Method đúng không? (POST thay vì PUT, GET thay vì DELETE)
- [ ] Headers có đính kèm auth token không?
- [ ] Request body match với backend DTO không?

#### 5.2 Response Handling

- [ ] Frontend đọc đúng field name từ response?
- [ ] Loading state được handle (spinner, disable button)?
- [ ] Error state được handle (show message, không crash)?
- [ ] Empty state được handle (empty list, no data)?
- [ ] Null/undefined được guard (optional chaining `?.`)?

#### 5.3 UX Completeness

- [ ] Form validation có chạy ở client-side trước khi gửi request?
- [ ] Sau khi thành công có feedback cho user (toast, redirect, refresh data)?
- [ ] Double-submit được prevent (button disable khi đang loading)?

---

### PHASE 6 — Performance Quick-Check

Chỉ tập trung vào vấn đề có thể gây chậm ngay:

- [ ] **N+1 Query**: Vòng lặp có trigger query riêng lẻ không? (Dùng JOIN hoặc `findAll` thay vì loop `findById`)
- [ ] **Pagination**: Endpoint trả list có phân trang không? (Không return toàn bộ 100k records)
- [ ] **Missing Index**: Column được dùng trong WHERE/JOIN có index không?
- [ ] **Duplicate API Call**: Component mount xong có gọi API 2 lần không? (React StrictMode, useEffect deps sai)
- [ ] **Large Payload**: Response có trả về field thừa không cần thiết không?

```sql
-- Phát hiện N+1: query dạng này trong loop là red flag
SELECT * FROM orders WHERE user_id = ? -- chạy trong vòng lặp 1000 lần

-- Fix:
SELECT o.*, u.name FROM orders o JOIN users u ON o.user_id = u.id
WHERE u.id IN (...)
```

---

### PHASE 7 — Test Coverage Check

- [ ] Feature mới có unit test không?
- [ ] Happy path được test chưa?
- [ ] Error path (exception, validation fail) được test chưa?
- [ ] Test có mock dependencies đúng cách không (không test thật DB/API ngoài)?
- [ ] Nếu là bug fix: có regression test để prevent bug quay lại không?

| Feature | Unit Test | Integration Test | Edge Case Test | Coverage Estimate |
|---------|-----------|-----------------|----------------|-------------------|
| [Tên]   | YES/NO    | YES/NO          | YES/NO         | ~X% |

---

## OUTPUT FORMAT

### Feature Check Summary

| Feature | Completeness | Regression Risk | Security | Performance | Test | Final |
|---------|-------------|----------------|---------|------------|------|-------|
| [Tên] | ✅/⚠️/❌ | ✅/⚠️/❌ | ✅/⚠️/❌ | ✅/⚠️/❌ | ✅/⚠️/❌ | SHIP/HOLD |

Legend: ✅ Pass | ⚠️ Warning (cần fix nhưng không block) | ❌ Fail (phải fix trước deploy)

---

### Critical Issues (PHẢI FIX TRƯỚC KHI DEPLOY)

Mỗi issue phải có:

```
[CRITICAL] Tên issue
File: path/to/file.ts, line: XX
Evidence: [đoạn code thực tế gây lỗi]
Impact: [Ai/cái gì bị ảnh hưởng]
Fix: [Code fix cụ thể, không chung chung]
```

---

### Regression Warnings (Nên kiểm tra kỹ trước deploy)

```
[REGRESSION RISK] Tên warning
Feature cũ có thể bị ảnh hưởng: [tên feature]
Lý do: [giải thích cụ thể]
Cần test: [test case cần verify]
```

---

### API Contract Issues

| Endpoint | Issue | Frontend Impact | Fix |
|---------|-------|----------------|-----|
| POST /api/v1/... | Field `userId` rename thành `id` | Frontend crash khi đọc `response.userId` | Revert hoặc update frontend |

---

### Go/No-Go Decision

```
SHIP ✅ — Tất cả critical issues resolved, regression risk thấp
HOLD ❌ — Còn [N] critical issues cần fix:
  1. [Issue 1]
  2. [Issue 2]
CONDITIONAL ⚠️ — Có thể ship nếu:
  1. [Điều kiện 1]
  2. [Điều kiện 2]
```

---

## CHECKLIST NHANH (dùng trước mỗi merge)

```
PRE-MERGE CHECKLIST

Feature Logic
[ ] Feature hoạt động đúng theo spec
[ ] Validation đầy đủ (required, type, length, format)
[ ] Error message rõ ràng, không expose internals
[ ] Transaction được dùng cho multi-step DB operations

Regression
[ ] Tìm và kiểm tra tất cả shared dependencies
[ ] Migration safe (UP/DOWN/idempotent)
[ ] Không có field rename trong API response

Security
[ ] Auth/authz đúng trên tất cả endpoint mới
[ ] IDOR không xảy ra (kiểm tra ownership)
[ ] Không có sensitive data trong response/log

Frontend
[ ] API URL, method, headers đúng
[ ] Tất cả response fields được đọc đúng tên
[ ] Loading/error/empty state được handle

Performance
[ ] Không có N+1 query
[ ] List endpoint có pagination
[ ] Không có duplicate API call

Tests
[ ] Unit test cho business logic
[ ] Test cover error case
```

---

## IMPORTANT RULES

1. **VERIFY, ĐỪNG TIN** — Đừng tin comment, đừng tin tên file, đọc code thực tế.
2. **FOCUS VÀO CÁI MỚI** — Audit toàn bộ hệ thống tốn thời gian; tập trung vào diff.
3. **REGRESSION TRƯỚC** — Feature mới chạy tốt nhưng phá feature cũ = không ship được.
4. **EVIDENCE CHO MỌI FINDING** — Mỗi issue phải có file path + code snippet.
5. **FIX PHẢI CỤ THỂ** — "Thêm validation" là vô nghĩa. "Thêm `@NotBlank` vào field `email` ở `CreateUserDto.java:15`" mới có giá trị.
6. **NO-GO NẾU CÒN CRITICAL** — Không có exception. Critical = block deploy.
