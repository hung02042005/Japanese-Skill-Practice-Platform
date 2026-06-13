# SPEC — Staff Chấm Điểm Bài Nói (Speaking Grading)
> **Feature ID:** `feat-staff` | **Page:** `StaffGrading`
> **Route:** `/staff/grading`
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-06-03
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning
> **Master ref:** `MASTERFrontend-Student-Staff-SPEC.md`
> **Backend ref:** `feat-support` — UC-31 (Grade Speaking Submission)

---

## 1. TỔNG QUAN TRANG

Trang cho phép Staff nghe bài nộp nói (speaking submission) của học viên, xem điểm đề xuất của AI, và nhập điểm thủ công cùng phản hồi chi tiết. Điểm thủ công (`manual_score`) ghi đè điểm AI (`ai_overall_score`) để tính `final_score`.

Layout dạng **master-detail**: trái là danh sách submission chờ chấm, phải là panel nghe + nhập điểm.

**Prefix CSS:** `grd-`
**activeTab:** `'staff-grading'`
**Guard:** `<StaffRoute>`
**State:** Local state + `useCallback`

---

## 2. LAYOUT

```
┌──────────────────────────────────────────────────────────────────┐
│  StaffTopNav  activeTab="staff-grading"                          │
├──────────────────────────────────────────────────────────────────┤
│  <main className="grd-body">                                     │
│                                                                  │
│  [Page Header: "Chấm Bài Nói"  |  Badge: "7 chờ chấm"]          │
│                                                                  │
│  ┌────────────────────┐  ┌──────────────────────────────────┐   │
│  │  SUBMISSION LIST   │  │  GRADING PANEL                   │   │
│  │  (360px)           │  │  (flex: 1)                       │   │
│  │                    │  │                                  │   │
│  │ [Status tabs]      │  │  [Student Info Header]           │   │
│  │  Chờ chấm | Đã chấm│  │  Nguyễn A | N3 | 01/06 14:30   │   │
│  │                    │  │                                  │   │
│  │ ┌────────────────┐ │  │  [Audio Player Section]          │   │
│  │ │ Nguyễn Văn A   │ │  │  ┌──────────────────────────┐   │   │
│  │ │ N3 • 01/06     │ │  │  │  ▶ [░░░░░░░░░░░░░░░]  89s│   │   │
│  │ │ ⏱ 1:29   ○ AI  │ │  │  └──────────────────────────┘   │   │
│  │ └────────────────┘ │  │                                  │   │
│  │ ┌────────────────┐ │  │  [AI Score Section]              │   │
│  │ │ Trần Thị B     │ │  │  Tổng: 72.5 | Phát âm: 68.0    │   │
│  │ │ N4 • 30/05     │ │  │  Lưu loát: 77.0               │   │
│  │ │ ✅ Đã chấm     │ │  │  Lỗi AI phát hiện: "..."       │   │
│  │ └────────────────┘ │  │  Gợi ý AI: "..."                │   │
│  │ ...                │  │                                  │   │
│  │                    │  │  [Manual Grading Section]        │   │
│  │                    │  │  Điểm thủ công (0-100): [___]   │   │
│  │                    │  │                                  │   │
│  │                    │  │  Phản hồi chi tiết:              │   │
│  │                    │  │  [textarea...]                   │   │
│  │                    │  │                                  │   │
│  │                    │  │  [Hủy]  [Lưu điểm →]            │   │
│  └────────────────────┘  └──────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. FILE STRUCTURE

```
pages/staff/StaffGrading.jsx
pages/staff/StaffGrading.css
components/staff/SubmissionList.jsx     ← danh sách trái (>60 dòng)
components/staff/GradingPanel.jsx       ← panel phải: audio + AI + form (>60 dòng)
```

---

## 4. STATE

```js
const [submissions,  setList]     = useState([]);
const [selectedId,   setSelected] = useState(null);
const [detail,       setDetail]   = useState(null);   // submission detail
const [isLoadList,   setLoadList] = useState(true);
const [isLoadDetail, setLoadDtl]  = useState(false);
const [statusTab,    setTab]      = useState('pending');  // 'pending' | 'graded'
const [currentPage,  setPage]     = useState(1);
const [totalPages,   setTotal]    = useState(1);
const [pendingCount, setPending]  = useState(0);
const [manualScore,  setScore]    = useState('');
const [feedback,     setFeedback] = useState('');
const [isSaving,     setSaving]   = useState(false);
const [saveError,    setSaveError]= useState('');
const PAGE_SIZE = 20;
```

---

## 5. API — `staffService.js`

```js
// Lấy danh sách submission speaking
export async function getSpeakingSubmissions({ status, page = 0, size = 20 } = {}) {
  const params = { submissionType: 'speaking', page, size };
  if (status) params.status = status;  // 'pending' | 'ai_graded' | 'graded'
  const res = await api.get('/staff/submissions', { params });
  return res.data.data;
  // { content: [{ submissionId, studentName, jlptLevel, durationSeconds, submittedAt, status, aiOverallScore }], totalPages }
}

