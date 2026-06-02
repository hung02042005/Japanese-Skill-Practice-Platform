import { useState } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import { JlptBadge } from '../../components/common/Badges';
import { useToast, ToastContainer } from '../../components/common/Toast';
import QuestionFormModal from '../../components/staff/QuestionFormModal';
import QuestionPreviewDrawer from '../../components/staff/QuestionPreviewDrawer';
import './StaffQuestions.css';

/* ─── Mock data ──────────────────────────────────────────────────── */
const MOCK_QUESTIONS = [
  {
    id: 101,
    questionText: "N5 Kanji: '水' đọc là gì? Chọn đáp án đúng.",
    skill: 'kanji',
    jlptLevel: 'N5',
    questionType: 'multiple_choice',
    status: 'published',
    isLocked: true,
    options: { A: 'mizu', B: 'kawa', C: 'yama', D: 'ki' },
    correctOption: 'A',
    explanation: "'水' là nước, đọc là mizu (くん読み).",
  },
  {
    id: 102,
    questionText: "Chọn từ đồng nghĩa với 'うれしい' (vui vẻ, sung sướng)",
    skill: 'vocabulary',
    jlptLevel: 'N4',
    questionType: 'multiple_choice',
    status: 'draft',
    isLocked: false,
    options: { A: 'たのしい', B: 'かなしい', C: 'こわい', D: 'むずかしい' },
    correctOption: 'A',
    explanation: "'たのしい' cũng có nghĩa vui vẻ, gần nghĩa nhất với うれしい.",
  },
  {
    id: 103,
    questionText: '～てから có nghĩa là gì?',
    skill: 'grammar',
    jlptLevel: 'N4',
    questionType: 'multiple_choice',
    status: 'pending_review',
    isLocked: false,
    options: { A: 'Sau khi ~', B: 'Trước khi ~', C: 'Trong khi ~', D: 'Vì ~' },
    correctOption: 'A',
    explanation: '～てから = sau khi làm việc gì đó. Ví dụ: 食べてから行く = Ăn xong rồi mới đi.',
  },
  {
    id: 104,
    questionText: '私は毎日___で学校に行きます。(Tôi đến trường bằng phương tiện gì hàng ngày?)',
    skill: 'grammar',
    jlptLevel: 'N5',
    questionType: 'fill_blank',
    status: 'draft',
    isLocked: false,
    correctAnswer: 'バス',
    explanation: 'Điền phương tiện giao thông phù hợp, ví dụ: バス (xe buýt).',
  },
  {
    id: 105,
    questionText: '「すみません」 có thể dùng để xin lỗi và để thu hút sự chú ý.',
    skill: 'vocabulary',
    jlptLevel: 'N5',
    questionType: 'true_false',
    status: 'published',
    isLocked: true,
    correctAnswer: 'Đúng',
    explanation: 'すみません đa dụng: xin lỗi (I\'m sorry) và gây chú ý (Excuse me).',
  },
  {
    id: 106,
    questionText: 'Kanji 山 có nghĩa là gì?',
    skill: 'kanji',
    jlptLevel: 'N5',
    questionType: 'multiple_choice',
    status: 'rejected',
    isLocked: false,
    options: { A: 'Núi', B: 'Sông', C: 'Biển', D: 'Hồ' },
    correctOption: 'A',
    explanation: "'山' (やま) = núi.",
  },
  {
    id: 107,
    questionText: 'Trong đoạn văn sau, tác giả muốn nói điều gì? (đọc hiểu N3)',
    skill: 'reading',
    jlptLevel: 'N3',
    questionType: 'multiple_choice',
    status: 'draft',
    isLocked: false,
    options: {
      A: 'Tầm quan trọng của việc học',
      B: 'Vẻ đẹp thiên nhiên',
      C: 'Cuộc sống đô thị',
      D: 'Truyền thống gia đình',
    },
    correctOption: 'A',
    explanation: 'Đoạn văn mẫu tập trung vào giáo dục và học tập.',
  },
];

