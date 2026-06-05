import { useState, useRef, useEffect, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import { JlptBadge } from '../../components/common/Badges';
import { useToast, ToastContainer } from '../../components/common/Toast';
import ContentStatusActions from '../../components/staff/ContentStatusActions';
import ContentFormModal from '../../components/staff/ContentFormModal';
import StaffPageHero from '../../components/staff/StaffPageHero';
import './StaffContent.css';

// ─── Mock Data ──────────────────────────────────────────────────────────────

const MOCK_COURSES = [
  { id: 1, title: 'Tiếng Nhật N5 Cơ Bản', jlptLevel: 'N5', status: 'published', updatedAt: '01/06/2026', description: 'Khóa học cơ bản cho người mới bắt đầu' },
  { id: 2, title: 'Tiếng Nhật N4 Tiếp Nối', jlptLevel: 'N4', status: 'pending_review', updatedAt: '02/06/2026', description: 'Nâng cao từ N5 lên N4' },
  { id: 3, title: 'Tiếng Nhật N3 Trung Cấp', jlptLevel: 'N3', status: 'draft', updatedAt: '03/06/2026', description: 'Khoá học trình độ trung cấp' },
];

const MOCK_LESSONS = [
  { id: 1, title: 'Bài 1 — Giới thiệu bản thân', jlptLevel: 'N5', lessonType: 'lesson', status: 'published', updatedAt: '01/06/2026' },
  { id: 2, title: 'Bài 2 — Số đếm và thời gian', jlptLevel: 'N5', lessonType: 'lesson', status: 'pending_review', updatedAt: '02/06/2026' },
  { id: 3, title: 'Bài 3 — Kanji N5 cơ bản', jlptLevel: 'N5', lessonType: 'lesson', status: 'draft', updatedAt: '03/06/2026' },
  { id: 4, title: 'Bài đọc hiểu N4 — Mùa xuân', jlptLevel: 'N4', lessonType: 'reading', status: 'draft', updatedAt: '01/06/2026' },
  { id: 5, title: 'Luyện nghe N3 — Tại sân bay', jlptLevel: 'N3', lessonType: 'listening', status: 'rejected', updatedAt: '30/05/2026' },
  { id: 6, title: 'Luyện nói N4 — Hỏi đường', jlptLevel: 'N4', lessonType: 'speaking', status: 'published', updatedAt: '28/05/2026' },
];

const MOCK_VOCABULARY = [
  { id: 1, word: '学校', reading: 'がっこう', meaning: 'Trường học', jlptLevel: 'N5', partOfSpeech: '名詞', status: 'published', updatedAt: '01/06/2026' },
  { id: 2, word: '食べる', reading: 'たべる', meaning: 'Ăn', jlptLevel: 'N5', partOfSpeech: '動詞', status: 'draft', updatedAt: '02/06/2026' },
  { id: 3, word: '綺麗', reading: 'きれい', meaning: 'Đẹp / Sạch sẽ', jlptLevel: 'N4', partOfSpeech: '形容詞', status: 'pending_review', updatedAt: '03/06/2026' },
  { id: 4, word: '図書館', reading: 'としょかん', meaning: 'Thư viện', jlptLevel: 'N4', partOfSpeech: '名詞', status: 'published', updatedAt: '31/05/2026' },
];

const MOCK_GRAMMAR = [
  { id: 1, pattern: '～てから', meaning: 'Sau khi ~, rồi mới ~', jlptLevel: 'N5', formation: 'V-て形 + から', status: 'published', updatedAt: '01/06/2026' },
  { id: 2, pattern: '～ても', meaning: 'Dù ~ đi nữa', jlptLevel: 'N4', formation: 'V/Adj て形 + も', status: 'draft', updatedAt: '03/06/2026' },
  { id: 3, pattern: '～ことができる', meaning: 'Có thể làm ~', jlptLevel: 'N4', formation: 'V辞書形 + ことができる', status: 'pending_review', updatedAt: '02/06/2026' },
  { id: 4, pattern: '～ようになる', meaning: 'Trở nên có thể ~ / Đã ~ (thay đổi)', jlptLevel: 'N3', formation: 'V辞書形/Vない形 + ようになる', status: 'rejected', updatedAt: '30/05/2026' },
];

const MOCK_KANJI = [
  { id: 1, character: '水', onyomi: 'スイ', kunyomi: 'みず', meaning: 'Nước', jlptLevel: 'N5', strokeCount: 4, status: 'published', updatedAt: '01/06/2026' },
  { id: 2, character: '山', onyomi: 'サン', kunyomi: 'やま', meaning: 'Núi', jlptLevel: 'N5', strokeCount: 3, status: 'published', updatedAt: '01/06/2026' },
  { id: 3, character: '電', onyomi: 'デン', kunyomi: 'いかずち', meaning: 'Điện', jlptLevel: 'N4', strokeCount: 13, status: 'draft', updatedAt: '03/06/2026' },
  { id: 4, character: '語', onyomi: 'ゴ', kunyomi: 'かた.る', meaning: 'Ngôn ngữ / Kể', jlptLevel: 'N4', strokeCount: 14, status: 'pending_review', updatedAt: '02/06/2026' },
];

const DATA_MAP = {
  course: MOCK_COURSES,
  lesson: MOCK_LESSONS,
  vocabulary: MOCK_VOCABULARY,
  grammar: MOCK_GRAMMAR,
  kanji: MOCK_KANJI,
};

const CONTENT_TABS = [
  { id: 'course', label: 'Khóa học' },
  { id: 'lesson', label: 'Bài học' },
  { id: 'vocabulary', label: 'Từ vựng' },
  { id: 'grammar', label: 'Ngữ pháp' },
  { id: 'kanji', label: 'Kanji' },
];

const LESSON_TYPE_LABELS = {
  lesson: 'Bài học',
  reading: 'Đọc hiểu',
  listening: 'Luyện nghe',
  speaking: 'Luyện nói',
};

const STATUS_META = {
  draft:          { label: 'Nháp',          cls: 'sfc-status--draft' },
  pending_review: { label: 'Chờ duyệt',     cls: 'sfc-status--pending_review' },
  published:      { label: 'Đã xuất bản',   cls: 'sfc-status--published' },
  rejected:       { label: 'Từ chối',       cls: 'sfc-status--rejected' },
  archived:       { label: 'Lưu trữ',       cls: 'sfc-status--archived' },
};

const PAGE_SIZE = 10;

// ─── Sub-components ──────────────────────────────────────────────────────────

function StatusBadge({ status }) {
  const meta = STATUS_META[status] || { label: status, cls: 'sfc-status--draft' };
  return <span className={`sfc-status ${meta.cls}`}>{meta.label}</span>;
}

// ─── Main Component ──────────────────────────────────────────────────────────

export default function StaffContent() {
  const navigate = useNavigate();
  const location = useLocation();

  const [activeContentTab, setActiveContentTab] = useState('lesson');
  const [items, setItems] = useState([...DATA_MAP['lesson']]);
  const [search, setSearch] = useState('');
  const [levelFilter, setLevelFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [showModal, setShowModal] = useState(false);
  const [editItem, setEditItem] = useState(null);
  const [createType, setCreateType] = useState('lesson');
  const [showDropdown, setShowDropdown] = useState(false);

  const { toasts, addToast, removeToast } = useToast();
  const dropdownRef = useRef(null);

  // Close dropdown on outside click
  useEffect(() => {
    if (!showDropdown) return;
    const handleMouseDown = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setShowDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleMouseDown);
    return () => document.removeEventListener('mousedown', handleMouseDown);
  }, [showDropdown]);

  // Switch content tab
  const handleTabChange = (tabId) => {
    setActiveContentTab(tabId);
    setItems([...DATA_MAP[tabId]]);
    setSearch('');
    setLevelFilter('');
    setStatusFilter('');
    setCurrentPage(1);
  };

  // Filtering logic
  const filteredItems = items.filter((item) => {
    const searchable = (
      item.title || item.word || item.pattern || item.character || ''
    ).toLowerCase();
    const matchSearch = !search || searchable.includes(search.toLowerCase());
    const matchLevel = !levelFilter || item.jlptLevel === levelFilter;
    const matchStatus = !statusFilter || item.status === statusFilter;
    return matchSearch && matchLevel && matchStatus;
  });

  const totalPages = Math.max(1, Math.ceil(filteredItems.length / PAGE_SIZE));
  const safePage = Math.min(currentPage, totalPages);
  const pageItems = filteredItems.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const handleSearchChange = (e) => {
    setSearch(e.target.value);
    setCurrentPage(1);
  };

  const handleLevelChange = (e) => {
    setLevelFilter(e.target.value);
    setCurrentPage(1);
  };

  const handleStatusChange = (e) => {
    setStatusFilter(e.target.value);
    setCurrentPage(1);
  };

  // Actions
  const handleEdit = useCallback((item) => {
    setEditItem(item);
    setCreateType(item.contentType || activeContentTab);
    setShowModal(true);
  }, [activeContentTab]);

  const handleSubmit = useCallback((item) => {
    setItems((prev) =>
      prev.map((it) => it.id === item.id ? { ...it, status: 'pending_review' } : it)
    );
    addToast({ type: 'success', message: 'Đã gửi duyệt thành công!' });
  }, [addToast]);

  const handleView = useCallback((item) => {
    addToast({ type: 'info', message: `Xem: ${item.title || item.word || item.pattern || item.character}` });
  }, [addToast]);

  const handleSave = useCallback((savedItem) => {
    if (editItem) {
      setItems((prev) => prev.map((it) => it.id === savedItem.id ? savedItem : it));
      addToast({ type: 'success', message: 'Đã cập nhật nội dung thành công!' });
    } else {
      setItems((prev) => [savedItem, ...prev]);
      addToast({ type: 'success', message: 'Đã tạo nội dung thành công!' });
    }
    setShowModal(false);
    setEditItem(null);
  }, [editItem, addToast]);

  const handleOpenCreate = (type) => {
    setCreateType(type);
    setEditItem(null);
    setShowModal(true);
    setShowDropdown(false);
  };

  const handleModalClose = () => {
    setShowModal(false);
    setEditItem(null);
  };

  // ─── Table renderers ────────────────────────────────────────────────────────

  const renderCourseTable = () => (
    <table className="sfc-table" aria-label="Danh sách khóa học">
      <thead>
        <tr>
          <th>Tên khóa học</th>
          <th>Cấp độ</th>
          <th>Trạng thái</th>
          <th>Cập nhật</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        {pageItems.map((item) => (
          <tr key={item.id}>
            <td>{item.title}</td>
            <td><JlptBadge level={item.jlptLevel} /></td>
            <td><StatusBadge status={item.status} /></td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>{item.updatedAt}</td>
            <td>
              <ContentStatusActions
                item={item}
                onEdit={handleEdit}
                onSubmit={handleSubmit}
                onView={handleView}
              />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );

  const renderLessonTable = () => (
    <table className="sfc-table" aria-label="Danh sách bài học">
      <thead>
        <tr>
          <th>Tiêu đề</th>
          <th>Cấp độ</th>
          <th>Loại</th>
          <th>Trạng thái</th>
          <th>Cập nhật</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        {pageItems.map((item) => (
          <tr key={item.id}>
            <td>{item.title}</td>
            <td><JlptBadge level={item.jlptLevel} /></td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>
              {LESSON_TYPE_LABELS[item.lessonType] || item.lessonType}
            </td>
            <td><StatusBadge status={item.status} /></td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>{item.updatedAt}</td>
            <td>
              <ContentStatusActions
                item={item}
                onEdit={handleEdit}
                onSubmit={handleSubmit}
                onView={handleView}
              />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );

  const renderVocabTable = () => (
    <table className="sfc-table" aria-label="Danh sách từ vựng">
      <thead>
        <tr>
          <th>Từ vựng</th>
          <th>Cách đọc</th>
          <th>Cấp độ</th>
          <th>Trạng thái</th>
          <th>Cập nhật</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        {pageItems.map((item) => (
          <tr key={item.id}>
            <td style={{ fontWeight: 600, fontSize: 16 }}>{item.word}</td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>{item.reading}</td>
            <td><JlptBadge level={item.jlptLevel} /></td>
            <td><StatusBadge status={item.status} /></td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>{item.updatedAt}</td>
            <td>
              <ContentStatusActions
                item={item}
                onEdit={handleEdit}
                onSubmit={handleSubmit}
                onView={handleView}
              />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );

  const renderGrammarTable = () => (
    <table className="sfc-table" aria-label="Danh sách ngữ pháp">
      <thead>
        <tr>
          <th>Cấu trúc</th>
          <th>Ý nghĩa</th>
          <th>Cấp độ</th>
          <th>Trạng thái</th>
          <th>Cập nhật</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        {pageItems.map((item) => (
          <tr key={item.id}>
            <td style={{ fontWeight: 600, fontFamily: 'monospace', fontSize: 15 }}>{item.pattern}</td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13, maxWidth: 220 }}>{item.meaning}</td>
            <td><JlptBadge level={item.jlptLevel} /></td>
            <td><StatusBadge status={item.status} /></td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>{item.updatedAt}</td>
            <td>
              <ContentStatusActions
                item={item}
                onEdit={handleEdit}
                onSubmit={handleSubmit}
                onView={handleView}
              />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );

  const renderKanjiTable = () => (
    <table className="sfc-table" aria-label="Danh sách Kanji">
      <thead>
        <tr>
          <th>Kanji</th>
          <th>Âm on</th>
          <th>Âm kun</th>
          <th>Cấp độ</th>
          <th>Trạng thái</th>
          <th>Cập nhật</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        {pageItems.map((item) => (
          <tr key={item.id}>
            <td><span className="sfc-kanji-char">{item.character}</span></td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>{item.onyomi}</td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>{item.kunyomi}</td>
            <td><JlptBadge level={item.jlptLevel} /></td>
            <td><StatusBadge status={item.status} /></td>
            <td style={{ color: 'var(--color-text-sub)', fontSize: 13 }}>{item.updatedAt}</td>
            <td>
              <ContentStatusActions
                item={item}
                onEdit={handleEdit}
                onSubmit={handleSubmit}
                onView={handleView}
              />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );

  const renderTable = () => {
    if (pageItems.length === 0) {
      return (
        <div className="sfc-empty-wrap">
          <EmptyState
            title="Không có nội dung nào"
            subtitle="Hãy tạo nội dung mới hoặc thử thay đổi bộ lọc"
            mascotVariant="empty"
            mascotSize={80}
          />
        </div>
      );
    }
    switch (activeContentTab) {
      case 'course':     return renderCourseTable();
      case 'lesson':     return renderLessonTable();
      case 'vocabulary': return renderVocabTable();
      case 'grammar':    return renderGrammarTable();
      case 'kanji':      return renderKanjiTable();
      default:           return null;
    }
  };

  // ─── Render ─────────────────────────────────────────────────────────────────

  return (
    <div className="sfc-page">
      <StaffTopNav activeTab="staff-content" />

      <main className="sfc-body">
        <h1 className="sfc-visually-hidden">Quản Lý Học Liệu</h1>

        <StaffPageHero
          accent="gold"
          title="Quản Lý Học Liệu"
          subtitle="Soạn thảo khóa học, bài học, từ vựng, ngữ pháp và Kanji theo từng cấp độ JLPT"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              {/* Cuộn giấy Nhật (巻物) */}
              <rect x="10" y="14" width="28" height="20" rx="1.5"/>
              <ellipse cx="10" cy="24" rx="3.5" ry="10"/>
              <ellipse cx="38" cy="24" rx="3.5" ry="10"/>
              <line x1="16" y1="20" x2="32" y2="20"/>
              <line x1="16" y1="25" x2="28" y2="25"/>
              <line x1="16" y1="30" x2="24" y2="30"/>
            </svg>
          }
        />

        {/* Page header */}
        <div className="sfc-page-header">
          <p className="sfc-page-title">Quản Lý Học Liệu</p>
          <div className="sfc-create-wrap" ref={dropdownRef}>
            <button
              className="sfc-btn-create"
              onClick={() => setShowDropdown((v) => !v)}
              aria-haspopup="true"
              aria-expanded={showDropdown}
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
              </svg>
              Tạo mới
              <svg
                width="12"
                height="12"
                viewBox="0 0 24 24"
                fill="none"
                aria-hidden="true"
                style={{ transition: 'transform var(--transition)', transform: showDropdown ? 'rotate(180deg)' : 'rotate(0deg)' }}
              >
                <path d="M6 9l6 6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            {showDropdown && (
              <div className="sfc-dropdown" role="menu">
                {CONTENT_TABS.map((tab) => (
                  <button
                    key={tab.id}
                    className="sfc-dropdown-item"
                    role="menuitem"
                    onClick={() => handleOpenCreate(tab.id)}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Content type tabs */}
        <div className="sfc-content-tabs" role="tablist" aria-label="Loại học liệu">
          {CONTENT_TABS.map((tab) => (
            <button
              key={tab.id}
              className={`sfc-content-tab${activeContentTab === tab.id ? ' sfc-content-tab--active' : ''}`}
              role="tab"
              aria-selected={activeContentTab === tab.id}
              onClick={() => handleTabChange(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Filter bar */}
        <div className="sfc-filter-bar">
          <input
            className="sfc-search"
            type="search"
            placeholder="Tìm kiếm..."
            value={search}
            onChange={handleSearchChange}
            aria-label="Tìm kiếm nội dung"
          />
          <select
            className="sfc-select"
            value={levelFilter}
            onChange={handleLevelChange}
            aria-label="Lọc theo cấp độ"
          >
            <option value="">Tất cả cấp độ</option>
            <option value="N5">N5</option>
            <option value="N4">N4</option>
            <option value="N3">N3</option>
            <option value="N2">N2</option>
            <option value="N1">N1</option>
          </select>
          <select
            className="sfc-select"
            value={statusFilter}
            onChange={handleStatusChange}
            aria-label="Lọc theo trạng thái"
          >
            <option value="">Tất cả trạng thái</option>
            <option value="draft">Nháp</option>
            <option value="pending_review">Chờ duyệt</option>
            <option value="published">Đã xuất bản</option>
            <option value="rejected">Từ chối</option>
            <option value="archived">Lưu trữ</option>
          </select>
        </div>

        {/* Table */}
        <div className="sfc-table-wrap" role="tabpanel">
          {renderTable()}
        </div>

        {/* Pagination */}
        {filteredItems.length > PAGE_SIZE && (
          <Pagination
            currentPage={safePage}
            totalPages={totalPages}
            onChange={(page) => setCurrentPage(page)}
          />
        )}
      </main>

      {/* Modal */}
      <ContentFormModal
        isOpen={showModal}
        contentType={createType}
        editItem={editItem}
        onClose={handleModalClose}
        onSave={handleSave}
      />

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
