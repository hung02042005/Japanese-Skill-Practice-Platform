import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as managerService from '../../api/managerService';

// --- Thunks ---

export const fetchReviewQueueThunk = createAsyncThunk(
  'managerReview/fetchQueue',
  async (filters, { rejectWithValue }) => {
    try {
      const data = await managerService.getReviewQueue(filters);
      return data; // { content, totalElements, totalPages }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải hàng đợi phê duyệt');
    }
  }
);

export const getReviewContentDetailThunk = createAsyncThunk(
  'managerReview/getDetail',
  async ({ contentId, contentType }, { rejectWithValue }) => {
    try {
      const data = await managerService.getReviewableContentDetail(contentId, contentType);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể lấy chi tiết nội dung');
    }
  }
);

export const reviewContentThunk = createAsyncThunk(
  'managerReview/review',
  async (payload, { rejectWithValue }) => {
    try {
      const data = await managerService.reviewContent(payload);
      return data; // ReviewResultResponse
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi xử lý phê duyệt');
    }
  }
);

export const requestChangesThunk = createAsyncThunk(
  'managerReview/requestChanges',
  async (payload, { rejectWithValue }) => {
    try {
      const data = await managerService.requestContentChanges(payload);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi yêu cầu chỉnh sửa');
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

const managerReviewSlice = createSlice({
  name: 'managerReview',
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
      .addCase(fetchReviewQueueThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchReviewQueueThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload.content || [];
        state.totalElements = action.payload.totalElements || 0;
        state.totalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchReviewQueueThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
        state.items = [];
        state.totalElements = 0;
        state.totalPages = 0;
      });
  },
});

export const { clearError, resetState } = managerReviewSlice.actions;
export default managerReviewSlice.reducer;
