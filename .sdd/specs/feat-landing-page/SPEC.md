# SPEC — Landing Page (Home)
> **Feature ID:** `feat-landing-page`
> **Route:** `/` (public, không cần auth)
> **Version:** 1.0 | **Status:** Draft
> **Author:** Team | **Last Updated:** 2026-05-31
> **Design ref:** `DESIGN.md` — SakuJi · Hanami E-learning

---

## 1. TỔNG QUAN TRANG

Trang chủ công khai (không cần đăng nhập). Mục tiêu duy nhất: **chuyển đổi khách truy cập thành người đăng ký**. Toàn bộ nội dung dẫn đến một trong hai hành động: "Get started" (đăng ký) hoặc "Đăng nhập".

**Cấu trúc sections theo thứ tự từ trên xuống:**

```
[1] TopBar        — Logo + nav links + Login button
[2] Hero          — Headline + CTA chính + Saku-chan illustration
[3] Feature-A     — "Bạn học tiếng Nhật như thế nào?" (2-col xen kẽ)
[4] Feature-B     — "Ngoài Kanji, bạn sẽ được học" (3-card ngang)
[5] Footer        — Links + copyright + Saku-chan nhỏ
```

**File structure:**
```
apps/frontend/src/
├── pages/
│   └── home/
│       ├── Home.jsx              ← page root, ghép các section
│       ├── Home.css
│       ├── sections/
│       │   ├── TopBar.jsx        ← navigation bar trang chủ
│       │   ├── TopBar.css
│       │   ├── HeroSection.jsx
│       │   ├── HeroSection.css
│       │   ├── FeatureASection.jsx
│       │   ├── FeatureASection.css
│       │   ├── FeatureBSection.jsx
│       │   ├── FeatureBSection.css
│       │   └── FooterSection.jsx
│       │   └── FooterSection.css
│       └── assets/               ← SVG illustrations riêng cho landing
│           ├── saku-chan-hero.svg
│           ├── saku-chan-float.svg
│           ├── saku-chan-phone.svg
│           ├── kanji-card-1.svg … kanji-card-5.svg
│           ├── spaced-rep-curve.svg
│           ├── phone-mockup.svg
│           ├── feat-vocab.svg
│           ├── feat-globe.svg
│           └── feat-dictionary.svg
```

---

## 2. DESIGN TOKENS ÁP DỤNG

Lấy từ `DESIGN.md` — chỉ ghi lại giá trị dùng trong landing page:

```css
/* Màu */
--color-primary:      #E8637A;   /* sakura pink */
--color-primary-light:#F4A7B3;
--color-primary-bg:   #FFF0F3;
--color-secondary:    #4CAF50;   /* green — CTA chính */
--color-accent:       #F5C842;   /* gold */
--color-bg:           #FAF7F4;   /* washi canvas */
--color-card:         #FFFFFF;
--color-text:         #2D2D2D;
--color-text-sub:     #757575;
--color-border:       #E8E0DC;

/* Landing-page only */
--color-cream:        #F7F4E6;   /* nền section Feature-B */
--color-hero-bg:      #FFF8FA;   /* tông hồng rất nhạt cho hero */

/* Radius */
--radius-md:   12px;
--radius-lg:   16px;
--radius-xl:   24px;
--radius-full: 9999px;

/* Shadow */
--shadow-card: 0 4px 20px rgba(0,0,0,0.08);
--shadow-hero: 0 8px 40px rgba(232,99,122,0.12);
```

---

## 3. SECTION 1 — TOPBAR

### Layout
```
┌──────────────────────────────────────────────────────────────────┐
│ [🌸 SakuJi logo]      [Tính năng][Bảng giá][Blog]    [Đăng nhập][Get started▶]│
└──────────────────────────────────────────────────────────────────┘
```

### Spec chi tiết

```
Vị trí:    sticky top: 0; z-index: 100
Height:    68px
Background: rgba(255,255,255,0.92) + backdrop-filter: blur(12px)
Border:    border-bottom: 1px solid var(--color-border)
Padding:   0 max(24px, calc((100vw - 1200px)/2))
```

