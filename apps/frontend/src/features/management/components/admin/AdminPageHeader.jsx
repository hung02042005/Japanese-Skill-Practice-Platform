import SakuChan from '@/shared/components/common/SakuChan';
import './AdminPageHeader.css';

function PetalDecoration() {
  return (
    <div className="admin-header__petals" aria-hidden="true">
      <svg className="header-petal header-petal--1" width="32" height="32" viewBox="0 0 32 32"><ellipse cx="16" cy="16" rx="7" ry="14" fill="#E8637A" opacity="0.10" transform="rotate(-20 16 16)"/><ellipse cx="16" cy="16" rx="7" ry="14" fill="#F4A7B3" opacity="0.08" transform="rotate(20 16 16)"/></svg>
      <svg className="header-petal header-petal--2" width="22" height="22" viewBox="0 0 22 22"><ellipse cx="11" cy="11" rx="5" ry="10" fill="#F4A7B3" opacity="0.14" transform="rotate(15 11 11)"/></svg>
      <svg className="header-petal header-petal--3" width="26" height="26" viewBox="0 0 26 26"><ellipse cx="13" cy="13" rx="6" ry="12" fill="#E8637A" opacity="0.09" transform="rotate(-30 13 13)"/></svg>
      <svg className="header-petal header-petal--4" width="18" height="18" viewBox="0 0 18 18"><ellipse cx="9" cy="9" rx="4" ry="8" fill="#F4A7B3" opacity="0.16" transform="rotate(10 9 9)"/></svg>
      <svg className="header-petal header-petal--5" width="14" height="14" viewBox="0 0 14 14"><ellipse cx="7" cy="7" rx="3" ry="6" fill="#E8637A" opacity="0.12" transform="rotate(-45 7 7)"/></svg>
    </div>
  );
}

/**
 * Props:
 *   chipIcon     — ReactNode / SVG cho chip nhỏ ở đầu
 *   chipLabel    — text chip
 *   title        — tiêu đề trang
 *   subtitle     — mô tả ngắn
 *   mascotVariant — variant SakuChan (default 'happy')
 *   mascotSize   — px (default 100)
 */
export function AdminPageHeader({
  chipIcon,
  chipLabel,
  title,
  subtitle,
  mascotVariant = 'happy',
  mascotSize    = 100,
}) {
  return (
    <div className="admin-header">
      <PetalDecoration />
      <div className="admin-header__inner">
        <div className="admin-header__text">
          {(chipIcon || chipLabel) && (
            <span className="admin-header__chip">
              {chipIcon}
              {chipLabel}
            </span>
          )}
          {title    && <h1 className="admin-header__title">{title}</h1>}
          {subtitle && <p  className="admin-header__sub">{subtitle}</p>}
        </div>
        <div className="admin-header__mascot" aria-hidden="true">
          <SakuChan variant={mascotVariant} size={mascotSize} />
        </div>
      </div>
    </div>
  );
}
