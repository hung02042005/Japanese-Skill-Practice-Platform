import api from './authService';

// ─── Dashboard ───────────────────────────────────────────────────────────────
export async function getDashboard() {
  const res = await api.get('/students/dashboard');
  return res.data;
}

// ─── Onboarding ──────────────────────────────────────────────────────────────
export async function submitOnboarding({ jlptGoal, dailyMinutes, focusSkills }) {
  const res = await api.post('/students/onboarding', { jlptGoal, dailyMinutes, focusSkills });
  return res.data.data;
}

// ─── Profile ─────────────────────────────────────────────────────────────────
export async function getMyProfile() {
  const res = await api.get('/students/me');
  return res.data.data;
}

export async function updateProfile({ fullName, phone, dateOfBirth, bio }) {
  const res = await api.put('/students/me', { fullName, phone, dateOfBirth, bio });
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
export async function changePassword({ currentPassword, newPassword }) {
  const res = await api.post('/auth/change-password', { currentPassword, newPassword });
  return res.data;
}

// ─── Lessons ─────────────────────────────────────────────────────────────────
export async function getLessonDetail(lessonId) {
  const res = await api.get(`/lessons/${lessonId}`);
  return res.data.data;
}

export async function getNextLesson() {
  const res = await api.get('/students/next-lesson');
  return res.data.data;
}

export async function markProgress(contentType, contentId, status = 'completed', progressPercent = 100) {
  const res = await api.post('/learning-progress', { contentType, contentId, status, progressPercent });
  return res.data.data;
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

export async function getKanjiProgressSummary(level) {
  const res = await api.get('/kanji/progress-summary', { params: { level } });
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

export async function getVocabularyList({ level, topic, search, page = 0, size = 20 } = {}) {
  const params = { page, size };
  if (level)  params.level  = level;
  if (topic)  params.topic  = topic;
  if (search) params.search = search;
  const res = await api.get('/vocabulary', { params });
  return res.data.data;
}

export async function getVocabTopics(level) {
  const res = await api.get('/vocabulary/topics', { params: { level } });
  return res.data.data;
}

export async function markVocabComplete(vocabId) {
  const res = await api.post(`/vocabulary/${vocabId}/complete`);
  return res.data.data;
}

export async function addVocabToFlashcard(vocabId) {
  const res = await api.post('/flashcard/add', { vocabId });
  return res.data.data;
}

// ─── Flashcard SRS ───────────────────────────────────────────────────────────
export async function getFlashcardDecks() {
  const res = await api.get('/flashcard-decks');
  return res.data.data;
}

export async function getFlashcardsDue(size = 50) {
  const res = await api.get('/flashcards', { params: { dueOnly: true, size, page: 0 } });
  return res.data.data;
}

export async function getFlashcardsByDeck(deckName, page = 0, size = 50) {
  const res = await api.get('/flashcards', { params: { deckName, page, size } });
  return res.data.data;
}

export async function revealFlashcard(flashcardId) {
  const res = await api.get(`/flashcards/${flashcardId}/reveal`);
  return res.data.data;
}

export async function rateFlashcard(flashcardId, rating) {
  const res = await api.post(`/flashcards/${flashcardId}/review`, { rating });
  return res.data.data;
}

export async function createDeck(deckName) {
  const res = await api.post('/flashcard-decks', { deckName });
  return res.data.data;
}

export async function deleteDeck(deckName) {
  const res = await api.delete(`/flashcard-decks/${encodeURIComponent(deckName)}`);
  return res.data;
}

export async function addToFlashcard(contentType, contentId, deckName = 'Mặc định') {
  const res = await api.post('/flashcards', { contentType, contentId, deckName });
  return res.data.data;
}

// ─── Assessment ───────────────────────────────────────────────────────────────
export async function getExamList({ level, page = 0, size = 10 } = {}) {
  const params = { type: 'exam', page, size };
  if (level) params.level = level;
  const res = await api.get('/assessments', { params });
  return res.data.data;
}

export async function getAssessmentDetail(assessmentId) {
  const res = await api.get(`/assessments/${assessmentId}`);
  return res.data.data;
}

export async function submitQuizAttempt({ assessmentId, answers }) {
  const res = await api.post('/quiz-attempts', { assessmentId, answers });
  return res.data.data;
}

export async function getQuizAttemptResult(attemptId) {
  const res = await api.get(`/quiz-attempts/${attemptId}`);
  return res.data.data;
}

export async function getMyExamHistory({ page = 0, size = 10 } = {}) {
  const res = await api.get('/quiz-attempts/me', { params: { page, size } });
  return res.data.data;
}

// ─── Progress & Stats ────────────────────────────────────────────────────────
export async function getMyStats() {
  const res = await api.get('/students/me/stats');
  return res.data.data;
}

// ─── Subscription ────────────────────────────────────────────────────────────
export async function getSubscriptionPlans() {
  const res = await api.get('/subscriptions/plans');
  return res.data.data;
}

export async function getCurrentSubscription() {
  const res = await api.get('/subscriptions/me');
  return res.data.data;
}

export async function checkoutSubscription(planId) {
  const res = await api.post('/subscriptions/checkout', { planId });
  return res.data.data;
}

export async function verifySubscription(orderId) {
  const res = await api.get('/subscriptions/verify', { params: { orderId } });
  return res.data.data;
}

// ─── Kana ────────────────────────────────────────────────────────────────────
export async function getKanaList(type) {
  const res = await api.get('/kana', { params: { type } });
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

// ─── Quiz (Practice) ─────────────────────────────────────────────────────────
export async function getQuizList({ level, skill, page = 0, size = 20 } = {}) {
  const params = { level, page, size };
  if (skill) params.skill = skill;
  const res = await api.get('/quizzes', { params });
  return res.data.data;
}

export async function getQuizQuestions(quizId) {
  const res = await api.get(`/quizzes/${quizId}/questions`);
  return res.data.data;
}

export async function submitPracticeQuiz(quizId, answers) {
  const res = await api.post('/quiz-attempts', {
    quizId,
    answers: answers.map(([questionId, selectedOptionId]) => ({ questionId, selectedOptionId })),
  });
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
  return res.data.data;
}

export async function getSpeakingResult(jobId) {
  const res = await api.get(`/speaking/${jobId}`, { timeout: 10000 });
  return res.data.data;
}

// ─── OCR (AI) ────────────────────────────────────────────────────────────────
export async function submitOcr(kanjiId, imageFile) {
  const form = new FormData();
  form.append('kanjiId', String(kanjiId));
  form.append('imageFile', imageFile);
  const res = await api.post('/ai/ocr/submit', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data.data;
}

export async function getOcrResult(jobId) {
  const res = await api.get(`/ai/ocr/${jobId}`);
  return res.data.data;
}

// ─── Certificates ────────────────────────────────────────────────────────────
export async function getMyCertificates() {
  const res = await api.get('/certificates/me');
  return res.data.data;
}

export async function getCertificateProgress() {
  const res = await api.get('/certificates/me/progress');
  return res.data.data;
}

export async function downloadCertificate(certId) {
  const res = await api.get(`/certificates/${certId}/download`);
  return res.data.data;
}
