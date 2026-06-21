/**
 * StudentIcons.jsx — SakuJi custom icon set
 *
 * All icons are flat-vector SVG drawn in the same geometric style as SakuChan.
 * Every shape uses `currentColor` so the parent element's CSS `color` property
 * controls the hue — no hard-coded hex values inside.
 *
 * Usage:  <FlameIcon size={20} />
 */

/* ─── Streak ─────────────────────────────────────────── */

/** 🔥 Flame — streak fire, white on the pink gradient */
export function FlameIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Outer flame body */}
      <path
        d="M12 3 C9.5 7 7 10.5 7 14 C7 17.3 9.2 20 12 20 C14.8 20 17 17.3 17 14 C17 10.5 14.5 7 12 3Z"
        fill="currentColor"
      />
      {/* Inner soft teardrop — gives depth */}
      <path
        d="M12 9 C10.5 11.5 10 13 10 14.2 C10 15.8 10.9 17 12 17 C13.1 17 14 15.8 14 14.2 C14 13 13.5 11.5 12 9Z"
        fill="currentColor" opacity="0.30"
      />
      {/* Highlight sparkle */}
      <ellipse cx="10.8" cy="14.5" rx="1.3" ry="2.2" fill="white" fillOpacity="0.32"
        transform="rotate(-12 10.8 14.5)" />
    </svg>
  );
}

/* ─── Dashboard feature shortcuts ────────────────────── */

/** あ Kana — stacked flash cards with hiragana */
export function KanaIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Back card */}
      <rect x="2" y="9" width="14" height="10" rx="2.5" fill="currentColor" opacity="0.15"
        transform="rotate(-10 9 14)" />
      {/* Middle card */}
      <rect x="4" y="7" width="14" height="10" rx="2.5" fill="currentColor" opacity="0.40"
        transform="rotate(-4 11 12)" />
      {/* Front card */}
      <rect x="6" y="5" width="14" height="11" rx="2.5" fill="currentColor" />
      {/* あ — Noto Sans JP is always loaded */}
      <text
        x="13" y="13.5"
        textAnchor="middle" fontSize="8" fontWeight="700"
        fill="white" fontFamily="'Noto Sans JP', sans-serif"
      >あ</text>
      {/* Small ア label below cards */}
      <text
        x="13" y="20.5"
        textAnchor="middle" fontSize="5" fontWeight="600"
        fill="currentColor" opacity="0.45" fontFamily="'Noto Sans JP', sans-serif"
      >ア</text>
    </svg>
  );
}

/** 📖 Vocab — dictionary with a pop-out word card */
export function VocabIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Dictionary body */}
      <rect x="3" y="4" width="12" height="17" rx="2" fill="currentColor" opacity="0.18" />
      {/* Spine */}
      <rect x="3" y="4" width="3.5" height="17" rx="2" fill="currentColor" opacity="0.65" />
      {/* Text lines on page */}
      <line x1="9" y1="10" x2="13.5" y2="10" stroke="currentColor" strokeWidth="1.1"
        strokeLinecap="round" strokeOpacity="0.45" />
      <line x1="9" y1="13" x2="13.5" y2="13" stroke="currentColor" strokeWidth="1.1"
        strokeLinecap="round" strokeOpacity="0.45" />
      <line x1="9" y1="16" x2="12"   y2="16" stroke="currentColor" strokeWidth="1.1"
        strokeLinecap="round" strokeOpacity="0.45" />
      {/* Pop-out word card (top right) */}
      <rect x="11" y="2" width="10" height="8" rx="2.5" fill="currentColor" />
      <line x1="13" y1="5"   x2="19" y2="5"   stroke="white" strokeWidth="1.2"
        strokeLinecap="round" strokeOpacity="0.85" />
      <line x1="13" y1="7.5" x2="19" y2="7.5" stroke="white" strokeWidth="1.2"
        strokeLinecap="round" strokeOpacity="0.85" />
    </svg>
  );
}

