import { Navigate, useLocation } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';

function StaffRoute({ children }) {
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const location = useLocation();
  if (!isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />;
  if (user?.role !== 'STAFF' && user?.role !== 'ADMIN') return <Navigate to="/dashboard" replace />;
  if (user?.role === 'STAFF' && user?.staffRole === 'staff_manager') return <Navigate to="/manager" replace />;
  return children;
}

export default StaffRoute;
