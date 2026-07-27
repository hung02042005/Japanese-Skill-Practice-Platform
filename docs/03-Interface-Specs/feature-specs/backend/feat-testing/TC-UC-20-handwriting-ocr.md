# TC-UC-20 — Test Cases: Luyện Viết Tay AI (Handwriting OCR)

> **Feature:** `feat-ai-skills` | **UC:** UC-20 | **Version:** 1.0
> **Nguồn AC:** UC-20 § 8 (AC-20-01…09) | **Nguồn BR:** UC-20 § 4 (BR-20-01…10)
> **Quy trình:** xem `TEST-PROCESS-COVERAGE.md`
> **Cập nhật:** 2026-07-21

---

## 1. UNIT TESTS — HandwritingService (JUnit 5 + Mockito)

> **File:** `HandwritingServiceTest.java` | **Tag:** `@Tag("unit")`

### TC-U-20-01 — Nộp ảnh hợp lệ tạo submission `pending` + enqueue OCR
| **Tham chiếu** AC-20-02, BR-20-02 | **Ưu tiên** P1 |
**Setup:** `targetType='kanji'`, `targetId=5` tồn tại; PNG 1MB; `expectedCharacter='水'`.
**Expected:** `SubmissionResponse{submissionId, status='PENDING'}`; save với `submissionType='handwriting'`, `status='pending'`, `handwritingImageUrl` là path; `ocrJobQueue.enqueue()` gọi 1 lần.

### TC-U-20-02 — File gif → `InvalidFileTypeException`
| **Tham chiếu** AC-20-07 | **Ưu tiên** P0 |
**Expected:** ném exception; không lưu file, không tạo submission.

### TC-U-20-03 — PNG > 5MB → `FileTooLargeException` (boundary 5MB+1B)
| **Tham chiếu** AC-20-08 | **Ưu tiên** P0 |
> Đối chứng: PNG đúng 5MB được chấp nhận.

### TC-U-20-04 — targetType ngoài {kanji,kana} → `ValidationException`
| **Tham chiếu** BR-20-09, UC-20 §5 | **Ưu tiên** P1 |
**Setup:** `targetType='romaji'`. **Expected:** 400 VALIDATION_FAILED; không tạo submission.

### TC-U-20-05 — targetId không tồn tại trong bảng tương ứng → not found
| **Tham chiếu** BR-20-09, UC-20 §3.2 | **Ưu tiên** P1 |
**Setup:** `targetType='kanji'`, `targetId=9999` không có trong `kanji`. **Expected:** ném `SubmissionNotFoundException`/`ContentNotFoundException`.

### TC-U-20-06 — expectedCharacter rỗng → `ValidationException`
| **Tham chiếu** UC-20 §5 | **Ưu tiên** P1 |

### TC-U-20-07 — OCR đúng (similarity ≥ 70) → isCorrect=true
| **Tham chiếu** AC-20-03, BR-20-04 | **Ưu tiên** P1 |
**Setup:** engine trả `recognized='水'`, `similarity=88`. **Expected:** submission `similarityPercent=88`, `isCorrect=true`, `status='ai_graded'`, `ocrProcessedAt≈now`.

### TC-U-20-08 — OCR sai (similarity < 70) → isCorrect=false (boundary 69/70)
| **Tham chiếu** AC-20-04, BR-20-04 | **Ưu tiên** P1 |
**Setup:** `similarity=69` → isCorrect=false; kiểm thêm `similarity=70` → isCorrect=true (biên ngưỡng).

### TC-U-20-09 — OCR fail toàn bộ retry → fallback
| **Tham chiếu** AC-20-06, BR-20-05, BR-20-06 | **Ưu tiên** P0 |
**Setup:** engine timeout 4 lần (backoff 1s/2s/4s). **Expected:** `recognizedCharacter=null`, `similarityPercent=0`, `isCorrect=false`, `status='ai_graded'`; log đủ `{submissionId, engine, attempt, status, duration, errorMessage}`.

### TC-U-20-10 — Retry đúng số lần & backoff
| **Tham chiếu** BR-20-06 | **Ưu tiên** P1 |
**Expected:** engine gọi tối đa 4 lần; delay 1s,2s,4s.

### TC-U-20-11 — Poll submission người khác → không truy cập
| **Tham chiếu** UC-20 §5 | **Ưu tiên** P0 (authz) |
**Expected:** `findByIdAndStudentId` → empty → `SubmissionNotFoundException`.

---

## 2. INTEGRATION TESTS — SubmissionRepository & DB

### TC-I-20-01 — handwriting_image_url lưu path, không binary (ADR-006)
| **Tham chiếu** AC-20-09, BR-20-01 | **Ưu tiên** P0 |

### TC-I-20-02 — targetId resolve đúng bảng kanji vs kana_characters
| **Tham chiếu** BR-20-09 | **Ưu tiên** P1 |
**Steps:** seed kanji id=5 và kana id=5; nộp `targetType='kana', targetId=5` phải trỏ `kana_characters`, không phải `kanji`.

---

## 3. API / CONTROLLER TESTS — MockMvc `@Tag("api")`

