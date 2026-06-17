# TRACEABILITY — Content & Question Bank Management (`feat-content-management`)

> **Mục đích:** Ma trận truy vết **AC → FR → Test Case** cho UC-24..28, dùng để rà soát phủ kiểm thử và đảm bảo mọi Acceptance Criteria có ≥1 test.
> **Nguồn:** `UC-24..UC-28` §3 (FR) & §8 (AC); `feat-testing/TC-UC-24..28`.
> **Cập nhật:** 2026-06-12 | **Kết luận:** 84/84 AC đã có test phủ (sau khi vá 2 gap UC-25 — xem §7).

---

## 1. UC-24 — Manage Question Bank

| AC | Mô tả ngắn | FR | Test Case |
|:---|:---|:---|:---|
| AC-24-01 | Tạo câu hỏi nháp | FR-24-01/02/09 | TC-U-24-01, TC-A-24-01 |
| AC-24-02 | Thiếu trường bắt buộc | FR-24-02 | TC-U-24-02 |
| AC-24-03 | Trắc nghiệm thiếu đáp án | FR-24-06 | TC-U-24-03 |
| AC-24-04 | Enum JLPT sai | FR-24-05 | TC-U-24-04 |
| AC-24-05 | Lọc skill + level | FR-24-12/13 | TC-I-24-02 |
| AC-24-06 | Tìm kiếm từ khóa | FR-24-11 | TC-I-24-03 |
| AC-24-07 | Chi tiết kèm `isLocked` | FR-24-15 | TC-I-24-01 |
| AC-24-08 | Cập nhật câu draft | FR-24-16/19 | TC-U-24-07 |
| AC-24-09 | Chặn sửa câu đã làm bài | FR-24-17 | TC-U-24-05, TC-A-24-04 |
| AC-24-10 | Chặn sửa khi pending_review | FR-24-18 | TC-U-24-06 |
| AC-24-11 | Gửi duyệt thành công | FR-24-20/21 | TC-U-24-10, TC-A-24-05 |
| AC-24-12 | Chặn Staff tự publish | FR-24-23 | TC-A-24-06 |
| AC-24-13 | Không phải chủ sở hữu | FR-24-24 | TC-U-24-08 |

**Bổ trợ (không gắn AC trực tiếp):** ownership bypass STAFF_MANAGER (FR-24-24 → TC-U-24-09); submit khi published (FR-24-22 → TC-U-24-11); detail 404 (FR-24-14 → TC-A-24-07); không lộ Entity (NFR-24-04 → TC-A-24-08); 401/403 (NFR-24-02 → TC-A-24-02/03). → **13/13 AC phủ.**

---

## 2. UC-25 — Manage Grammar Content

| AC | Mô tả ngắn | FR | Test Case |
|:---|:---|:---|:---|
| AC-01 | Tạo grammar nháp, bỏ qua status | FR-01/02/03 | TC-U-25-01, TC-A-25-01 |
| AC-02 | JLPT sai | FR-04 | TC-U-25-02 |
| AC-03 | Thiếu trường bắt buộc | FR-05 | TC-U-25-03 |
| AC-04 | Liên kết lesson cùng / khác cấp độ | FR-06/08 | TC-U-25-04, TC-U-25-05 |
| AC-05 | List của mình, loại deleted | FR-09/11 | TC-I-25-01 |
| AC-06 | Chi tiết không tồn tại → 404 | FR-12 | **TC-U-25-14** (vá §7) |
| AC-07 | Cập nhật draft → updated_at | FR-13 | **TC-U-25-15** (vá §7) |
| AC-08 | Sửa khi published → 422 | FR-14 | TC-U-25-07, TC-A-25-03 |
| AC-09 | Sửa khi pending_review → 422 | FR-15 | TC-U-25-08 |
| AC-10 | PUT bỏ qua status client | FR-16 | TC-U-25-09 |
| AC-11 | Sửa grammar Staff khác → 403 | FR-17 | TC-U-25-10 |
| AC-12 | Submit-review draft đủ trường | FR-18 | TC-U-25-11 |
| AC-13 | Submit-review khi published → 422 | FR-19 | TC-U-25-13 |
| AC-14 | Submit-review thiếu ví dụ → 422 | FR-20 | TC-U-25-12 |
| AC-15 | Không có đường publish | FR-21 | TC-A-25-02 (contract) |
| AC-16 | Thiếu JWT / sai role | NFR-01 | TC-A-25-02 |
| AC-17 | Không lộ Entity | NFR-07 | TC-A-25-04 |

