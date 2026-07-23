import { StatCard } from './StatCard';
import { IcChart, IcSystemHealth, STAT_ICONS } from './ManageUsersIcons';

function StatSkeleton() {
  return <div className="adb-stat-skel" aria-hidden="true" />;
}

function SystemStatusBadge({ status }) {
  const cfg = {
    OK:          { label: 'Bình thường', cls: 'adb-status--ok'   },
    MAINTENANCE: { label: 'Bảo trì',     cls: 'adb-status--warn'  },
    ERROR:       { label: 'Sự cố',       cls: 'adb-status--err'   },
  }[status] ?? { label: status, cls: '' };
  return <span className={`adb-status-badge ${cfg.cls}`}>{cfg.label}</span>;
}

function statVariant(systemStatus) {
  if (systemStatus === 'MAINTENANCE') return 'new';
  if (systemStatus === 'ERROR')       return 'banned';
  return 'active';
}

/**
 * Props:
 *   summary   — { totalUsers, activeToday, quizAttemptsToday, systemStatus }
 *   isLoading — bool
 *   onRetry   — fn
 */
export function DashboardStatRow({ summary, isLoading, onRetry }) {
  if (isLoading) {
    return (
      <section className="adb-stats" aria-label="Số liệu hệ thống">
        <StatSkeleton /><StatSkeleton /><StatSkeleton /><StatSkeleton />
      </section>
    );
  }

  if (!summary) {
    return (
      <section className="adb-stats">
        <div className="adb-error-banner" role="alert">
          <span>Không thể tải số liệu tổng quan.</span>
          <button className="adb-retry-btn" onClick={onRetry}>Thử lại</button>
        </div>
      </section>
    );
  }

  return (
    <section className="adb-stats" aria-label="Số liệu hệ thống">
      <StatCard
        icon={STAT_ICONS.total}
        value={summary.totalUsers ?? 0}
        label="Tổng người dùng"
        variant="total"
      />
      <StatCard
        icon={STAT_ICONS.active}
        value={summary.activeToday ?? 0}
        label="Hoạt động hôm nay"
        variant="active"
      />
      <StatCard
        icon={<IcChart />}
        value={summary.quizAttemptsToday ?? 0}
        label="Bài thi / Quiz hôm nay"
        variant="new"
      />
      <div
        className={`stat-card stat-card--${statVariant(summary.systemStatus)}`}
        aria-label={`Trạng thái hệ thống: ${summary.systemStatus}`}
      >
        <div className="stat-card__icon"><IcSystemHealth /></div>
        <div className="stat-card__body">
          <SystemStatusBadge status={summary.systemStatus ?? 'OK'} />
          <div className="stat-card__label">Trạng thái hệ thống</div>
        </div>
      </div>
    </section>
  );
}
