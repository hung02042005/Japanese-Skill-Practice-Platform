import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as staffService from '@/shared/api/staffService';

// ─── Lesson Thunks ──────────────────────────────────────────────────────────

export const fetchLessonsThunk = createAsyncThunk(
  'staffLearning/fetchLessons',
  async (filters, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffLessons(filters);
      return data; // { content, totalElements, totalPages }
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách bài học');
    }
  }
);

// ─── Vocabulary Thunks ──────────────────────────────────────────────────────

export const fetchVocabularyThunk = createAsyncThunk(
  'staffLearning/fetchVocabulary',
  async (filters, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffVocabularyList(filters);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách từ vựng');
    }
  }
);

export const getVocabularyDetailThunk = createAsyncThunk(
  'staffLearning/getVocabularyDetail',
  async (vocabularyId, { rejectWithValue }) => {
    try {
      return await staffService.getStaffVocabularyItem(vocabularyId);
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể lấy chi tiết từ vựng');
    }
  }
);

// ─── Kanji Thunks ───────────────────────────────────────────────────────────

export const fetchKanjiThunk = createAsyncThunk(
  'staffLearning/fetchKanji',
  async (filters, { rejectWithValue }) => {
    try {
      const data = await staffService.getStaffKanjiList(filters);
      return data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể tải danh sách Kanji');
    }
  }
);

export const getKanjiDetailThunk = createAsyncThunk(
  'staffLearning/getKanjiDetail',
  async (kanjiId, { rejectWithValue }) => {
    try {
      return await staffService.getStaffKanjiItem(kanjiId);
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Không thể lấy chi tiết Kanji');
    }
  }
);

// ─── Course is mapped via lessons (no separate entity) ────────────────────

// ─── Slice ──────────────────────────────────────────────────────────────────

const initialState = {
  // Lessons
  lessons: [],
  lessonsTotalElements: 0,
  lessonsTotalPages: 0,
  lessonsStatus: 'idle',
  lessonsError: null,

  // Vocabulary
  vocabulary: [],
  vocabularyTotalElements: 0,
  vocabularyTotalPages: 0,
  vocabularyStatus: 'idle',
  vocabularyError: null,
  vocabularyDetail: null,

  // Kanji
  kanji: [],
  kanjiTotalElements: 0,
  kanjiTotalPages: 0,
  kanjiStatus: 'idle',
  kanjiError: null,
  kanjiDetail: null,
};

const staffLearningSlice = createSlice({
  name: 'staffLearning',
  initialState,
  reducers: {
    clearErrors(state) {
      state.lessonsError = null;
      state.vocabularyError = null;
      state.kanjiError = null;
    },
    resetState() {
      return initialState;
    },
  },
  extraReducers: (builder) => {
    builder
      // ── Lessons ──
      .addCase(fetchLessonsThunk.pending, (state) => {
        state.lessonsStatus = 'loading';
        state.lessonsError = null;
      })
      .addCase(fetchLessonsThunk.fulfilled, (state, action) => {
        state.lessonsStatus = 'succeeded';
        state.lessons = action.payload.content || [];
        state.lessonsTotalElements = action.payload.totalElements || 0;
        state.lessonsTotalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchLessonsThunk.rejected, (state, action) => {
        state.lessonsStatus = 'failed';
        state.lessonsError = action.payload;
        state.lessons = [];
        state.lessonsTotalElements = 0;
        state.lessonsTotalPages = 0;
      })
      // ── Vocabulary ──
      .addCase(fetchVocabularyThunk.pending, (state) => {
        state.vocabularyStatus = 'loading';
        state.vocabularyError = null;
      })
      .addCase(fetchVocabularyThunk.fulfilled, (state, action) => {
        state.vocabularyStatus = 'succeeded';
        state.vocabulary = action.payload.content || [];
        state.vocabularyTotalElements = action.payload.totalElements || 0;
        state.vocabularyTotalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchVocabularyThunk.rejected, (state, action) => {
        state.vocabularyStatus = 'failed';
        state.vocabularyError = action.payload;
        state.vocabulary = [];
        state.vocabularyTotalElements = 0;
        state.vocabularyTotalPages = 0;
      })
      .addCase(getVocabularyDetailThunk.fulfilled, (state, action) => {
        state.vocabularyDetail = action.payload;
      })

      // ── Kanji ──
      .addCase(fetchKanjiThunk.pending, (state) => {
        state.kanjiStatus = 'loading';
        state.kanjiError = null;
      })
      .addCase(fetchKanjiThunk.fulfilled, (state, action) => {
        state.kanjiStatus = 'succeeded';
        state.kanji = action.payload.content || [];
        state.kanjiTotalElements = action.payload.totalElements || 0;
        state.kanjiTotalPages = action.payload.totalPages || 0;
      })
      .addCase(fetchKanjiThunk.rejected, (state, action) => {
        state.kanjiStatus = 'failed';
        state.kanjiError = action.payload;
        state.kanji = [];
        state.kanjiTotalElements = 0;
        state.kanjiTotalPages = 0;
      })
      .addCase(getKanjiDetailThunk.fulfilled, (state, action) => {
        state.kanjiDetail = action.payload;
      });
  },
});

export const { clearErrors, resetState } = staffLearningSlice.actions;
export default staffLearningSlice.reducer;
