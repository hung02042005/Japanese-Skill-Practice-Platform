import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as staffService from '@/shared/api/staffService';

// --- Thunks ---

export const fetchGrammarsThunk = createAsyncThunk(
  'staffGrammar/fetchList',
  async (filters, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffGrammars(filters);
      return data; // { content: [...], totalElements, totalPages }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi tải danh sách ngữ pháp');
    }
  }
);

export const getGrammarDetailThunk = createAsyncThunk(
  'staffGrammar/getDetail',
  async (grammarId, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffGrammar(grammarId);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi lấy chi tiết ngữ pháp');
    }
  }
);

export const createGrammarThunk = createAsyncThunk(
  'staffGrammar/create',
  async (payload, { rejectWithValue }) => {
    try {
      const res = await staffService.createStaffGrammar(payload);
      // res is the ApiResponse object: { status, message, data: {...} }
      return res.data ?? res;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi tạo ngữ pháp');
    }
  }
);

export const updateGrammarThunk = createAsyncThunk(
  'staffGrammar/update',
  async ({ grammarId, payload }, { rejectWithValue }) => {
    try {
      const res = await staffService.updateStaffGrammar(grammarId, payload);
      return res.data ?? res;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi cập nhật ngữ pháp');
    }
  }
);

export const submitGrammarReviewThunk = createAsyncThunk(
  'staffGrammar/submitReview',
  async (grammarId, { rejectWithValue }) => {
    try {
      const data = await staffService.submitStaffGrammarForReview(grammarId);
      return data.data; // { grammarId, contentType, status: 'pending_review' }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Lỗi khi gửi duyệt ngữ pháp');
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

const staffGrammarSlice = createSlice({
  name: 'staffGrammar',
  initialState,
  reducers: {
    clearError(state) {
      state.error = null;
    },
    resetState(state) {
      state.items = [];
      state.totalElements = 0;
      state.totalPages = 0;
      state.status = 'idle';
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch List
      .addCase(fetchGrammarsThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchGrammarsThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload.content || [];
        state.totalElements = action.payload.totalElements || 0;
        state.totalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchGrammarsThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })
      
      // Create
      .addCase(createGrammarThunk.fulfilled, (_state, _action) => {
        // Optionally prepend to list or let caller refresh
        // For simplicity, we just rely on component re-fetching list after success.
      })
      
      // Update
      .addCase(updateGrammarThunk.fulfilled, (state, action) => {
        const updatedItem = action.payload;
        if (updatedItem && updatedItem.grammarId) {
          const idx = state.items.findIndex((item) => item.grammarId === updatedItem.grammarId || item.id === updatedItem.grammarId);
          if (idx !== -1) {
            // map updated properties
            state.items[idx] = { ...state.items[idx], ...updatedItem };
          }
        }
      })
      
      // Submit Review
      .addCase(submitGrammarReviewThunk.fulfilled, (state, action) => {
        const { grammarId, status } = action.payload;
        const idx = state.items.findIndex((item) => item.grammarId === grammarId || item.id === grammarId);
        if (idx !== -1) {
          state.items[idx].status = status;
        }
      });
  },
});

export const { clearError, resetState } = staffGrammarSlice.actions;
export default staffGrammarSlice.reducer;
