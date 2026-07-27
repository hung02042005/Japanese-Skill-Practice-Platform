# TEST FAILURE LOG — Đợt bổ sung Unit Test Backend

> **Mục đích**: Ghi lại MỌI lần test fail trong quá trình viết bổ sung unit test, kèm nguyên nhân
> gốc và cách sửa. Đây là tài liệu tra cứu khi gặp lại lỗi tương tự — không phải changelog.
>
> **Phạm vi**: `apps/backend` — đợt nâng coverage 65.9% → 83.3% (2026-07-26).
> **Cách chạy**: `mvn -o test -Dtest='<TênTest>' -Djacoco.skip=true -Dspotless.check.skip=true`

---

## Tổng quan

| # | File test | Loại lỗi | Số test fail | Trạng thái |
|---|-----------|----------|--------------|------------|
| F-01 | `EmailServiceTemplateTest` | Assertion | 12 | ✅ Đã sửa |
| F-02 | `DictionaryServiceCoverageTest` | Assertion | 1 | ✅ Đã sửa |
| F-03 | `StaffQuizSubmitReviewControllerTest` | Compile | — | ✅ Đã sửa |
| F-04 | `NotebookServiceCoverageTest` | Compile | 13 lỗi biên dịch | ✅ Đã sửa |
| F-05 | `StaffExamServiceCoverageTest` | Compile | 5 lỗi biên dịch | ✅ Đã sửa |
| F-06 | `StaffExamServiceCoverageTest` | `UnfinishedStubbing` | 13 | ✅ Đã sửa |
| E-01 | (mọi test) | Sai cú pháp lệnh Maven | — | ✅ Đã sửa |
| E-02 | (mọi test) | Sai thư mục chạy Maven | — | ✅ Đã sửa |
| E-03 | (đo coverage) | `cd` short-circuit → đọc báo cáo cũ | — | ✅ Đã sửa |

---

## F-01 — `EmailServiceTemplateTest`: 12/15 test fail

### Log lỗi

```
[ERROR] Tests run: 15, Failures: 12, Errors: 0, Skipped: 0 -- in com.jlpt.shared.email.EmailServiceTemplateTest
[ERROR] EmailServiceTemplateTest.sendVerificationEmail_noDbTemplate_usesDefaultSubjectAndContent:82
        expected: <true> but was: <false>
[ERROR] EmailServiceTemplateTest.renderContent_htmlInAdminTemplate_isEscapedNotInjected:174
        expected: <true> but was: <false>
[ERROR] EmailServiceTemplateTest.sendPasswordResetEmail_noDbTemplate_buildsResetLinkFromFrontendUrl:103
        expected: <true> but was: <false>
... (toàn bộ 12 test có assert trên nội dung HTML đều fail; các assert trên subject thì PASS)
```

### Nguyên nhân gốc

`EmailService.sendOnce()` dựng message bằng:

```java
MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//                                                        ^^^^ multipart = true
```

Khi `multipart = true`, `MimeMessage.getContent()` **không trả về chuỗi HTML** mà trả về một
`MimeMultipart`. Test gọi `mimeMessage.getContent().toString()` → nhận được chuỗi kiểu
`jakarta.mail.internet.MimeMultipart@1a2b3c` (object identity), nên mọi `assertTrue(html.contains(...))`
đều false. Đây là **lỗi của test, không phải lỗi của code sản phẩm**.

Manh mối phân biệt: assert trên `getSubject()` vẫn pass — chứng tỏ email được dựng đúng, chỉ có
cách đọc phần body là sai.

### Cách sửa

Duyệt đệ quy multipart để lấy phần text:

```java
private String extractText(Part part) throws Exception {
    Object content = part.getContent();
    if (content instanceof String text) {
        return text;
    }
    if (content instanceof Multipart multipart) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < multipart.getCount(); i++) {
            sb.append(extractText(multipart.getBodyPart(i)));
        }
        return sb.toString();
    }
    return "";
}
```

Cần thêm import `jakarta.mail.Multipart` và `jakarta.mail.Part`.

### Bài học

Khi test email dựng qua `MimeMessageHelper(message, true, ...)`, **luôn** phải giải nén multipart.
`getContent().toString()` chỉ hoạt động khi `multipart = false`.