**→ 17/17 AC phủ** (AC-06, AC-07 đã vá).

---

## 3. UC-26 — Manage Quiz

| AC | Mô tả ngắn | FR | Test Case |
|:---|:---|:---|:---|
| AC-26-01 | Tạo quiz nháp | FR-26-01/07 | TC-U-26-01, TC-A-26-01 |
| AC-26-02 | Thiếu lesson_id & topic | FR-26-03 | TC-U-26-02 |
| AC-26-03 | pass>total | FR-26-05 | TC-U-26-03 |
| AC-26-04 | JLPT sai | FR-26-04 | TC-U-26-04 |
| AC-26-05 | List theo level (chỉ quiz) | FR-26-10/12 | TC-I-26-01 |
| AC-26-06 | Chi tiết sort + scoreMatched | FR-26-13/14 | TC-A-26-06 |
| AC-26-07 | Gán câu hỏi order+score | FR-26-19/23 | TC-U-26-06 |
| AC-26-08 | Gán trùng | FR-26-22 | TC-U-26-07, TC-I-26-02/04, TC-A-26-02 |
| AC-26-09 | Gán câu chưa publish | FR-26-21 | TC-U-26-08 |
| AC-26-10 | Σscore khớp → submit OK | FR-26-26 | TC-U-26-11 |
| AC-26-11 | Σscore lệch → 422 | FR-26-26 | TC-U-26-12, TC-A-26-03 |
| AC-26-12 | Quiz rỗng → 422 | FR-26-28 | TC-U-26-13 |
| AC-26-13 | Khóa khi published | FR-26-24 | TC-U-26-10 |
| AC-26-14 | Chặn update khi pending | FR-26-16 | TC-U-26-14 |
| AC-26-15 | Chặn Staff tự publish | FR-26-30 | TC-A-26-04 |
| AC-26-16 | Không phải chủ sở hữu | FR-26-31 | TC-U-26-15 |

**→ 16/16 AC phủ.**

---

## 4. UC-27 — Manage Learning Content

| AC | Mô tả ngắn | FR | Test Case |
|:---|:---|:---|:---|
| AC-27-01 | Tạo lesson nháp | FR-27-01/09/11 | TC-U-27-01, TC-A-27-01 |
| AC-27-02 | Lesson thiếu lessonType | FR-27-09 | TC-U-27-02 |
| AC-27-03 | Lesson không nội dung | FR-27-11 | TC-U-27-03 |
| AC-27-04 | Listening thiếu audio | FR-27-12 | TC-U-27-04 |
| AC-27-05 | Tạo vocabulary | FR-27-16 | TC-U-27-06 |
| AC-27-06 | Vocabulary thiếu furigana | FR-27-16 | TC-U-27-07 |
| AC-27-07 | Vocabulary lesson không tồn tại | FR-27-18 | TC-U-27-08 |
| AC-27-08 | Tạo kanji | FR-27-20 | TC-U-27-09 |
| AC-27-09 | Kanji trùng | FR-27-21 | TC-U-27-10, TC-I-27-02, TC-A-27-02 |
| AC-27-10 | Kanji thiếu On/Kun | FR-27-20 | TC-U-27-11 |
| AC-27-11 | Update khi pending | FR-27-04 | TC-U-27-12 |
| AC-27-12 | Update khi rejected | FR-27-04 | TC-U-27-13 |
| AC-27-13 | Gửi duyệt thành công | FR-27-25/26 | TC-U-27-14 |
| AC-27-14 | Chặn Staff tự publish | FR-27-05 | TC-A-27-03 |
| AC-27-15 | Không phải chủ sở hữu | FR-27-06 | TC-U-27-15 |
| AC-27-16 | Media lưu URL | FR-27-03 | TC-I-27-01 |

**→ 16/16 AC phủ.**

---

## 5. UC-28 — Manage JLPT Mock Exams

