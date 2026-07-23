// Tiện ích định dạng thời gian — dùng chung cho support/notification.

// "5 phút trước" / "3 giờ trước" / "2 ngày trước" / ngày tháng nếu xa hơn 7 ngày.
export function formatRelativeTime(isoStr) {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  if (Number.isNaN(d.getTime())) return '';
  const diff = Math.floor((Date.now() - d.getTime()) / 60000); // phút
  if (diff < 1) return 'vừa xong';
  if (diff < 60) return `${diff} phút trước`;
  if (diff < 1440) return `${Math.floor(diff / 60)} giờ trước`;
  if (diff < 10080) return `${Math.floor(diff / 1440)} ngày trước`;
  return d.toLocaleDateString('vi-VN');
}

// "01/06/2026 09:12"
export function formatDateTime(isoStr) {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  if (Number.isNaN(d.getTime())) return '';
  return `${d.toLocaleDateString('vi-VN')} ${d.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  })}`;
}
