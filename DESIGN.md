## Overview

**SakuJi** (さくじ — 桜 sakura + 字 ký tự) is a Japanese language learning platform targeting JLPT exam takers (N5 → N1). The visual language is **"Hanami E-learning"**: soft, blooming, and petal-driven — inspired by the feeling of sitting under a cherry blossom tree in spring and picking up a textbook. The mascot is **Saku-chan**: a chubby, round sakura petal (花びら) that has come to life — soft pink body, tiny dot eyes, rosy blushed cheeks, miniature hands, and a crown of three pink petals sitting on top of its head. Saku-chan reacts to every key moment: spinning excitedly on a streak milestone, drooping sadly on a wrong answer, drifting peacefully during idle states like a petal caught in the wind.

The colour story opens with **sakura pink** as the primary brand hue — warm, approachable, distinctly Japanese — supported by an **emerald green** for positive action CTAs, a **soft gold** for achievement moments, and an **ivory white** canvas that evokes washi paper. There is no corporate blue, no heavy dark chrome. Every surface breathes spring air.

Type is handled by two families: **Nunito** (rounded humanist sans) for all UI text, labels, and buttons — its circular terminals echo the petal geometry of the mascot; **Noto Sans JP** as the mandatory CJK fallback for kanji, kana, and furigana rendering. Nunito Black (800) is reserved for large numbers and hero CTA labels only.

Buttons are pill-shaped — `border-radius: 9999px` on all primary and secondary CTAs, evoking the oval outline of a falling petal. Cards use generous corner radii (12–24 px). No element in the UI has a 0 px corner.

**Key Characteristics:**

- Sakura pink (`#E89AAA`) as the primary brand hue — active nav states, highlighted text, streak indicators.
- Emerald green (`#5DBB69`) for positive action CTAs ("HỌC TỪ MỚI"), correct-answer feedback, and study-days stat cards.
- Soft gold (`#F7C948`) for word-count achievements, star decorations, and streak flame.
- Ivory canvas (`#FAF7F4`) instead of flat gray — the washi-paper warmth that ties the sakura theme together.
- Saku-chan mascot on every key screen: idle (drifting), happy (spinning), correct (jumping + petals), wrong (wilting briefly), empty state (sleeping on a branch).
- Three-column dashboard (Streak sidebar | Main content | Stat sidebar), collapsing to one column on mobile.
- Per-page CSS files alongside each React component — no CSS Modules or utility-class-heavy markup.

---

## Colors

### Brand & Accent

- **Sakura Pink** (`--color-primary` — `#E89AAA`): The brand's signature. Soft pastel sakura pink used for active nav tab underlines, streak card gradient, highlighted inline text. Never used as a large fill background.
- **Petal Light** (`--color-primary-light` — `#F7CBD4`): Hover states, streak card gradient end-stop, soft badge tints, petal effects.
- **Deep Petal** (`--color-primary-dark` — `#D84F68`): Pressed/active states on primary-coloured elements.
- **Blossom Tint** (`--color-primary-bg` — `#FFF0F3`): Nav tab hover background, primary chip fill, streak-related highlight rows.

### Surface

- **Washi** (`--color-bg` — `#FAF7F4`): The default page background — warm ivory that evokes Japanese washi paper and lifts white cards without harsh contrast.
- **Card** (`--color-card` — `#FFFFFF`): Every content card, modal, and panel surface.
- **Secondary Tint** (`--color-secondary-bg` — `#F4FBF5`): Stat card background for the "days studied" variant.
- **Gold Tint** (`--color-accent-bg` — `#FFF7DD`): Stat card background for the "words learned" variant.

### Text

- **Ink** (`--color-text` — `#2D2D2D`): All body text, headings, and label copy.
- **Mist** (`--color-text-sub` — `#6B625E`): Supporting labels, nav tab inactive, captions, placeholders.
- **Disabled** (`--color-text-disabled` — `#B7ABA5`): Disabled form fields and buttons.
- **Divider** (`--color-border` — `#E8E0DC`): Input borders, card separators, nav bottom edge — slightly warm to match the washi canvas.

### Semantic

