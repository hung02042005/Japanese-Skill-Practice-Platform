/**
 * FeedbackIcons.jsx — icon kết quả/phản hồi (gợi ý, khen, chào...) — tách từ AppIcons.jsx.
 *
 * Cùng convention với StudentIcons.jsx:
 *   - <svg viewBox="0 0 24 24"> nhận prop `size`, dùng `currentColor`.
 *   - `aria-hidden="true"` (icon trang trí; nhãn ngữ nghĩa đặt ở phần tử cha).
 */

/** 💡 Lightbulb — gợi ý / giải thích */
export function LightbulbIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M9 18h6M10 21h4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path
        d="M12 3a6 6 0 0 0-4 10.5c.7.7 1 1.3 1 2.5h6c0-1.2.3-1.8 1-2.5A6 6 0 0 0 12 3Z"
        fill="currentColor"
      />
    </svg>
  );
}

/** 👍 Thumbs up — làm tốt */
export function ThumbsUpIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M7 10v10H4V10h3Zm3 0 3.5-7c1.3 0 2.2 1 2 2.3L14.8 9H20a2 2 0 0 1 2 2.3l-1.2 6A2 2 0 0 1 18.8 19H10V10Z"
        fill="currentColor"
      />
    </svg>
  );
}

/** 👋 Wave — lời chào */
export function WaveIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M7 11V6.5a1.5 1.5 0 0 1 3 0V11m0-1V5a1.5 1.5 0 0 1 3 0v6m0-1V6a1.5 1.5 0 0 1 3 0v7a6 6 0 0 1-6 6h-1a5 5 0 0 1-4-2l-3-4a1.6 1.6 0 0 1 2.4-2L7 13"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  );
}

/** 🎵 Music note — audio / nghe */
export function MusicIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M9 18V5l10-2v13" stroke="currentColor" strokeWidth="2" strokeLinecap="round" fill="none" />
      <circle cx="6" cy="18" r="3" fill="currentColor" />
      <circle cx="16" cy="16" r="3" fill="currentColor" />
    </svg>
  );
}

/** 🛠 Wrench — bảo trì */
export function WrenchIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M15 6a4 4 0 0 0-5 5L4 17l3 3 6-6a4 4 0 0 0 5-5l-2.5 2.5L14 8l2.5-2.5A4 4 0 0 0 15 6Z"
        fill="currentColor"
      />
    </svg>
  );
}

/** 🎯 Target — mục tiêu */
export function TargetIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2" fill="none" />
      <circle cx="12" cy="12" r="5" stroke="currentColor" strokeWidth="2" fill="none" opacity="0.7" />
      <circle cx="12" cy="12" r="1.6" fill="currentColor" />
    </svg>
  );
}

/** 📐 Ruler — ngữ pháp / cấu trúc */
export function RulerIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M3 15 15 3l6 6L9 21 3 15Z" fill="currentColor" opacity="0.9" />
      <path
        d="M7 13l1.5 1.5M10 10l1.5 1.5M13 7l1.5 1.5"
        stroke="white"
        strokeWidth="1.3"
        strokeLinecap="round"
        strokeOpacity="0.7"
      />
    </svg>
  );
}

/** 📝 Pen — viết / bài viết */
export function PenIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="4" y="3" width="14" height="18" rx="2.5" fill="currentColor" opacity="0.15" />
      <rect x="4" y="3" width="14" height="18" rx="2.5" stroke="currentColor" strokeWidth="1.6" fill="none" />
      <path d="M7 8h8M7 11h8M7 14h5" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeOpacity="0.6" />
    </svg>
  );
}

/** 🌱 Sprout — cấp độ / khởi đầu */
export function SproutIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 21v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="M12 14C12 10 9 8 5 8c0 4 3 6 7 6Z" fill="currentColor" />
      <path d="M12 12c0-3 3-5 6-5 0 3-3 5-6 5Z" fill="currentColor" opacity="0.7" />
    </svg>
  );
}

/** 🔁 Repeat — ôn lại */
export function RepeatIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 9a4 4 0 0 1 4-4h9l-2-2m2 2-2 2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" fill="none" />
      <path d="M20 15a4 4 0 0 1-4 4H7l2 2m-2-2 2-2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" fill="none" />
    </svg>
  );
}

/** ✦ Sparkle — điểm nhấn trang trí */
export function SparkleIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 2l2 8 8 2-8 2-2 8-2-8-8-2 8-2 2-8Z" fill="currentColor" />
    </svg>
  );
}
