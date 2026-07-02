/**
 * AppIcons.jsx — bộ icon SVG dùng chung (thay cho emoji làm icon).
 *
 * Cùng convention với StudentIcons.jsx:
 *   - <svg viewBox="0 0 24 24"> nhận prop `size`, dùng `currentColor`.
 *   - `aria-hidden="true"` (icon trang trí; nhãn ngữ nghĩa đặt ở phần tử cha).
 *
 * Icon đã có trong StudentIcons.jsx (FlameIcon, StarIcon, CalendarIcon, MicIcon,
 * DictionaryIcon, VocabIcon, ReadingIcon, QuizIcon…) thì tái sử dụng, không vẽ lại.
 */

/* ─── Streak / Stat ──────────────────────────────────── */

/** 💤 Moon — streak = 0 (ngủ đông) */
export function MoonIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5Z" fill="currentColor" />
    </svg>
  );
}

/* ─── Role / Badge ───────────────────────────────────── */

/** 👑 Crown — admin / VIP */
export function CrownIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M3 8l3.5 3L12 5l5.5 6L21 8l-1.6 10H4.6L3 8Z" fill="currentColor" />
      <circle cx="3" cy="7" r="1.4" fill="currentColor" />
      <circle cx="21" cy="7" r="1.4" fill="currentColor" />
      <circle cx="12" cy="4" r="1.4" fill="currentColor" />
      <rect x="4.6" y="19" width="14.8" height="2" rx="1" fill="currentColor" opacity="0.7" />
    </svg>
  );
}

/** 🌿 Leaf — cấp độ / gói học */
export function LeafIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M20 4C10 4 4 10 4 20c10 0 16-6 16-16Z" fill="currentColor" opacity="0.9" />
      <path
        d="M9 15C11 12 14 9 18 7"
        stroke="white"
        strokeWidth="1.4"
        strokeLinecap="round"
        strokeOpacity="0.7"
        fill="none"
      />
    </svg>
  );
}

/** 👤 User — role / học viên */
export function UserIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="8" r="4" fill="currentColor" />
      <path d="M4 20c0-4 3.6-6 8-6s8 2 8 6" fill="currentColor" opacity="0.85" />
    </svg>
  );
}

/** 🌸 Sakura — hoa anh đào (empty state / trang trí) */
export function SakuraIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {[0, 72, 144, 216, 288].map((deg) => (
        <path
          key={deg}
          d="M12 12C12 8 13 4 12 3C11 4 12 8 12 12Z"
          fill="currentColor"
          opacity="0.85"
          transform={`rotate(${deg} 12 12)`}
        />
      ))}
      <circle cx="12" cy="12" r="2" fill="currentColor" />
    </svg>
  );
}

/* ─── Media / Action ─────────────────────────────────── */

/** 🔊 Speaker — phát âm */
export function SpeakerIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 9v6h4l5 4V5L8 9H4Z" fill="currentColor" />
      <path
        d="M16 8.5a5 5 0 0 1 0 7"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        fill="none"
      />
      <path
        d="M18.5 6a8 8 0 0 1 0 12"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        fill="none"
        opacity="0.6"
      />
    </svg>
  );
}

/** ▶ Play */
export function PlayIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M7 5v14l12-7L7 5Z" fill="currentColor" />
    </svg>
  );
}

/** ⏹ Stop */
export function StopIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="6" y="6" width="12" height="12" rx="2.5" fill="currentColor" />
    </svg>
  );
}

/** ↻ Refresh / retry */
export function RefreshIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M20 12a8 8 0 1 1-2.3-5.6"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        fill="none"
      />
      <path
        d="M20 4v4h-4"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  );
}

/* ─── Modal / Object ─────────────────────────────────── */

/** 🔑 Key — mật khẩu / cấp lại */
export function KeyIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="8" cy="8" r="4.5" stroke="currentColor" strokeWidth="2" fill="none" />
      <path
        d="M11 11l8 8m-3 0l3-3m-6 0l2 2"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        fill="none"
      />
    </svg>
  );
}

/** 🗑️ Trash — xoá */
export function TrashIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M5 7h14M9 7V5h6v2m-8 0 1 13h8l1-13"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  );
}

/** 🔒 Lock */
export function LockIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="5" y="10" width="14" height="10" rx="2.5" fill="currentColor" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" strokeWidth="2" fill="none" />
    </svg>
  );
}

