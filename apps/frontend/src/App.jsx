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
import Pricing from './pages/pricing/Pricing';
import Blog from './pages/blog/Blog';
import Dashboard from './pages/dashboard/Dashboard';
import VerifyEmail from './pages/verify-email/VerifyEmail';
import Onboarding from './pages/onboarding/Onboarding';
import Profile from './pages/profile/Profile';
import ChangePassword from './pages/settings/ChangePassword';
import LearnNew from './pages/learn/LearnNew';
import Review from './pages/review/Review';
import LessonDetail from './pages/lessons/LessonDetail';
import Flashcard from './pages/flashcard/Flashcard';
import MockTestList from './pages/mock-test/MockTestList';
import MockTestAttempt from './pages/mock-test/MockTestAttempt';
import MockTestResults from './pages/mock-test/MockTestResults';
import Progress from './pages/progress/Progress';
import Subscription from './pages/subscription/Subscription';
import SubscriptionSuccess from './pages/subscription/SubscriptionSuccess';
import KanjiList from './pages/kanji/KanjiList';
import KanjiPractice from './pages/kanji/KanjiPractice';
import Certificates from './pages/certificates/Certificates';
import Grammar from './pages/grammar/Grammar';
import Dictionary from './pages/dictionary/Dictionary';
import Reading from './pages/reading/Reading';
import Listening from './pages/listening/Listening';
import NotFound from './pages/error/NotFound';
import Forbidden from './pages/error/Forbidden';
import ManageUsers     from './pages/admin/ManageUsers';
import AdminDashboard  from './pages/admin/AdminDashboard';
import AdminSettings   from './pages/admin/AdminSettings';
import PrivateRoute from './components/common/PrivateRoute';
import AdminRoute   from './components/common/AdminRoute';
import StaffRoute        from './components/common/StaffRoute';
import StaffDashboard    from './pages/staff/StaffDashboard';
import StaffContent      from './pages/staff/StaffContent';
import StaffQuestions    from './pages/staff/StaffQuestions';
import StaffAssessments  from './pages/staff/StaffAssessments';
import StaffTickets      from './pages/staff/StaffTickets';
import StaffGrading      from './pages/staff/StaffGrading';
import StaffNotifications from './pages/staff/StaffNotifications';
import StaffReviewQueue  from './pages/staff/StaffReviewQueue';
import StaffStudents    from './pages/staff/StaffStudents';
import KanaList         from './pages/kana/KanaList';
import VocabularyList   from './pages/vocabulary/VocabularyList';
import QuizPage         from './pages/quiz/QuizPage';
import SpeakingPage     from './pages/speaking/SpeakingPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/" element={<Home />} />
        <Route path="/tinh-nang" element={<Features />} />
        <Route path="/bang-gia" element={<Pricing />} />
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
        <Route path="/learn"        element={<PrivateRoute><LearnNew /></PrivateRoute>} />
        <Route path="/review"       element={<PrivateRoute><Review /></PrivateRoute>} />
        <Route path="/lessons/:id"  element={<PrivateRoute><LessonDetail /></PrivateRoute>} />
        <Route path="/flashcard"    element={<PrivateRoute><Flashcard /></PrivateRoute>} />
        <Route path="/mock-test"                    element={<PrivateRoute><MockTestList /></PrivateRoute>} />
        <Route path="/mock-test/:id/attempt"        element={<PrivateRoute><MockTestAttempt /></PrivateRoute>} />
        <Route path="/mock-test/:id/results"        element={<PrivateRoute><MockTestResults /></PrivateRoute>} />
        <Route path="/progress"     element={<PrivateRoute><Progress /></PrivateRoute>} />
        <Route path="/subscription"         element={<PrivateRoute><Subscription /></PrivateRoute>} />
        <Route path="/subscription/success" element={<PrivateRoute><SubscriptionSuccess /></PrivateRoute>} />
        <Route path="/kanji"        element={<PrivateRoute><KanjiList /></PrivateRoute>} />
        <Route path="/kanji/:id"    element={<PrivateRoute><KanjiPractice /></PrivateRoute>} />
        <Route path="/certificates" element={<PrivateRoute><Certificates /></PrivateRoute>} />
        <Route path="/grammar"      element={<PrivateRoute><Grammar /></PrivateRoute>} />
        <Route path="/dictionary"   element={<PrivateRoute><Dictionary /></PrivateRoute>} />
        <Route path="/reading"      element={<PrivateRoute><Reading /></PrivateRoute>} />
        <Route path="/listening"    element={<PrivateRoute><Listening /></PrivateRoute>} />
        <Route path="/kana"         element={<PrivateRoute><KanaList /></PrivateRoute>} />
        <Route path="/vocabulary"   element={<PrivateRoute><VocabularyList /></PrivateRoute>} />
        <Route path="/quiz"         element={<PrivateRoute><QuizPage /></PrivateRoute>} />
        <Route path="/speaking"     element={<PrivateRoute><SpeakingPage /></PrivateRoute>} />

        {/* Protected — Staff */}
        <Route path="/staff"                 element={<StaffRoute><StaffDashboard /></StaffRoute>} />
        <Route path="/staff/content"         element={<StaffRoute><StaffContent /></StaffRoute>} />
        <Route path="/staff/questions"       element={<StaffRoute><StaffQuestions /></StaffRoute>} />
        <Route path="/staff/assessments"     element={<StaffRoute><StaffAssessments /></StaffRoute>} />
        <Route path="/staff/tickets"         element={<StaffRoute><StaffTickets /></StaffRoute>} />
        <Route path="/staff/grading"         element={<StaffRoute><StaffGrading /></StaffRoute>} />
        <Route path="/staff/notifications"   element={<StaffRoute><StaffNotifications /></StaffRoute>} />
        <Route path="/staff/review-queue"    element={<StaffRoute><StaffReviewQueue /></StaffRoute>} />
        <Route path="/staff/students"        element={<StaffRoute><StaffStudents /></StaffRoute>} />

        {/* Protected — Admin */}
        <Route path="/admin"          element={<AdminRoute><AdminDashboard /></AdminRoute>} />
        <Route path="/admin/users"    element={<AdminRoute><ManageUsers /></AdminRoute>} />
        <Route path="/admin/settings" element={<AdminRoute><AdminSettings /></AdminRoute>} />

        {/* Catch-all */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
