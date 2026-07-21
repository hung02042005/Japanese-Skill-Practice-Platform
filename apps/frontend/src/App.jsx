import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/login/Login';
import ForgotPassword from './pages/forgot-password/ForgotPassword';
import ResetPassword from './pages/forgot-password/ResetPassword';
import StaffForgotPassword from './pages/forgot-password/StaffForgotPassword';
import StaffChangeTempPassword from './pages/forgot-password/StaffChangeTempPassword';
import StaffSetupPassword from './pages/forgot-password/StaffSetupPassword';
import Register from './pages/register/Register';
import Home from './pages/home/Home';
import Features from './pages/features/Features';
import Blog from './pages/blog/Blog';
import Dashboard from './pages/dashboard/Dashboard';
import VerifyEmail from './pages/verify-email/VerifyEmail';
import Onboarding from './pages/onboarding/Onboarding';
import Profile from './pages/profile/Profile';
import ChangePassword from './pages/settings/ChangePassword';
import ChangeEmail from './pages/settings/ChangeEmail';
import LessonDetail from './pages/lessons/LessonDetail';
import MockTestList from './pages/mock-test/MockTestList';
import MockTestAttempt from './pages/mock-test/MockTestAttempt';
import MockTestResults from './pages/mock-test/MockTestResults';
import Progress from './pages/progress/Progress';
import KanjiList from './pages/kanji/KanjiList';
import KanjiPractice from './pages/kanji/KanjiPractice';
import Grammar from './pages/grammar/Grammar';
import Dictionary from './pages/dictionary/Dictionary';
import Notebook from './pages/notebook/Notebook';
import Reading from './pages/reading/Reading';
import NotFound from './pages/error/NotFound';
import Forbidden from './pages/error/Forbidden';
import PrivateRoute from './components/common/PrivateRoute';
import AdminRoute   from './components/common/AdminRoute';
import StaffRoute        from './components/common/StaffRoute';
import ManagerRoute      from './components/common/ManagerRoute';
import KanaList         from './pages/kana/KanaList';
import VocabularyRoute       from './pages/vocabulary/VocabularyRoute';
import VocabFlashcardSession from './pages/vocabulary/VocabFlashcardSession';
import CourseList       from './pages/courses/CourseList';
import QuizPage         from './pages/quiz/QuizPage';
import SpeakingPage     from './pages/speaking/SpeakingPage';
import SupportTickets       from './pages/support/SupportTickets';
import SupportTicketDetail  from './pages/support/SupportTicketDetail';
import Notifications        from './pages/notifications/Notifications';
import { ToastProvider } from './context/ToastContext';

// Admin/Staff/Manager routes lazy-loaded — nhóm học viên (traffic chính) không cần
// tải sẵn code các khu vực quản trị này trong bundle ban đầu.
const ManageUsers     = lazy(() => import('./pages/admin/ManageUsers'));
const AdminDashboard  = lazy(() => import('./pages/admin/AdminDashboard'));
const AdminSettings   = lazy(() => import('./pages/admin/AdminSettings'));
const AdminReports    = lazy(() => import('./pages/admin/AdminReports'));

const ManagerDashboard        = lazy(() => import('./pages/manager/ManagerDashboard'));
const ManagerReviewQueue      = lazy(() => import('./pages/manager/ManagerReviewQueue'));
const ManagerContentPipeline  = lazy(() => import('./pages/manager/ManagerContentPipeline'));
const ManagerDeletedTopics    = lazy(() => import('./pages/manager/ManagerDeletedTopics'));
const ManagerNotifications    = lazy(() => import('./pages/manager/ManagerNotifications'));
const ManagerTickets          = lazy(() => import('./pages/manager/ManagerTickets'));

const StaffDashboard    = lazy(() => import('./pages/staff/StaffDashboard'));
const StaffContent      = lazy(() => import('./pages/staff/StaffContent'));
const StaffQuestions    = lazy(() => import('./pages/staff/StaffQuestions'));
const StaffAssessments  = lazy(() => import('./pages/staff/StaffAssessments'));
const StaffTickets      = lazy(() => import('./pages/staff/StaffTickets'));
const StaffGrading      = lazy(() => import('./pages/staff/StaffGrading'));
const StaffStudents     = lazy(() => import('./pages/staff/StaffStudents'));

function PageLoading() {
  return <div className="app-route-loading" role="status" aria-label="Đang tải trang..." />;
}

