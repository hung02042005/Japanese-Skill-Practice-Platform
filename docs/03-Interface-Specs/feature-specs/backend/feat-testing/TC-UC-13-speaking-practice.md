# TC-UC-13 — Test Cases: Luyện Nói & Chấm Điểm AI (Speaking Practice)

> **Feature:** `feat-ai-skills` | **UC:** UC-13 | **Version:** 1.0
> **Nguồn AC:** UC-13 § 8 (AC-13-01…08) | **Nguồn BR:** UC-13 § 4 (BR-13-01…09)
> **Quy trình:** xem `TEST-PROCESS-COVERAGE.md`
> **Cập nhật:** 2026-07-21

---

## 1. UNIT TESTS — SpeakingService (JUnit 5 + Mockito)

> **File:** `SpeakingServiceTest.java` | **Tag:** `@Tag("unit")`

### TC-U-13-01 — Nộp bài hợp lệ tạo submission `pending` và enqueue job
| | |
|:---|:---|
| **Tham chiếu** | AC-13-02, BR-13-02 | **Ưu tiên** | P1 |

**Setup:** `lessonId=1` tồn tại `lesson_type='speaking'`, `status='published'`; file webm 2MB hợp lệ; `fileStorage.store()` → `"/uploads/spk/1.webm"`.
**Steps:** gọi `speakingService.submitSpeaking(1, webmFile, studentId=7)`.
**Expected:**
- Trả `SubmissionResponse{submissionId, status='PENDING'}`.
- `submissionRepository.save()` gọi 1 lần với `submissionType='speaking'`, `status='pending'`, `recordingUrl` là chuỗi path.
- `aiJobQueue.enqueue(submissionId)` được gọi đúng 1 lần.
- KHÔNG chờ AI (không gọi engine đồng bộ).

### TC-U-13-02 — File sai định dạng → ném `InvalidFileTypeException`
| | |
|:---|:---|
| **Tham chiếu** | AC-13-06, BR-13-02 | **Ưu tiên** | P0 |

**Setup:** file MIME `video/mp4`.
**Steps:** `submitSpeaking(1, mp4File, 7)`.
**Expected:** ném `InvalidFileTypeException`; `fileStorage.store()` KHÔNG gọi; `submissionRepository.save()` KHÔNG gọi.

### TC-U-13-03 — File > 10MB → ném `FileTooLargeException`
| | |
|:---|:---|
| **Tham chiếu** | AC-13-07 | **Ưu tiên** | P0 |

**Setup:** file webm size = 10MB + 1 byte (boundary).
**Expected:** ném `FileTooLargeException`; không lưu file, không tạo submission.
> Boundary đối chứng: file đúng 10MB (10 * 1024 * 1024) phải **được chấp nhận**.

### TC-U-13-04 — lessonId không phải type 'speaking' → `LessonNotFoundException`
| | |
|:---|:---|
| **Tham chiếu** | BR-13-08, UC-13 §3.2 | **Ưu tiên** | P1 |

**Setup:** `lessonId=2` tồn tại nhưng `lesson_type='reading'`.
**Expected:** ném `LessonNotFoundException`; không tạo submission.

### TC-U-13-05 — AI thành công cập nhật đầy đủ điểm, `status='ai_graded'`
| | |
|:---|:---|
| **Tham chiếu** | AC-13-04, BR-13-06 | **Ưu tiên** | P1 |

**Setup:** engine trả `{overall=82, pronunciation=80, fluency=85, errors=[...], suggestions="..."}`.
**Steps:** gọi `speakingAsyncProcessor.process(submissionId)`.
**Expected:** submission cập nhật `aiOverallScore=82`, `status='ai_graded'`, `aiGradedAt≈now`, `finalScore=82` (manual_score null → dùng ai_overall).

### TC-U-13-06 — Điểm AI ngoài [0,100] bị clamp + log warning
| | |
|:---|:---|
| **Tham chiếu** | BR-13-05, UC-13 §3.2 | **Ưu tiên** | P1 |

**Setup:** engine trả `overall=130`, `pronunciation=-5`.
**Expected:** lưu `aiOverallScore=100`, `aiPronunciationScore=0`; có log WARN chứa giá trị gốc; không ném exception.

### TC-U-13-07 — Retry đúng backoff 1s/2s/4s, tối đa 3 retry
| | |
|:---|:---|
| **Tham chiếu** | BR-13-03 | **Ưu tiên** | P1 |

**Setup:** engine ném timeout ở attempt 1–3, thành công ở attempt 4.
**Expected:** engine được gọi 4 lần (1 gốc + 3 retry); delay giữa các lần = 1s,2s,4s (dùng fake clock/scheduler); kết quả cuối được lưu.

### TC-U-13-08 — AI fail toàn bộ → fallback message, `aiOverallScore=null`
| | |
|:---|:---|
| **Tham chiếu** | AC-13-05, BR-13-04, UC-13 §3.3 | **Ưu tiên** | P0 |

