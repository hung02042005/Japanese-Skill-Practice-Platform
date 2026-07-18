# Cheatsheet vẽ diagram bằng draw.io + export

Mọi diagram đều được viết tay dưới dạng `.drawio` thô (mxGraph XML) — không cần mở GUI của
draw.io. Mỗi file dưới đây tự nó là một file `.drawio` hợp lệ, đầy đủ (bọc các đoạn cell
trong khung sườn ở §0). Mỗi loại diagram nên để riêng một file (ví dụ
`uc-student.drawio`, `er-schema.drawio`, `class-auth.drawio`) — đừng nhét nhiều diagram
không liên quan vào chung một page của cùng một file, sẽ khó export/bảo trì lại sau này.

## 0. Khung sườn file + câu lệnh export đã kiểm chứng

```xml
<mxfile host="Electron" version="24.0.0">
  <diagram name="Page-1" id="page1">
    <mxGraphModel dx="800" dy="600" grid="1" gridSize="10" guides="1" tooltips="1"
        connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1100"
        pageHeight="850" math="0" shadow="0">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
        <!-- toàn bộ cell vertex/edge đặt ở đây, parent="1" trừ khi lồng trong một container -->
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
```

Lệnh export (đã chạy thử và kiểm chứng thật trong môi trường của repo này — draw.io
Desktop 30.2.4, không cần Xvfb/display gì trên Windows):

```bash
"/c/Program Files/draw.io/draw.io.exe" --export --format png --width 1600 --transparent \
  --output "path/to/out.png" "path/to/in.drawio"
```

Trên PowerShell gốc, quote đường dẫn exe tương tự:
`& "C:\Program Files\draw.io\draw.io.exe" --export ...`.
Ghi chú:

- `--width` scale theo tỷ lệ — chọn 1200–1800 cho các diagram sẽ nhúng vào docx ở chiều
  rộng ~6.5in (width càng cao thì càng nét khi in).
- Bỏ `--transparent` với các diagram có nền màu cần giữ (ví dụ ER table có `fillColor`);
  dùng `--transparent` cho diagram dạng nét vẽ (UC, sequence) để hoà vào nền trắng của
  trang docx.
- `-e/--embed-diagram` nhúng luôn diagram có thể sửa được vào bên trong file PNG (draw.io
  có thể mở lại một PNG export kiểu này) — nên thêm vào để PNG vẫn sửa được kể cả khi mất
  file nguồn `.drawio`: thêm `--embed-diagram` vào lệnh trên.
- Export cả một folder cùng lúc: `drawio.exe --export --format png -r --output <dir> <dir>`.

---

## 1. Use Case diagram (RDS I.1.2.a)

```xml
<mxCell id="actor1" value="Student" style="shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;" vertex="1" parent="1">
  <mxGeometry x="40" y="120" width="30" height="60" as="geometry" />
</mxCell>
<mxCell id="sysBoundary" value="JLPT Learning Platform" style="rounded=0;whiteSpace=wrap;html=1;verticalAlign=top;fillColor=none;" vertex="1" parent="1">
  <mxGeometry x="160" y="40" width="360" height="400" as="geometry" />
</mxCell>
<mxCell id="uc1" value="Login" style="ellipse;whiteSpace=wrap;html=1;" vertex="1" parent="1">
  <mxGeometry x="200" y="80" width="140" height="60" as="geometry" />
</mxCell>
<mxCell id="uc2" value="Learn Kanji" style="ellipse;whiteSpace=wrap;html=1;" vertex="1" parent="1">
  <mxGeometry x="200" y="180" width="140" height="60" as="geometry" />
</mxCell>
<!-- association thường: không có mũi tên -->
<mxCell id="e1" style="edgeStyle=orthogonalEdgeStyle;html=1;endArrow=none;rounded=0;" edge="1" parent="1" source="actor1" target="uc1">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
<!-- quan hệ <<include>>/<<extend>> giữa hai use case -->
<mxCell id="e2" value="&lt;&lt;include&gt;&gt;" style="edgeStyle=orthogonalEdgeStyle;html=1;dashed=1;endArrow=open;elbow=vertical;" edge="1" parent="1" source="uc1" target="uc2">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

Đặt hình chữ nhật system-boundary vào TRƯỚC (z-order thấp hơn) để actor bên ngoài và use
case bên trong đều hiển thị đúng — thứ tự trong XML chính là z-order.

---

## 2. ER diagram / database schema (RDS I.3.1.a, SDS I.2.a)

Mỗi "entity" là một container `swimlane` (thanh tiêu đề = tên bảng) dùng
`stackLayout` để các row tự xếp chồng; mỗi row là một `text` con đơn giản. Đây là shape
ER-table chuẩn của draw.io.

```xml
<mxCell id="tbl_student" value="student_users" style="swimlane;html=1;childLayout=stackLayout;horizontal=1;startSize=26;horizontalStack=0;resizeParent=1;resizeParentMax=0;collapsible=0;marginBottom=0;fillColor=#dae8fc;fontStyle=1;" vertex="1" parent="1">
  <mxGeometry x="40" y="40" width="220" height="120" as="geometry" />
