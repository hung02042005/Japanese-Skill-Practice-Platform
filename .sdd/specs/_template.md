# SPEC — [FEATURE_NAME]
>
> **Feature ID:** `feat-[id]`
> **UC Coverage:** UC-XX, UC-YY
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** YYYY-MM-DD

---

## 1. CONTEXT & GOAL

### 1.1 Bối cảnh

[Mô tả vấn đề / nhu cầu nghiệp vụ feature này giải quyết]

### 1.2 Mục tiêu

[Mục tiêu cụ thể, đo lường được]

### 1.3 Tại sao cần?

[Business value, lý do ưu tiên]

---

## 2. ACTOR

| Actor | Role | Điều kiện tiền quyết |
|:---|:---|:---|
| [Actor] | [Mô tả ngắn] | [Precondition] |

---

## 3. FUNCTIONAL REQUIREMENTS (EARS)

> **EARS Syntax:**
>
> - `WHEN [trigger] THE SYSTEM SHALL [behavior]`
> - `WHILE [state] THE SYSTEM SHALL [behavior]`
> - `IF [condition] THEN THE SYSTEM SHALL [response]`
> - `THE SYSTEM SHALL [ubiquitous requirement]`

### 3.1 [Sub-feature / Happy Path]

| ID | EARS Requirement |
|:---|:---|
| FR-[ID]-01 | WHEN ... THE SYSTEM SHALL ... |
| FR-[ID]-02 | IF ... THEN THE SYSTEM SHALL ... |

### 3.2 [Alternate / Edge Cases]

| ID | EARS Requirement |
|:---|:---|
| FR-[ID]-10 | WHEN ... THE SYSTEM SHALL ... |

---

## 4. NON-FUNCTIONAL REQUIREMENTS

| ID | Category | Requirement |
|:---|:---|:---|
| NFR-[ID]-01 | Performance | [e.g., API response < 500ms p95] |
| NFR-[ID]-02 | Security | [e.g., JWT required, bcrypt cost ≥ 10] |
| NFR-[ID]-03 | Availability | [e.g., uptime 99.5%] |
| NFR-[ID]-04 | Logging | [e.g., SLF4J, no System.out.println] |

---

## 5. DATA MODEL

### 5.1 Bảng chính

```sql
-- Tên bảng + các cột quan trọng
TABLE_NAME (
  column_name  TYPE         -- mô tả
)
```

### 5.2 Quan hệ

[Mermaid ER hoặc mô tả quan hệ giữa các bảng]

---

## 6. API SPEC

### `METHOD /api/[resource]`

**Request:**

```json
{
  "field": "type — mô tả"
}
```

**Response (200):**

```json
{
  "status": 200,
  "message": "...",
  "data": {}
}
```

---

## 7. ERROR HANDLING

| HTTP Code | Error Code | Message | Trigger |
|:---:|:---|:---|:---|
| 400 | VALIDATION_FAILED | "..." | Input không hợp lệ |
| 401 | UNAUTHORIZED | "..." | Thiếu/hết hạn JWT |
| 403 | FORBIDDEN | "..." | Không đủ quyền |
| 404 | NOT_FOUND | "..." | Resource không tồn tại |
| 409 | CONFLICT | "..." | Trùng lặp |
| 422 | BUSINESS_RULE_VIOLATION | "..." | Vi phạm nghiệp vụ |
| 500 | INTERNAL_ERROR | "Internal server error" | Lỗi hệ thống |

---

## 8. ACCEPTANCE CRITERIA

| ID | Scenario | Given | When | Then |
|:---|:---|:---|:---|:---|
| AC-[ID]-01 | [Tên kịch bản] | [Trạng thái ban đầu] | [Hành động] | [Kết quả mong đợi] |

---

## OUT OF SCOPE

- ❌ [Điều không làm trong feature này]
- ❌ [Boundary rõ ràng]
