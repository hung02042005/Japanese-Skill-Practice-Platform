import { useState, useMemo, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getDashboardSummary, getAuditLog } from '../../api/adminService';
import AdminTopNav from '../../components/layout/AdminTopNav';
import { AdminPageHeader } from '../../components/admin/AdminPageHeader';
import { JlptBadge, StatusBadge } from '../../components/common/Badges';
import { Pagination } from '../../components/common/Pagination';
import { EmptyState } from '../../components/common/EmptyState';
import { IcAdminChip } from '../../components/admin/ManageUsersIcons';
import './AdminReports.css';

// ─── Mock Data ───────────────────────────────────────────────────────────────

const MOCK_CONTENT_LOG = [
  { id: 1,  type: 'course',      title: 'Tiếng Nhật N5 Cơ Bản',           jlptLevel: 'N5', uploadedBy: 'Nguyễn Thị Mai',    email: 'mai.nguyen@sakuji.vn', status: 'published',     createdAt: '2026-06-01' },
  { id: 2,  type: 'lesson',      title: 'Bài 1 — Giới thiệu bản thân',     jlptLevel: 'N5', uploadedBy: 'Nguyễn Thị Mai',    email: 'mai.nguyen@sakuji.vn', status: 'published',     createdAt: '2026-06-01' },
  { id: 3,  type: 'vocabulary',  title: '学校 (trường học)',                jlptLevel: 'N5', uploadedBy: 'Trần Văn Hùng',     email: 'hung.tran@sakuji.vn',  status: 'published',     createdAt: '2026-06-01' },
  { id: 4,  type: 'grammar',     title: '～てから (sau khi ~)',             jlptLevel: 'N5', uploadedBy: 'Trần Văn Hùng',     email: 'hung.tran@sakuji.vn',  status: 'published',     createdAt: '2026-06-01' },
  { id: 5,  type: 'kanji',       title: '水 — Nước',                       jlptLevel: 'N5', uploadedBy: 'Lê Thị Hoa',        email: 'hoa.le@sakuji.vn',     status: 'published',     createdAt: '2026-06-01' },
  { id: 6,  type: 'course',      title: 'Tiếng Nhật N4 Tiếp Nối',          jlptLevel: 'N4', uploadedBy: 'Nguyễn Thị Mai',    email: 'mai.nguyen@sakuji.vn', status: 'pending_review',createdAt: '2026-06-02' },
  { id: 7,  type: 'lesson',      title: 'Bài đọc hiểu N4 — Mùa xuân',     jlptLevel: 'N4', uploadedBy: 'Phạm Minh Đức',     email: 'duc.pham@sakuji.vn',   status: 'draft',         createdAt: '2026-06-02' },
  { id: 8,  type: 'vocabulary',  title: '綺麗 (đẹp / sạch sẽ)',            jlptLevel: 'N4', uploadedBy: 'Trần Văn Hùng',     email: 'hung.tran@sakuji.vn',  status: 'pending_review',createdAt: '2026-06-03' },
  { id: 9,  type: 'grammar',     title: '～ても (dù ~ đi nữa)',             jlptLevel: 'N4', uploadedBy: 'Phạm Minh Đức',     email: 'duc.pham@sakuji.vn',   status: 'draft',         createdAt: '2026-06-03' },
  { id: 10, type: 'kanji',       title: '電 — Điện',                       jlptLevel: 'N4', uploadedBy: 'Lê Thị Hoa',        email: 'hoa.le@sakuji.vn',     status: 'draft',         createdAt: '2026-06-03' },
  { id: 11, type: 'course',      title: 'Tiếng Nhật N3 Trung Cấp',         jlptLevel: 'N3', uploadedBy: 'Nguyễn Thị Mai',    email: 'mai.nguyen@sakuji.vn', status: 'draft',         createdAt: '2026-06-03' },
  { id: 12, type: 'lesson',      title: 'Luyện nghe N3 — Tại sân bay',     jlptLevel: 'N3', uploadedBy: 'Phạm Minh Đức',     email: 'duc.pham@sakuji.vn',   status: 'rejected',      createdAt: '2026-05-30' },
  { id: 13, type: 'grammar',     title: '～ことができる (có thể làm ~)',     jlptLevel: 'N4', uploadedBy: 'Trần Văn Hùng',     email: 'hung.tran@sakuji.vn',  status: 'pending_review',createdAt: '2026-06-02' },
  { id: 14, type: 'grammar',     title: '～ようになる (trở nên có thể ~)',   jlptLevel: 'N3', uploadedBy: 'Trần Văn Hùng',     email: 'hung.tran@sakuji.vn',  status: 'rejected',      createdAt: '2026-05-30' },
  { id: 15, type: 'kanji',       title: '語 — Ngôn ngữ',                   jlptLevel: 'N4', uploadedBy: 'Lê Thị Hoa',        email: 'hoa.le@sakuji.vn',     status: 'pending_review',createdAt: '2026-06-02' },
  { id: 16, type: 'lesson',      title: 'Luyện nói N4 — Hỏi đường',        jlptLevel: 'N4', uploadedBy: 'Phạm Minh Đức',     email: 'duc.pham@sakuji.vn',   status: 'published',     createdAt: '2026-05-28' },
  { id: 17, type: 'vocabulary',  title: '食べる (ăn)',                      jlptLevel: 'N5', uploadedBy: 'Trần Văn Hùng',     email: 'hung.tran@sakuji.vn',  status: 'draft',         createdAt: '2026-06-02' },
  { id: 18, type: 'vocabulary',  title: '図書館 (thư viện)',                jlptLevel: 'N4', uploadedBy: 'Lê Thị Hoa',        email: 'hoa.le@sakuji.vn',     status: 'published',     createdAt: '2026-05-31' },
  { id: 19, type: 'kanji',       title: '山 — Núi',                        jlptLevel: 'N5', uploadedBy: 'Lê Thị Hoa',        email: 'hoa.le@sakuji.vn',     status: 'published',     createdAt: '2026-06-01' },
  { id: 20, type: 'lesson',      title: 'Bài 2 — Số đếm và thời gian',     jlptLevel: 'N5', uploadedBy: 'Nguyễn Thị Mai',    email: 'mai.nguyen@sakuji.vn', status: 'pending_review',createdAt: '2026-06-02' },
];

