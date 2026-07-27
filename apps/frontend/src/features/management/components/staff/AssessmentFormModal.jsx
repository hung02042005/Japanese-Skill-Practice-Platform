import { useState, useEffect } from 'react';

const JLPT_LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

function blankQuiz(editItem) {
  return {
    title: editItem?.title || '',
    jlptLevel: editItem?.jlptLevel || 'N5',
    topic: editItem?.topic || '',
    durationMin: editItem?.durationMin ?? 30,
    passScore: editItem?.passScore ?? 60,
    totalScore: editItem?.totalScore ?? 100,
  };
}

function blankExam(editItem) {
  return {
    title: editItem?.title || '',
    jlptLevel: editItem?.jlptLevel || 'N5',
    durationMin: editItem?.durationMin ?? 90,
    passScore: editItem?.passScore ?? 80,
    totalScore: editItem?.totalScore ?? 180,
    description: editItem?.description || '',
  };
}

function validateCommon(form) {
  const errs = {};
  if (!form.title.trim()) errs.title = 'Vui lòng nhập tiêu đề.';
  if (!form.jlptLevel) errs.jlptLevel = 'Vui lòng chọn cấp độ JLPT.';
  if (!form.durationMin || form.durationMin < 1) errs.durationMin = 'Thời gian phải >= 1 phút.';
  if (form.passScore === '' || form.passScore == null || form.passScore < 0) errs.passScore = 'Điểm đạt phải >= 0.';
  if (!form.totalScore || form.totalScore < 1) errs.totalScore = 'Tổng điểm phải >= 1.';
  if (form.passScore > form.totalScore) errs.passScore = 'Điểm đạt không được lớn hơn tổng điểm.';
  return errs;
}

export default function AssessmentFormModal({ isOpen, mode, editItem, onClose, onSave, onSaveAndSubmit }) {
  const isQuiz = mode === 'quiz';
  const [form, setForm] = useState(isQuiz ? blankQuiz(editItem) : blankExam(editItem));
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isOpen) {
      setForm(isQuiz ? blankQuiz(editItem) : blankExam(editItem));
      setErrors({});
    }
  }, [isOpen, mode, editItem, isQuiz]);

  useEffect(() => {
    if (!isOpen) return;
    const handleKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const set = (field) => (e) => {
    const val = e.target.type === 'number'
      ? (e.target.value === '' ? '' : Number(e.target.value))
      : e.target.value;
    setForm((prev) => ({ ...prev, [field]: val }));
    setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const buildData = () => {
    const errs = validateCommon(form);
    if (Object.keys(errs).length > 0) { setErrors(errs); return null; }

    const payload = {
      title: form.title.trim(),
      jlptLevel: form.jlptLevel,
      durationMin: Number(form.durationMin),
      passScore: Number(form.passScore),
      totalScore: Number(form.totalScore),
    };
    if (isQuiz) {
      if (form.topic.trim()) payload.topic = form.topic.trim();
    } else if (form.description.trim()) {
      payload.description = form.description.trim();
    }
    return payload;
  };

  const handleSaveDraft = () => {
    const data = buildData();
    if (!data) return;
    onSave(data);
  };

  const handleSaveAndSubmit = () => {
    const data = buildData();
    if (!data) return;
    onSaveAndSubmit(data);
  };

  const typeLabel = isQuiz ? 'Quiz' : 'Đề thi';
  const title = editItem ? `Chỉnh sửa ${typeLabel}` : `Tạo ${typeLabel} mới`;

  return (
    <div
      className="sfa-modal-backdrop"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
      role="dialog"
      aria-modal="true"
      aria-label={title}
    >
      <div className="sfa-modal">
        <div className="sfa-modal-header">
          <h2 className="sfa-modal-title">{title}</h2>
          <button type="button" className="sfa-modal-close" onClick={onClose} aria-label="Đóng">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div className="sfa-modal-body">
          <div className="sfa-field">
            <label className="sfa-field-label">Tiêu đề <span className="sfa-req">*</span></label>
            <input type="text" className={'sfa-input' + (errors.title ? ' sfa-input--err' : '')}
              placeholder={isQuiz ? 'Nhập tiêu đề quiz...' : 'Nhập tiêu đề đề thi...'}
              value={form.title} onChange={set('title')} />
            {errors.title && <span className="sfa-field-error">{errors.title}</span>}
          </div>

          <div className="sfa-field">
            <label className="sfa-field-label">Cấp độ JLPT <span className="sfa-req">*</span></label>
            <select className={'sfa-select' + (errors.jlptLevel ? ' sfa-input--err' : '')}
              value={form.jlptLevel} onChange={set('jlptLevel')}>
              {JLPT_LEVELS.map((lvl) => <option key={lvl} value={lvl}>{lvl}</option>)}
            </select>
            {errors.jlptLevel && <span className="sfa-field-error">{errors.jlptLevel}</span>}
          </div>

          {isQuiz && (
            <div className="sfa-field">
              <label className="sfa-field-label">Chủ đề</label>
              <input type="text" className="sfa-input"
                placeholder="Chủ đề quiz (tuỳ chọn)..."
                value={form.topic} onChange={set('topic')} />
            </div>
          )}

          <div className="sfa-field">
            <label className="sfa-field-label">Thời gian (phút) <span className="sfa-req">*</span></label>
            <input type="number" className={'sfa-input' + (errors.durationMin ? ' sfa-input--err' : '')}
              min={1} max={300} value={form.durationMin} onChange={set('durationMin')} />
            {errors.durationMin && <span className="sfa-field-error">{errors.durationMin}</span>}
          </div>

          <div className="sfa-field">
            <label className="sfa-field-label">Điểm đạt <span className="sfa-req">*</span></label>
            <input type="number" className={'sfa-input' + (errors.passScore ? ' sfa-input--err' : '')}
              min={0} value={form.passScore} onChange={set('passScore')} />
            {errors.passScore && <span className="sfa-field-error">{errors.passScore}</span>}
          </div>

          <div className="sfa-field">
            <label className="sfa-field-label">Tổng điểm <span className="sfa-req">*</span></label>
            <input type="number" className={'sfa-input' + (errors.totalScore ? ' sfa-input--err' : '')}
              min={1} value={form.totalScore} onChange={set('totalScore')} />
            {errors.totalScore && <span className="sfa-field-error">{errors.totalScore}</span>}
          </div>

          {!isQuiz && (
            <div className="sfa-field">
              <label className="sfa-field-label">Mô tả</label>
              <textarea className="sfa-textarea"
                placeholder="Mô tả ngắn về đề thi (tuỳ chọn)..."
                value={form.description} onChange={set('description')} />
            </div>
          )}
        </div>

        <div className="sfa-modal-footer">
          <button type="button" className="sfa-btn-ghost" onClick={onClose}>Hủy</button>
          <button type="button" className="sfa-btn-draft" onClick={handleSaveDraft}>Lưu nháp</button>
          <button
            type="button"
            className="sfa-btn-submit-modal"
            onClick={handleSaveAndSubmit}
          >
            Lưu và gửi duyệt
          </button>
        </div>
      </div>
    </div>
  );
}