- **Success / Go** (`--color-secondary` — `#5DBB69`): Primary CTA button fill, correct-answer feedback, study-days stat card border.
- **Achievement / Gold** (`--color-accent` — `#F7C948`): Word-count stat card border, star icon fill, streak flame glow, achievement badge.
- **Error** (`--color-error` — `#E57373`): Form field error border, wrong-answer flash, inline error message.
- **Warning** (`--color-warning` — `#F4A261`): Near-deadline notices, VIP expiry alerts.

### JLPT Level Colours

Each JLPT level has a dedicated chip colour pair used consistently across badges, filter tabs, and lesson headers:

| Level | Background | Text | Feel |
| --- | --- | --- | --- |
| N5 | `#E8F5E9` | `#2E7D32` | Sprout green — first steps |
| N4 | `#E3F2FD` | `#1565C0` | Sky blue — gaining confidence |
| N3 | `#FFF3E0` | `#E65100` | Warm amber — middle ground |
| N2 | `#F3E5F5` | `#6A1B9A` | Wisteria purple — advanced |
| N1 | `#FCE4EC` | `#C62828` | Deep sakura red — mastery |

---

## Typography

### Font Family

Two families cover all use cases:

1. **Nunito** — the primary UI font. Rounded terminals echo the soft petal curves of Saku-chan and the overall hanami aesthetic. Loaded in weights 400, 600, 700, 800. Used for every UI string: nav labels, headings, body, buttons, captions, number displays.
2. **Noto Sans JP** — mandatory CJK fallback, loaded alongside Nunito for kanji, kana, and furigana glyph coverage. Weight 400 only at runtime; Latin portions inherit Nunito weights.

```
font-family: 'Nunito', 'Noto Sans JP', system-ui, sans-serif;
```

### Hierarchy

| Token | Size | Weight | Line Height | Use |
| --- | --- | --- | --- | --- |
| `display-xl` | 36px | 800 | 1.25 | Hero score, streak milestone celebration |
| `display-lg` | 30px | 800 | 1.25 | Page-level titles (Dashboard, Kanji) |
| `heading-lg` | 24px | 700 | 1.3 | Section headings, modal titles |
| `heading-md` | 20px | 700 | 1.35 | Card headings, quiz question |
| `heading-sm` | 18px | 600 | 1.4 | Sub-section labels |
| `body-lg` | 16px | 400 | 1.5 | Default body text |
| `body-md` | 14px | 400 | 1.5 | Form labels, description text |
| `body-sm` | 12px | 400 | 1.5 | Captions, timestamps, fine print |
| `label-md` | 14px | 600 | 1 | Nav tab labels, input labels |
| `label-sm` | 12px | 700 | 1 | Badges, chips, JLPT level tags |
| `button-lg` | 15px | 800 | 1 | Primary CTA ("HỌC TỪ MỚI") — uppercase |
| `button-md` | 14px | 700 | 1 | Standard buttons |
| `number-xl` | 48px | 800 | 1 | Streak count, large score display |

### Principles

- **Nunito weight 800 only for numbers and hero CTAs.** Overuse of black weight collapses the soft, approachable tone.
- **Never use font size below 12px.** Japanese characters require legibility headroom; 12px is the absolute floor.
- **Furigana (ruby text) renders at 50% of the parent kanji size.** For a 20px kanji, ruby sits at 10px — the one accepted exception to the 12px floor, required for authentic Japanese typesetting.
- **Uppercase text only on primary CTA labels.** No uppercase on nav, headings, or body copy.
- **Sakura pink text only for highlighted terms** ("Thời Điểm Vàng", level-up messages). Do not colour body paragraphs pink.

---

## Layout

### Spacing System

Base unit: **4px**.

| Token | Value | Use |
| --- | --- | --- |
| `--space-1` | 4px | Icon inner padding, tight inline gaps |
| `--space-2` | 8px | Between badge and label, checkbox gap |
| `--space-3` | 12px | Form field internal padding |
| `--space-4` | 16px | Card internal padding (compact), nav item padding |
| `--space-5` | 20px | Form group margin-bottom |
| `--space-6` | 24px | Card internal padding (default), section gap |
| `--space-8` | 32px | Between major sections within a page |
| `--space-10` | 40px | Auth card padding-x |
| `--space-12` | 48px | Page-level vertical rhythm |
| `--space-16` | 64px | TopNav height |

