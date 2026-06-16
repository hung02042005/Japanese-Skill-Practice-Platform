import { useState, useEffect, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import { JlptBadge } from '../../components/common/Badges';
import { useToast, ToastContainer } from '../../components/common/Toast';
import QuestionFormModal from '../../components/staff/QuestionFormModal';
import QuestionPreviewDrawer from '../../components/staff/QuestionPreviewDrawer';
import StaffPageHero from '../../components/staff/StaffPageHero';
import {
  fetchQuestionsThunk,
  createQuestionThunk,
  updateQuestionThunk,
  submitQuestionReviewThunk,
} from '../../store/slices/staffQuestionSlice';
import './StaffQuestions.css';

const SKILL_LABELS = {
  vocabulary: 'Từ vựng',
  grammar: 'Ngữ pháp',
  kanji: 'Kanji',
  reading: 'Đọc hiểu',
  listening: 'Nghe',
  mixed: 'Tổng hợp',
};

const SKILL_COLORS = {
  vocabulary: { bg: '#E3F2FD', text: '#1565C0' },
  grammar: { bg: '#F3E5F5', text: '#6A1B9A' },
  kanji: { bg: '#FCE4EC', text: '#C62828' },
  reading: { bg: '#E8F5E9', text: '#2E7D32' },
  listening: { bg: '#FFF3E0', text: '#E65100' },
  mixed: { bg: '#F0EDEB', text: '#6B625E' },
};

const TYPE_LABELS = {
  multiple_choice: 'Trắc nghiệm',
  fill_blank: 'Điền vào',
  true_false: 'Đúng/Sai',
};

const STATUS_CONFIG = {
  draft: { cls: 'sfq-status--draft', label: 'Nháp' },
  pending_review: { cls: 'sfq-status--pending', label: 'Chờ duyệt' },
  published: { cls: 'sfq-status--published', label: 'Đã xuất bản' },
  rejected: { cls: 'sfq-status--rejected', label: 'Từ chối' },
  archived: { cls: 'sfq-status--draft', label: 'Lưu trữ' },
  deleted: { cls: 'sfq-status--draft', label: 'Đã xóa' },
};

const PAGE_SIZE = 10;

function truncate(str, n) {
  if (!str) return '';
  return str.length > n ? str.slice(0, n) + '…' : str;
}

export default function StaffQuestions() {
  const dispatch = useDispatch();
  const {
    items: questions,
    totalElements,
    totalPages,
    status,
    error,
  } = useSelector((state) => state.staffQuestion);
  const loading = status === 'loading' || status === 'idle';

  const [search, setSearch] = useState('');
  const [skillFilter, setSkill] = useState('');
  const [levelFilter, setLevel] = useState('');
  const [typeFilter, setType] = useState('');
  const [statusFilter, setStatus] = useState('');
  const [currentPage, setPage] = useState(1);
  const [showModal, setModal] = useState(false);
  const [editQuestion, setEditQ] = useState(null);
  const [prefillData, setPrefill] = useState(null);
  const [previewQ, setPreviewQ] = useState(null);

  const { toasts, addToast, removeToast } = useToast();

  const fetchQuestions = useCallback(() => {
    return dispatch(fetchQuestionsThunk({
      q: search || undefined,
      skill: skillFilter || undefined,
      jlptLevel: levelFilter || undefined,
      questionType: typeFilter || undefined,
      status: statusFilter || undefined,
      page: currentPage - 1,
      size: PAGE_SIZE,
    }));
  }, [dispatch, search, skillFilter, levelFilter, typeFilter, statusFilter, currentPage]);

  useEffect(() => {
    fetchQuestions();
  }, [fetchQuestions]);

  const handleFilterChange = (setter) => (e) => {
    setter(e.target.value);
    setPage(1);
  };

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

  const handleSave = async (formData) => {
    try {
      if (editQuestion) {
        await dispatch(updateQuestionThunk({ questionId: editQuestion.questionId, payload: formData })).unwrap();
        addToast('success', 'Đã cập nhật câu hỏi');
      } else {
        await dispatch(createQuestionThunk(formData)).unwrap();
        if (currentPage !== 1) setPage(1);
        addToast('success', 'Đã thêm câu hỏi mới');
      }
      closeModal();
      await fetchQuestions();
    } catch (err) {
      addToast('error', err || 'Lỗi khi lưu câu hỏi');
    }
  };

  const handleSaveAndSubmit = async (formData) => {
    try {
      let response;
      if (editQuestion) {
        response = await dispatch(updateQuestionThunk({ questionId: editQuestion.questionId, payload: formData })).unwrap();
      } else {
        response = await dispatch(createQuestionThunk(formData)).unwrap();
        if (currentPage !== 1) setPage(1);
      }
      // response = ApiResponse { status, message, data: { questionId, ... } }
      const questionId = response?.data?.questionId;
      await dispatch(submitQuestionReviewThunk(questionId)).unwrap();
      addToast('success', 'Đã lưu và gửi câu hỏi #' + questionId + ' để duyệt');
      closeModal();
      await fetchQuestions();
    } catch (err) {
      addToast('error', err || 'Lỗi khi lưu và gửi duyệt');
    }
  };

  const onSubmit = async (q) => {
    try {
      await dispatch(submitQuestionReviewThunk(q.questionId)).unwrap();
      addToast('success', 'Đã gửi câu hỏi #' + q.questionId + ' để duyệt');
      await fetchQuestions();
    } catch (err) {
      addToast('error', err || 'Lỗi khi gửi duyệt');
    }
  };

  const publishedCount = questions.filter((q) => q.status === 'published').length;
  const pendingCount = questions.filter((q) => q.status === 'pending_review').length;
  const draftCount = questions.filter((q) => q.status === 'draft').length;
  const rejectedCount = questions.filter((q) => q.status === 'rejected').length;

  return (
    <div className="sfq-page">
      <StaffTopNav activeTab="staff-questions" />

      <main className="sfq-body">
        <StaffPageHero
          accent="indigo"
          title="Ngân Hàng Câu Hỏi"
          subtitle="Tạo và quản lý câu hỏi trắc nghiệm, điền vào cho tất cả các kỹ năng và cấp độ JLPT"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M4 19 L10 15.5 L38 15.5 L44 19" />
              <line x1="10" y1="23" x2="38" y2="23" />
              <line x1="16" y1="19" x2="16" y2="44" />
              <line x1="32" y1="19" x2="32" y2="44" />
            </svg>
          }
        />

        <div className="sfq-page-header">
          <h1 className="sfq-page-title">Ngân Hàng Câu Hỏi</h1>
          <button type="button" className="sfq-btn-add" onClick={openAdd}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" aria-hidden="true">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            Thêm câu hỏi
          </button>
        </div>

        <div className="sfq-filter-bar">
          <input
            type="text"
            className="sfq-search"
            placeholder="Tìm kiếm câu hỏi…"
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(1); }}
          />
          <select className="sfq-select" value={skillFilter} onChange={handleFilterChange(setSkill)} aria-label="Lọc theo kỹ năng">
            <option value="">Kỹ năng</option>
            {Object.entries(SKILL_LABELS).map(([val, label]) => (
              <option key={val} value={val}>{label}</option>
            ))}
          </select>
          <select className="sfq-select" value={levelFilter} onChange={handleFilterChange(setLevel)} aria-label="Lọc theo cấp độ">
            <option value="">Cấp độ</option>
            {['N5', 'N4', 'N3', 'N2', 'N1'].map((lvl) => (
              <option key={lvl} value={lvl}>{lvl}</option>
            ))}
          </select>
          <select className="sfq-select sfq-col-type" value={typeFilter} onChange={handleFilterChange(setType)} aria-label="Lọc theo loại">
            <option value="">Loại</option>
            {Object.entries(TYPE_LABELS).map(([val, label]) => (
              <option key={val} value={val}>{label}</option>
            ))}
          </select>
          <select className="sfq-select" value={statusFilter} onChange={handleFilterChange(setStatus)} aria-label="Lọc theo trạng thái">
            <option value="">Trạng thái</option>
            <option value="draft">Nháp</option>
            <option value="pending_review">Chờ duyệt</option>
            <option value="published">Đã xuất bản</option>
            <option value="rejected">Từ chối</option>
            <option value="archived">Lưu trữ</option>
            <option value="deleted">Đã xoá</option>
          </select>
        </div>

        <div className="sfq-summary">
          <span>Tổng: <strong>{totalElements}</strong> câu</span>
          <span>Đã xuất bản: <strong>{publishedCount}</strong></span>
          <span>Chờ duyệt: <strong>{pendingCount}</strong></span>
          <span>Nháp: <strong>{draftCount}</strong></span>
          <span>Từ chối: <strong>{rejectedCount}</strong></span>
        </div>

        <div className="sfq-table-wrap">
          {loading ? (
            <div className="sfq-empty-wrap">
              <EmptyState title="Đang tải…" subtitle="Vui lòng chờ trong giây lát." />
            </div>
          ) : error ? (
            <div className="sfq-empty-wrap">
              <EmptyState title="Lỗi tải dữ liệu" subtitle={error} />
            </div>
          ) : questions.length === 0 ? (
            <div className="sfq-empty-wrap">
              <EmptyState title="Không tìm thấy câu hỏi" subtitle="Thử thay đổi bộ lọc hoặc từ khoá tìm kiếm." />
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
                {questions.map((q) => {
                  const skillColor = SKILL_COLORS[q.skill] ?? SKILL_COLORS.mixed;
                  const statusInfo = STATUS_CONFIG[q.status] ?? STATUS_CONFIG.draft;
                  const qId = q.questionId ?? q.id;
                  return (
                    <tr key={qId} className={q.isLocked ? 'sfq-row--locked' : ''}>
                      <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>
                        {`#${qId}`}
                      </td>
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
                      <td>
                        <span className="sfq-skill-badge" style={{ background: skillColor.bg, color: skillColor.text }}>
                          {SKILL_LABELS[q.skill] ?? q.skill}
                        </span>
                      </td>
                      <td><JlptBadge level={q.jlptLevel} /></td>
                      <td className="sfq-col-type">
                        <span className="sfq-type-pill">{TYPE_LABELS[q.questionType] ?? q.questionType}</span>
                      </td>
                      <td>
                        <span className={'sfq-status ' + statusInfo.cls}>{statusInfo.label}</span>
                        {q.isLocked && (
                          <span className="sfq-lock-icon" title="Câu hỏi đã bị khóa do có attempt" aria-label="Câu hỏi đã bị khóa">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                            </svg>
                          </span>
                        )}
                      </td>
                      <td>
                        <div className="sfq-actions">
                          <button type="button" className="sfq-btn-icon" onClick={() => setPreviewQ(q)} title="Xem trước" aria-label="Xem trước câu hỏi">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                              <circle cx="12" cy="12" r="3" />
                            </svg>
                          </button>
                          {q.isLocked && (
                            <button type="button" className="sfq-btn--newver" onClick={() => onNewVersion(q)}>
                              Tạo phiên bản mới
                            </button>
                          )}
                          {!q.isLocked && (q.status === 'draft' || q.status === 'rejected') && (
                            <>
                              <button type="button" className="sfq-btn-icon" onClick={() => onEdit(q)} title="Chỉnh sửa" aria-label="Chỉnh sửa câu hỏi">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                                </svg>
                              </button>
                              <button type="button" className="sfq-btn--submit" onClick={() => onSubmit(q)}>
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

        <Pagination currentPage={currentPage} totalPages={totalPages} onChange={setPage} />
      </main>

      <QuestionFormModal isOpen={showModal} editQuestion={editQuestion} prefillData={prefillData} onClose={closeModal} onSave={handleSave} onSaveAndSubmit={handleSaveAndSubmit} />
      <QuestionPreviewDrawer question={previewQ} onClose={() => setPreviewQ(null)} />
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
