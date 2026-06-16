import { useState, useCallback } from 'react';
import './Toast.css';

/* Dùng hook này trong page để quản lý danh sách toasts */
export function useToast() {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((type, message) => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, type, message }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 3200);
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return { toasts, addToast, removeToast };
}

const ICONS = { success: '✅', error: '❌', info: '💬' };

export function ToastContainer({ toasts, onRemove }) {
  if (!toasts.length) return null;
  return (
    <div className="toast-stack" aria-live="polite">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast--${t.type}`} role="alert">
          <span className="toast__icon">{ICONS[t.type] ?? '💬'}</span>
          <span className="toast__msg">{t.message}</span>
          <button type="button" className="toast__close" onClick={() => onRemove(t.id)} aria-label="Đóng">×</button>
        </div>
      ))}
    </div>
  );
}
