# SQL Performance Guide — JLPT E-Learning (MySQL 8)

> **Mục đích**: Hướng dẫn tối ưu hiệu năng truy vấn MySQL cho toàn bộ hệ thống JLPT.
> **Lưu ý**: Tài liệu này được viết ban đầu cho SQL Server và đang được chuyển đổi. Một số khái niệm (như Query Store) có thể áp dụng tương đương qua Performance Schema của MySQL.
> Áp dụng cho mọi query trong JPA, JPQL, `@Query` native, và Flyway migration.
> Liên quan: [`constraints/global.md`](../../01-SRS-Requirements/constraints/global.md) (GLOB-PERF, GLOB-DB) | [`database/init.sql`](../../../database/init.sql).

---

## 1. Nguyên Tắc Cốt Lõi

| ID | Rule | Mức độ |
|----|------|--------|
| **PERF-01** | **TUYỆT ĐỐI KHÔNG** `SELECT *` trong query phức tạp — chỉ select cột cần thiết | 🔴 CRITICAL |
| **PERF-02** | Tránh N+1 query — dùng `JOIN FETCH` hoặc `@EntityGraph` | 🔴 CRITICAL |
| **PERF-03** | Mọi cột dùng trong `WHERE`, `JOIN ON`, `ORDER BY` phải có index | 🟠 HIGH |
| **PERF-04** | API CRUD thông thường: response time ≤ **500ms** (95th percentile) | 🟠 HIGH |
| **PERF-05** | Subscription VIP check cache tại Redis TTL ≤ **5 phút** — không query DB mỗi request | 🟠 HIGH |
| **PERF-06** | Tránh function trên cột trong `WHERE` — phá vỡ index scan | 🟡 MEDIUM |
| **PERF-07** | Pagination bắt buộc cho mọi API trả danh sách ≥ 20 phần tử | 🟡 MEDIUM |
| **PERF-08** | Kiểm tra Execution Plan bằng SSMS trước khi merge query phức tạp | 🟡 MEDIUM |

---

## 2. Anti-Patterns Phải Tránh

### 2.1. SELECT * — Cấm tuyệt đối

```sql
-- ❌ SAI — lấy toàn bộ cột, kể cả NVARCHAR(MAX) không cần
SELECT * FROM lessons WHERE status = 'published';

-- ✅ ĐÚNG — chỉ lấy đúng cột cần render
SELECT lesson_id, title, jlpt_level, lesson_type, display_order
FROM   lessons
WHERE  status = 'published'
  AND  jlpt_level = 'N3'
ORDER BY display_order;
```

**Lý do**: Bảng `lessons` có `content_text NVARCHAR(MAX)`, `explanation NVARCHAR(MAX)` — đọc
cột này không cần thiết làm tăng I/O và thời gian serialize đáng kể.

---

### 2.2. N+1 Query — Phổ biến nhất trong JPA

```java
// ❌ SAI — N+1: 1 query lấy attempts + N query lấy answers
List<TestAttempt> attempts = attemptRepo.findByStudentId(studentId);
for (TestAttempt a : attempts) {
    a.getAnswers().size(); // LAZY load → query riêng từng attempt
}

// ✅ ĐÚNG — JOIN FETCH trong 1 query duy nhất
@Query("""
    SELECT a FROM TestAttempt a
    JOIN FETCH a.answers
    WHERE a.studentId = :studentId
      AND a.status = 'submitted'
""")
List<TestAttempt> findWithAnswers(@Param("studentId") Long studentId);
```

```sql
-- ✅ ĐÚNG — dạng SQL thuần
SELECT ta.attempt_id, ta.total_score, ta.submitted_at,
       aa.question_id, aa.is_correct, aa.score
FROM   test_attempts   ta
JOIN   attempt_answers aa ON aa.attempt_id = ta.attempt_id
WHERE  ta.student_id = @studentId
  AND  ta.status     = 'submitted';
```

---

### 2.3. Function Trên Cột WHERE — Phá Index

