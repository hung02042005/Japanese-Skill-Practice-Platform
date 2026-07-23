import { Link, useNavigate } from 'react-router-dom';
import AppLogo from '@/shared/components/common/AppLogo';
import SakuChan from '@/shared/components/common/SakuChan';
import './Error.css';

export default function Forbidden() {
  const navigate = useNavigate();
  return (
    <div className="err-page">
      <div className="err-topbar">
        <Link to="/" aria-label="SakuJi — về trang chủ">
          <AppLogo size={28} />
        </Link>
      </div>
      <div className="err-content">
        <SakuChan variant="wrong" size={160} />
        <div className="err-code" aria-hidden="true">403</div>
        <h1 className="err-title">Không có quyền truy cập</h1>
        <p className="err-subtitle">Bạn không có quyền xem trang này.</p>
        <div className="err-actions">
          <button className="err-btn err-btn--ghost" onClick={() => navigate(-1)}>← Quay lại</button>
          <Link to="/dashboard" className="err-btn err-btn--primary">Về Dashboard</Link>
        </div>
      </div>
    </div>
  );
}
