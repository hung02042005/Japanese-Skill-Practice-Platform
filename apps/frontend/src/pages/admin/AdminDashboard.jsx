import { useState, useEffect, useCallback } from 'react';
import { Link }                             from 'react-router-dom';
import AdminTopNav                           from '../../components/layout/AdminTopNav';
import { AdminPageHeader }                  from '../../components/admin/AdminPageHeader';
import { StatCard }                         from '../../components/admin/StatCard';
import { ToastContainer, useToast }         from '../../components/common/Toast';
import {
  IcAdminChip, IcAddStaff, IcBan, IcCheck, IcKey, IcSwap, IcTrash, IcEdit,
  IcChart, IcSystemHealth,
  IcMail, IcShield, IcWrench, IcBell,
  STAT_ICONS, TAB_ICONS,
} from '../../components/admin/ManageUsersIcons';
import { getDashboardSummary, getAuditLog } from '../../api/adminService';
import './AdminDashboard.css';

/* ── Relative time ── */
function relativeTime(iso) {
  const diff = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
  if (diff < 60)   return 'vừa xong';
  if (diff < 3600) return `${Math.floor(diff / 60)} phút trước`;
  if (diff < 86400)return `${Math.floor(diff / 3600)} giờ trước`;
  return `${Math.floor(diff / 86400)} ngày trước`;
}

