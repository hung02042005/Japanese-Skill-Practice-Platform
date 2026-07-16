# Bản đồ nguồn nội dung

Mỗi section của template đều ứng với một nguồn thật, đã có sẵn trong repo. Lấy nội dung từ
những nguồn này — không bao giờ bịa ra actor, use case, table, ngày tháng nào không có căn
cứ. Nếu một section không có nguồn thật nào, để lại placeholder `TODO:` (xem SKILL.md bước
4) thay vì bịa nội dung.

**Quan trọng — backend tổ chức theo feature, không phải theo layer.** Phần "File
Structure" trong `CLAUDE.md` (mô tả `controller/`, `service/`, `repository/`... là các
package cấp cao nhất) đã lỗi thời. Cấu trúc thật, hiện tại (đã kiểm chứng bằng cách liệt
kê cây thư mục) là:

```
apps/backend/src/main/java/com/jlpt/
├── feature/
│   ├── admin/            (dto/)
│   ├── assessment/       (dto/)
│   ├── auth/              (dto/, event/)
│   ├── contentreview/    (dto/, exception/, handler/, repository/)
│   ├── dictionary/        (controller/, dto/, service/)
│   ├── flashcard/         (controller/, dto/, ...)
│   ├── learning/
│   ├── notification/
│   ├── publishedcontent/
│   ├── staff/
│   ├── staffcontent/
│   ├── student/
│   └── support/
└── shared/
    ├── common/, config/, dto/, email/, exception/, notification/, security/
```

Mỗi `feature/<module>` là một **package** cho RDS §I.3.2 / SDS §I.1, và là một **block lặp
lại** cho SDS §II "Code Designs" (mỗi module một Class Diagram + class spec + sequence
diagram — tổng cộng 13 module; đừng cố làm hết 13 module trong một lượt, chia thành các
đợt). Xem `docs/02-SDD-Architecture/system-design/refactor-layer-to-feature.md` để biết lý
do của lần refactor này, phòng khi cần giải thích quyết định kiến trúc trong một section
nào đó.

---

## RDS (`Template2_RDS Document.docx`)

| Section | Nguồn |
|---|---|
| I.1.1 Actors | `docs/01-SRS-Requirements/use-cases/Bao_cao_dac_ta_Use_Case.md` §1.3 "Phân loại tác nhân" — 4 nhóm: Student, Staff, StaffManager, Admin |
| I.1.2 Use Cases (danh sách + diagram) | Cùng file trên, §2 "BẢNG TỔNG HỢP USE CASE" — 40 UC, gom theo actor (Student 20, Staff 12, StaffManager 2, Admin 6); UC diagram = một diagram use-case vẽ bằng drawio cho mỗi nhóm actor |
| I.2.1/2.2 Screens Flow + Descriptions | `docs/03-Interface-Specs/feature-specs/frontend/feat-*/SPEC-*.md` (mỗi screen một file SPEC) + `docs/03-Interface-Specs/feature-specs/frontend/FRONTEND-FLOW.md` và `MASTERFrontend-SPEC.md` để biết luồng điều hướng giữa các screen |
| I.2.3 Screen Authorization | Suy ra từ các check role trong `apps/backend/src/main/java/com/jlpt/shared/security/` (annotation/guard kiểu `@PreAuthorize`) đối chiếu với các nhóm actor ở trên — grep annotation role trong từng controller |
| I.2.4 Non-UI Functions | Các job AI chạy async (OCR, Speech) và scheduled job — grep trong `apps/backend/src/main/java/com/jlpt/feature/*/...` tìm `@Scheduled`, `@Async`, các class job/queue |
| I.3.1 Database Schema + Table Descriptions | `docs/02-SDD-Architecture/database-design/JLPT_database.md` — 23 bảng, đã có sẵn nhóm + mục đích + cột chính cho từng bảng |
| I.3.2 Code Packages | Cây `feature/<module>` + `shared/<area>` ở trên |
| II. Requirement Specifications (bảng Functional Description theo từng UC) | `docs/03-Interface-Specs/feature-specs/backend/feat-*/UC-NN-*.md` — đọc file UC tương ứng cho từng số UC; file này đã có sẵn các mục Preconditions/Postconditions/Normal Flow/Alternative Flow/Actors khớp với các cột của bảng. Không phải UC-NN nào cũng đã có file chi tiết riêng (40 UC trong bảng tổng hợp SRS nhưng số file `UC-NN-*.md` thực tế ít hơn) — với UC nào chưa có file chi tiết, ghi `TODO: chưa có detail spec cho UC-NN` thay vì tự bịa ra luồng xử lý. Bảng con Business Rules: grep trong file UC / `feat-*/SPEC.md` tìm tham chiếu `FR-` / business rule. |
| III. Design Specifications (UI Design + Database Access theo từng screen) | `docs/03-Interface-Specs/feature-specs/frontend/feat-*/SPEC-*.md` để lấy field/button; đối chiếu với `feature/<module>/controller` + `repository` tương ứng trong backend source để biết chính xác table DB/CRUD nào được động tới, và `docs/03-Interface-Specs/api-postman/*.postman_collection.json` để lấy đúng shape request/response thật làm căn cứ cho mô tả SQL/field |
| IV. Assumptions/Limitations/Business Rules | `docs/01-SRS-Requirements/constraints/{business,global,safety}.md` |
| Record of Changes | `git log --oneline` trên các file docs/spec liên quan, hoặc để trống các row để team tự điền dần theo thời gian (bảng này vốn dùng để cập nhật tay theo thời gian, không phải điền hồi tố) |

