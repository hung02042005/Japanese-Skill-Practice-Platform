# Playwright CLI Quick Reference

Đây là tài liệu ghi chú các lệnh kỹ năng (skills) sử dụng với Playwright CLI.

| Hoạt động | Chỉ huy |
| :--- | :--- |
| Cài đặt CLI | `npm install -g @playwright/cli@latest` |
| Cài đặt kỹ năng | `playwright-cli install --skills` |
| Mở một trang | `playwright-cli open https://example.com` |
| Nhấp vào một phần tử | `playwright-cli click e15` |
| Nhập văn bản | `playwright-cli type "hello world"` |
| Chụp ảnh màn hình | `playwright-cli screenshot` |
| Nhận ảnh chụp nhanh trang | `playwright-cli snapshot` |
| Chạy đầu (có giao diện) | `playwright-cli open https://example.com --headed` |
| Sử dụng Firefox | `playwright-cli open --browser=firefox` |
| Giám sát phiên | `playwright-cli show`