---

## F-02 — `DictionaryServiceCoverageTest`: 1/18 test fail

### Log lỗi

```
[ERROR] Tests run: 18, Failures: 1, Errors: 0, Skipped: 0 -- in com.jlpt.feature.dictionary.DictionaryServiceCoverageTest
[ERROR] DictionaryServiceCoverageTest.search_entitiesWithoutLevelOrTopic_mapToNulls:197
        expected: <null> but was: <LESSON>
```

### Nguyên nhân gốc

Test muốn phủ nhánh `l.getLessonType() != null ? ... : null` trong `DictionaryService.toLessonItem()`,
nên tạo entity "trống":

```java
Lesson bareLesson = Lesson.builder().id(4L).title("z").build();
```

Nhưng entity `Lesson` khai báo giá trị mặc định cho `lessonType` (`LessonType.LESSON`) và Lombok
`@Builder.Default` giữ nguyên mặc định đó khi builder không set. Kết quả: `getLessonType()` trả về
`LESSON` chứ không phải `null` → nhánh null không bao giờ chạy.

### Cách sửa

Set null tường minh sau khi build:

```java
Lesson bareLesson = Lesson.builder().id(4L).title("z").build();
bareLesson.setLessonType(null);
```

### Bài học

Entity trong dự án này có nhiều field mang giá trị mặc định (`status`, `lessonType`, `loginAttempts`,
`currentStreak`, `isEditable`...). **Không giả định `builder()` bỏ trống = null.** Muốn phủ nhánh
null-check thì phải `setXxx(null)` tường minh. Ngược lại, có field KHÔNG có `@Builder.Default`
(vd `StudentUser.status`) thì builder lại để null thật — phải set giá trị tường minh, nếu không
`getStatus().getValue()` sẽ ném NPE. Kiểm tra từng field trước khi viết fixture.

---

## F-03 — `StaffQuizSubmitReviewControllerTest`: lỗi biên dịch

### Log lỗi

```
The constructor SpeakingLessonMutationResponse(Long, String) is not visible
```

### Nguyên nhân gốc

Test dựng DTO bằng constructor:

```java
new SpeakingLessonMutationResponse(8L, "pending_review")
```

Nhưng DTO này khai báo `@Builder` + `@Getter` trên class có field `final`, không có
`@AllArgsConstructor` public — Lombok sinh constructor **package-private** để builder dùng nội bộ.

### Cách sửa

```java
SpeakingLessonMutationResponse.builder()
        .lessonId(8L)
        .status("pending_review")
        .build()
```

### Bài học

DTO trong dự án dùng nhiều kiểu Lombok khác nhau: `@Data` (có no-arg constructor + setter),
`@Builder` (chỉ builder), `record` (canonical constructor). Kiểm tra annotation trước khi khởi tạo.

---

## E-01 — Sai cú pháp chọn nhiều test trong Maven

### Log lỗi

```
[ERROR] Failed to execute goal ...:test (default-test) on project jlpt-backend:
No tests matching pattern "EmailServiceTemplateTest+AdminSettingsServiceSmtpTest" were executed!
```

### Nguyên nhân & cách sửa

Surefire ngăn cách nhiều test bằng **dấu phẩy**, không phải dấu `+`:

```bash
# SAI
mvn -o test -Dtest='TestA+TestB'
# ĐÚNG
mvn -o test -Dtest='TestA,TestB'
```

---

## E-02 — Chạy Maven sai thư mục

### Log lỗi

```
[ERROR] The goal you specified requires a project to execute but there is no POM in this
directory (D:\Japanese-Skill-Practice-Platform).
```

### Nguyên nhân & cách sửa

`pom.xml` nằm ở `apps/backend`, không phải gốc repo. Luôn `cd apps/backend` trước khi chạy `mvn`.
Lỗi này hay tái diễn vì thư mục làm việc của shell có thể bị reset giữa các lệnh.

---

## F-04 — `NotebookServiceCoverageTest`: 13 lỗi biên dịch

### Log lỗi

