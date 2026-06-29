import api from './authService';

// --- Support Tickets (UC-29 · StaffSupportController) -------------------------

// Danh sách ticket. Trả { content, totalElements, totalPages }.
export async function getTickets({ status, category, priority, q, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (status) params.status = status;
  if (category) params.category = category;
  if (priority) params.priority = priority;
  if (q) params.q = q;
  const res = await api.get('/staff/tickets', { params });
  return res.data.data;
}

// Chi tiết + replies[].
export async function getTicketDetail(ticketId) {
  const res = await api.get(`/staff/tickets/${ticketId}`);
  return res.data.data; // TicketDetailResponse
}

// Phản hồi. Backend tự chuyển OPEN/ASSIGNED → IN_PROGRESS + notify student.
// 403 nếu không phải người được giao (và không phải manager).
export async function replyTicket(ticketId, { message, attachmentUrl } = {}) {
  const res = await api.post(`/staff/tickets/${ticketId}/reply`, { message, attachmentUrl });
  return res.data.data; // TicketReplyResponse
}

// Đóng ticket → RESOLVED + audit log + notify student.
export async function closeTicket(ticketId) {
  const res = await api.post(`/staff/tickets/${ticketId}/close`);
  return res.data.data; // TicketResponse
}

// --- Broadcast Notifications (UC-30 · StaffNotificationController) ------------

// Broadcast async — trả { jobId } (202). KHÔNG poll.
// payload: { title, content, notificationType, channel, targetJlptLevel, scheduledAt? }
export async function sendBroadcast(payload) {
  const res = await api.post('/staff/notifications', payload);
  return res.data.data; // { jobId }
}

// --- Staff Students -----------------------------------------------------------
export async function getStaffStudents({ search, level, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (search) params.search = search;
  if (level) params.level = level;
  if (status) params.status = status;
  const res = await api.get('/staff/students', { params });
  return res.data.data;
}

export async function getStudentProgress(studentId) {
  const res = await api.get(`/staff/students/${studentId}/progress`);
  return res.data.data;
}

export async function suspendStudent(studentId, reason = '') {
  const res = await api.post(`/staff/students/${studentId}/suspend`, { reason });
  return res.data.data;
}

export async function activateStudent(studentId) {
  const res = await api.post(`/staff/students/${studentId}/activate`);
  return res.data.data;
}

// --- Staff Dashboard ----------------------------------------------------------
export async function getStaffDashboard() {
  const res = await api.get('/staff/dashboard');
  return res.data.data;
}

// --- Staff Question Bank -----------------------------------------------------

export async function getStaffQuestions({ q, skill, jlptLevel, questionType, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (q) params.q = q;
  if (skill) params.skill = skill;
  if (jlptLevel) params.jlptLevel = jlptLevel;
  if (questionType) params.questionType = questionType;
  if (status) params.status = status;
  const res = await api.get('/staff/questions', { params });
  return res.data.data;
}

export async function getStaffQuestion(questionId) {
  const res = await api.get(`/staff/questions/${questionId}`);
  return res.data.data;
}

export async function createStaffQuestion(data) {
  const res = await api.post('/staff/questions', data);
  return res.data;
}

export async function updateStaffQuestion(questionId, data) {
  const res = await api.put(`/staff/questions/${questionId}`, data);
  return res.data;
}

export async function submitStaffQuestionForReview(questionId) {
  const res = await api.post(`/staff/questions/${questionId}/submit-review`);
  return res.data;
}

// --- Staff Grammar Bank (UC-25) -----------------------------------------------

export async function getStaffGrammars({ jlptLevel, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (jlptLevel) params.jlptLevel = jlptLevel;
  if (status) params.status = status;
  const res = await api.get('/staff/grammar', { params });
  return res.data.data;
}

export async function getStaffGrammar(grammarId) {
  const res = await api.get(`/staff/grammar/${grammarId}`);
  return res.data.data;
}

export async function createStaffGrammar(data) {
  const res = await api.post('/staff/grammar', data);
  return res.data;
}

export async function updateStaffGrammar(grammarId, data) {
  const res = await api.put(`/staff/grammar/${grammarId}`, data);
  return res.data;
}

export async function submitStaffGrammarForReview(grammarId) {
  const res = await api.post(`/staff/grammar/${grammarId}/submit-review`);
  return res.data;
}

// --- Staff Quiz (UC-26) — base /api/staff/assessments -------------------------

export async function getStaffQuizzes({ jlptLevel, status, lessonId, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (jlptLevel) params.jlptLevel = jlptLevel;
  if (status) params.status = status;
  if (lessonId) params.lessonId = lessonId;
  const res = await api.get('/staff/assessments', { params });
  return res.data.data; // { content, totalElements, totalPages }
}

export async function getStaffQuiz(assessmentId) {
  const res = await api.get(`/staff/assessments/${assessmentId}`);
  return res.data.data;
}

export async function createStaffQuiz(data) {
  const res = await api.post('/staff/assessments', data);
  return res.data;
}

export async function updateStaffQuiz(assessmentId, data) {
  const res = await api.put(`/staff/assessments/${assessmentId}`, data);
  return res.data;
}

export async function assignStaffQuizQuestions(assessmentId, assignments) {
  const res = await api.post(`/staff/assessments/${assessmentId}/assign-questions`, { assignments });
  return res.data.data;
}

// --- Staff Exam (UC-28) — base /api/staff/exams -------------------------------

export async function getStaffExams({ jlptLevel, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (jlptLevel) params.jlptLevel = jlptLevel;
  if (status) params.status = status;
  const res = await api.get('/staff/exams', { params });
  return res.data.data; // { content, totalElements, totalPages }
}

export async function getStaffExam(assessmentId) {
  const res = await api.get(`/staff/exams/${assessmentId}`);
  return res.data.data;
}

export async function createStaffExam(data) {
  const res = await api.post('/staff/exams', data);
  return res.data;
}

export async function updateStaffExam(assessmentId, data) {
  const res = await api.put(`/staff/exams/${assessmentId}`, data);
  return res.data;
}

export async function assignStaffExamQuestions(assessmentId, assignments) {
  const res = await api.post(`/staff/exams/${assessmentId}/assign-questions`, { assignments });
  return res.data.data;
}

// --- Assessment / Exam submit review (UC-26 / UC-28) --------------------------
// POST /api/staff/contents/submit-review with contentType 'assessment' | 'exam'.

export async function submitAssessmentForReview(contentType, contentId) {
  const res = await api.post('/staff/contents/submit-review', { contentType, contentId });
  return res.data;
}

// --- Staff Learning Content (UC-27) — base /api/staff -------------------------

export async function createStaffLesson(data) {
  const res = await api.post('/staff/lessons', data);
  return res.data; // ApiResponse { status, message, data: LessonDetailResponse }
}

export async function updateStaffLesson(lessonId, data) {
  const res = await api.put(`/staff/lessons/${lessonId}`, data);
  return res.data;
}

export async function createStaffVocabulary(data) {
  const res = await api.post('/staff/vocabulary', data);
  return res.data;
}

export async function updateStaffVocabulary(vocabularyId, data) {
  const res = await api.put(`/staff/vocabulary/${vocabularyId}`, data);
  return res.data;
}

// --- Vocabulary topics (catalog) — staff chọn/tạo chủ đề khi soạn từ vựng -----

export async function getStaffVocabularyTopics(level) {
  const res = await api.get('/staff/vocabulary-topics', { params: { level } });
  return res.data.data; // [{ topicId, jlptLevel, slug, titleJa, titleVi, displayOrder, status }]
}

export async function createStaffVocabularyTopic(data) {
  const res = await api.post('/staff/vocabulary-topics', data);
  return res.data; // ApiResponse { status, message, data: VocabTopicResponse }
}

export async function createStaffKanji(data) {
  const res = await api.post('/staff/kanji', data);
  return res.data;
}

export async function updateStaffKanji(kanjiId, data) {
  const res = await api.put(`/staff/kanji/${kanjiId}`, data);
  return res.data;
}

// submit-review cho learning content dùng chung endpoint contents/submit-review
// với contentType ∈ { lesson, vocabulary, kanji } — xem submitAssessmentForReview.



// --- Staff Learning Content (UC-27) — GET endpoints ---------------------------------

export async function getStaffLessons({ q, jlptLevel, lessonType, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (q) params.q = q;
  if (jlptLevel) params.jlptLevel = jlptLevel;
  if (lessonType) params.lessonType = lessonType;
  if (status) params.status = status;
  const res = await api.get('/staff/lessons', { params });
  return res.data.data; // { content, totalElements, totalPages }
}

export async function getStaffLesson(lessonId) {
  const res = await api.get(`/staff/lessons/${lessonId}`);
  return res.data.data;
}

export async function getStaffVocabularyList({ q, jlptLevel, topic, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (q) params.q = q;
  if (jlptLevel) params.jlptLevel = jlptLevel;
  if (topic) params.topic = topic;
  if (status) params.status = status;
  const res = await api.get('/staff/vocabulary', { params });
  return res.data.data; // { content, totalElements, totalPages }
}

export async function getStaffVocabularyItem(vocabularyId) {
  const res = await api.get(`/staff/vocabulary/${vocabularyId}`);
  return res.data.data;
}

export async function getStaffKanjiList({ q, jlptLevel, status, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (q) params.q = q;
  if (jlptLevel) params.jlptLevel = jlptLevel;
  if (status) params.status = status;
  const res = await api.get('/staff/kanji', { params });
  return res.data.data; // { content, totalElements, totalPages }
}

export async function getStaffKanjiItem(kanjiId) {
  const res = await api.get(`/staff/kanji/${kanjiId}`);
  return res.data.data;
}

// --- Review Feedback (UC-33 — Staff xem lý do từ chối / yêu cầu chỉnh sửa) ---

export async function getContentReviewFeedback(contentId, contentType) {
  const res = await api.get(`/staff/content/${contentId}/feedback`, { params: { contentType } });
  return res.data.data; // ReviewFeedbackResponse
}
