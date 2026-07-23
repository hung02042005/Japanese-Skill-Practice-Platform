import { useState, useEffect, useCallback } from 'react';
import ManagerTopNav from '@/shared/components/layout/ManagerTopNav';
import StaffPageHero from '@/features/management/components/staff/StaffPageHero';
import { EmptyState } from '@/shared/components/common/EmptyState';
import { useToast } from '@/shared/context/ToastContext';
import { getDeletedContents, restoreDeletedContent } from '@/shared/api/managerService';
import './ManagerDeletedTopics.css';

const CATEGORIES = [
  { id: 'all', label: 'Tất cả loại' },
  { id: 'lesson', label: 'Bài học' },
  { id: 'question', label: 'Câu hỏi' },
  { id: 'vocabulary', label: 'Từ vựng' },
  { id: 'grammar', label: 'Ngữ pháp' },
  { id: 'kanji', label: 'Kanji' },
  { id: 'assessment', label: 'Bài kiểm tra' }
];

const CATEGORY_LABELS = {
  lesson: 'Bài học',
  question: 'Câu hỏi',
  vocabulary: 'Từ vựng',
  grammar: 'Ngữ pháp',
  kanji: 'Kanji',
  assessment: 'Bài kiểm tra'
};

function IconRestore({ size = 18 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
      <polyline points="3 3 3 8 8 8"></polyline>
    </svg>
  );
}

export default function ManagerDeletedTopics() {
  const { success, error } = useToast();

  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedLevel, setSelectedLevel] = useState('all');
  const [loading, setLoading] = useState(false);
  const [deletedItems, setDeletedItems] = useState([]);
  const [actionPendingId, setActionPendingId] = useState(null);

  // ─── Fetch deleted contents ─────────────────────────────────────────
  const fetchDeleted = useCallback(async (cat) => {
    setLoading(true);
    try {
      const data = await getDeletedContents(cat);
      setDeletedItems(data || []);
    } catch (err) {
      error('Không thể tải danh sách nội dung đã xóa.');
    } finally {
      setLoading(false);
    }
  }, [error]);

  useEffect(() => {
    fetchDeleted(selectedCategory);
  }, [selectedCategory, fetchDeleted]);

  // ─── Restore deleted content ───────────────────────────────────────
  const handleRestore = async (item) => {
    const label = CATEGORY_LABELS[item.contentType] || 'nội dung';
    if (!window.confirm(`Bạn có chắc muốn khôi phục ${label.toLowerCase()}: "${item.titleOrText}"?`)) {
      return;
    }
    setActionPendingId(item.id);
    try {
      await restoreDeletedContent(item.contentType, item.id);
      success(`Khôi phục ${label.toLowerCase()} "${item.titleOrText}" thành công.`);
      fetchDeleted(selectedCategory);
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Lỗi khi khôi phục nội dung';
      error(msg);
    } finally {
      setActionPendingId(null);
    }
  };

  const filtered = deletedItems.filter(item => {
    if (selectedLevel === 'all') return true;
    return item.jlptLevel && item.jlptLevel.toUpperCase() === selectedLevel.toUpperCase();
  });

  return (
    <div className="mdt-page">
      <ManagerTopNav activeTab="manager-deleted-topics" />

      <main className="mdt-body">
        <StaffPageHero
          accent="red"
          title="Thư Mục Đã Xóa"
          subtitle="Quản lý và khôi phục các mục học liệu bị soft-deleted (Bài học, Câu hỏi, Từ vựng, Ngữ pháp, Kanji, Bài kiểm tra)"
          icon={
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              <line x1="10" y1="11" x2="10" y2="17" />
              <line x1="14" y1="11" x2="14" y2="17" />
            </svg>
          }
        />

        {/* Filter bar with Category dropdown */}
        <div className="mdt-filter-bar">
          <div className="mdt-filter-item">
            <select
              id="mdt-cat-select"
              className="mdt-select"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              aria-label="Thể loại"
            >
              {CATEGORIES.map((cat) => (
                <option key={cat.id} value={cat.id}>{cat.label}</option>
              ))}
            </select>
          </div>

          <div className="mdt-filter-item">
            <select
              id="mdt-level-select"
              className="mdt-select"
              value={selectedLevel}
              onChange={(e) => setSelectedLevel(e.target.value)}
              aria-label="Cấp độ JLPT"
            >
              <option value="all">Tất cả cấp độ</option>
              <option value="N5">N5</option>
              <option value="N4">N4</option>
              <option value="N3">N3</option>
              <option value="N2">N2</option>
              <option value="N1">N1</option>
            </select>
          </div>

          {!loading && (
            <span className="mdt-result-count">
              Tổng số: {filtered.length} mục
            </span>
          )}
        </div>

        {/* Content list */}
        {loading ? (
          <div className="mdt-loading-wrap" aria-busy="true">
            <div className="mdt-spinner" />
            <p>Đang tải dữ liệu...</p>
          </div>
        ) : filtered.length > 0 ? (
          <div className="mdt-table-wrap">
            <table className="mdt-table">
              <thead>
                <tr>
                  <th>Thể loại</th>
                  <th>Nội dung</th>
                  <th>Cấp độ</th>
                  <th>Ngày xóa</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((item) => (
                  <tr key={`${item.contentType}-${item.id}`}>
                    <td>
                      <span className={`mdt-type-badge mdt-type-badge--${item.contentType}`}>
                        {CATEGORY_LABELS[item.contentType] || item.contentType}
                      </span>
                    </td>
                    <td className="mdt-td-title-vi">{item.titleOrText}</td>
                    <td className="mdt-td-level">
                      {item.jlptLevel ? (
                        <span className={`mdt-level-badge mdt-level-badge--${item.jlptLevel.toLowerCase()}`}>
                          {item.jlptLevel}
                        </span>
                      ) : (
                        <span className="mdt-level-none">—</span>
                      )}
                    </td>
                    <td className="mdt-td-date">{item.updatedAt || '—'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <button
                        type="button"
                        className="mdt-btn-action mdt-btn-action--restore"
                        onClick={() => handleRestore(item)}
                        disabled={actionPendingId === item.id}
                        title="Khôi phục"
                        aria-label={`Khôi phục ${item.titleOrText}`}
                      >
                        <IconRestore size={16} />
                        {actionPendingId === item.id ? 'Đang khôi phục...' : 'Khôi phục'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <EmptyState
            mascotVariant="happy"
            title="Thùng rác trống"
            subtitle="Hiện tại không có mục nào bị xóa thuộc thể loại này."
          />
        )}
      </main>
    </div>
  );
}
