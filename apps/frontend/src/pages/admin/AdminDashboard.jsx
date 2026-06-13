import { useState, useEffect, useCallback }       from 'react';
import AdminTopNav                                  from '../../components/layout/AdminTopNav';
import { AdminPageHeader }                          from '../../components/admin/AdminPageHeader';
import { ToastContainer, useToast }                from '../../components/common/Toast';
import { DashboardStatRow }                        from '../../components/admin/DashboardStatRow';
import { DashboardActivityLog }                    from '../../components/admin/DashboardActivityLog';
import { DashboardQuickActions }                   from '../../components/admin/DashboardQuickActions';
import { IcAdminChip }                             from '../../components/admin/ManageUsersIcons';
import { getDashboardSummary, getAuditLog }        from '../../api/adminService';
import './AdminDashboard.css';

export default function AdminDashboard() {
  const [summary,      setSummary]  = useState(null);
  const [logs,         setLogs]     = useState(null);
  const [isLoadingSum, setLoadSum]  = useState(true);
  const [isLoadingLog, setLoadLog]  = useState(true);
  const { toasts, addToast, removeToast } = useToast();

  const fetchSummary = useCallback(async () => {
    setLoadSum(true);
    try {
      setSummary(await getDashboardSummary());
    } catch {
      setSummary(null);
    } finally {
      setLoadSum(false);
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

  useEffect(() => { fetchSummary(); fetchLogs(); }, [fetchSummary, fetchLogs]);

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
        <DashboardStatRow
          summary={summary}
          isLoading={isLoadingSum}
          onRetry={fetchSummary}
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
