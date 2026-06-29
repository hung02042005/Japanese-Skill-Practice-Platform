import { useState, useEffect, useRef } from 'react';
import HanziWriter from 'hanzi-writer';
import { getStaffVocabularyTopics, createStaffVocabularyTopic } from '../../api/staffService';
import { lookupKanjiByReading, getKanjiInfo } from '../../utils/kanjiLookup';

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
    case 'course': {
      const exp = editItem?.explanation || editItem?.description || '';
      return {
        ...base,
        title: editItem?.title || '',
        description: exp,
        explanation: exp,
        lessonType: 'lesson',
      };
    }
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
        furigana: editItem?.furigana || '',
        meaning: editItem?.meaning || '',
        wordType: editItem?.wordType || '名詞',
        topicId: editItem?.topicId ? String(editItem.topicId) : '',
        exampleSentenceJp: editItem?.exampleSentenceJp || '',
      };
    case 'grammar':
      return {
        ...base,
        title: editItem?.title || '',
        structure: editItem?.structure || '',
        meaning: editItem?.meaning || '',
        formula: editItem?.formula || '',
        usageExplanation: editItem?.usageExplanation || '',
        exampleSentenceJp: editItem?.exampleSentenceJp || '',
        exampleSentenceVi: editItem?.exampleSentenceVi || '',
      };
    case 'kanji':
      return {
        ...base,
        characterValue: editItem?.characterValue || '',
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

  // Catalog chủ đề từ vựng (theo cấp độ) + form tạo nhanh chủ đề mới.
  const [topics, setTopics] = useState([]);
  const [topicsLoading, setTopicsLoading] = useState(false);
  const [topicError, setTopicError] = useState('');
  const [showNewTopic, setShowNewTopic] = useState(false);
  const [newTopic, setNewTopic] = useState({ titleVi: '', titleJa: '' });
  const [creatingTopic, setCreatingTopic] = useState(false);

  // Kiểm tra ký tự Kanji có dữ liệu nét (hanzi-writer) — chặn tạo chữ học viên không tô được.
  // status: 'idle' (chưa nhập) | 'checking' | 'valid' | 'invalid'
  const [kanjiCheck, setKanjiCheck] = useState({ status: 'idle', strokeCount: null });

  // B — Tra Kanji theo cách đọc: gõ romaji ("gaku") → gợi ý Kanji để chọn (vì staff chỉ gõ được chữ Latinh).
  const [kanjiQuery, setKanjiQuery] = useState('');
  const [kanjiSuggest, setKanjiSuggest] = useState({ kana: '', candidates: [] });

  useEffect(() => {
    setForm(buildInitialForm(contentType, editItem));
    setKanjiQuery('');
    setKanjiSuggest({ kana: '', candidates: [] });
  }, [contentType, editItem, isOpen]);

  const handleKanjiQuery = (val) => {
    setKanjiQuery(val);
    setKanjiSuggest(lookupKanjiByReading(val));
  };

  // Chọn 1 ký tự Kanji → đặt characterValue và tự điền onyomi/kunyomi (vẫn cho sửa lại).
  // force = true (bấm chip): ghi đè cách đọc; force = false (gõ/dán tay): chỉ điền khi ô đang trống.
  const applyKanji = (ch, force) => {
    setForm((prev) => {
      const next = { ...prev, characterValue: ch };
      const info = getKanjiInfo(ch);
      if (info) {
        if (force || !prev.onyomi) next.onyomi = info.on;
        if (force || !prev.kunyomi) next.kunyomi = info.kun;
      }
      return next;
    });
  };

  // A — Validate ký tự Kanji + C — đồng bộ số nét từ hanzi-writer.
  useEffect(() => {
    if (!isOpen || contentType !== 'kanji') return;
    const ch = (form.characterValue || '').trim();
    if (!ch) { setKanjiCheck({ status: 'idle', strokeCount: null }); return; }

    let cancelled = false;
    setKanjiCheck({ status: 'checking', strokeCount: null });
    const timer = setTimeout(() => {
      HanziWriter.loadCharacterData(ch)
        .then((data) => {
          if (cancelled) return;
          const n = data?.strokes?.length || null;
          setKanjiCheck({ status: 'valid', strokeCount: n });
          // C: lấy số nét đúng từ dữ liệu nét, không để staff gõ tay lệch.
          if (n) setForm((prev) => (prev.strokeCount === n ? prev : { ...prev, strokeCount: n }));
        })
        .catch(() => {
          if (!cancelled) setKanjiCheck({ status: 'invalid', strokeCount: null });
        });
    }, 350);

    return () => { cancelled = true; clearTimeout(timer); };
  }, [isOpen, contentType, form.characterValue]);

  // Tải danh sách chủ đề khi mở form Từ vựng / đổi cấp độ.
  useEffect(() => {
    if (!isOpen || contentType !== 'vocabulary' || !form.jlptLevel) return;
    let cancelled = false;
    setTopicsLoading(true);
    setTopicError('');
    setShowNewTopic(false);
    getStaffVocabularyTopics(form.jlptLevel)
      .then((list) => { if (!cancelled) setTopics(list || []); })
      .catch(() => { if (!cancelled) setTopicError('Không tải được danh sách chủ đề.'); })
      .finally(() => { if (!cancelled) setTopicsLoading(false); });
    return () => { cancelled = true; };
  }, [isOpen, contentType, form.jlptLevel]);

  const handleCreateTopic = async () => {
    const titleVi = newTopic.titleVi.trim();
    const titleJa = newTopic.titleJa.trim();
    if (!titleVi || !titleJa) { setTopicError('Cần nhập cả tên tiếng Việt và tiếng Nhật.'); return; }
    setCreatingTopic(true);
    setTopicError('');
    try {
      const res = await createStaffVocabularyTopic({ jlptLevel: form.jlptLevel, titleVi, titleJa });
      const created = res?.data;
      if (!created?.topicId) throw new Error(res?.message || 'Lỗi tạo chủ đề');
      setTopics((prev) => [...prev, created]);
      setForm((prev) => ({ ...prev, topicId: String(created.topicId) }));
      setNewTopic({ titleVi: '', titleJa: '' });
      setShowNewTopic(false);
    } catch (err) {
      setTopicError(err?.response?.data?.message || err?.message || 'Lỗi tạo chủ đề');
    } finally {
      setCreatingTopic(false);
    }
  };

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

  const getSubmitPayload = (status) => {
    const payload = {
      ...form,
      contentType,
      status,
      id: editItem?.id || Date.now(),
      updatedAt: new Date().toLocaleDateString('vi-VN'),
    };
    if (contentType === 'course') {
      const exp = form.explanation || form.description || '';
      payload.lessonType = 'lesson';
      payload.explanation = exp;
      payload.description = exp;
      payload.contentText = exp.trim() || form.title.trim() || 'Course Content';
    }
    if (contentType === 'vocabulary') {
      payload.topicId = form.topicId ? Number(form.topicId) : null;
    }
    return payload;
  };

  // Từ vựng bắt buộc có chủ đề (FR-redo-topic) — chặn submit nếu chưa chọn.
  const submit = (status) => {
    if (contentType === 'vocabulary' && !form.topicId) {
      setTopicError('Vui lòng chọn (hoặc tạo) một chủ đề cho từ vựng.');
      return;
    }
    // A — Chặn tạo Kanji không có dữ liệu nét (học viên sẽ không tô được).
    if (contentType === 'kanji' && kanjiCheck.status !== 'valid') {
      return;
    }
    onSave(getSubmitPayload(status));
  };

  const handleSaveDraft = () => submit('draft');

  const handleSaveSubmit = () => submit('pending_review');

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
                  value={form.explanation || form.description || ''}
                  onChange={(e) => {
                    const val = e.target.value;
                    setForm((prev) => ({ ...prev, explanation: val, description: val }));
                  }}
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
                  value={form.furigana}
                  onChange={(e) => set('furigana', e.target.value)}
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
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-topic">Chủ đề</label>
                <select
                  id="sfc-field-topic"
                  className="sfc-input sfc-select"
                  value={form.topicId}
                  onChange={(e) => set('topicId', e.target.value)}
                  disabled={topicsLoading}
                >
                  <option value="">{topicsLoading ? 'Đang tải chủ đề…' : '— Chọn chủ đề —'}</option>
                  {topics.map((t) => (
                    <option key={t.topicId} value={String(t.topicId)}>{t.titleVi}</option>
                  ))}
                </select>
                {!showNewTopic ? (
                  <button
                    type="button"
                    className="sfc-btn-ghost"
                    style={{ marginTop: 6, alignSelf: 'flex-start' }}
                    onClick={() => { setShowNewTopic(true); setTopicError(''); }}
                  >
                    ➕ Tạo chủ đề mới
                  </button>
                ) : (
                  <div className="sfc-newtopic" style={{ marginTop: 8, display: 'grid', gap: 6 }}>
                    <input
                      className="sfc-input"
                      type="text"
                      placeholder="Tên chủ đề (tiếng Việt) — ví dụ: Gia đình"
                      value={newTopic.titleVi}
                      onChange={(e) => setNewTopic((p) => ({ ...p, titleVi: e.target.value }))}
                    />
                    <input
                      className="sfc-input"
                      type="text"
                      placeholder="Tên chủ đề (tiếng Nhật) — ví dụ: 家族"
                      value={newTopic.titleJa}
                      onChange={(e) => setNewTopic((p) => ({ ...p, titleJa: e.target.value }))}
                    />
                    <div style={{ display: 'flex', gap: 8 }}>
                      <button type="button" className="sfc-btn-submit-modal" onClick={handleCreateTopic} disabled={creatingTopic}>
                        {creatingTopic ? 'Đang tạo…' : 'Lưu chủ đề'}
                      </button>
                      <button type="button" className="sfc-btn-ghost" onClick={() => { setShowNewTopic(false); setTopicError(''); }}>
                        Hủy
                      </button>
                    </div>
                  </div>
                )}
                {topicError && <span className="sfc-field-error" style={{ color: 'var(--color-danger, #c0392b)' }}>{topicError}</span>}
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-pos">Từ loại</label>
                <select
                  id="sfc-field-pos"
                  className="sfc-input sfc-select"
                  value={form.wordType}
                  onChange={(e) => set('wordType', e.target.value)}
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
                  placeholder="Câu ví dụ tiếng Nhật..."
                  value={form.exampleSentenceJp}
                  onChange={(e) => set('exampleSentenceJp', e.target.value)}
                />
              </div>
            </>
          )}

          {/* ---- GRAMMAR ---- */}
          {contentType === 'grammar' && (
            <>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-grammar-title">Tiêu đề</label>
                <input
                  id="sfc-field-grammar-title"
                  className="sfc-input"
                  type="text"
                  placeholder="Ví dụ: Ngữ pháp ～てから (N5)"
                  value={form.title}
                  onChange={(e) => set('title', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-pattern">Cấu trúc ngữ pháp</label>
                <input
                  id="sfc-field-pattern"
                  className="sfc-input"
                  type="text"
                  placeholder="Ví dụ: ～てから"
                  value={form.structure}
                  onChange={(e) => set('structure', e.target.value)}
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
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-usage">Giải thích cách dùng</label>
                <textarea
                  id="sfc-field-usage"
                  className="sfc-textarea"
                  placeholder="Giải thích cách dùng chi tiết..."
                  value={form.usageExplanation}
                  onChange={(e) => set('usageExplanation', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-formation">Cách chia / Cấu trúc (Công thức)</label>
                <input
                  id="sfc-field-formation"
                  className="sfc-input"
                  type="text"
                  placeholder="Ví dụ: V-て形 + から"
                  value={form.formula}
                  onChange={(e) => set('formula', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-example">Ví dụ tiếng Nhật</label>
                <textarea
                  id="sfc-field-example"
                  className="sfc-textarea"
                  placeholder="Câu ví dụ tiếng Nhật..."
                  value={form.exampleSentenceJp}
                  onChange={(e) => set('exampleSentenceJp', e.target.value)}
                />
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-examplevi">Dịch ví dụ</label>
                <textarea
                  id="sfc-field-examplevi"
                  className="sfc-textarea"
                  placeholder="Dịch nghĩa câu ví dụ..."
                  value={form.exampleSentenceVi}
                  onChange={(e) => set('exampleSentenceVi', e.target.value)}
                />
              </div>
            </>
          )}

          {/* ---- KANJI ---- */}
          {contentType === 'kanji' && (
            <>
              {/* B — Tìm Kanji bằng cách đọc romaji (vd: "gaku" → 学/楽/額/岳) rồi bấm chọn. */}
              <div className="sfc-field">
                <label className="sfc-field-label" htmlFor="sfc-field-kanjisearch">Tìm Kanji theo cách đọc</label>
                <input
                  id="sfc-field-kanjisearch"
                  className="sfc-input"
                  type="text"
                  placeholder='Gõ romaji, ví dụ: "gaku" hoặc "mizu"'
                  value={kanjiQuery}
                  onChange={(e) => handleKanjiQuery(e.target.value)}
                />
                {kanjiQuery.trim() && (
                  kanjiSuggest.candidates.length > 0 ? (
                    <div style={{ marginTop: 8 }}>
                      <span className="sfc-field-hint" style={{ color: '#6B7280', display: 'block', marginBottom: 4 }}>
                        Cách đọc <strong>{kanjiSuggest.kana}</strong> — bấm để chọn:
                      </span>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                        {kanjiSuggest.candidates.map((ch) => (
                          <button
                            key={ch}
                            type="button"
                            onClick={() => applyKanji(ch, true)}
                            title={`Chọn ${ch}`}
                            style={{
                              fontSize: 22, fontWeight: 700, lineHeight: 1,
                              padding: '6px 10px', cursor: 'pointer',
                              border: form.characterValue === ch ? '2px solid #2563EB' : '1px solid #D1D5DB',
                              borderRadius: 8,
                              background: form.characterValue === ch ? '#EFF6FF' : '#FFFFFF',
                            }}
                          >
                            {ch}
                          </button>
                        ))}
                      </div>
                    </div>
                  ) : (
                    <span className="sfc-field-hint" style={{ color: '#6B7280' }}>
                      Không tìm thấy Kanji JLPT cho cách đọc này. Bạn có thể nhập trực tiếp ký tự bên dưới.
                    </span>
                  )
                )}
              </div>
              <div className="sfc-field">
                <label className="sfc-field-label sfc-field-label--req" htmlFor="sfc-field-char">Kanji (1 ký tự)</label>
                <input
                  id="sfc-field-char"
                  className="sfc-input"
                  type="text"
                  placeholder="Ví dụ: 水"
                  value={form.characterValue}
                  onChange={(e) => {
                    const v = e.target.value;
                    // Lấy ký tự Kanji (CJK) đầu tiên nếu có (vd khi dán cả từ), không thì lấy ký tự cuối.
                    // KHÔNG dùng maxLength để bộ gõ IME tiếng Nhật vẫn soạn chữ được.
                    const cjk = v.match(/[㐀-鿿]/);
                    applyKanji(cjk ? cjk[0] : v.slice(-1), false);
                  }}
                  style={{ fontSize: 24, textAlign: 'center', fontWeight: 700 }}
                />
                {kanjiCheck.status === 'checking' && (
                  <span className="sfc-field-hint" style={{ color: '#6B7280' }}>
                    ⏳ Đang kiểm tra dữ liệu nét…
                  </span>
                )}
                {kanjiCheck.status === 'valid' && (
                  <span className="sfc-field-hint" style={{ color: '#16A34A' }}>
                    ✔ Có dữ liệu nét — học viên tô được ({kanjiCheck.strokeCount} nét).
                  </span>
                )}
                {kanjiCheck.status === 'invalid' && (
                  <span className="sfc-field-error" style={{ color: 'var(--color-danger, #c0392b)' }}>
                    ✖ Ký tự này chưa có dữ liệu nét (kana hoặc kanji riêng của Nhật chưa được hỗ trợ).
                    Học viên sẽ không tô được — vui lòng chọn ký tự khác.
                  </span>
                )}
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
                  placeholder="Tự động theo dữ liệu nét"
                  value={form.strokeCount}
                  readOnly
                  title="Số nét được lấy tự động từ dữ liệu nét của ký tự."
                  style={{ backgroundColor: '#F3F4F6', cursor: 'not-allowed' }}
                />
                <span className="sfc-field-hint" style={{ color: '#6B7280' }}>
                  Tự động lấy từ dữ liệu nét — khớp đúng với phần luyện viết của học viên.
                </span>
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
