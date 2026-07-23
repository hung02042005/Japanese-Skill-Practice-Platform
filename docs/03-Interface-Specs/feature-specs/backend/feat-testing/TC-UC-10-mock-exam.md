# TC-UC-10 — Test Cases: Thi Thử JLPT (Mock Exam)

> **Feature:** `feat-mock-test` | **UC:** UC-10 | **Version:** 1.0
> **Nguồn AC:** UC-10 § 8 (AC-10-01…13) | **Nguồn BR:** UC-10 § 4 (BR-10-01…13)
> **Quy trình:** xem `TEST-PROCESS-COVERAGE.md`
> **Cập nhật:** 2026-07-21

---

## 1. UNIT TESTS — MockExamService `@Tag("unit")`

### TC-U-10-01 — Start exam: server ghi started_at, tính expiresAt
| **Tham chiếu** AC-10-03, BR-10-01, BR-10-02 | **Ưu tiên** P0 |
**Setup:** `durationMin=60`, fixed clock. **Expected:** `startedAt=serverNow`; `expiresAt=startedAt+60m`; client timestamp (nếu gửi) bị bỏ qua.

### TC-U-10-02 — Start exam ẩn correctOption, group theo section
| **Tham chiếu** AC-10-02, BR-10-06 | **Ưu tiên** P0 |
**Expected:** mỗi `ExamQuestionResponse` không có `correctOption`; sections nhóm đúng.

### TC-U-10-03 — VIP-only + student FREE → VipRequiredException
| **Tham chiếu** AC-10-11, BR-10-11 | **Ưu tiên** P0 |
**Setup:** exam `isVipOnly=true`, `subscriptionService.hasActiveVip()=false`. **Expected:** ném exception; không tạo attempt.

### TC-U-10-04 — Manual submit trong giờ → tính điểm, status='submitted'
| **Tham chiếu** AC-10-06, BR-10-03, BR-10-05 | **Ưu tiên** P1 |
**Setup:** NOW ≤ expiresAt. **Expected:** score tính server-side per-section.

### TC-U-10-05 — Manual submit sau giờ → TimeExceededException (boundary)
| **Tham chiếu** AC-10-05, BR-10-03 | **Ưu tiên** P0 |
**Setup:** NOW = expiresAt + 1s, `isAutoSubmit=false`. **Expected:** ném exception; attempt vẫn `in_progress`; không persist answers.
> Đối chứng biên: NOW = expiresAt (đúng hạn) → được nộp.

### TC-U-10-06 — Auto-submit sau giờ → bỏ qua time check, status='auto_submitted'
| **Tham chiếu** AC-10-04, BR-10-04 | **Ưu tiên** P1 |
**Setup:** NOW=61 phút, `isAutoSubmit=true`. **Expected:** không ném; `status='auto_submitted'`; điểm lưu.

### TC-U-10-07 — Tính điểm 3 section đúng
| **Tham chiếu** AC-10-06 | **Ưu tiên** P1 |
**Setup:** language đúng 20, reading 30, listening 20. **Expected:** `languageKnowledge=20, reading=30, listening=20, totalScore=70`.

### TC-U-10-08 — isPassed = (totalScore ≥ passScore) — biên
| **Tham chiếu** AC-10-07, AC-10-08, BR-10-09 | **Ưu tiên** P1 |
**Setup:** passScore=90. Kiểm: total=90→pass=true; total=89→false; total=95→true; total=80→false.

### TC-U-10-09 — Tất cả sai → totalScore=0, isPassed=false
| **Tham chiếu** AC-10-13, BR-10-08 | **Ưu tiên** P1 |

### TC-U-10-10 — totalScore ngoài [0,maxScore] → BusinessRuleViolationException
| **Tham chiếu** BR-10-08 | **Ưu tiên** P0 |
**Expected:** log [ERROR]; không persist.

### TC-U-10-11 — Nộp lại attempt submitted → AttemptAlreadySubmittedException (422)
| **Tham chiếu** AC-10-10, BR-10-07 | **Ưu tiên** P0 |

### TC-U-10-12 — Attempt của người khác → ForbiddenException (403)
| **Tham chiếu** BR-10-10, UC-10 §3.7 | **Ưu tiên** P0 (authz) |

### TC-U-10-13 — Bản ghi mới mỗi lần thi
| **Tham chiếu** AC-10-09, BR-10-07 | **Ưu tiên** P1 |

### TC-U-10-14 — Audit log EXAM_SUBMITTED
| **Tham chiếu** BR-10-13 | **Ưu tiên** P1 |
**Expected:** `{studentId, assessmentId, attemptId, score, isPassed}`.

---

## 2. INTEGRATION TESTS `@Tag("integration")`

### TC-I-10-01 — @Transactional: answers + test_attempts persist cùng transaction
| **Tham chiếu** BR-10-12 | **Ưu tiên** P0 |
**Steps:** ép lỗi giữa saveAll answers và update attempt → rollback toàn bộ (0 answers persist).

### TC-I-10-02 — started_at persist đúng UTC server time
| **Tham chiếu** AC-10-03, BR-10-01 | **Ưu tiên** P1 |

### TC-I-10-03 — Review chỉ đọc được sau khi submitted, có correctOption + explanation
| **Tham chiếu** AC-10-12, BR-10-14 | **Ưu tiên** P1 |