**Logo — `.topbar-logo`**
```
Layout: flex align-center gap-10px
Icon:   Saku-chan mini SVG (28×28px) — cánh hoa hồng tròn mắt chấm
Text:   "SakuJi"
        font: Nunito 800, 22px
        color: var(--color-text)
        "Saku" — màu var(--color-primary)
        "Ji"   — màu var(--color-text)
Hover:  subtle scale(1.02) trên toàn logo block
```

**Nav links — `.topbar-nav`**
```
Items: Tính năng / Bảng giá / Blog
Font:  Nunito 600, 15px, color: var(--color-text-sub)
Hover: color: var(--color-text)
Gap:   32px giữa các link
Transition: color 150ms ease
```

**Actions — `.topbar-actions`**
```
[Đăng nhập] — btn-ghost, font 600 14px, color var(--color-text-sub)
[Get started →] — btn-primary nhỏ:
  background: var(--color-secondary)
  color: white
  border-radius: var(--radius-full)
  padding: 10px 22px
  font: Nunito 700 14px
  shadow: 0 2px 8px rgba(76,175,80,0.30)
  Hover: brightness(1.08)
  Arrow icon: "→" inline, margin-left: 4px
```

**Responsive:**
- `< 768px`: nav links ẩn → hamburger icon (☰) góc phải. Drawer slide-in từ trái.

---

## 4. SECTION 2 — HERO

### Layout tổng thể
```
Background: linear-gradient(160deg, #FFF8FA 0%, #FAF7F4 60%, #F0FAF0 100%)
            → chuyển sắc nhẹ từ hồng → ivory → xanh lá rất nhạt
Min-height: 100vh (hoặc calc(100vh - 68px))
Display:    grid, 2 columns, gap: 64px
Padding:    80px max(24px, calc((100vw-1200px)/2))
Max-width:  1200px, centered
```

```
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  LEFT (text)                    RIGHT (illustration)         │
│  ─────────────────               ─────────────────────────   │
│  [tag chip]                      [Saku-chan hero SVG]        │
│                                  [floating kanji cards]      │
│  Memorize 1000                   [sakura petals bg]         │
│  Kanji in 1 month                                           │
│                                                              │
│  [sub text]                                                  │
│                                                              │
│  [● Get started]                                             │
│  [  Xem demo    ]                                            │
│                                                              │
│  [stats row: 50K users | 1000 kanji | 4.9★]                │
└──────────────────────────────────────────────────────────────┘
```

### LEFT column — `.hero-content`

**Tag chip — `.hero-tag`**
```
Text:       "🌸 Học tiếng Nhật theo cách của bạn"
Background: var(--color-primary-bg)
Color:      var(--color-primary)
Font:       Nunito 600, 13px
Border:     1px solid var(--color-primary-light)
Border-radius: var(--radius-full)
Padding:    6px 16px
Display:    inline-flex, margin-bottom: 24px
```

**Headline — `.hero-headline`**
```
Text (2 dòng):
  Line 1: "Memorize 1000 Kanji"
  Line 2: "in 1 month"         ← "1 month" màu var(--color-primary)

Font:       Nunito 800, clamp(36px, 5vw, 60px)
Color:      var(--color-text)
Line-height: 1.1
Letter-spacing: -1px
Margin-bottom: 20px
```

**Sub-text — `.hero-sub`**
```
Text:   "Phương pháp Thời Điểm Vàng giúp bạn nhớ lâu hơn,
         học ít hơn, và đạt JLPT N1 nhanh hơn bao giờ hết."
Font:   Nunito 400, 17px
Color:  var(--color-text-sub)
Max-width: 440px
Line-height: 1.65
Margin-bottom: 36px
```