```sql
-- ❌ SAI — YEAR() trên cột không thể dùng index
SELECT * FROM test_attempts
WHERE YEAR(submitted_at) = 2026;

-- ✅ ĐÚNG — range filter, index-friendly
SELECT attempt_id, student_id, total_score, submitted_at
FROM   test_attempts
WHERE  submitted_at >= '2026-01-01'
  AND  submitted_at <  '2027-01-01';
```

```sql
-- ❌ SAI — LOWER() trên email phá index
SELECT student_id FROM student_users WHERE LOWER(email) = 'user@example.com';

-- ✅ ĐÚNG — email lưu lowercase từ đầu hoặc dùng collation CI
SELECT student_id FROM student_users WHERE email = 'user@example.com';
```

---

### 2.4. Subquery Thay Vì JOIN

```sql
-- ❌ CHẬM — subquery chạy lại cho mỗi row
SELECT lesson_id, title,
       (SELECT COUNT(*) FROM question_assignments qa
        WHERE qa.parent_id = l.lesson_id AND qa.parent_type = 'lesson') AS q_count
FROM   lessons l
WHERE  status = 'published';

-- ✅ NHANH hơn — aggregation với LEFT JOIN
SELECT l.lesson_id, l.title, COUNT(qa.assignment_id) AS q_count
FROM   lessons l
LEFT JOIN question_assignments qa
       ON qa.parent_id = l.lesson_id AND qa.parent_type = 'lesson'
WHERE  l.status = 'published'
GROUP BY l.lesson_id, l.title;
```

---

### 2.5. Không Có Pagination

```sql
-- ❌ NGUY HIỂM — trả toàn bộ 10,000+ từ vựng
SELECT * FROM vocabulary WHERE status = 'published';

-- ✅ ĐÚNG — offset pagination
SELECT vocabulary_id, word, meaning, jlpt_level
FROM   vocabulary
WHERE  status     = 'published'
  AND  jlpt_level = @level
ORDER BY vocabulary_id
OFFSET (@page * @pageSize) ROWS
FETCH NEXT @pageSize ROWS ONLY;
```

> **Lưu ý OFFSET lớn**: Khi `@page` rất lớn (> 1000), OFFSET pagination chậm dần. Dùng
> **keyset pagination** (WHERE `vocabulary_id > @lastId`) cho API scroll/infinite.

---

## 3. Index Strategy theo Bảng

### 3.1. Index hiện có trong `init.sql`

| Bảng | Index | Dùng cho |
|------|-------|---------|
| `lessons` | `IX_lessons_public_list (lesson_type, status, jlpt_level, display_order)` | Student browse lessons by level |
| `lessons` | `IX_lessons_creator_status (created_by, status)` | Staff xem bài của mình |
| `vocabulary` | `IX_vocab_public_lookup (status, jlpt_level, topic)` | Browse vocab by level/topic |
| `vocabulary` | `IX_vocab_word (word)` | Dictionary search |
| `kanji` | `IX_kanji_public_level (status, jlpt_level)` | Browse kanji by level |
| `grammar_points` | `IX_grammar_public_level (status, jlpt_level)` | Browse grammar by level |
| `questions` | `IX_questions_public_bank (status, skill, jlpt_level)` | Question bank filter |
| `test_attempts` | `IX_attempt_student_type (student_id, attempt_type)` | Student history |
| `test_attempts` | `IX_attempt_student_status_date (student_id, status, submitted_at)` | Student result list |
| `flashcards` | `IX_flashcards_next_review (student_id, next_review_date)` | SRS daily review queue |
| `student_content_progress` | `IX_progress_bookmarks (student_id, is_bookmarked, content_type)` | Bookmark list |
| `auth_tokens` | `IX_auth_tokens_student_active (student_id, token_type, expires_at)` | JWT validation |
| `notifications` | `IX_notif_student_unread (student_id, is_read, created_at)` | Unread badge |

### 3.2. Khi nào cần thêm index mới

Thêm index khi:
- Query xuất hiện **≥ 3 lần** trong codebase và không có index phù hợp
- Execution Plan báo **Index Scan** thay vì **Index Seek** trên bảng > 10k row
- Trang hiển thị chậm > 500ms sau khi đã loại bỏ N+1

