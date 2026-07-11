import { Navigate, useLocation } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';

// UI-only guard: chỉ để định tuyến/UX đúng. Đây KHÔNG phải lớp bảo mật — nếu
// user.role/staffRole trong localStorage bị chỉnh sửa thủ công, request thật
// vẫn bị backend chặn 401/403 qua @PreAuthorize/hasRole trong SecurityConfig.java.
function ManagerRoute({ children }) {
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const location = useLocation();
  if (!isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />;
  if (user?.role === 'ADMIN') return children;
  if (user?.role !== 'STAFF' || user?.staffRole !== 'staff_manager') {
    return <Navigate to="/staff" replace />;
  }
  return children;
}

export default ManagerRoute;
