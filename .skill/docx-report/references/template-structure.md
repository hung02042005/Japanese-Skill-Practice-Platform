# Bản đồ cấu trúc template

Trích xuất trực tiếp từ `Temp_Document/*.docx` bằng python-docx (style + text của từng
paragraph, hình dạng table, và vị trí ảnh, theo đúng thứ tự trong tài liệu). Dùng file
này thay vì tự parse lại `.docx` mỗi lần chạy. `[IMAGE]` đánh dấu một paragraph mà XML của
nó chứa `<w:drawing>` — đó chính là chỗ cần chèn ảnh diagram/screenshot vào. Text kiểu
`[...]`/`<<...>>` là text hướng dẫn/mẫu của FPT — luôn phải thay thế, không bao giờ để lại
trong bản output.

Lưu ý: các đoạn trích trong khung dưới đây (tên heading, header của table, nhãn field...)
là **nguyên văn tiếng Anh lấy trực tiếp từ file `.docx` gốc** — giữ nguyên không dịch, vì
đó chính là text thật sự có trong template và cần khớp đúng khi tìm kiếm trong tài liệu.
Phần giải thích/chú thích (sau dấu `<-`) là của skill này viết thêm, đã dịch sang tiếng
Việt.

---

## Template2_RDS Document.docx → `RDS_Document.docx`

22 table, 8 ảnh. Các style paragraph dùng: `Title`, `Heading 1..5`, `Normal`,
`List Paragraph`, `List`, `Body Text`.

```
[Title] Requirement & Design Specification
[Normal] <<Project name>> / Version / date line
Heading 1: Record of Changes
  Table (13x5): Version | A*M,D | In charge | Change Description   <- thêm mỗi thay đổi thật một row (dựa theo git tag/PR)

Heading 1: I. Overview
  Heading 2: 1. User Requirements
    Heading 3: 1.1 Actors
      Table (Nx3): # | Actor | Description                         <- mỗi nhóm actor một row (Student/Staff/StaffManager/Admin)
    Heading 3: 1.2 Use Cases
      Heading 4: a. Diagram(s)
        [IMAGE]                                                     <- diagram Use Case (actor + hình elip), một cái cho mỗi nhóm actor hoặc một cái tổng
      Heading 4: b. Descriptions
        Table (Nx4): ID | Feature | Use Case | Use Case Description <- mỗi UC một row (40 row, gom theo feature)
  Heading 2: 2. Overall Functionalities
    Heading 3: 2.1 Screens Flow
      [IMAGE]                                                       <- diagram screens-flow (hình chữ nhật = screen, hình elip = pop-up, mũi tên = điều hướng)
    Heading 3: 2.2 Screen Descriptions
      Table (Nx4): # | Feature | Screen | Description
    Heading 3: 2.3 Screen Authorization
      Table: Screen | các cột Role (X = được phép)                  <- cột = Student/Staff/StaffManager/Admin
    Heading 3: 2.4 Non-UI Functions
      Table (Nx4): # | Feature | System Function | Description      <- batch job, AI job chạy async, cron, ...
  Heading 2: 3. System High Level Design
    Heading 3: 3.1 Database Design
      Heading 4: a. Database Schema
        [IMAGE]                                                     <- ER diagram
      Heading 4: b. Table Descriptions
        Table (Nx3): No | Table | Description (+ ghi chú PK/FK)     <- mỗi bảng DB một row (23 row)
    Heading 3: 3.2 Code Packages
      [IMAGE]                                                       <- package diagram
      Table (Nx3): No | Package | Description                      <- mỗi Java package một row (controller/service/repository/...)

Heading 1: II. Requirement Specifications                           <<< SECTION LẶP LẠI, một block cho mỗi Feature > mỗi Use Case >>>
  Heading 2: N. <<Feature Name>>
    Heading 3: N.M <<UseCaseCode_UC Name>>
      Heading 4: a. Functionalities
        Table (15x4) "Functional Description Template":
          UC ID and Name | Created By / Date Created | Primary Actor / Secondary Actors |
          Trigger | Description | Preconditions | Postconditions | Normal Flow |
          Alternative Flows | Exceptions | Priority | Frequency of Use | Business Rules |
          Other Information | Assumptions
      Heading 4: b. Business Rules
        Table (Nx3): ID | Business Rule | Business Rule Description

Heading 1: III. Design Specifications                               <<< SECTION LẶP LẠI, một block cho mỗi Feature > mỗi Screen/Function >>>
  Heading 2: N. <<Feature Name>>
    Heading 3: N.M <<SubFeature/Screen Name>>
      Heading 4: a. <<Screen/Function Name>>
        Heading 5: UI Design
          [IMAGE]                                                   <- mockup UI (có thể dùng screenshot thật thay vì vẽ bằng drawio)
          Table (Nx3): Field Name | Field Type | Description
        Heading 5: Database Access
          Table (Nx3): Table | CRUD | Description
          SQL Commands (paragraph văn bản thường, mỗi câu SELECT/INSERT/UPDATE một dòng)

Heading 1: IV. Appendix
  Heading 2: 1. Assumptions & Dependencies (bullet list, đánh số AS-n / DE-n)
  Heading 2: 2. Limitations & Exclusions (văn bản tự do)
  Heading 2: 3. Business Rules
    Table (Nx3): ID | Category | Rule Definition
```