</mxCell>
<mxCell id="tbl_student_pk" value="PK  student_id : BIGINT" style="text;html=1;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;portConstraint=eastwest;rotatable=0;points=[[0,0.5],[1,0.5]];" vertex="1" parent="tbl_student">
  <mxGeometry y="26" width="220" height="26" as="geometry" />
</mxCell>
<mxCell id="tbl_student_email" value="    email : VARCHAR(255)" style="text;html=1;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;portConstraint=eastwest;rotatable=0;points=[[0,0.5],[1,0.5]];" vertex="1" parent="tbl_student">
  <mxGeometry y="52" width="220" height="26" as="geometry" />
</mxCell>

<mxCell id="tbl_progress" value="student_content_progress" style="swimlane;html=1;childLayout=stackLayout;horizontal=1;startSize=26;horizontalStack=0;resizeParent=1;resizeParentMax=0;collapsible=0;marginBottom=0;fillColor=#d5e8d4;fontStyle=1;" vertex="1" parent="1">
  <mxGeometry x="360" y="40" width="240" height="120" as="geometry" />
</mxCell>
<mxCell id="tbl_progress_fk" value="FK  student_id : BIGINT" style="text;html=1;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;portConstraint=eastwest;rotatable=0;points=[[0,0.5],[1,0.5]];" vertex="1" parent="tbl_progress">
  <mxGeometry y="26" width="240" height="26" as="geometry" />
</mxCell>

<!-- quan hệ FK dạng crow's-foot, nối row-với-row -->
<mxCell id="rel1" style="edgeStyle=entityRelationEdgeStyle;fontSize=12;html=1;endArrow=ERmany;startArrow=ERone;rounded=0;exitX=1;exitY=0.5;entryX=0;entryY=0.5;" edge="1" parent="1" source="tbl_student_pk" target="tbl_progress_fk">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

Dùng đúng tiền tố `PK`/`FK` trong text của row (khớp với cách RDS/SDS mô tả key ở section
Table Description). Tô màu theo nhóm bảng (Auth/Content/Assessment/...) bằng `fillColor`
— dùng lại đúng các nhóm "Nhóm" đã liệt kê trong
`docs/02-SDD-Architecture/database-design/JLPT_database.md`.

---

## 3. Package diagram (RDS I.3.2, SDS I.1)

UML package = shape "folder tab":