// Lấy chi tiết 1 submission
export async function getSubmissionDetail(submissionId) {
  const res = await api.get(`/staff/submissions/${submissionId}`);
  return res.data.data;
  // {
  //   submissionId, studentName, jlptLevel, submittedAt,
  //   recordingUrl, durationSeconds,
  //   aiOverallScore, aiPronunciationScore, aiFluencyScore,
  //   aiHighlightedErrors, aiSuggestions, aiGradedAt,
  //   manualScore, manualFeedback, gradedBy, gradedAt,
  //   finalScore, status
  // }
}

// Chấm điểm thủ công
export async function gradeSubmission(submissionId, { manualScore, manualFeedback }) {
  const res = await api.post(`/staff/submissions/${submissionId}/grade`, {
    manualScore: parseFloat(manualScore),
    manualFeedback,
  });
  return res.data.data;
  // { submissionId, status: 'graded', finalScore, gradedAt }
}
```

---

## 6. SUBMISSION LIST (cột trái)

### Status tabs

```
[Chờ chấm (7)] [Đã chấm]
```

Badge đỏ trên tab "Chờ chấm" — số lượng `pendingCount`.

### Submission Card

```
┌────────────────────────────────────┐
│ Nguyễn Văn A                 N3   │
│ 01/06 14:30  ·  ⏱ 1:29 giây       │
│                                    │
│ AI: 72.5/100  [Chưa chấm]          │
└────────────────────────────────────┘
```

Trạng thái pill trong card:

| Status | Label | Background | Text |
|:---|:---|:---|:---|
| `pending` | Chưa qua AI | `#FFF3E0` | `--color-warning` |
| `ai_graded` | Chờ chấm | `--color-primary-bg` | `--color-primary-dark` |
| `graded` | Đã chấm | `--color-secondary-bg` | `--color-secondary` |

Active card: border-left `3px solid var(--color-primary)`, background `var(--color-primary-bg)`.

```css
.grd-sub-card { padding: 14px 16px; border-bottom: 1px solid var(--color-border); cursor: pointer; transition: background var(--transition); }
.grd-sub-card:hover { background: var(--color-primary-bg); }
.grd-sub-card--active { background: var(--color-primary-bg); border-left: 3px solid var(--color-primary); }
.grd-sub-name  { font-size: 14px; font-weight: 600; color: var(--color-text); }
.grd-sub-meta  { font-size: 12px; color: var(--color-text-sub); margin-top: 4px; display: flex; gap: 10px; align-items: center; }
.grd-sub-ai    { font-size: 12px; font-weight: 700; color: var(--color-text-sub); margin-top: 6px; }
```

---

## 7. GRADING PANEL (cột phải)

### Student Info Header

```
┌──────────────────────────────────────────────────────┐
│ [UserAvatar name={name} size={40}]                   │
│ Nguyễn Văn A               [JlptBadge level="N3"]   │
│ Nộp lúc: 01/06/2026  14:30:00                        │
│ Thời lượng: 1 phút 29 giây                           │
└──────────────────────────────────────────────────────┘
```

### Audio Player Section

Dùng `<audio>` HTML5 native với `controls`:

```jsx
<div className="grd-audio-section">
  <h3 className="grd-section-title">Bài nộp</h3>
  <audio
    controls
    src={detail.recordingUrl}
    className="grd-audio-player"
    aria-label={`Bài nói của ${detail.studentName}`}
  />
  <span className="grd-audio-duration">
    Thời lượng: {formatDuration(detail.durationSeconds)}
  </span>
</div>
```

