import { useState, useRef, useEffect } from 'react';

export default function AudioRecorder({ maxSeconds = 60, onRecordingComplete, disabled }) {
  const [isRecording, setRecording] = useState(false);
  const [elapsed,     setElapsed]   = useState(0);
  const [audioUrl,    setAudioUrl]  = useState(null);
  const mediaRecRef = useRef(null);
  const chunksRef   = useRef([]);
  const timerRef    = useRef(null);

  useEffect(() => () => { stopAll(); }, []);

  const stopAll = () => {
    clearInterval(timerRef.current);
    if (mediaRecRef.current?.state !== 'inactive') {
      mediaRecRef.current?.stop();
    }
  };

  const startRecording = async () => {
    if (disabled) return;
    setAudioUrl(null);
    chunksRef.current = [];
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const rec = new MediaRecorder(stream);
      mediaRecRef.current = rec;

      rec.ondataavailable = (e) => {
        if (e.data.size > 0) chunksRef.current.push(e.data);
      };
      rec.onstop = () => {
        stream.getTracks().forEach((t) => t.stop());
        const blob = new Blob(chunksRef.current, { type: 'audio/webm' });
        const url  = URL.createObjectURL(blob);
        setAudioUrl(url);
        onRecordingComplete(blob);
      };

      rec.start();
      setRecording(true);
      setElapsed(0);
      timerRef.current = setInterval(() => {
        setElapsed((s) => {
          if (s + 1 >= maxSeconds) {
            stopRecording();
            return s + 1;
          }
          return s + 1;
        });
      }, 1000);
    } catch {
      // microphone permission denied or not available
    }
  };

  const stopRecording = () => {
    clearInterval(timerRef.current);
    if (mediaRecRef.current?.state === 'recording') mediaRecRef.current.stop();
    setRecording(false);
  };

  const fmt = (s) => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;

  return (
    <div className="spk-recorder">
      {!isRecording && !audioUrl && (
        <button
          className="spk-btn-record"
          onClick={startRecording}
          disabled={disabled}
          aria-label="Bắt đầu ghi âm"
        >
          🎙 Bắt đầu ghi âm
        </button>
      )}

      {isRecording && (
        <div className="spk-recording-active">
          <span className="spk-rec-dot" aria-hidden="true" />
          <span className="spk-rec-label">Đang ghi âm...</span>
          <span className="spk-rec-timer" aria-live="polite">
            {fmt(elapsed)} / {fmt(maxSeconds)}
          </span>
          <button
            className="spk-btn-stop"
            onClick={stopRecording}
            aria-label="Dừng ghi âm"
          >
            ⏹ Dừng
          </button>
        </div>
      )}

      {audioUrl && !isRecording && (
        <div className="spk-playback">
          <audio controls src={audioUrl} aria-label="Nghe lại bản ghi âm của bạn" />
        </div>
      )}
    </div>
  );
}
