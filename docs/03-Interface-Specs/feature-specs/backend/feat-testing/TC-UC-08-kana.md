# TC-UC-08 — Test Cases: Học Kana

> **Feature:** `feat-core-learning` | **UC:** UC-08 | **Version:** 1.0
> **Nguồn AC:** UC-08 § 8 (AC-08-01…06) | **Nguồn BR:** § 4 (BR-08-01…07)
> **Quy trình:** xem `TEST-PROCESS-COVERAGE.md`
> **Cập nhật:** 2026-07-21

---

## 1. UNIT TESTS — KanaService `@Tag("unit")`

### TC-U-08-01 — GET chart hiragana trả toàn bộ bảng (≥46), không phân trang
| **Tham chiếu** AC-08-01, BR-08-01 | **Ưu tiên** P1 |
**Expected:** ≥46 ký tự, order theo display_order, mỗi item có `audioUrl`.

### TC-U-08-02 — Toàn bộ item có audioUrl + strokeOrderUrl (ảnh tĩnh)
| **Tham chiếu** AC-08-02, BR-08-03 | **Ưu tiên** P1 |

### TC-U-08-03 — type ngoài {hiragana,katakana} → ValidationException (400)
| **Tham chiếu** AC-08-05 | **Ưu tiên** P1 |

### TC-U-08-04 — Đánh dấu completed → upsert progress (content_type='kana')
| **Tham chiếu** AC-08-04, BR-08-04 | **Ưu tiên** P1 |

### TC-U-08-05 — Upsert không duplicate
| **Tham chiếu** AC-08-06, BR-08-04 | **Ưu tiên** P1 |

### TC-U-08-06 — contentId không tồn tại → ContentNotFoundException (404)
| **Tham chiếu** UC-08 §3.4 | **Ưu tiên** P1 |

### TC-U-08-07 — progressPercent ngoài [0,100] / status sai → ValidationException (400)
| **Tham chiếu** UC-08 §3.5 | **Ưu tiên** P1 |

### TC-U-08-08 — Hạ tiến độ thủ công không được (chỉ tăng)
| **Tham chiếu** BR-08-05 | **Ưu tiên** P1 |

### TC-U-08-09 — Cập nhật last_activity_date khi đánh dấu
| **Tham chiếu** BR-08-06 | **Ưu tiên** P2 |

### TC-U-08-10 — Kana luôn công khai — không check VIP/published
| **Tham chiếu** BR-08-07 | **Ưu tiên** P2 |
**Expected:** không gọi subscription/VIP check; trả bảng cho mọi student active.

---

## 2. INTEGRATION TESTS `@Tag("integration")`

### TC-I-08-01 — UNIQUE progress chặn duplicate cho content_type='kana' | AC-08-06 · P1
### TC-I-08-02 — findByType order theo display_order | AC-08-01 · P2

---

## 3. API / CONTROLLER TESTS `@Tag("api")`

### TC-A-08-01 — GET /api/kana?type=hiragana → 200 danh sách đầy đủ | AC-08-01 · P1
### TC-A-08-02 — GET /api/kana?type=romaji → 400 VALIDATION_FAILED | AC-08-05 · P1
### TC-A-08-03 — POST progress kana completed → 200 | AC-08-04 · P1
### TC-A-08-04 — POST progress contentId sai → 404 CONTENT_NOT_FOUND | UC-08 §3.4 · P1
### TC-A-08-05 — Không JWT → 401 | P0

---

## 4. FRONTEND COMPONENT TESTS

### TC-F-08-01 — Phát audio từ audioUrl KHÔNG gọi API backend
| **Tham chiếu** AC-08-03, BR-08-02 | **Ưu tiên** P1 |
**Expected:** click phát → `new Audio(url).play()`; verify không có fetch/XHR mới tới backend.

### TC-F-08-02 — Chuyển tab hiragana↔katakana gọi GET đúng type | AC-08-01 · P2
### TC-F-08-03 — strokeOrderUrl render ảnh tĩnh | BR-08-03 · P2

---

## 5. TEST DATA SUMMARY

| Fixture | kana_type | ghi chú |
|:---|:---|:---|
| `hiraganaSet` | hiragana | ≥46 ký tự, đủ audio_url + stroke_order_url |
| `katakanaSet` | katakana | ≥46 ký tự |
| `progressKana` | — | student 7 đã học 1 ký tự |

---

## 6. COVERAGE CHECKLIST

| Nguồn | TC | ✔ |
|:---|:---|:---:|
| AC-08-01 | TC-U-08-01, TC-A-08-01 | ✅ |
| AC-08-02 | TC-U-08-02 | ✅ |
| AC-08-03 | TC-F-08-01 | ✅ |
| AC-08-04 | TC-U-08-04, TC-A-08-03 | ✅ |
| AC-08-05 | TC-U-08-03, TC-A-08-02 | ✅ |
| AC-08-06 | TC-U-08-05, TC-I-08-01 | ✅ |
| BR-08-01 | TC-U-08-01 | ✅ |
| BR-08-02 | TC-F-08-01 | ✅ |
| BR-08-03 | TC-U-08-02, TC-F-08-03 | ✅ |
| BR-08-04 | TC-U-08-04/05 | ✅ |
| BR-08-05 | TC-U-08-08 | ✅ |
| BR-08-06 | TC-U-08-09 | ✅ |
| BR-08-07 | TC-U-08-10 | ✅ |
