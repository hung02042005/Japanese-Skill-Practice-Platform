# TC-UC-24 — Test Cases: Quản Lý Ngân Hàng Câu Hỏi (Manage Question Bank)

> **Feature:** `feat-content-management` | **UC:** UC-24 | **Version:** 1.0
> **Nguồn AC:** UC-24 § 8 (AC-24-01..13) | **Nguồn FR:** UC-24 § 3 (FR-24-01..25), SPEC.md § 3.1
> **Actor:** Staff | **Cập nhật:** 2026-06-12

---

## 1. UNIT TESTS — QuestionService (JUnit 5 + Mockito)

> **File:** `QuestionServiceTest.java` | **Tag:** `@Tag("unit")`

### TC-U-24-01 — Tạo câu hỏi multiple_choice hợp lệ → draft
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-01 |
| **Tham chiếu** | AC-24-01, FR-24-01/02/06/09 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:** `currentStaffId = 5`; request multiple_choice đủ A/B/C/D + `correctOption='A'`, `skill=kanji`, `jlptLevel=N5`.
**Steps:** Gọi `service.create(req, 5L)`.
**Expected:**
- Bản ghi lưu với `status='draft'`, `created_by=5`, `created_at≈NOW()`.
- Client gửi `status='published'` → bị **bỏ qua**, vẫn `draft`.
- Trả `questionId` mới; SLF4J log `Staff 5 create question {id}`.

### TC-U-24-02 — Thiếu trường bắt buộc → VALIDATION_FAILED
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-02 |
| **Tham chiếu** | AC-24-02, FR-24-02 |
| **Loại** | Unit — Validation |
| **Ưu tiên** | P0 |

**Steps:** Gọi `service.create(req thiếu skill, 5L)`.
**Expected:** Ném `ValidationException` (400 `VALIDATION_FAILED`); repository `save` KHÔNG được gọi.

### TC-U-24-03 — multiple_choice thiếu đáp án → MISSING_OPTIONS
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-03 |
| **Tham chiếu** | AC-24-03, FR-24-06 |
| **Loại** | Unit — Validation |
| **Ưu tiên** | P0 |

**Steps:** Request `questionType=multiple_choice`, thiếu `optionD`.
**Expected:** 400 `MISSING_OPTIONS`; rollback (không `save`).

### TC-U-24-04 — JLPT level ngoài tập → INVALID_JLPT_LEVEL
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-04 |
| **Tham chiếu** | AC-24-04, FR-24-05 |
| **Loại** | Unit — Validation |
| **Ưu tiên** | P1 |

**Steps:** `jlptLevel='N6'`.
**Expected:** 400 `INVALID_JLPT_LEVEL`.

### TC-U-24-05 — Cập nhật câu đã làm bài → QUESTION_LOCKED
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-05 |
| **Tham chiếu** | AC-24-09, FR-24-17, LESSON-005 |
| **Loại** | Unit — Service (Correctness) |
| **Ưu tiên** | P0 |

**Setup:**
```java
when(questionRepository.findById(200L)).thenReturn(Optional.of(aQuestion().withId(200L).withCreatedBy(5L).withStatus("draft").build()));
when(questionRepository.existsAttemptAnswerByQuestionId(200L)).thenReturn(true);
```
**Steps:** Gọi `service.update(200L, req, 5L)`.
**Expected:** Ném `QuestionLockedException` (409 `QUESTION_LOCKED`); `save` KHÔNG được gọi; dữ liệu không đổi.

### TC-U-24-06 — Cập nhật khi status=pending_review → INVALID_STATUS_TRANSITION
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-06 |
| **Tham chiếu** | AC-24-10, FR-24-18 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:** Câu 105 `status='pending_review'`, chưa khóa.
**Steps:** `service.update(105L, req, 5L)`.
**Expected:** 409 `INVALID_STATUS_TRANSITION`; không `save`.

### TC-U-24-07 — Cập nhật câu draft hợp lệ → refresh updated_at
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-07 |
| **Tham chiếu** | AC-24-08, FR-24-16/19 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Setup:** Câu 105 `draft`, `existsAttemptAnswer=false`, owner=5.
**Steps:** `service.update(105L, req, 5L)`.
**Expected:** `save` được gọi; `updated_at` mới; status vẫn `draft`.

