import { useState } from 'react';
import ManagerTopNav from '../../components/layout/ManagerTopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { useToast, ToastContainer } from '../../components/common/Toast';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './ManagerReviewQueue.css';

const MOCK_REVIEW_ITEMS = [
  { id: 1, contentType: 'lesson',     title: 'Bài 3 — Kanji N5 cơ bản',               jlptLevel: 'N5', submittedBy: 'Staff Lan',  submittedAt: '03/06/2026 09:00', status: 'pending_review' },
  { id: 2, contentType: 'question',   title: "N5 Kanji: 'おはよう' có nghĩa là gì?", jlptLevel: 'N5', submittedBy: 'Staff Minh', submittedAt: '02/06/2026 15:30', status: 'pending_review' },
  { id: 3, contentType: 'vocabulary', title: '図書館 — Thư viện (N4)',                  jlptLevel: 'N4', submittedBy: 'Staff Lan',  submittedAt: '02/06/2026 11:00', status: 'pending_review' },
  { id: 4, contentType: 'grammar',    title: '～ことができる (N4)',                     jlptLevel: 'N4', submittedBy: 'Staff Bình', submittedAt: '01/06/2026 14:00', status: 'pending_review' },
  { id: 5, contentType: 'exam',       title: 'Mock Test JLPT N4 Vol.2',                jlptLevel: 'N4', submittedBy: 'Staff Minh', submittedAt: '01/06/2026 10:00', status: 'pending_review' },
  { id: 6, contentType: 'kanji',      title: '電 (Điện) — N4',                         jlptLevel: 'N4', submittedBy: 'Staff Bình', submittedAt: '31/05/2026 16:30', status: 'pending_review' },
  { id: 7, contentType: 'lesson',     title: 'Bài 12 — Ngữ pháp N3 nâng cao',          jlptLevel: 'N3', submittedBy: 'Staff Lan',  submittedAt: '31/05/2026 09:00', status: 'pending_review' },
  { id: 8, contentType: 'vocabulary', title: '経済学 — Kinh tế học (N2)',               jlptLevel: 'N2', submittedBy: 'Staff Hà',   submittedAt: '30/05/2026 14:00', status: 'pending_review' },
];

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
    <span className="mrq-type-chip" style={{ background: m.bg, color: m.text }}>
      {m.label}
    </span>
  );
}

export default function ManagerReviewQueue() {
  const [items, setItems] = useState(MOCK_REVIEW_ITEMS);
  const [typeFilter, setType] = useState('');
  const [levelFilter, setLevel] = useState('');
  const [staffFilter, setStaff] = useState('');
  const { toasts, addToast, removeToast } = useToast();

  const staffList = [...new Set(MOCK_REVIEW_ITEMS.map((i) => i.submittedBy))];

  const pending = items.filter((i) => i.status === 'pending_review');
  const filtered = pending
    .filter((i) => !typeFilter || i.contentType === typeFilter)
    .filter((i) => !levelFilter || i.jlptLevel === levelFilter)
    .filter((i) => !staffFilter || i.submittedBy === staffFilter);

  function handleApprove(item) {
    setItems((prev) => prev.map((i) => i.id === item.id ? { ...i, status: 'published' } : i));
    addToast('success', `Đã duyệt: ${item.title}`);
  }

  function handleReject(item) {
    setItems((prev) => prev.map((i) => i.id === item.id ? { ...i, status: 'rejected' } : i));
    addToast('error', `Đã từ chối: ${item.title}`);
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
          {filtered.length > 0 && (
            <span className="mrq-pending-count">{filtered.length} mục chờ duyệt</span>
          )}
        </div>

        <div className="mrq-filter-bar">
          <select className="mrq-select" value={staffFilter} onChange={(e) => setStaff(e.target.value)} aria-label="Lọc theo staff">
            <option value="">Tất cả Staff</option>
            {staffList.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>

          <select className="mrq-select" value={typeFilter} onChange={(e) => setType(e.target.value)} aria-label="Lọc theo loại">
            <option value="">Tất cả loại</option>
            {Object.entries(TYPE_META).map(([k, v]) => (
              <option key={k} value={k}>{v.label}</option>
            ))}
          </select>

          <select className="mrq-select" value={levelFilter} onChange={(e) => setLevel(e.target.value)} aria-label="Lọc theo cấp độ">
            <option value="">Tất cả cấp độ</option>
            {['N5','N4','N3','N2','N1'].map((l) => <option key={l} value={l}>{l}</option>)}
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
                {filtered.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <div className="mrq-content-cell">
                        <TypeChip type={item.contentType} />
                        <span className="mrq-content-title">{item.title}</span>
                      </div>
                    </td>
                    <td className="mrq-submitter">{item.submittedBy}</td>
                    <td><JlptBadge level={item.jlptLevel} /></td>
                    <td className="mrq-date">{item.submittedAt}</td>
                    <td>
                      <div className="mrq-actions">
                        <button
                          className="mrq-btn-approve"
                          onClick={() => handleApprove(item)}
                          aria-label={`Duyệt ${item.title}`}
                        >
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M20 6L9 17l-5-5" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                          Duyệt
                        </button>
                        <button
                          className="mrq-btn-reject"
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
