import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getBookmarks, addBookmark, removeBookmark } from '../../api/studentService';

// ─── Thunks ──────────────────────────────────────────────────────────────────

export const fetchBookmarksThunk = createAsyncThunk(
  'bookmark/fetchBookmarks',
  async ({ type, page = 0, size = 50 } = {}, { rejectWithValue }) => {
    try {
      return await getBookmarks(type, page, size);
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể tải bookmark.');
    }
  },
);

export const addBookmarkThunk = createAsyncThunk(
  'bookmark/add',
  async ({ contentType, contentId, note = '' }, { rejectWithValue }) => {
    try {
      return await addBookmark(contentType, contentId, note);
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể thêm bookmark.');
    }
  },
);

export const removeBookmarkThunk = createAsyncThunk(
  'bookmark/remove',
  async ({ contentType, contentId }, { rejectWithValue }) => {
    try {
      await removeBookmark(contentType, contentId);
      return { contentType, contentId };
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể xoá bookmark.');
    }
  },
);

// ─── Slice ───────────────────────────────────────────────────────────────────

const bookmarkSlice = createSlice({
  name: 'bookmark',
  initialState: {
    // Page.content — list of BookmarkResponse
    items: [],
    totalElements: 0,
    status: 'idle',   // 'idle' | 'loading' | 'succeeded' | 'failed'
    addStatus: 'idle',
    removeStatus: 'idle',
    error: null,
  },
  reducers: {
    clearError(state) {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    // fetchBookmarks
    builder
      .addCase(fetchBookmarksThunk.pending, (state) => { state.status = 'loading'; state.error = null; })
      .addCase(fetchBookmarksThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        // Backend trả Page<BookmarkResponse>: { content, totalElements, ... }
        state.items = action.payload?.content ?? action.payload ?? [];
        state.totalElements = action.payload?.totalElements ?? state.items.length;
      })
      .addCase(fetchBookmarksThunk.rejected, (state, action) => { state.status = 'failed'; state.error = action.payload; });

    // addBookmark
    builder
      .addCase(addBookmarkThunk.pending, (state) => { state.addStatus = 'loading'; })
      .addCase(addBookmarkThunk.fulfilled, (state, action) => {
        state.addStatus = 'idle';
        // Prepend nếu chưa có
        const exists = state.items.some(
          (b) => b.contentType === action.payload.contentType && b.contentId === action.payload.contentId,
        );
        if (!exists) {
          state.items = [action.payload, ...state.items];
          state.totalElements += 1;
        }
      })
      .addCase(addBookmarkThunk.rejected, (state, action) => { state.addStatus = 'failed'; state.error = action.payload; });

    // removeBookmark
    builder
      .addCase(removeBookmarkThunk.pending, (state) => { state.removeStatus = 'loading'; })
      .addCase(removeBookmarkThunk.fulfilled, (state, action) => {
        state.removeStatus = 'idle';
        state.items = state.items.filter(
          (b) => !(b.contentType === action.payload.contentType && b.contentId === action.payload.contentId),
        );
        state.totalElements = Math.max(0, state.totalElements - 1);
      })
      .addCase(removeBookmarkThunk.rejected, (state, action) => { state.removeStatus = 'failed'; state.error = action.payload; });
  },
});

export const { clearError } = bookmarkSlice.actions;
export default bookmarkSlice.reducer;
