## Tổng Quan

**SakuJi** (さくじ — 桜 sakura + 字 ký tự) là nền tảng học tiếng Nhật dành cho người luyện thi JLPT (N5 → N1). Ngôn ngữ thị giác của sản phẩm là **"Hanami E-learning"**: mềm mại, nở rộ và lấy cánh hoa làm cảm hứng chính, gợi cảm giác ngồi dưới tán hoa anh đào mùa xuân và mở một cuốn giáo trình. Mascot là **Saku-chan**: một cánh hoa anh đào tròn trịa, mũm mĩm (花びら) đã sống dậy, có thân màu hồng dịu, mắt chấm nhỏ, má ửng hồng, đôi tay bé xíu và vương miện ba cánh hoa màu hồng trên đầu. Saku-chan phản ứng với mọi khoảnh khắc quan trọng: xoay tròn phấn khích khi đạt mốc streak, rũ xuống buồn bã khi trả lời sai, hoặc trôi nhẹ nhàng ở trạng thái chờ như một cánh hoa trong gió.

Câu chuyện màu sắc mở đầu bằng **sakura pink** làm màu thương hiệu chính: ấm áp, dễ gần và đậm chất Nhật Bản. Màu này được hỗ trợ bởi **emerald green** cho các CTA hành động tích cực, **soft gold** cho khoảnh khắc thành tựu, và nền **ivory white** gợi giấy washi. Không dùng xanh corporate, không dùng chrome tối nặng nề. Mọi bề mặt đều có cảm giác thoáng như không khí mùa xuân.

Typography dùng hai họ chữ: **Nunito** (rounded humanist sans) cho toàn bộ chữ UI, label và button, vì các đầu nét tròn của Nunito đồng điệu với hình học cánh hoa của mascot; **Noto Sans JP** là fallback CJK bắt buộc để hiển thị kanji, kana và furigana. Nunito Black (800) chỉ dành cho số lớn và label CTA hero.

Button có dạng pill: `border-radius: 9999px` cho toàn bộ CTA primary và secondary, gợi đường viền oval của cánh hoa đang rơi. Card dùng bo góc rộng rãi (12–24 px). Không có phần tử UI nào bo góc 0 px.

**Đặc Trưng Chính:**

- Sakura pink (`#E89AAA`) là màu thương hiệu chính, dùng cho trạng thái nav active, chữ nhấn mạnh và chỉ báo streak.
- Emerald green (`#5DBB69`) dùng cho CTA hành động tích cực ("HỌC TỪ MỚI"), phản hồi trả lời đúng và stat card ngày học.
- Soft gold (`#F7C948`) dùng cho thành tựu số từ đã học, trang trí ngôi sao và ngọn lửa streak.
- Ivory canvas (`#FAF7F4`) thay cho xám phẳng, tạo cảm giác giấy washi ấm áp và liên kết toàn bộ chủ đề sakura.
- Saku-chan xuất hiện trên mọi màn hình quan trọng: idle (trôi nhẹ), happy (xoay), correct (nhảy + cánh hoa), wrong (héo nhẹ), empty state (ngủ trên cành).
- Dashboard ba cột (sidebar Streak | nội dung chính | sidebar thống kê), thu gọn còn một cột trên mobile.
- Mỗi page/component React có file CSS riêng đặt cạnh nhau; không dùng CSS Modules hoặc markup nặng utility class.

---

## Màu Sắc

### Thương Hiệu & Nhấn Mạnh

- **Sakura Pink** (`--color-primary` — `#E89AAA`): Dấu ấn thương hiệu. Màu hồng sakura pastel dịu, dùng cho gạch chân nav tab active, gradient của streak card và chữ nhấn mạnh inline. Không dùng làm nền fill lớn.
- **Petal Light** (`--color-primary-light` — `#F7CBD4`): Hover state, điểm dừng cuối của gradient streak card, tint badge mềm và hiệu ứng cánh hoa.
- **Deep Petal** (`--color-primary-dark` — `#D84F68`): Trạng thái pressed/active của các phần tử dùng màu primary.
- **Blossom Tint** (`--color-primary-bg` — `#FFF0F3`): Nền hover nav tab, nền chip primary và các hàng highlight liên quan đến streak.

