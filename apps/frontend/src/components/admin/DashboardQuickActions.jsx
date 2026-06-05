import { Link } from 'react-router-dom';
import { IcMail, IcShield, IcWrench, IcBell, TAB_ICONS } from './ManageUsersIcons';

const QUICK_ITEMS = [
  {
    to:       '/admin/reports',
    iconBg:   '#E8F5E9',
    iconColor:'#2E7D32',
    icon:     (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <line x1="3" y1="20" x2="21" y2="20"/><rect x="4" y="13" width="4" height="7" rx="1"/><rect x="10" y="7" width="4" height="13" rx="1"/><rect x="16" y="10" width="4" height="10" rx="1"/>
      </svg>
    ),
    label:    'Báo cáo',
    desc:     'Dữ liệu hệ thống & nội dung',
  },
  {
    to:       '/admin/users',
    iconBg:   'var(--color-primary-bg)',
    iconColor:'var(--color-primary)',
    icon:     TAB_ICONS.student,
    label:    'Quản lý người dùng',
    desc:     'Xem và quản lý tài khoản',
  },
  {
    to:       '/admin/settings?tab=email',
    iconBg:   '#E3F2FD',
    iconColor:'#1565C0',
    icon:     <IcMail />,
    label:    'Cài đặt SMTP',
    desc:     'Cấu hình email hệ thống',
  },
  {
    to:       '/admin/settings?tab=security',
    iconBg:   '#F3E5F5',
    iconColor:'#6A1B9A',
    icon:     <IcShield />,
    label:    'Bảo mật',
    desc:     'Giới hạn đăng nhập, JWT',
  },
  {
    to:       '/admin/settings?tab=notifications',
    iconBg:   'var(--color-accent-bg)',
    iconColor:'#B45309',
    icon:     <IcBell />,
    label:    'Thông báo tự động',
    desc:     'Quản lý quy tắc thông báo',
  },
  {
    to:       '/admin/settings?tab=system',
    iconBg:   '#FFF3E0',
    iconColor:'#F57C00',
    icon:     <IcWrench />,
    label:    'Chế độ bảo trì',
    desc:     'Bật / tắt bảo trì hệ thống',
  },
];

export function DashboardQuickActions() {
  return (
    <nav className="adb-card adb-quick" aria-label="Truy cập nhanh">
      <h2 className="adb-card-title">Truy Cập Nhanh</h2>
      <div className="adb-quick-list">
        {QUICK_ITEMS.map((item) => (
          <Link key={item.to} to={item.to} className="adb-quick-item">
            <div
              className="adb-qi-icon"
              style={{ background: item.iconBg, color: item.iconColor }}
              aria-hidden="true"
            >
              {item.icon}
            </div>
            <div className="adb-qi-text">
              <span className="adb-qi-label">{item.label}</span>
              <span className="adb-qi-desc">{item.desc}</span>
            </div>
            <span className="adb-qi-arrow" aria-hidden="true">›</span>
          </Link>
        ))}
      </div>
    </nav>
  );
}