**CTA group — `.hero-cta`**
```
Layout: flex, flex-direction: column, gap: 12px, align-items: flex-start

[btn-get-started]:
  Text:           "Get started"
  Background:     var(--color-secondary)
  Color:          white
  Font:           Nunito 800, 17px, uppercase, letter-spacing: 0.5px
  Border-radius:  var(--radius-full)
  Padding:        16px 48px
  Shadow:         0 4px 16px rgba(76,175,80,0.35)
  Hover:          translateY(-2px) + shadow stronger
  Active:         scale(0.97)
  Icon:           → arrow right, margin-left: 8px

[btn-demo]:
  Text:           "Xem thử demo"
  Background:     white
  Color:          var(--color-text)
  Font:           Nunito 600, 15px
  Border-radius:  var(--radius-full)
  Border:         1.5px solid var(--color-border)
  Padding:        14px 40px
  Hover:          border-color var(--color-primary), color var(--color-primary)
```

**Stats row — `.hero-stats`**
```
Layout: flex, gap: 32px, margin-top: 48px
Items (3):
  [50K+]   "Học viên đang học"
  [1.000]  "Kanji được luyện tập"
  [4.9 ★]  "Đánh giá trung bình"

Each stat:
  Number: Nunito 800, 24px, color: var(--color-text)
  Label:  Nunito 400, 13px, color: var(--color-text-sub)
  Divider: 1px solid var(--color-border) giữa các stat (dọc)
```

### RIGHT column — `.hero-illustration`

```
Position: relative
Display:  flex, align-items: center, justify-content: center
```

**Saku-chan hero — `.hero-mascot`**
```
File:     assets/saku-chan-hero.svg
Width:    clamp(280px, 35vw, 420px)
Position: relative, z-index: 2
Animation: float 4s ease-in-out infinite
  @keyframes float {
    0%, 100% { transform: translateY(0); }
    50%       { transform: translateY(-14px); }
  }

Saku-chan hero variant (mô tả illustration):
  - Dáng đứng, nhìn thẳng, tay giơ cao cầm bút lông (筆)
  - Thân tròn mũm mĩm, màu trắng hồng nhạt (#FFF0F3)
  - Má đỏ ửng, mắt chấm đen sáng lấp lánh
  - Vương miện 3 cánh hoa sakura màu #E8637A trên đầu
  - Mang tạp dề trắng nhỏ với chữ 「学」(gaku = học)
```

**Floating kanji cards — `.hero-kanji-card` (×5)**
```
Mỗi card:
  Background: white
  Border-radius: var(--radius-md)
  Shadow: var(--shadow-card)
  Padding: 10px 14px
  Layout: kanji lớn + furigana nhỏ + nghĩa VI

Card content (5 cards):
  1. 「山」やま  — Núi   (top-left của mascot)
  2. 「水」みず  — Nước  (top-right)
  3. 「花」はな  — Hoa   (bottom-left)
  4. 「空」そら  — Bầu trời (mid-right)
  5. 「日」にち  — Mặt trời (bottom-right)

Kanji: Noto Sans JP 700, 28px, color var(--color-text)
Furigana: Nunito 400, 11px, color var(--color-text-sub)
Nghĩa: Nunito 600, 12px, color var(--color-primary)

Position: absolute, mỗi card offset khác nhau
Animation: floatCard 3s ease-in-out infinite
  animation-delay: mỗi card +0.5s lệch nhau
```

**Background sakura petals — `.hero-petals`**
```
SVG petals (6–8 chiếc):
  Fill: var(--color-primary-light) opacity 0.18
  Size: 20–40px mỗi cánh
  Position: absolute, scattered quanh mascot
  Animation: petalDrift 6s ease-in-out infinite alternate
    transform: rotate + translateY nhẹ
  pointer-events: none
```

**Background blob — `.hero-blob`**
```
SVG blob hữu cơ (không tròn đều, không vuông — giống "amíp"):
  Fill: var(--color-primary-bg) opacity 0.6
  Width: clamp(300px, 45vw, 500px)
  Position: absolute, centered behind mascot
  z-index: 1
  Animation: blobMorph 8s ease-in-out infinite
    (SVG path morphing nhẹ nếu dùng GSAP, hoặc scale/rotate nếu CSS only)
```

**Responsive Hero:**
```
< 1024px: 2 col → 1 col; illustration above text
< 768px:  mascot width 200px; headline font-size 32px; stats 2-up grid
```

---

