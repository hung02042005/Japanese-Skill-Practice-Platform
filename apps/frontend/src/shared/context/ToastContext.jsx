import { createContext, useContext, useState, useCallback, useMemo } from 'react';
import { CheckCircleIcon, XCircleIcon, InfoIcon } from '@/shared/components/common/AppIcons';
import '../components/common/Toast.css';

/**
 * ToastContext — provider toast toàn cục.
 *
 * Dùng:
 *   const { addToast, success, error, info } = useToast();
 *   success('Đã lưu thành công');
 *   error(getErrorMessage(err));
 *
 * <ToastProvider> được mount một lần ở App.jsx và tự render viewport.
 */

const ToastCtx = createContext(null);

const AUTO_DISMISS_MS = 3200;
const MAX_TOASTS = 4;
const ICONS = { success: CheckCircleIcon, error: XCircleIcon, info: InfoIcon };

function ToastViewport({ toasts, onRemove }) {
  if (!toasts.length) return null;
  return (
    <div className="toast-stack" aria-live="polite">
      {toasts.map((t) => {
        const Icon = ICONS[t.type] ?? ICONS.info;
        return (
          <div key={t.id} className={`toast toast--${t.type}`} role="alert">
            <span className="toast__icon">
              <Icon size={20} />
            </span>
            <span className="toast__msg">{t.message}</span>
            <button
              type="button"
              className="toast__close"
              onClick={() => onRemove(t.id)}
              aria-label="Đóng"
            >
              ×
            </button>
          </div>
        );
      })}
    </div>
  );
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback((typeOrObj, message) => {
    let type = typeOrObj;
    let msg = message;
    if (typeOrObj && typeof typeOrObj === 'object') {
      type = typeOrObj.type;
      msg = typeOrObj.message;
    }
    if (!msg) return;
    const id = Date.now() + Math.random();
    setToasts((prev) => {
      const next = [...prev, { id, type, message: msg }];
      return next.length > MAX_TOASTS ? next.slice(next.length - MAX_TOASTS) : next;
    });
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, AUTO_DISMISS_MS);
  }, []);

  const value = useMemo(
    () => ({
      toasts,
      addToast,
      removeToast,
      success: (msg) => addToast('success', msg),
      error: (msg) => addToast('error', msg),
      info: (msg) => addToast('info', msg),
    }),
    [toasts, addToast, removeToast],
  );

  return (
    <ToastCtx.Provider value={value}>
      {children}
      <ToastViewport toasts={toasts} onRemove={removeToast} />
    </ToastCtx.Provider>
  );
}

const NOOP_TOAST = {
  toasts: [],
  addToast: () => {},
  removeToast: () => {},
  success: () => {},
  error: () => {},
  info: () => {},
};

export function useToast() {
  return useContext(ToastCtx) ?? NOOP_TOAST;
}