/* ── Log icon + color by action_type ── */
const LOG_STYLE = {
  create_staff:            { icon: <IcAddStaff />, bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
  suspend_user:            { icon: <IcBan />,      bg: '#FFF3E0',                   color: '#F57C00' },
  activate_user:           { icon: <IcCheck />,    bg: 'var(--color-secondary-bg)', color: '#2E7D32' },
  soft_delete_user:        { icon: <IcTrash />,    bg: '#FFEAEA',                   color: 'var(--color-error)' },
  reset_password_initiated:{ icon: <IcKey />,      bg: 'var(--color-primary-bg)',   color: 'var(--color-primary)' },
  change_staff_role:       { icon: <IcSwap />,     bg: '#F3E5F5',                   color: '#6A1B9A' },
  update_setting:          { icon: <IcEdit />,     bg: '#E3F2FD',                   color: '#1565C0' },
};
function logStyle(type) {
  return LOG_STYLE[type] ?? { icon: <IcAdminChip />, bg: 'var(--color-primary-bg)', color: 'var(--color-primary)' };
}

function actionLabel(log) {
  const map = {
    create_staff:             `Tạo tài khoản Staff`,
    suspend_user:             `Đình chỉ tài khoản`,
    activate_user:            `Kích hoạt lại tài khoản`,
    soft_delete_user:         `Xóa tài khoản (soft delete)`,
    reset_password_initiated: `Đặt lại mật khẩu`,
    change_staff_role:        `Đổi vai trò Staff`,
    update_setting:           `Cập nhật cài đặt hệ thống`,
  };
  const base = map[log.actionType] ?? log.actionType;
  return log.targetEmail ? `${base}: ${log.targetEmail}` : base;
}

/* ── System status badge ── */
function SystemStatusBadge({ status }) {
  const cfg = {
    OK:          { label: 'Bình thường', cls: 'adb-status--ok'  },
    MAINTENANCE: { label: 'Bảo trì',     cls: 'adb-status--warn' },
    ERROR:       { label: 'Sự cố',       cls: 'adb-status--err'  },
  }[status] ?? { label: status, cls: '' };
  return <span className={`adb-status-badge ${cfg.cls}`}>{cfg.label}</span>;
}

/* ── Skeleton ── */
function StatSkeleton() {
  return (
    <div className="adb-stat-skel" aria-hidden="true" />
  );
}
function LogSkeleton() {
  return (
    <>
      {Array.from({ length: 6 }).map((_, i) => (
        <div key={i} className="adb-log-skel" aria-hidden="true">
          <div className="adb-log-skel__icon" />
          <div className="adb-log-skel__lines">
            <div className="adb-log-skel__line adb-log-skel__line--main" />
            <div className="adb-log-skel__line adb-log-skel__line--sub" />
          </div>
        </div>
      ))}
    </>
  );
}

/* ═══════════════════════════════════════
   MAIN PAGE
═══════════════════════════════════════ */
export default function AdminDashboard() {
  const [summary, setSummary]     = useState(null);
  const [logs, setLogs]           = useState([]);
  const [isLoadingSum, setLoadSum]= useState(true);
  const [isLoadingLog, setLoadLog]= useState(true);
  const [errorSum, setErrorSum]   = useState('');
  const [errorLog, setErrorLog]   = useState('');
  const { toasts, addToast, removeToast } = useToast();

  const fetchSummary = useCallback(async () => {
    setLoadSum(true); setErrorSum('');
    try {
      const data = await getDashboardSummary();
      setSummary(data);
    } catch {
      setErrorSum('Không thể tải số liệu tổng quan.');
    } finally {
      setLoadSum(false);
    }
  }, []);

  const fetchLogs = useCallback(async () => {
    setLoadLog(true); setErrorLog('');
    try {
      const data = await getAuditLog({ page: 0, size: 10 });
      setLogs(data?.content ?? []);
    } catch {
      setErrorLog('Không thể tải lịch sử hoạt động.');
    } finally {
      setLoadLog(false);
    }
  }, []);

  useEffect(() => { fetchSummary(); fetchLogs(); }, [fetchSummary, fetchLogs]);

  const statVariant = (systemStatus) => {
    if (systemStatus === 'MAINTENANCE') return 'new';
    if (systemStatus === 'ERROR')       return 'banned';
    return 'active';
  };

  return (
    <div className="adb-page">
      <AdminTopNav activeTab="admin-overview" />

      <AdminPageHeader
        chipIcon={<IcAdminChip />}
        chipLabel="Tổng quan"
        title="Bảng Điều Khiển"
        subtitle="Theo dõi hoạt động hệ thống theo thời gian thực"
        mascotVariant={isLoadingSum ? 'thinking' : 'happy'}
        mascotSize={100}
      />

      <main className="adb-body">

        {/* ── Stat row ── */}
        <section className="adb-stats" aria-label="Số liệu hệ thống">
          {isLoadingSum ? (
            <>
              <StatSkeleton /><StatSkeleton /><StatSkeleton /><StatSkeleton />
            </>
          ) : errorSum ? (
            <div className="adb-error-banner" role="alert">
              <span>{errorSum}</span>
              <button className="adb-retry-btn" onClick={fetchSummary}>Thử lại</button>
            </div>
          ) : (
            <>
              <StatCard
                icon={STAT_ICONS.total}
                value={summary?.totalUsers ?? 0}
                label="Tổng người dùng"
                variant="total"
              />
              <StatCard
                icon={STAT_ICONS.active}
                value={summary?.activeToday ?? 0}
                label="Hoạt động hôm nay"
                variant="active"
              />
              <StatCard
                icon={<IcChart />}
                value={summary?.quizAttemptsToday ?? 0}
                label="Bài thi / Quiz hôm nay"
                variant="new"
              />
              <div
                className={`stat-card stat-card--${statVariant(summary?.systemStatus)}`}
                aria-label={`Trạng thái hệ thống: ${summary?.systemStatus}`}
              >
                <div className="stat-card__icon"><IcSystemHealth /></div>
                <div className="stat-card__body">
                  <SystemStatusBadge status={summary?.systemStatus ?? 'OK'} />
                  <div className="stat-card__label">Trạng thái hệ thống</div>
                </div>
              </div>
            </>
          )}
        </section>

        {/* ── Content row ── */}
        <div className="adb-content-row">

          {/* Activity log */}
          <section className="adb-card adb-activity" aria-label="Hoạt động gần đây">
            <div className="adb-card-header">
              <h2 className="adb-card-title">Hoạt Động Gần Đây</h2>
              <span className="adb-reports-link adb-reports-link--disabled" title="Sắp có">
                Xem tất cả
              </span>
            </div>

            {isLoadingLog ? (
              <LogSkeleton />
            ) : errorLog ? (
              <div className="adb-error-banner" role="alert">
                <span>{errorLog}</span>
                <button className="adb-retry-btn" onClick={fetchLogs}>Thử lại</button>
              </div>
            ) : logs.length === 0 ? (
              <p className="adb-log-empty">Chưa có hoạt động nào được ghi nhận.</p>
            ) : (
              <ul className="adb-log-list" role="list">
                {logs.map((log) => {
                  const { icon, bg, color } = logStyle(log.actionType);
                  return (
                    <li key={log.logId} className="adb-log-item">
                      <div
                        className="adb-log-icon"
                        style={{ background: bg, color }}
                        aria-hidden="true"
                      >
                        {icon}
                      </div>
                      <div className="adb-log-content">
                        <span className="adb-log-action">{actionLabel(log)}</span>
                        <span className="adb-log-meta">
                          {log.adminEmail} · {relativeTime(log.createdAt)}
                        </span>
                      </div>
                    </li>
                  );
                })}
              </ul>
            )}
          </section>

          {/* Quick actions */}
          <nav className="adb-card adb-quick" aria-label="Truy cập nhanh">
            <h2 className="adb-card-title">Truy Cập Nhanh</h2>
            <div className="adb-quick-list">

              <QuickItem
                to="/admin/users"
                iconBg="var(--color-primary-bg)"
                iconColor="var(--color-primary)"
                icon={TAB_ICONS.student}
                label="Quản lý người dùng"
                desc="Xem và quản lý tài khoản"
              />
              <QuickItem
                to="/admin/settings?tab=email"
                iconBg="#E3F2FD"
                iconColor="#1565C0"
                icon={<IcMail />}
                label="Cài đặt SMTP"
                desc="Cấu hình email hệ thống"
              />
              <QuickItem
                to="/admin/settings?tab=security"
                iconBg="#F3E5F5"
                iconColor="#6A1B9A"
                icon={<IcShield />}
                label="Bảo mật"
                desc="Giới hạn đăng nhập, JWT"
              />
              <QuickItem
                to="/admin/settings?tab=notifications"
                iconBg="var(--color-accent-bg)"
                iconColor="#B45309"
                icon={<IcBell />}
                label="Thông báo tự động"
                desc="Quản lý quy tắc thông báo"
              />
              <QuickItem
                to="/admin/settings?tab=system"
                iconBg="#FFF3E0"
                iconColor="#F57C00"
                icon={<IcWrench />}
                label="Chế độ bảo trì"
                desc="Bật / tắt bảo trì hệ thống"
              />
            </div>
          </nav>

        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}

function QuickItem({ to, iconBg, iconColor, icon, label, desc }) {
  return (
    <Link to={to} className="adb-quick-item">
      <div className="adb-qi-icon" style={{ background: iconBg, color: iconColor }} aria-hidden="true">
        {icon}
      </div>
      <div className="adb-qi-text">
        <span className="adb-qi-label">{label}</span>
        <span className="adb-qi-desc">{desc}</span>
      </div>
      <span className="adb-qi-arrow" aria-hidden="true">›</span>
    </Link>
  );
}
