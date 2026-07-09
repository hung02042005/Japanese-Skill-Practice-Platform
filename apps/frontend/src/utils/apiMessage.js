/**
 * apiMessage.js — trích message hiển thị cho toast từ response/lỗi axios.
 *
 * Backend trả về ApiResponse { status, message, data }. Các helper này đọc
 * đúng field `message` (đã Việt hoá ở backend) và luôn có fallback an toàn.
 */

const DEFAULT_ERROR = 'Có lỗi xảy ra, vui lòng thử lại.';

/**
 * Lấy message lỗi từ error của axios (ưu tiên message backend).
 * @param {unknown} err
 * @param {string} [fallback]
 * @returns {string}
 */
export function getErrorMessage(err, fallback = DEFAULT_ERROR) {
  const backendMsg = err?.response?.data?.message;
  if (typeof backendMsg === 'string' && backendMsg.trim()) return backendMsg;
  if (typeof err?.message === 'string' && err.message.trim() && err.message !== 'Network Error') {
    return err.message;
  }
  if (err?.message === 'Network Error')
    return 'Không kết nối được máy chủ. Kiểm tra mạng và thử lại.';
  return fallback;
}
