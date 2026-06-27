import { useState, useEffect, useCallback } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import StaffPageHero from '../../components/staff/StaffPageHero';
import TicketList from '../../components/staff/TicketList';
import TicketDetail from '../../components/staff/TicketDetail';
import { useToast, ToastContainer } from '../../components/common/Toast';
import { Pagination } from '../../components/common/Pagination';
import { getTickets, getTicketDetail, replyTicket, closeTicket } from '../../api/staffService';
import './StaffTickets.css';

const TABS = [
  { value: '', label: 'Tất cả' },
  { value: 'open', label: 'Chưa giao' },
  { value: 'assigned', label: 'Đã giao' },
  { value: 'in_progress', label: 'Đang xử lý' },
  { value: 'resolved', label: 'Đã giải quyết' },
];
const PAGE_SIZE = 20;

export default function StaffTickets() {
  const { toasts, addToast, removeToast } = useToast();
  const [tickets, setTickets] = useState([]);
  const [selectedId, setSelected] = useState(null);
  const [detail, setDetail] = useState(null);
  const [statusFilter, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotal] = useState(1);
  const [isLoadingList, setLoadList] = useState(true);
  const [isLoadingDtl, setLoadDtl] = useState(false);
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
    } finally {
      setLoadList(false);
    }
  }, [statusFilter, page, addToast]);

  useEffect(() => { loadList(); }, [loadList]);

  const openDetail = useCallback(async (ticketId) => {
    setSelected(ticketId);
    setLoadDtl(true);
    try {
      setDetail(await getTicketDetail(ticketId));
    } catch {
      addToast('error', 'Không thể tải chi tiết ticket.');
      setDetail(null);
    } finally {
      setLoadDtl(false);
    }
  }, [addToast]);

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
      if (err?.response?.status === 409) openDetail(selectedId);
      return false;
    } finally {
      setSending(false);
    }
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
    } finally {
      setClosing(false);
    }
  }

  function changeFilter(value) { setStatus(value); setPage(0); setSelected(null); setDetail(null); }

  return (
    <div className="tkt-page">
      <StaffTopNav activeTab="staff-tickets" />
      <main className="tkt-shell">
        <StaffPageHero
          accent="green"
          title="Hỗ Trợ Học Viên"
          subtitle="Phản hồi các ticket được giao cho bạn và đóng khi đã xử lý xong"
          icon={<svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M4 30 Q16 16 24 22 Q32 16 44 30"/><path d="M24 22 L22 32 L18 40"/><path d="M24 22 Q28 13 33 8"/><line x1="33" y1="8" x2="37" y2="5"/></svg>}
        />
        <div className="tkt-body">
          <div className="tkt-list-wrap">
            <TicketList
              tickets={tickets}
              selectedId={selectedId}
              statusFilter={statusFilter}
              tabs={TABS}
              onSelect={openDetail}
              onStatusFilterChange={changeFilter}
              isLoading={isLoadingList}
            />
            <Pagination currentPage={page + 1} totalPages={totalPages} onChange={(p) => setPage(p - 1)} />
          </div>
          <TicketDetail
            detail={detail}
            isLoading={isLoadingDtl}
            isSending={isSending}
            isClosing={isClosing}
            onReply={handleReply}
            onClose={handleClose}
          />
        </div>
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