## 5. SECTION 3 — FEATURE-A "Bạn học tiếng Nhật như thế nào?"

### Layout tổng thể
```
Background: var(--color-bg) — washi #FAF7F4
Padding:    100px max(24px, calc((100vw-1200px)/2))
```

**Section header — `.feat-a-header`**
```
Text:       "Bạn học tiếng Nhật như thế nào?"
Font:       Nunito 800, clamp(28px, 4vw, 44px)
Color:      var(--color-text)
Text-align: center
Max-width:  640px, margin: 0 auto 72px
```

### Row 1 — Spaced Repetition (Illustration trái | Text phải)

```
┌─────────────────────────────────────────────────────────────┐
│  LEFT (illustration, 50%)      RIGHT (text, 50%)            │
│  ─────────────────────         ─────────────────────────    │
│  [Saku-chan float SVG]         [Nhãn xanh lá]               │
│  [kanji cards bay xung quanh] [Heading xanh lá đậm]         │
│  [Spaced Rep Curve chart]     [Description text]            │
│  ["Time to review" label]     [● CTA trắng viên thuốc]      │
└─────────────────────────────────────────────────────────────┘
```

**LEFT — `.feat-a-illus`**
```
Position: relative
Height: 480px

[Saku-chan float]:
  File: assets/saku-chan-float.svg
  Width: 180px
  Position: absolute, center-left
  Animation: float 4s ease-in-out infinite
  Variant mô tả:
    - Dáng ngồi thiền (khoanh tay, mắt nhắm)
    - Vây quanh là các kanji card nhỏ bay lơ lửng theo quỹ đạo ellipse
    - Ánh sáng gold nhỏ sparkle xung quanh (★ icons)

[Kanji orbit cards] (3–4 thẻ):
  Tương tự hero-kanji-card nhưng nhỏ hơn (padding 8px 10px)
  Animation: orbit 6s linear infinite theo path ellipse
  animation-delay khác nhau mỗi thẻ

[Spaced Rep Curve — assets/spaced-rep-curve.svg]:
  SVG minh họa đường cong ghi nhớ:
    - Trục X: "Time" (các mốc: Day 1, 3, 7, 14, 30)
    - Trục Y: "Memory %" (0→100%)
    - Đường Forgetting Curve: màu var(--color-border), nét đứt
    - Đường SakuJi curve: màu var(--color-primary), solid, dày hơn
    - Điểm "review" trên đường: chấm tròn màu var(--color-accent) + ripple animation
  Label "Time to review":
    Font: Nunito 700, 12px
    Color: var(--color-accent)
    Background: #FFFDE7
    Border: 1px solid var(--color-accent)
    Border-radius: var(--radius-full)
    Padding: 4px 12px
    Position: absolute, gắn vào điểm review trên curve
  Width: 320px
  Position: absolute, bottom-left của illustration area
```

**RIGHT — `.feat-a-text`**
```
Display: flex, flex-direction: column, justify-content: center
Padding-left: 48px

[Tag nhãn]:
  Text: "Thời Điểm Vàng ✦"
  Font: Nunito 700, 13px
  Color: var(--color-secondary)
  Background: var(--color-secondary-bg)
  Border-radius: var(--radius-full)
  Padding: 5px 14px
  Margin-bottom: 16px

[Heading]:
  Text: "Ghi nhớ Kanji và từ vựng với tính năng Thời Điểm Vàng"
  Font: Nunito 800, clamp(22px, 2.5vw, 32px)
  Color: var(--color-secondary)   ← màu xanh lá nổi bật
  Line-height: 1.25
  Margin-bottom: 16px

[Description]:
  Text: "Thuật toán Spaced Repetition của SakuJi tự động tính toán
         khoảng cách ôn tập tối ưu, giúp bạn nhớ lâu hơn mà học ít hơn.
         Saku-chan sẽ nhắc bạn đúng lúc bạn sắp quên."
  Font: Nunito 400, 16px
  Color: var(--color-text-sub)
  Line-height: 1.7
  Margin-bottom: 32px

[CTA button]:
  Text: "Thử ngay miễn phí →"
  Background: white
  Color: var(--color-text)
  Font: Nunito 700, 15px
  Border-radius: var(--radius-full)
  Border: 1.5px solid var(--color-border)
  Padding: 13px 32px
  Shadow: var(--shadow-card)
  Hover: border-color var(--color-secondary), color var(--color-secondary)
  Width: fit-content
```