### TC-A-20-01 — POST /api/submissions/handwriting → 202 + submissionId
| **Tham chiếu** AC-20-02 | **Ưu tiên** P0 |
**Expected:** 202; body `{submissionId, status:'PENDING'}`; < 500ms.

### TC-A-20-02 — File gif → 400 INVALID_FILE_TYPE
| AC-20-07 · P0 |

### TC-A-20-03 — PNG 6MB → 400 FILE_TOO_LARGE
| AC-20-08 · P0 |

### TC-A-20-04 — targetType='romaji' → 400 VALIDATION_FAILED
| BR-20-09 · P1 |

### TC-A-20-05 — targetId không tồn tại → 404 SUBMISSION_NOT_FOUND
| UC-20 §3.2 · P1 |

### TC-A-20-06 — GET result đang xử lý → 200 { status: PENDING }
| AC-20 (poll) · P1 |

### TC-A-20-07 — GET result đã chấm → 200 recognizedCharacter/similarity/isCorrect
| AC-20-03/04 · P1 |

### TC-A-20-08 — GET result OCR fail → 200 fallback (null/0/false), không raw error
| AC-20-06, BR-20-07 · P0 |

### TC-A-20-09 — Không JWT → 401 UNAUTHORIZED
| UC-20 §3.2 · P0 |

---

## 4. SECURITY INVARIANT TESTS — `@Tag("security")`

### TC-S-20-01 — Response không chứa trường liên quan stroke order/calligraphy (ADR-007)
| **Tham chiếu** AC-20-05, BR-20-03 | **Ưu tiên** P1 |
**Steps:** duyệt JSON result. **Expected:** không có key `strokeOrder`, `strokeDirection`, `calligraphyScore`.

### TC-S-20-02 — Không lộ raw OCR error / engine nội bộ
| **Tham chiếu** BR-20-07 | **Ưu tiên** P0 |

### TC-S-20-03 — Poll submission người khác → 404, không lộ dữ liệu
| authz · P0 |

---

## 5. FRONTEND COMPONENT TESTS — Jest + RTL

### TC-F-20-01 — Hiển thị ký tự mục tiêu + strokeOrderUrl (ảnh tĩnh)
| AC-20-01 · P2 | **Expected:** render `<img>` strokeOrderUrl; không có animation/canvas-guide động.

### TC-F-20-02 — Export canvas → PNG khi nhấn "Nộp bài"
| AC-20-02 · P1 | **Expected:** gọi `canvas.toBlob('image/png')` rồi POST multipart.

### TC-F-20-03 — Render kết quả OCR (ký tự nhận diện + % + đúng/sai)
| AC-20-03/04 · P1 |

### TC-F-20-04 — OCR fail → message thân thiện
| AC-20-06 · P1 |

---

## 6. TEST DATA SUMMARY

| Fixture | targetType | tồn tại | file |
|:---|:---|:---|:---|
| `kanjiTarget` | kanji | có (id=5) | — |
| `kanaTarget` | kana | có (id=5) | — |
| `pngValid` | — | — | png 1MB |
| `pngBoundary` | — | — | png đúng 5MB |
| `pngTooLarge` | — | — | png 5MB+1B |
| `gifInvalid` | — | — | gif |

---

## 7. COVERAGE CHECKLIST

| Nguồn | TC | ✔ |
|:---|:---|:---:|
| AC-20-01 (ký tự + stroke tĩnh) | TC-F-20-01 | ✅ |
| AC-20-02 (async 202) | TC-U-20-01, TC-A-20-01 | ✅ |
| AC-20-03 (đúng ≥70) | TC-U-20-07, TC-A-20-07 | ✅ |
| AC-20-04 (sai <70) | TC-U-20-08, TC-A-20-07 | ✅ |
| AC-20-05 (chỉ similarity, không stroke) | TC-S-20-01 | ✅ |
| AC-20-06 (fallback) | TC-U-20-09, TC-A-20-08 | ✅ |
| AC-20-07 (file sai định dạng) | TC-U-20-02, TC-A-20-02 | ✅ |
| AC-20-08 (file quá lớn) | TC-U-20-03, TC-A-20-03 | ✅ |
| AC-20-09 (không BLOB) | TC-I-20-01 | ✅ |
| BR-20-01 | TC-I-20-01 | ✅ |
| BR-20-02 | TC-U-20-01 | ✅ |
| BR-20-03 (không stroke order) | TC-S-20-01 | ✅ |
| BR-20-04 (ngưỡng 70) | TC-U-20-07/08 | ✅ |
| BR-20-05 (fail → 0/false/null) | TC-U-20-09 | ✅ |
| BR-20-06 (timeout+retry 3x) | TC-U-20-10 | ✅ |
| BR-20-07 (không raw error) | TC-A-20-08, TC-S-20-02 | ✅ |
| BR-20-08 (log đầy đủ) | TC-U-20-09 | ✅ |
| BR-20-09 (targetType/targetId hợp lệ) | TC-U-20-04/05, TC-I-20-02 | ✅ |
| BR-20-10 (engine qua interface) | *mock engine ở TC-U-20-07/09/10* | ✅ |
| Authz | TC-U-20-11, TC-S-20-03 | ✅ |