```css
.grd-audio-player { width: 100%; border-radius: var(--radius-md); margin-top: 8px; }
.grd-audio-section { background: var(--color-bg); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 16px; }
```

### AI Score Section

```
┌──────────────────────────────────────────────────────┐
│  Kết quả đánh giá AI                                 │
│                                                      │
│  Điểm tổng:  72.5 / 100                              │
│  Phát âm:    68.0                                    │
│  Lưu loát:   77.0                                    │
│                                                      │
│  Lỗi phát hiện:                                      │
│  "おはよう — âm điệu kéo dài bất thường ở cuối"     │
│                                                      │
│  Gợi ý cải thiện:                                    │
│  "Chú ý ngắt âm dứt khoát hơn ở cuối câu."          │
└──────────────────────────────────────────────────────┘
```

Nếu `status === 'pending'` (AI chưa chấm): hiển thị skeleton / "AI đang xử lý...".

AI Score bars (mini progress bars):

```jsx
<div className="grd-ai-score-bar">
  <span className="grd-ai-score-label">Phát âm</span>
  <ProgressBar value={detail.aiPronunciationScore} />
  <span className="grd-ai-score-value">{detail.aiPronunciationScore}</span>
</div>
```

### Manual Grading Section

```
┌──────────────────────────────────────────────────────┐
│  Chấm điểm thủ công                                  │
│                                                      │
│  Điểm thủ công * (0 – 100)                           │
│  [85.5___________]                                   │
│  ← Để rỗng nếu muốn dùng điểm AI                    │
│                                                      │
│  Phản hồi chi tiết *                                 │
│  [Nhập nhận xét cho học viên...]                     │
│  [                              ]                    │
│  [                              ] (textarea 5 dòng) │
│                                                      │
│  Điểm kết quả sẽ là: 85.5 ← preview real-time       │
│                                                      │
│  [Hủy]          [Lưu điểm ✓]                        │
└──────────────────────────────────────────────────────┘
```

**Real-time preview điểm kết quả:**
```js
const previewFinalScore = manualScore !== '' ? parseFloat(manualScore) : detail?.aiOverallScore;
```

**Validation client-side:**
- `manualScore` phải là số trong [0, 100] hoặc rỗng
- `feedback` không rỗng (bắt buộc khi lưu điểm thủ công)

**Nút "Lưu điểm" disabled khi:**
- `isSaving`
- `feedback.trim() === ''`
- `manualScore !== ''` && (`parseFloat(manualScore) < 0` || `parseFloat(manualScore) > 100`)
- `detail.status === 'graded'` (đã chấm rồi — chỉ được xem)

**Submit flow:**
1. Gọi `gradeSubmission(selectedId, { manualScore, manualFeedback: feedback })`
2. Update detail local: `status = 'graded'`, `finalScore = result.finalScore`
3. Update card trong list: status → `graded`
4. Cập nhật `pendingCount` (giảm 1)
5. Toast success: "Đã lưu điểm. Học viên sẽ nhận thông báo kết quả."
6. Tự động chọn submission tiếp theo trong list (nếu có)

**Đã chấm — readonly mode:**
```
Điểm thủ công: 85.5 / 100
Phản hồi: "Phát âm từ 'おはよう' rất tốt..."
Chấm lúc: 01/06/2026 16:22:00
Điểm kết quả: 85.5 / 100  ✅ Hoàn thành
```

---

## 8. EMPTY STATES

**Không chọn submission (panel phải rỗng):**
```jsx
<div className="grd-empty-panel">
  <SakuChan variant="idle" size={80} aria-hidden="true" />
  <p>Chọn một bài nộp để bắt đầu chấm</p>
</div>
```

**Danh sách chờ chấm rỗng:**
```jsx
<EmptyState
  title="Không còn bài nào chờ chấm"
  subtitle="Tất cả bài nộp speaking đã được xử lý. Làm tốt lắm!"
  mascotVariant="celebrate"
  mascotSize={120}
/>
```

---

## 9. LOADING / ERROR

