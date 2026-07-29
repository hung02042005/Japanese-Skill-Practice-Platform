# CodeGraph & Graphify — Lợi Ích Cho Dự Án JLPT E-Learning

> **Mục đích tài liệu**: Giải thích chi tiết công dụng, lợi ích cốt lõi và cách hai công cụ phân tích mã nguồn — **CodeGraph** và **Graphify** — hỗ trợ trực tiếp cho việc phát triển, review, bảo trì và bảo vệ đồ án JLPT E-Learning System.
> **Tài liệu liên quan**: [`codegraph_prompts.md`](./codegraph_prompts.md) (20 prompt mẫu dùng CodeGraph) · [`AGENTS.md`](../../../AGENTS.md) § graphify (quy tắc dùng graphify) · [`CLAUDE.md`](../../../CLAUDE.md) (ADR, Anti-pattern làm nền cho các câu hỏi review) · [`codegraph-token-savings-report.md`](./codegraph-token-savings-report.md) — **số liệu đo thực tế** (không phải ước lượng) cho thấy tiết kiệm token bao nhiêu %, sinh bởi [`tools/codegraph-token-savings.ps1`](../../../tools/codegraph-token-savings.ps1)

---

## 1. Vấn đề cả hai công cụ cùng giải quyết

Dự án này có quy mô không nhỏ: kiến trúc layered (Controller → Service → Repository → Entity), tổ chức theo feature package (`com.jlpt.feature.*`: auth, student.kana, student.reading, flashcard, dictionary, notification, support, quiz/exam, AI OCR/Speech...), cộng thêm frontend React chia theo role (student/staff/admin). Khi làm việc với AI agent (Claude Code) hoặc khi tự mình cần hiểu lại code cũ, có ba vấn đề lặp lại:

1. **Đọc code thô = tốn token / tốn thời gian mà vẫn dễ bỏ sót.** `grep` tìm ra tên hàm nhưng không cho biết ai gọi ai, dữ liệu đi qua bao nhiêu tầng trước khi tới DB.
2. **Repo càng lớn, việc "hiểu toàn cục" càng khó** — đặc biệt khi cần trả lời câu hỏi bảo vệ đồ án kiểu "kiến trúc hệ thống là gì", "module nào phụ thuộc module nào", "có God Class/N+1 Query/Dead Code ở đâu không".
3. **Review sai lệch với luật riêng của dự án.** Review chung chung không biết dự án này có ADR-003 (bcrypt ≥ 10), ADR-004 (soft delete bắt buộc), ADR-005 (DTO pattern), LESSON-003 (role + subscription), LESSON-006 (AI không silent fail) — nên bỏ sót đúng những lỗi quan trọng nhất với dự án.

**CodeGraph** và **Graphify** giải quyết ba vấn đề này theo hai cách bổ sung nhau: một cái *truy vấn theo yêu cầu, trả source thật*; một cái *xây sẵn bản đồ tri thức, truy vấn cực rẻ*.

---

## 2. CodeGraph — "Kính hiển vi" truy vết call-graph theo yêu cầu

### 2.1. Nó là gì trong dự án này

CodeGraph là MCP tool (`codegraph_explore`) build một **index tĩnh** (`.codegraph/`) rồi cho phép hỏi bằng ngôn ngữ tự nhiên hoặc tên symbol để lấy về **source code thật + call path** liên quan — không phải tóm tắt, không phải đoán.

Trong repo này, index nằm ở `docs/06-Management/skills/.codegraph/` (không phải ở root — CodeGraph chỉ dò lên thư mục cha từ `projectPath` được truyền vào, không dò xuống, nên phải trỏ đúng `projectPath` hoặc gọi `codegraph init` lại ở root nếu muốn dùng path gọn — xem chi tiết trong [`codegraph_prompts.md`](./codegraph_prompts.md) mục "Đọc trước khi dùng").

### 2.2. Lợi ích cốt lõi

