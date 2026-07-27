"""Builds SDS_Document_v2.docx from Guides Templates-20260721/Template2_SDS
Document.docx (the current, correct template for this project — NOT the older
Temp_Document/Template3_SDS Document.docx that build_sds.py targets).

Structure follows Template2 exactly:
  I. Record of Changes
  II. Software Design Document
    1. High Level Design (1.1 Architecture, 1.2 Package Diagram, 1.3 Database Design)
    2. State Transition Diagrams
    3. Detailed Design (one 3.N per feature: Class Diagram, Class Specifications,
       Sequence Diagram(s), Database Queries)

Run from repo root:
    python docs/07-Release-Documents/build_sds_v3.py
"""
from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt

ROOT = Path(__file__).resolve().parents[2]
TEMPLATE = ROOT / "Guides  Templates-20260721" / "Template2_SDS Document.docx"
LOGO = ROOT / "Guides  Templates-20260721" / "images_sds" / "img_rId8.png"
OUT = ROOT / "docs" / "07-Release-Documents" / "SDS_Document_v2.docx"
DIA_SDS = ROOT / "docs" / "07-Release-Documents" / "diagrams" / "sds"
DIA_RDS = ROOT / "docs" / "07-Release-Documents" / "diagrams" / "rds"


# --------------------------------------------------------------------------- #
# docx helpers (same conventions as build_template1_srs_docs_v2.py)
# --------------------------------------------------------------------------- #

def clear_document(doc):
    body = doc._body._element
    for child in list(body):
        if child.tag.endswith("sectPr"):
            continue
        body.remove(child)


def add_p(doc, text="", bold=False, italic=False, align=None, size=10.5):
    p = doc.add_paragraph()
    if align is not None:
        p.alignment = align
    r = p.add_run(text)
    r.bold = bold
    r.italic = italic
    r.font.size = Pt(size)
    return p


def add_h(doc, text, level):
    return doc.add_heading(text, level=level)


def add_table(doc, headers, rows, col_widths=None):
    table = doc.add_table(rows=1, cols=len(headers))
    table.style = "Table Grid"
    for i, header in enumerate(headers):
        run = table.rows[0].cells[i].paragraphs[0].add_run(str(header))
        run.bold = True
    for row in rows:
        cells = table.add_row().cells
        for i, value in enumerate(row):
            cells[i].text = str(value)
    return table


def add_picture_if_exists(doc, path, width=6.4):
    if path.exists():
        doc.add_picture(str(path), width=Inches(width))
    else:
        add_p(doc, f"[Diagram placeholder: {path.name}]")


def add_toc_field(doc):
    paragraph = doc.add_paragraph()
    run = paragraph.add_run()
    fld_begin = OxmlElement("w:fldChar")
    fld_begin.set(qn("w:fldCharType"), "begin")
    run._r.append(fld_begin)

    run = paragraph.add_run()
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = 'TOC \\o "1-5" \\h \\z \\u'
    run._r.append(instr)

    run = paragraph.add_run()
    fld_separate = OxmlElement("w:fldChar")
    fld_separate.set(qn("w:fldCharType"), "separate")
    run._r.append(fld_separate)

    fallback_lines = [
        "I. Record of Changes",
        "II. Software Design Document",
        "1. High Level Design",
        "1.1 Software Architecture",
        "1.2 Package Diagram",
        "1.3 Database Design",
        "2. State Transition Diagrams",
        "3. Detailed Design",
    ]
    for index, line in enumerate(fallback_lines):
        if index == 0:
            paragraph.add_run(line)
        else:
            doc.add_paragraph(line)

    paragraph = doc.add_paragraph()
    run = paragraph.add_run()
    fld_end = OxmlElement("w:fldChar")
    fld_end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_end)


def add_field_table(doc, fields):
    """fields: list of (No, Field, PK, FK, UN, NN, Description)"""
    add_table(doc, ["No", "Field", "PK", "FK", "UN", "NN", "Description"], fields)


def _set_cell_shading(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), fill_hex)
    tcPr.append(shd)


def _set_cell_border(cell, color_hex, sz=12):
    tcPr = cell._tc.get_or_add_tcPr()
    borders = OxmlElement("w:tcBorders")
    for side in ("top", "left", "bottom", "right"):
        el = OxmlElement(f"w:{side}")
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), str(sz))
        el.set(qn("w:space"), "0")
        el.set(qn("w:color"), color_hex)
        borders.append(el)
    tcPr.append(borders)


def _set_cell_margins(cell, top=120, bottom=120, left=160, right=160):
    tcPr = cell._tc.get_or_add_tcPr()
    mar = OxmlElement("w:tcMar")
    for side, val in (("top", top), ("bottom", bottom), ("left", left), ("right", right)):
        el = OxmlElement(f"w:{side}")
        el.set(qn("w:w"), str(val))
        el.set(qn("w:type"), "dxa")
        mar.append(el)
    tcPr.append(mar)


def add_sql(doc, sql_text):
    """Render the SQL block inside a shaded, bordered callout box so it visibly
    stands out from the surrounding text. Plain paragraph/table borders in
    OOXML only draw straight lines — true geometric rounded corners need a
    Word drawing shape (VML/DrawingML), which risks silently corrupting the
    .docx if the hand-built XML isn't pixel-exact and can't be verified
    without opening real Word. This gets the same practical result (a
    clearly boxed-off, highlighted query block) without that risk."""
    table = doc.add_table(rows=1, cols=1)
    table.autofit = True
    cell = table.rows[0].cells[0]
    cell.paragraphs[0].text = ""
    _set_cell_shading(cell, "F2F6FC")
    _set_cell_border(cell, "8EAADB", sz=12)
    _set_cell_margins(cell)
    p = cell.paragraphs[0]
    for i, line in enumerate(sql_text.strip("\n").split("\n")):
        if i > 0:
            p.add_run().add_break()
        r = p.add_run(line)
        r.font.name = "Consolas"
        r.font.size = Pt(9.5)
    return table


# --------------------------------------------------------------------------- #
# Content: 1.2 Package Diagram — 14 packages, real (from feature/* source tree)
# --------------------------------------------------------------------------- #

