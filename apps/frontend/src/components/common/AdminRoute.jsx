import { Navigate, useLocation } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';

/**
 * AdminRoute — bảo vệ các route dành riêng cho ADMIN.
 * - Chưa đăng nhập          → redirect /login
 * - Đăng nhập nhưng không phải ADMIN → redirect /dashboard
 * - ADMIN hợp lệ            → render children
 */
function AdminRoute({ children }) {
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

export default AdminRoute;
