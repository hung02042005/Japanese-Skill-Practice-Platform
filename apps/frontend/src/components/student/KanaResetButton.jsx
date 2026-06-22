import { useState } from 'react';
import { resetProgress } from '../../api/studentService';

export default function KanaResetButton({ onResetSuccess }) {
  const [isResetting, setIsResetting] = useState(false);

  const handleReset = async () => {
    if (!window.confirm("Bạn có chắc chắn muốn reset toàn bộ tiến độ học Kana không?")) {
      return;
    }
    
    setIsResetting(true);
    try {
      await resetProgress('kana');
      if (onResetSuccess) {
        onResetSuccess();
      } else {
        // Fallback tự động tải lại trang nếu không truyền prop
        window.location.reload(); 
      }
    } catch (error) {
      alert("Lỗi khi reset tiến độ: " + (error?.response?.data?.message || error.message));
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
