import { useState, useEffect, useMemo, useRef } from 'react';
import AdminTopNav from '../../components/layout/AdminTopNav';
import { AdminPageHeader }           from '../../components/admin/AdminPageHeader';
import { StatCard }                  from '../../components/admin/StatCard';
import { StatusBadge, RoleBadge, JlptBadge, ROLE_LABELS, STAFF_ROLE_LABELS } from '../../components/common/Badges';
import { UserAvatar }                from '../../components/common/UserAvatar';
import { Pagination }                from '../../components/common/Pagination';
import { EmptyState }                from '../../components/common/EmptyState';
import { ToastContainer, useToast }  from '../../components/common/Toast';
import {
  IcAdminChip, IcAddStaff, IcSearchGlass,
  IcBan, IcCheck, IcKey, IcSwap, IcTrash,
  TAB_ICONS, STAT_ICONS,
} from '../../components/admin/ManageUsersIcons';
import { ConfirmModal, SuspendModal, CreateStaffModal, ChangeStaffRoleModal } from '../../components/admin/UserModals';
import { SkeletonRow } from '../../components/admin/SkeletonRow';
import {
  listUsers, createStaff, suspendUser, activateUser,
  resetPassword, softDeleteUser, changeStaffRole,
} from '../../api/adminService';
import './ManageUsers.css';

/* ── Constants ── */
const PAGE_SIZE = 20;

const TYPE_TABS = [
  { value: 'student', label: 'Học viên' },
  { value: 'staff',   label: 'Nhân viên' },
  { value: 'admin',   label: 'Quản trị' },
];

function formatDate(d) {
  if (!d) return '—';
  const dt = new Date(d);
  return `${String(dt.getDate()).padStart(2,'0')}/${String(dt.getMonth()+1).padStart(2,'0')}/${dt.getFullYear()}`;
}