### Bề Mặt

- **Washi** (`--color-bg` — `#FAF7F4`): Nền mặc định của trang, màu ivory ấm gợi giấy washi Nhật Bản và giúp card trắng nổi lên mà không tương phản gắt.
- **Card** (`--color-card` — `#FFFFFF`): Bề mặt cho toàn bộ content card, modal và panel.
- **Secondary Tint** (`--color-secondary-bg` — `#F4FBF5`): Nền stat card cho biến thể "days studied".
- **Gold Tint** (`--color-accent-bg` — `#FFF7DD`): Nền stat card cho biến thể "words learned".

### Văn Bản

- **Ink** (`--color-text` — `#2D2D2D`): Toàn bộ body text, heading và label.
- **Mist** (`--color-text-sub` — `#6B625E`): Label phụ, nav tab inactive, caption và placeholder.
- **Disabled** (`--color-text-disabled` — `#B7ABA5`): Form field và button bị vô hiệu hóa.
- **Divider** (`--color-border` — `#E8E0DC`): Border input, đường phân tách card, cạnh dưới TopNav; hơi ấm để khớp nền washi.

### Ngữ Nghĩa

- **Success / Go** (`--color-secondary` — `#5DBB69`): Fill button CTA chính, phản hồi trả lời đúng, border stat card ngày học.
- **Achievement / Gold** (`--color-accent` — `#F7C948`): Border stat card số từ, fill icon ngôi sao, ánh sáng ngọn lửa streak, achievement badge.
- **Error** (`--color-error` — `#E57373`): Border field lỗi, flash trả lời sai, thông báo lỗi inline.
- **Warning** (`--color-warning` — `#F4A261`): Cảnh báo gần hạn, cảnh báo VIP sắp hết hạn.

### Màu Cấp Độ JLPT

Mỗi cấp JLPT có một cặp màu chip riêng, dùng nhất quán trên badge, filter tab và lesson header:

| Level | Background | Text | Cảm Giác |
| --- | --- | --- | --- |
| N5 | `#E8F5E9` | `#2E7D32` | Xanh mầm non — những bước đầu tiên |
| N4 | `#E3F2FD` | `#1565C0` | Xanh trời — bắt đầu tự tin hơn |
| N3 | `#FFF3E0` | `#E65100` | Hổ phách ấm — giai đoạn trung cấp |
| N2 | `#F3E5F5` | `#6A1B9A` | Tím tử đằng — nâng cao |
| N1 | `#FCE4EC` | `#C62828` | Đỏ sakura đậm — thành thạo |

---

## Typography

### Font Family

Hai họ chữ bao phủ toàn bộ nhu cầu:

1. **Nunito** — font UI chính. Các đầu nét tròn gợi lại đường cong mềm của cánh hoa Saku-chan và thẩm mỹ hanami tổng thể. Load các weight 400, 600, 700, 800. Dùng cho mọi chuỗi UI: label nav, heading, body, button, caption, số liệu.
2. **Noto Sans JP** — fallback CJK bắt buộc, load cùng Nunito để bảo đảm kanji, kana và furigana có đủ glyph. Runtime chỉ cần weight 400; phần Latin kế thừa weight từ Nunito.

```css
font-family: 'Nunito', 'Noto Sans JP', system-ui, sans-serif;
```

### Phân Cấp

| Token | Size | Weight | Line Height | Dùng Cho |
| --- | --- | --- | --- | --- |
| `display-xl` | 36px | 800 | 1.25 | Điểm hero, mốc streak cần ăn mừng |
| `display-lg` | 30px | 800 | 1.25 | Title cấp trang (Dashboard, Kanji) |
| `heading-lg` | 24px | 700 | 1.3 | Heading section, title modal |
| `heading-md` | 20px | 700 | 1.35 | Heading card, câu hỏi quiz |
| `heading-sm` | 18px | 600 | 1.4 | Label tiểu mục |
| `body-lg` | 16px | 400 | 1.5 | Body text mặc định |
| `body-md` | 14px | 400 | 1.5 | Label form, mô tả |
| `body-sm` | 12px | 400 | 1.5 | Caption, timestamp, chữ nhỏ |
| `label-md` | 14px | 600 | 1 | Label nav tab, label input |
| `label-sm` | 12px | 700 | 1 | Badge, chip, tag cấp JLPT |
| `button-lg` | 15px | 800 | 1 | CTA primary ("HỌC TỪ MỚI") — uppercase |
| `button-md` | 14px | 700 | 1 | Button tiêu chuẩn |
| `number-xl` | 48px | 800 | 1 | Số streak, điểm lớn |