---

### Row 2 — Flashcard trên điện thoại (Text trái | Illustration phải)

Bố cục **đảo ngược** so với Row 1.

```
┌─────────────────────────────────────────────────────────────┐
│  LEFT (text, 50%)              RIGHT (illustration, 50%)    │
│  ─────────────────────         ─────────────────────────    │
│  [Tag nhãn]                   [Phone mockup]                │
│  [Heading]                    [Flashcard trên màn hình]     │
│  [Description]                [Saku-chan nhỏ góc dưới]      │
│  [● CTA]                                                    │
└─────────────────────────────────────────────────────────────┘
```

**RIGHT — `.feat-a-phone`**
```
Position: relative
Height: 520px
Display: flex, align-items: center, justify-content: center

[Phone mockup — assets/phone-mockup.svg]:
  Width: 220px
  Position: relative, z-index: 2
  Border-radius: 32px (bo góc phone)
  Shadow: 0 24px 60px rgba(0,0,0,0.15)

  Nội dung màn hình điện thoại (SVG embed):
    - Header "Flashcard N3" — Nunito 600 12px
    - Flashcard trắng giữa màn hình:
        Mặt trước: 「猫」 lớn (Noto Sans JP 60px) + にゃんこ furigana
        Mặt sau:   nghĩa "con mèo" + ví dụ câu
    - Nút "✓ Đã nhớ" màu green + "✗ Ôn lại" màu pink pill-shape
    - Progress bar phía trên: màu var(--color-primary) 60% đầy

[Saku-chan phone — assets/saku-chan-phone.svg]:
  Width: 80px
  Position: absolute, bottom: -16px, right: -16px, z-index: 3
  Variant: `happy` — nhảy lên nhìn vào màn hình phone
  Animation: bounce 1.2s ease-in-out infinite alternate

[Background ring]:
  SVG hình tròn nét đứt màu var(--color-primary-light) opacity 0.3
  Width: 300px
  Position: absolute, center, z-index: 1
  Animation: spin 20s linear infinite
```

**LEFT — `.feat-a-text` (row 2)**
```
[Tag nhãn]:
  Text: "Flashcard thông minh 📱"
  Color: var(--color-primary)
  Background: var(--color-primary-bg)

[Heading]:
  Text: "Luyện Kanji mọi lúc, mọi nơi với Flashcard tương tác"
  Font: Nunito 800, clamp(22px, 2.5vw, 32px)
  Color: var(--color-text)

[Description]:
  Text: "Lật thẻ, tự đánh giá, và Saku-chan sẽ ghi nhớ tiến độ của bạn.
         Hoạt động hoàn toàn trên trình duyệt — không cần tải app."
  Font: Nunito 400, 16px, color var(--color-text-sub)

[CTA]:
  Text: "Học ngay →"
  Cùng style với Row 1 CTA trắng
```

**Gap giữa Row 1 và Row 2:** `margin-top: 96px`

**Responsive Feature-A:**
```
< 1024px: mỗi row → 1 col (illustration trên, text dưới)
< 768px: padding giảm, font-size responsive
```

---

## 6. SECTION 4 — FEATURE-B "Ngoài Kanji, bạn sẽ được học"

### Layout tổng thể
```
Background: var(--color-cream) — #F7F4E6
Padding:    100px max(24px, calc((100vw-1200px)/2))
```

**Section header — `.feat-b-header`**
```
Text:       "Ngoài Kanji, bạn sẽ được học"
Font:       Nunito 800, clamp(26px, 4vw, 42px)
Color:      var(--color-text)
Text-align: center
Margin-bottom: 60px

Sub-text (optional):
  "SakuJi không chỉ là luyện Kanji — đây là hành trình tiếng Nhật toàn diện."
  Font: Nunito 400, 16px, color var(--color-text-sub), margin-top: 12px
```