PACKAGES = [
    ("01", "auth", "Student login/register, JWT access + refresh token, email verification, forgot password, Google OAuth login."),
    ("02", "staff", "Staff/StaffManager accounts: separate login, staff member management, staff password reset."),
    ("03", "admin", "Admin accounts, audit log, statistics dashboard, system configuration (system_settings), maintenance mode."),
    ("04", "student", "Student profile, dashboard, learning progress (student_content_progress), avatar, enrolled courses."),
    ("05", "learning", "Learning content: Kana, Kanji, Vocabulary, Grammar, Lesson by JLPT level (N5-N1)."),
    ("06", "assessment", "Question bank, Quiz/Exam (Assessment), attempts (TestAttempt), server-side grading, speaking/writing submissions (StudentSubmission)."),
    ("07", "speaking", "Student records and submits speaking (shadowing) exercises, polls the result (SpeakingController/SpeakingService); grading is done by teachers in the support package — no AI auto-grading step (removed by a recent refactor)."),
    ("08", "staffcontent", "Where Staff draft content before publishing: questions, quizzes, exams, grammar, vocabulary, and a dedicated Staff dashboard."),
    ("09", "contentreview", "Content review flow for Staff-authored content (approve/reject) before it goes public, with review audit logging."),
    ("10", "publishedcontent", "Snapshot of published content, served to Students (kept separate from Staff's draft copy)."),
    ("11", "flashcard", "Flashcard + Spaced Repetition (SRS), system decks or user-created decks."),
    ("12", "dictionary", "Kanji/Vocabulary dictionary lookup by keyword or type."),
    ("13", "notification", "Notifications sent to individual Students (notification type, delivery channel)."),
    ("14", "support", "Support tickets submitted by Students and staff replies (TicketReply); Staff's speaking-grading queue (SupportTicketService, UC-31)."),
]

# --------------------------------------------------------------------------- #
# Content: 1.3 Database Design — 13 core tables, real fields from
# apps/backend/src/main/resources/db/migration/V1__init_schema.sql (MySQL 8,
# per ADR-009 — NOT the older SQL Server design in JLPT_database.md).
# --------------------------------------------------------------------------- #

