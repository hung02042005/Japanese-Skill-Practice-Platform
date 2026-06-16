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
    if (!window.confirm(`Hủy xuất bản: ${item.titleOrText ?? item.title}?`)) return;
    try {
      await dispatch(changePublishedStatusThunk({
        contentId: item.contentId,
        payload: { newStatus: 'archived' },
      })).unwrap();
      setActionMsg(`Đã hủy xuất bản: ${item.titleOrText ?? item.title}`);
      fetchData();
    } catch (err) {
      setActionMsg(err || 'Lỗi khi hủy xuất bản');
    }
  }

  async function handleRestore(item) {
    if (!window.confirm(`Khôi phục: ${item.titleOrText ?? item.title}?`)) return;
    try {
      await dispatch(restorePublishedContentThunk({
        contentId: item.contentId,
        payload: { targetStatus: 'published' },
      })).unwrap();
      setActionMsg(`Đã khôi phục: ${item.titleOrText ?? item.title}`);
      fetchData();
    } catch (err) {
      setActionMsg(err || 'Lỗi khi khôi phục');
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
            {Object.entries(TYPE_META).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
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
                            title="Hủy xuất bản / Lưu trữ"
                          >
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                              <path d="M4 7V4h16v3M4 7l2 13h12l2-13M4 7h16" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            </svg>
                            Lưu trữ
                          </button>
                          {item.status === 'archived' && (
                            <button
                              className="mcp-btn-restore"
                              onClick={() => handleRestore(item)}
                              title="Khôi phục"
                            >
                              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                <path d="M3 7v6h6M21 17a9 9 0 0 0-15-6.7L3 13" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
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
