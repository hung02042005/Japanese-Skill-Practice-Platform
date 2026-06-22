import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as staffService from '../../api/staffService';

// --- Thunks ---

export const fetchExamsThunk = createAsyncThunk(
  'staffExam/fetchList',
  async (filters, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffExams(filters);
      return data; // { content, totalElements, totalPages }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách đề thi');
    }
  }
);

export const getExamDetailThunk = createAsyncThunk(
  'staffExam/getDetail',
  async (assessmentId, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffExam(assessmentId);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể lấy chi tiết đề thi');
    }
  }
);

export const createExamThunk = createAsyncThunk(
  'staffExam/create',
  async (payload, { rejectWithValue }) => {
    try {
      const res = await staffService.createStaffExam(payload);
      return res; // ApiResponse { status, message, data }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi tạo đề thi');
    }
  }
);

export const updateExamThunk = createAsyncThunk(
  'staffExam/update',
  async ({ assessmentId, payload }, { rejectWithValue }) => {
    try {
      const res = await staffService.updateStaffExam(assessmentId, payload);
      return res;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi cập nhật đề thi');
    }
  }
);

export const submitExamReviewThunk = createAsyncThunk(
  'staffExam/submitReview',
  async (assessmentId, { rejectWithValue }) => {
    try {
      const res = await staffService.submitAssessmentForReview('exam', assessmentId);
      return res.data ?? res;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi gửi duyệt đề thi');
    }
  }
);

export const assignExamQuestionsThunk = createAsyncThunk(
  'staffExam/assignQuestions',
  async ({ assessmentId, assignments }, { rejectWithValue }) => {
    try {
      return await staffService.assignStaffExamQuestions(assessmentId, assignments);
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi gán câu hỏi vào đề thi');
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

const staffExamSlice = createSlice({
  name: 'staffExam',
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
      .addCase(fetchExamsThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchExamsThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload.content || [];
        state.totalElements = action.payload.totalElements || 0;
        state.totalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchExamsThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
        state.items = [];
        state.totalElements = 0;
        state.totalPages = 0;
      });
  },
});

export const { clearError, resetState } = staffExamSlice.actions;
export default staffExamSlice.reducer;
