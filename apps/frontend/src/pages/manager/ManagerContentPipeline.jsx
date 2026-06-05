import { useState, useCallback, useEffect } from 'react';
import ManagerTopNav from '../../components/layout/ManagerTopNav';
import { JlptBadge } from '../../components/common/Badges';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './ManagerContentPipeline.css';

const MOCK_ITEMS = [
  { id:  1, type: 'lesson',     title: 'Bài 3 — Kanji N5 cơ bản',            level: 'N5', staff: 'Staff Lan',  createdAt: '03/06/2026', status: 'pending_review' },
  { id:  2, type: 'question',   title: "N5 Kanji: 'おはよう' có nghĩa gì?",  level: 'N5', staff: 'Staff Minh', createdAt: '02/06/2026', status: 'pending_review' },
  { id:  3, type: 'vocabulary', title: '図書館 — Thư viện (N4)',               level: 'N4', staff: 'Staff Lan',  createdAt: '02/06/2026', status: 'pending_review' },
  { id:  4, type: 'grammar',    title: '～ことができる (N4)',                  level: 'N4', staff: 'Staff Bình', createdAt: '01/06/2026', status: 'pending_review' },
  { id:  5, type: 'exam',       title: 'Mock Test JLPT N4 Vol.2',             level: 'N4', staff: 'Staff Minh', createdAt: '01/06/2026', status: 'pending_review' },
  { id:  6, type: 'lesson',     title: 'Bài 10 — Động từ nhóm 2 N4',         level: 'N4', staff: 'Staff Hà',   createdAt: '29/05/2026', status: 'published' },
  { id:  7, type: 'vocabulary', title: 'Nhóm từ giao thông N3',               level: 'N3', staff: 'Staff Lan',  createdAt: '28/05/2026', status: 'published' },
  { id:  8, type: 'grammar',    title: '～てから (N4)',                        level: 'N4', staff: 'Staff Bình', createdAt: '27/05/2026', status: 'published' },
  { id:  9, type: 'kanji',      title: '電 (Điện) — N4',                      level: 'N4', staff: 'Staff Bình', createdAt: '26/05/2026', status: 'published' },
  { id: 10, type: 'lesson',     title: 'Bài 1 — Chào hỏi N5',                level: 'N5', staff: 'Staff Minh', createdAt: '25/05/2026', status: 'published' },
  { id: 11, type: 'question',   title: 'N4 Vocab: 電話する có nghĩa gì?',    level: 'N4', staff: 'Staff Lan',  createdAt: '24/05/2026', status: 'rejected' },
  { id: 12, type: 'grammar',    title: '～ことができる (lỗi ví dụ thiếu)',    level: 'N4', staff: 'Staff Hà',   createdAt: '23/05/2026', status: 'rejected' },
  { id: 13, type: 'lesson',     title: 'Bài nháp — Grammar N3 vol.3',        level: 'N3', staff: 'Staff Tuấn', createdAt: '22/05/2026', status: 'draft' },
  { id: 14, type: 'vocabulary', title: 'Draft: Từ chủ đề kinh doanh N2',     level: 'N2', staff: 'Staff Bình', createdAt: '21/05/2026', status: 'draft' },
];

const STATUS_TABS = [
  { key: 'pending_review', label: 'Chờ duyệt',  colorClass: 'mcp-tab--amber' },
  { key: 'published',      label: 'Đã xuất bản',colorClass: 'mcp-tab--green' },
  { key: 'rejected',       label: 'Từ chối',    colorClass: 'mcp-tab--red'   },
  { key: 'draft',          label: 'Bản nháp',   colorClass: 'mcp-tab--gray'  },
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
    <span className="mcp-type-chip" style={{ background: m.bg, color: m.text }}>
      {m.label}
    </span>
  );
}

const PAGE_SIZE = 6;

