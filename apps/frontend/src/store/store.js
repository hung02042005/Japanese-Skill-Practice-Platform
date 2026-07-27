import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import studentReducer from '../features/dashboard/studentSlice';
import staffGrammarReducer from '../features/management/staffGrammarSlice';
import staffQuestionReducer from '../features/management/staffQuestionSlice';
import staffQuizReducer from '../features/management/staffQuizSlice';
import staffExamReducer from '../features/management/staffExamSlice';
import managerReviewReducer from '../features/management/managerReviewSlice';
import staffLearningReducer from '../features/management/staffLearningSlice';
import publishedContentReducer from '../features/management/publishedContentSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    student: studentReducer,
    staffGrammar: staffGrammarReducer,
    staffQuestion: staffQuestionReducer,
    staffQuiz: staffQuizReducer,
    staffExam: staffExamReducer,
    managerReview: managerReviewReducer,
    staffLearning: staffLearningReducer,
    publishedContent: publishedContentReducer,
  },
});