**Quy trình thêm index**:
1. Phân tích Execution Plan trong SSMS (`Ctrl+M` → chạy query)
2. Kiểm tra "Missing Index" suggestion trong plan
3. Viết Flyway migration mới (không DDL thủ công)
4. Đặt tên convention: `IX_<table>_<purpose>`

```sql
-- Ví dụ: Flyway migration V7__add_index_flashcards_content.sql
CREATE INDEX IX_flashcards_content_type
    ON flashcards(student_id, content_type, content_id)
    WHERE content_id IS NOT NULL;
GO
```

---

## 4. Query Patterns Theo Module

### 4.1. Auth — JWT Validation

```sql
-- Kiểm tra token hợp lệ (chạy mỗi request → phải cực nhanh)
SELECT token_id, actor_type, student_id, staff_id, admin_id, expires_at
FROM   auth_tokens
WHERE  token_value = @tokenValue   -- IX_auth_tokens_value
  AND  revoked_at  IS NULL
  AND  expires_at  > SYSUTCDATETIME();
-- ✅ Dùng index IX_auth_tokens_value + filter expires_at
```

### 4.2. Student — Browse Lessons by Level

```sql
-- Học viên xem danh sách bài học N3
SELECT lesson_id, title, lesson_type, jlpt_level, display_order,
       audio_url, video_url
FROM   lessons
WHERE  status     = 'published'
  AND  jlpt_level = 'N3'
ORDER BY display_order, lesson_id
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;
-- ✅ Dùng IX_lessons_public_list (lesson_type, status, jlpt_level, display_order)
-- ⚠️ Bỏ lesson_type filter nếu muốn tất cả loại bài → index vẫn hỗ trợ partial
```

### 4.3. Flashcard — SRS Daily Review Queue

```sql
-- Lấy flashcard cần ôn hôm nay (SRS)
SELECT flashcard_id, front_text, back_text, content_type,
       interval_days, repetition_count, last_rating
FROM   flashcards
WHERE  student_id       = @studentId
  AND  next_review_date <= CAST(GETDATE() AS DATE)
ORDER BY next_review_date, flashcard_id
OFFSET 0 ROWS FETCH NEXT 50 ROWS ONLY;
-- ✅ Dùng IX_flashcards_next_review (student_id, next_review_date)
```

### 4.4. Student Progress — Dashboard Stats

```sql
-- Thống kê tổng quan học viên (dashboard)
SELECT
    COUNT(*) FILTER (WHERE status = 'completed')  AS completed_count,
    COUNT(*) FILTER (WHERE status = 'learning')   AS learning_count,
    COUNT(*) FILTER (WHERE is_bookmarked = 1)     AS bookmark_count
FROM student_content_progress
WHERE student_id   = @studentId
  AND content_type = 'lesson';
-- SQL Server không có FILTER — dùng SUM(CASE WHEN)

SELECT
    SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END)  AS completed_count,
    SUM(CASE WHEN status = 'learning'  THEN 1 ELSE 0 END)  AS learning_count,
    SUM(CASE WHEN is_bookmarked = 1    THEN 1 ELSE 0 END)  AS bookmark_count
FROM   student_content_progress
WHERE  student_id   = @studentId
  AND  content_type = 'lesson';
-- ✅ Dùng IX_progress_student_type (student_id, content_type)
```

### 4.5. Staff — Content Management List

```sql
-- Staff xem danh sách bài mình tạo (có filter status)
SELECT l.lesson_id, l.title, l.jlpt_level, l.status,
       l.created_at, l.published_at,
       su.full_name AS approved_by_name
FROM   lessons l
LEFT JOIN staff_users su ON su.staff_id = l.approved_by
WHERE  l.created_by = @staffId
  AND  l.status     IN ('draft', 'pending_review', 'rejected')
ORDER BY l.updated_at DESC
OFFSET @offset ROWS FETCH NEXT 20 ROWS ONLY;
-- ✅ Dùng IX_lessons_creator_status (created_by, status)
```

### 4.6. Admin — Reporting Query

