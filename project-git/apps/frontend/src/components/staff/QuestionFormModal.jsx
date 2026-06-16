import { useState, useEffect } from 'react';

/* ── Constants ───────────────────────────────────────────────────── */
const SKILL_OPTIONS = [
  { value: 'vocabulary', label: 'Từ vựng' },
  { value: 'grammar',    label: 'Ngữ pháp' },
  { value: 'kanji',      label: 'Kanji' },
  { value: 'reading',    label: 'Đọc hiểu' },
  { value: 'listening',  label: 'Nghe' },
  { value: 'mixed',      label: 'Tổng hợp' },
];

const LEVEL_OPTIONS = ['N5', 'N4', 'N3', 'N2', 'N1'];

const TYPE_OPTIONS = [
  { value: 'multiple_choice', label: 'Trắc nghiệm' },
  { value: 'fill_blank',      label: 'Điền vào chỗ trống' },
  { value: 'true_false',      label: 'Đúng/Sai' },
];

/* ── Blank form state factory ────────────────────────────────────── */
function blankForm(prefill) {
  return {
    questionText:     prefill?.questionText        ?? '',
    questionType:     prefill?.questionType        ?? 'multiple_choice',
    skill:            prefill?.skill               ?? '',
    jlptLevel:        prefill?.jlptLevel           ?? '',
    optionA:          prefill?.optionA              ?? '',
    optionB:          prefill?.optionB              ?? '',
    optionC:          prefill?.optionC              ?? '',
    optionD:          prefill?.optionD              ?? '',
    correctOption:    prefill?.correctOption        ?? '',
    correctAnswerText: prefill?.correctAnswerText   ?? '',
    explanation:       prefill?.explanation         ?? '',
  };
}

/* ── Validation ──────────────────────────────────────────────────── */
function validate(form) {
  const errors = {};

  if (!form.questionText.trim()) {
    errors.questionText = 'Vui lòng nhập nội dung câu hỏi.';
  }
  if (!form.skill) {
    errors.skill = 'Vui lòng chọn kỹ năng.';
  }
  if (!form.jlptLevel) {
    errors.jlptLevel = 'Vui lòng chọn cấp độ JLPT.';
  }

  if (form.questionType === 'multiple_choice') {
    if (!form.optionA.trim()) errors.optionA = 'Đáp án A là bắt buộc.';
    if (!form.optionB.trim()) errors.optionB = 'Đáp án B là bắt buộc.';
    if (!form.optionC.trim()) errors.optionC = 'Đáp án C là bắt buộc.';
    if (!form.optionD.trim()) errors.optionD = 'Đáp án D là bắt buộc.';
    if (!form.correctOption) errors.correctOption = 'Vui lòng chọn đáp án đúng.';
  }

  if (form.questionType === 'fill_blank' || form.questionType === 'true_false') {
    if (!form.correctAnswerText.trim()) errors.correctAnswerText = 'Vui lòng nhập đáp án đúng.';
  }

  return errors;
}

