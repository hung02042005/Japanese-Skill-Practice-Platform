/**
 * BadgeIcons.jsx — icon trạng thái/streak và vai trò/badge (tách từ AppIcons.jsx).
 *
 * Cùng convention với StudentIcons.jsx:
 *   - <svg viewBox="0 0 24 24"> nhận prop `size`, dùng `currentColor`.
 *   - `aria-hidden="true"` (icon trang trí; nhãn ngữ nghĩa đặt ở phần tử cha).
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