### Nguyên Tắc

- **Nunito weight 800 chỉ dùng cho số và hero CTA.** Lạm dụng black weight sẽ làm mất cảm giác mềm mại, dễ gần.
- **Không dùng font nhỏ hơn 12px.** Ký tự tiếng Nhật cần khoảng thở để dễ đọc; 12px là ngưỡng tối thiểu tuyệt đối.
- **Furigana (ruby text) hiển thị bằng 50% kích thước kanji cha.** Với kanji 20px, ruby là 10px. Đây là ngoại lệ duy nhất được chấp nhận dưới ngưỡng 12px, cần thiết cho typesetting tiếng Nhật đúng chuẩn.
- **Uppercase chỉ dùng cho label CTA primary.** Không uppercase nav, heading hoặc body copy.
- **Chữ màu sakura pink chỉ dùng cho thuật ngữ nhấn mạnh** ("Thời Điểm Vàng", thông báo lên level). Không tô màu hồng cho đoạn văn body.

---

## Layout

### Hệ Thống Spacing

Đơn vị cơ sở: **4px**.

| Token | Value | Dùng Cho |
| --- | --- | --- |
| `--space-1` | 4px | Padding trong icon, gap inline rất nhỏ |
| `--space-2` | 8px | Khoảng giữa badge và label, gap checkbox |
| `--space-3` | 12px | Padding nội bộ form field |
| `--space-4` | 16px | Padding card compact, padding nav item |
| `--space-5` | 20px | Margin-bottom của form group |
| `--space-6` | 24px | Padding card mặc định, gap section |
| `--space-8` | 32px | Khoảng giữa các section lớn trong page |
| `--space-10` | 40px | Padding-x của auth card |
| `--space-12` | 48px | Nhịp dọc cấp trang |
| `--space-16` | 64px | Chiều cao TopNav |

### Grid & Container

**Dashboard layout** — ba cột đặt tên rõ ràng trong một hàng flex ngang:

- Sidebar trái: `width: 220px; flex-shrink: 0` — chứa StreakCard với Saku-chan.
- Nội dung chính: `flex: 1; min-width: 0` — vùng học tập chính.
- Sidebar phải: `width: 200px; flex-shrink: 0` — các StatCard xếp dọc.

**Auth layout** — một card căn giữa:

- Max-width 440px, padding 40px, margin ngang auto.
- Nền trắng, border-radius 16px, shadow Level 2.

**Exam layout** — không có sidebar:

- Max-width 720px, căn giữa, progress bar ghim dưới TopNav.

### Chiến Lược Responsive

#### Breakpoint

| Name | Width | Thay Đổi Chính |
| --- | --- | --- |
| Mobile | < 768px | Ẩn cả hai sidebar; một cột; hamburger nav |
| Tablet | 768–1199px | Ẩn sidebar trái; sidebar phải tùy chọn |
| Desktop | ≥ 1200px | Dashboard đủ ba cột |

#### Touch Target

Mọi phần tử tương tác phải đạt tối thiểu **44 × 44px**. Nav tab, icon button và checkbox label đạt ngưỡng này bằng `min-height` hoặc `padding` rõ ràng.

#### Chiến Lược Thu Gọn

- TopNav tabs: desktop hiển thị đủ label + icon → tablet chỉ icon → mobile dùng hamburger drawer.
- Dashboard sidebars: ẩn ở tablet trở xuống; có thể truy cập qua slide-in drawer nếu cần.
- Quiz answer grid: 2 cột trên desktop → 1 cột trên mobile.

#### Hành Vi Hình Ảnh

