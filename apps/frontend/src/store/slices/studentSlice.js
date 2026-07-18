import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getDashboard, getVocabHome, getCourses } from '../../api/studentService';

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
    } catch (err) {
      // Backend chưa sẵn sàng → dùng demo để không vỡ UI (giống vocab-home/courses).
      // Lỗi thật (500, network, auth...) phải hiện rõ cho người dùng, không âm thầm che bằng data giả.
      if (err?.response?.status === 404) return DEMO;
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể tải Dashboard.');
    }
  },
);

// ── Vocab Home (SPEC-vocab-home FR-VH-01) ──────────────────────────────────────
// Demo fallback giữ trang chạy được khi backend /students/vocab-home chưa sẵn sàng
// (cùng pattern với fetchDashboardThunk).
const DEMO_VOCAB_HOME = {
  streak: 10,
  weekDays: [true, true, false, true, true, false, false],
  courseTitle: 'N5 Kanji & Vocab',
  level: 'N5',
  subscription: 'FREE',
  lessons: [
    { topicId: 1, slug: 'greetings',  titleJp: 'はじめましょう', subtitleEn: 'Start with Mochi', status: 'active',    thumbnail: 'saku-mascot' },
    { topicId: 2, slug: 'introduce',  titleJp: 'はじめまして',   subtitleEn: 'Nice to meet you', status: 'available', thumbnail: '窓' },
    { topicId: 3, slug: 'family',     titleJp: '家族',           subtitleEn: 'Family & people',  status: 'available', thumbnail: '家' },
    { topicId: 4, slug: 'food',       titleJp: '食べ物',         subtitleEn: 'Food & drink',     status: 'available', thumbnail: null },
    { topicId: 5, slug: 'travel',     titleJp: '旅行',           subtitleEn: 'Travel basics',    status: 'available', thumbnail: null },
  ],
};

export const fetchVocabHomeThunk = createAsyncThunk(
  'student/fetchVocabHome',
  async (level, { rejectWithValue }) => {
    try {
      return await getVocabHome(level);
    } catch (err) {
      // Backend chưa sẵn sàng → dùng demo để không vỡ UI (giống dashboard).
      if (err?.response?.status === 404) return DEMO_VOCAB_HOME;
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể tải trang Từ vựng.');
    }
  },
);

// ── Course List (SPEC-course-list FR-CL-01) ────────────────────────────────────
// Demo fallback giữ trang /courses chạy được khi backend /students/courses chưa
// sẵn sàng (cùng pattern với fetchVocabHomeThunk). VIP đã bỏ khỏi phạm vi.
const DEMO_COURSES = {
  currentLevel: 'N5',
  courses: [
    { jlptLevel: 'N5', title: 'Tiếng Nhật N5', description: 'Hiragana, Katakana, từ vựng & ngữ pháp cơ bản', completedLessons: 5, totalLessons: 40 },
    { jlptLevel: 'N4', title: 'Tiếng Nhật N4', description: 'Giao tiếp hàng ngày, mở rộng từ vựng & Kanji',   completedLessons: 0, totalLessons: 52 },
    { jlptLevel: 'N3', title: 'Tiếng Nhật N3', description: 'Trung cấp: cấu trúc phức hợp, đọc hiểu thực tế',  completedLessons: 0, totalLessons: 60 },
    { jlptLevel: 'N2', title: 'Tiếng Nhật N2', description: 'Cao cấp & học thuật',                            completedLessons: 0, totalLessons: 70 },
    { jlptLevel: 'N1', title: 'Tiếng Nhật N1', description: 'Trình độ bản ngữ',                              completedLessons: 0, totalLessons: 80 },
  ],
};

export const fetchCoursesThunk = createAsyncThunk(
  'student/fetchCourses',
  async (_, { rejectWithValue }) => {
    try {
      return await getCourses();
    } catch (err) {
      // Backend chưa sẵn sàng → dùng demo để không vỡ UI (giống vocab-home).
      if (err?.response?.status === 404) return DEMO_COURSES;
      return rejectWithValue(err?.response?.data?.message ?? 'Không thể tải danh sách khoá học.');
    }
  },
);

const studentSlice = createSlice({
  name: 'student',
  initialState: {
    ...DEMO,
    status: 'idle',
    error: null,
    // Vocab Home — sub-state riêng để không ghi đè lessons/status của Dashboard.
    vocabHome: {
      streak: 0,
      weekDays: [],
      courseTitle: 'N5 Kanji & Vocab',
      level: 'N5',
      subscription: undefined,
      lessons: [],
    },
    vocabHomeStatus: 'idle',
    vocabHomeError: null,
    // Course List — chọn cấp độ JLPT (VIP đã bỏ khỏi phạm vi).
    courses: [],
    currentLevel: 'N5',
    coursesStatus: 'idle',
    coursesError: null,
  },
  reducers: {
    clearError: (state) => { state.error = null; },
    setSelectedLevel: (state, action) => { state.selectedLevel = action.payload; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboardThunk.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchDashboardThunk.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.streak         = action.payload.streak;
        state.weekDays       = action.payload.weekDays;
        state.course         = action.payload.course;
        state.lessons        = action.payload.lessons;
        state.wordCount      = action.payload.wordCount;
        state.daysThisMonth  = action.payload.daysThisMonth;
        // Hiển thị đúng cấp độ học viên (đặt khi onboarding) thay vì mặc định N5.
        if (action.payload.selectedLevel) state.selectedLevel = action.payload.selectedLevel;
      })
      .addCase(fetchDashboardThunk.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })
      // ── Vocab Home ──
      .addCase(fetchVocabHomeThunk.pending, (state) => {
        state.vocabHomeStatus = 'loading';
        state.vocabHomeError = null;
      })
      .addCase(fetchVocabHomeThunk.fulfilled, (state, action) => {
        state.vocabHomeStatus = 'succeeded';
        state.vocabHome = {
          streak:       action.payload.streak ?? 0,
          weekDays:     action.payload.weekDays ?? [],
          courseTitle:  action.payload.courseTitle ?? 'N5 Kanji & Vocab',
          level:        action.payload.level ?? 'N5',
          subscription: action.payload.subscription,
          lessons:      action.payload.lessons ?? [],
        };
      })
      .addCase(fetchVocabHomeThunk.rejected, (state, action) => {
        state.vocabHomeStatus = 'failed';
        state.vocabHomeError = action.payload;
      })
      // ── Course List ──
      .addCase(fetchCoursesThunk.pending, (state) => {
        state.coursesStatus = 'loading';
        state.coursesError = null;
      })
      .addCase(fetchCoursesThunk.fulfilled, (state, action) => {
        state.coursesStatus = 'succeeded';
        state.courses      = action.payload.courses ?? [];
        state.currentLevel = action.payload.currentLevel ?? state.currentLevel;
      })
      .addCase(fetchCoursesThunk.rejected, (state, action) => {
        state.coursesStatus = 'failed';
        state.coursesError = action.payload;
      });
  },
});

export const { clearError, setSelectedLevel } = studentSlice.actions;
export default studentSlice.reducer;
