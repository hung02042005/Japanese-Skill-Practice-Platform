import { useState, useEffect } from 'react';
import SakuChan from '../auth/SakuChan';
import { ProgressBar } from '../common/ProgressBar';
import './GradingPanel.css';

const LEVEL_COLORS = {
  N5: { bg: '#E8F5E9', text: '#2E7D32' },
  N4: { bg: '#E3F2FD', text: '#1565C0' },
  N3: { bg: '#FFF3E0', text: '#E65100' },
  N2: { bg: '#F3E5F5', text: '#6A1B9A' },
  N1: { bg: '#FCE4EC', text: '#C62828' },
};

function formatDatetime(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  });
}

function formatDuration(s) {
  const m = Math.floor(s / 60);
  const sec = s % 60;
  return `${m} phút ${sec} giây`;
}

export default function GradingPanel({ submission, detail, onGrade }) {
  const [manualScore, setScore] = useState('');
  const [feedback, setFeedback] = useState('');
  const [scoreError, setScoreError] = useState('');

  useEffect(() => {
    setScore('');
    setFeedback('');
    setScoreError('');
  }, [detail?.submissionId]);

  if (!submission) {
    return (
      <div className="grd-detail-col">
        <div className="grd-empty-panel">
          <SakuChan variant="idle" size={80} aria-hidden="true" />
          <p>Chọn một bài nộp để bắt đầu chấm</p>
        </div>
      </div>
    );
  }

  if (!detail) {
    return (
      <div className="grd-detail-col">
        <div className="grd-empty-panel">
          <p style={{ color: 'var(--color-text-sub)', fontSize: 14 }}>Đang tải...</p>
        </div>
      </div>
    );
  }

  const lc = LEVEL_COLORS[detail.jlptLevel] ?? {};
  const previewScore = manualScore !== '' ? (parseFloat(manualScore) || 0) : detail.aiOverallScore;

  function handleSave() {
    if (manualScore !== '') {
      const v = parseFloat(manualScore);
      if (isNaN(v) || v < 0 || v > 100) {
        setScoreError('Điểm phải là số từ 0 đến 100.');
        return;
      }
    }
    if (!feedback.trim()) return;
    setScoreError('');
    onGrade(detail.submissionId, manualScore, feedback);
  }

  const isGraded = detail.status === 'graded';
  const saveDisabled = !feedback.trim() || (manualScore !== '' && (isNaN(parseFloat(manualScore)) || parseFloat(manualScore) < 0 || parseFloat(manualScore) > 100));

  return (
    <div className="grd-detail-col">
      <div className="grd-detail-inner">

        {/* Student info */}
        <div className="grd-section-card">
          <div className="grd-student-row">
            <div className="grd-avatar" aria-hidden="true">
              {detail.studentName?.charAt(0)?.toUpperCase() ?? '?'}
            </div>
            <div>
              <div className="grd-student-name">
                {detail.studentName}
                <span className="grd-level-chip" style={{ background: lc.bg, color: lc.text }}>
                  {detail.jlptLevel}
                </span>
              </div>
              <div className="grd-student-meta">
                <span>Nộp lúc: {formatDatetime(detail.submittedAt)}</span>
                <span>Thời lượng: {formatDuration(detail.durationSeconds)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Audio player */}
        <div className="grd-section-card">
          <h3 className="grd-section-title">Bài nộp</h3>
          {detail.recordingUrl ? (
            <audio
              controls
              src={detail.recordingUrl}
              className="grd-audio-player"
              aria-label={`Bài nói của ${detail.studentName}`}
            />
          ) : (
            <div className="grd-no-audio" aria-label="Không có file ghi âm">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M19 10v2a7 7 0 0 1-14 0v-2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              Không có file ghi âm (demo)
            </div>
          )}
          <span className="grd-duration">Thời lượng: {formatDuration(detail.durationSeconds)}</span>
        </div>

        {/* AI Score */}
        <div className="grd-section-card">
          <h3 className="grd-section-title">Kết quả đánh giá AI</h3>
          <div className="grd-ai-scores">
            <div className="grd-ai-score-item">
              <span className="grd-ai-score-num">{detail.aiOverallScore}</span>
              <span className="grd-ai-score-lbl">Tổng</span>
              <ProgressBar value={detail.aiOverallScore} />
            </div>
            <div className="grd-ai-score-item">
              <span className="grd-ai-score-num">{detail.aiPronunciationScore}</span>
              <span className="grd-ai-score-lbl">Phát âm</span>
              <ProgressBar value={detail.aiPronunciationScore} />
            </div>
            <div className="grd-ai-score-item">
              <span className="grd-ai-score-num">{detail.aiFluencyScore}</span>
              <span className="grd-ai-score-lbl">Lưu loát</span>
              <ProgressBar value={detail.aiFluencyScore} />
            </div>
          </div>
          {detail.aiHighlightedErrors && (
            <div className="grd-ai-errors">
              <strong>Lỗi phát hiện:</strong>
              {detail.aiHighlightedErrors}
            </div>
          )}
          {detail.aiSuggestions && (
            <div className="grd-ai-suggestions">
              <strong>Gợi ý cải thiện:</strong>
              {detail.aiSuggestions}
            </div>
          )}
        </div>

        {/* Manual grading */}
        <div className="grd-section-card">
          <h3 className="grd-section-title">Chấm điểm thủ công</h3>

          {isGraded ? (
            <div className="grd-readonly-block">
              <div className="grd-readonly-row">
                <strong>Điểm thủ công</strong>
                {detail.manualScore != null ? `${detail.manualScore} / 100` : 'Dùng điểm AI'}
              </div>
              <div className="grd-readonly-row">
                <strong>Phản hồi</strong>
                {detail.manualFeedback}
              </div>
              <div className="grd-readonly-row">
                <strong>Chấm lúc</strong>
                {formatDatetime(detail.gradedAt)} — {detail.gradedBy}
              </div>
              <div className="grd-readonly-row">
                <strong>Điểm kết quả</strong>
                <span className="grd-final-score">{detail.finalScore} / 100 ✅ Hoàn thành</span>
              </div>
            </div>
          ) : (
            <>
              <div className="grd-field">
                <label className="grd-field-label" htmlFor="grd-score-input">
                  Điểm thủ công (0–100)
                </label>
                <input
                  id="grd-score-input"
                  type="number"
                  min="0"
                  max="100"
                  step="0.5"
                  className={`grd-score-input${scoreError ? ' grd-score-input--err' : ''}`}
                  value={manualScore}
                  onChange={(e) => { setScore(e.target.value); setScoreError(''); }}
                  placeholder="Để rỗng = dùng điểm AI"
                  aria-label="Điểm thủ công"
                  aria-describedby="grd-preview"
                />
                <span className="grd-score-hint">Để rỗng nếu muốn dùng điểm AI ({detail.aiOverallScore})</span>
                {scoreError && <span className="grd-score-error">{scoreError}</span>}
                <span id="grd-preview" className="grd-preview-score" aria-live="polite">
                  Điểm kết quả sẽ là: <strong>{previewScore}</strong> / 100
                </span>
              </div>

              <div className="grd-field">
                <label className="grd-field-label" htmlFor="grd-feedback">
                  Phản hồi chi tiết *
                </label>
                <textarea
                  id="grd-feedback"
                  className="grd-feedback-area"
                  rows={5}
                  placeholder="Nhập nhận xét chi tiết cho học viên..."
                  value={feedback}
                  onChange={(e) => setFeedback(e.target.value)}
                  aria-label="Phản hồi chi tiết"
                  aria-required="true"
                />
              </div>

              <div className="grd-form-footer">
                <button
                  className="grd-cancel-btn"
                  onClick={() => { setScore(''); setFeedback(''); setScoreError(''); }}
                >
                  Hủy
                </button>
                <button
                  className="grd-save-btn"
                  onClick={handleSave}
                  disabled={saveDisabled}
                >
                  Lưu điểm ✓
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
