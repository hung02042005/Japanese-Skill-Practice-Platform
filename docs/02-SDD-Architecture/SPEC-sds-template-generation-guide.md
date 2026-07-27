# SPEC — Hướng dẫn Generate SDS Document (Template3) dạng Markdown

> **Nguồn**: `Temp_Document/Template3_SDS Document.docx` (FPT University — Software Design Specification template)
> **Ví dụ đã generate theo spec này**: [`SDS-Japanese-Skill-Practice-Platform.md`](SDS-Japanese-Skill-Practice-Platform.md)
> **Đối tượng dùng**: Dev hoặc AI Agent cần tạo SDS cho 1 feature/module mới của dự án này.

---

## 1. Mục đích

Template3 gốc (.docx) là file Word có các đoạn `[Provide the ... ]` và `<placeholder>` — dùng để điền tay trên Word. Spec này quy định cách **generate ra file `.md` đã điền sẵn nội dung thật** của dự án (không phải giữ nguyên placeholder), để:

- AI Agent/Dev có thể tạo SDS cho feature mới mà không cần đọc lại file .docx mỗi lần.
- Nội dung SDS bám sát code thật (class/method/SQL lấy từ source, không bịa).
- Format đồng nhất giữa các SDS trong `docs/02-SDD-Architecture/`.

---

## 2. Cấu trúc gốc của Template3 (.docx)

| # | Mục trong .docx | Nội dung gốc (placeholder) |
|---|---|---|
| — | Cover page | `<<PROJECT NAME>>` + "Software Design Specification" |
| — | RECORD OF CHANGES | Bảng Date / A-M-D / In charge / Change Description |
| — | Table of Contents | Tự động theo heading |
| I | Overview → 1. Code Packages | Package diagram + bảng mô tả từng package |
| I | Overview → 2. Database Design → a. Database Schema | ERD (hình ảnh) |
| I | Overview → 2. Database Design → b. Table Description | Bảng No / Table / Description |
| II | Code Designs → `<Feature/Function NameN>` → a. Class Diagram | Hình class diagram |
| II | Code Designs → ... → b. Class Specifications | Bảng No / Method / Description cho từng class |
| II | Code Designs → ... → c. Sequence Diagram(s) | Hình sequence diagram |
| II | Code Designs → ... → d. Database Queries | SQL thực tế dùng trong feature |
| II | Code Designs → lặp lại mục `2. <Feature/Function Name2>`, `3. ...` cho từng feature |

Đây là bộ khung **bắt buộc phải giữ nguyên thứ tự heading** khi generate `.md`. Phần khác biệt là: mỗi ô placeholder phải được thay bằng dữ liệu thật lấy từ repo.

---

## 3. Ánh xạ Docx → Markdown → Nguồn dữ liệu trong repo

| Mục docx | Heading .md tương ứng | Lấy dữ liệu thật ở đâu trong repo |
|---|---|---|
| Cover page | `# <Tên dự án>` + `## Software Design Specification` | Tên dự án lấy từ `CLAUDE.md` header hoặc `package.json` |
| RECORD OF CHANGES | `## RECORD OF CHANGES` | 1 dòng cho lần tạo/sửa hiện tại: ngày (`currentDate`), người/agent thực hiện, mô tả thay đổi |
| I.1 Code Packages | `### 1. Code Packages` | Liệt kê thư mục con của `apps/backend/src/main/java/com/jlpt/feature/*` (mỗi thư mục = 1 package). Mô tả package: đọc nhanh các class chính trong thư mục đó (`find <package> -maxdepth 1 -name "*.java"`) |
| I.2.a Database Schema | `#### a. Database Schema` | Không vẽ lại từ đầu — tham chiếu [`database-design/JLPT_database.md`](database-design/JLPT_database.md); chỉ vẽ **mermaid ERD rút gọn** cho đúng nhóm bảng liên quan tới feature đang viết |
| I.2.b Table Description | `#### b. Table Description` | Copy mô tả bảng liên quan từ `JLPT_database.md` (mục "3. Mô Tả Các Bảng Chính"), không chép lại toàn bộ 23 bảng |
| II.N `<Feature Name>` | `### N. <Tên feature bằng tiếng Việt/Anh, khớp tên package>` | Chọn 1 feature/luồng nghiệp vụ cụ thể (vd: "Quiz Submission", "Đăng nhập"), không viết chung chung |
| II.N.a Class Diagram | `#### a. Class Diagram` | Đọc source thật của feature (`Controller`, `Service`, `Repository`, `Entity` liên quan) rồi vẽ bằng **Mermaid `classDiagram`** — method/field trong diagram phải khớp tên method thật trong code (dùng `grep -n "public \|private "` để lấy chữ ký) |
| II.N.b Class Specifications | `#### b. Class Specifications` | Bảng No/Method/Description cho **service chính** của feature. Description phải nêu: input, business rule áp dụng (đối chiếu `CLAUDE.md § LESSONS LEARNED`/`ADR`), output |
| II.N.c Sequence Diagram | `#### c. Sequence Diagram(s)` | Vẽ bằng **Mermaid `sequenceDiagram`**, dựng từ luồng gọi thật trong code (Controller → Service → Repository → DB), có branch `alt/else` cho các case lỗi (403/409/401...) nếu code có throw exception tương ứng |
| II.N.d Database Queries | `#### d. Database queries` | Query SQL rút ra từ JPQL/`@Query`/logic Repository thật của feature (không sinh SQL tưởng tượng nếu ORM tự sinh — ghi rõ tên bảng/cột theo `JLPT_database.md`) |

