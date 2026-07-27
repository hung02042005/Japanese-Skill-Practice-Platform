import './UserAvatar.css';

/**
 * Props:
 *   name      — họ tên để lấy chữ cái đầu
 *   userType  — 'student' | 'staff' | 'admin'  (ảnh hưởng màu avatar)
 *   isActive  — hiện dot xanh online khi true
 *   size      — px (default 40)
 */
export function UserAvatar({ name, userType = 'student', isActive = false, size = 40 }) {
  const initial = name?.charAt(0)?.toUpperCase() ?? '?';
  const fontSize = Math.round(size * 0.375);

  return (
    <div
      className={`user-avatar user-avatar--${userType}`}
      style={{ width: size, height: size, fontSize }}
      aria-hidden="true"
    >
      {initial}
      {isActive && <span className="user-avatar__dot" />}
    </div>
  );
}