// ─── Constants ───────────────────────────────────────────────────────────────

const TABS = [
  { id: 'overview',  label: 'Tổng quan' },
  { id: 'content',   label: 'Nội dung' },
  { id: 'activity',  label: 'Lịch sử hành động' },
];

const CONTENT_TYPES = ['Tất cả', 'course', 'lesson', 'vocabulary', 'grammar', 'kanji'];
const CONTENT_TYPE_LABELS = { course: 'Khóa học', lesson: 'Bài học', vocabulary: 'Từ vựng', grammar: 'Ngữ pháp', kanji: 'Kanji' };
const JLPT_LEVELS = ['Tất cả', 'N5', 'N4', 'N3', 'N2', 'N1'];
const STATUSES = ['Tất cả', 'published', 'pending_review', 'draft', 'rejected'];
const STATUS_LABELS = { published: 'Đã xuất bản', pending_review: 'Chờ duyệt', draft: 'Bản nháp', rejected: 'Từ chối' };
const ACTION_LABELS = {
  create_staff:             'Tạo tài khoản Staff',
  suspend_user:             'Đình chỉ tài khoản',
  activate_user:            'Kích hoạt lại',
  soft_delete_user:         'Xóa tài khoản',
  reset_password_initiated: 'Đặt lại mật khẩu',
  change_staff_role:        'Đổi vai trò Staff',
  update_setting:           'Cập nhật cài đặt',
};
const ACTION_TYPES = ['Tất cả', ...Object.keys(ACTION_LABELS)];
const PAGE_SIZE = 10;

