import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useCountdown } from './useCountdown';

describe('useCountdown', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('bắt đầu ở 0, không đếm cho tới khi start() được gọi', () => {
    const { result } = renderHook(() => useCountdown());
    const [seconds] = result.current;
    expect(seconds).toBe(0);
  });

  it('start(60) rồi giảm dần mỗi giây', () => {
    const { result } = renderHook(() => useCountdown());

    act(() => {
      const [, start] = result.current;
      start(60);
    });
    expect(result.current[0]).toBe(60);

    act(() => {
      vi.advanceTimersByTime(1000);
    });
    expect(result.current[0]).toBe(59);

    act(() => {
      vi.advanceTimersByTime(5000);
    });
    expect(result.current[0]).toBe(54);
  });

  it('dừng lại ở 0, không chạy về số âm', () => {
    const { result } = renderHook(() => useCountdown());

    act(() => {
      const [, start] = result.current;
      start(2);
    });

    act(() => {
      vi.advanceTimersByTime(5000);
    });
    expect(result.current[0]).toBe(0);
  });
});
