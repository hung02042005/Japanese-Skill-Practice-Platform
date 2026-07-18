import { useState, useEffect, useMemo, useCallback } from 'react';
import { JlptBadge } from '../common/Badges';
import { getStaffQuiz, getStaffExam, getStaffQuestions } from '../../api/staffService';

// Section hợp lệ cho đề thi (khớp VALID_SECTIONS ở StaffExamService).
const EXAM_SECTIONS = [
  { id: 'vocabulary', label: 'Từ vựng' },
  { id: 'grammar',    label: 'Ngữ pháp' },
  { id: 'kanji',      label: 'Kanji' },
  { id: 'reading',    label: 'Đọc hiểu' },
  { id: 'listening',  label: 'Nghe' },
];
const VALID_SECTION_IDS = EXAM_SECTIONS.map((s) => s.id);

const SKILL_LABELS = {
  vocabulary: 'Từ vựng', grammar: 'Ngữ pháp', kanji: 'Kanji',
  reading: 'Đọc hiểu', listening: 'Nghe', mixed: 'Tổng hợp',
};

function truncate(text, n) {
  if (!text) return '';
  return text.length > n ? `${text.slice(0, n)}…` : text;
}

/**
 * Gán câu hỏi (đã publish) vào quiz/exam — UC-26 / UC-28.
 * Replace semantics: danh sách gửi lên là toàn bộ tập câu hỏi mong muốn.
 *
 * Props:
 *  - isOpen, mode ('quiz'|'exam'), assessment ({ assessmentId, title, jlptLevel, totalScore })
 *  - onClose(): đóng modal
 *  - onSubmit(assignments): Promise — trang cha dispatch thunk + toast + refetch
 */
