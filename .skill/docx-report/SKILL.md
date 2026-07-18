---
name: docx-report
description: "Dùng khi người dùng yêu cầu tạo, cập nhật hoặc xuất các tài liệu bàn giao kiểu SWP391 của dự án dưới dạng .docx — Requirement & Design Specification (RDS), Software Design Specification (SDS), hoặc Final Release Document — kết hợp nội dung text thật của dự án với các diagram (Use Case, ER, Package, Class, Sequence, Screens Flow) được vẽ bằng draw.io và nhúng vào dưới dạng ảnh. Kích hoạt khi có yêu cầu như 'tạo RDS', 'tạo SDS', 'xuất Final Release Document', 'làm tài liệu docx cho dự án', 'update SDS với module mới', 'export requirement spec ra Word'."
---

# /docx-report

Biến cây `docs/` và source code của dự án thành 3 tài liệu bàn giao `.docx` chính thức
theo chuẩn FPT SWP391, dùng các template trong `Temp_Document/` làm khuôn style/layout
chuẩn, và draw.io cho mọi diagram. Tự động hoàn toàn khi đã xác định phạm vi (scope) —
không cần chỉnh tay trong Word, dù vẫn nên có một lượt xem lại bằng mắt trong Word ở cuối
(xem Bước 7).

## Skill này KHÔNG bao gồm

`Temp_Document/` còn có 3 template `.xlsx` (AI Usage Report, Project Tracking, Issues
Report) — nằm ngoài phạm vi skill này. Nếu người dùng hỏi về mấy file đó, nói rõ là ngoài
phạm vi và dừng lại; đừng ngầm hiểu yêu cầu xlsx thành yêu cầu docx.

## Các tài liệu mục tiêu

| Mục tiêu | Template | Output |
|---|---|---|
| `rds` | `Temp_Document/Template2_RDS Document.docx` | `docs/07-Release-Documents/RDS_Document.docx` |
| `sds` | `Temp_Document/Template3_SDS Document.docx` | `docs/07-Release-Documents/SDS_Document.docx` |
| `final` | `Temp_Document/Template5_Final Release Document.docx` | `docs/07-Release-Documents/Final_Release_Document.docx` |

File nguồn diagram (.drawio) và ảnh xuất ra nằm ở
`docs/07-Release-Documents/diagrams/<mục tiêu>/`.

## Các file reference (chỉ đọc khi tới đúng bước cần dùng)

- `references/template-structure.md` — bản đồ heading/table/vị trí-ảnh chính xác của cả
  3 template, đã được reverse-engineer sẵn. Không bao giờ tự parse lại file `.docx` từ
  đầu — đọc file này thay vào đó.
- `references/content-sourcing.md` — mỗi section của template lấy nội dung thật từ đường
  dẫn nào trong repo (use case, DB schema, cấu trúc package, screen spec, tài liệu
  deployment, ...), kèm lưu ý là backend hiện tổ chức theo feature
  (`feature/<module>` + `shared/<area>`), KHÔNG phải theo layer như mô tả trong
  `CLAUDE.md` ở root (phần đó đã lỗi thời).
- `references/drawio-cheatsheet.md` — mxGraph XML copy-paste được cho từng loại diagram
  skill này cần, kèm câu lệnh CLI export đã được kiểm chứng.
- `scripts/docx_tools.py` — các hàm hỗ trợ python-docx: copy template, tìm vị trí ảnh,
  thay ảnh diagram mẫu bằng PNG thật, nhân bản cả một section lặp lại
  (`repeat_block`), thay token, nhân bản row của table, và một bước kiểm tra cấu trúc ở
  cuối. Import file này (`sys.path.insert(0, ".claude/skills/docx-report/scripts")`) thay
  vì tự viết lại từ đầu — phần khó nhất (đặc biệt là `repeat_block`, và cái bẫy
  Table-of-Contents `<w:sdt>` khiến so khớp text bị nhầm mà hàm này đã né được) đã được
  giải quyết và test sẵn ở đó rồi.

## Các bước thực hiện

### 1. Xác định phạm vi & nơi xuất file

