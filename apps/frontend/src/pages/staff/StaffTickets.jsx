import { useState, useEffect, useCallback } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { useToast, ToastContainer } from '../../components/common/Toast';
import TicketList from '../../components/staff/TicketList';
import TicketDetail from '../../components/staff/TicketDetail';
import StaffPageHero from '../../components/staff/StaffPageHero';
import { getTickets, getTicketDetail, replyTicket, closeTicket } from '../../api/staffService';
import './StaffTickets.css';

function toListItem(t) {
  return {
    id: t.ticketId,
    subject: t.subject,
    priority: t.priority,
    status: t.status,
    studentName: t.studentName,
    createdAt: t.createdAt,
    hasUnread: false,
  };
}

function toReplies(replies) {
  return (replies ?? []).map((r) => ({
    replyId: r.replyId,
    senderType: r.senderRole === 'STAFF' ? 'staff' : 'student',
    senderName: r.senderName,
    message: r.message,
    createdAt: r.createdAt,
  }));
}

export default function StaffTickets() {
  const [tickets, setTickets] = useState([]);
  const [detail, setDetail] = useState(null);
  const [selectedId, setSelected] = useState(null);
  const [statusFilter, setStatus] = useState('open');
  const [isLoadingList, setLoadingList] = useState(true);
  const { toasts, addToast, removeToast } = useToast();

  const loadTickets = useCallback(async () => {
    setLoadingList(true);
    try {
      const res = await getTickets({ status: statusFilter || undefined });
      setTickets((res.content ?? []).map(toListItem));
    } catch {
      addToast('error', 'Không thể tải danh sách ticket.');
    } finally {
      setLoadingList(false);
    }
  }, [statusFilter, addToast]);

  useEffect(() => {
    loadTickets();
  }, [loadTickets]);

  useEffect(() => {
    if (!selectedId) {
      setDetail(null);
      return;
    }
    let active = true;
    getTicketDetail(selectedId)
      .then((d) => {
        if (active) setDetail(d);
      })
      .catch(() => {
        if (active) addToast('error', 'Không thể tải chi tiết ticket.');
      });
    return () => {
      active = false;
    };
  }, [selectedId, addToast]);

  const selectedTicket = detail
    ? {
        id: detail.ticketId,
        subject: detail.subject,
        priority: detail.priority,
        status: detail.status,
        studentName: detail.studentName,
        jlptLevel: detail.jlptLevel ?? '—',
        createdAt: detail.createdAt,
      }
    : null;
  const selectedReplies = toReplies(detail?.replies);

  const handleReply = useCallback(
    async (message, newStatus) => {
      if (!selectedId) return;
      try {
        await replyTicket(selectedId, message);
        if (newStatus === 'closed' || newStatus === 'resolved') {
          await closeTicket(selectedId);
        }
        const refreshed = await getTicketDetail(selectedId);
        setDetail(refreshed);
        setTickets((prev) =>
          prev.map((t) => (t.id === selectedId ? toListItem(refreshed) : t)),
        );
        addToast('success', 'Đã gửi phản hồi!');
      } catch {
        addToast('error', 'Không thể gửi phản hồi. Vui lòng thử lại.');
      }
    },
    [selectedId, addToast],
  );

  const handleStatusChange = useCallback(
    async (newStatus) => {
      if (!selectedId) return;
      if (newStatus !== 'closed' && newStatus !== 'resolved') return;
      try {
        await closeTicket(selectedId);
        const refreshed = await getTicketDetail(selectedId);
        setDetail(refreshed);
        setTickets((prev) =>
          prev.map((t) => (t.id === selectedId ? toListItem(refreshed) : t)),
        );
        addToast('success', 'Đã cập nhật trạng thái');
      } catch {
        addToast('error', 'Không thể cập nhật trạng thái.');
      }
    },
    [selectedId, addToast],
  );

  return (
    <div className="tkt-page">
      <StaffTopNav activeTab="staff-tickets" />
      <main className="tkt-body">
        <StaffPageHero
          accent="green"
          title="Hỗ Trợ Học Viên"
          subtitle="Xử lý và phản hồi các yêu cầu hỗ trợ, câu hỏi từ học viên"
          icon={
            <svg width="40" height="40" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              {/* Hạc giấy origami (折り鶴) */}
              <path d="M4 30 Q16 16 24 22 Q32 16 44 30"/>
              <path d="M24 22 L22 32 L18 40"/>
              <path d="M24 22 Q28 13 33 8"/>
              <line x1="33" y1="8" x2="37" y2="5"/>
              <path d="M4 30 L8 35"/>
              <path d="M44 30 L40 35"/>
            </svg>
          }
        />
        {isLoadingList ? (
          <div className="tkt-empty-list">Đang tải...</div>
        ) : (
          <TicketList
            tickets={tickets}
            selectedId={selectedId}
            statusFilter={statusFilter}
            onSelect={setSelected}
            onStatusFilterChange={setStatus}
          />
        )}
        <TicketDetail
          ticket={selectedTicket}
          replies={selectedReplies}
          onReply={handleReply}
          onStatusChange={handleStatusChange}
        />
      </main>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  );
}