```
[ERROR] NotebookServiceCoverageTest.java:[415,57] incompatible types: invalid constructor reference
[ERROR] NotebookServiceCoverageTest.java:[416,41] incompatible types: java.lang.String cannot be
        converted to java.util.List<com.jlpt.feature.flashcard.dto.ReviewDeckAddRequest.Item>
[ERROR] NotebookServiceCoverageTest.java:[432,33] cannot find symbol
          symbol: method added()
[ERROR] NotebookServiceCoverageTest.java:[433,33] cannot find symbol
          symbol: method skipped()
... (lặp lại cho 6 chỗ dùng added()/skipped())
```

### Nguyên nhân gốc

Đoán sai chữ ký của 2 record — đây là lỗi **tự suy diễn tên/thứ tự field mà không đọc file gốc**:

| Đã viết trong test (SAI) | Thực tế trong code |
|---|---|
| `new ReviewDeckAddRequest(reason, items)` | `ReviewDeckAddRequest(List<Item> items, String reason)` — **items đứng TRƯỚC** |
| `ReviewDeckAddRequest.Item::new` (1 tham số) | `Item(String contentType, Long contentId)` — **2 tham số** |
| `response.added()` / `response.skipped()` | `addedCount()` / `skippedCount()` |

### Cách sửa

```java
private ReviewDeckAddRequest request(String reason, Long... contentIds) {
    List<ReviewDeckAddRequest.Item> items = java.util.Arrays.stream(contentIds)
            .map(id -> new ReviewDeckAddRequest.Item("VOCABULARY", id))
            .toList();
    return new ReviewDeckAddRequest(items, reason);
}
```

Và đổi toàn bộ accessor:

```bash
sed -i 's/\.added()/.addedCount()/g; s/\.skipped()/.skippedCount()/g' <file test>
```

### Bài học

Với `record`, **luôn `cat` file DTO trước khi viết test**. Record không có builder nên thứ tự tham số
là bắt buộc, và trình biên dịch chỉ báo "invalid constructor reference" khá mơ hồ. Ngược lại DTO
dùng `@Builder` thì thứ tự không quan trọng — đó là lý do các test trước không dính lỗi này.

---

## F-05 — `StaffExamServiceCoverageTest`: 5 lỗi biên dịch

### Log lỗi

```
[ERROR] StaffExamServiceCoverageTest.java:[146,37] cannot find symbol
          symbol: method builder()
[ERROR] StaffExamServiceCoverageTest.java:[275,29] cannot find symbol
          symbol: method getScoreMatched()
... (getScoreMatched lặp ở 4 chỗ)
```

### Nguyên nhân gốc

Hai giả định sai khác nhau:

1. **`ExamQuestionRefEntity` chỉ khai báo `@Getter`** — không có `@Builder`, không `@Setter`, không
   `@AllArgsConstructor`. Đây là entity read-only ánh xạ bảng `questions` chỉ để tra cứu, khác hẳn
   các entity khác trong dự án vốn đều có `@Builder`.
2. **`scoreMatched` là `boolean` nguyên thuỷ**, không phải `Boolean`. Lombok sinh accessor
   `isScoreMatched()` chứ không phải `getScoreMatched()`.

### Cách sửa

Với (2) — đổi toàn bộ accessor:

```bash
sed -i 's/\.getScoreMatched()/.isScoreMatched()/g' <file test>
```

Với (1) — xem F-06, vì cách sửa đầu tiên lại đẻ ra lỗi khác.

### Bài học

`boolean` (nguyên thuỷ) → `isXxx()`; `Boolean` (wrapper) → `getXxx()`. Trong cùng dự án có cả hai
kiểu: `ExamDetailResponse.scoreMatched` là `boolean` (dùng `is`), còn
`QuestionResponse.isLocked` lại là `Boolean` (dùng `getIsLocked()`). Phải xem khai báo field.

---

## F-06 — `StaffExamServiceCoverageTest`: 13 test lỗi `UnfinishedStubbing`

### Log lỗi

```
[ERROR] Tests run: 65, Failures: 0, Errors: 13
[ERROR] StaffExamServiceCoverageTest.assignQuestions_validPayload_replacesAndReportsScoreMatch
        :557->questionRef:148 » UnfinishedStubbing
[ERROR] StaffExamServiceCoverageTest.assignQuestions_deletedQuestion_isTreatedAsNotFound
        :557->questionRef:148 » UnfinishedStubbing
... (13 test, tất cả đều trỏ về cùng dòng questionRef:148)
```

