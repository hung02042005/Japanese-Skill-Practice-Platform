"""Builds the 12 drawio source files for SDS §II Code Designs (6 features x
Class Diagram + Sequence Diagram). Run from repo root:
    python docs/07-Release-Documents/diagrams/sds/build_all.py
Then export all of them to PNG with the draw.io CLI (see build_sds.py).
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from gen_diagrams import build_class_diagram, build_sequence_diagram

OUT = Path(__file__).parent

# --------------------------------------------------------------------------- #
# 1. Authentication & Login
# --------------------------------------------------------------------------- #

build_class_diagram(
    OUT / "class-auth.drawio",
    boxes=[
        {"id": "c1", "name": "AuthController", "x": 40, "y": 40, "w": 300, "header_color": "#ffe6cc", "methods": [
            "+ login(LoginRequest): ApiResponse~LoginApiResponse~",
            "+ register(RegisterRequest): ApiResponse~StudentResponse~",
            "+ refresh(RefreshTokenRequest): ApiResponse~RefreshTokenResponse~",
            "+ logout(LogoutRequest): ApiResponse~Void~",
            "+ verifyEmail(VerifyEmailRequest): ApiResponse~Void~",
            "+ forgotPassword(ForgotPasswordRequest): ApiResponse~Void~",
            "+ resetPassword(ResetPasswordRequest): ApiResponse~Void~",
            "+ googleLogin(GoogleTokenRequest): ApiResponse~AuthResponse~",
        ]},
        {"id": "c2", "name": "AuthenticationService", "x": 400, "y": 40, "w": 300, "methods": [
            "+ checkAccountType(email, ip): AccountTypeResponse",
            "+ login(LoginRequest, ip): LoginApiResponse",
            "+ loginStaff(LoginRequest, ip): LoginApiResponse",
            "+ refresh(RefreshTokenRequest): RefreshTokenResponse",
            "+ logout(LogoutRequest): void",
            "+ loginWithGoogle(GoogleTokenRequest): AuthResponse",
        ]},
        {"id": "c3", "name": "RegistrationService", "x": 760, "y": 40, "w": 260, "methods": [
            "+ register(RegisterRequest): StudentResponse",
        ]},
        {"id": "c4", "name": "PasswordResetService", "x": 760, "y": 220, "w": 260, "methods": [
            "+ forgotPassword(email): void",
            "+ resetPassword(token, newPassword): void",
        ]},
        {"id": "c5", "name": "JwtProvider", "x": 400, "y": 300, "w": 260, "methods": [
            "+ generateAccessToken(actorType, actorId): String",
            "+ generateRefreshToken(actorType, actorId): String",
            "+ validateToken(token): boolean",
        ]},
        {"id": "c6", "name": "AuthTokenRepository", "x": 40, "y": 300, "w": 300, "header_color": "#d5e8d4", "methods": [
            "+ findByTokenHashAndTokenType(hash, type): Optional~AuthToken~",
        ]},
        {"id": "c7", "name": "StudentUserRepository", "x": 40, "y": 420, "w": 300, "header_color": "#d5e8d4", "methods": []},
        {"id": "c8", "name": "StaffUserRepository", "x": 40, "y": 520, "w": 300, "header_color": "#d5e8d4", "methods": []},
        {"id": "c9", "name": "AdminUserRepository", "x": 40, "y": 620, "w": 300, "header_color": "#d5e8d4", "methods": []},
    ],
    edges=[
        {"src": "c1", "tgt": "c2"}, {"src": "c1", "tgt": "c3"}, {"src": "c1", "tgt": "c4"},
        {"src": "c2", "tgt": "c5"}, {"src": "c2", "tgt": "c6"}, {"src": "c2", "tgt": "c7"},
        {"src": "c2", "tgt": "c8"}, {"src": "c2", "tgt": "c9"}, {"src": "c3", "tgt": "c7"},
    ],
    page_w=1200, page_h=800,
)

build_sequence_diagram(
    OUT / "seq-auth.drawio",
    participants=[("student", "Student", 20), ("fe", "Frontend", 220), ("ac", "AuthController", 420),
                  ("as", "AuthenticationService", 620), ("am", "AuthenticationManager", 850),
                  ("jwt", "JwtProvider", 1050), ("db", "SQL Server", 1230)],
    messages=[
        {"src_id": "student", "tgt_id": "fe", "y": 110, "label": "1: enter email/password"},
        {"src_id": "fe", "tgt_id": "ac", "y": 150, "label": "2: POST /api/auth/login"},
        {"src_id": "ac", "tgt_id": "as", "y": 190, "label": "3: login(request, ip)"},
        {"src_id": "as", "tgt_id": "am", "y": 230, "label": "4: authenticate(email, password)"},
        {"src_id": "am", "tgt_id": "db", "y": 270, "label": "5: SELECT * FROM student_users WHERE email=?"},
        {"src_id": "db", "tgt_id": "am", "y": 310, "label": "6: return row", "dashed": True, "arrow": "open"},
        {"src_id": "am", "tgt_id": "as", "y": 350, "label": "7: Authentication OK", "dashed": True, "arrow": "open"},
        {"src_id": "as", "tgt_id": "jwt", "y": 390, "label": "8: generateAccessToken()/generateRefreshToken()"},
        {"src_id": "as", "tgt_id": "db", "y": 430, "label": "9: INSERT INTO auth_tokens(...)"},
        {"src_id": "as", "tgt_id": "ac", "y": 470, "label": "10: LoginApiResponse", "dashed": True, "arrow": "open"},
        {"src_id": "ac", "tgt_id": "fe", "y": 510, "label": "11: 200 OK { accessToken, refreshToken, student }", "dashed": True, "arrow": "open"},
    ],
    frames=[{"label": "alt wrong password -> 401 / correct password -> continue", "x1": 200, "x2": 1300, "y1": 340, "y2": 530}],
    page_w=1400, page_h=600,
)

# --------------------------------------------------------------------------- #
# 2. Quiz Submission (Assessment)
# --------------------------------------------------------------------------- #

build_class_diagram(
    OUT / "class-quiz.drawio",
    boxes=[
        {"id": "c1", "name": "StudentAssessmentController", "x": 40, "y": 40, "w": 320, "header_color": "#ffe6cc", "methods": [
            "+ listAssessments(...): ApiResponse",
            "+ startAssessment(id, student): ApiResponse",
            "+ submitAssessment(id, request, student): ApiResponse",
        ]},
        {"id": "c2", "name": "QuizService", "x": 420, "y": 40, "w": 300, "methods": [
            "+ startQuiz(quizId, student): ExamStartResponse",
            "+ submitQuiz(quizId, studentId, attemptId, answers): ScoreResponse",
            "- calculateScore(attempt, assessment, answers): ScoreResponse",
        ]},
        {"id": "c3", "name": "AssessmentRepository", "x": 780, "y": 40, "w": 300, "header_color": "#d5e8d4", "methods": [
            "+ findByIdAndAssessmentTypeAndStatus(...): Optional~Assessment~",
        ]},
        {"id": "c4", "name": "QuestionAssignmentRepository", "x": 780, "y": 160, "w": 300, "header_color": "#d5e8d4", "methods": [
            "+ findByParentTypeAndParentIdOrderByDisplayOrder(...): List~QuestionAssignment~",
        ]},
        {"id": "c5", "name": "TestAttemptRepository", "x": 780, "y": 280, "w": 300, "header_color": "#d5e8d4", "methods": [
            "+ findByIdForUpdate(attemptId): Optional~TestAttempt~",
        ]},
        {"id": "c6", "name": "AttemptAnswerRepository", "x": 780, "y": 400, "w": 300, "header_color": "#d5e8d4", "methods": [
            "+ saveAll(List~AttemptAnswer~): List~AttemptAnswer~",
        ]},
        {"id": "c7", "name": "Assessment", "x": 40, "y": 220, "w": 260, "header_color": "#dae8fc", "methods": [
            "+ Long id", "+ AssessmentType assessmentType", "+ Integer durationMin",
            "+ BigDecimal totalScore", "+ BigDecimal passScore",
        ]},
        {"id": "c8", "name": "TestAttempt", "x": 40, "y": 400, "w": 260, "methods": [
            "+ Long id", "+ AttemptStatus status", "+ LocalDateTime startedAt", "+ BigDecimal maxScore",
        ]},
        {"id": "c9", "name": "AttemptAnswer", "x": 40, "y": 560, "w": 260, "methods": [
            "+ Long id", "+ String selectedOption", "+ Boolean isCorrect",
        ]},
    ],
    edges=[
        {"src": "c1", "tgt": "c2"}, {"src": "c2", "tgt": "c3"}, {"src": "c2", "tgt": "c4"},
        {"src": "c2", "tgt": "c5"}, {"src": "c2", "tgt": "c6"}, {"src": "c2", "tgt": "c7"},
        {"src": "c2", "tgt": "c8"}, {"src": "c2", "tgt": "c9"},
    ],
    page_w=1200, page_h=800,
)

build_sequence_diagram(
    OUT / "seq-quiz.drawio",
    participants=[("student", "Student", 20), ("fe", "Frontend", 220), ("sac", "StudentAssessmentController", 420),
                  ("qs", "QuizService", 680), ("tar", "TestAttemptRepository", 900),
                  ("aar", "AttemptAnswerRepository", 1100), ("db", "SQL Server", 1300)],
    messages=[
        {"src_id": "student", "tgt_id": "fe", "y": 110, "label": "1: click \"Submit\""},
        {"src_id": "fe", "tgt_id": "sac", "y": 150, "label": "2: POST /api/assessments/{id}/submit"},
        {"src_id": "sac", "tgt_id": "qs", "y": 190, "label": "3: submitQuiz(quizId, studentId, attemptId, answers)"},
        {"src_id": "qs", "tgt_id": "tar", "y": 230, "label": "4: findByIdForUpdate(attemptId)"},
        {"src_id": "tar", "tgt_id": "db", "y": 270, "label": "5: SELECT ... FOR UPDATE"},
        {"src_id": "db", "tgt_id": "tar", "y": 310, "label": "6: locked row", "dashed": True, "arrow": "open"},
        {"src_id": "qs", "tgt_id": "qs", "y": 350, "label": "7: calculateScore(attempt, assessment, answers)"},
        {"src_id": "qs", "tgt_id": "aar", "y": 390, "label": "8: saveAll(attemptAnswers)"},
        {"src_id": "aar", "tgt_id": "db", "y": 430, "label": "9: INSERT INTO attempt_answers(...)"},
        {"src_id": "qs", "tgt_id": "tar", "y": 470, "label": "10: save(status=SUBMITTED, score=...)"},
        {"src_id": "tar", "tgt_id": "db", "y": 510, "label": "11: UPDATE test_attempts SET status='SUBMITTED'"},
        {"src_id": "qs", "tgt_id": "sac", "y": 550, "label": "12: ScoreResponse", "dashed": True, "arrow": "open"},
        {"src_id": "sac", "tgt_id": "fe", "y": 590, "label": "13: 200 OK { score, maxScore, results[] }", "dashed": True, "arrow": "open"},
    ],
    frames=[{"label": "alt status!=IN_PROGRESS->409 / student_id mismatch->403 / valid->continue", "x1": 400, "x2": 1350, "y1": 340, "y2": 610}],
    page_w=1500, page_h=680,
)

# --------------------------------------------------------------------------- #
# 3. Flashcard SRS
# --------------------------------------------------------------------------- #

build_class_diagram(
    OUT / "class-flashcard.drawio",
    boxes=[
        {"id": "c1", "name": "StudentFlashcardController", "x": 40, "y": 40, "w": 300, "header_color": "#ffe6cc", "methods": [
            "+ getSession(studentId, deckId, topicId, newLimit): ApiResponse~SessionResponse~",
            "+ submitReview(id, studentId, ReviewRequest): ApiResponse~ReviewResultResponse~",
        ]},
        {"id": "c2", "name": "FlashcardSrsService", "x": 400, "y": 40, "w": 320, "methods": [
            "+ submitReview(flashcardId, studentId, ReviewRequest): ReviewResultResponse",
            "+ getSession(studentId, deckId, topicId, newLimit): SessionResponse",
            "- applySm2(Flashcard, LastRating): void",
            "- getSessionLocked(studentId, deckId, topicId, newLimit): SessionResponse",
        ]},
        {"id": "c3", "name": "FlashcardDeckSupport", "x": 780, "y": 40, "w": 300, "methods": [
            "+ ownCardOrThrow(flashcardId, studentId): Flashcard",
            "+ ownDeckOrThrow(studentId, deckId): FlashcardDeck",
            "+ getOrCreateDeck(student, name): FlashcardDeck",
        ]},
        {"id": "c4", "name": "FlashcardResolver", "x": 780, "y": 220, "w": 300, "methods": [
            "+ loadContentMaps(List~Flashcard~): ContentMaps",
        ]},
        {"id": "c5", "name": "FlashcardRepository", "x": 400, "y": 260, "w": 320, "header_color": "#d5e8d4", "methods": [
            "+ findWrongVocabCardsInSession(studentId, sessionId): List~Flashcard~",
            "+ findByStudentAndDeck(studentId, deckId): List~Flashcard~",
            "+ findByStudentAndContentIds(studentId, type, ids): List~Flashcard~",
        ]},
        {"id": "c6", "name": "Flashcard", "x": 40, "y": 260, "w": 300, "methods": [
            "+ Long id", "+ ContentType contentType", "+ LastRating lastRating",
            "+ Integer intervalDays", "+ BigDecimal easeFactor", "+ Integer repetitionCount",
            "+ LocalDate nextReviewDate", "+ String lastSessionId",
        ]},
    ],
    edges=[
        {"src": "c1", "tgt": "c2"}, {"src": "c2", "tgt": "c3"}, {"src": "c2", "tgt": "c4"},
        {"src": "c2", "tgt": "c5"}, {"src": "c2", "tgt": "c6"},
    ],
    page_w=1200, page_h=650,
)

build_sequence_diagram(
    OUT / "seq-flashcard.drawio",
    participants=[("student", "Student", 20), ("fe", "Frontend", 220), ("sfc", "StudentFlashcardController", 420),
                  ("srs", "FlashcardSrsService", 680), ("ds", "FlashcardDeckSupport", 900),
                  ("fr", "FlashcardRepository", 1100), ("db", "SQL Server", 1300)],
    messages=[
        {"src_id": "student", "tgt_id": "fe", "y": 110, "label": "1: flip card / choose answer"},
        {"src_id": "fe", "tgt_id": "sfc", "y": 150, "label": "2: POST /api/flashcards/{id}/review"},
        {"src_id": "sfc", "tgt_id": "srs", "y": 190, "label": "3: submitReview(flashcardId, studentId, request)"},
        {"src_id": "srs", "tgt_id": "ds", "y": 230, "label": "4: ownCardOrThrow(flashcardId, studentId)"},
        {"src_id": "ds", "tgt_id": "db", "y": 270, "label": "5: SELECT * FROM flashcards WHERE ..."},
        {"src_id": "srs", "tgt_id": "srs", "y": 340, "label": "6: applySm2(card, rating)"},
        {"src_id": "srs", "tgt_id": "fr", "y": 380, "label": "7: save(card)"},
        {"src_id": "fr", "tgt_id": "db", "y": 420, "label": "8: UPDATE flashcards SET ease_factor=...,next_review_date=..."},
        {"src_id": "srs", "tgt_id": "fr", "y": 460, "label": "9 (opt, last card in session): findWrongVocabCardsInSession(...)"},
        {"src_id": "srs", "tgt_id": "sfc", "y": 500, "label": "10: ReviewResultResponse", "dashed": True, "arrow": "open"},
        {"src_id": "sfc", "tgt_id": "fe", "y": 540, "label": "11: 200 OK { correct, nextReviewDate, wrongWords[] }", "dashed": True, "arrow": "open"},
    ],
    frames=[{"label": "alt does not own card -> 404 / valid ownership -> continue", "x1": 400, "x2": 1350, "y1": 300, "y2": 560}],
    page_w=1500, page_h=630,
)

# --------------------------------------------------------------------------- #
# 4. Kanji Writing Evaluation (OCR/DTW)
# --------------------------------------------------------------------------- #

build_class_diagram(
    OUT / "class-kanji.drawio",
    boxes=[
        {"id": "c1", "name": "StudentKanjiController", "x": 40, "y": 40, "w": 300, "header_color": "#ffe6cc", "methods": [
            "+ evaluateStroke(EvaluateRequest): ApiResponse~EvaluateResponse~",
            "+ saveWritingAttempt(AttemptRequest, studentId): ApiResponse~AttemptResponse~",
        ]},
        {"id": "c2", "name": "KanjiWritingService", "x": 420, "y": 40, "w": 300, "interface": True, "methods": [
            "+ evaluateStroke(EvaluateRequest): EvaluateResponse",
            "+ saveAttempt(AttemptRequest, studentId): AttemptResponse",
        ]},
        {"id": "c3", "name": "KanjiWritingServiceImpl", "x": 420, "y": 220, "w": 320, "methods": [
            "+ evaluateStroke(EvaluateRequest): EvaluateResponse",
            "+ saveAttempt(AttemptRequest, studentId): AttemptResponse",
            "- computeDtw(List~double[]~, List~double[]~): double",
            "- normalize(List~double[]~): List~double[]~",
            "- qualityFromDtw(score): String",
        ]},
        {"id": "c4", "name": "KanjiWritingAttemptRepository", "x": 800, "y": 220, "w": 320, "header_color": "#d5e8d4", "methods": [
            "+ save(KanjiWritingAttempt): KanjiWritingAttempt",
        ]},
        {"id": "c5", "name": "KanjiWritingAttempt", "x": 800, "y": 380, "w": 300, "methods": [
            "+ Long id", "+ Long kanjiId", "+ String characterValue",
            "+ Double avgDtwScore", "+ String finalQuality", "+ String strokeDetails",
        ]},
    ],
    edges=[
        {"src": "c3", "tgt": "c2", "arrow": "block", "label": "implements"},
        {"src": "c1", "tgt": "c2"}, {"src": "c3", "tgt": "c4"}, {"src": "c3", "tgt": "c5"},
    ],
    page_w=1200, page_h=650,
)

build_sequence_diagram(
    OUT / "seq-kanji.drawio",
    participants=[("student", "Student", 20), ("fe", "Frontend (Canvas)", 220), ("skc", "StudentKanjiController", 460),
                  ("svc", "KanjiWritingServiceImpl", 720), ("ar", "KanjiWritingAttemptRepository", 980),
                  ("db", "SQL Server", 1200)],
    messages=[
        {"src_id": "student", "tgt_id": "fe", "y": 110, "label": "1: draw 1 stroke on canvas (repeat per stroke)"},
        {"src_id": "fe", "tgt_id": "skc", "y": 150, "label": "2: POST /api/kanji/writing/evaluate-stroke"},
        {"src_id": "skc", "tgt_id": "svc", "y": 190, "label": "3: evaluateStroke(request)"},
        {"src_id": "svc", "tgt_id": "svc", "y": 230, "label": "4: normalize + downsample + computeDtw"},
        {"src_id": "svc", "tgt_id": "skc", "y": 270, "label": "5: EvaluateResponse{dtwScore,quality}", "dashed": True, "arrow": "open"},
        {"src_id": "skc", "tgt_id": "fe", "y": 310, "label": "6: 200 OK (instant feedback, not persisted yet)", "dashed": True, "arrow": "open"},
        {"src_id": "student", "tgt_id": "fe", "y": 380, "label": "7: character complete (all strokes drawn)"},
        {"src_id": "fe", "tgt_id": "skc", "y": 420, "label": "8: POST /api/kanji/writing/attempt {strokes[]}"},
        {"src_id": "skc", "tgt_id": "svc", "y": 460, "label": "9: saveAttempt(request, studentId)"},
        {"src_id": "svc", "tgt_id": "svc", "y": 500, "label": "10: avgDtwScore=average(strokes.dtwScore)"},
        {"src_id": "svc", "tgt_id": "ar", "y": 540, "label": "11: save(KanjiWritingAttempt)"},
        {"src_id": "ar", "tgt_id": "db", "y": 580, "label": "12: INSERT INTO kanji_writing_attempts(...)"},
        {"src_id": "svc", "tgt_id": "skc", "y": 620, "label": "13: AttemptResponse{finalQuality,avgDtwScore}", "dashed": True, "arrow": "open"},
    ],
    frames=[{"label": "loop for each stroke drawn by Student", "x1": 200, "x2": 800, "y1": 90, "y2": 330}],
    page_w=1400, page_h=700,
)

# --------------------------------------------------------------------------- #
# 5. Speaking Submission Grading (Shadowing) — corrected: no AI grading step
#    (removed by commit db0d1648, "drop AI auto-grading, keep teacher grading only").
#    Student submit lives in feature/speaking (SpeakingController/SpeakingService),
#    Staff grading lives in feature/support (StaffGradingController/SupportTicketService).
# --------------------------------------------------------------------------- #

build_class_diagram(
    OUT / "class-speaking.drawio",
    boxes=[
        {"id": "c1", "name": "SpeakingController", "x": 40, "y": 40, "w": 300, "header_color": "#ffe6cc", "methods": [
            "+ listExercises(level, userDetails): ApiResponse~List~SpeakingExerciseResponse~~",
            "+ submit(exerciseId, audio, userDetails): ApiResponse~SpeakingSubmitResponse~",
            "+ getResult(jobId, userDetails): ApiResponse~SpeakingResultResponse~",
        ]},
        {"id": "c2", "name": "SpeakingService", "x": 400, "y": 40, "w": 320, "methods": [
            "+ getExercises(level, studentId): List~SpeakingExerciseResponse~",
            "+ submit(exerciseId, audio, student): SpeakingSubmitResponse",
            "+ getResult(jobId, studentId): SpeakingResultResponse",
            "- toGradedResponse(StudentSubmission): SpeakingResultResponse",
        ]},
        {"id": "c3", "name": "SpeakingAudioStorageService", "x": 780, "y": 40, "w": 320, "methods": [
            "+ store(MultipartFile, studentId): StoredAudio",
        ]},
        {"id": "c4", "name": "StaffGradingController", "x": 40, "y": 260, "w": 300, "header_color": "#ffe6cc", "methods": [
            "+ listSubmissions(type, status, page, size): ApiResponse~Map~",
            "+ getSubmission(submissionId): ApiResponse~SubmissionResponse~",
            "+ gradeSubmission(Authentication, submissionId, ManualGradeRequest): ApiResponse~GradeResponse~",
        ]},
        {"id": "c5", "name": "SupportTicketService", "x": 400, "y": 260, "w": 320, "methods": [
            "+ getAllSubmissions(type, status, page, size): Page~SubmissionResponse~",
            "+ getSubmissionDetail(submissionId): SubmissionResponse",
            "+ manualGrade(submissionId, actorEmail, ManualGradeRequest): GradeResponse",
        ]},
        {"id": "c6", "name": "StudentSubmissionRepository", "x": 780, "y": 260, "w": 320, "header_color": "#d5e8d4", "methods": [
            "+ findAllByTypeAndFilters(type, status, Pageable): Page~StudentSubmission~",
            "+ findByIdAndStudent_Id(jobId, studentId): Optional~StudentSubmission~",
            "+ findSpeakingStats(studentId, type, ids): List~Object[]~",
        ]},
        {"id": "c7", "name": "StudentSubmission", "x": 400, "y": 460, "w": 320, "methods": [
            "+ Long id", "+ SubmissionType submissionType", "+ SubmissionStatus status",
            "+ String recordingUrl", "+ BigDecimal manualScore", "+ String manualFeedback",
            "+ StaffUser gradedBy",
        ]},
    ],
    edges=[
        {"src": "c1", "tgt": "c2"}, {"src": "c2", "tgt": "c3"}, {"src": "c2", "tgt": "c6"},
        {"src": "c4", "tgt": "c5"}, {"src": "c5", "tgt": "c6"}, {"src": "c2", "tgt": "c7"}, {"src": "c5", "tgt": "c7"},
    ],
    page_w=1300, page_h=750,
)

build_sequence_diagram(
    OUT / "seq-speaking.drawio",
    participants=[("student", "Student", 20), ("fe1", "Frontend (Speaking)", 200), ("sc", "SpeakingController", 460),
                  ("ss", "SpeakingService", 680), ("db", "SQL Server", 880),
                  ("staff", "Staff", 1040), ("fe2", "Frontend (StaffGrading)", 1180),
                  ("sgc", "StaffGradingController", 1440)],
    messages=[
        {"src_id": "student", "tgt_id": "fe1", "y": 110, "label": "1: record audio, choose exercise"},
        {"src_id": "fe1", "tgt_id": "sc", "y": 150, "label": "2: POST /api/speaking/submit (multipart audio)"},
        {"src_id": "sc", "tgt_id": "ss", "y": 190, "label": "3: submit(exerciseId, audio, student)"},
        {"src_id": "ss", "tgt_id": "db", "y": 230, "label": "4: INSERT INTO student_submissions (status=PENDING)"},
        {"src_id": "ss", "tgt_id": "sc", "y": 270, "label": "5: SpeakingSubmitResponse{jobId,status=PENDING}", "dashed": True, "arrow": "open"},
        {"src_id": "sc", "tgt_id": "fe1", "y": 310, "label": "6: 202 Accepted { jobId }", "dashed": True, "arrow": "open"},
        {"src_id": "staff", "tgt_id": "fe2", "y": 390, "label": "7: open speaking submissions queue"},
        {"src_id": "fe2", "tgt_id": "sgc", "y": 430, "label": "8: GET /api/staff/submissions?type=speaking&status=pending"},
        {"src_id": "sgc", "tgt_id": "db", "y": 470, "label": "9: SELECT * FROM student_submissions WHERE status='PENDING'"},
        {"src_id": "staff", "tgt_id": "fe2", "y": 510, "label": "10: listen to recording, enter score"},
        {"src_id": "fe2", "tgt_id": "sgc", "y": 550, "label": "11: POST /api/staff/submissions/{id}/grade"},
        {"src_id": "sgc", "tgt_id": "db", "y": 590, "label": "12: UPDATE student_submissions SET manual_score=?,status='GRADED'"},
        {"src_id": "sgc", "tgt_id": "fe2", "y": 630, "label": "13: 200 OK \"Score saved\"", "dashed": True, "arrow": "open"},
    ],
    frames=[
        {"label": "1) Student submits (feature/speaking) — no AI grading step anymore", "x1": 180, "x2": 940, "y1": 90, "y2": 330},
        {"label": "2) Staff grades manually (feature/support) — condition: submissionType=SPEAKING and status != REJECTED", "x1": 1000, "x2": 1500, "y1": 370, "y2": 650},
    ],
    page_w=1600, page_h=720,
)

# --------------------------------------------------------------------------- #
# 6. Content Review (Manager Approve/Reject)
# --------------------------------------------------------------------------- #

build_class_diagram(
    OUT / "class-review.drawio",
    boxes=[
        {"id": "c1", "name": "ManagerReviewController", "x": 40, "y": 40, "w": 320, "header_color": "#ffe6cc", "methods": [
            "+ getReviewQueue(...): ApiResponse~ReviewQueueResponse~",
            "+ getContentDetail(contentId, type): ApiResponse~ReviewableContentDetailResponse~",
            "+ review(ReviewActionRequest): ApiResponse~ReviewResultResponse~",
            "+ requestChanges(RequestChangesRequest): ApiResponse~ReviewResultResponse~",
        ]},
        {"id": "c2", "name": "ContentReviewService", "x": 420, "y": 40, "w": 340, "methods": [
            "+ getReviewQueue(...): ReviewQueueResponse",
            "+ getContentDetail(managerEmail, contentId, type): ReviewableContentDetailResponse",
            "+ review(managerEmail, ReviewActionRequest): ReviewResultResponse",
            "+ requestChanges(managerEmail, RequestChangesRequest): ReviewResultResponse",
            "- requireManager(email): StaffUser",
            "- guardSelfReview(ContentSnapshot, StaffUser): void",
            "- ensureUpdated(affectedRows): void",
        ]},
        {"id": "c3", "name": "ReviewableContentResolver", "x": 820, "y": 40, "w": 320, "methods": [
            "+ resolve(ContentType): ReviewableContentHandler",
        ]},
        {"id": "c4", "name": "ReviewableContentHandler", "x": 820, "y": 220, "w": 320, "interface": True, "methods": [
            "+ findActiveById(contentId): Optional~ContentSnapshot~",
            "+ approve(contentId, manager, now): int",
            "+ transitionFromPending(contentId, status, now): int",
        ]},
        {"id": "c5", "name": "ReviewAuditService", "x": 420, "y": 420, "w": 320, "methods": [
            "+ log(manager, action, type, tableName, contentId, feedback): void",
        ]},
        {"id": "c6", "name": "ContentType", "x": 40, "y": 420, "w": 260, "interface": False, "header_color": "#e1d5e7", "methods": [
            "LESSON", "GRAMMAR", "VOCABULARY", "KANJI", "QUESTION", "ASSESSMENT",
        ]},
    ],
    edges=[
        {"src": "c1", "tgt": "c2"}, {"src": "c2", "tgt": "c3"}, {"src": "c2", "tgt": "c5"},
        {"src": "c3", "tgt": "c4"}, {"src": "c2", "tgt": "c6"},
    ],
    page_w=1250, page_h=650,
)

build_sequence_diagram(
    OUT / "seq-review.drawio",
    participants=[("mgr", "Staff Manager", 20), ("fe", "Frontend", 220), ("mrc", "ManagerReviewController", 420),
                  ("crs", "ContentReviewService", 660), ("hdl", "ReviewableContentHandler", 900),
                  ("ras", "ReviewAuditService", 1100), ("db", "SQL Server", 1300)],
    messages=[
        {"src_id": "mgr", "tgt_id": "fe", "y": 110, "label": "1: select content -> Approve/Reject"},
        {"src_id": "fe", "tgt_id": "mrc", "y": 150, "label": "2: POST /api/manager/reviews"},
        {"src_id": "mrc", "tgt_id": "crs", "y": 190, "label": "3: review(managerEmail, request)"},
        {"src_id": "crs", "tgt_id": "crs", "y": 230, "label": "4: requireManager(managerEmail)"},
        {"src_id": "crs", "tgt_id": "hdl", "y": 300, "label": "5: findActiveById(contentId)"},
        {"src_id": "hdl", "tgt_id": "db", "y": 340, "label": "6: SELECT ... WHERE status='pending_review'"},
        {"src_id": "crs", "tgt_id": "crs", "y": 380, "label": "7: guardSelfReview(snapshot, manager)"},
        {"src_id": "crs", "tgt_id": "hdl", "y": 450, "label": "8: approve(...)/transitionFromPending(REJECTED)"},
        {"src_id": "hdl", "tgt_id": "db", "y": 490, "label": "9: UPDATE <content table> SET status=? WHERE status='pending_review'"},
        {"src_id": "crs", "tgt_id": "ras", "y": 560, "label": "10: log(manager, action, type, tableName, contentId, feedback)"},
        {"src_id": "ras", "tgt_id": "db", "y": 600, "label": "11: INSERT INTO admin_audit_logs(...)"},
        {"src_id": "crs", "tgt_id": "mrc", "y": 640, "label": "12: ReviewResultResponse", "dashed": True, "arrow": "open"},
        {"src_id": "mrc", "tgt_id": "fe", "y": 680, "label": "13: 200 OK { status }", "dashed": True, "arrow": "open"},
    ],
    frames=[
        {"label": "alt not STAFF_MANAGER->403 / self-review->403 / reject missing feedback->400", "x1": 400, "x2": 1350, "y1": 270, "y2": 700},
    ],
    page_w=1500, page_h=760,
)

print("Wrote 12 .drawio files to", OUT)
