import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { searchDictionary } from '../../api/studentService';

// ─── Thunks ──────────────────────────────────────────────────────────────────

export const searchDictionaryThunk = createAsyncThunk(
  'dictionary/search',
  async ({ q, jlptLevel, type }, { rejectWithValue }) => {
    try {
      return await searchDictionary(q, jlptLevel, type);
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể tìm kiếm. Thử lại sau.');
    }
  },
);

// ─── Slice ───────────────────────────────────────────────────────────────────

const dictionarySlice = createSlice({
  name: 'dictionary',
  initialState: {
    // Results from backend: { keyword, vocabulary, kanji, grammar, lessons }
    results: null,
    query: '',
    status: 'idle',  // 'idle' | 'loading' | 'succeeded' | 'failed'
    error: null,
  },
  reducers: {
    setQuery(state, action) {
      state.query = action.payload;
      if (!action.payload) {
        state.results = null;
        state.status = 'idle';
        state.error = null;
      }
    },
    clearResults(state) {
      state.results = null;
      state.query = '';
      state.status = 'idle';
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(searchDictionaryThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(searchDictionaryThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.results = action.payload;
      })
      .addCase(searchDictionaryThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      });
  },
});

export const { setQuery, clearResults } = dictionarySlice.actions;
export default dictionarySlice.reducer;