- **Loading list:** skeleton 4 submission cards
- **Loading detail:** skeleton 3 sections (audio bar, AI scores, form)
- **Error list:** error banner + retry
- **Error detail:** "Không thể tải bài nộp. Vui lòng thử lại." + retry button
- **Save error:** `saveError` hiển thị inline dưới form: text đỏ `grd-save-error`

---

## 10. CSS KEY CLASSES

```css
.grd-page  { min-height: 100vh; display: flex; flex-direction: column; background: var(--color-bg); }
.grd-body  {
  flex: 1; max-width: 1300px; width: 100%; margin: 0 auto;
  padding: 28px 32px 48px; box-sizing: border-box;
  display: flex; gap: 0;
}

.grd-list-col {
  width: 360px; flex-shrink: 0;
  border-right: 1px solid var(--color-border);
  display: flex; flex-direction: column;
  overflow: hidden;
}
.grd-list-scroll { flex: 1; overflow-y: auto; }

.grd-detail-col {
  flex: 1; overflow-y: auto;
  padding: 24px 32px;
  display: flex; flex-direction: column; gap: 20px;
}

.grd-section-card {
  background: var(--color-card); border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm); padding: 20px 24px;
}
.grd-section-title { font-size: 15px; font-weight: 700; color: var(--color-text); margin: 0 0 16px; }

.grd-ai-scores { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 12px; }
.grd-ai-score-item { text-align: center; }
.grd-ai-score-num  { font-size: 24px; font-weight: 800; color: var(--color-text); }
.grd-ai-score-lbl  { font-size: 11px; color: var(--color-text-sub); margin-top: 2px; }

.grd-score-input {
  width: 140px; height: 44px; padding: 0 14px;
  border: 1.5px solid var(--color-border); border-radius: var(--radius-md);
  font-size: 16px; font-weight: 700; color: var(--color-text);
  background: var(--color-bg);
}
.grd-score-input:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(232,154,170,0.15); }
.grd-score-input--err  { border-color: var(--color-error); }

.grd-preview-score { font-size: 14px; font-weight: 600; color: var(--color-secondary); margin-top: 8px; }
.grd-feedback-area { width: 100%; min-height: 120px; padding: 12px 14px; resize: vertical; border: 1.5px solid var(--color-border); border-radius: var(--radius-md); font-size: 14px; font-family: var(--font-base); box-sizing: border-box; }
.grd-feedback-area:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(232,154,170,0.15); }

.grd-form-footer { display: flex; justify-content: flex-end; gap: 10px; padding-top: 12px; border-top: 1px solid var(--color-border); }
.grd-save-btn {
  height: 44px; padding: 0 28px; background: var(--color-secondary); color: white;
  border: none; border-radius: var(--radius-full); font-size: 14px; font-weight: 700;
  cursor: pointer; transition: filter var(--transition);
}
.grd-save-btn:hover:not(:disabled) { filter: brightness(1.07); }
.grd-save-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.grd-save-error { font-size: 12px; color: var(--color-error); margin-top: 6px; }
```

---

## 11. RESPONSIVE

```css
@media (max-width: 1199px) { .grd-body { padding: 24px 20px 48px; } }
@media (max-width: 767px)  {
  .grd-body     { flex-direction: column; padding: 0; }
  .grd-list-col { width: 100%; border-right: none; border-bottom: 1px solid var(--color-border); max-height: 40vh; overflow-y: auto; }
  .grd-detail-col { padding: 16px; }
  .grd-ai-scores  { grid-template-columns: repeat(3, 1fr); }
}
```

---

## 12. ACCESSIBILITY

- [ ] `<audio>` có `aria-label` tên học viên và bài nộp
- [ ] Score input có `aria-label="Điểm thủ công"`, `aria-describedby` chỉ vào preview score
- [ ] Preview score: `aria-live="polite"` để thông báo khi điểm thay đổi
- [ ] Textarea phản hồi: `aria-label="Phản hồi chi tiết"`, `aria-required="true"`
- [ ] Submission list: `role="list"`, card là `role="listitem"` + `aria-selected`
- [ ] Readonly mode (đã chấm): tất cả inputs có `readOnly` hoặc `disabled` + `aria-readonly`
- [ ] Badge "Chờ chấm": `aria-label="7 bài đang chờ chấm điểm"`