### TC-U-24-08 — Không phải chủ sở hữu → FORBIDDEN
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-08 |
| **Tham chiếu** | AC-24-13, FR-24-24 |
| **Loại** | Unit — Service (Security) |
| **Ưu tiên** | P0 |

**Setup:** Câu 105 `created_by=5`; caller `staffId=9`, role `STAFF`.
**Steps:** `service.update(105L, req, 9L)`.
**Expected:** Ném `OwnershipDeniedException` (403 `FORBIDDEN`); không `save`.

### TC-U-24-09 — STAFF_MANAGER bỏ qua ownership guard
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-09 |
| **Tham chiếu** | FR-24-24 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P2 |

**Setup:** Câu 105 `created_by=5`, `draft`; caller role `STAFF_MANAGER`.
**Expected:** Update thành công (guard bỏ qua).

### TC-U-24-10 — Gửi duyệt câu draft → pending_review
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-10 |
| **Tham chiếu** | AC-24-11, FR-24-20/21 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P0 |

**Setup:** Câu 105 `draft`, đủ trường, owner=5.
**Steps:** `submissionService.submitForReview(new SubmitReviewRequest("question",105L), 5L)`.
**Expected:** `status='pending_review'`; log `submit`.

### TC-U-24-11 — Gửi duyệt khi status=published → INVALID_STATUS_TRANSITION
| Thuộc tính | Nội dung |
|:---|:---|
| **ID** | TC-U-24-11 |
| **Tham chiếu** | FR-24-22 |
| **Loại** | Unit — Service |
| **Ưu tiên** | P1 |

**Steps:** submit câu `status='published'`.
**Expected:** 409 `INVALID_STATUS_TRANSITION`; status không đổi.

---

## 2. INTEGRATION TESTS — Repository & DB Layer

> **File:** `QuestionRepositoryIT.java` | **Tag:** `@Tag("integration")` | **Stack:** Testcontainers (SQL Server) + Flyway

### TC-I-24-01 — isLocked qua tồn tại trong attempt_answers
| **ID** | TC-I-24-01 · **Tham chiếu** AC-24-07, FR-24-15 · **Ưu tiên** P0 |
|:---|:---|

**Steps:** Seed câu 200 + 1 bản ghi `attempt_answers(question_id=200)`; câu 105 không có.
**Expected:** `existsAttemptAnswerByQuestionId(200)=true`, `(105)=false`.

### TC-I-24-02 — Lọc AND theo skill + jlptLevel, loại deleted
| **ID** | TC-I-24-02 · **Tham chiếu** AC-24-05, FR-24-12/13 · **Ưu tiên** P0 |
|:---|:---|

**Steps:** Seed 342 câu (40 câu kanji/N5, 3 câu `status='deleted'`); gọi `findWithFilters(null,"kanji","N5",null,null, page0/20)`.
**Expected:** Chỉ câu kanji N5 không `deleted`; phân trang đúng, sort `updated_at` desc.

### TC-I-24-03 — Tìm kiếm theo từ khóa (LIKE, không phân biệt hoa thường)
| **ID** | TC-I-24-03 · **Tham chiếu** AC-24-06, FR-24-11 · **Ưu tiên** P1 |
|:---|:---|

**Steps:** Seed câu chứa "水"; gọi `findWithFilters(q="水",...)`.
**Expected:** Kết quả chứa câu có "水" trong `question_text`.

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `StaffQuestionControllerTest.java` | **Tag:** `@Tag("api")` | `@WebMvcTest` + Spring Security test

### TC-A-24-01 — POST /api/staff/questions — STAFF → 201
| **ID** | TC-A-24-01 · **Tham chiếu** AC-24-01 · **Ưu tiên** P0 |
|:---|:---|

**Request:** body multiple_choice hợp lệ + `@WithMockUser(authorities="STAFF")`.
**Expected:** `HTTP 201`, `{ status:201, data:{ questionId, status:"draft" } }`.