```sql
-- Báo cáo số lượt thi theo ngày (Admin dashboard)
SELECT
    CAST(submitted_at AS DATE) AS exam_date,
    attempt_type,
    COUNT(*)                   AS attempt_count,
    AVG(total_score)           AS avg_score
FROM   test_attempts
WHERE  submitted_at >= @fromDate
  AND  submitted_at <  @toDate
  AND  status IN ('submitted','auto_submitted')
GROUP BY CAST(submitted_at AS DATE), attempt_type
ORDER BY exam_date DESC;
-- ✅ Thêm index nếu báo cáo chạy thường xuyên:
-- CREATE INDEX IX_attempts_report ON test_attempts(submitted_at, attempt_type, status);
```

### 4.7. Subscription Check (Cached)

```java
// ✅ Pattern chuẩn: Cache Redis trước, query DB là fallback
public boolean hasActiveVipSubscription(Long studentId) {
    String cacheKey = "sub:vip:" + studentId;
    Boolean cached = redisTemplate.opsForValue().get(cacheKey, Boolean.class);
    if (cached != null) return cached;

    boolean isVip = subscriptionRepo.existsActiveVip(studentId); // DB query
    redisTemplate.opsForValue().set(cacheKey, isVip, 5, TimeUnit.MINUTES);
    return isVip;
}
```

```sql
-- Repository query (chỉ chạy khi cache miss)
SELECT TOP 1 1
FROM   student_subscriptions
WHERE  student_id  = @studentId
  AND  plan        = 'VIP'
  AND  is_active   = 1
  AND  end_date    > SYSUTCDATETIME();
```

---

## 5. JPA / Hibernate Best Practices

### 5.1. Tránh LAZY Loading ngoài Transaction

```java
// ❌ SAI — LazyInitializationException vì session đã đóng
@Service
public class LessonService {
    public List<String> getQuestionTexts(Long lessonId) {
        Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();
        return lesson.getQuestions().stream() // LAZY — session đã đóng!
                     .map(Question::getQuestionText)
                     .toList();
    }
}

// ✅ ĐÚNG — JOIN FETCH trong query
@Query("SELECT l FROM Lesson l JOIN FETCH l.questions WHERE l.lessonId = :id")
Optional<Lesson> findWithQuestions(@Param("id") Long id);
```

### 5.2. Projection thay vì Entity đầy đủ

```java
// ❌ KHÔNG CẦN load toàn bộ Entity khi chỉ cần vài field
List<Lesson> lessons = lessonRepo.findByStatus("published");

// ✅ Dùng Projection Interface — query chỉ SELECT cần thiết
public interface LessonSummary {
    Long getLessonId();
    String getTitle();
    String getJlptLevel();
    Integer getDisplayOrder();
}

@Query("SELECT l.lessonId, l.title, l.jlptLevel, l.displayOrder " +
       "FROM Lesson l WHERE l.status = :status AND l.jlptLevel = :level")
List<LessonSummary> findSummaryByStatusAndLevel(String status, String level);
```

### 5.3. @BatchSize cho Collection LAZY

```java
// Khi cần LAZY nhưng tránh N+1 (load theo batch)
@Entity
public class Assessment {
    @OneToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 30) // Hibernate load 30 items/batch thay vì 1
    private List<QuestionAssignment> questionAssignments;
}
```

### 5.4. Đặt @Transactional Đúng Chỗ

```java
// ✅ @Transactional CHỈ ở Service layer (GLOB-ARCH-01)
@Service
@Transactional(readOnly = true) // default read-only cho service
public class LessonService {

    @Transactional // Override: write transaction
    public LessonResponse createLesson(CreateLessonRequest req) { ... }

    // readOnly = true — không cần override
    public LessonResponse getLessonById(Long id) { ... }
}

// ❌ KHÔNG đặt @Transactional tại Controller hay Repository
```

---

## 6. Execution Plan & SSMS Tools

### 6.1. Đọc Execution Plan

Trong SSMS:
1. `Ctrl+M` → bật **Include Actual Execution Plan**
2. Chạy query
3. Tab "Execution Plan" hiện sau kết quả

