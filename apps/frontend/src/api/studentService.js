import api from './authService';

// ─── Dashboard ───────────────────────────────────────────────────────────────
export async function getDashboard() {
  const res = await api.get('/students/dashboard');
  return res.data;
}

// ─── Vocab Home (gamified lesson-path) — UC-09 / UC-19 ─────────────────────────
export async function getVocabHome(level) {
  const res = await api.get('/students/vocab-home', { params: level ? { level } : undefined });
  return res.data.data;
}

// ─── Courses (chọn cấp độ JLPT) — UC-08 / UC-09 ───────────────────────────────
export async function getCourses() {
  const res = await api.get('/students/courses');
  return res.data.data;
}

// ─── Onboarding ──────────────────────────────────────────────────────────────
export async function submitOnboarding({ jlptGoal, dailyMinutes, focusSkills }) {
  const res = await api.post('/students/onboarding', { jlptGoal, dailyMinutes, focusSkills });
  return res.data.data;
}

// ─── Profile ─────────────────────────────────────────────────────────────────
export async function updateProfile({ fullName, phone }) {
  const res = await api.put('/students/me', { fullName, phone });
  return res.data.data;
}

export async function uploadAvatar(file) {
  const form = new FormData();
  form.append('avatar', file);
  const res = await api.post('/students/me/avatar', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data.data;
}

// ─── Password ────────────────────────────────────────────────────────────────
export async function changePassword({ currentPassword, newPassword, confirmPassword }) {
  const res = await api.put('/students/me/password', { currentPassword, newPassword, confirmPassword });
  return res.data;
}

// ─── Email ───────────────────────────────────────────────────────────────────
export async function requestEmailChange({ newEmail, currentPassword }) {
  const res = await api.post('/students/me/email/otp', { newEmail, currentPassword });
  return res.data;
}

export async function confirmEmailChange({ newEmail, otpCode }) {
  const res = await api.put('/students/me/email', { newEmail, otpCode });
  return res.data.data;
}

// ─── Lessons ─────────────────────────────────────────────────────────────────
export async function getLessonDetail(lessonId) {
  const res = await api.get(`/lessons/${lessonId}`);
  return res.data.data;
}

export async function markProgress(contentType, contentId, status = 'completed', progressPercent = 100) {
  const res = await api.post('/learning-progress', { contentType, contentId, status, progressPercent });
  return res.data.data;
}

export async function resetProgress(contentType) {
  const res = await api.delete('/learning-progress/reset', { params: { contentType } });
  return res.data;
}

// ─── Core Learning Content ───────────────────────────────────────────────────
export async function getKanjiList({ level, page = 0, size = 50 } = {}) {
  const params = { page, size };
  if (level) params.level = level;
  const res = await api.get('/kanji', { params });
  return res.data.data;
}

export async function getKanjiDetail(kanjiId) {
  const res = await api.get(`/kanji/${kanjiId}`);
  return res.data.data;
}

/**
 * Gửi nét vừa vẽ lên backend để chạy DTW.
 * @param {number} strokeIndex - chỉ số nét (0-based)
 * @param {Array}  userPath    - [[x, y], ...] đã flip Y-up
 * @param {Array}  referencePath - median từ HanziWriter [[x, y], ...]
 */
export async function evaluateKanjiStroke({ strokeIndex, userPath, referencePath }) {
  const res = await api.post('/kanji/writing/evaluate-stroke', {
    strokeIndex,
    userPath,
    referencePath,
  });
  return res.data.data;
}

/**
 * Lưu kết quả toàn bộ phiên luyện viết sau khi hoàn thành.
 */
export async function saveKanjiWritingAttempt({ kanjiId, characterValue, totalStrokes, strokes }) {
  const res = await api.post('/kanji/writing/attempt', {
    kanjiId,
    characterValue,
    totalStrokes,
    strokes,
  });
  return res.data.data;
}

export async function getGrammarList({ level, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (level) params.level = level;
  const res = await api.get('/grammar-points', { params });
  return res.data.data;
}

export async function getGrammarDetail(grammarId) {
  const res = await api.get(`/grammar-points/${grammarId}`);
  return res.data.data;
}

export async function getVocabularyList({ level, topicId, search, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (level)   params.level   = level;
  if (topicId) params.topicId = topicId;
  if (search)  params.search  = search;
  const res = await api.get('/vocabulary', { params });
  return res.data.data;
}

// Trả danh sách chủ đề dạng object { topicId, jlptLevel, slug, titleJa, titleVi, ... }.
export async function getVocabTopics(level) {
  const res = await api.get('/vocabulary/topics', { params: { level } });
  return res.data.data;
}

export async function markVocabComplete(vocabId) {
  return markProgress('vocabulary', vocabId);
}

// ─── Flashcard SRS ───────────────────────────────────────────────────────────
const vocabFlashcardSessionRequests = new Map();

export async function getFlashcardDecks() {
  const res = await api.get('/flashcard-decks');
  return res.data.data;
}

export async function getFlashcardsByDeck(deckId, page = 0, size = 50, q, dueOnly = false, sort) {
  const res = await api.get('/flashcards', {
    params: {
      deckId, page, size,
      q: q || undefined,
      dueOnly: dueOnly || undefined,
      // Tên 'sortBy' (KHÔNG phải 'sort'): 'sort' là param riêng của Spring Pageable → trùng sẽ sinh
      // 2 mệnh đề ORDER BY ở backend (500). BE đọc @RequestParam("sortBy").
      sortBy: sort && sort !== 'due' ? sort : undefined,
    },
  });
  return res.data.data;
}

// Gỡ hàng loạt thẻ khỏi sổ tay (3B). ids: number[] → trả số thẻ đã gỡ.
export async function bulkDeleteFlashcards(ids) {
  const res = await api.post('/flashcards/bulk-delete', { ids });
  return res.data.data;
}

export async function getVocabFlashcardSession({ topicId, newLimit } = {}) {
  const params = { topicId, newLimit };
  const requestKey = `topic:${topicId}:${newLimit ?? ''}`;

  if (vocabFlashcardSessionRequests.has(requestKey)) {
    return vocabFlashcardSessionRequests.get(requestKey);
  }

  // POST (không phải GET) vì build phiên có side-effect: backend tạo deck/thẻ MỚI cho từ được chọn.
  const request = api
    .post('/flashcards/session', null, { params })
    .then((res) => res.data.data)
    .finally(() => vocabFlashcardSessionRequests.delete(requestKey));

  vocabFlashcardSessionRequests.set(requestKey, request);
  return request;
}

export async function submitFlashcardReview(
  flashcardId,
  { rating, selectedOptionId, isLastCardInSession, sessionId },
) {
  const res = await api.post(`/flashcards/${flashcardId}/review`, {
    rating,
    selectedOptionId,
    isLastCardInSession,
    sessionId,
  });
  return res.data.data;
}

export async function addWrongWordsToReviewDeck(items) {
  const res = await api.post('/flashcards/review-deck/add', { items, reason: 'wrong' });
  return res.data.data;
}

// Gỡ một thẻ khỏi sổ (soft-delete card) — SPEC-notebook §5
export async function removeFlashcardCard(flashcardId) {
  const res = await api.delete(`/flashcards/${flashcardId}`);
  return res.data;
}

// Lưu thủ công 1 từ vào Sổ tay "Từ cần ôn lại" — dùng chung review-deck/add (SPEC-dictionary §5)
export async function saveToNotebook(contentType, contentId) {
  const res = await api.post('/flashcards/review-deck/add', {
    items: [{ contentType: contentType.toUpperCase(), contentId }],
    reason: 'manual',
  });
  return res.data.data;
}

// ─── Dictionary ──────────────────────────────────────────────────────────────
// Tìm kiếm gom nhóm (UC-16) — backend lọc status='published' (FR-DICT-05)
export async function searchDictionary(q, jlptLevel, type) {
  const res = await api.get('/dictionary/search', { params: { q, jlptLevel, type } });
  return res.data.data;
}

// Phân trang theo 1 loại — nút "Xem thêm" (1B). page 0 = 10 mục overview, page 1+ nối tiếp.
// Trả { type, items, hasMore }.
export async function searchDictionaryByType(q, type, { jlptLevel, page = 1, size = 10 } = {}) {
  const res = await api.get(`/dictionary/search/${type}`, {
    params: { q, jlptLevel, page, size },
  });
  return res.data.data;
}

// ─── Assessment ───────────────────────────────────────────────────────────────
export async function getExamList({ level, page = 0, size = 10 } = {}) {
  const params = { type: 'exam', page, size };
  if (level) params.level = level;
  const res = await api.get('/assessments', { params });
  return res.data.data;
}

export async function startAssessment(assessmentId) {
  const res = await api.post(`/assessments/${assessmentId}/start`);
  return res.data.data;
}

export async function submitAssessment(assessmentId, { attemptId, isAutoSubmit = false, answers }) {
  const res = await api.post(`/assessments/${assessmentId}/submit`, { attemptId, isAutoSubmit, answers });
  return res.data.data;
}

export async function getExamReview(attemptId) {
  const res = await api.get(`/test-attempts/${attemptId}/review`);
  return res.data.data;
}

export async function getMyExamHistory({ page = 0, size = 10 } = {}) {
  const res = await api.get('/test-attempts', { params: { type: 'exam', page, size } });
  return res.data.data;
}

// ─── Progress & Stats ────────────────────────────────────────────────────────
export async function getMyStats() {
  const res = await api.get('/students/me/stats');
  return res.data.data;
}

// ─── Kana ────────────────────────────────────────────────────────────────────
export async function getKanaList(script) {
  const res = await api.get('/kana', { params: { script } });
  return res.data.data;
}

export async function markKanaComplete(kanaId) {
  const res = await api.post('/learning-progress', {
    contentType: 'kana',
    contentId: kanaId,
    status: 'completed',
    progressPercent: 100,
  });
  return res.data.data;
}

// ─── Quiz (Practice) — dùng chung backend assessment (assessment_type='quiz') ──
// Danh sách quiz đã published; làm bài qua startAssessment/submitAssessment như exam.
export async function getQuizzes({ level, page = 0, size = 20 } = {}) {
  const params = { type: 'quiz', page, size };
  if (level) params.level = level;
  const res = await api.get('/assessments', { params });
  return res.data.data; // { content, totalElements, totalPages }
}

// ─── Reading (Đọc hiểu) — UC-14 ──────────────────────────────────────────────
// Danh sách bài đọc theo cấp độ. Trả Page { content, totalElements, totalPages }.
export async function getReadingLessons({ level, page = 0, size = 50 } = {}) {
  const res = await api.get('/lessons', { params: { type: 'reading', level, page, size } });
  return res.data.data;
}

// Chi tiết 1 bài đọc: { id, title, jlptLevel, passageText, questions[] }.
export async function getReadingDetail(lessonId) {
  const res = await api.get(`/lessons/${lessonId}/reading`);
  return res.data.data;
}

// Nộp bài đọc. answers: [{ questionId, selectedOption }]. Chấm điểm tại backend.
// Trả { attemptId, score, maxScore, results: [{ questionId, isCorrect, correctOption, explanation }] }.
export async function submitReading(lessonId, answers) {
  const res = await api.post(`/lessons/${lessonId}/submit`, { attemptType: 'reading', answers });
  return res.data.data;
}

// ─── Speaking (AI) ───────────────────────────────────────────────────────────
export async function getSpeakingExercises(level) {
  const res = await api.get('/speaking/exercises', { params: { level } });
  return res.data.data;
}

export async function submitSpeakingAudio(exerciseId, audioBlob) {
  const formData = new FormData();
  formData.append('exerciseId', exerciseId);
  formData.append('audio', audioBlob, 'recording.webm');
  const res = await api.post('/speaking/submit', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000,
  });
  return res.data.data; // { jobId, status: 'PENDING' } — bài chờ giáo viên chấm
}

// ─── Hỗ trợ / Ticket — UC-29 (SupportController) ──────────────────────────────

// Danh sách ticket của tôi. status optional. Trả { content, totalElements, totalPages }.
export async function getMyTickets({ status, page = 0, size = 10 } = {}) {
  const params = { page, size };
  if (status) params.status = status;
  const res = await api.get('/support/tickets', { params });
  return res.data.data;
}

// Chi tiết 1 ticket + replies[]. 403 nếu không phải ticket của mình; 404 nếu không tồn tại.
export async function getTicketDetail(ticketId) {
  const res = await api.get(`/support/tickets/${ticketId}`);
  return res.data.data; // TicketDetailResponse
}

// Tạo ticket mới. priority: 'low'|'normal'|'high'|'urgent' (mặc định normal).
export async function createTicket({ subject, content, category, priority }) {
  const res = await api.post('/support/tickets', { subject, content, category, priority });
  return res.data.data; // TicketResponse (status 201)
}

// Gửi phản hồi vào ticket của mình. attachmentUrl optional (≤500 ký tự).
export async function replyTicket(ticketId, { message, attachmentUrl } = {}) {
  const res = await api.post(`/support/tickets/${ticketId}/reply`, { message, attachmentUrl });
  return res.data.data; // TicketReplyResponse
}

// Học viên tự đóng ticket của mình → status 'closed'.
export async function closeMyTicket(ticketId) {
  const res = await api.post(`/support/tickets/${ticketId}/close`);
  return res.data.data; // TicketResponse
}

// ─── Thông báo — UC-30 (NotificationController) ───────────────────────────────

// Danh sách thông báo. Trả { content, totalElements, totalPages, unreadCount }.
export async function getMyNotifications({ page = 0, size = 20 } = {}) {
  const res = await api.get('/notifications', { params: { page, size } });
  return res.data.data;
}

// Đánh dấu 1 thông báo đã đọc. 403 nếu không phải của mình.
export async function markNotificationRead(notificationId) {
  const res = await api.post(`/notifications/${notificationId}/read`);
  return res.data;
}

// Đánh dấu tất cả đã đọc → { markedCount }.
export async function markAllNotificationsRead() {
  const res = await api.post('/notifications/read-all');
  return res.data.data;
}