- Mascot Saku-chan: PNG nền trong suốt, căn giữa trong vùng nội dung, không có card background phía sau. Size qua prop: `sm` 80px / `md` 160px / `lg` 300px.
- Trang trí cánh hoa rơi (chi tiết nền tùy chọn): các cánh hoa SVG nhỏ, bán trong suốt, `pointer-events: none`, `position: absolute`, `opacity: 0.12`; chỉ dùng ở vùng hero dashboard.
- Avatar người dùng: crop tròn, 36px trong TopNav, 80px trong profile page.

---

## Elevation & Depth

| Level | Treatment | Dùng Cho |
| --- | --- | --- |
| Level 0 — Flat | Không shadow, không border | Nền canvas washi |
| Level 1 — Card | `box-shadow: 0 2px 8px rgba(0,0,0,0.07)` | Content card, input field trên nền trắng |
| Level 2 — Raised | `box-shadow: 0 4px 12px rgba(0,0,0,0.10)` | Stat card, mặt flashcard, CTA button |
| Level 3 — Floating | `box-shadow: 0 8px 24px rgba(0,0,0,0.12)` | Modal, dropdown menu, toast notification |
| Petal Glow | `box-shadow: 0 2px 10px rgba(232,154,170,0.22)` | StreakCard và card tương tác primary-pink khi hover |

Border chỉ dùng cho trạng thái ngữ nghĩa: error (đỏ), focus (vòng hồng) và biến thể stat card (vàng/xanh). Divider cấu trúc chỉ dùng `border-bottom: 1px solid var(--color-border)` trên TopNav. Không dùng border cứng trên card.

---

## Shapes

### Thang Border Radius

| Token | Value | Dùng Cho |
| --- | --- | --- |
| `--radius-sm` | 8px | Button lựa chọn đáp án quiz, tag chip nhỏ |
| `--radius-md` | 12px | Content card, form input, panel mặc định |
| `--radius-lg` | 16px | Stat card, sidebar panel |
| `--radius-xl` | 24px | StreakCard, modal, auth container |
| `--radius-full` | 9999px | Toàn bộ CTA button dạng pill, avatar, badge chip |

Thương hiệu **không bao giờ dùng góc 0px** trên bất kỳ phần tử tương tác nào. Ngay cả chip nhỏ nhất cũng dùng `--radius-sm` 8px. Dạng pill của primary button trực tiếp gợi lại silhouette oval của cánh hoa anh đào đang rơi.

### Hình Học Saku-chan

- Saku-chan nằm trong bounding box vuông; silhouette cánh hoa hữu cơ được xử lý hoàn toàn bằng illustration, CSS không clip.
- Render dưới dạng PNG nền trong suốt đặt trực tiếp lên canvas, không bọc trong card hoặc nền màu.
- Vương miện cánh hoa trên đầu Saku-chan dùng màu crown riêng `#E8637A`, một sắc sakura đậm hơn và khác với brand primary `#E89AAA`. Không bao giờ đổi màu crown.

---

## Components

### TopNav

Chiều cao 64px, nền trắng, `border-bottom: 1px solid var(--color-border)`.
Layout: `[Logo + wordmark "SakuJi" 160px] [Nav tabs — flex center] [User area — right]`

**`topnav-tab`** — mỗi item điều hướng:

- Layout: icon (24px) xếp trên label (`label-md`, 14px / 600).
- Inactive: `color: var(--color-text-sub)`.
- Active: `color: var(--color-primary)` + `border-bottom: 2px solid var(--color-primary)`.
- Hover: `background: var(--color-primary-bg)`, `border-radius: --radius-md` trên khối tab.
- Padding: 8px 16px.

**`topnav-user`** — phía bên phải:

- Email text truncate ở 160px, `body-sm`, `color-text-sub`.
- Avatar 36px crop tròn, `border-radius: --radius-full`.

### Cards

**`streak-card`** — panel streak ở sidebar trái có Saku-chan:

- Background: `linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%)`.
- Border-radius: `--radius-xl`. Padding: 16px.
- Title "Ngày Streak": màu trắng, `heading-sm`.
- Số streak: màu trắng, `number-xl` (48px / 800).
- Icon ngọn lửa màu `--color-accent` (gold): full opacity khi streak > 0; `opacity: 0.30` khi streak = 0; `animation: pulse 2s infinite` khi streak > 0.
- Saku-chan biến thể `sm` (80px): đặt bottom-left, trạng thái `happy` khi streak > 0, `idle` khi streak = 0.

