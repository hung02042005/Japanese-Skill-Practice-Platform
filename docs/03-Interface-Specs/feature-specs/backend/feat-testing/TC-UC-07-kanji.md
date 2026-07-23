# TC-UC-07 — Test Cases: Học Kanji

> **Feature:** `feat-core-learning` | **UC:** UC-07 | **Version:** 1.0
> **Nguồn AC:** UC-07 § 8 (AC-07-01…04) | **Nguồn BR:** § 4 (BR-07-01…07)
> **Quy trình:** xem `TEST-PROCESS-COVERAGE.md`
> **Cập nhật:** 2026-07-21

---

## 1. UNIT TESTS — KanjiService `@Tag("unit")`

### TC-U-07-01 — Detail đầy đủ trường Kanji
| **Tham chiếu** AC-07-01, BR-07-01 | **Ưu tiên** P1 |
**Expected:** `characterValue, strokeCount, strokeOrderUrl, onyomi, kunyomi, meaning, exampleWord, exampleReading, exampleMeaning`.

### TC-U-07-02 — List chỉ published, is_deleted=0
| **Tham chiếu** BR-07-01 | **Ưu tiên** P1 |

### TC-U-07-03 — strokeOrderUrl là ảnh tĩnh (không animation field)
| **Tham chiếu** AC-07-04, BR-07-02 | **Ưu tiên** P1 |

### TC-U-07-04 — Đánh dấu completed → upsert progress (content_type='kanji')
| **Tham chiếu** AC-07-02, BR-07-03 | **Ưu tiên** P1 |

### TC-U-07-05 — Upsert không tạo duplicate
| **Tham chiếu** BR-07-03 | **Ưu tiên** P1 |

### TC-U-07-06 — Add flashcard mới → tạo bản ghi flashcards (201)
| **Tham chiếu** AC-07-03, BR-07-05 | **Ưu tiên** P1 |

### TC-U-07-07 — Add flashcard trùng → FlashcardExistsException (409)
| **Tham chiếu** BR-07-05, UC-07 §3.2 | **Ưu tiên** P1 |

### TC-U-07-08 — Kanji không tồn tại/chưa publish → ContentNotFoundException (404)
| **Tham chiếu** UC-07 §3.2 | **Ưu tiên** P1 |

### TC-U-07-09 — Level ngoài {N5..N1} → LEVEL_MISMATCH (422)
| **Tham chiếu** UC-07 §3.2 | **Ưu tiên** P1 |

### TC-U-07-10 — VIP-only + FREE → VipRequiredException (403)
| **Tham chiếu** BR-07-07 | **Ưu tiên** P0 |

### TC-U-07-11 — Hạ tiến độ thủ công → PROGRESS_REGRESSION (422)
| **Tham chiếu** BR-07-04 | **Ưu tiên** P1 |

### TC-U-07-12 — Mọi lượt xem cập nhật last_activity_date
| **Tham chiếu** BR-07-06 | **Ưu tiên** P2 |

---

## 2. INTEGRATION TESTS `@Tag("integration")`

### TC-I-07-01 — UNIQUE progress chặn duplicate | BR-07-03 · P1
### TC-I-07-02 — UNIQUE flashcard (student,kanji,contentId) → 409 khi trùng | BR-07-05 · P1

---

## 3. API / CONTROLLER TESTS `@Tag("api")`

### TC-A-07-01 — GET /api/kanji?level=N5 → 200, phân trang | AC-07-01 · P1
### TC-A-07-02 — GET /api/kanji/{id} → 200 đầy đủ trường, không animation | AC-07-01/04 · P1
### TC-A-07-03 — POST /api/flashcards kanji mới → 201 | AC-07-03 · P1
### TC-A-07-04 — POST /api/flashcards trùng → 409 FLASHCARD_EXISTS | BR-07-05 · P1
### TC-A-07-05 — GET detail draft → 404 CONTENT_NOT_FOUND | UC-07 §3.2 · P1
### TC-A-07-06 — GET VIP + FREE → 403 VIP_REQUIRED | BR-07-07 · P0
### TC-A-07-07 — POST progress hạ tiến độ → 422 PROGRESS_REGRESSION | BR-07-04 · P1
### TC-A-07-08 — Không JWT → 401 | P0

---

## 4. FRONTEND COMPONENT TESTS

### TC-F-07-01 — strokeOrderUrl render `<img>` tĩnh (không canvas động) | AC-07-04 · P2
### TC-F-07-02 — "Add to Flashcard" → POST /flashcards; trùng hiện thông báo 409 | AC-07-03 · P2

---

## 5. TEST DATA SUMMARY

| Fixture | level | status | is_vip_only |
|:---|:---|:---|:---:|
| `kanjiN5Pub` | N5 | published | 0 |
| `kanjiDraft` | N5 | draft | 0 |
| `kanjiVip` | N3 | published | 1 |
| `flashcardExisting` | — | — | student 7 đã có card kanji |

---

## 6. COVERAGE CHECKLIST

| Nguồn | TC | ✔ |
|:---|:---|:---:|
| AC-07-01 | TC-U-07-01, TC-A-07-01/02 | ✅ |
| AC-07-02 | TC-U-07-04 | ✅ |
| AC-07-03 | TC-U-07-06, TC-A-07-03 | ✅ |
| AC-07-04 | TC-U-07-03, TC-A-07-02, TC-F-07-01 | ✅ |
| BR-07-01 | TC-U-07-02/08 | ✅ |
| BR-07-02 | TC-U-07-03 | ✅ |
| BR-07-03 | TC-U-07-05, TC-I-07-01 | ✅ |
| BR-07-04 | TC-U-07-11, TC-A-07-07 | ✅ |
| BR-07-05 | TC-U-07-07, TC-I-07-02, TC-A-07-04 | ✅ |
| BR-07-06 | TC-U-07-12 | ✅ |
| BR-07-07 | TC-U-07-10, TC-A-07-06 | ✅ |