export default function AssignQuestionsModal({ isOpen, mode, assessment, onClose, onSubmit }) {
  const isExam = mode === 'exam';
  const [available, setAvailable] = useState([]);
  // selected: questionId -> { score, sectionName, order }
  const [selected, setSelected] = useState({});
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!assessment) return;
    setLoading(true);
    setError('');
    try {
      const [detail, qPage] = await Promise.all([
        isExam ? getStaffExam(assessment.assessmentId) : getStaffQuiz(assessment.assessmentId),
        getStaffQuestions({ status: 'published', jlptLevel: assessment.jlptLevel, size: 100 }),
      ]);
      setAvailable(qPage?.content ?? qPage?.items ?? []);

      // Nạp các câu hỏi đã gán trước đó (nếu có) làm trạng thái khởi tạo.
      const preset = {};
      (detail?.questions ?? []).forEach((a, idx) => {
        preset[a.questionId] = {
          score: a.score ?? 1,
          sectionName: a.sectionName ?? '',
          order: idx,
        };
      });
      setSelected(preset);
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Không thể tải dữ liệu câu hỏi.');
    } finally {
      setLoading(false);
    }
  }, [assessment, isExam]);

  useEffect(() => {
    if (isOpen) load();
    else { setSelected({}); setAvailable([]); setError(''); }
  }, [isOpen, load]);

  useEffect(() => {
    if (!isOpen) return;
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [isOpen, onClose]);

  const toggle = (q) => {
    setSelected((prev) => {
      const next = { ...prev };
      if (next[q.questionId]) {
        delete next[q.questionId];
      } else {
        const nextOrder = Object.keys(next).length;
        const defaultSection = VALID_SECTION_IDS.includes(q.skill) ? q.skill : '';
        next[q.questionId] = { score: 1, sectionName: defaultSection, order: nextOrder };
      }
      return next;
    });
  };

  const setField = (questionId, field, value) => {
    setSelected((prev) => ({ ...prev, [questionId]: { ...prev[questionId], [field]: value } }));
  };

  const selectedList = useMemo(
    () => Object.entries(selected).sort((a, b) => a[1].order - b[1].order),
    [selected],
  );
  const scoreSum = useMemo(
    () => selectedList.reduce((s, [, v]) => s + (Number(v.score) || 0), 0),
    [selectedList],
  );
  const target = Number(assessment?.totalScore) || 0;
  const scoreMatched = target > 0 && Math.abs(scoreSum - target) < 1e-9;

  // Validation: mỗi câu được chọn phải score>0; với exam phải có section hợp lệ.
  const invalidMsg = useMemo(() => {
    if (selectedList.length === 0) return 'Chọn ít nhất 1 câu hỏi.';
    for (const [qid, v] of selectedList) {
      if (!(Number(v.score) > 0)) return `Câu #${qid}: điểm phải > 0.`;
      if (isExam && !VALID_SECTION_IDS.includes(v.sectionName)) return `Câu #${qid}: chọn phần (section).`;
    }
    return '';
  }, [selectedList, isExam]);

  async function handleSave() {
    if (invalidMsg) { setError(invalidMsg); return; }
    setSaving(true);
    setError('');
    try {
      const assignments = selectedList.map(([questionId, v], idx) => {
        const item = { questionId: Number(questionId), displayOrder: idx, score: Number(v.score) };
        if (isExam || v.sectionName) item.sectionName = v.sectionName;
        return item;
      });
      await onSubmit(assignments);
    } catch (err) {
      setError(typeof err === 'string' ? err : (err?.message ?? 'Lỗi khi gán câu hỏi.'));
    } finally {
      setSaving(false);
    }
  }

  if (!isOpen || !assessment) return null;

  return (
    <div
      className="sfa-modal-backdrop"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
      role="dialog"
      aria-modal="true"
      aria-label={`Gán câu hỏi ${assessment.title}`}
    >
      <div className="sfa-modal sfa-assign-modal">
        <div className="sfa-modal-header">
          <h2 className="sfa-modal-title">Gán câu hỏi — {assessment.title}</h2>
          <button type="button" className="sfa-modal-close" onClick={onClose} aria-label="Đóng">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div className="sfa-modal-body">
          <div className="sfa-assign-summary">
            <JlptBadge level={assessment.jlptLevel} />
            <span>Đã chọn: <strong>{selectedList.length}</strong> câu</span>
            <span style={{ color: scoreMatched ? 'var(--color-secondary)' : 'var(--color-warning)' }}>
              Tổng điểm: <strong>{scoreSum}</strong>{target > 0 ? ` / ${target}` : ''}
              {target > 0 && (scoreMatched ? ' ✓' : ' (chưa khớp)')}
            </span>
          </div>

          {error && <div className="sfa-assign-error" role="alert">{error}</div>}

          {loading ? (
            <p style={{ padding: 20, textAlign: 'center' }}>Đang tải câu hỏi…</p>
          ) : available.length === 0 ? (
            <p style={{ padding: 20, textAlign: 'center' }}>
              Không có câu hỏi <strong>đã xuất bản</strong> ở cấp độ {assessment.jlptLevel}.
              Hãy tạo câu hỏi và để Manager duyệt trước.
            </p>
          ) : (
            <div className="sfa-assign-list">
              {available.map((q) => {
                const sel = selected[q.questionId];
                const checked = Boolean(sel);
                return (
                  <div key={q.questionId} className={`sfa-assign-row${checked ? ' sfa-assign-row--on' : ''}`}>
                    <label className="sfa-assign-pick">
                      <input type="checkbox" checked={checked} onChange={() => toggle(q)} />
                      <span className="sfa-assign-qtext" title={q.questionText}>
                        <span className="sfa-assign-qskill">{SKILL_LABELS[q.skill] ?? q.skill}</span>
                        {truncate(q.questionText, 70)}
                      </span>
                    </label>
                    {checked && (
                      <div className="sfa-assign-fields">
                        <label>
                          Điểm
                          <input
                            type="number" min="0" step="0.5" value={sel.score}
                            onChange={(e) => setField(q.questionId, 'score', e.target.value)}
                          />
                        </label>
                        {isExam && (
                          <label>
                            Phần
                            <select
                              value={sel.sectionName}
                              onChange={(e) => setField(q.questionId, 'sectionName', e.target.value)}
                            >
                              <option value="">— chọn —</option>
                              {EXAM_SECTIONS.map((s) => <option key={s.id} value={s.id}>{s.label}</option>)}
                            </select>
                          </label>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div className="sfa-modal-footer">
          <button type="button" className="sfa-btn-ghost" onClick={onClose} disabled={saving}>Huỷ</button>
          <button
            type="button"
            className="sfa-btn-create"
            onClick={handleSave}
            disabled={saving || loading || Boolean(invalidMsg)}
            title={invalidMsg || undefined}
          >
            {saving ? 'Đang lưu…' : 'Lưu câu hỏi'}
          </button>
        </div>
      </div>
    </div>
  );
}