**Setup:** engine ném timeout ở cả 4 attempt.
**Expected:** submission `status='ai_graded'`, `aiOverallScore=null`, `aiSuggestions="Không thể xử lý bài nộp. Vui lòng thử lại sau."`; log đủ `{submissionId, engine, attempt, status, duration, errorMessage}` (BR-13-07); KHÔNG có raw exception message trong field trả về user.

### TC-U-13-09 — Poll khi chưa xong trả PENDING
| | |
|:---|:---|
| **Tham chiếu** | AC-13-03 | **Ưu tiên** | P1 |

**Setup:** submission `status='pending'` thuộc student 7.
**Steps:** `getResult(submissionId, 7)`.
**Expected:** trả `PendingResponse{status='PENDING'}` + message hướng dẫn; không lộ field điểm.

### TC-U-13-10 — Poll submission của người khác → không truy cập được
| | |
|:---|:---|
| **Tham chiếu** | UC-13 §5 (submissionId thuộc student đăng nhập) | **Ưu tiên** | P0 (authz) |

**Setup:** submission thuộc student 7; gọi với student 9.
**Expected:** ném `SubmissionNotFoundException` (dùng `findByIdAndStudentId`) — KHÔNG trả dữ liệu.

---

## 2. INTEGRATION TESTS — SubmissionRepository & DB

> **File:** `SpeakingRepositoryIT.java` | **Tag:** `@Tag("integration")`

### TC-I-13-01 — recording_url lưu chuỗi path, không binary (ADR-006)
| | |
|:---|:---|
| **Tham chiếu** | AC-13-08, BR-13-01 | **Ưu tiên** | P0 |

**Steps:** save submission với `recordingUrl="/uploads/spk/1.webm"`; reload; kiểm cột.
**Expected:** cột `recording_url` kiểu chuỗi chứa path; không có cột BLOB audio; độ dài < 512.

### TC-I-13-02 — findByIdAndStudentId lọc đúng chủ sở hữu
| | |
|:---|:---|
| **Tham chiếu** | authz | **Ưu tiên** | P1 |

**Steps:** seed submission (student=7); query với (id, studentId=9).
**Expected:** trả `Optional.empty()`.

---

## 3. API / CONTROLLER TESTS — MockMvc

> **File:** `SpeakingControllerTest.java` | **Tag:** `@Tag("api")` (`@WebMvcTest`)

### TC-A-13-01 — POST /api/submissions/speaking → 202 + submissionId
| | |
|:---|:---|
| **Tham chiếu** | AC-13-02, BR-13-02 | **Ưu tiên** | P0 |

**Request:** multipart `lessonId=1` + `audioFile` (webm). **Mock:** service trả `{submissionId=5, status='PENDING'}`.
**Expected:** HTTP **202**; body `{ submissionId:5, status:"PENDING" }`; phản hồi < 500ms (không block AI).

### TC-A-13-02 — File mp4 → 400 INVALID_FILE_TYPE
| **Tham chiếu** AC-13-06 | **Ưu tiên** P0 |
**Mock:** service ném `InvalidFileTypeException`. **Expected:** HTTP 400, `errorCode="INVALID_FILE_TYPE"`.

### TC-A-13-03 — File 15MB → 400 FILE_TOO_LARGE
| **Tham chiếu** AC-13-07 | **Ưu tiên** P0 |
**Expected:** HTTP 400, `errorCode="FILE_TOO_LARGE"` (khớp cả cấu hình `spring.servlet.multipart.max-file-size`).

### TC-A-13-04 — lessonId thiếu → 400 VALIDATION_FAILED
| **Tham chiếu** UC-13 §5 | **Ưu tiên** P1 |

### TC-A-13-05 — lessonId không tồn tại/không phải speaking → 404 LESSON_NOT_FOUND
| **Tham chiếu** BR-13-08 | **Ưu tiên** P1 |

### TC-A-13-06 — GET result đang xử lý → 200 { status: PENDING }
| **Tham chiếu** AC-13-03 | **Ưu tiên** P1 |

### TC-A-13-07 — GET result đã chấm → 200 full result
| **Tham chiếu** AC-13-04 | **Ưu tiên** P1 |
**Expected:** body có `aiOverallScore, aiPronunciationScore, aiFluentScore, aiHighlightedErrors, aiSuggestions, finalScore`.

### TC-A-13-08 — GET result AI fail → 200 fallback, KHÔNG raw error
| **Tham chiếu** AC-13-05, BR-13-04 | **Ưu tiên** P0 |
**Expected:** `aiSuggestions` là message thân thiện; body không chứa chuỗi stacktrace/exception/tên class engine.