Xác định cần build (những) tài liệu mục tiêu nào từ yêu cầu của người dùng. Nếu họ chỉ nói
"tạo tài liệu" mà không nói rõ mục tiêu nào, hỏi lại (dùng AskUserQuestion) xem là
RDS/SDS/Final/hay cả ba — đừng đoán mò cho một task tốn nhiều thời gian như thế này. Nếu
họ nêu rõ mục tiêu nhưng không nói phạm vi con (ví dụ "tạo RDS" mà không nói rõ use case
nào), mặc định lấy **toàn bộ nội dung thật đang có** (cả 40 UC, cả 13 feature module,
...) nhưng xử lý theo từng đợt (xem Bước 4) thay vì cố làm trong một lượt khổng lồ — và
nói trước cho người dùng biết quy mô sẽ lớn cỡ nào (ví dụ: "RDS có khoảng 40 block use
case và ~13 screen cần chi tiết hoá; tôi sẽ chia thành các đợt 5–8 mục một"). Nếu người
dùng cho phạm vi nhỏ hơn rõ ràng ("chỉ module auth thôi", "UC-01 đến UC-05"), tôn trọng
đúng phạm vi đó — làm nhỏ mà xong trọn vẹn tốt hơn làm lớn mà dở dang.

Tạo `docs/07-Release-Documents/` và `docs/07-Release-Documents/diagrams/<mục tiêu>/` nếu
chưa có.

### 2. Kiểm tra môi trường

```bash
python3 -c "import docx; print('python-docx OK')"
```

Và xác nhận draw.io Desktop CLI có sẵn — trên máy này nó được cài ở
`C:\Program Files\draw.io\draw.io.exe` (đã kiểm chứng hoạt động); nếu đường dẫn đó không
còn, thử `where drawio` xem có bản cài trong PATH không. Nếu một trong hai kiểm tra thất
bại, nói rõ cho người dùng biết cần cài gì (`pip install python-docx`; draw.io Desktop từ
https://github.com/jgraph/drawio-desktop/releases) rồi dừng lại — đừng tự chế cách vòng
tránh.

### 3. Copy template

Copy file tương ứng từ `Temp_Document/` sang đường dẫn output trong bảng ở trên.
**Tuyệt đối không sửa trực tiếp file gốc trong `Temp_Document/`** — luôn thao tác trên bản
copy. Đây chính là cách để giữ nguyên font chữ/style/header/footer/theme của template
miễn phí; nếu build `.docx` từ `Document()` rỗng thì sẽ mất hết những thứ đó.

Nếu file output đã tồn tại sẵn (từ một lần chạy trước), mở file đó ra để làm tiếp thay vì
copy đè lại từ template, để không làm mất công sức đã làm trước đó — trừ khi người dùng
yêu cầu tạo lại từ đầu hoàn toàn.

### 4. Thu thập nội dung

Làm lần lượt từng section theo `references/content-sourcing.md`. Với các section có số
lượng lớn (một block cho mỗi use case, mỗi feature module, mỗi screen), dùng Agent tool để
chạy song song — mỗi agent xử lý một đợt khoảng 5-8 mục,
`subagent_type="general-purpose"` (cần Read và khả năng trả về kết quả có cấu trúc; agent
không cần tự ghi file, việc ghi file do bước điều phối này đảm nhiệm) — theo đúng cách
skill `graphify` chia nhỏ corpus lớn. Việc của mỗi agent: đọc các file
`UC-NN-*.md` / `SPEC-*.md` / `.java` tương ứng cho đợt của nó và trả về bản tóm tắt có
cấu trúc (các trường khớp với cột của bảng mục tiêu) — không phải văn xuôi, để bước lắp
ráp ở §6 có thể đưa thẳng vào cell của table.

**Nguyên tắc trung thực:** không bao giờ bịa ra một actor, use case, screen, table, class,
business rule, hay ngày tháng nào không có nguồn thật. Nếu một mã UC trong bảng tổng hợp
SRS (`Bao_cao_dac_ta_Use_Case.md`) chưa có file chi tiết `UC-NN-*.md` tương ứng, hoặc một
screen chưa có `SPEC-*.md`, ghi `TODO: <thiếu gì đó> — <mã UC/screen>` vào cell đó thay vì
bịa ra nội dung nghe có vẻ hợp lý, và gom hết các TODO này lại để báo cáo ở cuối (Bước 7).

### 5. Diagram

Với mỗi vị trí ảnh được liệt kê cho mục tiêu này trong `template-structure.md`:
1. Xác định loại diagram (UC / ER / Package / Class / Sequence / Screens-flow) dựa vào
   section chứa nó.
2. Viết XML `.drawio` dựa trên đoạn mẫu tương ứng trong `drawio-cheatsheet.md` làm khung
   sườn, điền tên/field/method thật đã thu thập ở Bước 4 vào — không bao giờ để text
   placeholder kiểu "ClassA".
3. Lưu vào `docs/07-Release-Documents/diagrams/<mục tiêu>/<slug>.drawio`.
4. Export:
   ```bash
   "C:\Program Files\draw.io\draw.io.exe" --export --format png --width 1600 --embed-diagram \
     --output "docs/07-Release-Documents/diagrams/<mục tiêu>/<slug>.png" \
     "docs/07-Release-Documents/diagrams/<mục tiêu>/<slug>.drawio"
   ```
5. Dùng Read tool xem lại ảnh PNG vừa xuất một lần để chắc chắn nó render hợp lý trước khi
   làm tiếp — bắt lỗi diagram sai ở đây rẻ hơn nhiều so với phát hiện sau khi đã nhúng vào
   docx rồi.

Riêng section User Manual của Final Release Document cần **screenshot thật**, không phải
mockup vẽ bằng draw.io (xem ghi chú ở cuối `template-structure.md`) — nếu app chưa chạy để
chụp màn hình được, để lại `TODO: screenshot — <workflow>` thay vì dùng tạm một wireframe.

### 6. Lắp ráp

Viết một script Python ngắn dùng riêng cho mỗi lần chạy (đừng cố làm cho nó generic hoàn
toàn ngay từ đầu — các section/token cụ thể khác nhau tuỳ mục tiêu) làm việc sau:
```python
import sys
sys.path.insert(0, r".claude/skills/docx-report/scripts")
from docx_tools import *

doc = open_from_template("Temp_Document/Template2_RDS Document.docx",
                          "docs/07-Release-Documents/RDS_Document.docx")
# ... dùng repeat_block() cho từng section lặp lại, fill_tokens_in_range() /
# set_cell_text() / add_table_row_like_last() để điền nội dung, replace_image_in_paragraph()
# cho từng diagram ...
doc.save("docs/07-Release-Documents/RDS_Document.docx")
```

Các quy tắc quan trọng rút ra được từ việc test trên template thật:

- `repeat_block(doc, start_substr, end_substr, n_copies)` — nhân bản cả một section
  heading+table(s)+ảnh N lần. Luôn thao tác trên các "khoảng phần tử" (element ranges) mà
  hàm này trả về (`fill_tokens_in_range`, `tables_in_range`, `image_paragraphs_in_range`)
  — không bao giờ tìm kiếm lại trên toàn bộ tài liệu bên trong một vòng lặp qua từng mục,
  nếu không sẽ sửa nhầm sang bản sao khác.
- Bất kỳ tìm kiếm substring nào trên `doc.element.body.iterchildren()` đều phải bỏ qua
  phần tử `<w:sdt>` (field Mục Lục của Word cache lại text của mọi heading, nên tìm kiếm
  ngây thơ sẽ khớp vào Mục Lục trước cả heading thật — `docx_tools.py` đã tự né lỗi này
  rồi; đừng vô tình viết lại code tay và tái tạo lại lỗi đó).
- Với các bảng tra cứu đơn giản chỉ cần thêm row thật nhiều hơn số row mẫu (1-2 row) có
  sẵn trong template (bảng Actors, bảng Package, bảng Mô Tả Table trong DB), dùng
  `add_table_row_like_last()` thay vì `repeat_block()` — hàm đó dùng cho cả section
  heading+table, không phải để tăng số row của một table đơn lẻ.
- Giới hạn chiều rộng ảnh nhúng ở 6.5in (`Inches(6.5)`) để không bị tràn lề trang.

### 7. Kiểm tra lại

```python
print(summarize(doc2))              # số heading/table/ảnh — kiểm tra chéo xem có khớp
                                     # với số mục thực sự đã điền hay không
print(find_leftover_markers(doc2))  # còn sót cái nào nghĩa là bỏ sót section đó
```

Môi trường này không có `pandoc`/`soffice`, nên không có cách xem trước dạng ảnh/PDF tự
động — kiểm tra cấu trúc như trên cộng với xem lại vài ảnh diagram đã xuất (dùng Read
tool) là toàn bộ phần QA tự động có thể làm. Nói rõ điều này trong báo cáo cuối cùng, đừng
ngầm ý là docx đã được kiểm tra bằng mắt.

### 8. Báo cáo

Báo cho người dùng biết: đã build (những) mục tiêu nào, file `.docx` và nguồn `.drawio`
nằm ở đâu, đã điền được bao nhiêu block lặp lại (ví dụ "32/40 use case — 8 cái chưa có
detail spec"), và toàn bộ danh sách các `TODO:` còn lại cần người quyết định. Khuyên người
dùng nên xem lại lần cuối trong Microsoft Word về layout/phân trang/font trước khi nộp.
