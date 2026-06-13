import { useState } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { useToast, ToastContainer } from '../../components/common/Toast';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './StaffReviewQueue.css';

const MOCK_REVIEW_ITEMS = [
  { id: 1, contentType: 'lesson', title: 'Bài 3 — Kanji N5 cơ bản', jlptLevel: 'N5', submittedBy: 'Staff Lan', submittedAt: '03/06/2026 09:00', status: 'pending_review' },
  { id: 2, contentType: 'question', title: "N5 Kanji: 'おはよう' có nghĩa là gì?", jlptLevel: 'N5', submittedBy: 'Staff Minh', submittedAt: '02/06/2026 15:30', status: 'pending_review' },
  { id: 3, contentType: 'vocabulary', title: '図書館 — Thư viện (N4)', jlptLevel: 'N4', submittedBy: 'Staff Lan', submittedAt: '02/06/2026 11:00', status: 'pending_review' },
  { id: 4, contentType: 'grammar', title: '～ことができる (N4)', jlptLevel: 'N4', submittedBy: 'Staff Bình', submittedAt: '01/06/2026 14:00', status: 'pending_review' },
  { id: 5, contentType: 'exam', title: 'Mock Test JLPT N4 Vol.2', jlptLevel: 'N4', submittedBy: 'Staff Minh', submittedAt: '01/06/2026 10:00', status: 'pending_review' },
  { id: 6, contentType: 'kanji', title: '電 (Điện) — N4', jlptLevel: 'N4', submittedBy: 'Staff Bình', submittedAt: '31/05/2026 16:30', status: 'pending_review' },
];

const TYPE_META = {
  lesson:     { label: 'Bài học',   bg: '#E8F5E9', text: '#2E7D32' },
  question:   { label: 'Câu hỏi',   bg: '#FCE4EC', text: '#C62828' },
  vocabulary: { label: 'Từ vựng',   bg: '#E3F2FD', text: '#1565C0' },
  grammar:    { label: 'Ngữ pháp',  bg: '#F3E5F5', text: '#6A1B9A' },
  kanji:      { label: 'Kanji',     bg: '#FCE4EC', text: '#C62828' },
  exam:       { label: 'Đề thi',    bg: '#FFF3E0', text: '#E65100' },
};

function TypeChip({ type }) {
  const m = TYPE_META[type] ?? { label: type, bg: '#EEE', text: '#666' };
  return (
    <span className="rqe-type-chip" style={{ background: m.bg, color: m.text }}>
      {m.label}
    </span>
  );
}

export default function StaffReviewQueue() {
  const [items, setItems] = useState(MOCK_REVIEW_ITEMS);
  const [typeFilter, setType] = useState('');
  const [levelFilter, setLevel] = useState('');
  const { toasts, addToast, removeToast } = useToast();

  const pending = items.filter((i) => i.status === 'pending_review');
  const filtered = pending
    .filter((i) => !typeFilter || i.contentType === typeFilter)
    .filter((i) => !levelFilter || i.jlptLevel === levelFilter);

  function handleApprove(item) {
    setItems((prev) => prev.map((i) => i.id === item.id ? { ...i, status: 'published' } : i));
    addToast('success', `Đã duyệt: ${item.title}`);
  }

  function handleReject(item) {
    setItems((prev) => prev.map((i) => i.id === item.id ? { ...i, status: 'rejected' } : i));
    addToast('error', `Đã từ chối: ${item.title}`);
  }

  return (
    <div className="rqe-page">
      <StaffTopNav activeTab="staff-review" />

      <main className="rqe-body">
        <StaffPageHero
          accent="indigo"
          title="Hàng Đợi Duyệt"
          subtitle="Xét duyệt và phê duyệt nội dung do staff soạn thảo trước khi xuất bản"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              {/* Con dấu Hanko (判子) */}
              <rect x="20" y="4" width="8" height="13" rx="3"/>
              <rect x="20" y="17" width="8" height="5"/>
              <rect x="14" y="22" width="20" height="18" rx="3"/>
              <line x1="18" y1="31" x2="30" y2="31"/>
              <line x1="24" y1="25" x2="24" y2="37"/>
              <circle cx="24" cy="31" r="7" strokeWidth="0.8" opacity="0.4"/>
            </svg>
          }
        />
        <div className="rqe-page-header">
          <div>
            <h1 className="rqe-page-title">Hàng Đợi Duyệt</h1>
            <p className="rqe-subtitle">Chỉ dành cho Staff Manager — duyệt nội dung do Staff gửi lên.</p>
          </div>
          {filtered.length > 0 && (
            <span className="rqe-pending-count">
              {filtered.length} mục chờ duyệt
            </span>
          )}
        </div>

        <div className="rqe-filter-bar">
          <label className="rqe-visually-hidden" htmlFor="rqe-type">Lọc theo loại</label>
          <select id="rqe-type" className="rqe-select" value={typeFilter} onChange={(e) => setType(e.target.value)}>
            <option value="">Tất cả loại</option>
            {Object.entries(TYPE_META).map(([k, v]) => (
              <option key={k} value={k}>{v.label}</option>
            ))}
          </select>

          <label className="rqe-visually-hidden" htmlFor="rqe-level">Lọc theo cấp độ</label>
          <select id="rqe-level" className="rqe-select" value={levelFilter} onChange={(e) => setLevel(e.target.value)}>
            <option value="">Tất cả cấp độ</option>
            {['N5','N4','N3','N2','N1'].map((l) => (
              <option key={l} value={l}>{l}</option>
            ))}
          </select>
        </div>

        {filtered.length === 0 ? (
          <EmptyState
            title="Không còn mục nào chờ duyệt"
            subtitle="Tất cả nội dung đã được xử lý. Làm tốt lắm!"
            mascotVariant="celebrate"
            mascotSize={120}
          />
        ) : (
          <div className="rqe-table-wrap">
            <table className="rqe-table">
              <caption className="rqe-visually-hidden">Hàng đợi duyệt nội dung</caption>
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
                {filtered.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <div className="rqe-content-cell">
                        <TypeChip type={item.contentType} />
                        <span className="rqe-content-title">{item.title}</span>
                      </div>
                    </td>
                    <td className="rqe-submitter">{item.submittedBy}</td>
                    <td><JlptBadge level={item.jlptLevel} /></td>
                    <td className="rqe-date">{item.submittedAt}</td>
                    <td>
                      <div className="rqe-actions">
                        <button
                          className="rqe-btn-approve"
                          onClick={() => handleApprove(item)}
                          aria-label={`Duyệt ${item.title}`}
                        >
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                          Duyệt
                        </button>
                        <button
                          className="rqe-btn-reject"
                          onClick={() => handleReject(item)}
                          aria-label={`Từ chối ${item.title}`}
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
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
