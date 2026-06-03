import { useState, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
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

const MOCK_AUDIT_LOG = [
  { logId: 1,  actionType: 'create_staff',             targetEmail: 'hung.tran@sakuji.vn',   adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-03T08:12:00Z' },
  { logId: 2,  actionType: 'suspend_user',             targetEmail: 'student01@gmail.com',   adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-03T08:45:00Z' },
  { logId: 3,  actionType: 'activate_user',            targetEmail: 'student02@gmail.com',   adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-03T09:10:00Z' },
  { logId: 4,  actionType: 'reset_password_initiated', targetEmail: 'mai.nguyen@sakuji.vn',  adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-03T09:30:00Z' },
  { logId: 5,  actionType: 'change_staff_role',        targetEmail: 'duc.pham@sakuji.vn',    adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-03T10:05:00Z' },
  { logId: 6,  actionType: 'update_setting',           targetEmail: null,                     adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-03T10:22:00Z' },
  { logId: 7,  actionType: 'soft_delete_user',         targetEmail: 'olduser@gmail.com',     adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-02T14:00:00Z' },
  { logId: 8,  actionType: 'create_staff',             targetEmail: 'hoa.le@sakuji.vn',      adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-02T11:30:00Z' },
  { logId: 9,  actionType: 'suspend_user',             targetEmail: 'student03@gmail.com',   adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-02T09:15:00Z' },
  { logId: 10, actionType: 'update_setting',           targetEmail: null,                     adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-01T16:40:00Z' },
  { logId: 11, actionType: 'activate_user',            targetEmail: 'student04@gmail.com',   adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-01T14:20:00Z' },
  { logId: 12, actionType: 'reset_password_initiated', targetEmail: 'hoa.le@sakuji.vn',      adminEmail: 'admin@sakuji.vn', createdAt: '2026-06-01T11:05:00Z' },
  { logId: 13, actionType: 'change_staff_role',        targetEmail: 'mai.nguyen@sakuji.vn',  adminEmail: 'admin@sakuji.vn', createdAt: '2026-05-31T15:50:00Z' },
  { logId: 14, actionType: 'create_staff',             targetEmail: 'duc.pham@sakuji.vn',    adminEmail: 'admin@sakuji.vn', createdAt: '2026-05-30T10:00:00Z' },
  { logId: 15, actionType: 'soft_delete_user',         targetEmail: 'spam@gmail.com',        adminEmail: 'admin@sakuji.vn', createdAt: '2026-05-30T08:30:00Z' },
];

const MOCK_OVERVIEW = {
  totalUsers:    312,
  totalStudents: 289,
  totalStaff:    21,
  totalAdmin:    2,
  totalContent:  84,
  byCourse:      3,
  byLesson:      18,
  byVocab:       31,
  byGrammar:     17,
  byKanji:       15,
  pendingReview: 7,
  examAttempts:  1248,
  avgScore:      72,
};

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
  const s = MOCK_OVERVIEW;
  return (
    <div className="rp-overview">
      <section className="rp-section">
        <h2 className="rp-section-title">Người dùng</h2>
        <div className="rp-stat-grid">
          <StatCard value={s.totalUsers}    label="Tổng người dùng"  color="var(--color-primary)" />
          <StatCard value={s.totalStudents} label="Học viên"          sub={`${Math.round(s.totalStudents/s.totalUsers*100)}% tổng số`} color="#2E7D32" />
          <StatCard value={s.totalStaff}    label="Nhân viên"         color="#1565C0" />
          <StatCard value={s.totalAdmin}    label="Quản trị viên"     color="#6A1B9A" />
        </div>
      </section>

      <section className="rp-section">
        <h2 className="rp-section-title">Nội dung học liệu</h2>
        <div className="rp-stat-grid">
          <StatCard value={s.totalContent}  label="Tổng nội dung"    color="var(--color-primary)" />
          <StatCard value={s.byCourse}      label="Khóa học"          color="#4527A0" />
          <StatCard value={s.byLesson}      label="Bài học"           color="var(--color-primary)" />
          <StatCard value={s.byVocab}       label="Từ vựng"           color="#2E7D32" />
          <StatCard value={s.byGrammar}     label="Ngữ pháp"          color="#1565C0" />
          <StatCard value={s.byKanji}       label="Kanji"             color="#E65100" />
          <StatCard value={s.pendingReview} label="Chờ duyệt"         color="#F57C00" />
        </div>
      </section>

      <section className="rp-section">
        <h2 className="rp-section-title">Luyện tập & kiểm tra</h2>
        <div className="rp-stat-grid rp-stat-grid--2">
          <StatCard value={s.examAttempts.toLocaleString()} label="Lượt làm bài thi / quiz" color="var(--color-primary)" />
          <StatCard value={`${s.avgScore}%`}                label="Điểm trung bình"          color="#2E7D32" />
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
  const [actionFilter, setActionFilter] = useState('Tất cả');
  const [page, setPage] = useState(1);

  function handleAction(val) {
    setActionFilter(val);
    setPage(1);
  }

  const filtered = useMemo(() => MOCK_AUDIT_LOG.filter((log) => {
    if (actionFilter !== 'Tất cả' && log.actionType !== actionFilter) return false;
    return true;
  }), [actionFilter]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const pageItems  = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

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
        <span className="rp-count">{filtered.length} bản ghi</span>
      </div>

      {pageItems.length === 0 ? (
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
                {pageItems.map((log, idx) => (
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
