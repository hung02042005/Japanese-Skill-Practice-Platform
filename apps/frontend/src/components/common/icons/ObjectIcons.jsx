/**
 * ObjectIcons.jsx — icon dùng trong modal/đối tượng (key, trash, lock...) — tách từ AppIcons.jsx.
 *
 * Cùng convention với StudentIcons.jsx:
 *   - <svg viewBox="0 0 24 24"> nhận prop `size`, dùng `currentColor`.
 *   - `aria-hidden="true"` (icon trang trí; nhãn ngữ nghĩa đặt ở phần tử cha).
 */

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
