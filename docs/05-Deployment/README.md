# TÀI LIỆU TRIỂN KHAI CLOUD NATIVE (DEVOPS MASTER GUIDE)

**Dự án:** Hệ Thống Học Tiếng Nhật JLPT (sakuji.online)  
**Mục đích:** Tài liệu hướng dẫn chi tiết kiến trúc phân tán (Cloud Native) Serverless để vận hành hệ thống miễn phí vĩnh viễn, không cần quản lý máy chủ vật lý hay VPS.

---

## 1. SƠ ĐỒ KIẾN TRÚC TỔNG THỂ (SYSTEM ARCHITECTURE DIAGRAM)

Khác với kiến trúc cũ (chạy tất cả trên 1 VPS bằng Docker Compose), kiến trúc mới chia cắt hoàn toàn Frontend, Backend và Database lên các dịch vụ đám mây chuyên biệt.

```mermaid
flowchart TD
    %% Định nghĩa các tác nhân
    User[Người dùng web\n(Browser/Mobile)]
    Dev[Quản trị viên / Dev]
    GitHub[GitHub Repository\n(Nơi chứa source code)]

    %% Cloudflare Pages (Frontend)
    subgraph Frontend[Tầng Trình Diễn - Cloudflare Pages]
        CF_CDN[Mạng phân phối toàn cầu CDN]
        React[Website ReactJS]
    end

    %% Render (Backend PaaS)
    subgraph Backend[Tầng Xử Lý - Render PaaS]
        API[Spring Boot REST API]
        Storage[Cloudinary / Firebase\n(Lưu trữ Ảnh Upload)]
    end

    %% Aiven (Database)
    subgraph DB_Layer[Tầng Dữ Liệu - Aiven DBaaS]
        MySQL[(MySQL 8 Database)]
    end

    %% Luồng CI/CD (Tự động)
    Dev -- "Code Push" --> GitHub
    GitHub -- "Webhook (Tự động build Frontend)" --> CF_CDN
    GitHub -- "Webhook (Tự động build Backend từ Dockerfile)" --> API

    %% Luồng dữ liệu (Runtime)
    User -- "Truy cập sakuji.online" --> CF_CDN
    CF_CDN -- "Tải giao diện" --> React
    React -- "Gọi HTTP REST API" --> API
    API -- "Truy vấn dữ liệu" --> MySQL
    API -- "Lưu/Lấy ảnh" --> Storage

    %% Ghi chú màu sắc
    classDef git fill:#24292e,color:#fff,stroke-width:0px;
    classDef cloudflare fill:#f38020,color:#fff,stroke-width:0px;
    classDef render fill:#4628db,color:#fff,stroke-width:0px;
    classDef aiven fill:#ff3559,color:#fff,stroke-width:0px;
    
    class GitHub git;
    class CF_CDN,React cloudflare;
    class API render;
    class MySQL aiven;
```

---

## 2. DANH SÁCH NHÀ CUNG CẤP ĐÁM MÂY (CLOUD PROVIDERS)

Kiến trúc này tận dụng tối đa các gói Free Tier trọn đời của các dịch vụ chuyên biệt:

1.  **Cloudflare Pages (Dành cho Frontend):**
    *   *Nhiệm vụ:* Lưu trữ các file tĩnh (HTML, CSS, JS) của React và phát chúng đến người dùng qua mạng CDN toàn cầu.
    *   *Ưu điểm:* Miễn phí hoàn toàn băng thông. Tốc độ nạp trang siêu tốc. 
2.  **Render.com (Dành cho Backend):**
    *   *Nhiệm vụ:* Nền tảng PaaS (Platform as a Service) kéo code từ GitHub, dùng Dockerfile của ta để đóng gói và chạy Spring Boot.
    *   *Giới hạn:* Gói Free chỉ có 512MB RAM và sẽ "ngủ đông" nếu không có ai gọi API trong 15 phút. Spring Boot đã được tối ưu (`-Xmx256m`) để không bị sập.
3.  **Aiven (Dành cho Database):**
    *   *Nhiệm vụ:* Dịch vụ DBaaS (Database as a Service) cung cấp sẵn một máy chủ MySQL 8 được cấu hình bảo mật hoàn chỉnh.
    *   *Đặc điểm:* Cung cấp sẵn chuỗi kết nối (`jdbc:mysql://...`), dung lượng 5GB miễn phí.

---

## 3. LỢI ÍCH CỦA KIẾN TRÚC MỚI (TRẢ LỜI CÂU HỎI BẢO VỆ ĐỒ ÁN)

### Câu hỏi 1: Tại sao lại bỏ mô hình Docker Compose trên VPS cũ để chuyển sang kiến trúc này?
**Trả lời:** Có 3 lý do chính:
1.  **Về chi phí:** Chạy Spring Boot và MySQL trên cùng 1 VPS đòi hỏi máy chủ ít nhất 4GB RAM (Khoảng 20-25$/tháng). Mô hình Cloud Native mới chia cắt các thành phần ra các dịch vụ miễn phí, giúp giảm chi phí hệ thống xuống còn 0đ.
2.  **Khả năng mở rộng (Scalability):** Nếu lượng người dùng tăng đột biến, ta chỉ cần "nhấn nút" nâng cấp gói dịch vụ ở đúng cái nút thắt (Ví dụ chỉ nâng cấp Backend trên Render) mà không ảnh hưởng gì tới Database hay Frontend.
3.  **Bảo trì bằng số không (Zero-Ops):** Không còn phải lo lắng về việc quản lý Linux, cập nhật hệ điều hành, hay cấu hình Firewall chống DDoS. Nhà cung cấp đám mây tự lo phần đó.

### Câu hỏi 2: CI/CD tự động hoạt động như thế nào trong kiến trúc này?
**Trả lời:** 
Chúng ta áp dụng triệt để nguyên lý GitOps: GitHub đóng vai trò là "Chân lý" (Source of Truth). 
Mỗi khi lập trình viên gõ lệnh `git push` lên nhánh `main`, GitHub sẽ lập tức "bắn tín hiệu" (Webhook) cho Cloudflare Pages và Render. Hai máy chủ này sẽ tự động tải đoạn code mới nhất về và thay thế bản cũ mà không cần can thiệp bằng tay.

---

## 4. GHI CHÚ VỀ BẢO MẬT VÀ LƯU TRỮ TỆP TIN

Do chạy trên nền tảng Serverless / PaaS, hệ thống **không lưu trữ được file cục bộ** (Ephemeral Filesystem). 
*   **Vấn đề:** Nếu người dùng tải ảnh đại diện lên, ảnh đó sẽ bị mất mỗi khi Render khởi động lại.
*   **Giải pháp:** Bắt buộc tích hợp dịch vụ lưu trữ đám mây của bên thứ ba như **Cloudinary** hoặc **Firebase Storage** hoặc **AWS S3**. Backend chỉ làm nhiệm vụ nhận file và đẩy thẳng lên Cloudinary, sau đó lưu lại đường link (URL) của bức ảnh vào trong MySQL.

*(Sơ đồ đã được cập nhật để phản ánh luồng lưu trữ file này ở phần 1).*
