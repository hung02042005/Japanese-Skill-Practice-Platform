import { useState, useEffect, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import { useToast, ToastContainer } from '../../components/common/Toast';
import StaffPageHero from '../../components/staff/StaffPageHero';
import AssessmentFormModal from '../../components/staff/AssessmentFormModal';
import AssessmentPreviewModal from '../../components/staff/AssessmentPreviewModal';
import {
  fetchQuizzesThunk,
  createQuizThunk,
  updateQuizThunk,
  submitQuizReviewThunk,
} from '../../store/slices/staffQuizSlice';
import {
  fetchExamsThunk,
  createExamThunk,
  updateExamThunk,
  submitExamReviewThunk,
} from '../../store/slices/staffExamSlice';
import './StaffAssessments.css';

const STATUS_META = {
  draft:          { label: 'Nháp',         bg: '#F0EDEB',                        color: 'var(--color-text-sub)' },
  pending_review: { label: 'Chờ duyệt',    bg: 'var(--color-accent-bg)',          color: 'var(--color-warning)' },
  published:      { label: 'Đã xuất bản',  bg: 'var(--color-secondary-bg)',       color: 'var(--color-secondary)' },
  rejected:       { label: 'Từ chối',      bg: '#FFEAEA',                         color: 'var(--color-error)' },
  archived:       { label: 'Lưu trữ',      bg: '#F0EDEB',                         color: 'var(--color-text-sub)' },
};

function StatusBadge({ status }) {
  const m = STATUS_META[status] ?? STATUS_META.draft;
  return (
    <span className="sfa-status" style={{ background: m.bg, color: m.color }}>
      {m.label}
    </span>
  );
}

function formatDate(value) {
  if (!value) return '';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString('vi-VN');
}

const TABS = [
  { id: 'quiz', label: 'Quiz' },
  { id: 'exam', label: 'Đề thi' },
];

const PAGE_SIZE = 10;

export default function StaffAssessments() {
  const dispatch = useDispatch();
  const [activeTab, setActiveTab] = useState('quiz');
  const [modalOpen, setModalOpen] = useState(false);
  const [editItem, setEditItem] = useState(null);
  const [previewItem, setPreviewItem] = useState(null);
  const [levelFilter, setLevel] = useState('');
  const [statusFilter, setStatus] = useState('');
  const [currentPage, setPage] = useState(1);
  const { toasts, addToast, removeToast } = useToast();

  const quizState = useSelector((state) => state.staffQuiz);
  const examState = useSelector((state) => state.staffExam);
  const isQuiz = activeTab === 'quiz';
  const view = isQuiz ? quizState : examState;
  const items = view.items;
  const totalPages = view.totalPages;
  const loading = view.status === 'loading' || view.status === 'idle';
  const error = view.error;

  const fetchList = useCallback(() => {
    const filters = {
      jlptLevel: levelFilter || undefined,
      status: statusFilter || undefined,
      page: currentPage - 1,
      size: PAGE_SIZE,
    };
    return dispatch(isQuiz ? fetchQuizzesThunk(filters) : fetchExamsThunk(filters));
  }, [dispatch, isQuiz, levelFilter, statusFilter, currentPage]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const handleTabChange = (id) => {
    setActiveTab(id);
    setLevel('');
    setStatus('');
    setPage(1);
  };

  const handleFilter = (setter) => (e) => {
    setter(e.target.value);
    setPage(1);
  };

  function handleCreate() {
    setEditItem(null);
    setModalOpen(true);
  }

  function handleEdit(item) {
    setEditItem(item);
    setModalOpen(true);
  }

  function closeModal() {
    setModalOpen(false);
    setEditItem(null);
  }

  // Lưu nháp (tạo mới hoặc cập nhật)
  async function handleSave(payload) {
    try {
      if (editItem) {
        const assessmentId = editItem.assessmentId;
        await dispatch(isQuiz
          ? updateQuizThunk({ assessmentId, payload })
          : updateExamThunk({ assessmentId, payload })).unwrap();
        addToast('success', `Đã cập nhật: ${payload.title}`);
      } else {
        await dispatch(isQuiz ? createQuizThunk(payload) : createExamThunk(payload)).unwrap();
        if (currentPage !== 1) setPage(1);
        addToast('success', `Đã lưu nháp: ${payload.title}`);
      }
      closeModal();
      await fetchList();
    } catch (err) {
      addToast('error', err || 'Lỗi khi lưu');
    }
  }

  // Lưu và gửi duyệt
  async function handleSaveAndSubmit(payload) {
    try {
      let assessmentId;
      if (editItem) {
        assessmentId = editItem.assessmentId;
        await dispatch(isQuiz
          ? updateQuizThunk({ assessmentId, payload })
          : updateExamThunk({ assessmentId, payload })).unwrap();
      } else {
        const res = await dispatch(isQuiz ? createQuizThunk(payload) : createExamThunk(payload)).unwrap();
        assessmentId = res?.data?.assessmentId;
        if (currentPage !== 1) setPage(1);
      }
      await dispatch(isQuiz ? submitQuizReviewThunk(assessmentId) : submitExamReviewThunk(assessmentId)).unwrap();
      addToast('success', `Đã gửi duyệt: ${payload.title}`);
      closeModal();
      await fetchList();
    } catch (err) {
      addToast('error', err || 'Lỗi khi lưu và gửi duyệt');
    }
  }

  async function handleSubmit(item) {
    try {
      await dispatch(isQuiz ? submitQuizReviewThunk(item.assessmentId) : submitExamReviewThunk(item.assessmentId)).unwrap();
      addToast('success', `Đã gửi duyệt: ${item.title}`);
      await fetchList();
    } catch (err) {
      addToast('error', err || 'Lỗi khi gửi duyệt');
    }
  }

  return (
    <div className="sfa-page">
      <StaffTopNav activeTab="staff-assessments" />

      <main className="sfa-body">
        <StaffPageHero
          accent="pink"
          title="Đề Thi & Quiz"
          subtitle="Tạo và quản lý đề thi JLPT mock và quiz theo chủ đề cho học viên"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M10 6 h20 l8 8 v28 a2 2 0 0 1-2 2 H12 a2 2 0 0 1-2-2 V8 a2 2 0 0 1 2-2 z"/>
              <polyline points="30,6 30,14 38,14"/>
              <polyline points="15,26 21,32 33,20"/>
              <line x1="15" y1="38" x2="28" y2="38"/>
              <line x1="15" y1="43" x2="24" y2="43"/>
            </svg>
          }
        />

        <div className="sfa-page-header">
          <h1 className="sfa-page-title">Đề Thi & Quiz</h1>
          <button className="sfa-btn-create" onClick={handleCreate}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
            </svg>
            Tạo mới
          </button>
        </div>

        <div className="sfa-tabs" role="tablist">
          {TABS.map((t) => (
            <button
              key={t.id}
              role="tab"
              aria-selected={activeTab === t.id}
              className={`sfa-tab${activeTab === t.id ? ' sfa-tab--active' : ''}`}
              onClick={() => handleTabChange(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>

        <div style={{ display: 'flex', gap: 12, margin: '16px 0', flexWrap: 'wrap' }}>
          <select className="sfa-select" value={levelFilter} onChange={handleFilter(setLevel)} aria-label="Lọc theo cấp độ" style={{ maxWidth: 180 }}>
            <option value="">Tất cả cấp độ</option>
            {['N5', 'N4', 'N3', 'N2', 'N1'].map((l) => <option key={l} value={l}>{l}</option>)}
          </select>
          <select className="sfa-select" value={statusFilter} onChange={handleFilter(setStatus)} aria-label="Lọc theo trạng thái" style={{ maxWidth: 200 }}>
            <option value="">Tất cả trạng thái</option>
            <option value="draft">Nháp</option>
            <option value="pending_review">Chờ duyệt</option>
            <option value="published">Đã xuất bản</option>
            <option value="rejected">Từ chối</option>
            <option value="archived">Lưu trữ</option>
          </select>
        </div>

        {loading ? (
          <EmptyState title="Đang tải…" subtitle="Vui lòng chờ trong giây lát." mascotVariant="thinking" mascotSize={100} />
        ) : error ? (
          <EmptyState title="Lỗi tải dữ liệu" subtitle={error} mascotVariant="thinking" mascotSize={100} />
        ) : items.length === 0 ? (
          <EmptyState
            title="Chưa có nội dung"
            subtitle="Bắt đầu tạo quiz hoặc đề thi đầu tiên."
            mascotVariant="thinking"
            mascotSize={120}
          />
        ) : (
          <div className="sfa-table-wrap">
            <table className="sfa-table">
              <caption className="sfa-visually-hidden">
                {isQuiz ? 'Danh sách quiz' : 'Danh sách đề thi'}
              </caption>
              <thead>
                <tr>
                  <th>Tiêu đề</th>
                  <th>Level</th>
                  <th>Số câu hỏi</th>
                  <th>Thời gian (phút)</th>
                  <th>Trạng thái</th>
                  <th>Cập nhật</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.assessmentId}>
                    <td className="sfa-title-cell">{item.title}</td>
                    <td><JlptBadge level={item.jlptLevel} /></td>
                    <td>{item.questionCount} câu</td>
                    <td>{item.durationMin} phút</td>
                    <td><StatusBadge status={item.status} /></td>
                    <td>{formatDate(item.updatedAt)}</td>
                    <td>
                      <div className="sfa-actions">
                        <button
                          className="sfa-btn-icon"
                          onClick={() => setPreviewItem(item)}
                          aria-label={`Xem ${item.title}`}
                          title="Xem"
                        >
                          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
                          </svg>
                        </button>
                        {(item.status === 'draft' || item.status === 'rejected') && (
                          <>
                            <button className="sfa-btn-icon" onClick={() => handleEdit(item)} aria-label={`Sửa ${item.title}`} title="Sửa">
                              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                              </svg>
                            </button>
                            <button
                              className="sfa-btn-submit"
                              onClick={() => handleSubmit(item)}
                              aria-label={`Gửi duyệt ${item.title}`}
                            >
                              Gửi duyệt
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination currentPage={currentPage} totalPages={totalPages} onChange={setPage} />
      </main>

      <AssessmentFormModal
        isOpen={modalOpen}
        mode={activeTab}
        editItem={editItem}
        onClose={closeModal}
        onSave={handleSave}
        onSaveAndSubmit={handleSaveAndSubmit}
      />

      <AssessmentPreviewModal
        item={previewItem}
        mode={activeTab}
        onClose={() => setPreviewItem(null)}
      />

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