**`stat-card`** — panel metric ở sidebar phải:

- `type="words"`: `background: var(--color-accent-bg)`, `border: 2px solid var(--color-accent)`.
- `type="streak"`: `background: var(--color-secondary-bg)`, `border: 2px solid var(--color-secondary)`.
- Border-radius: `--radius-lg`. Padding: 16px. Width: 100% trong cột 200px.
- Label: `body-sm`, `color-text-sub`. Value: `heading-lg`, `color-text`.
- Icon trang trí: 40px, góc trên bên phải, `opacity: 0.20`.

**`content-card`** — panel nội dung trắng dùng chung:

- Background: `var(--color-card)`. Border-radius: `--radius-md`. Padding: 24px. Shadow: Level 2.

### Buttons

**`btn-primary`** — CTA hành động chính:

- Background: `var(--color-secondary)` (green). Color: white.
- Label: `button-lg`, uppercase. Border-radius: `--radius-full` (pill).
- Padding: 14px 48px. Min-width: 180px.
- Shadow: Level 2. Hover: `filter: brightness(1.08)`. Active: `transform: scale(0.97)`.
- Disabled: `opacity: 0.60`, `cursor: not-allowed`.

**`btn-secondary`** — biến thể outline:

- Background: transparent. Border: `2px solid var(--color-primary)`. Color: `var(--color-primary)`.
- Border-radius: `--radius-full`. Padding: 12px 32px.
- Hover: `background: var(--color-primary-bg)`.

**`btn-ghost`** — mức nhấn thấp:

- Background: transparent. Color: `var(--color-text-sub)`. Không border.
- Hover: `color: var(--color-text)`.

**`btn-icon`** — icon button tròn 44 × 44px:

- Background: white. Shadow: Level 1. Border-radius: `--radius-full`.
- Hover: `background: var(--color-primary-bg)`.

### Forms

**`form-input`** — text input tiêu chuẩn:

- Background: `#FAF7F4` (washi tint). Border: `1.5px solid var(--color-border)`. Border-radius: `--radius-md`.
- Height: 48px. Padding: 0 16px. Font: `body-lg`.
- Focus: `border-color: var(--color-primary)`, `box-shadow: 0 0 0 3px rgba(232,154,170,0.18)`, `background: white`.
- Error state (`.has-error`): `border-color: var(--color-error)`, `background: #FEF2F2`.
- Error focus: `box-shadow: 0 0 0 3px rgba(229,115,115,0.12)`.

**`field-error`** — lỗi inline bên dưới input:

- `font-size: 12px`, `color: var(--color-error)`, `margin-top: 4px`.

### Navigation Chips

**`jlpt-badge`** — chỉ báo level:

- Border-radius: `--radius-full`. Padding: 3px 10px. Font: `label-sm` (12px / 700).
- Cặp màu: xem bảng Màu Cấp Độ JLPT phía trên.
- Dùng trong: course card, lesson header, filter tab, search result.

### Feedback

**`progress-bar`** — tiến độ tuyến tính:

- Track: `height: 8px`, `background: var(--color-border)`, `border-radius: --radius-full`.
- Fill: `background: var(--color-primary)` (pink) cho tiến độ học chung; `var(--color-secondary)` (green) cho mức hoàn thành quiz/exam.
- Transition: `width 0.3s ease`.

**`loading-spinner`** — chỉ báo xoay:

- Size: `sm` 20px / `md` 40px / `lg` 60px.
- Color: `var(--color-primary)`. Animation: `spin 0.8s linear infinite`.

**`empty-state`** — màn hình không có dữ liệu:

- Saku-chan biến thể `md` (160px) ở tư thế `thinking` hoặc đang ngủ, căn giữa.
- Title: `heading-md`. Description: `body-lg`, `color-text-sub`.
- Có thể có CTA button bên dưới mô tả.
- **Không bao giờ hiển thị trang trống** — luôn render component này khi list rỗng.

**`toast`** — thông báo ngắn hạn:

- Vị trí: fixed top-right, `z-index: 9999`. Width: 320px.
- Border-radius: `--radius-md`. Shadow: Level 3. Padding: 14px 16px.
- Biến thể: success (left-border xanh), error (left-border đỏ), warning (left-border amber), info (left-border xanh dương).
- Tự đóng: 3 giây.

### Mascot Saku-chan

**`saku-chan`** — nhân vật của SakuJi:

- Render dưới dạng PNG nền trong suốt đặt trực tiếp lên canvas; không có card bọc hoặc container màu.
- Size qua prop: `sm` 80px / `md` 160px / `lg` 300px.
- Biến thể animation qua CSS class:

| Variant | CSS animation | Trigger |
| --- | --- | --- |
| `idle` | `sway 3s ease-in-out infinite` (nghiêng trái-phải nhẹ) | Mặc định / chưa có hành động người dùng |
| `happy` | `spin-bounce 0.6s ease` rồi quay về idle | Mốc streak, lên level |
| `correct` | `jump 0.4s ease` + overlay cánh hoa confetti | Trả lời quiz đúng |
| `wrong` | `wilt 0.3s ease` (rũ xuống rồi hồi lại) | Trả lời quiz sai |
| `thinking` | `peek 2s ease infinite` (mắt liếc qua lại) | Loading, empty state |
| `celebrate` | `spin 0.8s ease` + SVG cánh hoa rơi | Đậu exam, kỷ lục streak |

Confetti cánh hoa rơi ở biến thể `celebrate`: 6–8 shape SVG cánh hoa, `position: absolute`, `animation-delay` ngẫu nhiên (0–0.6s), rơi từ phía trên bounding box của mascot, `opacity: 0` ở cuối. Cánh hoa dùng fill `var(--color-primary-light)`.

---

## Nên Và Không Nên

### Nên

- Dùng `var(--color-primary)` sakura pink **chỉ** cho dấu nhấn, chỉ báo active và gradient streak card. Không fill một bề mặt lớn bằng màu này.
- Dùng `var(--color-secondary)` green cho button hành động chính trên mọi màn hình học và luyện tập. Green = "go learn", tạo tín hiệu an toàn và khích lệ trên nền thương hiệu hồng.
- Áp dụng `--radius-full` (pill) cho mọi CTA button. Pill oval trực tiếp gợi cánh hoa anh đào đang rơi, là hình dạng tương tác dễ nhận diện nhất của thương hiệu.
- Hiển thị Saku-chan đúng biến thể cảm xúc trên mọi màn hình quan trọng. Mascot không được đứng yên trên màn hình vừa đưa phản hồi cho người dùng.
- Khớp chính xác các cặp màu badge cấp JLPT, vì đây là tín hiệu UX quan trọng giúp học viên điều hướng nội dung nhiều cấp.
- Luôn có skeleton/spinner `isLoading` và thông báo `error` cho mọi component dựa trên API.
- Dùng washi canvas (`--color-bg` `#FAF7F4`) làm nền trang; không dùng `#FFFFFF` hoặc `#F5F5F5` thuần vì sẽ phá vỡ không khí mùa xuân ấm áp.

### Không Nên

- Không dùng góc 0px trên bất kỳ phần tử tương tác nào; thương hiệu không có cảm giác sắc cạnh hoặc hình học cứng.
- Không đặt Saku-chan trong card hoặc trên panel màu; mascot nổi trực tiếp trên canvas, không có nền phía sau.
- Không dùng `var(--color-primary)` sakura pink làm nền fill lớn; màu này sẽ lấn át chữ và mất chức năng nhấn.
- Không hard-code giá trị hex màu trong CSS component; luôn tham chiếu biến CSS `--color-*`.
- Không hiển thị trang trắng hoặc vùng trắng rỗng; luôn render `<EmptyState />` với Saku-chan hoặc skeleton loader.
- Không thêm font family thứ ba. Nunito + Noto Sans JP là bộ typography hoàn chỉnh.
- Không dùng font weight trên 800 hoặc dưới 400, và không bao giờ dùng weight 800 cho body text.
- Không dùng uppercase ở đâu ngoài label button CTA primary.
- Không dùng xanh dương làm màu thương hiệu. Màu xanh duy nhất được phép xuất hiện là badge JLPT N4; đó là chỉ báo level, không phải thành phần thương hiệu.