**Dấu hiệu cần tối ưu**:

| Dấu hiệu | Nghĩa | Hành động |
|----------|-------|-----------|
| **Table Scan** / **Clustered Index Scan** trên bảng lớn | Không có index phù hợp | Thêm index |
| **Key Lookup** với nhiều row | Index chỉ có một phần cột SELECT | Mở rộng index (covering index) |
| **Hash Match** thay vì **Nested Loops** | Join không dùng được index | Kiểm tra FK index |
| **Sort** tốn > 10% cost | ORDER BY không có index | Thêm index bao gồm cột ORDER BY |
| **Missing Index** (gợi ý màu xanh) | SQL Server gợi ý index | Đánh giá và tạo nếu phù hợp |

### 6.2. Query Store (SQL Server)

```sql
-- Tìm top 10 query chậm nhất (chạy trong SSMS với DB JLPT_LearningDB)
SELECT TOP 10
    qsq.query_id,
    qsqt.query_sql_text,
    qsrs.avg_duration      / 1000.0 AS avg_ms,
    qsrs.max_duration      / 1000.0 AS max_ms,
    qsrs.count_executions           AS exec_count
FROM sys.query_store_query         qsq
JOIN sys.query_store_query_text    qsqt ON qsqt.query_text_id = qsq.query_text_id
JOIN sys.query_store_plan          qsp  ON qsp.query_id       = qsq.query_id
JOIN sys.query_store_runtime_stats qsrs ON qsrs.plan_id       = qsp.plan_id
ORDER BY qsrs.avg_duration DESC;
```

### 6.3. Kiểm tra Index Usage

```sql
-- Index nào đang được dùng / không dùng
SELECT
    OBJECT_NAME(i.object_id)       AS table_name,
    i.name                         AS index_name,
    i.type_desc,
    ius.user_seeks,
    ius.user_scans,
    ius.user_lookups,
    ius.user_updates
FROM sys.indexes i
LEFT JOIN sys.dm_db_index_usage_stats ius
       ON ius.object_id = i.object_id
      AND ius.index_id  = i.index_id
      AND ius.database_id = DB_ID()
WHERE OBJECT_NAME(i.object_id) IN (
    'lessons','vocabulary','kanji','grammar_points',
    'questions','test_attempts','flashcards',
    'student_content_progress','auth_tokens'
)
ORDER BY table_name, i.index_id;
```

---

## 7. Checklist Trước Khi Merge

Trước khi merge bất kỳ thay đổi có SQL query:

- [ ] Query không dùng `SELECT *`
- [ ] Không có N+1 (dùng JOIN FETCH hoặc Projection)
- [ ] Mọi cột trong `WHERE`, `JOIN ON`, `ORDER BY` có index
- [ ] Danh sách API có pagination (`OFFSET ... FETCH NEXT`)
- [ ] Subscription check đi qua Redis cache, không query DB trực tiếp mỗi request
- [ ] Không có function bao quanh cột trong `WHERE` (ví dụ: `YEAR()`, `LOWER()`)
- [ ] Query phức tạp đã xem Execution Plan trong SSMS
- [ ] Schema change đi kèm Flyway migration (không DDL thủ công)

---

## 8. Tham Chiếu

| Nguồn | Nội dung liên quan |
|-------|-------------------|
| [`constraints/global.md`](../../01-SRS-Requirements/constraints/global.md) `§GLOB-DB`, `§GLOB-PERF` | Ràng buộc DB và hiệu năng |
| [`database/init.sql`](../../../database/init.sql) | Schema đầy đủ và index hiện có |
| [`CLAUDE.md § ADR-004`](../../../CLAUDE.md) | Soft Delete — không dùng DELETE |
| [`CLAUDE.md § ADR-005`](../../../CLAUDE.md) | DTO Pattern — không trả Entity |
| `AGENTS.md § Forbidden Patterns` | N+1, SELECT *, bypass Security |
| [SQL Server Query Store Docs](https://learn.microsoft.com/sql/relational-databases/performance/monitoring-performance-by-using-the-query-store) | Monitor query performance |
