import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import studentReducer from './slices/studentSlice';
import flashcardReducer from './slices/flashcardSlice';
import dictionaryReducer from './slices/dictionarySlice';
import bookmarkReducer from './slices/bookmarkSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    student: studentReducer,
    flashcard: flashcardReducer,
    dictionary: dictionaryReducer,
    bookmark: bookmarkReducer,
  },
});