### 3 Feature Cards — `.feat-b-grid`
```
Display: grid
Grid-template-columns: repeat(3, 1fr)
Gap: 28px
Max-width: 1100px, margin: 0 auto
```

**Card chung — `.feat-b-card`**
```
Background:     var(--color-card) — white
Border-radius:  var(--radius-xl) — 24px
Shadow:         0 4px 24px rgba(0,0,0,0.07)
Padding:        36px 28px 32px
Display:        flex, flex-direction: column, align-items: center
Text-align:     center
Transition:     transform 200ms ease, shadow 200ms ease
Hover:          translateY(-6px), shadow: 0 12px 36px rgba(0,0,0,0.10)
```

---

**Card 1 — Từ vựng hội thoại (`.feat-b-card--vocab`)**

*Illustration description:*
```
[Illustration — assets/feat-vocab.svg]:
  Height: 180px
  Nội dung SVG:
    Nền: hình bầu dục pastel hồng nhạt (#FFE8EC) làm backdrop
    3–4 bong bóng hội thoại (speech bubble) nhiều màu:
      Bubble 1 (hồng):    「こんにちは」  — cỡ lớn nhất
      Bubble 2 (xanh lá): 「ありがとう」  — cỡ vừa
      Bubble 3 (vàng):    「おいしい！」  — cỡ nhỏ, góc nghiêng
      Bubble 4 (tím nhạt):「すみません」  — nhỏ nhất, phía sau
    Mỗi bubble có đuôi chỉ hướng khác nhau
    Các chữ Nhật dùng Noto Sans JP 700, màu tương phản với bubble
    Vài sparkle ★ nhỏ rải rác (màu var(--color-accent))
    Saku-chan cực nhỏ (24px) ló ra sau bubble lớn nhất, mắt tròn tò mò
```

```
[Title]:      "Từ vựng & Hội thoại"
Font:         Nunito 700, 20px, color var(--color-text)
Margin-top:   20px

[Description]:
  "Học từ vựng theo chủ đề từ N5 đến N1, luyện câu hội thoại
   thực tế với phát âm chuẩn của người bản ngữ."
Font: Nunito 400, 14px, color var(--color-text-sub), line-height 1.65
Margin-top: 10px
```

---

**Card 2 — Giao tiếp quốc tế (`.feat-b-card--globe`)**

*Illustration description:*
```
[Illustration — assets/feat-globe.svg]:
  Height: 180px
  Nội dung SVG:
    Nền: hình tròn pastel xanh lá rất nhạt (#E8F5E9) làm backdrop
    Quả địa cầu trung tâm:
      Đường kinh tuyến/vĩ tuyến nét thanh màu #A5D6A7
      Mảng đất màu xanh lá #66BB6A
      Mảng biển màu #E3F2FD (xanh dương rất nhạt)
    3–4 điểm định vị (location pin 📍):
      Pin màu var(--color-primary) ở Japan
      Pin màu var(--color-accent) ở Vietnam
      Pin màu var(--color-secondary) ở 1–2 vị trí khác
    Mũi tên cong (arc) nối giữa các pin, nét đứt, màu var(--color-text-sub) opacity 0.4
    Animation: địa cầu xoay chậm 20s linear infinite (transform: rotateY)
               hoặc nếu CSS: subtle sway
    Saku-chan (24px) đứng trên "Japan pin", tay vẫy chào
```

```
[Title]:      "Giao tiếp & Kết nối"
[Description]:
  "Luyện nói, nghe hiểu với người học từ khắp nơi trên thế giới.
   Thực hành hội thoại trong môi trường an toàn, thân thiện."
```

---

**Card 3 — Từ điển tích hợp (`.feat-b-card--dict`)**

