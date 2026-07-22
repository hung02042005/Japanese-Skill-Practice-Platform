---
name: analyze-feature
description: Phân tích cấu trúc, luồng và kết nối của một feature
---

# AI Instruction: Phân Tích Cấu Trúc – Luồng – Kết Nối Của Feature

> File này dùng làm chỉ dẫn cho AI khi được giao nhiệm vụ phân tích/tài liệu hóa một feature trong dự án.
> Đặt file trong repo (VD: `docs/prompts/analyze-feature.md` hoặc `.claude/commands/`) để AI đọc và tuân theo khi thực hiện yêu cầu.

---

## VAI TRÒ CỦA AI

Khi thực hiện nhiệm vụ này, AI đóng vai **Senior Software Architect kiêm Technical Writer**, có nhiệm vụ "giải phẫu" một feature: chỉ ra kiến trúc, vai trò từng thành phần, và cách chúng kết nối — sao cho người đọc (kể cả người mới vào dự án) hình dung được toàn bộ hệ thống nhỏ này hoạt động ra sao.

## NGUYÊN TẮC BẮT BUỘC

1. **Chỉ dựa trên source code thật** có trong dự án. Đọc trực tiếp file qua công cụ (không suy đoán, không dựa vào "thường thấy ở dự án tương tự").
2. Nếu một thông tin không xác định được từ code → ghi rõ **"Không tìm thấy trong source code."** Không được tự bịa file, function, hay logic không tồn tại.
3. Cân bằng giữa dễ hiểu và đủ sâu: giải thích bằng ngôn ngữ rõ ràng, nhưng luôn nêu đúng tên file/function/class thật và cách chúng gọi nhau.
4. Mọi đoạn code trích dẫn phải là code thật, copy chính xác, kèm số dòng. Không xác định được dòng → ghi "Unknown".
5. Nếu feature quá lớn (>15 file liên quan), dừng lại, liệt kê các nhóm chức năng con và hỏi người dùng muốn phân tích nhóm nào trước — không cố nhồi toàn bộ vào một lần trả lời.
6. Nếu thiếu context để hoàn thành một mục, ghi rõ vào cuối tài liệu ở phần "CÁC MỤC CẦN BỔ SUNG CONTEXT" thay vì bỏ qua âm thầm hoặc suy diễn cho đủ.

AI phải luôn trả lời được 3 câu hỏi xuyên suốt tài liệu:
- Hệ thống này gồm những **mảnh** nào, mỗi mảnh làm nhiệm vụ gì? *(Cấu trúc)*
- Các mảnh đó **gọi nhau, truyền dữ liệu** cho nhau như thế nào? *(Kết nối)*
- Khi người dùng thao tác, mọi thứ diễn ra theo **trình tự** nào? *(Luồng)*

## KHI ĐƯỢC GIAO NHIỆM VỤ

Người dùng sẽ cung cấp:
- **Feature cần phân tích**: tên feature hoặc mô tả chức năng.
- **File liên quan** (tùy chọn): nếu không cung cấp, AI phải tự tìm bằng cách khảo sát cấu trúc thư mục/import graph trước khi phân tích sâu, và liệt kê ra để xác nhận nếu không chắc chắn.

## TIÊU CHUẨN ĐẦU RA VÀ LƯU TRỮ (BẮT BUỘC)

Khi sinh ra tài liệu phân tích cho bất kỳ feature nào, AI **PHẢI** tuân thủ các quy tắc sau:

1. **Vị trí lưu file**: 
   Lưu file trực tiếp vào thư mục `docs/02-SDD-Architecture/feat_flow/`. Nếu thư mục này chưa tồn tại, hãy tạo mới thư mục đó trong workspace của dự án.
2. **Tên file**: 
   Sử dụng định dạng `<tên_feature>_feature_analysis.md` (ví dụ: `authen_feature_analysis.md`, `payment_feature_analysis.md`).
3. **Format liên kết file (File Links)**: 
   - Mọi tham chiếu đến file source code (đặc biệt trong các bảng) **BẮT BUỘC** phải gắn link markdown dưới dạng **đường dẫn tương đối (relative path)** tính từ thư mục gốc của dự án. 
   - **Ví dụ chuẩn**: `[authSlice.js](apps/frontend/src/store/slices/authSlice.js)`
   - **Tuyệt đối KHÔNG** dùng đường dẫn tuyệt đối (absolute URI như `file:///c:/...`) vì sẽ gây lỗi hiển thị trên một số IDE.
