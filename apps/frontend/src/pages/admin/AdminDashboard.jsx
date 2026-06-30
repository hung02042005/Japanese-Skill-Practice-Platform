import { useState, useEffect, useCallback }       from 'react';
import AdminTopNav                                  from '../../components/layout/AdminTopNav';
import { AdminPageHeader }                          from '../../components/admin/AdminPageHeader';
import { ToastContainer, useToast }                from '../../components/common/Toast';
import { DashboardStatRow }                        from '../../components/admin/DashboardStatRow';
import { DashboardKpiRow }                         from '../../components/admin/DashboardKpiRow';
import { DashboardActivityLog }                    from '../../components/admin/DashboardActivityLog';
import { DashboardQuickActions }                   from '../../components/admin/DashboardQuickActions';
import { IcAdminChip }                             from '../../components/admin/ManageUsersIcons';
import { getDashboardOverview, getAuditLog } from '../../api/adminService';
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