```xml
<mxCell id="pkg_auth" value="feature.auth" style="shape=folder;fontStyle=1;spacingTop=10;tabWidth=90;tabHeight=20;tabPosition=left;html=1;whiteSpace=wrap;fillColor=#fff2cc;" vertex="1" parent="1">
  <mxGeometry x="40" y="40" width="180" height="100" as="geometry" />
</mxCell>
<mxCell id="pkg_shared" value="shared.security" style="shape=folder;fontStyle=1;spacingTop=10;tabWidth=90;tabHeight=20;tabPosition=left;html=1;whiteSpace=wrap;fillColor=#f8cecc;" vertex="1" parent="1">
  <mxGeometry x="280" y="40" width="180" height="100" as="geometry" />
</mxCell>
<!-- dependency: mũi tên nét đứt -->
<mxCell id="dep1" style="edgeStyle=orthogonalEdgeStyle;html=1;dashed=1;endArrow=open;" edge="1" parent="1" source="pkg_auth" target="pkg_shared">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

Mỗi `feature/<module>` một box + mỗi `shared/<area>` cấp cao nhất một box (xem danh sách
package thật trong `content-sourcing.md`) — chỉ vẽ mũi tên dependency ở chỗ source code
thật sự import chéo qua package khác (grep `import com.jlpt.shared` /
`import com.jlpt.feature.X` bên trong module `Y` để xác nhận dependency có thật, đừng đoán
mò).

---

## 4. UML class diagram (SDS II.a)

Box class 3 phần (compartment), dựng tay từ một `swimlane` + hai `text` con + một
`line` phân cách, theo đúng cách phân rã shape "Class 3" chuẩn của draw.io:

```xml
<mxCell id="cls_authservice" value="AuthService" style="swimlane;fontStyle=1;childLayout=stackLayout;horizontal=1;startSize=26;horizontalStack=0;resizeParent=0;resizeParentMax=0;collapsible=0;marginBottom=0;html=1;" vertex="1" parent="1">
  <mxGeometry x="40" y="40" width="240" height="150" as="geometry" />
</mxCell>
<mxCell id="cls_authservice_attrs" value="- studentRepository: StudentRepository&#10;- jwtProvider: JwtProvider" style="text;html=1;align=left;verticalAlign=top;spacingLeft=4;spacingRight=4;overflow=hidden;whiteSpace=wrap;" vertex="1" parent="cls_authservice">
  <mxGeometry y="26" width="240" height="46" as="geometry" />
</mxCell>
<mxCell id="cls_authservice_div" value="" style="line;strokeWidth=1;fillColor=none;align=left;verticalAlign=middle;spacingTop=-1;spacingLeft=3;spacingRight=3;rotatable=0;points=[];portConstraint=eastwest;" vertex="1" parent="cls_authservice">
  <mxGeometry y="72" width="240" height="8" as="geometry" />
</mxCell>
<mxCell id="cls_authservice_methods" value="+ login(email, password): AuthResult&#10;+ register(dto): Student" style="text;html=1;align=left;verticalAlign=top;spacingLeft=4;spacingRight=4;overflow=hidden;whiteSpace=wrap;" vertex="1" parent="cls_authservice">
  <mxGeometry y="80" width="240" height="70" as="geometry" />
</mxCell>
```

Edge quan hệ giữa các class box (nối box-với-box, không phải row-với-row như ER):

- Association: `edgeStyle=orthogonalEdgeStyle;html=1;endArrow=open;`
- Dependency: thêm `dashed=1;`
- Inheritance/implements: `endArrow=block;endFill=0;html=1;`
- Composition: `startArrow=diamondThin;startFill=1;endArrow=none;html=1;`
- Aggregation: giống trên nhưng `startFill=0;`

Lấy đúng tên field/method thật từ file `.java` (đọc file thật) — không được đoán ra chữ
ký (signature).

---

## 5. Sequence diagram (SDS II.c)

Mỗi participant = một box tiêu đề + một lifeline nét đứt (một edge có geometry cố định,
không có source/target) chạy dọc xuống trang; message = edge ngang giữa hai lifeline ở
y tăng dần; activation bar = hình chữ nhật mỏng trên lifeline (tuỳ chọn, thêm sau khi luồng
cơ bản đã render đúng).

```xml
<!-- participant -->
<mxCell id="p_student" value="Student" style="rounded=0;whiteSpace=wrap;html=1;" vertex="1" parent="1">
  <mxGeometry x="40" y="40" width="120" height="30" as="geometry" />
</mxCell>
<mxCell id="p_frontend" value="Frontend" style="rounded=0;whiteSpace=wrap;html=1;" vertex="1" parent="1">
  <mxGeometry x="260" y="40" width="120" height="30" as="geometry" />
</mxCell>
<mxCell id="p_backend" value="AuthController" style="rounded=0;whiteSpace=wrap;html=1;" vertex="1" parent="1">
  <mxGeometry x="480" y="40" width="120" height="30" as="geometry" />