### TC-A-13-09 — submissionId không tồn tại → 404 SUBMISSION_NOT_FOUND
| **Tham chiếu** UC-13 §3.2 | **Ưu tiên** P1 |

### TC-A-13-10 — Không JWT → 401 UNAUTHORIZED
| **Tham chiếu** UC-13 §3.2 | **Ưu tiên** P0 |

---

## 4. SECURITY INVARIANT TESTS — `@Tag("security")`

### TC-S-13-01 — Poll result của submission người khác → 404, không lộ dữ liệu
| **Tham chiếu** UC-13 §5 | **Ưu tiên** P0 |
**Steps:** student B poll submissionId của student A. **Expected:** HTTP 404; body không chứa điểm/URL audio của A.

### TC-S-13-02 — Response không bao giờ chứa raw AI error / đường dẫn nội bộ
| **Tham chiếu** BR-13-04, BR-13-07 | **Ưu tiên** P0 |
**Steps:** duyệt đệ quy JSON của mọi response result. **Expected:** không key/giá trị chứa `Exception`, `timeout`, stacktrace, tên engine cụ thể.

---

## 5. FRONTEND COMPONENT TESTS — Jest + RTL

> **File:** `SpeakingPage.test.jsx`, `AudioRecorder.test.jsx`

### TC-F-13-01 — Sau nộp bài hiển thị trạng thái "đang xử lý" và bắt đầu poll
**Mock:** POST → 202. **Expected:** hiển thị "Bài nộp đang được xử lý…"; kích hoạt polling GET result.

### TC-F-13-02 — Poll trả PENDING giữ trạng thái chờ, không hiện điểm
**Mock:** GET → `{status:'PENDING'}`. **Expected:** vẫn spinner; không render khối điểm.

### TC-F-13-03 — Poll trả ai_graded render điểm + gợi ý
**Mock:** GET → full result. **Expected:** render `aiOverallScore`, danh sách lỗi, gợi ý; dừng polling.

### TC-F-13-04 — AI fail render message thân thiện, không lỗi kỹ thuật
**Mock:** GET → fallback. **Expected:** hiển thị "Không thể xử lý bài nộp…"; không hiển thị mã lỗi kỹ thuật.

---

## 6. TEST DATA SUMMARY

| Fixture | lesson_type | status | file |
|:---|:---|:---|:---|
| `speakingLesson` | speaking | published | — |
| `readingLesson` | reading | published | — |
| `validWebm` | — | — | webm, 2MB |
| `webmBoundary` | — | — | webm, đúng 10MB |
| `webmTooLarge` | — | — | webm, 10MB+1B |
| `mp4Invalid` | — | — | mp4, 1MB |

---

## 7. COVERAGE CHECKLIST

| Nguồn | TC | ✔ |
|:---|:---|:---:|
| AC-13-01 (xem bài) | *ngoài BE core — kiểm ở FE render + GET lesson (TC-F gián tiếp)* | ⚠️ FE |
| AC-13-02 (async 202) | TC-U-13-01, TC-A-13-01 | ✅ |
| AC-13-03 (poll pending) | TC-U-13-09, TC-A-13-06 | ✅ |
| AC-13-04 (kết quả đầy đủ) | TC-U-13-05, TC-A-13-07 | ✅ |
| AC-13-05 (fallback) | TC-U-13-08, TC-A-13-08 | ✅ |
| AC-13-06 (file sai định dạng) | TC-U-13-02, TC-A-13-02 | ✅ |
| AC-13-07 (file quá lớn) | TC-U-13-03, TC-A-13-03 | ✅ |
| AC-13-08 (không BLOB) | TC-I-13-01 | ✅ |
| BR-13-01 (URL không BLOB) | TC-I-13-01 | ✅ |
| BR-13-02 (202 ngay) | TC-U-13-01, TC-A-13-01 | ✅ |
| BR-13-03 (timeout+retry 3x backoff) | TC-U-13-07 | ✅ |
| BR-13-04 (không lộ raw error) | TC-U-13-08, TC-S-13-02 | ✅ |
| BR-13-05 (clamp [0,100]) | TC-U-13-06 | ✅ |
| BR-13-06 (final = manual ?? ai) | TC-U-13-05 | ✅ |
| BR-13-07 (log đầy đủ) | TC-U-13-08 | ✅ |
| BR-13-08 (chỉ lesson speaking) | TC-U-13-04, TC-A-13-05 | ✅ |
| BR-13-09 (engine qua interface) | *kiến trúc — verify bằng mock engine ở TC-U-13-05/07/08* | ✅ |
| Authz (submission chủ sở hữu) | TC-U-13-10, TC-I-13-02, TC-S-13-01 | ✅ |

> ⚠️ AC-13-01 (xem thông tin bài luyện nói) là contract của `GET /api/lessons/{id}/speaking` — phủ ở TC-F-13-* và nên bổ sung 1 API test khi endpoint có controller test riêng.
