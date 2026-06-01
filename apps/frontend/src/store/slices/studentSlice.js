import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getDashboard } from '../../api/studentService';

// Demo data — replace with real API response when backend is ready
const DEMO = {
  streak: 7,
  weekDays: [true, true, false, true, true, true, false],
  course: {
    id: 1,
    title: 'Lộ Trình Tiếng Nhật N5',
    jlptLevel: 'N5',
    description: 'Nắm vững nền tảng: Hiragana, Katakana, từ vựng & ngữ pháp cơ bản',
    completedLessons: 5,
    totalLessons: 40,
  },
  lessons: [
    {
      id: 1,
      title: 'Hiragana',
      description: 'Nhận biết 46 âm tiết Hiragana cơ bản',
      jlptLevel: 'N5',
      status: 'active',
      progress: 0.6,
      thumbnail: 'あ',
    },
    {
      id: 2,
      title: 'Katakana',
      description: 'Bảng chữ cái thứ hai của tiếng Nhật',
      jlptLevel: 'N5',
      status: 'available',
      progress: 0,
      thumbnail: 'ア',
    },
    {
      id: 3,
      title: 'Từ vựng N5',
      description: '800 từ vựng N5 thông dụng nhất',
      jlptLevel: 'N5',
      status: 'available',
      progress: 0,
      thumbnail: '語',
    },
    {
      id: 4,
      title: 'Ngữ pháp N5',
      description: 'Các mẫu câu ngữ pháp cơ bản N5',
      jlptLevel: 'N5',
      status: 'locked',
      progress: 0,
      thumbnail: '文',
    },
    {
      id: 5,
      title: 'Kanji N5',
      description: '103 Kanji N5 theo bộ thủ',
      jlptLevel: 'N5',
      status: 'locked',
      progress: 0,
      thumbnail: '漢',
    },
  ],
  wordCount: 248,
  daysThisMonth: 18,
};

export const fetchDashboardThunk = createAsyncThunk(
  'student/fetchDashboard',
  async (_, { rejectWithValue }) => {
    try {
      const res = await getDashboard();
      return res.data;
    } catch {
      return DEMO;
    }
  },
);

const studentSlice = createSlice({
  name: 'student',
  initialState: {
    ...DEMO,
    status: 'idle',
    error: null,
  },
  reducers: {
    clearError: (state) => { state.error = null; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboardThunk.pending, (state) => {
        state.status = 'loading';
      })
      .addCase(fetchDashboardThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.streak         = action.payload.streak;
        state.weekDays       = action.payload.weekDays;
        state.course         = action.payload.course;
        state.lessons        = action.payload.lessons;
        state.wordCount      = action.payload.wordCount;
        state.daysThisMonth  = action.payload.daysThisMonth;
      })
      .addCase(fetchDashboardThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      });
  },
});

export const { clearError } = studentSlice.actions;
export default studentSlice.reducer;
