# TÀI LIỆU QUẢN TRỊ, VẬN HÀNH VÀ TRIỂN KHAI HỆ THỐNG (DEVOPS MASTER GUIDE)

**Dự án:** Hệ Thống Học Tiếng Nhật JLPT (sakuji.online)  
**Mục đích:** Tài liệu hướng dẫn chi tiết từ lý thuyết đến thực hành, dùng để bàn giao, vận hành hệ thống và bảo vệ đồ án trước hội đồng.

---

## 1. SƠ ĐỒ KIẾN TRÚC TỔNG THỂ (SYSTEM ARCHITECTURE DIAGRAM)

Dưới đây là sơ đồ luồng dữ liệu (Data Flow) và cách các thành phần trong hệ thống giao tiếp với nhau.

```mermaid
flowchart TD
    %% Định nghĩa các tác nhân bên ngoài
    User[Người dùng web\n(Browser/Mobile)]
    Dev[Quản trị viên\n(Máy cá nhân - Windows)]
    Google[Google OAuth2\n(Xác thực Đăng nhập)]

    %% Cloudflare Layer
    subgraph Cloudflare[Cloudflare Tầng Bảo Vệ]
        DNS[DNS Server]
        Proxy[DDoS Protection & SSL]
    end

    %% VPS Layer
    subgraph VPS[Máy chủ Azure VPS - Ubuntu 22.04]
        subgraph Docker[Docker Engine (Mạng nội bộ 127.0.0.1)]
            FE[jlpt-frontend\n(Nginx - Port 80)]
            BE[jlpt-backend\n(Spring Boot - Port 8080)]
            DB[(jlpt-db\nSQL Server - Port 1433)]
            Redis[(jlpt-redis\nRedis - Port 6379)]
        end
        SSHD[SSH Daemon\n(Port 22)]
    end

    %% Luồng người dùng
    User -- "HTTPS (Bảo mật)" --> Proxy
    Proxy -- "HTTP (Đã làm sạch)" --> FE
    FE -- "REST API Call" --> BE
    BE -- "Đọc/Ghi dữ liệu" --> DB
    BE -- "Cache/Session" --> Redis
    
    %% Luồng xác thực Google
    FE -. "Redirect" .-> Google
    Google -. "Callback (JWT)" .-> BE

    %% Luồng quản trị viên (DevOps)
    Dev -- "1. SSH Tunnel (Mã hóa)" --> SSHD
    SSHD -- "2. Trực tiếp" --> DB
    Dev -. "Xem dữ liệu bằng DBeaver" .-> DB

    %% Ghi chú màu sắc
    classDef secure fill:#e8f4f8,stroke:#0366d6,stroke-width:2px;
    classDef db fill:#f0f8ff,stroke:#28a745,stroke-width:2px;
    class Proxy secure;
    class SSHD secure;
    class DB db;
```

---

## 2. DANH SÁCH CÔNG NGHỆ VÀ PHẦN MỀM SỬ DỤNG

Để vận hành hệ thống này, chúng ta sử dụng một bộ "Tech Stack" chuẩn công nghiệp hiện nay:

### 2.1. Tầng Hạ tầng & Mạng (Infrastructure & Network)
1.  **Azure Virtual Machine (VPS):** Máy chủ đám mây chạy hệ điều hành **Ubuntu 22.04 LTS**. Nơi lưu trữ toàn bộ source code và chạy ứng dụng.
2.  **Cloudflare:** Dịch vụ DNS và Reverse Proxy.
    *   *Công dụng:* Ẩn IP thật của VPS để chống bị hacker tấn công trực tiếp (DDoS). Tự động cấp phát chứng chỉ bảo mật SSL (giúp web có biểu tượng ổ khóa HTTPS).

### 2.2. Tầng Ứng dụng & Triển khai (Application & Deployment)
1.  **Docker & Docker Compose:** Nền tảng ảo hóa cấp hệ điều hành (Containerization).
    *   *Công dụng:* Đóng gói mã nguồn cùng toàn bộ thư viện thành các "thùng chứa" (container). Giúp việc đem code từ máy cá nhân lên máy chủ không bao giờ bị lỗi môi trường.
