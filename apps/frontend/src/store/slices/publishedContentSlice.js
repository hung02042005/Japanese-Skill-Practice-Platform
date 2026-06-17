import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as managerService from '../../api/managerService';

// --- Thunks ---

export const fetchPublishedContentsThunk = createAsyncThunk(
  'publishedContent/fetchList',
  async (filters, { rejectWithValue }) => {
    try {
      const data = await managerService.getPublishedContents(filters);
      return data; // { content, totalElements, totalPages }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách nội dung đã xuất bản');
    }
  }
);

export const getPublishedContentDetailThunk = createAsyncThunk(
  'publishedContent/getDetail',
  async ({ contentId, contentType }, { rejectWithValue }) => {
    try {
      return await managerService.getPublishedContentDetail(contentId, contentType);
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể lấy chi tiết nội dung');
    }
  }
);

export const changePublishedStatusThunk = createAsyncThunk(
  'publishedContent/changeStatus',
  async ({ contentId, payload }, { rejectWithValue }) => {
    try {
      const data = await managerService.changePublishedContentStatus(contentId, payload);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi cập nhật trạng thái');
    }
  }
);

export const restorePublishedContentThunk = createAsyncThunk(
  'publishedContent/restore',
  async ({ contentId, payload }, { rejectWithValue }) => {
    try {
      const data = await managerService.restorePublishedContent(contentId, payload);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi khôi phục nội dung');
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
  detail: null,
};

const publishedContentSlice = createSlice({
  name: 'publishedContent',
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
      .addCase(fetchPublishedContentsThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchPublishedContentsThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload.content || [];
        state.totalElements = action.payload.totalElements || 0;
        state.totalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchPublishedContentsThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
        state.items = [];
        state.totalElements = 0;
        state.totalPages = 0;
      })
      .addCase(getPublishedContentDetailThunk.fulfilled, (state, action) => {
        state.detail = action.payload;
      });
  },
});

export const { clearError, resetState } = publishedContentSlice.actions;
export default publishedContentSlice.reducer;
