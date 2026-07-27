/**
 * Toast.jsx — shim tương thích ngược.
 *
 * Cơ chế toast đã chuyển sang provider toàn cục tại `context/ToastContext.jsx`.
 * File này giữ lại 2 export cũ để các trang hiện có không phải sửa import:
 *   - useToast()        → hook thật, lấy từ ToastContext (toast toàn cục).
 *   - <ToastContainer/> → no-op: viewport giờ do <ToastProvider> render một lần.
 *
 * Trang mới nên import trực tiếp: import { useToast } from '@/shared/context/ToastContext';
 */
export { useToast } from '@/shared/context/ToastContext';

export function ToastContainer() {
  return null;
}