### Grid & Container

**Dashboard layout** — three named columns in a horizontal flex row:

- Left sidebar: `width: 220px; flex-shrink: 0` — holds StreakCard with Saku-chan
- Main content: `flex: 1; min-width: 0` — primary learning area
- Right sidebar: `width: 200px; flex-shrink: 0` — StatCards stack vertically

**Auth layout** — single centered card:

- Max-width 440px, padding 40px, auto horizontal margin.
- Background white, border-radius 16px, shadow Level 2.

**Exam layout** — no sidebars:

- Max-width 720px centered, progress bar pinned below TopNav.

### Responsive Strategy

#### Breakpoints

| Name | Width | Key Changes |
| --- | --- | --- |
| Mobile | < 768px | Both sidebars hidden; single column; hamburger nav |
| Tablet | 768–1199px | Left sidebar hidden; right sidebar optional |
| Desktop | ≥ 1200px | Full three-column dashboard |

#### Touch Targets

All interactive elements must be at minimum **44 × 44px**. Nav tabs, icon buttons, and checkbox labels meet this threshold via explicit `min-height` or `padding`.

#### Collapsing Strategy

- TopNav tabs: full label+icon row at desktop → icon-only at tablet → hamburger drawer at mobile.
- Dashboard sidebars: hidden at tablet and below; accessible via slide-in drawer if needed.
- Quiz answer grid: 2-column at desktop → 1-column at mobile.

#### Image Behaviour

- Saku-chan mascot: transparent PNG centered in content area, no card background behind it. Size via prop: `sm` 80px / `md` 160px / `lg` 300px.
- Falling petal decoration (optional background detail): small semi-transparent SVG petals, `pointer-events: none`, `position: absolute`, `opacity: 0.12` — used only on dashboard hero area.
- User avatar: circular crop, 36px in TopNav, 80px in profile page.

---

## Elevation & Depth

| Level | Treatment | Use |
| --- | --- | --- |
| Level 0 — Flat | No shadow, no border | Washi canvas background |
| Level 1 — Card | `box-shadow: 0 2px 8px rgba(0,0,0,0.07)` | Content cards, input fields on white bg |
| Level 2 — Raised | `box-shadow: 0 4px 12px rgba(0,0,0,0.10)` | Stat cards, flashcard face, CTA buttons |
| Level 3 — Floating | `box-shadow: 0 8px 24px rgba(0,0,0,0.12)` | Modals, dropdown menus, toast notifications |
| Petal Glow | `box-shadow: 0 2px 10px rgba(232,154,170,0.22)` | StreakCard and primary-pink interactive cards on hover |

Borders are used only for semantic state — error (red), focus (pink ring), and stat card variant (gold/green). Structural dividers use `border-bottom: 1px solid var(--color-border)` on the TopNav only. No hard borders on cards.

---

## Shapes

### Border Radius Scale

| Token | Value | Use |
| --- | --- | --- |
| `--radius-sm` | 8px | Quiz answer option buttons, small tag chips |
| `--radius-md` | 12px | Content cards, form inputs, default panels |
| `--radius-lg` | 16px | Stat cards, sidebar panels |
| `--radius-xl` | 24px | StreakCard, modals, auth container |
| `--radius-full` | 9999px | All CTA buttons (pill), avatar, badge chips |

The brand **never uses 0px corners** on any interactive element. Even the most compact chip has `--radius-sm` 8px. The pill shape of primary buttons directly references the oval silhouette of a falling sakura petal.

### Saku-chan Geometry

- Saku-chan fills a square bounding box; the organic petal silhouette is handled entirely by the illustration — CSS applies no clipping.
- Rendered as a transparent PNG dropped directly onto the canvas, without a card or coloured background behind it.
- The petal-crown on Saku-chan's head uses the dedicated crown colour `#E8637A` — a deeper sakura tone, distinct from the brand primary `#E89AAA`. Crown colour must never be changed.

---

## Components

### TopNav