export default function ManagerContentPipeline() {
  const [activeStatus, setActiveStatus] = useState('pending_review');
  const [typeFilter,   setType]         = useState('');
  const [levelFilter,  setLevel]        = useState('');
  const [staffFilter,  setStaff]        = useState('');
  const [currentPage,  setCurrentPage]  = useState(1);
  const [isLoading,    setLoading]      = useState(false);
  const [error,        setError]        = useState('');

  const staffList = [...new Set(MOCK_ITEMS.map((i) => i.staff))];

  useEffect(() => { setCurrentPage(1); }, [activeStatus, typeFilter, levelFilter, staffFilter]);

  const simulateFetch = useCallback(() => {
    setLoading(true);
    setError('');
    setTimeout(() => setLoading(false), 400);
  }, []);

  useEffect(() => { simulateFetch(); }, [simulateFetch, activeStatus]);

  const filtered = MOCK_ITEMS
    .filter((i) => i.status === activeStatus)
    .filter((i) => !typeFilter  || i.type  === typeFilter)
    .filter((i) => !levelFilter || i.level === levelFilter)
    .filter((i) => !staffFilter || i.staff === staffFilter);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const pageItems  = filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

  const counts = {};
  STATUS_TABS.forEach((t) => {
    counts[t.key] = MOCK_ITEMS.filter((i) => i.status === t.key).length;
  });

  return (
    <div className="mcp-page">
      <ManagerTopNav activeTab="manager-pipeline" />

      <main className="mcp-body">
        <StaffPageHero
          accent="pink"
          title="Pipeline Nội Dung"
          subtitle="Theo dõi toàn bộ nội dung theo trạng thái — từ bản nháp đến xuất bản"
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

        {/* Status tabs */}
        <div className="mcp-status-tabs" role="tablist">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.key}
              role="tab"
              aria-selected={activeStatus === tab.key}
              className={`mcp-status-tab ${tab.colorClass}${activeStatus === tab.key ? ' mcp-status-tab--active' : ''}`}
              onClick={() => setActiveStatus(tab.key)}
            >
              {tab.label}
              <span className="mcp-tab-count">{counts[tab.key]}</span>
            </button>
          ))}
        </div>

        {/* Filter bar */}
        <div className="mcp-filter-bar">
          <select className="mcp-select" value={staffFilter} onChange={(e) => setStaff(e.target.value)} aria-label="Lọc theo Staff">
            <option value="">Tất cả Staff</option>
            {staffList.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <select className="mcp-select" value={typeFilter} onChange={(e) => setType(e.target.value)} aria-label="Lọc theo loại">
            <option value="">Tất cả loại</option>
            {Object.entries(TYPE_META).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
          </select>
          <select className="mcp-select" value={levelFilter} onChange={(e) => setLevel(e.target.value)} aria-label="Lọc theo cấp độ">
            <option value="">Tất cả cấp độ</option>
            {['N5','N4','N3','N2','N1'].map((l) => <option key={l} value={l}>{l}</option>)}
          </select>
          <span className="mcp-result-count">{filtered.length} mục</span>
        </div>

        {/* Error */}
        {error && (
          <div className="mcp-error-banner" role="alert">
            <span>{error}</span>
            <button className="mcp-retry-btn" onClick={simulateFetch}>Thử lại</button>
          </div>
        )}

        {/* Table */}
        {isLoading ? (
          <div className="mcp-skel-wrap" aria-hidden="true">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="mcp-row-skel" />
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            title="Không có mục nào"
            subtitle="Không có nội dung nào trong trạng thái này."
            mascotVariant="thinking"
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
                    <th scope="col">Ngày tạo</th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.map((item, i) => (
                    <tr key={item.id} className="mcp-tr" style={{ '--row-i': i }}>
                      <td>
                        <div className="mcp-content-cell">
                          <TypeChip type={item.type} />
                          <span className="mcp-content-title">{item.title}</span>
                        </div>
                      </td>
                      <td className="mcp-td-staff">{item.staff}</td>
                      <td><JlptBadge level={item.level} /></td>
                      <td className="mcp-td-date">{item.createdAt}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {totalPages > 1 && (
              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onChange={setCurrentPage}
              />
            )}
          </>
        )}
      </main>
    </div>
  );
}
