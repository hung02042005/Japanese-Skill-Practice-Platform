import { useState, useCallback } from 'react';
import StaffTopNav from '../../components/layout/StaffTopNav';
import { useToast, ToastContainer } from '../../components/common/Toast';
import TicketList from '../../components/staff/TicketList';
import TicketDetail from '../../components/staff/TicketDetail';
import './StaffTickets.css';

const MOCK_TICKETS = [
  {
    id: 45,
    subject: 'Lỗi âm thanh trong bài luyện shadowing N3',
    priority: 'urgent',
    status: 'open',
    studentName: 'Nguyễn Văn A',
    jlptLevel: 'N3',
    createdAt: '2026-06-03T10:30:00',
    hasUnread: true,
  },
  {
    id: 44,
    subject: 'Không hiểu ngữ pháp ～ても trong bài N4',
    priority: 'high',
    status: 'in_progress',
    studentName: 'Trần Thị B',
    jlptLevel: 'N4',
    createdAt: '2026-06-02T14:20:00',
    hasUnread: false,
  },
  {
    id: 43,
    subject: 'Muốn biết cách luyện JLPT N2 hiệu quả nhất',
    priority: 'normal',
    status: 'open',
    studentName: 'Lê Văn C',
    jlptLevel: 'N2',
    createdAt: '2026-06-01T09:15:00',
    hasUnread: true,
  },
  {
    id: 42,
    subject: 'Câu hỏi về chính sách hoàn tiền gói VIP',
    priority: 'normal',
    status: 'resolved',
    studentName: 'Phạm Thị D',
    jlptLevel: 'N5',
    createdAt: '2026-05-31T16:00:00',
    hasUnread: false,
  },
  {
    id: 41,
    subject: 'Bài kiểm tra bị lỗi không nộp được',
    priority: 'high',
    status: 'open',
    studentName: 'Hoàng Minh E',
    jlptLevel: 'N4',
    createdAt: '2026-05-30T11:45:00',
    hasUnread: true,
  },
  {
    id: 40,
    subject: 'Yêu cầu cấp chứng chỉ hoàn thành khóa học N5',
    priority: 'low',
    status: 'closed',
    studentName: 'Vũ Thị F',
    jlptLevel: 'N5',
    createdAt: '2026-05-29T08:30:00',
    hasUnread: false,
  },
];