### Nguyên nhân gốc

Sau F-05, `ExamQuestionRefEntity` không dựng được bằng builder nên chuyển sang mock:

```java
private ExamQuestionRefEntity questionRef(Long id, String status, String level) {
    ExamQuestionRefEntity ref = mock(ExamQuestionRefEntity.class);
    lenient().when(ref.getId()).thenReturn(id);   // ← nổ ở đây
    ...
}
```

Nhưng helper này được gọi **lồng bên trong** một lời gọi stubbing khác:

```java
when(questionRefRepository.findById(100L)).thenReturn(Optional.of(questionRef(100L, "published", "N5")));
//   ^ Mockito đã mở một phiên stubbing...        ^ ...và questionRef() lại mở phiên stubbing MỚI bên trong
```

Mockito giữ trạng thái stubbing toàn cục theo thread. Khi `when(A)` chưa kết thúc mà đã gọi `when(B)`,
nó báo `UnfinishedStubbing`. Tất cả 13 test dùng helper này đều chết cùng một lý do.

### Cách sửa

Không mock nữa — dựng instance thật và set field bằng reflection, giữ nguyên mọi call-site:

```java
private ExamQuestionRefEntity questionRef(Long id, String status, String level) {
    ExamQuestionRefEntity ref = new ExamQuestionRefEntity();
    ReflectionTestUtils.setField(ref, "id", id);
    ReflectionTestUtils.setField(ref, "questionText", "Câu hỏi " + id);
    ReflectionTestUtils.setField(ref, "status", status);
    ReflectionTestUtils.setField(ref, "jlptLevel", level);
    return ref;
}
```

(Cách khác: gán ra biến cục bộ trước rồi mới `when(...)`, nhưng phải sửa cả 13 chỗ gọi.)

### Bài học

**Không bao giờ tạo + stub mock bên trong biểu thức `thenReturn(...)`.** Với entity không có
builder/setter, `ReflectionTestUtils.setField` là lựa chọn an toàn hơn mock: không dính trạng thái
stubbing, không cần `lenient()`, và object hành xử đúng như thật.

---

## E-03 — `cd` thất bại làm lệnh Maven bị bỏ qua, đọc nhầm báo cáo cũ

### Triệu chứng

Lệnh đo coverage:

```bash
cd apps/backend && mvn -o test jacoco:report ... ; python3 <đọc jacoco.csv>
```

In ra số liệu **y hệt lần đo trước**, dù vừa thêm cả một file test mới. Kèm dòng dễ bỏ qua:

```
/usr/bin/bash: line 15: cd: apps/backend: No such file or directory
```

### Nguyên nhân gốc

Thư mục làm việc của shell **đã ở sẵn** `apps/backend` từ lệnh trước, nên `cd apps/backend`
(đường dẫn tương đối) thất bại → toán tử `&&` short-circuit → **`mvn` không hề chạy**. Nhưng phần sau
ngăn bởi `;` vẫn chạy, đọc file `jacoco.csv` **cũ** còn sót lại từ lần build trước và in ra số liệu
lỗi thời. Kết quả: tưởng là test mới không làm tăng coverage.

### Cách sửa

Dùng đường dẫn tuyệt đối và kiểm tra thư mục trước khi kết luận:

```bash
pwd    # xác nhận trước
mvn -o test jacoco:report ...
```

### Bài học

Đây là loại lỗi **nguy hiểm nhất** trong cả đợt: không có test nào fail, không có exception, chỉ có
số liệu sai dẫn tới kết luận sai. Khi một chỉ số "không thay đổi như mong đợi", việc đầu tiên phải
kiểm tra là lệnh có thực sự chạy không — đừng vội sửa test.

---

## Phát hiện về code sản phẩm (không phải lỗi test)

Ghi lại để cân nhắc xử lý riêng — **chưa sửa** vì nằm ngoài phạm vi đợt viết test.

### P-01: `KanaServiceImpl.determineRow()` có 2 điều kiện chết (dead condition)

```java
if (r.startsWith("s") || r.startsWith("sh")) return "sa-row";
//                       ^^^^^^^^^^^^^^^^^^ không bao giờ chạy: "sh" luôn startsWith "s"
if (r.startsWith("t") || r.startsWith("ch") || r.startsWith("ts")) return "ta-row";
//                                             ^^^^^^^^^^^^^^^^^^ không bao giờ chạy
```