| AC | Mô tả ngắn | FR | Test Case |
|:---|:---|:---|:---|
| AC-28-01 | Tạo exam nháp | FR-28-01/05 | TC-U-28-01, TC-A-28-01 |
| AC-28-02 | Bỏ qua status client | FR-28-05 | TC-U-28-02 |
| AC-28-03 | Thiếu totalScore | FR-28-02 | TC-U-28-03 |
| AC-28-04 | pass>total | FR-28-04 | TC-U-28-04 |
| AC-28-05 | JLPT sai | FR-28-03 | TC-U-28-05 |
| AC-28-06 | List theo level (chỉ exam) | FR-28-10/12 | TC-I-28-01 |
| AC-28-07 | Chi tiết gom theo section | FR-28-13/14 | TC-I-28-02 |
| AC-28-08 | Gán kèm section | FR-28-20 | TC-U-28-06 |
| AC-28-09 | Thiếu sectionName | FR-28-21 | TC-U-28-07, TC-A-28-02 |
| AC-28-10 | Section không hợp lệ | FR-28-22 | TC-U-28-08 |
| AC-28-11 | Gán câu chưa publish | FR-28-24 | TC-U-28-09 |
| AC-28-12 | Gán câu khác cấp độ | FR-28-25 | TC-U-28-10, TC-I-28-03, TC-A-28-03 |
| AC-28-13 | Gán trùng | FR-28-26 | TC-U-28-11, TC-I-28-04 |
| AC-28-14 | Σscore khớp → submit OK | FR-28-30 | TC-U-28-13 |
| AC-28-15 | Σscore lệch → 422 | FR-28-30 | TC-U-28-14, TC-A-28-04 |
| AC-28-16 | Đề rỗng → 422 | FR-28-31 | TC-U-28-15 |
| AC-28-17 | Khóa khi published | FR-28-28 | TC-U-28-12 |
| AC-28-18 | Chặn update khi pending | FR-28-17 | TC-U-28-16 |
| AC-28-19 | Chặn Staff tự publish | FR-28-33 | TC-A-28-05 |
| AC-28-20 | Không phải chủ sở hữu | FR-28-34 | TC-U-28-17 |
| AC-28-21 | Thiếu JWT / sai role | NFR-28-02 | TC-A-28-06 |
| AC-28-22 | Chỉ trả DTO | NFR-28-05 | TC-A-28-07 |

**→ 22/22 AC phủ.**

---

## 6. Tổng hợp phủ AC

| UC | Số AC | AC có test | Tỷ lệ |
|:---|:---:|:---:|:---:|
| UC-24 | 13 | 13 | 100% |
| UC-25 | 17 | 17 | 100% |
| UC-26 | 16 | 16 | 100% |
| UC-27 | 16 | 16 | 100% |
| UC-28 | 22 | 22 | 100% |
| **Tổng** | **84** | **84** | **100%** |

---

## 7. Phát hiện khi rà soát (Findings) & xử lý

- **GAP-25-01 (đã vá):** `AC-06` (GET chi tiết grammar không tồn tại/deleted → 404, FR-12) chưa có test → **bổ sung `TC-U-25-14`**.
- **GAP-25-02 (đã vá):** `AC-07` (cập nhật grammar `draft` thành công → refresh `updated_at`, FR-13) chưa có test → **bổ sung `TC-U-25-15`**.
- **Lưu ý đánh số FR (không phải lỗi):** UC-26 bỏ trống `FR-26-09`, `FR-26-29`; UC-28 bỏ trống `FR-28-08`, `FR-28-09` — do nhóm FR theo mục (3.1/3.2/...) trong UC gốc, **không** thiếu yêu cầu. Giữ nguyên để khớp tài liệu UC.
- **FR ubiquitous chung** (logging, soft delete: FR-24-25, FR-26-32/33, FR-27-08, FR-28-35/36) không gắn AC riêng — kiểm bằng test bổ trợ ở mục "Bổ trợ" từng UC và Definition of Done.
- **Quy ước tham chiếu:** UC-25 dùng `FR-xx` (không prefix UC) theo đúng tài liệu `UC-25`; UC-24/26/27/28 dùng `FR-<uc>-xx`. Các TC giữ đúng quy ước của UC tương ứng.