/** ✏️ Quiz — answer sheet with check boxes */
export function QuizIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Answer sheet paper */}
      <rect x="5" y="2" width="14" height="20" rx="2.5" fill="currentColor" opacity="0.14" />
      <rect x="5" y="2" width="14" height="20" rx="2.5" stroke="currentColor" strokeWidth="1.5" fill="none" />
      {/* Checkbox 1 — checked */}
      <rect x="7.5" y="7.5" width="4" height="4" rx="1.2" fill="currentColor" opacity="0.78" />
      <path d="M8.5 9.5 L9.7 10.8 L11.5 8.5"
        stroke="white" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round" />
      <line x1="13.5" y1="9.5" x2="17" y2="9.5"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
      {/* Checkbox 2 — checked */}
      <rect x="7.5" y="14" width="4" height="4" rx="1.2" fill="currentColor" opacity="0.78" />
      <path d="M8.5 16 L9.7 17.3 L11.5 15"
        stroke="white" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round" />
      <line x1="13.5" y1="16" x2="17" y2="16"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
    </svg>
  );
}

/** 🎤 Mic — microphone with curved stand */
export function MicIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Mic capsule body */}
      <rect x="8" y="2" width="8" height="12" rx="4" fill="currentColor" />
      {/* Grille lines */}
      <line x1="9.5" y1="7"  x2="14.5" y2="7"
        stroke="white" strokeWidth="1" strokeLinecap="round" strokeOpacity="0.45" />
      <line x1="9.5" y1="10" x2="14.5" y2="10"
        stroke="white" strokeWidth="1" strokeLinecap="round" strokeOpacity="0.45" />
      {/* Stand arc */}
      <path d="M5 12 Q5 20 12 20 Q19 20 19 12"
        stroke="currentColor" strokeWidth="2" strokeLinecap="round" fill="none" />
      {/* Vertical stem */}
      <line x1="12" y1="20" x2="12" y2="23"
        stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      {/* Base */}
      <line x1="9" y1="23" x2="15" y2="23"
        stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

/** 📚 Reading — paper scroll with text lines */
export function ReadingIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Scroll body */}
      <rect x="5" y="5" width="14" height="14" rx="2.5" fill="currentColor" opacity="0.14" />
      <rect x="5" y="5" width="14" height="14" rx="2.5" stroke="currentColor" strokeWidth="1.5" fill="none" />
      {/* Left curl */}
      <path d="M5 5 Q3 5 3 8 Q3 12 5 12"
        stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" />
      {/* Right curl */}
      <path d="M19 5 Q21 5 21 8 Q21 12 19 12"
        stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" />
      {/* Text lines */}
      <line x1="8" y1="9"  x2="16" y2="9"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.55" />
      <line x1="8" y1="12" x2="16" y2="12"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.55" />
      <line x1="8" y1="15" x2="13" y2="15"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.55" />
    </svg>
  );
}

/** 🎧 Headphones — arc band + two earpiece rectangles */
export function HeadphonesIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Headband arc */}
      <path d="M5 14 Q5 5 12 5 Q19 5 19 14"
        stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" fill="none" />
      {/* Left earpiece */}
      <rect x="3" y="13" width="5" height="7" rx="2.5" fill="currentColor" />
      <rect x="4" y="14.5" width="3" height="4" rx="1.5" fill="currentColor" opacity="0.38" />
      {/* Right earpiece */}
      <rect x="16" y="13" width="5" height="7" rx="2.5" fill="currentColor" />
      <rect x="17" y="14.5" width="3" height="4" rx="1.5" fill="currentColor" opacity="0.38" />
    </svg>
  );
}

/** 🏆 Certificate — round medal with star + ribbon tails */
export function CertificateIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Ribbon tails */}
      <path d="M9 14 L7 23 L12 19Z" fill="currentColor" opacity="0.65" />
      <path d="M15 14 L17 23 L12 19Z" fill="currentColor" opacity="0.50" />
      {/* Medal disc */}
      <circle cx="12" cy="9" r="8" fill="currentColor" />
      <circle cx="12" cy="9" r="5.5" fill="currentColor" opacity="0.38" />
      {/* 5-pointed star */}
      <path
        d="M12 5.2 L13.1 8.2 L16.4 8.2 L13.8 10.1 L14.9 13.1 L12 11.2 L9.1 13.1 L10.2 10.1 L7.6 8.2 L10.9 8.2 Z"
        fill="white" fillOpacity="0.92"
      />
    </svg>
  );
}