DB_TABLES = [
    ("admin_users", "System administrator accounts.", [
        ("01", "admin_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "email", "", "", "X", "X", "Login email, unique."),
        ("03", "password_hash", "", "", "", "", "Bcrypt hash (cost >= 10, ADR-003); nullable until first password set."),
        ("04", "full_name", "", "", "", "X", "Display name."),
        ("05", "status", "", "", "", "X", "active / suspended / pending / deleted."),
        ("06", "login_attempts", "", "", "", "X", "Failed login counter for lockout."),
        ("07", "locked_until", "", "", "", "", "Lockout expiry timestamp."),
        ("08", "last_login_at", "", "", "", "", "Last successful login."),
        ("09", "created_at / updated_at", "", "", "", "X", "Audit timestamps."),
    ]),
    ("staff_users", "Staff and StaffManager accounts (StaffManager is staff_role='staff_manager', not a separate table).", [
        ("01", "staff_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "email", "", "", "X", "X", "Login email, unique."),
        ("03", "password_hash", "", "", "", "", "Bcrypt hash; nullable until Admin provisions/first login."),
        ("04", "full_name", "", "", "", "X", "Display name."),
        ("05", "staff_role", "", "", "", "X", "staff / staff_manager."),
        ("06", "status", "", "", "", "X", "active / suspended / pending / deleted."),
        ("07", "must_change_password", "", "", "", "X", "Forces password change after Admin-issued temp password."),
        ("08", "login_attempts / locked_until / last_login_at", "", "", "", "", "Lockout tracking, same pattern as admin_users."),
        ("09", "created_at / updated_at", "", "", "", "X", "Audit timestamps."),
    ]),
    ("student_users", "Student accounts, including Google OAuth identity and JLPT level/streak tracking.", [
        ("01", "student_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "email", "", "", "X", "X", "Login email, unique."),
        ("03", "password_hash", "", "", "", "", "Bcrypt hash; nullable for OAuth-only accounts."),
        ("04", "full_name", "", "", "", "X", "Display name."),
        ("05", "status", "", "", "", "X", "active / suspended / pending / deleted."),
        ("06", "email_verified_at", "", "", "", "", "Set after OTP verification (UC-02)."),
        ("07", "avatar_url / phone", "", "", "", "", "Profile fields (UC-04)."),
        ("08", "oauth_provider / oauth_provider_id / oauth_provider_email / oauth_linked_at", "", "", "X*", "", "*Unique together (oauth_provider, oauth_provider_id) — Google login (UC-01)."),
        ("09", "current_jlpt_level / target_jlpt_level", "", "", "", "", "N5-N1, used for content access checks (BR-08)."),
        ("10", "current_streak / longest_streak / last_activity_date", "", "", "", "", "Learning streak stats (UC-19)."),
        ("11", "login_attempts / locked_until / last_login_at", "", "", "", "", "Lockout tracking after 5 failed attempts (BR from AuthenticationService)."),
        ("12", "created_at / updated_at", "", "", "", "X", "Audit timestamps."),
    ]),
    ("auth_tokens", "Shared token store (session, refresh, reset password, email verification) for all 3 actor types.", [
        ("01", "token_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "actor_type", "", "", "", "X", "admin / staff / student."),
        ("03", "admin_id / staff_id / student_id", "", "X", "", "", "Exactly one is set, per actor_type (CK_auth_token_actor)."),
        ("04", "token_type", "", "", "", "X", "session / email_verification / password_reset / refresh / limited_session."),
        ("05", "token_value", "", "", "", "X", "Random UUID, or 6-digit OTP for email_verification."),
        ("06", "ip_address", "", "", "", "", "Request IP at issuance."),
        ("07", "expires_at", "", "", "", "X", "Expiry timestamp."),
        ("08", "revoked_at", "", "", "", "", "Soft revoke on logout (ADR-004 — no hard delete)."),
        ("09", "created_at", "", "", "", "X", "Issue timestamp."),
    ]),
    ("questions", "Question bank with inline A/B/C/D options (no separate question_options table).", [
        ("01", "question_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "question_text", "", "", "", "X", "Question body."),
        ("03", "question_type", "", "", "", "X", "multiple_choice / fill_blank / true_false."),
        ("04", "skill", "", "", "", "X", "vocabulary / grammar / kanji / reading / listening / mixed."),
        ("05", "jlpt_level", "", "", "", "X", "N5-N1."),
        ("06", "option_a .. option_d / correct_option", "", "", "", "", "Inline options + correct letter (multiple_choice)."),
        ("07", "correct_answer_text", "", "", "", "", "Correct text (fill_blank)."),
        ("08", "created_by / approved_by", "", "X", "", "", "FK -> staff_users."),
        ("09", "status", "", "", "", "X", "draft / pending_review / rejected / published / archived / deleted."),
        ("10", "published_at / created_at / updated_at", "", "", "", "", "Lifecycle timestamps."),
    ]),
    ("assessments", "Quiz and Exam combined via assessment_type (no separate quizzes/exams tables).", [
        ("01", "assessment_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "assessment_type", "", "", "", "X", "quiz / exam."),
        ("03", "title", "", "", "", "X", "Assessment title."),
        ("04", "lesson_id", "", "X", "", "", "FK -> lessons, optional link to a lesson."),
        ("05", "jlpt_level / duration_min / pass_score / total_score", "", "", "", "", "Exam parameters."),
        ("06", "status", "", "", "", "X", "draft / pending_review / rejected / published / archived / deleted."),
        ("07", "is_deleted", "", "", "", "X", "Soft delete flag (ADR-004)."),
        ("08", "created_by / approved_by", "", "X", "", "", "FK -> staff_users."),
        ("09", "published_at / created_at / updated_at", "", "", "", "", "Lifecycle timestamps."),
    ]),
    ("test_attempts", "One quiz/exam/practice attempt by a Student; status prevents double submission (LESSON-005).", [
        ("01", "attempt_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "student_id", "", "X", "", "X", "FK -> student_users, ON DELETE CASCADE."),
        ("03", "attempt_type", "", "", "", "X", "exam / quiz / practice / reading / listening."),
        ("04", "parent_type / parent_id", "", "", "", "X*", "*parent_type required; identifies the assessment/lesson attempted."),
        ("05", "started_at / submitted_at / duration_seconds", "", "", "", "", "Timing, validated server-side (BR-02)."),
        ("06", "total_score / max_score / is_passed", "", "", "", "", "Computed only in the Service layer, never trusted from client (BR-02)."),
        ("07", "language_knowledge_score / reading_score / listening_score", "", "", "", "", "Per-section JLPT scores."),
        ("08", "status", "", "", "", "X", "in_progress / submitted / auto_submitted / abandoned (see §2.1 State Diagram)."),
    ]),
    ("attempt_answers", "A Student's answer to each question in an attempt, used for grading and review.", [
        ("01", "answer_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "attempt_id", "", "X", "", "X", "FK -> test_attempts, ON DELETE CASCADE."),
        ("03", "question_id", "", "X", "", "X", "FK -> questions."),
        ("04", "selected_option / answer_text", "", "", "", "", "Student's answer (multiple_choice / fill_blank)."),
        ("05", "is_correct / score", "", "", "", "", "Computed server-side during grading (BR-02)."),
        ("06", "answered_at", "", "", "", "X", "Answer timestamp."),
    ]),
    ("student_submissions", "Speaking/handwriting submissions requiring Staff grading (legacy AI columns kept for old data).", [
        ("01", "submission_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "student_id", "", "X", "", "X", "FK -> student_users, ON DELETE CASCADE."),
        ("03", "submission_type", "", "", "", "X", "speaking / handwriting."),
        ("04", "status", "", "", "", "X", "pending / ai_graded / graded / rejected (see §2.2 State Diagram)."),
        ("05", "exercise_id", "", "X", "", "", "FK -> lessons (speaking exercise)."),
        ("06", "recording_url / duration_seconds", "", "", "", "", "Speaking submission media (no BLOB, ADR-006)."),
        ("07", "ai_overall_score .. ai_graded_at", "", "", "", "", "Legacy AI-grading columns; not written by current code."),
        ("08", "kanji_id / kana_id", "", "X", "", "", "FK -> kanji / kana_characters (handwriting OCR target)."),
        ("09", "handwriting_image_url / expected_character / recognized_character / similarity_percent", "", "", "", "", "OCR handwriting fields (ADR-007: similarity % only)."),
        ("10", "manual_score / manual_feedback / graded_by / graded_at", "", "X*", "", "", "*graded_by -> staff_users. Staff's final grade (AGENTS.md §7.5)."),
        ("11", "submitted_at / updated_at", "", "", "", "X", "Lifecycle timestamps."),
    ]),
    ("flashcard_decks", "Flashcard deck (system or personal); first-class table separate from flashcards.", [
        ("01", "deck_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "student_id", "", "X", "", "", "FK -> student_users; NULL for system decks."),
        ("03", "name / description / jlpt_level / topic / color / display_order", "", "", "", "", "Deck metadata."),
        ("04", "is_system / is_review_deck", "", "", "", "X", "System deck flag; 1 auto-generated review deck per student."),
        ("05", "is_deleted", "", "", "", "X", "Soft delete flag (ADR-004)."),
        ("06", "active_name_key / review_deck_key", "", "", "X", "", "Generated columns emulating SQL Server's filtered unique index on MySQL."),
    ]),
    ("flashcards", "One flashcard belonging to a Student, plus SRS state (ease_factor, interval_days, next_review_date).", [
        ("01", "flashcard_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "student_id", "", "X", "", "", "FK -> student_users."),
        ("03", "deck_id", "", "X", "", "X", "FK -> flashcard_decks."),
        ("04", "content_type / content_id", "", "", "", "X*", "*content_type required; kanji / vocabulary / grammar / custom."),
        ("05", "front_text / back_text", "", "", "", "", "Card content (custom cards)."),
        ("06", "last_rating", "", "", "", "", "easy / hard / wrong — last SM-2 review outcome."),
        ("07", "interval_days / repetition_count / ease_factor / next_review_date / last_reviewed_at", "", "", "", "", "SM-2 spaced-repetition state (default ease_factor 2.50)."),
        ("08", "last_session_id", "", "", "", "", "Groups cards reviewed in the same session."),
        ("09", "is_deleted / created_at", "", "", "", "X", "Soft delete flag + creation timestamp."),
    ]),
    ("kanji_writing_attempts", "One Student attempt at hand-writing a Kanji character on canvas; DTW similarity score.", [
        ("01", "attempt_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "student_id", "", "X", "", "X", "FK -> student_users."),
        ("03", "kanji_id / character_value", "", "", "", "X*", "*character_value required; kanji_id references kanji.kanji_id."),
        ("04", "total_strokes / avg_dtw_score", "", "", "", "X*", "*total_strokes required; avg_dtw_score is the DTW similarity metric (ADR-007)."),
        ("05", "final_quality", "", "", "", "", "perfect / good / ok / bad, derived from avg_dtw_score thresholds."),
        ("06", "stroke_details", "", "", "", "", "Per-stroke coordinates (JSON text)."),
        ("07", "is_deleted / created_at / updated_at / created_by", "", "", "", "X*", "*is_deleted required; soft delete flag (ADR-004)."),
    ]),
    ("admin_audit_logs", "Records every content-review / submission-grading / admin action taken by Staff, StaffManager, or Admin.", [
        ("01", "audit_id", "X", "", "", "X", "PK, auto-increment."),
        ("02", "admin_actor_id / staff_actor_id / student_actor_id", "", "X", "", "", "Exactly one is set (CK_audit_actor)."),
        ("03", "action", "", "", "", "X", "Action name, e.g. APPROVE_CONTENT, GRADE_SUBMISSION."),
        ("04", "target_table / target_id", "", "", "", "", "The row the action was performed on."),
        ("05", "description", "", "", "", "", "Free-text detail of the action."),
        ("06", "ip_address / created_at", "", "", "", "X*", "*created_at required; actor's IP + audit timestamp."),
    ]),
]

# --------------------------------------------------------------------------- #
# Content: 3. Detailed Design — 6 features, real SQL from the services
# --------------------------------------------------------------------------- #

FEATURES = [
    {
        "name": "3.1 Authentication & Login",
        "class_png": "class-auth.png",
        "seq_png": "seq-auth.png",
        "seq_name": "User Login Sequence",
        "specs": [
            ("AuthenticationService Class", [
                ("01", "checkAccountType(email, ip)", "Input: email, caller IP. Rate-limited to 10 calls/minute/IP. Returns the account type (Student/Staff/Admin) or not-found so the FE can show the right login form."),
                ("02", "login(LoginRequest, ip)", "Authenticates a Student via AuthenticationManager + passwordEncoder; on success, issues an access token + refresh token (JwtProvider) and stores the refresh token in auth_tokens with actor_type=STUDENT."),
                ("03", "loginStaff(LoginRequest, ip)", "Same as login but for StaffUser/AdminUser; additionally checks the account status (locked/inactive)."),
                ("04", "refresh(RefreshTokenRequest)", "Input: refresh token. Looks up auth_tokens by hash, checks expires_at and that it has not been revoked, issues a new access token (rotating the refresh token)."),
                ("05", "logout(LogoutRequest)", "Revokes (marks) the current refresh token in auth_tokens — no hard-delete (ADR-004)."),
                ("06", "loginWithGoogle(GoogleTokenRequest)", "Verifies the Google ID token, finds or creates a student_users row by oauth_provider_id, issues a JWT the same way as a normal login."),
            ]),
            ("RegistrationService Class", [
                ("01", "register(RegisterRequest)", "Input: email, password, name. Hashes the password with bcrypt cost >= 10 (ADR-003), creates student_users with email_verified_at=null, and publishes a SendVerificationEmailEvent asynchronously."),
            ]),
        ],
        "sql": """-- checkAccountType / login: look up the account by email
SELECT * FROM student_users WHERE email = ? AND is_deleted = 0;

-- store the refresh token on successful login
INSERT INTO auth_tokens (actor_type, actor_id, token_type, token_hash, expires_at, created_at)
VALUES ('STUDENT', ?, 'REFRESH', ?, ?, GETDATE());

-- refresh(): look up a still-valid refresh token
SELECT * FROM auth_tokens
WHERE token_hash = ? AND token_type = 'REFRESH' AND expires_at > GETDATE();

-- logout(): revoke the token (soft, no DELETE)
UPDATE auth_tokens SET revoked_at = GETDATE() WHERE token_hash = ?;""",
    },
    {
        "name": "3.2 Quiz Submission (Assessment)",
        "class_png": "class-quiz.png",
        "seq_png": "seq-quiz.png",
        "seq_name": "Submit Quiz Sequence",
        "specs": [
            ("QuizService Class", [
                ("01", "startQuiz(quizId, student)", "Input: quiz id, the current Student. Checks the quiz is PUBLISHED, loads the assigned questions (question_assignments), creates a new TestAttempt with status=IN_PROGRESS. Never returns the correct answers to the client."),
                ("02", "submitQuiz(quizId, studentId, attemptId, answers)", "Locks the test_attempts row with findByIdForUpdate (pessimistic lock) to prevent a race on duplicate submission; checks attempt.student.id==studentId (blocks submitting on someone else's behalf) and status==IN_PROGRESS (blocks double submission, LESSON-005). Calls calculateScore."),
                ("03", "calculateScore(attempt, assessment, answers)", "The score is computed entirely server-side, never trusting a score from the client: matches answers against each Question's correct_option, saves each AttemptAnswer, updates attempt.status=SUBMITTED."),
            ]),
        ],
        "sql": """-- startQuiz(): check the quiz is published
SELECT * FROM assessments
WHERE id = ? AND assessment_type = 'QUIZ' AND status = 'PUBLISHED';

-- load the assigned questions, in display order
SELECT qa.* FROM question_assignments qa
WHERE qa.parent_type = 'ASSESSMENT' AND qa.parent_id = ?
ORDER BY qa.display_order;

-- create a new attempt when the student starts the quiz
INSERT INTO test_attempts (student_id, attempt_type, parent_type, parent_id, started_at, max_score, status)
VALUES (?, 'QUIZ', 'ASSESSMENT', ?, GETDATE(), ?, 'IN_PROGRESS');

-- submitQuiz(): lock the attempt row to prevent duplicate submission
SELECT * FROM test_attempts WITH (UPDLOCK, ROWLOCK) WHERE id = ?;

-- save each answer after grading
INSERT INTO attempt_answers (attempt_id, question_id, selected_option, is_correct)
VALUES (?, ?, ?, ?);

-- update the attempt after submission
UPDATE test_attempts
SET status = 'SUBMITTED', submitted_at = GETDATE(), score = ?
WHERE id = ?;""",
    },
    {
        "name": "3.3 Flashcard SRS (Spaced Repetition)",
        "class_png": "class-flashcard.png",
        "seq_png": "seq-flashcard.png",
        "seq_name": "Flashcard Review Sequence",
        "specs": [
            ("FlashcardSrsService Class", [
                ("01", "submitReview(flashcardId, studentId, ReviewRequest)", "Verifies card ownership (ownCardOrThrow). For a multiple-choice vocabulary quiz, the server matches selectedOptionId against the correct contentId itself (never trusts a correct/incorrect flag from the client). Calls applySm2 to update the review schedule."),
                ("02", "getSession(studentId, deckId, topicId, newLimit)", "Input: exactly one of deckId (personal notebook) or topicId (curriculum topic), plus a limit on new cards (default 10, cap 20). Locks on (studentId, deck/topic) to prevent two tabs creating a session at the same time. Priority order: not-yet-learned -> due for review -> the rest."),
                ("03", "applySm2(Flashcard, LastRating)", "Implements the SM-2 algorithm: WRONG -> ease decreases (floor 1.30), repetitionCount resets; HARD -> ease unchanged; EASY -> ease+0.1 (cap 2.50). Always updates nextReviewDate = today + intervalDays."),
            ]),
        ],
        "sql": """-- ownCardOrThrow(): verify card ownership before grading
SELECT * FROM flashcards
WHERE flashcard_id = ? AND student_id = ? AND is_deleted = 0;

-- applySm2() -> save(): update the review schedule after grading
UPDATE flashcards
SET ease_factor = ?, interval_days = ?, repetition_count = ?,
    next_review_date = ?, last_reviewed_at = GETDATE(), last_rating = ?
WHERE flashcard_id = ?;

-- end of session: collect words answered wrong in THIS session
SELECT * FROM flashcards
WHERE student_id = ? AND last_session_id = ? AND content_type = 'VOCABULARY' AND last_rating = 'WRONG';""",
    },
    {
        "name": "3.4 Kanji Writing Evaluation (OCR/DTW Similarity)",
        "class_png": "class-kanji.png",
        "seq_png": "seq-kanji.png",
        "seq_name": "Kanji Writing Evaluation Sequence",
        "specs": [
            ("KanjiWritingServiceImpl Class", [
                ("01", "evaluateStroke(EvaluateRequest)", "Input: the coordinates of the stroke the Student just drew + the reference stroke for that specific stroke index. Normalizes both paths (normalize+downsample to at most 20 points), computes the distance via DTW, maps it to a quality bucket using 3 fixed thresholds (300/650/1200). Synchronous, does not persist to the DB."),
                ("02", "saveAttempt(AttemptRequest, studentId)", "Input: every stroke drawn for one Kanji character + the per-stroke DTW score. Computes avgDtwScore as the average of the per-stroke scores, derives finalQuality, saves one KanjiWritingAttempt row (with strokeDetails as JSON)."),
                ("03", "computeDtw(s1, s2)", "Classic Dynamic Time Warping (ADR-007: compares similarity % only, no stroke-order analysis like image-based OCR). Invariant to differences in drawing speed between the two paths."),
            ]),
        ],
        "sql": """-- saveAttempt(): persist one completed writing attempt
INSERT INTO kanji_writing_attempts
    (student_id, kanji_id, character_value, total_strokes, avg_dtw_score, final_quality, stroke_details, created_by)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);

-- evaluateStroke() never queries the DB — all DTW computation happens in memory per request.""",
    },
    {
        "name": "3.5 Speaking Submission Grading (Shadowing)",
        "class_png": "class-speaking.png",
        "seq_png": "seq-speaking.png",
        "seq_name": "Speaking Submission and Grading Sequence",
        "specs": [
            ("SpeakingService Class", [
                ("01", "getExercises(level, studentId)", "Input: JLPT level + student id. Returns the list of published speaking exercises (Lesson of type SPEAKING) for that level, with attemptCount/bestScore (the student's highest teacher-graded score)."),
                ("02", "submit(exerciseId, audio, student)", "Input: exercise id + multipart audio file. Validates the exercise exists & is PUBLISHED, stores the file via SpeakingAudioStorageService (no BLOB in the DB, LESSON-002), creates a StudentSubmission with status=PENDING — there is no AI grading step (removed by a refactor, see the Sequence Diagram). Returns a jobId for the Student to poll."),
                ("03", "getResult(jobId, studentId)", "Input: submission id + the calling student id (only ever returns that student's own submission). Returns PENDING if not yet graded (manualScore==null), COMPLETED with score/feedback once graded, FAILED if status=REJECTED."),
            ]),
            ("SupportTicketService Class", [
                ("01", "getAllSubmissions(submissionType, status, page, size)", "Input: submissionType filter (default speaking), status, pagination. Returns a Page<SubmissionResponse> for the Staff grading queue (UC-31)."),
                ("02", "getSubmissionDetail(submissionId)", "Returns the detail of one submission: recordingUrl, and if already graded, manualScore/manualFeedback/gradedBy. finalScore = manualScore if present, otherwise falls back to aiOverallScore (a legacy column, always null for new data since the AI grading step was removed)."),
                ("03", "manualGrade(submissionId, actorEmail, ManualGradeRequest)", "Current business rule: can only grade a submission whose submissionType=SPEAKING (422 otherwise) and status != REJECTED (422 if already rejected) — no longer requires status=AI_GRADED as it used to, since the AI grading step was removed (commit db0d1648). Sets manualScore/manualFeedback/gradedBy/gradedAt, moves status to GRADED, sends a Notification (ACHIEVEMENT) and writes an AdminAuditLog."),
            ]),
        ],
        "sql": """-- SpeakingService.submit(): create a new submission, awaiting teacher grading (no AI step anymore)
INSERT INTO student_submissions (student_id, submission_type, status, exercise_id, recording_url, created_at)
VALUES (?, 'SPEAKING', 'PENDING', ?, ?, GETDATE());

-- SupportTicketService.getAllSubmissions(): the speaking grading queue
SELECT * FROM student_submissions
WHERE submission_type = 'SPEAKING' AND (status = ? OR ? IS NULL)
ORDER BY submitted_at ASC;

-- manualGrade(): record the teacher's score, change status (no longer checks status=AI_GRADED)
UPDATE student_submissions
SET manual_score = ?, manual_feedback = ?, graded_by = ?, graded_at = GETDATE(), status = 'GRADED'
WHERE submission_id = ? AND status != 'REJECTED';

-- notify the Student once grading is done
INSERT INTO notifications (student_id, title, message, notification_type, reference_key, created_by)
VALUES (?, 'Your speaking submission has been graded', ?, 'ACHIEVEMENT', ?, ?);""",
    },
    {
        "name": "3.6 Content Review (Manager Approve/Reject)",
        "class_png": "class-review.png",
        "seq_png": "seq-review.png",
        "seq_name": "Content Review Sequence",
        "specs": [
            ("ContentReviewService Class", [
                ("01", "getContentDetail(managerEmail, contentId, typeStr)", "Verifies managerEmail is an active STAFF_MANAGER (requireManager), resolves the right handler for the type (course/lesson/grammar/vocabulary/kanji/question/assessment), returns a snapshot (title, JLPT level, author, submission date, content)."),
                ("02", "review(managerEmail, ReviewActionRequest)", "Blocks reviewing one's own content (guardSelfReview). APPROVE -> handler.approve() (status=published); REJECT -> feedback is required, then handler.transitionFromPending(REJECTED). Every action is logged via ReviewAuditService.log(...)."),
                ("03", "requestChanges(managerEmail, RequestChangesRequest)", "Asks Staff to revise content: feedback is required, moves status back to draft (default) or rejected depending on targetStatus. Uses the same guardSelfReview + audit-log mechanism as review()."),
                ("04", "ensureUpdated(affectedRows)", "If the UPDATE affected 0 rows, the content is no longer in pending_review (already handled concurrently by another Manager) -> throws ConcurrentReviewException to prevent one review overwriting another."),
            ]),
        ],
        "sql": """-- findActiveById(): fetch content awaiting review (example: the GRAMMAR handler)
SELECT * FROM grammar_points WHERE grammar_id = ? AND status = 'pending_review';

-- approve(): only updates if still pending_review (prevents double approval)
UPDATE grammar_points
SET status = 'published', approved_by = ?, published_at = GETDATE()
WHERE grammar_id = ? AND status = 'pending_review';

-- reject() / requestChanges(): move back to draft or rejected
UPDATE grammar_points
SET status = ?, updated_at = GETDATE()
WHERE grammar_id = ? AND status = 'pending_review';

-- write an audit row for every review action
INSERT INTO admin_audit_logs (staff_id, action, target_table, target_id, note, created_at)
VALUES (?, ?, ?, ?, ?, GETDATE());""",
    },
    {
        "name": "3.7 Staff Content Authoring & Submit for Review",
        "class_png": "class-staffcontent.png",
        "seq_png": "seq-staffcontent.png",
        "seq_name": "Staff Submit-for-Review Sequence",
        "specs": [
            ("StaffGrammarServiceImpl Class (representative — the same create-draft / submit-for-review pattern "
             "is repeated per content type: StaffQuestionServiceImpl, StaffQuizService, StaffExamService, "
             "LearningContentServiceImpl for kanji/vocab/lesson)", [
                ("01", "createGrammar(CreateGrammarRequest, staffEmail)", "Input: grammar fields, the authoring Staff's email. Parses jlptLevel server-side so only N1..N5 can ever be stored (never trusts client-side validation alone), forces status=DRAFT and createdBy=the calling Staff regardless of what the request body contains."),
                ("02", "updateGrammar(grammarId, UpdateGrammarRequest, staffEmail)", "Input: grammar id, partial fields to update. guardOwnershipOrManager blocks a Staff from editing another Staff's content (a Staff Manager may edit any). Editing is blocked once status=PUBLISHED, and only allowed from DRAFT/REJECTED otherwise (mirrors LESSON-005's intent: don't let content mutate while it's mid-workflow)."),
                ("03", "submitForReview(grammarId, staffEmail)", "Input: grammar id + the calling Staff's email. Re-checks ownership, then guards every mandatory field (structure/meaning/usageExplanation/exampleSentenceJp/jlptLevel) server-side even though the FE form already validates them — the server never trusts that the FE ran its checks. Only DRAFT or REJECTED can be submitted; moves status to PENDING_REVIEW, which is exactly what ContentReviewService's review queue reads (§3.6)."),
            ]),
        ],
        "sql": """-- createGrammar(): always starts as DRAFT, creator is whoever is authenticated (not client-supplied)
INSERT INTO grammar_points (title, structure, meaning, jlpt_level, status, created_by, created_at)
VALUES (?, ?, ?, ?, 'DRAFT', ?, GETDATE());

-- submitForReview(): only DRAFT/REJECTED content can move to PENDING_REVIEW
UPDATE grammar_points
SET status = 'PENDING_REVIEW'
WHERE grammar_id = ? AND status IN ('DRAFT', 'REJECTED');

-- listGrammars(): a Staff only ever sees their own drafts/submissions (a Manager sees all via a separate query)
SELECT * FROM grammar_points
WHERE created_by = ? AND (jlpt_level = ? OR ? IS NULL) AND (status = ? OR ? IS NULL) AND status != 'DELETED';""",
    },
    {
        "name": "3.8 Admin User Management",
        "class_png": "class-admin-user.png",
        "seq_png": "seq-admin-user.png",
        "seq_name": "Suspend User Sequence",
        "specs": [
            ("AdminUserService Class", [
                ("01", "createStaff(adminEmail, CreateStaffRequest)", "Input: email/name/staffRole. Rejects if the email already exists across all 3 account tables (student/staff/admin), creates a StaffUser with status=PENDING (cannot log in yet), issues a 24h EMAIL_VERIFICATION auth_tokens row, and emails an invitation link — the Staff sets their own password via setupStaffPassword (never issued in plaintext by the Admin)."),
                ("02", "suspendUser(adminEmail, type, userId, SuspendUserRequest)", "checkSelfModification blocks an Admin from suspending their own account. Sets status=SUSPENDED + suspendReason, then revokes every currently-active refresh token for that user (LESSON-based defense-in-depth: a suspended account's existing sessions stop working immediately, not just future logins) via AuthTokenRepository.revokeAllActiveByStudentId/StaffId."),
                ("03", "softDeleteUser(adminEmail, type, userId)", "Same self-modification + status guard as suspend, but sets status=DELETED (ADR-004: soft delete only — no DELETE FROM). Deleting an Admin account through this endpoint is explicitly rejected (BusinessRuleException) — Admin accounts can't be removed this way."),
                ("04", "restoreUser(adminEmail, type, userId)", "Only valid from status=DELETED (else BusinessRuleException); flips back to ACTIVE. Every mutating call also writes an admin_audit_logs row via the private auditLog() helper (action, targetTable, targetId, description) so every account-management action is traceable."),
            ]),
        ],
        "sql": """-- suspendUser(): change status and store the reason
UPDATE student_users SET status = 'SUSPENDED', suspend_reason = ? WHERE id = ?;

-- immediately invalidate that user's existing sessions (not just block future logins)
UPDATE auth_tokens SET revoked_at = ? WHERE actor_id = ? AND actor_type = 'STUDENT' AND revoked_at IS NULL;

-- every admin action is audited
INSERT INTO admin_audit_logs (admin_id, action, target_table, target_id, description, created_at)
VALUES (?, 'suspend_user', 'student_users', ?, ?, GETDATE());

-- restoreUser(): only DELETED -> ACTIVE is allowed
UPDATE student_users SET status = 'ACTIVE' WHERE id = ? AND status = 'DELETED';""",
    },
    {
        "name": "3.9 Student Support Ticket",
        "class_png": "class-support-ticket.png",
        "seq_png": "seq-support-ticket.png",
        "seq_name": "Ticket Lifecycle Sequence",
        "specs": [
            ("SupportTicketService Class", [
                ("01", "createTicket(studentId, TicketRequest)", "Input: subject, content, category, optional priority (defaults NORMAL). Always creates status=OPEN — the Student cannot set an initial status."),
                ("02", "addStaffReply(ticketId, staffEmail, TicketReplyRequest)", "Blocks replying on a closed ticket (checkTicketNotClosed). Authorization is neither 'any Staff' nor 'FE hides the button': only the assigned Staff or a STAFF_MANAGER may reply — enforced server-side (ForbiddenException otherwise). The first Staff reply on an OPEN/ASSIGNED ticket auto-advances it to IN_PROGRESS. Fires a notifyStudent() IN_APP notification so the Student learns about the reply without polling."),
                ("03", "assignTicket(ticketId, assignToStaffId, actorEmail, isAdmin)", "Only a STAFF_MANAGER (or an Admin acting via a separate admin path, isAdmin=true) may assign a ticket. Rejects assigning to a non-ACTIVE staff member (422). Assigning an OPEN ticket moves it to ASSIGNED — this is the approval gate before a Staff can touch it."),
                ("04", "closeTicket(ticketId, actorEmail)", "Staff/Manager-side close -> RESOLVED (vs. closeStudentTicket, the Student's own close -> CLOSED, which additionally checks ownership and that the ticket isn't already closed). Both paths set resolvedAt and, for the Staff path, notify the Student and write an admin_audit_logs row."),
            ]),
        ],
        "sql": """-- createTicket(): every new ticket starts OPEN, owned by the creating student
INSERT INTO tickets (student_id, subject, content, category, priority, status, created_at)
VALUES (?, ?, ?, ?, ?, 'OPEN', GETDATE());

-- assignTicket(): Staff Manager approves + hands off to a specific Staff
UPDATE tickets SET assigned_to = ?, status = 'ASSIGNED' WHERE ticket_id = ? AND status = 'OPEN';

-- addStaffReply(): only the assignee or a manager may write here (checked in Java, not SQL)
INSERT INTO ticket_replies (ticket_id, staff_sender_id, message, created_at) VALUES (?, ?, ?, GETDATE());
UPDATE tickets SET status = 'IN_PROGRESS', last_reply_at = GETDATE() WHERE ticket_id = ? AND status IN ('OPEN','ASSIGNED');

-- closeTicket() (staff/manager path): terminal state RESOLVED
UPDATE tickets SET status = 'RESOLVED', resolved_at = GETDATE() WHERE ticket_id = ?;""",
    },
    {
        "name": "3.10 Staff Broadcast Notification",
        "class_png": "class-notification.png",
        "seq_png": "seq-notification.png",
        "seq_name": "Broadcast + Scheduled Email Delivery Sequence",
        "specs": [
            ("NotificationService / NotificationDispatcher Classes", [
                ("01", "broadcast(actorEmail, SendNotificationRequest)", "staffManagerGuard.requireManager blocks any non-STAFF_MANAGER from broadcasting system-wide (LESSON-003 pattern: role check, not UI hiding). resolveTargets(targetJlptLevel) resolves the audience — 'ALL'/blank = every ACTIVE student, otherwise students at one JLPT level. Returns a synthetic jobId immediately (job_notification_<epochMillis>) without waiting for the fan-out to finish."),
                ("02", "NotificationDispatcher.broadcastAsync(targets, request, staff)", "Runs on a separate @Async bean specifically so @Async isn't bypassed by Spring's self-invocation proxy limitation (a same-class call would run synchronously). Writes one Notification row per target student on a background thread — the HTTP response already returned before this finishes (Async AI/Integration anti-pattern avoided: always returns a job id, never blocks the request thread, per ADR/anti-pattern table 'Sync AI Calls')."),
                ("03", "NotificationDispatcher.deliverPendingEmails()", "@Scheduled(fixedDelay=60_000) — every 60s, batches up to 100 notifications whose channel is EMAIL/BOTH and sentAt IS NULL and scheduledAt has arrived, and emails each one. Best-effort: a failed send is logged but sentAt is still stamped, so a permanently-broken address can't loop-retry forever (documented trade-off, not silent failure — LESSON-006 partially applies: failures are logged, though there's no retry/backoff here since notifications are non-critical)."),
            ]),
            ("NotificationRuleService Class (Admin-side configuration, UC-40)", [
                ("01", "createRule/updateRule(NotificationRuleRequest, adminId)", "Stores each rule as a JSON blob in system_settings (settingGroup='notification', settingKey=ruleKey) — enabled flag, trigger condition, channel, and message template. As of this build, this is configuration metadata only: no scheduled/event-driven consumer reads triggerCondition to fire notifications automatically yet; actual sends still go through the explicit broadcast()/notifyStudent() calls documented above."),
            ]),
        ],
        "sql": """-- broadcastAsync(): one row per targeted student, written off the request thread
INSERT INTO notifications (student_id, title, content, notification_type, channel, is_auto, staff_creator_id, created_at)
VALUES (?, ?, ?, ?, ?, 0, ?, GETDATE());

-- deliverPendingEmails(): due batch, channel EMAIL/BOTH, not yet sent
SELECT * FROM notifications
WHERE channel IN ('EMAIL','BOTH') AND sent_at IS NULL AND scheduled_at <= GETDATE()
ORDER BY scheduled_at ASC LIMIT 100;

UPDATE notifications SET sent_at = GETDATE() WHERE notification_id = ?;

-- NotificationRuleService: rules live in the generic system_settings table, not a dedicated table
INSERT INTO system_settings (setting_group, setting_key, setting_value, is_editable, updated_by)
VALUES ('notification', ?, ?, 1, ?);""",
    },
    {
        "name": "3.11 Published Content Lifecycle (Unpublish/Archive/Delete/Restore)",
        "class_png": "class-publishedcontent.png",
        "seq_png": "seq-publishedcontent.png",
        "seq_name": "Change Status / Restore Sequence",
        "specs": [
            ("PublishedContentService Class", [
                ("01", "changeStatus(managerEmail, contentId, ChangeStatusRequest)", "requireManager guards STAFF_MANAGER-only (enforced in the Service layer since the JWT only grants ROLE_STAFF to every staff account — the Controller's @PreAuthorize can't tell managers apart from regular Staff). Only operates on status=published content. findBlockingReferences runs in the SAME transaction right before the status flip (FR-34-14..17): e.g. a Question still assigned to a live Quiz can't be archived/deleted until that reference is removed — throws ResourceInUseException listing every blocker."),
                ("02", "ManagedContentHandler.changeStatus(contentId, target, now)", "One handler implementation per content type (lesson/kanji/vocabulary/grammar/question/assessment), same resolver pattern as ContentReviewService in §3.6. The UPDATE is conditioned on status='published'; if 0 rows are affected the content left 'published' concurrently (another Manager acted first) and the service throws InvalidStateTransitionException instead of silently no-op'ing — the same optimistic-concurrency guard as ContentReviewService.ensureUpdated()."),
                ("03", "restore(managerEmail, contentId, RestoreContentRequest)", "'deleted' is a terminal state — restoring a deleted item throws RestoreNotAllowedException (ADR-004: soft delete is one-way from the Manager's UI; a DBA could still reverse it directly, but the API never does). Only 'archived' -> 'published' is a valid restore; anything else is InvalidStateTransitionException."),
            ]),
        ],
        "sql": """-- changeStatus(): guarded by both the current status AND absence of blocking references
UPDATE questions SET status = ?, updated_at = ? WHERE question_id = ? AND status = 'published';

-- findBlockingReferences() example: is this question still assigned to any assessment?
SELECT * FROM question_assignments WHERE question_id = ? AND parent_type = 'ASSESSMENT';

-- restore(): archived -> published only; deleted is terminal and can never be restored via this API
UPDATE questions SET status = 'published', updated_at = ? WHERE question_id = ? AND status = 'archived';

-- every lifecycle transition is audited (reuses ContentReview's audit table)
INSERT INTO admin_audit_logs (staff_id, action, target_table, target_id, note, created_at)
VALUES (?, ?, ?, ?, ?, GETDATE());""",
    },
]

STATE_DIAGRAMS = [
    ("2.1 Test Attempt Status", "state-test-attempt.png",
     "Status column: test_attempts.status. Applies to every quiz/exam/practice attempt (UC-10, UC-11). "
     "The attempt starts in_progress on the server, and moves to exactly one final state — submitted (student "
     "action), auto_submitted (server-side timer), or abandoned (session ends without submitting). All three "
     "final states are immutable once reached (BR-04): the score and answers can never be changed afterward."),
    ("2.2 Student Submission Status", "state-submission.png",
     "Status column: student_submissions.status. Applies to speaking (UC-13) and handwriting (UC-20) submissions. "
     "The current code path is pending -> graded / rejected by Staff (SupportTicketService.manualGrade); the "
     "ai_graded state and its transitions (dashed, red) are legacy — the schema still allows them for old data, "
     "but no current code path creates them, since AI auto-grading was removed (commit db0d1648)."),
    ("2.3 Learning Content Review Status", "state-content-review.png",
     "Shared status column used by lessons, kanji, vocabulary, grammar_points, questions, and assessments. "
     "Staff authors content as draft, submits it for pending_review, and a StaffManager either approves "
     "(published), rejects, or requests changes (back to draft) — see ContentReviewService in §3.6. Published "
     "content can later be archived/restored, and archived content can be soft-deleted (ADR-004: is_deleted "
     "flag or status=deleted, never a hard DELETE)."),
]


# --------------------------------------------------------------------------- #
# Assembly
# --------------------------------------------------------------------------- #

def make_docx():
    doc = Document(str(TEMPLATE))
    clear_document(doc)
    for section in doc.sections:
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(0.9)
        section.right_margin = Inches(0.9)

    if LOGO.exists():
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run()
        r.add_picture(str(LOGO), width=Inches(1.4))
    add_p(doc, "SOFTWARE DESIGN SPECIFICATION", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=18)
    add_p(doc, "Japanese Skill Practice Platform", bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, size=16)
    add_p(doc, "- Hanoi, July 2026 -", align=WD_ALIGN_PARAGRAPH.CENTER)
    add_p(doc, "Table of Contents", bold=True)
    add_toc_field(doc)

    add_h(doc, "I. Record of Changes", 1)
    add_table(doc, ["Date", "A/M/D", "In charge", "Change Description"], [
        ("2026-07-22", "A", "AI Agent", "Initial SDS_Document.docx built from the old Temp_Document/Template3 template: Overview (14 packages, DB schema) and 6 Code Designs flows (Auth, Quiz Submission, Flashcard SRS, Kanji Writing, Speaking Grading, Content Review)."),
        ("2026-07-22", "M", "AI Agent", "Corrected the Speaking Submission Grading flow to match current code (no AI auto-grading step, commit db0d1648); translated to English."),
        ("2026-07-24", "M", "AI Agent", "Rebuilt against the correct template (Guides Templates-20260721/Template2_SDS Document.docx): restructured into I/II + 1.High Level Design (1.1 new Architecture diagram, 1.2 Package Diagram, 1.3 Database Design rewritten as one sub-section per table with real PK/FK/UN/NN fields from V1__init_schema.sql) + 2.State Transition Diagrams (new — 3 real status columns) + 3.Detailed Design (6 features, kept Class Specifications/Database Queries as bonus content). Fixed every diagram where an arrow or line previously crossed through another box's text (class diagrams, package diagram, ER diagram)."),
        ("2026-07-24", "M", "AI Agent", "Fixed section 3's heading skeleton to match Template2 exactly (3.N.1 Class Diagram, 3.N.2 <named Sequence Diagram> — e.g. '3.1.2 User Login Sequence' — instead of a generic '3.N.3 Sequence Diagram(s)'/'3.N.4 Database Queries'); Class Specifications and Database Queries are now supporting content under those two headings, not separate numbered sections."),
        ("2026-07-27", "A", "AI Agent", "Added 5 flows that were missing from section 3 (3.7 Staff Content Authoring & Submit for Review, 3.8 Admin User Management, 3.9 Student Support Ticket, 3.10 Staff Broadcast Notification, 3.11 Published Content Lifecycle), each read directly from source and covering a distinct backend module (staffcontent, admin, support, notification, publishedcontent) not previously documented."),
    ])
    add_p(doc, "*A - Added, M - Modified, D - Deleted")

    add_h(doc, "II. Software Design Document", 1)

    # ---- 1. High Level Design ----
    add_h(doc, "1. High Level Design", 2)

    add_h(doc, "1.1 Software Architecture", 3)
    add_p(doc, "The system is a monolithic Spring Boot 3 / Java 21 backend (ADR-002) behind a REST API, "
               "serving a React 18 single-page frontend, with MySQL 8 as the primary datastore (ADR-009) and "
               "four external systems reached only from the backend: SMTP (email), Google OAuth (social login), "
               "file storage for media (ADR-006 — no BLOB in the DB), and an external AI service for OCR/Speech "
               "scoring, always called asynchronously (ADR anti-pattern: no Sync AI Calls).")
    add_picture_if_exists(doc, DIA_SDS / "architecture.png")

    add_h(doc, "1.2 Package Diagram", 3)
    add_picture_if_exists(doc, DIA_RDS / "package-diagram.png")
    add_p(doc, "Package descriptions", bold=True)
    add_table(doc, ["No", "Package", "Description"], PACKAGES)

    add_h(doc, "1.3 Database Design", 3)
    add_p(doc, "DBMS: MySQL 8.4 (LTS), database JLPT_LearningDB, utf8mb4/utf8mb4_unicode_ci, container UTC "
               "(ADR-009). Schema source of truth: apps/backend/src/main/resources/db/migration/V1__init_schema.sql. "
               "The diagram below and the 13 tables detailed in this section are the core tables behind the 6 "
               "flows in section 3 — see docs/02-SDD-Architecture/database-design/JLPT_database.md for the full "
               "table list (note: that markdown predates the MySQL migration in a few narrative details; this "
               "section's field-level tables come directly from the current SQL migration, not from that file).")
    add_picture_if_exists(doc, DIA_RDS / "er-diagram.png")
    for i, (table_name, desc, fields) in enumerate(DB_TABLES, 1):
        add_h(doc, f"1.3.{i} {table_name}", 4)
        add_p(doc, desc, italic=True)
        add_field_table(doc, fields)

    # ---- 2. State Transition Diagrams ----
    add_h(doc, "2. State Transition Diagrams", 2)
    for title, png, desc in STATE_DIAGRAMS:
        add_h(doc, title, 3)
        add_p(doc, desc)
        add_picture_if_exists(doc, DIA_SDS / png)

    # ---- 3. Detailed Design ----
    # Template2 skeleton per feature is exactly two Heading-4 children:
    #   3.N.1 Class Diagram
    #   3.N.2 <Sequence Diagram Name>  (3.N.3, 3.N.4, ... for additional sequence
    #                                    diagrams of the same feature, if any)
    # Class Specifications / Database Queries are not separate numbered
    # headings in the template — they are folded in as supporting content
    # right under the diagram they document, so the heading outline matches
    # Template2 exactly while keeping the real, code-grounded detail.
    add_h(doc, "3. Detailed Design", 2)
    add_p(doc, "Each feature below provides a Class Diagram and a Sequence Diagram, per Template2's structure. "
               "Class Specifications (method-level detail) and Database Queries are included as supporting "
               "content directly under the diagram they document, rather than as separate numbered sections.",
          italic=True)
    for feat in FEATURES:
        add_h(doc, feat["name"], 3)
        idx = feat["name"].split()[0]  # e.g. "3.1"

        add_h(doc, f"{idx}.1 Class Diagram", 4)
        add_picture_if_exists(doc, DIA_SDS / feat["class_png"])
        add_p(doc, "Class Specifications", bold=True)
        for class_name, rows in feat["specs"]:
            add_p(doc, class_name, bold=True, size=10)
            add_table(doc, ["No", "Method", "Description"], rows)

        add_h(doc, f"{idx}.2 {feat['seq_name']}", 4)
        add_picture_if_exists(doc, DIA_SDS / feat["seq_png"])
        add_p(doc, "Database Queries", bold=True)
        add_sql(doc, feat["sql"])

    doc.save(str(OUT))
    print("saved", OUT)


if __name__ == "__main__":
    make_docx()
