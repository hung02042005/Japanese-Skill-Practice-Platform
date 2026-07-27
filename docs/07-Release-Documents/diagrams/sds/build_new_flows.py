"""Builds the drawio source + PNG for the 5 new SDS §3 flows (3.7-3.11), added
to document the flows that were missing from the original 6 (auth, quiz,
flashcard, kanji, speaking, content review):
    3.7  Staff Content Authoring & Submit for Review
    3.8  Admin User Management
    3.9  Student Support Ticket
    3.10 Staff Broadcast Notification (async fan-out + scheduled email)
    3.11 Published Content Lifecycle (Unpublish/Archive/Delete/Restore)

Class diagram boxes are laid out with stack_column() (boxes stacked in a
column, y computed from actual wrapped-text height) instead of hand-picked
x/y, since hand-picked coordinates don't know how many lines a method list
will wrap to and boxes end up overlapping.

Run from repo root:
    python "docs/07-Release-Documents/diagrams/sds/build_new_flows.py"
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from gen_diagrams import build_class_diagram, build_sequence_diagram
from render_png import render_class_diagram_png, render_sequence_diagram_png
from render_png import _box_height as rp_box_height

OUT = Path(__file__).parent


def stack_column(x, w, items, gap=50, start_y=40):
    """Stack boxes vertically in one column; y is computed from each box's
    actual wrapped-text height so later boxes never overlap earlier ones."""
    y = start_y
    out = []
    for it in items:
        b = dict(it)
        b["x"] = x
        b["w"] = w
        b["y"] = y
        h = rp_box_height(b.get("methods", []), w)
        out.append(b)
        y += h + gap
    return out


def bounds(boxes, margin=50):
    max_x = max(b["x"] + b.get("w", 260) for b in boxes) + margin
    max_y = max(b["y"] + rp_box_height(b.get("methods", []), b.get("w", 260)) for b in boxes) + margin
    return max_x, max_y


def save_class(name, boxes, edges):
    page_w, page_h = bounds(boxes)
    build_class_diagram(OUT / f"{name}.drawio", boxes=boxes, edges=edges, page_w=page_w, page_h=page_h)
    render_class_diagram_png(OUT / f"{name}.png", boxes=boxes, edges=edges, page_w=page_w, page_h=page_h)


def emit_seq(name, **kwargs):
    build_sequence_diagram(OUT / f"{name}.drawio", **kwargs)
    render_sequence_diagram_png(OUT / f"{name}.png", **kwargs)


# --------------------------------------------------------------------------- #
# 3.7 Staff Content Authoring & Submit for Review
# --------------------------------------------------------------------------- #

col1 = stack_column(40, 320, [
    {"id": "c1", "name": "StaffGrammarController", "header_color": "#ffe6cc", "methods": [
        "+ createGrammar(CreateGrammarRequest): ApiResponse~GrammarDetailResponse~",
        "+ listGrammars(level,status,page,size): ApiResponse~Map~",
        "+ updateGrammar(id, UpdateGrammarRequest): ApiResponse~GrammarDetailResponse~",
    ]},
    {"id": "c3", "name": "StaffGrammarService", "interface": True, "methods": [
        "+ createGrammar(request, staffEmail): GrammarDetailResponse",
        "+ updateGrammar(id, request, staffEmail): GrammarDetailResponse",
        "+ submitForReview(id, staffEmail): GrammarSubmitReviewResponse",
    ]},
    {"id": "c4", "name": "StaffGrammarServiceImpl", "methods": [
        "+ createGrammar(request, staffEmail): GrammarDetailResponse",
        "+ submitForReview(id, staffEmail): GrammarSubmitReviewResponse",
        "- guardOwnershipOrManager(grammar, staff): void",
    ]},
])
col2 = stack_column(420, 320, [
    {"id": "c2", "name": "StaffGrammarSubmitReviewController", "header_color": "#ffe6cc", "methods": [
        "+ submitReview(grammarId): ApiResponse~GrammarSubmitReviewResponse~",
    ]},
    {"id": "c6", "name": "GrammarPoint", "methods": [
        "+ Long id", "+ ContentStatus status  {DRAFT|PENDING_REVIEW|PUBLISHED|REJECTED}",
        "+ StaffUser createdBy", "+ JlptLevel jlptLevel",
    ]},
])
col3 = stack_column(800, 300, [
    {"id": "c7", "name": "StaffUserRepository", "header_color": "#d5e8d4", "methods": [
        "+ findByEmail(email): Optional~StaffUser~",
    ]},
    {"id": "c5", "name": "StaffGrammarRepository", "header_color": "#d5e8d4", "methods": [
        "+ findByCreatedByWithFilters(...): Page~GrammarPoint~",
        "+ findByIdAndStatusNot(id, DELETED): Optional~GrammarPoint~",
    ]},
])
save_class("class-staffcontent", col1 + col2 + col3, edges=[
    {"src": "c1", "tgt": "c3"}, {"src": "c2", "tgt": "c3"},
    {"src": "c4", "tgt": "c3", "arrow": "block", "label": "implements"},
    {"src": "c4", "tgt": "c5"}, {"src": "c4", "tgt": "c6"}, {"src": "c4", "tgt": "c7"},
])

emit_seq(
    "seq-staffcontent",
    participants=[("staff", "Staff", 20), ("fe", "Frontend", 220), ("sgc", "StaffGrammarController", 420),
                  ("src", "StaffGrammarSubmitReviewController", 680), ("svc", "StaffGrammarServiceImpl", 960),
                  ("db", "MySQL", 1200)],
    messages=[
        {"src_id": "staff", "tgt_id": "fe", "y": 110, "label": "1: fill in grammar form, click Save"},
        {"src_id": "fe", "tgt_id": "sgc", "y": 150, "label": "2: POST /api/staff/grammar"},
        {"src_id": "sgc", "tgt_id": "svc", "y": 190, "label": "3: createGrammar(request, staffEmail)"},
        {"src_id": "svc", "tgt_id": "db", "y": 230, "label": "4: INSERT INTO grammar_points (status='DRAFT', created_by=?)"},
        {"src_id": "svc", "tgt_id": "sgc", "y": 270, "label": "5: GrammarDetailResponse{status=draft}", "dashed": True},
        {"src_id": "sgc", "tgt_id": "fe", "y": 310, "label": "6: 201 Created", "dashed": True},
        {"src_id": "staff", "tgt_id": "fe", "y": 380, "label": "7: click \"Submit for review\" once content is complete"},
        {"src_id": "fe", "tgt_id": "src", "y": 420, "label": "8: POST /api/staff/grammar/{id}/submit-review"},
        {"src_id": "src", "tgt_id": "svc", "y": 460, "label": "9: submitForReview(grammarId, staffEmail)"},
        {"src_id": "svc", "tgt_id": "svc", "y": 500, "label": "10: guardOwnershipOrManager + check mandatory fields"},
        {"src_id": "svc", "tgt_id": "db", "y": 540, "label": "11: UPDATE grammar_points SET status='PENDING_REVIEW'"},
        {"src_id": "svc", "tgt_id": "src", "y": 580, "label": "12: GrammarSubmitReviewResponse", "dashed": True},
        {"src_id": "src", "tgt_id": "fe", "y": 620, "label": "13: 200 OK (now waits in Manager's review queue -> §3.6)", "dashed": True},
    ],
    frames=[{"label": "alt not owner & not manager->403 / status not DRAFT/REJECTED->409 / missing required field->400", "x1": 700, "x2": 1250, "y1": 445, "y2": 600}],
    page_w=1450, page_h=700,
)

# --------------------------------------------------------------------------- #
# 3.8 Admin User Management
# --------------------------------------------------------------------------- #

col1 = stack_column(40, 320, [
    {"id": "c1", "name": "AdminController", "header_color": "#ffe6cc", "methods": [
        "+ listUsers(type,q,status,...): ApiResponse~Map~",
        "+ createStaff(CreateStaffRequest): ApiResponse~CreateStaffResponse~",
        "+ suspendUser(type,id,SuspendUserRequest): ApiResponse~SuspendUserResponse~",
        "+ activateUser(type,id): ApiResponse~ActivateUserResponse~",
        "+ softDeleteUser(type,id): ApiResponse~SoftDeleteUserResponse~",
        "+ restoreUser(type,id): ApiResponse~RestoreUserResponse~",
        "+ changeStaffRole(staffId,ChangeStaffRoleRequest): ApiResponse~ChangeStaffRoleResponse~",
    ]},
])
col2 = stack_column(420, 320, [
    {"id": "c2", "name": "AdminUserService", "methods": [
        "+ createStaff(adminEmail, request): CreateStaffResponse",
        "+ suspendUser(adminEmail, type, id, request): SuspendUserResponse",
        "+ softDeleteUser(adminEmail, type, id): SoftDeleteUserResponse",
        "+ restoreUser(adminEmail, type, id): RestoreUserResponse",
        "- checkSelfModification(actorId, type, targetId): void",
        "- auditLog(actor, action, table, id, desc): void",
    ]},
    {"id": "c5", "name": "AuthTokenRepository", "header_color": "#d5e8d4", "methods": [
        "+ revokeAllActiveByStudentId(id, now): int",
        "+ revokeAllActiveByStaffId(id, now): int",
    ]},
    {"id": "c6", "name": "AdminAuditLogRepository", "header_color": "#d5e8d4", "methods": [
        "+ save(AdminAuditLog): AdminAuditLog",
    ]},
])
col3 = stack_column(800, 300, [
    {"id": "c3", "name": "StudentUserRepository", "header_color": "#d5e8d4", "methods": [
        "+ findById(id): Optional~StudentUser~",
    ]},
    {"id": "c4", "name": "StaffUserRepository", "header_color": "#d5e8d4", "methods": []},
    {"id": "c7", "name": "AdminAuditLog", "methods": [
        "+ Long id", "+ AdminUser adminActor", "+ String action",
        "+ String targetTable", "+ Long targetId", "+ String description",
    ]},
])
save_class("class-admin-user", col1 + col2 + col3, edges=[
    {"src": "c1", "tgt": "c2"}, {"src": "c2", "tgt": "c3"}, {"src": "c2", "tgt": "c4"},
    {"src": "c2", "tgt": "c5"}, {"src": "c2", "tgt": "c6"}, {"src": "c6", "tgt": "c7"},
])

emit_seq(
    "seq-admin-user",
    participants=[("admin", "Admin", 20), ("fe", "Frontend", 220), ("ac", "AdminController", 420),
                  ("aus", "AdminUserService", 660), ("atr", "AuthTokenRepository", 900),
                  ("db", "MySQL", 1120)],
    messages=[
        {"src_id": "admin", "tgt_id": "fe", "y": 110, "label": "1: open user detail, click \"Suspend\", enter reason"},
        {"src_id": "fe", "tgt_id": "ac", "y": 150, "label": "2: POST /api/admin/users/{type}/{id}/suspend"},
        {"src_id": "ac", "tgt_id": "aus", "y": 190, "label": "3: suspendUser(adminEmail, type, id, request)"},
        {"src_id": "aus", "tgt_id": "aus", "y": 230, "label": "4: checkSelfModification(actorId, type, id)"},
        {"src_id": "aus", "tgt_id": "db", "y": 300, "label": "5: SELECT * FROM student_users WHERE id=?"},
        {"src_id": "aus", "tgt_id": "db", "y": 340, "label": "6: UPDATE student_users SET status='SUSPENDED', suspend_reason=?"},
        {"src_id": "aus", "tgt_id": "atr", "y": 380, "label": "7: revokeAllActiveByStudentId(id, now)"},
        {"src_id": "atr", "tgt_id": "db", "y": 420, "label": "8: UPDATE auth_tokens SET revoked_at=? WHERE actor_id=? AND revoked_at IS NULL"},
        {"src_id": "aus", "tgt_id": "db", "y": 460, "label": "9: INSERT INTO admin_audit_logs (action='suspend_user', ...)"},
        {"src_id": "aus", "tgt_id": "ac", "y": 500, "label": "10: SuspendUserResponse{status=suspended}", "dashed": True},
        {"src_id": "ac", "tgt_id": "fe", "y": 540, "label": "11: 200 OK (student's active sessions are now revoked)", "dashed": True},
    ],
    frames=[{"label": "alt admin targets own account->403 / already suspended/deleted->409 / valid->continue", "x1": 400, "x2": 1170, "y1": 265, "y2": 560}],
    page_w=1350, page_h=620,
)

# --------------------------------------------------------------------------- #
# 3.9 Student Support Ticket
# --------------------------------------------------------------------------- #

col1 = stack_column(40, 340, [
    {"id": "c1", "name": "SupportController", "header_color": "#ffe6cc", "methods": [
        "+ createTicket(TicketRequest): ApiResponse~TicketResponse~",
        "+ replyToTicket(id, TicketReplyRequest): ApiResponse~TicketReplyResponse~",
        "+ closeTicket(id): ApiResponse~TicketResponse~",
    ]},
    {"id": "c3", "name": "SupportTicketService", "methods": [
        "+ createTicket(studentId, TicketRequest): TicketResponse",
        "+ addStudentReply(id, studentId, req): TicketReplyResponse",
        "+ addStaffReply(id, staffEmail, req): TicketReplyResponse",
        "+ assignTicket(id, staffId, actorEmail, isAdmin): TicketResponse",
        "+ closeTicket(id, actorEmail): TicketResponse",
    ]},
    {"id": "c6", "name": "Ticket", "methods": [
        "+ Long id", "+ TicketStatus status  {OPEN|ASSIGNED|IN_PROGRESS|RESOLVED|CLOSED}",
        "+ Priority priority", "+ StaffUser assignedTo", "+ StudentUser student",
    ]},
])
col2 = stack_column(460, 300, [
    {"id": "c2", "name": "StaffSupportController", "header_color": "#ffe6cc", "methods": [
        "+ getAllTickets(status,category,priority,q): ApiResponse~Map~",
        "+ assignTicket(id, AssignTicketRequest): ApiResponse~TicketResponse~",
        "+ replyToTicket(id, TicketReplyRequest): ApiResponse~TicketReplyResponse~",
        "+ closeTicket(id): ApiResponse~TicketResponse~",
    ]},
])
col3 = stack_column(840, 300, [
    {"id": "c7", "name": "NotificationService", "methods": [
        "+ notifyStudent(student, title, content, type, key, staff): void",
    ]},
    {"id": "c4", "name": "TicketRepository", "header_color": "#d5e8d4", "methods": [
        "+ findByStudentId(id, Pageable): Page~Ticket~",
        "+ findAllByFilters(...): Page~Ticket~",
    ]},
    {"id": "c5", "name": "TicketReplyRepository", "header_color": "#d5e8d4", "methods": [
        "+ findByTicketIdOrderByCreatedAtAsc(id): List~TicketReply~",
    ]},
])
save_class("class-support-ticket", col1 + col2 + col3, edges=[
    {"src": "c1", "tgt": "c3"}, {"src": "c2", "tgt": "c3"}, {"src": "c3", "tgt": "c4"},
    {"src": "c3", "tgt": "c5"}, {"src": "c3", "tgt": "c6"}, {"src": "c3", "tgt": "c7"},
])

emit_seq(
    "seq-support-ticket",
    participants=[("student", "Student", 20), ("sc", "SupportController", 220), ("svc", "SupportTicketService", 480),
                  ("db", "MySQL", 720), ("mgr", "Staff Manager", 900), ("ssc", "StaffSupportController", 1060),
                  ("staff", "Assigned Staff", 1320)],
    messages=[
        {"src_id": "student", "tgt_id": "sc", "y": 110, "label": "1: POST /api/support/tickets {subject,content}"},
        {"src_id": "sc", "tgt_id": "svc", "y": 150, "label": "2: createTicket(studentId, request)"},
        {"src_id": "svc", "tgt_id": "db", "y": 190, "label": "3: INSERT INTO tickets (status='OPEN')"},
        {"src_id": "svc", "tgt_id": "sc", "y": 230, "label": "4: 201 Created", "dashed": True},
        {"src_id": "mgr", "tgt_id": "ssc", "y": 300, "label": "5: POST /api/staff/tickets/{id}/assign {staffId}"},
        {"src_id": "ssc", "tgt_id": "svc", "y": 340, "label": "6: assignTicket(id, staffId, mgrEmail, false)"},
        {"src_id": "svc", "tgt_id": "db", "y": 380, "label": "7: UPDATE tickets SET assigned_to=?, status='ASSIGNED'"},
        {"src_id": "staff", "tgt_id": "ssc", "y": 450, "label": "8: POST /api/staff/tickets/{id}/reply {message}"},
        {"src_id": "ssc", "tgt_id": "svc", "y": 490, "label": "9: addStaffReply(id, staffEmail, request)"},
        {"src_id": "svc", "tgt_id": "db", "y": 530, "label": "10: INSERT INTO ticket_replies; UPDATE tickets SET status='IN_PROGRESS'"},
        {"src_id": "svc", "tgt_id": "student", "y": 570, "label": "11: notifyStudent(\"Ticket has a new reply\")", "dashed": True},
        {"src_id": "staff", "tgt_id": "ssc", "y": 630, "label": "12: POST /api/staff/tickets/{id}/close"},
        {"src_id": "ssc", "tgt_id": "svc", "y": 670, "label": "13: closeTicket(id, staffEmail)"},
        {"src_id": "svc", "tgt_id": "db", "y": 710, "label": "14: UPDATE tickets SET status='RESOLVED', resolved_at=NOW()"},
    ],
    frames=[
        {"label": "OPEN (student) -> ASSIGNED (manager) -> IN_PROGRESS (assignee replies) -> RESOLVED (assignee closes)", "x1": 180, "x2": 1400, "y1": 90, "y2": 730},
    ],
    page_w=1500, page_h=800,
)

# --------------------------------------------------------------------------- #
# 3.10 Staff Broadcast Notification (async fan-out + scheduled email delivery)
# --------------------------------------------------------------------------- #

col1 = stack_column(40, 320, [
    {"id": "c1", "name": "StaffNotificationController", "header_color": "#ffe6cc", "methods": [
        "+ broadcast(SendNotificationRequest): ApiResponse~Map~jobId~~",
    ]},
    {"id": "c2", "name": "AdminNotificationRuleController", "header_color": "#ffe6cc", "methods": [
        "+ list(): ApiResponse~List~NotificationRuleResponse~~",
        "+ create(NotificationRuleRequest): ApiResponse~NotificationRuleResponse~",
    ]},
])
col2 = stack_column(420, 300, [
    {"id": "c3", "name": "NotificationService", "methods": [
        "+ broadcast(actorEmail, request): String jobId",
        "+ notifyStudent(student, title, content, type, key, staff): void",
        "- resolveTargets(jlptLevel): List~StudentUser~",
    ]},
    {"id": "c4", "name": "NotificationRuleService", "methods": [
        "+ createRule(request, adminId): NotificationRuleResponse",
        "+ listRules(): List~NotificationRuleResponse~",
    ]},
])
col3 = stack_column(780, 320, [
    {"id": "c5", "name": "NotificationDispatcher", "methods": [
        "+ @Async broadcastAsync(targets, request, staff): CompletableFuture~Void~",
        "+ @Scheduled deliverPendingEmails(): void  (every 60s)",
    ]},
    {"id": "c6", "name": "NotificationRepository", "header_color": "#d5e8d4", "methods": [
        "+ save(Notification): Notification",
        "+ findDuePendingEmails(channels, now, page): List~Notification~",
    ]},
    {"id": "c8", "name": "Notification", "methods": [
        "+ Long id", "+ Channel channel  {IN_APP|EMAIL|BOTH}",
        "+ Boolean isAuto", "+ LocalDateTime sentAt", "+ LocalDateTime scheduledAt",
    ]},
])
col4 = stack_column(1150, 280, [
    {"id": "c7", "name": "EmailService", "methods": [
        "+ sendNotificationEmail(to, title, content): void",
    ]},
])
save_class("class-notification", col1 + col2 + col3 + col4, edges=[
    {"src": "c1", "tgt": "c3"}, {"src": "c2", "tgt": "c4"}, {"src": "c3", "tgt": "c5"},
    {"src": "c5", "tgt": "c6"}, {"src": "c5", "tgt": "c7"}, {"src": "c6", "tgt": "c8"},
])

emit_seq(
    "seq-notification",
    participants=[("mgr", "Staff Manager", 20), ("fe", "Frontend", 220), ("snc", "StaffNotificationController", 420),
                  ("ns", "NotificationService", 680), ("nd", "NotificationDispatcher", 920),
                  ("db", "MySQL", 1140), ("es", "EmailService", 1340)],
    messages=[
        {"src_id": "mgr", "tgt_id": "fe", "y": 110, "label": "1: compose broadcast (title, content, target level)"},
        {"src_id": "fe", "tgt_id": "snc", "y": 150, "label": "2: POST /api/staff/notifications"},
        {"src_id": "snc", "tgt_id": "ns", "y": 190, "label": "3: broadcast(actorEmail, request)"},
        {"src_id": "ns", "tgt_id": "ns", "y": 230, "label": "4: requireManager + resolveTargets(jlptLevel)"},
        {"src_id": "ns", "tgt_id": "nd", "y": 290, "label": "5: broadcastAsync(targets, request, staff)  [@Async, non-blocking]"},
        {"src_id": "ns", "tgt_id": "db", "y": 330, "label": "6: INSERT INTO admin_audit_logs (action='BROADCAST_SENT')"},
        {"src_id": "ns", "tgt_id": "snc", "y": 370, "label": "7: jobId", "dashed": True},
        {"src_id": "snc", "tgt_id": "fe", "y": 410, "label": "8: 202 Accepted { jobId }", "dashed": True},
        {"src_id": "nd", "tgt_id": "db", "y": 470, "label": "9 (async, per target): INSERT INTO notifications (channel, is_auto=false)"},
        {"src_id": "nd", "tgt_id": "db", "y": 540, "label": "10 (every 60s): SELECT * FROM notifications WHERE channel IN ('EMAIL','BOTH') AND sent_at IS NULL AND scheduled_at<=NOW()"},
        {"src_id": "nd", "tgt_id": "es", "y": 580, "label": "11: sendNotificationEmail(student.email, title, content)"},
        {"src_id": "nd", "tgt_id": "db", "y": 620, "label": "12: UPDATE notifications SET sent_at=NOW()  (best-effort, no infinite retry)"},
    ],
    frames=[
        {"label": "sync: HTTP request/response returns immediately with jobId", "x1": 400, "x2": 1400, "y1": 165, "y2": 430},
        {"label": "async: @Scheduled(fixedDelay=60000) NotificationDispatcher.deliverPendingEmails()", "x1": 900, "x2": 1420, "y1": 450, "y2": 640},
    ],
    page_w=1600, page_h=700,
)

# --------------------------------------------------------------------------- #
# 3.11 Published Content Lifecycle (Unpublish / Archive / Delete / Restore)
# --------------------------------------------------------------------------- #

col1 = stack_column(40, 320, [
    {"id": "c1", "name": "PublishedContentController", "header_color": "#ffe6cc", "methods": [
        "+ getPublishedContents(type,level,page,size): ApiResponse~PublishedContentListResponse~",
        "+ changeStatus(id, ChangeStatusRequest): ApiResponse~StatusChangeResultResponse~",
        "+ restore(id, RestoreContentRequest): ApiResponse~StatusChangeResultResponse~",
    ]},
    {"id": "c6", "name": "TargetStatus", "header_color": "#e1d5e7", "methods": [
        "UNPUBLISH  ->  archived", "DELETE  ->  deleted",
    ]},
])
col2 = stack_column(420, 320, [
    {"id": "c2", "name": "PublishedContentService", "methods": [
        "+ changeStatus(managerEmail, id, request): StatusChangeResultResponse",
        "+ restore(managerEmail, id, request): StatusChangeResultResponse",
        "- requireManager(email): StaffUser",
    ]},
    {"id": "c5", "name": "ReviewAuditService", "methods": [
        "+ log(manager, action, type, table, id, reason): void",
    ]},
])
col3 = stack_column(800, 300, [
    {"id": "c3", "name": "ManagedContentResolver", "methods": [
        "+ resolve(ContentType): ManagedContentHandler",
    ]},
    {"id": "c4", "name": "ManagedContentHandler", "interface": True, "methods": [
        "+ findById(id): Optional~ManagedContentSnapshot~",
        "+ findBlockingReferences(id): List~ReferenceItemResponse~",
        "+ changeStatus(id, target, now): int",
        "+ restore(id, now): int",
    ]},
])
save_class("class-publishedcontent", col1 + col2 + col3, edges=[
    {"src": "c1", "tgt": "c2"}, {"src": "c2", "tgt": "c3"}, {"src": "c3", "tgt": "c4"},
    {"src": "c2", "tgt": "c5"}, {"src": "c2", "tgt": "c6"},
])

emit_seq(
    "seq-publishedcontent",
    participants=[("mgr", "Staff Manager", 20), ("fe", "Frontend", 220), ("pcc", "PublishedContentController", 420),
                  ("pcs", "PublishedContentService", 680), ("hdl", "ManagedContentHandler", 920),
                  ("ras", "ReviewAuditService", 1140), ("db", "MySQL", 1340)],
    messages=[
        {"src_id": "mgr", "tgt_id": "fe", "y": 110, "label": "1: select published item -> Archive/Delete, enter reason"},
        {"src_id": "fe", "tgt_id": "pcc", "y": 150, "label": "2: PUT /api/manager/published-contents/{id}/status"},
        {"src_id": "pcc", "tgt_id": "pcs", "y": 190, "label": "3: changeStatus(managerEmail, id, request)"},
        {"src_id": "pcs", "tgt_id": "hdl", "y": 230, "label": "4: findById(id) -> check status=='published'"},
        {"src_id": "hdl", "tgt_id": "db", "y": 270, "label": "5: SELECT * FROM <content table> WHERE id=?"},
        {"src_id": "pcs", "tgt_id": "hdl", "y": 340, "label": "6: findBlockingReferences(id)"},
        {"src_id": "hdl", "tgt_id": "db", "y": 380, "label": "7: SELECT ... referencing rows (e.g. quiz using this question)"},
        {"src_id": "pcs", "tgt_id": "hdl", "y": 450, "label": "8: changeStatus(id, target, now)"},
        {"src_id": "hdl", "tgt_id": "db", "y": 490, "label": "9: UPDATE <content table> SET status=? WHERE id=? AND status='published'"},
        {"src_id": "pcs", "tgt_id": "ras", "y": 530, "label": "10: log(manager, action, type, table, id, reason)"},
        {"src_id": "ras", "tgt_id": "db", "y": 570, "label": "11: INSERT INTO admin_audit_logs(...)"},
        {"src_id": "pcs", "tgt_id": "pcc", "y": 610, "label": "12: StatusChangeResultResponse{status}", "dashed": True},
        {"src_id": "pcc", "tgt_id": "fe", "y": 650, "label": "13: 200 OK", "dashed": True},
    ],
    frames=[
        {"label": "alt blocking references exist->409 ResourceInUse / concurrent change (rows=0)->409 / valid->continue", "x1": 400, "x2": 1370, "y1": 265, "y2": 670},
    ],
    page_w=1550, page_h=730,
)

print("Wrote 10 .drawio + 10 .png files (3.7-3.11) to", OUT)
