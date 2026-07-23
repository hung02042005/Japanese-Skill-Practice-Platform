# TC-UC-11 — Test Cases: Quiz & Luyện Tập

> **Feature:** `feat-assessment` | **UC:** UC-11 | **Version:** 1.0
> **Nguồn AC:** UC-11 § 8 (AC-11-01…08) | **Nguồn BR:** UC-11 § 4 (BR-11-01…08)
> **Quy trình:** xem `TEST-PROCESS-COVERAGE.md`
> **Cập nhật:** 2026-07-21

---

## 1. UNIT TESTS — AssessmentService `@Tag("unit")`

### TC-U-11-01 — Start quiz ẩn correctOption
| **Tham chiếu** AC-11-02, BR-11-01 | **Ưu tiên** P0 |
**Setup:** quiz 10 câu, mỗi câu có `correctOption`. **Steps:** `startAssessment(id, studentId)`.
**Expected:** `QuizStartResponse.questions[*]` KHÔNG có `correctOption`/`correctAnswerText`; có `optionA..D`; tạo `TestAttempt(status='in_progress')`.

### TC-U-11-02 — Tính điểm server-side đúng (8/10)
| **Tham chiếu** AC-11-03, BR-11-02 | **Ưu tiên** P0 |
**Setup:** 8 đáp án khớp `correctOption` DB, 2 sai. **Expected:** `score=8`, `maxScore=10`; điểm lấy từ DB, KHÔNG từ client.

### TC-U-11-03 — fill_blank so sánh case-insensitive + trim
| **Tham chiếu** BR-11-07 | **Ưu tiên** P1 |
**Setup:** correct=`"たべる"`; answer=`"  たべる "` và biến thể khác hoa/thường. **Expected:** isCorrect=true sau trim + equalsIgnoreCase.

### TC-U-11-04 — Tất cả sai → score=0 (không âm)
| **Tham chiếu** AC-11-06, BR-11-04 | **Ưu tiên** P1 |

### TC-U-11-05 — score < 0 hoặc > maxScore → BusinessRuleViolationException
| **Tham chiếu** BR-11-04, UC-11 §3.5 | **Ưu tiên** P0 |
**Expected:** ném exception; log [ERROR] score invariant; KHÔNG save attempt_answers, KHÔNG update test_attempts.

### TC-U-11-06 — Nộp lại attempt đã submitted → AttemptAlreadySubmittedException
| **Tham chiếu** AC-11-05, BR-11-05 | **Ưu tiên** P0 |
**Setup:** attempt `status='submitted'`. **Expected:** ném exception; bản ghi cũ không đổi.

### TC-U-11-07 — Nộp attempt của người khác → ForbiddenException (403)
| **Tham chiếu** BR-11-06 | **Ưu tiên** P0 (authz) |
**Setup:** `attempt.studentId=7`, JWT studentId=9.

### TC-U-11-08 — Mỗi lần nộp tạo attempt MỚI
| **Tham chiếu** AC-11-04, BR-11-03 | **Ưu tiên** P1 |
**Expected:** attemptId lần 2 ≠ lần 1; bản ghi cũ giữ nguyên.

### TC-U-11-09 — Ghi audit log QUIZ_SUBMITTED
| **Tham chiếu** BR-11-08 | **Ưu tiên** P1 |
**Expected:** `auditLog("QUIZ_SUBMITTED", studentId, {attemptId, assessmentId, score})` gọi 1 lần.

### TC-U-11-10 — Quiz không tồn tại/chưa published → AssessmentNotFoundException
| **Tham chiếu** UC-11 §3.4 | **Ưu tiên** P1 |

---

## 2. INTEGRATION TESTS — Repository `@Tag("integration")`

### TC-I-11-01 — Filter list: chỉ published, đúng level
| **Tham chiếu** AC-11-01 | **Ưu tiên** P1 |
**Setup:** 3 quiz N3 published, 1 N3 draft, 1 N2 published. **Expected:** query trả đúng 3.

### TC-I-11-02 — Submit lần 2 tạo bản ghi test_attempts riêng biệt (immutability)
| **Tham chiếu** AC-11-04, BR-11-03 | **Ưu tiên** P1 |
**Expected:** 2 row test_attempts; batch insert attempt_answers đủ số câu.

### TC-I-11-03 — @Transactional rollback khi score invariant vi phạm
| **Tham chiếu** BR-11-04 | **Ưu tiên** P1 |
**Expected:** không có attempt_answers nào persist sau exception.

