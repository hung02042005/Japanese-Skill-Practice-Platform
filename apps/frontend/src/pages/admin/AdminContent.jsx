import { useState } from 'react';
import AdminTopNav from '../../components/layout/AdminTopNav';
import { AdminPageHeader } from '../../components/admin/AdminPageHeader';
import { EmptyState } from '../../components/common/EmptyState';
import { Pagination } from '../../components/common/Pagination';
import { JlptBadge } from '../../components/common/Badges';
import { ToastContainer, useToast } from '../../components/common/Toast';
import { IcAdminChip } from '../../components/admin/ManageUsersIcons';
import './AdminContent.css';

// Mock data — replace with API calls when backend content endpoints are ready

const MOCK_COURSES = [
  { id: 1, title: 'Tiếng Nhật N5 Cơ Bản',   jlptLevel: 'N5', status: 'published',     updatedAt: '01/06/2026', uploadedBy: 'Nguyễn Thị Mai' },
  { id: 2, title: 'Tiếng Nhật N4 Tiếp Nối',  jlptLevel: 'N4', status: 'pending_review', updatedAt: '02/06/2026', uploadedBy: 'Nguyễn Thị Mai' },
  { id: 3, title: 'Tiếng Nhật N3 Trung Cấp', jlptLevel: 'N3', status: 'draft',          updatedAt: '03/06/2026', uploadedBy: 'Phạm Minh Đức'  },
];

const MOCK_LESSONS = [
  { id: 1, title: 'Bài 1 — Giới thiệu bản thân',  jlptLevel: 'N5', lessonType: 'lesson',    status: 'published',     updatedAt: '01/06/2026', uploadedBy: 'Nguyễn Thị Mai' },
  { id: 2, title: 'Bài 2 — Số đếm và thời gian',  jlptLevel: 'N5', lessonType: 'lesson',    status: 'pending_review', updatedAt: '02/06/2026', uploadedBy: 'Nguyễn Thị Mai' },
  { id: 3, title: 'Bài 3 — Kanji N5 cơ bản',      jlptLevel: 'N5', lessonType: 'lesson',    status: 'draft',          updatedAt: '03/06/2026', uploadedBy: 'Phạm Minh Đức'  },
  { id: 4, title: 'Bài đọc hiểu N4 — Mùa xuân',   jlptLevel: 'N4', lessonType: 'reading',   status: 'draft',          updatedAt: '01/06/2026', uploadedBy: 'Phạm Minh Đức'  },
  { id: 5, title: 'Luyện nghe N3 — Tại sân bay',  jlptLevel: 'N3', lessonType: 'listening', status: 'rejected',       updatedAt: '30/05/2026', uploadedBy: 'Trần Văn Hùng'  },
  { id: 6, title: 'Luyện nói N4 — Hỏi đường',     jlptLevel: 'N4', lessonType: 'speaking',  status: 'published',     updatedAt: '28/05/2026', uploadedBy: 'Trần Văn Hùng'  },
];

const MOCK_VOCABULARY = [
  { id: 1, word: '学校', reading: 'がっこう', meaning: 'Trường học',       jlptLevel: 'N5', partOfSpeech: '名詞', status: 'published',     updatedAt: '01/06/2026', uploadedBy: 'Trần Văn Hùng' },
  { id: 2, word: '食べる', reading: 'たべる', meaning: 'Ăn',               jlptLevel: 'N5', partOfSpeech: '動詞', status: 'draft',          updatedAt: '02/06/2026', uploadedBy: 'Trần Văn Hùng' },
  { id: 3, word: '綺麗',  reading: 'きれい',  meaning: 'Đẹp / Sạch sẽ',   jlptLevel: 'N4', partOfSpeech: '形容詞', status: 'pending_review', updatedAt: '03/06/2026', uploadedBy: 'Lê Thị Hoa'   },
  { id: 4, word: '図書館', reading: 'としょかん', meaning: 'Thư viện',     jlptLevel: 'N4', partOfSpeech: '名詞', status: 'published',     updatedAt: '31/05/2026', uploadedBy: 'Lê Thị Hoa'   },
];