/** ⭐ VIP Diamond — premium upgrade icon */
export function DiamondIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Diamond outline / body */}
      <path d="M12 2 L19 9 L12 22 L5 9 Z" fill="currentColor" opacity="0.88" />
      {/* Inner facet shading */}
      <path d="M5 9 L12 9 L12 22Z" fill="currentColor" opacity="0.50" />
      <path d="M19 9 L12 9 L12 22Z" fill="currentColor" opacity="0.70" />
      {/* Girdle line */}
      <line x1="5" y1="9" x2="19" y2="9" stroke="white" strokeWidth="0.9" strokeOpacity="0.45" />
      {/* Sparkle — right */}
      <line x1="21" y1="4"  x2="21" y2="8"  stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeOpacity="0.55" />
      <line x1="19" y1="6"  x2="23" y2="6"  stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeOpacity="0.55" />
      <circle cx="21" cy="6" r="1.2" fill="currentColor" opacity="0.5" />
      {/* Sparkle — left */}
      <circle cx="3.5" cy="5.5" r="0.9" fill="currentColor" opacity="0.40" />
    </svg>
  );
}

/* ─── Quick Action Cards ─────────────────────────────── */

/** 🃏 Flashcard — two overlapping study cards */
export function FlashcardIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Back card (rotated) */}
      <rect x="2" y="6" width="15" height="11" rx="2.5" fill="currentColor" opacity="0.24"
        transform="rotate(-7 9.5 11.5)" />
      {/* Front card */}
      <rect x="6" y="8" width="16" height="11" rx="2.5" fill="currentColor" opacity="0.88" />
      {/* Content lines */}
      <line x1="9"  y1="12.5" x2="20" y2="12.5"
        stroke="white" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.78" />
      <line x1="9"  y1="15.5" x2="17" y2="15.5"
        stroke="white" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.78" />
      {/* Top-right corner circle */}
      <circle cx="20.5" cy="10.5" r="1.6" fill="white" fillOpacity="0.55" />
    </svg>
  );
}

/** 📋 Exam — clipboard with answer rows */
export function ExamIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Clipboard body */}
      <rect x="5" y="5" width="14" height="17" rx="2.5" fill="currentColor" opacity="0.14" />
      <rect x="5" y="5" width="14" height="17" rx="2.5" stroke="currentColor" strokeWidth="1.5" fill="none" />
      {/* Clip at top */}
      <rect x="9" y="2.5" width="6" height="5" rx="2.5" fill="currentColor" />
      {/* Answer rows */}
      <line x1="8"  y1="11" x2="16" y2="11"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
      <line x1="8"  y1="14" x2="16" y2="14"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
      <line x1="8"  y1="17" x2="13" y2="17"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
    </svg>
  );
}

/** 🔍 Dictionary — magnifying glass with text lines inside */
export function DictionaryIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Glass fill */}
      <circle cx="10" cy="10" r="7.5" fill="currentColor" opacity="0.13" />
      {/* Glass ring */}
      <circle cx="10" cy="10" r="7.5" stroke="currentColor" strokeWidth="2" fill="none" />
      {/* Text lines inside lens */}
      <line x1="6.5" y1="8.5"  x2="13.5" y2="8.5"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
      <line x1="6.5" y1="11"   x2="13.5" y2="11"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
      <line x1="6.5" y1="13.5" x2="10.5" y2="13.5"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
      {/* Handle */}
      <line x1="15.8" y1="15.8" x2="22" y2="22"
        stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  );
}

