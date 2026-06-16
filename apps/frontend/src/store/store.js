import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import studentReducer from './slices/studentSlice';
import staffGrammarReducer from './slices/staffGrammarSlice';
import staffQuestionReducer from './slices/staffQuestionSlice';
import staffQuizReducer from './slices/staffQuizSlice';
import staffExamReducer from './slices/staffExamSlice';
import managerReviewReducer from './slices/managerReviewSlice';
import staffLearningReducer from './slices/staffLearningSlice';
import publishedContentReducer from './slices/publishedContentSlice';

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