Height 64px, white background, `border-bottom: 1px solid var(--color-border)`.
Layout: `[Logo + "SakuJi" wordmark 160px] [Nav tabs — flex centre] [User area — right]`

**`topnav-tab`** — each navigation item:

- Layout: icon (24px) stacked above label (`label-md`, 14px / 600).
- Inactive: `color: var(--color-text-sub)`.
- Active: `color: var(--color-primary)` + `border-bottom: 2px solid var(--color-primary)`.
- Hover: `background: var(--color-primary-bg)`, `border-radius: --radius-md` on the tab block.
- Padding: 8px 16px.

**`topnav-user`** — right side:

- Email text truncated at 160px, `body-sm`, `color-text-sub`.
- Avatar 36px circular crop, `border-radius: --radius-full`.

### Cards

**`streak-card`** — left-sidebar streak panel featuring Saku-chan:

- Background: `linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%)`.
- Border-radius: `--radius-xl`. Padding: 16px.
- Title "Ngày Streak": white, `heading-sm`.
- Streak number: white, `number-xl` (48px / 800).
- Flame icon coloured `--color-accent` (gold): full opacity when streak > 0; `opacity: 0.30` when streak = 0; `animation: pulse 2s infinite` when streak > 0.
- Saku-chan `sm` variant (80px): positioned bottom-left, in `happy` state when streak > 0, `idle` when streak = 0.

**`stat-card`** — right-sidebar metric panels:

- `type="words"`: `background: var(--color-accent-bg)`, `border: 2px solid var(--color-accent)`.
- `type="streak"`: `background: var(--color-secondary-bg)`, `border: 2px solid var(--color-secondary)`.
- Border-radius: `--radius-lg`. Padding: 16px. Width: 100% within the 200px column.
- Label: `body-sm`, `color-text-sub`. Value: `heading-lg`, `color-text`.
- Decorative icon: 40px, top-right corner, `opacity: 0.20`.

**`content-card`** — generic white content panel:

- Background: `var(--color-card)`. Border-radius: `--radius-md`. Padding: 24px. Shadow: Level 2.

### Buttons

**`btn-primary`** — the main action CTA:

- Background: `var(--color-secondary)` (green). Color: white.
- Label: `button-lg`, uppercase. Border-radius: `--radius-full` (pill).
- Padding: 14px 48px. Min-width: 180px.
- Shadow: Level 2. Hover: `filter: brightness(1.08)`. Active: `transform: scale(0.97)`.
- Disabled: `opacity: 0.60`, `cursor: not-allowed`.

**`btn-secondary`** — outline variant:

- Background: transparent. Border: `2px solid var(--color-primary)`. Color: `var(--color-primary)`.
- Border-radius: `--radius-full`. Padding: 12px 32px.
- Hover: `background: var(--color-primary-bg)`.

**`btn-ghost`** — low-emphasis:

- Background: transparent. Color: `var(--color-text-sub)`. No border.
- Hover: `color: var(--color-text)`.

**`btn-icon`** — 44 × 44px circular icon button:

- Background: white. Shadow: Level 1. Border-radius: `--radius-full`.
- Hover: `background: var(--color-primary-bg)`.

### Forms

**`form-input`** — standard text input:

- Background: `#FAF7F4` (washi tint). Border: `1.5px solid var(--color-border)`. Border-radius: `--radius-md`.
- Height: 48px. Padding: 0 16px. Font: `body-lg`.
- Focus: `border-color: var(--color-primary)`, `box-shadow: 0 0 0 3px rgba(232,154,170,0.18)`, `background: white`.
- Error state (`.has-error`): `border-color: var(--color-error)`, `background: #FEF2F2`.
- Error focus: `box-shadow: 0 0 0 3px rgba(229,115,115,0.12)`.

**`field-error`** — inline error below input:

- `font-size: 12px`, `color: var(--color-error)`, `margin-top: 4px`.

### Navigation Chips

**`jlpt-badge`** — level indicator:

- Border-radius: `--radius-full`. Padding: 3px 10px. Font: `label-sm` (12px / 700).
- Colour pairs: see JLPT Level Colours table above.
- Used in: course cards, lesson headers, filter tabs, search results.

### Feedback

**`progress-bar`** — linear progress:

