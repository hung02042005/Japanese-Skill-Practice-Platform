# SPEC — Hướng dẫn Generate Requirement & Design Specification (Template2/RDS) dạng Markdown

> **Nguồn**: `Temp_Document/Template2_RDS Document.pdf` / `.docx` (FPT University template, ví dụ mẫu "GAMS" — Global Access Management System)
> **Xem cùng**: [`SPEC-sds-template-generation-guide.md`](../02-SDD-Architecture/SPEC-sds-template-generation-guide.md) (Template3/SDS), [`SPEC-issues-report-template-generation-guide.md`](../06-Management/SPEC-issues-report-template-generation-guide.md) (Template4), [`SPEC-final-release-template-generation-guide.md`](../06-Management/SPEC-final-release-template-generation-guide.md) (Template5)
> **Đối tượng dùng**: Dev hoặc AI Agent cần soạn/bổ sung tài liệu đặc tả yêu cầu + thiết kế màn hình chi tiết cho dự án này.

---

## 1. Mục đích

Template2 (**RDS — Requirement & Design Specification**) là tài liệu **gộp cả yêu cầu (SRS) lẫn thiết kế màn hình chi tiết (mini-SDS theo từng màn hình)** trong 1 file: Actor/Use Case → Functional Description đầy đủ (Trigger/Pre-Post condition/Normal Flow đánh số/Exception) → UI field-level design + SQL cho từng màn hình.

Dự án này **đã tách sẵn** 2 tài liệu riêng biệt thay vì gộp làm 1 như Template2:

- Yêu cầu (Use Case, Actor) → `docs/01-SRS-Requirements/use-cases/`, `docs/01-SRS-Requirements/constraints/`
- Thiết kế hệ thống (Class/Sequence/DB) → `docs/02-SDD-Architecture/SDS-Japanese-Skill-Practice-Platform.md`

→ Spec này **không đề xuất viết lại 1 file RDS gộp trùng lặp nội dung đã có**. Thay vào đó, spec quy định: (1) cách generate phần Template2 yêu cầu mà 2 tài liệu hiện tại **chưa có ở đúng độ chi tiết** (Functional Description đầy đủ theo từng UC, UI field-table + SQL theo từng màn hình, Screen Authorization matrix), và (2) cách trỏ sang tài liệu có sẵn cho phần đã trùng, để không tạo 2 nguồn sự thật lệch nhau.

---

## 2. Cấu trúc gốc của Template2 (.docx/.pdf)

| # | Mục | Nội dung gốc (placeholder) |
|---|---|---|
| — | Cover + Table of Contents | Giống Template3/5 (`<<PROJECT NAME>>` + tên tài liệu + "– Hanoi, [tháng] [năm] –") — Template2 có thêm dòng `Version: X.X` trên cover và header/footer lặp mỗi trang dạng `<Mã tài liệu>_vX.X · Page N/20` mà Template3/5 không có |
| — | Record of Changes | **Khác Template3/5**: Template2 có **5 cột** `Version \| Date \| A*/M,D \| In charge \| Change Description` (thêm cột `Version` so với đầu mục); Template3/5 chỉ có 4 cột `Date \| A*/M,D \| In charge \| Change Description` — khi generate tài liệu theo đúng Template2, phải thêm cột `Version` |
| I.1 | User Requirements → Actors, Use Cases (a. Diagram, b. Description) | Bảng Actor, sơ đồ Use Case, bảng UC tóm tắt |
| I.2 | Overall Functionalities → Screens Flow, Screen Descriptions, Screen Authorization, Non-UI Functions | Sơ đồ luồng màn hình, bảng mô tả màn hình, ma trận phân quyền theo Role × Screen/Action |
| I.3 | System High Level Design → Database Design, Code Packages | Giống hệt Template3 § Overview |
| II | Requirement Specifications → mỗi Feature/UC có **Functional Description Template** đầy đủ (UC ID/Name, Created By/Date, Primary/Secondary Actor, Trigger, Description, Preconditions, Postconditions, Normal Flow "X.0", Alternative Flows "X.Y", Exceptions "X.Y.EZ", Priority, Frequency of Use, Business Rules, Other Information, Assumptions) + bảng Business Rules riêng cho UC đó | Ví dụ mẫu: `UC-2_Login System` (kèm luồng Google/Facebook login), `UC-5_Order a Meal`, `UC-6_Register for Payroll Deduction` |
| III | Design Specifications → mỗi Screen/Function có **UI Design** (mockup + bảng Field Name/Type/Description) và **Database Access** (bảng Table/CRUD/Description + SQL Commands) | Ví dụ mẫu đầy đủ: màn `User Login`, `Setting List`, `Setting Details` |
| IV | Appendix → Assumptions & Dependencies, Limitations & Exclusions, Business Rules (bảng ID/Category/Rule Definition) | |

