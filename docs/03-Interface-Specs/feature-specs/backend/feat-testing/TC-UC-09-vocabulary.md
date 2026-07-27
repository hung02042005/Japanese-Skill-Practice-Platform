# TC-UC-09 — Test Cases: Học Từ Vựng

> **Feature:** `feat-core-learning` | **UC:** UC-09 | **Version:** 1.0
> **Nguồn AC:** UC-09 § 8 (AC-09-01…05) | **Nguồn BR:** § 4 (BR-09-01…08)
> **Quy trình:** xem `TEST-PROCESS-COVERAGE.md`
> **Cập nhật:** 2026-07-21

---

## 1. UNIT TESTS — VocabularyService `@Tag("unit")`

### TC-U-09-01 — Lọc đồng thời level + topic
| **Tham chiếu** AC-09-01, BR-09-02 | **Ưu tiên** P1 |
**Setup:** N5 topic "Du lịch" và N5 topic "Gia đình". **Steps:** list(level=N5, topic="Du lịch"). **Expected:** chỉ trả từ N5 thuộc "Du lịch".

### TC-U-09-02 — Detail đầy đủ trường + ví dụ song ngữ
| **Tham chiếu** AC-09-02, BR-09-08 | **Ưu tiên** P1 |
**Expected:** `word, furigana, meaning, jlptLevel, topic, audioUrl, exampleSentenceJp, exampleSentenceVi`; JP+VI trả cùng nhau.

### TC-U-09-03 — Chỉ published, is_deleted=0
| **Tham chiếu** AC-09-05, BR-09-01 | **Ưu tiên** P1 |

### TC-U-09-04 — Đánh dấu completed → upsert progress (content_type='vocabulary')
| **Tham chiếu** AC-09-03, BR-09-03 | **Ưu tiên** P1 |

### TC-U-09-05 — Upsert không duplicate
| **Tham chiếu** BR-09-03 | **Ưu tiên** P1 |

### TC-U-09-06 — Add flashcard vocabulary mới → 201
| **Tham chiếu** AC-09-04, BR-09-05 | **Ưu tiên** P1 |

### TC-U-09-07 — Add flashcard trùng → FlashcardExistsException (409)
| **Tham chiếu** BR-09-05 | **Ưu tiên** P1 |

### TC-U-09-08 — Detail draft → ContentNotFoundException (404)
| **Tham chiếu** AC-09-05 | **Ưu tiên** P1 |

### TC-U-09-09 — Level ngoài {N5..N1} → LEVEL_MISMATCH (422)
| **Tham chiếu** UC-09 §3.2 | **Ưu tiên** P1 |

### TC-U-09-10 — VIP-only + FREE → VipRequiredException (403)
| **Tham chiếu** BR-09-07 | **Ưu tiên** P0 |

### TC-U-09-11 — Hạ tiến độ thủ công → PROGRESS_REGRESSION (422)
| **Tham chiếu** BR-09-04 | **Ưu tiên** P1 |

### TC-U-09-12 — Cập nhật last_activity_date
| **Tham chiếu** BR-09-06 | **Ưu tiên** P2 |

### TC-U-09-13 — topic không khớp → trả rỗng (không lỗi)
| **Tham chiếu** UC-09 §5 | **Ưu tiên** P2 |

---

## 2. INTEGRATION TESTS `@Tag("integration")`

### TC-I-09-01 — UNIQUE progress chặn duplicate | BR-09-03 · P1
### TC-I-09-02 — Query lọc level AND topic AND status='published' | AC-09-01 · P1
### TC-I-09-03 — UNIQUE flashcard vocabulary → 409 khi trùng | BR-09-05 · P1

---

## 3. API / CONTROLLER TESTS `@Tag("api")`

### TC-A-09-01 — GET /api/vocabulary?level=N5&topic=Du lịch → 200 lọc đúng | AC-09-01 · P1
### TC-A-09-02 — GET /api/vocabulary/{id} → 200 đầy đủ + ví dụ song ngữ | AC-09-02 · P1
### TC-A-09-03 — GET list N5 với từ draft → không xuất hiện | AC-09-05 · P1
### TC-A-09-04 — GET detail draft → 404 CONTENT_NOT_FOUND | AC-09-05 · P1
### TC-A-09-05 — POST /api/flashcards vocabulary mới → 201 | AC-09-04 · P1
### TC-A-09-06 — POST /api/flashcards trùng → 409 FLASHCARD_EXISTS | BR-09-05 · P1
### TC-A-09-07 — GET VIP + FREE → 403 VIP_REQUIRED | BR-09-07 · P0
### TC-A-09-08 — POST progress hạ tiến độ → 422 PROGRESS_REGRESSION | BR-09-04 · P1
### TC-A-09-09 — Không JWT → 401 | P0

---

## 4. FRONTEND COMPONENT TESTS

### TC-F-09-01 — Phát audio từ audioUrl không gọi backend | UC-09 §6 · P2
### TC-F-09-02 — Lọc level+topic gọi API đúng param | AC-09-01 · P2
### TC-F-09-03 — "Add to Flashcard" → POST; trùng hiển thị 409 | AC-09-04 · P2

---

## 5. TEST DATA SUMMARY

| Fixture | level | topic | status | is_vip_only |
|:---|:---|:---|:---|:---:|
| `vocabN5DuLich` | N5 | Du lịch | published | 0 |
| `vocabN5GiaDinh` | N5 | Gia đình | published | 0 |
| `vocabDraft` | N5 | Du lịch | draft | 0 |
| `vocabVip` | N3 | — | published | 1 |

---

## 6. COVERAGE CHECKLIST

| Nguồn | TC | ✔ |
|:---|:---|:---:|
| AC-09-01 | TC-U-09-01, TC-I-09-02, TC-A-09-01 | ✅ |
| AC-09-02 | TC-U-09-02, TC-A-09-02 | ✅ |
| AC-09-03 | TC-U-09-04 | ✅ |
| AC-09-04 | TC-U-09-06, TC-A-09-05 | ✅ |
| AC-09-05 | TC-U-09-03/08, TC-A-09-03/04 | ✅ |
| BR-09-01 | TC-U-09-03 | ✅ |
| BR-09-02 | TC-U-09-01 | ✅ |
| BR-09-03 | TC-U-09-05, TC-I-09-01 | ✅ |
| BR-09-04 | TC-U-09-11, TC-A-09-08 | ✅ |
| BR-09-05 | TC-U-09-07, TC-I-09-03, TC-A-09-06 | ✅ |
| BR-09-06 | TC-U-09-12 | ✅ |
| BR-09-07 | TC-U-09-10, TC-A-09-07 | ✅ |
| BR-09-08 | TC-U-09-02 | ✅ |