const MOCK_REPLIES_MAP = {
  45: [
    {
      replyId: 1,
      senderType: 'student',
      senderName: 'Nguyễn Văn A',
      message:
        'Xin chào, tôi đang luyện bài shadowing N3 nhưng file âm thanh không phát được. Tôi đã thử refresh trang nhiều lần nhưng vẫn không được. Bạn có thể giúp tôi không?',
      createdAt: '2026-06-03T10:30:00',
    },
    {
      replyId: 2,
      senderType: 'staff',
      senderName: 'Staff Minh',
      message:
        'Chào bạn! Cảm ơn bạn đã liên hệ. Chúng tôi đang kiểm tra vấn đề này. Bạn có thể thử xóa cache trình duyệt rồi reload lại không? Nếu vẫn lỗi, bạn đang dùng trình duyệt gì vậy?',
      createdAt: '2026-06-03T11:00:00',
    },
    {
      replyId: 3,
      senderType: 'student',
      senderName: 'Nguyễn Văn A',
      message:
        'Tôi đã xóa cache nhưng vẫn không được. Tôi đang dùng Chrome phiên bản mới nhất trên Windows 11.',
      createdAt: '2026-06-03T11:15:00',
    },
  ],
  44: [
    {
      replyId: 4,
      senderType: 'student',
      senderName: 'Trần Thị B',
      message:
        'Chào staff, trong bài N4 có mẫu ngữ pháp ～ても nhưng tôi không hiểu cách dùng khi nào dùng ～ても và khi nào dùng ～たとしても. Bạn có thể giải thích không?',
      createdAt: '2026-06-02T14:20:00',
    },
    {
      replyId: 5,
      senderType: 'staff',
      senderName: 'Staff Lan',
      message:
        '～ても dùng khi "dù X đi nữa thì vẫn Y" trong tình huống thực tế. ～たとしても mạnh hơn, dùng cho giả định "kể cả nếu X (dù khó xảy ra) thì vẫn Y". Hy vọng giúp ích được bạn!',
      createdAt: '2026-06-02T15:00:00',
    },
  ],
  43: [
    {
      replyId: 6,
      senderType: 'student',
      senderName: 'Lê Văn C',
      message:
        'Xin chào, tôi đang học N3 được 6 tháng và muốn lên N2 trong 1 năm. Bạn có thể chia sẻ lộ trình học hiệu quả không?',
      createdAt: '2026-06-01T09:15:00',
    },
  ],
  42: [
    {
      replyId: 7,
      senderType: 'student',
      senderName: 'Phạm Thị D',
      message: 'Chào, tôi muốn hỏi về chính sách hoàn tiền nếu tôi huỷ gói VIP sau 1 tuần?',
      createdAt: '2026-05-31T16:00:00',
    },
    {
      replyId: 8,
      senderType: 'staff',
      senderName: 'Staff Minh',
      message:
        'Chào bạn! Theo chính sách của SakuJi, bạn có thể hoàn tiền 100% trong vòng 7 ngày đầu nếu chưa sử dụng quá 30% nội dung VIP. Bạn muốn tiến hành hoàn tiền không?',
      createdAt: '2026-05-31T16:30:00',
    },
    {
      replyId: 9,
      senderType: 'student',
      senderName: 'Phạm Thị D',
      message: 'Vâng tôi hiểu rồi, cảm ơn bạn. Tôi sẽ tiếp tục dùng!',
      createdAt: '2026-05-31T17:00:00',
    },
  ],
};

export default function StaffTickets() {
  const [tickets, setTickets] = useState(MOCK_TICKETS);
  const [repliesMap, setRepliesMap] = useState(MOCK_REPLIES_MAP);
  const [selectedId, setSelected] = useState(null);
  const [statusFilter, setStatus] = useState('open');
  const { toasts, addToast, removeToast } = useToast();

  const filtered = statusFilter
    ? tickets.filter((t) => t.status === statusFilter)
    : tickets;

  const selectedTicket = tickets.find((t) => t.id === selectedId) ?? null;
  const selectedReplies = repliesMap[selectedId] ?? [];

  const handleReply = useCallback(
    (message, newStatus) => {
      const newReply = {
        replyId: Date.now(),
        senderType: 'staff',
        senderName: 'Staff (Bạn)',
        message,
        createdAt: new Date().toISOString(),
      };

      setRepliesMap((prev) => ({
        ...prev,
        [selectedId]: [...(prev[selectedId] ?? []), newReply],
      }));

      if (newStatus) {
        setTickets((prev) =>
          prev.map((t) =>
            t.id === selectedId ? { ...t, status: newStatus, hasUnread: false } : t
          )
        );
      } else {
        setTickets((prev) =>
          prev.map((t) =>
            t.id === selectedId ? { ...t, hasUnread: false } : t
          )
        );
      }

      addToast({ type: 'success', message: 'Đã gửi phản hồi!' });
    },
    [selectedId, addToast]
  );

  const handleStatusChange = useCallback(
    (newStatus) => {
      setTickets((prev) =>
        prev.map((t) =>
          t.id === selectedId ? { ...t, status: newStatus } : t
        )
      );
      addToast({ type: 'success', message: 'Đã cập nhật trạng thái' });
    },
    [selectedId, addToast]
  );

  return (
    <div className="tkt-page">
      <StaffTopNav activeTab="staff-tickets" />
      <main className="tkt-body">
        <TicketList
          tickets={filtered}
          selectedId={selectedId}
          statusFilter={statusFilter}
          onSelect={setSelected}
          onStatusFilterChange={setStatus}
        />
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
