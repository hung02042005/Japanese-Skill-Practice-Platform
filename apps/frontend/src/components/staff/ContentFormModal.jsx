import { useState, useEffect, useRef } from 'react';

const TYPE_LABELS = {
  course: 'Khóa học',
  lesson: 'Bài học',
  vocabulary: 'Từ vựng',
  grammar: 'Ngữ pháp',
  kanji: 'Kanji',
};

const JLPT_LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

const LESSON_TYPE_OPTIONS = [
  { value: 'lesson', label: 'Bài học' },
  { value: 'reading', label: 'Đọc hiểu' },
  { value: 'listening', label: 'Luyện nghe' },
  { value: 'speaking', label: 'Luyện nói' },
];

const PART_OF_SPEECH_OPTIONS = [
  { value: '名詞', label: '名詞 — Danh từ' },
  { value: '動詞', label: '動詞 — Động từ' },
  { value: '形容詞', label: '形容詞 — Tính từ' },
  { value: '副詞', label: '副詞 — Trạng từ' },
];

function buildInitialForm(contentType, editItem) {
  const base = {
    jlptLevel: editItem?.jlptLevel || 'N5',
  };

  switch (contentType) {
    case 'course':
      return {
        ...base,
        title: editItem?.title || '',
        description: editItem?.description || '',
      };
    case 'lesson':
      return {
        ...base,
        title: editItem?.title || '',
        lessonType: editItem?.lessonType || 'lesson',
        contentText: editItem?.contentText || '',
      };
    case 'vocabulary':
      return {
        ...base,
        word: editItem?.word || '',
        reading: editItem?.reading || '',
        meaning: editItem?.meaning || '',
        partOfSpeech: editItem?.partOfSpeech || '名詞',
        exampleSentence: editItem?.exampleSentence || '',
      };
    case 'grammar':
      return {
        ...base,
        pattern: editItem?.pattern || '',
        meaning: editItem?.meaning || '',
        formation: editItem?.formation || '',
        exampleSentence: editItem?.exampleSentence || '',
      };
    case 'kanji':
      return {
        ...base,
        character: editItem?.character || '',
        onyomi: editItem?.onyomi || '',
        kunyomi: editItem?.kunyomi || '',
        meaning: editItem?.meaning || '',
        strokeCount: editItem?.strokeCount || '',
      };
    default:
      return base;
  }
}

