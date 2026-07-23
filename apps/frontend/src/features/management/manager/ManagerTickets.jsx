import { useState, useEffect, useCallback } from 'react';
import ManagerTopNav from '@/shared/components/layout/ManagerTopNav';
import StaffPageHero from '@/features/management/components/staff/StaffPageHero';
import TicketList from '@/features/management/components/staff/TicketList';
import TicketDetail from '@/features/management/components/staff/TicketDetail';
import AssignPanel from '@/features/management/components/manager/AssignPanel';
import { useToast, ToastContainer } from '@/shared/components/common/Toast';
import { Pagination } from '@/shared/components/common/Pagination';
import { getTickets, getTicketDetail, replyTicket, closeTicket } from '@/shared/api/staffService';
import { assignTicket, getAssignableStaff } from '@/shared/api/managerService';
import './ManagerTickets.css';

const TABS = [
  { value: 'open', label: 'Chưa giao' },
  { value: 'assigned', label: 'Đã giao' },
  { value: 'in_progress', label: 'Đang xử lý' },
  { value: '', label: 'Tất cả' },
];
const PAGE_SIZE = 20;

export default function ManagerTickets() {
  const { toasts, addToast, removeToast } = useToast();
  const [tickets, setTickets] = useState([]);
  const [staffList, setStaffList] = useState([]);
  const [staffError, setStaffError] = useState('');
  const [selectedId, setSelected] = useState(null);
  const [detail, setDetail] = useState(null);
  const [statusFilter, setStatus] = useState('open');
  const [page, setPage] = useState(0);
  const [totalPages, setTotal] = useState(1);
  const [isLoadingList, setLoadList] = useState(true);
  const [isLoadingDtl, setLoadDtl] = useState(false);
  const [isAssigning, setAssigning] = useState(false);
  const [isSending, setSending] = useState(false);
  const [isClosing, setClosing] = useState(false);

  const loadList = useCallback(async () => {
    setLoadList(true);
    try {
      const data = await getTickets({ status: statusFilter, page, size: PAGE_SIZE });
      setTickets(data?.content ?? []);
      setTotal(data?.totalPages ?? 1);
    } catch {
      addToast('error', 'Không thể tải danh sách ticket.');
    } finally { setLoadList(false); }
  }, [statusFilter, page, addToast]);

  const loadStaff = useCallback(async () => {
    try { setStaffList(await getAssignableStaff()); setStaffError(''); }
    catch { setStaffError('Chưa tải được danh sách nhân viên.'); }
  }, []);

  useEffect(() => { loadList(); }, [loadList]);
  useEffect(() => { loadStaff(); }, [loadStaff]);

  const openDetail = useCallback(async (ticketId) => {
    setSelected(ticketId);
    setLoadDtl(true);
    try { setDetail(await getTicketDetail(ticketId)); }
    catch { addToast('error', 'Không thể tải chi tiết ticket.'); setDetail(null); }
    finally { setLoadDtl(false); }
  }, [addToast]);

  async function handleAssign(staffId) {
    setAssigning(true);
    try {
      const t = await assignTicket(selectedId, staffId);
      setDetail((d) => ({ ...d, status: t.status, assignedToStaffId: t.assignedToStaffId, assignedToStaffName: t.assignedToStaffName }));
      addToast('success', `Đã giao ticket cho ${t.assignedToStaffName}`);
      loadList(); loadStaff();
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Không thể phân công ticket.');
    } finally { setAssigning(false); }
  }

  async function handleReply(message, attachmentUrl) {
    setSending(true);
    try {
      const reply = await replyTicket(selectedId, { message, attachmentUrl });
      setDetail((d) => ({ ...d, status: d.status === 'open' || d.status === 'assigned' ? 'in_progress' : d.status, replies: [...(d.replies ?? []), reply] }));
      loadList();
      addToast('success', 'Đã gửi phản hồi');
      return true;
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Không thể gửi phản hồi.');
      return false;
    } finally { setSending(false); }
  }

  async function handleClose() {
    setClosing(true);
    try {
      const updated = await closeTicket(selectedId);
      setDetail((d) => ({ ...d, status: updated.status }));
      loadList();
      addToast('success', 'Ticket đã được đóng');
    } catch (err) {
      addToast('error', err?.response?.data?.message ?? 'Không thể đóng ticket.');
    } finally { setClosing(false); }
  }

  function changeFilter(value) { setStatus(value); setPage(0); setSelected(null); setDetail(null); }

  return (
    <div className="mtk-page">
      <ManagerTopNav activeTab="manager-tickets" />
      <main className="mtk-shell">
        <StaffPageHero
          accent="pink"
          title="Phân Công Ticket"
          subtitle="Duyệt yêu cầu hỗ trợ từ học viên và giao cho nhân viên xử lý"
          icon={<svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="20" y="6" width="8" height="10" rx="3"/><rect x="12" y="26" width="24" height="16" rx="3"/><line x1="24" y1="16" x2="24" y2="26"/></svg>}
        />
        <div className="mtk-body">
          <div className="mtk-list-wrap">
            <TicketList tickets={tickets} selectedId={selectedId} statusFilter={statusFilter} tabs={TABS} onSelect={openDetail} onStatusFilterChange={changeFilter} isLoading={isLoadingList} />
            <Pagination currentPage={page + 1} totalPages={totalPages} onChange={(p) => setPage(p - 1)} />
          </div>
          <TicketDetail
            detail={detail}
            isLoading={isLoadingDtl}
            isSending={isSending}
            isClosing={isClosing}
            onReply={handleReply}
            onClose={handleClose}
            headerExtra={detail && <AssignPanel detail={detail} staffList={staffList} staffError={staffError} isAssigning={isAssigning} onAssign={handleAssign} />}
          />
        </div>
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