const SKILL_LABELS = {
  vocabulary: 'Từ vựng',
  grammar:    'Ngữ pháp',
  kanji:      'Kanji',
  reading:    'Đọc hiểu',
  listening:  'Nghe',
  mixed:      'Tổng hợp',
};

const SKILL_COLORS = {
  vocabulary: { bg: '#E3F2FD', text: '#1565C0' },
  grammar:    { bg: '#F3E5F5', text: '#6A1B9A' },
  kanji:      { bg: '#FCE4EC', text: '#C62828' },
  reading:    { bg: '#E8F5E9', text: '#2E7D32' },
  listening:  { bg: '#FFF3E0', text: '#E65100' },
  mixed:      { bg: '#F0EDEB', text: '#6B625E' },
};

const TYPE_LABELS = {
  multiple_choice: 'Trắc nghiệm',
  fill_blank:      'Điền vào',
  true_false:      'Đúng/Sai',
};

const STATUS_CONFIG = {
  draft:          { cls: 'sfq-status--draft',     label: 'Nháp' },
  pending_review: { cls: 'sfq-status--pending',   label: 'Chờ duyệt' },
  published:      { cls: 'sfq-status--published', label: 'Đã xuất bản' },
  rejected:       { cls: 'sfq-status--rejected',  label: 'Từ chối' },
};

const PAGE_SIZE = 10;

/* ─── Truncate helper ────────────────────────────────────────────── */
function truncate(str, n) {
  if (!str) return '';
  return str.length > n ? str.slice(0, n) + '…' : str;
}