2.  **Nginx (Bên trong Frontend):** Máy chủ web siêu tốc độ, dùng để phục vụ các file tĩnh (HTML, CSS, JS của React) và định tuyến (Route) API xuống Backend.

### 2.3. Tầng Dữ liệu (Database Layer)
1.  **Microsoft SQL Server 2022 (Linux Edition):** Hệ quản trị cơ sở dữ liệu quan hệ mạnh mẽ, được bọc trong Docker để tiết kiệm chi phí mua Azure SQL thô.
2.  **Redis:** Hệ quản trị CSDL in-memory, giúp Backend xử lý Cache và tốc độ cao.

### 2.4. Phần mềm ở Máy cá nhân (Dành cho Quản trị viên)
1.  **Windows PowerShell:** Dùng để gõ lệnh SSH điều khiển máy chủ VPS từ xa và đào đường hầm bảo mật.
2.  **DBeaver (Community Edition):** Phần mềm giao diện (GUI) để xem, sửa, xóa dữ liệu bên trong Database bằng trực quan thay vì gõ lệnh SQL khô khan.

---

## 3. KIẾN THỨC LÝ THUYẾT CỐT LÕI (BẢO VỆ ĐỒ ÁN)

Phần này cung cấp các câu hỏi và câu trả lời mang tính chuyên môn cao, giúp bạn tự tin bảo vệ kiến trúc hệ thống trước hội đồng.

### Câu hỏi 1: Tại sao lại dùng Docker mà không cài trực tiếp (Native) phần mềm lên VPS?
**Trả lời:** Docker mang lại 3 lợi ích khổng lồ:
1.  **Tính nhất quán:** Code chạy được trên máy Dev thì 100% chạy được trên máy chủ. Không có tình trạng xung đột phiên bản Java hay Node.js.
2.  **Dễ dàng bảo trì và di dời:** Chỉ với 1 lệnh `docker compose up`, toàn bộ 4 hệ thống (FE, BE, DB, Redis) sẽ tự động liên kết với nhau. Nếu VPS bị hỏng, việc dời sang VPS mới chỉ mất 5 phút thay vì cả ngày cài cắm lại từ đầu.
3.  **Cách ly an toàn:** Kẻ gian nếu có hack được vào Frontend cũng không thể trực tiếp phá hỏng Database vì chúng nằm ở 2 container (phân vùng ảo) riêng biệt.

### Câu hỏi 2: Tại sao không mở cổng Database (1433) ra ngoài Internet để quản lý cho dễ? SSH Tunnel là gì?
**Trả lời:** 
*   **Nguy cơ:** Việc mở cổng 1433 Public là tối kỵ trong an toàn thông tin (SecOps). Hacker sẽ dùng botnet quét IP và tấn công dò mật khẩu (Brute-force) liên tục, dẫn đến sập máy chủ hoặc bị cài mã độc tống tiền (Ransomware).
*   **Giải pháp (SSH Tunnel):** Chúng ta bít kín cổng 1433, chỉ cho phép nội bộ (localhost) của VPS truy cập. Khi quản trị viên muốn xem dữ liệu, họ sẽ tạo một **Đường hầm SSH (SSH Tunnel)** từ máy tính cá nhân. Đường hầm này mã hóa dữ liệu theo chuẩn quân đội RSA/Ed25519, đâm xuyên qua VPS và lấy dữ liệu nội bộ ra. An toàn tuyệt đối 100%.

### Câu hỏi 3: Vấn đề CORS là gì? Tại sao khi chưa cấu hình đúng thì chức năng "Đăng nhập Google" bị lỗi?
**Trả lời:**
*   CORS (Cross-Origin Resource Sharing) là cơ chế bảo mật của Trình duyệt. Trình duyệt cấm trang web `A` lén lút gọi API lấy dữ liệu của trang web `B` trừ khi trang web `B` cho phép.
*   Khi tích hợp Google OAuth2, Google yêu cầu một URL phản hồi (Redirect URI) cực kỳ nghiêm ngặt. Nếu Backend (chạy ở localhost) và Frontend (chạy ở tên miền sakuji.online) không đồng nhất cấu hình, Google sẽ từ chối trả về Token bảo mật, gây ra lỗi xác thực.

