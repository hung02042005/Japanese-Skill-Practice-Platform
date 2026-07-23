import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './features/auth/login/Login';
import ForgotPassword from './features/auth/forgot-password/ForgotPassword';
import ResetPassword from './features/auth/forgot-password/ResetPassword';
import StaffForgotPassword from './features/auth/forgot-password/StaffForgotPassword';
import StaffChangeTempPassword from './features/auth/forgot-password/StaffChangeTempPassword';
import StaffSetupPassword from './features/auth/forgot-password/StaffSetupPassword';
import Register from './features/auth/register/Register';
import Home from './features/public/home/Home';
import Features from './features/public/features/Features';
import Blog from './features/public/blog/Blog';
import Dashboard from './features/dashboard/dashboard/Dashboard';
import VerifyEmail from './features/auth/verify-email/VerifyEmail';
import Onboarding from './features/onboarding/onboarding/Onboarding';
import Profile from './features/profile/profile/Profile';
import ChangePassword from './features/settings/settings/ChangePassword';
import ChangeEmail from './features/settings/settings/ChangeEmail';
import LessonDetail from './features/courses/lessons/LessonDetail';
import MockTestList from './features/mock-test/mock-test/MockTestList';
import MockTestAttempt from './features/mock-test/mock-test/MockTestAttempt';
import MockTestResults from './features/mock-test/mock-test/MockTestResults';
import Progress from './features/progress/progress/Progress';
import KanjiList from './features/kanji/kanji/KanjiList';
import KanjiPractice from './features/kanji/kanji/KanjiPractice';
import Grammar from './features/grammar/grammar/Grammar';
import Dictionary from './features/dictionary/dictionary/Dictionary';
import Notebook from './features/notebook/notebook/Notebook';
import NotFound from './features/public/error/NotFound';
import Forbidden from './features/public/error/Forbidden';
import PrivateRoute from './shared/components/common/PrivateRoute';
import AdminRoute   from './shared/components/common/AdminRoute';
import StaffRoute        from './shared/components/common/StaffRoute';
import ManagerRoute      from './shared/components/common/ManagerRoute';
import KanaList         from './features/kana/kana/KanaList';
import VocabularyRoute       from './features/vocabulary/vocabulary/VocabularyRoute';
import VocabFlashcardSession from './features/vocabulary/vocabulary/VocabFlashcardSession';
import CourseList       from './features/courses/courses/CourseList';
import QuizPage         from './features/quiz/quiz/QuizPage';
import SpeakingPage     from './features/speaking/speaking/SpeakingPage';
import SupportTickets       from './features/public/support/SupportTickets';
import SupportTicketDetail  from './features/public/support/SupportTicketDetail';
import Notifications        from './features/notifications/notifications/Notifications';
import { ToastProvider } from './shared/context/ToastContext';

// Admin/Staff/Manager routes lazy-loaded — nhóm học viên (traffic chính) không cần
// tải sẵn code các khu vực quản trị này trong bundle ban đầu.
const ManageUsers     = lazy(() => import('./features/management/admin/ManageUsers'));
const AdminDashboard  = lazy(() => import('./features/management/admin/AdminDashboard'));
const AdminSettings   = lazy(() => import('./features/management/admin/AdminSettings'));
const AdminReports    = lazy(() => import('./features/management/admin/AdminReports'));

const ManagerDashboard        = lazy(() => import('./features/management/manager/ManagerDashboard'));
const ManagerReviewQueue      = lazy(() => import('./features/management/manager/ManagerReviewQueue'));
const ManagerContentPipeline  = lazy(() => import('./features/management/manager/ManagerContentPipeline'));
const ManagerDeletedTopics    = lazy(() => import('./features/management/manager/ManagerDeletedTopics'));
const ManagerNotifications    = lazy(() => import('./features/management/manager/ManagerNotifications'));
const ManagerTickets          = lazy(() => import('./features/management/manager/ManagerTickets'));

const StaffDashboard    = lazy(() => import('./features/management/staff/StaffDashboard'));
const StaffContent      = lazy(() => import('./features/management/staff/StaffContent'));
const StaffQuestions    = lazy(() => import('./features/management/staff/StaffQuestions'));
const StaffAssessments  = lazy(() => import('./features/management/staff/StaffAssessments'));
const StaffTickets      = lazy(() => import('./features/management/staff/StaffTickets'));
const StaffGrading      = lazy(() => import('./features/management/staff/StaffGrading'));
const StaffStudents     = lazy(() => import('./features/management/staff/StaffStudents'));

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
