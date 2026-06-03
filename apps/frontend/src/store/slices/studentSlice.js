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
  selectedLevel: 'N5',
  lessons: [
    // ── N5 ──
    { id: 1,  title: 'Hiragana',     description: 'Nhận biết 46 âm tiết Hiragana cơ bản',     jlptLevel: 'N5', lessonType: 'KANA',      status: 'active',    progress: 0.6,  thumbnail: 'あ' },
    { id: 2,  title: 'Katakana',     description: 'Bảng chữ cái thứ hai của tiếng Nhật',       jlptLevel: 'N5', lessonType: 'KANA',      status: 'available', progress: 0,    thumbnail: 'ア' },
    { id: 3,  title: 'Từ vựng N5',   description: '800 từ vựng N5 thông dụng nhất',            jlptLevel: 'N5', lessonType: 'VOCAB',     status: 'available', progress: 0,    thumbnail: '語' },
    { id: 4,  title: 'Ngữ pháp N5',  description: 'Các mẫu câu ngữ pháp cơ bản N5',           jlptLevel: 'N5', lessonType: 'GRAMMAR',   status: 'available', progress: 0,    thumbnail: '文' },
    { id: 5,  title: 'Kanji N5',     description: '103 Kanji N5 theo bộ thủ',                  jlptLevel: 'N5', lessonType: 'KANJI',     status: 'available', progress: 0,    thumbnail: '漢' },
    { id: 17, title: 'Đọc hiểu N5',  description: 'Bài đọc ngắn & câu hỏi hiểu nội dung N5',  jlptLevel: 'N5', lessonType: 'READING',   status: 'available', progress: 0,    thumbnail: '読' },
    { id: 18, title: 'Nghe hiểu N5', description: 'Hội thoại & câu hỏi nghe hiểu cơ bản N5',  jlptLevel: 'N5', lessonType: 'LISTENING', status: 'available', progress: 0,    thumbnail: '聴' },
    // ── N4 ──
    { id: 6,  title: 'Từ vựng N4',   description: '1.500 từ vựng N4 giao tiếp hàng ngày',     jlptLevel: 'N4', lessonType: 'VOCAB',     status: 'active',    progress: 0.3,  thumbnail: '語' },
    { id: 7,  title: 'Ngữ pháp N4',  description: 'Các mẫu câu N4 phổ biến',                  jlptLevel: 'N4', lessonType: 'GRAMMAR',   status: 'available', progress: 0,    thumbnail: '文' },
    { id: 8,  title: 'Kanji N4',     description: '181 Kanji N4 theo chủ đề',                  jlptLevel: 'N4', lessonType: 'KANJI',     status: 'available', progress: 0,    thumbnail: '漢' },
    { id: 19, title: 'Đọc hiểu N4',  description: 'Đoạn văn thực tế & câu hỏi hiểu nội dung', jlptLevel: 'N4', lessonType: 'READING',   status: 'available', progress: 0,    thumbnail: '読' },
    { id: 20, title: 'Nghe hiểu N4', description: 'Hội thoại hàng ngày & câu hỏi nghe hiểu',  jlptLevel: 'N4', lessonType: 'LISTENING', status: 'available', progress: 0,    thumbnail: '聴' },
    // ── N3 ──
    { id: 9,  title: 'Từ vựng N3',   description: '3.000 từ vựng N3 trung cấp thực tế',       jlptLevel: 'N3', lessonType: 'VOCAB',     status: 'active',    progress: 0.1,  thumbnail: '語' },
    { id: 10, title: 'Ngữ pháp N3',  description: 'Cấu trúc câu phức hợp N3',                 jlptLevel: 'N3', lessonType: 'GRAMMAR',   status: 'available', progress: 0,    thumbnail: '法' },
    { id: 11, title: 'Đọc hiểu N3',  description: 'Phân tích văn bản tin tức & truyện ngắn',  jlptLevel: 'N3', lessonType: 'READING',   status: 'available', progress: 0,    thumbnail: '読' },
    { id: 21, title: 'Kanji N3',     description: '367 Kanji N3 theo nhóm ngữ nghĩa',          jlptLevel: 'N3', lessonType: 'KANJI',     status: 'available', progress: 0,    thumbnail: '漢' },
    { id: 22, title: 'Nghe hiểu N3', description: 'Hội thoại phức tạp & tình huống thực tế',  jlptLevel: 'N3', lessonType: 'LISTENING', status: 'available', progress: 0,    thumbnail: '聴' },
    // ── N2 ──
    { id: 12, title: 'Từ vựng N2',   description: '6.000 từ vựng N2 cao cấp & học thuật',     jlptLevel: 'N2', lessonType: 'VOCAB',     status: 'active',    progress: 0.05, thumbnail: '語' },
    { id: 13, title: 'Ngữ pháp N2',  description: 'Cấu trúc câu nâng cao & lịch sự ngữ',     jlptLevel: 'N2', lessonType: 'GRAMMAR',   status: 'available', progress: 0,    thumbnail: '文' },
    { id: 14, title: 'Đọc hiểu N2',  description: 'Báo chí, luận văn & văn bản chính thức',   jlptLevel: 'N2', lessonType: 'READING',   status: 'available', progress: 0,    thumbnail: '読' },
    { id: 23, title: 'Kanji N2',     description: '367 Kanji N2 theo lĩnh vực học thuật',      jlptLevel: 'N2', lessonType: 'KANJI',     status: 'available', progress: 0,    thumbnail: '漢' },
    { id: 24, title: 'Nghe hiểu N2', description: 'Bài giảng, phỏng vấn & nội dung học thuật', jlptLevel: 'N2', lessonType: 'LISTENING', status: 'available', progress: 0,   thumbnail: '聴' },
    // ── N1 ──
    { id: 15, title: 'Từ vựng N1',   description: '10.000 từ vựng N1 & thành ngữ bản ngữ',    jlptLevel: 'N1', lessonType: 'VOCAB',     status: 'active',    progress: 0.02, thumbnail: '語' },
    { id: 16, title: 'Ngữ pháp N1',  description: 'Cú pháp văn học, học thuật & cổ ngữ',      jlptLevel: 'N1', lessonType: 'GRAMMAR',   status: 'available', progress: 0,    thumbnail: '法' },
    { id: 25, title: 'Đọc hiểu N1',  description: 'Văn bản học thuật, văn học & triết học',   jlptLevel: 'N1', lessonType: 'READING',   status: 'available', progress: 0,    thumbnail: '読' },
    { id: 26, title: 'Kanji N1',     description: '1.162 Kanji N1 — bao gồm kanji hiếm gặp',  jlptLevel: 'N1', lessonType: 'KANJI',     status: 'available', progress: 0,    thumbnail: '漢' },
    { id: 27, title: 'Nghe hiểu N1', description: 'Bài phát biểu, tranh luận & nội dung phức tạp', jlptLevel: 'N1', lessonType: 'LISTENING', status: 'available', progress: 0, thumbnail: '聴' },
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
    setSelectedLevel: (state, action) => { state.selectedLevel = action.payload; },
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

export const { clearError, setSelectedLevel } = studentSlice.actions;
export default studentSlice.reducer;
