import { useState, useRef, useEffect, useCallback } from 'react';
import { MicIcon } from '@/shared/components/common/StudentIcons';
import { StopIcon } from '@/shared/components/common/AppIcons';

const SUPPORTS_RECORDING =
  typeof navigator !== 'undefined' &&
  !!navigator.mediaDevices?.getUserMedia &&
  typeof window !== 'undefined' &&
  typeof window.MediaRecorder !== 'undefined';

// Chọn mime type audio mà trình duyệt thực sự hỗ trợ (Safari không có webm/opus).
function pickMimeType() {
  if (typeof MediaRecorder === 'undefined' || !MediaRecorder.isTypeSupported) return '';
  const candidates = ['audio/webm;codecs=opus', 'audio/webm', 'audio/ogg;codecs=opus', 'audio/mp4'];
  return candidates.find((t) => MediaRecorder.isTypeSupported(t)) ?? '';
}

export default function AudioRecorder({ maxSeconds = 60, onRecordingComplete, disabled }) {
  const [isRecording, setRecording] = useState(false);
  const [elapsed,     setElapsed]   = useState(0);
  const [audioUrl,    setAudioUrl]  = useState(null);
  const [error,       setError]     = useState('');
  const mediaRecRef  = useRef(null);
  const chunksRef    = useRef([]);
  const timerRef     = useRef(null);
  const streamRef    = useRef(null);
  const audioUrlRef  = useRef(null);

  const clearTimer = () => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  };

  const stopStream = () => {
    streamRef.current?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;
  };

  const stopRecording = useCallback(() => {
    clearTimer();
    if (mediaRecRef.current && mediaRecRef.current.state === 'recording') {
      mediaRecRef.current.stop(); // onstop lo phần tạo blob + dọn stream
    }
    setRecording(false);
  }, []);

  // Tự dừng khi chạm giới hạn thời gian (thay vì gọi stop trong updater của setElapsed).
  useEffect(() => {
    if (isRecording && elapsed >= maxSeconds) {
      stopRecording();
    }
  }, [isRecording, elapsed, maxSeconds, stopRecording]);

  // Dọn dẹp khi unmount.
  useEffect(
    () => () => {
      clearTimer();
      if (mediaRecRef.current && mediaRecRef.current.state === 'recording') {
        mediaRecRef.current.stop();
      }
      stopStream();
      if (audioUrlRef.current) URL.revokeObjectURL(audioUrlRef.current);
    },
    []
  );

  const startRecording = async () => {
    if (disabled) return;
    setError('');

    if (!SUPPORTS_RECORDING) {
      setError('Trình duyệt không hỗ trợ ghi âm. Hãy dùng Chrome, Edge hoặc Firefox bản mới.');
      return;
    }
    if (typeof window !== 'undefined' && window.isSecureContext === false) {
      setError('Ghi âm cần kết nối bảo mật (HTTPS) hoặc localhost. Trang hiện tại chưa đủ điều kiện.');
      return;
    }

    // Bỏ bản ghi cũ (nếu có).
    if (audioUrlRef.current) {
      URL.revokeObjectURL(audioUrlRef.current);
      audioUrlRef.current = null;
    }
    setAudioUrl(null);
    chunksRef.current = [];

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;

      const mimeType = pickMimeType();
      const rec = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream);
      mediaRecRef.current = rec;

      rec.ondataavailable = (e) => {
        if (e.data.size > 0) chunksRef.current.push(e.data);
      };
      rec.onstop = () => {
        stopStream();
        const type = rec.mimeType || 'audio/webm';
        const blob = new Blob(chunksRef.current, { type });
        if (blob.size === 0) {
          setError('Không thu được âm thanh. Hãy kiểm tra micro rồi thử lại.');
          return;
        }
        const url = URL.createObjectURL(blob);
        audioUrlRef.current = url;
        setAudioUrl(url);
        onRecordingComplete?.(blob);
      };

      rec.start();
      setRecording(true);
      setElapsed(0);
      timerRef.current = setInterval(() => setElapsed((s) => s + 1), 1000);
    } catch (err) {
      stopStream();
      const name = err?.name;
      if (name === 'NotAllowedError' || name === 'SecurityError') {
        setError('Bạn đã chặn quyền micro. Hãy cho phép truy cập micro trong trình duyệt rồi thử lại.');
      } else if (name === 'NotFoundError' || name === 'DevicesNotFoundError') {
        setError('Không tìm thấy micro. Hãy cắm/bật micro rồi thử lại.');
      } else {
        setError('Không thể bắt đầu ghi âm. Vui lòng kiểm tra micro và thử lại.');
      }
    }
  };

  const fmt = (s) => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;

  return (
    <div className="spk-recorder">
      {error && (
        <div className="spk-rec-error" role="alert">
          {error}
        </div>
      )}

      {!isRecording && !audioUrl && (
        <button
          className="spk-btn-record"
          onClick={startRecording}
          disabled={disabled}
          aria-label="Bắt đầu ghi âm"
        >
          <MicIcon size={18} /> Bắt đầu ghi âm
        </button>
      )}

      {isRecording && (
        <div className="spk-recording-active">
          <span className="spk-rec-dot" aria-hidden="true" />
          <span className="spk-rec-label">Đang ghi âm...</span>
          <span className="spk-rec-timer" aria-live="polite">
            {fmt(elapsed)} / {fmt(maxSeconds)}
          </span>
          <button className="spk-btn-stop" onClick={stopRecording} aria-label="Dừng ghi âm">
            <StopIcon size={16} /> Dừng
          </button>
        </div>
      )}

      {audioUrl && !isRecording && (
        <div className="spk-playback">
          <audio controls src={audioUrl} aria-label="Nghe lại bản ghi âm của bạn" />
          <button
            className="spk-btn-rerecord"
            onClick={startRecording}
            disabled={disabled}
            aria-label="Ghi âm lại"
          >
            <MicIcon size={16} /> Ghi lại
          </button>
        </div>
      )}
    </div>
  );
}
