import { useEffect, useCallback, useRef } from 'react';

export function useGoogleSignIn(onCredential) {
  const containerRef = useRef(null);
  const initializedRef = useRef(false);

  const stableCallback = useCallback(
    (response) => onCredential(response.credential),
    [onCredential],
  );

  const initGSI = useCallback(() => {
    if (initializedRef.current || !containerRef.current) return;
    if (!window.google?.accounts?.id) return;

    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
    if (!clientId) return;

    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: stableCallback,
    });

    window.google.accounts.id.renderButton(containerRef.current, {
      type: 'standard',
      theme: 'outline',
      size: 'large',
      text: 'continue_with',
      shape: 'rectangular',
      width: containerRef.current.offsetWidth || 360,
    });

    initializedRef.current = true;
  }, [stableCallback]);

  useEffect(() => {
    if (window.google?.accounts?.id) {
      initGSI();
      return;
    }
    const check = setInterval(() => {
      if (window.google?.accounts?.id) {
        clearInterval(check);
        initGSI();
      }
    }, 200);
    return () => clearInterval(check);
  }, [initGSI]);

  return containerRef;
}
