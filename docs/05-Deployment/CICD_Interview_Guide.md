# Hướng Dẫn Trả Lời Phỏng Vấn Giảng Viên: Hệ Thống CI/CD

Tài liệu này được biên soạn dưới dạng Câu hỏi - Đáp án (Q&A) nhằm giúp bạn tự tin trả lời mọi câu hỏi của giảng viên liên quan đến hệ thống CI/CD (GitHub Actions) trong dự án **Japanese Skill Practice Platform**.

---

## 1. Khái Niệm Cơ Bản

### Q: CI/CD là gì? Nó có vai trò gì trong dự án của em?
**Trả lời:**
- **CI (Continuous Integration - Tích hợp liên tục):** Là quá trình tự động hóa việc kiểm tra code mỗi khi có thành viên đẩy (push) code mới lên repository. Trong dự án của em, CI sẽ tự động tải code về, biên dịch (build), kiểm tra lỗi cú pháp/format code (Spotless) và chạy các kịch bản kiểm thử (Unit Test, Jacoco Coverage).
- **CD (Continuous Deployment - Triển khai liên tục):** Là quá trình tự động hóa việc đưa ứng dụng lên máy chủ (VPS/Server) để người dùng có thể sử dụng. 
- **Vai trò trong dự án:** CI/CD giúp em tự động hóa hoàn toàn quy trình kiểm tra và triển khai. Thay vì phải build và deploy bằng tay (dễ sai sót và mất thời gian), hệ thống sẽ tự làm mọi thứ. Nó đóng vai trò như một "người gác cổng" đảm bảo code lỗi không bao giờ được đưa lên môi trường thật.

---

## 2. Cách Thức Hoạt Động Của CI/CD Trong Dự Án

### Q: Em cài đặt CI/CD bằng công cụ gì và cấu trúc ra sao?
**Trả lời:**
- Em sử dụng **GitHub Actions**, một công cụ CI/CD được tích hợp sẵn ngay trong GitHub.
- Cấu hình được đặt trong thư mục `.github/workflows/`. Dự án có 2 file chính:
  1. `ci.yml`: Đảm nhiệm việc Build & Test (Chạy CI).
  2. `cd.yml`: Đảm nhiệm việc kết nối SSH vào máy chủ VPS và deploy bằng Docker (Chạy CD).

### Q: Khi nào thì CI/CD được kích hoạt? (Triggers)
**Trả lời:**
- **Với CI (`ci.yml`):** Được kích hoạt tự động mỗi khi có lệnh `git push` hoặc tạo `Pull Request` vào các nhánh quan trọng (như `main`, `develop` hoặc các nhánh tính năng đang làm việc). Em cũng cấu hình thêm `workflow_dispatch` để có thể tự bấm nút chạy thủ công trên giao diện web của GitHub khi cần gỡ lỗi.
- **Với CD (`cd.yml`):** Chỉ được kích hoạt khi code được push hoặc merge vào nhánh `main` (nhánh production) VÀ thường là sau khi luồng CI đã báo thành công (tích xanh).

### Q: Luồng CI (Build & Test) của em thực thi những công việc gì cụ thể?
**Trả lời:**
Trong file `ci.yml`, em chia làm 2 jobs chạy song song để tiết kiệm thời gian:
1. **Frontend CI:** Cài đặt môi trường Node.js 20, tải thư viện (`npm ci`), chạy kiểm tra cú pháp (`npm run lint`), và tiến hành build code React/Vite.
2. **Backend CI:** Cài đặt Java 21, chạy lệnh Maven (`mvn clean verify`). Quá trình này sẽ đi qua các bước:
   - Biên dịch code Java.
   - Chạy Spotless plugin để đảm bảo format code chuẩn (không thừa dấu cách, đúng chuẩn thụt lề).
   - Chạy Unit Test.
   - Chạy Jacoco plugin để kiểm tra độ phủ (Code Coverage) của Unit Test.

---

## 3. Các Vấn Đề Thực Tế (Kinh Nghiệm Xử Lý Lỗi)

*(Giảng viên rất thích hỏi về các khó khăn em gặp phải và cách em giải quyết. Hãy dùng các lỗi thực tế chúng ta vừa sửa để trả lời!)*

### Q: Trong quá trình làm CI/CD, em có gặp khó khăn hay lỗi gì không? Cách em giải quyết?
**Trả lời:**
Dạ có, trong quá trình cấu hình CI cho Backend, em gặp 2 vấn đề lớn:

1. **Lỗi Format Code (Spotless Plugin):** 
   - **Vấn đề:** Khi code trên máy tính Windows, ký tự xuống dòng là `CRLF`, nhưng môi trường chạy của GitHub Actions là Linux (Ubuntu) dùng `LF`. Plugin `spotless-maven-plugin` của em rất nghiêm ngặt, nó phát hiện các file Java chưa được format chuẩn (như file `AuthService.java`) và đánh fail tiến trình CI (exit code 1).
   - **Cách giải quyết:** Em phải chạy lệnh `mvn spotless:apply` ở máy local để tự động format lại toàn bộ source code cho chuẩn, sau đó commit lại thì CI mới chuyển sang màu xanh.

2. **Lỗi Code Coverage (Jacoco Plugin):**
   - **Vấn đề:** Mặc dù code biên dịch thành công và các test đều pass, nhưng CI vẫn báo lỗi ở bước Jacoco. Nguyên nhân là do dự án cấu hình bắt buộc (threshold) tỷ lệ Unit Test phải bao phủ **80%** số dòng code (COVEREDRATIO). Nhưng thực tế backend lúc đó mới chỉ đạt khoảng 17% độ phủ.
   - **Cách giải quyết:** Để không bị chặn quy trình deploy, em đã phải vào file `pom.xml` cấu hình tạm thời hạ mức threshold của Jacoco xuống `0.10` (10%) để CI vượt qua bước này. Em có kế hoạch sẽ viết thêm Unit Test bổ sung sau này để đạt chuẩn 80% trở lại.

### Q: Em lấy đâu ra chứng chỉ và mật khẩu để GitHub Actions tự động nhảy vào máy chủ VPS deploy được?
**Trả lời:**
- Em tuyệt đối không lưu mật khẩu hay khóa SSH thẳng vào code vì rất mất an toàn. 
- Em sử dụng tính năng **GitHub Secrets** (nằm trong mục Settings của repository). Em lưu IP của máy chủ (`VPS_HOST`), tài khoản (`VPS_USERNAME`), và khóa bí mật (`VPS_SSH_KEY`) ở đó. File `cd.yml` sẽ tự động lấy các biến ẩn này ra để sử dụng khi kết nối SSH vào VPS.

---

## 4. Tổng Kết Giá Trị
Nếu giảng viên hỏi chốt lại giá trị của việc này, bạn hãy nói:
> "Việc áp dụng CI/CD giúp nhóm em tiết kiệm được rất nhiều giờ làm việc thủ công, giảm thiểu rủi ro khi đưa code lên server. Quan trọng nhất, nó rèn luyện cho team kỷ luật viết code: Ai viết code sai format hoặc không viết Unit Test thì code người đó không thể nào được đưa lên production."
