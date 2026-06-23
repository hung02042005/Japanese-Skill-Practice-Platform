# ALGO — Xếp thứ tự thẻ ÔN trong phiên (tăng cường §3.6)
>
> **Thuộc:** `feat-flashcard-srs` | **Quan hệ:** **TĂNG CƯỜNG** `SPEC.md §3.6` cadence v2.1 — KHÔNG thay mô hình nhịp.
> **Quyết định:** Phương án **(b)** — giữ nguyên cadence **gate-theo-backlog** (`REVIEW_TRIGGER=5 → REVIEW_BURST`), learning-steps (FR-FC-73..76), caps (FR-FC-78/79), điều kiện kết thúc (FR-FC-80/81). Bổ sung **2 thứ**: (1) **thứ tự thẻ ÔN theo điểm ưu tiên đa yếu tố** thay cho `next_review_date ASC`; (2) **trải đều độ khó** khi xả burst.
> **Vị trí impl:** Backend — `FlashcardSrsService.getSession` (giữ FR-FC-55: không gửi đáp án đúng về client).
> **Trạng thái:** Draft — thêm FR-FC-82/83 vào §3.6.

---

## 1. PHẠM VI THAY ĐỔI (chỉ 2 điểm)

| Giữ nguyên (đã code, §3.6 v2.1) | Thay đổi (doc này) |
|:--|:--|
| Cadence gate-backlog: phục vụ NEW; `reviewQueue ≥ REVIEW_TRIGGER=5` → xả `REVIEW_BURST` | Thứ tự lấy thẻ trong burst: **điểm ưu tiên §3** thay `next_review_date ASC` (FR-FC-70/72/79) |
| Learning-steps re-queue theo rating (FR-FC-73..76) | Khi xả burst: **chống 2 thẻ "hard" liền nhau** (§4) |
| Caps `MAX_NEW=20`, `MAX_REVIEW=50` (FR-FC-78/79) | `REVIEW_BURST` mở rộng **3–4** (thay vì cứng 3) để nhích tỉ lệ ôn về 30–40% |
| Kết thúc + dồn "Từ cần ôn lại" (FR-FC-80/81) | — |

> **7 quy tắc nguồn** ánh xạ vào (b): **R2** (ưu tiên sai/cũ/khó) → §3; **R7** (trải đều độ khó) → §4; **R1/R3/R4** (nhịp 5→3-4, ~30–40%) → **xấp xỉ bằng gate** (xem §5); **R5** (không có thẻ ôn → chỉ NEW) và **R6** (không lặp quá gần) → đã có sẵn trong gate + learning-steps.

---

## 2. ĐẦU VÀO / ĐẦU RA & ÁNH XẠ DB

```ts
// reviewQ được xếp thứ tự bằng scoreAndSort() trước khi đưa vào vòng cadence §3.6
ReviewCard = { card_id, last_reviewed_at, correct_rate, difficulty, next_review_date }
```

| Trường | Nguồn thực tế (`flashcards`) | Ghi chú |
|:--|:--|:--|
| `last_reviewed_at` | `last_reviewed_at` | có sẵn |
| `next_review_date` | `next_review_date` | quá hạn = today − date |
| `difficulty` (0..1) | `(2.5 − ease_factor) / 1.2`, clamp [0,1] | ease thấp ⇒ khó cao |
| `correct_rate` (0..1) | **xấp xỉ** từ `last_rating`: `easy→0.9, hard→0.6, wrong→0.1` | **CHỐT:** dùng xấp xỉ, không thêm cột/migration. Nâng cấp sau bằng `times_seen`/`times_correct` nếu cần (§6.2) |

---

## 3. ĐIỂM ƯU TIÊN THẺ ÔN (R2) — thay `next_review_date ASC`

```
W_WRONG = 0.50 ; W_STALE = 0.30 ; W_DIFF = 0.20      # tổng 1.0
STALE_WINDOW = 14   # ngày

wrongScore = 1 − correct_rate
staleScore = clamp( max(0, today − next_review_date) / STALE_WINDOW , 0, 1 )   # quá hạn càng lâu càng cao
diffScore  = difficulty

priority   = W_WRONG*wrongScore + W_STALE*staleScore + W_DIFF*diffScore

scoreAndSort(reviewCards) = sort(reviewCards, by priority DESC)[: MAX_REVIEW]
```

Thứ tự ưu tiên định tính: **trả lời sai gần đây** (W_WRONG lớn nhất) → **lâu chưa ôn / quá hạn** → **độ khó cao**, đúng R2.

> `user_level`: phiên đã giới hạn theo `deckId` hoặc `level+topic` (FR-FC-50) nên thường đồng cấp; giữ là tham số tùy chọn để hạ ưu tiên thẻ lệch cấp nếu lọt vào.

---

## 4. TRẢI ĐỀU ĐỘ KHÓ KHI XẢ BURST (R7)

Band: `easy < 0.33 ≤ med < 0.66 ≤ hard`.