---

## SDS (`Template3_SDS Document.docx`)

| Section | Nguồn |
|---|---|
| I.1 Code Packages | Cùng cây `feature/`+`shared/` như RDS I.3.2 |
| I.2 Database Design | Giống RDS I.3.1 (`JLPT_database.md`) — ER diagram của SDS có thể dùng lại đúng ảnh PNG đã xuất cho RDS |
| II. Code Designs — Class Diagram | Với mỗi `feature/<module>`, liệt kê các class thật của nó: `find apps/backend/src/main/java/com/jlpt/feature/<module> -name "*.java"`, gom theo Controller/Service/Repository/Entity/DTO, vẽ mỗi class một box với field/method thật (không bịa) |
| II. Code Designs — Class Specifications | Đọc các method public của từng class (Read file `.java`) và tóm tắt input/output/xử lý bên trong vào bảng method — đừng đoán hành vi, đọc thân method thật |
| II. Code Designs — Sequence Diagram(s) | Trace một request thật từ đầu đến cuối cho mỗi feature: method Controller → method Service → Repository/entity — đọc đúng chuỗi gọi trong source (grep tìm lời gọi service bên trong controller, rồi lời gọi repository bên trong service) |
| II. Code Designs — Database Queries | Các interface Repository (annotation `@Query`) hoặc JPQL/native query thật sự có trong repository của `feature/<module>` |

---

## Final Release Document (`Template5_Final Release Document.docx`)

| Section | Nguồn |
|---|---|
| I. Deliverable Package | Tên file thật: bản dump DB (thư mục `database/` — kiểm tra xem thực tế có gì), docx RDS/SDS đã generate (chính là output của skill này), file xlsx backlog/issues nếu có. **Repo này chưa có git tag nào** (`git tag` trả về rỗng) — đừng bịa ra link tag; để lại `TODO: tạo và gắn link tag release` thay vào đó |
| II. Installation Guides | `docs/05-Deployment/CI_CD.md`, `Deploy_Diagram.md`, `docker-compose*.yml` ở root repo, file `.env.example` trong `apps/backend/` và `apps/frontend/` để biết các biến môi trường cần thiết |
| III.1 Overview | `docs/01-SRS-Requirements/shared_context.md` và phần "Domain" trong `CLAUDE.md` ở root để viết đoạn mô tả sản phẩm |
| III.2+ Hướng dẫn workflow | Chọn các workflow học viên giá trị cao nhất (Login, Học Kanji/Từ vựng/Ngữ pháp, Làm bài thi thử, Flashcard SRS, Luyện nói) — viết nội dung các bước từ file SPEC tương ứng trong `feature-specs`, nhưng **screenshot phải là thật** (xem ghi chú ở template-structure.md) — đừng thay screenshot bằng wireframe vẽ bằng drawio |