Hệ quả: **branch coverage của method này không thể đạt 100%** dù test đủ mọi input hợp lệ. Muốn phủ
hết phải xoá 2 vế thừa (`startsWith("ch")` thì vẫn cần giữ vì "ch" không bắt đầu bằng "t").

**Đã kiểm chứng bằng số đo**: sau khi `KanaServiceImplTest` phủ đủ 28 romaji đại diện cho mọi hàng
(gồm cả "shi", "tsu", "chi"), JaCoCo báo `KanaServiceImpl: branch 48/50` — **đúng 2 nhánh còn thiếu
chính là 2 điều kiện chết này**. Đây là bằng chứng cụ thể cho thấy chỉ tiêu "100% branch" là bất khả
thi nếu không sửa code sản phẩm.

### P-02: `KanaServiceImpl.determineRow()` so khớp hàng "a" bằng `contains` thay vì so bằng

```java
if ("a,i,u,e,o".contains(r)) return "a-row";
```

Chuỗi `","` hay `"i,u"` cũng thoả điều kiện. Với dữ liệu kana thật thì không xảy ra, nhưng đây là
so khớp lỏng ngoài chủ ý — nên đổi sang `Set.of("a","i","u","e","o").contains(r)`.

### P-03: `StaffQuestionServiceImpl.updateQuestion()` trim không nhất quán

`questionText` được `.trim()`, nhưng `skill`, `jlptLevel`, `questionType` thì gán thẳng không trim,
trong khi các field đi qua `applyUpdate()` lại có trim. Test hiện đang khẳng định đúng hành vi thật
(`assertEquals("  grammar  ", response.getSkill())`) — nếu sau này sửa code cho nhất quán thì phải
cập nhật assertion này.

---

## Cảnh báo KHÔNG phải lỗi thật (bỏ qua an toàn)

### IDE báo `cannot find symbol: method builder()`

```
message: "cannot find symbol\n  symbol: method builder()\n  location: class SystemSetting"
Can't initialize javac processor due to (most likely) a class loader problem:
java.lang.NoClassDefFoundError: Could not initialize class lombok.javac.Javac
```

**Đây là lỗi của Java LSP trong IDE (NetBeans-based), không phải lỗi code.** Annotation processor
của Lombok không khởi tạo được trong tiến trình LSP nên mọi method Lombok sinh ra đều bị báo thiếu.
`mvn test` biên dịch và chạy hoàn toàn bình thường. **Chỉ tin kết quả từ Maven.**

### Log ERROR/WARN xuất hiện khi test PASS

```
ERROR com.jlpt.shared.security.JwtProvider : Invalid JWT token: Malformed protected header JSON...
WARN  c.j.s.exception.GlobalExceptionHandler : Business Exception: TOO_MANY_REQUESTS
```

Đây là log **có chủ đích** của các test đang kiểm tra nhánh lỗi (token hỏng, vượt rate limit).
Test vẫn xanh. Đừng nhầm log ứng dụng với test failure — chỉ đọc dòng `Tests run: ... Failures: ...`.

### Tiếng Việt hiển thị sai trong log console

```
Business Exception: TOO_MANY_REQUESTS - Qu� nhi?u y�u c?u
```

Console Windows dùng codepage không phải UTF-8. Nội dung trong DB/response vẫn đúng; assertion so
sánh chuỗi tiếng Việt vẫn pass. Không cần sửa.

---

## Ghi chú vận hành

- Mockito mặc định **STRICT_STUBS**: stub không dùng đến sẽ làm test fail với
  `UnnecessaryStubbingException`. Nếu class test có nhiều nhánh không dùng chung stub, tách stub vào
  từng test (vd `stubActingAdmin()`) hoặc dùng `@MockitoSettings(strictness = Strictness.LENIENT)`.
- `EmailServiceTest.sendNotificationEmail_AllRetriesFail_SavesToOutbox` chạy ~4s vì
  `EmailService.sendHtmlEmail()` `Thread.sleep(2000)` giữa 3 lần retry. Đây là hành vi thật của
  code, không phải test treo.
