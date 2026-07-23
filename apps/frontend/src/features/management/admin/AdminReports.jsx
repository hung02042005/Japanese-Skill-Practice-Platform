import { useState, useEffect, useCallback } from 'react';
import { getAuditLog } from '@/shared/api/adminService';
import AdminTopNav from '@/shared/components/layout/AdminTopNav';
import { AdminPageHeader } from '@/features/management/components/admin/AdminPageHeader';
import { Pagination } from '@/shared/components/common/Pagination';
import { EmptyState } from '@/shared/components/common/EmptyState';
import { IcAdminChip } from '@/features/management/components/admin/ManageUsersIcons';
import {
  ACTION_GROUPS,
  getActionLabel,
  getActionColors,
  getRoleMeta,
} from '@/shared/utils/auditMeta';
import './AdminReports.css';

const PAGE_SIZE = 10;

// ─── Sub-components ───────────────────────────────────────────────────────────

function ActionChip({ actionType }) {
  const { bg, color } = getActionColors(actionType);
  return <span className="rp-type-chip" style={{ background: bg, color }}>{getActionLabel(actionType)}</span>;
}

function RoleChip({ role }) {
  const { label, bg, color } = getRoleMeta(role);
  return <span className="rp-type-chip" style={{ background: bg, color }}>{label}</span>;
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function AdminReports() {
  const [logs,         setLogs]        = useState([]);
  const [totalPages,   setTotalPages]  = useState(1);
  const [page,         setPage]        = useState(1);
  const [isLoading,    setLoading]     = useState(true);
  const [hasError,     setHasError]    = useState(false);
  const [actionFilter, setActionFilter] = useState('');

  const fetchPage = useCallback((p, action) => {
    setLoading(true);
    setHasError(false);
    getAuditLog({ page: p - 1, size: PAGE_SIZE, action: action || undefined })
      .then((data) => {
        setLogs(data?.content ?? []);
        setTotalPages(data?.totalPages ?? 1);
      })
      .catch(() => setHasError(true))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchPage(page, actionFilter); }, [fetchPage, page, actionFilter]);

  function handleAction(val) {
    setActionFilter(val);
    setPage(1);
  }

  return (
    <div className="rp-page">
      <AdminTopNav activeTab="reports" />

      <AdminPageHeader
        chipIcon={<IcAdminChip />}
        chipLabel="Báo cáo"
        title="Lịch Sử Hành Động"
        subtitle="Nhật ký kiểm toán mọi thao tác của Admin, Manager và Staff trên hệ thống"
        mascotVariant="happy"
        mascotSize={100}
      />

      <main className="rp-body">
        <div className="rp-panel">
          <div className="rp-tab-body">
            <div className="rp-tab-toolbar">
              <div className="rp-filter-bar">
                <select
                  className="rp-select"
                  value={actionFilter}
                  onChange={(e) => handleAction(e.target.value)}
                  aria-label="Lọc theo hành động"
                >
                  <option value="">Hành động: Tất cả</option>
                  {ACTION_GROUPS.map((group) => (
                    <optgroup key={group.label} label={group.label}>
                      {group.actions.map((a) => (
                        <option key={a} value={a}>{getActionLabel(a)}</option>
                      ))}
                    </optgroup>
                  ))}
                </select>
              </div>
              <span className="rp-count">
                {isLoading ? '...' : `${logs.length} bản ghi (trang ${page}/${totalPages})`}
              </span>
            </div>

            {hasError ? (
              <div className="rp-error-state" role="alert">
                <p>Không thể tải lịch sử hoạt động.</p>
                <button type="button" className="rp-retry-btn" onClick={() => fetchPage(page, actionFilter)}>
                  Thử lại
                </button>
              </div>
            ) : isLoading ? (
              <div className="rp-loading" aria-live="polite">Đang tải lịch sử hoạt động...</div>
            ) : logs.length === 0 ? (
              <EmptyState title="Không có kết quả" subtitle="Thử thay đổi bộ lọc." mascotVariant="thinking" mascotSize={90} />
            ) : (
              <>
                <div className="rp-table-wrap">
                  <table className="rp-table">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Vai trò</th>
                        <th>Hành động</th>
                        <th>Chi tiết</th>
                        <th>Người thực hiện</th>
                        <th>Thời gian</th>
                      </tr>
                    </thead>
                    <tbody>
                      {logs.map((log, idx) => (
                        <tr key={log.logId}>
                          <td className="rp-td-num">{(page - 1) * PAGE_SIZE + idx + 1}</td>
                          <td><RoleChip role={log.actorRole} /></td>
                          <td><ActionChip actionType={log.actionType} /></td>
                          <td className="rp-td-target">
                            {log.targetEmail || log.description || <span className="rp-none">—</span>}
                          </td>
                          <td className="rp-td-admin">
                            {log.actorName || log.adminEmail || <span className="rp-none">—</span>}
                            {log.actorName && log.adminEmail && (
                              <span className="rp-uploader-email"> {log.adminEmail}</span>
                            )}
                          </td>
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
        </div>
      </main>
    </div>
  );
}
