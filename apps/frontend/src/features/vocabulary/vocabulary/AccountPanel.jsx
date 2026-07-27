/**
 * AccountPanel — thẻ tài khoản ở right sidebar (SPEC §8.1).
 * Truy cập tự do: dự án không có gói VIP, chỉ hiển thị cấp độ JLPT.
 *
 * Props:
 *   user — { fullName, email, jlptLevel }
 */
export default function AccountPanel({ user }) {
  const initial = user?.fullName?.charAt(0)?.toUpperCase() ?? '?';
  const level   = user?.jlptLevel ?? 'N5';

  return (
    <div className="vh-account">
      <div className="vh-account-avatar" aria-hidden="true">{initial}</div>
      <div className="vh-account-name">{user?.fullName ?? 'Học viên'}</div>
      <div className="vh-account-meta">{level}</div>
    </div>
  );
}
