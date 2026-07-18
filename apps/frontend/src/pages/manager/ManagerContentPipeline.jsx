import { useState, useCallback, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import ManagerTopNav from '../../components/layout/ManagerTopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import StaffPageHero from '../../components/staff/StaffPageHero';
import {
  fetchPublishedContentsThunk,
  changePublishedStatusThunk,
  restorePublishedContentThunk,
} from '../../store/slices/publishedContentSlice';
import './ManagerContentPipeline.css';

const TYPE_META = {
  lesson:     { label: 'Bài học',  bg: '#E8F5E9', text: '#2E7D32' },
  question:   { label: 'Câu hỏi', bg: '#FCE4EC', text: '#C62828' },
  vocabulary: { label: 'Từ vựng', bg: '#E3F2FD', text: '#1565C0' },
  grammar:    { label: 'Ngữ pháp', bg: '#F3E5F5', text: '#6A1B9A' },
  kanji:      { label: 'Kanji',   bg: '#FCE4EC', text: '#C62828' },
  exam:       { label: 'Đề thi',  bg: '#FFF3E0', text: '#E65100' },
  quiz:       { label: 'Quiz',    bg: '#E8EAF6', text: '#283593' },
  assessment: { label: 'Bài kiểm', bg: '#E8EAF6', text: '#283593' },
};

function TypeChip({ type }) {
  const m = TYPE_META[type] ?? { label: type, bg: '#EEE', text: '#666' };
  return (
    <span className="mcp-type-chip" style={{ background: m.bg, color: m.text }}>
      {m.label}
    </span>
  );
}

const PAGE_SIZE = 10;

export default function ManagerContentPipeline() {
  const dispatch = useDispatch();
  const { items, totalPages, status, error } = useSelector((state) => state.publishedContent);
  const loading = status === 'loading' || status === 'idle';

  const [typeFilter,  setType]    = useState('');
  const [levelFilter, setLevel]   = useState('');
  const [currentPage, setPage]    = useState(1);
  const [actionMsg,   setActionMsg] = useState('');
  const [pendingId,   setPendingId] = useState(null);

  const fetchData = useCallback(() => {
    return dispatch(fetchPublishedContentsThunk({
      type: typeFilter || undefined,
      jlptLevel: levelFilter || undefined,
      page: currentPage - 1,
      size: PAGE_SIZE,
    }));
  }, [dispatch, typeFilter, levelFilter, currentPage]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleFilter = (setter) => (e) => {
    setter(e.target.value);
    setPage(1);
  };

  async function handleUnpublish(item) {
    const title = item.titleOrText ?? item.title;
    if (!window.confirm(`Hủy xuất bản: ${title}?`)) return;
    const reason = window.prompt('Nhập lý do hủy xuất bản (tùy chọn):', '') ?? '';
    setPendingId(item.contentId);
    try {
      await dispatch(changePublishedStatusThunk({
        contentId: item.contentId,
        payload: { contentType: item.contentType, status: 'archived', reason: reason.trim() || undefined },
      })).unwrap();
      setActionMsg(`Đã hủy xuất bản: ${title}`);
      fetchData();
    } catch (err) {
      setActionMsg(typeof err === 'string' ? err : 'Có lỗi xảy ra khi hủy xuất bản, vui lòng thử lại.');
    } finally {
      setPendingId(null);
    }
  }

  async function handleRestore(item) {
    const title = item.titleOrText ?? item.title;
    if (!window.confirm(`Khôi phục: ${title}?`)) return;
    setPendingId(item.contentId);
    try {
      await dispatch(restorePublishedContentThunk({
        contentId: item.contentId,
        payload: { contentType: item.contentType },
      })).unwrap();
      setActionMsg(`Đã khôi phục: ${title}`);
      fetchData();
    } catch (err) {
      setActionMsg(typeof err === 'string' ? err : 'Có lỗi xảy ra khi khôi phục, vui lòng thử lại.');
    } finally {
      setPendingId(null);
    }
  }

  async function handleDelete(item) {
    const title = item.titleOrText ?? item.title;
    if (!window.confirm(`Bạn có chắc muốn xóa: "${title}"?\nMục này sẽ được chuyển vào Thư mục đã xóa.`)) return;
    const reason = window.prompt('Nhập lý do xóa (bắt buộc, tối thiểu 10 ký tự):', '') ?? '';
    if (!reason.trim()) return;
    if (reason.trim().length < 10) {
      alert('Lý do xóa phải có ít nhất 10 ký tự.');
      return;
    }
    setPendingId(item.contentId);
    try {
      await dispatch(changePublishedStatusThunk({
        contentId: item.contentId,
        payload: { contentType: item.contentType, status: 'deleted', reason: reason.trim() },
      })).unwrap();
      setActionMsg(`Đã xóa và chuyển vào Thư mục đã xóa: ${title}`);
      fetchData();
    } catch (err) {
      setActionMsg(typeof err === 'string' ? err : 'Có lỗi xảy ra khi xóa, vui lòng thử lại.');
    } finally {
      setPendingId(null);
    }
  }

  return (
    <div className="mcp-page">
      <ManagerTopNav activeTab="manager-pipeline" />

      <main className="mcp-body">
        <StaffPageHero
          accent="pink"
          title="Pipeline Nội Dung"
          subtitle="Quản lý nội dung đã xuất bản — hủy xuất bản, lưu trữ hoặc khôi phục"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <rect x="4"  y="8" width="10" height="32" rx="2"/>
              <rect x="19" y="8" width="10" height="22" rx="2"/>
              <rect x="34" y="8" width="10" height="14" rx="2"/>
              <line x1="14" y1="24" x2="19" y2="24"/>
              <line x1="29" y1="21" x2="34" y2="21"/>
            </svg>
          }
        />

        {/* Action feedback */}
        {actionMsg && (
          <div className="mcp-action-banner" role="alert" onClick={() => setActionMsg('')}>
            {actionMsg} <span style={{ cursor: 'pointer', marginLeft: 8 }}>✕</span>
          </div>
        )}

        {/* Filter bar */}
        <div className="mcp-filter-bar">
          <select className="mcp-select" value={typeFilter} onChange={handleFilter(setType)} aria-label="Lọc theo loại">
            <option value="">Tất cả loại</option>
            {Object.entries(TYPE_META)
              // Backend ContentType chỉ có 'assessment' (gộp quiz + đề thi) — không có 'exam'/'quiz'
              // riêng, gửi type=exam hoặc type=quiz sẽ bị 400 VALIDATION_FAILED ở ManagedContentResolver.
              .filter(([k]) => k !== 'exam' && k !== 'quiz')
              .map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
          </select>
          <select className="mcp-select" value={levelFilter} onChange={handleFilter(setLevel)} aria-label="Lọc theo cấp độ">
            <option value="">Tất cả cấp độ</option>
            {['N5','N4','N3','N2','N1'].map((l) => <option key={l} value={l}>{l}</option>)}
          </select>
          {!loading && <span className="mcp-result-count">{items.length} mục</span>}
        </div>

        {/* Error */}
        {error && (
          <div className="mcp-error-banner" role="alert">
            <span>{error}</span>
            <button className="mcp-retry-btn" onClick={fetchData}>Thử lại</button>
          </div>
        )}

        {/* Table */}
        {loading ? (
          <div className="mcp-skel-wrap" aria-hidden="true">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="mcp-row-skel" />
            ))}
          </div>
        ) : items.length === 0 ? (
          <EmptyState
            title="Không có nội dung nào"
            subtitle="Chưa có nội dung nào được xuất bản."
            mascotVariant="empty"
            mascotSize={120}
          />
        ) : (
          <>
            <div className="mcp-table-wrap">
              <table className="mcp-table">
                <thead>
                  <tr>
                    <th scope="col">Nội dung</th>
                    <th scope="col">Người tạo</th>
                    <th scope="col">Level</th>
                    <th scope="col">Ngày xuất bản</th>
                    <th scope="col">Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((item) => (
                    <tr key={`${item.contentType}-${item.contentId}`}>
                      <td>
                        <div className="mcp-content-cell">
                          <TypeChip type={item.contentType} />
                          <span className="mcp-content-title">{item.titleOrText ?? item.title}</span>
                        </div>
                      </td>
                      <td className="mcp-td-staff">{item.submittedBy ?? item.createdBy ?? '—'}</td>
                      <td><JlptBadge level={item.jlptLevel} /></td>
                      <td className="mcp-td-date">{item.publishedAt ? new Date(item.publishedAt).toLocaleDateString('vi-VN') : '—'}</td>
                      <td>
                        <div className="mcp-actions">
                          <button
                            className="mcp-btn-archive"
                            onClick={() => handleUnpublish(item)}
                            disabled={pendingId === item.contentId}
                            title="Hủy xuất bản / Lưu trữ"
                          >
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                              <path d="M4 7V4h16v3M4 7l2 13h12l2-13M4 7h16" />
                            </svg>
                            Lưu trữ
                          </button>
                          
                          <button
                            className="mcp-btn-delete"
                            onClick={() => handleDelete(item)}
                            disabled={pendingId === item.contentId}
                            title="Xóa nội dung"
                          >
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="3 6 5 6 21 6"></polyline>
                              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                              <line x1="10" y1="11" x2="10" y2="17"></line>
                              <line x1="14" y1="11" x2="14" y2="17"></line>
                            </svg>
                            Xóa
                          </button>

                          {item.status === 'archived' && (
                            <button
                              className="mcp-btn-restore"
                              onClick={() => handleRestore(item)}
                              disabled={pendingId === item.contentId}
                              title="Khôi phục"
                            >
                              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M3 7v6h6M21 17a9 9 0 0 0-15-6.7L3 13" />
                              </svg>
                              Khôi phục
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {totalPages > 1 && (
              <Pagination currentPage={currentPage} totalPages={totalPages} onChange={setPage} />
            )}
          </>
        )}
      </main>
    </div>
  );
}