---

## 4. HƯỚNG DẪN VẬN HÀNH VÀ CẬP NHẬT CODE (DÀNH CHO NGƯỜI MỚI)

Mỗi khi bạn sửa code ở máy cá nhân, push lên GitHub và muốn cập nhật lên web `sakuji.online`, hãy làm đúng từng bước chậm rãi như sau:

**Bước 1: Kết nối vào máy chủ VPS**
1. Mở phần mềm **PowerShell** (hoặc Terminal) trên máy tính cá nhân của bạn.
2. Gõ lệnh kết nối: `ssh jlptadmin@135.149.56.179` và nhấn Enter.
3. Khi hệ thống hỏi mật khẩu, hãy nhập mật khẩu VPS vào (lưu ý: khi gõ mật khẩu trên Linux, màn hình sẽ không hiện ra dấu sao `*` nào cả, cứ tự tin gõ đúng và nhấn Enter).

**Bước 2: Nâng cấp quyền và di chuyển tới thư mục Code**
1. Để thao tác với Docker, bạn phải có quyền tối cao (root). Gõ lệnh này:
   ```bash
   sudo su -
   ```
   *(Nhập lại mật khẩu VPS một lần nữa nếu được hỏi).*
2. Khi thấy dấu nhắc lệnh đổi thành `root@jlpt-vps:~#`, hãy gõ lệnh để chui vào thư mục chứa dự án:
   ```bash
   cd /opt/Japanese-Skill-Practice-Platform
   ```

**Bước 3: Kéo code mới và Tái khởi động hệ thống**
1. Gõ lệnh tải code mới nhất từ GitHub về:
   ```bash
   git pull origin main
   ```
2. Đóng gói lại hệ thống (Lệnh này mất khoảng 1-2 phút, cứ để nó chạy kệ nó):
   ```bash
   docker compose build --no-cache
   ```
3. Sau khi build xong (báo `DONE`), khởi chạy lại toàn bộ hệ thống:
   ```bash
   docker compose up -d
   ```
4. Khi thấy màn hình báo 4 dòng `✔ Container ... Started / Running` màu xanh lá cây là bạn đã cập nhật web thành công!

---

## 5. HƯỚNG DẪN CẦM TAY CHỈ VIỆC: KẾT NỐI DATABASE BẰNG DBEAVER

Đây là hướng dẫn **cầm tay chỉ việc từng nút bấm** để kết nối an toàn từ máy tính ở nhà vào thẳng Database đang bị khóa kín bên trong VPS.

### Bước 1: Tự đào "Đường hầm" bằng PowerShell (Cực kỳ quan trọng)
*Tuyệt đối không dùng tính năng SSH Tunnel có sẵn của phần mềm DBeaver vì nó rất hay bị kẹt trên Windows. Chúng ta sẽ đào hầm thủ công.*

1. Mở phần mềm **PowerShell** (màu xanh/đen) trên máy tính Windows của bạn (mở một cửa sổ mới tinh).
2. Copy chính xác dòng lệnh sau dán vào và nhấn Enter:
   ```powershell
   ssh -L 14330:127.0.0.1:14330 jlptadmin@135.149.56.179
   ```
   > **Sửa 11/07/2026 (P0.3):** cổng đích ở phía VPS phải là `14330`, KHÔNG phải `1433` — `docker-compose.yml` chỉ map `"14330:1433"` (host:container), nghĩa là chính VPS chỉ có cổng `14330` thực sự đang lắng nghe (`ss -tlnp` xác nhận); cổng `1433` chỉ tồn tại nội bộ trong mạng Docker, không có gì lắng nghe ở `127.0.0.1:1433` trên VPS. Dùng nhầm `1433` ở đây sẽ khiến tunnel "thông" (không báo lỗi) nhưng không kết nối được tới database — đúng loại lỗi từng gây nhầm lẫn thật khi cấu hình DataGrip/DBeaver.