```
# Lấy thẻ kế tiếp từ reviewQ đã sắp theo priority, nhưng tránh 2 'hard' liền nhau
popForBurst(reviewQ, lastEmitted):
    head = reviewQ[0]
    if band(head)=='hard' and band(lastEmitted)=='hard':
        alt = first card in reviewQ[1 .. LOOKAHEAD] with band != 'hard'   # LOOKAHEAD = 3
        if alt exists: return remove(reviewQ, alt)      # hoãn 'hard' 1 nhịp
    return reviewQ.pop(0)
```

- *Thẻ MỚI*: khi nạp `newQ`, sắp **tăng dần độ khó** (`difficulty ?? 0.5`, asc) để khối đầu nhẹ, nặng dần — tránh "sốc" (R7). Không đổi cách gate phục vụ NEW.

---

## 5. NHÚNG VÀO PSEUDOCODE §3.6 (b)

Chỉ thay 2 chỗ tô đậm; phần còn lại y nguyên §3.6.

```
REVIEW_TRIGGER = 5 ; REVIEW_BURST ∈ [3,4]            # (b): burst 3–4 (R1/R4)
newQ    = rampByDifficulty(newCards)[: min(newLimit, MAX_NEW)]     # ★ R7: sắp độ khó tăng dần
reviewQ = scoreAndSort(dueCards)                     # ★ R2: theo priority §3 (KHÔNG next_review_date ASC)

presented = 0 ; pending = [] ; lastReview = null
while newQ or reviewQ or pending:
    moveReadyPendingIntoReviewQ(pending, presented)              # FR-FC-73/74 (re-queue) — R6 qua step-offset

    if len(reviewQ) >= REVIEW_TRIGGER or not newQ:               # FR-FC-70 / FR-FC-72 (gate giữ nguyên)
        burst = 0 ; target = chooseBurst(reviewQ, newQ)          # 3 hoặc 4 (cân tỉ lệ 30–40%, R4)
        while reviewQ and burst < target:
            card = popForBurst(reviewQ, lastReview)              # ★ R7: chống 2 'hard' liền nhau
            serveAndRate(card); lastReview = card; burst++
    elif newQ:                                                   # FR-FC-71
        serveAndRate(newQ.pop(0))

chooseBurst(reviewQ, newQ):
    # 5 NEW : 3 REVIEW ≈ 37.5%; nâng 4 khi backlog ôn lớn so với NEW còn lại, chặn ratio ≤ 40%
    return 4 if (len(reviewQ) > len(newQ) and projectedRatio(+1) <= 0.40) else 3
```

**R1/R3** (≤5 NEW liên tiếp, sau đó chèn ôn): xấp xỉ tự nhiên — gate phục vụ tối đa các NEW cho tới khi backlog REVIEW chạm 5 rồi xả burst; learning-steps liên tục bơm thẻ trả lời chưa chắc vào reviewQ nên backlog chạm ngưỡng đều đặn. **R4** (30–40%): điều tiết bằng `REVIEW_BURST 3–4` + `chooseBurst`.

---

## 6. GHI CHÚ TRIỂN KHAI

### 6.1 Đổi tối thiểu trong `FlashcardSrsService.getSession`

1. Thay sắp xếp `reviewQ` từ `ORDER BY next_review_date ASC` → tính `priority` (§3) trong bộ nhớ rồi sort DESC.
2. Nạp `correct_rate`/`difficulty` từ `last_rating`/`ease_factor` (không truy vấn thêm bảng).
3. Thêm `popForBurst` (chống 2 hard liền nhau) và `chooseBurst` (3–4).
4. `rampByDifficulty(newQ)` khi nạp thẻ mới.

> Không migration, không cột mới, không đổi SM-2/learning-steps/caps.

### 6.2 Đường nâng cấp `correct_rate` (tùy chọn, sau)

Thêm `times_seen INT`, `times_correct INT` vào `flashcards` (migration mới) ⇒ `correct_rate = times_correct / NULLIF(times_seen,0)`. Khi có, thay hàm xấp xỉ. Ngoài phạm vi bản này.

---

## 7. FR MỚI (đưa vào §3.6)

| ID | EARS Requirement |
|:---|:---|
| FR-FC-82 | WHEN building/refilling the REVIEW queue, THE SYSTEM SHALL order review cards by a priority score `0.5·(1−correctRate) + 0.3·staleness + 0.2·difficulty` (DESC), replacing the `next_review_date ASC` ordering in FR-FC-70/72/79. `correctRate` is approximated from `last_rating` (`easy=0.9, hard=0.6, wrong=0.1`); `difficulty=(2.5−ease_factor)/1.2` clamped [0,1]; `staleness=clamp(overdueDays/14,0,1)`. |
| FR-FC-83 | WHEN serving a REVIEW burst, THE SYSTEM SHALL avoid presenting two `hard`-band cards (`difficulty ≥ 0.66`) consecutively if a non-hard card exists within the next 3 queued cards; AND THE SYSTEM SHALL admit NEW cards ordered by ascending difficulty. `REVIEW_BURST` is 3–4 (default 3; 4 when review backlog exceeds remaining NEW and the projected review ratio stays ≤ 40%). |

> FR-FC-82/83 **không** đổi DB, SM-2, learning-steps hay điều kiện kết thúc. AC gợi ý: thẻ trả lời sai gần đây xuất hiện sớm hơn thẻ chỉ "đến hạn"; không có 2 thẻ hard liền nhau khi còn lựa chọn.