---

## 4. Quy ước đặt tên & vị trí file

- **Vị trí**: `docs/02-SDD-Architecture/SDS-<phạm-vi>.md`
  - SDS toàn dự án (nhiều feature): `SDS-Japanese-Skill-Practice-Platform.md`
  - SDS riêng 1 feature lớn (nếu tách file để đỡ dài): `SDS-<feature-name-kebab-case>.md`, ví dụ `SDS-flashcard-srs.md`
- **Không tạo file `.docx`/`.pdf`** trừ khi user yêu cầu xuất bản chính thức — mặc định chỉ tạo `.md` (dễ diff, dễ review trong PR).
- Đầu file luôn có dòng credit nguồn template + link tới `JLPT_database.md`/`SoDoDuAn.md` như 2 doc nền tảng đã có.

---

## 5. Quy trình từng bước để generate 1 SDS.md mới

1. **Xác định phạm vi**: 1 feature cụ thể hay toàn hệ thống? Tên feature phải khớp tên package thật trong `apps/backend/.../feature/`.
2. **Khảo sát code thật** của feature đó:
   - `find apps/backend/.../feature/<name> -maxdepth 1 -name "*.java"` → liệt kê class.
   - `grep -n "public \|@Transactional"` trên Controller + Service chính → lấy method signature thật.
   - Đọc method quan trọng (business logic phức tạp, có lock/validate) để hiểu luồng trước khi vẽ sequence diagram.
3. **Đối chiếu quy tắc nghiệp vụ** trong `CLAUDE.md` (ADR/LESSON/Anti-pattern) liên quan đến feature — nêu trong phần "Class Specifications" nếu có áp dụng (vd: soft delete, lock khi có attempt, AI async + fallback).
4. **Viết Overview** (Code Packages + Database Design) — chỉ phần liên quan, không lặp lại toàn bộ nếu đã có SDS tổng ở file khác (dùng link tham chiếu thay vì copy).
5. **Viết Code Designs cho từng feature** theo đúng thứ tự a→b→c→d ở Mục 3.
6. **Không để lại placeholder dạng `<...>` hay `[Provide ...]`** — nếu thiếu thông tin thật, phải đọc thêm code, không bịa nội dung.
7. **Review lại**: mỗi class/method/tên bảng nêu trong file phải grep ra được trong repo hiện tại (tên có thể đã đổi qua các lần refactor).

---

## 6. Checklist trước khi coi là hoàn thành

- [ ] Có `RECORD OF CHANGES` với ít nhất 1 dòng cho lần tạo file này.
- [ ] Mục lục (Table of Contents) khớp với heading thật trong file.
- [ ] Code Packages liệt kê đúng thư mục package thật (`find .../feature -maxdepth 1 -type d`).
- [ ] Database Schema có mermaid ERD + link sang `JLPT_database.md`, không tự vẽ bảng không tồn tại.
- [ ] Mỗi feature trong "Code Designs" có đủ 4 mục con: Class Diagram, Class Specifications, Sequence Diagram, Database Queries.
- [ ] Method/class tên trong diagram khớp tên thật trong source (không viết tắt/đổi tên tùy ý).
- [ ] Không còn placeholder `<...>`, `[Provide ...]`, `XYZ Class`, `ABC Class` sót lại từ template gốc.
- [ ] File đặt đúng thư mục `docs/02-SDD-Architecture/`, đặt tên theo Mục 4.

---

## 7. Ví dụ tham chiếu

File [`SDS-Japanese-Skill-Practice-Platform.md`](SDS-Japanese-Skill-Practice-Platform.md) đã áp dụng đầy đủ spec này cho 2 luồng: **Authentication & Login** và **Quiz Submission (Assessment)**. Dùng file đó làm mẫu tham chiếu format khi viết thêm SDS cho các feature còn lại (Flashcard SRS, OCR Kanji, Speech Shadowing, Content Review...).
