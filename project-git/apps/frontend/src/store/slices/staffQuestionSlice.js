import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as staffService from '../../api/staffService';

// --- Thunks ---

export const fetchQuestionsThunk = createAsyncThunk(
  'staffQuestion/fetchList',
  async (filters, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffQuestions(filters);
      return data; // { content: [...], totalElements, totalPages }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách câu hỏi');
    }
  }
);

export const getQuestionDetailThunk = createAsyncThunk(
  'staffQuestion/getDetail',
  async (questionId, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffQuestion(questionId);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể lấy chi tiết câu hỏi');
    }
  }
);

export const createQuestionThunk = createAsyncThunk(
  'staffQuestion/create',
  async (payload, { rejectWithValue }) => {
    try {
      const res = await staffService.createStaffQuestion(payload);
      // res is the ApiResponse object: { status, message, data: {...} }
      return res;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi tạo câu hỏi');
    }
  }
);

export const updateQuestionThunk = createAsyncThunk(
  'staffQuestion/update',
  async ({ questionId, payload }, { rejectWithValue }) => {
    try {
      const res = await staffService.updateStaffQuestion(questionId, payload);
      return res;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi cập nhật câu hỏi');
    }
  }
);

export const submitQuestionReviewThunk = createAsyncThunk(
  'staffQuestion/submitReview',
  async (questionId, { rejectWithValue }) => {
    try {
      const res = await staffService.submitStaffQuestionForReview(questionId);
      return res.data ?? res;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi gửi duyệt câu hỏi');
    }
  }
);

// --- Slice ---

const initialState = {
  items: [],
  totalElements: 0,
  totalPages: 0,
  status: 'idle', // 'idle' | 'loading' | 'succeeded' | 'failed'
  error: null,
};

const staffQuestionSlice = createSlice({
  name: 'staffQuestion',
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
      .addCase(fetchQuestionsThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchQuestionsThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload.content || [];
        state.totalElements = action.payload.totalElements || 0;
        state.totalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchQuestionsThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
        state.items = [];
        state.totalElements = 0;
        state.totalPages = 0;
      });
  },
});

export const { clearError, resetState } = staffQuestionSlice.actions;
export default staffQuestionSlice.reducer;