---

## 3. API / CONTROLLER TESTS `@Tag("api")`

### TC-A-10-01 — GET list exam N3 → chỉ published (2 bài), không draft/N2
| AC-10-01 · P1 |

### TC-A-10-02 — POST /start → 200, ẩn correctOption, có attemptId/startedAt/expiresAt
| AC-10-02, AC-10-03 · P0 |

### TC-A-10-03 — POST /start VIP-only + FREE → 403 VIP_REQUIRED
| AC-10-11 · P0 |

### TC-A-10-04 — POST /submit manual sau giờ → 400 TIME_EXCEEDED
| AC-10-05 · P0 |

### TC-A-10-05 — POST /submit auto sau giờ → 200 status=auto_submitted
| AC-10-04 · P1 |

### TC-A-10-06 — POST /submit đúng → 200 sectionScores + isPassed
| AC-10-06/07 · P1 |

### TC-A-10-07 — POST /submit lại → 422 ATTEMPT_ALREADY_SUBMITTED
| AC-10-10 · P0 |

### TC-A-10-08 — POST /submit attempt người khác → 403 FORBIDDEN
| BR-10-10 · P0 |

### TC-A-10-09 — GET /review → 200 correctOption + explanation + sectionScores
| AC-10-12 · P1 |

### TC-A-10-10 — GET /status → remainingSeconds = max(0, expiresAt-NOW)
| UC-10 §3.3 · P2 |

### TC-A-10-11 — Validation: isAutoSubmit thiếu → 400
| UC-10 §5 · P1 |

### TC-A-10-12 — Không JWT → 401
| P0 |

---

## 4. SECURITY INVARIANT TESTS `@Tag("security")`

### TC-S-10-01 — /start KHÔNG lộ correctOption (95 câu)
| AC-10-02, BR-10-06 · P0 |

### TC-S-10-02 — Client gửi score/started_at bị bỏ qua
| BR-10-01, BR-10-05 · P0 |

### TC-S-10-03 — VIP check real-time (không cache quá 5 phút)
| BR-10-11 · P1 | hủy VIP giữa chừng → start mới bị chặn.

---

## 5. FRONTEND COMPONENT TESTS

### TC-F-10-01 — Countdown timer tính từ expiresAt server
| BR-10-02 · P1 |

### TC-F-10-02 — Timer về 0 → auto POST submit isAutoSubmit=true
| AC-10-04 · P1 |

### TC-F-10-03 — Hiển thị điểm từng phần + đạt/không đạt
| AC-10-06/07 · P1 |

---

## 6. TEST DATA SUMMARY

| Fixture | type | level | status | durationMin | passScore | isVipOnly |
|:---|:---|:---|:---|:---:|:---:|:---:|
| `examN3` ×2 | exam | N3 | published | 60 | 90 | false |
| `examN3Draft` | exam | N3 | draft | — | — | — |
| `examN2` | exam | N2 | published | 105 | 95 | false |
| `examVip` | exam | N3 | published | 60 | 90 | true |
| `studentFree` | — | — | — | — | — | subscription=FREE |

---

## 7. COVERAGE CHECKLIST

| Nguồn | TC | ✔ |
|:---|:---|:---:|
| AC-10-01 | TC-A-10-01 | ✅ |
| AC-10-02 | TC-U-10-02, TC-A-10-02, TC-S-10-01 | ✅ |
| AC-10-03 | TC-U-10-01, TC-I-10-02 | ✅ |
| AC-10-04 | TC-U-10-06, TC-A-10-05, TC-F-10-02 | ✅ |
| AC-10-05 | TC-U-10-05, TC-A-10-04 | ✅ |
| AC-10-06 | TC-U-10-07, TC-A-10-06 | ✅ |
| AC-10-07 | TC-U-10-08 | ✅ |
| AC-10-08 | TC-U-10-08 | ✅ |
| AC-10-09 | TC-U-10-13 | ✅ |
| AC-10-10 | TC-U-10-11, TC-A-10-07 | ✅ |
| AC-10-11 | TC-U-10-03, TC-A-10-03 | ✅ |
| AC-10-12 | TC-I-10-03, TC-A-10-09 | ✅ |
| AC-10-13 | TC-U-10-09 | ✅ |
| BR-10-01 | TC-U-10-01, TC-S-10-02 | ✅ |
| BR-10-02 | TC-U-10-01, TC-F-10-01 | ✅ |
| BR-10-03 | TC-U-10-05 | ✅ |
| BR-10-04 | TC-U-10-06 | ✅ |
| BR-10-05 | TC-U-10-04, TC-S-10-02 | ✅ |
| BR-10-06 | TC-U-10-02, TC-S-10-01 | ✅ |
| BR-10-07 | TC-U-10-11/13 | ✅ |
| BR-10-08 | TC-U-10-10 | ✅ |
| BR-10-09 | TC-U-10-08 | ✅ |
| BR-10-10 | TC-U-10-12, TC-A-10-08 | ✅ |
| BR-10-11 | TC-S-10-03 | ✅ |
| BR-10-12 | TC-I-10-01 | ✅ |
| BR-10-13 | TC-U-10-14 | ✅ |