*Illustration description:*
```
[Illustration — assets/feat-dictionary.svg]:
  Height: 180px
  Nội dung SVG:
    Nền: hình tứ giác bo góc pastel hồng đào (#FFE4EE) làm backdrop
    Cuốn từ điển (book):
      Bìa màu var(--color-primary) — #E8637A
      Gáy sách nổi khối 3D nhẹ (màu #C44E62 darker)
      Trang mở, nội dung trang: các dòng chữ mờ (line placeholders)
        + 1 từ highlight màu vàng: 「桜」với furigana さくら
      Ribbon đánh dấu trang màu var(--color-accent)
    Kính lúp lớn:
      Tay cầm kính: màu #F5C842 (gold), thanh tròn
      Kính: trong suốt với border #E8637A, 2px
      Bên trong kính zoom vào chữ 「桜」phóng to
      Hiệu ứng: vòng tròn highlight màu #FFF0F3 bên trong kính
    Vài chữ kanji nhỏ lơ lửng xung quanh (「愛」「美」「心」) opacity 0.3
    Saku-chan (24px) cầm kính lúp, nhìn vào từ điển, dáng tò mò
```

```
[Title]:      "Từ điển tiếng Nhật"
[Description]:
  "Từ điển tích hợp hơn 100.000 từ, tra cứu tức thì, có audio
   phát âm, ví dụ câu và lưu từ yêu thích chỉ với 1 click."
```

**Responsive Feature-B:**
```
< 1024px: grid 3→2 col
< 640px:  grid 2→1 col; card padding 24px
```

---

## 7. SECTION 5 — FOOTER

### Layout tổng thể
```
Background: #2D2D2D   ← var(--color-text) — màu ink đậm, ấm
Color:      #FFFFFF
Padding:    64px max(24px, calc((100vw-1200px)/2)) 40px
```

```
┌──────────────────────────────────────────────────────────────────┐
│  [🌸 SakuJi logo]     [Sản phẩm]   [Tài nguyên]   [Công ty]    │
│  [Tagline]             - Flashcard   - Blog          - Về chúng tôi│
│  [Social icons]        - Kanji       - Tài liệu      - Tuyển dụng │
│  [Saku-chan nhỏ ngủ]   - Từ điển     - API docs      - Liên hệ   │
│                        - Bảng giá                                │
├──────────────────────────────────────────────────────────────────┤
│  © 2026 SakuJi. All rights reserved.    [🇻🇳 Tiếng Việt ▾]     │
└──────────────────────────────────────────────────────────────────┘
```

### Top area — `.footer-top`
```
Display: grid
Grid-template-columns: 2fr 1fr 1fr 1fr
Gap: 48px
Padding-bottom: 48px
Border-bottom: 1px solid rgba(255,255,255,0.10)
```

**Column 1 — Brand — `.footer-brand`**
```
[Logo block]:
  Icon Saku-chan mini (28px) + Text "SakuJi"
  "Saku" màu var(--color-primary-light) — #F4A7B3 (hồng nhạt trên nền tối)
  "Ji"   màu white

[Tagline]:
  "Hành trình tiếng Nhật của bạn bắt đầu từ một cánh hoa 🌸"
  Font: Nunito 400, 14px
  Color: rgba(255,255,255,0.60)
  Margin-top: 12px, max-width: 220px, line-height: 1.65

[Social icons]:
  Margin-top: 20px
  Icons: Facebook / YouTube / TikTok / Discord
  Size: 36×36px mỗi icon
  Background: rgba(255,255,255,0.08)
  Border-radius: var(--radius-full)
  Color: rgba(255,255,255,0.70)
  Hover: background rgba(255,255,255,0.16), color white
  Gap: 8px

[Saku-chan sleeping]:
  File: assets/saku-chan-sleep.svg (variant đặc biệt: mắt nhắm, zZZ)
  Width: 64px
  Margin-top: 24px
  opacity: 0.85
  Animation: breathe 3s ease-in-out infinite
    @keyframes breathe {
      0%, 100% { transform: scale(1); }
      50%       { transform: scale(1.04); }
    }
```

**Columns 2–4 — Link groups — `.footer-links`**
```
[Column heading]:
  Font: Nunito 700, 13px
  Color: rgba(255,255,255,0.40)
  Text-transform: uppercase
  Letter-spacing: 0.8px
  Margin-bottom: 16px

[Links]:
  Font: Nunito 400, 14px
  Color: rgba(255,255,255,0.70)
  Line-height: 2.2
  Text-decoration: none
  Hover: color white, text-decoration: underline
```