---

## 3. API / CONTROLLER TESTS `@Tag("api")`

### TC-A-11-01 — POST /start → 200, response ẩn correctOption
| AC-11-02 · P0 | duyệt JSON: không key `correctOption`.

### TC-A-11-02 — POST /submit đúng 8/10 → 200 score=8, results[]
| AC-11-03, AC-11-07 · P0 | mỗi result có `isCorrect`, `correctOption` (sau nộp), `explanation`.

### TC-A-11-03 — POST /submit lại attempt submitted → 422 ATTEMPT_ALREADY_SUBMITTED
| AC-11-05 · P0 |

### TC-A-11-04 — POST /submit attempt người khác → 403
| BR-11-06 · P0 |

### TC-A-11-05 — Quiz không tồn tại → 404 ASSESSMENT_NOT_FOUND
| UC-11 §3.4 · P1 |

### TC-A-11-06 — Validation: answers rỗng → 400 VALIDATION_FAILED
| UC-11 §5 · P1 |

### TC-A-11-07 — Validation: selectedOption='E' → 400
| UC-11 §5 · P1 |

### TC-A-11-08 — Không JWT → 401 UNAUTHORIZED
| AC-11-08 · P0 |

### TC-A-11-09 — score invariant vi phạm → 422 SCORE_INVARIANT_VIOLATION
| UC-11 §3.5 · P1 |

---

## 4. SECURITY INVARIANT TESTS `@Tag("security")`

### TC-S-11-01 — GET/start KHÔNG lộ correctOption dưới mọi hình thức
| AC-11-02, BR-11-01 · P0 | duyệt đệ quy JSON toàn bộ questions.

### TC-S-11-02 — Client gửi score bị bỏ qua (điểm lấy từ DB)
| BR-11-02 · P0 | body submit thêm field `score=100` (giả mạo) → response score tính từ DB, không phải 100.

### TC-S-11-03 — Không nộp được attempt của người khác
| BR-11-06 · P0 |

---

## 5. FRONTEND COMPONENT TESTS

### TC-F-11-01 — Không hiển thị đáp án đúng trước khi nộp
| BR-11-01 · P1 |

### TC-F-11-02 — Sau nộp hiển thị điểm + đúng/sai + giải thích
| AC-11-03, AC-11-07 · P1 |

### TC-F-11-03 — Nộp lại attempt đã nộp → hiển thị "Bài đã được nộp"
| AC-11-05 · P2 |

---

## 6. TEST DATA SUMMARY

| Fixture | type | level | status |
|:---|:---|:---|:---|
| `quizN3Published` ×3 | quiz | N3 | published |
| `quizN3Draft` | quiz | N3 | draft |
| `quizN2Published` | quiz | N2 | published |
| `attemptInProgress` | — | — | in_progress (student 7) |
| `attemptSubmitted` | — | — | submitted (student 7) |

---

## 7. COVERAGE CHECKLIST

| Nguồn | TC | ✔ |
|:---|:---|:---:|
| AC-11-01 (list theo level) | TC-I-11-01 | ✅ |
| AC-11-02 (ẩn correctOption) | TC-U-11-01, TC-A-11-01, TC-S-11-01 | ✅ |
| AC-11-03 (tính điểm) | TC-U-11-02, TC-A-11-02 | ✅ |
| AC-11-04 (attempt mới) | TC-U-11-08, TC-I-11-02 | ✅ |
| AC-11-05 (chặn nộp lại) | TC-U-11-06, TC-A-11-03 | ✅ |
| AC-11-06 (score ≥ 0) | TC-U-11-04 | ✅ |
| AC-11-07 (giải thích) | TC-A-11-02 | ✅ |
| AC-11-08 (chặn không JWT) | TC-A-11-08 | ✅ |
| BR-11-01 | TC-U-11-01, TC-S-11-01 | ✅ |
| BR-11-02 | TC-U-11-02, TC-S-11-02 | ✅ |
| BR-11-03 | TC-U-11-08, TC-I-11-02 | ✅ |
| BR-11-04 | TC-U-11-05, TC-I-11-03, TC-A-11-09 | ✅ |
| BR-11-05 | TC-U-11-06 | ✅ |
| BR-11-06 | TC-U-11-07, TC-A-11-04, TC-S-11-03 | ✅ |
| BR-11-07 (fill_blank) | TC-U-11-03 | ✅ |
| BR-11-08 (audit) | TC-U-11-09 | ✅ |