3. Nếu nó hỏi `Are you sure you want to continue connecting (yes/no)?`, hãy gõ chữ `yes` và nhấn Enter.
4. Nhập mật khẩu VPS của bạn vào.
5. Khi bạn thấy dòng chữ `jlptadmin@jlpt-vps:~$` hiện ra, tức là đường hầm đã THÔNG!
6. **LƯU Ý SỐNG CÒN:** TUYỆT ĐỐI KHÔNG bấm dấu `X` tắt cái cửa sổ PowerShell này đi. Hãy bấm dấu `-` để **thu nhỏ (minimize)** nó xuống thanh Taskbar. Đường hầm của bạn đang chạy ngầm ở cổng `14330`!

### Bước 2: Cấu hình phần mềm DBeaver (Chỉ 10 giây)
1. Mở phần mềm DBeaver lên. Ở góc trên cùng bên trái, bấm vào biểu tượng **Phích cắm có dấu +** (New Database Connection).
2. Chọn biểu tượng **Microsoft SQL Server** to đùng màu xanh đậm -> Bấm **Next**.
3. Tại tab **Main** (màn hình cấu hình chính), bạn điền CHÍNH XÁC từng chữ như sau:
   * **Host:** Xóa chữ `localhost` đi, gõ vào `127.0.0.1`
   * **Port:** Gõ thêm số `0` vào cuối để thành **`14330`** (Đây chính là cái hầm nãy bạn vừa đào).
   * **Database/Schema:** Xóa chữ `master` đi, gõ vào `JLPT_LearningDB`
   * **Username:** gõ `sa`
   * **Password:** gõ đúng giá trị `MSSQL_SA_PASSWORD` hiện tại trong file `.env` trên VPS (`/opt/Japanese-Skill-Practice-Platform/.env`) — **không có mật khẩu cố định**, và tuyệt đối không copy mật khẩu cũ đã lưu trong DBeaver/DataGrip của bạn nếu mật khẩu trên VPS vừa được đổi (xem lưu ý ở mục 6 tài liệu `CI_CD.md` về rủi ro `.env` lệch với dữ liệu đã khởi tạo)
4. Tích chọn ô **Save password**.
5. Nhìn xuống dưới cùng của bảng, tích chọn vào ô **Trust Server Certificate**.
6. **LƯU Ý:** Không được bấm sang tab SSH. Để nguyên tab Main.

### Bước 3: Test và Chiêm ngưỡng thành quả
1. Bấm nút **Test Connection...** ở góc dưới cùng bên trái.
2. Nếu nó hỏi tải Driver thì cứ bấm **Download**.
3. Nếu nó hiện ra một cái bảng nhỏ có chữ **Connected (màu xanh lá cây)**, xin chúc mừng, bạn đã thành công 100%!
4. Bấm **OK** ở bảng Connected, rồi bấm **Finish (hoặc OK)** ở bảng to để lưu lại cấu hình.

### Bước 4: Hướng dẫn xem Dữ liệu (Bảng student_users)
1. Ở danh sách cột bên trái của DBeaver, **nháy đúp chuột** vào kết nối `JLPT_LearningDB`.
2. Bấm vào các mũi tên (`>`) để mở rộng cây thư mục theo ĐÚNG THỨ TỰ sau:
   👉 `JLPT_LearningDB` -> `Schemas` -> `dbo` -> `Tables`
3. Kéo xuống tìm cái bảng có tên là **`student_users`** rồi **nháy đúp chuột** vào nó.
4. Ở màn hình to vừa bung ra bên phải (thường mặc định là tab *Properties* hiển thị các cột), bạn nhìn lên phía trên, chuyển sang tab **Data (Dữ liệu)**.
5. BÙM! 💥 Danh sách tài khoản người dùng đang chạy trên web thật của bạn đã hiện ra.
6. Mỗi khi có người dùng mới đăng ký, bạn chỉ việc chuyển sang tab Data này và bấm **F5**, dữ liệu mới nhất sẽ nhảy ra tức thời (Real-time).

---
*Tài liệu này được biên soạn bằng tâm huyết, đúc kết từ quá trình debug thực tế 1-1. Hãy chia sẻ cho các thành viên trong nhóm để cùng nhau quản trị hệ thống tốt nhất!*