const STAFF_LIST = [...new Set(MOCK_CONTENT_LOG.map((c) => c.uploadedBy))];

// ─── Sub-components ───────────────────────────────────────────────────────────

function StatCard({ value, label, sub, color }) {
  return (
    <div className="rp-stat-card">
      <div className="rp-stat-value" style={{ color }}>{value}</div>
      <div className="rp-stat-label">{label}</div>
      {sub && <div className="rp-stat-sub">{sub}</div>}
    </div>
  );
}

function FilterBar({ filters, onChange }) {
  return (
    <div className="rp-filter-bar">
      <select className="rp-select" value={filters.type} onChange={(e) => onChange('type', e.target.value)}>
        {CONTENT_TYPES.map((t) => <option key={t} value={t}>{t === 'Tất cả' ? 'Loại: Tất cả' : CONTENT_TYPE_LABELS[t]}</option>)}
      </select>
      <select className="rp-select" value={filters.level} onChange={(e) => onChange('level', e.target.value)}>
        {JLPT_LEVELS.map((l) => <option key={l} value={l}>{l === 'Tất cả' ? 'Cấp độ: Tất cả' : l}</option>)}
      </select>
      <select className="rp-select" value={filters.staff} onChange={(e) => onChange('staff', e.target.value)}>
        <option value="Tất cả">Nhân viên: Tất cả</option>
        {STAFF_LIST.map((s) => <option key={s} value={s}>{s}</option>)}
      </select>
      <select className="rp-select" value={filters.status} onChange={(e) => onChange('status', e.target.value)}>
        {STATUSES.map((s) => <option key={s} value={s}>{s === 'Tất cả' ? 'Trạng thái: Tất cả' : STATUS_LABELS[s]}</option>)}
      </select>
    </div>
  );
}

function TypeChip({ type }) {
  const label = CONTENT_TYPE_LABELS[type] ?? type;
  const colors = {
    course:     { bg: '#EDE7F6', color: '#4527A0' },
    lesson:     { bg: 'var(--color-primary-bg)', color: 'var(--color-primary)' },
    vocabulary: { bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
    grammar:    { bg: '#E3F2FD', color: '#1565C0' },
    kanji:      { bg: '#FFF3E0', color: '#E65100' },
  };
  const { bg, color } = colors[type] ?? { bg: '#F5F5F5', color: '#555' };
  return (
    <span className="rp-type-chip" style={{ background: bg, color }}>{label}</span>
  );
}

function ActionChip({ actionType }) {
  const label = ACTION_LABELS[actionType] ?? actionType;
  const colors = {
    create_staff:             { bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
    suspend_user:             { bg: '#FFF3E0',                   color: '#F57C00' },
    activate_user:            { bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
    soft_delete_user:         { bg: '#FFEAEA',                   color: 'var(--color-error)' },
    reset_password_initiated: { bg: 'var(--color-primary-bg)',   color: 'var(--color-primary)' },
    change_staff_role:        { bg: '#F3E5F5',                   color: '#6A1B9A' },
    update_setting:           { bg: '#E3F2FD',                   color: '#1565C0' },
  };
  const { bg, color } = colors[actionType] ?? { bg: '#F5F5F5', color: '#555' };
  return <span className="rp-type-chip" style={{ background: bg, color }}>{label}</span>;
}

// ─── Tabs ─────────────────────────────────────────────────────────────────────

function OverviewTab() {
  const [summary,   setSummary]   = useState(null);
  const [isLoading, setLoading]   = useState(true);
  const [hasError,  setHasError]  = useState(false);

  const fetch = useCallback(() => {
    setLoading(true);
    setHasError(false);
    getDashboardSummary()
      .then((data) => setSummary(data))
      .catch(() => setHasError(true))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetch(); }, [fetch]);

  if (isLoading) {
    return (
      <div className="rp-overview">
        <section className="rp-section">
          <div className="rp-stat-grid">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="rp-stat-card rp-stat-card--skel" aria-hidden="true" />
            ))}
          </div>
        </section>
      </div>
    );
  }

  if (hasError) {
    return (
      <div className="rp-error-state" role="alert">
        <p>Không thể tải số liệu tổng quan.</p>
        <button type="button" className="rp-retry-btn" onClick={fetch}>Thử lại</button>
      </div>
    );
  }

  return (
    <div className="rp-overview">
      <section className="rp-section">
        <h2 className="rp-section-title">Tổng quan hệ thống</h2>
        <div className="rp-stat-grid rp-stat-grid--4">
          <StatCard value={summary?.totalUsers ?? 0}        label="Tổng người dùng"        color="var(--color-primary)" />
          <StatCard value={summary?.activeToday ?? 0}       label="Hoạt động hôm nay"      color="#2E7D32" />
          <StatCard value={summary?.quizAttemptsToday ?? 0} label="Lượt thi / quiz hôm nay" color="#1565C0" />
          <StatCard
            value={summary?.systemStatus ?? 'OK'}
            label="Trạng thái hệ thống"
            color={summary?.systemStatus === 'OK' ? '#2E7D32' : summary?.systemStatus === 'MAINTENANCE' ? '#F57C00' : '#C62828'}
          />
        </div>
      </section>
    </div>
  );
}