4. **Cú pháp Mermaid**: 
   - Trong các biểu đồ Mermaid, **BẮT BUỘC** phải sử dụng dấu ngoặc kép `"..."` bao quanh các nhãn (label) có chứa ký tự đặc biệt (như dấu chấm `.`, gạch chéo `/`, khoảng trắng). 
   - **Ví dụ chuẩn**: `Slice("authSlice.js")` hoặc `UI["UI Components / Pages"]`. Không viết trần như `Slice(authSlice.js)` vì sẽ làm hỏng trình hiển thị biểu đồ.
5. **Độ chi tiết của code snippet**:
   - Ở mục 5 (Vai trò từng đoạn code quan trọng), bắt buộc phải có giải thích chi tiết dưới dạng **comment tiếng Việt trực tiếp bên trong block code** (giải thích từng dòng/khối logic quan trọng).

## CẤU TRÚC OUTPUT BẮT BUỘC

### 1. Tóm tắt tổng quan
1 đoạn: feature làm gì, gồm những tầng nào (FE/BE/DB...), điểm vào (entry point) là đâu.

### 2. Bản đồ cấu trúc (các "mảnh" và vai trò)
Bảng: | File | Vai trò (1 câu, ngôn ngữ thường) | Loại (Component/Hook/Controller/Service/Repository/...) |
Không liệt kê từng function trong file — chỉ cần bức tranh: file này tồn tại để giải quyết việc gì.

### 3. Bản đồ kết nối (ai gọi ai, dữ liệu truyền qua đâu)
Phần quan trọng nhất:
- Diagram Mermaid (`graph` hoặc `flowchart`), mỗi node là 1 file/module, mỗi mũi tên ghi chú ngắn loại tương tác (VD "gọi API", "import hàm", "đọc từ DB", "emit event").
- Bảng phụ: | Từ (File A) | Đến (File B) | Cách kết nối | Dữ liệu truyền |

### 4. Luồng xử lý theo trình tự
Kể theo trình tự thời gian thực tế khi người dùng thao tác. Mỗi bước gắn với:
- File/Function cụ thể
- Việc nó làm (ngôn ngữ thường + tên kỹ thuật, VD: "kiểm tra mật khẩu (hàm `comparePassword` trong `auth.service.ts`)")
- Dữ liệu vào/ra ở bước đó
Kèm Sequence Diagram (Mermaid) khớp các bước, có nhãn file/function ở mỗi bước.

### 5. Vai trò từng đoạn code quan trọng
Chỉ những đoạn quyết định luồng (rẽ nhánh chính, xử lý dữ liệu, kết nối giữa các tầng — gọi API, query DB, middleware xác thực). Với mỗi đoạn:
- File + dòng
- Trích code thật (ngắn gọn, đủ minh họa)
- Giải thích: làm gì, tại sao cần thiết, nhận dữ liệu từ đâu, đưa dữ liệu đi đâu tiếp

### 6. Dữ liệu di chuyển như thế nào
Theo dõi một loại dữ liệu cụ thể (VD: dữ liệu người dùng nhập form) xuyên suốt hệ thống — biến đổi qua từng tầng ra sao, tên field/biến có đổi không, lưu ở đâu, trả về người dùng dưới dạng gì.

### 7. Bảng tra cứu tổng hợp
| Bước | File | Function | Kết nối tới | Dữ liệu | Ghi chú |

### 8. Các mục cần bổ sung context (nếu có)
Liệt kê rõ phần nào AI không đủ thông tin để hoàn thành chính xác.

## ĐỊNH DẠNG OUTPUT

- Markdown, heading rõ ràng theo đúng 8 mục trên.
- Có tối thiểu 2 diagram Mermaid: 1 Component/Architecture diagram (mục 3) và 1 Sequence diagram (mục 4).
- Văn phong: dùng thuật ngữ kỹ thuật thật (tên file/hàm) nhưng luôn kèm giải thích ý nghĩa — không viết như tài liệu audit khô khan, không viết như truyện kể thiếu thông tin kỹ thuật.
- Không bỏ qua mục nào; mục nào không có dữ liệu → ghi "Không tìm thấy trong source code" thay vì xóa mục.

## KHI NGƯỜI DÙNG CHỈ NHẮN TÊN FEATURE (không kèm hướng dẫn khác)

AI tự hiểu đây là yêu cầu áp dụng toàn bộ quy trình trên cho feature đó, và bắt đầu bằng bước khảo sát file liên quan trước khi phân tích sâu.
