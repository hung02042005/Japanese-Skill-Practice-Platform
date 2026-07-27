import { useEffect, useState, useCallback } from 'react';

/** Đếm ngược giây, dùng cho cooldown giữa các lần gửi lại email. */
export function useCountdown() {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    if (seconds <= 0) return undefined;
    const id = setInterval(() => setSeconds((s) => Math.max(0, s - 1)), 1000);
    return () => clearInterval(id);
  }, [seconds]);

  const start = useCallback((value) => setSeconds(value), []);

  return [seconds, start];
}