const MOCK_GRAMMAR = [
  { id: 1, pattern: '～てから',       meaning: 'Sau khi ~, rồi mới ~',            jlptLevel: 'N5', formation: 'V-て形 + から',             status: 'published',     updatedAt: '01/06/2026', uploadedBy: 'Trần Văn Hùng' },
  { id: 2, pattern: '～ても',         meaning: 'Dù ~ đi nữa',                     jlptLevel: 'N4', formation: 'V/Adj て形 + も',             status: 'draft',          updatedAt: '03/06/2026', uploadedBy: 'Phạm Minh Đức'  },
  { id: 3, pattern: '～ことができる',  meaning: 'Có thể làm ~',                    jlptLevel: 'N4', formation: 'V辞書形 + ことができる',      status: 'pending_review', updatedAt: '02/06/2026', uploadedBy: 'Trần Văn Hùng' },
  { id: 4, pattern: '～ようになる',    meaning: 'Trở nên có thể ~ / Đã ~ (thay đổi)', jlptLevel: 'N3', formation: 'V辞書形/Vない形 + ようになる', status: 'rejected', updatedAt: '30/05/2026', uploadedBy: 'Phạm Minh Đức' },
];

const MOCK_KANJI = [
  { id: 1, character: '水', onyomi: 'スイ',  kunyomi: 'みず',    meaning: 'Nước',          jlptLevel: 'N5', strokeCount: 4,  status: 'published',     updatedAt: '01/06/2026', uploadedBy: 'Lê Thị Hoa'   },
  { id: 2, character: '山', onyomi: 'サン',  kunyomi: 'やま',    meaning: 'Núi',           jlptLevel: 'N5', strokeCount: 3,  status: 'published',     updatedAt: '01/06/2026', uploadedBy: 'Lê Thị Hoa'   },
  { id: 3, character: '電', onyomi: 'デン',  kunyomi: 'いかずち', meaning: 'Điện',          jlptLevel: 'N4', strokeCount: 13, status: 'draft',          updatedAt: '03/06/2026', uploadedBy: 'Trần Văn Hùng' },
  { id: 4, character: '語', onyomi: 'ゴ',    kunyomi: 'かた.る',  meaning: 'Ngôn ngữ / Kể', jlptLevel: 'N4', strokeCount: 14, status: 'pending_review', updatedAt: '02/06/2026', uploadedBy: 'Lê Thị Hoa'   },
];

const DATA_MAP = { course: MOCK_COURSES, lesson: MOCK_LESSONS, vocabulary: MOCK_VOCABULARY, grammar: MOCK_GRAMMAR, kanji: MOCK_KANJI };

const CONTENT_TABS = [
  { id: 'course',     label: 'Khóa học' },
  { id: 'lesson',     label: 'Bài học' },
  { id: 'vocabulary', label: 'Từ vựng' },
  { id: 'grammar',    label: 'Ngữ pháp' },
  { id: 'kanji',      label: 'Kanji' },
];

const LESSON_TYPE_LABELS = {
  lesson: 'Bài học', reading: 'Đọc hiểu', listening: 'Luyện nghe', speaking: 'Luyện nói',
};

const STATUS_META = {
  draft:          { label: 'Nháp',         cls: 'adc-status--draft'     },
  pending_review: { label: 'Chờ duyệt',    cls: 'adc-status--pending'   },
  published:      { label: 'Đã xuất bản',  cls: 'adc-status--published' },
  rejected:       { label: 'Từ chối',      cls: 'adc-status--rejected'  },
  archived:       { label: 'Lưu trữ',      cls: 'adc-status--archived'  },
};

const PAGE_SIZE = 10;

function ContentStatusBadge({ status }) {
  const meta = STATUS_META[status] ?? { label: status, cls: '' };
  return <span className={`adc-status ${meta.cls}`}>{meta.label}</span>;
}

function getItemLabel(item) {
  return item.title || item.word || item.pattern || item.character || '';
}