| Lợi ích | Vì sao quan trọng với dự án này |
|---|---|
| **Trả về source thật + call path**, không phải tóm tắt AI tự bịa | Khi review bcrypt cost, soft delete, DTO mapping — cần thấy đúng dòng code, không thể chấp nhận suy đoán sai gây review "ma" |
| **Truy vết end-to-end một chức năng cụ thể** (frontend component → API → Controller → Service → Repository → Entity → DB) | Kiến trúc layered 4 tầng của dự án là đúng đối tượng mà call-graph mô tả tốt nhất — vd. truy vết luồng login, luồng nộp quiz, luồng OCR async |
| **Impact analysis trước khi sửa code** — biết `AuthService` bị gọi từ đâu, gọi tới đâu, page/component frontend nào bị ảnh hưởng | Đúng yêu cầu "Refactor Safeguard" trong `AGENTS.md` §9.3: đổi >3 file hoặc >200 dòng phải xác nhận trước — CodeGraph cho dữ liệu để xác nhận có căn cứ thay vì đoán |
| **Tìm Dead Code bằng call graph thật**, không phải grep văn bản | Dự án dùng JSX render động — grep không thấy component được gọi qua biến, còn call graph thì thấy |
| **Phát hiện vi phạm kiến trúc**: Controller gọi thẳng Repository (bỏ qua Service), Service cross-module không qua interface, circular dependency giữa feature package | Đây chính là Anti-pattern "God Controller"/"Circular Dependencies" trong `CLAUDE.md` — CodeGraph kiểm tra được bằng dữ liệu quan hệ thực, không chỉ nhìn tên file |
| **Sinh tài liệu SRS/SDD từ chính code đang chạy** | Thay vì viết tài liệu tay rồi lệch với code, dùng CodeGraph liệt kê feature/API/entity thật để tài liệu luôn khớp implementation — hữu ích cho báo cáo đồ án và bảo vệ |
| **Review theo đúng luật riêng của dự án** (bcrypt ≥ 10, soft delete, DTO không lộ Entity, AI không silent fail, quiz lock, role+subscription) | 20 prompt có sẵn trong `codegraph_prompts.md` đã trỏ thẳng vào từng ADR/LESSON — không phải review chung chung, mà review đúng cái dự án này quan tâm |

### 2.3. Khi nào dùng CodeGraph

