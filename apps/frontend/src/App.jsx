import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/login/Login';
import ForgotPassword from './pages/forgot-password/ForgotPassword';
import ResetPassword from './pages/forgot-password/ResetPassword';
import Register from './pages/register/Register';
import Home from './pages/home/Home';
import Dashboard from './pages/dashboard/Dashboard';
import VerifyEmail from './pages/verify-email/VerifyEmail';
import ManageUsers from './pages/admin/ManageUsers';
import PrivateRoute from './components/common/PrivateRoute';
import AdminRoute from './components/common/AdminRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/verify-email" element={<VerifyEmail />} />

        {/* Protected — Student */}
        <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />

        {/* Protected — Admin */}
        <Route path="/admin/users" element={<AdminRoute><ManageUsers /></AdminRoute>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;