/** 📓 Notebook — sổ tay "Từ cần ôn lại" (book + spiral binding + bookmark) */
export function NotebookIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Cover fill */}
      <rect x="5" y="3" width="14" height="18" rx="2.5" fill="currentColor" opacity="0.13" />
      {/* Cover outline */}
      <rect x="5" y="3" width="14" height="18" rx="2.5" stroke="currentColor" strokeWidth="2" fill="none" />
      {/* Spiral binding */}
      <line x1="9" y1="2" x2="9" y2="22" stroke="currentColor" strokeWidth="2" strokeOpacity="0.5" />
      {/* Text lines */}
      <line x1="11.5" y1="8.5"  x2="16" y2="8.5"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
      <line x1="11.5" y1="11.5" x2="16" y2="11.5"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.5" />
      {/* Bookmark */}
      <path d="M14.5 3 V10 l1.75 -1.3 L18 10 V3" fill="currentColor" opacity="0.7" />
    </svg>
  );
}

/** 📊 Progress — three ascending bar chart */
export function ChartIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Bar 1 — short */}
      <rect x="2"   y="16" width="5" height="6"  rx="2.5" fill="currentColor" opacity="0.45" />
      {/* Bar 2 — medium */}
      <rect x="9.5" y="11" width="5" height="11" rx="2.5" fill="currentColor" opacity="0.70" />
      {/* Bar 3 — tall */}
      <rect x="17"  y="5"  width="5" height="17" rx="2.5" fill="currentColor" />
      {/* Trend dots + line */}
      <circle cx="4.5"  cy="14.5" r="1.5" fill="currentColor" opacity="0.65" />
      <circle cx="12"   cy="9.5"  r="1.5" fill="currentColor" opacity="0.82" />
      <circle cx="19.5" cy="3.5"  r="1.5" fill="currentColor" />
      <path d="M4.5 14.5 L12 9.5 L19.5 3.5"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeOpacity="0.48" />
    </svg>
  );
}

/* ─── Stat Cards ──────────────────────────────────────── */

/** ⭐ Words stat — 5-pointed star */
export function StarIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Outer star */}
      <path
        d="M12 2 L14.5 9 L22 9 L16 13.5 L18.5 20.5 L12 16 L5.5 20.5 L8 13.5 L2 9 L9.5 9 Z"
        fill="currentColor"
      />
      {/* Inner star — softer tint for depth */}
      <path
        d="M12 5.5 L14 10.5 L19.5 10.5 L15 13.8 L17 18.8 L12 15.5 L7 18.8 L9 13.8 L4.5 10.5 L10 10.5 Z"
        fill="currentColor" opacity="0.35"
      />
    </svg>
  );
}

/** 📅 Calendar — box with header + highlighted day */
export function CalendarIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {/* Body */}
      <rect x="3" y="5" width="18" height="16" rx="3" fill="currentColor" opacity="0.14" />
      <rect x="3" y="5" width="18" height="16" rx="3" stroke="currentColor" strokeWidth="1.5" fill="none" />
      {/* Header fill */}
      <path d="M3 8 L3 5 Q3 5 6 5 L18 5 Q21 5 21 5 L21 8 Z" fill="currentColor" opacity="0.72" />
      <rect x="3" y="8" width="18" height="1.5" fill="currentColor" opacity="0.72" />
      {/* Clip hooks */}
      <line x1="8"  y1="3" x2="8"  y2="7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <line x1="16" y1="3" x2="16" y2="7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      {/* Day dots — row 1 */}
      <circle cx="8"  cy="14" r="1.5" fill="currentColor" opacity="0.60" />
      <circle cx="12" cy="14" r="1.5" fill="currentColor" opacity="0.60" />
      <circle cx="16" cy="14" r="1.5" fill="currentColor" opacity="0.60" />
      {/* Day dots — row 2 (today highlighted) */}
      <circle cx="8"  cy="18" r="1.5" fill="currentColor" opacity="0.40" />
      <circle cx="12" cy="18" r="2.5" fill="currentColor" />
      <circle cx="16" cy="18" r="1.5" fill="currentColor" opacity="0.40" />
    </svg>
  );
}