function ContentTab() {
  const [filters, setFilters] = useState({ type: 'Tất cả', level: 'Tất cả', staff: 'Tất cả', status: 'Tất cả' });
  const [page, setPage] = useState(1);

  function handleFilter(key, val) {
    setFilters((prev) => ({ ...prev, [key]: val }));
    setPage(1);
  }

  const filtered = useMemo(() => MOCK_CONTENT_LOG.filter((item) => {
    if (filters.type   !== 'Tất cả' && item.type        !== filters.type)   return false;
    if (filters.level  !== 'Tất cả' && item.jlptLevel   !== filters.level)  return false;
    if (filters.staff  !== 'Tất cả' && item.uploadedBy  !== filters.staff)  return false;
    if (filters.status !== 'Tất cả' && item.status      !== filters.status) return false;
    return true;
  }), [filters]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const pageItems  = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  return (
    <div className="rp-tab-body">
      <div className="rp-tab-toolbar">
        <FilterBar filters={filters} onChange={handleFilter} />
        <span className="rp-count">{filtered.length} mục</span>
      </div>

      {pageItems.length === 0 ? (
        <EmptyState title="Không có kết quả" subtitle="Thử thay đổi bộ lọc để tìm nội dung." mascotVariant="thinking" mascotSize={90} />
      ) : (
        <>
          <div className="rp-table-wrap">
            <table className="rp-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Loại</th>
                  <th>Tiêu đề</th>
                  <th>Cấp độ</th>
                  <th>Người tạo</th>
                  <th>Ngày tạo</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((item, idx) => (
                  <tr key={item.id}>
                    <td className="rp-td-num">{(page - 1) * PAGE_SIZE + idx + 1}</td>
                    <td><TypeChip type={item.type} /></td>
                    <td className="rp-td-title">{item.title}</td>
                    <td><JlptBadge level={item.jlptLevel} /></td>
                    <td>
                      <div className="rp-uploader">
                        <span className="rp-uploader-name">{item.uploadedBy}</span>
                        <span className="rp-uploader-email">{item.email}</span>
                      </div>
                    </td>
                    <td className="rp-td-date">{new Date(item.createdAt).toLocaleDateString('vi-VN')}</td>
                    <td><StatusBadge status={item.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}

function ActivityTab() {
  const [logs,         setLogs]        = useState([]);
  const [totalPages,   setTotalPages]  = useState(1);
  const [page,         setPage]        = useState(1);
  const [isLoading,    setLoading]     = useState(true);
  const [hasError,     setHasError]    = useState(false);
  const [actionFilter, setActionFilter] = useState('Tất cả');

  const fetchPage = useCallback((p) => {
    setLoading(true);
    setHasError(false);
    getAuditLog({ page: p - 1, size: PAGE_SIZE })
      .then((data) => {
        setLogs(data?.content ?? []);
        setTotalPages(data?.totalPages ?? 1);
      })
      .catch(() => setHasError(true))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchPage(page); }, [fetchPage, page]);

  function handleAction(val) {
    setActionFilter(val);
  }

  const visibleLogs = useMemo(
    () => (actionFilter === 'Tất cả' ? logs : logs.filter((l) => l.actionType === actionFilter)),
    [logs, actionFilter],
  );

  if (hasError) {
    return (
      <div className="rp-error-state" role="alert">
        <p>Không thể tải lịch sử hoạt động.</p>
        <button type="button" className="rp-retry-btn" onClick={() => fetchPage(page)}>Thử lại</button>
      </div>
    );
  }

  return (
    <div className="rp-tab-body">
      <div className="rp-tab-toolbar">
        <div className="rp-filter-bar">
          <select className="rp-select" value={actionFilter} onChange={(e) => handleAction(e.target.value)}>
            {ACTION_TYPES.map((a) => (
              <option key={a} value={a}>{a === 'Tất cả' ? 'Hành động: Tất cả' : ACTION_LABELS[a]}</option>
            ))}
          </select>
        </div>
        <span className="rp-count">{isLoading ? '...' : `${visibleLogs.length} bản ghi (trang này)`}</span>
      </div>

      {isLoading ? (
        <div className="rp-loading" aria-live="polite">Đang tải lịch sử hoạt động...</div>
      ) : visibleLogs.length === 0 ? (
        <EmptyState title="Không có kết quả" subtitle="Thử thay đổi bộ lọc." mascotVariant="thinking" mascotSize={90} />
      ) : (
        <>
          <div className="rp-table-wrap">
            <table className="rp-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Hành động</th>
                  <th>Đối tượng</th>
                  <th>Admin thực hiện</th>
                  <th>Thời gian</th>
                </tr>
              </thead>
              <tbody>
                {visibleLogs.map((log, idx) => (
                  <tr key={log.logId}>
                    <td className="rp-td-num">{(page - 1) * PAGE_SIZE + idx + 1}</td>
                    <td><ActionChip actionType={log.actionType} /></td>
                    <td className="rp-td-target">{log.targetEmail ?? <span className="rp-none">—</span>}</td>
                    <td className="rp-td-admin">{log.adminEmail}</td>
                    <td className="rp-td-date">
                      {new Date(log.createdAt).toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination currentPage={page} totalPages={totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function AdminReports() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialTab = searchParams.get('tab') ?? 'overview';
  const [activeTab, setActiveTab] = useState(
    TABS.some((t) => t.id === initialTab) ? initialTab : 'overview'
  );

  function switchTab(id) {
    setActiveTab(id);
    setSearchParams({ tab: id }, { replace: true });
  }

  return (
    <div className="rp-page">
      <AdminTopNav activeTab="reports" />

      <AdminPageHeader
        chipIcon={<IcAdminChip />}
        chipLabel="Báo cáo"
        title="Báo Cáo & Thống Kê"
        subtitle="Tổng quan hệ thống, nội dung học liệu và lịch sử hành động quản trị"
        mascotVariant="happy"
        mascotSize={100}
      />

      <main className="rp-body">
        <nav className="rp-tabs" aria-label="Tab báo cáo">
          {TABS.map((t) => (
            <button
              key={t.id}
              type="button"
              className={`rp-tab${activeTab === t.id ? ' rp-tab--active' : ''}`}
              onClick={() => switchTab(t.id)}
              aria-current={activeTab === t.id ? 'page' : undefined}
            >
              {t.label}
            </button>
          ))}
        </nav>

        <div className="rp-panel">
          {activeTab === 'overview'  && <OverviewTab />}
          {activeTab === 'content'   && <ContentTab />}
          {activeTab === 'activity'  && <ActivityTab />}
        </div>
      </main>
    </div>
  );
}