---

## 3. Ánh xạ mục → nguồn dữ liệu thật trong repo

| Mục Template2 | Trạng thái trong repo | Nguồn thật / cách xử lý |
|---|---|---|
| I.1.1 Actors | ✅ Có sẵn | [`use-cases/Bao_cao_dac_ta_Use_Case.md`](use-cases/Bao_cao_dac_ta_Use_Case.md) § "1.3. Phân loại tác nhân" — trỏ link, không viết lại |
| I.1.2.b Use Case description | ✅ Có sẵn (40 UC) | Cùng file, § "2. BẢNG TỔNG HỢP USE CASE" (chia theo nhóm Student/Staff/StaffManager/Admin) — trỏ link |
| I.1.2.a Use Case diagram | ✅ Có sẵn | [`Overall-Functionalities.md § "I.1.2.a — Use Case Diagram"`](Overall-Functionalities.md#i12a--use-case-diagram) — Mermaid, nhóm theo 4 role |
| I.2.1 Screens Flow | ✅ Có sẵn | [`Overall-Functionalities.md § "I.2.1 — Screens Flow"`](Overall-Functionalities.md#i21--screens-flow) — Mermaid theo route thật trong `App.jsx` |
| I.2.2 Screen Descriptions | ✅ Có sẵn | [`Overall-Functionalities.md § "I.2.2 — Screen Descriptions"`](Overall-Functionalities.md#i22--screen-descriptions) — 14 dòng theo route + component thật |
| I.2.3 Screen Authorization | ✅ Có sẵn | [`Overall-Functionalities.md § "I.2.3 — Screen Authorization"`](Overall-Functionalities.md#i23--screen-authorization) — ma trận đối chiếu cả route-guard Frontend lẫn `@PreAuthorize` Backend |
| I.2.4 Non-UI Functions | ✅ Có sẵn | [`Overall-Functionalities.md § "I.2.4 — Non-UI Functions"`](Overall-Functionalities.md#i24--non-ui-functions) — 5 job thật (`SpeakingAsyncProcessor`, `AuthEventListener` x2, `NotificationDispatcher` x2) |
| I.3.1 Database Design | ✅ Có sẵn | [`database-design/JLPT_database.md`](../02-SDD-Architecture/database-design/JLPT_database.md) — trỏ link |
| I.3.2 Code Packages | ✅ Có sẵn | [`SDS-Japanese-Skill-Practice-Platform.md`](../02-SDD-Architecture/SDS-Japanese-Skill-Practice-Platform.md) § "I.1 Code Packages" — trỏ link |
| II. Functional Description Template (đầy đủ, đánh số Normal/Alternative/Exception Flow) | ⚠️ Có 1 ví dụ đầy đủ, 39/40 UC còn lại vẫn rút gọn | [`RDS-User-Login.md § II`](RDS-User-Login.md) đã nâng cấp **UC-01_User Login** lên đúng rigor Template2 (Created By/Date, Trigger, Normal Flow "1.0", Alt Flow "1.1", Exception "1.0.E1"–"E5", Priority, Business Rules). 39 UC còn lại trong `use-cases/Bao_cao_dac_ta_Use_Case.md` **vẫn ở bản rút gọn** — dùng `RDS-User-Login.md` làm mẫu khi cần nâng cấp UC khác |
| II.*.b Business Rules (per UC) | ⚠️ Có ví dụ, chưa phủ hết UC | [`RDS-User-Login.md § II.1.1.b`](RDS-User-Login.md) trích đúng ID thật (`BIZ-AUTH-01/02/04/06`) cho UC-01 + phát hiện 1 rule có trong code nhưng **chưa có ID** trong `constraints/business.md` (khoá tài khoản sau 5 lần sai/15 phút) — cần bổ sung ID chính thức khi cập nhật file đó |
| III. UI Design (field-level, mỗi màn hình) | ⚠️ Có 1 ví dụ đầy đủ (`User Login`), các màn khác chưa làm | [`RDS-User-Login.md § III.1.1`](RDS-User-Login.md) — bảng Field Name/Type/Description dựng từ `Login.jsx` thật. Áp dụng cùng cách (Mục 5 bước 3) cho các màn hình còn lại khi cần |
| III. Database Access + SQL (mỗi màn hình) | ⚠️ Có ví dụ ở đúng granularity | [`RDS-User-Login.md § III.1.1`](RDS-User-Login.md) đã tách SQL theo **đúng 1 màn hình** (Login) từ logic thật trong `AuthenticationService`, khác `SDS-*.md` (SQL theo feature/nhiều màn hình gộp) — dùng làm mẫu tách nhỏ cho màn khác |
| III.1.2 System Access (mẫu `User Login`) | ✅ Có sẵn | [`RDS-User-Login.md`](RDS-User-Login.md) — bảng Field/DB Access/SQL đầy đủ, ghi rõ điểm khác biệt thật với ví dụ gốc Template2 (dự án chỉ có Google OAuth, không có Facebook Login; bcrypt thay vì MD5) |
| IV.3 Business Rules (ID/Category/Rule Definition) | ✅ Có sẵn | [`Overall-Functionalities.md § IV.3`](Overall-Functionalities.md#iv3--business-rules-định-dạng-template2-id--category--rule-definition) — ánh xạ Category sang 8 mục trong `constraints/business.md`, không copy lại để tránh lệch |
| IV.1/IV.2 Assumptions & Dependencies, Limitations & Exclusions | ✅ Có bản nháp | [`Overall-Functionalities.md § IV.1/IV.2`](Overall-Functionalities.md#iv1--assumptions--dependencies) — **có 1 phát hiện quan trọng cần biết**: engine chấm phát âm (UC-13) hiện là `StubSpeechRecognitionEngine`, sinh điểm mô phỏng tất định, **chưa tích hợp ASR/AI thật**. Bản nháp cần team xác nhận lại, chưa phải bản chốt |

---

## 4. Quy ước đặt tên & vị trí file

- **Không tạo 1 file `RDS-*.md` gộp mới cho toàn hệ thống mà COPY nội dung đã có** — sẽ trùng lặp với `use-cases/`, `constraints/`, và `SDS-*.md`, vi phạm nguyên tắc 1 nguồn sự thật. **Ngoại lệ được phép**: 1 file "master/index" duy nhất theo đúng khung mục lục Template2 (Cover → RoC → I → II → III → IV), nội dung mỗi mục chỉ **tóm tắt ngắn + link** sang nguồn thật (không copy nguyên văn) — đây không tính là "gộp trùng lặp". Ví dụ: [`RDS-Japanese-Skill-Practice-Platform.md`](RDS-Japanese-Skill-Practice-Platform.md).
- Khi cần **bổ sung phần còn thiếu** (Mục 3, các dòng ⚠️/❌), bổ sung **ngay trong file gốc tương ứng**:
  - Actor/UC thiếu chi tiết → sửa trực tiếp trong `docs/01-SRS-Requirements/use-cases/Bao_cao_dac_ta_Use_Case.md`.
  - Business Rule thiếu → bổ sung trong `docs/01-SRS-Requirements/constraints/business.md`.
  - **Use Case diagram, Screens Flow, Screen Authorization matrix, Non-UI Functions, Business Rules Category mapping, Assumptions/Limitations** (các mục còn lại của I.1.2.a, I.2, IV mà không gắn với 1 màn hình cụ thể) → gộp chung 1 file `docs/01-SRS-Requirements/Overall-Functionalities.md` — **đã tạo**, xem file này trước khi thêm mục mới, tránh tạo file thứ 2 trùng phạm vi.
  - UI field-table + Database Access + SQL cho **1 màn hình cụ thể** → tạo file riêng theo mẫu ở bullet dưới, không gộp vào `Overall-Functionalities.md`.
- Nếu 1 màn hình cụ thể cần đủ format Template2 (UC + UI field-table + SQL trong 1 file, ví dụ để nộp báo cáo theo đúng mẫu FPT), đặt tại `docs/01-SRS-Requirements/RDS-<Screen-Name>.md` — chỉ 1 màn hình/nhóm màn hình liên quan mỗi file, không gộp toàn hệ thống. Ví dụ đã có: [`RDS-User-Login.md`](RDS-User-Login.md).

---

## 5. Quy trình từng bước khi cần soạn 1 phần Template2 còn thiếu

1. **Xác định đúng mục đang thiếu** theo bảng Mục 3 (đừng viết lại phần đã ✅ có sẵn).
2. **Với Functional Description đầy đủ 1 UC**: mở đúng UC trong `Bao_cao_dac_ta_Use_Case.md`, giữ nguyên nội dung hiện có, bổ sung thêm: `Created By`/`Date Created` (lấy từ `git log --follow` của UC đó nếu cần), đánh số lại `Luồng cơ bản` thành `X.0`, `Luồng thay thế` thành `X.1`/`X.2`, thêm mục Exception đánh số `X.0.EZ` cho từng lỗi hệ thống thật ném ra (tra trong code — vd `BadCredentialsException`, `AttemptAlreadySubmittedException`), `Priority` (Must/Should/Could Have), `Business Rules` (trích ID thật từ `constraints/business.md`).
3. **Với UI Design 1 màn hình**: mở đúng file `.jsx` của trang đó, liệt kê từng field/nút thật (tên biến, `placeholder`, loại input) thành bảng `Field Name/Field Type/Description` — không suy đoán field chưa tồn tại trong code.
4. **Với Database Access + SQL 1 màn hình**: tra service/repository backend gọi bởi màn hình đó (theo route API thật FE gọi), liệt kê bảng bị CRUD + copy nguyên văn câu SQL/JPQL thật (hoặc query tương đương suy từ Spring Data method name) — không tự chế SQL.
5. **Với Screen Authorization matrix**: đọc các route-guard thật trong `App.jsx` (`PrivateRoute`/`StaffRoute`/`ManagerRoute`/`AdminRoute`) + `@PreAuthorize` ở backend controller tương ứng, dựng bảng Role × Screen (X = có quyền) — 2 nguồn (FE guard + BE `@PreAuthorize`) phải khớp nhau, nếu lệch thì ghi chú rõ đây là bug cần báo lại, không tự chọn 1 bên làm chuẩn.
6. **Review lại**: mọi UC ID, Business Rule ID, tên bảng, tên field trích dẫn trong phần mới viết phải tồn tại thật trong `use-cases/`, `constraints/`, hoặc code — grep lại để xác nhận trước khi coi là xong.

---

## 6. Checklist trước khi coi là hoàn thành

- [ ] Không tạo file `RDS-*.md` gộp toàn hệ thống trùng nội dung `use-cases/`, `constraints/`, `SDS-*.md`.
- [ ] Phần bổ sung Functional Description có đủ: Trigger, Preconditions, Postconditions, Normal Flow đánh số `X.0`, Alternative Flow đánh số `X.Y`, Exception đánh số `X.Y.EZ`, Priority, Business Rules (ID thật).
- [ ] Bảng UI field-level (nếu viết) khớp đúng field/nút thật trong `.jsx`, không suy đoán field chưa tồn tại.
- [ ] SQL trong "Database Access" là query thật/suy trực tiếp từ Repository method thật, không tự chế.
- [ ] Screen Authorization matrix đối chiếu cả FE route-guard lẫn BE `@PreAuthorize`, ghi chú rõ nếu 2 bên lệch nhau thay vì tự chọn 1 bên.
- [ ] Business Rule ID trích dẫn tồn tại thật trong `constraints/business.md`, không tự đặt ID mới.
- [ ] File đặt đúng vị trí theo Mục 4, không đặt tên gây hiểu nhầm là bản dịch đầy đủ 1-1 của Template2.

---

## 7. Ghi chú tình trạng hiện tại của repo

*(Cập nhật lần cuối: sau khi tạo `Overall-Functionalities.md` + `RDS-User-Login.md`)*

- **Đã có** toàn bộ phần I.1.2.a, I.2.1–I.2.4, IV.1–IV.3 tại [`Overall-Functionalities.md`](Overall-Functionalities.md), và 1 ví dụ đầy đủ rigor Template2 (Functional Description + UI Design + Database Access/SQL) cho UC-01 tại [`RDS-User-Login.md`](RDS-User-Login.md) — xem trạng thái ✅ chi tiết ở bảng Mục 3.
- **Còn thiếu thật sự** (chưa làm trong các file trên):
  - 39/40 UC còn lại trong `use-cases/Bao_cao_dac_ta_Use_Case.md` vẫn ở bản rút gọn (thiếu đánh số flow, Priority, Frequency of Use, Created By) — `RDS-User-Login.md` chỉ là 1 ví dụ mẫu, chưa áp dụng đại trà.
  - UI field-table + Database Access/SQL Template2-style mới có cho màn `User Login` — các màn hình khác (Setting/Content/Question/Dashboard...) vẫn dựa vào `SDS-Japanese-Skill-Practice-Platform.md` (đặc tả theo **feature**, không theo **từng màn hình** như Template2 muốn — khác granularity, không phải tương đương).
  - Rule khoá tài khoản sau 5 lần đăng nhập sai/15 phút có trong code (`AuthenticationService`) nhưng **chưa có ID chính thức** trong `constraints/business.md` — phát hiện khi viết `RDS-User-Login.md`, cần bổ sung.
  - **Phát hiện quan trọng**: engine chấm điểm phát âm (UC-13/UC-31, package `feature/speaking`) hiện là `StubSpeechRecognitionEngine` — sinh điểm mô phỏng tất định từ độ dài audio, **chưa phải AI/ASR thật**. Cần biết khi viết Functional Description hoặc demo cho UC-13.
  - Assumptions & Dependencies / Limitations & Exclusions ở `Overall-Functionalities.md § IV.1/IV.2` mới là **bản nháp đầu tiên**, chưa được team xác nhận chính thức.