### TC-A-24-02 — POST — không JWT → 401
| **ID** | TC-A-24-02 · **Tham chiếu** NFR-24-02 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 401 UNAUTHORIZED`; service KHÔNG được gọi.

### TC-A-24-03 — POST — role STUDENT → 403
| **ID** | TC-A-24-03 · **Tham chiếu** NFR-24-02 · **Ưu tiên** P0 |
|:---|:---|

**Request:** `@WithMockUser(authorities="STUDENT")`.
**Expected:** `HTTP 403 FORBIDDEN`.

### TC-A-24-04 — PUT — câu đã làm bài → 409 QUESTION_LOCKED
| **ID** | TC-A-24-04 · **Tham chiếu** AC-24-09 · **Ưu tiên** P0 |
|:---|:---|

**Mock:** `service.update(...)` ném `QuestionLockedException`.
**Expected:** `HTTP 409`, message "Câu hỏi đã được học viên làm, không thể sửa trực tiếp".

### TC-A-24-05 — POST /contents/submit-review → 200 pending_review
| **ID** | TC-A-24-05 · **Tham chiếu** AC-24-11 · **Ưu tiên** P0 |
|:---|:---|

**Request:** `{ "contentType":"question", "contentId":105 }`.
**Expected:** `HTTP 200`, `data:{ status:"pending_review" }`.

### TC-A-24-06 — Cố đặt status=published → 403 PUBLISH_NOT_ALLOWED
| **ID** | TC-A-24-06 · **Tham chiếu** AC-24-12, FR-24-23 · **Ưu tiên** P0 |
|:---|:---|

**Expected:** `HTTP 403 PUBLISH_NOT_ALLOWED`; status không đổi.

### TC-A-24-07 — GET /{id} — không tồn tại → 404
| **ID** | TC-A-24-07 · **Tham chiếu** FR-24-14 · **Ưu tiên** P1 |
|:---|:---|

**Expected:** `HTTP 404 QUESTION_NOT_FOUND`.

### TC-A-24-08 — Response chỉ DTO, không lộ Entity
| **ID** | TC-A-24-08 · **Tham chiếu** NFR-24-04, ADR-005 · **Ưu tiên** P0 |
|:---|:---|

**Steps:** GET /{id}; parse đệ quy JSON.
**Expected:** Có `questionId`, `isLocked`, `status`...; KHÔNG có `hibernateLazyInitializer`/field nhạy cảm ngoài DTO.

---

## 4. TEST DATA SUMMARY
| Fixture | questionId | created_by | status | attempt_answers? |
|:---|:---|:---|:---|:---|
| `draftOwn` | 105 | 5 | draft | không |
| `lockedQuestion` | 200 | 5 | draft | có (đã làm bài) |
| `pendingOwn` | 106 | 5 | pending_review | không |
| `publishedOwn` | 107 | 5 | published | có |
| `byOther` | 108 | 9 | draft | không |
| `missing` | 999 | — | (không tồn tại) | — |

---

## 5. COVERAGE CHECKLIST (Rule nghiệp vụ UC-24)
| Rule/FR | Mô tả | Test Case | Covered? |
|:---|:---|:---|:---|
| FR-24-01/02 | Create → draft + created_by, bỏ qua status client | TC-U-24-01, TC-A-24-01 | ✅ |
| FR-24-06 | multiple_choice đủ option | TC-U-24-03 | ✅ |
| FR-24-05 | JLPT level hợp lệ | TC-U-24-04 | ✅ |
| FR-24-15/17 | isLocked + chặn sửa câu đã làm | TC-U-24-05, TC-I-24-01, TC-A-24-04 | ✅ |
| FR-24-18 | Update chỉ khi draft/rejected | TC-U-24-06/07 | ✅ |
| FR-24-24 | Ownership guard | TC-U-24-08/09, TC-A-24-03 | ✅ |
| FR-24-20/22 | Submit-review transition | TC-U-24-10/11, TC-A-24-05 | ✅ |
| FR-24-23 | Cấm Staff tự publish | TC-A-24-06 | ✅ |
| FR-24-11/12/13 | Search/filter/loại deleted | TC-I-24-02/03 | ✅ |
| NFR-24-02 | JWT + role | TC-A-24-02/03 | ✅ |
| ADR-005 | Không lộ Entity | TC-A-24-08 | ✅ |
