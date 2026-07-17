# TÀI LIỆU HƯỚNG DẪN TRIỂN KHAI (DEPLOYMENT)

**Dự án:** Hệ Thống Học Tiếng Nhật JLPT (sakuji.online)  
**Kiến trúc:** Triển khai nguyên khối qua Docker Compose trên máy chủ ảo (Cloud Server / VPS).

---

## Danh Mục Tài Liệu

Bộ tài liệu Deploy đã được dọn dẹp và làm sạch nhằm mang lại các chỉ dẫn chuẩn xác nhất. Vui lòng tham khảo các File sau:

👉 [**Hướng Dẫn Cấu Hình CloudFly VPS (CloudFly_VPS_Deployment_Guide.md)**](./CloudFly_VPS_Deployment_Guide.md)
*Chứa cấu hình tạo file `.env`, Setup Google Auth, Gửi Email, Tính năng Staging & Rollback.*

👉 [**Sơ Đồ Hệ Thống Và Luồng CI/CD (Deploy_Diagram.md)**](./Deploy_Diagram.md)
*Sơ đồ chi tiết giải thích quá trình Code đi từ máy Dev -> Môi trường Staging -> Môi trường Production kèm Check Uptime Server.*

👉 [**Cẩm Nang Lệnh Docker (Docker_Cheatsheet.md)**](./Docker_Cheatsheet.md)
*Từ điển tra cứu nhanh các lệnh `docker` / `docker-compose` khi thao tác trên VPS (bật/tắt, xem log, build lại, quản lý container).*

---

## Kiến Trúc Triển Khai Chạy Thực Tế

Hệ thống có một môi trường giả lập (Staging) cùng chạy song song với môi trường thực (Production) trên cùng 1 con VPS (IP: `222.255.181.207`), với hệ quản trị CSDL là **MySQL 8.4**.

Bạn có thể xem chi tiết ở [Sơ đồ đầy đủ tại đây](./Deploy_Diagram.md).