/* ─── StaffQuestions ─────────────────────────────────────────────── */
export default function StaffQuestions() {
  const [questions,    setQuestions]  = useState(MOCK_QUESTIONS);
  const [search,       setSearch]     = useState('');
  const [skillFilter,  setSkill]      = useState('');
  const [levelFilter,  setLevel]      = useState('');
  const [typeFilter,   setType]       = useState('');
  const [statusFilter, setStatus]     = useState('');
  const [currentPage,  setPage]       = useState(1);
  const [showModal,    setModal]       = useState(false);
  const [editQuestion, setEditQ]      = useState(null);
  const [prefillData,  setPrefill]    = useState(null);
  const [previewQ,     setPreviewQ]   = useState(null);

  const { toasts, addToast, removeToast } = useToast();

  /* ── Filtering ───────────────────────────────────────────────── */
  const filtered = questions.filter((q) => {
    if (search      && !q.questionText.toLowerCase().includes(search.toLowerCase())) return false;
    if (skillFilter && q.skill       !== skillFilter) return false;
    if (levelFilter && q.jlptLevel   !== levelFilter) return false;
    if (typeFilter  && q.questionType !== typeFilter)  return false;
    if (statusFilter && q.status     !== statusFilter) return false;
    return true;
  });

  /* ── Pagination ──────────────────────────────────────────────── */
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage   = Math.min(currentPage, totalPages);
  const pageSlice  = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const handlePageChange = (p) => setPage(p);

  /* ── Summary counts ──────────────────────────────────────────── */
  const publishedCount = questions.filter((q) => q.status === 'published').length;
  const pendingCount   = questions.filter((q) => q.status === 'pending_review').length;
  const draftCount     = questions.filter((q) => q.status === 'draft').length;

  /* ── Modal handlers ──────────────────────────────────────────── */
  const openAdd = () => {
    setEditQ(null);
    setPrefill(null);
    setModal(true);
  };

  const onEdit = (q) => {
    setEditQ(q);
    setPrefill(null);
    setModal(true);
  };

  const onNewVersion = (q) => {
    setEditQ(null);
    setPrefill(q);
    setModal(true);
  };

  const closeModal = () => {
    setModal(false);
    setEditQ(null);
    setPrefill(null);
  };

  const handleSave = (savedQ) => {
    if (editQuestion) {
      setQuestions((prev) => prev.map((q) => (q.id === savedQ.id ? savedQ : q)));
      addToast('success', `Đã cập nhật câu hỏi #${savedQ.id}`);
    } else {
      setQuestions((prev) => [savedQ, ...prev]);
      addToast('success', `Đã thêm câu hỏi mới #${savedQ.id}`);
    }
    closeModal();
  };

  /* ── Submit for review ───────────────────────────────────────── */
  const onSubmit = (q) => {
    setQuestions((prev) =>
      prev.map((item) =>
        item.id === q.id ? { ...item, status: 'pending_review' } : item
      )
    );
    addToast('success', `Đã gửi câu hỏi #${q.id} để duyệt`);
  };

  /* ── Filter reset on filter change ──────────────────────────── */
  const handleFilterChange = (setter) => (e) => {
    setter(e.target.value);
    setPage(1);
  };

  return (
    <div className="sfq-page">
      <StaffTopNav activeTab="staff-questions" />

      <main className="sfq-body">
        {/* Page header */}
        <div className="sfq-page-header">
          <h1 className="sfq-page-title">Ngân Hàng Câu Hỏi</h1>
          <button type="button" className="sfq-btn-add" onClick={openAdd}>
            {/* Plus icon */}
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5"  y1="12" x2="19" y2="12" />
            </svg>
            Thêm câu hỏi
          </button>
        </div>

        {/* Filter bar */}
        <div className="sfq-filter-bar">
          <input
            type="text"
            className="sfq-search"
            placeholder="Tìm kiếm câu hỏi…"
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(1); }}
          />

          <select
            className="sfq-select"
            value={skillFilter}
            onChange={handleFilterChange(setSkill)}
            aria-label="Lọc theo kỹ năng"
          >
            <option value="">Kỹ năng</option>
            {Object.entries(SKILL_LABELS).map(([val, label]) => (
              <option key={val} value={val}>{label}</option>
            ))}
          </select>

          <select
            className="sfq-select"
            value={levelFilter}
            onChange={handleFilterChange(setLevel)}
            aria-label="Lọc theo cấp độ"
          >
            <option value="">Cấp độ</option>
            {['N5', 'N4', 'N3', 'N2', 'N1'].map((lvl) => (
              <option key={lvl} value={lvl}>{lvl}</option>
            ))}
          </select>

          <select
            className="sfq-select sfq-col-type"
            value={typeFilter}
            onChange={handleFilterChange(setType)}
            aria-label="Lọc theo loại"
          >
            <option value="">Loại</option>
            {Object.entries(TYPE_LABELS).map(([val, label]) => (
              <option key={val} value={val}>{label}</option>
            ))}
          </select>

          <select
            className="sfq-select"
            value={statusFilter}
            onChange={handleFilterChange(setStatus)}
            aria-label="Lọc theo trạng thái"
          >
            <option value="">Trạng thái</option>
            <option value="draft">Nháp</option>
            <option value="pending_review">Chờ duyệt</option>
            <option value="published">Đã xuất bản</option>
            <option value="rejected">Từ chối</option>
          </select>
        </div>

        {/* Summary bar */}
        <div className="sfq-summary">
          <span>Tổng: <strong>{questions.length}</strong> câu</span>
          <span>Đã xuất bản: <strong>{publishedCount}</strong></span>
          <span>Chờ duyệt: <strong>{pendingCount}</strong></span>
          <span>Nháp: <strong>{draftCount}</strong></span>
        </div>

        {/* Table */}
        <div className="sfq-table-wrap">
          {pageSlice.length === 0 ? (
            <div className="sfq-empty-wrap">
              <EmptyState
                title="Không tìm thấy câu hỏi"
                subtitle="Thử thay đổi bộ lọc hoặc từ khoá tìm kiếm."
              />
            </div>
          ) : (
            <table className="sfq-table" aria-label="Danh sách câu hỏi">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Câu hỏi</th>
                  <th>Kỹ năng</th>
                  <th>Level</th>
                  <th className="sfq-col-type">Loại</th>
                  <th>Trạng thái</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {pageSlice.map((q) => {
                  const skillColor = SKILL_COLORS[q.skill] ?? SKILL_COLORS.mixed;
                  const statusInfo = STATUS_CONFIG[q.status] ?? STATUS_CONFIG.draft;

                  return (
                    <tr
                      key={q.id}
                      className={q.isLocked ? 'sfq-row--locked' : ''}
                    >
                      {/* ID */}
                      <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>
                        #{q.id}
                      </td>

                      {/* Question text */}
                      <td>
                        <span
                          className="sfq-q-text"
                          title={q.questionText}
                          onClick={() => setPreviewQ(q)}
                          role="button"
                          tabIndex={0}
                          onKeyDown={(e) => e.key === 'Enter' && setPreviewQ(q)}
                        >
                          {truncate(q.questionText, 80)}
                        </span>
                      </td>

                      {/* Skill */}
                      <td>
                        <span
                          className="sfq-skill-badge"
                          style={{ background: skillColor.bg, color: skillColor.text }}
                        >
                          {SKILL_LABELS[q.skill] ?? q.skill}
                        </span>
                      </td>

                      {/* Level */}
                      <td>
                        <JlptBadge level={q.jlptLevel} />
                      </td>

                      {/* Type */}
                      <td className="sfq-col-type">
                        <span className="sfq-type-pill">
                          {TYPE_LABELS[q.questionType] ?? q.questionType}
                        </span>
                      </td>

                      {/* Status */}
                      <td>
                        <span className={`sfq-status ${statusInfo.cls}`}>
                          {statusInfo.label}
                        </span>
                        {q.isLocked && (
                          <span
                            className="sfq-lock-icon"
                            title="Câu hỏi đã bị khóa do có attempt"
                            aria-label="Câu hỏi đã bị khóa"
                          >
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                            </svg>
                          </span>
                        )}
                      </td>

                      {/* Actions */}
                      <td>
                        <div className="sfq-actions">
                          {/* View (always) */}
                          <button
                            type="button"
                            className="sfq-btn-icon"
                            onClick={() => setPreviewQ(q)}
                            title="Xem trước"
                            aria-label="Xem trước câu hỏi"
                          >
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                              <circle cx="12" cy="12" r="3" />
                            </svg>
                          </button>

                          {/* Locked: new version button */}
                          {q.isLocked && (
                            <button
                              type="button"
                              className="sfq-btn--newver"
                              onClick={() => onNewVersion(q)}
                            >
                              Tạo phiên bản mới
                            </button>
                          )}

                          {/* Unlocked + editable statuses */}
                          {!q.isLocked && (q.status === 'draft' || q.status === 'rejected') && (
                            <>
                              <button
                                type="button"
                                className="sfq-btn-icon"
                                onClick={() => onEdit(q)}
                                title="Chỉnh sửa"
                                aria-label="Chỉnh sửa câu hỏi"
                              >
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                                </svg>
                              </button>
                              <button
                                type="button"
                                className="sfq-btn--submit"
                                onClick={() => onSubmit(q)}
                              >
                                Gửi duyệt
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>

        {/* Pagination */}
        <Pagination
          currentPage={safePage}
          totalPages={totalPages}
          onChange={handlePageChange}
        />
      </main>

      {/* Modals & drawers */}
      <QuestionFormModal
        isOpen={showModal}
        editQuestion={editQuestion}
        prefillData={prefillData}
        onClose={closeModal}
        onSave={handleSave}
      />

      <QuestionPreviewDrawer
        question={previewQ}
        onClose={() => setPreviewQ(null)}
      />

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
