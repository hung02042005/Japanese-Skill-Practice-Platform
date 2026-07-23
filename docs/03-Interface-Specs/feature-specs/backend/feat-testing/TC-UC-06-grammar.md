# TC-UC-06 (Grammar) — Test Cases: Học Ngữ Pháp

> **Feature:** `feat-core-learning` | **UC:** UC-06 (Learn Grammar) | **Version:** 1.0
> **Nguồn AC:** UC-06-grammar § 8 (AC-06-01…08) | **Nguồn BR:** § 4 (BR-06-01…07)
> **Quy trình:** xem `TEST-PROCESS-COVERAGE.md`
> **⚠ Lưu ý:** Có 2 UC-06 khác nhau — file này cho **Ngữ pháp** (core-learning). Xem `TC-UC-06-staff-reset-password.md` cho UC-06 auth.
> **Cập nhật:** 2026-07-21

---

## 1. UNIT TESTS — GrammarService `@Tag("unit")`

### TC-U-06G-01 — List chỉ trả published, is_deleted=0
| **Tham chiếu** AC-06-01, BR-06-01 | **Ưu tiên** P1 |
**Setup:** N3 có 2 published + 1 draft + 1 deleted. **Expected:** trả 2 bản published.

### TC-U-06G-02 — Detail đầy đủ trường
| **Tham chiếu** AC-06-02, BR-06-06 | **Ưu tiên** P1 |
**Expected:** `structure, formula, meaning, usageExplanation, exampleSentenceJp, exampleSentenceVi, progressStatus` đều có; ví dụ JP+VI trả cùng nhau.

### TC-U-06G-03 — Level ngoài {N5..N1} → LevelMismatchException (422)
| **Tham chiếu** AC-06-05 | **Ưu tiên** P1 |

### TC-U-06G-04 — Detail nội dung draft/deleted → ContentNotFoundException (404)
| **Tham chiếu** AC-06-06, BR-06-01 | **Ưu tiên** P1 |

### TC-U-06G-05 — Đánh dấu completed lần đầu → tạo progress, set completed_at
| **Tham chiếu** AC-06-03, BR-06-02 | **Ưu tiên** P1 |

### TC-U-06G-06 — Đánh dấu lại → upsert, KHÔNG duplicate
| **Tham chiếu** AC-06-04, BR-06-02 | **Ưu tiên** P1 |
**Expected:** vẫn 1 bản ghi (UNIQUE student+type+content).

### TC-U-06G-07 — Hạ tiến độ (completed→learning) → ProgressRegressionException (422)
| **Tham chiếu** AC-06-08, BR-06-03 | **Ưu tiên** P1 |
**Setup:** progress hiện tại completed/100; gửi learning/20. **Expected:** ném exception; DB không đổi.

### TC-U-06G-08 — VIP-only + FREE → VipRequiredException (403)
| **Tham chiếu** AC-06-07, BR-06-05 | **Ưu tiên** P0 |

### TC-U-06G-09 — Mọi lượt xem cập nhật last_activity_date
| **Tham chiếu** BR-06-04 | **Ưu tiên** P2 |

### TC-U-06G-10 — progressPercent ngoài [0,100] / status sai → ValidationException (400)
| **Tham chiếu** UC-06 §3.5 | **Ưu tiên** P1 |

---

## 2. INTEGRATION TESTS `@Tag("integration")`

### TC-I-06G-01 — UNIQUE(student_id, content_type, content_id) chặn duplicate
| **Tham chiếu** AC-06-04, BR-06-02 | **Ưu tiên** P1 |

### TC-I-06G-02 — List gắn cờ isCompleted đúng theo student
| **Tham chiếu** AC-06-01 | **Ưu tiên** P2 |

---

## 3. API / CONTROLLER TESTS `@Tag("api")`

### TC-A-06G-01 — GET list N3 → 200, không chứa draft | AC-06-01 · P1
### TC-A-06G-02 — GET detail → 200 đầy đủ trường | AC-06-02 · P1
### TC-A-06G-03 — GET list level=N9 → 422 LEVEL_MISMATCH | AC-06-05 · P1
### TC-A-06G-04 — GET detail draft → 404 CONTENT_NOT_FOUND | AC-06-06 · P1
### TC-A-06G-05 — GET detail VIP + FREE → 403 VIP_REQUIRED | AC-06-07 · P0
### TC-A-06G-06 — POST progress hạ tiến độ → 422 PROGRESS_REGRESSION | AC-06-08 · P1
### TC-A-06G-07 — POST progress contentType sai → 400 VALIDATION_FAILED | UC-06 §3.5 · P1
### TC-A-06G-08 — Response theo chuẩn {status,message,data}, không Entity JPA | BR-06-07 · P1
### TC-A-06G-09 — Không JWT → 401 | P0

---

## 4. FRONTEND COMPONENT TESTS

### TC-F-06G-01 — Đổi dropdown level gọi lại API đúng param | AC-06-01 · P2
### TC-F-06G-02 — Nút "Đánh dấu đã học" → POST learning-progress | AC-06-03 · P1
### TC-F-06G-03 — Nội dung VIP hiển thị khối khoá khi 403 | AC-06-07 · P2

---

## 5. TEST DATA SUMMARY

| Fixture | level | status | is_vip_only |
|:---|:---|:---|:---:|
| `grammarN3Pub` ×2 | N3 | published | 0 |
| `grammarN3Draft` | N3 | draft | 0 |
| `grammarVip` | N3 | published | 1 |
| `progressCompleted` | — | — | student 7, completed/100 |

---

## 6. COVERAGE CHECKLIST

| Nguồn | TC | ✔ |
|:---|:---|:---:|
| AC-06-01 | TC-U-06G-01, TC-A-06G-01 | ✅ |
| AC-06-02 | TC-U-06G-02, TC-A-06G-02 | ✅ |
| AC-06-03 | TC-U-06G-05, TC-F-06G-02 | ✅ |
| AC-06-04 | TC-U-06G-06, TC-I-06G-01 | ✅ |
| AC-06-05 | TC-U-06G-03, TC-A-06G-03 | ✅ |
| AC-06-06 | TC-U-06G-04, TC-A-06G-04 | ✅ |
| AC-06-07 | TC-U-06G-08, TC-A-06G-05 | ✅ |
| AC-06-08 | TC-U-06G-07, TC-A-06G-06 | ✅ |
| BR-06-01 | TC-U-06G-01/04 | ✅ |
| BR-06-02 | TC-U-06G-06, TC-I-06G-01 | ✅ |
| BR-06-03 | TC-U-06G-07 | ✅ |
| BR-06-04 | TC-U-06G-09 | ✅ |
| BR-06-05 | TC-U-06G-08 | ✅ |
| BR-06-06 | TC-U-06G-02 | ✅ |
| BR-06-07 | TC-A-06G-08 | ✅ |