</mxCell>
<mxCell id="p_db" value="student_users (DB)" style="rounded=0;whiteSpace=wrap;html=1;" vertex="1" parent="1">
  <mxGeometry x="700" y="40" width="140" height="30" as="geometry" />
</mxCell>

<!-- lifeline: edge dọc nét đứt với điểm cố định, không nối vào cell nào khác -->
<mxCell id="ll_student" style="html=1;endArrow=none;dashed=1;" edge="1" parent="1">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="100" y="70" as="sourcePoint" />
    <mxPoint x="100" y="500" as="targetPoint" />
  </mxGeometry>
</mxCell>
<mxCell id="ll_frontend" style="html=1;endArrow=none;dashed=1;" edge="1" parent="1">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="320" y="70" as="sourcePoint" />
    <mxPoint x="320" y="500" as="targetPoint" />
  </mxGeometry>
</mxCell>
<mxCell id="ll_backend" style="html=1;endArrow=none;dashed=1;" edge="1" parent="1">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="540" y="70" as="sourcePoint" />
    <mxPoint x="540" y="500" as="targetPoint" />
  </mxGeometry>
</mxCell>
<mxCell id="ll_db" style="html=1;endArrow=none;dashed=1;" edge="1" parent="1">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="770" y="70" as="sourcePoint" />
    <mxPoint x="770" y="500" as="targetPoint" />
  </mxGeometry>
</mxCell>

<!-- message: nét liền = gọi đồng bộ, nét đứt = trả kết quả -->
<mxCell id="m1" value="1: submit email/password" style="html=1;endArrow=block;fontSize=11;" edge="1" parent="1">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="100" y="100" as="sourcePoint" />
    <mxPoint x="320" y="100" as="targetPoint" />
  </mxGeometry>
</mxCell>
<mxCell id="m2" value="2: POST /api/auth/login" style="html=1;endArrow=block;fontSize=11;" edge="1" parent="1">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="320" y="140" as="sourcePoint" />
    <mxPoint x="540" y="140" as="targetPoint" />
  </mxGeometry>
</mxCell>
<mxCell id="m3" value="3: findByEmail(email)" style="html=1;endArrow=block;fontSize=11;" edge="1" parent="1">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="540" y="180" as="sourcePoint" />
    <mxPoint x="770" y="180" as="targetPoint" />
  </mxGeometry>
</mxCell>
<mxCell id="m4" value="4: return student row" style="html=1;endArrow=open;dashed=1;fontSize=11;" edge="1" parent="1">
  <mxGeometry relative="1" as="geometry">
    <mxPoint x="770" y="220" as="sourcePoint" />
    <mxPoint x="540" y="220" as="targetPoint" />
  </mxGeometry>
</mxCell>
```

Đánh số từng message theo đúng thứ tự luồng trong nhãn của nó (`1:`, `2:`, ...) — nhờ vậy
dễ đối chiếu với các bước Normal Flow đã viết sẵn trong file `UC-NN-*.md` tương ứng (các
bước đó vốn đã được đánh số theo đúng cách này, ví dụ `UC-01-login.md` có bước 1-12 —
dùng lại nguyên văn text của các bước đó làm nhãn message thay vì viết lại từ đầu).

---

## 6. Screens-flow diagram (RDS I.2.1)

- Screen thường: hình chữ nhật `rounded=0;whiteSpace=wrap;html=1;`.
- Screen pop-up: `ellipse;whiteSpace=wrap;html=1;` (hình elip, đúng theo hướng dẫn của
  template) hoặc `rounded=1;arcSize=30;whiteSpace=wrap;html=1;` (chữ nhật bo góc) nếu dùng
  hình elip cho tên screen thấy không hợp lý.
- Screen nhiều tab: dùng lại shape folder-tab ở §3 (một box hình tab, nhãn tab = tên
  screen, và liệt kê tên các tab bên trong thân box).
- Mũi tên điều hướng, có nhãn là hành động kích hoạt:
  `edgeStyle=orthogonalEdgeStyle;html=1;endArrow=block;` với value kiểu `"Click Login"`.
