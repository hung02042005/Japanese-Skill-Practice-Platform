# Cẩm Nang Lệnh Docker & Docker Compose (Cheatsheet)

Tài liệu này tổng hợp toàn bộ các câu lệnh Docker từ cơ bản đến nâng cao, được giải thích theo ngữ cảnh thực tế của dự án JLPT E-Learning. Bạn có thể dùng tài liệu này như một cuốn "từ điển" mỗi khi cần thao tác với Server.

---

## Phần 1: Các lệnh `docker-compose` (Sử dụng 90% thời gian)
Đây là các lệnh quản lý toàn bộ hệ thống (gồm cả Frontend, Backend, Database) cùng một lúc. Các lệnh này **bắt buộc phải chạy ở thư mục có chứa file `docker-compose.yml`**.

| Lệnh | Chức năng & Khi nào cần dùng |
|------|------------------------------|
| `docker-compose up -d` | **Bật hệ thống ngầm:** Dùng khi bạn vừa khởi động lại máy hoặc muốn chạy dự án mà không bị chiếm cửa sổ Terminal. Chữ `-d` (detach) nghĩa là chạy ngầm. |
| `docker-compose up -d --build` | **Cập nhật Code mới:** Dùng khi bạn **vừa sửa code** Frontend/Backend hoặc đổi cấu hình trong `.env`. Lệnh này ép Docker phải "build" (đóng gói lại) những phần bị thay đổi rồi mới chạy. |
| `docker-compose down` | **Tắt và dọn dẹp hệ thống:** Dùng khi bạn muốn tắt hoàn toàn dự án. Lệnh này sẽ dừng các container và xóa mạng lưới (network) kết nối chúng. Rất an toàn, không làm mất dữ liệu. |
| `docker-compose stop` | **Tạm dừng hệ thống:** Dùng khi bạn muốn dừng hệ thống tạm thời nhưng không xóa network. |
| `docker-compose restart` | **Khởi động lại toàn bộ:** Dùng khi hệ thống bị treo hoặc bạn muốn reset nhanh lại mọi thứ. |
| `docker-compose logs -f` | **Xem màn hình console (Log):** Dùng khi bạn muốn xem ai đang truy cập web, hoặc tại sao API bị lỗi 500. Chữ `-f` (follow) giúp log tự động cuộn xuống khi có dòng mới cập nhật. |
| `docker-compose logs -f backend` | **Xem log của riêng Backend:** Dùng khi bạn chỉ muốn soi lỗi của Spring Boot mà không muốn bị nhiễu bởi log của Frontend. |

---

## Phần 2: Các lệnh `docker` cơ bản (Quản lý Container)
Đây là các lệnh thao tác trực tiếp lên từng "hộp" (container) riêng lẻ. Dùng khi bạn muốn can thiệp sâu.

| Lệnh | Chức năng & Khi nào cần dùng |
|------|------------------------------|
| `docker ps` | **Xem cái gì đang chạy:** Hiển thị danh sách các Container đang hoạt động, kèm theo trạng thái (Up/Exited) và cổng (Port) đang mở. Rất hay dùng để kiểm tra xem Backend có đang bị sập không. |
| `docker ps -a` | **Xem TẤT CẢ Container:** Hiển thị cả những cái đang chạy lẫn những "xác" Container đã bị tắt (giống cái frontend màu xám bạn thấy lúc nãy). |
| `docker stop <tên_container>` | **Tắt 1 hộp cụ thể:** Ví dụ `docker stop jlpt-backend`. Dùng khi chỉ muốn tắt mỗi Backend để nâng cấp, để nguyên Frontend chạy. |
| `docker start <tên_container>`| **Bật 1 hộp cụ thể:** Dùng để bật lại container vừa bị stop. |
| `docker rm <tên_container>` | **Xóa 1 hộp cụ thể:** Xóa bỏ hoàn toàn "xác" container đã tắt. (Chỉ xóa được khi nó đã bị stop). |

---

## Phần 3: Thao tác sâu vào bên trong Container
Bạn có bao giờ thắc mắc: "Code của mình chạy vào Docker rồi thì nó nằm ở đâu, làm sao mở thư mục bên trong Docker ra xem?" Đây là các lệnh dành cho việc đó:

| Lệnh | Chức năng & Khi nào cần dùng |
|------|------------------------------|
| `docker exec -it jlpt-backend /bin/sh` | **Chui vào trong máy ảo Backend:** Lệnh này mở một terminal Linux "bên trong" container Backend. Dùng khi bạn muốn kiểm tra xem file `.jar` có đúng ở đó không, hoặc xem file ảnh upload có vào đúng thư mục không. (Để thoát ra, gõ `exit`). |
| `docker exec -it jlpt-frontend /bin/sh`| **Chui vào máy ảo Frontend:** Dùng để kiểm tra xem Nginx cấu hình đúng chưa, file build React nằm ở đâu. |

---

## Phần 4: Quản lý ổ đĩa và Dọn dẹp Rác (Cực kỳ quan trọng)
Docker sau một thời gian dài sử dụng (build đi build lại nhiều lần) sẽ sinh ra rất nhiều file tạm và "xác" cũ gây đầy ổ cứng.

| Lệnh | Chức năng & Khi nào cần dùng |
|------|------------------------------|
| `docker images` | **Xem các file đóng gói:** Liệt kê toàn bộ các Image đã tải hoặc đã build trên máy tính. Kèm theo dung lượng của chúng. |
| `docker rmi <tên_image>` | **Xóa 1 Image:** Xóa các image cũ không còn dùng để giải phóng ổ cứng. |
| `docker volume ls` | **Xem danh sách ổ cứng ảo:** Dùng để kiểm tra ổ cứng lưu trữ file upload `jlpt-uploads` của dự án. |
| `docker system prune` | **🧹 Nút Dọn Rác Thần Thánh:** Dùng khi máy báo đầy bộ nhớ. Lệnh này dọn sạch toàn bộ các Container đã tắt, các Image rác (dangling) và Cache không dùng tới. Giúp lấy lại hàng Gigabyte dung lượng trống. |
| `docker system prune -a --volumes`| **Xóa sạch sành sanh (Nguy hiểm):** Xóa trắng toàn bộ Docker về như lúc mới cài (Bao gồm cả dữ liệu Database và file upload nếu bạn không cẩn thận). Chỉ dùng khi muốn đập đi xây lại từ con số 0. |

---

## 🎯 Quy trình chuẩn khi Làm việc hàng ngày:

**Tình huống 1: Mở máy tính lên làm việc**
👉 Gõ: `docker-compose up -d`

**Tình huống 2: Code xong một tính năng mới ở Backend/Frontend và muốn kiểm tra**
👉 Gõ: `docker-compose up -d --build`

**Tình huống 3: Thấy web chạy lỗi, muốn xem tại sao**
👉 Gõ: `docker-compose logs -f`

**Tình huống 4: Làm việc xong, tắt máy tính đi ngủ**
👉 Gõ: `docker-compose down` (hoặc cứ tắt máy luôn cũng được, Docker sẽ tự tắt).
