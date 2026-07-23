import { useState, useEffect, useCallback }       from 'react';
import AdminTopNav                                  from '@/shared/components/layout/AdminTopNav';
import { AdminPageHeader }                          from '@/features/management/components/admin/AdminPageHeader';
import { ToastContainer, useToast }                from '@/shared/components/common/Toast';
import { DashboardStatRow }                        from '@/features/management/components/admin/DashboardStatRow';
import { DashboardKpiRow }                         from '@/features/management/components/admin/DashboardKpiRow';
import { DashboardActivityLog }                    from '@/features/management/components/admin/DashboardActivityLog';
import { DashboardQuickActions }                   from '@/features/management/components/admin/DashboardQuickActions';
import { IcAdminChip }                             from '@/features/management/components/admin/ManageUsersIcons';
import { getDashboardOverview, getAuditLog } from '@/shared/api/adminService';
import './AdminDashboard.css';

export default function AdminDashboard() {
  const [overview,      setOverview]  = useState(null);
  const [logs,          setLogs]      = useState(null);
  const [isLoadingOv,   setLoadOv]    = useState(true);
  const [isLoadingLog,  setLoadLog]   = useState(true);
  const { toasts, removeToast } = useToast();

  const fetchOverview = useCallback(async () => {
    setLoadOv(true);
    try {
      setOverview(await getDashboardOverview());
    } catch {
      setOverview(null);
    } finally {
      setLoadOv(false);
    }
  }, []);

  const fetchLogs = useCallback(async () => {
    setLoadLog(true);
    try {
      const data = await getAuditLog({ page: 0, size: 10 });
      setLogs(data?.content ?? []);
    } catch {
      setLogs(null);
    } finally {
      setLoadLog(false);
    }
  }, []);

  useEffect(() => { fetchOverview(); fetchLogs(); }, [fetchOverview, fetchLogs]);

  return (
    <div className="adb-page">
      <AdminTopNav activeTab="admin-overview" />

      <AdminPageHeader
        chipIcon={<IcAdminChip />}
        chipLabel="Tổng quan"
        title="Bảng Điều Khiển"
        subtitle="Theo dõi hoạt động hệ thống theo thời gian thực"
        mascotVariant={isLoadingOv ? 'thinking' : 'happy'}
        mascotSize={100}
      />

      <main className="adb-body">
        <DashboardStatRow
          summary={overview?.summary}
          isLoading={isLoadingOv}
          onRetry={fetchOverview}
        />

        <DashboardKpiRow
          data={overview?.kpi}
          isLoading={isLoadingOv}
          onRetry={fetchOverview}
        />

        <div className="adb-content-row">
          <DashboardActivityLog
            logs={logs}
            isLoading={isLoadingLog}
            onRetry={fetchLogs}
          />
          <DashboardQuickActions />
        </div>
      </main>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