function App() {
  return (
    <ToastProvider>
      <BrowserRouter>
        <Suspense fallback={<PageLoading />}>
        <Routes>
        {/* Public */}
        <Route path="/" element={<Home />} />
        <Route path="/tinh-nang" element={<Features />} />
        <Route path="/blog" element={<Blog />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/staff/forgot-password" element={<StaffForgotPassword />} />
        <Route path="/staff/setup-password" element={<StaffSetupPassword />} />
        <Route path="/staff/change-temp-password" element={<StaffChangeTempPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/verify-email" element={<VerifyEmail />} />
        <Route path="/403" element={<Forbidden />} />

        {/* Protected — Student */}
        <Route path="/dashboard"    element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/onboarding"   element={<PrivateRoute><Onboarding /></PrivateRoute>} />
        <Route path="/profile"      element={<PrivateRoute><Profile /></PrivateRoute>} />
        <Route path="/settings/change-password" element={<PrivateRoute><ChangePassword /></PrivateRoute>} />
        <Route path="/settings/change-email" element={<PrivateRoute><ChangeEmail /></PrivateRoute>} />
        <Route path="/lessons/:id"  element={<PrivateRoute><LessonDetail /></PrivateRoute>} />
        <Route path="/mock-test"                    element={<PrivateRoute><MockTestList /></PrivateRoute>} />
        <Route path="/mock-test/:id/attempt"        element={<PrivateRoute><MockTestAttempt /></PrivateRoute>} />
        <Route path="/mock-test/:id?/results"       element={<PrivateRoute><MockTestResults /></PrivateRoute>} />
        <Route path="/progress"     element={<PrivateRoute><Progress /></PrivateRoute>} />
        <Route path="/kanji"        element={<PrivateRoute><KanjiList /></PrivateRoute>} />
        <Route path="/kanji/:id"    element={<PrivateRoute><KanjiPractice /></PrivateRoute>} />
        <Route path="/grammar"      element={<PrivateRoute><Grammar /></PrivateRoute>} />
        <Route path="/dictionary"   element={<PrivateRoute><Dictionary /></PrivateRoute>} />
        <Route path="/notebook"     element={<PrivateRoute><Notebook /></PrivateRoute>} />
        <Route path="/reading"      element={<PrivateRoute><Reading /></PrivateRoute>} />
        <Route path="/kana"         element={<PrivateRoute><KanaList /></PrivateRoute>} />
        <Route path="/vocabulary"           element={<PrivateRoute><VocabularyRoute /></PrivateRoute>} />
        <Route path="/vocabulary/flashcard" element={<PrivateRoute><VocabFlashcardSession /></PrivateRoute>} />
        <Route path="/courses"              element={<PrivateRoute><CourseList /></PrivateRoute>} />
        <Route path="/quiz"         element={<PrivateRoute><QuizPage /></PrivateRoute>} />
        <Route path="/speaking"     element={<PrivateRoute><SpeakingPage /></PrivateRoute>} />
        <Route path="/support"                  element={<PrivateRoute><SupportTickets /></PrivateRoute>} />
        <Route path="/support/tickets/:ticketId" element={<PrivateRoute><SupportTicketDetail /></PrivateRoute>} />
        <Route path="/notifications"            element={<PrivateRoute><Notifications /></PrivateRoute>} />

        {/* Protected — Staff */}
        <Route path="/staff"                 element={<StaffRoute><StaffDashboard /></StaffRoute>} />
        <Route path="/staff/content"         element={<StaffRoute><StaffContent /></StaffRoute>} />
        <Route path="/staff/questions"       element={<StaffRoute><StaffQuestions /></StaffRoute>} />
        <Route path="/staff/assessments"     element={<StaffRoute><StaffAssessments /></StaffRoute>} />
        <Route path="/staff/tickets"         element={<StaffRoute><StaffTickets /></StaffRoute>} />
        {/* DEFER: Speaking chưa có backend — gỡ khỏi nav, giữ route cho dev nội bộ (xem SPEC_DEAD_CODE_AUDIT F4) */}
        <Route path="/staff/grading"         element={<StaffRoute><StaffGrading /></StaffRoute>} />
        <Route path="/staff/students"        element={<StaffRoute><StaffStudents /></StaffRoute>} />

        {/* Protected — Manager (staff_manager role) */}
        <Route path="/manager"                    element={<ManagerRoute><ManagerDashboard /></ManagerRoute>} />
        <Route path="/manager/review-queue"       element={<ManagerRoute><ManagerReviewQueue /></ManagerRoute>} />
        <Route path="/manager/content-pipeline"   element={<ManagerRoute><ManagerContentPipeline /></ManagerRoute>} />
        <Route path="/manager/deleted-topics"     element={<ManagerRoute><ManagerDeletedTopics /></ManagerRoute>} />
        <Route path="/manager/notifications"      element={<ManagerRoute><ManagerNotifications /></ManagerRoute>} />
        <Route path="/manager/tickets"            element={<ManagerRoute><ManagerTickets /></ManagerRoute>} />

        {/* Protected — Admin */}
        <Route path="/admin"           element={<AdminRoute><AdminDashboard /></AdminRoute>} />
        <Route path="/admin/users"     element={<AdminRoute><ManageUsers /></AdminRoute>} />
        <Route path="/admin/settings"  element={<AdminRoute><AdminSettings /></AdminRoute>} />
        <Route path="/admin/reports"   element={<AdminRoute><AdminReports /></AdminRoute>} />

        {/* Catch-all */}
        <Route path="*" element={<NotFound />} />
        </Routes>
        </Suspense>
      </BrowserRouter>
    </ToastProvider>
  );
}

export default App;