export default function ContentFormModal({ isOpen, contentType, editItem, onClose, onSave }) {
  const [form, setForm] = useState(() => buildInitialForm(contentType, editItem));
  const backdropRef = useRef(null);

  useEffect(() => {
    setForm(buildInitialForm(contentType, editItem));
  }, [contentType, editItem, isOpen]);

  useEffect(() => {
    if (!isOpen) return;
    const handleKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const handleBackdropClick = (e) => {
    if (e.target === backdropRef.current) onClose();
  };

  const set = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const handleSaveDraft = () => {
    onSave({ ...form, contentType, status: 'draft', id: editItem?.id || Date.now(), updatedAt: new Date().toLocaleDateString('vi-VN') });
  };

  const handleSaveSubmit = () => {
    onSave({ ...form, contentType, status: 'pending_review', id: editItem?.id || Date.now(), updatedAt: new Date().toLocaleDateString('vi-VN') });
  };

  const typeLabel = TYPE_LABELS[contentType] || 'Nội dung';
  const modalTitle = editItem ? `Chỉnh sửa ${typeLabel}` : `Tạo ${typeLabel} mới`;

  return (
    <div
      className="sfc-modal-backdrop"
      ref={backdropRef}
      onClick={handleBackdropClick}
      role="dialog"
      aria-modal="true"
      aria-labelledby="sfc-modal-title-id"
    >
      <div className="sfc-modal">
        <div className="sfc-modal-header">
          <h2 className="sfc-modal-title" id="sfc-modal-title-id">{modalTitle}</h2>
          <button className="sfc-modal-close" onClick={onClose} aria-label="Đóng">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>
        </div>

        <div className="sfc-modal-body">
          {/* JLPT Level — common to all types */}
          <div className="sfc-field">
            <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-level">Cấp độ JLPT</label>
            <select
              id="sfc-field-level"
              className="sfc-input sfc-select"
              value={form.jlptLevel}
              onChange={(e) => set('jlptLevel', e.target.value)}
            >
              {JLPT_LEVELS.map((lvl) => (
                <option key={lvl} value={lvl}>{lvl}</option>
              ))}
            </select>
          </div>

          {/* ---- COURSE ---- */}
          {contentType === 'course' && (
            <>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-title">Tên khóa học</label>
                <input
                  id="sfc-field-title"
                  className="sfc-input"
                  type="text"
                  placeholder="Nhập tên khóa học..."
                  value={form.title}
                  onChange={(e) => set('title', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-desc">Mô tả</label>
                <textarea
                  id="sfc-field-desc"
                  className="sfc-textarea"
                  placeholder="Mô tả khóa học..."
                  value={form.description}
                  onChange={(e) => set('description', e.target.value)}
                />
              </div>
            </>
          )}

          {/* ---- LESSON ---- */}
          {contentType === 'lesson' && (
            <>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-title">Tiêu đề bài học</label>
                <input
                  id="sfc-field-title"
                  className="sfc-input"
                  type="text"
                  placeholder="Nhập tiêu đề bài học..."
                  value={form.title}
                  onChange={(e) => set('title', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-lessontype">Loại bài học</label>
                <select
                  id="sfc-field-lessontype"
                  className="sfc-input sfc-select"
                  value={form.lessonType}
                  onChange={(e) => set('lessonType', e.target.value)}
                >
                  {LESSON_TYPE_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-contenttext">Nội dung bài học</label>
                <textarea
                  id="sfc-field-contenttext"
                  className="sfc-textarea"
                  placeholder="Nội dung bài học..."
                  value={form.contentText}
                  onChange={(e) => set('contentText', e.target.value)}
                />
              </div>
            </>
          )}

          {/* ---- VOCABULARY ---- */}
          {contentType === 'vocabulary' && (
            <>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-word">Từ vựng</label>
                <input
                  id="sfc-field-word"
                  className="sfc-input"
                  type="text"
                  placeholder="Nhập từ vựng (ví dụ: 学校)..."
                  value={form.word}
                  onChange={(e) => set('word', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-reading">Cách đọc (hiragana/katakana)</label>
                <input
                  id="sfc-field-reading"
                  className="sfc-input"
                  type="text"
                  placeholder="ví dụ: がっこう"
                  value={form.reading}
                  onChange={(e) => set('reading', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-meaning">Nghĩa</label>
                <input
                  id="sfc-field-meaning"
                  className="sfc-input"
                  type="text"
                  placeholder="Nghĩa tiếng Việt..."
                  value={form.meaning}
                  onChange={(e) => set('meaning', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-pos">Từ loại</label>
                <select
                  id="sfc-field-pos"
                  className="sfc-input sfc-select"
                  value={form.partOfSpeech}
                  onChange={(e) => set('partOfSpeech', e.target.value)}
                >
                  {PART_OF_SPEECH_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-example">Ví dụ</label>
                <input
                  id="sfc-field-example"
                  className="sfc-input"
                  type="text"
                  placeholder="Câu ví dụ..."
                  value={form.exampleSentence}
                  onChange={(e) => set('exampleSentence', e.target.value)}
                />
              </div>
            </>
          )}

          {/* ---- GRAMMAR ---- */}
          {contentType === 'grammar' && (
            <>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-pattern">Cấu trúc ngữ pháp</label>
                <input
                  id="sfc-field-pattern"
                  className="sfc-input"
                  type="text"
                  placeholder="Ví dụ: ～てから"
                  value={form.pattern}
                  onChange={(e) => set('pattern', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-meaning">Ý nghĩa</label>
                <input
                  id="sfc-field-meaning"
                  className="sfc-input"
                  type="text"
                  placeholder="Ý nghĩa tiếng Việt..."
                  value={form.meaning}
                  onChange={(e) => set('meaning', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-formation">Cách chia / Cấu trúc</label>
                <input
                  id="sfc-field-formation"
                  className="sfc-input"
                  type="text"
                  placeholder="Ví dụ: V-て形 + から"
                  value={form.formation}
                  onChange={(e) => set('formation', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-example">Ví dụ</label>
                <textarea
                  id="sfc-field-example"
                  className="sfc-textarea"
                  placeholder="Câu ví dụ..."
                  value={form.exampleSentence}
                  onChange={(e) => set('exampleSentence', e.target.value)}
                />
              </div>
            </>
          )}

          {/* ---- KANJI ---- */}
          {contentType === 'kanji' && (
            <>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-char">Kanji (1 ký tự)</label>
                <input
                  id="sfc-field-char"
                  className="sfc-input"
                  type="text"
                  maxLength={1}
                  placeholder="Ví dụ: 水"
                  value={form.character}
                  onChange={(e) => set('character', e.target.value.slice(0, 1))}
                  style={{ fontSize: 24, textAlign: 'center', fontWeight: 700 }}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-onyomi">Âm on (Onyomi)</label>
                <input
                  id="sfc-field-onyomi"
                  className="sfc-input"
                  type="text"
                  placeholder="Ví dụ: スイ"
                  value={form.onyomi}
                  onChange={(e) => set('onyomi', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-kunyomi">Âm kun (Kunyomi)</label>
                <input
                  id="sfc-field-kunyomi"
                  className="sfc-input"
                  type="text"
                  placeholder="Ví dụ: みず"
                  value={form.kunyomi}
                  onChange={(e) => set('kunyomi', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-meaning">Nghĩa</label>
                <input
                  id="sfc-field-meaning"
                  className="sfc-input"
                  type="text"
                  placeholder="Nghĩa tiếng Việt..."
                  value={form.meaning}
                  onChange={(e) => set('meaning', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-strokes">Số nét</label>
                <input
                  id="sfc-field-strokes"
                  className="sfc-input"
                  type="number"
                  min={1}
                  max={50}
                  placeholder="Ví dụ: 4"
                  value={form.strokeCount}
                  onChange={(e) => set('strokeCount', e.target.value === '' ? '' : Number(e.target.value))}
                />
              </div>
            </>
          )}
        </div>

        <div className="sfc-modal-footer">
          <button className="sfc-btn-ghost" onClick={onClose} type="button">Hủy</button>
          <button className="sfc-btn-draft" onClick={handleSaveDraft} type="button">Lưu nháp</button>
          <button className="sfc-btn-submit-modal" onClick={handleSaveSubmit} type="button">Lưu và gửi duyệt</button>
        </div>
      </div>
    </div>
  );
}
