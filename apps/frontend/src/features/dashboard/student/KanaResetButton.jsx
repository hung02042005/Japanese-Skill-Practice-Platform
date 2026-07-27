import { useState } from 'react';
import { resetProgress } from '@/shared/api/studentService';
import { useToast } from '@/shared/context/ToastContext';
import { getErrorMessage } from '@/shared/utils/apiMessage';

export default function KanaResetButton({ onResetSuccess }) {
  const [isResetting, setIsResetting] = useState(false);
  const { success, error: toastError } = useToast();

  const handleReset = async () => {
    if (!window.confirm("Bạn có chắc chắn muốn reset toàn bộ tiến độ học Kana không?")) {
      return;
    }

    setIsResetting(true);
    try {
      await resetProgress('kana');
      success('Đã reset tiến độ học Kana.');
      if (onResetSuccess) {
        onResetSuccess();
      } else {
        // Fallback tự động tải lại trang nếu không truyền prop
        window.location.reload();
      }
    } catch (error) {
      toastError(getErrorMessage(error, 'Lỗi khi reset tiến độ.'));
    } finally {
      setIsResetting(false);
    }
  };

  return (
    <button
      onClick={handleReset}
      disabled={isResetting}
      className="kna-reset-btn"
      title="Khôi phục lại trạng thái chưa học cho toàn bộ bảng Kana"
    >
      {isResetting ? 'Đang xử lý...' : 'Reset'}
    </button>
  );
}