### Bottom area — `.footer-bottom`
```
Display: flex
Justify-content: space-between
Align-items: center
Padding-top: 24px

[Copyright]:
  "© 2026 SakuJi. Mọi quyền được bảo lưu."
  Font: Nunito 400, 13px
  Color: rgba(255,255,255,0.40)

[Language selector]:
  "🇻🇳 Tiếng Việt ▾"
  Font: Nunito 600, 13px
  Color: rgba(255,255,255,0.60)
  Background: rgba(255,255,255,0.06)
  Border: 1px solid rgba(255,255,255,0.12)
  Border-radius: var(--radius-sm)
  Padding: 6px 12px
  Cursor: pointer
  Hover: background rgba(255,255,255,0.12)
```

**Responsive Footer:**
```
< 1024px: grid 4→2 col (brand+product | resource+company)
< 640px:  grid 2→1 col; social icons centered; brand centered
```

---

## 8. ANIMATIONS TOÀN TRANG

```css
/* Khai báo tại Home.css */

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50%       { transform: translateY(-14px); }
}

@keyframes petalDrift {
  0%   { transform: translateY(0) rotate(0deg); }
  100% { transform: translateY(-8px) rotate(12deg); }
}

@keyframes orbit {
  from { transform: rotate(0deg) translateX(120px) rotate(0deg); }
  to   { transform: rotate(360deg) translateX(120px) rotate(-360deg); }
}

@keyframes breathe {
  0%, 100% { transform: scale(1); }
  50%       { transform: scale(1.04); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: 0.6; transform: scale(0.92); }
}

/* Scroll reveal — áp dụng qua IntersectionObserver */
.reveal {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}
.reveal.visible {
  opacity: 1;
  transform: translateY(0);
}
```

**Scroll reveal:** Mỗi section và mỗi card trong Feature-B có class `.reveal`. Dùng `IntersectionObserver` trong `Home.jsx` để thêm class `.visible` khi phần tử vào viewport. Không dùng thư viện animation ngoài.

---

## 9. ROUTE & COMPONENT

```jsx
// App.jsx — thêm route
import Home from './pages/home/Home';
<Route path="/" element={<Home />} />
// Không dùng Navigate to /login nữa — trang chủ là /
// Redirect login về /dashboard sau khi auth thành công
```

```jsx
// pages/home/Home.jsx
import TopBar from './sections/TopBar';
import HeroSection from './sections/HeroSection';
import FeatureASection from './sections/FeatureASection';
import FeatureBSection from './sections/FeatureBSection';
import FooterSection from './sections/FooterSection';
import './Home.css';

function Home() {
  return (
    <div className="home-root">
      <TopBar />
      <main>
        <HeroSection />
        <FeatureASection />
        <FeatureBSection />
      </main>
      <FooterSection />
    </div>
  );
}

export default Home;
```

---

## 10. ACCESSIBILITY

| Yêu cầu | Cách thực hiện |
|:---|:---|
| Heading hierarchy | `h1` cho hero headline; `h2` cho mỗi section header; `h3` cho card titles |
| Alt text | Mọi SVG mascot/illustration có `alt` mô tả bằng tiếng Việt |
| Focus visible | Outline `2px solid var(--color-primary)` trên mọi interactive element |
| Color contrast | Text trên nền kem: `#2D2D2D` đạt WCAG AA (≥ 4.5:1). Text trắng trên `#2D2D2D` footer: đạt AA |
| Motion | `@media (prefers-reduced-motion: reduce)`: tắt toàn bộ animation (`animation: none`) |
| CTA screen reader | Button "Get started" có `aria-label="Đăng ký học miễn phí"` |

---

## 11. OUT OF SCOPE

- ❌ Pricing section (page riêng `/pricing`)
- ❌ Testimonials / review carousel
- ❌ Video embed hero
- ❌ Cookie banner
- ❌ Live chat widget
- ❌ Dark mode (xem DESIGN.md — out of scope toàn hệ thống)