export default function AdminContent() {
  const [activeTab, setActiveTab] = useState('lesson');
  const [items, setItems]         = useState([...DATA_MAP.lesson]);
  const [search, setSearch]       = useState('');
  const [levelFilter, setLevel]   = useState('');
  const [statusFilter, setStatus] = useState('');
  const [currentPage, setPage]    = useState(1);
  const { toasts, addToast, removeToast } = useToast();

  function changeTab(id) {
    setActiveTab(id);
    setItems([...DATA_MAP[id]]);
    setSearch('');
    setLevel('');
    setStatus('');
    setPage(1);
  }

  const filtered = items.filter((item) => {
    const label = getItemLabel(item).toLowerCase();
    if (search && !label.includes(search.toLowerCase())) return false;
    if (levelFilter  && item.jlptLevel !== levelFilter)  return false;
    if (statusFilter && item.status    !== statusFilter) return false;
    return true;
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage   = Math.min(currentPage, totalPages);
  const pageItems  = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  function doArchive(item) {
    setItems((prev) => prev.map((it) => it.id === item.id ? { ...it, status: 'archived' } : it));
    addToast('info', 'Đã lưu trữ nội dung.');
  }
  function doDelete(item) {
    setItems((prev) => prev.filter((it) => it.id !== item.id));
    addToast('success', 'Đã xóa nội dung.');
  }

  function renderActions(item) {
    return (
      <div className="adc-acts">
        {item.status === 'pending_review' && (
          <span className="adc-act-note" title="Manager phụ trách duyệt nội dung này">Chờ Manager duyệt</span>
        )}
        {item.status === 'published' && (
          <button type="button" className="adc-act adc-act--archive" onClick={() => doArchive(item)} title="Lưu trữ">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><line x1="10" y1="12" x2="14" y2="12"/>
            </svg>
          </button>
        )}
        {(item.status === 'draft' || item.status === 'rejected' || item.status === 'archived') && (
          <button type="button" className="adc-act adc-act--delete" onClick={() => doDelete(item)} title="Xóa nội dung">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/>
            </svg>
          </button>
        )}
      </div>
    );
  }

  function renderTable() {
    if (pageItems.length === 0) {
      return (
        <div className="adc-empty">
          <EmptyState
            title="Không có nội dung nào"
            subtitle="Thử thay đổi bộ lọc hoặc cấp độ JLPT"
            mascotVariant="empty"
            mascotSize={80}
          />
        </div>
      );
    }

    const sharedCols = (item, i) => (
      <>
        <td className="adc-td-idx">{(safePage - 1) * PAGE_SIZE + i + 1}</td>
      </>
    );

    switch (activeTab) {
      case 'course':
        return (
          <table className="adc-table">
            <thead><tr><th>#</th><th>Tên khóa học</th><th>Cấp độ</th><th>Người tạo</th><th>Cập nhật</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
              {pageItems.map((item, i) => (
                <tr key={item.id}>
                  {sharedCols(item, i)}
                  <td className="adc-td-main">{item.title}</td>
                  <td><JlptBadge level={item.jlptLevel} /></td>
                  <td className="adc-td-sub">{item.uploadedBy}</td>
                  <td className="adc-td-sub">{item.updatedAt}</td>
                  <td><ContentStatusBadge status={item.status} /></td>
                  <td onClick={(e) => e.stopPropagation()}>{renderActions(item)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );

      case 'lesson':
        return (
          <table className="adc-table">
            <thead><tr><th>#</th><th>Tiêu đề</th><th>Cấp độ</th><th>Loại</th><th>Người tạo</th><th>Cập nhật</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
              {pageItems.map((item, i) => (
                <tr key={item.id}>
                  {sharedCols(item, i)}
                  <td className="adc-td-main">{item.title}</td>
                  <td><JlptBadge level={item.jlptLevel} /></td>
                  <td className="adc-td-sub">{LESSON_TYPE_LABELS[item.lessonType] || item.lessonType}</td>
                  <td className="adc-td-sub">{item.uploadedBy}</td>
                  <td className="adc-td-sub">{item.updatedAt}</td>
                  <td><ContentStatusBadge status={item.status} /></td>
                  <td onClick={(e) => e.stopPropagation()}>{renderActions(item)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );

      case 'vocabulary':
        return (
          <table className="adc-table">
            <thead><tr><th>#</th><th>Từ vựng</th><th>Cách đọc</th><th>Cấp độ</th><th>Người tạo</th><th>Cập nhật</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
              {pageItems.map((item, i) => (
                <tr key={item.id}>
                  {sharedCols(item, i)}
                  <td className="adc-td-jp">{item.word}</td>
                  <td className="adc-td-sub">{item.reading}</td>
                  <td><JlptBadge level={item.jlptLevel} /></td>
                  <td className="adc-td-sub">{item.uploadedBy}</td>
                  <td className="adc-td-sub">{item.updatedAt}</td>
                  <td><ContentStatusBadge status={item.status} /></td>
                  <td onClick={(e) => e.stopPropagation()}>{renderActions(item)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );

      case 'grammar':
        return (
          <table className="adc-table">
            <thead><tr><th>#</th><th>Cấu trúc</th><th>Ý nghĩa</th><th>Cấp độ</th><th>Người tạo</th><th>Cập nhật</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
              {pageItems.map((item, i) => (
                <tr key={item.id}>
                  {sharedCols(item, i)}
                  <td className="adc-td-mono">{item.pattern}</td>
                  <td className="adc-td-sub adc-td-meaning">{item.meaning}</td>
                  <td><JlptBadge level={item.jlptLevel} /></td>
                  <td className="adc-td-sub">{item.uploadedBy}</td>
                  <td className="adc-td-sub">{item.updatedAt}</td>
                  <td><ContentStatusBadge status={item.status} /></td>
                  <td onClick={(e) => e.stopPropagation()}>{renderActions(item)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );

      case 'kanji':
        return (
          <table className="adc-table">
            <thead><tr><th>#</th><th>Kanji</th><th>Âm on</th><th>Âm kun</th><th>Cấp độ</th><th>Người tạo</th><th>Cập nhật</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
              {pageItems.map((item, i) => (
                <tr key={item.id}>
                  {sharedCols(item, i)}
                  <td><span className="adc-kanji-char">{item.character}</span></td>
                  <td className="adc-td-sub">{item.onyomi}</td>
                  <td className="adc-td-sub">{item.kunyomi}</td>
                  <td><JlptBadge level={item.jlptLevel} /></td>
                  <td className="adc-td-sub">{item.uploadedBy}</td>
                  <td className="adc-td-sub">{item.updatedAt}</td>
                  <td><ContentStatusBadge status={item.status} /></td>
                  <td onClick={(e) => e.stopPropagation()}>{renderActions(item)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );

      default:
        return null;
    }
  }

  return (
    <div className="adc-page">
      <AdminTopNav activeTab="manage-content" />

      <AdminPageHeader
        chipIcon={<IcAdminChip />}
        chipLabel="Nội dung"
        title="Quản Lý Nội Dung"
        subtitle="Xem, duyệt và quản lý toàn bộ học liệu từ nhân viên"
        mascotVariant="idle"
        mascotSize={100}
      />

      <main className="adc-body">
        <div className="adc-tabs" role="tablist" aria-label="Loại nội dung">
          {CONTENT_TABS.map((tab) => (
            <button
              key={tab.id}
              type="button"
              role="tab"
              className={`adc-tab${activeTab === tab.id ? ' adc-tab--active' : ''}`}
              aria-selected={activeTab === tab.id}
              onClick={() => changeTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="adc-filter-bar">
          <div className="adc-search-wrap">
            <input
              type="search"
              className="adc-search"
              placeholder="Tìm kiếm nội dung..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(1); }}
              aria-label="Tìm kiếm nội dung"
            />
          </div>
          <select className="adc-select" value={levelFilter} onChange={(e) => { setLevel(e.target.value); setPage(1); }} aria-label="Lọc cấp độ">
            <option value="">Tất cả cấp độ</option>
            {['N5','N4','N3','N2','N1'].map((l) => <option key={l} value={l}>{l}</option>)}
          </select>
          <select className="adc-select" value={statusFilter} onChange={(e) => { setStatus(e.target.value); setPage(1); }} aria-label="Lọc trạng thái">
            <option value="">Tất cả trạng thái</option>
            <option value="draft">Nháp</option>
            <option value="pending_review">Chờ duyệt</option>
            <option value="published">Đã xuất bản</option>
            <option value="rejected">Từ chối</option>
            <option value="archived">Lưu trữ</option>
          </select>
          <span className="adc-count">{filtered.length} mục</span>
        </div>

        <div className="adc-table-wrap" role="tabpanel">
          {renderTable()}
        </div>

        {filtered.length > PAGE_SIZE && (
          <Pagination currentPage={safePage} totalPages={totalPages} onChange={setPage} />
        )}
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