/* ═══════════════════════════════════════
   MAIN PAGE
═══════════════════════════════════════ */
function ManageUsers() {
  const [activeType, setActiveType]       = useState('student');
  const [users, setUsers]                 = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages]       = useState(1);
  const [isLoading, setIsLoading]         = useState(true);
  const [currentPage, setCurrentPage]     = useState(1);

  const [search, setSearch]               = useState('');
  const [debouncedSearch, setDebounced]   = useState('');
  const [statusFilter, setStatusFilter]   = useState('');
  const [jlptFilter, setJlptFilter]       = useState('');
  const [staffRoleFilter, setStaffRole]   = useState('');

  const [isSubmitting, setSubmitting]     = useState(false);
  const { toasts, addToast, removeToast } = useToast();

  const [confirmModal, setConfirmModal]   = useState({ open: false, action: '', userId: null, userType: null, userName: '' });
  const [suspendModal, setSuspendModal]   = useState({ open: false, userId: null, userType: null, userName: '' });
  const [staffRoleModal, setStaffRoleMod] = useState({ open: false, userId: null, userName: '', currentStaffRole: 'staff' });
  const [createStaffOpen, setCreateStaff] = useState(false);

  /* Debounce search */
  const debounceRef = useRef(null);
  useEffect(() => {
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => { setDebounced(search); setCurrentPage(1); }, 400);
    return () => clearTimeout(debounceRef.current);
  }, [search]);

  /* Reset page on filter/type change */
  useEffect(() => { setCurrentPage(1); }, [activeType, statusFilter, jlptFilter, staffRoleFilter]);

  /* Build query params */
  function buildParams(page) {
    const params = { type: activeType, page: page - 1, size: PAGE_SIZE };
    if (debouncedSearch)                               params.q         = debouncedSearch;
    if (statusFilter)                                  params.status     = statusFilter;
    if (activeType === 'student' && jlptFilter)        params.jlptLevel  = jlptFilter;
    if (activeType === 'staff'   && staffRoleFilter)   params.staffRole  = staffRoleFilter;
    return params;
  }

  /* Fetch users */
  useEffect(() => {
    let cancelled = false;
    setIsLoading(true);
    listUsers(buildParams(currentPage))
      .then((data) => {
        if (cancelled) return;
        setUsers(data.content ?? []);
        setTotalElements(data.totalElements ?? 0);
        setTotalPages(data.totalPages ?? 1);
        setIsLoading(false);
      })
      .catch(() => {
        if (cancelled) return;
        setIsLoading(false);
        addToast('error', 'Không thể tải danh sách người dùng, vui lòng thử lại!');
      });
    return () => { cancelled = true; };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeType, debouncedSearch, statusFilter, jlptFilter, staffRoleFilter, currentPage]);

  /* Stats from current page data */
  const stats = useMemo(() => ({
    total:     totalElements,
    active:    users.filter((u) => u.status === 'active').length,
    suspended: users.filter((u) => u.status === 'suspended').length,
    pending:   users.filter((u) => u.status === 'pending').length,
  }), [users, totalElements]);

  function reload() {
    setUsers([]);
    setIsLoading(true);
    listUsers(buildParams(currentPage))
      .then((data) => { setUsers(data.content ?? []); setTotalElements(data.totalElements ?? 0); setTotalPages(data.totalPages ?? 1); })
      .catch(() => addToast('error', 'Không thể làm mới danh sách'))
      .finally(() => setIsLoading(false));
  }

  /* ── Action openers ── */
  const openSuspend    = (u) => setSuspendModal({ open: true, userId: u.userId, userType: u.userType, userName: u.fullName });
  const openConfirm    = (action, u) => setConfirmModal({ open: true, action, userId: u.userId, userType: u.userType, userName: u.fullName });
  const openChangeRole = (u) => setStaffRoleMod({ open: true, userId: u.userId, userName: u.fullName, currentStaffRole: u.staffRole ?? 'staff' });

  /* ── Action handlers ── */
  async function handleSuspend(reason) {
    setSubmitting(true);
    try {
      await suspendUser(suspendModal.userType, suspendModal.userId, reason);
      addToast('success', 'Đã đình chỉ tài khoản thành công!');
      setSuspendModal((m) => ({ ...m, open: false }));
      reload();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally { setSubmitting(false); }
  }

  async function handleConfirm() {
    const { action, userType, userId } = confirmModal;
    setSubmitting(true);
    try {
      if (action === 'activate')   await activateUser(userType, userId);
      if (action === 'reset-pass') await resetPassword(userType, userId);
      if (action === 'delete')     await softDeleteUser(userType, userId);
      const msgs = { activate: 'Đã kích hoạt lại tài khoản!', 'reset-pass': 'Email đặt lại mật khẩu đã được gửi!', delete: 'Đã xóa tài khoản thành công!' };
      addToast('success', msgs[action]);
      setConfirmModal((m) => ({ ...m, open: false }));
      reload();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally { setSubmitting(false); }
  }

  async function handleCreateStaff(data) {
    setSubmitting(true);
    try {
      await createStaff(data);
      addToast('success', 'Tạo tài khoản nhân viên thành công! Email mời đã được gửi.');
      setCreateStaff(false);
      if (activeType !== 'staff') setActiveType('staff');
      else reload();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally { setSubmitting(false); }
  }

  async function handleChangeStaffRole(newRole) {
    setSubmitting(true);
    try {
      await changeStaffRole(staffRoleModal.userId, newRole);
      addToast('success', `Đã cập nhật vai trò thành "${STAFF_ROLE_LABELS[newRole]}"!`);
      setStaffRoleMod((m) => ({ ...m, open: false }));
      reload();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại');
    } finally { setSubmitting(false); }
  }

  /* ── Render ── */
  return (
    <div className="mu-page">
      <AdminTopNav activeTab="manage-users" />

      {/* Header */}
      <AdminPageHeader
        chipIcon={<IcAdminChip />}
        chipLabel="Quản trị viên"
        title="Quản lý Người Dùng"
        subtitle="Theo dõi, phân quyền và quản lý toàn bộ tài khoản trên hệ thống"
      />

      {/* Body */}
      <div className="mu-body">

        {/* Stats */}
        <div className="mu-stats-row">
          <StatCard icon={STAT_ICONS.total}     variant="total"  loading={isLoading} value={totalElements}   label={`Tổng ${ROLE_LABELS[activeType]}`} />
          <StatCard icon={STAT_ICONS.active}    variant="active" loading={isLoading} value={stats.active}    label="Đang hoạt động (trang này)" />
          <StatCard icon={STAT_ICONS.suspended} variant="banned" loading={isLoading} value={stats.suspended} label="Đình chỉ (trang này)" />
          <StatCard icon={STAT_ICONS.pending}   variant="new"    loading={isLoading} value={stats.pending}   label="Chờ kích hoạt (trang này)" />
        </div>

        {/* Type tabs + Create Staff */}
        <div className="mu-type-bar">
          <div className="mu-type-tabs">
            {TYPE_TABS.map((tab) => (
              <button
                key={tab.value}
                type="button"
                className={`mu-type-tab${activeType===tab.value?' mu-type-tab--on':''}`}
                onClick={() => { setActiveType(tab.value); setSearch(''); setStatusFilter(''); setJlptFilter(''); setStaffRole(''); }}
              >
                <span className="mu-tab-icon">{TAB_ICONS[tab.value]}</span>
                {tab.label}
              </button>
            ))}
          </div>
          {activeType === 'staff' && (
            <button type="button" className="mu-btn mu-btn--primary mu-btn--add" onClick={() => setCreateStaff(true)}>
              <IcAddStaff />
              Tạo nhân viên mới
            </button>
          )}
        </div>

        {/* Filter bar */}
        <div className="mu-filter-bar">
          <div className="mu-search">
            <span className="mu-search-ic"><IcSearchGlass /></span>
            <input
              type="text"
              className="mu-search-input"
              placeholder="Tìm theo tên hoặc email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              aria-label="Tìm kiếm người dùng"
            />
            {search && <button type="button" className="mu-search-clear" onClick={() => setSearch('')} aria-label="Xoá tìm kiếm">×</button>}
          </div>

          <div className="mu-selects">
            <select className="mu-sel" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="">Tất cả trạng thái</option>
              <option value="active">✅ Hoạt động</option>
              <option value="suspended">🔒 Đình chỉ</option>
              <option value="pending">⏳ Chờ kích hoạt</option>
              <option value="deleted">🗑️ Đã xóa</option>
            </select>

            {activeType === 'student' && (
              <select className="mu-sel" value={jlptFilter} onChange={(e) => setJlptFilter(e.target.value)}>
                <option value="">Tất cả cấp độ</option>
                {['N5','N4','N3','N2','N1'].map((l) => <option key={l} value={l}>{l}</option>)}
              </select>
            )}

            {activeType === 'staff' && (
              <select className="mu-sel" value={staffRoleFilter} onChange={(e) => setStaffRole(e.target.value)}>
                <option value="">Tất cả vai trò staff</option>
                <option value="staff">🌿 Nhân viên</option>
                <option value="staff_manager">⭐ Quản lý nhân viên</option>
              </select>
            )}
          </div>

          {!isLoading && (
            <span className="mu-count">{totalElements} người dùng</span>
          )}
        </div>

        {/* Table */}
        <div className="mu-table-card">
          <div className="mu-table-scroll">
            <table className="mu-table">
              <thead>
                <tr>
                  <th className="mu-th mu-th--idx">#</th>
                  <th className="mu-th">Người dùng</th>
                  <th className="mu-th">Vai trò</th>
                  {activeType === 'student' && <th className="mu-th">Cấp độ JLPT</th>}
                  {activeType === 'student' && <th className="mu-th">Streak</th>}
                  <th className="mu-th">Ngày đăng ký</th>
                  <th className="mu-th">Trạng thái</th>
                  <th className="mu-th mu-th--act">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {isLoading
                  ? Array.from({ length: 8 }, (_, i) => <SkeletonRow key={i} />)
                  : users.length === 0
                    ? (
                      <tr>
                        <td colSpan={9} className="mu-empty-cell">
                          <EmptyState
                            title="Không tìm thấy ai cả!"
                            subtitle="Thử thay đổi bộ lọc hoặc từ khoá tìm kiếm nhé 🌸"
                          />
                        </td>
                      </tr>
                    )
                    : users.map((u, idx) => (
                      <tr
                        key={u.userId}
                        className={`mu-tr${u.status==='suspended'||u.status==='deleted'?' mu-tr--banned':''}`}
                        style={{ '--row-delay': `${idx * 30}ms` }}
                      >
                        <td className="mu-td mu-td--idx">{(currentPage - 1) * PAGE_SIZE + idx + 1}</td>

                        <td className="mu-td">
                          <div className="mu-user-cell">
                            <UserAvatar name={u.fullName} userType={u.userType} isActive={u.status === 'active'} />
                            <div className="mu-user-text">
                              <span className="mu-user-name">{u.fullName}</span>
                              <span className="mu-user-email">{u.email}</span>
                            </div>
                          </div>
                        </td>

                        <td className="mu-td"><RoleBadge userType={u.userType} staffRole={u.staffRole} /></td>
                        {activeType === 'student' && <td className="mu-td"><JlptBadge level={u.currentJlptLevel} /></td>}
                        {activeType === 'student' && (
                          <td className="mu-td">
                            <span className="mu-streak">
                              <span className={u.currentStreak > 0 ? 'mu-fire mu-fire--on' : 'mu-fire'} aria-hidden="true">🔥</span>
                              {u.currentStreak ?? 0}
                            </span>
                          </td>
                        )}
                        <td className="mu-td mu-td--date">{formatDate(u.createdAt)}</td>
                        <td className="mu-td"><StatusBadge status={u.status} /></td>

                        <td className="mu-td mu-td--act" onClick={(e) => e.stopPropagation()}>
                          <div className="mu-acts">
                            {(u.status === 'active' || u.status === 'pending') && (
                              <button type="button" className="mu-act-ic mu-act-ic--suspend" onClick={() => openSuspend(u)} title="Đình chỉ tài khoản" aria-label="Đình chỉ tài khoản"><IcBan /></button>
                            )}
                            {u.status === 'suspended' && (
                              <button type="button" className="mu-act-ic mu-act-ic--activate" onClick={() => openConfirm('activate', u)} title="Kích hoạt lại" aria-label="Kích hoạt lại tài khoản"><IcCheck /></button>
                            )}
                            {u.status !== 'deleted' && (
                              <button type="button" className="mu-act-ic mu-act-ic--reset" onClick={() => openConfirm('reset-pass', u)} title="Đặt lại mật khẩu" aria-label="Đặt lại mật khẩu"><IcKey /></button>
                            )}
                            {u.userType === 'staff' && u.status !== 'deleted' && (
                              <button type="button" className="mu-act-ic mu-act-ic--role" onClick={() => openChangeRole(u)} title="Đổi vai trò Staff" aria-label="Đổi vai trò Staff"><IcSwap /></button>
                            )}
                            {u.userType !== 'admin' && u.status !== 'deleted' && (
                              <button type="button" className="mu-act-ic mu-act-ic--delete" onClick={() => openConfirm('delete', u)} title="Xóa tài khoản" aria-label="Xóa tài khoản"><IcTrash /></button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))
                }
              </tbody>
            </table>
          </div>

          <Pagination currentPage={currentPage} totalPages={totalPages} onChange={setCurrentPage} />
        </div>
      </div>

      {/* Modals */}
      <SuspendModal modal={suspendModal} onConfirm={handleSuspend} onClose={() => !isSubmitting && setSuspendModal((m) => ({ ...m, open: false }))} isSubmitting={isSubmitting} />
      <ConfirmModal modal={confirmModal} onConfirm={handleConfirm} onClose={() => !isSubmitting && setConfirmModal((m) => ({ ...m, open: false }))} isSubmitting={isSubmitting} />
      <ChangeStaffRoleModal modal={staffRoleModal} onConfirm={handleChangeStaffRole} onClose={() => !isSubmitting && setStaffRoleMod((m) => ({ ...m, open: false }))} isSubmitting={isSubmitting} />
      <CreateStaffModal open={createStaffOpen} onConfirm={handleCreateStaff} onClose={() => !isSubmitting && setCreateStaff(false)} isSubmitting={isSubmitting} />

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}

export default ManageUsers;