Các vị trí ảnh cần điền (8 tổng cộng): logo trang bìa (giữ nguyên — logo trường, không
phải nội dung dự án), UC diagram, Screens Flow diagram, DB Schema (ER) diagram, Package
diagram, cộng thêm mỗi screen được chi tiết hoá ở §III sẽ có thêm một ảnh UI mockup (số
lượng tăng theo số screen bạn chọn chi tiết hoá — nên bắt đầu với những screen dùng nhiều
nhất, không cần làm hết toàn bộ).

---

## Template3_SDS Document.docx → `SDS_Document.docx`

5 table, 5 ảnh. Style dùng: `Normal`, `NormalH` (tiêu đề section ở trang bìa),
`Heading 1..4`.

```
[IMAGE]  <<Project name>> / Software Design Specification / date line
NormalH: Record of changeS
  Table (14x4): Date | A*M,D | In charge | Change Description

Heading 1: I. Overview
  Heading 2: 1. Code Packages
    [IMAGE]                                                         <- package diagram tổng
    Table (Nx3): No | Package | Description                        <- mỗi Java package một row
  Heading 2: 2. Database Design
    Heading 3: a. Database Schema
      [IMAGE]                                                       <- ER diagram (có thể dùng lại đúng ảnh của RDS)
    Heading 3: b. Table Description
      Table (Nx3): No | Table | Description

Heading 1: II. Code Designs                                         <<< SECTION LẶP LẠI, một block cho mỗi Feature/Function >>>
  Heading 2: N. <Feature/Function Name>
    Heading 3: a. Class Diagram
      [IMAGE]                                                       <- class diagram cho các class thuộc feature này
    Heading 3: b. Class Specifications
      Heading 4: <ClassName> Class                                  <<< lặp lại lồng bên trong, một block cho mỗi class trong diagram >>>
        Table (Nx3): No | Method | Description
    Heading 3: c. Sequence Diagram(s)
      [IMAGE]                                                       <- một sequence diagram cho mỗi flow chính trong feature
    Heading 3: d. Database Queries
      SQL Commands (paragraph văn bản thường)
```

---

## Template5_Final Release Document.docx → `Final_Release_Document.docx`

1 table, 1 ảnh.

```
[IMAGE] <<Project name>> / Final Release Document / date line

Heading 1: I. Deliverable Package
  Table (7x3): No. | File | Notes                                   <- liệt kê file thật: bản dump DB, docx RDS/SDS, xlsx backlog, xlsx issues, tag source code
  Related deliverables (bullet): link source code đã tag, link video demo

Heading 1: II. Installation Guides
  (văn bản tự do: prerequisite, biến môi trường, docker-compose up, các bước migration — lấy từ docs/05-Deployment/)

Heading 1: III. User Manual
  Heading 2: 1. Overview
    (văn bản tự do: app làm gì, các actor là ai)
  Heading 2: 2. <<Workflow Name N>>                                  <<< SECTION LẶP LẠI, một block cho mỗi workflow chính của người dùng >>>
    (các bước hướng dẫn bằng văn bản + screenshot thật, không phải drawio diagram — đây phải
    là screenshot thật của app đang chạy; skill cần nói rõ điều này thay vì giả một mockup
    thành "screenshot")
```

Lưu ý: khác với RDS/SDS, section User Manual cần **screenshot thật** của app đang chạy,
không phải mockup vẽ bằng draw.io. Nếu app không chạy được/không truy cập được, để lại
placeholder `TODO: screenshot — <workflow> — <route>` thay vì thay thế bằng wireframe vẽ
bằng drawio và giả vờ đó là screenshot.
