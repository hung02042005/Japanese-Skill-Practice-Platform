import { Navigate, useLocation } from 'react-router-dom';
import { useAppSelector } from '@/store/hooks';

function PrivateRoute({ children }) {
  const { isAuthenticated } = useAppSelector((state) => state.auth);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}

export default PrivateRoute;
