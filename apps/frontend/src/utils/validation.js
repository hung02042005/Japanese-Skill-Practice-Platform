/**
 * validation.js — helper validate client-side dùng chung cho các form (auth…).
 *
 * Mục tiêu: phản hồi cụ thể ngay tại từng ô nhập, không phải đợi round-trip
 * backend rồi hiện một banner chung chung. Quy tắc mật khẩu KHỚP regex backend
 * (RegisterRequest/ResetPasswordRequest): `^(?=.*[A-Z])(?=.*\d).{8,}$`.
 */

// Đủ dùng cho form: có ký tự trước @, có domain và TLD.
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** true nếu chuỗi rỗng/chỉ khoảng trắng. */
export function isBlank(v) {
  return !v || !String(v).trim();
}

/** true nếu email đúng định dạng cơ bản. */
export function isEmail(v) {
  return EMAIL_RE.test(String(v ?? '').trim());
}

/**
 * Kiểm tra email, trả message lỗi tiếng Việt hoặc '' nếu hợp lệ.
 */
export function emailError(v) {
  if (isBlank(v)) return 'Email là bắt buộc';
  if (!isEmail(v)) return 'Email không hợp lệ';
  return '';
}

/**
 * Kiểm tra độ mạnh mật khẩu (khớp backend). Trả message lỗi hoặc '' nếu đạt.
 */
export function passwordError(v) {
  if (isBlank(v)) return 'Mật khẩu là bắt buộc';
  if (v.length < 8) return 'Mật khẩu phải có ít nhất 8 ký tự';
  if (!/[A-Z]/.test(v)) return 'Mật khẩu cần ít nhất 1 chữ hoa';
  if (!/\d/.test(v)) return 'Mật khẩu cần ít nhất 1 chữ số';
  return '';
}

/**
 * Kiểm tra field bắt buộc chung. Trả message hoặc '' nếu có giá trị.
 */
export function requiredError(v, label = 'Trường này') {
  return isBlank(v) ? `${label} là bắt buộc` : '';
}

/**
 * Kiểm tra cổng (port): bắt buộc, số nguyên trong khoảng 1–65535.
 * Trả message lỗi hoặc '' nếu hợp lệ.
 */
export function portError(v) {
  if (isBlank(v)) return 'Cổng là bắt buộc';
  const s = String(v).trim();
  if (!/^\d+$/.test(s)) return 'Cổng phải là số nguyên';
  const n = Number(s);
  if (n < 1 || n > 65535) return 'Cổng phải trong khoảng 1–65535';
  return '';
}

/**
 * Kiểm tra xác nhận mật khẩu khớp. Trả message hoặc '' nếu khớp.
 */
export function confirmError(password, confirm) {
  if (isBlank(confirm)) return 'Vui lòng xác nhận mật khẩu';
  if (password !== confirm) return 'Mật khẩu xác nhận không khớp';
  return '';
}
