import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as staffService from '../../api/staffService';

// --- Thunks ---

export const fetchQuizzesThunk = createAsyncThunk(
  'staffQuiz/fetchList',
  async (filters, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffQuizzes(filters);
      return data; // { content, totalElements, totalPages }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách quiz');
    }
  }
);

export const getQuizDetailThunk = createAsyncThunk(
  'staffQuiz/getDetail',
  async (assessmentId, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffQuiz(assessmentId);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể lấy chi tiết quiz');
    }
  }
);

export const createQuizThunk = createAsyncThunk(
  'staffQuiz/create',
  async (payload, { rejectWithValue }) => {
    try {
      const res = await staffService.createStaffQuiz(payload);
      return res; // ApiResponse { status, message, data }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi tạo quiz');
    }
  }
);

export const updateQuizThunk = createAsyncThunk(
  'staffQuiz/update',
  async ({ assessmentId, payload }, { rejectWithValue }) => {
    try {
      const res = await staffService.updateStaffQuiz(assessmentId, payload);
      return res;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi cập nhật quiz');
    }
  }
);

export const submitQuizReviewThunk = createAsyncThunk(
  'staffQuiz/submitReview',
  async (assessmentId, { rejectWithValue }) => {
    try {
      const res = await staffService.submitAssessmentForReview('assessment', assessmentId);
      return res.data ?? res;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi gửi duyệt quiz');
    }
  }
);

export const assignQuizQuestionsThunk = createAsyncThunk(
  'staffQuiz/assignQuestions',
  async ({ assessmentId, assignments }, { rejectWithValue }) => {
    try {
      return await staffService.assignStaffQuizQuestions(assessmentId, assignments);
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi gán câu hỏi vào quiz');
    }
  }
);

// --- Slice ---

const initialState = {
  items: [],
  totalElements: 0,
  totalPages: 0,
  status: 'idle',
  error: null,
};

const staffQuizSlice = createSlice({
  name: 'staffQuiz',
  initialState,
  reducers: {
    clearError(state) {
      state.error = null;
    },
    resetState() {
      return initialState;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchQuizzesThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchQuizzesThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload.content || [];
        state.totalElements = action.payload.totalElements || 0;
        state.totalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchQuizzesThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
        state.items = [];
        state.totalElements = 0;
        state.totalPages = 0;
      });
  },
});

export const { clearError, resetState } = staffQuizSlice.actions;
export default staffQuizSlice.reducer;
