import { useState } from 'react';
import { resetProgress } from '../../api/studentService';

export default function VocabResetButton({ onResetSuccess }) {
  const [isResetting, setIsResetting] = useState(false);

  const handleReset = async () => {
    if (!window.confirm("Bạn có chắc chắn muốn reset toàn bộ tiến độ học Từ vựng không?")) {
      return;
    }
    
    setIsResetting(true);
    try {
      await resetProgress('vocabulary');
      if (onResetSuccess) {
        onResetSuccess();
      } else {
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