- Cần **source code thật** để trả lời câu hỏi kiến trúc, không chỉ cần biết "có tồn tại hay không"
- Trước khi sửa một Service/Controller quan trọng → chạy impact analysis (prompt #15) để biết ảnh hưởng
- Trước khi bảo vệ đồ án → chạy prompt #20 ("senior-level review") để có danh sách Top 20 cải tiến kèm severity, file path, giải thích
- Cần sinh SRS/SDD bám sát code thật (prompt #18, #19) thay vì viết tài liệu tay

### 2.4. Giới hạn cần biết

- Có **explore budget** giới hạn (mặc định `maxFiles = 12`/lần gọi) — không hỏi "phân tích toàn bộ kiến trúc" trong 1 câu, phải chia theo module thật của repo (auth, student.kana, flashcard...) và ưu tiên hỏi bằng tên symbol cụ thể.
- Index phải build/cập nhật lại khi code thay đổi nhiều (không tự động realtime như graphify).

---

## 3. Graphify — "Bản đồ tri thức" toàn repo, cập nhật rẻ, tra cứu tức thời

### 3.1. Nó là gì trong dự án này

Graphify là CLI dựng một **knowledge graph** toàn repo tại `apps/graphify-out/` (gồm `graph.json`, `manifest.json`, `cache/`, và tuỳ chọn `GRAPH_REPORT.md` + `wiki/index.md`). Khác với CodeGraph (truy vấn on-demand), Graphify **xây sẵn** cấu trúc phân khu (community detection), liệt kê **god nodes** (các file/class trung tâm, độ kết nối cao — chính là ứng viên vi phạm Anti-pattern "God Class"), và các liên kết bất ngờ giữa các module.

Theo quy tắc đã ghi trong `AGENTS.md` § graphify, khi người dùng gõ `/graphify`, agent phải ưu tiên:

- `graphify query "<câu hỏi>"` — hỏi trực tiếp, trả về **đồ thị con tối ưu**, tiết kiệm token hơn nhiều so với đọc toàn bộ `GRAPH_REPORT.md` hoặc grep thô
- `graphify path "<A>" "<B>"` — tìm đường đi ngắn nhất giữa hai thành phần (vd. từ `QuizController` đến `QuizAttempt` entity)
- `graphify explain "<khái niệm>"` — giải thích một khái niệm/thành phần cụ thể trong kiến trúc
- `graphify update .` — cập nhật đồ thị sau khi sửa code, **chỉ phân tích AST cục bộ, hoàn toàn miễn phí** (không gọi AI, không tốn token)
- `graphify diagnostics` — chẩn đoán khi nghi ngờ đồ thị thiếu liên kết hoặc sụp edge

### 3.2. Lợi ích cốt lõi

| Lợi ích | Vì sao quan trọng với dự án này |
|---|---|
| **Tra cứu gần như tức thời, cực rẻ token** — trả đồ thị con tối ưu thay vì buộc AI đọc lại toàn bộ file nguồn | Repo ~600+ file; hỏi lại kiến trúc nhiều lần trong một phiên làm việc (hoặc qua nhiều phiên) sẽ không phải "học lại từ đầu" mỗi lần |
| **God Nodes** được liệt kê sẵn trong `GRAPH_REPORT.md` | Phát hiện trực tiếp vi phạm nguyên tắc "Max 300 lines/file" và "God Class" trong `CLAUDE.md` mà không cần hỏi AI phân tích lại — đồ thị đã tính độ kết nối sẵn |
| **Community detection (cấu trúc phân khu)** cho thấy các file thực sự "đi cùng nhau" theo hành vi, không chỉ theo thư mục | Giúp kiểm tra xem việc tổ chức `com.jlpt.feature.*` có đúng như thiết kế hay đã bị rò rỉ phụ thuộc chéo module (liên quan Anti-pattern "Circular Dependencies") |
| **Surprising Connections** (liên kết bất ngờ) | Cảnh báo sớm coupling không mong muốn — vd. module `flashcard` vô tình phụ thuộc trực tiếp vào `notification` thay vì qua interface chung |
| **Cập nhật gia tăng, miễn phí, chỉ AST cục bộ** (`graphify update .`) | Không tốn chi phí AI mỗi lần sửa code nhỏ — phù hợp chạy thường xuyên trong vòng lặp dev, khác hẳn CodeGraph vốn tốn "budget" mỗi lần explore |
| **`wiki/index.md`** (nếu có) cho điều hướng tổng quan theo phân khu kiến trúc | Thay thế việc phải duyệt từng file nguồn để hiểu tổng thể — đặc biệt hữu ích khi có thành viên mới join hoặc khi cần ôn lại kiến trúc trước bảo vệ đồ án |
| **`graphify path`** tìm đường đi ngắn nhất giữa 2 thành phần | Trả lời nhanh câu hỏi kiểu "từ trang React nào thì tới được bảng `quiz_attempts`?" mà không cần lần theo từng file thủ công |

### 3.3. Khi nào dùng Graphify

- Câu hỏi có thể trả lời bằng **cấu trúc/quan hệ** (không cần xem toàn bộ source thật) → dùng `graphify query`/`path`/`explain` trước, rẻ hơn nhiều so với đọc `GRAPH_REPORT.md` hay grep
- Sau **mỗi lần sửa code** → chạy `graphify update .` để đồ thị không bị lệch thực tế (miễn phí, nên làm thường xuyên)
- Cần đánh giá kiến trúc tổng thể nhanh (God Nodes, community, liên kết bất ngờ) → đọc `GRAPH_REPORT.md`
- Nghi ngờ đồ thị sai/thiếu → `graphify diagnostics`

### 3.4. Giới hạn cần biết

- Đồ thị là ảnh chụp cấu trúc (structure), **không thay thế được việc đọc source thật** khi cần review chi tiết logic nghiệp vụ (bcrypt cost, DTO field nào bị lộ...) — lúc đó vẫn cần CodeGraph hoặc đọc file trực tiếp.
- Output tại `apps/graphify-out/` được git-ignore (`.gitignore` dòng 30-31) — là artifact sinh ra cục bộ, không phải nguồn sự thật để commit.

---

## 4. So sánh nhanh — chọn công cụ nào?

| Tiêu chí | CodeGraph | Graphify |
|---|---|---|
| Cách hoạt động | Truy vấn theo yêu cầu (on-demand explore) | Đồ thị tri thức xây sẵn, truy vấn tức thời |
| Kết quả trả về | **Source code thật** + call path | **Đồ thị con** (quan hệ, cấu trúc), không phải source đầy đủ |
| Chi phí mỗi lần dùng | Có "explore budget" giới hạn theo quy mô repo | Rất rẻ — `query`/`path`/`explain` trả đồ thị con tối ưu |
| Cập nhật khi code đổi | Cần re-index/gọi lại explore | `graphify update .` — chỉ AST cục bộ, **miễn phí** |
| Điểm mạnh nhất | Review sâu theo đúng ADR/LESSON của dự án; impact analysis; sinh SRS/SDD bám code thật | Bức tranh toàn cục nhanh (God Nodes, community, liên kết bất ngờ); tra cứu lặp lại rẻ |
| Dùng tốt nhất khi | Cần bằng chứng là source thật (review bảo mật, viết tài liệu, trước khi refactor) | Cần định hướng nhanh trong repo lớn, hoặc hỏi lặp đi lặp lại về cấu trúc |

**Quy tắc phối hợp thực tế**: dùng **Graphify trước** để định vị nhanh (module nào, file nào là trung tâm, đường đi giữa 2 thành phần) — sau đó dùng **CodeGraph** để đào sâu vào đúng file/module đã định vị, lấy source thật và làm review/impact analysis chi tiết theo luật riêng của dự án (`codegraph_prompts.md`).

---

## 5. Lợi ích tổng hợp cho vòng đời dự án

```
┌─────────────────────────────────────────────────────────────────┐
│  Onboarding / ôn kiến trúc trước bảo vệ đồ án                    │
│   → graphify (wiki/index.md, GRAPH_REPORT.md) cho bức tranh nhanh│
├─────────────────────────────────────────────────────────────────┤
│  Trong lúc code (mỗi lần sửa xong)                                │
│   → graphify update .  (miễn phí, giữ đồ thị luôn đúng)          │
├─────────────────────────────────────────────────────────────────┤
│  Trước khi refactor / sửa Service quan trọng                     │
│   → CodeGraph impact analysis (callers/callees/frontend liên quan)│
├─────────────────────────────────────────────────────────────────┤
│  Review bảo mật & kiến trúc theo đúng ADR/LESSON của dự án        │
│   → CodeGraph + 20 prompt trong codegraph_prompts.md              │
├─────────────────────────────────────────────────────────────────┤
│  Sinh tài liệu SRS/SDD/checklist deploy bám sát code thật         │
│   → CodeGraph (prompt #17, #18, #19)                              │
└─────────────────────────────────────────────────────────────────┘
```

Nói ngắn gọn: **Graphify giúp không lạc trong rừng code** (định hướng nhanh, rẻ, cập nhật liên tục), còn **CodeGraph giúp không đoán mò khi cần bằng chứng thật** (source code, call path, review đúng luật riêng của dự án JLPT E-Learning). Dùng cùng nhau giảm đáng kể thời gian "tìm hiểu lại code cũ" và giảm rủi ro review/sửa sai lệch với kiến trúc đã thiết kế.
