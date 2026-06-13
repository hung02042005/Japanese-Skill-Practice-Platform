import SakuChan from '../auth/SakuChan';
import './EmptyState.css';

/**
 * Props:
 *   title         — tiêu đề hiển thị
 *   subtitle      — mô tả gợi ý (optional)
 *   mascotVariant — variant truyền vào SakuChan (default 'thinking')
 *   mascotSize    — size SakuChan (default 104)
 */
export function EmptyState({
  title    = 'Không tìm thấy dữ liệu',
  subtitle = 'Thử thay đổi bộ lọc hoặc từ khoá tìm kiếm nhé 🌸',
  mascotVariant = 'thinking',
  mascotSize    = 104,
  children,
}) {
  return (
    <div className="empty-state">
      <SakuChan variant={mascotVariant} size={mascotSize} />
      <p className="empty-state__title">{title}</p>
      {subtitle && <p className="empty-state__sub">{subtitle}</p>}
      {children}
    </div>
  );
}