/* ── QuestionFormModal ───────────────────────────────────────────── */
export default function QuestionFormModal({ isOpen, editQuestion, prefillData, onClose, onSave, onSaveAndSubmit }) {
  const [form, setForm]     = useState(() => blankForm(editQuestion ?? prefillData));
  const [errors, setErrors] = useState({});

  /* Reset form whenever the modal opens or the target question changes */
  useEffect(() => {
    if (isOpen) {
      setForm(blankForm(editQuestion ?? prefillData));
      setErrors({});
    }
  }, [isOpen, editQuestion, prefillData]);

  /* Close on Escape */
  useEffect(() => {
    if (!isOpen) return;
    const handleKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  /* ── Field change helpers ──────────────────────────────────────── */
  const set = (field) => (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));

  /* ── Build validated request body ─────────────────────────────── */
  const buildRequestBody = () => {
    const errs = validate(form);
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return null;
    }
    return {
      questionText:      form.questionText.trim(),
      questionType:      form.questionType,
      skill:             form.skill,
      jlptLevel:         form.jlptLevel,
      explanation:       form.explanation.trim() || undefined,
      optionA:           form.questionType === 'multiple_choice' ? form.optionA.trim() : undefined,
      optionB:           form.questionType === 'multiple_choice' ? form.optionB.trim() : undefined,
      optionC:           form.questionType === 'multiple_choice' ? form.optionC.trim() : undefined,
      optionD:           form.questionType === 'multiple_choice' ? form.optionD.trim() : undefined,
      correctOption:     form.questionType === 'multiple_choice' ? form.correctOption : undefined,
      correctAnswerText: form.questionType !== 'multiple_choice' ? form.correctAnswerText.trim() : undefined,
    };
  };

  /* ── Save draft handler ─────────────────────────────────────────── */
  const handleSave = () => {
    const body = buildRequestBody();
    if (!body) return;
    onSave(body);
  };

  /* ── Save & submit for review handler ──────────────────────────── */
  const handleSaveAndSubmit = () => {
    const body = buildRequestBody();
    if (!body) return;
    onSaveAndSubmit(body);
  };

  const isEdit  = Boolean(editQuestion);
  const title   = isEdit ? 'Chỉnh sửa câu hỏi' : 'Thêm câu hỏi mới';

  return (
    /* Backdrop */
    <div
      className="sfq-modal-backdrop"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
      role="dialog"
      aria-modal="true"
      aria-label={title}
    >
      <div className="sfq-modal" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="sfq-modal-header">
          <h2 className="sfq-modal-title">{title}</h2>
          <button
            type="button"
            className="sfq-modal-close"
            onClick={onClose}
            aria-label="Đóng"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6"  y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="sfq-modal-body">

          {/* ① Question text */}
          <div className="sfq-field">
            <label className="sfq-field-label">
              Câu hỏi <span style={{ color: 'var(--color-error)' }}>*</span>
            </label>
            <textarea
              className={'sfq-textarea' + (errors.questionText ? ' sfq-input--err' : '')}
              style={{ minHeight: 80 }}
              placeholder="Nhập nội dung câu hỏi…"
              value={form.questionText}
              onChange={set('questionText')}
            />
            {errors.questionText && (
              <span className="sfq-field-error">{errors.questionText}</span>
            )}
          </div>

          {/* ② Question type */}
          <div className="sfq-field">
            <label className="sfq-field-label">
              Loại câu hỏi <span style={{ color: 'var(--color-error)' }}>*</span>
            </label>
            <div className="sfq-radio-group">
              {TYPE_OPTIONS.map((opt) => (
                <label key={opt.value} className="sfq-radio-label">
                  <input
                    type="radio"
                    name="questionType"
                    value={opt.value}
                    checked={form.questionType === opt.value}
                    onChange={set('questionType')}
                  />
                  {opt.label}
                </label>
              ))}
            </div>
          </div>

          {/* ③ Skill */}
          <div className="sfq-field">
            <label className="sfq-field-label">
              Kỹ năng <span style={{ color: 'var(--color-error)' }}>*</span>
            </label>
            <select
              className={'sfq-select-field' + (errors.skill ? ' sfq-input--err' : '')}
              value={form.skill}
              onChange={set('skill')}
            >
              <option value="">— Chọn kỹ năng —</option>
              {SKILL_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
            {errors.skill && <span className="sfq-field-error">{errors.skill}</span>}
          </div>

          {/* ④ JLPT Level */}
          <div className="sfq-field">
            <label className="sfq-field-label">
              Cấp độ JLPT <span style={{ color: 'var(--color-error)' }}>*</span>
            </label>
            <select
              className={'sfq-select-field' + (errors.jlptLevel ? ' sfq-input--err' : '')}
              value={form.jlptLevel}
              onChange={set('jlptLevel')}
            >
              <option value="">— Chọn cấp độ —</option>
              {LEVEL_OPTIONS.map((lvl) => (
                <option key={lvl} value={lvl}>{lvl}</option>
              ))}
            </select>
            {errors.jlptLevel && <span className="sfq-field-error">{errors.jlptLevel}</span>}
          </div>

          {/* ── Multiple choice extras ──────────────────────────── */}
          {form.questionType === 'multiple_choice' && (
            <>
              <div className="sfq-options-grid">
                <div className="sfq-field">
                  <label className="sfq-field-label">
                    Đáp án A <span style={{ color: 'var(--color-error)' }}>*</span>
                  </label>
                  <input
                    type="text"
                    className={'sfq-input' + (errors.optionA ? ' sfq-input--err' : '')}
                    placeholder="Nhập đáp án A"
                    value={form.optionA}
                    onChange={set('optionA')}
                  />
                  {errors.optionA && <span className="sfq-field-error">{errors.optionA}</span>}
                </div>
                <div className="sfq-field">
                  <label className="sfq-field-label">
                    Đáp án B <span style={{ color: 'var(--color-error)' }}>*</span>
                  </label>
                  <input
                    type="text"
                    className={'sfq-input' + (errors.optionB ? ' sfq-input--err' : '')}
                    placeholder="Nhập đáp án B"
                    value={form.optionB}
                    onChange={set('optionB')}
                  />
                  {errors.optionB && <span className="sfq-field-error">{errors.optionB}</span>}
                </div>
                <div className="sfq-field">
                  <label className="sfq-field-label">
                    Đáp án C <span style={{ color: 'var(--color-error)' }}>*</span>
                  </label>
                  <input
                    type="text"
                    className={'sfq-input' + (errors.optionC ? ' sfq-input--err' : '')}
                    placeholder="Nhập đáp án C"
                    value={form.optionC}
                    onChange={set('optionC')}
                  />
                  {errors.optionC && <span className="sfq-field-error">{errors.optionC}</span>}
                </div>
                <div className="sfq-field">
                  <label className="sfq-field-label">
                    Đáp án D <span style={{ color: 'var(--color-error)' }}>*</span>
                  </label>
                  <input
                    type="text"
                    className={'sfq-input' + (errors.optionD ? ' sfq-input--err' : '')}
                    placeholder="Nhập đáp án D"
                    value={form.optionD}
                    onChange={set('optionD')}
                  />
                  {errors.optionD && <span className="sfq-field-error">{errors.optionD}</span>}
                </div>
              </div>

              <div className="sfq-field">
                <label className="sfq-field-label">
                  Đáp án đúng <span style={{ color: 'var(--color-error)' }}>*</span>
                </label>
                <div className="sfq-radio-group">
                  {['A', 'B', 'C', 'D'].map((letter) => (
                    <label key={letter} className="sfq-radio-label">
                      <input
                        type="radio"
                        name="correctOption"
                        value={letter}
                        checked={form.correctOption === letter}
                        onChange={set('correctOption')}
                      />
                      {letter}
                    </label>
                  ))}
                </div>
                {errors.correctOption && (
                  <span className="sfq-field-error">{errors.correctOption}</span>
                )}
              </div>
            </>
          )}

          {/* ── Fill blank answer ───────────────────────────────── */}
          {form.questionType === 'fill_blank' && (
            <div className="sfq-field">
              <label className="sfq-field-label">
                Đáp án đúng <span style={{ color: 'var(--color-error)' }}>*</span>
              </label>
              <input
                type="text"
                className={'sfq-input' + (errors.correctAnswerText ? ' sfq-input--err' : '')}
                placeholder="Nhập đáp án đúng"
                value={form.correctAnswerText}
                onChange={set('correctAnswerText')}
              />
              {errors.correctAnswerText && (
                <span className="sfq-field-error">{errors.correctAnswerText}</span>
              )}
            </div>
          )}

          {/* ── True/False answer ───────────────────────────────── */}
          {form.questionType === 'true_false' && (
            <div className="sfq-field">
              <label className="sfq-field-label">
                Đáp án đúng <span style={{ color: 'var(--color-error)' }}>*</span>
              </label>
              <div className="sfq-radio-group">
                <label className="sfq-radio-label">
                  <input
                    type="radio"
                    name="correctAnswerText"
                    value="true"
                    checked={form.correctAnswerText === 'true'}
                    onChange={set('correctAnswerText')}
                  />
                  True (Đúng)
                </label>
                <label className="sfq-radio-label">
                  <input
                    type="radio"
                    name="correctAnswerText"
                    value="false"
                    checked={form.correctAnswerText === 'false'}
                    onChange={set('correctAnswerText')}
                  />
                  False (Sai)
                </label>
              </div>
            </div>
          )}

          {/* ⑤ Explanation */}
          <div className="sfq-field">
            <label className="sfq-field-label">Giải thích</label>
            <textarea
              className="sfq-textarea"
              placeholder="Nhập giải thích (tuỳ chọn)…"
              value={form.explanation}
              onChange={set('explanation')}
            />
          </div>
        </div>

        {/* Footer */}
        <div className="sfq-modal-footer">
          <button
            type="button"
            className="sfq-btn-ghost-modal"
            onClick={onClose}
          >
            Hủy
          </button>
          <button
            type="button"
            className="sfq-btn-draft-modal"
            onClick={handleSave}
          >
            Lưu nháp
          </button>
          {onSaveAndSubmit && (
            <button
              type="button"
              className="sfq-btn-submit-modal"
              onClick={handleSaveAndSubmit}
            >
              Lưu và gửi duyệt
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
