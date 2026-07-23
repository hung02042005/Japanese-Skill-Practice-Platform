import { Link, useNavigate } from 'react-router-dom';
import AppLogo from '@/shared/components/common/AppLogo';
import SakuChan from '@/shared/components/common/SakuChan';
import './Error.css';

export default function NotFound() {
  const navigate = useNavigate();
  return (
    <div className="err-page">
      <div className="err-topbar">
        <Link to="/" aria-label="SakuJi — về trang chủ">
          <AppLogo size={28} />
        </Link>
      </div>
      <div className="err-content">
        <SakuChan variant="thinking" size={160} />
        <div className="err-code" aria-hidden="true">404</div>
        <h1 className="err-title">Trang không tìm thấy</h1>
        <p className="err-subtitle">Trang bạn tìm không tồn tại hoặc đã bị di chuyển.</p>
        <div className="err-actions">
          <button className="err-btn err-btn--ghost" onClick={() => navigate(-1)}>← Quay lại</button>
          <Link to="/dashboard" className="err-btn err-btn--primary">Về Dashboard</Link>
        </div>
      </div>
    </div>
  );
}