/** ✏️ Pencil — chỉnh sửa */
export function PencilIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M4 20l1-4L16 5l3 3L8 19l-4 1Z" fill="currentColor" />
      <path
        d="M14 7l3 3"
        stroke="white"
        strokeWidth="1.3"
        strokeLinecap="round"
        strokeOpacity="0.7"
      />
    </svg>
  );
}

/** 🔗 Link */
export function LinkIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M9 15l6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path
        d="M10 6l1-1a4 4 0 0 1 6 6l-1 1M14 18l-1 1a4 4 0 0 1-6-6l1-1"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        fill="none"
      />
    </svg>
  );
}

/** 📱 Phone — mobile app */
export function PhoneIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect x="6" y="2" width="12" height="20" rx="3" fill="currentColor" opacity="0.9" />
      <rect x="8" y="5" width="8" height="12" rx="1" fill="white" fillOpacity="0.85" />
      <circle cx="12" cy="19.5" r="1" fill="white" />
    </svg>
  );
}

/** 🔍 ScanText — nhận diện chữ viết (AI · OCR) */
export function ScanTextIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M4 8V6a2 2 0 0 1 2-2h2M16 4h2a2 2 0 0 1 2 2v2M20 16v2a2 2 0 0 1-2 2h-2M8 20H6a2 2 0 0 1-2-2v-2"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        fill="none"
      />
      <line
        x1="8"
        y1="10"
        x2="16"
        y2="10"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
      />
      <line
        x1="8"
        y1="13"
        x2="14"
        y2="13"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeOpacity="0.6"
      />
    </svg>
  );
}

/** 🎉 Confetti — hoàn thành / chúc mừng */
export function ConfettiIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M3 21l6-14 9 9-15 5Z" fill="currentColor" opacity="0.85" />
      <circle cx="16" cy="4" r="1.3" fill="currentColor" />
      <circle cx="20" cy="8" r="1.3" fill="currentColor" />
      <circle cx="14" cy="9" r="1" fill="currentColor" opacity="0.7" />
      <path d="M18 2v3M21 5h-3" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
    </svg>
  );
}

/* ─── Status / Toast ─────────────────────────────────── */

/** ✅ Success circle */
export function CheckCircleIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="10" fill="currentColor" />
      <path
        d="M7.5 12.5l3 3 6-6.5"
        stroke="white"
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  );
}

/** ❌ Error circle */
export function XCircleIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="10" fill="currentColor" />
      <path d="M8.5 8.5l7 7m0-7l-7 7" stroke="white" strokeWidth="2.2" strokeLinecap="round" />
    </svg>
  );
}

/** 💬 Info bubble */
export function InfoIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M4 5a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H9l-5 4V5Z"
        fill="currentColor"
      />
      <circle cx="12" cy="8" r="1.2" fill="white" />
      <rect x="11.2" y="10" width="1.6" height="4.5" rx="0.8" fill="white" />
    </svg>
  );
}

/** ✔ / ✓ Check (không viền) */
export function CheckIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M5 12.5l4.5 4.5L19 6.5"
        stroke="currentColor"
        strokeWidth="2.4"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  );
}

/** ✖ Close / X */
export function XIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M6 6l12 12M18 6L6 18"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
      />
    </svg>
  );
}

/** ➕ Plus */
export function PlusIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" />
    </svg>
  );
}

/** ⏱ Clock */
export function ClockIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="13" r="8" stroke="currentColor" strokeWidth="2" fill="none" />
      <path
        d="M12 9v4l3 2"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        fill="none"
      />
      <path d="M9 3h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

/** ⏳ Spinner (hourglass tĩnh — dùng cùng CSS spin nếu muốn) */
export function SpinnerIcon({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 2a10 10 0 0 1 10 10"
        stroke="currentColor"
        strokeWidth="2.4"
        strokeLinecap="round"
        fill="none"
      />
      <circle
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="2.4"
        strokeOpacity="0.2"
        fill="none"
      />
    </svg>
  );
}

/** ♥ / ♡ Heart — bookmark (prop `filled`) */
export function HeartIcon({ size = 24, filled = true }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 20S4 14.5 4 9a4.5 4.5 0 0 1 8-2.8A4.5 4.5 0 0 1 20 9c0 5.5-8 11-8 11Z"
        fill={filled ? 'currentColor' : 'none'}
        stroke="currentColor"
        strokeWidth="2"
        strokeLinejoin="round"
      />
    </svg>
  );
}

/* ─── Result / Feedback ──────────────────────────────── */

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