- Track: `height: 8px`, `background: var(--color-border)`, `border-radius: --radius-full`.
- Fill: `background: var(--color-primary)` (pink) for general learning progress; `var(--color-secondary)` (green) for quiz/exam completion.
- Transition: `width 0.3s ease`.

**`loading-spinner`** — rotating indicator:

- Sizes: `sm` 20px / `md` 40px / `lg` 60px.
- Color: `var(--color-primary)`. Animation: `spin 0.8s linear infinite`.

**`empty-state`** — zero-data screens:

- Saku-chan `md` variant (160px) in `thinking` or sleeping pose, centred.
- Title: `heading-md`. Description: `body-lg`, `color-text-sub`.
- Optional CTA button below description.
- **Never show a blank page** — always render this component when a list is empty.

**`toast`** — ephemeral notification:

- Position: top-right fixed, `z-index: 9999`. Width: 320px.
- Border-radius: `--radius-md`. Shadow: Level 3. Padding: 14px 16px.
- Variants: success (green left-border), error (red left-border), warning (amber left-border), info (blue left-border).
- Auto-dismiss: 3 seconds.

### Saku-chan Mascot

**`saku-chan`** — the SakuJi character:

- Rendered as a transparent PNG dropped directly on the canvas — no wrapping card or coloured container.
- Sizes via prop: `sm` 80px / `md` 160px / `lg` 300px.
- Animation variants via CSS class:

| Variant | CSS animation | Trigger |
| --- | --- | --- |
| `idle` | `sway 3s ease-in-out infinite` (gentle left-right tilt) | Default / no user action |
| `happy` | `spin-bounce 0.6s ease` then back to idle | Streak milestone, level-up |
| `correct` | `jump 0.4s ease` + confetti petal overlay | Quiz correct answer |
| `wrong` | `wilt 0.3s ease` (droop then recover) | Quiz wrong answer |
| `thinking` | `peek 2s ease infinite` (eyes glance side to side) | Loading, empty state |
| `celebrate` | `spin 0.8s ease` + falling petals SVG | Exam passed, streak record |

Falling petal confetti on `celebrate` variant: 6–8 SVG petal shapes, `position: absolute`, randomised `animation-delay` (0–0.6s), falling from top of mascot bounding box, `opacity: 0` at end. Petals use `var(--color-primary-light)` fill.

---

## Do's and Don'ts

### Do

- Use `var(--color-primary)` sakura pink **only** for accent marks, active indicators, and the streak card gradient. Never fill a large surface with it.
- Use `var(--color-secondary)` green for the primary action button on every learning and practice screen. Green = "go learn" — it reads as a safe, encouraging signal against the pink brand.
- Apply `--radius-full` (pill) to every CTA button. The oval pill directly echoes a falling sakura petal — it is the brand's most recognisable interactive shape.
- Show Saku-chan in the emotionally correct variant on every key screen. The mascot must never be static on a screen that has just given the user feedback.
- Match the JLPT level badge colour pairs exactly — these are load-bearing UX signals for students navigating multi-level content.
- Always provide an `isLoading` skeleton or spinner and an `error` message for every API-backed component.
- Use the washi canvas (`--color-bg` `#FAF7F4`) as the page background — never plain `#FFFFFF` or `#F5F5F5`, which break the warm spring atmosphere.

### Don't

- Don't use 0px corners on any interactive element — the brand is never harsh or geometric.
- Don't place Saku-chan inside a card or on a coloured panel — the mascot floats on the canvas with no background behind it.
- Don't use `var(--color-primary)` sakura pink as a large fill background — it overpowers text and loses its accent function.
- Don't hard-code colour hex values in component CSS — always reference a `--color-*` CSS variable.
- Don't show a blank page or empty white area — always render `<EmptyState />` with Saku-chan or a skeleton loader.
- Don't add a third font family. Nunito + Noto Sans JP is the complete typographic set.
- Don't use font weight above 800 or below 400, and never apply weight 800 to body text.
- Don't use uppercase text anywhere except primary CTA button labels.
- Don't use blue as a brand colour. The only blue that may appear is the JLPT N4 badge — it is a level indicator, not a brand element.
