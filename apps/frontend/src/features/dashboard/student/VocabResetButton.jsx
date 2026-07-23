import { useState } from 'react';
import { resetProgress } from '@/shared/api/studentService';
import { useToast } from '@/shared/context/ToastContext';
import { getErrorMessage } from '@/shared/utils/apiMessage';

export default function VocabResetButton({ onResetSuccess }) {
  const [isResetting, setIsResetting] = useState(false);
  const { success, error: toastError } = useToast();

  const handleReset = async () => {
    if (!window.confirm("Bạn có chắc chắn muốn reset toàn bộ tiến độ học Từ vựng không?")) {
      return;
    }

    setIsResetting(true);
    try {
      await resetProgress('vocabulary');
      success('Đã reset tiến độ học Từ vựng.');
      if (onResetSuccess) {
        onResetSuccess();
      } else {
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
      style={{
        padding: '6px 12px',
        background: '#fef2f2',
        color: '#ef4444',
        border: '1px solid #fca5a5',
        borderRadius: '6px',
        cursor: isResetting ? 'not-allowed' : 'pointer',
        fontWeight: '500',
        fontSize: '0.85rem',
        marginLeft: '12px'
      }}
      title="Khôi phục lại trạng thái chưa học cho toàn bộ Từ vựng"
    >
      {isResetting ? 'Đang xử lý...' : 'Reset'}
    </button>
  );
}
