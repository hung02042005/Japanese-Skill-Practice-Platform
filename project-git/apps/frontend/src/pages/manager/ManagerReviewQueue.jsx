import { useState, useEffect, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import ManagerTopNav from '../../components/layout/ManagerTopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import { useToast, ToastContainer } from '../../components/common/Toast';
import StaffPageHero from '../../components/staff/StaffPageHero';
import {
  fetchReviewQueueThunk,
  reviewContentThunk,
  requestChangesThunk,
} from '../../store/slices/managerReviewSlice';
import './ManagerReviewQueue.css';

const TYPE_META = {
  lesson:     { label: 'Bài học',  bg: '#E8F5E9', text: '#2E7D32' },
  question:   { label: 'Câu hỏi', bg: '#FCE4EC', text: '#C62828' },
  vocabulary: { label: 'Từ vựng', bg: '#E3F2FD', text: '#1565C0' },
  grammar:    { label: 'Ngữ pháp', bg: '#F3E5F5', text: '#6A1B9A' },
  kanji:      { label: 'Kanji',   bg: '#FCE4EC', text: '#C62828' },
  exam:       { label: 'Đề thi',  bg: '#FFF3E0', text: '#E65100' },
  assessment: { label: 'Quiz',    bg: '#EDE7F6', text: '#4527A0' },
  quiz:       { label: 'Quiz',    bg: '#EDE7F6', text: '#4527A0' },
};

const PAGE_SIZE = 20;

function TypeChip({ type }) {
  const m = TYPE_META[type] ?? { label: type, bg: '#EEE', text: '#666' };
  return (
    <span className="mrq-type-chip" style={{ background: m.bg, color: m.text }}>
      {m.label}
    </span>
  );
}

function formatDateTime(value) {
  if (!value) return '';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

export default function ManagerReviewQueue() {
  const dispatch = useDispatch();
  const { items, totalPages, totalElements, status, error } = useSelector((state) => state.managerReview);
  const loading = status === 'loading' || status === 'idle';

  const [typeFilter, setType] = useState('');
  const [levelFilter, setLevel] = useState('');
  const [currentPage, setPage] = useState(1);
  const { toasts, addToast, removeToast } = useToast();

  const fetchQueue = useCallback(() => {
    return dispatch(fetchReviewQueueThunk({
      type: typeFilter || undefined,
      jlptLevel: levelFilter || undefined,
      page: currentPage - 1,
      size: PAGE_SIZE,
    }));
  }, [dispatch, typeFilter, levelFilter, currentPage]);

  useEffect(() => {
    fetchQueue();
  }, [fetchQueue]);

  const handleFilter = (setter) => (e) => {
    setter(e.target.value);
    setPage(1);
  };

  async function handleApprove(item) {
    try {
      await dispatch(reviewContentThunk({
        contentType: item.contentType,
        contentId: item.contentId,
        action: 'APPROVE',
      })).unwrap();
      addToast('success', `Đã duyệt: ${item.titleOrText}`);
      await fetchQueue();
    } catch (err) {
      addToast('error', err || 'Lỗi khi duyệt nội dung');
    }
  }

  async function handleReject(item) {
    const feedback = window.prompt('Nhập lý do từ chối (bắt buộc):', '');
    if (feedback === null) return; // cancelled
    if (!feedback.trim()) {
      addToast('error', 'Vui lòng nhập lý do từ chối.');
      return;
    }
    try {
      await dispatch(reviewContentThunk({
        contentType: item.contentType,
        contentId: item.contentId,
        action: 'REJECT',
        feedback: feedback.trim(),
      })).unwrap();
      addToast('error', `Đã từ chối: ${item.titleOrText}`);
      await fetchQueue();
    } catch (err) {
      addToast('error', err || 'Lỗi khi từ chối nội dung');
    }
  }

  async function handleRequestChanges(item) {
    const feedback = window.prompt('Nhập nội dung yêu cầu chỉnh sửa (bắt buộc):', '');
    if (feedback === null) return;
    if (!feedback.trim()) {
      addToast('error', 'Vui lòng nhập nội dung yêu cầu chỉnh sửa.');
      return;
    }
    try {
      await dispatch(requestChangesThunk({
        contentType: item.contentType,
        contentId: item.contentId,
        feedback: feedback.trim(),
      })).unwrap();
      addToast('success', `Đã yêu cầu chỉnh sửa: ${item.titleOrText}`);
      await fetchQueue();
    } catch (err) {
      addToast('error', err || 'Lỗi khi yêu cầu chỉnh sửa');
    }
  }

  return (
    <div className="mrq-page">
      <ManagerTopNav activeTab="manager-review" />

      <main className="mrq-body">
        <StaffPageHero
          accent="gold"
          title="Hàng Đợi Duyệt"
          subtitle="Xét duyệt và phê duyệt nội dung do Staff soạn thảo trước khi xuất bản"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <rect x="20" y="4" width="8" height="13" rx="3"/>
              <rect x="20" y="17" width="8" height="5"/>
              <rect x="14" y="22" width="20" height="18" rx="3"/>
              <line x1="18" y1="31" x2="30" y2="31"/>
              <line x1="24" y1="25" x2="24" y2="37"/>
              <circle cx="24" cy="31" r="7" strokeWidth="0.8" opacity="0.4"/>
            </svg>
          }
        />

        <div className="mrq-page-header">
          <div>
            <h1 className="mrq-page-title">Hàng Đợi Duyệt</h1>
            <p className="mrq-subtitle">Duyệt nội dung do Staff gửi lên trước khi xuất bản cho học viên.</p>
          </div>
          {totalElements > 0 && (
            <span className="mrq-pending-count">{totalElements} mục chờ duyệt</span>
          )}
        </div>

        <div className="mrq-filter-bar">
          <select className="mrq-select" value={typeFilter} onChange={handleFilter(setType)} aria-label="Lọc theo loại">
            <option value="">Tất cả loại</option>
            {Object.entries(TYPE_META)
              .filter(([k]) => k !== 'quiz') // 'assessment' đã đại diện quiz
              .map(([k, v]) => (
                <option key={k} value={k}>{v.label}</option>
              ))}
          </select>

          <select className="mrq-select" value={levelFilter} onChange={handleFilter(setLevel)} aria-label="Lọc theo cấp độ">
            <option value="">Tất cả cấp độ</option>
            {['N5','N4','N3','N2','N1'].map((l) => <option key={l} value={l}>{l}</option>)}
          </select>
        </div>

        {loading ? (
          <EmptyState title="Đang tải…" subtitle="Vui lòng chờ trong giây lát." mascotVariant="thinking" mascotSize={100} />
        ) : error ? (
          <EmptyState title="Lỗi tải dữ liệu" subtitle={error} mascotVariant="thinking" mascotSize={100} />
        ) : items.length === 0 ? (
          <EmptyState
            title="Không còn mục nào chờ duyệt"
            subtitle="Tất cả nội dung đã được xử lý. Làm tốt lắm!"
            mascotVariant="celebrate"
            mascotSize={120}
          />
        ) : (
          <div className="mrq-table-wrap">
            <table className="mrq-table">
              <caption className="mrq-visually-hidden">Hàng đợi duyệt nội dung</caption>
              <thead>
                <tr>
                  <th>Nội dung</th>
                  <th>Người gửi</th>
                  <th>Level</th>
                  <th>Thời gian gửi</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={`${item.contentType}-${item.contentId}`}>
                    <td>
                      <div className="mrq-content-cell">
                        <TypeChip type={item.contentType} />
                        <span className="mrq-content-title">{item.titleOrText}</span>
                      </div>
                    </td>
                    <td className="mrq-submitter">{item.submittedBy}</td>
                    <td><JlptBadge level={item.jlptLevel} /></td>
                    <td className="mrq-date">{formatDateTime(item.submittedAt)}</td>
                    <td>
                      <div className="mrq-actions">
                        <button
                          className="mrq-btn-approve"
                          onClick={() => handleApprove(item)}
                          aria-label={`Duyệt ${item.titleOrText}`}
                        >
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                          Duyệt
                        </button>
                        <button
                          className="mrq-btn-reject"
                          onClick={() => handleRequestChanges(item)}
                          aria-label={`Yêu cầu chỉnh sửa ${item.titleOrText}`}
                          title="Trả lại cho Staff để chỉnh sửa"
                        >
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M3 7v6h6M21 17a9 9 0 0 0-15-6.7L3 13" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                          Yêu cầu sửa
                        </button>
                        <button
                          className="mrq-btn-reject"
                          onClick={() => handleReject(item)}
                          aria-label={`Từ chối ${item.titleOrText}`}
                        >
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
                          </svg>
                          Từ chối
                        </button>
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

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
