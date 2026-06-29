import { StatCard } from './StatCard';
import { STAT_ICONS, IcBell } from './ManageUsersIcons';

function KpiSkeleton() {
  return <div className="adb-stat-skel" aria-hidden="true" />;
}

/**
 * Hàng KPI quản trị — dữ liệu giàu từ GET /admin/dashboard.
 * Props:
 *   data      — { newStudentsThisMonth, openTickets, inProgressTickets,
 *                 pendingSubmissions, suspendedStudents }
 *   isLoading — bool
 *   onRetry   — fn
 */
export function DashboardKpiRow({ data, isLoading, onRetry }) {
  if (isLoading) {
    return (
      <section className="adb-stats" aria-label="Chỉ số vận hành">
        <KpiSkeleton /><KpiSkeleton /><KpiSkeleton /><KpiSkeleton />
      </section>
    );
  }

  if (!data) {
    return (
      <section className="adb-stats">
        <div className="adb-error-banner" role="alert">
          <span>Không thể tải chỉ số vận hành.</span>
          <button className="adb-retry-btn" onClick={onRetry}>Thử lại</button>
        </div>
      </section>
    );
  }

  const openTickets = (data.openTickets ?? 0) + (data.inProgressTickets ?? 0);

  return (
    <section className="adb-stats" aria-label="Chỉ số vận hành">
      <StatCard
        icon={STAT_ICONS.total}
        value={data.newStudentsThisMonth ?? 0}
        label="Học viên mới (tháng này)"
        variant="new"
      />
      <StatCard
        icon={<IcBell />}
        value={openTickets}
        label="Ticket đang xử lý"
        variant="total"
      />
      <StatCard
        icon={STAT_ICONS.pending}
        value={data.pendingSubmissions ?? 0}
        label="Bài chờ chấm"
        variant="active"
      />
      <StatCard
        icon={STAT_ICONS.suspended}
        value={data.suspendedStudents ?? 0}
        label="Học viên bị đình chỉ"
        variant="banned"
      />
    </section>
  );
}
